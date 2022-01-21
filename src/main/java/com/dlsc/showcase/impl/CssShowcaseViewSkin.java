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
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CssShowcaseViewSkin extends SkinBase<CssShowcaseView> {

    public static final String SKIN_BASE = "com/sun/javafx/scene/control/skin/";

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

    public CssShowcaseViewSkin(CssShowcaseView view) {
        super(view);
        getChildren().add(root = new BorderPane());
        updateView(false, 0, null);
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

    private void updateView(boolean retina, int selectedTab, SamplePage.Section scrolledSection) {
        try {
            // Create sample page and nav
            samplePageNavigation = new SamplePageNavigation();
            samplePage = samplePageNavigation.getSamplePage();
            // Create Content Area
            contentTabs = new TabPane();

            Tab tab1 = new Tab("All Controls");
            tab1.setContent(samplePageNavigation);

            Tab tab2 = new Tab("Mosaic");
            tab2.setContent(new ScrollPane(mosaic = FXMLLoader.load(CssShowcaseViewSkin.class.getResource("ui-mosaic.fxml"))));

            Tab tab3 = new Tab("Alignment");
            tab3.setContent(new ScrollPane(heightTest = FXMLLoader.load(CssShowcaseViewSkin.class.getResource("SameHeightTest.fxml"))));

            Tab tab4 = new Tab("Windows");
            tab4.setContent(new ScrollPane(simpleWindows = new SimpleWindowPage()));

            Tab tab5 = new Tab("Combinations");
            tab5.setContent(new ScrollPane(combinationsTest = FXMLLoader.load(CssShowcaseViewSkin.class.getResource("CombinationTest.fxml"))));

            contentTabs.getTabs().addAll(tab1, tab2, tab3, tab4, tab5);
            contentTabs.getSelectionModel().select(selectedTab);

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
            retinaButton.setSelected(retina);
            retinaButton.setOnAction(event -> {
                ToggleButton btn = (ToggleButton) event.getSource();
                setRetinaMode(btn.isSelected());
            });

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
                    retinaButton
//                    new Label("Base:"),
//                    createBaseColorPicker(),
//                    new Label("Background:"),
//                    createBackgroundColorPicker(),
//                    new Label("Accent:"),
//                    createAccentColorPicker()
            );

            toolBar.setId("TestAppToolbar");
            // Create content group used for scaleing @2x
            Pane contentGroup = new Pane() {
                @Override
                protected void layoutChildren() {
                    double scale = contentTabs.getTransforms().isEmpty() ? 1 : ((Scale) contentTabs.getTransforms().get(0)).getX();
                    contentTabs.resizeRelocate(0, 0, getWidth() / scale, getHeight() / scale);
                }
            };
            contentGroup.getChildren().add(contentTabs);

            // populate root
            root.setTop(toolBar);
            root.setCenter(contentGroup);

            samplePage.getStyleClass().add("needs-background");
            mosaic.getStyleClass().add("needs-background");
            heightTest.getStyleClass().add("needs-background");
            combinationsTest.getStyleClass().add("needs-background");
            simpleWindows.setModena(true);

            // apply retina scale
            if (retina) {
                contentTabs.getTransforms().setAll(new Scale(2, 2));
            }
            root.applyCss();
            // update state
            Platform.runLater(() -> {
                // move focus out of the way
                stylesheetsBox.requestFocus();
                samplePageNavigation.setCurrentSection(scrolledSection);
            });
        } catch (IOException ex) {
            Logger.getLogger(CssShowcaseViewSkin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ColorPicker createBaseColorPicker() {
        ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);
        colorPicker.getCustomColors().addAll(
                Color.TRANSPARENT,
                Color.web("#f3622d"),
                Color.web("#fba71b"),
                Color.web("#57b757"),
                Color.web("#41a9c9"),
                Color.web("#888"),
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.PURPLE,
                Color.MAGENTA,
                Color.BLACK
        );
        colorPicker.valueProperty().addListener((observable, oldValue, c) -> setBaseColor(c));
        colorPicker.setDisable(false);
        return colorPicker;
    }

    public void setBaseColor(Color c) {
    }

    private ColorPicker createBackgroundColorPicker() {
        ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);
        colorPicker.getCustomColors().addAll(
                Color.TRANSPARENT,
                Color.web("#f3622d"),
                Color.web("#fba71b"),
                Color.web("#57b757"),
                Color.web("#41a9c9"),
                Color.web("#888"),
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.PURPLE,
                Color.MAGENTA,
                Color.BLACK
        );
        colorPicker.valueProperty().addListener((observable, oldValue, c) -> {
        });
        colorPicker.setDisable(false);
        return colorPicker;
    }

    private ColorPicker createAccentColorPicker() {
        ColorPicker colorPicker = new ColorPicker(Color.web("#0096C9"));
        colorPicker.getCustomColors().addAll(
                Color.TRANSPARENT,
                Color.web("#0096C9"),
                Color.web("#4fb6d6"),
                Color.web("#f3622d"),
                Color.web("#fba71b"),
                Color.web("#57b757"),
                Color.web("#41a9c9"),
                Color.web("#888"),
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.PURPLE,
                Color.MAGENTA,
                Color.BLACK
        );
        colorPicker.valueProperty().addListener((observable, oldValue, c) -> setAccentColor(c));
        colorPicker.setDisable(false);
        return colorPicker;
    }

    public void setAccentColor(Color c) {
    }


    private String colorToRGBA(Color color) {
        return String.format((Locale) null, "rgba(%d, %d, %d, %f)",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255),
                color.getOpacity());
    }
}
