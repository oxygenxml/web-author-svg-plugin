package com.oxygenxml.sdksamples.svg;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;

/**
 * Cache of equation descriptors per opened document.
 * 
 * @author cristi_talau
 */
public class PerDocumentSvgCache {

  /**
   * The document model.
   */
  private final AuthorDocumentModel docModel;
  
  /**
   * Map from node identifiers to SVG fragments. 
   */
  private final Map<Long, String> svgElements = new HashMap<>(0);

  /**
   * The cache size after the last compaction.
   */
  private long lastCompactedCacheSize = 4L;

  /**
   * Constructor.
   */
  public PerDocumentSvgCache(AuthorDocumentModel docModel) {
    this.docModel = docModel;
  }

  /**
   * Freezes the XML content that corresponds to the given element.
   * 
   * @param elem The author element.
   * @return The id of the cache entry.
   * 
   * @throws BadLocationException
   */
  public synchronized long freezeSvgFrag(AuthorElement elem) throws BadLocationException {
    long elemId = docModel.getNodeIndexer().getId(elem);
    
    AuthorDocumentController documentController = docModel.getAuthorDocumentController();
    AuthorDocumentFragment svgFrag = documentController
        .createDocumentFragment(elem, true);
    String xml = documentController.serializeFragmentToXML(svgFrag);
    
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
  private void compactCache() {
    svgElements.entrySet().removeIf(
        entry -> docModel.getNodeIndexer().getObjectById(entry.getKey()) == null);
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
