# FinSecure Pay – Runbook, Demo Script and Evidence Checklist

Replace e-mail addresses and the cloud names with your own. Never commit credentials.

## Task 1 – Build, test, GitHub
```
mvn clean verify                      # 16 JUnit 5 tests
git remote add origin https://github.com/<you>/finsecure-pay.git
git push -u origin main
```
Evidence: GitHub URL, `target/surefire-reports`, console output.

## Task 2 – Container
```
docker build -t selvig88/finsecure-pay:1.0.0 .
docker run -d --name fsp -p 8080:8080 selvig88/finsecure-pay:1.0.0
curl http://localhost:8080/health
docker login -u selvig88          # token via prompt, not on command line
docker push selvig88/finsecure-pay:1.0.0
```
Evidence: `docker images`, `docker ps`, curl output, Docker Hub tag page.

## Task 3 – Kubernetes on AWS (EKS)
Prerequisites: AWS account (personal, MFA on, budget alert set), an IAM user with programmatic access and least privilege (never the root user), `aws`, `eksctl` and `kubectl` installed. Configure with `aws configure` (keys stay in `~/.aws`, never in the repo).
```
aws sts get-caller-identity                          # confirms who you are
eksctl create cluster -f eks/cluster.yaml            # 2 x c7i-flex.large managed nodes (free-plan eligible)
aws eks update-kubeconfig --name finsecure-eks --region us-east-1 --profile finsecure
kubectl get nodes

kubectl apply -f k8s/namespace-config.yaml -f k8s/deployment-service.yaml
kubectl -n finsecure get pods,svc,cm
kubectl -n finsecure get svc finsecure-pay          # EXTERNAL-IP is an ELB hostname on AWS; wait until it appears
curl http://<ELB-HOSTNAME>/health

# recovery
kubectl -n finsecure delete pod <one-pod>
kubectl -n finsecure get pods -w                     # new pod appears, 2/2 Ready again
```
The image is public on Docker Hub, so the cluster needs no pull secret. EKS charges for the control plane and nodes while it exists: create it only when ready to demo.

**Clean up (avoids charges)**: delete the Service first so the load balancer is removed, then the cluster.
```
kubectl -n finsecure delete svc finsecure-pay
eksctl delete cluster -f eks/cluster.yaml
```
Check the EC2, EKS and Load Balancer consoles afterwards for leftovers.

## Task 4 – Jenkins
1. Jenkins (with plugins: Pipeline, Git, Credentials Binding, SonarQube Scanner, Email Extension, JUnit) on a host with JDK 25, Maven, Docker, kubectl, Trivy.
2. Credentials (Manage Jenkins > Credentials): `dockerhub-creds` (username/password – use a Docker Hub access token), `kubeconfig` (secret file), `nvd-api-key` (secret text). The EKS kubeconfig calls `aws eks get-token`, so the Jenkins host also needs the AWS CLI and credentials for a least-privilege IAM user (store them as Jenkins credentials or an instance role, never in the repo). SonarQube server named `sonarqube` under Manage Jenkins > System; SMTP under Extended E-mail Notification.
3. New Pipeline job → "Pipeline script from SCM" → GitHub repo → `Jenkinsfile`. Add a GitHub webhook (or rely on `pollSCM`).
4. **Success run**: change a line, push, watch all stages go green; `kubectl -n finsecure rollout history deployment/finsecure-pay`.
5. **Failed-test demo**: in `TransactionValidatorTest` add `assertEquals(1, 2);`, push → Test stage fails, later stages (Push/Deploy) are skipped. Revert afterwards.
6. **Rollout**: second successful run shows a new image tag: `kubectl -n finsecure get deploy finsecure-pay -o jsonpath='{.spec.template.spec.containers[0].image}'`.
7. **Rollback**: Build with Parameters → `ACTION=ROLLBACK` (or `kubectl -n finsecure rollout undo deployment/finsecure-pay`).

## Task 5 – DevSecOps
| Gate | Where | Controlled failure |
|---|---|---|
| SonarQube quality gate | Jenkins stage | e.g. add an obvious bug/hard-coded password; gate fails |
| OWASP Dependency-Check, CVSS ≥ 7 | `mvn -Psecurity verify` | add `commons-collections:commons-collections:3.2.1` (CVE-2015-7501, critical) → build fails; remove/upgrade to `org.apache.commons:commons-collections4:4.5.0` → passes |
| Trivy image scan | Jenkins stage | use an old base image (e.g. `eclipse-temurin:17-jre-focal` w/ unpatched libs) → HIGH/CRITICAL → fail; revert to current base image |

