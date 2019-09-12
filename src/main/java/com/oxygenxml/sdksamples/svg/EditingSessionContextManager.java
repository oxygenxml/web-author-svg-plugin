package com.oxygenxml.sdksamples.svg;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;

/**
 * Plugin that adds several editing context attributes.
 * 
 * @author cristi_talau
 */
public class EditingSessionContextManager {
  /**
   * The ID of the document model.
   */
  static final String DOCUMENT_MODEL_ID = "com.oxygenxml.sdksamples.svg.doc_id";
  
  /**
   * {@link EditingSessionContext} attribute that points to the SVG cache.
   */
  static final String SVG_CACHE = "com.oxygenxml.sdksamples.svg.cache";
  
  /**
   * Secure random generator.
   */
  private static final SecureRandom random = new SecureRandom();
  
  /**
   * The global cache of active documents.
   */
  private static final Cache<String, AuthorAccess> activeDocsCache = CacheBuilder.newBuilder()
      .weakValues()
      .build();

  /**
   * Sets up the editing context when the document is opened.
   * 
   * @param authorAccess The author access.
   */
  public static void ensureInitialized(AuthorAccess authorAccess) {
    EditingSessionContext editingContext = authorAccess.getEditorAccess().getEditingContext();
    if (editingContext.getAttribute(DOCUMENT_MODEL_ID) == null) {
      String docId = generateId();
      activeDocsCache.put(docId, authorAccess);
      editingContext.setAttribute(DOCUMENT_MODEL_ID, docId);
      editingContext.setAttribute(SVG_CACHE, new PerDocumentSvgCache(authorAccess.getDocumentController()));
    }
  }

  /**
   * @return A secure unique random ID.
   */
  private static String generateId() {
    byte[] bytes = new byte[20];
    random.nextBytes(bytes);
    return Hex.encodeHexString(bytes);
  }

  /**
   * Returns the document stored with the document ID.
   * 
   * @param docId The document ID.
   * 
   * @return The document author access.
   */
  public static AuthorAccess getDocument(String docId) {
    return activeDocsCache.getIfPresent(docId);
  }
}
