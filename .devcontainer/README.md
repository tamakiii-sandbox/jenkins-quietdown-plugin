# Dev Container for Jenkins Plugin Development

This Dev Container configuration is set up for developing the Jenkins QuietDown Plugin using Visual Studio Code's Remote - Containers extension.

## Features

- Maven 3.9 with Eclipse Temurin JDK 21
- Persistent Maven repository cache
- Pre-configured Java and Maven extensions for VS Code
- Port forwarding for Jenkins (8080)
- Automatic dependency resolution on container startup

## Getting Started

1. Install [Docker](https://www.docker.com/products/docker-desktop) and [Visual Studio Code](https://code.visualstudio.com/)
2. Install the [Remote - Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) for VS Code
3. Open this project in VS Code
4. Click on the green button in the bottom-left corner of VS Code or press F1 and select "Remote-Containers: Reopen in Container"
5. Wait for the container to build and initialize

## Development Workflow

Once the container is running, you can:

- Build the plugin: `mvn package`
- Run tests: `mvn test`
- Clean the project: `mvn clean`
- Install the plugin: `mvn install`

You can also use the Maven extension in VS Code to run these commands.

## Troubleshooting

If you encounter dependency resolution issues:

1. Force update dependencies: `mvn -U dependency:resolve`
2. Clear Maven cache: `rm -rf /root/.m2/repository`
3. Rebuild the container: Press F1 and select "Remote-Containers: Rebuild Container"

## Notes

- The Maven repository is persisted in a Docker volume named `maven-repo`
- The plugin source code is mounted from your local filesystem to `/app` in the container
- Java 21 is used as specified in the project's pom.xml
