# Intro

This is the crAPI challenge solutions page. Go through the [challenges page](challenges.md) to get an idea about which vulnerabilities exist in crAPI.

# Challenge Solutions

It is assumed that the container crapi-web is running in port 8888.
Verify the ports of the container by running the following command : `docker ps`

## BOLA Vulnerabilities

### [Challenge 1 - Access details of another user’s vehicle](challenges.md#challenge-1---access-details-of-another-users-vehicle)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. From the *Dashboard*, choose *Add a Vehicle* and add the vehicle by providing the VIN and pincode received in Mailhog mailbox after Signup or by reinitiating from *Dashboard* page.
3. After the vehicle details are verified successful, the vehicle will get added and then be populated in the *Dashboard* page.
4. Observe the request sent when we click *Refresh Location*. It can be seen that the endpoint is in the format `/identity/api/v2/vehicle/<vehicleid>/location`.
5. Sensitive information like latitude and longitude are provided back in the response for the endpoint. Send the request to *Repeater* for later purpose.
6. Click *Community* in the navbar to visit http://localhost:8888/forum
7. It can be observed that the forum posts are populated based on the response from `/community/api/v2/community/posts/recent` endpoint. On further analysing the response, it can be seen that `vehicleid` is also received back corresponding to the author of each post.
8. Edit the vehicleid in the request sent to *Repeater* in Step 5 with the `vehicleid` received from endpoint `/community/api/v2/community/posts/recent`.
9. Upon sending the request, sensitive details like latitude, longitude and full name are received in the response.

The above challenge was completed using Burp Suite Community Edition.

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

### [Challenge 8 - Get an item for free](challenges.md#challenge-8---get-an-item-for-free)

#### Detailed solution
1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. There is an initial available balance of $100. Try to order the *Seat* item for $10 from the shop by using the *Buy* button and observe the request sent.
4. On observing the POST request `/workshop/api/shop/orders`, it can be observed that `credit` has been reduced by $10 and the current available balance is $90.
5. With this knowledge, we can try to send the captured POST request `/workshop/api/shop/orders` to *Repeater*. 
6. Try to change the value of `quantity` in the request body to a negative value and send the request. It can be observed that the available balance has now increased and the order has been placed.
7. We can verify that the order has been placed by going to the Past Orders section and thus completing the challenge.

The above challenge was completed using Burp Suite Community Edition.

### [Challenge 9 - Increase your balance by $1,000 or more](challenges.md#challenge-9---increase-your-balance-by-1000-or-more)

It is recommended to complete *Challenge 8 - Get an item for free* before attempting this challenge.

#### Detailed solution
1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. There is an initial available balance of $100. Try to order the *Seat* item for $10 from the shop by using the *Buy* button and observe the request sent.
4. On observing the POST request `/workshop/api/shop/orders`, it can be observed that `credit` has been reduced by $10 and the current available balance is $90.
5. With this knowledge, we can try to send the captured POST request `/workshop/api/shop/orders` to *Repeater*. 
6. Try to change the value of `quantity` in the request body to a negative value and send the request. It can be observed that the available balance has now increased and the order has been placed.
7. Inorder to increase the balance by $1000 or more, provide an appropriate value in the ‘quantity’ (ie: -100 or less) and send the request. It can be observed that the available balance has now increased by $1000 or more.

The above challenge was completed using Burp Suite Community Edition.

### Challenge 10 - Update internal video properties

## SSRF

### Challenge 11 - Make crAPI send an HTTP call to https://www.google.com and return the HTTP response. 

## NoSQL Injection

### Challenge 12 - Find a way to get free coupons without knowing the coupon code.

## SQL Injection

### Challenge 13 - Find a way to redeem a coupon that you have already claimed by modifying the database

## Unauthenticated Access

### Challenge 14 - Find an endpoint that does not perform authentication checks for a user.

## << 2 secret challenges >>
