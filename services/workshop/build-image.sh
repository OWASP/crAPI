#!/bin/bash
set -x
cd "$(dirname $0)"
docker build -t crapi-workshop .
