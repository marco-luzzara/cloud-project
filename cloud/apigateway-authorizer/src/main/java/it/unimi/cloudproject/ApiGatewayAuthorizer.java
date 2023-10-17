package it.unimi.cloudproject;

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.cloudproject.lambda.authorizer.errors.CannotAuthorizeRequest;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.iam.IamClient;
import com.auth0.jwt.JWT;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Map;
import java.util.function.Function;

// TODO: https://docs.spring.io/spring-cloud-function/docs/current/reference/html/aws.html
// to speed up cold starts
@SpringBootApplication
@ComponentScan(basePackages = "it.unimi.cloudproject")
public class ApiGatewayAuthorizer {

		public static void main(String[] args) {
			SpringApplication.run(ApiGatewayAuthorizer.class, args);
		}

		@Autowired
		private UserService userService;

		@Autowired
		private ShopService shopService;

    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

		@Bean
		public Function<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> authorize() {
				return (event) -> {
            var userPoolId = System.getProperty("aws.cognito.user_pool_id");

						var authToken = event.getHeaders().get("Authorization"); // .getAuthorizationToken() is not supported in Localstack
						var jwt = JWT.decode(authToken);
            var username = jwt.getClaim("cognito:username").asString();
            var principalId = jwt.getSubject();

            try (var iamClient = IamClient.builder().build(); var cognitoClient = CognitoIdentityProviderClient.create()) {
                var groups = AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.adminListGroupsForUser(b -> b.username(username).userPoolId(userPoolId)),
                        CannotAuthorizeRequest::new).groups();

                var groupRoleArn = groups.get(0).roleArn();
                var groupRoleName = groupRoleArn.substring(groupRoleArn.lastIndexOf("/") + 1);

                var inlinePolicy = AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> iamClient.getRole(b -> b.roleName(groupRoleName)),
                        CannotAuthorizeRequest::new).role().assumeRolePolicyDocument();

                IamPolicyResponse.PolicyDocument policyDocument = gson.fromJson(inlinePolicy, IamPolicyResponse.PolicyDocument.class);

                System.out.println("policyDocument = " + policyDocument.getVersion());
                return IamPolicyResponse.builder()
												.withContext(Map.of())
                        .withPolicyDocument(policyDocument)
                        .withPrincipalId(principalId).build();
						}
				};
		}
}
