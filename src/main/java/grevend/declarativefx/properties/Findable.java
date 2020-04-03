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

package grevend.declarativefx.properties;

import grevend.declarativefx.Component;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface Findable {

    @Nullable Component<? extends Node> find(@NotNull String id, boolean root);

    default @Nullable Component<? extends Node> find(@NotNull String id) {
        return this.find(id, true);
    }

    default @Nullable Collection<Component<? extends Node>> find(@NotNull String... identifiers) {
        return Arrays.stream(identifiers).map(this::find).filter(Objects::nonNull).collect(Collectors.toList());
    }

    default @Nullable Collection<Component<? extends Node>> find(@NotNull Iterable<String> identifiers) {
        return StreamSupport.stream(identifiers.spliterator(), false).map(this::find).filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

}
