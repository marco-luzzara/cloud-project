package it.unimi.cloudproject.infrastructure.utilities;

import it.unimi.cloudproject.infrastructure.errors.Error;
import it.unimi.cloudproject.ui.errors.user.RegistrationFailedError;
import software.amazon.awssdk.core.SdkResponse;

import java.util.function.Supplier;

public class AwsUtils {
    public static <Response extends SdkResponse> Response runSdkRequestAndAssertResult(Supplier<Response> fn,
                                                                                       Supplier<Error> exc) {
        var response = fn.get();

        if (!response.sdkHttpResponse().isSuccessful())
            throw exc.get();

        return response;
    }
}
