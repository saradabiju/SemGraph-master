package com.assemblogue.plr.app.generic.semgraph;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GraphController {

	public void createNewGraph(Stage ownerStage,String graphName) {


   //    nwstg.initModality(Modality.APPLICATION_MODAL);
     //  nwstg.initOwner(ownerStage);
     //  nwstg.setTitle(graphName);
		BorderPane root = new BorderPane();

    	try {

			Scene scene = new Scene(root,640,480);

			scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
			ownerStage.setTitle(graphName);
			ownerStage.setScene(scene);
			ownerStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}

		root.setCenter(new RootLayout());


            //nwstg.show();



	}

}
