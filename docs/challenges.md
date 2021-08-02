# Intro

This is the crAPI challenge page.

crAPI (Completely Ridiculous Application Programmer Interface) defines an API which is intentionally vulnerable to the OWASP API Top 10 vulnerabilities. crAPI is meant to illustrate and educate by presenting these issues for you to discover and exploit.

The crAPI challenge is for you to find and exploit as many of these vulnerabilities as you can.

There are two approaches to hack crAPI - the first is to look at it as a complete black box test, where you get no directions, but just try to understand the app from scratch and hack it.

The second approach is using this page, which will give you an idea about which vulnerabilities exist in crAPI and will direct you on how to exploit them. 

# Challenges

## BOLA Vulnerabilities

### Challenge 1 - Access details of another user’s vehicle

To solve the challenge, you need to leak sensitive information of another user’s vehicle.

* Since vehicle IDs are not sequential numbers, but GUIDs, you need to find a way to expose the vehicle ID of another user.

* Find an API endpoint that receives a vehicle ID and returns information about it.

### Challenge 2 - Access mechanic reports of other users

crAPI allows vehicle owners to contact their mechanics by submitting a "contact mechanic" form. This challenge is about accessing mechanic reports that were submitted by other users.

* Analyze the report submission process

* Find an hidden API endpoint that exposes details of a mechanic report

* Change the report ID to access other reports

## Broken User Authentication

### Challenge 3 - Reset the password of a different user

* Find an email address of another user on crAPI

* Brute forcing might be the answer. If you face any protection mechanisms, remember to leverage the predictable nature of REST APIs to find more similar API endpoints.

## Excessive Data Exposure

### Challenge 4 - Find an API endpoint that leaks sensitive information of other users

### Challenge 5 - Find an API endpoint that leaks an internal property of a video

In this challenge, you need to find an internal property of the video resource that shouldn’t be exposed to the user. This property name and value can help you to exploit other vulnerabilities.

## Rate Limiting

### Challenge 6 - Perform a layer 7 DoS using ‘contact mechanic’ feature

## BFLA 

### Challenge 7 - Delete a video of another user

* Leverage the predictable nature of REST APIs to find an admin endpoint to delete videos

* Delete a video of someone else

## Mass Assignment

### Challenge 8 - Get an item for free

crAPI allows users to return items they have ordered. You simply click the "return order" button, receive a QR code and show it in a USPS store.
To solve this challenge, you need to find a way to get refunded for an item that you haven’t actually returned.

* Leverage the predictable nature of REST APIs to find a shadow API endpoint that allows you to edit properties of a specific order.

### Challenge 9 - Increase your balance by $1,000 or more

After solving the "Get an item for free" challenge, be creative and find a way to get refunded for an item you never returned, but this time try to get a bigger refund.

### Challenge 10 - Update internal video properties

After solving the "Find an API endpoint that leaks an internal property of videos" challenge, try to find an endpoint that would allow you to change the internal property of the video. Changing the value can help you to exploit another vulnerability.

## SSRF

### Challenge 11 - Make crAPI send an HTTP call to "[www.google.com](www.google.com)" and return the HTTP response. 

## NoSQL Injection

### Challenge 12 - Find a way to get free coupons without knowing the coupon code.

## << 2 secret challenges >>

There are two more secret challenges in crAPI, that are pretty complex, and for now we don’t share details about them, except the fact they are really cool. 
