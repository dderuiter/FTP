/**********************************************************************************************************************
 *
 * WARNING:
 * Copyright Ⓒ 2016 by D.DeRuiter
 * Do not use, modify, or distribute in any way without express written consent.
 *
 * PROJECT:
 * FTP (Freakin Terrific Program)
 *
 * DESCRIPTION:
 * Controller class for updating the FTP client view.
 *
 * AUTHOR:
 * Davis DeRuiter
 *
 * DATE CREATED:
 * 03/12/2016
 *
 **********************************************************************************************************************/

package com.deruiter.ftp.controller;

import com.deruiter.ftp.model.Server;
import com.deruiter.ftp.model.TransferListener;
import com.deruiter.ftp.model.TreeItemEnhanced;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.ArrayList;

public class ClientController
{
    // Text Field instance variables
    @FXML
    private TextField host;
    @FXML
    private TextField username;
    @FXML
    private TextField port;

    // Text Area instance variable
    @FXML
    private TextArea connectionDetails;

    // Password Field instance variable
    @FXML
    private PasswordField password;

    // Tree View instance variables
    @FXML
    private TreeView<String> clientTree;
    @FXML
    private TreeView<String> serverTree;

    // Progress Bar instance variable
    @FXML
    private ProgressBar progressBar;

    // Check Box instance variable
    @FXML
    private CheckBox showHiddenFiles;

    // Label instance variable
    @FXML
    private Label statusInfo;

    // Circle instance variable
    @FXML
    private Circle statusIndicator;

    // Instance variables
    private Server server;
    private boolean transferInProgress = false;
    private BooleanProperty connectionLiveProp = new SimpleBooleanProperty();
    private final String EMPTY_FOLDER_ICON_NAME = "empty_folder_simple.png";
    private final String OPEN_FOLDER_ICON_NAME = "open_folder_simple.png";
    private final String FILE_ICON_NAME = "file.png";
    private TreeViewSelected selectedTreeView;
    private enum TreeViewSelected { LOCAL, REMOTE };

    /**
     * Constructor is called before initialize method.
     * Must remain a default constructor (no parameters) for load() call to work properly.
     */
    public ClientController()
    {
    }

    /**
     * Initializes controller class. This method is automatically called after fxml file has been loaded.
     */
    @FXML
    private void initialize()
    {
        updateClientTree();

        // Add listener for connection status
        connectionLiveProp.addListener(observable ->
        {
            // Check if a connection is live
            if(connectionLiveProp.getValue())
            {
                statusIndicator.setFill(Color.valueOf("#32B7B7"));
                statusInfo.setText("Connected");
            }
            else // Connection NOT live
            {
                statusIndicator.setFill(Color.valueOf("#f44362"));
                statusInfo.setText("No connection");
            }
        });
    }

