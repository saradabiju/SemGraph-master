package com.assemblogue.plr.app.generic.semgraph;

import java.util.*;
import java.util.List;

import com.assemblogue.plr.contentsdata.PLRContentsData;
import com.assemblogue.plr.contentsdata.ontology.OntologyItem;
import com.assemblogue.plr.lib.EntityNode;
import com.assemblogue.plr.lib.Node;

/**
 * グラフ操作クラス
 * ルートノード直下のアプリフォルダノードにグラフとしてファイルノードを作成する。
 * グラフ内の全ノードを管理するcontrolNodeをひとつ作成する。
 *
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class GraphActor extends OssActor {
	private PlrActor plrAct;

	private EntityNode rootNode;    // 自グラフのノードが属するストレージのルートノード
    private String userId;         // ストレージUserID

	private EntityNode selfNode;	// 自グラフを表すファイルノード
    private String selfNodeName;    // ファイルノード名

    // 定数
    // ControlNodeは、グラフ毎に1つ生成される、該当グラフの全ノードを記述するノード
	private static String ControlNode_Name = "ControlNode";
    private static String ControlNode_Type = "Class";

    // ControlNodeのノード記録用ノードの名前
    private static String EntryNode_Name = "Entry";
    // ControlNodeに記録されるノード毎の情報
    private static String EntryProperty_literal_node_id = "node_id";  // ノードID
    private static String EntryProperty_literal_node_link = "node_link";  // ノードへのリンク
    private static String EntryProperty_literal_sort_order = "sort_order";  // 表示ソート順
    private static String EntryProperty_literal_root = "root";  // 最左ノードフラグ
    private static String EntryProperty_literal_visible = "visible"; // 最左ノード可視フラグ

    // ##############################################################################
    /**
     * ControlNode Entry の登録内容
     */
    public class EntryProperty {
        EntityNode entryNode; // entry node実体
        EntityNode node; 	// node_idが示す実体
        String nodeId;
        String sortOrder;
        String root;		// true:トップレベルノード false:
        String visible;		// トップレベルノード時に true:可視 false:不可視

        EntryProperty(EntityNode entry_node, EntityNode node, String node_id, String sort_order, String root, String visible) {
            this.entryNode = entry_node;
            this.node = node;
            this.nodeId = node_id;
            this.sortOrder = sort_order;
            this.root = root;
            this.visible = visible;
        }

        EntryProperty(EntityNode entry_node, Map<String,String> map) {
            this.entryNode = entry_node;

            if (map.containsKey(EntryProperty_literal_node_id)) {
                nodeId = map.get(EntryProperty_literal_node_id);
                System.out.println("NODES");
            } else {
                nodeId = null;
            }

            if (map.containsKey(EntryProperty_literal_sort_order)) {
                sortOrder = map.get(EntryProperty_literal_sort_order);
            } else {
                sortOrder = null;
            }

            if (map.containsKey(EntryProperty_literal_root)) {
                root = map.get(EntryProperty_literal_root);
            } else {
                root = null;
            }

            if (map.containsKey(EntryProperty_literal_visible)) {
                visible = map.get(EntryProperty_literal_visible);
            } else {
                visible = null;
            }
        }

        public void regist(EntityNode node) {
            plrAct.setLiteral(node, EntryProperty_literal_node_id, "*", nodeId);
            plrAct.setLiteral(node, EntryProperty_literal_sort_order, "*", sortOrder);
            plrAct.setLiteral(node, EntryProperty_literal_root, "*", root);   // root nodeか？ true/false
            plrAct.setLiteral(node, EntryProperty_literal_visible, "*", visible);   // 表示可否フラグ
            plrAct.setLinkedNode(node, EntryProperty_literal_node_link, this.node);
           System.out.println("new node");
            //plrAct.sync(node);
        }

        public void change(String entryNode_prop, String newValue) {
            if (entryNode_prop.equals(EntryProperty_literal_node_id)) {
            	System.out.println("change id");
                nodeId = newValue;
            } else if (entryNode_prop.equals(EntryProperty_literal_sort_order)) {
                sortOrder = newValue;
            } else if (entryNode_prop.equals(EntryProperty_literal_root)) {
                root = newValue;
            } else if (entryNode_prop.equals(EntryProperty_literal_visible)) {
                visible = newValue;
                System.out.println("node edit");
            }

            TxtList.debug("GraphActor/change:109/sync entryNode");

            plrAct.setLiteral(entryNode, entryNode_prop, "*", newValue);
            plrAct.asyncNode(entryNode);
        }
    }
    // ##############################################################################

    // ##############################################################################
	/**
	 * 管理ノード controlNode
	 * グラフであるファイルノードに1つ作られるcontolNodeの操作クラス
	 * controlNodeには、当該ファイルノードに作成されたノードを全て登録する。
	 */
	private class ControlNode {
		EntityNode ctrl_node = null;    // controlNode本体

		List<com.assemblogue.plr.lib.Node> list; // 登録ノードリスト
        // node idをキーにした、登録ノードリスト
        Map<String, EntryProperty> entry = new LinkedHashMap<>();

        /**
         * 管理ノードは存在するか？
         * @return true:ある false:ない
         */
        boolean exist() {
            if (ctrl_node == null) {
                return false;
            }
            return true;
        }

		/**
		 * controlNodeの作成
		 * @param root グラフノード
		 */
		void create(EntityNode root) {
            TxtList.debug("GraphActor/create:146/sync root");
            EntityNode ctrl_node = plrAct.createInnerNode(root, ControlNode_Name, ControlNode_Type);
            plrAct.sync(root);
			open(ctrl_node);
		}

		/**
		 * 既存controlNodeを開く
		 * @param ctrlnode 実体ノード
		 */
		void open(EntityNode ctrlnode) {
            TxtList.debug("GraphActor/open:155/sync root");
            ctrl_node = ctrlnode;
			plrAct.sync(ctrl_node);
			update();
		}

		/**
		 * 登録ノードリストの更新
		 */
		void update() {
            // EntryNode_Name属性を取得
            // PLRの返す順番を元に処理することで、順番を固定化
            list = ctrl_node.getProperty(EntryNode_Name);

            entry.clear();

            // ファイルノード内のノードリストを取得
            List<NodeInfo<Node>> slflist = list(selfNode);

            // entryリストを更新する。
            for (com.assemblogue.plr.lib.Node nd : list) {
                EntityNode entry_node = nd.asEntity();

                Map<String,String> lit_map = plrAct.getliterals(entry_node);
                EntryProperty ep = new EntryProperty(entry_node, lit_map);

                if (ep.nodeId != null) {
                    for (NodeInfo<com.assemblogue.plr.lib.Node> ni : slflist) {
                        if (ni.getNode().getId().equals(ep.nodeId)) {
                        	System.out.println("Update list");
                            // ノード実態との紐つけをする
                            ep.node = ni.getNode().asEntity();
                            break;
                        }
                    }

                    entry.put(ep.nodeId, ep);
                }
            }

            // sync(node);
        }

		/**
		 * controlNodeにノードを登録する
		 * @param nwnode 追加するノード
         * @param isroot ルートノードか？ true:ルートノード false:ちがう
         * @return true:ctrlノード更新あり
         */
		boolean regist(EntityNode nwnode, boolean isroot) {
			if (nwnode == null) {
				return false;
			}

			String nwnode_id = nwnode.getNodeId();

			// 自身は登録しない
			if (ctrl_node.getNodeId().equals(nwnode_id)) {
				return false;
			}
			// 登録済nodeか、idで判断する
            if (entry.containsKey(nwnode_id)) {
			    return false;
            }
			// 最左ノードか？
			String rtstr = "true";
            if (!isroot) {
				rtstr = "false";
			}

			// 未登録のnodeは新規登録
            EntityNode newentry = plrAct.createInnerNode(ctrl_node, EntryNode_Name,"Class");
            // sortorderにはepochtime →作った順
            EntryProperty ep = new EntryProperty(newentry, nwnode, nwnode_id, String.valueOf(Utils.currentEpochtime()),rtstr,"true");
            ep.regist(newentry);
            System.out.println("inner node");
            entry.put(ep.nodeId, ep);

            // グラフノードの内部ノードにつき、グラフノード同期で処理
            // TxtList.debug("GraphActor/regist:233/sync node v2");
            // plrAct.asyncNode(ctrl_node);
            // plrAct.sync(newentry);

            return true;
        }

		/**
		 * controlNode登録ノードを返す
		 * @return 登録ノードのリスト
		 */
		List<EntityNode> getEntryNodes() {
            // EntoryNodeのリストを探索する
            List<EntityNode> nodes = new ArrayList<>();

            Iterator entries = entry.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry me = (Map.Entry) entries.next();
                EntryProperty ep = (EntryProperty) me.getValue();
                if (ep.node != null) {
                    nodes.add(ep.node);
                }
            }

            return nodes;
        }

        /**
         * 登録ノード情報の取得
         * @param node
         * @return
         */
		EntryProperty get(EntityNode node) {
		    if (entry.containsKey(node.getNodeId())){
		        return entry.get(node.getNodeId());
            }
            return null;
        }

        /**
         * ノードの削除
         * 実際には削除しないので、ルートノードとして見えなくする
         * @param node_id ノードID
         */
        void delete(String node_id) {
		    EntryProperty ep = entry.get(node_id);
            ep.change(EntryProperty_literal_visible, "false");
        }
	};

	private ControlNode ctrlNode = new ControlNode();
    // ##############################################################################

	/**
	 * ノードアクターを作成する
	 * nodeがnull以外の場合、既存ファイルノードを開き、nameがnull以外の場合、新規にファイルノードを作成する。
	 * @param name ファイルノード名
     * @param node ファイルノード
	 */
    GraphActor(String name, EntityNode node) {
        super();

        this.plrAct = AppController.plrAct;
        this.userId = plrAct.getUserID();

        if (node != null) {
            // 既存グラフ
            this.open_graphNode(node);
        } else if (name != null) {
            // 新規グラフ
            this.create_graphNode(name);
        }

        plrAct.asyncNode(selfNode);
        this.selfNodeName = plrAct.getName(this.selfNode);

        // HYPERNODEの名前
        if (this.selfNodeName.equals(AppProperty.LT_HYPERNODE)) {
            String nm = plrAct.getNodeName(this.selfNode);
            if (nm != null) {
                this.selfNodeName = nm;
            }
        }
    }

	/**
	 * グラフ生成
     * グラフを格納するファイルノードを生成する
	 * @param name グラフ名
     * @return 実体ノード
     */
	private EntityNode create_graphNode(String name) {
        rootNode = plrAct.getAplRootFolderNode();
        if (rootNode == null) {
            return null;
        }
        selfNode = plrAct.createFileNode(rootNode, name, "Class");

        if (selfNode == null) {
			return null;
		}

        TxtList.debug("GraphActor/create_graphNode:349/sync rootNode");
        plrAct.sync(rootNode);

        // ControlNodeを作成する
		ctrlNode.create(selfNode);

        return selfNode;
	}

	/**
	 * 既存グラフを開く
	 * @param node ノード
     * @return 実体ノード
	 */
    private EntityNode open_graphNode(EntityNode node) {
        if (node == null) {
            return null;
        }

        rootNode = plrAct.getAplRootFolderNode2(node);
        if (rootNode == null) {
            return null;
        }

        selfNode = node;

        // 既存ノードを開く際は完了復帰の同期
        TxtList.debug("GraphActor/open_graphNode:377/sync selfnode");
        plrAct.sync(selfNode);
        boolean update_ctrlnode = false;

        // controlNodeがあるか調べ、なければ新規作成
        List<NodeInfo<Node>> list = list(selfNode);

        // controlNodeを探す
        for (NodeInfo<com.assemblogue.plr.lib.Node> ni : list) {
            if (ni.equalName(ControlNode_Name)) {
                ctrlNode.open(ni.getNode().asEntity());
                break;
            }
        }

        if (!ctrlNode.exist()) {
            // controlNodeが無い場合は新規作成する
            ctrlNode.create(selfNode);
         //   System.out.println("update file");
            update_ctrlnode = true;
        }

        // 既存ノードをcontrollNodeに追加する
        for (NodeInfo<com.assemblogue.plr.lib.Node> ni : list) {
            // 中で既登録判定をしている
            if (ni.getNode().isEntity() && isClassName(ni.name)) {
                if (ctrlNode.regist(ni.getNode().asEntity(), false)) {
                    update_ctrlnode = true;
                }
            }
        }

        if (update_ctrlnode) {
            ctrlNode.update();
            plrAct.asyncNode(selfNode);
        }

        return selfNode;
    }

    /**
     * クラス名か?
     * 正規表現よりも軽いか？？
     * @param str 検査文字列
     * @return true:クラス名(#大文字〜)
     */
	private boolean isClassName(String str) {
        if (str == null) {
            return false;
        }
        if (str.length() < 2) {
            return false;
        }

	    if (str.charAt(0) == '#' && Character.isUpperCase(str.charAt(1))) {
            return true;
        }

        return false;
    }

	/**
	 * グラフのノードを取得する
	 * @return ノード
	 */
	public EntityNode getGraphNode() { return selfNode; }

	/**
	 * グラフのノードの名前を取得する
	 * @return ノード名
	 */
	public String getGraphName() { return this.selfNodeName; }

	/**
	 * グラフ内部ノードを作成する
	 * @param name ノード名
	 * @param mostleft　最左ノードか
	 * @return
	 */
	public EntityNode createGraphInnerNode(String name, boolean mostleft) {
		if (selfNode == null) {
			return null;
		}

        // 内部ノードを作成し、controlNodeに追加
		EntityNode nwnode;
		nwnode = plrAct.createInnerNode(selfNode, name, "Class");
        //TxtList.debug("GraphActor/createGraphInnerNode:474/sync selfNode");
        //plrAct.asyncNode(selfNode);
        ctrlNode.regist(nwnode, mostleft);
        // 名前：クラス名を記録
        plrAct.setLiteral(nwnode, AppProperty.LT_NAME,"*", name);
        //TxtList.debug("GraphActor/createGraphInnerNode:474/sync selfNode");
        //plrAct.asyncNode(selfNode);

		return nwnode;
	}

	/**
	 * controlNodeに登録されているノードを取得する
	 * @return 実体ノードのリスト
	 */
    public List<EntityNode> getEntryNodes() {
        if (ctrlNode == null) {
			return null;
		}

		return ctrlNode.getEntryNodes();
	}

    /**
     * controlNodeの指定内部ノードの属性を取得する
     * @param node 実体ノード
     * @return 属性実体ノード
     */
