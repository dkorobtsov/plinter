package io.github.dkorobtsov.plinter.rules;

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

  public ShampooRule(int iterations) {
    if (iterations < 1) throw new IllegalArgumentException("iterations < 1: " + iterations);
    this.iterations = iterations;
  }

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
