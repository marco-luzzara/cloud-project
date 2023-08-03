package it.unimi.cloudproject.ui.testcontainer;

import com.google.gson.Gson;
import it.unimi.cloudproject.application.dto.UserCreationRequest;
import it.unimi.cloudproject.application.dto.responses.UserCreationResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
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

    private static class JsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {

        private final Class<T> targetType;

        public JsonBodyHandler(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
            return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(),
                    this::parseJson);
        }

        private T parseJson(InputStream inputStream) {
            return gson.fromJson(new InputStreamReader(inputStream), targetType);
        }
    }
}
