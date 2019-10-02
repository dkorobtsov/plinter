package io.github.dkorobtsov.plinter;

import io.github.dkorobtsov.plinter.core.internal.Util;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests to validate that core utility methods are working as expected.
 */
public class CoreUtilsUnitTest extends BaseTest {

  @Test
  public void testNoPathSegmentsReturnedOnUrlWithNoPath() throws MalformedURLException {
    final List<String> noSlashInTheEndSegments = Util
        .encodedPathSegments(new URL("https://google.com"));

    Assert.assertTrue("There should be no segments", noSlashInTheEndSegments.isEmpty());
  }

  @Test
  public void testNoPathSegmentsWithSlashOnlyAfterDomain() throws MalformedURLException {
    final List<String> emptyPathSegment = Util
        .encodedPathSegments(new URL("https://google.com/"));
    final List<String> expectedPathSegments = Collections.singletonList("");

    Assert.assertEquals(emptyPathSegment, expectedPathSegments);
  }

  @Test
  public void testPathSegmentsReturnedOnUrlWithPath() throws MalformedURLException {
    final List<String> segmentsWithPath = Util
        .encodedPathSegments(new URL("https://google.com/api/dev"));
    final List<String> expectedPathSegments = Arrays.asList("api", "dev");

    Assert.assertEquals(segmentsWithPath, expectedPathSegments);
  }

}
