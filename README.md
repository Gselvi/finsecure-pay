# FinSecure Pay - Transaction Validation API

Spring Boot 3 / Java 25 service that validates payment transactions.

## Endpoints
| Method | Path | Description |
|---|---|---|
| GET | `/health` | Liveness check |
| POST | `/validate` | Validates a transaction (200 `VALID`, 400 `INVALID`) |
| GET | `/actuator/prometheus` | Prometheus metrics |

## Validation rules
- `transactionId`, `fromAccount`, `toAccount`, `currency` are required
- `amount` must be > 0 and <= `finsecure.max-amount` (default 10000)
- `currency` must be in `finsecure.allowed-currencies` (default USD,EUR,GBP,INR)
- `fromAccount` and `toAccount` must differ

## Build, test, run
```
mvn clean verify
mvn spring-boot:run
curl http://localhost:8080/health
curl -X POST http://localhost:8080/validate -H "Content-Type: application/json" \
  -d '{"transactionId":"T1","fromAccount":"A100","toAccount":"B200","amount":250,"currency":"USD"}'
```

Config can be overridden with env vars `FINSECURE_MAX_AMOUNT` and `FINSECURE_ALLOWED_CURRENCIES`.

## Repository layout
| Path | Purpose |
|---|---|
| `src/` | Application and JUnit 5 tests |
| `Dockerfile`, `.dockerignore` | Multi-stage, non-root container image |
| `k8s/` | Namespace, ConfigMap, Deployment (2 replicas, probes), LoadBalancer Service |
| `Jenkinsfile` | Checkout → Build → Test → Package → Sonar + Dependency-Check → Docker build → Trivy → Push → Deploy (auto-rollback; `ACTION=ROLLBACK`) |
| `monitoring/` | ServiceMonitor, Prometheus alert rules, Grafana dashboard, Alertmanager e-mail config |
| `docs/DESIGN.md` | Design document, architecture diagram, AI use case |
| `docs/RUNBOOK.md` | Step-by-step commands, demo script, evidence checklist |

Security scan locally: `mvn -Psecurity verify` (needs `NVD_API_KEY` env var; fails on CVSS >= 7).
