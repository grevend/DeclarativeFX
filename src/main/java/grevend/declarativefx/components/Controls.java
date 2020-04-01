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

package grevend.declarativefx.components;

import grevend.declarativefx.util.BindableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
//import javafx.scene.image.Image;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
//import static grevend.declarativefx.components;

import java.util.function.Consumer;

import static grevend.declarativefx.components.Compat.FX;

public class Controls {

    public static @NotNull FX<TextField> TextField(@NotNull String placeholder) {
        return FX(new TextField()).set("prompttext", placeholder);
    }

    public static @NotNull FX<TextField> TextField(@NotNull Consumer<String> consumer) {
        return FX(new TextField()).on("text", (observable, oldalue, newValue) -> consumer.accept((String) newValue));
    }

    public static @NotNull FX<TextField> TextField(@NotNull String placeholder, @NotNull Consumer<String> consumer) {
        return TextField(placeholder).on("text", (observable, oldalue, newValue) -> consumer.accept((String) newValue));
    }

    public static @NotNull <V> FX<TextField> TextField(@NotNull BindableValue<V> bindableValue) {
        return FX(new TextField()).bind(bindableValue);
    }

    public static @NotNull <V> FX<TextField> TextField(@NotNull BindableValue<V> bindableValue,
                                                       @NotNull String placeholder) {
        return TextField(bindableValue).set("prompttext", placeholder);
    }

    public static @NotNull FX<TextArea> TextArea(@NotNull String placeholder) {
        return FX(new TextArea()).set("prompttext", placeholder);
    }

    public static @NotNull FX<TextArea> TextArea(@NotNull Consumer<String> consumer) {
        return FX(new TextArea()).on("text", (observable, oldalue, newValue) -> consumer.accept((String) newValue));
    }

    public static @NotNull FX<TextArea> TextArea(@NotNull String placeholder, @NotNull Consumer<String> consumer) {
        return TextArea(placeholder).on("text", (observable, oldalue, newValue) -> consumer.accept((String) newValue));
    }

    public static @NotNull <V> FX<TextArea> TextArea(@NotNull BindableValue<V> bindableValue) {
        return FX(new TextArea()).bind(bindableValue);
    }

    public static @NotNull <V> FX<TextArea> TextArea(@NotNull BindableValue<V> bindableValue,
                                                     @NotNull String placeholder) {
        return TextArea(bindableValue).set("prompttext", placeholder);
    }

    public static @NotNull FX<Button> Button(@NotNull String text) {
        return FX(new Button(text));
    }

    public static @NotNull FX<Button> Button(@NotNull String text, @NotNull EventHandler<ActionEvent> handler) {
        var node = new Button(text);
        node.setOnAction(handler);
        return FX(node);
    }

    // TODO: finish this....
    public static @NotNull FX<Button> Button(@NotNull String text,
                                             String img,
                                             double[] imgSize) {
        var btn = new javafx.scene.control.Button(text);
        if(img.contains("http") || img.contains("https")){ // url
           var image = new Image(img, imgSize[0], imgSize[1], true, true);
            btn.setGraphic(new ImageView(image));
        }else{
            // file??
//            Image temp = new Image("file:"+img, imgSize[0], imgSize[1], true, true);
            btn.setGraphic(Layout.Image(img).construct());
        }
        //Image temp = new Image(getClass().getResourceAsStream(img), imgSize[0], imgSize[1], true, true);

        return FX(new Button(text));
    }

}