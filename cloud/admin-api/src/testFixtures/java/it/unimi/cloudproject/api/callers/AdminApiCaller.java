package it.unimi.cloudproject.api.callers;

import com.google.gson.Gson;
import it.unimi.cloudproject.api.bodyhandlers.GeneralBodyHandler;
import it.unimi.cloudproject.api.callers.dto.ShopCreationBody;
import it.unimi.cloudproject.lambda.admin.dto.responses.ShopCreationResponse;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class AdminApiCaller {
    private final AppContainer appContainer;

    private static final Gson gson = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public AdminApiCaller(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public <TResponse> HttpResponse<TResponse> callAdminCreateShopApi(ShopCreationBody shopCreationBody, String authToken) throws IOException, InterruptedException {
        var strBody = gson.toJson(shopCreationBody, ShopCreationBody.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + authToken)
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("shops"))
                        .build(),
                new GeneralBodyHandler<>(Map.of(200, () -> GeneralBodyHandler.getJsonBodyHandler(ShopCreationResponse.class))));
    }
}
