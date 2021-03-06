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

package grevend.declarativefx.test;

import grevend.declarativefx.component.Component;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @since 0.6.1
 */
public final class ComponentFixture<N extends Node, C extends Component<N>> {

    private final C component;

    @Contract(pure = true)
    public ComponentFixture(@NotNull C component) {
        this.component = component;
    }

    @Contract(pure = true)
    public final C component() {
        return this.component;
    }

    @NotNull
    @Contract("_ -> new")
    public final Mouse mouse(@NotNull Robot robot) {
        return new Mouse(component, robot);
    }

    @NotNull
    @Contract("_ -> new")
    public final Keyboard keyboard(@NotNull Robot robot) {
        return new Keyboard(component, robot);
    }

    @NotNull
    @Contract("_ -> new")
    public final PropertyAssertion<N, C, ComponentFixture<N, C>> prop(@NotNull String property) {
        return new PropertyAssertion<>(this, property);
    }

    /**
     * @since 0.6.1
     */
    public static abstract class Input {

        protected final Component<? extends Node> component;
        protected final Robot robot;

        @Contract(pure = true)
        private Input(@NotNull Component<? extends Node> component, @NotNull Robot robot) {
            this.component = component;
            this.robot = robot;
        }

    }

    /**
     * @since 0.6.1
     */
    public static final class Mouse extends Input {

        private Mouse(@NotNull Component<? extends Node> node, @NotNull Robot robot) {
            super(node, robot);
        }

        @Contract("_ -> this")
        public Mouse delay(int delay) {
            this.robot.delay(delay);
            return this;
        }

        @Contract("_ -> this")
        public Mouse press(@NotNull MouseButton button) {
            this.robot.snap(component);
            this.robot.pressMouse(button);
            return this;
        }

        @Contract("_ -> this")
        public Mouse release(@NotNull MouseButton button) {
            this.robot.pressMouse(button);
            return this;
        }

        @Contract("_ -> this")
        public Mouse click(@NotNull MouseButton button) {
            this.robot.snap(component);
            this.robot.clickMouse(button);
            return this;
        }

        @Contract(" -> this")
        public Mouse click() {
            return this.click(MouseButton.PRIMARY);
        }

        @Contract("_ -> this")
        public Mouse wheel(int amount) {
            this.robot.wheelMouse(amount);
            return this;
        }

    }

    /**
     * @since 0.6.1
     */
    public static final class Keyboard extends Input {

        private Keyboard(@NotNull Component<? extends Node> node, @NotNull Robot robot) {
            super(node, robot);
        }

        @Contract("_ -> this")
        public Keyboard delay(int delay) {
            this.robot.delay(delay);
            return this;
        }

        @Contract("_ -> this")
        public Keyboard press(int code) {
            this.robot.pressKey(code);
            return this;
        }

        @Contract("_ -> this")
        public Keyboard press(@NotNull KeyCode code) {
            this.robot.pressKey(code);
            return this;
        }

        @Contract("_ -> this")
        public Keyboard release(int code) {
            this.robot.releaseKey(code);
            return this;
        }

        @Contract("_ -> this")
        public Keyboard release(@NotNull KeyCode code) {
            this.robot.releaseKey(code);
            return this;
        }

        @Contract("_ -> this")
        public Keyboard enter(char c) {
            this.robot.enterKey(c);
            return this;
        }

        @Contract("_ -> this")
        public Keyboard enter(int code) {
            this.robot.enterKey(code);
            return this;
        }

        @Contract("_ -> this")
        public Keyboard enter(@NotNull String text) {
            this.robot.enterText(text);
            return this;
        }

    }

}
