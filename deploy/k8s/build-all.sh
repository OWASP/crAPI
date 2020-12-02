#!/bin/sh
set -x
set -e
cd "$(dirname $0)"
scripts=$(find ../../services/ -name 'build-image*')
for script in ${scripts}
do
    echo "Executing $script"
    bash -x "$script"
done

# Deploy to local repository
docker images | grep crapi | grep -v localhost | awk '{print $1}' | xargs -L1 -I{} docker tag {} localhost:5000/{}:v1
docker images | grep crapi |  grep localhost | grep v1 | awk '{print $1}' | xargs -L1 -I{}  docker push {}:v1
