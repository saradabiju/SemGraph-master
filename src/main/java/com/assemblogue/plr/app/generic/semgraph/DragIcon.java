package com.assemblogue.plr.app.generic.semgraph;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.CubicCurve;

public class DragIcon extends AnchorPane{

	@FXML AnchorPane root_pane;

	private DragIconType mType = null;

	public DragIcon() {

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("DragIcon.fxml")
				);

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();

		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}
	}

	@FXML
	private void initialize() {}

	public void relocateToPoint (Point2D p) {

		//relocates the object to a point that has been converted to
		//scene coordinates
		Point2D localCoords = getParent().sceneToLocal(p);

		relocate (
				(int) (localCoords.getX() - (getBoundsInLocal().getWidth() / 2)),
				(int) (localCoords.getY() - (getBoundsInLocal().getHeight() / 2))
			);
	}

	public DragIconType getType () { return mType; }

	public void setType (DragIconType type) {

		mType = type;

		getStyleClass().clear();
		getStyleClass().add("dragicon");

		//added because the cubic curve will persist into other icons
		if (this.getChildren().size() > 0)
			getChildren().clear();

		switch (mType) {

		case cubic_curve:



			Pane  pane = new Pane();

			pane.setPrefWidth(64.0);
			pane.setPrefHeight(64.0);
			//pane.getStyleClass().add("icon-blue");
			pane.setLayoutX(0.0);
			pane.setLayoutY(0.0);

			CubicCurve curve = new CubicCurve();

			curve.setStartX(10.0);
			curve.setStartY(20.0);
			curve.setEndX(54.0);
			curve.setEndY(44.0);
			curve.setControlX1(64.0);
			curve.setControlY1(20.0);
			curve.setControlX2(0.0);
			curve.setControlY2(44.0);
			curve.getStyleClass().add("cubic-icon");



			//r//oot_pane.
			getChildren().add(pane);
			addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
		        @Override public void handle(MouseEvent event) {
		        	pane.getChildren().add(curve);
		        	getStyleClass().add("icon-yellow");
		        }
		        });
		break;


		case grey:
			//getStyleClass().add("icon-black");
			addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
		        @Override public void handle(MouseEvent event) {

		       //      getStyleClass().add("icon-grey");
		        }
		        });
		break;

		default:
		break;
		}
	}
}