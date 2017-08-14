package com.oxygenxml.sdksamples.svg;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

/**
 * SvgPlugin is a sample plug-in used to define vector-based graphics.
 * 
 * @author costi_dumitrescu
 * 
 */
public class SvgPlugin extends Plugin {

  /**
   * Plugin instance.
   */
  private static SvgPlugin instance = null;

  /**
   * Constructor.
   * 
   * @param descriptor Plugin descriptor.
   */
  public SvgPlugin(PluginDescriptor descriptor) {
    super(descriptor);

    if (instance != null) {
      throw new IllegalStateException("Already instantiated!");
    }
    instance = this;
  }

  /**
   * Get the plugin instance.
   * 
   * @return the shared plugin instance.
   */
  public static SvgPlugin getInstance() {
    return instance;
  }
}
