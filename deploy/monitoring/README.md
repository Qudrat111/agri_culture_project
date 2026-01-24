# Monitoring stack (Helm): Prometheus + Loki + Grafana

This folder implements DevOps requirements **#1–#5**:

1. Install Prometheus, Loki, Grafana, Loki-stack via Helm charts
2. Disable HostToContainer mount propagation on node-exporter DaemonSet
3. Login to Grafana dashboard
4. Add data sources (auto-provisioned)
5. PromQL and LogQL cheat sheet

## 0) Prereqs

* Helm 3
* kubectl configured for your cluster

Create a namespace:

```bash
kubectl create namespace monitoring
```

Add Helm repos:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
```

## 1) Install Prometheus

```bash
helm upgrade --install prometheus prometheus-community/prometheus \
  -n monitoring \
  -f prometheus-values.yaml
```

**Requirement #2 implemented:** `prometheus-node-exporter.hostRootFsMount.enabled=false` in `prometheus-values.yaml`.

## 2) Install Loki (loki-stack)

```bash
helm upgrade --install loki grafana/loki-stack \
  -n monitoring \
  -f loki-stack-values.yaml
```

This installs:

* **Loki** (log store)
* **Promtail** DaemonSet (collects pod logs and pushes them to Loki)

## 3) Install Grafana

```bash
helm upgrade --install grafana grafana/grafana \
  -n monitoring \
  -f grafana-values.yaml
```

## 4) Login to Grafana

Get the external IP:

```bash
kubectl -n monitoring get svc grafana
```

Open `http://<EXTERNAL-IP>`.

Credentials (from values):

* user: `admin`
* password: `admin`

## 5) Data sources (Prometheus + Loki)

They are provisioned automatically by `grafana-values.yaml` using Grafana provisioning (`provisioning/datasources`).

## 6) PromQL quick start

* CPU (per pod):
  ```
  sum(rate(container_cpu_usage_seconds_total{namespace="agri"}[5m])) by (pod)
  ```
* Memory (per pod):
  ```
  sum(container_memory_working_set_bytes{namespace="agri"}) by (pod)
  ```
* Custom metric example (order created):
  ```
  increase(agri_orders_created_total[10m])
  ```

## 7) LogQL quick start

* All logs in agri namespace:
  ```
  {namespace="agri"}
  ```
* Logs from the order service deployment:
  ```
  {namespace="agri", app_kubernetes_io_component="order"}
  ```
* Filter errors:
  ```
  {namespace="agri"} |= "ERROR"
  ```
