package com.assemblogue.plr.app.generic.semgraph;


import java.util.*;
import java.util.List;

import com.assemblogue.plr.contentsdata.ontology.OntologyItem;
import com.assemblogue.plr.lib.EntityNode;
import com.assemblogue.plr.lib.Node;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.util.Callback;
import javafx.util.StringConverter;


/**
 * ノード編集ウィンドウ
 *
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class NodePainController {
    private Boolean debugmenu = false;

    private Stage ownerStage;
    private GraphActor graphAct;

    private PlrActor plrAct;

    private Stage stage;
    private BorderPane bpRoot;
    private ScrollPane spRoot;
    private ComboBox<String> topClassSelector;
    private Label topClassBtn;

    // toolbar
    // ノードリストボタンとRDFグラフボタンを配置する
    //private String[] btnText = {"node.btn.refresh", "graph.open.graph.list", "nodepain.btn.nodelist", "nodepain.btn.rdfGraph", "nodepain.btn.tgtFolder", "menuItem.node.search", "nodepain.btn.information" };
    private String[] btnText = {"node.btn.refresh", "graph.open.graph.list", "nodepain.btn.nodelist", "nodepain.btn.rdfGraph", "nodepain.btn.tgtFolder", "menuItem.node.search" };
    private ToolBar toolBar;

    // ノードペインの関係クラス追加ComboBox初期状態表示用
    private OntMenu defaultItm;

    // ノード一覧
    private Stage nodelistStage;
    private ObservableList<NodeCell> nodelistItems;

    // Rdfグラフ
    private Stage rdfGraphStage;
    private RdfManager rdfMan;
    private Group rdfGraphGroup;

    // Node pain
    private HBox baseHbox;
    private VBox baseVbox;

    // 内部ノード実態とボタンの紐つけ
    private List<NodeCell> nodeCells = new ArrayList<>();

    /**
     * 指定したNodeCellをNodeCellリストから取得する
     *
     * @param node_id
     * @return
     */
    private NodeCell search(String node_id) {
        for (NodeCell nc : nodeCells) {
            if (node_id.equals(plrAct.getId(nc.node))) {

            }
        }

        return null;
    }

    /**
     * 指定したNodeCellをNodeCellリストから取得する
     *
     * @param node
     * @return
     */
    private NodeCell search(EntityNode node) {
        for (NodeCell nc : nodeCells) {
            if (node == nc.node) {
                return nc;
            }
        }

        return null;
    }

    /**
     * ノードペインを作成する
     * 　引数で渡されたグラフ内の編集用
     *
     * @param owner
     * @param graph_act グラフ
     */
    NodePainController(Stage owner, GraphActor graph_act) {
        this.ownerStage = owner;
        this.graphAct = graph_act;
        this.plrAct = AppController.plrAct;
        this.stage = new Stage();

        ownerStage.showingProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                this.exec_cansel();
            }
        }));

        // make toolbar
        Button button[] = new Button[btnText.length];
        for (int i = 0; i < btnText.length; i++) {
            button[i] = new Button(Messages.getString(btnText[i]));
            button[i].setPrefHeight(20);
            button[i].setFocusTraversable(false);
        }
        button[0].setOnAction(event -> this.exec_updateNodeTree());  // 更新
        button[1].setOnAction(event -> this.exec_openGraphList());  // グラフ一覧を開く
        button[2].setOnAction(event -> this.exec_nodeList());  // ノード一覧
        button[3].setOnAction(event -> this.exec_rdfGraph());  // RDFグラフ
        button[4].setOnAction(event -> this.exec_tgtGraph());  // グラフをターゲット登録する
        button[5].setOnAction(event -> this.exec_openSearchNode());  // 検索
        //button[6].setOnAction(event -> this.exec_information());  // グラフ情報を表示する

        // setup parts
        this.toolBar = new ToolBar(button);



        this.bpRoot = new BorderPane();
        bpRoot.setTop(this.toolBar);
        bpRoot.setCenter(this.spRoot);
        spRoot.setStyle(AppProperty.SCROLLPANE_BASE_STYLE);

        // トップクラスの選択メニューを生成する
        List<OntologyItem> list_itm = graphAct.getOss().topClasses();
        if (list_itm.size() > 1) {
            topClassSelector = createTopClassSelector();
        } else {
            topClassBtn = createTopClassBtn();
        }

        // ノード編集ペインの関係クラス追加ComboBoxの初期値設定用 表示用ラベルさえあればよい
        defaultItm = new OntMenu(graphAct, null);
        defaultItm.menu.label = AppProperty.ADD_NODE_BTN;
    }


    public void toFront() {
        if (stage != null) {
            stage.toFront();
        }
    }

    private void exec_openGraphList() {
        GraphManager.openGraphList();
    }

    private void exec_openSearchNode() {
        GraphManager.openSearchNode();
    }

    /**
     * トップレベルノード生成ボタン（セレクタ）の生成
     * 生成可能なクラスが複数ある場合、どのクラスを生成するのか選択可能とする
     * @return ComboBox
     */
    private ComboBox<String> createTopClassSelector() {
        // トップクラスの選択メニューを表示する
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().clear();
        comboBox.setValue(AppProperty.ADD_NODE_BTN);
      for (OntologyItem itm : graphAct.getOss().topClasses()) {
            comboBox.getItems().add(AppProperty.ADD_NODE_BTN_PLUS + graphAct.getLabel(itm));
        }

        comboBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observ, String oldval, String newval) -> {
            if (!newval.equals(AppProperty.ADD_NODE_BTN)) {
                // "+ label"から"label"にする
                String label = newval.substring(AppProperty.ADD_NODE_BTN_PLUS.length(), newval.length());

                for (OntologyItem itm : graphAct.getOss().topClasses()) {
                    if (label.equals(graphAct.getLabel(itm))) {
                        this.exec_addNode(itm.id);
                        return;
                    }
                }
            }
        });

        return comboBox;
    }

    /**
     * トップレベルノード生成ボタンの生成(実はラベル)
     * 生成可能なクラスが単数の場合
     * @return Label
     */
    private Label createTopClassBtn() {
        Label button = new Label(AppProperty.ADD_NODE_BTN);
        button.setPadding(Insets.EMPTY);
        button.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()*1.5d));
        button.setStyle(AppProperty.ADD_TOP_NODE_BTN_STYLE);
        button.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    List<OntologyItem> itm = graphAct.getOss().topClasses();
                    if (itm.size() > 0) {
                        exec_addNode(itm.get(0).id);
                    }
                }
            });

        return button;
    }


    public Stage getStage() {
        return stage;
    }

    /**
     * 編集エリアを作成
     *
     * @param nd_name ノード名
     */
    public void createEditArea(String nd_name) {
        stage.setTitle(nd_name);
        stage.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                this.exec_cansel();
            }
        });

        // make node edit area
        update_nodeTree(true);

        stage.setScene(new Scene(bpRoot, Utils.getWindowWidth(), Utils.getWindowHeight()));
    }

    /**
     * グラフ同期、およびツリー表示の更新
     */
    private void exec_updateNodeTree() {
        //TxtList.debug("NodePainController:exec_nodeList: sync");
        plrAct.sync(graphAct.getGraphNode());
        update_nodeTree(true);
    }

    /**
     * ツリー表示の更新
     */
    private void update_nodeTree(boolean sync_flag) {
        nodeCells.clear();

        HBox base_hbox = new HBox();
        base_hbox.setAlignment(Pos.CENTER);
        base_hbox.setStyle(AppProperty.ADD_TOP_NODE_BTN_STYLE);

        // トップレベルノード生成ボタン
        if (topClassBtn == null) {
            base_hbox.getChildren().add(topClassSelector);
        } else {
            base_hbox.getChildren().add(topClassBtn);
        }

        VBox base_vbox = new VBox();
        base_vbox.setStyle(AppProperty.BOX_BASE_STYLE);

        base_hbox.getChildren().add(base_vbox);

        // すでに存在するノード　List<EntityNode>で回す
        Map<String,EntityNode> footprint = new LinkedHashMap<>();
        for (EntityNode node : graphAct.getEntryNodes()) {
            if (graphAct.isVisibleRoot(node)) {
                // ルートノードとして表示可能
                NodeCell ndc = createNodeBtn(footprint,null, node, sync_flag);
                ndc.visibleroot = true;
                base_vbox.getChildren().add(ndc.hbox);
            }
        }

        // set base_v_bx on ScrollPane
        HBox tmp = baseHbox;
        baseVbox = base_vbox;
        baseHbox = base_hbox;
        spRoot.setContent(baseHbox);

        if (tmp != null) {
            // 以前の登録分をクリア
            tmp.getChildren().clear();
        }
    }

    /**
     * ノードペインを閉じる
     * ノードリスト一覧を開いていたら、一緒に閉じる。
     * 複数同じノードリストを開いた際の整合性が取れなくなるため。
     */
    private void exec_cansel() {
        //fndAct.sync(); 変更時にsyncしているので、このタイミングで実施する必要なし
        stage.close();

        if (nodelistStage != null && nodelistStage.isShowing()) {
            nodelistStage.close();
        }

        if (rdfGraphStage != null && rdfGraphStage.isShowing()) {
            rdfGraphStage.close();
        }

        GraphManager.close(graphAct);
    }

    /**
     * ノードボタンを作成
     * ボタンに右クリックメニューをセットしておく
     *
     * @param node
     * @return
     */
    private NodeCell createNodeBtn(Map<String,EntityNode> footprint, NodeCell parent, EntityNode node, boolean sync_flag) {
        Label btn;
        NodeCell ndc;
        boolean reentry = false;

        if (footprint != null) {
            if (footprint.containsKey(node.getNodeId())) {
                reentry = true;
            } else {
                footprint.put(node.getNodeId(), node);
            }
        }

        Map<String,Node> node_properties = plrAct.listToMap(node);

        EntityNode node_hypernode = null;
        if (node_properties.containsKey(AppProperty.LT_HYPERNODE)) {
            node_hypernode = node_properties.get(AppProperty.LT_HYPERNODE).asEntity();
        }

        btn = new Label(plrAct.getId(node));
        btn.setWrapText(true); // 折り返しあり
        btn.setPrefHeight(20);
        btn.setPrefWidth(200);
        btn.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

        ndc = new NodeCell(parent, btn, node);

        String item_id = plrAct.getName(node);
        if (node_properties.containsKey(AppProperty.LT_NAME)) {
            item_id = node_properties.get(AppProperty.LT_NAME).asLiteral().getValue().toString();
        }

        ndc.hypernode = node_hypernode;
        ndc.ontMenu = new OntMenu(graphAct, graphAct.getOss().getNode(item_id));
        ndc.setDispStr(getDisplayContents(node, node_properties)); // hypernode設定後に呼ぶこと

        nodeCells.add(ndc);
        btn.setPrefHeight(ndc.vbox.getPrefHeight());

        btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    // 左ボタンダブルクリックでハイパーノードを開く
                    boolean doubleClicked = event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2;
                    if (doubleClicked) {
                        open_hypernode(ndc, false);
                    }
                } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                    // 右ボタンクリックで編集メニューを開く
                    create_editMenu(ndc).show(btn, event.getScreenX(), event.getScreenY());
                }
            }
        });

        // 依存ノードが作成可能なクラスには、依存ノード追加ボタンをつける
        // 直下のプロパティのうち、rangeがクラスのプロパティ名
        List<OntMenu.OntMenuItem> ranged_omi = ndc.ontMenu.getRangedClassItem();
        // クラスをrangeとするプロパティがある
        if (ranged_omi.size() > 0) {
            HBox hbox_relBtn = new HBox();
            hbox_relBtn.setStyle(AppProperty.BOX_STYLE_REL_BTN);
            hbox_relBtn.setAlignment(Pos.CENTER);

            // 関係性追加ボタン作成
            if (!reentry) {
                if (ranged_omi.size() > 1) {
                    ndc.rangeSelector = createRangeSelector(ranged_omi, ndc);
                    hbox_relBtn.getChildren().add(ndc.rangeSelector);
                } else {
                    ndc.rangeSelectorBtn = createRangeSelectorBtn(ranged_omi, ndc);
                    hbox_relBtn.getChildren().add(ndc.rangeSelectorBtn);
                }
            } else {
                ndc.rangeSelectorBtn = createRangeSelectorHiddenBtn();
                hbox_relBtn.getChildren().add(ndc.rangeSelectorBtn);
            }

            ndc.hbox.getChildren().add(hbox_relBtn);
            ndc.hbox.getChildren().add(ndc.vbox);

            if (!reentry) {
                // 依存ノード作成
                List<NodeInfo<Node>> list = graphAct.list(node);  // ノード属性を取得し、
                for (OntMenu.OntMenuItem omi : ranged_omi) {    // メニュー項目とクラスrange付き属性と照らし合わせる
                    for (NodeInfo<Node> ni : list) {
                        if (ni.name.equals(omi.id)) {
                            Map<String, Node> properties = plrAct.listToMap(ni.getNode().asEntity());

                            // クラスrangeでボタンを作成、他属性はそれ付随する
                            if (properties.containsKey(AppProperty.RANGE)) {
                                // リンクノード
                                EntityNode nd = properties.get(AppProperty.RANGE).asEntity();
                                if (nd != null) {
                                    // 依存ノード生成：別のグラフかもしれないので同期が必要
                                    if (!nd.isInner()) {
                                        if (sync_flag) {
                                            TxtList.debug("CreateBtn:sync");
                                            plrAct.sync(nd);
                                        }
                                    }

                                    NodeCell child = createNodeBtn(footprint, ndc, nd, sync_flag);
                                    child.propNode = ni.getNode().asEntity();  // 親が持つ、自分を指すプロパティノード
                                    ndc.vbox.getChildren().add(child.hbox);

                                    for (String key : properties.keySet()) {
                                        if (key.equals(AppProperty.RANGE)) {
                                            continue;
                                        }
                                        Node literal = properties.get(key);
                                        if (!literal.isLiteral()) {
                                            continue;
                                        }
                                        // サブプロパティメニューの生成
                                        String value = OntMenu.convertNew(literal.asLiteral().getValue().toString());

                                        // 右側ノードは複数あるので、NodeCellの1OntMenuでは賄いきれない
                                        OntMenu ontMenu = new OntMenu(graphAct, graphAct.getOss().getNode(item_id));
                                        List<OntMenu.OntMenuItem> ranged_omi_sub = ontMenu.getRangedClassItem();
                                        for (OntMenu.OntMenuItem omi_sub : ranged_omi_sub) {
                                            if (!omi_sub.id.equals(omi.id)) {
                                                continue;
                                            }
                                            HBox relmenu = makeSubProppertyMenu(ndc, child, nd, ontMenu, omi_sub, value);
                                            if (relmenu.getChildren().size() > 0) {
                                                child.hbox.getChildren().add(0, relmenu);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return ndc;
    }

    /**
     * 編集メニューの作成
     * @param ndc
     * @return
     */
    private ContextMenu create_editMenu(NodeCell ndc) {
        ContextMenu popup = new ContextMenu();

        // Debug Info
        if (debugmenu) {
            MenuItem pmnuInfo = new MenuItem(Messages.getString("editmenu.information"));
            pmnuInfo.setOnAction(event -> this.show_indormation(ndc.node, ndc.getDispStr()));
            popup.getItems().addAll(pmnuInfo, new SeparatorMenuItem());
        }

        // PROPERTY
        MenuItem pmnuProperty = new MenuItem(Messages.getString("editmenu.property"));
        pmnuProperty.setOnAction(event -> this.property(new Stage(), ndc.node, ndc.ontMenu));
        popup.getItems().addAll(pmnuProperty, new SeparatorMenuItem());

        // HYPRE NODE
        MenuItem pmnuOpnHypNd = new MenuItem(Messages.getString("editmenu.open.hypernode"));
        if (ndc.hypernode == null) {
            pmnuOpnHypNd.setStyle(AppProperty.EMENU_NOT_ACTIVE);
        } else {
            pmnuOpnHypNd.setOnAction(event -> this.open_hypernode(ndc, true));
        }
        MenuItem pmnuHypNd = new MenuItem(Messages.getString("editmenu.hypernode"));
        if (GraphManager.getHyperTarget() == null) {
            pmnuHypNd.setStyle(AppProperty.EMENU_NOT_ACTIVE);
        } else {
            pmnuHypNd.setOnAction(event -> this.hypernode(ndc));
        }
        MenuItem pmnuDelHypNd = new MenuItem(Messages.getString("editmenu.del.hypernode"));
        if (ndc.hypernode == null) {
            pmnuDelHypNd.setStyle(AppProperty.EMENU_NOT_ACTIVE);
        } else {
            pmnuDelHypNd.setOnAction(event -> this.deleteHypernode(ndc));
        }
        pmnuDelHypNd.setOnAction(event -> this.deleteHypernode(ndc));
        popup.getItems().addAll(pmnuOpnHypNd, pmnuHypNd, pmnuDelHypNd, new SeparatorMenuItem());

        // TARGET
        MenuItem pmnuTarget = new MenuItem(Messages.getString("editmenu.target"));
        pmnuTarget.setOnAction(event -> this.target(ndc));

        MenuItem pmnuReference = new MenuItem(Messages.getString("editmenu.reference"));
        if (GraphManager.getTarget() == null) {
            pmnuReference.setStyle(AppProperty.EMENU_NOT_ACTIVE);
        } else {
            pmnuReference.setOnAction(event -> this.reference(ndc));
        }
        popup.getItems().addAll(pmnuTarget, pmnuReference, new SeparatorMenuItem());

        // REMOVE
        MenuItem pmnuRemove = new MenuItem(Messages.getString("editmenu.remove"));
        pmnuRemove.setOnAction(event -> this.remove(ndc));
        popup.getItems().add(pmnuRemove);

        return popup;
    }

    /**
     * 右側ノード追加ボタン生成
     * 右側ノードのクラスを選択するComboBox及び、クラス決定後の右側ノード生成
     * 生成可能なクラスが複数でてくることを考慮してComboBoxでクラスを選択できるようにした。
     * @param ranged_omi 選択項目リスト
     * @param ndc        親ボタン
     * @return ComboBoxオブジェクト
     */
    private ComboBox<OntMenu.OntMenuItem> createRangeSelector(List<OntMenu.OntMenuItem> ranged_omi, NodeCell ndc) {
        // OntMenuItemのComboBoxを作成し、表示文字列取得メソッドをOverride
        ComboBox<OntMenu.OntMenuItem> combo_box = new ComboBox<>();

        combo_box.setConverter(new StringConverter<OntMenu.OntMenuItem>() {
            @Override
            // setValue時に呼ばれる
            public String toString(OntMenu.OntMenuItem omi) {
                if (omi == null) {
                    return AppProperty.ADD_NODE_BTN;
                }
                return omi.label;
            }
            @Override
            public OntMenu.OntMenuItem fromString(String string) {
                return null;
            }
        });

        // Combobox の表示更新メソットをOverride
        Callback<ListView<OntMenu.OntMenuItem>, ListCell<OntMenu.OntMenuItem>> cellFactory = (ListView<OntMenu.OntMenuItem> param) -> new ListCell<OntMenu.OntMenuItem>() {
            @Override
            protected void updateItem(OntMenu.OntMenuItem omi, boolean empty) {
                super.updateItem(omi, empty);
                if (omi != null && !empty) {
                    setText(omi.label);
                } else {
                    setText(AppProperty.ADD_NODE_BTN);
                }
            }
        };
        combo_box.setCellFactory(cellFactory);

        // 選択肢の登録
        combo_box.getItems().clear();
        combo_box.setValue(defaultItm.menu); // 初期値

        if (ranged_omi.size() > 1) {
            // 複数選択可能な場合、生成するクラスを選べるようにする
            for (OntMenu.OntMenuItem omi : ranged_omi) {
                combo_box.getItems().add(omi);
            }

            // クリック時の処理
            combo_box.valueProperty().addListener((ObservableValue<? extends OntMenu.OntMenuItem> observable, OntMenu.OntMenuItem oldValue, OntMenu.OntMenuItem newValue) -> {
                if (Objects.nonNull(newValue)) {
                    // 右側ノードを追加
                    this.exec_addRelNode(newValue, ndc);
                }
            });
        }

        return combo_box;
    }

    /**
     * 右側ノード追加ボタン生成
     * 右側ノードのクラスが一つしかない場合、ボタンの生成および、クラス決定後の右側ノード生成
     *
     * @param ranged_omi 選択項目リスト
     * @param ndc        親ボタン
     * @return Buttonオブジェクト
     */
    private Label createRangeSelectorBtn(List<OntMenu.OntMenuItem> ranged_omi, NodeCell ndc) {
        Label button = new Label(AppProperty.ADD_NODE_BTN);
        button.setPadding(Insets.EMPTY);
        button.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()*1.5d));
        button.setStyle(AppProperty.ADD_RIGHT_NODE_BTN_STYLE);
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // 右側ノードを追加
                exec_addRelNode(ranged_omi.get(0), ndc);
            }
        });

        button.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

        return button;
    }

    private Label createRangeSelectorHiddenBtn() {
        Label button = new Label(AppProperty.ADD_NODE_BTN);
        button.setPadding(Insets.EMPTY);
        button.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()*1.5d));
        button.setStyle(AppProperty.ADD_RIGHT_NODE_HIDDEN_BTN_STYLE);
        button.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

        return button;
    }

    /**
     * ハイパーノードを開く
     * * @param ndc
     */
    public void open_hypernode(NodeCell ndc, boolean alert_flag) {
        if (ndc.hypernode != null) {
            if (!ndc.hypernode.equals(graphAct.getGraphNode())) {
                GraphManager.getAppController().openGraph(ndc.hypernode);
            }
        } else {
            if (alert_flag) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(Messages.getString("alert.graph.not.hypernode"));
                alert.showAndWait();
            }
        }

    }

    /**
     * 右側ノードの追加
     * グラフにrangeで示されるクラスを作成
     * 親ノードにrangeを定義域とするプロパティを表現する内部クラスを作成
     * 内部クラスの属性に、rangeクラスのリンク属性と他属性値リテラルを追加
     * @param omi OntMenuItem 選択オブジェクト
     * @param ndc NodeCell　親ノード情報
     */
    private void exec_addRelNode(OntMenu.OntMenuItem omi, NodeCell ndc) {
        // ノード／リテラルの追加
        List<EntityNode> new_entitynode = graphAct.addRelationClass(ndc.node, omi.ontologyItem, omi.range);
        plrAct.asyncNode(ndc.node);
        plrAct.asyncNode(new_entitynode.get(0));
        // ノードボタンを追加し、ツリー、関連stageの表示を更新する
        createNodeBtn(null, ndc, new_entitynode.get(0), false);

        // 追加ノードの同期（他グラフのノードを更新したかもしれないので同期をとる）
        TxtList.debug("NodePainController:exec_addRelNode: sync");
        plrAct.asyncNode(new_entitynode.get(1));

        final Thread thread = new Thread(() -> {
            // ノードボタンを追加し、ツリー、関連stageの表示を更新する
            try {
                javafx.application.Platform.runLater(() -> {
                    update_nodeTree(false);
                    update_stages();
                });
            } catch (Exception e) {
            }
        });
        plrAct.setService(thread);

/*
        // ノードボタンを追加し、ツリー、関連stageの表示を更新する
        createNodeBtn(ndc, new_entitynode.get(0));
        update_nodeTree();
        update_stages();
*/
    }

    /**
     * サブプロパティ選択メニューの生成
     * DiscardMenuで言うと、"関係"属性の値選択用
     *
     * @param ndc      親ノードボタン
     * @param new_node 追加ノード
     * @param om       メニューアイテム
     * @param omi      クラスメニューアイテム
     * @param val      初期値
     * @return 生成した選択メニュー
     */
    private HBox makeSubProppertyMenu(NodeCell ndc, NodeCell child, EntityNode new_node, OntMenu om, OntMenu.OntMenuItem omi, String val) {
        String omi_id = omi.id;
        HBox relmenu = new HBox(10d);
        relmenu.setAlignment(Pos.CENTER_LEFT); // 縦中央、横左寄せ
        relmenu.setStyle(AppProperty.REL_LABEL_STYLE);

        // 指定属性(omi)のサブメニュー構造を取得
        List<OntMenu.OntMenuItem> sub_omi_list = om.makeMenuList2(omi_id);
        for (OntMenu.OntMenuItem sub_omi : sub_omi_list) {
            // 設定済みの属性値(OntologyIte.label)をラベル名とする
            // fxLabelには、初期状態ではオントロジ属性名が入る
            if (val != null && !val.equals("")) {
                child.dispRelStr = val;
                sub_omi.fxLabel.setText(val);
                sub_omi.fxLabel.setTextFill(Color.BLACK);
            } else {
                sub_omi.fxLabel.setTextFill(Color.GRAY);
            }

            relmenu.getChildren().add(sub_omi.fxLabel);

            // 属性値が選択されたら、ラベル値を書き換えるので、ラベル値のリスナで選択された属性値を取得する
            sub_omi.fxLabel.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null) {
                        // 親ノード(ndc.node)の属性ノード(名前：omi.id)のうち、rangeが自分(uri)を向いている属性の値を変更する
                        String uri = plrAct.getUriStr(new_node); // 向き先
                        graphAct.setRelationLiteral(ndc.node, omi_id, child.propNode, newValue);
                        child.dispRelStr = newValue;

                        update_nodeTree(false);
                        if (rdfGraphStage != null && rdfGraphStage.isShowing()) {
                            // rdfグラフが開いていたら更新する
                            update_rdfGraph();
                        }
                    }
                }
            });
        }

        return relmenu;
    }


    /**
     * 指定ノードの属性を編集する
     * @param stg : メニュー表示用ステージ
     * @param node 編集対象のノード
     * @param ontmenu 編集対象のメニュー構造
     */
    public void property(Stage stg, EntityNode node, OntMenu ontmenu) {
        // 他人の編集結果を取り込む
        //TxtList.debug("NodepainControler:property sync");
        //plrAct.sync(node);

        stg.setTitle(plrAct.getId(node) + " : " + plrAct.getName(node));

        // 登録、キャンセルボタン
        Button regist = new Button(Messages.getString("nodepropertymenu.regist"));
        regist.setOnAction(event -> set_property(stg, node, ontmenu));
        Button cansel = new Button(Messages.getString("nodepropertymenu.cancel"));
        cansel.setOnAction(event -> cancel_property(stg));
        HBox btn_area = new HBox();
        btn_area.setAlignment(Pos.CENTER_RIGHT);
        btn_area.setMargin(regist, new Insets(2, 10, 10, 10));
        btn_area.setMargin(cansel, new Insets(2, 10, 10, 10));
        btn_area.getChildren().addAll(regist, cansel);

        // 属性メニューVBox作成し、下部に登録、キャンセルをつける
        VBox menu_vbox = ontmenu.makeMenu(graphAct.getLiterals(node)); // 設定済の属性値を取得してメニューを作成
        menu_vbox.getChildren().add(btn_area);

        BorderPane bp = new BorderPane();
        bp.setCenter(menu_vbox);
        stg.setScene(new Scene(bp));
        stg.show();
    }

    /**
     * ノードへの属性設定
     *
     * @param node
     * @param ontmenu
     */
    private void set_property(Stage stg, EntityNode node, OntMenu ontmenu) {
        // 現在の属性値を取得する
        HashMap<OntMenu.OntMenuItem, OntMenu.OntMenuItem> res = ontmenu.getResult();

        Iterator entries = res.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();

            OntMenu.OntMenuItem key = (OntMenu.OntMenuItem) entry.getKey();
            OntMenu.OntMenuItem val = (OntMenu.OntMenuItem) entry.getValue();

            setPropertyToEntityNode(node, key, val);
        }

        TxtList.debug("NodePainControlle/set_property:595/sync node");
        plrAct.asyncNode(node); // sync

        // NodeCell更新
        update_nodeCellContents(node);

        // ノードツリーの再描画
        update_nodeTree(false);

        /*
        // 関連ステージの更新
        update_nodListItems();
        if (rdfGraphStage != null && rdfGraphStage.isShowing()) {
            update_stages();
        }
        */

        final Thread thread = new Thread(() -> {
            // ノードボタンを追加し、ツリー、関連stageの表示を更新する
            try {
                javafx.application.Platform.runLater(() -> {
                    // 関連ステージの更新
                    update_nodListItems();
                    if (rdfGraphStage != null && rdfGraphStage.isShowing()) {
                        update_stages();
                    }
                });
            } catch (Exception e) {
            }
        });
        plrAct.setService(thread);

        stg.close();
    }

    /**
     * 指定ノードに属性値をセットする
     *
     * @param node 　属性値のセット対象ノード
     * @param key  　属性名オントロジアイテム
     * @param val  　属性値オントロジアイテム
     */
    private void setPropertyToEntityNode(EntityNode node, OntMenu.OntMenuItem key, OntMenu.OntMenuItem val) {
        if (val.value.size() <= 0) {
            return;
        }

        if (key.maxCardinality == 1) {
            // 単数設定のなものはリテラルを登録する
            plrAct.setLiteral(node, key.id, "*", val.value.get(0));
        } else if (key.maxCardinality > 1) {
            // 複数設定可能なものはノードを作り、リテラルを登録する
            plrAct.setLiteral(node, key.id, "*", val.value.get(0));
        }
    }

    /**
     * 属性メニューを閉じる
     *
     * @param stg
     * @return
     */
    private void cancel_property(Stage stg) {
        stg.close();
    }

    /**
     * ノードコピー
     * @param ndc コピー元ノード
     */
    void target(NodeCell ndc) {
        ndc.setGraphNode(graphAct.getGraphNode());   //
        GraphManager.setTarget(ndc);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(Messages.getString("alert.header.target"));
        alert.setContentText(ndc.getDispStr());
        alert.showAndWait();
    }

    /**
     * ノード貼り付け
     * @param ndc 貼り付け先ノード
     */
    private void reference(NodeCell ndc) {
        NodeCell tgt_ndc = GraphManager.getTarget();
        boolean update = false;

        String alert_header;
        String alert_content;

        if (tgt_ndc == null) {
            alert_header = Messages.getString("alert.header.ref.ng");
            alert_content = Messages.getString("alert.content.no.tgt");
        } else {
            String old_dispstr = ndc.getDispStr();
            List<OntMenu.OntMenuItem> ranged_omi = ndc.ontMenu.getRangedClassItem();

            // クラスをrangeとするプロパティがある
            for (OntMenu.OntMenuItem omi : ranged_omi) {
                // ノードの属性にEntityノードを設定、下位ノードのNodeCellを作成しなおす
                EntityNode nd = graphAct.addRelationClass(ndc.node, omi.ontologyItem, tgt_ndc.node);
                if (nd != null) {
                    TxtList.debug("NodePainControlle/reference:829/sync ndc.node");
                    plrAct.asyncNode(nd);
                }
            }

            update = true;

            alert_header = Messages.getString("alert.header.ref.ok");
            alert_content = Messages.getString("alert.content.paste.dst") + old_dispstr +
                    Messages.getString("alert.content.paste.src") + tgt_ndc.dispStr.getValue() +
                    Messages.getString("alert.content.paste.to");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(alert_header);
        alert.setContentText(alert_content);
        alert.showAndWait();

        if (update) {
            final Thread thread = new Thread(() -> {
                // ノードボタンを追加し、ツリー、関連stageの表示を更新する
                try {
                    javafx.application.Platform.runLater(() -> {
                        update_nodeTree(false);
                        update_stages();
                    });
                } catch (Exception e) {
                }
            });
            plrAct.setService(thread);
        }
    }

    /**
     * ハイパーノードの設定
     * 現在ターゲットになっているグラフをハイパーノードとして設定する
     *
     * @param ndc
     */
    private void hypernode(NodeCell ndc) {
        boolean update = false;
        EntityNode hypernode = GraphManager.getHyperTarget();
        String hypernode_name = GraphManager.getHyperTargetName();

        String alert_header;
        String alert_content;

        if (hypernode == null) {
            alert_header = Messages.getString("alert.graph.header.ref.ng");
            alert_content = Messages.getString("alert.graph.content.no.tgt");
        } else {
            // ハイパーノードへのリンクを属性として保存
            plrAct.setLinkedNode(ndc.node, AppProperty.LT_HYPERNODE, hypernode);
            // NodeCellにハイパーノードを保存
            ndc.hypernode = hypernode;

            TxtList.debug("NodePainControlle/hypernoce:738/sync ndc.node");
            plrAct.asyncNode(ndc.node);

            update = true;

            alert_header = Messages.getString("alert.graph.header.ref.ok");
            alert_content =
                    Messages.getString("alert.content.paste.dst") + ndc.getDispStr() +
                            Messages.getString("alert.content.paste.src") + hypernode_name +
                            Messages.getString("alert.content.paste.to");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(alert_header);
        alert.setContentText(alert_content);
        alert.showAndWait();

        if (update) {
            final Thread thread = new Thread(() -> {
                // ノードボタンを追加し、ツリー、関連stageの表示を更新する
                try {
                    javafx.application.Platform.runLater(() -> {
                        // ツリー、ノード一覧、RDFグラフの再描画
                        update_nodeTree(false);
                        update_stages();
                    });
                } catch (Exception e) {
                }
            });
            plrAct.setService(thread);
        }
    }

    /**
     * ハイパーノードの削除
     * 現在ターゲットになっているグラフからハイパーノードを削除する
     *
     * @param ndc
     */
    private void deleteHypernode(NodeCell ndc) {
        if (ndc.hypernode == null) {
            return;
        }
        // NodeCellのハイパーノード設定を削除
        ndc.hypernode = null;

        // ノード`からハイパーノードを削除
        plrAct.removeProperty(ndc.node, AppProperty.LT_HYPERNODE);

        TxtList.debug("NodePainControlle/delHypernode:791/sync ndc.node");
        // graphAct.sync(ndc.node);
		plrAct.asyncNode(ndc.node);

		/*
        // ツリー、ノード一覧、RDFグラフの再描画
        update_nodeTree();
        update_stages();
        */

        // 削除メッセージ
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(Messages.getString("alert.graph.header.ref.ok"));
        alert.setContentText(Messages.getString("alert.graph.content.del"));
        alert.showAndWait();

        final Thread thread = new Thread(() -> {
            // ノードボタンを追加し、ツリー、関連stageの表示を更新する
            try {
                javafx.application.Platform.runLater(() -> {
                    // ツリー、ノード一覧、RDFグラフの再描画
                    update_nodeTree(false);
                    update_stages();
                });
            } catch (Exception e) {
            }
        });
        plrAct.setService(thread);
    }

    /**
     * ノードの削除
     * 最左ノードは不可視にする
     * リンク先属性は属性を削除する
     * @param ndc 削除ボタン
     */
    private void remove(NodeCell ndc) {
        if (ndc.parent == null) {
            // 最左ノード
            graphAct.remove(ndc.node);
            nodeCells.remove(ndc);
        } else {
            // 親の属性から削除
            for (OntMenu.OntMenuItem omi : ndc.parent.ontMenu.getRangedClassItem()) {
                graphAct.removeRelationItem(ndc.parent.node, omi.ontologyItem, ndc.node, ndc.propNode);
            }

            ndc.parent.child.remove(ndc);
        }

        // ツリー更新
        update_nodeTree(false);

        // リスト表示は全ノードを表示するので、更新の必要なし
        if (rdfGraphStage != null && rdfGraphStage.isShowing()) {
            update_rdfGraph();
        }
    }

    /**
	 * ノード追加ボタン処理：最左ノードを追加する
     * オントロジのトップレベルクラスを生成する。
	 */
	private void exec_addNode(String item_id) {
        if (item_id.equals(AppProperty.ADD_NODE_BTN)) {
            // ノードタイプを選択していない場合
            return;
        }
        // グラフに実体ノードを追加
        EntityNode node = graphAct.createGraphInnerNode(item_id, true);
        // 追加ノードのセルを追加する
        NodeCell ndc = createNodeBtn(null,null, node, false);
        if (graphAct.isVisibleRoot(node)) {
            ndc.visibleroot = true;
        }
        // 一旦＋ボタンを削除して、新規ボタンを追加、そのあとに再度＋を追加する
        if (topClassBtn == null) {
            baseVbox.getChildren().add(ndc.hbox);
            topClassSelector = createTopClassSelector();
        } else {
            baseVbox.getChildren().add(ndc.hbox);
        }
        // 追加したノードにフォーカスする
        ndc.btn.requestFocus();
        // 関連ステージの更新
        update_stages();
    }

    /**
     * ノードボタン、ノード一覧の表示文字列を取得
     */
    private String getDisplayContents(EntityNode node, Map<String, Node> properties) {
        if (properties == null) {
            properties = plrAct.listToMap(node);
        }

        // 決め打ち：ここで各ノードボタンの表示テキストを決める
        String val = null;
        if (properties.containsKey(AppProperty.ITEM_ID_CNT)) {
            Node literal = properties.get(AppProperty.ITEM_ID_CNT);
            val = literal.asLiteral().getValue().toString();
        } else {
            val = Messages.getString("nodecountents.prompt");
        }

        return val;
    }

    /**
     * グラフに紐つくステージを更新する
     */
    private void update_stages() {
        if (nodelistStage != null && nodelistStage.isShowing()) {
            // ノード一覧を開いていたら更新する
            update_nodListItems();
        }

        if (rdfGraphStage != null && rdfGraphStage.isShowing()) {
            // rdfグラフが開いていたら更新する
            update_rdfGraph();
        }
    }

	/**
	 * ノード一覧の表示
	 */
	private void exec_nodeList() {
        if (nodelistStage == null) {
            nodelistStage = new Stage();
            nodelistStage.setTitle(Messages.getString("nodepain.btn.nodelist") + " : "+graphAct.getGraphName());
        }
        if (nodelistStage.isShowing()) {
            return;
        }

        //TxtList.debug("NodePainController:exec_nodeList: sync");
        //plrAct.sync(graphAct.getGraphNode());

        //　今まで作成したノードを取得し、ListViewを作成する
        update_nodListItems();

        ListView<NodeCell>  nodelist = new ListView<>();
        nodelist.setItems(nodelistItems);
        nodelist.setMaxSize(Utils.getWindowWidth(),Utils.getWindowHeight());

        // 表示文字列を取得するクラスを返す。表示時にOverrideメソッドが呼ばれる。
        nodelist.setCellFactory(new Callback<ListView<NodeCell>, ListCell<NodeCell>>() {
            @Override
            public ListCell<NodeCell> call(ListView<NodeCell> param) {
                return new NodeCell.NodeCellStr();
            }
        });

        // 右クリック時の処理
        MenuItem item1 = new MenuItem(Messages.getString("editmenu.property"));
        item1.setOnAction((javafx.event.ActionEvent t) -> {
            NodeCell ndc = NodeCell.getSelectedItem(nodelist);
            OntMenu ontmenu = new OntMenu(graphAct, graphAct.getOss().getNode(plrAct.getName(ndc.node)));
            property(new Stage(), ndc.node, ontmenu);
        });

        MenuItem item2 = new MenuItem(Messages.getString("editmenu.target"));
        item2.setOnAction((javafx.event.ActionEvent t) -> {
            NodeCell ndc = NodeCell.getSelectedItem(nodelist);
            target(ndc);
        });

        ContextMenu popup = new ContextMenu();
        if (debugmenu) {
            MenuItem item0 = new MenuItem(Messages.getString("editmenu.information"));
            item0.setOnAction((javafx.event.ActionEvent t) -> {
                NodeCell ndc = NodeCell.getSelectedItem(nodelist);
                show_indormation(ndc.node, ndc.getDispStr());
            });
            popup.getItems().addAll(item0, new SeparatorMenuItem(), item1,item2);
        } else {
            popup.getItems().addAll(item1, new SeparatorMenuItem(), item2);
        }
        nodelist.setContextMenu(popup);
        nodelist.setOnContextMenuRequested((ContextMenuEvent event) -> {
            popup.show(nodelist, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        // 画面構成：ツールバーに再描画ボタンを置き、そのしたにリストを配置する
        Button refresh_btn;
        refresh_btn = new Button(Messages.getString("node.btn.refresh"));
        refresh_btn.setPrefHeight(20);
        refresh_btn.setFocusTraversable(false);
        refresh_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                update_nodListItems();
        }});
        //
        ToolBar tool_bar = new ToolBar(refresh_btn);
        BorderPane border_pane = new BorderPane();
        border_pane.setTop(tool_bar);
        border_pane.setCenter(nodelist);
        nodelistStage.setScene(new Scene(border_pane, Utils.getWindowWidth()*0.3d, Utils.getWindowWidth()*0.6d));
        nodelistStage.show();
	}

    /**
     * ndcの表示文字列を更新する
     */
    private void update_nodeCellContents(EntityNode node) {
        String dispstr = getDisplayContents(node, null);

        // ノードボタンが存在すれば、それを使う
        NodeCell nc = search(plrAct.getId(node));
        if (nc != null && nc.btn != null) {
            nc.setDispStr(dispstr);
        }
    }

    /**
     * 現存ノードのNodeCellリストの取得
     * @return 現存ノードのリスト
     */
    public List<NodeCell> getNodeCells() {
        List<NodeCell> list = new ArrayList<>();

        for (EntityNode nd : graphAct.getEntryNodes()) {
            String dispstr = getDisplayContents(nd, null);

            // ノードボタンが存在すれば、それを使う
            NodeCell nc = search(plrAct.getId(nd));
            if (nc != null && nc.btn != null) {
                nc.setDispStr(dispstr);
            } else {
                nc = new NodeCell(nd, dispstr);
                String item_id = plrAct.getName(nd);
                nc.ontMenu = new OntMenu(graphAct, graphAct.getOss().getNode(item_id));
            }

            list.add(nc);
        }

        return list;
    }

    /**
     * ノード一覧表示名の更新
     */
    private void update_nodListItems() {
        //　今まで作成したノードを取得し、ListViewを作成する
        if (nodelistItems == null) {
            nodelistItems = FXCollections.observableArrayList();
        }

        nodelistItems.clear();
        nodelistItems.addAll(getNodeCells());
    }

    /**
     * RDFグラフ表示
     * NodeCellを辿ってグラフを作成する
     */
	private void exec_rdfGraph() {
        if (rdfGraphStage == null) {
            rdfGraphStage = new Stage();
            rdfGraphStage.setTitle(Messages.getString("nodepain.btn.rdfGraph") + " : "+graphAct.getGraphName());
        }
        if (rdfGraphGroup == null) {
            rdfGraphGroup = new Group();
        }
        if (rdfMan == null) {
            rdfMan = new RdfManager();
        }

        update_rdfGraph();

        // set base_v_bx on ScrollPane
        ScrollPane pane = new ScrollPane();
        pane.setPrefSize(Utils.getWindowWidth(), Utils.getWindowHeight());
        pane.setContent(rdfGraphGroup);

        // 画面構成：ツールバーに再描画ボタンを置き、そのしたにリストを配置する
        Button refresh_btn;
        refresh_btn = new Button(Messages.getString("node.btn.refresh"));
        refresh_btn.setPrefHeight(20);
        refresh_btn.setFocusTraversable(false);
        refresh_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                update_rdfGraph();
            }});
        //
        ToolBar tool_bar = new ToolBar(refresh_btn);
        BorderPane border_pane = new BorderPane();
        border_pane.setTop(tool_bar);
        border_pane.setCenter(pane);
        //
        rdfGraphStage.setScene(new Scene(border_pane));

        if (!rdfGraphStage.isShowing()) {
            rdfGraphStage.show();
        }
    }

    /**
     * RDF グラフ内容の更新
     */
    private void update_rdfGraph() {
        if (rdfGraphStage == null || rdfGraphGroup == null || rdfMan == null) {
            return;
        }

        rdfMan.clear();
        rdfGraphGroup.getChildren().clear();

        Map<String, RdfNode> rdfmap = new HashMap<>();
        Map<String, EntityNode> footprint = new LinkedHashMap<>();

        for (NodeCell ndc: nodeCells) {
            if (!ndc.visibleroot) {
                continue;
            }
            traceNodeCell(footprint,null, ndc, rdfmap);
        }

        rdfMan.Draw(rdfGraphGroup);
    }

    /**
     * NodeCellを辿ってグラフを作成する
     * @param parent 親ノード（ノード間の関係性を処理するため、親と子が必要）
     * @param ndc 処理対象のNodeCell
     */
    private void traceNodeCell(Map<String, EntityNode> footprint, RdfNode parent, NodeCell ndc, Map<String,RdfNode> rdfmap) {
        boolean reentry = false;

        if (parent == null && !ndc.visibleroot) {
	        // 最左ノードで不可視属性
            return;
        }

        if (footprint != null) {
            if (footprint.containsKey(ndc.node.getNodeId())) {
                reentry = true;
                // return;
            } else {
                footprint.put(ndc.node.getNodeId(), ndc.node);
            }
        }

        String ndc_node_uri = plrAct.getUriStr(ndc.node);

        RdfNode self;
        if (rdfmap.containsKey(ndc_node_uri)){
            self = rdfmap.get(ndc_node_uri);
        } else {
            self = rdfMan.createNode(ndc.getDispStr(), plrAct.getId(ndc.node), rdfGraphGroup);
            rdfmap.put(ndc_node_uri, self);
        }

        if (parent != null) {
            String rel_str = ndc.dispRelStr;
            if (rel_str == null) {
                rel_str = "";
            }

            // ノード間矢印
            RdfNode.EdgeDir edge_dir = RdfNode.EdgeDir.PARENT;
            if (rel_str.contains(AppProperty.PROP_DIR_LEFT)){
                edge_dir = RdfNode.EdgeDir.CHILD;
                rel_str = rel_str.substring(0, rel_str.indexOf(AppProperty.PROP_DIR_LEFT));
            } else if (rel_str.contains(AppProperty.PROP_DIR_RIGHT)) {
                rel_str = rel_str.substring(0, rel_str.indexOf(AppProperty.PROP_DIR_RIGHT));
            } else if (rel_str.contains(AppProperty.PROP_DIR_LEFT_OLD)){
                edge_dir = RdfNode.EdgeDir.CHILD;
                rel_str = rel_str.substring(0, rel_str.indexOf(AppProperty.PROP_DIR_LEFT_OLD));
            } else if (rel_str.contains(AppProperty.PROP_DIR_RIGHT_OLD)) {
                rel_str = rel_str.substring(0, rel_str.indexOf(AppProperty.PROP_DIR_RIGHT_OLD));
            }

            if (!parent.equals(self)) {
                // 自分に自分は登録できない
                rdfMan.NodeConnect(parent, self, edge_dir, rel_str);
            }
        }

        if (!reentry) {
            for (NodeCell child : ndc.child) {
                // さらに自分の子供へ … rdfmap, groupはそのまま渡す
                traceNodeCell(footprint, self, child, rdfmap);
            }
        }

        if (footprint != null && !reentry) {
            footprint.remove(ndc.node.getNodeId());
        }
    }

    /**
     * ノード情報の表示（デバッグ用）
     * @param node ノード
     * @param str 表示用文字列
     */
    private void show_indormation(EntityNode node, String str) {
        Stage stage = new Stage();
        stage.setTitle("Information: " + graphAct.getGraphName());

        VBox vbox = new VBox();

        String id = plrAct.getId(node);
        if (id == null) {
            id = "取得不可";
        }
        String uri = plrAct.getUriStr(node);

        vbox.getChildren().add(new Label("Graph Name :"+graphAct.getGraphName()));
        vbox.getChildren().add(new Label("Graph URI :"+graphAct.getGraphNode().getURI().toString()));
        vbox.getChildren().add(new Label("ID   :"+id));
        vbox.getChildren().add(new Label("URI  :"+uri));
        vbox.getChildren().add(new Label("表示  :"+str));

        stage.setScene(new Scene(vbox));
        stage.show();
    }

    /**
     * ノードndcのkey属性を返す
     * @param ndc
     * @param key
     * @return
     */
    public String getValue(NodeCell ndc, String key) {
        Map<String,String>  literals = graphAct.getLiterals(ndc.node);
        String item_id = ndc.ontMenu.getItemId(key);

        if (literals.containsKey(item_id)) {
            return literals.get(item_id);
        }

        return null;
    }

    /**
     * グラフをターゲット登録する
     */
    private void exec_tgtGraph() {
        EntityNode graph = graphAct.getGraphNode();
        String graph_name = graphAct.getGraphName();

        GraphManager.setHyperTarget(graph, graph_name);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(Messages.getString("alert.graph.header.target"));
        alert.setContentText(Messages.getString("alert.graph.graph") + graph_name);
        alert.showAndWait();
    }

    /**
     * グラフ情報を表示する
     */
    private void exec_information() {
        EntityNode node = graphAct.getGraphNode();
        show_indormation(node, plrAct.getName(node));
    }
}