    /**
     * Handles establishing a connection to the server.
     */
    @FXML
    private void handleConnect()
    {
        try
        {
            // Check if a connection is NOT live
            if(!connectionLiveProp.getValue())
            {
                // Constant local variables
                final String SERVER = host.getText();
                final String USER = username.getText();
                final String PASSWORD = password.getText();
                final String PORT = port.getText();

                // Check if any connection specification field is empty
                if(SERVER.isEmpty() || USER.isEmpty() || PASSWORD.isEmpty() || PORT.isEmpty())
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(RootController.getPrimaryStage());
                    alert.setTitle("Connection Info");
                    alert.setHeaderText("Connection Failed");
                    alert.setContentText("All connection detail fields must be filled out.");
                    alert.showAndWait();
                    return;
                }

                // Create a new server
                server = new Server(SERVER, USER, PASSWORD, Integer.parseInt(PORT));

                // Display connection details
                connectionDetails.appendText("\n\nAttempting to connect to " + "(" + SERVER + ").");

                // Perform connection setup
                setupConnection();

                // Update UI
                connectionLiveProp.setValue(server.getFTPClient().isConnected());
                updateServerTree();
            }
            else
            {
                // Display error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(RootController.getPrimaryStage());
                alert.setTitle("Connection Info");
                alert.setHeaderText("Connection Failed");
                alert.setContentText("FTP connection already in progress.");
                alert.showAndWait();
            }
        }
        catch (Exception ex)
        {
            // Log error
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            connectionDetails.appendText("\nFailed to establish connection.");
        }
    }

    /**
     * Handles closing a connection with the server.
     */
    @FXML
    private void handleDisconnect()
    {
        // Check if connection ongoing
        if (!connectionLiveProp.getValue())
        {
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(RootController.getPrimaryStage());
            alert.setTitle("Connection Info");
            alert.setHeaderText("Disconnect Failed");
            alert.setContentText("No FTP connection in progress.");
            alert.showAndWait();
        }
        // Check if transfer in progress because can't disconnect while transfer ongoing
        else if(transferInProgress)
        {
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(RootController.getPrimaryStage());
            alert.setTitle("Connection Info");
            alert.setHeaderText("Disconnect Failed");
            alert.setContentText("File transfer in progress.");
            alert.showAndWait();

            return; // Exit before attempting to disconnect
        }
        else
        {
            disconnect();
        }
    }

    /**
     * Disconnects the FTP client.
     */
    private void disconnect()
    {
        try // Logout and close connection
        {
            // Logout
            connectionDetails.appendText("\nAttempting to disconnect.");
            System.out.println("Attempting to disconnect.");
            server.getFTPClient().logout();
            connectionDetails.appendText("\nLogout successful.");
            System.out.println("Logout successful.");

            // Disconnect
            server.getFTPClient().disconnect();
            connectionDetails.appendText("\nDisconnect successful.");
            System.out.println("Disconnect successful.");

            // Update UI
            connectionLiveProp.setValue(server.getFTPClient().isConnected());
            serverTree.setRoot(null);
        }
        catch (Exception ex) // Failure
        {
            connectionDetails.appendText("\nFailed to disconnect.");
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Handles uploading a file to the server.
     */
    @FXML
    private void handleUpload()
    {
        // Check if a transfer is currently in progress
        if(checkIfTransferInProgress())
        {
            // Exit because simultaneous uploads not allowed
            return;
        }

        // Get path of file to be uploaded
        final String localFilePath = findFilePath(true); // Needed as final in order to pass to task

        // Check if file selected not valid
        if(localFilePath.equals(""))
        {
            return; // Exit before uploading
        }

        // Get path to upload destination directory
        String destinationPath = findDestinationPath(true);

        // Check if file selected not valid
        if(destinationPath.equals(""))
        {
            return; // Exit before uploading
        }

        // Change server directory to destination
        server.setDirectory(destinationPath);

        // Create task to upload file
        Task task = new Task<Void>()
        {
            @Override public Void call()
            {
                FTPClient ftpServer = server.getFTPClient();
                try
                {
                    // Change location to directory where file will be uploaded
                    ftpServer.changeWorkingDirectory(server.getDirectory());
                    System.out.println(ftpServer.getReplyString());

                    // Construct outgoing file and display size in bytes
                    File outgoingFile = new File(localFilePath);
                    long totalSize = outgoingFile.length();
                    System.out.println("Total Size: " + totalSize + " bytes");

                    // Add copy listener
                    TransferListener transferListener = new TransferListener();
                    transferListener.getBytesTransferredProperty().addListener(observable ->
                            updateProgress(transferListener.getBytesTransferredProperty().getValue(), totalSize));
                    ftpServer.setCopyStreamListener(transferListener);
                    System.out.println(ftpServer.getReplyString());

                    // Construct input stream
                    String fileName = localFilePath.substring(localFilePath.lastIndexOf("/") + 1);
                    InputStream inputStream = new FileInputStream(outgoingFile);

                    // Upload file to server
                    transferInProgress = true;
                    System.out.println("Start uploading...");
                    boolean wasSuccessful = ftpServer.storeFile(fileName, inputStream);
                    System.out.println(ftpServer.getReplyString());
                    inputStream.close();

                    // Check if upload was successful
                    if(wasSuccessful)
                    {
                        System.out.println("MB Transferred: " + transferListener.getMegaBytesTransferred());
                    }
                }
                catch (Exception ex)
                {
                    System.out.println("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
                finally
                {
                    transferInProgress = false;
                }
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    /**
     * Handles downloading a file from the server.
     */
    @FXML
    private void handleDownload()
    {
        // Check if a transfer is currently in progress
        if(checkIfTransferInProgress())
        {
            // Exit early because simultaneous transfers not allowed
            return;
        }

        // Get directory path of file to be downloaded
        String tempFilePath = findFilePath(false);
        final String fileName = tempFilePath.substring(tempFilePath.lastIndexOf("/") + 1);
        tempFilePath = tempFilePath.substring(0, tempFilePath.lastIndexOf("/") + 1);
        String remoteDirPath = tempFilePath;

        // Check if file selected not valid
        if(remoteDirPath.equals(""))
        {
            return; // Exit before uploading
        }

        // Change server directory to destination
        server.setDirectory(remoteDirPath);

        // Get path to upload destination directory
        final String destinationDirPath = findDestinationPath(false);

        // Check if file selected not valid
        if(destinationDirPath.equals(""))
        {
            return; // Exit before uploading
        }

        // Create task to upload file
        Task task = new Task<Void>()
        {
            @Override public Void call()
            {
                FTPClient ftpServer = server.getFTPClient();
                try
                {
                    // Change directory on server to download file location
                    ftpServer.changeWorkingDirectory(server.getDirectory());
                    System.out.println(ftpServer.getReplyString());

                    // Construct incoming file and display size in bytes
                    System.out.println("File Name: " + fileName);
                    ftpServer.sendCommand("SIZE", fileName);
                    String reply = ftpServer.getReplyString();
                    int totalSize = Integer.valueOf(reply.substring(reply.lastIndexOf(" ") + 1).trim());
                    System.out.println("Total Size: " + totalSize + " bytes");

                    // Add copy listener
                    TransferListener transferListener = new TransferListener();
                    transferListener.getBytesTransferredProperty().addListener(observable ->
                            updateProgress(transferListener.getBytesTransferredProperty().getValue(), totalSize));
                    ftpServer.setCopyStreamListener(transferListener);
                    System.out.println(ftpServer.getReplyString());

                    // Construct output stream
                    OutputStream outputStream = new FileOutputStream(destinationDirPath + "/" + fileName);

                    // Download file to server
                    transferInProgress = true;
                    System.out.println("Start downloading...");
                    boolean wasSuccessful = ftpServer.retrieveFile(fileName, outputStream);
                    System.out.println(ftpServer.getReplyString());
                    outputStream.close();

                    // Check if download was successful
                    if(wasSuccessful)
                    {
                        System.out.println("MB Transferred: " + transferListener.getMegaBytesTransferred());
                    }
                }
                catch (Exception ex)
                {
                    System.out.println("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
                finally
                {
                    transferInProgress = false;
                }
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    /**
     * Checks if a file transfer is already in progress and cancels the transfer if there is already one.
     */
    private boolean checkIfTransferInProgress()
    {
        // Check if file upload/download in progress
        if(transferInProgress)
        {
            // Already transferring a file so fail
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(RootController.getPrimaryStage());
            alert.setTitle("Transfer Info");
            alert.setHeaderText("Transfer Failed");
            alert.setContentText("File transfer already in progress.");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    /**
     * Handles deleting a file from the client or server.
     */
    @FXML
    private void handleDelete()
    {
        // Local variables
        TreeItem selectedNode;
        String fileName;

        // Check if local (client) TreeView selected last
        if(selectedTreeView == TreeViewSelected.LOCAL)
        {
            selectedNode = clientTree.getSelectionModel().getSelectedItem();
            String fullPath = findBranchPath(selectedNode);
            String dirPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
            fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);

            // Show delete confirmation alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(RootController.getPrimaryStage());
            alert.setTitle("Delete Operation");
            alert.setHeaderText("Confirmation Required");
            alert.setContentText("Are you sure you want to delete " + fileName + " from...\n(LOCAL): " + dirPath);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    File file = new File(fullPath);
                    boolean wasSuccessful = file.delete();

                    // Check if delete completed successfully
                    if(wasSuccessful)
                    {
                        System.out.println("Deleted " + fileName + " successfully.");
                    }
                    else
                    {
                        System.out.println("Delete failed.");
                    }
                }
            });
        }
        else // Remote (server) TreeView selected last
        {
            selectedNode = serverTree.getSelectionModel().getSelectedItem();
            FTPClient ftp = server.getFTPClient();
            String tmpPath = findBranchPath(selectedNode);
            String dirPath = tmpPath.substring(0, tmpPath.lastIndexOf("/"));
            fileName = tmpPath.substring(tmpPath.lastIndexOf("/") + 1);

            try
            {
                ftp.changeWorkingDirectory(dirPath);
                System.out.println(ftp.getReplyString());
                System.out.println("Working Directory: " + ftp.printWorkingDirectory());

                // Show delete confirmation alert
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initOwner(RootController.getPrimaryStage());
                alert.setTitle("Delete Operation");
                alert.setHeaderText("Confirmation Required");
                alert.setContentText("Are you sure you want to delete " + fileName + " from...\n(REMOTE): " + dirPath);

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try
                        {
                            boolean wasSuccessful = ftp.deleteFile(fileName);

                            // Check if delete completed successfully
                            if(wasSuccessful)
                            {
                                System.out.println("Deleted " + fileName + " successfully.");
                            }
                            else
                            {
                                System.out.println("Delete failed.");
                            }
                        }
                        catch (IOException ex)
                        {
                            System.out.println("Error: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                });
            }
            catch (IOException ex)
            {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Sets up a new connection to the FTP server.
     *
     * @throws IOException
     */
    private void setupConnection() throws IOException
    {
        FTPClient ftpClient = server.getFTPClient();

        // Set the connection timeout (in milliseconds)
        ftpClient.setConnectTimeout(1500); // 1.5 sec

        // Set the keep alive timeout (in milliseconds)
        ftpClient.setControlKeepAliveTimeout(5000); // 5 sec

        // Switch mode to avoid firewall blocking
        ftpClient.enterLocalPassiveMode();

        // Connect and login to FTP server
        ftpClient.connect(server.getAddress(), server.getPort());
        System.out.println(ftpClient.getReplyString());
        connectionDetails.appendText("\nConnection successful.");

        ftpClient.login(server.getUser(), server.getPassword());
        connectionDetails.appendText("\nLogin as " + server.getUser() + " successful.");

        // Set file type to be transferred to binary
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        // Change working directory to root
        ftpClient.changeWorkingDirectory("/");
        System.out.println("Remote system is " + ftpClient.getSystemType());
        System.out.println("Current directory is " + ftpClient.printWorkingDirectory());

        System.out.println(ftpClient.getStatus());

        System.out.println("Keep Alive Timeout: " + ftpClient.getControlKeepAliveTimeout());
        System.out.println("Keep Alive Reply Timeout: " + ftpClient.getControlKeepAliveReplyTimeout());

        // Clean-up on exit of window
        RootController.getPrimaryStage().setOnHidden(e ->
        {
            // Check if connection ongoing
            if (connectionLiveProp.getValue())
            {
                disconnect();
            }
        });
    }

    /**
     * Shows a no selection made alert.
     */
    private void showNoSelectionAlert(String alertType)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(RootController.getPrimaryStage());
        alert.setTitle(alertType + " Info");
        alert.setHeaderText(alertType + " Not Started");
        alert.setContentText("No file selected to " + alertType.toLowerCase() + ".");
        alert.showAndWait();
    }

    /**
     * Finds the path to the file selected from the TreeView.
     *
     * @param isUpload
     *          whether the transfer is an upload (alternative is the transfer is a download).
     * @return the path to the file selected from the TreeView.
     */
    private String findFilePath(boolean isUpload)
    {
        // Local variables
        String alertType = "";
        TreeItem selectedNode;
        String filePath = "";

        // Check if file upload in process
        if(isUpload)
        {
            alertType = "Upload";
            selectedNode = clientTree.getSelectionModel().getSelectedItem();
        }
        else // File download in process
        {
            alertType = "Download";
            selectedNode = serverTree.getSelectionModel().getSelectedItem();
        }

        // Check if file selected
        if(selectedNode != null)
        {
            // Check if directory selected
            if(((TreeItemEnhanced)selectedNode).isDirectory())
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(RootController.getPrimaryStage());
                alert.setTitle(alertType + " Info");
                alert.setHeaderText(alertType + " Failed");
                alert.setContentText("Cannot " + alertType.toLowerCase() + " a directory.");
                alert.showAndWait();

                return "";  // Exit method before attempting to upload
            }

            filePath = findBranchPath(selectedNode);
            System.out.println("Selected Path: " + filePath);
        }
        else // No file selected
        {
            showNoSelectionAlert(alertType);

            return ""; // Exit method before attempting to upload
        }

        return filePath;
    }

    /**
     * Finds the path to the destination directory selected from TreeView.
     *
     * @param isUpload
     *          whether the transfer is an upload (alternative is the transfer is a download).
     * @return the path to the destination directory selected from TreeView.
     */
    private String findDestinationPath(boolean isUpload)
    {
        // Local variables
        String alertType = "";
        TreeItem selectedNode;
        String destinationPath = "";

        // Check if file upload in process
        if(isUpload)
        {
            alertType = "Upload";
            selectedNode = serverTree.getSelectionModel().getSelectedItem();
        }
        else // File download in process
        {
            alertType = "Download";
            selectedNode = clientTree.getSelectionModel().getSelectedItem();
        }

        // Get path to destination directory
        if(selectedNode != null)
        {
            // Check if non-directory selected
            if(!((TreeItemEnhanced)selectedNode).isDirectory())
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(RootController.getPrimaryStage());
                alert.setTitle(alertType + " Info");
                alert.setHeaderText(alertType + " Failed");
                alert.setContentText(alertType + " destination must be a directory.");
                alert.showAndWait();

                return "";  // Exit method before attempting to upload
            }

            destinationPath = findBranchPath(selectedNode);
            System.out.println("Selected Destination: " + destinationPath);
        }
        else // No file selected
        {
            showNoSelectionAlert(alertType);

            return ""; // Exit method before attempting to upload
        }

        return destinationPath;
    }

    /**
     * Finds path to selected directory.
     *
     * @param branchNode
     *          the node for which to find the file path.
     * @return the file path to the selected item.
     */
    private String findBranchPath(TreeItem branchNode)
    {
        // Local variables
        String path = "";
        path = branchNode.getValue().toString();
        TreeItem tempNode = branchNode;

        // Keep going up until root node reached
        while((tempNode = tempNode.getParent()) != null)
        {
            path = tempNode.getValue() + "/" + path;
        }

        // Check if path begins with backward slash (used for OSX and Linux)
        if(path.charAt(0) == '/')
        {
            path = path.substring(1); // Remove root slash "/"
        }

        // Check if node is root
        if(path.equals(""))
        {
            path = "/";
        }

        return path;
    }

    /**
     * Populates the TreeView branch with the corresponding files found inside the client directory.
     *
     * @param dir
     *          the directory for which to update the Tree View.
     * @param branchNode
     *          the Tree View node which corresponds to the selected directory.
     */
    private void populateClientDir(File dir, TreeItem branchNode)
    {
        // Local variables
        File[] files = dir.listFiles();
        ArrayList<TreeItem> nodes = new ArrayList<>();

        // Iterate through every file in the directory
        for(int i = 0 ; i < files.length; i++)
        {
            File file = files[i];
            TreeItem newNode = new TreeItemEnhanced(file.getName());

            // Check if current file is hidden on client
            if(!showHiddenFiles.isSelected() && file.isHidden())
            {
                // Skip hidden file
                continue;
            }

            // Check if directory
            if (file.isDirectory())
            {
                // Check if empty directory
                if(file.listFiles().length != 0)
                {
                    // Add empty TreeItem so this node is branch node
                    newNode.getChildren().add(new TreeItem(""));

                    // Set graphic to folder with contents
                    newNode.setGraphic(new ImageView(
                            new Image(getClass().getClassLoader().getResourceAsStream(OPEN_FOLDER_ICON_NAME))));
                }
                else // Non-empty directory
                {
                    // Set graphic to empty folder
                    newNode.setGraphic(new ImageView(
                            new Image(getClass().getClassLoader().getResourceAsStream(EMPTY_FOLDER_ICON_NAME))));
                }
                ((TreeItemEnhanced)newNode).markAsDirectory();
            }
            else // File
            {
                // Set graphic to file
                newNode.setGraphic(new ImageView(
                        new Image(getClass().getClassLoader().getResourceAsStream(FILE_ICON_NAME))));
            }
            nodes.add(newNode);
        }

        branchNode.getChildren().addAll(nodes);
    }

    /**
     * Populates the TreeView branch with the corresponding files found inside the server directory.
     *
     * @param path
     *          the path to the current directory.
     * @param branchNode
     *          the selected node in the Tree View.
     * @throws IOException
     */
    private void populateServerDir(String path, TreeItem branchNode) throws IOException
    {
        // Local variables
        FTPFile[] files = server.getFTPClient().listFiles(path);
        ArrayList<TreeItem> nodes = new ArrayList<>();

        // Iterate through all files in directory
        for(int i = 0; i < files.length; i++)
        {
            FTPFile file = files[i];
            TreeItem newNode = new TreeItemEnhanced(file.getName());

            // Check if directory
            if (file.isDirectory())
            {
                // Check if empty directory
                if(server.getFTPClient().listFiles(path + "/" + file.getName()).length != 0)
                {
                    // Add empty TreeItem so this node is branch node
                    newNode.getChildren().add(new TreeItem(""));

                    // Set graphic to folder with contents
                    newNode.setGraphic(new ImageView(
                            new Image(getClass().getClassLoader().getResourceAsStream(OPEN_FOLDER_ICON_NAME))));
                }
                else // Non-empty directory
                {
                    // Set graphic to empty folder
                    newNode.setGraphic(new ImageView(
                            new Image(getClass().getClassLoader().getResourceAsStream(EMPTY_FOLDER_ICON_NAME))));
                }
                ((TreeItemEnhanced)newNode).markAsDirectory();
            }
            else // File
            {
                // Set graphic to file
                newNode.setGraphic(new ImageView(
                        new Image(getClass().getClassLoader().getResourceAsStream(FILE_ICON_NAME))));
            }

            nodes.add(newNode);
        }

        branchNode.getChildren().addAll(nodes);
    }

    /**
     * Updates the client TreeView by populating it initially with files in user's home directory and adds event handler
     * to populate selected TreeView branch with all sub files found inside corresponding server directory.
     * (Refresh occurs whenever branch expanded.)
     */
    private void updateClientTree()
    {
        // Local variables
        File rootDir = File.listRoots()[0];
        TreeItem rootNode = new TreeItemEnhanced(rootDir.toString().replace("\\", "/"));
        ((TreeItemEnhanced)rootNode).markAsDirectory();
        rootNode.setExpanded(true);
        populateClientDir(rootDir, rootNode);
        clientTree.setRoot(rootNode);

        // Track which TreeView was selected last for use with delete button
        clientTree.getSelectionModel().selectedItemProperty().addListener( observable ->
                selectedTreeView = TreeViewSelected.LOCAL);

        rootNode.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeItem.TreeModificationEvent>()
        {
            @Override
            public void handle(TreeItem.TreeModificationEvent event)
            {
                TreeItem curNode = event.getTreeItem();
                System.out.println("Tree Item: " + curNode);

                // Check if branch has never been expanded before (by looking for blank TreeItem)
                if(((TreeItem)curNode.getChildren().get(0)).getValue().equals(""))
                {
                    // Remove place holder node from TreeView
                    curNode.getChildren().remove(0);
                }
                else
                {
                    // Remove all nodes from current branch (in order to refresh and not have duplicates)
                    curNode.getChildren().clear();
                }

                String path = findBranchPath(curNode);
                System.out.println("Selected Path: " + path);

                // Create directory using specified path
                File expandedDir = new File(path);

                // List files inside expanded directory on TreeView
                populateClientDir(expandedDir, curNode);
            }
        });
    }

    /**
     * Updates the server TreeView by populating it initially with files in user's home directory and adds event handler
     * to populate selected TreeView branch with all sub files found inside corresponding server directory.
     * (Refresh occurs whenever branch expanded.)
     */
    private void updateServerTree()
    {
        try
        {
            // Local variables
            TreeItem rootNode = new TreeItemEnhanced("/");
            ((TreeItemEnhanced)rootNode).markAsDirectory();
            rootNode.setExpanded(true);
            populateServerDir("/", rootNode);
            serverTree.setRoot(rootNode);

            // Track which TreeView was selected last for use with delete button
            serverTree.getSelectionModel().selectedItemProperty().addListener( observable ->
                    selectedTreeView = TreeViewSelected.REMOTE);

            rootNode.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeItem.TreeModificationEvent>()
            {
                @Override
                public void handle(TreeItem.TreeModificationEvent event)
                {
                    // Check if transfer in progress because can't update branch and transfer file simultaneously
                    if(transferInProgress)
                    {
                        return; // Exit before attempting to update server directory
                    }

                    TreeItem curNode = event.getTreeItem();
                    System.out.println("Tree Item: " + curNode);

                    // Check if branch has never been expanded before (by looking for blank TreeItem)
                    if(((TreeItem)curNode.getChildren().get(0)).getValue().equals(""))
                    {
                        // Remove place holder node from TreeView
                        curNode.getChildren().remove(0);
                    }
                    else
                    {
                        // Remove all nodes from current branch (in order to refresh and not have duplicates)
                        curNode.getChildren().clear();
                    }

                    String path = findBranchPath(curNode);
                    System.out.println("Selected Path: " + path);

                    // List files inside expanded directory on TreeView
                    try
                    {
                        populateServerDir(path, curNode);
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
        }
        catch (Exception ex)
        {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Prints the server directory to the console with the proper level of indentation.
     * (Used for debugging purposes.)
     *
     * @param ftpClient
     *          the client to print server directory information for.
     * @param path
     *          the file path to the directory.
     * @param level
     *          the level of the directory.
     * @throws IOException
     */
    private void printServerDirs(FTPClient ftpClient, String path, int level) throws IOException
    {
        // Local variable
        FTPFile[] fileList = ftpClient.listFiles(path);

        // Iterate through all files in directory
        for(int i = 0; i < fileList.length; i++)
        {
            FTPFile file = fileList[i];
            System.out.println(String.format("%" + level + "s%s", "", file.getName()));

            // Check if current item is a directory
            if(file.isDirectory())
            {
                printServerDirs(ftpClient, path + "/" + file.getName(), (level + 1));
            }
        }
    }

    /**
     * Handles the displaying of Help info when the corresponding button is clicked.
     */
    @FXML
    public void handleHelp()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(RootController.getPrimaryStage());
        alert.setTitle("Help");
        alert.setHeaderText("Application created by Davis DeRuiter");
        String helpDetails = "";
        helpDetails += "Connect: Opens connection to server.\n";
        helpDetails += "Disconnect: Terminates connection to server.\n";
        helpDetails += "Upload: Transfers selected file to server.\n";
        helpDetails += "Download: Transfers selected file from server.\n";
        helpDetails += "Delete: Removes selected file from client or server.\n";
        alert.setContentText(helpDetails);
        alert.showAndWait();
    }
}