package com.assemblogue.plr.app.generic.semgraph;

import java.io.IOException;

import com.aquafx_project.AquaFx;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;


/**
 * アプリケーションのメインクラスです。
 *
 * @author <a href="mailto:m.ikemoto@runzan.co.jp">IKEMOTO, Masahiro</a>
 * @modified <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 * @see Application
 */
public class App extends Application {

	public static void main(String[] args) throws Exception {
		launch(args);

		System.exit(0);
	}

	private AppController appController;

	@Override
	public void start(Stage stage) throws Exception {
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("AppController.fxml"), Messages.getResources());

		Parent root;
		setUserAgentStylesheet(STYLESHEET_CASPIAN);

		// setUserAgentStylesheet(STYLESHEET_MODENA);
		try {

			root = loader.load();


		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		appController = loader.getController();
		appController.setStage(stage);

		Scene scene = new Scene(root, 480, 680);

		stage.setTitle(Messages.getString("application.title") + " " + AppProperty.VERSION);
		stage.getIcons().add(new Image(App.class.getResourceAsStream("editor.jpg")));
	
		stage.setScene(scene);

        // 自動同期
		ScheduledService<Boolean> ss = new ScheduledService<Boolean>() {
			@Override
			protected Task<Boolean> createTask() {

				Task<Boolean> task = new Task<Boolean>() {
					@Override
					protected Boolean call() throws Exception {
						Thread.sleep(500); // msec
                        stage.showingProperty();
						appController.execserver();
						return true;
					};
				};

				return task;
			}
		};

		ss.start();

        stage.show();
	}

	@Override
	public void stop() {
		if (appController != null) {
			appController.destroyPLR();
		}
	}
}
