# OAuth Implementation for AEM

This implementation provides OAuth authentication for public pages using Google, Facebook, and Azure AD.

## Setup Instructions

### 1. Configure OAuth Providers

#### Google OAuth
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add authorized redirect URI: `http://localhost:4502/bin/oauth/callback`
6. Copy Client ID and Client Secret

#### Facebook OAuth
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app
3. Add Facebook Login product
4. Configure Valid OAuth Redirect URIs: `http://localhost:4502/bin/oauth/callback`
5. Copy App ID and App Secret

#### Azure AD OAuth
1. Go to [Azure Portal](https://portal.azure.com/)
2. Navigate to Azure Active Directory > App registrations
3. Create new registration
4. Add redirect URI: `http://localhost:4502/bin/oauth/callback`
5. Create client secret under Certificates & secrets
6. Copy Application (client) ID, Directory (tenant) ID, and Client Secret

### 2. Configure AEM

Update the OSGi configuration at:
`/apps/mysite/osgiconfig/config/com.mysite.core.services.OAuthConfig.cfg.json`

Or configure via AEM Web Console:
`http://localhost:4502/system/console/configMgr`

Search for "OAuth Configuration" and enter your credentials.

### 3. Build and Deploy

```bash
mvn clean install -PautoInstallSinglePackage
```

### 4. Add Component to Page

1. Open a page in AEM editor
2. Add "OAuth Login" component from "My Site - Content" group
3. Publish the page

## Usage

Users can click on any of the three login buttons:
- Sign in with Google
- Sign in with Facebook
- Sign in with Microsoft

After successful authentication, user information is stored in the session and a user profile is created in AEM.

## Security Notes

- Always use HTTPS in production
- Store credentials securely (use environment variables or AEM Crypto)
- Update redirect URIs for production domains
- Implement proper session management and CSRF protection
