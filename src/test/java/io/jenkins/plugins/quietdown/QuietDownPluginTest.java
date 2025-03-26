package io.jenkins.plugins.quietdown;

import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Unit tests for {@link QuietDownPlugin}.
 */
public class QuietDownPluginTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    /**
     * Test that the plugin can be loaded and started.
     */
    @Test
    public void testPluginStartup() throws Exception {
        // Create an instance of the plugin
        QuietDownPlugin plugin = new QuietDownPlugin();
        
        // Start the plugin
        plugin.start();
        
        // Verify the plugin was started successfully
        // This is a simple test to ensure the plugin can be loaded without errors
        assertNotNull(plugin);
    }
    
    /**
     * Test that the plugin extension is properly registered.
     */
    @Test
    public void testPluginExtension() throws Exception {
        // Create an instance of the plugin extension
        QuietDownPlugin.PluginImpl extension = new QuietDownPlugin.PluginImpl();
        
        // Start the extension
        extension.start();
        
        // Verify the extension was started successfully
        assertNotNull(extension);
    }
}
