package com.github.hmdev.util;

import java.util.regex.Pattern;

/**
 * 文字変換と判別関連の関数定義クラス
 */
public class CharUtils
{
	/** 全角英数字を半角に変換
	 * @param src 全角文字列
	 * @return 半角文字列 */
	static public String fullToHalf(String src)
	{
		char[] c = src.toCharArray();
		for (int i=c.length-1; i>=0; i--) {
			if (c[i] >= '０' && c[i] <= '９') c[i] = (char)(c[i]-'０'+'0');
			else if (c[i] >= 'Ａ' && c[i] <= 'ｚ') c[i] = (char)(c[i]-'ａ'+'a');
		}
		return new String(c);
	}
	
	/** すべて同じ文字かチェック */
	static public boolean isSameChars(char[] ch, int begin, int end)
	{
		for (int i=begin+1; i<end; i++) {
			if (ch[begin] != ch[i]) return false;
		}
		return true;
	}
	
	/** 半角数字かチェック */
	static public boolean isNum(char ch)
	{
		switch (ch) {
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': return true;
		}
		return false;
	}
	
	/** 英字かどうかをチェック 拡張ラテン文字含む
	 * 半角スペースは含まない */
	static public boolean isHalf(char ch)
	{
		return (0x21 <= ch && ch <= 0x02AF);
	}
	/** 英字かどうかをチェック 拡張ラテン文字含む
	 * 半角スペースは含まない */
	static public boolean isHalf(char[] chars)
	{
		for (char ch : chars) {
			if (!isHalf(ch)) return false;
		}
		return true;
	}
	
	/** 英字かどうかをチェック 拡張ラテン文字含む
	 * 半角スペースを含む */
	static public boolean isHalfSpace(char ch)
	{
		return (0x20 <= ch && ch <= 0x02AF);
	}
	/** 英字かどうかをチェック 拡張ラテン文字含む
	 * 半角スペースを含む */
	static public boolean isHalfSpace(char[] chars)
	{
		for (char ch : chars) {
			if (!isHalfSpace(ch)) return false;
		}
		return true;
	}
	
	static public boolean isFullAlpha(char ch)
	{
		return ('Ａ' <= ch && ch <= 'Ｚ') || ('ａ' <= ch && ch <= 'ｚ') || ('０' <= ch && ch <= '９') || '＠' == ch || '＿' == ch;
	}
	/** 半角数字かチェック */
	static public boolean isFullNum(char ch)
	{
		switch (ch) {
			case '０': case '１': case '２': case '３': case '４': case '５': case '６': case '７': case '８': case '９': return true;
		}
		return false;
	}
	
	/** ひらがなかチェック 半角濁点半濁点は全角に変換済 */
	static public boolean isHiragana(char ch)
	{
		return ('ぁ'<=ch && ch<='ん') || 'ゕ'==ch || 'ゖ'==ch || 'ー'==ch || 'ゝ'==ch || 'ゞ'==ch || 'ヽ'==ch || 'ヾ'==ch || '゛'==ch || '゜'==ch
				|| 'ι'==ch; //濁点処理用の例外
				// || 'ﾞ'==ch || 'ﾟ'== ch;
	}
	/** カタカナかチェック 半角濁点半濁点は全角に変換済 */
	static public boolean isKatakana(char ch)
	{
		if ('ァ'<=ch && ch<='ヶ') return true;
		switch (ch) {
		case 'ァ': case 'ィ': case 'ゥ': case 'ェ': case 'ォ': case 'ヵ': case 'ㇰ': case 'ヶ': case 'ㇱ': case 'ㇲ': case 'ッ': case 'ㇳ': case 'ㇴ':
		case 'ㇵ': case 'ㇶ': case 'ㇷ': case 'ㇸ': case 'ㇹ': case 'ㇺ': case 'ャ': case 'ュ': case 'ョ': case 'ㇻ': case 'ㇼ': case 'ㇽ': case 'ㇾ': case 'ㇿ': case 'ヮ':
		case 'ー': case 'ゝ': case 'ゞ': case 'ヽ': case 'ヾ': case '゛': case '゜':
		//case 'ﾞ': case 'ﾟ':
			return true;
		}
		return false;
	}
	
	static public boolean isSpace(String line)
	{
		char c;
		for (int i=line.length()-1; i>=0; i--) {
			c = line.charAt(i);
			if (c != ' ' && c != '　' && c != ' ') return false;
		}
		return true;
	}
	
