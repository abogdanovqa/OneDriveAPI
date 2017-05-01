package net.tjeerd.onedrive.core;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class MobileAppPrincipal extends Principal {
    public MobileAppPrincipal(String clientId, String clientSecret, String authorizationCode) {
        super(clientId, clientSecret, authorizationCode);
    }

    public MobileAppPrincipal(String clientId, String clientSecret, String authorizationCode, String refreshToken) {
        super(clientId, clientSecret, authorizationCode, refreshToken);
    }

    public boolean isValid() {
        return (StringUtils.isNotEmpty(getClientId()) && StringUtils.isNotEmpty(getClientSecret()) &&
                StringUtils.isNotEmpty(getAuthorizationCode()));
    }
}
