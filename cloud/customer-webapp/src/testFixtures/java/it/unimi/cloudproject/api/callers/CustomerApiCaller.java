package it.unimi.cloudproject.api.callers;

import com.google.gson.Gson;
import it.unimi.cloudproject.api.bodyhandlers.JsonBodyHandler;
import it.unimi.cloudproject.lambda.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.lambda.dto.requests.user.UserLoginRequest;
import it.unimi.cloudproject.lambda.dto.responses.user.LoginResponse;
import it.unimi.cloudproject.lambda.dto.responses.user.UserCreationResponse;
import it.unimi.cloudproject.lambda.dto.responses.user.UserGetInfoResponse;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CustomerApiCaller {
    private final AppContainer appContainer;

    private static Gson gson = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public CustomerApiCaller(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public HttpResponse<UserCreationResponse> callUserCreateApi(UserCreationRequest userCreationRequest) throws IOException, InterruptedException {
        var strBody = gson.toJson(userCreationRequest, UserCreationRequest.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("users"))
                        .build(),
                new JsonBodyHandler<>(UserCreationResponse.class));
    }

    public HttpResponse<LoginResponse> callUserLoginApi(UserLoginRequest userLoginRequest) throws IOException, InterruptedException {
        var strBody = gson.toJson(userLoginRequest, UserLoginRequest.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("login"))
                        .build(),
                new JsonBodyHandler<>(LoginResponse.class));
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

    public HttpResponse<UserGetInfoResponse> callUserGetInfoApi(String token) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .GET()
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .header("Authorization", token)
                        .uri(this.appContainer.buildApiUrl("users/me"))
                        .build(),
                new JsonBodyHandler<>(UserGetInfoResponse.class));
    }
}
