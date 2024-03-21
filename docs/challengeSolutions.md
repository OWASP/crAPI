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

### [Challenge 2 - Access mechanic reports of other users](challenges.md#challenge-2---access-mechanic-reports-of-other-users)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. After adding a vehicle, we will have an option to send service request to mechanic by using the *Contact Mechanic* option.
3. Observe the request sent after the *Send Service Request*. In the response of `/workshop/api/merchant/contact_mechanic`, we will be able to find the hidden API endpoint in `report_link`.
4. Go to the link present as value in `report_link`.
5. Change the value of report_id in the request and send it to access mechanic reports of other users.


## Broken User Authentication

### [Challenge 3 - Reset the password of a different user](challenges.md#challenge-3---Reset-the-password-of-a-different-user)

#### Detailed solution

1. Go to the login page http://localhost:8888/login and click on forgot password and you will be brought to http://127.0.0.1:8888/forgot-password.
2. Enter **Email** of the user you are targeting for password reset.
3. Enter **OTP** and **New Password** and capture the request in burp proxy, the endpoint would be /identity/api/auth/v3/check-otp.
4. Send the request to **Intruder** and change _v3_ to _v2_ in `/identity/api/auth/v3/check-otp` as v3 has a rate-limit set.
5. select the value of otp parameter as payload and load https://raw.githubusercontent.com/danielmiessler/SecLists/master/Fuzzing/4-digits-0000-9999.txt as wordlist.
6. start the attack and soon you would see a request with a different length containing message "OTP Verified".
7. Now you can login with the new password on http://localhost:8888/login
   

## Excessive Data Exposure

### [Challenge 4 - Find an API endpoint that leaks sensitive information of other users](challenges.md#challenge-4---Find-an-API-endpoint-that-leaks-sensitive-information-of-other-users)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. Click *Community* in the navbar to visit http://localhost:8888/forum
3. Observer the reponse of http://localhost:8888/forum in burp, the endpoint would be /community/api/v2/community/posts/recent
4. You would see excessive details in response like email and vehicleid.

### [Challenge 5 - Find an API endpoint that leaks an internal property of a video](challenges.md#challenge-5---Find-an-API-endpoint-that-leaks-an-internal-property-of-a-video)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. Click *Profile Icon* in the navbar to visit http://127.0.0.1:8888/my-profile
3. Upload a video in "My Personal Video" section.
4. Click on the 3 dots in front of "My Personal Video" and select "Change Video Name" option. Change the video name to anything.
5. Observer the response of "Change Video Name" in burp, the endpoint would be like `/identity/api/v2/user/videos/{VIDEO_ID}`
6. You would see excessive details about the video like **conversion_params**.

## Rate Limiting

### [Challenge 6 - Perform a layer 7 DoS using ‘contact mechanic’ feature](challenges.md#challenge-6---Perform-a-layer-7-DoS-using-contact-mechanic-feature)

### Detailed solution

1. Login to the application from http://localhost:8888/login
2. After adding a vehicle, we will have an option to send service request to mechanic by using the *Contact Mechanic* option.
3. Observe the request sent after the *Send Service Request*. It would be on the endpoint of `/workshop/api/merchant/contact_mechanic`.
4. In the request change the value of `repeat_request_if_failed` to **true** and `number_of_repeats` to **1000** and send the request.
5. You will get `Service unavailable. Seems like you caused layer 7 DoS :)` in response.

## BFLA 

### [Challenge 7 - Delete a video of another user](challenges.md#challenge-7---Delete-a-video-of-another-user)

### Detailed solution

1. Login to the application from http://localhost:8888/login
2. Click *Profile Icon* in the navbar to visit http://127.0.0.1:8888/my-profile
3. Upload a video in "My Personal Video" section.
4. Click on the 3 dots in front of "My Personal Video" and select the "Change Video Name" option. Chnage the video name to anything.
5. Observer the request of "Change Video Name" in burp, the endpoint would be like `/identity/api/v2/user/videos/{VIDEO_ID}`
6. Change request method from `PUT` to `DELETE` and change `user` in /identity/api/v2/user/videos/{VIDEO_ID} to `admin`, it should look like this `/identity/api/v2/admin/videos/{VIDEO_ID}`.
7. Now send the request and you will get a message saying `User video deleted successfully`.

