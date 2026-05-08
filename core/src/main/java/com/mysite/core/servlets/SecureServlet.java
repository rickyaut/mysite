package com.mysite.core.servlets;

import com.mysite.core.annotations.RequireAuth;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import java.io.IOException;
import java.util.Arrays;

public abstract class SecureServlet extends SlingAllMethodsServlet {
    
    protected boolean checkPermission(SlingHttpServletRequest request, SlingHttpServletResponse response) 
            throws IOException {
        
        RequireAuth auth = this.getClass().getAnnotation(RequireAuth.class);
        if (auth == null) {
            return true;
        }
        
        Boolean authenticated = (Boolean) request.getAttribute("authenticated");
        if (authenticated == null || !authenticated) {
            response.sendError(401, "Unauthorized");
            return false;
        }
        
        String provider = (String) request.getAttribute("oauth_provider");
        if (!Arrays.asList(auth.providers()).contains(provider)) {
            response.sendError(403, "Provider not allowed");
            return false;
        }
        
        if (auth.roles().length > 0) {
            String userEmail = (String) request.getAttribute("user_email");
            if (!hasRole(userEmail, auth.roles())) {
                response.sendError(403, "Insufficient permissions");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean hasRole(String userEmail, String[] roles) {
        return true;
    }
}
