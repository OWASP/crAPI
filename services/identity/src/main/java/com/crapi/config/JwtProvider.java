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

package com.crapi.config;

import com.crapi.entity.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.text.ParseException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

  @Value("${app.jwtExpiration}")
  private int jwtExpiration;

  private KeyPair keyPair;

  private RSAKey publicRSAKey;

  private Map<String, Object> publicJwkSet;

  public JwtProvider(@Value("${app.jwksJson}") String jwksJson) {
    try {
      Base64.Decoder decoder = Base64.getDecoder();
      InputStream jwksStream = new ByteArrayInputStream(decoder.decode(jwksJson));
      JWKSet jwkSet = JWKSet.load(jwksStream);
      List<JWK> keys = jwkSet.getKeys();
      if (keys.size() != 1 || !Objects.equals(keys.get(0).getAlgorithm().getName(), "RS256")) {
        throw new RuntimeException("Invalid JWKS key passed!!!");
      }

      RSAKey rsaKey = keys.get(0).toRSAKey();
      this.publicRSAKey = rsaKey.toPublicJWK();
      this.keyPair = rsaKey.toKeyPair();
      this.publicJwkSet = jwkSet.toJSONObject();
    } catch (IOException | ParseException | JOSEException e) {
      throw new RuntimeException(e);
    }
  }

  public String getPublicJwkSet() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(this.publicJwkSet);
  }

  /**
   * @param user
   * @return generated token with expire date
   */
  public String generateJwtToken(User user) {
    return Jwts.builder()
        .setSubject((user.getEmail()))
        .claim("role", user.getRole().getName())
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpiration))
        .signWith(SignatureAlgorithm.RS256, this.keyPair.getPrivate())
        .compact();
  }

  /**
   * @param token
   * @return username from JWT Token
   */
  public String getUserNameFromJwtToken(String token) throws ParseException {
    // Parse without verifying token signature
    return JWTParser.parse(token).getJWTClaimsSet().getSubject();
  }

  // Load RSA Public Key for JKU header if present
  private RSAKey getKeyFromJkuHeader(JWSHeader header) {
    try {
      URI jku = header.getJWKURL();
      if (jku != null) {
        URLConnection connection = jku.toURL().openConnection();
        JWKSet jwkSet = JWKSet.load(connection.getInputStream());
        logger.info("JWKSet from URL : " + jwkSet.toString(false));
        JWK key = jwkSet.getKeyByKeyId(header.getKeyID());
        if (key != null && Objects.equals(key.getAlgorithm().getName(), "RS256")) {
          return key.toRSAKey().toPublicJWK();
        }
      }
    } catch (IOException | ParseException e) {
      return null;
    }

    return null;
  }

  // Construct secret for JWT Verification through HS256 (vulnerability)
  private String getJwtSecret(JWSHeader header) throws JOSEException {
    // defaultSecret is RSA Public Key as String
    // Algorithm Confusion Attack
    Base64.Encoder encoder = Base64.getEncoder();
    String defaultSecret = encoder.encodeToString(this.publicRSAKey.toPublicKey().getEncoded());

    // Check if KID header is pointing to /dev/null file
    String kid = header.getKeyID();
    if (kid != null && kid.contains("/dev/null")) {
      return "AA==";
    }

    return defaultSecret;
  }

  /**
   * @param authToken
   * @return validate token expire and true boolean
   */
  public boolean validateJwtToken(String authToken) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(authToken);
      JWSHeader header = signedJWT.getHeader();
      Algorithm alg = header.getAlgorithm();

      // JWT Algorithm confusion vulnerability
      logger.info("Algorithm: " + alg.getName());
      if (Objects.equals(alg.getName(), "HS256")) {
        String secret = getJwtSecret(header);
        logger.info("JWT Secret: " + secret);
        Jwts.parser()
            .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
            .parseClaimsJws(authToken);
        return true;
      } else {
        RSAKey verificationKey = getKeyFromJkuHeader(header);
        JWSVerifier verifier;
        if (verificationKey == null) {
          verifier = new RSASSAVerifier(this.publicRSAKey);
        } else {
          logger.info("Key from JKU: " + verificationKey.toJSONString());
          verifier = new RSASSAVerifier(verificationKey);
        }

        return signedJWT.verify(verifier);
      }

    } catch (ParseException e) {
      logger.error("Could not parse JWT Token -> Message: %d", e);
    } catch (JOSEException e) {
      logger.error("RSA JWK Extraction failed -> Message: %d", e);
    }

    return false;
  }
}
