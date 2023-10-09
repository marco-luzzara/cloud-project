package it.unimi.cloudproject.api.callers;

import com.google.gson.Gson;
import it.unimi.cloudproject.api.bodyhandlers.GeneralBodyHandler;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserLoginRequest;
import it.unimi.cloudproject.lambda.customer.dto.responses.LoginResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserCreationResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserGetInfoResponse;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class CustomerApiCaller {
    private final AppContainer appContainer;

    private static final Gson gson = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public CustomerApiCaller(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public <TResponse> HttpResponse<TResponse> callUserCreateApi(UserCreationRequest userCreationRequest) throws IOException, InterruptedException {
        var strBody = gson.toJson(userCreationRequest, UserCreationRequest.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("users"))
                        .build(),
                new GeneralBodyHandler<>(Map.of(200, () -> GeneralBodyHandler.getJsonBodyHandler(UserCreationResponse.class))));
    }

    public <TResponse> HttpResponse<TResponse> callUserLoginApi(UserLoginRequest userLoginRequest) throws IOException, InterruptedException {
        var strBody = gson.toJson(userLoginRequest, UserLoginRequest.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("login"))
                        .build(),
                new GeneralBodyHandler<>(Map.of(200, () -> GeneralBodyHandler.getJsonBodyHandler(LoginResponse.class))));
    }

    public HttpResponse<String> callUserDeleteApi(String token) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .DELETE()
                        .timeout(Duration.ofSeconds(100))
                        .header("Authorization", token)
                        .uri(this.appContainer.buildApiUrl("users/me"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public <TResponse> HttpResponse<TResponse> callUserGetInfoApi(String authToken) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .GET()
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .header("Authorization", authToken)
                        .uri(this.appContainer.buildApiUrl("users/me"))
                        .build(),
                new GeneralBodyHandler<>(Map.of(200, () -> GeneralBodyHandler.getJsonBodyHandler(UserGetInfoResponse.class),
                        404, HttpResponse.BodySubscribers::discarding)));
    }

    public HttpResponse<String> callUserSubscribeToShopApi(String authToken,
                                                           int shopId) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .header("Authorization", authToken)
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("users/me/subscriptions/" + shopId))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
