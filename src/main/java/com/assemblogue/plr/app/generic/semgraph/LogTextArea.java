package com.assemblogue.plr.app.generic.semgraph;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;


/**
 * 行末事情スクロースするTextArea
 * http://stackoverflow.com/questions/17799160/javafx-textarea-and-autoscroll
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class LogTextArea extends TextArea {
    private final BooleanProperty pausedScrollProperty = new SimpleBooleanProperty(false);
    private double scrollPosition = 0;

    public LogTextArea() {
        super();
    }

    public void setMessage(String data) {
        if (isPausedScroll()) {
            scrollPosition = this.getScrollTop();
            this.setText(data);
            this.setScrollTop(scrollPosition);
        } else {
            this.setText(data);
            this.setScrollTop(Double.MAX_VALUE);
        }
    }

    public final BooleanProperty pausedScrollProperty() { return pausedScrollProperty; }
    public final boolean isPausedScroll() { return pausedScrollProperty.getValue(); }
    public final void setPausedScroll(boolean value) { pausedScrollProperty.setValue(value); }
}

