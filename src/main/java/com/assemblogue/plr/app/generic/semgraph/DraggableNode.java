package com.assemblogue.plr.app.generic.semgraph;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import com.assemblogue.plr.contentsdata.ontology.OntologyItem;
import com.assemblogue.plr.lib.EntityNode;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class DraggableNode extends NodeCell {

	@FXML
	private AnchorPane root_pane;
	@FXML
	private AnchorPane left_link_handle;
	@FXML
	private AnchorPane right_link_handle;
	@FXML
	private TextArea title_bar;
	@FXML
	private Label close_button;
	@FXML
	private ChoiceBox<String> choicebox;
	@FXML
	private HBox attribute;
	private EventHandler<MouseEvent> mLinkHandleDragDetected;

	private EventHandler<DragEvent> mLinkHandleDragDropped;
	private EventHandler<DragEvent> mContextLinkDragOver;
	private EventHandler<DragEvent> mContextLinkDragDropped;

	private EventHandler<DragEvent> mContextDragOver;
	private EventHandler<DragEvent> mContextDragDropped;
	private GraphActor graphAct;
	private OntMenu ontmenu;

	private DragIconType mType = null;

	private Point2D mDragOffset = new Point2D(0.0, 0.0);

	private final DraggableNode self;

	private NodeLink mDragLink = null;
	private AnchorPane right_pane = null;
	private final List<String> mLinkIds = new ArrayList<String>();
	public static NodeCell parent;
	public static Label btn;
	public static EntityNode node;

	public DraggableNode(GraphActor gact) {

		super(parent, new Label("My Node"), node);
		this.graphAct = gact;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DraggableNode.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		self = this;

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

		buildNodeDragHandlers();
		buildLinkDragHandlers();

		left_link_handle.setOnDragDetected(mLinkHandleDragDetected);
		right_link_handle.setOnDragDetected(mLinkHandleDragDetected);

		left_link_handle.setOnDragDropped(mLinkHandleDragDropped);
		right_link_handle.setOnDragDropped(mLinkHandleDragDropped);
		// self.setOnMouseClicked(mContextonMouseClick);
		mDragLink = new NodeLink();
		mDragLink.setVisible(true);
		// attribute.setOnAction(mouseEvent -> addRelation(ontmenu));

		parentProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				right_pane = (AnchorPane) getParent();

				// choicebox.setItems(FXCollections.observableArrayList("付加的関係","正付加関係","負付加関係","正因果関係","負因果関係"));
			}

		});
		createOntMenu();
		Circle c = new Circle();
		c.setRadius(5.0f);
		// this.getChildren().add(c);
		Rectangle r = new Rectangle(20, 20, 25, 10);
		r.setArcHeight(15);
		r.setArcWidth(15);
		r.setStroke(Color.BLACK);
		// this.getChildren().add(r);
	}

	private void addRelation(OntMenu Stage) {
		System.out.println("Entered");

	}

	/**
	 * Method created to initialize the ontmenu
	 */

	private void createOntMenu() {
		System.out.println("Entered in crete menussss");
		String item_id = "#Entity";
		String omi_id = "#rel";
		String val = "";
		HBox relmenu = new HBox(10d);
		relmenu.setAlignment(Pos.CENTER_LEFT); // 縦中央、横左寄せ
		relmenu.setStyle(AppProperty.REL_LABEL_STYLE);
		// dragnode.ontMenu = new OntMenu(graphAct, graphAct.getOss().getNode(item_id));
		// List<OntMenu.OntMenuItem> ranged_omi = ndc.ontMenu.getRangedClassItem();
		System.out.println("graphAct" + graphAct);
		System.out.println("graphAct.getOss()" + graphAct.getOss());
		System.out.println("graphAct.getOss().getNode(item_id)" + graphAct.getOss().getNode(item_id));
		OntMenu ontMenu = new OntMenu(graphAct, graphAct.getOss().getNode(item_id));
		List<OntMenu.OntMenuItem> sub_omi_list = ontMenu.makeMenuList2(omi_id);

		for (OntMenu.OntMenuItem sub_omi : sub_omi_list) {
			// 設定済みの属性値(OntologyIte.label)をラベル名とする
			// fxLabelには、初期状態ではオントロジ属性名が入る
			if (val != null && !val.equals("")) {
				// child.dispRelStr = val;
				sub_omi.fxLabel.setText(val);
				sub_omi.fxLabel.setTextFill(Color.BLACK);
			} else {
				sub_omi.fxLabel.setTextFill(Color.GRAY);
			}
			System.out.println(" sub_omi.fxLabel" + sub_omi.fxLabel.getText());
			relmenu.getChildren().add(sub_omi.fxLabel);

			// 属性値が選択されたら、ラベル値を書き換えるので、ラベル値のリスナで選択された属性値を取得する
			sub_omi.fxLabel.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (newValue != null) {
						title_bar.setText(newValue);
						System.out.println("newValue" + newValue);
					}
				}
			});
		}

		attribute.getChildren().addAll(relmenu);

	}

	public void registerLink(String linkId) {
		mLinkIds.add(linkId);
	}

	public void relocateToPoint(Point2D p) {

		// relocates the object to a point that has been converted to
		// scene coordinates
		Point2D localCoords = getParent().sceneToLocal(p);

		relocate((int) (localCoords.getX() - mDragOffset.getX()), (int) (localCoords.getY() - mDragOffset.getY()));
	}

	public DragIconType getType() {
		return mType;
	}

	public void setType(DragIconType type) {

		mType = type;

		getStyleClass().clear();
		getStyleClass().add("dragicon");

		switch (mType) {

		case grey:
			getStyleClass().add("icon-grey");
			break;

		default:
			break;
		}
	}

	public void buildNodeDragHandlers() {

		mContextDragOver = new EventHandler<DragEvent>() {

			// dragover to handle node dragging in the right pane view
			@Override
			public void handle(DragEvent event) {

				event.acceptTransferModes(TransferMode.ANY);
				relocateToPoint(new Point2dSerial(event.getSceneX(), event.getSceneY()));

				event.consume();
			}
		};

		// dragdrop for node dragging
		mContextDragDropped = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);
				System.out.println("Drag dropped");

				event.setDropCompleted(true);

				event.consume();
			}
		};
		// close button click
		close_button.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				AnchorPane parent = (AnchorPane) self.getParent();
				parent.getChildren().remove(self);

				// iterate each link id connected to this node
				// find it's corresponding component in the right-hand
				// AnchorPane and delete it.

				// Note: other nodes connected to these links are not being
				// notified that the link has been removed.
				for (ListIterator<String> iterId = mLinkIds.listIterator(); iterId.hasNext();) {

					String id = iterId.next();

					for (ListIterator<Node> iterNode = parent.getChildren().listIterator(); iterNode.hasNext();) {

						Node node = iterNode.next();

						if (node.getId() == null)
							continue;

						if (node.getId().equals(id))
							iterNode.remove();
					}

					iterId.remove();
				}
			}

		});

		// drag detection for node dragging
		title_bar.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);

				getParent().setOnDragOver(mContextDragOver);
				getParent().setOnDragDropped(mContextDragDropped);

				// begin drag ops
				mDragOffset = new Point2D(event.getX(), event.getY());

				relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

				ClipboardContent content = new ClipboardContent();
				DragContainer container = new DragContainer();

				// container.addData ("type", mType.toString());
				content.put(DragContainer.AddNode, container);

				startDragAndDrop(TransferMode.ANY).setContent(content);

				event.consume();
			}

		});
	}

	private void buildLinkDragHandlers() {

		mLinkHandleDragDetected = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);

				getParent().setOnDragOver(mContextLinkDragOver);
				getParent().setOnDragDropped(mContextLinkDragDropped);

				// Set up user-draggable link
				right_pane.getChildren().add(0, mDragLink);

				mDragLink.setVisible(true);

				Point2D p = new Point2D(getLayoutX() + (getWidth() / 2.0), getLayoutY() + (getHeight() / 2.0));

				mDragLink.setStart(p);

				// Drag content code
				ClipboardContent content = new ClipboardContent();
				DragContainer container = new DragContainer();

				// pass the UUID of the source node for later lookup
				container.addData("source", getId());

				content.put(DragContainer.AddLink, container);

				startDragAndDrop(TransferMode.ANY).setContent(content);

				event.consume();
			}
		};

		mLinkHandleDragDropped = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);

				// get the drag data. If it's null, abort.
				// This isn't the drag event we're looking for.
				DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);

				if (container == null)
					return;

				// hide the draggable NodeLink and remove it from the right-hand AnchorPane's
				// children
				mDragLink.setVisible(true);
				right_pane.getChildren().remove(0);

				AnchorPane link_handle = (AnchorPane) event.getSource();

				ClipboardContent content = new ClipboardContent();

				// pass the UUID of the target node for later lookup
				container.addData("target", getId());

				content.put(DragContainer.AddLink, container);

				event.getDragboard().setContent(content);
				event.setDropCompleted(true);
				event.consume();
			}
		};

		mContextLinkDragOver = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				event.acceptTransferModes(TransferMode.ANY);

				// Relocate end of user-draggable link
				if (!mDragLink.isVisible())
					mDragLink.setVisible(true);

				mDragLink.setEnd(new Point2D(event.getX(), event.getY()));

				event.consume();

			}
		};

		// drop event for link creation
		mContextLinkDragDropped = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				System.out.println("context link drag dropped");

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);

				// hide the draggable NodeLink and remove it from the right-hand AnchorPane's
				// children
				mDragLink.setVisible(true);
				right_pane.getChildren().remove(0);
				mDragLink.setArrowDropped(new Point2D(event.getX(), event.getY()));
				event.setDropCompleted(true);
				event.consume();
			}

		};

	}

}
