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
 * Model class for a tree item that differentiates between files and directories.
 *
 * AUTHOR:
 * Davis DeRuiter
 *
 * DATE CREATED:
 * 03/16/2016
 *
 **********************************************************************************************************************/

package com.deruiter.ftp.model;

import javafx.scene.control.TreeItem;

public class TreeItemEnhanced extends TreeItem
{
    // Instance variable
    private boolean isDirectory = false;

    /**
     * Constructor for an enhanced tree item.
     *
     * @param value
     *          the value for the tree item.
     */
    public TreeItemEnhanced(String value)
    {
        super(value);
    }

    /**
     * Marks the tree item as a directory.
     */
    public void markAsDirectory()
    {
        isDirectory = true;
    }

    /**
     * Gets whether the tree item is a directory.
     *
     * @return whether the tree item is a directory.
     */
    public boolean isDirectory()
    {
        return isDirectory;
    }
}