Run locally: `export NVD_API_KEY=...; mvn -Psecurity verify` (first run downloads the NVD database; an API key makes it fast). Reports: `target/dependency-check-report.html`, `trivy-report.txt`.
Secrets evidence: Jenkins credentials screen (masked), `kubectl get secret alertmanager-smtp` (values not shown), `git grep -i password` shows nothing, `.gitignore`.

**Kubernetes Secret demo** (create from the command line, never from a committed file):
```
kubectl -n finsecure create secret generic finsecure-pay-secret --from-literal=api-key=<random-value>
kubectl -n finsecure get secret finsecure-pay-secret            # shows type and size, not the value
kubectl -n finsecure get secret finsecure-pay-secret -o jsonpath='{.data.api-key}' | base64 -d   # shows Secrets are only encoded, not encrypted
```
Then show the protection: RBAC read access limited to the app's service account (positive: allowed call works; negative: `kubectl auth can-i get secrets --as=system:serviceaccount:finsecure:default` is denied for others; audit: denial visible in the log) and, optionally on EKS, envelope encryption of Secrets with a KMS key (`secretsEncryption.keyARN` in the eksctl config; needs a KMS key you create first).

## Task 6 – Monitoring and alerting
```
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
kubectl create ns monitoring
kubectl -n monitoring create secret generic alertmanager-smtp --from-literal=password='<SMTP app password>'
helm install kube-prometheus-stack prometheus-community/kube-prometheus-stack -n monitoring -f monitoring/kube-prometheus-stack-values.yaml
kubectl apply -f monitoring/servicemonitor-alerts.yaml -f monitoring/grafana-dashboard.yaml
kubectl -n monitoring port-forward svc/kube-prometheus-stack-grafana 3000:80       # admin password: kubectl -n monitoring get secret kube-prometheus-stack-grafana -o jsonpath='{.data.admin-password}' | base64 -d
kubectl -n monitoring port-forward svc/kube-prometheus-stack-prometheus 9090
```
**Trigger alert** (invalid-rate > 50 % for 1 min):
```
for i in $(seq 1 600); do curl -s -o /dev/null -X POST http://<EXTERNAL-IP>/validate -H "Content-Type: application/json" -d '{"transactionId":"X","fromAccount":"A","toAccount":"A","amount":-1,"currency":"XXX"}'; sleep 0.2; done
```
Or, for the critical alert: `kubectl -n finsecure scale deploy finsecure-pay --replicas=0` (then back to 2). Evidence: Prometheus Alerts page (Firing), Alertmanager UI, received e-mail, Jenkins failure e-mail.

**Logs**: `kubectl -n finsecure logs deploy/finsecure-pay --tail=50`; `kubectl -n finsecure logs <pod> --previous`; `kubectl -n finsecure describe pod <pod>`; cloud logging (on EKS, enable CloudWatch Container Insights / the Amazon CloudWatch Observability add-on and query the `/aws/containerinsights/finsecure-eks/application` log group for `INVALID`). `kubectl logs` alone satisfies the brief's requirement to document how logs are accessed.

## Task 7 – Documentation and demo
- Design doc: [DESIGN.md](DESIGN.md) (architecture diagram in Mermaid – render and export as PNG for the deck).
- 10–15 min demo order: (1) code + tests 2 min → (2) Jenkinsfile & push triggers build 3 min → (3) failed-test demo 1 min → (4) security gate fail/fix 2 min → (5) K8s pods/service, curl, kill pod 2 min → (6) rollout + rollback 2 min → (7) Grafana + alert + email 2 min → (8) AI use case & lessons 1 min.
- Slide plan follows the assignment's 8–10 slide structure (overview; app/Git; Docker+K8s; CI/CD; DevSecOps; monitoring; AI & architecture; results/recovery; lessons).

## Core test-case checklist
- [ ] Unit tests pass  - [ ] Controlled failing test stops pipeline  - [ ] Container `/health` OK  - [ ] ≥2 pods Ready
- [ ] New version rollout OK  - [ ] Pod delete → recreated  - [ ] Critical/high finding blocks release  - [ ] Alert fires + email received
