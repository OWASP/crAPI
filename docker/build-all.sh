#!/bin/sh
set -x
set -e
cd "$(dirname $0)"
scripts=$(find ../ -name 'build-image*')
for script in ${scripts}
do
    echo "Executing $script"
    bash -x "$script"
done
