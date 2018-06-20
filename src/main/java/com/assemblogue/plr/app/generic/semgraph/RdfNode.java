package com.assemblogue.plr.app.generic.semgraph;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RDFグラフの個々のノード
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class RdfNode {

    public enum EdgeDir{
        NON,
        PARENT,     //このノードから親に矢印　親<-子
        CHILD       //親からこのノードに矢印　親->子
    }

    // 親ノード複数対応
    public class ParentAttr {
        public RdfNode node;        //親のノード     親はnull
        public String cursolText;   //矢印のテキスト　親は""
        public EdgeDir edgeDir;     //ノードの向き enum EdgeDir引数

        ParentAttr(RdfNode rdfnd, String cursolText, EdgeDir edgeDir) {
            this.node = rdfnd;
            this.cursolText = cursolText;
            this.edgeDir = edgeDir;
        }
    }

    public static final float PREFSIZE_WIDTH = 200;
    public static final float PREFSIZE_HEIGHT = 40;
    public static final float NODE_Y_MARGIN = 20;       //ノードの縦の間隔

    public String id;   //識別用ID 作成時に設定するID
    public String text; //このノード自体のテキスト
    public Label label;

    public Integer shared; // このノードを子供とする親の数

    public Map<RdfNode,ParentAttr> parent;  //親との接続情報（矢印の向き、関係性） 複数の親を持たせる場合ここを増やす
    public ArrayList<RdfNode> childNode;    //子のノード
    public CubicCurve edgePolygon;


    public RdfNode(String id, String text, Group group, double max_node_position_y){
        this.shared = 0;
        this.id = id;
        this.text = text;
        this.childNode = new ArrayList<>();

        this.parent = new LinkedHashMap<>();

        this.label = new Label(text);
        // "-fx-border-color: black; -fx-background-color: lavender;"
        this.label.setStyle(AppProperty.RDFGRAPH_BOX_STYLE);
        this.label.setPrefSize(PREFSIZE_WIDTH,PREFSIZE_HEIGHT);
        this.label.setLayoutX(0);
        this.label.setLayoutY(max_node_position_y + PREFSIZE_HEIGHT + NODE_Y_MARGIN);
        this.label.setAlignment(Pos.TOP_LEFT);
        this.label.setWrapText(true);
     //   MouseControlUtil.makeDraggable(label);
        group.getChildren().add(this.label);
    }

    public void setParent(RdfNode rdfnd, String cursolText, EdgeDir edgeDir) {
        ParentAttr attr = new ParentAttr(rdfnd, cursolText, edgeDir);
        parent.put(rdfnd, attr);
    }

}
