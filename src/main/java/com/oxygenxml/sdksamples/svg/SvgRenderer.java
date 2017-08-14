package com.oxygenxml.sdksamples.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.webapp.formcontrols.WebappFormControlRenderer;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.PrettyPrintException;

/**
 * Form control renderer that renders a SVG fragment as an image.
 *
 * @author costi_dumitrescu
 */
public class SvgRenderer extends WebappFormControlRenderer {
  /**
   * Logger
   */
  private static final Logger logger = Logger
      .getLogger(SvgRenderer.class.getName());

  /**
   * The location of the cache folder.
   */
  static File cacheFolder = null;
  
  /**
   * Sets the folder where to create temporary files.
   */
  static {
    String tempdir = System.getProperty("java.io.tmpdir");
    cacheFolder = new File(tempdir, "tmpsvg");
    cacheFolder.mkdirs();
  }

  /**
   * Render control.
   * 
   * @param context The context of the MathML form control.
   * @param out The output stream.
   * 
   * @throws IOException If the form control could not be redered.
   */
  @Override
  public void renderControl(AuthorInplaceContext context, Writer out)
      throws IOException {
    
    AuthorElement svgElement = context.getElem();
    AuthorDocumentController documentController = context.getAuthorAccess().getDocumentController();
    String systemID = documentController.getAuthorDocumentNode().getSystemID();
    
    try {
      AuthorDocumentFragment svgFrag = documentController.createDocumentFragment(svgElement, true);
      String xmlSvgFrag = documentController.serializeFragmentToXML(svgFrag);
      String xmlSvgFragPPed = this.formatAndIndentXmlFragment(xmlSvgFrag, systemID);
      
      // Create the svg file, saves it in the cache folder 
      // and then returns its name.
      String xmlSvgFragHash = createSvgFile(xmlSvgFragPPed);

      // The value of the 'src' attribute is the path of the svg servlet to
      // retrieve the image from server.
      String escapedXmlSvgFragPPed = PluginWorkspaceProvider.getPluginWorkspace()
          .getXMLUtilAccess().escapeAttributeValue(xmlSvgFragPPed);

      // The actual html fragment that is being sent to the browser. 
      out.append("<img class=\"svg-image\" src=\"../plugins-dispatcher/svg?xmlSvgFragHash="
          + xmlSvgFragHash + ".svg\" alt=\"" + escapedXmlSvgFragPPed + "\"></img>");
      
    } catch (Exception e) {
      logger.error(e, e);
      out.append("<span style=\"color: red\">Error rendering SVG image</span>");
    }
  }

  /**
   * Try to format and indent if possible.
   * 
   * @param xmlContent The content to format and indent
   * @param systemID The system Id of the document that the fragment belongs to.
   * 
   * @return A PP-ed version or the same content if not well formed.
   */
  String formatAndIndentXmlFragment(String xmlContent, String systemID) {
    String formattedContent = "";
    if (xmlContent != null) {
      formattedContent = xmlContent;
      try {
        formattedContent = PluginWorkspaceProvider.getPluginWorkspace()
            .getXMLUtilAccess()
            .prettyPrint(new StringReader(xmlContent), systemID);
      } catch (PrettyPrintException e1) {
        if (logger.isDebugEnabled()) {
          logger.debug("Content not in XML format");
        }
      }
    }
    return formattedContent;
  }

  /**
   * Creates the svg file in the temporary directory, and then 
   * returns the name of the file.
   * 
   * @param xmlSvgFrag The svg fragment as string content.
   * 
   * @return The filename which is derived from the content.
   * 
   * @throws NoSuchAlgorithmException If it fails.
   * @throws UnsupportedEncodingException If it fails.
   * @throws IOException If it fails.
   * @throws FileNotFoundException If it fails.
   */
  String createSvgFile(String xmlSvgFrag)
      throws NoSuchAlgorithmException, UnsupportedEncodingException,
      IOException, FileNotFoundException {

    MessageDigest cript = MessageDigest.getInstance("SHA-1");
    cript.reset();
    cript.update(xmlSvgFrag.getBytes("utf8"));
    String xmlSvgFragHash = new String(Hex.encodeHex(cript.digest()));

    BufferedWriter output = null;
    try {
        File file = new File(cacheFolder, xmlSvgFragHash + ".svg");
        output = new BufferedWriter(new FileWriter(file));
        output.write(xmlSvgFrag);
    } catch (IOException e ) {
        logger.error(e, e);
    } finally {
      if (output != null) {
        try{
            output.close();
        } catch(Exception e) {
        }
      }
    }
    return xmlSvgFragHash;
  }

  /**
   * @return Returns the description of the renderer.
   */
  @Override
  public String getDescription() {
    return "Svg Form Control Renderer";
  }
}
