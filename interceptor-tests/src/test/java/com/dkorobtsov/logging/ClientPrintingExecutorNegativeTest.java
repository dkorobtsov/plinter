package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_ZIP;
import static com.dkorobtsov.logging.internal.Util.CONTENT_TYPE;

import com.dkorobtsov.logging.internal.ClientPrintingExecutor;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import java.util.concurrent.Executors;
import org.junit.Test;

/**
 * Tests validating that printing thread interruption will not cause any unexpected exceptions.
 */
public class ClientPrintingExecutorNegativeTest {

  @Test
  public void testInterruptingPrintingJsonRequestDoesntCrashProcess() {
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
        .url("http://google.com")
        .build();

    final LoggerConfig loggerConfig = LoggerConfig.builder()
        .executor(Executors.newCachedThreadPool()).build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printRequest(loggerConfig, request);
  }

  @Test
  public void testInterruptingPrintingFileRequestDoesntCrashProcess() {
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_ZIP)
        .url("http://google.com")
        .build();

    final LoggerConfig loggerConfig = LoggerConfig.builder()
        .executor(Executors.newCachedThreadPool()).build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printRequest(loggerConfig, request);
  }

  @Test
  public void testInterruptingPrintingResponseDoesntCrashProcess() {
    final InterceptedResponse responseDetails = InterceptedResponse.builder().build();
    final LoggerConfig loggerConfig = LoggerConfig.builder()
        .executor(Executors.newCachedThreadPool()).build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printResponse(loggerConfig, responseDetails);
  }

}
