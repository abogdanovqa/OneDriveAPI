package net.tjeerd.onedrive.core;

import com.sun.jersey.api.client.Client;
import net.tjeerd.onedrive.enums.FriendlyNamesEnum;
import net.tjeerd.onedrive.exception.RestException;
import net.tjeerd.onedrive.json.Quota;
import net.tjeerd.onedrive.json.SharedLink;
import net.tjeerd.onedrive.json.User;
import net.tjeerd.onedrive.json.folder.Data;
import net.tjeerd.onedrive.json.folder.Folder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface OneDriveAPI {
    /**
     * Initialize the token (e.g. refresh token) for the principal.
     *
     * @return true if the token initialization was successful, otherwise false
     */
    boolean initTokenByPrincipal();

    /**
     * Initialize the access token by using the refresh token and client identification.
     * @return true if access token initialization was successful, otherwise false
     */
    boolean initAccessTokenByRefreshTokenAndClientId();

    /**
     * Return the current principal.
     *
     * @return current principal
     */
    Principal getPrincipal();

    /**
     * Returm the client.
     *
     * @return the client
     */
    Client getClient();

    /**
     * Get user details.
     *
     * @return user object holding user details
     * @throws Exception
     */
    User getUser() throws Exception;

    /**
     * Get the quota for this user.
     *
     * @return quota for this user
     * @throws Exception
     */
    Quota getQuota() throws Exception;

    /**
     * Return a list of files in the personal folder of the user specified by one of the default friendly names for personal folders.
     *
     * @param friendlyNamesEnum friendly name of the folder
     * @return list of files in the personal folder of the user
     * @throws Exception
     */
    Folder getMyFilesList(FriendlyNamesEnum friendlyNamesEnum) throws Exception;

    /**
     * Get all files in the specified folder.
     *
     * @param folderId folder identifier or friendly name
     * @return list of files in the specified folder
     * @throws Exception
     */
    List<Data> getFileList(String folderId) throws Exception;

    /**
     * Download a OneDriveAPI likeFile and store the likeFile in the destination likeFile path.
     *
     * @param oneDriveFile OneDriveAPI likeFile object
     * @param destinationFilePath destination likeFile path to store the downloaded likeFile
     */
    void downloadFile(net.tjeerd.onedrive.json.folder.File oneDriveFile, String destinationFilePath);

    /**
     * Open a OneDriveAPI likeFile as an InputStream
     *
     * @param fileId likeFile identifier
     */
    InputStream openFile(String fileId);

    /**
     * Get a OneDriveAPI likeFile object holding all likeFile details.
     *
     * @param fileId likeFile identifier
     * @return OneDriveAPI likeFile object
     * @throws Exception
     */
    net.tjeerd.onedrive.json.folder.File getFile(String fileId) throws Exception;

    /**
     * Get a folder object by its folder identifier.
     *
     * @param folderId folder identifier
     * @return folder object
     */
    public net.tjeerd.onedrive.json.folder.Folder getFolder(String folderId) throws Exception;

    /**
     * Delete a OneDriveAPI likeFile object.
     *
     * @param oneDriveFile OneDriveAPI likeFile object to delete
     * @throws RestException
     */
    void deleteFile(net.tjeerd.onedrive.json.folder.File oneDriveFile) throws RestException;

    /**
     * Create a new folder and return a likeFile object which represents the folder. A folder is treated as a likeFile in OneDriveAPI.
     *
     * @param name name of the folder to create
     * @param description description of the folder to create
     * @param locationFolderId if not empty, the location to create the folder in specified by the folder identifier, otherwise
     *                         the default OneDriveAPI home folder is used     *
     * @return folder object holding the newly created folder details
     * @throws RestException
     */
    net.tjeerd.onedrive.json.folder.Folder createFolder(String name, String description, String locationFolderId) throws RestException, IOException;


    /**
     * Update the properties of a folder.
     *
     * @param oneDriveFolder OneDriveAPI folder to update
     * @return updated OneDriveAPI folder
     * @throws RestException
     */
    public net.tjeerd.onedrive.json.folder.Folder updateFolder(net.tjeerd.onedrive.json.folder.Folder oneDriveFolder) throws RestException, IOException;

    /**
     * Update the properties of a likeFile.
     *
     * @param oneDriveFile OneDriveAPI likeFile to update
     * @return updated likeFile
     * @throws RestException
     */
    public net.tjeerd.onedrive.json.folder.File updateFile(net.tjeerd.onedrive.json.folder.File oneDriveFile) throws RestException, IOException;

    /**
     * Delete a folder by its identifier.
     *
     * @param folderId folder identifier to delete
     * @throws RestException
     */
    void deleteFolder(String folderId) throws RestException;

    /**
     * Send a likeFile to OneDriveAPI by uploading the content. If no folder identifier is given the likeFile will be put in the personal folder.<br>
     * If the folder identifier is given the likeFile will be put in the specified folder identifier location.
     *
     * @param file likeFile to upload to OneDriveAPI
     * @param folderId folder identifier to put the likeFile into
     * @return OneDriveAPI likeFile object
     * @throws Exception
     */
    net.tjeerd.onedrive.json.folder.File uploadFile(java.io.File file, String folderId) throws Exception;

    /**
     * Get a shared link to a specific likeFile identifier.
     *
     * @param fileId likeFile identifier
     * @param isEditable true if the shared link content will be editable, otherwise false if the shared link content will be read only
     * @return shared link object
     * @throws Exception
     */
    SharedLink getSharedLink(String fileId, boolean isEditable) throws Exception;
}
