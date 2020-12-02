Overview | crAPI
================

At a high level, the crAPI application is modeled as a B2C application that allows any user to get their car servicing done by a car mechanic. A user can create an account on the WebApp, manage his/her cars, search for car mechanics, submit servicing request for any car, and purchase car accessories from the vendor. The WebApp also has a community section where users can contribute with blog posts and comments.

The crAPI application, by design, does not implement all of its functionalities in the most secure manner. In other words, it deliberately exposes security vulnerabilities that can be exploited by any security enthusiast who is playing with the application. For more details on the vulnerabilities see the [challenges.md][0]

crAPI has a microservices architecture comprising the following services developed using different technologies: 

* web: Main Ingress service
* identiy: User and authentication endpoints
* community: Community blogs and comments endpoints
* workshop: Vehicle workshop endpoints
* mailhog: Mail service
* mongo: NoSQL Database
* postgres: SQL Database

[0]: ./challenges.md
