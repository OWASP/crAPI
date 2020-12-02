crAPI
=====

**c**ompletely **r**idiculous **API** (crAPI) will help you to understand the
ten most critical API security risks. crAPI is vulnerable by design, but you'll
be able to safely run it to educate/train yourself.

crAPI is modern, built on top of a microservices architecture. When time has
come to buy your first car, sign up for an account and start your journey. To
know more about crAPI, please check [crAPI's overview][overview].

## QuickStart Guide

### Docker

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

If you would like to deploy on kubernetes we have sample k8s configs already
created. Check [the setup instructions][setup] for more details.

---

Copyright (c) 2020 "Traceable AI". All rights reserved.

[overview]: docs/overview.md
[setup]: docs/setup.md
