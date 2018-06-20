package com.assemblogue.plr.app.generic.semgraph;

/**
 * 定数クラスです。
 * @created <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public final class AppProperty {
    public static String VERSION = "ver.1.00";

    // Semantic Editor Root Folder
    public static String CAPRAIBEX_ROOT_FOLDER = "CapraIbexRootFolder";

    // Semantic Editor Me to Friend Property
    public static String CAPRAIBEX_ME_TO_FRIEND_PROPRTY = "semgraph";

    // ノード
    // オントロジの記述外だが、グラフファイルノードやグラフ内ノードはname属性を持つ
    public static String LT_NAME = "name";  // ノード名


    // Graph オントロジ関連
    // オントロジに記述されないが、ノードはハイパーノード属性を持つ
    public static String LT_HYPERNODE = "HyperNodeURI";  // ハイパーノード

    // オントロジに記述される項目のうち、システムが自動的に値を設定する項目
    public static String ITEM_ID_BEGIN="#begin";   // ノード作成日時
    public static String ITEM_ID_CREATOR="#creator";   // ノード作成者

    // オントロジ記述決めうち；属性編集メニューには表示しない
    public static String ITEM_ID_REL="#rel";   // 関係性

    // オントロジ記述決めうち
    public static String ITEM_ID_CNT="#cnt";    // 内容
    public static String RANGE="range";   // 関係性のリンク先
    public static String TYPE_SYMMETRICK = "SymmetricProperty";  // 対象性あり

    // 属性の向き
    public static String PROP_DIR_RIGHT = "▶";
    public static String PROP_DIR_LEFT = "◀";
    public static String PROP_DIR_RIGHT_OLD = ":▶";
    public static String PROP_DIR_LEFT_OLD = ":◀";

    // ノード追加ボタン
    //  最左ノード追加ボタン
    public static String ADD_NODE_BTN = "＋";
    public static String ADD_NODE_BTN_PLUS = "＋　";

    // 編集ペインV/HBoxのスタイル
    //　編集ペインの背景色
    public static String SCROLLPANE_BASE_STYLE = "-fx-background:white;";
    //　編集ペインの背景色
    public static String BOX_BASE_STYLE = "-fx-background-color:white; -fx-border-style:none; -fx-border-insets:0;";
    //　ベース白色
    public static String BOX_STYLE_FILL_WHITE = "-fx-background-color:blue; -fx-border-style:none; -fx-border-insets:0;";
    //　ラベル
    public static String BOX_STYLE = "-fx-background-color:rgb(197,255,255); -fx-border-style:solid; -fx-border-width:1.0; -fx-border-insets:0; -fx-border-color:white;";
    //　関係性ボタン
    public static String BOX_STYLE_REL_BTN = "-fx-background-color:rgb(67,186,243); -fx-border-style:solid; -fx-border-width:1.0; -fx-border-insets:0; -fx-border-color:white;";
    //  トップレベルノード追加ボタン
    public static  String ADD_TOP_NODE_BTN_STYLE = "-fx-background-color:rgb(67,186,243); -fx-border-width:0.0; -fx-border-insets:0;";
    //  右側ノード追加ボタン
    public static  String ADD_RIGHT_NODE_BTN_STYLE = "-fx-background-color:rgb(67,186,243); -fx-border-style:none;";
    //  右側ノード追加ボタン(非表示)
    public static  String ADD_RIGHT_NODE_HIDDEN_BTN_STYLE = "-fx-text-fill:white; -fx-background-color:white; -fx-border-style:none;";

    // 関係性メニュー　項目ラベル
    public static String REL_BOX_STYLE = "-fx-background-color:rgb(197,255,255); -fx-border-style:solid; -fx-border-width:1.0; -fx-border-insets:0; -fx-border-color:white;";
    // 関係性メニュー　選択ラベル "-fx-background-color:linear-gradient(to bottom, lavender, lightgray)";
    public static String REL_LABEL_STYLE="-fx-background-color:rgb(137,255,206); -fx-border-style:solid; -fx-border-width:1.0; -fx-border-insets:0; -fx-border-color:white;";

    // 編集メニュー　選択不可項目
    public static String EMENU_NOT_ACTIVE="-fx-text-fill: lightgray;";

    // RDFグラフボックス色
    public static String RDFGRAPH_BOX_STYLE = "-fx-background-color:rgb(197,255,255); -fx-border-style:solid; -fx-border-width:1.0; -fx-border-insets:0; -fx-border-color:black;";

    private AppProperty () {}
}
