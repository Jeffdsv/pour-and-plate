# jenkins itself, plus the tools our pipeline needs
FROM jenkins/jenkins:lts-jdk21

USER root

# docker cli so jenkins can build and push images, az cli so it can deploy to azure
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl gnupg \
 && install -m 0755 -d /etc/apt/keyrings \
 && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
 && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo $VERSION_CODENAME) stable" > /etc/apt/sources.list.d/docker.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends docker-ce-cli docker-buildx-plugin \
 && curl -sL https://aka.ms/InstallAzureCLIDeb | bash \
 && rm -rf /var/lib/apt/lists/*

# kubectl and helm so jenkins can deploy to the kubernetes cluster
RUN ARCH=$(dpkg --print-architecture) \
 && curl -fsSLo /usr/local/bin/kubectl https://dl.k8s.io/release/v1.36.1/bin/linux/${ARCH}/kubectl \
 && chmod +x /usr/local/bin/kubectl \
 && curl -fsSL https://get.helm.sh/helm-v4.3.1-linux-${ARCH}.tar.gz | tar -xz -C /tmp \
 && mv /tmp/linux-${ARCH}/helm /usr/local/bin/helm \
 && rm -rf /tmp/linux-${ARCH}

# the plugin that lets a pipeline run steps inside docker containers
RUN jenkins-plugin-cli --plugins docker-workflow