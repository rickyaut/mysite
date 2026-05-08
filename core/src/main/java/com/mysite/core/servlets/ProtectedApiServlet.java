package com.mysite.core.servlets;

import com.mysite.core.annotations.RequireAuth;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = Servlet.class, property = {
    "sling.servlet.paths=/bin/api/protected",
    "sling.servlet.methods=GET"
})
@RequireAuth(providers = {"google", "azure"}, roles = {"admin", "editor"})
public class ProtectedApiServlet extends SecureServlet {
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) 
            throws IOException {
        
        if (!checkPermission(request, response)) {
            return;
        }
        
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Access granted\"}");
    }
}
