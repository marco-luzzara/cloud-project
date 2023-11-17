package it.unimi.cloudproject.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    public static String getStackTraceAsString(Throwable throwable) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        return sw.toString();
    }
}