/*
	public EntityNode getNodeFromEntryNode(EntityNode node) {
        Map<String,String> map = plrAct.getliterals(node);

        if (!map.containsKey(EntryProperty_literal_node_id)) {
            return null;
        }

		String node_id = map.get(EntryProperty_literal_node_id);
        for (com.assemblogue.plr.lib.Node nd : selfNode.getProperty(node_id)) {
			return nd.asEntity();
		}

		return null;
	}
*/

	/**
	 * グラフの同期
     * グラフ実体ノードとコントロールノードを同期する
	 */
	public synchronized void sync() {
        TxtList.debug("GraphActor/sync:522/sync ctrlNode.node, selfNode");
		plrAct.sync(ctrlNode.ctrl_node);
		plrAct.sync(selfNode);
	}

    /**
     * ノードの削除
     * 物理削除はできないので、ルートノードとして表示不可能にする
     * @param node EntityNode
     */
	public void remove(EntityNode node) {
        if (ctrlNode == null) {
            return;
        }

        // ルートvisible属性を変更
        ctrlNode.delete(plrAct.getId(node));
    }

    /**
     *  PLRContensDataを取得する
     * @return PLRContentsData
     */
    public PLRContentsData getOss() { return contentsData; }

    /**
     *  指定ノードはrootノードとして表示可能かを取得する
     * @param node ノードID
     * @return true:表示可能  false:表示不可
     */
    public boolean isVisibleRoot(EntityNode node) {
        if (ctrlNode.entry.containsKey(plrAct.getId(node))) {
            EntryProperty ep = ctrlNode.entry.get(plrAct.getId(node));

            if (ep.visible.equals("true") && ep.root.equals("true")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 指定IDのノードを取得する
     * @param node_id ノードID
     * @return EntityNode
     */
    public EntityNode getNode(String node_id) {
        if (ctrlNode.entry.containsKey(node_id)) {
            return ctrlNode.entry.get(node_id).node;
        }

        return null;
    }

    /**
     * 指定ノードの入力可能属性を取得する（第一階層のみ）
     * @param item_id OntologyItem ID(#〜)
     * @return 属性リスト
     */
    public List<PLRContentsData.InputableProperty> getPropertyMenu(String item_id) {
        OntologyItem item = contentsData.getNode(item_id);
        List<PLRContentsData.InputableProperty>  list = new ArrayList<>();

        for (PLRContentsData.InputableProperty ip : contentsData.PropertyMenu(item)) {
            if (ip.item.subPropertyOf == null) {
                list.add(ip);
            }
        }

        return list;
    }

    /**
     * 指定ノードのリテラルを取得する
     * @param node
     * @return リテラルリスト
     */
    public Map<String,String> getLiterals(EntityNode node) {
        return plrAct.getliterals(node);
    }

    /**
     *	nodeがnullの場合、ノードリスト最後のノードのプロパティを集める
     * @param node ノード
     * @return プロパティのリスト
     */
    public List<NodeInfo<Node>> list(EntityNode node) {
        List<NodeInfo<Node>> list = new ArrayList<>();
        plrAct.list(list,node);
        return list;
    }

    /**
     * 新規クラスの追加と、親クラスへのリンク登録
     * グラフノード直下に、nameノードを作成、親ノード(root)にnameノードへのリンク情報を追加する。
     * nameへのリンク情報は、rootノードにリンク用ノード(名前ontitem.id)を作成してリテラルとしてURIと属性値を設置する。
     * @param root 親ノード
     * @param ontitem 属性のOntologyItem
     * @param nw_name 追加するクラス名
     * @return 0:追加したノード 1:rootに追加した内部ノード
     */
    public List<EntityNode> addRelationClass(EntityNode root, OntologyItem ontitem, String nw_name) {
        // 新規クラスを作成(with sync)し、controllNodeへ追加
        EntityNode nw_node = createGraphInnerNode(nw_name, false);


        // 属性収容内部クラスを作成し、URI,属性値を収容する
        EntityNode i_node = plrAct.createInnerNode(root, ontitem.id, ontitem.type);

        if (ontitem.isClass()) {
            //TxtList.debug("create inner node name:" + ontitem.id + " type:Class");
        } else {
            if (ontitem.rangeRef != null) {
                // URIはrangeリテラル、属性値は属性名リテラルに収納する
                plrAct.setLinkedNode(i_node, AppProperty.RANGE, nw_node);
                // ここでは仮設定、実際は属性値変更リスナで設定している
                plrAct.setLiteral(i_node, ontitem.id, "*", "");
            }
        }

        //TxtList.debug("GraphActor/addRelationClass:670/sync i_node, root");
        //sync(fldNode); createGraphInnerNodeでsync済み
        //sync(i_node);
        //sync(root);  i_node同期でsyncされる？
        List<EntityNode> rt = new ArrayList<>();
        System.out.println("new array entry created with new node");
        rt.add(nw_node);
        rt.add(i_node);
        return rt;
    }


    public EntityNode addRelationClass(EntityNode root, OntologyItem ontitem, EntityNode nw_node) {
        // 属性収容内部クラスを作成し、URI,属性値を収容する
        EntityNode i_node = plrAct.createInnerNode(root, ontitem.id, ontitem.type);

        if (ontitem.isClass()) {
            //TxtList.debug("create inner node name:" + ontitem.id + " type:Class");
        } else {
            if (ontitem.rangeRef != null) {
                // URIはrangeリテラル、属性値は属性名リテラルに収納する
                plrAct.setLinkedNode(i_node, AppProperty.RANGE, nw_node);
                System.out.println("copy and paste");
                // ここでは仮設定、実際は属性値変更リスナで設定している
                plrAct.setLiteral(i_node, ontitem.id, "*", "");
            }
        }

        //TxtList.debug("GraphActor/addRelationClass:670/sync i_node, root");
        //sync(fldNode); createGraphInnerNodeでsync済み
        //sync(i_node);
        //sync(root);  i_node同期でsyncされる？
        return i_node;
    }

    /**
     * 属性値リテラルの設定
     * @param node 親ノード
     * @param item_id 属性のid
     * @param prop_node 変更する属性ノード
     * @param value 設定値
     */
    void setRelationLiteral(EntityNode node, String item_id, EntityNode prop_node, String value) {
        // 指定ノードのitem_idプロパティを取得し、prop_nodeと同じ属性ノードに、valueを追加する
        List<EntityNode> props = getproperty(node, item_id);

        for (EntityNode nd : props) {
            if (nd.equals(prop_node)) {
                Map<String, Node> properties = plrAct.listToMap(nd);
                if (properties.containsKey(AppProperty.RANGE)) {
                    // リテラルセット
                    plrAct.setLiteral(nd, item_id, "*", value);
                    //sync(nd);
                }
            }
        }

        TxtList.debug("GraphActor/setRelationLiteal:728/sync node");
        plrAct.asyncNode(node);
    }

    /**
     * 親クラスからのクラスリンク属性の削除
     * @param parent 親クラスノード
     * @param ontitem 属性のOntologyItem
     * @param linked_node 削除するリンクノード
     */
    void removeRelationItem(EntityNode parent, OntologyItem ontitem, EntityNode linked_node, EntityNode prop_node) {
        List<EntityNode> props = getproperty(parent, ontitem.id);

        for (EntityNode nd : props) {
            if (prop_node != null && !nd.equals(prop_node)) {
                continue;
            }

            Map<String, Node>  properties = plrAct.listToMap(nd);
            if (properties.containsKey(AppProperty.RANGE)) {
                EntityNode node = properties.get(AppProperty.RANGE).asEntity();
                if (node != null) {
                    if (node.getURI().equals(linked_node.getURI())) {
                        plrAct.removeProperty(parent, nd, ontitem.id);
                        break;
                    }
                }
            }
        }

        TxtList.debug("GraphActor/removeRelationItem:750/sync parent");
        plrAct.asyncNode(parent);
    }

    /**
     * nameのプロパティを取得する
     * @param node
     * @param name
     * @return
     */
    public List<EntityNode> getproperty(EntityNode node, String name) {
        List<EntityNode> res = new ArrayList<>();
        System.out.println("???");

        for (NodeInfo<Node> ni : list(node)) {
            if (ni.equalName(name)) {
                EntityNode en = ni.node.asEntity();
                res.add(en);
            }
        }

        return res;
    }
}
