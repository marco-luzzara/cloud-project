package it.unimi.cloudproject;

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.cloudproject.lambda.authorizer.errors.CannotAuthorizeRequest;
import it.unimi.cloudproject.lambda.authorizer.errors.UnauthorizedUserForShopError;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import it.unimi.cloudproject.utilities.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.iam.IamClient;
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
            var principalId = jwt.getSubject();

            try {
                checkForCustomAuthorization(event, jwt);

                var username = jwt.getClaim("cognito:username").asString();

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
            }
            catch (Exception exc) {
//                throw exc;
                return IamPolicyResponse.builder()
                        .withContext(Map.of(
                                "errorMessage", exc.getMessage(),
                                "stackTrace", ExceptionUtils.getStackTraceAsString(exc)))
                        .withPolicyDocument(IamPolicyResponse.PolicyDocument.builder()
                                .withVersion("2012-10-17")
                                .withStatement(List.of(
                                        IamPolicyResponse.Statement.builder()
                                                .withAction("execute-api:Invoke")
                                                .withResource(List.of(event.getMethodArn()))
                                                .withEffect("Deny")
                                                .build()
                                ))
                                .build())
                        .withPrincipalId(principalId).build();
            }
				};
		}

    /**
     * Does some additional checks on the shopId specified. It makes sure that the request is called
     * by the owner of the shop
     * @param event
     * @param jwt
     */
    private void checkForCustomAuthorization(APIGatewayCustomAuthorizerEvent event, DecodedJWT jwt) {
        if (!event.getMethodArn().matches(".*(?:/DELETE/shops/\\d+|/POST/shops/\\d+/messages)"))
            return;

        var dbIdClaim = jwt.getClaim("custom:dbId");
        var userId = Integer.parseInt(dbIdClaim.asString());
        var shopId = Integer.parseInt(event.getPathParameters().get("shopId"));

        var shopInfo = this.shopService.findById(shopId);

        if (shopInfo.shopOwnerId() != userId)
            throw new UnauthorizedUserForShopError(userId, shopId);
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