## Mass Assignment

### [Challenge 8 - Get an item for free](challenges.md#challenge-8---get-an-item-for-free)

#### Detailed solution
1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. There is an initial available balance of $100. Try to order the *Seat* item for $10 from the shop by using the *Buy* button and observe the request sent.
4. On observing the POST request `/workshop/api/shop/orders`, it can be observed that `credit` has been reduced by $10 and the current available balance is $90.
5. On `http://127.0.0.1:8888/shop` click on `Past Orders`. you will brought to `http://127.0.0.1:8888/past-orders`.
6. Then click on `Order Details` of the the seat you ordered. Endpoint would be `/orders?order_id={ORDER_ID}`.
7. Observer the response of `Order Details` in burp, you should see `status` is set to `delivered` in the reponse.
8. Change the request method from `GET` to `PUT` in request and add `{"status":"returned"}` as the data of request, now send the request.
9. You would see status value changed from `delivered` to `returned` and your credit will increase by 10.

### [Challenge 9 - Increase your balance by $1,000 or more](challenges.md#challenge-9---increase-your-balance-by-1000-or-more)

#### Detailed solution

##### Method 1

1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. There is an initial available balance of $100. Try to order the *Seat* item for $10 from the shop by using the *Buy* button and observe the request sent.
4. On observing the POST request `/workshop/api/shop/orders`, it can be observed that `credit` has been reduced by $10 and the current available balance is $90.
5. On `http://127.0.0.1:8888/shop` click on `Past Orders`. you will brought to `http://127.0.0.1:8888/past-orders`.
6. Then click on `Order Details` of the the seat you ordered. Endpoint would be `/orders?order_id={ORDER_ID}`.
7. Observer the response of `Order Details` in burp, you should see `status` is set to `delivered` in the reponse and `quantity` is set to `1`.
8. Change the request method from `GET` to `PUT` in request and add `{"status":"returned","quantity":"100"}` as the data of request, now send the request.
9. You would see status value and quantity changed in response and your credit will increase by 1000.

##### Method 2

1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. There is an initial available balance of $100. Try to order the *Seat* item for $10 from the shop by using the *Buy* button and observe the request sent.
4. On observing the POST request `/workshop/api/shop/orders`, it can be observed that `credit` has been reduced by $10 and the current available balance is $90.
5. With this knowledge, we can try to send the captured POST request `/workshop/api/shop/orders` to *Repeater*. 
6. Try to change the value of `quantity` in the request body to a negative value and send the request. It can be observed that the available balance has now increased and the order has been placed.
7. Inorder to increase the balance by $1000 or more, provide an appropriate value in the ‘quantity’ (ie: -100 or less) and send the request. It can be observed that the available balance has now increased by $1000 or more.

### [Challenge 10 - Update internal video properties](challenges.md#challenge-10---Update-internal-video-properties)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. Click *Profile Icon* in the navbar to visit http://127.0.0.1:8888/my-profile
3. Upload a video in "My Personal Video" section.
4. Click on the 3 dots in front of "My Personal Video" and select "Change Video Name" option. Change the video name to anything.
5. Observer the request of "Change Video Name" in burp, the endpoint would be like `/identity/api/v2/user/videos/{VIDEO_ID}`
6. Add `"conversion_params":"-v codec h264 && whoami"` in the JSON data of the request and send the request.
7. In the reponse you will see `conversion_params` value changed to `-v codec h264 && whoami`.

## SSRF

### [Challenge 11 - Make crAPI send an HTTP call to https://www.google.com and return the HTTP response.](challenges.md#challenge-11---make-crapi-send-an-http-call-to-wwwgooglecom-and-return-the-http-response)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. After adding a vehicle, we will have an option to send service request to mechanic by using the *Contact Mechanic* option.
3. Observe the request sent after the *Send Service Request*. In the request of `/workshop/api/merchant/contact_mechanic` we are sending the `mechanic_api` to server.
4. Change the value of `mechanic_api` to https://google.com in the request and send the request.
5. You will see the response of https://google.com inside the response of `/workshop/api/merchant/contact_mechanic`.

