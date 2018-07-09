package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.lib.EntityNode;
import com.assemblogue.plr.lib.PLR;
import com.assemblogue.plr.io.PassphrasePoster;
import com.assemblogue.plr.io.Storage;
import com.assemblogue.plr.io.StorageInfo;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * アプリケーションのコントローラクラスです。
 *
 * @author <a href="mailto:m.ikemoto@runzan.co.jp">IKEMOTO, Masahiro</a>
 * @modified <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 * @see Initializable
 */
public class AppController implements Initializable {
	// JavaFx
	@FXML private MenuItem menuItemRefresh;
	@FXML private MenuItem menuItemAcknowlegements;
	@FXML private MenuItem menuItemCloudSetting;
	@FXML private MenuItem menuItemGraphCreate;
	@FXML private MenuItem menuItemGraphOpen;
	@FXML private MenuItem menuItemNodeSearch;
	@FXML private LogTextArea logView;
	private Stage stage;

	// PLR
	private static PLR plr;
    private static Storage storage = null;
    // PLR Lapper
	public static PlrActor plrAct = new PlrActor();

	// 既存グラフを開くダイアログは１つだけ
    OpenGraphController og_ctrlr = null;

	// 検索ダイアログは1つだけ
    Stage sn_stage = null;
    SearchNodeController sn_ctrlr = null;
    GraphController graphController;

	void setStage(Stage stage) {
        this.stage = stage;
        stage.showingProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                if (og_ctrlr != null)  {
                    og_ctrlr.close();
                }
                if (sn_stage != null) {
                    sn_stage.close();
                }
            }
        }));
	}

    @Override
	public void initialize(URL location, ResourceBundle resources) {
		GraphManager.setAppController(this);
		System.out.println("first window");
		initializeUI();
        initializePLR();
    }

	private void initializeUI() {
		menuItemCloudSetting.setOnAction(mouseEvent -> exec_cloudSetting());
		menuItemGraphCreate.setOnAction(mouseEvent -> exec_createGraph());
		//menuItemGraphCreate.setOnAction(mouseEvent -> exec_newGraph());
		menuItemGraphOpen.setOnAction(mouseEvent -> exec_openGraph(true));
		menuItemNodeSearch.setOnAction(mouseEvent -> exec_searchNode());
		menuItemAcknowlegements.setOnAction(mouseEvent -> exec_acknowlegements());

		logView.setWrapText(true);

        // ストレージセットアップ督促メッセージ
        TxtList.set(Messages.getString("system.plr.setup"));
    }

	private void initializePLR() {
		PLR.createBuilder().build(new PLR.BuilderCallback() {
			@Override
			public void onReady(PLR plr) {
				System.out.println("intializePLR function on call");
				AppController.plr = plr;
				plrAct.setPlr(plr);
				initializeStorage();
			}

			@Override
			public void onError(Exception e) {

				AppController.this.onError(e);
			}
		});
	}

	private void initializeStorage() {
		List<StorageInfo> storageList = getStorageList();
		if (storageList.isEmpty()) {
			return;
		}

		plr.connectStorage(storageList.get(storageList.size() - 1), new StorageHandler());
	}

	private List<StorageInfo> getStorageList() {
		try {
			return plr.listStorages();
		} catch (Exception e) {
			onError(e);
			return Collections.emptyList();
		}
	}

	void setStorage(Storage storage) {
        stage.showingProperty();

		if ((this.storage = storage) == null) {
			return;
		}

        plrAct.setStorage(storage);

        // ルートノード同期
		initial_sync();
	}

	Storage getStorage() {
		return storage;
	}

	void destroyPLR() {
		if (plr != null) {
			plr.destroy();
		}
	}


    /**
     * 初期同期
     */
	private void initial_sync() {
		// ルートノード同期
		plrAct.preprocess();
		plrAct.sync(null);
		//plrAct.list(null, null);
	}

    /**
     * 定期同期処理
     */
    public void execserver() {
        sync();
    }

    /**
     * グラフノード同期とプロパティ取得
     * @return true: PLR準備OK
     */
    private boolean syncGraphNode() {
        plrAct.sync(null);
        plrAct.list(null,null);

        if (!plrAct.passphrase()) {
            return false;
        }

        return true;
    }

	/**
	 * メニュー：グラフ作成ダイアログを開く
	 */
	private void exec_createGraph() {
        if (!syncGraphNode()) {
            return;
        }

		Stage nwstg = new Stage();
		nwstg.initModality(Modality.APPLICATION_MODAL);
		nwstg.initOwner(this.stage);
		nwstg.setTitle(Messages.getString("menuItem.graph.create"));
		 nwstg.getIcons().add(new Image(App.class.getResourceAsStream("editor.jpg")));

		FXMLLoader loader =
				new FXMLLoader(getClass().getResource("CreateGraphController.fxml"), Messages.getResources());

		Parent root; try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		CreateGraphController ctrl;
		ctrl = loader.getController();
		ctrl.setStage(nwstg);
		ctrl.setOwnerStage(this.stage);
		ctrl.setAppController(this);

		Scene scene = new Scene(root, 512, 100);

		nwstg.setScene(scene);
		nwstg.show();
	}


	/**
	 * メニュー：グラフオープンダイアログを開く
     * ダイアログを開かずに、直接グラフ一覧を表示するように修正
     * 将来、自グラフ一覧、開示グラフ一覧、フレンドグラフ一覧のダイアログと
     * する際、fxmlでレイアウトを作ると思うので、fxml処理は残しておく
	 */
	synchronized public void exec_openGraph(boolean sync_flag) {
        if (!syncGraphNode()) {
            return;
        }

        if (og_ctrlr != null) {
            og_ctrlr.toFront();
            return;
        }

        // ダイアログを開く
        Stage nwstg = new Stage();
		nwstg.initModality(Modality.APPLICATION_MODAL);
		nwstg.initOwner(this.stage);
		nwstg.setTitle(Messages.getString("menuItem.graph.open"));
System.out.println("Executing open graph");
		FXMLLoader loader =
				new FXMLLoader(getClass().getResource("OpenGraphController.fxml"), Messages.getResources());

		Parent root; try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		OpenGraphController ctrl;
		ctrl = loader.getController();
		ctrl.setStage(nwstg);
		//ctrl.setOwnerStage(this.stage);
		ctrl.setAppController(this);
/*
		Scene scene = new Scene(root, 512, 100);
		nwstg.setScene(scene);
		nwstg.show();
*/
        // ダイアログを経由せずに、グラフ一覧を表示する
		og_ctrlr = ctrl;
        ctrl.openSelector(sync_flag);
	}

    /**
     * ダイアログを経由せずにグラフ一覧を表示する場合、
     * グラフ一覧を閉じる時のフラグ操作
     */
    public void close_openGraph() {
        og_ctrlr = null;
    }


	/**
	 * メニュー：ノード検索ダイアログを開く
	 */
	synchronized public void exec_searchNode() {
        if (plrAct.getAplRootFolderNode() == null) {
            return;
        }

        if (!syncGraphNode()) {
            return;
        }

		if (sn_ctrlr != null) {
            sn_ctrlr.toFront();
        	return;
		}

        if (sn_stage != null) {
            return;
        }

        sn_stage  = new Stage();
        //nwstg.initOwner(this.stage);
        System.out.println("Node search Function");
        sn_stage.setTitle(Messages.getString("menuItem.node.search"));

		FXMLLoader loader =
				new FXMLLoader(getClass().getResource("SearchNodeController.fxml"), Messages.getResources());

		Parent root; try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
            sn_stage = null;
			return;
		}

		SearchNodeController ctrl;
		ctrl = loader.getController();
		ctrl.setStage(sn_stage);

		Scene scene = new Scene(root, 512, 150);
        sn_stage.setScene(scene);
        System.out.println("loading search node function window");
        sn_stage.showingProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue == true && newValue == false) {
                sn_ctrlr = null;
                sn_stage = null;
            }
        }));

        sn_ctrlr = ctrl;
        sn_stage.show();
	}

	/**
	 * メニュー：グラフ作成
	 */
	public void createGraph(String name) {
		System.out.println("create graph function");
	    open_graph( name, null, stage);
	}


	/**
	 * メニュー：グラフ開く
	 */
	public void openGraph(EntityNode node) {
		System.out.println("open graph");
		open_graph(null, node,stage);
	}

