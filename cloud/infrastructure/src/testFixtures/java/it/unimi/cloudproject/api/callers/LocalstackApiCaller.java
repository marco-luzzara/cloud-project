package it.unimi.cloudproject.api.callers;

import com.google.gson.Gson;
import it.unimi.cloudproject.api.bodyhandlers.GeneralBodyHandler;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class LocalstackApiCaller {
    private final AppContainer appContainer;

    private static final Gson gson = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public LocalstackApiCaller(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public <TResponse> HttpResponse<TResponse> callGetEmailsApi(String receiverEmail) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .GET()
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildBaseUrl("_aws/ses"))
                        .build(),
                new GeneralBodyHandler<>(Map.of(200, GeneralBodyHandler::getJsonBodyHandler)));
    }
}
