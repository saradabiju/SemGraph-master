package com.assemblogue.plr.app.generic.semgraph;

import java.io.IOException;
import java.util.UUID;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polygon;

public class NodeLink extends AnchorPane {

	@FXML
	CubicCurve node_link;
	@FXML
	Polygon arrow;

	private final DoubleProperty mControlOffsetX = new SimpleDoubleProperty();
	private final DoubleProperty mControlOffsetY = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionX1 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionY1 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionX2 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionY2 = new SimpleDoubleProperty();

	public NodeLink() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NodeLink.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();

		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		// provide a universally unique identifier for this object
		setId(UUID.randomUUID().toString());
	}

	@FXML
	private void initialize() {

		mControlOffsetX.set(100.0);
		mControlOffsetY.set(50.0);

		mControlDirectionX1.bind(
				new When(node_link.startXProperty().greaterThan(node_link.endXProperty())).then(-1.0).otherwise(1.0));

		mControlDirectionX2.bind(
				new When(node_link.startXProperty().greaterThan(node_link.endXProperty())).then(1.0).otherwise(-1.0));

		node_link.controlX1Property()
				.bind(Bindings.add(node_link.startXProperty(), mControlOffsetX.multiply(mControlDirectionX1)));

		node_link.controlX2Property()
				.bind(Bindings.add(node_link.endXProperty(), mControlOffsetX.multiply(mControlDirectionX2)));

		node_link.controlY1Property()
				.bind(Bindings.add(node_link.startYProperty(), mControlOffsetY.multiply(mControlDirectionY1)));

		node_link.controlY2Property()
				.bind(Bindings.add(node_link.endYProperty(), mControlOffsetY.multiply(mControlDirectionY2)));
	}

	public void setStart(Point2D startPoint) {

		node_link.setStartX(startPoint.getX());
		node_link.setStartY(startPoint.getY());
	}

	public void setEnd(Point2D endPoint) {

		node_link.setEndX(endPoint.getX());
		node_link.setEndY(endPoint.getY());

		arrow.setLayoutX(endPoint.getX());
		arrow.setLayoutY(endPoint.getY() + 10);
	}

	public void setArrowDropped(Point2D endPoint) {

		// node_link.setEndX(endPoint.getX());
		// node_link.setEndY(endPoint.getY());
		System.out.println("Reached in drop event");
		arrow.setLayoutX(endPoint.getX());
		arrow.setLayoutY(endPoint.getY());
	}

	public void bindEnds(DraggableNode source, DraggableNode target, Point2D endPoint) {
		node_link.startXProperty().bind(Bindings.add(source.layoutXProperty(), (source.getWidth() / 2.0)));

		node_link.startYProperty().bind(Bindings.add(source.layoutYProperty(), (source.getWidth() / 2.0)));

		node_link.endXProperty().bind(Bindings.add(target.layoutXProperty(), 0));

		node_link.endYProperty().bind(Bindings.add(target.layoutYProperty(), -10));
		/*
		 * node_link.endYProperty().bind(
		 * Bindings.add(target.layoutYProperty(),target.layoutYProperty()));
		 */

		System.out.println("Reached in drop bindends X" + endPoint.getX());
		System.out.println("Reached in drop bindends Y" + endPoint.getY());
		// arrow.setCenterX(endPoint.getX());
		// arrow.setCenterY(endPoint.getY());
		// arrow.centerXProperty().bind(Bindings.add(target.layoutXProperty(), 0));
		// arrow.centerYProperty().bind(Bindings.add(target.layoutYProperty(),
		// (target.getHeight() / 2.0)));
		// arrow.layoutXProperty().bind(Bindings.add(target.layoutXProperty(), 1));
		// arrow.layoutYProperty().bind(Bindings.add(target.layoutYProperty(),
		// (target.getHeight() / 2.0)));
		arrow.layoutXProperty().bindBidirectional((target.layoutXProperty()));
		arrow.layoutYProperty().bindBidirectional((target.layoutYProperty()));
		// arrow.setLayoutX(endPoint.getX()/2);
		// arrow.setLayoutY(endPoint.getY()/2);
		source.registerLink(getId());
		target.registerLink(getId());
		System.out.println("target" +getId());
	}

}
