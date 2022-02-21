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
package com.dlsc.showcase.impl;

import com.dlsc.showcase.CssShowcaseView;
import com.dlsc.showcase.CssShowcaseView.CssConfiguration;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.util.StringConverter;
import org.scenicview.ScenicView;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CssShowcaseViewSkin extends SkinBase<CssShowcaseView> {

    static {
        System.getProperties().put("javafx.pseudoClassOverrideEnabled", "true");
    }

    private BorderPane root;
    private SamplePageNavigation samplePageNavigation;
    private SamplePage samplePage;
    private SimpleWindowPage simpleWindows;
    private Node mosaic;
    private Node heightTest;
    private Node combinationsTest;
    private Node customerTest;

    private ComboBox<CssConfiguration> stylesheetsBox;
    private ToggleButton retinaButton, rtlButton;
    private TabPane contentTabs;
    private Pane contentGroup;

    public CssShowcaseViewSkin(CssShowcaseView view) {
        super(view);

        root = new BorderPane();

        Label dropLabel = new Label("Drop one or more CSS files ...");

        StackPane glasspane = new StackPane(dropLabel);
        glasspane.setMouseTransparent(true);
        glasspane.getStyleClass().add("glass-pane");

        StackPane wrapper = new StackPane(root, glasspane);

        getChildren().add(wrapper);

        view.selectedConfigurationProperty().addListener(it -> updateStylesheets());
        view.additionalTabsProperty().addListener((Observable it) -> updateView());
        updateView();
        updateStylesheets();
    }

    private void updateStylesheets() {
        CssShowcaseView view = getSkinnable();

        if (contentGroup == null) {
            return;
        }

        contentGroup.getScene().getStylesheets().clear();

        CssConfiguration config = view.getSelectedConfiguration();
        if (config != null) {
            contentGroup.getScene().getStylesheets().addAll(config.getStylesheetUrls());
        }
    }

    public Map<String, Node> getContent() {
        return samplePage.getContent();
    }

    public void setRetinaMode(boolean retinaMode) {
        if (retinaMode) {
            contentTabs.getTransforms().setAll(new Scale(2, 2));
        } else {
            contentTabs.getTransforms().setAll(new Scale(1, 1));
        }
        contentTabs.requestLayout();
    }

    private void updateView() {
        try {
            // Create sample page and nav
            samplePageNavigation = new SamplePageNavigation();
            samplePage = samplePageNavigation.getSamplePage();
            // Create Content Area
            contentTabs = new TabPane();

            Tab tab1 = new Tab("All Controls");
            tab1.setContent(samplePageNavigation);
            tab1.setClosable(false);

            Tab tab2 = new Tab("Mosaic");
            tab2.setContent(new ScrollPane(mosaic = FXMLLoader.load(CssShowcaseViewSkin.class.getResource("ui-mosaic.fxml"))));
            tab2.setClosable(false);

            Tab tab3 = new Tab("Alignment");
            tab3.setContent(new ScrollPane(heightTest = FXMLLoader.load(CssShowcaseViewSkin.class.getResource("SameHeightTest.fxml"))));
            tab3.setClosable(false);

            Tab tab4 = new Tab("Windows");
            tab4.setContent(new ScrollPane(simpleWindows = new SimpleWindowPage()));
            tab4.setClosable(false);

            Tab tab5 = new Tab("Combinations");
            tab5.setContent(new ScrollPane(combinationsTest = FXMLLoader.load(CssShowcaseViewSkin.class.getResource("CombinationTest.fxml"))));
            tab5.setClosable(false);

            contentTabs.getTabs().addAll(tab1, tab2, tab3, tab4, tab5);
            contentTabs.getTabs().addAll(getSkinnable().getAdditionalTabs());

            // height test set selection for
            Platform.runLater(() -> {
                for (Node n : heightTest.lookupAll(".choice-box")) {
                    ((ChoiceBox) n).getSelectionModel().selectFirst();
                }
                for (Node n : heightTest.lookupAll(".combo-box")) {
                    ((ComboBox) n).getSelectionModel().selectFirst();
                }
            });

            // Create Toolbar
            retinaButton = new ToggleButton("@2x");
            retinaButton.setOnAction(event -> {
                ToggleButton btn = (ToggleButton) event.getSource();
                setRetinaMode(btn.isSelected());
            });

            Button scenicViewButton = new Button("Scenic View");
            scenicViewButton.setOnAction(evt -> ScenicView.show(contentGroup));

            stylesheetsBox = new ComboBox<>();
            stylesheetsBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(CssConfiguration entry) {
                    if (entry != null) {
                        return entry.getName();
                    }

                    return "";
                }

                @Override
                public CssConfiguration fromString(String s) {
                    return null;
                }
            });

            stylesheetsBox.itemsProperty().bind(getSkinnable().configurationsProperty());
            stylesheetsBox.valueProperty().bindBidirectional(getSkinnable().selectedConfigurationProperty());

            rtlButton = new ToggleButton("RTL");
            rtlButton.setOnAction(event -> root.setNodeOrientation(rtlButton.isSelected() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT));

            ToolBar toolBar = new ToolBar(new Label("Stylesheet:"),
                    stylesheetsBox,
                    rtlButton,
                    retinaButton,
                    scenicViewButton
            );

            toolBar.setId("TestAppToolbar");
            // Create content group used for scaleing @2x
            contentGroup = new Pane() {
                @Override
                protected void layoutChildren() {
                    double scale = contentTabs.getTransforms().isEmpty() ? 1 : ((Scale) contentTabs.getTransforms().get(0)).getX();
                    contentTabs.resizeRelocate(0, 0, getWidth() / scale, getHeight() / scale);
                }
            };
            contentGroup.getChildren().add(contentTabs);
            contentGroup.getStyleClass().add("root");

            // populate root
            root.setTop(toolBar);
            root.setCenter(contentGroup);

            samplePage.getStyleClass().add("needs-background");
            mosaic.getStyleClass().add("needs-background");
            heightTest.getStyleClass().add("needs-background");
            combinationsTest.getStyleClass().add("needs-background");
            simpleWindows.setModena(true);
        } catch (IOException ex) {
            Logger.getLogger(CssShowcaseViewSkin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
