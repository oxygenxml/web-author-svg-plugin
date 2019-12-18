package com.oxygenxml.sdksamples.svg;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.net.MediaType;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;

/**
 * Svg servlet used to retrieve the svg file after conversion.
 */
public class SvgServlet extends WebappServletPluginExtension {
  /**
   * Returns the PNG image that corresponds to the mathml equation.
   * 
   * @param httpRequest The HTTP request.
   * @param httpResponse The HTTP response.
   */
  @Override
  public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
    if (isOpenedInBrowserTab(httpRequest)) {
      httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Load SVG using an <img> tag.");
    } else {
      // The hash of the current wanted image.
      String docId = httpRequest.getParameter("docId");
      String elemId = httpRequest.getParameter("elemId");
      
      AuthorAccess authorAccess = EditingSessionContextManager.getDocument(docId);
      if (authorAccess != null) {
        EditingSessionContext editingContext = authorAccess.getEditorAccess().getEditingContext();
        PerDocumentSvgCache svgCache = (PerDocumentSvgCache) editingContext.getAttribute(EditingSessionContextManager.SVG_CACHE);
        
        String xml = svgCache.getXmlFragment(Long.valueOf(elemId));
        
        // mime type, cache, image content
        httpResponse.setHeader("Cache-Control", "max-age=31536000");
        httpResponse.setHeader("Content-Type", MediaType.SVG_UTF_8.toString());
        httpResponse.setHeader("Vary", "Accept-Encoding");
        httpResponse.setHeader("Content-Security-Policy", "sandbox");
        ByteStreams.copy(new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8)), httpResponse.getOutputStream());
      } else {
        httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "SVG file was not found.");
      }
    }
  }
  
  /**
   * @param httpRequest The HTTP request.
   * @return <code>true</code> if the URL is for sure opened in a browser tab.
   */
  private boolean isOpenedInBrowserTab(HttpServletRequest httpRequest) {
    String accept = httpRequest.getHeader("Accept");
    return accept != null && accept.contains("text/html");
  }
      
  /**
   * The path where this servlet is mapped.
   */
  @Override
  public String getPath() {
    return "svg";
  }
}
