package com.crapi.controller;

import com.crapi.model.ChangeEmailForm;
import com.crapi.model.CRAPIResponse;
import com.crapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/identity/api")
public class ChangeEmailController {

    @Autowired
    UserService userService;

    /**
     * @param changeEmailForm changeEmailForm contains old email and new email, api will send change email token to new email address.
     * @param request getting jwt token for user from request header
     * @return first check email is already registered or not if it is there then
     * return email already registered then try with new email.
     */
    @PostMapping("/v2/user/change-email")
    public ResponseEntity<CRAPIResponse> changesEmail(@Valid @RequestBody ChangeEmailForm changeEmailForm, HttpServletRequest request){
        CRAPIResponse changeEmailResponse = userService.changeEmailRequest(request,changeEmailForm);
        if (changeEmailResponse!=null && changeEmailResponse.getStatus()==403) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(changeEmailResponse);
        }else if (changeEmailResponse!=null && changeEmailResponse.getStatus()==404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(changeEmailResponse);
        }
            return ResponseEntity.status(HttpStatus.OK).body(changeEmailResponse);

    }

    /**
     * @param changeEmailForm changeEmailForm contains old email and new email, with token, this function will verify email and token
     * @param request getting jwt token for user from request header
     * @return verify token if it is valid then it will update the user email
     */
    @PostMapping("/v2/user/verify-email-token")
    public ResponseEntity<CRAPIResponse> verifyEmailToken(@RequestBody ChangeEmailForm changeEmailForm,HttpServletRequest request){
        CRAPIResponse verifyEmailTokenResponse = userService.verifyEmailToken(request,changeEmailForm);
        if (verifyEmailTokenResponse!=null && verifyEmailTokenResponse.getStatus()==200){
            return ResponseEntity.status(HttpStatus.OK).body(verifyEmailTokenResponse);
        }
        else if(verifyEmailTokenResponse!=null && verifyEmailTokenResponse.getStatus()==404){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(verifyEmailTokenResponse);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(verifyEmailTokenResponse);
    }

}
