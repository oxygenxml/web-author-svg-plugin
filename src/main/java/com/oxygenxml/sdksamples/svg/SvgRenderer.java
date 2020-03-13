package com.oxygenxml.sdksamples.svg;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.webapp.formcontrols.WebappFormControlRenderer;

/**
 * Form control renderer that renders a SVG fragment as an image.
 *
 * @author costi_dumitrescu
 */
public class SvgRenderer extends WebappFormControlRenderer {
  /**
   * Logger
   */
  private static final Logger logger = LogManager.getLogger(SvgRenderer.class.getName());

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
    
    AuthorAccess authorAccess = context.getAuthorAccess();
    EditingSessionContextManager.ensureInitialized(authorAccess);
    EditingSessionContext editingContext = authorAccess.getEditorAccess().getEditingContext();
    
    PerDocumentSvgCache equationCache = 
        (PerDocumentSvgCache) editingContext.getAttribute(EditingSessionContextManager.SVG_CACHE);
    String docId = (String) editingContext.getAttribute(EditingSessionContextManager.AUTHOR_ACCESS_ID);

    try {
      long elemId = equationCache.freezeSvgFrag(svgElement);
      String xmlSvgFrag = equationCache.getXmlFragment(elemId);
      String svgHash = DigestUtils.shaHex(xmlSvgFrag);

      // The actual html fragment that is being sent to the browser. 
      out.append("<img class=\"svg-image\" src=\"../plugins-dispatcher/svg?"
          + "xmlSvgFragHash=" + svgHash + "&" 
          + "elemId=" + elemId + "&" 
          + "docId=" + docId + "\"></img>");
    } catch (Exception e) {
      logger.error(e, e);
      out.append("<span style=\"color: red\">Error rendering SVG image</span>");
    }
  }

  @Override
  public boolean isChangeTrackingAware() {
    return true;
  }

  /**
   * @return Returns the description of the renderer.
   */
  @Override
  public String getDescription() {
    return "Svg Form Control Renderer";
  }
}
