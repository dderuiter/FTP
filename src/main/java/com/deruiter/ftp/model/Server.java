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
 * Model class for storing server connection information.
 *
 * AUTHOR:
 * Davis DeRuiter
 *
 * DATE CREATED:
 * 03/11/2016
 *
 **********************************************************************************************************************/

package com.deruiter.ftp.model;

import org.apache.commons.net.ftp.FTPClient;

public class Server
{
    // Instance variables
    private String address;
    private String user;
    private String password;
    private String directory;
    private int port;
    private FTPClient ftpClient;

    /**
     * Constructor for a server.
     *
     * @param address
     *          the IP address for the server.
     * @param user
     *          the user name to for logging in to the server.
     * @param password
     *          the password for logging in to the server.
     * @param port
     *          the port for the server.
     */
    public Server(String address, String user, String password, int port)
    {
        this.address = address;
        this.user = user;
        this.password = password;
        this.port = port;
        this.directory = "";

        ftpClient = new FTPClient();
    }

    /**
     * Sets the server's IP address.
     *
     * @param address
     *          the server's IP address.
     */
    public void setAddress(String address)
    {
        this.address = address;
    }

    /**
     * Gets the server's IP address.
     *
     * @return the server's IP address.
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * Sets the username to use for establishing a connection.
     *
     * @param user
     *          the username to use for establishing a connection.
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * Gets the username to use for establishing a connection.
     *
     * @return the username to use for establishing a connection.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Sets the password for server authentication purposes.
     *
     * @param password
     *          the password for server authentication purposes.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Gets the password for server authentication purposes.
     *
     * @return the password for server authentication purposes.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the port for establishing a connection.
     *
     * @param port
     *          the port for establishing a connection.
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Gets the port for establishing a connection.
     *
     * @return the port for establishing a connection.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the current directory for the server.
     *
     * @param directory
     *          the current directory for the server.
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }

    /**
     * Gets the current directory for the server.
     *
     * @return the current directory for the server.
     */
    public String getDirectory()
    {
        return directory;
    }

    /**
     * Gets the FTP client.
     *
     * @return the FTP client.
     */
    public FTPClient getFTPClient()
    {
        return ftpClient;
    }
}