package it.unimi.cloudproject.ui.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import it.unimi.cloudproject.infrastructure.errors.Error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public abstract class GenericLambda<TInput, TOutput> implements RequestHandler<TInput, TOutput> {
    public abstract TOutput execute(TInput input, LambdaLogger logger);

    @Override
    public TOutput handleRequest(TInput input, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            return execute(input, logger);
        }
        catch (Error err) {
            logger.log(err.getMessage());
            throw err;
        }
        catch (Exception exc) {
            logger.log("%s \n %s".formatted(exc.getMessage(), getStackTrace(exc)));
            throw new IllegalStateException("Something wrong happened, contact the support if the problem persists");
        }
    }

    private String getStackTrace(Exception exc) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        exc.printStackTrace(pw);

        return sw.toString();
    }
}
