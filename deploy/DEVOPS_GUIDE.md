# DevOps completion guide (DigitalOcean + Kubernetes + GitOps)

This guide maps **your 9 requirements** to concrete, runnable steps.

## Architecture (what you will run)

Namespaces:

* `agri` – your agriculture microservices + infra (Postgres, Kafka, Mongo)
* `monitoring` – Prometheus, Loki, Grafana
* `argocd` – ArgoCD (GitOps controller)

Flow:

1. GitHub Actions builds container images and pushes to GHCR
2. GitHub Actions updates `deploy/charts/agri-platform/values-prod.yaml` with the new image tag (GitOps)
3. ArgoCD notices the Git change and syncs Kubernetes automatically

---

## 1) DigitalOcean Kubernetes cluster (DOKS)

1. Create a DOKS cluster (UI or `doctl`). For an assignment-sized cluster:
   * 3 nodes (2 vCPU / 4GB) is enough for Kafka + DB + services + monitoring.
2. Download kubeconfig and verify:

```bash
kubectl get nodes
```

---

## 2) Install ArgoCD (requirement #9)

```bash
kubectl create namespace argocd
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

helm upgrade --install argocd argo/argo-cd -n argocd \
  --set server.service.type=LoadBalancer
```

Get the ArgoCD URL:

```bash
kubectl -n argocd get svc argocd-server
```

Get initial admin password:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d; echo
```

---

## 3) Install monitoring stack (requirements #1–#5)

Follow:

* `deploy/monitoring/README.md`

This installs:

* Prometheus (scrapes K8s + your apps)
* Loki + Promtail (collects pod logs)
* Grafana (dashboards + datasources auto-provisioned)

---

## 4) Install infra (Postgres + Kafka + Mongo)

Follow:

* `deploy/infra/README.md`

---

## 5) Deploy your agriculture application using Helm (requirement #6)

Local/manual install (useful for first run):

```bash
helm upgrade --install agri deploy/charts/agri-platform \
  -n agri \
  --create-namespace \
  --set image.registry=ghcr.io/<OWNER>/<REPO> \
  --set image.tag=latest
```

The Helm chart:

* Deploys 4 services (order, saga, inventory, query)
* Adds Prometheus scrape annotations to Services

---

## 6) Add custom metrics in the app (requirement #7)

Implemented in code:

* `OrderMetrics` → `agri_orders_created_total`, `agri_orders_confirmed_total`, `agri_orders_cancelled_total`
* `InventoryMetrics` → reservation success/fail + compensation counters
* `SagaMetrics` → started/failed/completed counters
* `QueryMetrics` → query request + failure counters

Metrics endpoint (for each service):

```
GET http://<SERVICE-IP>:<PORT>/actuator/prometheus
```

---

## 7) Prometheus scrape config (requirement #8)

Implemented in `deploy/monitoring/prometheus-values.yaml`:

* Prometheus loads `serverFiles.prometheus.yml` (a ConfigMap created by the Helm chart)
* A scrape job keeps only Services annotated with:
  * `prometheus.io/scrape: "true"`
  * `prometheus.io/path: "/actuator/prometheus"`
  * `prometheus.io/port: "8081"` (etc)

The agri Helm chart adds these annotations automatically.

---

## 8) GitHub Actions → GHCR → ArgoCD (requirement #9)

1. Push this repo to GitHub.
2. Enable GitHub Packages.
3. ArgoCD: apply the application manifest and change the repo URL placeholders:

```bash
kubectl apply -f deploy/argocd/agri-platform-application.yaml
```

4. On every push to `main`:
   * `.github/workflows/deploy-argocd.yaml` builds & pushes images
   * updates `deploy/charts/agri-platform/values-prod.yaml` with the new SHA
   * ArgoCD auto-sync deploys to DigitalOcean

---

## 9) How to verify everything on DigitalOcean

```bash
# App pods
kubectl -n agri get pods

# Query service public endpoint
kubectl -n agri get svc agri-query

# Metrics working (example)
kubectl -n agri port-forward svc/agri-order 8081:8081
curl -s http://localhost:8081/actuator/prometheus | head

# Logs in Loki (from Grafana Explore)
kubectl -n monitoring get svc grafana
```