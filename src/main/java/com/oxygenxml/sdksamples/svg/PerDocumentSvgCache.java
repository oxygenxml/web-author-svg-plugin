package com.oxygenxml.sdksamples.svg;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Cache of equation descriptors per opened document.
 * 
 * @author cristi_talau
 */
public class PerDocumentSvgCache {

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
  
  Map<AuthorElement, Long> nodeIndexer = new WeakHashMap<>();
  
  private long counter = 0;

  /**
   * Constructor.
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
    
    AuthorDocumentFragment mathMlFrag = docController
        .createDocumentFragment(elem, true);
    String xml = docController.serializeFragmentToXML(mathMlFrag);
    
    svgElements.put(elemId, xml);
    if (svgElements.size() > 2 * lastCompactedCacheSize) {
      compactCache();
      lastCompactedCacheSize = svgElements.size();
    }
    return elemId;
  }
  
  /**
   * Compact the cache, removing entries that correspond to stale AuthorElements.
   */
  @SuppressWarnings("unlikely-arg-type")
  private void compactCache() {
    svgElements.entrySet().removeIf(entry -> !nodeIndexer.containsKey(entry.getKey()));
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
