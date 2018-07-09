package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.lib.EntityNode;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * 作成済みNodeCell
 * NodeCell編集時の表示情報共有が目的
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class NodePalette {
    // 表示ノードの共有用
	class NodeParts {
		public EntityNode node;
		public SimpleStringProperty contents;  // bindして使う

		NodeParts(EntityNode node, String cnt) {
			this.node = node;
			this.contents = new SimpleStringProperty(cnt);
		}

		/**
		 * コンテンツ設定
		 * @param cnt
		 */
		public void setContents(String cnt) {
			if (contents == null) {
				contents = new SimpleStringProperty(cnt);
			} else {
				contents.setValue(cnt);
			}
		}

        /**
         * コンテンツ取得
         * @return
         */
		public SimpleStringProperty contents() {
			if (contents == null) {
				return null;
			}
            return contents;
		}

		/**
		 * 表示用文字列の取得
		 */
		public String contentsString() {
			return contents.getValue();
		}
	}

	Map<String,NodeParts>  palette = new HashMap<>();


    /**
     *
     */
	NodePalette() {
		palette.clear();
	}

    /**
     * クリア
     */
	public void clear() {
	    palette.clear();
    }

	/**
     * ノードの登録
     * @param node EntityNode
     */
    public void regist(String uri, EntityNode node) {
        if (!registered(uri)) {
            NodeParts ndprts = new NodeParts(node, "");
            palette.put(uri, ndprts);
        }
    }

	public void regist(EntityNode node) {
		if(node != null && node.getURI() !=null) { // i added this null check to avoid error. need to check later
		String uri = node.getURI().toString();
        regist(uri, node);
		}
	}

	/**
     * すでに登録済みかチェック
     * @param node EntityNode
     * @return 登録済:true
     */
    public boolean registered(EntityNode node) {
        return registered(node.getURI().toString());
	}

    /**
     * すでに登録済みかチェック
     * @param uri EntityNodeのURI
     * @return 登録済:true
     */
    public boolean registered(String uri) {
        if (palette.containsKey(uri)) {
            return true;
        }
        return false;
    }

    /**
     * NodePartsの取得
     * @param uri
     * @return
     */
    NodeParts getNodeParts(String uri) {
        if (registered(uri)) {
            return palette.get(uri);
        }
        return null;
    }

    NodeParts getNodeParts(EntityNode node) {
    	if(node != null && node.getURI() !=null) { // i added this null check to avoid error. need to check later
        String uri = node.getURI().toString();
        return getNodeParts(uri);
    	}
    	return null;
    }

    /**
     * コンテンツの設定
     * @param uri EntityNode
     * @param cnt コンテンツString
     * @return コンテンツ SimpleStringProperty
     */
    public void setContentsString(String uri, String cnt) {
        NodeParts ndprts = getNodeParts(uri);
        if (ndprts != null) {
            if (cnt == null) {
                cnt = "";
            }
            ndprts.setContents(cnt);
        }
    }

    /**
     * コンテンツの設定
     * @param node EntityNode
     * @param cnt コンテンツString
     * @return コンテンツ SimpleStringProperty
     */
    public void setContentsString(EntityNode node, String cnt) {
        regist(node);
        if(node != null && node.getURI() !=null) { // i added this null check to avoid error. need to check later
        String uri = node.getURI().toString();


        setContentsString(uri, cnt);    }
    }

    /**
     * コンテンツの取得
     * @param node EntityNode
     * @return コンテンツ SimpleStringProperty
     */
    public SimpleStringProperty getContents(EntityNode node) {
        NodeParts ndprts = getNodeParts(node);
        if (ndprts != null) {
            return ndprts.contents();
        }
        return null;
    }

    /**
     * コンテンツの取得
     * @param node EntityNode
     * @return コンテンツ String
     */
    public String getContentsString(EntityNode node) {
        SimpleStringProperty ssp = this.getContents(node);
        if (ssp == null) {
            return  null;
        }
        return ssp.getValue();
    }
}

