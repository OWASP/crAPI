#!/bin/bash
cd "$(dirname $0)"
kubectl create namespace crapi
#kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml

kubectl apply -n crapi -f ./rbac
kubectl apply -n crapi -f ./mongodb
kubectl apply -n crapi -f ./postgres
kubectl apply -n crapi -f ./mailhog
kubectl apply -n crapi -f ./identity
kubectl apply -n crapi -f ./community
kubectl apply -n crapi -f ./workshop
kubectl apply -n crapi -f ./web
