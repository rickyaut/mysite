package com.mysite.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.methods=GET",
        "sling.servlet.paths=/api/pet2",
        "sling.servlet.paths=/api/pet3",
        "sling.servlet.selectors=create",
        "sling.servlet.selectors=view",
        "sling.servlet.extensions=json"
    }
)
public class PetServlet2 extends SlingAllMethodsServlet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PetServlet2.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/json; charset=utf-8");
        LOGGER.info("====" + request.getRequestPathInfo().getExtension());
        response.getWriter().println("[1, 2, 3]");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
