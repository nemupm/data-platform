# How to create yaml

## spark-operator.yaml

```
helm template spark-operator incubator/sparkoperator --namespace spark-operator  --set enableWebhook=true  --set sparkJobNamespace=default > k8s/development/spark-operator/spark-operator.yaml
```