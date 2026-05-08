package com.mysite.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "OAuth Configuration", description = "OAuth provider configuration")
public @interface OAuthConfig {
    
    @AttributeDefinition(name = "Google Client ID")
    String google_client_id() default "";
    
    @AttributeDefinition(name = "Google Client Secret")
    String google_client_secret() default "";
    
    @AttributeDefinition(name = "Facebook App ID")
    String facebook_app_id() default "";
    
    @AttributeDefinition(name = "Facebook App Secret")
    String facebook_app_secret() default "";
    
    @AttributeDefinition(name = "Azure AD Client ID")
    String azure_client_id() default "";
    
    @AttributeDefinition(name = "Azure AD Client Secret")
    String azure_client_secret() default "";
    
    @AttributeDefinition(name = "Azure AD Tenant ID")
    String azure_tenant_id() default "";
    
    @AttributeDefinition(name = "Redirect URI")
    String redirect_uri() default "http://localhost:4502/bin/oauth/callback";
}
