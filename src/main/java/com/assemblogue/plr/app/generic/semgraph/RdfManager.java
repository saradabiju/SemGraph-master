package com.assemblogue.plr.app.generic.semgraph;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polygon;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RDFグラフ描画クラス
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class RdfManager {
    //ノードの位置調整用
    public double MaxNodePositionY;

    public Map<String,RdfNode> nodeList;
    public Map<String,RdfNode> headNode;

    //コンストラクタ
    public  RdfManager()
    {
        Initialize();
    }

    public void Initialize() {
        MaxNodePositionY = 0;
        nodeList = new LinkedHashMap<>();
        headNode = new LinkedHashMap<>();
    }

    //ノード同士の接続処理
    //CreateNodeで作成したノード同士を接続させる
    public void NodeConnect(RdfNode parent, RdfNode child, RdfNode.EdgeDir edgeDir, String edgeText){
        if(headNode.containsKey(child.id)) {
            headNode.remove(child.id);
        }

        if (!parent.childNode.contains(child)) {
            parent.childNode.add(child);
        }

        child.setParent(parent, edgeText, edgeDir);
    }

    /*
    public void NodeConnect(String parent, String child, RdfNode.EdgeDir edgeDir, String edgeText) {
        RdfNode parentNode = nodeList.get(parent);
        RdfNode childNode = nodeList.get(child);
        NodeConnect(parentNode,childNode,edgeDir,edgeText);
    }
    */

    //描画命令
    //これを呼ばないとグラフが表示されない
    //事前にRdfManagerでCreateNodeを使用してノードを作成し、NodeConnectでノード同士を接続する必要あり
    public void Draw(Group group){
        Map<String, RdfNode> footprint = new LinkedHashMap<>();

        for (String key : headNode.keySet()) {
            RdfNode parent = headNode.get(key);
            MaxNodePositionY += (RdfNode.PREFSIZE_HEIGHT + RdfNode.NODE_Y_MARGIN);
            parent.label.setLayoutY(MaxNodePositionY);
            if(parent.childNode.isEmpty())continue;

            int count = 0;
            for(RdfNode child : parent.childNode) {
                if (child.parent.containsKey(parent)) {
                    RdfNode.ParentAttr attr = child.parent.get(parent);
                    DrawConnectEdge(footprint, parent, child,group, attr.edgeDir, attr.cursolText, count++);
                } else {
                    DrawConnectEdge(footprint, parent, child,group, RdfNode.EdgeDir.NON, "", count++);
                }
            }
        }
     }

    //ノードの接続
    public void DrawConnectEdge(Map<String,RdfNode> footprint, RdfNode parent,RdfNode child,Group group,RdfNode.EdgeDir edgeDir,String cursolText,int counter){
        boolean reentry = false;
        if (footprint.containsKey(child.id)) {
            reentry = true;
        } else {
            footprint.put(child.id, child);
        }

        final int EDGE_MARGIN = 5;          //0だと矢印がラベルにくっついてしまう
        final double EDGE_SCALE = 30;       //矢印自体の大きさ
        int EDGE_HEAD_SIZE = 3;             //矢印の頭を伸ばす
        double setPosY = MaxNodePositionY;

        if (child.shared == 0) {
            //子の位置の更新
            child.label.setLayoutX(child.label.getLayoutX() + parent.label.getLayoutX() + RdfNode.PREFSIZE_WIDTH * 2);

            //子ノードの位置を親の子供順に配置
            setPosY = parent.label.getLayoutY() + (RdfNode.PREFSIZE_HEIGHT + RdfNode.NODE_Y_MARGIN) * counter;
            //子の位置は現在配置されているノードのY位置より上に配置しようとしているか
            if (counter != 0) {
                if (MaxNodePositionY >= setPosY) {
                    setPosY = MaxNodePositionY + (RdfNode.PREFSIZE_HEIGHT + RdfNode.NODE_Y_MARGIN);
                }
            }
            child.label.setLayoutY(setPosY);
        }
        child.shared++;

        //矢印の線を作成
        CubicCurve edge = new CubicCurve();
        Point2D mDragOffset = new Point2D (0.0, 0.0);

        // 親子の位置関係は？
        float dw = 0f;
        if (parent.label.getLayoutX()+RdfNode.PREFSIZE_WIDTH > child.label.getLayoutX()) {
            dw = RdfNode.PREFSIZE_WIDTH + EDGE_MARGIN*2.0f;
        }

        //始点
        edge.setStartX(parent.label.getLayoutX() + RdfNode.PREFSIZE_WIDTH + EDGE_MARGIN);
        edge.setStartY(parent.label.getLayoutY() + RdfNode.PREFSIZE_HEIGHT / 1.5f);
        //カーブ点1
        edge.setControlX1(parent.label.getLayoutX() + RdfNode.PREFSIZE_WIDTH + RdfNode.PREFSIZE_WIDTH / 2);
        edge.setControlY1(parent.label.getLayoutY() + RdfNode.PREFSIZE_HEIGHT / 2);
        //カーブ点2
        edge.setControlX2(parent.label.getLayoutX() + RdfNode.PREFSIZE_WIDTH * 2 - RdfNode.PREFSIZE_WIDTH / 2);
        edge.setControlY2(child.label.getLayoutY() + RdfNode.PREFSIZE_HEIGHT / 2);
        //終点
        edge.setEndX(child.label.getLayoutX() + dw - EDGE_MARGIN);
        edge.setEndY(child.label.getLayoutY() + RdfNode.PREFSIZE_HEIGHT / 2);
        //塗りつぶし
        edge.setFill(Paint.valueOf("#1f93ff00"));
        //色
        edge.setStroke(Paint.valueOf("BLACK"));

        //矢印の頭を作成
        //ここを参考:https://stackoverflow.com/questions/26702519/javafx-line-curve-with-arrow-head
        int arrowDir = 1;
        float arrowDir2 = -0.2f;
        if(edgeDir == RdfNode.EdgeDir.CHILD){//矢印の反転
            arrowDir = 0;
            arrowDir2 *= -1;
            EDGE_HEAD_SIZE *= -1;
            edge.setStartY(parent.label.getLayoutY() + RdfNode.PREFSIZE_HEIGHT / 4);
        }

        if (dw != 0.0f) {
            EDGE_HEAD_SIZE *= -1;
        }

        Point2D ori = evals(edge,arrowDir);
        Point2D tan = evalDt(edge,1).normalize().multiply(EDGE_SCALE);
        Polygon head = new Polygon(
                ori.getX()+arrowDir2*tan.getX()-0.2*tan.getY(),
                ori.getY()+arrowDir2*tan.getY()+0.2*tan.getX(),
                ori.getX() + EDGE_HEAD_SIZE,
                ori.getY(),
                ori.getX()+arrowDir2*tan.getX()+0.2*tan.getY(),
                ori.getY()+arrowDir2*tan.getY()-0.2*tan.getX()
        );

        //矢印のテキスト作成
        Label edgeLabel = new Label(cursolText);
        edgeLabel.setPrefSize(200,70);
        edgeLabel.setAlignment(Pos.TOP_LEFT);
        edgeLabel.setWrapText(true);
        edgeLabel.setLayoutX(edge.getEndX() - 25 - edgeLabel.getText().length() * 5);
        edgeLabel.setLayoutY(edge.getEndY()+(-45+25*child.shared));  // -45+25*child.shared　線を跨いでテキストを出すため

        group.getChildren().addAll(edgeLabel,edge,head);
        child.edgePolygon = edge;

        if (!reentry) {
            //再起呼び出し
            int count = 0;
            for (RdfNode childNode : child.childNode) {
                //DrawConnectEdge(child, childNode,group,childNode.edgeDir,childNode.parent.cursolText,count++);
                if (childNode.parent.containsKey(child)) {
                    RdfNode.ParentAttr attr = childNode.parent.get(child);
                    DrawConnectEdge(footprint, child, childNode, group, attr.edgeDir, attr.cursolText, count++);
                } else {
                    DrawConnectEdge(footprint, child, childNode, group, RdfNode.EdgeDir.NON, "", count++);
                }
            }

            //現在配置されているノードのY位置の値の更新
            if (MaxNodePositionY <= setPosY) {
                MaxNodePositionY = setPosY;
            }
        }
    }

    public static Point2D evals(CubicCurve c, float t){
        Point2D p=new Point2D(Math.pow(1-t,3)*c.getStartX()+
                3*t*Math.pow(1-t,2)*c.getControlX1()+
                3*(1-t)*t*t*c.getControlX2()+
                Math.pow(t, 3)*c.getEndX(),
                Math.pow(1-t,3)*c.getStartY()+
                        3*t*Math.pow(1-t, 2)*c.getControlY1()+
                        3*(1-t)*t*t*c.getControlY2()+
                        Math.pow(t, 3)*c.getEndY());
        return p;
    }

    private static Point2D evalDt(CubicCurve c, float t){
        Point2D p=new Point2D(-3*Math.pow(1-t,2)*c.getStartX()+
                3*(Math.pow(1-t, 2)-2*t*(1-t))*c.getControlX1()+
                3*((1-t)*2*t-t*t)*c.getControlX2()+
                3*Math.pow(t, 2)*c.getEndX(),
                -3*Math.pow(1-t,2)*c.getStartY()+
                        3*(Math.pow(1-t, 2)-2*t*(1-t))*c.getControlY1()+
                        3*((1-t)*2*t-t*t)*c.getControlY2()+
                        3*Math.pow(t, 2)*c.getEndY());
        return p;
    }
   

    //ほぼ先頭ノード作成用
    public RdfNode createNode(String nodeText,String createNodeID,Group group) {
        RdfNode node = new RdfNode(createNodeID,nodeText,group, this.MaxNodePositionY);
        nodeList.put(createNodeID,node);
        headNode.put(createNodeID,node);
        return node;
    }

    /**
     * 内部リストのクリア
     */
    public void clear() {
        nodeList.clear();
        headNode.clear();
    }

}

