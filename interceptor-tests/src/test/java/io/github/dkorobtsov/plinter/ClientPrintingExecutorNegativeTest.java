package io.github.dkorobtsov.plinter;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_ZIP;
import static io.github.dkorobtsov.plinter.core.internal.Util.CONTENT_TYPE;

import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.core.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import io.github.dkorobtsov.plinter.utils.TestLogger;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests validating that printing thread interruption will not cause any unexpected exceptions.
 */
public class ClientPrintingExecutorNegativeTest {

  @Test
  public void testInterruptingPrintingJsonRequestDoesntCrashProcess() {
    TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
        .url("http://google.com")
        .build();

    final LoggerConfig loggerConfig = LoggerConfig
        .builder()
        .logger(testLogger)
        .executor(Executors.newCachedThreadPool())
        .build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printRequest(loggerConfig, request);

    Assertions
        .assertThat(testLogger.formattedOutput())
        .isNotEmpty();
  }

  @Test
  public void testInterruptingPrintingFileRequestDoesntCrashProcess() {
    TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_ZIP)
        .url("http://google.com")
        .build();

    final LoggerConfig loggerConfig = LoggerConfig
        .builder()
        .logger(testLogger)
        .executor(Executors.newCachedThreadPool())
        .build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printRequest(loggerConfig, request);

    Assertions
        .assertThat(testLogger.formattedOutput())
        .isNotEmpty();
  }

  @Test
  public void testInterruptingPrintingResponseDoesntCrashProcess() {
    TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse responseDetails = InterceptedResponse.builder().build();
    final LoggerConfig loggerConfig = LoggerConfig
        .builder()
        .logger(testLogger)
        .executor(Executors.newCachedThreadPool())
        .build();

    Thread.currentThread().interrupt();
    ClientPrintingExecutor.printResponse(loggerConfig, responseDetails);

    Assertions
        .assertThat(testLogger.formattedOutput())
        .isNotEmpty();
  }

}
