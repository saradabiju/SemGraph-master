package com.assemblogue.plr.app.generic.semgraph;


import java.net.URL;
import java.util.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


/**
 * オントロジー読み込みウィンドウのコントローラクラスです。
 *
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class OtldrController implements Initializable {
    @FXML private Button ontRefButton;
    @FXML private Button otldrCansel;
    @FXML private TextField ontology_uri;

    private Stage stage;

    @Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeUI();
    }

	private void initializeUI() {
        ontRefButton.setOnAction(mouseEvent -> exec_selectFile());
        otldrCansel.setOnAction(mouseEvent -> exec_cansel());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void exec_selectFile() {

    }

    private void exec_cansel() {
        this.stage.close();
    }
}
