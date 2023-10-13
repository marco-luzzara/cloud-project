package it.unimi.cloudproject.api.bodyhandlers;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class GeneralBodyHandler<T> implements HttpResponse.BodyHandler<T> {

    private static final Gson gson = new Gson();
    private final Map<Integer, Supplier<HttpResponse.BodySubscriber<?>>> bodyHandlers;

    public GeneralBodyHandler(Map<Integer, Supplier<HttpResponse.BodySubscriber<?>>> bodyHandlers) {
        this.bodyHandlers = bodyHandlers;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return (HttpResponse.BodySubscriber<T>) bodyHandlers.get(responseInfo.statusCode()).get();
    }

    public static <TResponse> HttpResponse.BodySubscriber<TResponse> getJsonBodyHandler(Class<TResponse> resultClass) {
        return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(),
                (is) -> gson.fromJson(new InputStreamReader(is), resultClass));
    }

    public static HttpResponse.BodySubscriber<Map<String, Object>> getJsonBodyHandler() {
        return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(),
                (is) -> (Map<String, Object>) gson.fromJson(new InputStreamReader(is), Map.class));
    }
}
