package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.io.PlrEntry;
import com.assemblogue.plr.io.PlrEntry.Origin;
import com.assemblogue.plr.io.Storage;
import com.assemblogue.plr.lib.*;
import com.assemblogue.plr.lib.EntityNode.SyncCallback;
import com.assemblogue.plr.lib.Node;
import com.assemblogue.plr.lib.model.Friend;
import com.assemblogue.plr.lib.model.Root;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.StageStyle;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * PLR2操作レイヤ
 * コマンドシェル処理を参考に、必要最小限の処理を実装。
 * @arrenged <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class PlrActor {
    private static final String LANG_NONE_SYMBOL = "*";

    private boolean err_print = false;
    private boolean out_print = false;

    private PLR plr;
    private Storage storage;

    private Boolean node_update = false;

    private static Boolean passphrase = false;
    private static Boolean passphrase_waiting = false;

    private static Boolean friend_setup = false;

    public class NodeName {
        String name;
        String id;
        String uri;
        EntityNode node;

        NodeName(String name, String id, String uri, EntityNode node) {
            this.name = name;
            this.id = id;
            this.uri = uri;
            this.node = node;
        }
    }

    // ストレージルートノード
    private NodeInfo<EntityNode> storageRootNode = null;
    // アプリルートフォルダノード
    private NodeInfo<Node> aplRootFolderNode = null;

    // ノードリストテンポラリ
    private List<NodeInfo<Node>> propList = new ArrayList<>();

    // 非同期ノードsync用のスレッドプール
    private final ExecutorService service = Executors.newCachedThreadPool();


    PlrActor() {
    }

    public void setPlr(PLR plr) {
        this.plr = plr;
    }

    public void setStorage(Storage storage) {
        if (storage == null) {
            return;
        }

        if (this.storage != null) {
            if (this.storage.equals(storage)) {
                return;
            }
            if (aplRootFolderNode != null) {
                aplRootFolderNode = null;
            }
        }

        friend_setup = false;
        this.storage = storage;
    }

    /**
     * rootNode 準備
     *
     * @throws Exception
     */
    private void prepare() throws Exception {
        EntityNode node = null;

        try {
            // ストレージルートノードの取得：初回はパスフレーズを聞かれる（例外）
            node = plr.getRootNode(storage);
            outPrint("Loading root node... ");
            outPrint("done.\n");
            passphrase = true;

        } catch (IllegalStateException e) {
            if (passphrase_waiting) {
                return;
            }
            passphrase_waiting = true;

            // パスフレーズ要求
            try {
                //TextInputDialog textIn = new TextInputDialog("passphrase");
                TextInputDialog textIn = new TextInputDialog();
               textIn.setTitle(Messages.getString("dialog.passphrase.title"));

                textIn.getDialogPane().setHeaderText(Messages.getString("dialog.passphrase.label"));
                textIn.initStyle(StageStyle.UTILITY);
                TxtList.set("passphrase for " + getUserID());
                String str = textIn.showAndWait().orElse("");
                // ストレージルートノード取得
                storage.postPassphrase(str);
                node = plr.getRootNode(storage);
                passphrase = true;
                passphrase_waiting = false;
            } catch (Exception ee) {
                passphrase_waiting = false;
                errPrint("Error: " + ee.getLocalizedMessage());
                return;
            }
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
            return;
        }

        if (node == null) {
            outPrint("Cancelled");
            return;
        }

        if (storageRootNode == null) {
            storageRootNode = new NodeInfo<>(node, "root");
        } else {
            storageRootNode.set(node, "root");
        }

        if (!friend_setup) {
            friend_setup = true;
            // フレンド向け設定開始（別スレッド起動）
            setpupFriends();
        }
    }

    /**
     *　ストレージルードノードの取得
     */
    public void preprocess() {
        try {
			storageRootNode = null;
            prepare();
        }
        catch (Exception e) {
			errPrint("Error: " + e.getLocalizedMessage());
        }
    }

    /**
     * 同期
     * 指定ノードの同期をとる
     *　指定が無い場合、rootNodeと同期をとる
     * @param node
     */
    public synchronized void sync(EntityNode node) {
        TxtList.debug("sync");

        if (node != null) {
        	if (node.getNodeId().equals("")) {
				outPrint("sync:Folder: " + node.getURI().toString());
			System.out.println("syn node");
			} else {
				outPrint("sync:Node: " + node.getURI().toString());
			}
        }

		SyncHandler h = new SyncHandler();
		synchronized (h) {
			try {
				if (node == null) {
					if (storageRootNode == null) {
						preprocess();
						return;
					}

                    node = storageRootNode.node;
				}

				node.sync(true, h);
				// notify()まで待つ
                h.wait();
            } catch (InterruptedException e) {
				errPrint("Error: " + e.getLocalizedMessage());
			}
		}
	}

    /**
     * 同期
     * 指定ノードの同期をとる
     *　指定が無い場合、rootNodeと同期をとる
     * @param node
     */
    public synchronized void syncSimple(EntityNode node) {
        TxtList.debug("sync simple");

        if (node == null) {
            sync(node);
        }

        try {
            node.sync(true);
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
        }
    }

	/**
	 * 同期
	 */
	private class SyncHandler extends HandlerBase implements SyncCallback {
		private boolean updated;

		@Override
		public synchronized void onUpdate(EntityNode node, Origin origin) {
			outPrint("Update from " + origin);
			updated = true;
		}

		@Override
		public synchronized void onFinish(EntityNode node) {
			outPrint("Sync finished." + (updated ? "" : ".. no updates."));
            if (updated) {
                // ノードが更新された
                node_update = updated;
            }
			// wait解放：sync終了まで排他するため
			notify();
		}
	}

    /**
     * ノードsync
     * @param node
     */
    void asyncNode(EntityNode node) {
        TxtList.debug("entry async");
        final Thread thread = new Thread(() -> syncSimple(node));
        service.submit(thread);
    }

    void setService(Thread th) {
        service.submit(th);
    }

	/**
	 * ストレージのUserIDを取得する
	 * @return
	 */
	public String getUserID() {
		try {
			return storage.getUserId();
		} catch (Exception e) {
			errPrint("Error: " + e.getLocalizedMessage());
		}

		return null;
	}

	/**
	 * アプリルートフォルダノードの取得
	 * ストレージルート直下のアプリ用ルートフォルダノードを返す
	 * なければ作成する
	 * @return アプリルートフォルダノード
	 */
	public EntityNode getAplRootFolderNode() {
        if (aplRootFolderNode != null) {
            // 取得済
            return aplRootFolderNode.node.asEntity();
        }

        try {
            EntityNode storage_root = plr.getRootNode(storage);
            if (storage_root == null) {
                // まだストレージの準備ができていない。
                return null;
            }
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
            return null;
        }

        TxtList.debug("PlrAct:getAplRootFolderNode: sync");
        // ストレージルート直下のCapraIbexフォルダノードを探す
        // 既存フォルダがあれば、それを返す。
        // 無い場合は新規作成するが、作成失敗時、nullを返す。
        while (true) {
            sync(null);
            list(null, null);
            for (NodeInfo ni : propList) {
                if (ni.name.equals(AppProperty.CAPRAIBEX_ROOT_FOLDER)) {
                    aplRootFolderNode = new NodeInfo<>(ni.node, ni.name);
                    return aplRootFolderNode.node.asEntity();
                }
            }

            EntityNode nwnode = createFolderNode(AppProperty.CAPRAIBEX_ROOT_FOLDER, "GraphEditorRoot");
            if (nwnode != null) {
                // 再検索するためループを構成
                sync(nwnode);
                continue;
            }

            break;
        }

        return null;
    }


    /**
     * アプリルートフォルダノードの取得
     * @param root
     * @return
     */
    private EntityNode getAplRootFolderNode(EntityNode root) {
        TxtList.debug("PlrAct:getAplRootFolderNode with root: sync");

        sync(root);
        list(null, root);
        for (NodeInfo ni : propList) {
            if (ni.name.equals(AppProperty.CAPRAIBEX_ROOT_FOLDER)) {
                return ni.node.asEntity();
            }
        }

        return null;
    }

	/**
     * アプリルートフォルダノードの取得
     * フレンドのグラフを開くことを考慮し、uriからルートノードを取得し、
     * そのルートノードのアプリルートフォルダノードを探して返す
     * @return アプリルートフォルダノード
     */
    public EntityNode getAplRootFolderNode2(EntityNode node) {
        EntityNode root = getRootNode(node);
        if (root == null) {
            return null;
        }

        return getAplRootFolderNode(root);
    }

	/**
	 *  ノードのプロパティを集める
	 *	nodegがnullの場合、ストレージノードのプロパティを集める
	 */
	public synchronized void list(List<NodeInfo<Node>> list, EntityNode node) {
		if (list == null) {
			list = propList;
		}

        if (node == null) {
			if (storageRootNode == null) {
				return;
			}

			node = storageRootNode.node;
		}

		list.clear();

		for (String name : node.propertyNames()) {
			for (Node n : node.getProperty(name)) {
				list.add(new NodeInfo<>(n, name));
			}
		}
    }


    /**
     *  ノードのプロパティを集める
     *	nodegがnullの場合、ストレージノードのプロパティを集める
     */
    public synchronized Map<String, Node> listToMap(EntityNode node) {
        Map<String,Node> map = new HashMap<>();

        if (node == null) {
            if (storageRootNode == null) {
                return map;
            }

            node = storageRootNode.node;
        }

        for (String name : node.propertyNames()) {
            for (Node n : node.getProperty(name)) {
                map.put(name, n);
            }
        }

        return map;
    }

    /**
	 * ノードのリテラルを取得
	 * ノードの指定が無い場合、ストレージノードのリテラルを取得
	 * @return
	 */
	public synchronized Map<String,String> getliterals(EntityNode node) {
		Map<String, String> map = new HashMap<>();

		if (node == null) {
			if (storageRootNode == null) {
				return map;
			}

			node = storageRootNode.node;
		}

		if (node.inSync()) {
			// 同期中
			return map;
		}

		propList.clear();

		// プロパティ値の一時格納場所
		for (String name : node.propertyNames()) {
			for (Node n : node.getProperty(name)) {
				propList.add(new NodeInfo<>(n, name));

				if (n.isLiteral()) {
					LiteralNode ln = n.asLiteral();
					String val = String.valueOf(ln.getValue("ja"));
					if (val.length() <= 0) {
						val = String.valueOf(ln.getValue());
					}

					if (val != null && !val.equals("null")) {
						map.put(name, val);
					}
				}
			}
		}

		return map;
	}

    /**
     * プロパティの存在確認
     * 直前に取得したプロパティリストから探す
     * @param name プロパティ名
     * @return
     */
	public synchronized Boolean getproperty(String name) {
        NodeInfo nodeinfo = getProperty(name);

        if (nodeinfo == null) {
            return false;
        }

        return true;
    }

    /**
     * プロパティの存在確認
     * nodeがnullの場合、直前に取得したプロパティリストから探す
     * @param name プロパティ名
     * @return
     */
    public synchronized Boolean getproperty(EntityNode node, String name) {
        if (node != null) {
            list(this.propList, node);
        }
        return getproperty(name);
    }

    /**
	 *  フォルダノード生成
     *  ストレージルート直下に生成する
	 * @param name ノード名前
	 * @param type ノードタイプ
	 */
	private EntityNode createFolderNode(String name, String type) {
		EntityNode newNode;

		try {
			boolean folder = true, encrypt = true, inhelitPermissions = true;
			newNode = newEntityOuter(name, type, folder, encrypt, inhelitPermissions);
            setLiteral(newNode, AppProperty.LT_NAME, "*", name);
		} catch (Exception e) {
			errPrint("Error: " + e.getLocalizedMessage());
			return null;
		}

		return newNode;
	}

    /**
     *  ファイルノード生成
     * 　親ノードの配下に作成
     * @param ptnode 親ノード
     * @param name ノード名
     * @param type ノードタイプ
     * @return
     */
    public EntityNode createFileNode(EntityNode ptnode, String name, String type) {
        EntityNode newNode;

        try {
            boolean folder = false, encrypt = true, inhelitPermissions = true;
            newNode = ptnode.newEntity(name, type, folder, encrypt, inhelitPermissions);
            setLiteral(newNode, AppProperty.LT_NAME, "*", name);
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
            return null;
        }

        return newNode;
    }

	/**
	 *  内部ノード生成
	 * @param name
	 * @param type
	 */
	public EntityNode createInnerNode(EntityNode ptnode, String name, String type) {
		EntityNode newNode;
		boolean encrypt = true;

		try {
			newNode = ptnode.newInnerEntity(name, type, encrypt);
		} catch (Exception e) {
			errPrint("Error: " + e.getLocalizedMessage());
			return null;
		}

		return newNode;
	}

	/**
	 * プロパティの設定
	 * もし指定名プロパティがなければ作成する。
	 * @param name
	 * @param lang
	 * @param val
	 */
	public void setLiteral(EntityNode node, String name, String lang, String val) {
		if (getproperty(node, name)) {
            // すでに存在する
			String cmnd[] = {name, "set", lang, val};
			replace(name + " set " + lang + " " + val, cmnd);
		}
		else {
			try {
				LiteralNode lt_node = node.newLiteral(name);
				setLiteralNodeValue(lt_node, lang, val);
			} catch (Exception e) {
				errPrint("Error: " + e.getLocalizedMessage());
			}
		}
	}

    /**
     * プロパティ値の更新
     * @param commandLine
     * @param commands
     */
    private void replace(String commandLine, String[] commands) {
        try {
            NodeInfo nodeinfo = getProperty(commands[0]);
            if (nodeinfo != null) {
                nodeCommand(commandLine, commands, nodeinfo);
            }
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
        }
    }

    private void nodeCommand(String commandLine, String[] commands, NodeInfo nodeInfo) throws Exception {
        if (nodeInfo.node.isLiteral()) {
            literalNodeCommandI(commandLine, commands, nodeInfo.node.asLiteral(),0, "value", true, true);
        }
    }

    /**
     * 外部実体ノードの作成
     * ストレージノード直下に作成する
     * @param propName ノード名
     * @param type Ontology Class名
     * @param folder true:フォルダノード false:ファイルノード
     * @param encrypt 暗号化の有無(true/false)
     * @param inhelitPermissions
     * @return ノードオブジェクト
     * @throws Exception
     */
	private EntityNode newEntityOuter(String propName, String type, boolean folder, boolean encrypt, boolean inhelitPermissions) throws Exception {
		if (storageRootNode == null) {
			return null;
		}

		return storageRootNode.node.newEntity(propName, type, folder, encrypt, inhelitPermissions);
	}

	private void literalNodeCommandI(String commandLine, String[] commands, LiteralNode node, int offset, String valueName, boolean enableTypeCommand, boolean showMessage) throws NodeNotFoundException {
		switch (commands[1 + offset].charAt(0)) {
		case 't':
			if (!enableTypeCommand) {
				return;
			}

			String type;
			if (commands.length == (2 + offset)) {
				type = null;
			} else if (commands.length > (2 + offset)) {
				type = commands[2 + offset];
			} else {
				return;
			}

			setliteralNodeType(node, type);

			break;

		case 's':
			if (checkCommand(commands, 3 + offset)) {
				String lang = commands[2 + offset];
				if (LANG_NONE_SYMBOL.equals(lang)) {
                    lang = LiteralNode.LANG_NONE;
                }
				Object value; {
					String valueStr; {
						String[] s = commandLine.split("\\s+", 4 + offset);
						if (s.length < (4 + offset)) {
                            valueStr = null;
                        } else {
						    valueStr = s[3 + offset];
                        }
					}

					if (valueStr != null) {
						try {
							value = Integer.valueOf(valueStr);
						} catch (NumberFormatException e) {
							try {
								value = Float.valueOf(valueStr);
							} catch (NumberFormatException e2) {
								String lowerValueStr = valueStr.toLowerCase();
								if ("true".equals(lowerValueStr)) {
                                    value = Boolean.TRUE;
                                } else if ("false".equals(lowerValueStr)) {
                                    value = Boolean.FALSE;
                                } else {
								    value = valueStr;
                                }
							}
						}
					} else value = null;
				}
				if (showMessage) {
					outPrint(
						((value != null) ? "Set" : "Remove") + " " +
						(LiteralNode.LANG_NONE.equals(lang) ? "" : lang + " ") + valueName +
						((value != null) ?
						 " to " + value + " [" + value.getClass().getSimpleName() + "]" :
						 "") + ".");
				}

				if (value != null) {
                    node.setValue(lang, value);
                } else if (!node.removeValue(lang)) {
                    outPrint("LANG " + lang + " not found.");
                }

				break;
			}

			default:
		}
	}

	private boolean checkCommand(String[] commands, int num) {
	    if (commands.length < num) {
	        return false;
        }

        return true;
    }

    private void setliteralNodeType(LiteralNode node, String type) throws NodeNotFoundException {
		if (type == null) {
			node.removeType();
		} else {
			node.setType(type);
		}
	}

	private void setLiteralNodeValue(LiteralNode node, String lang, String valueStr) throws NodeNotFoundException {
		if (LANG_NONE_SYMBOL.equals(lang)) {
			lang = LiteralNode.LANG_NONE;
        }

		Object value = null;

		if (valueStr != null) {
			try {
				value = Integer.valueOf(valueStr);
			} catch (NumberFormatException e) {
				try {
					value = Float.valueOf(valueStr);
				} catch (NumberFormatException e2) {
					String lowerValueStr = valueStr.toLowerCase();
					if ("true".equals(lowerValueStr)) {
						value = Boolean.TRUE;
					} else if ("false".equals(lowerValueStr)) {
						value = Boolean.FALSE;
					} else {
						value = valueStr;
					}
				}
			}
		}

		if (value != null) {
			node.setValue(lang, value);
		} else if (!node.removeValue(lang)) {
			outPrint("LANG " + lang + " not found.");
		}
	}

	private String getNodeClass(Node node) {
		if (node.isEntity()) {
			return "Entity";
		}

		if (node.isLiteral()) {
			return "Literal";
		}

		if (node.isFile()) {
			return "File";
		}

		return "Unknown";
	}

	/**
	 * プロパティリストから指定のプロパティノードを返す
	 * 事前にlist()でプロパティリストを作成しておく
     * @param propIndexStr
	 * @return
	 */
	private NodeInfo getProperty(String propIndexStr) {
		NodeInfo nodeInfo = null;

		try {
		    for (NodeInfo ni : propList) {
		        if (propIndexStr.equals(ni.name)) {
                    nodeInfo = ni;
		            break;
                }
            }
		} catch (NumberFormatException e) {
			errPrint("PROP-INDEX must be an integer: " + propIndexStr);
			return null;
		} catch (IndexOutOfBoundsException e) {
			errPrint("PROP-INDEX not found: " + propIndexStr);
			return null;
		}

		if (nodeInfo == null) {
			errPrint("Property " + propIndexStr + " was removed.");
			return null;
		}
		return nodeInfo;
	}

	private abstract class HandlerBase {
		public synchronized void onError(EntityNode node, PlrEntry entry, Exception exception, Origin origin) {
			onError(entry, exception, origin);
		}

		public synchronized void onError(PlrEntry entry, Exception exception, Origin origin) {
			String id;
			if (entry != null) {
				if ((id = entry.getId()) == null) {
					id = "unset entry";
				} else {
					id = null;
				}

				errPrint("Error" + ((origin != null) ? " from " + origin : "") + ((id != null) ? " on " + id : "") + ":");
				exception.printStackTrace();
			}
		}
	}

	private void errPrint(String str) {
        if (err_print) {
            TxtList.set(str);
        }
    }

    private void outPrint(String str) {
        if (out_print) {
			TxtList.set(str);
		}
    }


	/**
	 *　フォルダルートのノードリストを取得する
	 * @return
	 */
	public List<NodeName> getRootNodeList() {
		List<NodeName> list = new ArrayList<>();

		for (NodeInfo ni : propList) {
			if (ni.node.isEntity()) {
				EntityNode en = ni.node.asEntity();
				NodeName nn = new NodeName(ni.name, ni.node.getId(), en.getURI().toString(), en);
				list.add(nn);
			}
		}

		return list;
	}

	/**
	 * uriからストレージのrootノードを取得する
	 * @param uri
	 * @return
	 */
	public EntityNode getRootNode(EntityNode node) {
		try {
			// 指定uriから取得したノードのストレージから、ストレージルートノードを取得する。
			Storage strg = node.getStorage();
			return plr.getRootNode(strg);
		} catch (Exception e) {
			errPrint("Error: " + e.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * Node 名前を取得
	 * @param node
	 * @return
	 */
	public String getName(EntityNode node) {
		if (node == null) {
			return null;
		}

		try {
			return node.getReferredPropertyName();
		} catch (Exception e) {
			errPrint("Error: " + e.getLocalizedMessage());
		}

		return null;
	}

	/**
	 * Node IDを取得
	 * @param node
	 * @return
	 */
	public String getId(EntityNode node) {
		if (node == null) {
			return null;
		}
		return node.getNodeId();
	}

    /**
     * Node URIを取得
     * @param node
     * @return
     */
    public String getUriStr(EntityNode node) {
        if (node == null) {
            return null;
        }
        return node.getURI().toString();
    }

    /**
     * 指定Nodeから属性を削除
     * @param node 親ノード
     * @param name 属性名
     */
    public void removeProperty(EntityNode root, Node node, String name) {
        try {
            root.removeProperty(name, node);
        } catch (Exception e) {
            errPrint("removeProperty:" + e.toString());
        }
    }

	/**
	 * 指定Nodeから属性を削除
	 * @param name 属性名
	 */
	public void removeProperty(EntityNode root, String name) {
		try {
			root.removeProperty(name);
		} catch (Exception e) {
			errPrint("removeProperty:" + e.toString());
		}
	}

	/**
	 * パスフレーズ設定済みか？
	 * @return 設定済み:true
	 */
	public boolean passphrase() {

    	return passphrase;
	}


    /**
     * フレンドリストの取得
     * @return フレンドのリスト
     */
    public List<Friend> getFriends() {
        if (storage != null) {
            try {
            	System.out.println("link with friend setup");
                Root root = plr.getRoot(storage);
                List<Friend> friends = root.listFriends();
                return friends;
            } catch (Exception e) {
                errPrint("Error: " + e.getLocalizedMessage());
            }
        }

        return null;
    }


    /**
     * フレンド公開用情報のセットアップ
     */
    public void setpupFriends() {
        // フレンド公開用情報のセットアップ
        frientToMe f2m = new frientToMe();
        f2m.start();
        System.out.println("connecting with cloud");
    }

    /**
	 * フレンド公開用情報のセットアップスレッド
	 */
	class frientToMe extends Thread {
    	public void run() {
			shareAplRootFolderToFriend();
		}
	}

    /**
     * フレンドへ公開する属性の設定を確認、必要に応じて新規に値をセットする
     * わたしからあなたへ。。。
     */
    private void shareAplRootFolderToFriend() {
        List<Friend> friends = getFriends();
        if (friends == null) {
            return;
        }

        List<String> friend_ids = new ArrayList<>();
        for (Friend friend : friends) {
            friend_ids.add(friend.getUserId());
        }

        EntityNode my_folder = getAplRootFolderNode();

        // MeToFriendノードの実体は１つ、最初に見つけたものにノードリンクとshare設定を行う
        for (Friend friend : friends) {
            try {
                // 情報公開用アプリフォルダノードを取得し、現在の値を書き込む
                List<EntityNode> m2f = getMeToFriendNode(friend, my_folder);
                if (m2f != null && m2f.size() > 0) {
                    EntityNode me_to_friend = m2f.get(0);

                    if (m2f.size() == 1) {
                        // LinkedNodeなし
                        Node new_node = me_to_friend.addProperty(AppProperty.CAPRAIBEX_ME_TO_FRIEND_PROPRTY, my_folder);
                        new_node.asEntity().share(false, friend_ids);
                    } else {
                        // 既存LinkedNodeへのshare設定
                        m2f.get(1).share(false, friend_ids);
                    }

                    sync(me_to_friend);
                }
            } catch (Exception e) {
                errPrint("Error: " + e.getLocalizedMessage());
            }
        }

        TxtList.set("Complete Me to Friend setup.");
    }

    /**
     * フレンドへ公開する属性の設定の確認
     * @param friend フレンド
     * @param my_folder 自アプリフォルダノード
     * @return 0:me to friend Node, 1:linkedNode
     */
      private List<EntityNode> getMeToFriendNode(Friend friend, EntityNode my_folder) {
        List<EntityNode> rt = new ArrayList<>();

        try {
            EntityNode me_to_friend = friend.getMeToFriendRootNode();
            if (me_to_friend == null) {
                return null;
            }

            rt.add(me_to_friend); // rt:0

            // 既存の同名属性をチェック
            sync(me_to_friend);
            for (Node n : me_to_friend.getProperty(AppProperty.CAPRAIBEX_ME_TO_FRIEND_PROPRTY)) {
                if (n.asEntity().getURI().equals(my_folder.getURI())) {
                    rt.add(n.asEntity());
                    // 同じノードがセットされている
                    return rt;  // rt.size() > 1
                }
                // 一旦属性を削除
                me_to_friend.removeProperty(AppProperty.CAPRAIBEX_ME_TO_FRIEND_PROPRTY);
            }

            return rt;
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
        }

        return null;
    }


    /**
     * フレンドのアプリフォルダノード直下のノードリストを取得する
     * @param friend フレンド
     * @return
     */
    public List<NodeName> getFriendRootNodeList(Friend friend) {
        try {
            EntityNode friend_root = friend.getFriendToMeRootNode();
            if (friend_root == null) {
                return null;
            }

            sync(friend_root);

            for (Node n : friend_root.getProperty(AppProperty.CAPRAIBEX_ME_TO_FRIEND_PROPRTY)) {
                EntityNode node = n.asEntity();
                sync(node);
                list(null, node);
                return getRootNodeList();
            }
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
        }

        return null;
    }

	/**
	 * ノードへのリンクをプロパティとして設定する
	 * もし指定名プロパティがなければ作成する。
      * @param node リンクを追加するノード
     * @param name　プロパティ名
     * @param target_node　リンク先ノード
     */
    public void setLinkedNode(EntityNode node, String name, EntityNode target_node) {
        try {
            for (Node n : node.getProperty(name)) {
                node.removeProperty(name);
                break;
            }

            node.addProperty(name, target_node);
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
		}
	}

    /**
     * プロパティとして保存したノードへのリンクを取得する
     * @param node ノード
     * @param name　プロパティ名
     * @return リンク先ノード
     */
    public EntityNode getLinkedNode(EntityNode node, String name) {
        try {
            for (Node n : node.getProperty(name)) {
                return n.asEntity();
            }
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
        }

        return null;
    }

	/**
	 * ノード内のリテラルnameを取得
	 * @param node
	 * @return
	 */
	public String getNodeName(EntityNode node) {
		try {
			//sync(node);
			Map<String, Node> properties;
			properties = listToMap(node);
			if (properties.containsKey(AppProperty.LT_NAME)) {
				String str = properties.get(AppProperty.LT_NAME).asLiteral().getValue().toString();
				return str;
			}
		} catch (Exception e) {
			errPrint("Error: " + e.getLocalizedMessage());
		}

		return null;
	}

    /**
      * 共有設定: 基本の共有設定（Read/Write、Anybody)
      * @param node
     */
	public void share(EntityNode node) {
	    try {
            node.share(false, (String)null);
        } catch (Exception e) {
            errPrint("Error: " + e.getLocalizedMessage());
        }
    }

}