## NoSQL Injection

### [Challenge 12 - Find a way to get free coupons without knowing the coupon code.](challenges.md#challenge-12---Find-a-way-to-get-free-coupons-without-knowing-the-coupon-code)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. Click *Add Coupons* button and enter any random string as coupon and click on **validate**.
4. Observer the last request sent to `/community/api/v2/coupon/validate-coupon` endpoint.
5. Change the value of `coupon_code` inside JSON data to `{"$ne": null}` and send the request.
6. You will get the coupon code in response.

## SQL Injection

### [Challenge 13 - Find a way to redeem a coupon that you have already claimed by modifying the database](challenges.md#challenge-13---Find-a-way-to-redeem-a-coupon-that-you-have-already-claimed-by-modifying-the-database)

#### Detailed solution

1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. Click *Add Coupons* button and enter a valid coupon code and click on **validate**.
4. Observer the last request sent to `/workshop/api/shop/apply_coupon` endpoint.
5. Change the value of `coupon_code` inside JSON data to `';SELECT current_database();--` and send the request. 
6. You would see the current database name in response.

## Unauthenticated Access

### [Challenge 14 - Find an endpoint that does not perform authentication checks for a user.](challenges.md#challenge-14---Find-an-endpoint-that-does-not-perform-authentication-checks-for-a-user)

#### Detailed solution

##### First Endpoint 

1. Login to the application from http://localhost:8888/login
2. Click *Shop* in the navbar to visit http://localhost:8888/shop
3. Oder the *Seat* item for $10 from the shop by using the *Buy* button.
4. On `http://127.0.0.1:8888/shop` click on `Past Orders`. you will brought to `http://127.0.0.1:8888/past-orders`.
5. Then click on `Order Details` of the the seat you ordered. Endpoint would be Endpoint would be `/orders?order_id={ORDER_ID}`.
6. Observer the request of `Order Details` in burp, remove the Authorization header from the request and send the request.
7. You will be able to see the details of the order. You can change the value of `order_id` in http://127.0.0.1:8888/orders?order_id={ORDER_ID} to view other users order deatils without Authorization.

##### Second Endpoint

1. Login to the application from http://localhost:8888/login
2. After adding a vehicle, we will have an option to send service request to mechanic by using the *Contact Mechanic* option.
3. Observe the request sent after the *Send Service Request*. In the response of `/workshop/api/merchant/contact_mechanic`, we will be able to find the hidden API endpoint in `report_link`.
4. Go to the link present as value in `report_link`.
5. Change the value of report_id in the request and send it to access mechanic reports of other users without Authorization.

## JWT Vulnerabilities

## [Challenge 15 - Find a way to forge valid JWT Tokens](challenges.md#challenge-15---find-a-way-to-forge-valid-jwt-tokens)

#### Detailed Solution

##### crAPI is vulnerable to the following JWT Vulnerabilities
1. JWT Algorithm Confusion Vulnerability
   - crAPI uses RS256 JWT Algorithm by default
   - Public Key to verify JWT is available at http://localhost:8888/.well-known/jwks.json
   - Convert the public key to base64 encoded form and use it as a secret to create a JWT in HS256 Algorithm
   - This JWT will be accepted as a valid JWT Token by crAPI
2. Invalid Signature Vulnerability
   - User Dashboard API is not validating JWT signature
   - Create a JWT with `sub` header set to a different user's email
   - With the above JWT you will be able to extract user data from user dashboard API endpoint
3. JKU Misuse Vulnerability
   - crAPI will verify JWT token with any public key that is pointed to by the `jku` JWT header
   - Create your own public/private key pair and sign a JWT in RS256 Algorithm
   - Host the public key somewhere in JWK format
   - Pass the public key URL in `jku` header of the JWT with appropriate `kid` header
   - This JWT will be accepted as a valid JWT Token by crAPI
4. KID Path Traversal Vulnerability
   - Set the `kid` header of JWT to `../../../../../../dev/null`
   - Create a custom JWT in HS256 algorithm with secret as `AA==`
     - `AA==` is the Base64 encoded form of Hex null byte `00`
   - This JWT will be accepted as a valid JWT Token by crAPI

## << 2 secret challenges >>
