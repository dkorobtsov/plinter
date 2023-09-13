package io.github.dkorobtsov.tests.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Got flaky tests? Shampoo them away.
 * <p>
 * Usage:
 * <p>
 * {@code @Rule public final TestRule shampoo = new ShampooRule(1000);}
 */
public final class ShampooRule implements TestRule {
  private final int iterations;

  /**
   * Constructs a new ShampooRule with the specified number of iterations.
   *
   * @param iterations the number of times to repeat tests
   * @throws IllegalArgumentException if iterations is less than 1
   */
  public ShampooRule(int iterations) {
    if (iterations < 1) {
      throw new IllegalArgumentException("iterations < 1: " + iterations);
    }
    this.iterations = iterations;
  }

  /**
   * Applies the ShampooRule to the given test statement.
   * Repeats the execution of the test statement for the specified number of iterations.
   *
   * @param base        the test statement to be repeated
   * @param description a Description object describing the test statement
   * @return a new Statement that repeats the execution of the test statement
   */
  @SuppressWarnings("NullableProblems")
  @Override
  public Statement apply(final Statement base,
                         Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        for (int i = 0; i < iterations; i++) {
          base.evaluate();
        }
      }
    };
  }
}
