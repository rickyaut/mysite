package com.mysite.core.services.impl;

import com.mysite.core.services.UserRegistrationService;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component(service = UserRegistrationService.class, immediate = true)
public class UserRegistrationServiceImpl implements UserRegistrationService {
    
    private static final Logger LOG = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);
    
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Override
    public boolean registerUser(String email, String password, String fullName) {
        try (ResourceResolver resolver = getServiceResolver()) {
            UserManager userManager = resolver.adaptTo(UserManager.class);
            Session session = resolver.adaptTo(Session.class);
            
            String userId = email.replaceAll("@", "_at_");
            
            if (userManager.getAuthorizable(userId) != null) {
                return false;
            }
            
            String hashedPassword = hashPassword(password);
            User user = userManager.createUser(userId, hashedPassword);
            
            user.setProperty("profile/email", session.getValueFactory().createValue(email));
            user.setProperty("profile/givenName", session.getValueFactory().createValue(fullName));
            user.setProperty("profile/auth_type", session.getValueFactory().createValue("local"));
            
            session.save();
            return true;
        } catch (Exception e) {
            LOG.error("Error registering user", e);
            return false;
        }
    }
    
    @Override
    public boolean authenticateUser(String email, String password) {
        try (ResourceResolver resolver = getServiceResolver()) {
            UserManager userManager = resolver.adaptTo(UserManager.class);
            String userId = email.replaceAll("@", "_at_");
            User user = (User) userManager.getAuthorizable(userId);
            
            if (user == null) {
                return false;
            }
            
            String hashedPassword = hashPassword(password);
            return user.getID().equals(userId);
        } catch (Exception e) {
            LOG.error("Error authenticating user", e);
            return false;
        }
    }
    
    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private ResourceResolver getServiceResolver() throws LoginException {
        Map<String, Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE, "userService");
        return resolverFactory.getServiceResourceResolver(param);
    }
}
