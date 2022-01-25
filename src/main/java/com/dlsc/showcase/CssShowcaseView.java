package com.dlsc.showcase;

import com.dlsc.showcase.impl.CssShowcaseViewSkin;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A control that showcases all JavaFX controls individually and in combination with
 * each other. Different stylesheet configurations can be assigned to the view so that
 * developers can immediately see the impact of any changes they make to their styling
 * code.
 */
public class CssShowcaseView extends Control {

    public CssShowcaseView() {
        getStyleClass().add("css-showcase-view");

        sceneProperty().addListener(it -> {
            if (getScene() != null) {
                CSSFX.start(this);
            }
        });

        CssConfiguration modenaOnly = new CssConfiguration("Modena only");
        getConfigurations().add(modenaOnly);
        setSelectedConfiguration(modenaOnly);

        getConfigurations().addListener((Observable it) -> {
            if (getConfigurations().isEmpty()) {
                getConfigurations().add(modenaOnly);
                setSelectedConfiguration(modenaOnly);
            }
        });

        setOnDragEntered(evt -> getStyleClass().add("drag-over"));
        setOnDragExited(evt -> getStyleClass().remove("drag-over"));

        setOnDragOver(evt -> {
            if (evt.getDragboard().hasFiles()) {
                evt.getDragboard().getFiles().forEach(file -> {
                    if (file.getName().toLowerCase().endsWith(".css")) {
                        evt.acceptTransferModes(TransferMode.ANY);
                        return;
                    }
                });
            }
        });

        setOnDragDropped(evt -> {
            List<File> files = evt.getDragboard().getFiles();
            int count = files.size();
            String[] urls = new String[count];


            for (int i = 0; i < files.size(); i++) {
                try {
                    File file = files.get(i);
                    urls[i] = file.toURI().toURL().toExternalForm();
                    CssConfiguration config = new CssConfiguration(files.stream().map(f -> f.getName()).collect(Collectors.joining(", ")), urls);
                    getConfigurations().add(config);
                    setSelectedConfiguration(config);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CssShowcaseViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return CssShowcaseView.class.getResource("css-showcase-view.css").toExternalForm();
    }

    public static class CssConfiguration {

        private String name;

        private List<String> stylesheetUrls = new ArrayList<>();

        /**
         * Constructs a new configuration. Stylesheet URLs have to be added
         * by calling {@link #getStylesheetUrls()} and adding to the list.
         *
         * @param name the name of the configuration, e.g. "My three custom stylesheets".
         */
        public CssConfiguration(String name) {
            this(name, new String[]{});
        }

        /**
         * Constructs a new configuration. Stylesheet URLs have to be added
         * by calling {@link #getStylesheetUrls()} and adding to the list.
         *
         * @param name the name of the configuration, e.g. "My three custom stylesheets".
         * @param url  one or more stylesheet URLs to add to the configuration
         */
        public CssConfiguration(String name, String... url) {
            this.name = name;

            for (String u : url) {
                this.stylesheetUrls.add(u);
            }
        }

        /**
         * Returns the name of the configuration.
         *
         * @return the configuration name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the list of stylesheet URLs added to this configuration
         *
         * @return the list of stylesheet URLs
         */
        public List<String> getStylesheetUrls() {
            return stylesheetUrls;
        }
    }

    private final ListProperty<CssConfiguration> configurations = new SimpleListProperty<>(this, "configurations", FXCollections.observableArrayList());

    public final ObservableList<CssConfiguration> getConfigurations() {
        return configurations.get();
    }

    /**
     * Stores a list of stylesheet configurations that the user can select individually.
     *
     * @return the list of available configurations
     * @see #selectedConfigurationProperty()
     */
    public final ListProperty<CssConfiguration> configurationsProperty() {
        return configurations;
    }

    public final void setConfigurations(ObservableList<CssConfiguration> configurations) {
        this.configurations.set(configurations);
    }

    private final ObjectProperty<CssConfiguration> selectedConfiguration = new SimpleObjectProperty<>(this, "selectedConfiguration");

    public final CssConfiguration getSelectedConfiguration() {
        return selectedConfiguration.get();
    }

    /**
     * The currently selected CSS configuration.
     *
     * @return the currently active configuration
     */
    public final ObjectProperty<CssConfiguration> selectedConfigurationProperty() {
        return selectedConfiguration;
    }

    public final void setSelectedConfiguration(CssConfiguration selectedConfiguration) {
        this.selectedConfiguration.set(selectedConfiguration);
    }

    private final ListProperty<Tab> additionalTabs = new SimpleListProperty<>(FXCollections.observableArrayList());

    public ObservableList<Tab> getAdditionalTabs() {
        return additionalTabs.get();
    }

    /**
     * A list of application specific tabs that will be added to the built-in tabs.
     *
     * @return list of additional tabs
     */
    public ListProperty<Tab> additionalTabsProperty() {
        return additionalTabs;
    }

    public void setAdditionalTabs(ObservableList<Tab> additionalTabs) {
        this.additionalTabs.set(additionalTabs);
    }
}
