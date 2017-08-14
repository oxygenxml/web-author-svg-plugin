package com.oxygenxml.sdksamples.svg;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;

import com.google.common.io.Files;
import com.google.common.net.MediaType;

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
	  
    // The hash of the current wanted image.
	  String xmlSvgFragHash = httpRequest.getParameter("xmlSvgFragHash");
	  
	  // the svg file.
	  File outputfile = new File(SvgRenderer.cacheFolder, xmlSvgFragHash);
	  if (outputfile.exists()) {
	    // mime type, cache, image content
	    Files.copy(outputfile, httpResponse.getOutputStream());
	    httpResponse.setHeader("Cache-Control", "max-age=31536000");
	    httpResponse.setHeader("Content-Type", MediaType.SVG_UTF_8.toString());
	    httpResponse.setHeader("Vary", "Accept-Encoding");
	  } else {
	    httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "SVG file was not found.");
	  }
  }
      
  /**
   * The path where this servlet is mapped.
   */
  @Override
  public String getPath() {
    return "svg";
  }
}
