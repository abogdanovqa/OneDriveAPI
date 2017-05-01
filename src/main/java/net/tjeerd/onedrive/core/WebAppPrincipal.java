package net.tjeerd.onedrive.core;

import net.tjeerd.onedrive.json.OAuth20Token;
import org.apache.commons.lang3.StringUtils;

/**
 * The class of principal uses to get a token for Web application.
 * If you use a Web application we need a redirectUrl and clientSecret doesn't require.
 *
 * If a principal has a refresh code authorization code doesn't require to get access token if
 * the refresh token is valid.
 */
public class WebAppPrincipal extends Principal {

    public WebAppPrincipal(String clientId, String clientSecret, String authorizationCode, String redirectUrl) {
        super(clientId, clientSecret, authorizationCode);
        this.redirectUrl = redirectUrl;
    }

    public void setRefreshToken(String token) {
        this.oAuth20Token = new OAuth20Token();
        this.oAuth20Token.setRefresh_token(token);
    }


    public boolean isValid() {
        return (StringUtils.isNotEmpty(getClientId())
                && StringUtils.isNotEmpty(getAuthorizationCode())
                && StringUtils.isNotEmpty(getRedirectUrl()));
    }
}
