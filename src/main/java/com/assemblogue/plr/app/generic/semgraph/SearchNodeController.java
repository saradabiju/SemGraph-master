package com.assemblogue.plr.app.generic.semgraph;


import java.net.URL;
import java.util.*;

import com.assemblogue.plr.lib.EntityNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Stage;
import javafx.util.Callback;


/**
 * ノード検索ウィンドウのコントローラクラスです。
 * プロパティ名と値を指定し、グラフ内のノードを検索します。
 *
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class SearchNodeController implements Initializable {

    @FXML private TextField search_key;
    @FXML private TextField search_value;
    @FXML private Button snodeCancel;
    @FXML private Button snodeSearch;

    // 検索項目ベース
    private Stage stage;

    // 検索ノード一覧
    private Stage nodelistStage;
    private ObservableList<NodeCell> nodelistItems;

    // NodeCellとNodePainControllerの紐つけ
    private Map<NodeCell,RootLayout> mapNpc = new HashMap<>();

    @Override
	public void initialize(URL location, ResourceBundle resources) {
    	System.out.println("Resource Bundles");
		initializeUI();
    }

	private void initializeUI() {
        snodeCancel.setOnAction(mouseEvent -> exec_cancel());
        snodeSearch.setOnAction(mouseEvent -> exec_search() );
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.showingProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                exec_cancel();
            }
        }));
    }

    private void exec_cancel() {
        if (nodelistStage != null && nodelistStage.isShowing()) {
            nodelistStage.close();
        }

        this.stage.close();
    }

    /**
     * ノード内容の検索
     * @return
     */
 /*   private List<NodeCell> getNodeCells() {
        String key = search_key.getText();
        String value = search_value.getText();

        List<NodeCell> results = new ArrayList<>();

        Map<EntityNode, EntityNode> nodes = new LinkedHashMap<>(); // node重複チェックマップ

        // オープン中グラフのリストを取得
        System.out.println("Open graph lists");
        List<GraphActor> flndacts = GraphManager.getOpenedGraphList();
        // グラフ中のノードの属性を検索し、返値リストを作成
        for (GraphActor flndact : flndacts) {
           RootLayout rootlayout = GraphManager.getRootlayout(flndact);
            if (rootlayout == null) {
                continue;
            }

          //  List<NodeCell> list_ndc = rootlayout.getNodeCells();

          /  for (NodeCell ndc : list_ndc) {
             //   String v = rootlayout.getValue(ndc, key);
                if (v != null && v.contains(value)) {
                    if (!nodes.containsKey(ndc.node)) {
                        results.add(ndc);
                        mapNpc.put(ndc, rootlayout);
                        nodes.put(ndc.node, ndc.node);
                    }
                }
            }
        }

        return results;
    }
*/
    /**
     * ノード一覧表示名の更新
     */
    private void update_nodListItems() {
        //　今まで作成したノードを取得し、ListViewを作成する
    	System.out.println("update function");
        if (nodelistItems == null) {
            nodelistItems = FXCollections.observableArrayList();
        }

        nodelistItems.clear();
     //   nodelistItems.addAll(getNodeCells());
    }

    /**
     * 検索結果リストの表示
     */
    private void exec_search() {
        if (nodelistStage != null && nodelistStage.isShowing()) {
            nodelistStage.toFront();
        }

        update_nodListItems();
        ListView<NodeCell> nodelist = new ListView<>();
        nodelist.setItems(nodelistItems);

        // 検索結果なし
        if (nodelistItems.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(Messages.getString("aleart.header.search.ng"));
            alert.showAndWait();
            return;
        }

        if (nodelistStage == null) {
            nodelistStage = new Stage();
            nodelistStage.setTitle("Search result");
        }

        // 表示文字列を取得するクラスを返す。表示時にOverrideメソッドが呼ばれる。
        nodelist.setCellFactory(new Callback<ListView<NodeCell>, ListCell<NodeCell>>() {
            @Override
            public ListCell<NodeCell> call(ListView<NodeCell> param) {
                return new NodeCell.NodeCellStr();
            }
        });

        // 右クリック時の処理本体
        MenuItem item1 = new MenuItem(Messages.getString("editmenu.property"));
        item1.setOnAction((javafx.event.ActionEvent t) -> {
            NodeCell ndc = NodeCell.getSelectedItem(nodelist);
            if (mapNpc.containsKey(ndc)) {
                Stage stg = new Stage();
                stg.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (oldValue && !newValue) {
                        // 属性設定が閉じた時の処理
                        update_nodListItems();
                    }
                });
                // 編集対象ノードのNodePainControllerを使う
                RootLayout rootlayout = mapNpc.get(ndc);
                if (rootlayout != null) {
                  //  rootlayout.property(stg, ndc.node, ndc.ontMenu);
                }
            }
        });

        MenuItem item2 = new MenuItem(Messages.getString("editmenu.target"));
        item2.setOnAction((javafx.event.ActionEvent t) -> {
            NodeCell ndc = NodeCell.getSelectedItem(nodelist);
            if (mapNpc.containsKey(ndc)) {
                RootLayout rootlayout = mapNpc.get(ndc);
               // rootlayout.target(ndc);
            }
        });

        // 右クリック時の処理登録
        ContextMenu popup = new ContextMenu();
        popup.getItems().addAll(item1,item2);
        nodelist.setContextMenu(popup);
        nodelist.setOnContextMenuRequested((ContextMenuEvent event) -> {
            popup.show(nodelist, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        nodelistStage.setScene(new Scene(nodelist));
        nodelistStage.show();
    }


    void toFront() {
        stage.toFront();
    }

}