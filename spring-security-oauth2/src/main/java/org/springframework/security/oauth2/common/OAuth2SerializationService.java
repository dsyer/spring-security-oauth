package org.springframework.security.oauth2.common;

import java.util.Map;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * Interface for OAuth 2 (de)serialization services.
 *
 * @author Ryan Heaton
 */
public interface OAuth2SerializationService {

  /**
   * Deserialize an access token.
   *
   * @param tokenParams The parsed token parameters.
   * @return The access token.
   */
  OAuth2AccessToken deserializeAccessToken(Map<String, String> tokenParams);

  /**
   * Deserialize an oauth error.
   *
   * @param errorParams The error parameters.
   * @return The exception.
   */
  OAuth2Exception deserializeError(Map<String, String> errorParams);

}
