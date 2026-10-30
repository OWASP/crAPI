/*
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Need to give path for application.properties file
 * So it will take all the data for database connection from application.properties
 */
/*@PropertySources({
        @PropertySource(value = "file:/home/hasher/Music/resources/application.properties", ignoreResourceNotFound = true)
})*/
/**
 * Entry point of the Identity service (container {@code crapi-identity}, the only JWT issuer in
 * crAPI).
 *
 * <p>Boots Spring and component-scans {@code com.crapi}: the REST layer in {@code controller},
 * business logic in {@code service.Impl}, JPA entities/repositories backed by PostgreSQL, and the
 * security wiring in {@code config}. Once the context is ready, {@code InitialDataConfig} seeds the
 * default users and vehicles.
 *
 * <p>Other services never talk to this class directly: {@code crapi-web} proxies {@code /identity/}
 * here, and Community/Workshop call {@code /identity/api/auth/verify} over the Docker network to
 * validate tokens.
 */
@SpringBootApplication(scanBasePackages = {"com.crapi"})
public class CRAPIBootApplication {

  public static void main(String[] args) {

    // ConfigurableApplicationContext context = SpringApplication.run(CRAPIBootApplication.class,
    // args);
    SpringApplication.run(CRAPIBootApplication.class, args);
  }
}
