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

package grevend.declarativefx.example;

import grevend.declarativefx.util.BindableCollection;
import grevend.declarativefx.util.BindableValue;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static grevend.declarativefx.components.Compat.Root;
import static grevend.declarativefx.components.Controls.Button;
import static grevend.declarativefx.components.Layout.*;

public class Example extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        stage.setWidth(175);
        stage.setHeight(100);

        BindableValue counter = new BindableValue(0);
        var list = BindableCollection.of(List.of("1","2"),List.of("3","4"));

        var root = Root(
            HBox(
                VBox(
                    Text("Value: 0").compute("text", counter, () -> "Value: " + counter.get()),
                    Button("Increment").on((event, component) -> {
                        counter.update(before -> (int) before + 1);
                        list.add(List.of(counter.get().toString(), "5"));
                    })
                ),
                VBox().builder(list, el -> Text(el.toString()))
            )
        );

        root.launch(stage);
        System.out.println(root.stringifyHierarchy());
    }

}