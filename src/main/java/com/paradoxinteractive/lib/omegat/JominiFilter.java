package com.paradoxinteractive.lib.omegat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.LinebreakPreservingReader;

/*
 * 詳細は
 * https://ck3.paradoxwikis.com/Localization
 * 
 * l_english:
 *  evergreen_decision:0 "常緑を獲得"
 * 
 * 1行目が宣言になっている。先頭にUTF-8のBOMがつく
 * 
 * 2行目以降はインデント(空白/タブどちらでも可)
 * 前半がキー、後半がバリュー
 * 
 * キーについて
 * コロンを挟んでバージョントラックング用の数字がつく、大抵は0になっている、ない場合もある
 * (※YAMLパーサーだとこの数字が原因でパースエラーになる)
 * 
 * バリューについて
 * ""で囲まれている
 * 改行は\n
 * "自体のエスケープはなく"のまま、もしくは替わりに“が使用される
 * 
 * #以降はコメント
 * 可読性のため空行をあけることがある
 * 
 * インラインコメントには未対応
 * #にはコメント判定以外に強調など修飾子で使われることがある(例:#EMP Emphasis here #!)
 * コメント行でないと判定した場合はインラインコメントも原文とみなして処理しないことにした
 * そうしないと人為的に文法ミス(文末の"つけ忘れ)でエラーになってしまう
 */
public class JominiFilter extends AbstractFilter {

	public static void loadPlugins() {
		Core.registerFilterClass(JominiFilter.class);
	}

	public static void unloadPlugins() {
	}

	@Override
	public String getFileFormatName() {
		return "Jomini YML";
	}

	@Override
	public Instance[] getDefaultInstances() {
		return new Instance[] { new Instance("*.yml", "UTF-8", "UTF-8"), };
	}

	@Override
	public boolean isSourceEncodingVariable() {
		return false;
	}

	@Override
	public boolean isTargetEncodingVariable() {
		return false;
	}

	@Override
	protected boolean isFileSupported(BufferedReader reader) {
		// 常に許可
		return true;
	}

	@Override
	protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc)
			throws IOException, TranslationException {
		LinebreakPreservingReader lbpr = null;
		String line;

		try {
			lbpr = new LinebreakPreservingReader(inFile);

			while ((line = lbpr.readLine()) != null) {
				JominiItem item = new JominiItem();
				item.Parse(line);

				// 空白だけの行の場合はスキップ
				if (item.isEmpty()) {
					outFile.write(line + lbpr.getLinebreak());
					continue;
				}

				// コメント行の場合はスキップ
				if (item.getComment().length() != 0) {
					outFile.write(line + lbpr.getLinebreak());
					continue;
				}

				String key = item.getKey();
				String value = item.getValue();

				// キーがない場合はスキップ
				if (key.length() == 0) {
					outFile.write(line + lbpr.getLinebreak());
					continue;
				}

				// キーだけで値がない場合はスキップ
				if (value.length() == 0) {
					outFile.write(line + lbpr.getLinebreak());
					continue;
				}

				// 両端の"を外す
				char[] arr = value.toCharArray();
				int length = arr.length;
				boolean is_start = false;
				boolean is_end = false;
				int start = 0;
				int end = length - 1;
				
				if (arr[start] == '"') {
					is_start = true;
					start = 1;
					length--;
				}
				if (arr[end] == '"') {
					is_end = true;
					length--;
				}
				String trimed = length <= 0 ? "" : new String(arr, start, length);

				// OmegaTに翻訳のソースを指示
				String trans = processEntry(trimed);

				// 翻訳が返ってくるので書き込む
				// 両脇に"を付与する
				item.setValue((is_start ? "\"" : "") + trans + (is_end ? "\"" : ""));
				outFile.write(item.getLine() + lbpr.getLinebreak());
			}
		} finally {
			if (lbpr != null) {
				lbpr.close();
			}
		}
	}
}
