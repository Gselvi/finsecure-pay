# Project Status (resume here)

Last updated: 2026-10-21

## Done (in repo, committed)
- Task 1: App, endpoints, 16 JUnit 5 tests passing (`mvn verify`, JDK 25+).
- Task 2: `Dockerfile` (multi-stage, non-root) and `.dockerignore` – written, not yet built.
- Task 3: `k8s/` manifests – written, not yet deployed.
- Task 4/5: `Jenkinsfile` with Sonar, Dependency-Check (CVSS >= 7), Trivy gates, auto-rollback – written, not yet run.
- Task 6: `monitoring/` Prometheus rules, Grafana dashboard, Alertmanager email values – written, not yet applied.
- Task 7: `docs/DESIGN.md` (architecture, AI use case, lessons) and `docs/RUNBOOK.md` (commands, demo script, checklist).

## Still to do
1. Move project outside OneDrive; create GitHub repo; `git remote add origin ...; git push -u origin main`.
2. Replace placeholders: `YOUR_DOCKERHUB_USER` (Jenkinsfile, k8s/deployment-service.yaml), e-mail addresses (Jenkinsfile, monitoring/kube-prometheus-stack-values.yaml).
3. Install Docker; build, run, tag and push the image (Task 2). Confirm `eclipse-temurin:25-jre` pulls.
4. Create managed Kubernetes cluster; deploy; demonstrate pod recovery (Task 3).
5. Set up Jenkins + credentials (`dockerhub-creds`, `kubeconfig`, `nvd-api-key`, Sonar server `sonarqube`, SMTP); run success, failed-test, rollout and rollback demos (Task 4).
6. Demonstrate security gate block + remediation (Task 5).
7. Install kube-prometheus-stack, trigger alert, capture email (Task 6).
8. Export architecture diagram, capture screenshots, build 8-10 slide deck, record 10-15 min demo (Task 7).
9. Delete cloud resources after the demo to avoid cost.

## Notes
- `mvn clean` fails inside OneDrive (locked `target/`); fine after moving.
- Local tools found: Git, JDK 21 and 26, Maven at `~\.maven\apache-maven-3.9.16`. Docker/kubectl/az/helm not installed.
- Set `JAVA_HOME` to a JDK 25+ (e.g. jdk-26) when building; pom targets Java 25.
