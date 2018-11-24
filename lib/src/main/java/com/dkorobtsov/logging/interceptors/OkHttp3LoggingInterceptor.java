package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.TextUtils.hasPrintableBody;

import com.dkorobtsov.logging.BodyUtils;
import com.dkorobtsov.logging.ClientPrintingExecutor;
import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttp3LoggingInterceptor extends AbstractOkHttpInterceptor implements Interceptor {

    public OkHttp3LoggingInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
        this.isLoggable = loggerConfig.isLoggable;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (skipLogging()) {
            return chain.proceed(request);
        }

        String subtype = BodyUtils.subtype(request);

        printRequest(loggerConfig, request, hasPrintableBody(subtype));

        final long startTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long responseTime = TimeUnit.NANOSECONDS
            .toMillis(System.nanoTime() - startTime);

        InterceptedResponse interceptedResponse = interceptedResponse(request, response,
            responseTime);

        ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);

        final ResponseBody body;
        if (interceptedResponse.hasPrintableBody) {
            body = ResponseBody
                .create(interceptedResponse.contentType, interceptedResponse.originalBody);
        } else {
            return response;
        }

        return response.newBuilder()
            .body(body)
            .build();
    }

}
