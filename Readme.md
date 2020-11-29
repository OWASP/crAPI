crAPI
=============
A completely ridiculous API.

Crapi setup comprises of following services
- web (Main Ingress service)
- identiy (User and authentication endpoints)
- community (Community blogs and comments endpoints)
- workshop (Vehicle workshop endpoints)
- mailhog (Mail service)
- mongo (NOSQL Database)
- postgres (SQL Database)

Quicktart Guide
-------------

## Docker setup

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

If you would like to deploy on kubernetes we have sample k8s configs already created. Refer docs/Install.md for more details.


Copyright (c) 2020 "Traceable AI". All rights reserved.
