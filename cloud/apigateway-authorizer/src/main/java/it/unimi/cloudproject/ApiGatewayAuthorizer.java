package it.unimi.cloudproject;

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import software.amazon.awssdk.services.iam.model.ListRolePoliciesRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

		private static final System.Logger LOGGER = System.getLogger(ApiGatewayAuthorizer.class.getName());
    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

		@Bean
		public Function<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> authorize() {
				return (event) -> {
            var userPoolId = System.getProperty("aws.cognito.user_pool_id");

						// .getAuthorizationToken() is not supported in Localstack, so I have to extract the token
						var authToken = event.getHeaders().get("Authorization").substring("Bearer ".length());
						var jwt = JWT.decode(authToken);
            var username = jwt.getClaim("cognito:username").asString();
            var principalId = jwt.getSubject();

            try (var iamClient = IamClient.builder().build(); var cognitoClient = CognitoIdentityProviderClient.create()) {
                var groups = AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.adminListGroupsForUser(b -> b.username(username).userPoolId(userPoolId)),
                        CannotAuthorizeRequest::new).groups();

                List<IamPolicyResponse.PolicyDocument> policyDocuments = new ArrayList<>();
                for (var group : groups) {
                    var groupRoleArn = group.roleArn();
                    var groupRoleName = groupRoleArn.substring(groupRoleArn.lastIndexOf("/") + 1);

                    var policyNames = AwsSdkUtils.runSdkRequestAndAssertResult(
                            () -> iamClient.listRolePolicies(ListRolePoliciesRequest.builder()
                                    .roleName(groupRoleName)
                                    .build()),
                            CannotAuthorizeRequest::new).policyNames();
                    for (var policyName : policyNames) {
                        var inlinePolicy = AwsSdkUtils.runSdkRequestAndAssertResult(
                                () -> iamClient.getRolePolicy(b -> b.roleName(groupRoleName).policyName(policyName)),
                                CannotAuthorizeRequest::new).policyDocument();
                        policyDocuments.add(gson.fromJson(inlinePolicy, IamPolicyResponse.PolicyDocument.class));
                    }
                }

                return IamPolicyResponse.builder()
												.withContext(getContextMap(jwt))
                        .withPolicyDocument(getMergedPolicyDocuments(policyDocuments))
                        .withPrincipalId(principalId).build();
						}
				};
		}

    private IamPolicyResponse.PolicyDocument getMergedPolicyDocuments(List<IamPolicyResponse.PolicyDocument> policyDocuments) {
        if (policyDocuments.isEmpty())
            // a policy with an empty statement means that access to any resources is implicitly denied
            return IamPolicyResponse.PolicyDocument.builder().withVersion("2012-10-17")
                    .withStatement(List.of())
                    .build();

        var allStatements = policyDocuments.stream().flatMap(pd -> pd.getStatement().stream()).collect(Collectors.toList());
        return IamPolicyResponse.PolicyDocument.builder()
                .withVersion(policyDocuments.get(0).getVersion())
                .withStatement(allStatements)
                .build();
    }

		private Map<String, Object> getContextMap(DecodedJWT jwt) {
				var username = jwt.getClaim("cognito:username").asString();
				var dbIdClaim = jwt.getClaim("custom:dbId");

				var contextMap = new HashMap<String, Object>();
				if (!dbIdClaim.isMissing())
            contextMap.put("dbId", dbIdClaim.asString());

        contextMap.put("username", username);

        return contextMap;
		}
}
