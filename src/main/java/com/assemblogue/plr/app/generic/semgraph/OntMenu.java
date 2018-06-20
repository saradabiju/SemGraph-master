package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.contentsdata.PLRContentsData;
import com.assemblogue.plr.contentsdata.misc.LocaleString;
import com.assemblogue.plr.contentsdata.ontology.OntologyItem;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;


/**
 * 属性メニュー
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class OntMenu {

    public class OntMenuItem {
        LinkedList<OntMenuItem> child;

        public OntologyItem ontologyItem;
        public Integer maxCardinality;
        public Integer minCardinality;
        public String id;
        public String label;
        public String range;
        public List<String> value;
        public long epochtime;
        public TextField fxText;
        public TextArea fxTextA;
        public Label fxLabel;
        public VBox vbox;

        OntMenuItem() {
            this.maxCardinality = 1;
            this.minCardinality = 1;
        }

        OntMenuItem(OntMenuItem omi) {
            this.id = omi.id;
            this.minCardinality = 0;
            this.maxCardinality = Integer.MAX_VALUE;
            this.value = new ArrayList<>();   // 値、選択された子供
            this.child = new LinkedList<>();
            this.label = omi.label;
            this.range = omi.range;
        }

        /**
         * オントロジー階層を辿り、メニューを構築する
         * @param root
         */
        OntMenuItem(OntologyItem root) {
            this.ontologyItem = root;
            this.minCardinality = 0;
            this.maxCardinality = Integer.MAX_VALUE;
            this.value = new ArrayList<>();   // 値、選択された子供
            this.child = new LinkedList<>();

            if (root != null) {
                this.id = root.id;
                this.label = LocaleString.getString(root.label, "ja");
                this.range = root.range;

                // name の入力可能な属性のリスト(サブプロパティを含めない)
                for (PLRContentsData.InputableProperty ip : graphact.getPropertyMenu(root.id)) {
                    OntMenuItem omi = new OntMenuItem(ip.item);
                    omi.maxCardinality = ip.maxCardinality;
                    omi.minCardinality = ip.minCardinality;
                    this.child.add(omi);
                }

                // サブプロパティを探索
                if (root.isProperty()) {
                    if (root.superClassOf != null) {
                        for (OntologyItem item : root.superClassOf) {
                            this.child.add(new OntMenuItem((item)));
                        }
                    } else {
                        if (!root.type.equals(AppProperty.TYPE_SYMMETRICK) && root.range == null) {
                            // 対象性のあるものについては、サブ項目を付加する
                            String[] dirs = {AppProperty.PROP_DIR_RIGHT, AppProperty.PROP_DIR_LEFT};
                            for (String dir : dirs) {
                                OntMenuItem sym_omi = new OntMenuItem(this);
                                sym_omi.ontologyItem = root;
                                if (dir.equals(AppProperty.PROP_DIR_LEFT)) {
                                    sym_omi.label = dir + this.label;
                                } else {
                                    sym_omi.label = this.label + dir;
                                }
                                this.child.add(sym_omi);
                            }
                        }
                    }
                }
            }
        }

        /**
         * rangeがクラスが否か
         * @return
         */
        boolean rangeIsClass() {
            if (range != null && range.charAt(0) == '#') {
                if (Character.isUpperCase(range.charAt(1))) {
                    return true;
                }
            }

            return false;
        }

        /**
         * label に一致するアイテムを取得
         * @param label
         * @return
         */
        OntMenuItem getOntMenuItem(String label) {
            if (this.label.equals(label)) {
                return this;
            }

            for (OntMenuItem omi : child) {
                OntMenuItem res = omi.getOntMenuItem(label);
                if (res != null) {
                    return res;
                }
            }

            return null;
        }
    }

    GraphActor graphact;
    OntMenuItem menu;

    OntMenu(GraphActor fndact, OntologyItem root) {
        this.graphact = fndact;
        this.menu = new OntMenuItem(root);
    }

    /**
     * 空のOntMenuItemを取得
     * @return OntMenuItemオブジェクト
     */
    public OntMenuItem getOntMenuItem() {
        OntMenuItem omi = new OntMenuItem();
        return omi;
    }

    /**
     * 結果の取得
     * keyとvalueのOntMenuItemが違う場合、Velueはいくつかの選択肢から選ばれたもの
     * keyのrangeが#〜のクラス名であり、かつkeyとvalueが異なる場合は、属性値にクラス実体が紐ついたものとする
     * @return
     */
    public HashMap<OntMenuItem,OntMenuItem> getResult() {
        HashMap<OntMenuItem, OntMenuItem> map = new HashMap<>();

        for (OntMenuItem item : menu.child) {
            OntMenuItem key = item;
            if (item.child.size() > 0) {
                item = latestOmi(item.child, null);
            }

            map.put(key, item);
        }

        return map;
    }

    /**
     * rangeがClassのOntMenuItemを取得
     * @return　OntMenuItemのリスト
     */
    public List<OntMenuItem> getRangedClassItem() {
        List<OntMenuItem> list = new ArrayList<>();

        if (menu.range != null && menu.rangeIsClass()) {
            list.add(menu);
        }

        for (OntMenuItem omi : menu.child) {
            if (omi.rangeIsClass()) {
                list.add(omi);
            }
        }

        return list;
    }

    /**
     * メニューリストのアイテムのうち、最新アイテム（最後に更新された項目）を取得する
     * @param lomi メニューアイテムリスト
     * @param omi 最新アイテム
     * @return 最新アイテム
     */
    private OntMenuItem latestOmi(List<OntMenuItem> lomi, OntMenuItem omi) {
        for (OntMenuItem item : lomi) {
            if (item.child.size() > 0) {
                omi = latestOmi(item.child, omi);
            }

            if ((omi == null) || (item.epochtime > omi.epochtime)) {
                omi = item;
            }
        }

        return omi;
    }

    /**
     * #### 環境依存コード ####
     * Controller側に記述すべきだが、OntMenuにべったりなのでこちらに置く
     * かつ、特定オントロジーに特化した部分がある。
     */
    /**
     * 属性メニューを作成する
     * @param literals 属性名->属性値のマップ
     * @return
     */
    VBox makeMenu(Map<String,String> literals) {
        VBox menu_vbox = new VBox(10d);

        for (OntMenuItem omi : this.menu.child) {

            if (omi.id.equals(AppProperty.ITEM_ID_REL)) {
                // 関係性は属性メニューに表示しない
                continue;
            }

            HBox hbox = new HBox(10d);
            hbox.setMaxWidth(Double.MAX_VALUE);
            hbox.setAlignment(Pos.CENTER_LEFT); // 配置位置

            Label label = new Label(omi.label + "：");
            label.setPrefWidth(70);
            hbox.getChildren().add(label);

            if (omi.range != null) {
                if (omi.id.equals(AppProperty.ITEM_ID_BEGIN)) {
                    String v = Utils.currentDateTime();
                    setupTextFiled(omi, literals, v, "日時を入力してください。");
                    hbox.getChildren().add(omi.fxText);
                } else if (omi.id.equals(AppProperty.ITEM_ID_CREATOR)) {
                    String v = AppController.plrAct.getUserID();
                    setupTextFiled(omi, literals, v, "入力してください。");
                    omi.fxText.setEditable(false);
                    omi.fxText.setMouseTransparent(true);
                    omi.fxText.setFocusTraversable(false);
                    omi.fxText.setStyle(AppProperty.EMENU_NOT_ACTIVE);
                    hbox.getChildren().add(omi.fxText);

                } else {
                    // 少なくとも1つ設定すべき項目
                    if (omi.range.equals("dateTime")) {
                        String v = null;

                        if (omi.maxCardinality == 1) {
                            if (literals.containsKey(omi.id)) {
                                v = literals.get(omi.id);
                            } else if (omi.minCardinality > 0) {
                                v = Utils.currentDateTime();
                            }
                        } else {
                            if (literals.containsKey(omi.id)) {
                                v = literals.get(omi.id);
                            }
                        }
                        setupTextFiled(omi, literals, v, "日時を入力してください。");
                        hbox.getChildren().add(omi.fxText);
                    } else if (omi.range.equals("string")) {
                        String v = null;
                        if (omi.maxCardinality == 1) {
                            if (literals.containsKey(omi.id)) {
                                v = literals.get(omi.id);
                            }
                        }
                        setupTextFiled(omi, literals, v, "入力してください");
                        hbox.getChildren().add(omi.fxText);
                    } else if (omi.range.equals("mmdata")) {
                        String v = null;
                        if (omi.maxCardinality == 1) {
                            if (literals.containsKey(omi.id)) {
                                v = literals.get(omi.id);
                            }
                        }
                        setupTextArea(omi, literals, v, "入力してください");
                        hbox.getChildren().add(omi.fxTextA);
                    } else {
                        // 他クラス
                    }
                }
            }

            if (omi.id.equals(AppProperty.ITEM_ID_CNT)) {
                // #cnt 内容は一番上におく
                menu_vbox.getChildren().add(0, hbox);
            } else {
                menu_vbox.getChildren().add(hbox);
            }
            menu_vbox.setMargin(hbox,new Insets(5,10,0,10));
        }

        return menu_vbox;
    }

    /**
     * 属性メニューの各入力欄を作成
     * @param omi 属性
     * @param literals ノード設定済の属性値
     * @param def　デフォルト文字列
     * @param prompt　プロンプト文字列
     */
    private void setupTextFiled(OntMenuItem omi, Map<String,String> literals, String def, String prompt) {
        omi.fxText = new TextField();
        omi.fxText.setPrefWidth(200);
        omi.fxText.setPrefHeight(10);

        String v = def;

        if (literals.containsKey(omi.id)) {
            v = literals.get(omi.id);
        }

        if (v != null) {
            if (omi.value.size() >= omi.maxCardinality) {
                omi.value.remove(omi.value.size() - 1);
            }
            omi.value.add(v);
        }

        if (omi.value.size() > 0) {
            omi.fxText.setText(omi.value.get(0));
        } else {
            omi.fxText.setPromptText(prompt);
        }

        // テキストフィールドの値変更を捕まえ、OntMenuItemにセットする
        omi.fxText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (omi.value.size() >= omi.maxCardinality) {
                    omi.value.remove(omi.value.size()-1);
                }
                omi.value.add(newValue);
            }
        });
    }

    /**
     * 属性メニューの各入力欄を作成 for TextArea
     * @param omi 属性
     * @param literals ノード設定済の属性値
     * @param def　デフォルト文字列
     * @param prompt　プロンプト文字列
     */
    private void setupTextArea(OntMenuItem omi, Map<String,String> literals, String def, String prompt) {
        omi.fxTextA = new TextArea();
        omi.fxTextA.setMaxWidth(Double.MAX_VALUE);
        omi.fxTextA.setPrefColumnCount(20);
        omi.fxTextA.setPrefRowCount(4);
        //omi.fxTextA.setWrapText(true);

        String v = def;

        if (literals.containsKey(omi.id)) {
            v = literals.get(omi.id);
        }

        if (v != null) {
            if (omi.value.size() >= omi.maxCardinality) {
                omi.value.remove(omi.value.size() - 1);
            }
            omi.value.add(v);
        }

        if (omi.value.size() > 0) {
            omi.fxTextA.setText(omi.value.get(0));
        } else {
            omi.fxTextA.setPromptText(prompt);
        }

        // テキストフィールドの値変更を捕まえ、OntMenuItemにセットする
        omi.fxTextA.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (omi.value.size() >= omi.maxCardinality) {
                    omi.value.remove(omi.value.size()-1);
                }
                omi.value.add(newValue);
            }
        });
    }

    /**
     * 選択時のアクション
     * @param omi
     */
    private void setValue(OntMenuItem omi, Label fx_label) {
        if (omi.value.size() >= omi.maxCardinality) {
            omi.value.remove(omi.value.size()-1);
        }

        omi.value.add(omi.id);
        omi.epochtime  = new Date().getTime(); // 現在時刻を保存
        fx_label.setText(omi.label); // Labelの表示文字列を属性のラベルに差し替え
    }

    private void setValue(OntMenuItem omi, Label fx_label, Stage stage) {
        setValue(omi, fx_label);
        stage.close();
    }

    /**
     * V/HBoxで関係性メニューを作る
     * @param omi メニュー項目ツリー
     * @param fx_label ノード編集ペインに表示するラベルオブジェクト
     * @return VBoxオブジェクト
     */
    private VBox recprops2(OntMenuItem omi, Label fx_label, Stage stage) {
        VBox vbox = new VBox(0d);

        vbox.setAlignment(Pos.CENTER_LEFT); // 縦中央、横左寄せ
        vbox.setStyle(AppProperty.BOX_BASE_STYLE);

        if (omi.child.size() > 0) {
            for (OntMenuItem omitm : omi.child) {
                HBox hbox = new HBox(0d);
                hbox.setAlignment(Pos.CENTER_LEFT); // 縦中央、横左寄せ
                hbox.setStyle(AppProperty.BOX_STYLE_FILL_WHITE);

                omitm.fxLabel = new Label(omitm.label);
                omitm.fxLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                omitm.fxLabel.setPrefWidth(80d);
                omitm.fxLabel.setWrapText(true);  // 折り返しあり

                HBox label_hbox = new HBox(0d);
                label_hbox.setAlignment(Pos.CENTER_LEFT); // 縦中央、横左寄せ

                if (omitm.child.size() == 0 && omitm.range == null) {
                    label_hbox.setStyle(AppProperty.REL_LABEL_STYLE);
                    label_hbox.getChildren().add(omitm.fxLabel);
                    hbox.getChildren().add(label_hbox);

                    // 末端；サブプロパティがなく、レンジも持っていないなら、ラベル相当
                    omitm.fxLabel.setOnMouseClicked(event -> setValue(omitm, fx_label, stage));
                } else {
                    label_hbox.setStyle(AppProperty.REL_BOX_STYLE);
                    label_hbox.getChildren().add(omitm.fxLabel);
                    hbox.getChildren().add(label_hbox);

                    // せっかくサブプロパティのリストを取得したので渡す
                    VBox submenu = recprops2(omitm, fx_label, stage);
                    hbox.getChildren().add(submenu);
                }

                vbox.getChildren().add(hbox);
            }
        }

        return vbox;
    }

    /**
     * V/HBoxで関係性メニューを作る
     * @param item_id オントロジー上のID
     * @return メニュー構成
     */
    List<OntMenuItem> makeMenuList2(String item_id) {
        List<OntMenuItem> omi_list = new ArrayList<>();

        for (OntMenuItem omi : this.menu.child) {
            if (omi.id.equals(item_id)) {
                Stage stage = new Stage();
                omi.fxLabel = new Label(omi.label);
                omi.fxLabel.setPrefWidth(50d);
                omi.fxLabel.setWrapText(true);  // 折り返しあり
                if (omi.child.size() > 0) {
                    omi.vbox = recprops2(omi, omi.fxLabel, stage);
                    omi.fxLabel.setOnContextMenuRequested(event -> showRelMenu(omi, stage));
                    omi.fxLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                }

                omi_list.add(omi);
            }
        }

        return omi_list;
    }

    /**
     * 右クリックでメニュー表示
     * @param omi
     */
    private void showRelMenu(OntMenuItem omi, Stage stage) {
        // 文字背景処理を変え、選択箇所を明確にする
        omi.fxLabel.setStyle("-fx-background-color:red;");

        stage.setTitle(omi.label);
        ScrollPane scrl_pane = new ScrollPane();
        scrl_pane.setContent(omi.vbox);
        scrl_pane.setFitToWidth(true);
        scrl_pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        stage.setScene(new Scene(scrl_pane));

        // 表示開始時、最大サイズは親ウィンドウと同等、表示開始後に最大サイズを広げる
        stage.setMaxHeight(Utils.getWindowHeight());

        // 選択せずに閉じた場合、背景色を元に戻す
        stage.showingProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue == true && newValue == false) {
                omi.fxLabel.setStyle("-fx-background-color:none;");
            }
        }));

        stage.show();

        double dd = omi.vbox.getWidth();
        if (dd < Utils.getWindowWidth()) {
            stage.setMinWidth(dd + 10d);
            stage.setMaxWidth(dd + 10d);
        } else {
            scrl_pane.setFitToWidth(false);
        }

        stage.setMaxHeight(Double.MAX_VALUE);
    }

    /**
     * label に一致するアイテムのIDを取得
     * @param label
     * @return
     */
    public String getItemId(String label) {
        OntMenuItem omi = menu.getOntMenuItem(label);
        if (omi == null) {
            return null;
        }

        return omi.ontologyItem.id;
    }

    /**
     * 関係性を示す三角形︎の旧→新変換
     * @param rel　関係性文字列
     * @return 新仕様文字列
     */
    public static String convertNew(String rel) {
        if (rel.equals("")) {
            return rel;
        }

        if (rel.contains(AppProperty.PROP_DIR_LEFT_OLD)){
            rel = rel.substring(0, rel.indexOf(AppProperty.PROP_DIR_LEFT_OLD));
            rel = AppProperty.PROP_DIR_LEFT+rel;
        } else if (rel.contains(AppProperty.PROP_DIR_RIGHT_OLD)) {
            rel = rel.substring(0, rel.indexOf(AppProperty.PROP_DIR_RIGHT_OLD));
            rel = rel+AppProperty.PROP_DIR_RIGHT;
        }

        return rel;
    }

}

