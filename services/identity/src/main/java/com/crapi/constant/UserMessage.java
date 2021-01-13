/*
 * Copyright 2020 Traceable, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crapi.constant;

/**
 * @author Traceable AI
 */

public class UserMessage {

    public static final String INVALID_CREDENTIALS = "Invalid Credentials";
    public static final String SIGN_UP_SUCCESS_MESSAGE = "User registered successfully! Please Login.";
    public static final String SIGN_UP_FAILED = "User registered failed! Please retry.";
    public static final String NUMBER_ALREADY_REGISTERED = "Number already registered! Number: ";
    public static final String EMAIL_ALREADY_REGISTERED = "Email already registered! Email: ";
    public static final String GIVEN_URL_ALREADY_USED = "Given URL is already used! Please try to login..";
    public static final String EMAIL_NOT_REGISTERED = "Given Email is not registered! ";
    public static final String INVALID_OTP = "Invalid OTP! Please try again..";
    public static final String ERROR = "ERROR..";
    public static final String OTP_VARIFIED_SUCCESS = "OTP verified";
    public static final String OTP_SEND_SUCCESS_ON_EMAIL = "OTP Sent on the provided email, ";
    public static final String EXCEED_NUMBER_OF_ATTEMPS = "You've exceeded the number of attempts.";
    public static final String PASSWORD_GOT_RESET = "Password reset successful.";
    public static final String VEHICLE_MODEL_IS_NOT_AVAILABLE = "Sorry we don't have Vehicle model for this company. Please select other..";
    public static final String VEHICLE_SAVED_SUCCESSFULLY = "Vehicle save successfully..";
    public static final String VEHICLE_NOT_FOUND = "Failed to get Vehicles";
    public static final String VEHICLE_DETAILS_SENT_TO_EMAIL = "Your newly purchased Vehicle Details have been sent to you email address. If you have used example.com email, check your email using the MailHog web portal. ";
    public static final String CHANGE_EMAIL_MESSAGE = "The token has been sent to your email. If you have used example.com email, check your email using the MailHog web portal.   ";
    public static final String CHANGE_EMAIL_OLD_USEREMAIL_NOT_FOUND_MESSAGE = "Sorry, we didn't find any user for ";
    public static final String INVALID_EMAIL_TOKEN = "Sorry, Token did't match";
    public static final String OLD_MAIL_DOES_NOT_BELONG = "Fail, email address parameter doesn’t belong to the user";
    public static final String NEW_MAIL_DOES_NOT_BELONG = "Fail, new email address parameter doesn’t match with token";
    public static final String EMAIL_CHANGE_SUCCESSFUL = "Your Email changed successful. Please login with updated email. ";
    public static final String VEHICLE_ALREADY_CREATED = "Sorry, This Vehicle is already added.";
    public static final String LOGIN_WITH_EMAIL_TOKEN = "Successfully logged in with email token.";
    public static final String TOKEN_VERIFICATION_FAILOLD = "Our security team made us remove the \"Login with token\" feature because it's not secure enough. It's not available after API version 2.7";
    public static final String TOKEN_VERIFICATION_MISSING = "Missing data. Field is null: ";
    public static final String DID_NOT_GET_VEHICLE_FOR_USER = "Sorry we didn't get vehicle for user: ";
    public static final String PROFILE_PICTURE_UPDATE = "Profile picture updated successfully.";
    public static final String VIDEO_DELETED_SUCCESS_MESSAGE = "User video deleted successfully.";
    public static final String YOU_HAVE_VIDEO_UPLOADED = "You have already uploaded a video. Please try with change video";

    public static final String CONVERT_VIDEO_INTERNAL_USE_ONLY = "Thi-S endpoint S-hould be accessed only inte-Rnally. -Fine? , endpoint_url http://crapi-identity:8080/identity/api/v2/user/videos/convert_video";
    public static final String CONVERT_VIDEO_INTERNAL_ERROR = "Error occured while executing.";
    public static final String CONVERT_VIDEO_CLOSE_TO_WIN_THE_GAME = "You are very close.";
    public static final String CONVERT_VIDEO_BASH_COMMAND_TRIGGERED = "Video conversion bash command triggered.";
    public static final String SORRY_DIDNT_GET_PROFILE = "Sorry, Didn't get any profile video name for the user.";
    public static final String THIS_IS_ADMIN_FUNCTION = "This is an admin function. Try to access the admin API";
    public static final String YOU_WON_THE_GAME = "You won the game.";
    public static final String USER_VIDEO_NAME_CHANGED = "Video name changed successfully..";
    public static final String CONVERT_VIDEO_PARAM_IS_MISSING = "Video_id param is missing";
    public static final String VIDEO_WAS_NOT_FOUND = "video wasn’t found";
    public static final String VIDEO_NOT_FOUND = "Video not found.";
    //public static final String
    // Exception Message
    public static final String INTERNAL_SERVER_ERROR_UNABLE_TO_CREATE_ACCOUNT = "Internal Server Error. Unable to create account..";
    public static final String INTERNAL_SERVER_ERROR = "Internal Issue! Please try after some time";
    public static final String CUSTOM_IO_EXCEPTION = "com.crapi.exception: IO Exception";
    public static final String CUSTOM_IO_EXCEPTION_UNABLE_TO_CREATE_VEHICLE = "Sorry! Unable to create the vehicle";
    public static final String CONVERSION_VIDEO_OK =  "V09XLiBMb29rIGF0IHlvdS4gQ29tYmluaW5nIE1hc3MgQXNzaWdubWVudCBh"
                                                    + "bmQgU1NSRiB0byBleHBsb2l0IGEgaGlkZGVuIFNoZWxsIEluamVjdGlvbi4K"
                                                    + "V2UncmUgdmVyeSBwcm91ZC4gWW91IHdvbiB0aGUgZ2FtZS4gVW5mb3J0dW5h"
                                                    + "dGVseSwgd2UgZG9uJ3QgYWN0dWFsbHkgIAphbGxvdyBvdXIgcGVudGVzdGVy"
                                                    + "IHRvIHJ1biBhcmJpdHJhcnkgc2hlbGwgY29tbWFuZHMgb24gb3VyIGJhY2tl"
                                                    + "bmQgOiggCkl0IGNvdWxkIGJlIHZlcnkgZXhwZW5zaXZlIGFuZCBhbm5veWlu"
                                                    + "ZyB0byBtYWludGFpbiBpdC4gCkFueWhvdywgaWYgeW91IGhvc3QgeW91ciBv"
                                                    + "d24gaW5zdGFuY2Ugb2YgY3JBUEksIHlvdSBjYW4gc2ltcGx5IGNoYW5nZSB0"
                                                    + "aGUKYGJsb2NrX3NoZWxsX2luamVjdGlvbnNgIGZsYWcgaW4gdGhlIGNvbmZp"
                                                    + "ZyBmaWxlIHRvIGFsbG93IHJlYWwgc2hlbGwgaW5qZWN0aW9ucy4=";



}
