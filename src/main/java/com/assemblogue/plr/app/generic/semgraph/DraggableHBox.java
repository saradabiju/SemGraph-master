package com.assemblogue.plr.app.generic.semgraph;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

class DraggableHBox extends HBox {
    public DraggableHBox() {
        this.setOnDragDetected(e -> {
            Dragboard db = this.startDragAndDrop(TransferMode.MOVE);

            // This is where the magic happens, you take a snapshot of the HBox.
            db.setDragView(this.snapshot(null, null));

            // The DragView wont be displayed unless we set the content of the dragboard as well.
            // Here you probably want to do more meaningful stuff than adding an empty String to the content.
            ClipboardContent content = new ClipboardContent();
            //content.put(DRAGGABLE_HBOX_TYPE, "");
            db.setContent(content);

            e.consume();
        });
    }
}
