package com.assemblogue.plr.app.generic.semgraph;

import java.util.ArrayList;
import java.util.List;

import com.assemblogue.plr.contentsdata.ontology.OntologyItem;

import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GraphController {

	private Stage ownerStage;
    private GraphActor graphAct;

    private PlrActor plrAct;
    private ComboBox<String> topClassSelector;


    // ノードペインの関係クラス追加ComboBox初期状態表示用
    private OntMenu defaultItm;
    private Stage nodelistStage;

    // 内部ノード実態とボタンの紐つけ
    private List<NodeCell> nodeCells = new ArrayList<>();
	private Stage stage;

    GraphController(Stage ownerStage,GraphActor graph_act) {
this.stage = ownerStage;
this.graphAct = graph_act;
        this.plrAct = AppController.plrAct;
       // this.stage = new Stage();

        ownerStage.showingProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                this.exec_cansel();
            }
        }));




        // ノード編集ペインの関係クラス追加ComboBoxの初期値設定用 表示用ラベルさえあればよい
        defaultItm = new OntMenu(graphAct, null);
        defaultItm.menu.label = AppProperty.ADD_NODE_BTN;

    }

    private void exec_cansel() {
        //fndAct.sync(); 変更時にsyncしているので、このタイミングで実施する必要なし
        stage.close();

        if (nodelistStage != null && nodelistStage.isShowing()) {
            nodelistStage.close();
        }

    /*    if (rdfGraphStage != null && rdfGraphStage.isShowing()) {
            rdfGraphStage.close();
        }
*/
        GraphManager.close(graphAct);
    }

}

