package com.assemblogue.plr.app.generic.semgraph;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * 国際化メッセージを扱うためのクラスです。
 *
 * @author <a href="mailto:m.ikemoto@runzan.co.jp">IKEMOTO, Masahiro</a>
 */
public class Messages {
	private static final ResourceBundle resources = ResourceBundle
		.getBundle(Messages.class.getPackage().getName() + ".app",
				com.assemblogue.plr.util.LocaleUtils.UTF8_ENCODING_CONTROL);

	/**
	 * リソースバンドルを取得します。
	 *
	 * @return リソースバンドル
	 */
	public static ResourceBundle getResources() {
		return resources;
	}

	/**
	 * メッセージを取得します。
	 *
	 * @param key メッセージのキー
	 * @param arguments フォーマット引数
	 * @return メッセージ
	 */
	public static String getString(String key, Object... arguments) {
		return MessageFormat.format(resources.getString(key), arguments);
	}
}
