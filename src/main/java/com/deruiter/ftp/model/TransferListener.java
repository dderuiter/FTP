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
 * Model class for listening for data transfers.
 *
 * AUTHOR:
 * Davis DeRuiter
 *
 * DATE CREATED:
 * 03/11/2016
 *
 **********************************************************************************************************************/

package com.deruiter.ftp.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

public class TransferListener implements CopyStreamListener
{
    // Instance variable
    private LongProperty totalBytesTransferredProperty;

    /**
     * Constructor for a transfer listener.
     */
    public TransferListener()
    {
        this.totalBytesTransferredProperty = new SimpleLongProperty();
    }

    /**
     * Triggers when any bytes are transferred.
     *
     * @param event
     *          the event triggered when bytes are transferred.
     */
    // @Override
    public void bytesTransferred(CopyStreamEvent event)
    {
        // Had to override method to implement interface (otherwise not used)
    }

    /**
     * Updates the total number of bytes transferred for the current upload/download.
     *
     * @param totalBytesTransferred
     *          the updated total number of bytes transferred for the current upload/download.
     * @param bytesTransferred
     *          the current number of bytes transferred.
     * @param streamSize
     *          the stream size.
     */
    // @Override
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize)
    {
        this.totalBytesTransferredProperty.setValue(totalBytesTransferred);
    }

    /**
     * Gets the number of bytes transferred for the current upload/download.
     *
     * @return the number of bytes transferred for the current upload/download.
     */
    public LongProperty getBytesTransferredProperty()
    {
        return totalBytesTransferredProperty;
    }

    /**
     * Gets the number of MegaBytes transferred for the current upload/download.
     *
     * @return the number of MegaBytes transferred for the current upload/download.
     */
    public long getMegaBytesTransferred()
    {
        return totalBytesTransferredProperty.getValue() / 1000000;
    }
}
