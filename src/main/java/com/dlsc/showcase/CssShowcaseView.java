package com.dlsc.showcase;

import com.dlsc.showcase.impl.CssShowcaseViewSkin;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.List;

public class CssShowcaseView extends Control {

    public CssShowcaseView() {
        getStyleClass().add("css-stylesheet-view");

        sceneProperty().addListener(it -> {
            if (getScene() != null) {
                CSSFX.start(this);
            }
        });

        selectedConfigurationProperty().addListener((it, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.getStylesheetUrls().forEach(url -> getStylesheets().remove(url));
            }
            if (newValue != null) {
                newValue.getStylesheetUrls().forEach(url -> getStylesheets().add(url));
            }
        });

        CssConfiguration modenaOnly = new CssConfiguration("Modena only");
        getConfigurations().add(modenaOnly);
        setSelectedConfiguration(modenaOnly);
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
         * @param url one or more stylesheet URLs to add to the configuration
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

        /**>
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
     * @see #selectedConfigurationProperty()
     * @return the list of available configurations
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
}
