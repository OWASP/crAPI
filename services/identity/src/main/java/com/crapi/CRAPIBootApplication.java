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
 * @author Traceable AI
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
