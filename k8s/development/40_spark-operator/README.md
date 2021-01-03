# How to create yaml

## spark-operator.yaml

```
helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator
helm template spark-operator spark-operator/spark-operator --namespace spark-operator --set webhook.enable=true --set sparkJobNamespace=default > k8s/development/spark-operator/spark-operator.yaml
```

## crds

install CustomResourceDefinitions manually
https://github.com/GoogleCloudPlatform/spark-on-k8s-operator/tree/master/charts/spark-operator-chart/crds

## about namespace

add `namespace: spark-operator` to resources which don't have metadata.namespace.
