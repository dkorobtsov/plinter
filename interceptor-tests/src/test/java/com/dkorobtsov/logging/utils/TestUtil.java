package com.dkorobtsov.logging.utils;

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import org.junit.Assert;

public final class TestUtil {

  public static final String PRINTING_THREAD_PREFIX = "Printer";

  private TestUtil() {

  }

  public static void assertEntryStartsWithParsableDate(String rawEntry) {
    final String[] entryElements = TestUtil
        .extractTextFromLogEntrySeparatedByBrackets(rawEntry);

    try {
      new SimpleDateFormat("yyyy-MM-ddd kk:mm:ss", Locale.ENGLISH).parse(entryElements[0]);
    } catch (ParseException e) {
      fail("Log entry expected to start with parsable date stamp. But was: \n" + rawEntry);
    }
  }

  @SuppressWarnings({"RegExpRedundantEscape", "RegExpSingleCharAlternation"})
  static String[] extractTextFromLogEntrySeparatedByBrackets(String logEntry) {
    return Arrays
        .stream(logEntry.split("\\[|\\]"))
        .filter(s -> s.trim().length() > 0)
        .map(String::trim)
        .toArray(String[]::new);
  }

  public static void assertLogEntryElementsCount(String entrySeparatedByBrackets,
      int expectedCount) {
    final String[] entryElements = TestUtil
        .extractTextFromLogEntrySeparatedByBrackets(entrySeparatedByBrackets);

    Assert.assertEquals(
        "Log event expected to contain " + expectedCount + " of elements. But was: \n"
            + entryElements.length, expectedCount, entryElements.length);
  }

}
