Setup | crAPI
=============

## Docker

You'll need to have Docker installed and running on your host system.
After having crAPI running, you may want to remove unnecessary docker images
left behind.

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


**Note**: All emails are sent to mailhog service by default and can be checked on
`http://localhost:8025`
You can change the smtp configuration if required however all emails with domain **example.com** will still go to mailhog.

## Kubernetes 

###  Minikube

Make sure minikube is up and running as well as the following addons:
`storage-provisioner`, `default-storageclass`, and `registry`.

1. Expose minikube registry to Docker

    ```
    $ docker run --rm -it --network=host alpine ash -c "apk add socat && socat TCP-LISTEN:5000,reuseaddr,fork TCP:$(minikube ip):5000"
    ```
2. Build Docker images and push to minikube registry

    ```
    $ deploy/k8s/minikube/build-all.sh
    ```
3. Bring the k8s cluster up

    ```
    $ deploy/k8s/minikube/deploy.sh
    ```

4. Run the following command to get the URLs
    ```
    crAPI URL:
    $ echo "http://$(minikube ip):30080"
    ```
    ```
    Mailhog URL:
    echo "http://$(minikube ip):30025"
    ```

## Vagrant

This option allows you to run crAPI within a virtual machine, thus isolated from
your system. You'll need to have [Vagrant] and, for example [VirtualBox]
installed.

1. Clone crAPI repository
    ```
    $ git clone [REPOSITORY-URL]
    ```
2. Start crAPI Virtual Machine
    ```
    $ cd deploy/vagrant && vagrant up
    ```
3. Visit `http://192.168.33.20`


**Note**: All emails are sent to mailhog service by default and can be checked on
`http://192.168.33.20:8025`
You can change the smtp configuration if required however all emails with domain **example.com** will still go to mailhog.

Once you're done playing with crAPI, you can remove it completely from your
system running the following command from the repository root directory

```
$ cd deploy/vagrant && vagrant destroy
```

[Vagrant]: https://www.vagrantup.com/downloads
[VirtualBox]: https://www.virtualbox.org/wiki/Downloads
