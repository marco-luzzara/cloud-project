package it.unimi.cloudproject.infrastructure.extensions.spring.cloud.function.adapters.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.GsonBuilder;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.cloud.function.json.GsonMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class FunctionInvokerEnrichedWithHeaders extends FunctionInvoker {
    protected static final GsonMapper mapper = new GsonMapper(new Gson());

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        var mappedInput = mapper.fromJson(input, Map.class);
        System.out.println(mappedInput);
//        System.setProperty("spring.cloud.function.definition", this.)
        super.handleRequest(input, output, context);
    }
}
