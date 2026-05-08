package com.mysite.core.servlets;

import com.mysite.core.services.OAuthService;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Map;

@Component(service = Servlet.class, property = {
    "sling.servlet.paths=/bin/oauth/callback",
    "sling.servlet.methods=GET"
})
public class OAuthCallbackServlet extends SlingAllMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(OAuthCallbackServlet.class);
    
    @Reference
    private OAuthService oauthService;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String sessionState = (String) request.getSession().getAttribute("oauth_state");
        String provider = (String) request.getSession().getAttribute("oauth_provider");
        
        if (!state.equals(sessionState)) {
            response.sendError(400, "Invalid state");
            return;
        }
        
        Map<String, String> userInfo = oauthService.getUserInfo(provider, code);
        
        if (userInfo.isEmpty()) {
            response.sendError(500, "Failed to get user info");
            return;
        }
        
        try {
            createOrUpdateUser(request.getResourceResolver(), userInfo);
            request.getSession().setAttribute("user_email", userInfo.get("email"));
            request.getSession().setAttribute("user_name", userInfo.get("name"));
            request.getSession().setAttribute("authenticated", true);
            response.sendRedirect("/content/mysite/us/en.html");
        } catch (Exception e) {
            LOG.error("Error creating user", e);
            response.sendError(500, "Authentication failed");
        }
    }
    
    private void createOrUpdateUser(ResourceResolver resolver, Map<String, String> userInfo) throws Exception {
        Session session = resolver.adaptTo(Session.class);
        UserManager userManager = resolver.adaptTo(UserManager.class);
        
        String userId = userInfo.get("email").replaceAll("@", "_at_");
        User user = (User) userManager.getAuthorizable(userId);
        
        if (user == null) {
            user = userManager.createUser(userId, null);
        }
        
        user.setProperty("profile/email", session.getValueFactory().createValue(userInfo.get("email")));
        user.setProperty("profile/givenName", session.getValueFactory().createValue(userInfo.get("name")));
        user.setProperty("profile/oauth_provider", session.getValueFactory().createValue(userInfo.get("provider")));
        
        session.save();
    }
}
