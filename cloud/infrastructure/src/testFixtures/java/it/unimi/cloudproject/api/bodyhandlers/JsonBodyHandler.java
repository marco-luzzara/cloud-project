package it.unimi.cloudproject.api.bodyhandlers;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;

public class JsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {

    private static Gson gson = new Gson();
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
