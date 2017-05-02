package net.tjeerd.onedrive;

import net.tjeerd.onedrive.core.OneDrive;
import net.tjeerd.onedrive.core.WebAppPrincipal;
import net.tjeerd.onedrive.enums.FriendlyNamesEnum;
import net.tjeerd.onedrive.json.folder.Data;
import net.tjeerd.onedrive.json.folder.Folder;

import java.io.File;
import java.util.List;


public class Step1InitToken {
    private static OneDrive oneDriveAPI;
    private static boolean DEBUG = true;
    private static String CLIENT_ID = "cc88468d-b10a-4d85-99d8-a52b3d1cc0fc"; // see OneDrive Development Dashboard
    private static String CLIENT_SECRET = "80sApMtSMrz2VeAYEaGxLct"; // see OneDrive Development Dashboard
    private static String AUTHORIZATION_CODE = "Mcf4ff2a3-397b-ca9d-c43a-8e665d88cfab"; // see description below

    /**
     * This program requires the following values which are defined at the top of the class:
     *
     * CLIENT_ID - unique identification for your client/software/app (see OneDrive Developers Dashboard)
     * CLIENT_SECRET - unique secret for your client, also see the OneDrive Developers Dashboard
     * AUTHORIZATION_CODE - unique authorization code you must generate by calling the following URL which
     * returns an authorization code (which is in the URL):
     *
     * https://login.live.com/oauth20_authorize.srf?
     * client_id=01234567890&
     * scope=wl.signin%20wl.basic%20wl.offline_access%20wl.skydrive_update%20wl.skydrive&
     * response_type=code&
     * redirect_uri=https://login.live.com/oauth20_desktop.srf
     *
     * In theory you should have to run this program only once to get the refresh token. The refresh token
     * can be reused every time to get a new access token . The access token is required to make OneDrive API calls.
     *
     * When done, see the OneDriveTest unit test class to see a few examples of OneDrive API calls. Be sure that
     * all properties (onedrive.properties) are defined in the test resources folder.
     *
     * @param args none used
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        WebAppPrincipal principal = new WebAppPrincipal(CLIENT_ID, CLIENT_SECRET, AUTHORIZATION_CODE, "https://www.gonitro.com/NitroProOAuth2Callback");
        principal.setRefreshToken("MCVOh51VP5wXttJM6D2!0ZWO1c5X4vX5UEhAVG*U!BsQ5QnexfqC3chhQVqeJrWmV!E1a5RYcoVqkZYGAaD6BLRJy5ggG7ylomWQ9z0pWNvB29qD59YuFGQLTxj8PrMfsTSKbmqxqImmNAD*spy5zTMwWtuGzHPWDVtgnXuyBSkANPRFO8gMrDRXTATP0gvmMjm4VPwAeEl7EHN3X7uqiiUWzlFenez8igk4ooi7iPbkl7DPOsD5hISrOwHH94ouUpZf58x2DZfCz3tsvzBORo3xeu4Al!FtkhJfVACrUFY1gtXuDdZvO06RP*t5NqQYGwaDXxnaDlnO2zRz!BTJAVq0gPL50rRzxrtG5qMfkUOh2dSkfOKV6Oq611!0ctUntm7IN8GnyOg5le5niPnleQps$");

        oneDriveAPI = new OneDrive(principal, DEBUG);
        oneDriveAPI.initToken();


        System.out.println("Refresh token: " + principal.getoAuth20Token().getRefresh_token());

        Data d = oneDriveAPI.getMyFilesList(FriendlyNamesEnum.ALL).getData().get(0);
//        boolean b = oneDriveAPI.initAccessTokenByRefreshTokenAndClientId();
//        Folder flds2 = oneDriveAPI.getMyFilesList(FriendlyNamesEnum.ALL);

        Folder f = oneDriveAPI.createFolder("TestA", "", d.getParent_id());
        oneDriveAPI.createFolder("TestABC", "", f.getId());
        oneDriveAPI.uploadFile(new File("./README.md"), f.getId());

        List<Data> list = oneDriveAPI.getFileList(f.getId());

        int iii = 0;
//        flds.
//        oneDriveAPI.getFileList(flds.getId());
    }

    /**
     * Generate a refresh token. You need to get authorization code before
     *
     * To get authorization code
     *
     * 1. Open in browser
     * "https://login.live.com/oauth20_authorize.srf?client_id=" + clientId + "&scope=wl.signin%20wl.basic%20wl.offline_access%20wl.skydrive_update&response_type=code&redirect_uri=" + redirectUrl
     *
     *2. Signin
     *
     *3. When you redirected to your "redirectUrl" copy the code from the address bar
     *
     *
     * @param clientId
     * @param clientSecret
     * @param authorization
     * @param redirectUrl
     * @return
     */
    public static String generateRefreshToken(String clientId, String clientSecret, String authorization, String redirectUrl) {
        WebAppPrincipal principal = new WebAppPrincipal(clientId, clientSecret, authorization, redirectUrl);

        oneDriveAPI = new OneDrive(principal, DEBUG);
        oneDriveAPI.initToken();

        return principal.getoAuth20Token().getRefresh_token();
    }
}
