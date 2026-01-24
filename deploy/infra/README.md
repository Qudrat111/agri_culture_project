# Infra dependencies (Helm)

The microservices need:

* **PostgreSQL** (write model)
* **Kafka** (events/commands)
* **MongoDB** (read model)

For demo/assignment purposes we install them in-cluster using Bitnami charts.

> Tip for production: prefer managed PostgreSQL/Mongo and a managed Kafka (or replace Kafka with a managed queue).

## 0) Namespace

```bash
kubectl create namespace agri
```

## 1) Add repo

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

## 2) Install PostgreSQL

```bash
helm upgrade --install postgresql bitnami/postgresql \
  -n agri \
  --set auth.username=postgres \
  --set auth.password=postgres \
  --set auth.database=agri_procurement
```

## 3) Install MongoDB

```bash
helm upgrade --install mongodb bitnami/mongodb \
  -n agri \
  --set auth.enabled=false
```

This creates a service named `mongodb` in the `agri` namespace.

## 4) Install Kafka

```bash
helm upgrade --install kafka bitnami/kafka \
  -n agri \
  --set kraft.enabled=true \
  --set zookeeper.enabled=false \
  --set listeners.client.protocol=PLAINTEXT
```

This creates a bootstrap service `kafka` (port 9092).

## 5) Verify

```bash
kubectl -n agri get pods
kubectl -n agri get svc
```