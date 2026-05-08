package com.mysite.core.services;

import java.util.Map;

public interface OAuthService {
    String getAuthorizationUrl(String provider, String state);
    Map<String, String> getUserInfo(String provider, String code);
    boolean verifyToken(String provider, String token);
}
