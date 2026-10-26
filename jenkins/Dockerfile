FROM jenkins/jenkins:lts-jdk21

USER root

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates unzip git \
 && rm -rf /var/lib/apt/lists/*

# JDK 25 and Maven for building the application (Jenkins itself keeps running on JDK 21)
RUN mkdir -p /opt/jdk25 \
 && curl -fsSL "https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse" | tar -xz -C /opt/jdk25 --strip-components=1 \
 && curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar -xz -C /opt \
 && ln -s /opt/apache-maven-3.9.9 /opt/maven

# Docker CLI (talks to the host Docker daemon through the mounted socket), kubectl and Trivy
RUN curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-27.3.1.tgz | tar -xz -C /usr/local/bin --strip-components=1 docker/docker \
 && curl -fsSLo /usr/local/bin/kubectl "https://dl.k8s.io/release/$(curl -fsSL https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" \
 && chmod +x /usr/local/bin/kubectl \
 && curl -fsSL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false" \
    CASC_JENKINS_CONFIG=/var/jenkins_home/casc.yaml
COPY casc.yaml /var/jenkins_home/casc.yaml
