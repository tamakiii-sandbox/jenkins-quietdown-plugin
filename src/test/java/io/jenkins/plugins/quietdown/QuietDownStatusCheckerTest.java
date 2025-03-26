package io.jenkins.plugins.quietdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link QuietDownStatusChecker}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, QuietDownStatusChecker.class, CpsFlowExecution.class, CpsThreadGroup.class})
@PowerMockIgnore({"javax.management.*", "javax.xml.*", "org.xml.*", "org.w3c.*"})
public class QuietDownStatusCheckerTest {
    
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    private Jenkins jenkins;
    private QuietDownStatusChecker checker;
    
    @Before
    public void setUp() {
        jenkins = mock(Jenkins.class);
        PowerMockito.mockStatic(Jenkins.class);
        when(Jenkins.get()).thenReturn(jenkins);
        
        checker = QuietDownStatusChecker.getInstance();
    }
    
    /**
     * Test that the singleton instance is properly created.
     */
    @Test
    public void testGetInstance() {
        QuietDownStatusChecker instance = QuietDownStatusChecker.getInstance();
        assertNotNull(instance);
        assertEquals(instance, QuietDownStatusChecker.getInstance());
    }
    
    /**
     * Test areAllJobsSafe when Jenkins is not in quietDown mode.
     */
    @Test
    public void testAreAllJobsSafeWhenNotQuieting() {
        when(jenkins.isQuietingDown()).thenReturn(false);
        
        assertFalse(checker.areAllJobsSafe());
    }
    
    /**
     * Test areAllJobsSafe when Jenkins is in quietDown mode and all jobs are safe.
     */
    @Test
    public void testAreAllJobsSafeWhenAllJobsAreSafe() {
        when(jenkins.isQuietingDown()).thenReturn(true);
        when(jenkins.getAllItems(Job.class)).thenReturn(Collections.emptyList());
        
        assertTrue(checker.areAllJobsSafe());
    }
    
    /**
     * Test areAllJobsSafe when Jenkins is in quietDown mode and some jobs are not safe.
     */
    @Test
    public void testAreAllJobsSafeWhenSomeJobsAreNotSafe() throws Exception {
        when(jenkins.isQuietingDown()).thenReturn(true);
        
        // Create a mock WorkflowJob and WorkflowRun
        WorkflowJob job = mock(WorkflowJob.class);
        WorkflowRun run = mock(WorkflowRun.class);
        
        // Set up the job to return the run
        when(job.getLastBuild()).thenReturn(run);
        when(run.isBuilding()).thenReturn(true);
        
        // Set up the CpsFlowExecution and CpsThreadGroup
        CpsFlowExecution execution = PowerMockito.mock(CpsFlowExecution.class);
        CpsThreadGroup threadGroup = PowerMockito.mock(CpsThreadGroup.class);
        
        when(run.getExecution()).thenReturn(execution);
        when(execution.getThreadGroup()).thenReturn(threadGroup);
        
        // Mock the reflection part to return false for pausedByQuietMode
        PowerMockito.whenNew(Field.class).withAnyArguments().thenReturn(mock(Field.class));
        
        // Create a list with our mock job
        List<Job<?, ?>> jobs = new ArrayList<>();
        jobs.add(job);
        
        when(jenkins.getAllItems(Job.class)).thenReturn(jobs);
        
        // Use PowerMockito to mock the isPausedByQuietDown method
        PowerMockito.doReturn(false).when(checker, "isPausedByQuietDown", run);
        
        assertFalse(checker.areAllJobsSafe());
    }
    
    /**
     * Test getPausedJobs when Jenkins is not in quietDown mode.
     */
    @Test
    public void testGetPausedJobsWhenNotQuieting() {
        when(jenkins.isQuietingDown()).thenReturn(false);
        
        List<Job<?, ?>> pausedJobs = checker.getPausedJobs();
        assertNotNull(pausedJobs);
        assertTrue(pausedJobs.isEmpty());
    }
    
    /**
     * Test getPausedJobs when Jenkins is in quietDown mode and there are paused jobs.
     */
    @Test
    public void testGetPausedJobsWhenJobsArePaused() throws Exception {
        when(jenkins.isQuietingDown()).thenReturn(true);
        
        // Create a mock WorkflowJob and WorkflowRun
        WorkflowJob job = mock(WorkflowJob.class);
        WorkflowRun run = mock(WorkflowRun.class);
        
        // Set up the job to return the run
        when(job.getLastBuild()).thenReturn(run);
        when(run.isBuilding()).thenReturn(true);
        
        // Set up the CpsFlowExecution and CpsThreadGroup
        CpsFlowExecution execution = PowerMockito.mock(CpsFlowExecution.class);
        CpsThreadGroup threadGroup = PowerMockito.mock(CpsThreadGroup.class);
        
        when(run.getExecution()).thenReturn(execution);
        when(execution.getThreadGroup()).thenReturn(threadGroup);
        
        // Create a list with our mock job
        List<Job<?, ?>> jobs = new ArrayList<>();
        jobs.add(job);
        
        when(jenkins.getAllItems(Job.class)).thenReturn(jobs);
        
        // Use PowerMockito to mock the isPausedByQuietDown method
        PowerMockito.doReturn(true).when(checker, "isPausedByQuietDown", run);
        
        List<Job<?, ?>> pausedJobs = checker.getPausedJobs();
        assertNotNull(pausedJobs);
        assertEquals(1, pausedJobs.size());
        assertEquals(job, pausedJobs.get(0));
    }
    
    /**
     * Test isPausedByQuietDown when the execution is null.
     */
    @Test
    public void testIsPausedByQuietDownWhenExecutionIsNull() {
        WorkflowRun run = mock(WorkflowRun.class);
        when(run.getExecution()).thenReturn(null);
        
        assertFalse(checker.isPausedByQuietDown(run));
    }
    
    /**
     * Test isPausedByQuietDown when the thread group is null.
     */
    @Test
    public void testIsPausedByQuietDownWhenThreadGroupIsNull() {
        WorkflowRun run = mock(WorkflowRun.class);
        CpsFlowExecution execution = mock(CpsFlowExecution.class);
        
        when(run.getExecution()).thenReturn(execution);
        when(execution.getThreadGroup()).thenReturn(null);
        
        assertFalse(checker.isPausedByQuietDown(run));
    }
}
