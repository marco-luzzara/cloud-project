package it.unimi.cloudproject.helpers;

import com.google.gson.Gson;
import it.unimi.cloudproject.services.dto.UserInfo;
import it.unimi.cloudproject.lambda.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.lambda.dto.responses.user.UserCreationResponse;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;
import it.unimi.cloudproject.api.JsonBodyHandler;

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

    public HttpResponse<String> callUserDeleteApi(int id) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .DELETE()
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("users/%d".formatted(id)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<UserInfo> callUserGetApi(int userId) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .GET()
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("users/%d".formatted(userId)))
                        .build(),
                new JsonBodyHandler<>(UserInfo.class));
    }
}
