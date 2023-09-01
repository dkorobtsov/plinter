package io.github.dkorobtsov.plinter.core.internal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Helper class for formatting printable requests and responses bodies.
 */
@SuppressWarnings("LineLength")
final class BodyFormatter {

  private static final int JSON_INDENT = 3;

  private BodyFormatter() {
  }

  @SuppressWarnings({
    "PMD.AvoidLiteralsInIfCondition",
    "PMD.DataflowAnomalyAnalysis"
  }) //by design
  static String formattedBody(String printableBody) {
    String message;
    try {
      if (printableBody.trim().charAt(0) == '{') {
        message = formatAsJsonObject(printableBody);
      } else if (printableBody.trim().charAt(0) == '[') {
        message = formatAsJsonArray(printableBody);
      } else if (printableBody.trim().charAt(0) == '<') {
        message = formatAsXml(printableBody);
      } else {
        message = printableBody;
      }
    } catch (JSONException e) {
      message = printableBody;
    } catch (StringIndexOutOfBoundsException e) {
      message = "";
    }
    return message;
  }

  private static String formatAsJsonObject(String msg) {
    return new JSONObject(msg).toString(JSON_INDENT);
  }

  private static String formatAsJsonArray(String msg) {
    return new JSONArray(msg).toString(JSON_INDENT);
  }

  /**
   * Method for pretty printing XML content.
   * <p>
   * N.B. Default implementation was adjusted according to OWASP suggestions to mitigate possible
   * security risks. For detailed information check:
   *
   * @see <a href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J">XML
   * External Entity (XXE) Prevention</a>
   */
  private static String formatAsXml(String msg) {
    String feature;
    try {
      final InputSource src = new InputSource(new StringReader(msg));
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

      feature = "http://apache.org/xml/features/disallow-doctype-decl";
      dbf.setFeature(feature, true);

      feature = "http://xml.org/sax/features/external-general-entities";
      dbf.setFeature(feature, false);

      feature = "http://xml.org/sax/features/external-parameter-entities";
      dbf.setFeature(feature, false);

      feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
      dbf.setFeature(feature, false);

      dbf.setXIncludeAware(false);
      dbf.setExpandEntityReferences(false);

      final DocumentBuilder safeBuilder = dbf.newDocumentBuilder();

      final Node document = safeBuilder.parse(src).getDocumentElement();

      final boolean keepDeclaration = msg.startsWith("<?xml");

      final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      final DOMImplementationLS impl = (DOMImplementationLS) registry
        .getDOMImplementation("LS");

      final LSSerializer writer = impl.createLSSerializer();

      writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
      writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

      return writer.writeToString(document);

    } catch (IOException | InstantiationException | ParserConfigurationException
             | IllegalAccessException | SAXException | ClassNotFoundException e) {

      // If failed to parse document - just showing as is.
      return msg;
    }
  }
}
