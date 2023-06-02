package com.shopinzone.config;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

  public static final String UNKNOWN = "unknown";

  @Value("${spring.security.auth-enabled}")
  private boolean isAuthEnabled;

  public Optional<String> getCurrentAuthenticatedUserId() {
    try {
      // context is occasionally null when auth is disabled
      if (SecurityContextHolder.getContext() == null
          || SecurityContextHolder.getContext().getAuthentication() == null) {
        return Optional.empty();
      }
      OAuth2AuthenticatedPrincipal principal =
          (OAuth2AuthenticatedPrincipal)
              SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      return Optional.ofNullable(principal.getAttribute("unique_name"))
          .flatMap(
              it -> {
                String uname = (String) it;
                String[] split = uname.split("@");
                if (split.length > 0) {
                  return Optional.of(split[0]);
                }
                return Optional.empty();
              });
    } catch (ClassCastException ex) {
      log.error("Error finding username from SecurityContext due to exception: {}", ex);
      return Optional.empty();
    }
  }

  public String getUserId() {
    if (isAuthEnabled) {
      return getCurrentAuthenticatedUserId().orElse(UNKNOWN);
    } else {
      return UNKNOWN;
    }
  }
}
