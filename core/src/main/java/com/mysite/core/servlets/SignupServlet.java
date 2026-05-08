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
    "sling.servlet.paths=/bin/auth/signup",
    "sling.servlet.methods=POST"
})
public class SignupServlet extends SlingAllMethodsServlet {
    
    @Reference
    private UserRegistrationService registrationService;
    
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        
        if (email == null || password == null || fullName == null) {
            response.sendError(400, "Missing required fields");
            return;
        }
        
        boolean success = registrationService.registerUser(email, password, fullName);
        
        if (success) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true,\"message\":\"User registered\"}");
        } else {
            response.sendError(409, "User already exists");
        }
    }
}
