package com.dkorobtsov.logging;

import com.dkorobtsov.logging.utils.TestLogger;
import com.dkorobtsov.logging.utils.TestUtil;
import org.junit.Assert;
import org.junit.Test;

public class FormattingTest {

  private final static String TEST_MESSAGE = "Test";

  @Test
  public void formatterTest_messageOnly_containsOneElement() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertLogEntryElementsCount(
        testLogger.lastFormattedEvent(true), 1);
  }

  @Test
  public void formatterTest_messageOnly_containsOnlyMessage() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    testLogger.log(TEST_MESSAGE);

    Assert.assertEquals("Logger output should contain message only",
        TEST_MESSAGE, testLogger.lastFormattedEvent(false));
  }

  @Test
  public void formatterTest_full_containsFourElements() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_FULL);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertLogEntryElementsCount(
        testLogger.lastFormattedEvent(true), 4);
  }

  @Test
  public void formatterTest_full_includesThreadName() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_FULL);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include thread name.",
        testLogger.lastFormattedEvent(true)
            .contains(Thread.currentThread().getName()));
  }

  @Test
  public void formatterTest_full_includesMessage() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_FULL);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include message text.",
        testLogger.lastFormattedEvent(true)
            .contains(TEST_MESSAGE));
  }

  @Test
  public void formatterTest_full_includesLevel() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_FULL);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include logging level.",
        testLogger.lastFormattedEvent(true)
            .contains("INFO"));
  }

  @Test
  public void formatterTest_full_startsWithDate() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_FULL);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertEntryStartsWithParsableDate(
        testLogger.lastFormattedEvent(true));
  }

  @Test
  public void formatterTest_dateMessage_containsTwoElements() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertLogEntryElementsCount(
        testLogger.lastFormattedEvent(true), 2);
  }

  @Test
  public void formatterTest_dateMessage_includesMessage() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include message text.",
        testLogger.lastFormattedEvent(true)
            .contains(TEST_MESSAGE));
  }

  @Test
  public void formatterTest_dateMessage_startsWithDate() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertEntryStartsWithParsableDate(
        testLogger.lastFormattedEvent(true));
  }

  @Test
  public void formatterTest_dateLevelMessage_containsThreeElements() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertLogEntryElementsCount(testLogger
        .lastFormattedEvent(true), 3);
  }

  @Test
  public void formatterTest_dateLevelMessage_includesMessage() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include message text.",
        testLogger.lastFormattedEvent(true)
            .contains(TEST_MESSAGE));
  }

  @Test
  public void formatterTest_dateLevelMessage_includesLevel() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include logging level.",
        testLogger.lastFormattedEvent(true)
            .contains("INFO"));
  }

  @Test
  public void formatterTest_dateLevelMessage_startsWithDate() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertEntryStartsWithParsableDate(
        testLogger.lastFormattedEvent(true));
  }

  @Test
  public void formatterTest_levelMessage_containsTwoElements() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertLogEntryElementsCount(
        testLogger.lastFormattedEvent(true), 2);
  }

  @Test
  public void formatterTest_levelMessage_includesMessage() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include message text.",
        testLogger.lastFormattedEvent(true)
            .contains(TEST_MESSAGE));
  }

  @Test
  public void formatterTest_levelMessage_containsLevel() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include logging level.",
        testLogger.lastFormattedEvent(true)
            .contains("INFO"));
  }

  @Test
  public void formatterTest_threadMessage_containsTwoElements() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_LEVEL_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    TestUtil.assertLogEntryElementsCount(
        testLogger.lastFormattedEvent(true), 2);
  }

  @Test
  public void formatterTest_threadMessage_includesMessage() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include message text.",
        testLogger.lastFormattedEvent(true)
            .contains(TEST_MESSAGE));
  }

  @Test
  public void formatterTest_threadMessage_containsThreadName() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

    testLogger.log(TEST_MESSAGE);

    Assert.assertTrue("Logger output should include thread name.",
        testLogger.lastFormattedEvent(true)
            .contains(Thread.currentThread().getName()));
  }

}
