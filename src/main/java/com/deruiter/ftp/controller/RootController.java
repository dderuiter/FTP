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
 * Controller class for updating the root view.
 *
 * AUTHOR:
 * Davis DeRuiter
 *
 * DATE CREATED:
 * 03/12/2016
 *
 **********************************************************************************************************************/

package com.deruiter.ftp.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class RootController
{
    private static Stage primaryStage;
    private BorderPane rootLayout;

    /**
     * Constructs a root layout controller.
     */
    public RootController(Stage primaryStage)
    {
        this.primaryStage = primaryStage;

        try
        {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("/com/deruiter/ftp/view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add(getClass().getResource("/DarkTheme.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Shows the client layout inside the root layout.
     */
    public void showClient()
    {
        try
        {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/deruiter/ftp/view/ClientLayout.fxml"));
            AnchorPane clientLayout = (AnchorPane) loader.load();

            // Set client layout into the center of root layout.
            rootLayout.setCenter(clientLayout);
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Get the primary GUI stage.
     * @return
     */
    public static Stage getPrimaryStage()
    {
        return primaryStage;
    }
}
