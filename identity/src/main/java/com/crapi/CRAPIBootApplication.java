package com.crapi;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * @author Traceabel AI
 */

/*
 * Need to give path for application.properties file
 * So it will take all the data for database connection from application.properties
 */
/*@PropertySources({
        @PropertySource(value = "file:/home/hasher/Music/resources/application.properties", ignoreResourceNotFound = true)
})*/
@SpringBootApplication(scanBasePackages = {"com.crapi"})
public class CRAPIBootApplication {

    public static void main(String[] args) {

        //ConfigurableApplicationContext context = SpringApplication.run(CRAPIBootApplication.class, args);
        SpringApplication.run(CRAPIBootApplication.class,args);
    }



}