	/** 英字かどうかをチェック 拡張ラテン文字含む */
	static public boolean isAlpha(char ch)
	{
		return ('A' <= ch  && ch <= 'Z') || ('a' <= ch && ch <= 'z') || (0x80 <= ch && ch <= 0x02AF);
	}
	
	/** 漢字かどうかをチェック
	 * 4バイト文字のも対応
	 * 漢字の間の「ノカケヵヶ」も漢字扱い
	 * IVS文字 U+e0100-e01efも漢字扱い */
	static public boolean isKanji(char[] ch, int i)
	{
		switch (ch[i]) {
		case '゛': case '゜':
			//二の字点は濁点付きも漢字
			return (i>0 && ch[i-1]=='〻');
		/*case 'ノ': case 'カ': case 'ケ': case 'ヵ': case 'ヶ':
			//漢字の間にある場合だけ漢字扱い
			if (i==0 || i+1==ch.length) return false;
			return _isKanji(ch, i-1) && _isKanji(ch, i+1);
		*/
		}
		return _isKanji(ch, i);
	}
	/** カタカナ以外の漢字チェック */
	static private boolean _isKanji(char[] ch, int i)
	{
		int pre = i==0?-1:ch[i-1];
		int c = ch[i];
		int suf = i+1>=ch.length?-1:ch[i+1];
		switch (c) {
		case '〓': case '〆': case '々': case '〻':
			return true;
		}
		if (0x4E00 <= c && c <= 0x9FFF) return true;//'一' <= ch && ch <= '龠'
		if (0xF900 <= c && c <= 0xFAFF) return true;//CJK互換漢字
		if (0xFE00 <= c && c <= 0xFE0D) return true;//VS1-14 (15,16は絵文字用なので除外)
		//0x20000-0x2A6DF UTF16({d840,dc00}-{d869,dedf})
		//0x2A700-0x2B81F UTF16({d869,df00}-{d86e,dc1f})
		//0x2F800-0x2FA1F UTF16({d87e,dc00}-{d87e,de1f})
		if (pre >= 0) {
			if (0xDB40 == pre && 0xDD00 <= c && c <= 0xDDEF) return true; //IVS e0100-e01ef
			if (0xD87E == pre && 0xDc00 <= c && c <= 0xDE1F) return true;
			int code = pre<<16|c&0xFFFF;
			if (0xD840DC00 <= code && code <= 0xD869DEDF) return true;
			if (0xD869DF00 <= code && code <= 0xD86EDC1F) return true;
		}
		if (suf >= 0) {
			if (0xDB40 == c && 0xDD00 <= suf && suf <= 0xDDEF) return true; //IVS e0100-e01ef
			if (0xD87E == c && 0xDc00 <= suf && suf <= 0xDE1F) return true;
			int code = c<<16|suf&0xFFFF;
			if (0xD840DC00 <= code && code <= 0xD869DEDF) return true;
			if (0xD869DF00 <= code && code <= 0xD86EDC1F) return true;
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////
	/** ファイル名に使えない文字を'_'に置換 */
	static public String escapeUrlToFile(String str)
	{
		return str.replaceAll("(\\?|\\&)", "/").replaceAll("(:|\\*|\\||\\<|\\>|\"|\\\\)", "_");
	}
	
	////////////////////////////////////////////////////////////////
	/** 前後の空白を除外 */
	static public String removeSpace(String text)
	{
		return text.replaceFirst("^[ |　]+", "").replaceFirst("[ |　]+$", "");
	}
	/** タグを除外 */
	static public String removeTag(String text)
	{
		return text.replaceAll("［＃.+?］", "").replaceAll("<[^>]+>", "");
	}
	
	/** ルビを除去 特殊文字のエスケープ文字 ※※ ※《 ※》 等が含まれる */
	static public String removeRuby(String text)
	{
		StringBuilder buf = new StringBuilder();
		char[] ch = text.toCharArray();
		boolean inRuby = false;
		for (int i=0; i<ch.length; i++) {
			if (inRuby) {
				if (ch[i] == '》' && !CharUtils.isEscapedChar(ch, i)) inRuby = false;
			} else {
				switch (ch[i]) {
				case '｜':
					if (CharUtils.isEscapedChar(ch, i)) buf.append(ch[i]); 
					break;
				case '《':
					if (CharUtils.isEscapedChar(ch, i)) buf.append(ch[i]);
					else inRuby = true;
					break;
				default:
					if (!inRuby) buf.append(ch[i]);
				}
			}
		}
		return buf.toString();
	}
	
	/** 文字がエスケープされた特殊文字ならtrue */
	static public boolean isEscapedChar(char[] ch, int idx)
	{
		boolean escaped = false;
		for (int i=idx-1; i >= 0; i--) {
			if (ch[i] == '※') escaped = !escaped;
			else return escaped;
		}
		return escaped;
	}
	/** 文字がエスケープされた特殊文字ならtrue */
	static public boolean isEscapedChar(StringBuilder ch, int idx)
	{
		boolean escaped = false;
		for (int i=idx-1; i >= 0; i--) {
			if (ch.charAt(i) == '※') escaped = !escaped;
			else return escaped;
		}
		return escaped;
	}
	
	
	/** HTML特殊文字をエスケープ */
	static public String escapeHtml(String text)
	{
		return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}
	
	/** 目次やタイトル用の文字列を取得 ルビ関連の文字 ｜《》 は除外済で他の特殊文字は'※'エスケープ
	 * @param maxLength 文字制限 これより大きい文字は短くして...をつける
	 * @prram 記号文字を短縮する */
	static public String getChapterName(String line, int maxLength, boolean reduce)
	{
		String name = line.replaceAll("［＃.+?］", "")//注記除去
				.replaceAll("※(※|《|》|［|］|〔|〕|〔|〕|〔|〕|｜)", "$1") //エスケープ文字から※除外
				.replaceAll("\t", " ").replaceFirst("^[ |　]+", "").replaceFirst("[ |　]+$",""); //前後の不要な文字所除去
		if (reduce) name = name.replaceAll("(=|＝|-|―|─)+", "$1");//連続する記号は1つに
		//タグはimgとaを削除
		name = chapterTagOpenPattern.matcher(name).replaceAll("");
		name = chapterTagClosePattern.matcher(name).replaceAll("");
		
		if (maxLength == 0) return name;
		return name.length()>maxLength ? name.substring(0, maxLength)+"..." : name;
	}
	/** imgとaタグ除去用のパターン */
	static Pattern chapterTagOpenPattern = Pattern.compile("< *(img|a) [^>]*>", Pattern.CASE_INSENSITIVE);
	static Pattern chapterTagClosePattern = Pattern.compile("< */ *(img|a)(>| [^>]*>)", Pattern.CASE_INSENSITIVE);
	
	/** 指定されたタグを削除
	 * @param single 単独または開始タグ 属性無し 複数のタグは|でOR条件を指定
	 * @param open 開始タグ 属性値有り 複数のタグは|でOR条件を指定
	 * @param close 終了タグ 複数のタグは|でOR条件を指定 */
	static String removeTag(String str, String single, String open, String close)
	{
		if (str.indexOf('<') == -1) return str;
		
		if (single != null) str = Pattern.compile("< *("+single+") */? *>", Pattern.CASE_INSENSITIVE).matcher(str).replaceAll("");
		if (open != null) str = Pattern.compile("< *("+open+") [^>]*>", Pattern.CASE_INSENSITIVE).matcher(str).replaceAll("");
		if (close != null) str = Pattern.compile("< */ *("+close+")(>| [^>]*>)", Pattern.CASE_INSENSITIVE).matcher(str).replaceAll("");
		return str;
	}
	
	static public String getChapterName(String line, int maxLength)
	{
		return getChapterName(line, maxLength, true);
	}
	
	/** BOMが文字列の先頭にある場合は除去 */
	static public String removeBOM(String str)
	{
		if (str != null && str.length() > 0) {
			if (str.charAt(0) == 0xFEFF) {
				return str.substring(1);
			} else {
				return str;
			}
		} else {
			return null;
		}
	}
	
	////////////////////////////////////////////////////////////////
	/** Test用 */
	/*public static void main(String[] args)
	{
		try {
			System.out.println(removeTag("あ<IMG>あ<IMG1>あ<img src=\"\"/>い<A>い<A1>い<AB>う<ab href=\"\">うう<a href=\"\">え<br>え<br/>え</a>お</ab>おお", "br", "img|a", "a"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
