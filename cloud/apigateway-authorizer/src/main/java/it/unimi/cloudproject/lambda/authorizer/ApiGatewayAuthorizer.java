package it.unimi.cloudproject.lambda.authorizer;

public class ApiGatewayAuthorizer {
}

//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerResponse;
//import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
//import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
//import com.amazonaws.services.cognitoidp.model.*;
//
//public class ApiGatewayAuthorizer implements RequestHandler<APIGatewayCustomAuthorizerEvent, Response> {
//
//    @Override
//    public APIGatewayCustomAuthorizerResponse handleRequest(APIGatewayCustomAuthorizerEvent event, Context context) {
//        String accessToken = event.getAuthorizationToken();
//
//        // Initialize Cognito Identity Provider client
//        AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard().build();
//
//        // Decode the access token and extract user attributes (e.g., user group)
//        // You'll need to implement this part based on your token structure
//        // Example: Decode JWT token, extract user group
//
//        // Check user's group membership and build IAM policy
//        // Allow access only if the user is in "group1"
//        String userGroup = ""; // Get user's group
//        String methodName = event.getMethodArn();
//        String effect = "Deny"; // Default to Deny
//
//        if ("group1".equals(userGroup)) {
//            effect = "Allow";
//        }
//
//        String policy = buildPolicy(methodName, effect);
//
//        // Construct the response
//        APIGatewayCustomAuthorizerResponse response = new APIGatewayCustomAuthorizerResponse();
//        response.setPrincipalID(userId);
//        response.setPolicyDocument(policy);
//
//        return response;
//    }
//
//    private String buildPolicy(String methodName, String effect) {
//        String policy = "{\n" +
//                "    \"Version\": \"2012-10-17\",\n" +
//                "    \"Statement\": [\n" +
//                "        {\n" +
//                "            \"Action\": \"execute-api:Invoke\",\n" +
//                "            \"Effect\": \"" + effect + "\",\n" +
//                "            \"Resource\": \"" + methodName + "\"\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";
//        return policy;
//    }
//}