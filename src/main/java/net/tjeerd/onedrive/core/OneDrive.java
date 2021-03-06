package net.tjeerd.onedrive.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import net.tjeerd.onedrive.enums.FriendlyNamesEnum;
import net.tjeerd.onedrive.enums.OneDriveEnum;
import net.tjeerd.onedrive.exception.RestException;
import net.tjeerd.onedrive.json.Quota;
import net.tjeerd.onedrive.json.SharedLink;
import net.tjeerd.onedrive.json.User;
import net.tjeerd.onedrive.json.folder.Data;
import net.tjeerd.onedrive.json.folder.DataList;
import net.tjeerd.onedrive.json.folder.Folder;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OneDrive implements OneDriveAPI {
    private static final Logger logger = LoggerFactory.getLogger(OneDrive.class);
    private OneDriveCore oneDriveCore;
    private Principal principal;
    private Client client;

    private static final String API_PATH_ME                = "me";
    private static final String API_PATH_ME_SKYDRIVE_QUOTA = "me/skydrive/quota";
    private static final String API_PATH_ME_SKYDRIVE       = "me/skydrive";
    private static final String API_PATH_ME_SKYDRIVE_FILES = "me/skydrive/files";
    private static final String API_PATH_FILES             = "files";
    private static final String API_PATH_CONTENT           = "content?download=true";
    private static final String API_PATH_SHARED_EDIT_LINK  = "shared_edit_link";
    private static final String API_PATH_SHARED_READ_LINK  = "shared_read_link";

    /**
     * Construct the OneDriveAPI API with the principal as argument.
     *
     * @param principal principal containing tokens, client identification, client secret etc.
     */
    public OneDrive(Principal principal) {
        DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
        defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
        defaultClientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = Client.create(defaultClientConfig);
        this.principal = principal;
        oneDriveCore = new OneDriveCore(client, principal);
    }

    /**
     * Construct the OneDriveAPI API with the principal as argument and optionally enable debugging.
     *
     * @param principal principal containing token, client identification, client secret etc.
     * @param debug true to enable debugging, otherwise false
     */
    public OneDrive(Principal principal, boolean debug) {
        DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
        defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
        //defaultClientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = Client.create(defaultClientConfig);

        if (debug) {
            client.addFilter(new LoggingFilter());
        }

        this.principal = principal;
        oneDriveCore = new OneDriveCore(client, principal);
    }


    public boolean initToken() {
        if (principal != null && principal.getoAuth20Token() != null
                && principal.getoAuth20Token().getRefresh_token() != null
                && !principal.getoAuth20Token().getRefresh_token().equals("")) {
            //try to use refresh token

            return initAccessTokenByRefreshTokenAndClientId();
        } else if(principal.isValid()) {
            return initTokenByPrincipal();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean initTokenByPrincipal() {
        if (principal.isValid()) {
            this.principal.setoAuth20Token(oneDriveCore.getTokenByPrincipal());
            return true;
        } else {
            logger.error("Cannot initialize tokens, the principal object is invalid. Some required properties are missed.");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean initAccessTokenByRefreshTokenAndClientId() {
        if (hasPrincipalRefreshTokenAndClientId()) {
            this.principal.getoAuth20Token().setAccess_token(oneDriveCore.getAccessTokenByRefreshTokenAndClientId().getAccess_token());
            return true;
        } else {
            return false;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Principal getPrincipal() {
        return this.principal;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Client getClient() {
        return this.client;
    }

    /**
     * Return whether the principal has a client identification and secret and authorization code set.
     *
     * @return true if principal has a client identification, secret and authorization code set, otherwise false
     */
    private boolean hasPrincipalClientSecretAndClientIdAndAuthorizationCode() {
        return (StringUtils.isNotEmpty(principal.getClientId()) && StringUtils.isNotEmpty(principal.getClientSecret()) &&
                StringUtils.isNotEmpty(principal.getAuthorizationCode()));
    }

    /**
     * Return whether the principal has a refresh token and client identification set.
     *
     * @return true if principal has a refresh token and client identification set, otherwise false
     */
    private boolean hasPrincipalRefreshTokenAndClientId() {
        return (StringUtils.isNotEmpty(principal.getoAuth20Token().getRefresh_token()) && StringUtils.isNotEmpty(principal.getClientId()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser() throws Exception {
        return (User) oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, API_PATH_ME, new User());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Quota getQuota() throws Exception {
        return (Quota) oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, API_PATH_ME_SKYDRIVE_QUOTA, new Quota());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Folder getMyFilesList(FriendlyNamesEnum friendlyNamesEnum) throws Exception {
        String apiPath = API_PATH_ME_SKYDRIVE;

        if (friendlyNamesEnum.equals(FriendlyNamesEnum.ALL)) {
            apiPath += "/" + API_PATH_FILES;
        } else {
            apiPath += "/" + friendlyNamesEnum.toString() + "/" + API_PATH_FILES;
        }

        return (Folder) oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, apiPath, new Folder());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Data> getFileList(String folderId) throws Exception {
        DataList dataList = (DataList) oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, folderId + "/" + API_PATH_FILES, new DataList());
        List<Data> list = new ArrayList<>();
        for (Data d : dataList.getData()) {
            list.add(d.convertToCorrespondingType());
        }
        return list;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void downloadFile(net.tjeerd.onedrive.json.folder.File oneDriveFile, String destinationFilePath) {
        ClientResponse clientResponse = oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_OCTET_STREAM, oneDriveFile.getId() + "/" + API_PATH_CONTENT);
        java.io.File destinationFile = new java.io.File(destinationFilePath);

        if (clientResponse.getStatus() == ClientResponse.Status.FOUND.getStatusCode()) {
            /* The response is a redirect location, so do a new call to get the real download location */
            try {
                clientResponse = oneDriveCore.doGetAPI(clientResponse.getLocation().toString(), MediaType.APPLICATION_OCTET_STREAM);
                java.io.File resultFile = clientResponse.getEntity(java.io.File.class);
                org.apache.commons.io.FileUtils.moveFile(resultFile, destinationFile);
                logger.info("OneDriveAPI likeFile '" + oneDriveFile.getName() + "' saved to '" + destinationFilePath + "' (" + destinationFile.length() + " bytes)");
            } catch (Exception exception) {
                logger.error("Could not download from redirect location " + clientResponse.getLocation());
                exception.printStackTrace();
            }
        } else if (clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            /* The response contains the location, so save the response to a likeFile */
            try {
                java.io.File resultFile = clientResponse.getEntity(java.io.File.class);
                org.apache.commons.io.FileUtils.moveFile(resultFile, destinationFile);
                logger.info("OneDriveAPI likeFile '" + oneDriveFile.getName() + "' saved to '" + destinationFilePath + "' (" + destinationFile.length() + " bytes)");
            } catch (Exception e) {
                logger.error("Cannot download or write likeFile with identifier '" + oneDriveFile.getId() + "' to destination '" + destinationFilePath + "'");
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream openFile(String fileId) {
        ClientResponse clientResponse = oneDriveCore.
                doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_OCTET_STREAM, fileId + "/" + API_PATH_CONTENT);

        if (clientResponse.getStatus() == ClientResponse.Status.FOUND.getStatusCode()) {
            /* The response is a redirect location, so do a new call to get the real download location */
            try {
                clientResponse = oneDriveCore.doGetAPI(
                        clientResponse.getLocation().toString(), MediaType.APPLICATION_OCTET_STREAM
                );
                return clientResponse.getEntity(InputStream.class);
            } catch (Exception e) {
                logger.error("Could not open likeFile from redirect location " + clientResponse.getLocation());
                e.printStackTrace();
            }
        } else if (clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            /* The response contains the location, so save the response to a likeFile */
            try {
                return clientResponse.getEntity(InputStream.class);
            } catch (Exception e) {
                logger.error("Cannot open likeFile with identifier '" + fileId + "'");
                e.printStackTrace();
            }
        } else if (clientResponse.getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()){
            /* Update access token and try open likeFile again */
            initAccessTokenByRefreshTokenAndClientId();
            return openFile(fileId);
        }
        throw new RuntimeException("Cannot download likeFile with identifier '" + fileId + "'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public net.tjeerd.onedrive.json.folder.File getFile(String fileId) throws Exception {
        return (net.tjeerd.onedrive.json.folder.File) oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, fileId, new net.tjeerd.onedrive.json.folder.File());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public net.tjeerd.onedrive.json.folder.Folder getFolder(String folderId) throws Exception {
        return (net.tjeerd.onedrive.json.folder.Folder) oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, folderId, new net.tjeerd.onedrive.json.folder.Folder());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFile(net.tjeerd.onedrive.json.folder.File oneDriveFile) throws RestException {
        oneDriveCore.doDeleteAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, oneDriveFile.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public net.tjeerd.onedrive.json.folder.File updateFile(net.tjeerd.onedrive.json.folder.File oneDriveFile) throws RestException, IOException {
        return (net.tjeerd.onedrive.json.folder.File) oneDriveCore.doPutAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, oneDriveFile.getId(), oneDriveFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public net.tjeerd.onedrive.json.folder.Folder updateFolder(net.tjeerd.onedrive.json.folder.Folder oneDriveFolder) throws RestException, IOException {
        return (Folder) oneDriveCore.doPutAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, oneDriveFolder.getId(), oneDriveFolder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public net.tjeerd.onedrive.json.folder.Folder createFolder(String name, String description, String locationFolderId) throws RestException, IOException {
        String apiPath;
        net.tjeerd.onedrive.json.folder.Folder oneDriveFolder = new Folder();
        oneDriveFolder.setName(name);
        oneDriveFolder.setDescription(description);

        if (StringUtils.isEmpty(locationFolderId)) {
            apiPath = API_PATH_ME_SKYDRIVE;
        } else {
            apiPath = locationFolderId;
        }

        return (Folder) oneDriveCore.doPostAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, apiPath, oneDriveFolder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFolder(String folderId) throws RestException {
        oneDriveCore.doDeleteAPI(new MultivaluedMapImpl(),MediaType.APPLICATION_JSON, folderId);
    }

    /**
     * Send a likeFile to OneDriveAPI by uploading the content. If no folder identifier is given the likeFile will be put in the personal folder.<br>
     * If the folder identifier is given the likeFile will be put in the specified folder identifier location.
     *
     * @param file likeFile to upload to OneDriveAPI
     * @param folderId folder identifier to put the likeFile into
     * @return OneDriveAPI likeFile object
     */
    public net.tjeerd.onedrive.json.folder.File uploadFile(File file, String folderId) {
        net.tjeerd.onedrive.json.folder.File oneDriveFile = new net.tjeerd.onedrive.json.folder.File();
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        ObjectMapper objectMapper = new ObjectMapper();
        String apiPath = "";

        if (folderId.isEmpty()) {
            apiPath = API_PATH_ME_SKYDRIVE_FILES + "/" + file.getName();
        } else {
            apiPath = folderId + "/" + API_PATH_FILES + "/" + file.getName();
        }

        queryParams.add(OneDriveCore.API_PARAM_ACCESS_TOKEN, principal.getoAuth20Token().getAccess_token());
        WebResource webResource = client.resource(OneDriveEnum.API_URL.toString() + apiPath);

        try {
            ClientResponse clientResponse = webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, fileToByteArray(file));
            oneDriveFile = objectMapper.readValue(clientResponse.getEntity(String.class).toString(), net.tjeerd.onedrive.json.folder.File.class);
        } catch (Exception e) {
            logger.error("Cannot upload likeFile '" + file.getAbsolutePath() + "' to OneDriveAPI");
            e.printStackTrace();
        }

        return oneDriveFile;
    }

    /**
     * Convert likeFile content to a byte array.
     *
     * @param file likeFile object to convert to a byte array
     * @return byte array holding the likeFile content
     */
    private byte[] fileToByteArray(java.io.File file) {
        byte[] byteArrayFile = new byte[(int) file.length()];

        try {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteArrayFile);
            fileInputStream.close();

        }catch(Exception e){
            e.printStackTrace();
        }

        return byteArrayFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SharedLink getSharedLink(String fileId, boolean isEditable) throws Exception {
        String apiPath;

        if (isEditable) {
            apiPath = fileId + "/" + API_PATH_SHARED_EDIT_LINK;
        } else {
            apiPath = fileId + "/" + API_PATH_SHARED_READ_LINK;
        }

        return (SharedLink) oneDriveCore.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, apiPath, new SharedLink());
    }

}
