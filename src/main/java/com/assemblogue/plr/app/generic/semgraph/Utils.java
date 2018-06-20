package com.assemblogue.plr.app.generic.semgraph;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.stage.Window;


/**
 * ユーティリティクラスです。
 *
 * @author <a href="mailto:m.ikemoto@runzan.co.jp">IKEMOTO, Masahiro</a>
 * @modified <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class Utils {
    static Rectangle2D d = Screen.getPrimary().getVisualBounds();

    /**
     * 生成するウィンドウのサイズの取得
     * 実行環境の画面サイズの7割のサイズ
     * @return
     */
    public static double getWindowHeight() {
        return d.getMaxY() * 0.7d;
    }
    public static double getWindowWidth() {
        return d.getMaxX() * 0.7d;
    }


	/**
	 * エラーダイアログを生成します。
	 *
	 * @param window 親ウィンドウ
	 * @param e 表示する例外
	 * @return エラーダイアログ
	 */
	public static Alert createErrorDialog(Window otherStage, Exception e) {
		Alert alert = new Alert(Alert.AlertType.ERROR); {
			alert.initOwner(otherStage);
			alert.initStyle(StageStyle.UTILITY);
			alert.setTitle(Messages.getString("alert.error.title"));
			alert.setHeaderText(Messages.getString("alert.error.headerText"));
			alert.setContentText(e.toString());
		}

		TextArea textArea; {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			textArea = new TextArea(sw.toString());
			textArea.setEditable(false);
			textArea.setWrapText(true);
			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);

			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);
		}

		GridPane expContent = new GridPane(); {
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(new Label(Messages.getString("alert.error.details")),
										 0, 0);
			expContent.add(textArea, 0, 1);
		}
		alert.getDialogPane().setExpandableContent(expContent);

		return alert;
	}

    /**
     * 現在日時文字列の取得
     * @return　現在日時文字列
     */
	public static String currentDateTime() {
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String datetime = sdf.format(now.getTime());
        return datetime;
	}

	/**
	 * 現在日時の取得（long epochtime）
	 * @return epochtime
	 */
	public static long currentEpochtime() {
		Date d = new Date();
		return d.getTime();
	}

	/**
	 * 実行中のクラス名とメソッド名を取得
	 * @return クラス名:メソッド名
	 */
	public static String getMethodName() {
		String classname = Thread.currentThread().getStackTrace()[2].getClassName();
		String methodname = Thread.currentThread().getStackTrace()[2].getMethodName();

		// スタックトレースから取得
		return classname+":"+methodname;
	}

	private Utils() {
	}
}
