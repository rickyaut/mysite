package com.mysite.core.servlets;

import com.mysite.core.services.OAuthService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.UUID;

@Component(service = Servlet.class, property = {
    "sling.servlet.paths=/bin/oauth/login",
    "sling.servlet.methods=GET"
})
public class OAuthLoginServlet extends SlingAllMethodsServlet {
    
    @Reference
    private OAuthService oauthService;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String provider = request.getParameter("provider");
        String state = UUID.randomUUID().toString();
        
        request.getSession().setAttribute("oauth_state", state);
        request.getSession().setAttribute("oauth_provider", provider);
        
        String authUrl = oauthService.getAuthorizationUrl(provider, state);
        response.sendRedirect(authUrl);
    }
}
