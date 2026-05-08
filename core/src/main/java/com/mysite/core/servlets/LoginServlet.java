package com.mysite.core.servlets;

import com.mysite.core.services.UserRegistrationService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = Servlet.class, property = {
    "sling.servlet.paths=/bin/auth/login",
    "sling.servlet.methods=POST"
})
public class LoginServlet extends SlingAllMethodsServlet {
    
    @Reference
    private UserRegistrationService registrationService;
    
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        if (email == null || password == null) {
            response.sendError(400, "Missing credentials");
            return;
        }
        
        boolean authenticated = registrationService.authenticateUser(email, password);
        
        if (authenticated) {
            request.getSession().setAttribute("user_email", email);
            request.getSession().setAttribute("authenticated", true);
            request.getSession().setAttribute("auth_type", "local");
            
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true,\"message\":\"Login successful\"}");
        } else {
            response.sendError(401, "Invalid credentials");
        }
    }
}
