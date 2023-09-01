package io.github.dkorobtsov.plinter.utils;

import io.github.dkorobtsov.plinter.rules.RetryRule;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to indicate that a test should be retried a certain number of times
 * if it fails. This can be useful for tests that are failing due to intermittent
 * issues such as race conditions.
 *
 * <p>
 * The value of this annotation represents the number of retries. If not specified,
 * the default value is 5.
 *
 * <p>
 * Note: This annotation requires the {@link RetryRule} to be added to the test
 * class to function correctly.
 *
 * <p>
 * Usage:
 * <pre>
 * {@code
 * @Rule
 * public RetryRule rule = new RetryRule();
 *
 * @Test
 * @Retry(10) // This test will be retried up to 10 times if it fails
 *    public void myTest() {
 *        // Test code here
 *    }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {

  /**
   * Specifies the number of retries for a test method.
   *
   * @return the number of retries, default is 5
   */
  int value() default 5;
}
