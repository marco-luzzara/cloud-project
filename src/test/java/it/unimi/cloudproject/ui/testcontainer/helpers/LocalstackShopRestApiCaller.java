package it.unimi.cloudproject.ui.testcontainer.helpers;

import com.google.gson.Gson;
import it.unimi.cloudproject.application.dto.ShopInfo;
import it.unimi.cloudproject.ui.dto.requests.shop.ShopCreationRequest;
import it.unimi.cloudproject.ui.dto.responses.shop.ShopCreationResponse;
import it.unimi.cloudproject.ui.testcontainer.AppContainer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LocalstackShopRestApiCaller {
    private final AppContainer appContainer;

    private static Gson gson = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public LocalstackShopRestApiCaller(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public HttpResponse<ShopCreationResponse> callShopCreateApi(ShopCreationRequest shopCreationRequest) throws IOException, InterruptedException {
        var strBody = gson.toJson(shopCreationRequest, ShopCreationRequest.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("shops"))
                        .build(),
                new JsonBodyHandler<>(ShopCreationResponse.class));
    }

    public HttpResponse<String> callShopDeleteApi(int id) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .DELETE()
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("shops/%d".formatted(id)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<ShopInfo> callShopGetApi(int shopId) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .GET()
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("shops/%d".formatted(shopId)))
                        .build(),
                new JsonBodyHandler<>(ShopInfo.class));
    }
}
