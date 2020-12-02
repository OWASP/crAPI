#! /bin/sh

python3 manage.py migrate user --fake &&\
python3 manage.py migrate crapi &&\
python3 manage.py migrate db &&\
python3 manage.py runserver 0.0.0.0:8000

exec "$@"
