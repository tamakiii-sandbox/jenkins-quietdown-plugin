FROM maven:3.9-eclipse-temurin-21

# Install additional packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    which \
    man \
    make \
    less \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

CMD ["tail", "-f", "/dev/null"]
