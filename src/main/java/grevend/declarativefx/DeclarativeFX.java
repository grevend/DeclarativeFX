/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.declarativefx;

import grevend.declarativefx.component.Component;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class DeclarativeFX {

    private static final Object MUTEX = new Object();
    private static volatile DeclarativeFX INSTANCE;

    private Mode mode;
    private Stage stage;
    private Scene scene;

    @Contract(pure = true)
    private DeclarativeFX() {
        this.mode = Mode.RELEASE;
    }

    @Contract(pure = true)
    public static void show(@NotNull Component<? extends Parent> component, @NotNull Stage stage) {
        getInstance().stage = stage;
        getInstance().show(component);
    }

    public static void launch(@NotNull Component<? extends Parent> component, @NotNull Stage parentStage, @NotNull Modality modality) {
        var stage = new Stage();
        stage.initModality(modality);
        stage.initOwner(parentStage);
        stage.setScene(new Scene(component.getNode()));
        stage.show();
    }

    @Nullable
    @Contract(pure = true)
    public static Component<? extends Node> findById(@NotNull String id, @NotNull Component<? extends Parent> root) {
        for (Component<?> component : root) {
            var componentId = component.get("id");
            if (componentId != null && componentId.equals(id)) {
                return component;
            }
        }
        return null;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static Collection<Component<? extends Node>> findByClass(@NotNull String clazz, @NotNull Component<? extends Parent> root) {
        var components = new ArrayList<Component<? extends Node>>();
        for (Component<?> component : root) {
            if (((Collection<String>) Objects.requireNonNull(component.get("styleclass"))).contains(clazz)) {
                components.add(component);
            }
        }
        return components;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <N extends Node> Collection<Component<N>> findByNode(@NotNull Class<N> clazz, @NotNull Component<? extends Parent> root) {
        var components = new ArrayList<Component<N>>();
        for (Component<?> component : root) {
            if (clazz.isInstance(component.getNode())) {
                components.add((Component<N>) component);
            }
        }
        return components;
    }

    @Nullable
    @Contract(pure = true)
    public static Component<? extends Node> findByMarker(int marker, @NotNull Component<? extends Parent> root) {
        for (Component<?> component : root) {
            var componentMarker = component.get("marker");
            if (componentMarker != null && componentMarker.equals(marker)) {
                return component;
            }
        }
        return null;
    }

    @NotNull
    public static Collection<Component<? extends Node>> findMarked(@NotNull Component<? extends Parent> root) {
        var components = new ArrayList<Component<? extends Node>>();
        for (Component<?> component : root) {
            if (component.isMarked()) {
                components.add(component);
            }
        }
        return components;
    }

    @NotNull
    public static Collection<Component<? extends Node>> findDecorated(@NotNull Component<? extends Parent> root) {
        var components = new ArrayList<Component<? extends Node>>();
        for (Component<?> component : root) {
            if (component.isDecorated()) {
                components.add(component);
            }
        }
        return components;
    }

    @NotNull
    public static DeclarativeFX getInstance() {
        var result = INSTANCE;
        if (result == null) {
            synchronized (MUTEX) {
                result = INSTANCE;
                if (result == null) INSTANCE = result = new DeclarativeFX();
            }
        }
        return result;
    }

    @Contract(pure = true)
    public void show(@NotNull Component<? extends Parent> component) {
        stage.setScene((this.scene = new Scene(component.getNode())));
        stage.show();
    }

    public void addStylesheet(@NotNull String stylesheet, @NotNull Class<?> clazz) {
        if (this.getScene() == null) {
            throw new IllegalStateException("Scene has not been constructed yet.");
        } else {
            if (this.mode == Mode.DEBUG) {
                File file = new File("src/main/resources" + stylesheet);
                if (file.exists()) {
                    this.getScene().getStylesheets().add(
                        (System.getProperty("os.name").toLowerCase().contains("win") ? "file:/" : "file://") +
                            file.getAbsolutePath().replace("\\", "/"));
                    return;
                }
            }
            this.getScene().getStylesheets().add(clazz.getResource(stylesheet).toExternalForm());
        }
    }

    public void removeStylesheet(@NotNull String stylesheet, @NotNull Class<?> clazz) {
        if (this.getScene() == null) {
            throw new IllegalStateException("Scene has not been constructed yet.");
        } else {
            if (this.mode == Mode.DEBUG) {
                File file = new File("src/main/resources" + stylesheet);
                if (file.exists()) {
                    this.getScene().getStylesheets().remove(
                        (System.getProperty("os.name").toLowerCase().contains("win") ? "file:/" : "file://") +
                            file.getAbsolutePath().replace("\\", "/"));
                    return;
                }
            }
            this.getScene().getStylesheets().remove(clazz.getResource(stylesheet).toExternalForm());
        }
    }

    @NotNull
    public Collection<String> getStylesheets() {
        if (this.getScene() == null) {
            throw new IllegalStateException("Scene has not been constructed yet.");
        } else {
            return this.getScene().getStylesheets();
        }
    }

    public void reloadStylesheets() {
        var stylesheets = new ArrayList<>(this.getStylesheets());
        this.getStylesheets().clear();
        Objects.requireNonNull(this.getScene()).getRoot().setStyle("-declarativefx-cache-reset: all;");
        this.getStylesheets().addAll(stylesheets);
    }

    public void enableDeveloperKeyboardShortcuts() {
        if (this.mode != Mode.DEBUG) {
            throw new IllegalStateException("Method enableDeveloperKeyboardShortcuts can only be used in DEBUG mode.");
        } else {
            if (this.getScene() != null) {
                this.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F5), this::reloadStylesheets);
            }
        }
    }

    @Nullable
    public Stage getStage() {
        return stage;
    }

    protected void setStage(@NotNull Stage stage) {
        this.stage = stage;
    }

    @Nullable
    public Scene getScene() {
        return scene;
    }

    protected void setScene(@NotNull Scene scene) {
        this.scene = scene;
    }

    @NotNull
    public Mode getMode() {
        return mode;
    }

    public void setMode(@NotNull Mode mode) {
        this.mode = mode;
    }

    public enum Mode {
        RELEASE, DEBUG;
    }

}