package com.mysite.core.filters;

import com.mysite.core.services.OAuthService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.*;
import java.io.IOException;

@Component(service = Filter.class, property = {
    "sling.filter.scope=REQUEST",
    "service.ranking:Integer=1000"
})
public class OAuthTokenFilter implements Filter {
    
    @Reference
    private OAuthService oauthService;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
        
        String token = getTokenFromCookie(slingRequest);
        String provider = getProviderFromCookie(slingRequest);
        
        if (token != null && provider != null) {
            if (oauthService.verifyToken(provider, token)) {
                String userEmail = getUserEmailFromToken(provider, token);
                slingRequest.setAttribute("authenticated", true);
                slingRequest.setAttribute("user_email", userEmail);
                slingRequest.setAttribute("oauth_provider", provider);
            } else {
                slingResponse.setStatus(401);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private String getTokenFromCookie(SlingHttpServletRequest request) {
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if ("oauth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    private String getProviderFromCookie(SlingHttpServletRequest request) {
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if ("oauth_provider".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    private String getUserEmailFromToken(String provider, String token) {
        return null;
    }
}
