package com.mysite.core.servlets;

import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
    service = Servlet.class,
    property = {
        "felix.webconsole.title=Pet",
        "felix.webconsole.label=pet",
        "felix.webconsole.category=Sling"
    }
)
public class PetServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=utf-8");
        response.getWriter().println("Hello from PetServlet");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
