package com.paradoxinteractive.lib.omegat;

/*
 * 解析サンプル
 *  evergreen_decision:0 "常緑を獲得"
 * 012345678901234567890123 4 5 6 78
 * 0         1         2
 * 
 * beginKey = 1
 * endKey = 20 (:のあとに数字がついてる場合がある)
 * beginValue = 22 ("を含めたものとする)
 * endValue = 28 ("を含めたものとする)
 * beginComment = -1
 * endComment = -1
 * 
 * 値は"で囲まれているべきだが終端がない場合もエラーとしない
 * 例1:次は値が「"常緑を獲得」となる
 *  evergreen_decision:0 "常緑を獲得
 * 例2:次は値が「""常緑を獲得"」となる
 *  evergreen_decision:0 ""常緑を獲得"
 * 
 * インラインコメントは考慮しない
 * 例:次は値が「"常緑を獲得" #ここのコメント」となる
 *  evergreen_decision:0 "常緑を獲得" #ここのコメント
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
		if (sa[0] == 0xFEFF) {
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
			endComment = length - 1;
			return;
		}

		beginKey = i;

		// 次のスペース/タブがくるまでKeyなのでスキップ
		do {
			endKey = i;
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
	}

	public boolean isEmpty() {
		return isBOM ? line.substring(1).trim().isEmpty() : line.trim().isEmpty();
	}

	public String getComment() {
		if (beginComment < 0 || endComment < beginComment) {
			return "";
		}
		return line.substring(beginComment, endComment);
	}

	public String getKey() {
		if (beginKey < 0 || endKey < beginKey) {
			return "";
		}
		return line.substring(beginKey, endKey);
	}

	public String getValue() {
		if (beginValue < 0 || endValue < beginValue) {
			return "";
		}
		return line.substring(beginValue, endValue);
	}

	public void setValue(String value) {
		String p = beginValue <= 0 ? "" : line.substring(0, beginValue);

		// 再解析
		Parse(p + value);
	}

	public String getLine() {
		return line;
	}
}
