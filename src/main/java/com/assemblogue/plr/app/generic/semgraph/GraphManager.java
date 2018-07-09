
package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.lib.EntityNode;

import java.util.*;

/**
 * グラフのノード全体にかかわる処理
 * シングルトン
 *
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class GraphManager {

    // AppControllerは一つだけなので。。
    static AppController appController = null;
    // 全ノードのとりまとめ
    static NodePalette nodepalette = new NodePalette();

    // オープン中グラフ情報
    // ハイパーノード、ノード検索向け
    static class GraphPain {
        GraphActor graph;
        //NodePainController ctrlr;
        RootLayout rootlayout;

        GraphPain(GraphActor graph, RootLayout rootlay) {
            this.graph = graph;
            this.rootlayout = rootlay;
        }
    };

    // オープン中のGraphActor
	private static Map<String, GraphPain> map = new LinkedHashMap<>();

    // ノードコピペ用記憶領域
    private static NodeCell target;

    // グラフコピペ用記憶領域（所謂ハイパーノード用）
    private static EntityNode hyperTarget;
    private static String hyperTargetName;

    /**
     * AppControllerの登録
     * @param apc AppController
     */
    public static void setAppController(AppController apc) {
        appController = apc;
    }

    /**
     * AppControllerの取得
     * @return AppController
     */
    public static AppController getAppController() {
        return appController;
    }

    /**
     * グラフの多重オープンチェック
     * @param grpact オープンしたグラフ
     */
	public static synchronized void open(GraphActor grpact, RootLayout rootLayout) {
        GraphPain fp = new GraphManager.GraphPain(grpact, rootLayout);

        // オープン中のグラフ
        map.put(appController.plrAct.getUriStr(grpact.getGraphNode()), fp);
    }

    public static synchronized void close(GraphActor grpact) {
        // オープン中のグラフリストから削除
        map.remove(appController.plrAct.getUriStr(grpact.getGraphNode()));
        if (map.size() == 0) {
            //  オープン中のグラフがない
            nodepalette.clear();
        }
    }

    public static synchronized boolean isOpend(GraphActor grpact) {
        if (map.containsKey(appController.plrAct.getUriStr(grpact.getGraphNode()))) {
            TxtList.set("FolderNode " + grpact.getGraphName() +" is opened.");

            return true;
        }

        return false;
    }

    public static synchronized void toFront(GraphActor grpact) {
        if (map.containsKey(appController.plrAct.getUriStr(grpact.getGraphNode()))) {
            GraphPain gp = map.get(appController.plrAct.getUriStr(grpact.getGraphNode()));
            gp.rootlayout.toFront();
        }
    }


    /**
     * オープン中のグラフのリストを取得する
     * @return GraphActorのリスト
     */
    public static synchronized List<GraphActor> getOpenedGraphList() {
        List<GraphActor> list = new ArrayList<>();

        for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            GraphPain fp = map.get(key);
            list.add(fp.graph);
        }

        return list;
    }

    /**
     * オープン中のNodePainControllerを取得する
     * @param grpact グラフ
     * @return NodePainContoroller
     */
    public static synchronized  RootLayout getRootlayout(GraphActor grpact) {
        String key = appController.plrAct.getUriStr(grpact.getGraphNode());

        if (map.containsKey(key)) {
        }

        return null;
    }

    /**
     * 指定したノードをターゲット登録する
     * @param node　EntityNode
     */
    public static synchronized void setTarget(NodeCell node) { target = node; }

    /**
     * 登録ターゲットを取得する
     * @return EntiryNode
     */
    public static synchronized NodeCell getTarget() {
        return target;
    }

    /**
     * 指定したグラフをターゲット登録する
     * @param node　EntityNode
     */
    public static synchronized void setHyperTarget(EntityNode node, String name) {
        hyperTarget = node;
        hyperTargetName = name;
    }

    /**
     * 登録グラフターゲットを取得する
     * @return EntiryNode
     */
    public static synchronized EntityNode getHyperTarget() {
        return hyperTarget;
    }
    public static synchronized String getHyperTargetName() {
        return hyperTargetName;
    }


    public static synchronized void openGraphList() {
        appController.exec_openGraph(false);
    }

    public static synchronized void openSearchNode() {
        appController.exec_searchNode();
    }

    /**
     * シングルトン
     */
    private GraphManager() {}
}