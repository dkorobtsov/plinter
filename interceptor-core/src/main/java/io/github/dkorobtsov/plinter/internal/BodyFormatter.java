package io.github.dkorobtsov.plinter.internal;

import static io.github.dkorobtsov.plinter.internal.Util.UTF_8;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * Helper class for formatting printable requests and responses bodies.
 */
final class BodyFormatter {

  private static final int JSON_INDENT = 3;

  private BodyFormatter() {
  }

  static String formattedBody(final byte[] msg) {
    final String bodyAsString = new String(msg, UTF_8);
    String message;
    try {
      if (bodyAsString.trim().charAt(0) == '{') {
        message = formatAsJsonObject(bodyAsString);
      } else if (bodyAsString.trim().charAt(0) == '[') {
        message = formatAsJsonArray(bodyAsString);
      } else if (bodyAsString.trim().charAt(0) == '<') {
        message = formatAsXml(bodyAsString);
      } else {
        message = bodyAsString;
      }
    } catch (JSONException e) {
      message = bodyAsString;
    }
    return message;
  }

  private static String formatAsJsonObject(String msg) {
    return new JSONObject(msg).toString(JSON_INDENT);
  }

  private static String formatAsJsonArray(String msg) {
    return new JSONArray(msg).toString(JSON_INDENT);
  }

  @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "By design.")
  private static String formatAsXml(String msg) {
    try {
      final InputSource src = new InputSource(new StringReader(msg));
      final Node document = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder().parse(src).getDocumentElement();
      final boolean keepDeclaration = msg.startsWith("<?xml");

      final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      final DOMImplementationLS impl = (DOMImplementationLS) registry
          .getDOMImplementation("LS");
      final LSSerializer writer = impl.createLSSerializer();

      writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
      writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

      return writer.writeToString(document);
    } catch (Exception e) {
      //If failed to parse - just showing as is.
      return msg;
    }
  }
}
