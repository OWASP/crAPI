#!/bin/bash

# Copyright 2020 Traceable, Inc.
#
# Licensed under the Apache License, Version 2.0 (the “License”);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an “AS IS” BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


cd "$(dirname $0)"
kubectl create namespace crapi

kubectl apply -n crapi -f ../base/rbac
kubectl apply -n crapi -f ../base/mongodb
kubectl apply -n crapi -f ../base/postgres
kubectl apply -n crapi -f ../base/mailhog
kubectl apply -n crapi -f ../base/identity
kubectl apply -n crapi -f ../base/community
kubectl apply -n crapi -f ../base/workshop
kubectl apply -n crapi -f ../base/web
kubectl apply -n crapi -f ./mailhog
kubectl apply -n crapi -f ./web
