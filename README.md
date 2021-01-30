# requirements

* macOS Big Sur v11.0.1
* docker desktop v3.0.3 
* kubernetes v1.19.3

# set up

```
kubectl apply -R -f secrets/k8s/development/
kubectl apply -R -f k8s/development/
```

# gui

* http://kafka-topics-ui.localhost/
* http://kafka-connect-ui.localhost/
* http://schema-registry-ui.localhost/
* http://minio.localhost/minio/
* http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
