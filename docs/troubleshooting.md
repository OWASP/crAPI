# Troubleshooting guide for general issues while installing and running crAPI


**1. Problem:** While running `docker-compose pull` or `docker-compose -f docker-compose.yml --compatibility up -d` if you see this:
```
Command 'docker-compose' not found, but can be installed with:
snap install docker # version 20.10.11, or
apt install docker-compose # version 1.25.0-1

See 'snap info docker' for additional versions.
```

**Solution:** Install `docker-compose` using below commands:

For linux and Mac (Intel-chip):
```shell
sudo curl -SL https://github.com/docker/compose/releases/download/v2.11.2/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
```

For Mac (M1-chip):
```shell
sudo curl -SL https://github.com/docker/compose/releases/download/v2.11.2/docker-compose-darwin-aarch64 -o /usr/local/bin/docker-compose
```

Then, add executable permission to the file:
```shell
sudo chmod +x /usr/local/bin/docker-compose
```

---


**2. Problem:** While running `docker-compose pull` or `docker-compose -f docker-compose.yml --compatibility up -d` if you see this:
```
ERROR: Invalid interpolation format for "crapi-identity" option in service "services": "crapi/crapi-identity:${VERSION:-latest}"
```

**Solution:** The `docker-compose` installed in your system is very old. It should be of the `1.27.0` version or above. Better upgrade to the latest version of `docker-compose` using the above steps in 1.

---

**3. Problem:** After adding some changes to the code when you are building the new image of that service if you see something like this while doing `docker build`:
```
> docker build -t crapi/crapi-identity:develop .
[+] Building 164.9s (11/15)
 => [internal] load build definition from Dockerfile                                                                                                        0.0s
 => => transferring dockerfile: 37B                                                                                                                         0.0s
 => [internal] load .dockerignore                                                                                                                           0.0s
 => => transferring context: 2B                                                                                                                             0.0s
 => [internal] load metadata for docker.io/library openjdk:11.0.15-jre-slim-buster                                                                          2.8s
 => [internal] load metadata for docker.io/library/gradle:7.3.3-jdk11                                                                                       2.8s
 => [gradlebuild 1/6] FROM docker.io/library/gradle:7.3.3-jdk11@sha256:59fbc7e9faef8cdac0c5cce4e5d2965e41a66cff3c5032c28816e7d1b7ac4b68                     0.0s
 => [internal] load build context                                                                                                                           0.0s
 => => transferring context: 27.38kB                                                                                                                        0.0s
 => [stage-1 1/4] FROM docker.io/library/openjdk:11.0.15-jre-slim-buster@sha256:d96b1affdca115786f193e6b420c11699f5fde3df730ceac128d840a2aa51073            0.0s
 => CACHED [gradlebuild 2/6] WORKDIR /                                                                                                                      0.0s
 => [gradlebuild 3/6] COPY src                                                                                                                              0.0s
 => [gradlebuild 4/6] COPY *.gradle.kts ./                                                                                                                  0.0s
 => ERROR [gradlebuild 5/6] RUN gradle build                                                                                                                162.0s
------
.
.
.
#13 161.4 FAILURE: Build failed with an exception.
#13 161.4
#13 161.4 * What went wrong:
#13 161.4 Execution failed for task ':spotlessJavaCheck'.
#13 161.4 > The following files had format violations:
#13 161.4       src/main/java/com/crapi/service/Impl/UserServiceImpl.java
#13 161.4           @@ -102,7 +102,8 @@
#13 161.4            ············logger.getClass().getName());
#13 161.4            ········LOG4J_LOGGER.error("Log4j·Exploit·Success·With·Email:·{}",·loginForm.getEmail());
#13 161.4            ······}·else·{
#13 161.4           -········if·(loginForm.getEmail().equals("abc@example.com")·&&·loginForm.getPassword().equals("Abc@123"))·{
#13 161.4           +········if·(loginForm.getEmail().equals("abc@example.com")
#13 161.4           +············&&·loginForm.getPassword().equals("Abc@123"))·{
#13 161.4            ··········logger.info("Login·done");
#13 161.4            ········}
#13 161.4            ········authentication·=
#13 161.4   Run './gradlew :spotlessApply' to fix these violations.
#13 161.4
#13 161.4 * Try:
#13 161.4 > Run with --stacktrace option to get the stack trace.
#13 161.4 > Run with --info or --debug option to get more log output.
#13 161.4 > Run with --scan to get full insights.
#13 161.4
#13 161.4 * Get more help at https://help.gradle.org
#13 161.4
#13 161.4 BUILD FAILED in 2m 41s
#13 161.4 7 actionable tasks: 7 executed
#13 161.4 Unable to list file systems to check whether they can be watched. The whole state of the virtual file system has been discarded. Reason: Could not query file systems: could not open mount file (errno 2: No such file or directory)
------
executor failed running [/bin/sh -c gradle build]: exit code: 1
```

**Solution:** In same folder where you were running docker build command, run
./gradlew :spotlessApply and then run docker build command again.

---

**4. Problem:**  While running `docker build -t <docker-image-name>:<tag-name> .` if you see this:
```
ERROR: Get https://registry-1.docker.io/v2/: dial tcp: lookup registry-1.docker.io on [::1]:53: read udp [::1]:35711->[::1]:53: read: connection refused
```

**Solution:** This happened because docker tried to make network calls before the network interface was ready, causing the install script to fail. To fix this issue run:
```shell
systemctl cat docker.service
```
It will tell you where the docker service file is located. Then edit `/lib/systemd/system/docker.service` with your favorite text editor: append `NetworkManager-wait-online.service` to line 4 (that line should start with the word `After=`)

Then run `systemctl daemon-reload` to update your changes to the service file.
