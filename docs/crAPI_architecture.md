# Knowing the crAPI

## crAPI as B2C Model

To keep it simple, the crAPI (completely ridiculous API) can be seen as a business-to-customer (B2C) application.  In this case, it is a car servicing B2C service.  A customer can sign up for an account, log in and place a request for servicing her/his car by choosing a mechanic.  Further, the customer can do shopping for car accessories.  Also, this B2C service provides the space to blog and engage the readers by responding to their comments.  To do all this, we have a web interface.

So, to recap quickly what does the B2C service of crAPI do:

  1. Provides the web UI for interacting with the crAPI
  2. One can create an account, log in, and then request a car service
  3. The mechanic can be chosen for servicing the car
  4. Shopping for car accessories is available and the order of the same be confirmed and placed
  5. Social space to blog, engage and interact with the B2C community is available
 
 
## Why crAPI?

It is obvious to have a question -- *Why crAPI as a B2C service?* 

The point is not about the business here.  It is about demonstrating the purpose of APIs and how to keep them secure to possible extents.  That way, learning and practicing API Security is demonstrated by programming the vulnerabilities mentioned in OWASP API Top 10.

**This B2C web application will serve as an entry point to the crAPI Web Services**.  Using this car servicing web application we will learn to practice the testing of APIs for their functionality and also for security.

Why the wait?  Get ready to install and spin up the crAPI instance on your box. It is designed to be a lightweight application.


## How to see the crAPI!

 - crAPI is built to be a lightweight application
 - So that anyone can start using it on their machine
 - The goal is, it has to run on a t2.micro machine with 1 CPU and 1 GB
 - crAPI is not for demonstrating the versatility of software system architecture
	 - It is for demonstrating the OWASP API Top 10 vulnerabilities
- crAPI will be a playground to practice the Security Testing of APIs primarily

What does it take to run crAPI on my laptop so that I can install, bring up the instance, and start practicing **without any hustle and being short of resources**?  To keep it simple, it has to be designed and implemented with minimal tech. 


# Architecture of crAPI

One of the primary goals when building a software system is to know -- what problem are we solving with this software?   To solve the problem effectively, the software system has to scale for the context.  

What is the problem statement that we are solving here?  
Problem Statement: **To help and guide in practicing the API Security Testing via ==crAPI== on one's machine**.

How are we doing it?  Is crAPI a monolith or microservice?



## Monolith or Microservice?

In concept, it looks like a microservice in architecture.  But technically it can be seen and works as a monolith.  As said, the goal here is not to demonstrate the software design and architecture.

Today, the web server, database, and browser browsing the crAPI web services via the web pages are all on one and the same machine.  

From a high level, it looks like this:

![a high level view of crAPI](/docs/images/monolith-pic-crapi.png "a high level view of crAPI")


## Architecture of crAPI

On a high level, the crAPI architecture is as below and consists of below components


![the crAPI architecture](/docs/images/crapi-architecture.png "the crAPI architecture")


 - **Web**
	 - This is implemented in JS
	 - This forms the web page of the car servicing business
	 - One signs in here on the web page and accesses the crAPI services
	 - It carries the request to the respective endpoints and brings back the response
	 - Runs on **OpenResty** which has an enhanced version of the Nginx core
 - **Identity**
	 - This is implemented in Java
	 - It is to manage the user account creation and authorization
	 - Create the vehicle and its details with the location
	 - JWT and OTP management is handled by this service
 - **Mailhog**
	 - It is an email testing tool used to simulate the email on user account creation
	 - This works with the Identity Service
 - **Workshop**
	 - This is implemented in Python
	 - It is to create the workshop (car servicing center) and order management
	 - The mechanic (servicing staff) and merchants are created here
 - **Community**
	 - This is implemented in Go
	 - It is for the social space that is to blog and engage the readers with comments
 - **Database**
	 - PostgreSQL or MongoDB
		 - We have a choice to choose the database be it SQL or NoSQL
		 - So one can explore to identify the vulnerabilities with SQL and NoSQL databases
 
