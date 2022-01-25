/*
 * Copyright (c) 2008, 2017, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.dlsc.showcase.demo;

import com.dlsc.showcase.CssShowcaseView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

public class CssShowcaseViewTestApp extends Application {

    private CssShowcaseView view;

    @Override
    public void start(Stage stage) {
        view = new CssShowcaseView();

        MenuItem loadFile = new MenuItem("Load CSS Stylesheets ...");
        loadFile.setOnAction(evt -> loadFile());
        loadFile.setAccelerator(KeyCombination.keyCombination("shortcut+o"));

        MenuItem clearStylesheets = new MenuItem("Clear");
        clearStylesheets.setOnAction(evt -> view.getConfigurations().clear());
        clearStylesheets.setAccelerator(KeyCombination.keyCombination("shortcut+e"));

        MenuItem exit = new MenuItem("Quit");
        exit.setOnAction(evt -> Platform.exit());
        exit.setAccelerator(KeyCombination.keyCombination("shortcut+q"));

        Menu menu = new Menu("File");
        menu.getItems().addAll(loadFile, clearStylesheets, exit);

        MenuBar menuBar = new MenuBar(menu);
        menuBar.setUseSystemMenuBar(false);

        BorderPane borderPane = new BorderPane(view);
        borderPane.setTop(menuBar);

        Scene scene = new Scene(borderPane, 1024, 768);

        stage.setScene(scene);
        stage.setTitle("ShowcaseFX");
        stage.show();
    }

    private FileChooser fileChooser;

    private void loadFile() {
        if (fileChooser == null) {
            fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new ExtensionFilter("CSS Stylesheets", "*.css"));
            fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
        }

        List<File> files = fileChooser.showOpenMultipleDialog(view.getScene().getWindow());
        if (files != null) {
            String[] urls = files.stream().map(file -> {
                try {
                    return file.toURI().toURL().toExternalForm();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return "";
            }).toArray(String[]::new);

            CssShowcaseView.CssConfiguration configuration = new CssShowcaseView.CssConfiguration( files.stream().map(file -> file.getName()).collect(Collectors.joining(", ")), urls);
            view.getConfigurations().add(configuration);
            view.setSelectedConfiguration(configuration);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
