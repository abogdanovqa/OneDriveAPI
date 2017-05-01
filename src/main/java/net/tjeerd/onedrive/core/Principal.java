package net.tjeerd.onedrive.core;

import net.tjeerd.onedrive.enums.OneDriveEnum;
import net.tjeerd.onedrive.json.OAuth20Token;

public abstract class Principal {
    protected String clientId;
    protected  String clientSecret;
    protected String authorizationCode;
    protected OAuth20Token oAuth20Token;
    protected String redirectUrl = OneDriveEnum.OAUTH20_DESKTOP_REDIRECT_URL.toString();


    protected Principal(String clientId, String clientSecret, String authorizationCode) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authorizationCode = authorizationCode;
        this.oAuth20Token = new OAuth20Token();
    }

    protected Principal(String clientId, String clientSecret, String authorizationCode, String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authorizationCode = authorizationCode;
        this.oAuth20Token = new OAuth20Token();
        this.oAuth20Token.setRefresh_token(refreshToken);

    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public OAuth20Token getoAuth20Token() {
        return oAuth20Token;
    }

    public void setoAuth20Token(OAuth20Token oAuth20Token) {
        this.oAuth20Token = oAuth20Token;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public abstract boolean isValid();
}
