package com.crapi.controller;

import com.crapi.model.CRAPIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

@RestController
@CrossOrigin
public class HealthCheckController {

    @GetMapping("/identity/health_check")
    public ResponseEntity<?> healthCheck(){
         return  ResponseEntity.status(HttpStatus.OK).body(new CRAPIResponse("Okay",200));
    }
}
