package com.oxygenxml.sdksamples.svg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Cache of equation descriptors per opened document.
 * 
 * @author cristi_talau
 */
public class PerDocumentSvgCache {
  
  private static final String XMLNS_SVG_NAMESPACE = "xmlns:svg";
  
  private static final String XMLNS_NAMESPACE = "xmlns";

  /**
   * The document controller.
   */
  private final AuthorDocumentController docController;
  
  /**
   * Map from node identifiers to SVG fragments. 
   */
  private final Map<Long, String> svgElements = new HashMap<>(0);

  /**
   * The cache size after the last compaction.
   */
  private long lastCompactedCacheSize = 4L;
  
  /**
   * The nodes indexer.
   */
  Map<AuthorElement, Long> nodeIndexer = new WeakHashMap<>();
  
  /**
   * counter used for indexing nodes.
   */
  private long counter = 0;

  /**
   * Constructor.
   * 
   * @param controller the author document controller.
   */
  public PerDocumentSvgCache(AuthorDocumentController controller) {
    this.docController = controller;
  }

  /**
   * Freezes the XML content that corresponds to the given element.
   * 
   * @param elem The author element.
   * 
   * @return The id of the cache entry.
   * 
   * @throws BadLocationException
   */
  public synchronized long freezeSvgFrag(AuthorElement elem) throws BadLocationException {
    long elemId = nodeIndexer.computeIfAbsent(elem, new Function<AuthorElement, Long>() {
      @Override
      public Long apply(AuthorElement t) {
        return counter++;
      }
    });
    
    AuthorDocumentFragment svgFrag = docController.createDocumentFragment(elem, true);
    
    // Browsers need the 'svg' namespace defined.
    List<AuthorNode> contentNodes = svgFrag.getContentNodes();
    if(!contentNodes.isEmpty()) {
      AuthorNode copyNode = contentNodes.get(0);
      if (copyNode instanceof AuthorElement) {
        AuthorElement copyElem = (AuthorElement) copyNode;
        
        AttrValue xmlnsSvg = copyElem.getAttribute(XMLNS_SVG_NAMESPACE);
        
        if(xmlnsSvg == null || !xmlnsSvg.isSpecified()) {
          xmlnsSvg = copyElem.getAttribute(XMLNS_NAMESPACE);
        }
        
        String namespaceValue = null;
        if(xmlnsSvg != null && !xmlnsSvg.isSpecified()) {
          namespaceValue = xmlnsSvg.getValue();
        } else {
          namespaceValue = "http://www.w3.org/2000/svg";
        }
        
        copyElem.setAttribute(XMLNS_SVG_NAMESPACE, new AttrValue(namespaceValue));
      }
      
      /*AuthorNode */copyNode = contentNodes.get(0);
      if (copyNode instanceof AuthorElement) {
        AuthorElement copyElem = (AuthorElement) copyNode;
        AttrValue xmlnsSvg = copyElem.getAttribute(XMLNS_SVG_NAMESPACE);
        if(xmlnsSvg != null && !xmlnsSvg.isSpecified()) {
          String namespaceValue = xmlnsSvg.getValue();
          copyElem.setAttribute(XMLNS_SVG_NAMESPACE, new AttrValue(namespaceValue));
        }
      }
    }
    String xml = docController.serializeFragmentToXML(svgFrag);
    svgElements.put(elemId, xml);
    if (svgElements.size() > 2 * lastCompactedCacheSize) {
      compactCache();
    }
    return elemId;
  }
  
  /**
   * Compact the cache, removing entries that correspond to stale AuthorElements.
   */
  private void compactCache() {
    HashSet<Long> valuesSet = new HashSet<>(nodeIndexer.values());
    
    svgElements.entrySet().removeIf(entry -> !valuesSet.contains(entry.getKey()));
    
    lastCompactedCacheSize = svgElements.size();
  }
  
  /**
   * @return The size of the cache.
   */
  int getSize() {
    return svgElements.size();
  }
  
  /**
   * The XML fragment of the given node.
   * 
   * @param elemId
   * @return The XML fragment that corresponds to the given element
   */
  public synchronized String getXmlFragment(long elemId) {
    return svgElements.get(elemId);
  }
}
