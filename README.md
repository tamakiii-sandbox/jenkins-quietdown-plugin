# Jenkins QuietDown Plugin

A Jenkins plugin for reliable deployment strategy when using quietDown mode, specifically addressing issues with Pipeline jobs.

## Problem Statement

When Jenkins enters quietDown mode:

1. Regular jobs simply don't start new builds
2. Pipeline jobs, however, have a more complex execution model with multiple steps and stages

When quietDown is activated, Pipeline jobs that are already running enter a special state where they:
1. Complete the current executing step
2. Then enter a "Pausing (Preparing for shutdown)" state
3. This state is managed by the Pipeline plugin, not the core Jenkins code

The issue is that this "Pausing (Preparing for shutdown)" state is difficult to detect via standard Jenkins APIs because:

- It's an internal state of the Pipeline execution engine
- It's not part of the standard Job/Run state model in Jenkins core
- The Pipeline plugin manages this state separately from the core Jenkins state model

## Solution

This plugin extends the Pipeline plugin to expose the internal paused state through an API, allowing you to:

1. Detect Pipeline jobs in the "Pausing (Preparing for shutdown)" state
2. Query all jobs that are in this special paused state
3. Implement reliable deployment strategies that ensure all jobs are properly stopped before proceeding

## How It Works

The plugin:

1. Extends the `CpsThreadGroup` class to expose the `pausedByQuietMode` state
2. Provides an API endpoint to query this state for all running Pipeline jobs
3. Integrates with your deployment strategy to ensure all jobs are properly stopped

## Usage

### API Endpoint

```
GET /jenkins/quietDownStatus
```

Response:
```json
{
  "quietingDown": true,
  "jobs": [
    {
      "name": "my-pipeline-job",
      "url": "http://jenkins/job/my-pipeline-job/",
      "buildNumber": 123,
      "status": "PAUSED_BY_QUIET_DOWN"
    },
    {
      "name": "another-pipeline-job",
      "url": "http://jenkins/job/another-pipeline-job/",
      "buildNumber": 456,
      "status": "RUNNING"
    }
  ]
}
```

### Programmatic Usage

```java
import org.jenkinsci.plugins.quietdown.QuietDownStatusChecker;

// Check if all jobs are safe to proceed with deployment
boolean allJobsSafe = QuietDownStatusChecker.getInstance().areAllJobsSafe();

// Get all jobs paused by quiet down
List<Job> pausedJobs = QuietDownStatusChecker.getInstance().getPausedJobs();
```

## Development

This plugin is developed using Docker to ensure a consistent development environment.

### Prerequisites

- Docker
- Docker Compose

### Setup

1. Clone the repository
2. Run `docker-compose up -d`
3. Access Jenkins at http://localhost:8080

## License

This plugin is licensed under the MIT License.
