package com.assemblogue.plr.app.generic.semgraph;
import java.util.ArrayList;
import java.util.List;

/**
 * 履歴
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class TxtList  {
	private static boolean debug = false;

	private static List<String> list = new ArrayList<>();
	private static int linelimiter = 100;

	private TxtList() {
		list.clear();
	}

	public static void set(String str) {
		if (list.size() > linelimiter) {
			list.remove(0);
		}

		list.add(str);
	}

	public static void debug(String str) {
		if (debug) {
			set(str);
		}
	}

	public static String get() {
		String str = null;

		for (String src : list) {
            if (str == null) {
                str = src;
                continue;
            }
            str = str + "\n" + src;
		}

		return str;
	}
}

/* end of file */
