package com.assemblogue.plr.app.generic.semgraph;


import java.net.URL;
import java.util.*;

import com.assemblogue.plr.lib.EntityNode;
import com.assemblogue.plr.lib.model.Friend;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;


/**
 * 既存フォルダノードオープンウィンドウのコントローラクラスです。
 *
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class OpenGraphController implements Initializable {
    @FXML private Button graphApply;
    @FXML private Button graphCancel;
    @FXML private Button graphRefButton;
    @FXML private TextField graph_uri;

    private AppController appcontroller;
    private Stage ownerStage;
    private Stage stage;
    private Stage tvStage;

    private Map<String, PlrActor.NodeName> nodeNameMap;

    private TreeItem<String> rootNode;


    @Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeUI();
    }

	private void initializeUI() {
        graphApply.setOnAction(mouseEvent -> exec_open());
        graphRefButton.setOnAction(mouseEvent -> exec_select(true));
        graphCancel.setOnAction(mouseEvent -> exec_cancel());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void setOwnerStage(Stage stage) { ownerStage = stage; }
    public void setAppController(AppController ctrl) { appcontroller = ctrl; }

    private void exec_cancel() {
        close();
    }

    /**
     * グラフ一覧の表示：直接表示用
     * あらかじめ、setAppControllerを行っておくこと
     */
    public void openSelector(boolean sync_flag) {
        exec_select(sync_flag);
    }

    /**
     * ルートノードのノードリストを表示する
     * 選択したノード名をテキストフィールドにセットする。
     * 事前にplrActor.sync, plrActor.listを実施しておくこと。
     *
     * 課題：フレンドのノードリストも表示するようにする。
     *　　　　フレンド名の下にノード名が来るようにしたい。
     */
    private void exec_select(boolean syncflag) {
        tvStage = new Stage();
        nodeNameMap = new HashMap<>();

        rootNode = new TreeItem<>(Messages.getString("graph.open.graph.list"));
        rootNode.setExpanded(true);

        // アプリルートを検索
        EntityNode root = appcontroller.plrAct.getAplRootFolderNode();
        if (root == null) {
            return;
        }
        if (syncflag == true) {
            TxtList.debug("OpenGraphControlle/exec_select:77/sync root");
            appcontroller.plrAct.sync(root);
        }
        appcontroller.plrAct.list(null, root);

        TreeItem<String> my_item = new TreeItem<>(appcontroller.plrAct.getUserID());
        my_item.setExpanded(true);

        // ルート直下のグラフを取得し、アイテムに登録
        for(PlrActor.NodeName pnn: appcontroller.plrAct.getRootNodeList()) {
            String name = pnn.name + " -> " + pnn.uri;
            nodeNameMap.put(name, pnn);
            TreeItem<String> item = new TreeItem<>(name);
            my_item.getChildren().add(item);
        }

        rootNode.getChildren().add(my_item);

        // フレンドノードリストアップは非同期で
        ListFriend lf = new ListFriend();
        lf.start();

        TreeView<String> tree = new TreeView<>(rootNode);
        tree.setPrefWidth(640);  // 幅は表示文字列長に合わせたいが。。。

        // アイテムが選択された時の処理
        tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                TreeItem selectitem = (TreeItem)newValue;
                String val = selectitem.getValue().toString();
                if (val.contains(" -> plr:")) {
                    graph_uri.setText(val);
                    tvStage.close();
                    exec_open();
                }
            }
        });

        tvStage.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == true && newValue == false) {
                appcontroller.close_openGraph();
            }
        });

        tvStage.setScene(new Scene(tree));
        tvStage.show();
    }

    /**
     * 前面へ
     */
    public void toFront() {
        if (tvStage != null) {
            tvStage.toFront();
        }
    }

    public void close() {
        if (tvStage != null) {
            tvStage.close();
        }
    }

    private void exec_open() {
        if (nodeNameMap.containsKey(graph_uri.getText())) {
            PlrActor.NodeName nn = nodeNameMap.get(graph_uri.getText());
            appcontroller.openGraph(nn.node.asEntity());
        } else {
            TxtList.set("Node is not exist.");
        }

        close(); // 上位ウィンドウクローズ
    }

    /**
     * フレンドのグラフをリストアップ
     * rootNodeに追加すると表示される
     */
    class ListFriend extends Thread {
        public void run() {
            listFriendGraph();
        }
    }

    private void listFriendGraph() {
        String title = Messages.getString("graph.open.graph.list");
        String s_title = title + Messages.getString("graph.open.graph.search.friend");
        rootNode.setValue(s_title);

        // フレンドもあれば
        List<Friend> friends = appcontroller.plrAct.getFriends();

        if (friends != null) {
            rootNode.setValue(s_title + Messages.getString("graph.open.graph.search.friend.n") + friends.size());

            for (Friend friend : friends) {
                TreeItem<String>  friend_item = new TreeItem<>(friend.getUserId());
                friend_item.setExpanded(true);

                // ルート直下のノード名リストを取得し、アイテムに登録
                List<PlrActor.NodeName> pnns = appcontroller.plrAct.getFriendRootNodeList(friend);
                if (pnns != null) {
                    for (PlrActor.NodeName pnn : pnns) {
                        String name = pnn.name + " -> " + pnn.uri;
                        nodeNameMap.put(name, pnn);
                        TreeItem<String> item = new TreeItem<>(name);
                        friend_item.getChildren().add(item);
                    }
                }

                rootNode.getChildren().add(friend_item);
            }
        }

        rootNode.setValue(title);
    }
}
