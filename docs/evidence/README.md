# Evidence index

Screenshots and logs backing the FinSecure Pay capstone. Each task folder has `screenshots/` (images in the order they were captured) and `logs/` (text output). Personal identifiers (local user ID, AWS account ID) were removed from the logs.

| Task | Folder | What it shows |
|------|--------|---------------|
| 1 | [task1-app-and-git](task1-app-and-git) | Spring Boot app in GitHub, source of both test classes, `mvn clean verify` with 16 passing tests, endpoint checks, git history |
| 2 | [task2-container](task2-container) | Multi-stage Dockerfile, non-root image build, container `/health` check, push to Docker Hub (`selvig88/finsecure-pay`) |
| 3 | [task3-kubernetes-eks](task3-kubernetes-eks) | EKS cluster and node group, `deployment-service.yaml` (2 replicas, probes, LoadBalancer), pods Ready, self-healing after pod delete |
| 4 | [task4-cicd-jenkins](task4-cicd-jenkins) | Jenkins pipeline stage view, build 5 (success), build 6 (failing test blocks later stages), build 7 (rollout), build 8 (rollback) |
| 5 | [task5-devsecops](task5-devsecops) | Trivy dependency and image scans blocking the build, SonarQube quality gate, least-privilege Jenkins deploy account |
| 6 | [task6-monitoring-alerting-logging](task6-monitoring-alerting-logging) | Prometheus targets and alert rules, alert email (firing and resolved), Grafana dashboard, `kubectl logs` |

## Scenario to evidence

| Scenario | Evidence |
|----------|----------|
| Jenkinsfile with all stages | `task4-cicd-jenkins/screenshots/Jenkins file1.png` to `Jenkins file4.png` |
| Pipeline triggered by a source change | `task4-cicd-jenkins/logs/01-jenkins-builds-summary.txt` (build 9, started by an SCM change) |
| Unit tests pass | `task1-app-and-git/screenshots/13`-`16`, `task1-app-and-git/logs/01-mvn-clean-verify.txt` |
| Failing test stops the pipeline | `task4-cicd-jenkins/screenshots/09`, `task4-cicd-jenkins/logs/console-build-6.txt` |
| Container serves `/health` | `task2-container/logs/01-docker-build-run-health.txt` |
| Two pods Ready behind a LoadBalancer | `task3-kubernetes-eks/screenshots/01`-`04`, `task3-kubernetes-eks/logs/08-k8s-state.txt` |
| Pod deleted and recreated | `task3-kubernetes-eks/screenshots/05`-`06` |
| Rollout and rollback | `task4-cicd-jenkins/screenshots/10`-`11`, `task4-cicd-jenkins/logs/console-build-7.txt`, `console-build-8.txt` |
| Vulnerability found and blocked, then fixed | `task5-devsecops/screenshots/01`-`02`, `task5-devsecops/logs/02`-`03`; build 15 blocked and build 16 passing: `task5-devsecops/screenshots/Remediation evidence build 15 blocked and build 16 passing.png`, `task5-devsecops/logs/Build 15 blocked.txt`, `Build 16 success remediation.txt` |
| Credentials stored in Jenkins, not in source | `task5-devsecops/screenshots/08-sonarqube credential managment.png`, `task5-devsecops/logs/01-jenkins-rbac-proof.txt` |
| Alert fires and email is delivered | `task6-monitoring-alerting-logging/screenshots/01`-`06`, `task6-monitoring-alerting-logging/logs/01-alert-fired-and-email-sent.txt` |

The recorded demo video is linked from the project presentation and is not stored in this repository.
