package com.paradoxinteractive.lib.omegat;

import org.apache.commons.io.ByteOrderMark;
import org.omegat.util.Log;

/*
 * 解析サンプル
 *  evergreen_decision:0 "常緑を獲得"
 * 012345678901234567890123 4 5 6 78
 * 0         1         2
 * 
 * beginKey = 1
 * endKey = 21 (:のあとに数字がついてる場合がある)
 * beginValue = 22 ("を含めたものとする)
 * endValue = 29 ("を含めたものとする)
 * beginComment = -1
 * endComment = -1
 * 
 * 値は"で囲まれているべきだが終端がない場合もエラーとしない
 * 例1:値が「"常緑を獲得"」となる
 *  evergreen_decision:0 "常緑を獲得"
 * 例2:値が「"常緑を獲得」となる
 *  evergreen_decision:0 "常緑を獲得
 * 例3:値が「常緑を獲得"」となる
 *  evergreen_decision:0 常緑を獲得"
 * 例4:値が「""常緑を獲得"」となる
 *  evergreen_decision:0 ""常緑を獲得"
 * 
 * まずインラインコメントは考慮せず値に入れる
 * 例:次は値が「"常緑を獲得" #ここのコメント」となる
 *  evergreen_decision:0 "常緑を獲得" #ここのコメント
 * 
 * 次に、値に含まれる「"」の数をカウントしながら「#」を探す
 * 奇数の時#が見つかったら、コメントではなくタグとみなし、そのまま値に含まれるものとする
 * 0個の時#が見つかったら以降をコメントとみなす
 * 偶数の時#が見つかったら以降をコメントとみなす
 */
public class JominiItem {
	private String line = "";
	private int beginKey = -1;
	private int endKey = -1;
	private int beginValue = -1;
	private int endValue = -1;
	private int beginComment = -1;
	private int endComment = -1;
	private boolean isBOM = false;

	public void Parse(String line) {
		this.line = line;
		beginKey = -1;
		endKey = -1;
		beginValue = -1;
		endValue = -1;
		beginComment = -1;
		endComment = -1;
		isBOM = false;

		char[] sa = line.toCharArray();
		int length = sa.length;
		int i = 0;

		if (length == 0) {
			// 空行だった
			return;
		}

		// 先頭がBOMの場合がある
		if (sa[0] == ByteOrderMark.UTF_BOM) {
			isBOM = true;
			if (length == 1) {
				// BOMしかなかった
				return;
			}
			i++;
		}

		// 先頭のスペース/タブをスキップ
		while (sa[i] <= ' ') {
			if (i >= length - 1) {
				// 終端まできた→空行だった
				return;
			}
			i++;
		}

		// #だったらコメント行
		// それ以外はキーとみなす
		if (sa[i] == '#') {
			beginComment = i;
			endComment = length;
			return;
		}

		beginKey = i;

		// 次のスペース/タブがくるまでKeyなのでスキップ
		do {
			endKey = i + 1;
			if (i >= length - 1) {
				// 終端まできたので終わる
				return;
			}
			i++;
		} while (sa[i] > ' ');

		// スペース/タブがなくなるまでスキップ
		while (sa[i] <= ' ') {
			if (i >= length - 1) {
				// 終端まできた→空行だった
				return;
			}
			i++;
		}

		// 以降は値が入ってるはず
		// 先頭or最後が"かの判定はしない
		beginValue = i;
		endValue = line.length();

		// Log.log("[jomini]beginValue=" + beginValue);
		// Log.log("[jomini]endValue=" + endValue);

		// 以降インラインコメント用の修正

		// "の数をカウントする
		int quotes = 0;
		// 直前の"の位置
		int last_quote = 0;

		// 先頭に"をつけ忘れることがあるので
		// 先頭が#でなかった場合、"があるものとみなして開始する
		if (sa[i] == '#') {
			// キーだけ定義されていて、""すらない、コメントだった
			beginValue = -1;
			endValue = -1;
			beginComment = i;
			endComment = line.length();
			return;
		}

		quotes++;
		last_quote = i;
		i++;

		while (i < length) {
			if (sa[i] != '"' && sa[i] != '#') {
				i++;
				continue;
			}

			if (sa[i] == '"') {
				quotes++;
				last_quote = i;
				i++;
				continue;
			}

			// ここへ来てるのはsa[i] == '#'のみ

			// 奇数個
			if (quotes % 2 != 0) {
				i++;
				continue;
			}

			// 隙間がない
			if (i - last_quote < 2) {
				//Log.log("[jomini]line=" + line);
				//Log.log("[jomini]value=" + getValue());
				i++;
				continue;
			}

			// last_quote+1 ～ i-1 まですべて空白か確認
			boolean allspace = true;
			for (int j = last_quote + 1; j <= i - 1; j++) {
				if (sa[j] > ' ') {
					allspace = false;
					break;
				}
			}
			if (!allspace) {
				i++;
				continue;
			}

			// 以降はインラインコメント
			endValue = last_quote + 1;
			beginComment = i;
			endComment = line.length();
			// Log.log("[jomini]line=" + line);
			// Log.log("[jomini]value=" + getValue());
			// Log.log("[jomini]comment=" + getComment());
			return;
		}
	}

	public boolean isEmpty() {
		return isBOM ? line.substring(1).trim().isEmpty() : line.trim().isEmpty();
	}

	public String getComment() {
		if (beginComment < 0 || endComment > line.length() || endComment < beginComment) {
			return "";
		}
		return line.substring(beginComment, endComment);
	}

	public String getKey() {
		if (beginKey < 0 || endKey > line.length() || endKey < beginKey) {
			return "";
		}
		return line.substring(beginKey, endKey);
	}

	public String getValue() {
		if (beginValue < 0 || endValue > line.length() || endValue < beginValue) {
			return "";
		}
		return line.substring(beginValue, endValue);
	}

	// 値を代入してから再解析
	public void setValue(String value) {
		if (beginValue < 0 || endValue > line.length() || endValue < beginValue) {
			Parse(value);
			return;
		}

		String p = line.substring(0, beginValue);
		String q = line.substring(endValue);
		Parse(p + value + q);
	}

	public String getLine() {
		return line;
	}
}
