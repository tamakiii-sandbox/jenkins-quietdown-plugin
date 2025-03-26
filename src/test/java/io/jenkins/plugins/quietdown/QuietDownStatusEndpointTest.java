package io.jenkins.plugins.quietdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.model.Job;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link QuietDownStatusEndpoint}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, QuietDownStatusChecker.class, ModelBuilder.class})
@PowerMockIgnore({"javax.management.*", "javax.xml.*", "org.xml.*", "org.w3c.*"})
public class QuietDownStatusEndpointTest {
    
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    @Mock
    private Jenkins jenkins;
    
    @Mock
    private QuietDownStatusChecker statusChecker;
    
    private QuietDownStatusEndpoint endpoint;
    
    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        when(Jenkins.get()).thenReturn(jenkins);
        
        PowerMockito.mockStatic(QuietDownStatusChecker.class);
        when(QuietDownStatusChecker.getInstance()).thenReturn(statusChecker);
        
        endpoint = new QuietDownStatusEndpoint();
    }
    
    /**
     * Test that the endpoint returns the correct icon file name.
     */
    @Test
    public void testGetIconFileName() {
        assertEquals("symbol-power", endpoint.getIconFileName());
    }
    
    /**
     * Test that the endpoint returns the correct display name.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("QuietDown Status", endpoint.getDisplayName());
    }
    
    /**
     * Test that the endpoint returns the correct URL name.
     */
    @Test
    public void testGetUrlName() {
        assertEquals("quietDownStatus", endpoint.getUrlName());
    }
    
    /**
     * Test that the doIndex method checks for admin permission.
     */
    @Test
    public void testDoIndexChecksPermission() throws Exception {
        StaplerRequest request = mock(StaplerRequest.class);
        StaplerResponse response = mock(StaplerResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        when(response.getWriter()).thenReturn(writer);
        
        // Mock the ModelBuilder to avoid NPE
        PowerMockito.mockStatic(ModelBuilder.class);
        ModelBuilder modelBuilder = PowerMockito.mock(ModelBuilder.class);
        Model model = PowerMockito.mock(Model.class);
        PowerMockito.when(modelBuilder.get(QuietDownStatusEndpoint.QuietDownStatusResponse.class)).thenReturn(model);
        
        endpoint.doIndex(request, response);
        
        // Verify that the Jenkins.ADMINISTER permission was checked
        verify(jenkins).checkPermission(Jenkins.ADMINISTER);
    }
    
    /**
     * Test that the doIndex method sets the correct content type.
     */
    @Test
    public void testDoIndexSetsContentType() throws Exception {
        StaplerRequest request = mock(StaplerRequest.class);
        StaplerResponse response = mock(StaplerResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        when(response.getWriter()).thenReturn(writer);
        
        // Mock the ModelBuilder to avoid NPE
        PowerMockito.mockStatic(ModelBuilder.class);
        ModelBuilder modelBuilder = PowerMockito.mock(ModelBuilder.class);
        Model model = PowerMockito.mock(Model.class);
        PowerMockito.when(modelBuilder.get(QuietDownStatusEndpoint.QuietDownStatusResponse.class)).thenReturn(model);
        
        endpoint.doIndex(request, response);
        
        // Verify that the content type was set correctly
        verify(response).setContentType("application/json;charset=UTF-8");
    }
    
    /**
     * Test the QuietDownStatusResponse class.
     */
    @Test
    public void testQuietDownStatusResponse() {
        // Mock Jenkins.isQuietingDown()
        when(jenkins.isQuietingDown()).thenReturn(true);
        
        // Create a mock job and run
        WorkflowJob job = mock(WorkflowJob.class);
        WorkflowRun run = mock(WorkflowRun.class);
        
        when(job.getFullName()).thenReturn("test-job");
        when(job.getAbsoluteUrl()).thenReturn("http://jenkins/job/test-job");
        when(job.getLastBuild()).thenReturn(run);
        when(run.getNumber()).thenReturn(42);
        when(run.isBuilding()).thenReturn(true);
        
        // Create a list of jobs
        List<Job<?, ?>> jobs = new ArrayList<>();
        jobs.add(job);
        
        when(jenkins.getAllItems(Job.class)).thenReturn(jobs);
        when(statusChecker.isPausedByQuietDown(run)).thenReturn(true);
        
        // Create the response object
        QuietDownStatusEndpoint.QuietDownStatusResponse response = new QuietDownStatusEndpoint.QuietDownStatusResponse();
        
        // Test the isQuietingDown method
        assertTrue(response.isQuietingDown());
        
        // Test the getJobs method
        List<QuietDownStatusEndpoint.JobStatus> jobStatuses = response.getJobs();
        assertNotNull(jobStatuses);
        assertEquals(1, jobStatuses.size());
        
        QuietDownStatusEndpoint.JobStatus jobStatus = jobStatuses.get(0);
        assertEquals("test-job", jobStatus.getName());
        assertEquals("http://jenkins/job/test-job", jobStatus.getUrl());
        assertEquals(42, jobStatus.getBuildNumber());
        assertEquals("PAUSED_BY_QUIET_DOWN", jobStatus.getStatus());
    }
    
    /**
     * Test the JobStatus class.
     */
    @Test
    public void testJobStatus() {
        QuietDownStatusEndpoint.JobStatus jobStatus = new QuietDownStatusEndpoint.JobStatus();
        
        // Test setters and getters
        jobStatus.setName("test-job");
        assertEquals("test-job", jobStatus.getName());
        
        jobStatus.setUrl("http://jenkins/job/test-job");
        assertEquals("http://jenkins/job/test-job", jobStatus.getUrl());
        
        jobStatus.setBuildNumber(42);
        assertEquals(42, jobStatus.getBuildNumber());
        
        jobStatus.setStatus("RUNNING");
        assertEquals("RUNNING", jobStatus.getStatus());
    }
}
