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

package grevend.declarativefx.components.properties;

import grevend.declarativefx.Component;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public interface Findable {

    @Nullable Component<? extends Node> findById(@NotNull String id, boolean root);

    default @Nullable Component<? extends Node> findById(@NotNull String id) {
        return this.findById(id, true);
    }

    @NotNull Collection<Component<? extends Node>> findByClass(
        @NotNull Collection<Component<? extends Node>> components, @NotNull String clazz, boolean root);

    default @NotNull Collection<Component<? extends Node>> findByClass(@NotNull String clazz, boolean root) {
        return this.findByClass(new ArrayList<>(), clazz, root);
    }

    default @NotNull Collection<Component<? extends Node>> findByClass(@NotNull String clazz) {
        return this.findByClass(new ArrayList<>(), clazz, true);
    }

}