package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.lib.EntityNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 内部ノードとボタンの紐つけ
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class NodeCell extends AnchorPane{
    // 表示レイアウト
    public HBox hbox;
    public VBox vbox;

    public NodeCell parent; // 親
    public List<NodeCell> child; // 階層

    public Label btn;

    public String dispRelStr; // 親との関係性文字列
    public EntityNode propNode; // 自分と紐つく親ノードのプロパティノード
    public EntityNode node; // 自分
    public EntityNode graphNode;
    public OntMenu ontMenu;

    public ComboBox<OntMenu.OntMenuItem> rangeSelector;
    public Label rangeSelectorBtn;

    public boolean visibleroot;  // true:最左ノード

    public EntityNode hypernode; // ハイパーノード

    public SimpleStringProperty dispStr;
    //MouseGestures mouseGestures;

    NodeCell(NodeCell parent, Label btn, EntityNode node) {
        this.btn = btn;
        this.node = node;
        this.graphNode = null;
        this.ontMenu = null;
        this.dispRelStr = null;
        this.parent = parent;
        this.child = new ArrayList<NodeCell>();
        this.visibleroot = false;

        this.hbox = new HBox();
        this.hbox.setAlignment(Pos.CENTER_LEFT); // 縦中央、横左寄せ

        this.hbox.setStyle(AppProperty.BOX_STYLE_FILL_WHITE);
        HBox btn_box = new HBox(); // ラベル用HBox
        //btn_box.getChildren().add(this.btn);
        btn_box.setStyle(AppProperty.BOX_STYLE);
        //this.hbox.getChildren().add(btn_box);

        this.vbox = new VBox();
        this.vbox.setAlignment(Pos.CENTER_LEFT); // 縦中央、横左寄せ

        if (parent != null) {
           // parent.child.add(this);
        }

        // 参照元のノードを統一する
        GraphManager.nodepalette.setContentsString(this.node, "");
        this.dispStr = GraphManager.nodepalette.getContents(this.node);
        if(this.dispStr != null) { // I added this null check
        this.btn.textProperty().bind(this.dispStr);
        }
    }

    /**
     * コンストラクタ（特殊用途）
     * @param node
     * @param disp_str
     */
    NodeCell(EntityNode node, String disp_str) {
        this.node = node;
        this.ontMenu = null;
        // 参照元のノードを統一する
        GraphManager.nodepalette.setContentsString(this.node,disp_str);
        this.dispStr = GraphManager.nodepalette.getContents(this.node);
    }

    /**
     * 表示用文字列の設定
     * @param disp_str
     */
    public void setDispStr(String disp_str) {
        GraphManager.nodepalette.setContentsString(node, disp_str);
        btn.setTextFill(getTextColor());
    }

    /**
     * 表示文字色の設定
     *　ハイパーノードの有無、未入力テキストで判定
     * @return 文字色
     */
    public Color getTextColor() {
        if (hypernode != null) {
            return Color.DEEPPINK;
        }

        String str = dispStr.getValue();
        if (str == null || str.equals(Messages.getString("nodecountents.prompt"))) {
            return Color.GRAY;
        }

        return Color.BLACK;
    }


    public String getDispStr() {
        return dispStr.getValue();
    }

    /**
     * グラフノードの設定
     * @param graph_node グラフノード
     */
    public void setGraphNode(EntityNode graph_node) {
        graphNode = graph_node;
    }

    /**
     * グラフノードの取得
     * @return グラフノード
     */
    public EntityNode getGraphNode() {
        return graphNode;
    }

    /**
     * すでに登録済みかチェック
     * @param ndc
     * @return
     */
    public boolean checkChild(NodeCell ndc) {
        if (child.size() > 0) {
            for (NodeCell tmp : child) {
                if (tmp == ndc) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * ListView<NodeCell> 表示文字列を返すため、ListCell継承し、更新処理をオーバーライド
     */
    public static class NodeCellStr extends ListCell<NodeCell> {
        @Override
        protected  void updateItem(NodeCell ndc, boolean empty) {
            super.updateItem(ndc, empty);
            if (!empty) {
                setText(ndc.dispStr.getValue());
            }

        }
    }

    /**
     * ListViewに設定した文字列は、両脇に括弧がついてしまう。
     * こんな感じ　[〜]　
     * なので、括弧内の文字列と比較する。
     */
    public static NodeCell getSelectedItem(ListView lv) {
        ObservableList selected = lv.getSelectionModel().getSelectedItems();
        NodeCell nc = (NodeCell) selected.get(0);
        return nc;
    }
}



