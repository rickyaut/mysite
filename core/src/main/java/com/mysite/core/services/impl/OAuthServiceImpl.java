package com.mysite.core.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.microsoft.aad.msal4j.*;
import com.mysite.core.services.OAuthConfig;
import com.mysite.core.services.OAuthService;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.types.User;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component(service = OAuthService.class, immediate = true)
@Designate(ocd = OAuthConfig.class)
public class OAuthServiceImpl implements OAuthService {
    
    private static final Logger LOG = LoggerFactory.getLogger(OAuthServiceImpl.class);
    private OAuthConfig config;
    
    @Activate
    protected void activate(OAuthConfig config) {
        this.config = config;
    }
    
    @Override
    public String getAuthorizationUrl(String provider, String state) {
        switch (provider.toLowerCase()) {
            case "google":
                return getGoogleAuthUrl(state);
            case "facebook":
                return getFacebookAuthUrl(state);
            case "azure":
                return getAzureAuthUrl(state);
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }
    
    @Override
    public Map<String, String> getUserInfo(String provider, String code) {
        try {
            switch (provider.toLowerCase()) {
                case "google":
                    return getGoogleUserInfo(code);
                case "facebook":
                    return getFacebookUserInfo(code);
                case "azure":
                    return getAzureUserInfo(code);
                default:
                    throw new IllegalArgumentException("Unknown provider: " + provider);
            }
        } catch (Exception e) {
            LOG.error("Error getting user info", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public boolean verifyToken(String provider, String token) {
        try {
            switch (provider.toLowerCase()) {
                case "google":
                    return verifyGoogleToken(token);
                case "facebook":
                    return verifyFacebookToken(token);
                case "azure":
                    return verifyAzureToken(token);
                default:
                    return false;
            }
        } catch (Exception e) {
            LOG.error("Error verifying token", e);
            return false;
        }
    }
    
    private String getGoogleAuthUrl(String state) {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            new NetHttpTransport(), GsonFactory.getDefaultInstance(),
            config.google_client_id(), config.google_client_secret(),
            Arrays.asList("openid", "email", "profile"))
            .build();
        return flow.newAuthorizationUrl()
            .setRedirectUri(config.redirect_uri())
            .setState(state)
            .build();
    }
    
    private Map<String, String> getGoogleUserInfo(String code) throws Exception {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            new NetHttpTransport(), GsonFactory.getDefaultInstance(),
            config.google_client_id(), config.google_client_secret(),
            Arrays.asList("openid", "email", "profile"))
            .build();
        
        GoogleTokenResponse response = flow.newTokenRequest(code)
            .setRedirectUri(config.redirect_uri())
            .execute();
        
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", response.parseIdToken().getPayload().getEmail());
        userInfo.put("name", (String) response.parseIdToken().getPayload().get("name"));
        userInfo.put("provider", "google");
        return userInfo;
    }
    
    private String getFacebookAuthUrl(String state) {
        return String.format(
            "https://www.facebook.com/v18.0/dialog/oauth?client_id=%s&redirect_uri=%s&state=%s&scope=email,public_profile",
            config.facebook_app_id(), config.redirect_uri(), state);
    }
    
    private Map<String, String> getFacebookUserInfo(String code) throws Exception {
        FacebookClient client = new DefaultFacebookClient(Version.LATEST);
        FacebookClient.AccessToken accessToken = client.obtainUserAccessToken(
            config.facebook_app_id(), config.facebook_app_secret(), config.redirect_uri(), code);
        
        FacebookClient userClient = new DefaultFacebookClient(accessToken.getAccessToken(), Version.LATEST);
        User user = userClient.fetchObject("me", User.class, 
            com.restfb.Parameter.with("fields", "id,name,email"));
        
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("provider", "facebook");
        return userInfo;
    }
    
    private String getAzureAuthUrl(String state) {
        return String.format(
            "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&scope=openid%%20profile%%20email&state=%s",
            config.azure_tenant_id(), config.azure_client_id(), config.redirect_uri(), state);
    }
    
    private Map<String, String> getAzureUserInfo(String code) throws Exception {
        ConfidentialClientApplication app = ConfidentialClientApplication.builder(
            config.azure_client_id(),
            ClientCredentialFactory.createFromSecret(config.azure_client_secret()))
            .authority("https://login.microsoftonline.com/" + config.azure_tenant_id())
            .build();
        
        AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(
            code, new URI(config.redirect_uri()))
            .scopes(Collections.singleton("openid profile email"))
            .build();
        
        CompletableFuture<IAuthenticationResult> future = app.acquireToken(parameters);
        IAuthenticationResult result = future.get();
        
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", result.account().username());
        userInfo.put("name", result.account().username());
        userInfo.put("provider", "azure");
        return userInfo;
    }
    
    private boolean verifyGoogleToken(String token) throws Exception {
        GoogleTokenResponse response = new GoogleTokenResponse();
        response.setAccessToken(token);
        return response.getAccessToken() != null;
    }
    
    private boolean verifyFacebookToken(String token) throws Exception {
        FacebookClient client = new DefaultFacebookClient(token, Version.LATEST);
        client.fetchObject("me", User.class);
        return true;
    }
    
    private boolean verifyAzureToken(String token) throws Exception {
        return token != null && !token.isEmpty();
    }
}
