package io.jenkins.plugins.quietdown;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.RootAction;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * API endpoint to expose the quietDown status information.
 * This endpoint provides information about Pipeline jobs that are paused by quietDown mode.
 */
@Extension
public class QuietDownStatusEndpoint implements RootAction {
    private static final String URL_NAME = "quietDownStatus";
    private static final ModelBuilder MODEL_BUILDER = new ModelBuilder();
    
    @Override
    public String getIconFileName() {
        return "symbol-power";
    }
    
    @Override
    public String getDisplayName() {
        return "QuietDown Status";
    }
    
    @Override
    public String getUrlName() {
        return URL_NAME;
    }
    
    /**
     * Handle the API request.
     *
     * @param request  The StaplerRequest
     * @param response The StaplerResponse
     * @throws IOException      If an error occurs while writing the response
     * @throws ServletException If an error occurs while processing the request
     */
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        
        QuietDownStatusResponse statusResponse = new QuietDownStatusResponse();
        Model model = MODEL_BUILDER.get(QuietDownStatusResponse.class);
        
        response.setContentType("application/json;charset=UTF-8");
        model.writeTo(statusResponse, response.getWriter());
    }
    
    /**
     * Response object for the quietDown status API.
     */
    @ExportedBean
    public static class QuietDownStatusResponse {
        /**
         * Get whether Jenkins is quieting down.
         *
         * @return true if Jenkins is quieting down, false otherwise
         */
        @Exported
        public boolean isQuietingDown() {
            return Jenkins.get().isQuietingDown();
        }
        
        /**
         * Get the list of jobs and their status.
         *
         * @return A list of job status objects
         */
        @Exported
        public List<JobStatus> getJobs() {
            List<JobStatus> jobStatuses = new ArrayList<>();
            QuietDownStatusChecker checker = QuietDownStatusChecker.getInstance();
            
            for (Job<?, ?> job : Jenkins.get().getAllItems(Job.class)) {
                Run<?, ?> lastBuild = job.getLastBuild();
                
                if (lastBuild != null && lastBuild.isBuilding()) {
                    JobStatus status = new JobStatus();
                    status.setName(job.getFullName());
                    status.setUrl(job.getAbsoluteUrl());
                    status.setBuildNumber(lastBuild.getNumber());
                    
                    if (lastBuild instanceof WorkflowRun && checker.isPausedByQuietDown((WorkflowRun) lastBuild)) {
                        status.setStatus("PAUSED_BY_QUIET_DOWN");
                    } else {
                        status.setStatus("RUNNING");
                    }
                    
                    jobStatuses.add(status);
                }
            }
            
            return jobStatuses;
        }
    }
    
    /**
     * Job status object for the quietDown status API.
     */
    @ExportedBean
    public static class JobStatus {
        private String name;
        private String url;
        private int buildNumber;
        private String status;
        
        /**
         * Get the name of the job.
         *
         * @return The name of the job
         */
        @Exported
        public String getName() {
            return name;
        }
        
        /**
         * Set the name of the job.
         *
         * @param name The name of the job
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get the URL of the job.
         *
         * @return The URL of the job
         */
        @Exported
        public String getUrl() {
            return url;
        }
        
        /**
         * Set the URL of the job.
         *
         * @param url The URL of the job
         */
        public void setUrl(String url) {
            this.url = url;
        }
        
        /**
         * Get the build number of the job.
         *
         * @return The build number of the job
         */
        @Exported
        public int getBuildNumber() {
            return buildNumber;
        }
        
        /**
         * Set the build number of the job.
         *
         * @param buildNumber The build number of the job
         */
        public void setBuildNumber(int buildNumber) {
            this.buildNumber = buildNumber;
        }
        
        /**
         * Get the status of the job.
         *
         * @return The status of the job
         */
        @Exported
        public String getStatus() {
            return status;
        }
        
        /**
         * Set the status of the job.
         *
         * @param status The status of the job
         */
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
