# Intro

This is the crAPI challenge solutions page. Go through the [challenges page](docs/challenges.md) to get an idea about which vulnerabilities exist in crAPI.

# Challenge Solutions

It is assumed that the container crapi-web is running in port 8888.
Verify the ports of the container by running the following command : `docker ps`

## BOLA Vulnerabilities

### Challenge 1 - Access details of another user’s vehicle

### Challenge 2 - Access mechanic reports of other users

## Broken User Authentication

### Challenge 3 - Reset the password of a different user

## Excessive Data Exposure

### Challenge 4 - Find an API endpoint that leaks sensitive information of other users

### Challenge 5 - Find an API endpoint that leaks an internal property of a video

## Rate Limiting

### Challenge 6 - Perform a layer 7 DoS using ‘contact mechanic’ feature

## BFLA 

### Challenge 7 - Delete a video of another user

## Mass Assignment

### [Challenge 8 - Get an item for free](docs/challenges.md#challenge-8---get-an-item-for-free)

#### Detailed solution
1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. There is an initial available balance of $100. Try to order the *Seat* item for $10 from the shop by using the *Buy* button and observe the request sent.
4. On observing the POST request `/workshop/api/shop/orders`, it can be observed that `credit` has been reduced by $10 and the current available balance is $90.
5. With this knowledge, we can try to send the captured POST request `/workshop/api/shop/orders` to Repeater. 
6. Try to change the value of `quantity` in the request body to a negative value and send the request. It can be observed that the available balance has now increased and the order has been placed.
7. We can verify that the order has been placed by going to the Past Orders section and thus completing the challenge.

The above challenge was completed using Burp Suite Community Edition.

### [Challenge 9 - Increase your balance by $1,000 or more](docs/challenges.md#challenge-9---increase-your-balance-by-1000-or-more)

It is recommended to complete *Challenge 8 - Get an item for free* before attempting this challenge.

#### Detailed solution
1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. There is an initial available balance of $100. Try to order the *Seat* item for $10 from the shop by using the *Buy* button and observe the request sent.
4. On observing the POST request `/workshop/api/shop/orders`, it can be observed that `credit` has been reduced by $10 and the current available balance is $90.
5. With this knowledge, we can try to send the captured POST request `/workshop/api/shop/orders` to Repeater. 
6. Try to change the value of `quantity` in the request body to a negative value and send the request. It can be observed that the available balance has now increased and the order has been placed.
7. Inorder to increase the balance by $1000 or more, provide an appropriate value in the ‘quantity’ (ie: -100 or less) and send the request. It can be observed that the available balance has now increased by $1000 or more.

The above challenge was completed using Burp Suite Community Edition.

### Challenge 10 - Update internal video properties

## SSRF

### Challenge 11 - Make crAPI send an HTTP call to "[www.google.com](www.google.com)" and return the HTTP response. 

## NoSQL Injection

### Challenge 12 - Find a way to get free coupons without knowing the coupon code.

## SQL Injection

### Challenge 13 - Find a way to redeem a coupon that you have already claimed by modifying the database

## Unauthenticated Access

### Challenge 14 - Find an endpoint that does not perform authentication checks for a user.

## << 2 secret challenges >>
