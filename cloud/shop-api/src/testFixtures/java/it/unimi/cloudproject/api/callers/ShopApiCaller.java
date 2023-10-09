package it.unimi.cloudproject.api.callers;

import com.google.gson.Gson;
import it.unimi.cloudproject.api.callers.dto.ShopPublishMessageRequestBody;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ShopApiCaller {
    private final AppContainer appContainer;

    private static final Gson gson = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public ShopApiCaller(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public HttpResponse<String> callShopDeleteApi(String authToken, int shopId) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .DELETE()
                        .timeout(Duration.ofSeconds(100))
                        .header("Authorization", authToken)
                        .uri(this.appContainer.buildApiUrl("shops/" + shopId))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> callShopPublishMessageApi(ShopPublishMessageRequestBody shopSubscriptionRequestBody,
                                                          String authToken,
                                                          int shopId) throws IOException, InterruptedException {
        var strBody = gson.toJson(shopSubscriptionRequestBody, ShopPublishMessageRequestBody.class);
        return HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(strBody))
                        .header("Content-Type", "application/json")
                        .header("Authorization", authToken)
                        .timeout(Duration.ofSeconds(100))
                        .uri(this.appContainer.buildApiUrl("shops/%d/messages".formatted(shopId)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
