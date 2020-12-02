Setup | crAPI
=============

## Docker

1. Clone crAPI repository
    ```
    $ git clone [REPOSITORY-URL]
    ```
2. Build all docker images
    ```
    $ deploy/docker/build-all.sh
    ```
3. Start crAPI
    ```
    $ docker-compose -f deploy/docker/docker-compose.yml --compatibility up -d
    ```
4. Visit `http://localhost:8888`


**Note**: All emails are sent to mailhog service and can be checked on
`http://localhost:8025`

## Kubernetes (minikube)

Make sure minikube is up and running as well as the following addons:
`storage-provisioner`, `default-storageclass`, and `registry`.

1. Expose minikube registry to Docker

    ```
    $ docker run --rm -it --network=host alpine ash -c "apk add socat && socat TCP-LISTEN:5000,reuseaddr,fork TCP:$(minikube ip):5000"
    ```
2. Build Docker images and push to minikube registry

    ```
    $ deploy/k8s/build-all.sh
    ```
3. Bring the k8s cluster up

    ```
    $ deploy/k8s/deploy.sh
    ```