/**	public void createNewGraph(String graphname) {

		graphController = new GraphController();
		System.out.println("create new graph");
		graphController.createNewGraph(this.stage,graphname);

	}



	/**
	 * ノードペインを開く
	 * @param name ノード名
	 * @param node ノード
	 */

	private void open_graph(String name, EntityNode node,Stage ownerStage) {
		if (plrAct.getAplRootFolderNode() == null) {
			System.out.println("Root Folder");
			return;
		}

		GraphActor gact = new GraphActor(name, node);
		if (gact.getGraphNode() == null) {
			TxtList.set("Graph "+name+" create Failed.");
			return;
		}

		// 表示ペインの基礎は作る
		//NodePainController nwNodeCtrl = new NodePainController(this.stage, gact);
		// オープン中のグラフ
		if (GraphManager.isOpend(gact)) {
			GraphManager.toFront(gact);  // 最前面に
			return;
		}


		GraphManager.open(gact, new RootLayout(gact));
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root,640,480);

		scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
		ownerStage.setTitle(gact.getGraphName());
		ownerStage.setScene(scene);
		ownerStage.show();
		root.setCenter(new RootLayout(gact));
		// Create NodePainController
		//nwNodeCtrl.createEditArea(gact.getGraphName());
	//	Stage stg = new Stage();
	//	stg.show();
	}

    /**
     * 同期処理
     */
    private synchronized void sync() {
        if (storage == null) {
            return;
        }

        if (!plrAct.passphrase()) {
            // パスフレーズが設定された後の初回同期まで
            plrAct.sync(null);
            //plrAct.list(null, null);
        }

        logView.setMessage(TxtList.get());   // settext
        logView.appendText("");
    }

    /**
     * ストレージ設定
     */
	private void exec_cloudSetting() {
		ChoiceDialog<Storage.Type> dialog; {
			Collection<Storage.Type> typeList; try {
				typeList = plr.listStorageTypes();
				System.out.println("cloud setting");
			} catch (Exception e) {
				onError(e);
				return;
			}
			dialog = new ChoiceDialog<>(typeList.iterator().next(), typeList);
			dialog.initOwner(stage);
			dialog.setTitle(Messages.getString("dialog.cloudType.title"));
			dialog.setHeaderText(Messages.getString("dialog.cloudType.headerText"));
			dialog.setContentText(Messages.getString("dialog.cloudType.contentText"));
		}
		dialog.showAndWait().ifPresent(type -> plr.newStorage(type, new StorageHandler()));
	}

	void onError(Exception e) {
		Platform.runLater(() -> Utils.createErrorDialog(stage, e).show());
	}

	private class StorageHandler implements PLR.StorageConnectionCallback {
		@Override
		public void onKeyPairNotReady(PassphrasePoster passphrasePoster) {
			Platform.runLater(() -> {
				Dialog<String> dialog = new Dialog<>();
				dialog.initOwner(stage);
				dialog.setTitle(Messages.getString("dialog.passphrase.title"));
				dialog.setHeaderText(Messages.getString("dialog.passphrase.new.headerText"));
				dialog.initStyle(StageStyle.UTILITY);
				dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

				GridPane grid = new GridPane();
				System.out.println("new grid");
				grid.setHgap(10);
				grid.setVgap(10);
				grid.setPadding(new Insets(20, 150, 10, 10));

				PasswordField passphrase = new PasswordField();
				passphrase.setPrefColumnCount(32);
				passphrase.setPromptText(Messages.getString("dialog.passphrase.label"));
				PasswordField confirm = new PasswordField();
				confirm.setPrefColumnCount(32);
				confirm.setPromptText(Messages.getString("dialog.passphrase.confirm.label"));

				grid.add(new Label(Messages.getString("dialog.passphrase.label")), 0, 0);
				grid.add(passphrase, 1, 0);
				grid.add(new Label(Messages.getString("dialog.passphrase.confirm.label")), 0, 1);
				grid.add(confirm, 1, 1);

				Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
				applyButton.setDisable(true);

				passphrase.textProperty().addListener((observable, oldValue, newValue) -> {
					applyButton.setDisable(newValue.isEmpty() || !newValue.equals(confirm.getText()));
				});

				confirm.textProperty().addListener((observable, oldValue, newValue) -> {
					applyButton.setDisable(newValue.isEmpty() || !newValue.equals(passphrase.getText()));
				});

				dialog.getDialogPane().setContent(grid);

				Platform.runLater(() -> passphrase.requestFocus());

				dialog.setResultConverter(dialogButton -> {
					if (dialogButton == ButtonType.APPLY) {
						return passphrase.getText();
					}
					return null;
				});

				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					passphrasePoster.post(result.get());
				} else {
					passphrasePoster.cancel();
				}
			});
		}

		@Override
		public void onConnect(Storage storage) {
			System.out.println("storage on connect");
			Platform.runLater(() -> setStorage(storage));
		}

		@Override
		public void onCancel() {
			// Nothing to do.
		}

		@Override
		public void onError(Exception e) {
			AppController.this.onError(e);
		}
	}

    /**
     * メッセージエリアの文字数制限
     */
    class LimitListener implements ChangeListener<String> {
        int limit_counter;
        LimitListener(int limitCount) {
            limit_counter = limitCount;
        }
        public void changed(ObservableValue<? extends String> value, String oldVal, String newVal) {
            if (limit_counter < newVal.length()) {
                TextInputControl tic = (TextInputControl) ((ReadOnlyProperty)value).getBean();
                tic.setText(newVal.substring(0,limit_counter));
            }
        }
    }


	private void exec_acknowlegements() {
        Stage nwstg = new Stage();
        nwstg.initModality(Modality.APPLICATION_MODAL);
        nwstg.initOwner(this.stage);
        nwstg.setTitle(Messages.getString("menuItem.ackowlegements.text"));

        VBox vbox = new VBox();
        HBox hbox = new HBox();
        vbox.getChildren().add(hbox);

        Label label = new Label();
        //label.setFont(new Font(20d));
        label.setText(Messages.getString("ackowlegements.jackson"));
        hbox.getChildren().add(label);

        ScrollPane scl_pane = new ScrollPane();
        scl_pane.setContent(vbox);

        nwstg.setScene(new Scene(scl_pane));
        System.out.println("executing new pane");
        nwstg.show();
    }


}
