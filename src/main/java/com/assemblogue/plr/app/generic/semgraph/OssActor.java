package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.contentsdata.PLRContentsData;
import com.assemblogue.plr.contentsdata.misc.LocaleString;
import com.assemblogue.plr.contentsdata.ontology.OntologyItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OSS オントロジー、スタイルシートを読み込み、オブジェクトを生成します。
 * @author <a href="mailto:kaneko@cri-mw.co.jp">KANEKO, yukinori</a>
 */
public class OssActor  {
	PLRContentsData contentsData;

	OssActor() {
        String fname_ont_jsonld = "DiscourseGraph.jsonld";
        String fname_css_jsonld = "DiscourseGraph_stylesheet.jsonld";

	    try {
            // jarファイルと同じディレクトリにオントロジjsonldを置いておく
            String jarPath = System.getProperty("java.class.path");
            String dirPath = jarPath.substring(0, jarPath.lastIndexOf(File.separator)+1);
            String otjsonld = readJsonldFromDirOrClassPath(dirPath, fname_ont_jsonld);
            String ssjsonld = readJsonldFromDirOrClassPath(dirPath, fname_css_jsonld);
            contentsData = new PLRContentsData(otjsonld, ssjsonld);
        } catch (Exception e) {
            TxtList.set("OSS init error: " + e.getMessage());
        }
	}

    PLRContentsData getOss() {
	    return contentsData;
    }

    private String readJsonldFromDirOrClassPath(String dirPath, String filePath) throws IOException {
        String content;
        try {
            // カレントディレクトリにあるJSONLDをまず読んでみる
            content = readFileWithPath(dirPath + "./"+filePath);
        } catch (Exception e) {
            // 失敗した場合はクラスパスから読む
            InputStream s = getClass().getResourceAsStream(filePath);
            if (s == null) {
                throw new RuntimeException("resource " + filePath + " not found");
            }
            content = readInputStream(s);
        }
        return content;
    }

    /**
     * ファイルを読み込みStringとして取得する
     * @param path ファイルパス
     * @return 読み込んだファイル内容
     * @throws IOException
     */
    private static String readFileWithPath(final String path) throws IOException {
        return Files.lines(Paths.get(path), Charset.forName("UTF-8"))
                .collect(Collectors.joining(System.getProperty("line.separator")));
    }

    /**
     * 入力ストリームを読み込みStringとして取得する
     * @param inputStream 入力ストリーム
     * @return 読み込んだファイル内容
     * @throws IOException
     */
    private static String readInputStream(final InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")))
                .lines()
                .collect(Collectors.joining(System.getProperty("line.separator")));
    }

    /**
     * アイテムのラベルを取得する
     * 基本日本語を返す
     * @param item アイテム
     * @return ラベル
     */
    public String getLabel(OntologyItem item) {
        String str = LocaleString.getString(item.label, "ja");
        if (str == null) {
            str = LocaleString.getString(item.label, "en");
        }
        if (str == null) {
            str = LocaleString.getString(item.label, null);
        }

        return str;
    }

    /**
     * アイテムのラベルを取得する
     * 基本日本語を返す
     * @param item_id item ID
     * @return
     */
    public String getLabel(String item_id) {
        OntologyItem item = contentsData.getNode(item_id);
        if (item == null) {
            return null;
        }

        return getLabel(item);
    }

    /**
     * アイテムのラベルを全種取得する
     * 基本日本語を返す
     * @param item_id アイテムID
     * @return ラベル
     */
    public Map<String,String> getLabelAll(String item_id) {
        OntologyItem item = contentsData.getNode(item_id);
        if (item == null) {
            return null;
        }

        return getLabelAll(item);
    }

    /**
     * アイテムのラベルを全種取得する
     * 基本日本語を返す
     * @param item アイテム
     * @return ラベル
     */
    public Map<String,String> getLabelAll(OntologyItem item) {
        Map<String,String> map = new HashMap<>();
        List<String> keys = Arrays.asList("ja","en");

        for (String key: keys) {
            try {
                String str = LocaleString.getString(item.label, key);
                if (str == null) {
                    str = "";
                }
                map.put(key, str);
            } catch (Exception e) {
            }
        }

        return map;
    }
}

/* end of file */
