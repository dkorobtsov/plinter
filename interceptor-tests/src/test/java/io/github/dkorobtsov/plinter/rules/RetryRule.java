package io.github.dkorobtsov.plinter.rules;

import io.github.dkorobtsov.plinter.utils.Retry;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A rule for retrying a test a certain number of times as specified by the
 * {@link Retry} annotation. This is useful for tests that are failing due to
 * race conditions. While these race conditions ideally should be fixed, until
 * they pose any real problem, this rule can be used to retry such tests.
 *
 * <p>
 * To use this rule, annotate the test method with the {@link Retry} annotation
 * and specify the number of retries. If the test passes within the specified
 * number of retries, the test is considered as passed. If the test still fails
 * after the specified number of retries, the test is considered as failed.
 *
 * <p>
 * Note: This rule should not be used as a way to make inherently flaky tests
 * pass. It's a workaround, not a solution.
 *
 * <p>
 * Usage:
 * <pre>
 * {@code
 * @Rule
 * public RetryRule rule = new RetryRule();
 *
 * @Test
 * @Retry(3)
 *  public void myTest() {
 *      // Test code here
 *  }
 * }
 * </pre>
 */
@SuppressWarnings("PMD.AvoidCatchingThrowable") //by Design
public class RetryRule implements MethodRule {

  /**
   * Applies the RetryRule to the given test statement.
   * Retries the execution of the test statement based on the value
   * specified in the Retry annotation.
   *
   * @param base   the test statement to be retried
   * @param method the FrameworkMethod representing the test method
   * @param target the test instance on which the test method is being invoked
   * @return a new Statement that retries the execution of the test statement
   */
  @Override
  public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Retry retryAnnotation = method.getAnnotation(Retry.class);
        int retryCount = (retryAnnotation != null) ? retryAnnotation.value() : 0;

        for (int i = 0; i <= retryCount; i++) {
          try {
            base.evaluate();
            // If the test passes, break the loop
            break;
          } catch (Throwable t) {
            // If this was the last retry, rethrow the exception
            if (i >= retryCount) {
              throw t;
            }
            // If the test fails and it's not the last retry, continue the loop to retry
          }
        }
      }
    };
  }
}
