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

import grevend.declarativefx.bindable.BindException;
import grevend.declarativefx.bindable.BindableCollection;
import grevend.declarativefx.bindable.BindableValue;
import grevend.declarativefx.components.Root;
import grevend.declarativefx.event.EventHandler;
import grevend.declarativefx.lifecycle.LifecycleException;
import grevend.declarativefx.properties.*;
import grevend.declarativefx.util.StringifiableHierarchy;
import grevend.declarativefx.util.Triplet;
import grevend.declarativefx.util.Utils;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableObjectValue;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Component<N extends Node>
    implements Lifecycle<N>, Fluent<N>, Bindable<N>, Listenable<N>, Identifiable<N>, Findable, Styleable<N>,
    StringifiableHierarchy, BiConsumer<BindableCollection.Change, Collection<? extends Component<? extends Node>>> {

    private final BindableCollection<Component<? extends Node>> children;
    private final Map<String, BindableValue> bindableProperties;
    private final Map<String, ObservableValue<Object>> observableProperties;
    private final Collection<Triplet<String, Object, Object>> lateBindings;
    private N node;
    private Component<? extends Node> parent;
    private String defaultProperty;

    public Component(@Nullable N node, @NotNull Collection<Component<? extends Node>> children) {
        this.node = node;
        this.children =
            children instanceof BindableCollection ?
                BindableCollection.of(children.stream().filter(Objects::nonNull).collect(Collectors.toList())) :
                BindableCollection.of(children, Objects::nonNull);
        this.bindableProperties = new HashMap<>();
        this.observableProperties = new HashMap<>();
        this.lateBindings = new ArrayList<>();
        this.children.forEach(child -> child.setParent(this));
        this.children.subscribe(this);
    }

    @SafeVarargs
    public Component(@Nullable N node, @Nullable Component<? extends Node>... children) {
        this(node, BindableCollection.of(children));
    }

    public Component(@Nullable N node) {
        this(node, List.of());
    }

    @Deprecated
    public Component(@NotNull Component<N> child) {
        this(null, List.of(child));
        child.beforeConstruction();
        this.setNode(child.construct());
        child.afterConstruction();
    }

    public @Nullable N getNode() {
        return this.node;
    }

    public void setNode(@Nullable N node) {
        this.node = node;
    }

    public @Nullable Component<? extends Node> getParent() {
        return this.parent;
    }

    public void setParent(@NotNull Component<? extends Node> parent) {
        this.parent = parent;
    }

    public @NotNull BindableCollection<Component<? extends Node>> getChildren() {
        return this.children;
    }

    public @NotNull Root<?> getRoot() {
        if (this.parent == null) {
            throw new IllegalStateException(
                "Component '" + this.toString() + "' should be Root or is missing a parent component.");
        }
        return this.parent.getRoot();
    }

    @Override
    public void beforeConstruction() {
        this.children.forEach(Lifecycle::beforeConstruction);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable N construct() {
        if (this.node instanceof Pane && !(this.node instanceof BorderPane)) {
            var pane = ((Pane) this.node);
            pane.getChildren().clear();
            for (Component<? extends Node> component : this.children) {
                var child = component.construct();
                if (child != null && child != node) {
                    pane.getChildren().add(child);
                }
            }
        } else if (this.node == null && this.children.size() == 1) {
            return (this.node = (N) this.children.iterator().next().construct());
        }
        return this.node;
    }

    @SuppressWarnings("unchecked")
    private void lateBind(@NotNull String property, @NotNull BindableValue bindableValue) {
        if (this.node != null) {
            var observableValue = Utils.getObservableValue(this.node, this.observableProperties, property);
            if (observableValue != null) {
                this.bindableProperties.put(property, bindableValue);
                //observableValue.addListener(observable -> bindableValue.set(observableValue.getValue()));
                if (observableValue instanceof WritableObjectValue) {
                    bindableValue.subscribe(((WritableObjectValue<Object>) observableValue)::setValue);
                }
            } else {
                throw new IllegalArgumentException("Property " + property.toLowerCase() + " does not exist.");
            }
        } else {
            throw new LifecycleException();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterConstruction() {
        for (var binding : this.lateBindings) {
            if (binding.getA() != null && binding.getB() != null) {
                if (binding.getB() instanceof BindableValue && binding.getC() == null) {
                    this.lateBind(binding.getA(), (BindableValue) binding.getB());
                } else if (binding.getB() instanceof String && binding.getC() == null) {
                    var bindableValue = this.getBinding((String) binding.getB());
                    if (bindableValue != null) {
                        this.lateBind(binding.getA(), bindableValue);
                    }
                } else if (binding.getB() instanceof BindableValue && binding.getC() instanceof Function) {
                    if (this.getPropertyBinding(binding.getA()) != null) {
                        Objects.requireNonNull(this.getPropertyBinding(binding.getA()))
                            .compute((BindableValue) binding.getB(), (Function<BindableValue, Object>) binding.getC());
                    } else {
                        throw new BindException(this.toString());
                    }
                } else if (binding.getB() instanceof BindableValue && binding.getC() instanceof Supplier) {
                    if (this.getPropertyBinding(binding.getA()) != null) {
                        Objects.requireNonNull(this.getPropertyBinding(binding.getA()))
                            .compute((BindableValue) binding.getB(), (Supplier<Object>) binding.getC());
                    } else {
                        throw new BindException(this.toString());
                    }
                } else {
                    throw new BindException("Late binding failed for " + binding.getA() + " on " + this + ".");
                }
            }
        }
        this.children.forEach(Lifecycle::afterConstruction);
    }

    @Override
    public void deconstruct() {
        this.children.forEach(Lifecycle::deconstruct);
    }

    @Override
    @Deprecated
    public @Nullable String getDefaultProperty() {
        return this.defaultProperty;
    }

    @Override
    @Deprecated
    public @NotNull Component<N> setDefaultProperty(@NotNull String property) {
        this.defaultProperty = property;
        return this;
    }

    @Override
    @Deprecated
    public @NotNull Map<String, BindableValue> getBindableValues() {
        return this.bindableProperties;
    }

    @Override
    @Deprecated
    public @NotNull Collection<Triplet<String, Object, Object>> getLateBindings() {
        return this.lateBindings;
    }

    @Override
    @Deprecated
    public @Nullable BindableValue getBinding(@NotNull String id) {
        return this.getRoot().getProviders().get(id);
    }

    @Override
    public @Nullable BindableValue getPropertyBinding(@NotNull String property) {
        return this.bindableProperties.get(property);
    }

    @Override
    public @Nullable Component<? extends Node> findById(@NotNull String id, boolean root) {
        if (this.getId() != null && this.getId().equals(id)) {
            return this;
        } else if (root) {
            return this.getRoot().findById(id, false);
        } else {
            for (Component<? extends Node> component : this.children) {
                var child = component.findById(id, false);
                if (child != null) {
                    return child;
                }
            }
            return null;
        }
    }

    @Override
    public @NotNull Collection<Component<? extends Node>> findByClass(
        @NotNull Collection<Component<? extends Node>> components, @NotNull String clazz, boolean root) {
        if (root) {
            return this.getRoot().findByClass(components, clazz, false);
        } else if (this.getClasses().contains(clazz)) {
            components.add(this);
        }

        this.children.forEach(child -> child.findByClass(components, clazz, false));
        return components;
    }

    @Override
    public @NotNull Component<N> fluent(@NotNull Consumer<N> consumer) {
        consumer.accept(this.node);
        return this;
    }

    @Override
    public @NotNull Component<N> self(@NotNull Consumer<Component<N>> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Component<N> set(@NotNull String property, @Nullable Object value) {
        if (property.toLowerCase().equals("id")) {
            if (value instanceof String) {
                this.setId((String) value);
            }
        } else if (property.toLowerCase().equals("style")) {
            if (value instanceof String) {
                this.setStyle((String) value);
            }
        } else {
            if (this.node != null) {
                var observableValue = Utils.getObservableValue(this.node, this.observableProperties, property);
                if (observableValue != null) {
                    if (observableValue instanceof WritableObjectValue) {
                        ((WritableObjectValue<Object>) observableValue).setValue(value);
                    }
                } else {
                    throw new IllegalArgumentException("Property " + property.toLowerCase() + " does not exist.");
                }
            } else {
                throw new LifecycleException();
            }
        }
        return this;
    }

    @Override
    public @Nullable Object get(@NotNull String property) {
        if (this.node != null) {
            var observableValue = Utils.getObservableValue(this.node, this.observableProperties, property);
            if (observableValue != null) {
                return observableValue.getValue();
            } else {
                throw new IllegalArgumentException("Property " + property.toLowerCase() + " does not exist.");
            }
        } else {
            throw new LifecycleException();
        }
    }

    @Override
    public @Nullable String getId() {
        if (this.node != null) {
            return this.node.getId();
        } else {
            throw new LifecycleException();
        }
    }

    @Override
    public @NotNull Component<N> setId(@NotNull String id) {
        if (this.node != null) {
            this.node.setId(id);
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    @Override
    public @NotNull <E extends Event> Component<N> on(@NotNull EventType<E> type, @NotNull EventHandler<E> handler) {
        if (this.node != null) {
            this.node.addEventHandler(type, event -> handler.onEvent(event, this));
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    @Override
    @Deprecated
    public @NotNull <E extends Event> Component<N> on(@NotNull EventType<E> type,
                                                      javafx.event.@NotNull EventHandler<E> handler) {
        if (this.node != null) {
            this.node.addEventHandler(type, handler);
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T> Component<N> on(@NotNull String property, @NotNull ChangeListener<T> listener) {
        if (this.node != null) {
            var observableValue = Utils.getObservableValue(this.node, this.observableProperties, property);
            if (observableValue != null) {
                observableValue.addListener((ChangeListener<Object>) listener);
            } else {
                throw new IllegalArgumentException("Property " + property.toLowerCase() + " does not exist.");
            }
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    @Override
    public @NotNull Component<N> on(@NotNull String property, @NotNull InvalidationListener listener) {
        if (this.node != null) {
            var observableValue = Utils.getObservableValue(this.node, this.observableProperties, property);
            if (observableValue != null) {
                observableValue.addListener(listener);
            } else {
                throw new IllegalArgumentException("Property " + property.toLowerCase() + " does not exist.");
            }
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    public Component<N> add(@NotNull Component<? extends Node> component) {
        this.children.add(component);
        return this;
    }

    public Component<N> addAll(@NotNull Collection<? extends Component<? extends Node>> components) {
        this.children.addAll(components);
        return this;
    }

    public Component<N> remove(@NotNull Component<? extends Node> component) {
        this.children.remove(component);
        return this;
    }

    public Component<N> removeAll(@NotNull Collection<? extends Component<? extends Node>> component) {
        this.children.removeAll(component);
        return this;
    }

    @Override
    public @NotNull String getStyle() {
        if (this.node != null) {
            return this.node.getStyle();
        } else {
            throw new LifecycleException();
        }
    }

    @Override
    public @NotNull Component<N> setStyle(@NotNull String style) {
        if (this.node != null) {
            this.node.setStyle(style);
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    @Override
    public @NotNull Component<N> addClass(@NotNull String clazz) {
        if (this.node != null) {
            this.node.getStyleClass().add(clazz);
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    @Override
    public @NotNull Component<N> removeClass(@NotNull String clazz) {
        if (this.node != null) {
            this.node.getStyleClass().add(clazz);
        } else {
            throw new LifecycleException();
        }
        return this;
    }

    @Override
    public @NotNull Collection<String> getClasses() {
        if (this.node != null) {
            return this.node.getStyleClass();
        } else {
            throw new LifecycleException();
        }
    }

    @Override
    public @NotNull String toString() {
        var builder = new StringBuilder();
        if (this.node != null) {
            builder.append(this.node.getClass().getTypeName()).append(this.getId() != null ? ("#" + this.getId()) : "")
                .append(this.getClasses().size() > 0 ? ("|" + String.join(",", this.getClasses())) : "");
        } else {
            builder.append(this.getClass().getTypeName());
        }
        return builder.toString();
    }

    @Override
    public @NotNull String stringify(@NotNull Verbosity verbosity) {
        return this.toString();
    }

    @Override
    public void stringifyHierarchy(@NotNull StringBuilder builder, @NotNull String prefix, @NotNull String childPrefix,
                                   @NotNull Verbosity verbosity) {
        builder.append(prefix).append(this.toString()).append(System.lineSeparator());
        for (var componentIter = this.getChildren().iterator(); componentIter.hasNext(); ) {
            var nextComponent = componentIter.next();
            if (nextComponent != this) {
                var hasNextComponent = componentIter.hasNext();
                nextComponent.stringifyHierarchy(builder, childPrefix + (hasNextComponent ? "├── " : "└── "),
                    childPrefix + (hasNextComponent ? "│   " : "    "), verbosity);
            }
        }
    }

    @Override
    public void accept(BindableCollection.Change collectionChange,
                       Collection<? extends Component<? extends Node>> components) {
        if (this.node instanceof Pane) {
            if (collectionChange == BindableCollection.Change.ADD) {
                components.forEach(component -> component.setParent(this));
                components.forEach(Component::beforeConstruction);
                ((Pane) this.node).getChildren()
                    .addAll(components.stream().map(Component::construct).collect(Collectors.toList()));
                components.forEach(Component::afterConstruction);
            } else {
                ((Pane) this.node).getChildren()
                    .removeAll(components.stream().map(Component::getNode).collect(Collectors.toList()));
                components.forEach(Component::deconstruct);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public @NotNull <E> Component<N> builder(@NotNull Collection<E> collection,
                                             @NotNull Function<E, Component<? extends Node>> build) {
        if (collection instanceof BindableCollection) {
            ((BindableCollection<E>) collection).subscribe((change, changes) -> {
                this.children.clear();
                var components = new ArrayList<Component<? extends Node>>();
                for (E element : collection) {
                    components.add(build.apply(element));
                }
                this.addAll(components);
            });
            ((BindableCollection<E>) collection).getConsumers()
                .forEach(consumer -> consumer.accept(BindableCollection.Change.NONE, List.of()));
            return this;
        } else {
            return this.builder(BindableCollection.of(collection), build);
        }
    }

}
