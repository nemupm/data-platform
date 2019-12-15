# development

local data platform running on docker for mac.

## setup

### create registry and push images

Before the following command, you must configure "docker.for.mac.localhost:5000"
as "insecure-registries" in "Daemon" tab of "Preferences".

```
$ kubectl apply -R -f ./k8s/development/kube-system/registry
$ docker tag <image name> docker.for.mac.localhost:5000/<image name>
$ docker push docker.for.mac.localhost:5000/<image name>
```
