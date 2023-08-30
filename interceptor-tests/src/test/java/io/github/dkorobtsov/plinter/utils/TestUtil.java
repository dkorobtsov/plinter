package io.github.dkorobtsov.plinter.utils;

import org.junit.Assert;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.fail;

/**
 * Collection of utility methods for use in tests.
 */
public final class TestUtil {

  public static final String PRINTING_THREAD_PREFIX = "Printer";
  private static final SecureRandom RANDOM = new SecureRandom();

  private TestUtil() {

  }

  /**
   * Generates random text of the specified length.
   *
   * @param length The length of the random text.
   * @return The random text.
   */
  @SuppressWarnings("PMD.AssignmentInOperand")
  public static String randomText(int length) {
    final StringBuilder text = new StringBuilder();

    // determines whether vowel(even) or consonant(odd)
    int which = RANDOM.nextInt(2);
    for (int i = 0; i < length; i++) {
      if (which++ % 2 == 0) {
        final String vowels = "aeiou";
        text.append(vowels.charAt(RANDOM.nextInt(vowels.length())));
      } else {
        final String consonants = "bcdfghjklmnpqrstvwxyz";
        text.append(consonants.charAt(RANDOM.nextInt(consonants.length())));
      }
    }
    return text.toString();
  }

  /**
   * Asserts that the entry starts with a parsable date stamp.
   *
   * @param rawEntry The raw entry to check.
   */
  public static void assertEntryStartsWithParsableDate(String rawEntry) {
    final String[] entryElements = TestUtil
      .extractTextFromLogEntrySeparatedByBrackets(rawEntry);

    try {
      new SimpleDateFormat("yyyy-MM-ddd kk:mm:ss", Locale.ENGLISH).parse(entryElements[0]);
    } catch (ParseException e) {
      fail("Log entry expected to start with parsable date stamp. But was: \n" + rawEntry);
    }
  }

  /**
   * Asserts the number of elements in a log entry separated by brackets.
   *
   * @param entrySeparatedByBrackets The log entry separated by brackets.
   * @param expectedCount            The expected number of elements.
   */
  public static void assertLogEntryElementsCount(String entrySeparatedByBrackets,
                                                 int expectedCount) {
    final String[] entryElements = TestUtil
      .extractTextFromLogEntrySeparatedByBrackets(entrySeparatedByBrackets);

    Assert.assertEquals(
      "Log event expected to contain " + expectedCount + " of elements. But was: \n"
        + entryElements.length, expectedCount, entryElements.length);
  }

  /**
   * Extracts text from a log entry separated by brackets.
   *
   * @param logEntry The log entry.
   * @return An array of extracted text elements.
   */
  @SuppressWarnings({"RegExpRedundantEscape", "RegExpSingleCharAlternation"})
  private static String[] extractTextFromLogEntrySeparatedByBrackets(String logEntry) {
    return Arrays
      .stream(logEntry.split("\\[|\\]"))
      .map(String::trim)
      .filter(trim -> !trim.isEmpty())
      .toArray(String[]::new);
  }

  /**
   * Returns the logging executor thread.
   *
   * @return The logging executor thread.
   * @throws AssertionError If the logging executor thread is not available.
   */
  public static Thread loggingExecutorThread() {
    return Thread.getAllStackTraces().keySet()
      .stream()
      .filter(it -> it.getName().startsWith(PRINTING_THREAD_PREFIX))
      .findFirst()
      .orElseThrow(() -> new AssertionError("Logging executor thread should be available."));
  }


}
