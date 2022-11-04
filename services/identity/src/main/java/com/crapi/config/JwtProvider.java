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
import io.jsonwebtoken.*;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
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

  public JwtProvider(
      @Value("${app.jwtPrivateKey}") String privateKey,
      @Value("${app.jwtPublicKey}") String publicKey) {
    try {
      Base64.Decoder decoder = Base64.getDecoder();
      byte[] publicBytes = decoder.decode(publicKey);
      byte[] privateBytes = decoder.decode(privateKey);

      X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(publicBytes);
      PKCS8EncodedKeySpec PKCS8privateKey = new PKCS8EncodedKeySpec(privateBytes);

      KeyFactory kf = KeyFactory.getInstance("RSA");
      RSAPublicKey rsaPubKey = (RSAPublicKey) kf.generatePublic(X509publicKey);
      RSAPrivateKey rsaPrivKey = (RSAPrivateKey) kf.generatePrivate(PKCS8privateKey);

      this.keyPair = new KeyPair(rsaPubKey, rsaPrivKey);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
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
  public String getUserNameFromJwtToken(String token) {
    return Jwts.parser()
        .setSigningKey(this.keyPair.getPublic())
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  /**
   * @param authToken
   * @return validate token expire and true boolean
   */
  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(this.keyPair.getPublic()).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException e) {
      logger.error("Invalid JWT signature -> Message: %d ", e);
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token -> Message: %d", e);
    } catch (ExpiredJwtException e) {
      logger.error("Expired JWT token -> Message: %d", e);
    } catch (UnsupportedJwtException e) {
      logger.error("Unsupported JWT token -> Message: %d", e);
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty -> Message: %d", e);
      //    } catch (UnsupportedEncodingException e) {
      //      logger.error("Unable to convert into byte -> Message: %d", e);
    }

    return false;
  }
}
