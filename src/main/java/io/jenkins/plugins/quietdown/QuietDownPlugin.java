package io.jenkins.plugins.quietdown;

import hudson.Extension;
import hudson.Plugin;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup;

import java.util.logging.Logger;

/**
 * Main plugin class for the Jenkins QuietDown Plugin.
 * This plugin provides functionality to detect and manage Pipeline jobs
 * that are in the "Pausing (Preparing for shutdown)" state during quietDown.
 */
public class QuietDownPlugin extends Plugin {
    private static final Logger LOGGER = Logger.getLogger(QuietDownPlugin.class.getName());

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting Jenkins QuietDown Plugin");
        super.start();
    }

    /**
     * Extension point to register the plugin with Jenkins.
     */
    @Extension
    public static class PluginImpl extends Plugin {
        @Override
        public void start() throws Exception {
            LOGGER.info("Initializing Jenkins QuietDown Plugin");
            super.start();
        }
    }
}
