package io.jenkins.plugins.quietdown;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to check the status of Pipeline jobs during quietDown.
 * This class provides methods to check if jobs are paused by quietDown mode.
 */
@Extension
public class QuietDownStatusChecker {
    private static final Logger LOGGER = Logger.getLogger(QuietDownStatusChecker.class.getName());
    
    private static QuietDownStatusChecker instance;
    
    /**
     * Get the singleton instance of the QuietDownStatusChecker.
     *
     * @return The QuietDownStatusChecker instance
     */
    public static synchronized QuietDownStatusChecker getInstance() {
        if (instance == null) {
            instance = new QuietDownStatusChecker();
        }
        return instance;
    }
    
    /**
     * Check if all jobs are safe to proceed with deployment.
     * A job is considered safe if it's not running or if it's paused by quietDown mode.
     *
     * @return true if all jobs are safe, false otherwise
     */
    public boolean areAllJobsSafe() {
        Jenkins jenkins = Jenkins.get();
        
        if (!jenkins.isQuietingDown()) {
            return false; // Not in quietDown mode, so not safe
        }
        
        for (Job<?, ?> job : jenkins.getAllItems(Job.class)) {
            if (job instanceof WorkflowJob) {
                WorkflowJob workflowJob = (WorkflowJob) job;
                Run<?, ?> lastBuild = workflowJob.getLastBuild();
                
                if (lastBuild != null && lastBuild.isBuilding()) {
                    WorkflowRun run = (WorkflowRun) lastBuild;
                    if (!isPausedByQuietDown(run)) {
                        return false; // Found a running job that's not paused
                    }
                }
            }
        }
        
        return true; // All jobs are safe
    }
    
    /**
     * Get all jobs that are paused by quietDown mode.
     *
     * @return A list of jobs that are paused by quietDown mode
     */
    public List<Job<?, ?>> getPausedJobs() {
        Jenkins jenkins = Jenkins.get();
        
        if (!jenkins.isQuietingDown()) {
            return Collections.emptyList(); // Not in quietDown mode, so no paused jobs
        }
        
        List<Job<?, ?>> pausedJobs = new ArrayList<>();
        
        for (Job<?, ?> job : jenkins.getAllItems(Job.class)) {
            if (job instanceof WorkflowJob) {
                WorkflowJob workflowJob = (WorkflowJob) job;
                Run<?, ?> lastBuild = workflowJob.getLastBuild();
                
                if (lastBuild != null && lastBuild.isBuilding()) {
                    WorkflowRun run = (WorkflowRun) lastBuild;
                    if (isPausedByQuietDown(run)) {
                        pausedJobs.add(job);
                    }
                }
            }
        }
        
        return pausedJobs;
    }
    
    /**
     * Check if a WorkflowRun is paused by quietDown mode.
     *
     * @param run The WorkflowRun to check
     * @return true if the run is paused by quietDown mode, false otherwise
     */
    public boolean isPausedByQuietDown(WorkflowRun run) {
        try {
            CpsFlowExecution execution = (CpsFlowExecution) run.getExecution();
            
            if (execution == null) {
                return false;
            }
            
            CpsThreadGroup threadGroup = execution.getThreadGroup();
            
            if (threadGroup == null) {
                return false;
            }
            
            // Access the pausedByQuietMode field using reflection
            Field pausedByQuietModeField = CpsThreadGroup.class.getDeclaredField("pausedByQuietMode");
            pausedByQuietModeField.setAccessible(true);
            return (boolean) pausedByQuietModeField.get(threadGroup);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking if run is paused by quietDown mode", e);
            return false;
        }
    }
}
