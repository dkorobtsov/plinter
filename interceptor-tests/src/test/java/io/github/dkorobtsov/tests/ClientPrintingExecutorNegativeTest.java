package io.github.dkorobtsov.tests;

import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.core.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import io.github.dkorobtsov.tests.utils.TestLogger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_ZIP;
import static io.github.dkorobtsov.plinter.core.internal.Util.CONTENT_TYPE;

/**
 * Tests validating that printing thread interruption will not cause any unexpected exceptions.
 */
public class ClientPrintingExecutorNegativeTest extends BaseTest {

  private Map.Entry<ExecutorService, Thread> executorEntry;

  @After
  public void tearDown() {
    if (executorEntry != null) {
      executorEntry.getKey().shutdownNow();
      try {
        if (!executorEntry.getKey().awaitTermination(1, TimeUnit.SECONDS)) {
          System.err.println("Executor did not terminate in the specified time.");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  @Test
  public void testInterruptingPrintingJsonRequestDoesNotCrashProcess() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
      .get()
      .addHeader(CONTENT_TYPE, APPLICATION_JSON)
      .url("http://google.com")
      .build();

    executorEntry = loggingExecutor();
    final LoggerConfig loggerConfig = LoggerConfig
      .builder()
      .logger(testLogger)
      .executor(executorEntry.getKey())
      .build();

    assertThatThreadInterruptionDoesNotCauseCrashes(request, loggerConfig);
  }

  @Test
  public void testInterruptingPrintingFileRequestDoesNotCrashProcess() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
      .get()
      .addHeader(CONTENT_TYPE, APPLICATION_ZIP)
      .url("http://google.com")
      .build();

    executorEntry = loggingExecutor();
    final LoggerConfig loggerConfig = LoggerConfig
      .builder()
      .logger(testLogger)
      .executor(executorEntry.getKey())
      .build();

    assertThatThreadInterruptionDoesNotCauseCrashes(request, loggerConfig);
  }

  @Test
  public void testInterruptingPrintingResponseDoesNotCrashProcess() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder().build();
    
    executorEntry = loggingExecutor();
    final LoggerConfig loggerConfig = LoggerConfig
      .builder()
      .logger(testLogger)
      .executor(executorEntry.getKey())
      .build();

    assertThatThreadInterruptionDoesNotCauseCrashes(response, loggerConfig);
  }

  private void assertThatThreadInterruptionDoesNotCauseCrashes(InterceptedRequest request,
                                                               LoggerConfig loggerConfig) {
    ClientPrintingExecutor.printRequest(loggerConfig, request);
    final Thread thread = executorEntry.getValue();
    Assertions.assertThat(thread.isInterrupted()).isFalse();

    thread.interrupt();
    Assertions.assertThat(thread.isInterrupted()).isTrue();

    ClientPrintingExecutor.printRequest(loggerConfig, request);
  }

  private void assertThatThreadInterruptionDoesNotCauseCrashes(InterceptedResponse response,
                                                               LoggerConfig loggerConfig) {
    ClientPrintingExecutor.printResponse(loggerConfig, response);
    final Thread thread = executorEntry.getValue();
    Assertions.assertThat(thread.isInterrupted()).isFalse();

    thread.interrupt();
    Assertions.assertThat(thread.isInterrupted()).isTrue();

    ClientPrintingExecutor.printResponse(loggerConfig, response);
  }
}
