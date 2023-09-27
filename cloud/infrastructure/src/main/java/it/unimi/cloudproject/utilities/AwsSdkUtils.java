package it.unimi.cloudproject.utilities;

import it.unimi.cloudproject.infrastructure.errors.InternalException;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.exception.SdkException;

import java.util.function.Function;
import java.util.function.Supplier;

public class AwsSdkUtils {
    public static <Response extends SdkResponse> Response runSdkRequestAndAssertResult(Supplier<Response> fn,
                                                                                       Function<Throwable, InternalException> exc) {
        var response = fn.get();

        if (!response.sdkHttpResponse().isSuccessful())
            throw exc.apply(SdkException.builder().message("%d: %s".formatted(
                    response.sdkHttpResponse().statusCode(),
                    response.sdkHttpResponse().statusText().orElse(""))).build());

        return response;
    }
}
