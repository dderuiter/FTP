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
 * Class for starting the application's execution and JavaFX root GUI.
 *
 * AUTHOR:
 * Davis DeRuiter
 *
 * DATE CREATED:
 * 03/11/2016
 *
 **********************************************************************************************************************/

package com.deruiter.ftp.main;

import com.deruiter.ftp.controller.RootController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application
{
    /**
     * Starts the JavaFX GUI.
     *
     * @param primaryStage
     *          the primary GUI stage.
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        final String iconSmall = "icon_32x32.png";
        final String iconMedium = "icon_64x64.png";
        final String iconLarge = "icon_128x128.png";

        primaryStage.setTitle("Freakin Terrific Program");
        primaryStage.getIcons().addAll(new Image(iconSmall), new Image(iconMedium), new Image(iconLarge));

        RootController rootController = new RootController(primaryStage);
        rootController.showClient();
    }

    /**
     * Starts the application's execution.
     *
     * @param args
     * 			the parameters to include for the program run.
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}