package it.unimi.cloudproject.ui.testcontainer;

import com.google.gson.Gson;
import it.unimi.cloudproject.application.dto.UserCreation;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class LocalstackRestApiCaller {
    private final AppContainer appContainer;

    private static Gson gson = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public LocalstackRestApiCaller(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public HttpResponse<String> callUserCreateApi(UserCreation userCreation) throws IOException, InterruptedException {
        var strBody = gson.toJson(userCreation, UserCreation.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("users"))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
