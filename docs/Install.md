Docker setup
-------------

#### To run on docker first build the images
```
./dockercompose/buildall.sh
```

#### To depoy
```
cd dockercompose && docker-compose --compatibility up
```

Visit: http://localhost:8888

Note: All emails are sent to mailhog service and can be checked on http://localhost:8025

Kubernetes setup (Minikube)
---------------------------
Make sure Minikube is up and running

Ensure following addons are up and running:
storage-provisioner, default-storageclass, registry

#### Minikube expose registry to Docker
```
docker run --rm -it --network=host alpine ash -c "apk add socat && socat TCP-LISTEN:5000,reuseaddr,fork TCP:$(minikube ip):5000"
```

#### Build the images and push to minikube registry
```
./k8s/build-all.sh
```

#### Bring the k8s cluster up
```
./k8s/deploy.sh
```
