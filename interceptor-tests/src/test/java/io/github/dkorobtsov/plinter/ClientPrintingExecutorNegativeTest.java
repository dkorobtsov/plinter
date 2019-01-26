package io.github.dkorobtsov.plinter;

import static io.github.dkorobtsov.plinter.internal.Util.APPLICATION_JSON;
import static io.github.dkorobtsov.plinter.internal.Util.APPLICATION_ZIP;
import static io.github.dkorobtsov.plinter.internal.Util.CONTENT_TYPE;

import io.github.dkorobtsov.plinter.LoggerConfig;
import io.github.dkorobtsov.plinter.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.internal.InterceptedResponse;
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

    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder()
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

    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = LoggerConfig.builder()
        .executor(Executors.newCachedThreadPool()).build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printRequest(loggerConfig, request);
  }

  @Test
  public void testInterruptingPrintingResponseDoesntCrashProcess() {
    final InterceptedResponse responseDetails = InterceptedResponse.builder().build();
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder()
        .executor(Executors.newCachedThreadPool()).build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printResponse(loggerConfig, responseDetails);
  }

}
