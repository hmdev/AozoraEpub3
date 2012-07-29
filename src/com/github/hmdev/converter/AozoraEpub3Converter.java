package com.github.hmdev.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.hmdev.info.BookInfo;
import com.github.hmdev.util.CharUtils;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.writer.Epub3Writer;

/**
 * 青空文庫テキストをePub3形式のXHTMLに変換
 * Licence: Non-commercial use only.
 */
public class AozoraEpub3Converter
{
	/** タイトル記載種別 */
	//final static public String[] titleType = {"表題+著者名", "著者名＋表題", "表題のみ", "なし", "ファイル名から"};
	public enum TitleType {
		TITLE_AUTHOR, AUTHOR_TITLE, TITLE_ONLY, NONE, FILENAME;
		final static public String[] titleTypeNames = {"表題＋著者名", "著者名＋表題", "表題のみ", "なし", "ファイル名から"};
		boolean hasTitleAuthor() {
			switch (this) {
			case TITLE_AUTHOR:
			case AUTHOR_TITLE:
			case TITLE_ONLY:
				return true;
			default:
				return false;
			}
		}
	}
	
	/** ファイル名 [著作者] 表題.txt から抽出するパターン */
	Pattern fileNamePattern = Pattern.compile("\\[(.+?)\\]( |　)*(.+?)(\\(|（|\\.)");
	
	//---------------- Properties ----------------//
	/** UTF-8以外の文字を代替文字に変換 */
	boolean userAlterCharEscape = false;
	
	/** 半角2文字の数字と!?を縦横中に変換 */
	boolean autoYoko = true;
	
	/** 栞用のid付きspanで本文を括るならtrue */
	boolean withIdSpan = false;
	
	/** 強制改行行数 ページ先頭からこれ以降の行 */
	int forcePageBreak = 500;
	/** 強制改行対象の空行 */
	int forcePageBreakEmptyLine = 2;
	/** 強制改行対象の空行後のパターン */
	Pattern forcePageBreakPattern = null;
	
	//---------------- Flags Variables ----------------//
	/** 字下げ後閉じるまでture */
	boolean inJisage = false;
	/** 字下げ折り返しの場合のみtrue inJisage=false時に合わせてfalse */
	boolean inJisageCr = false;

	
	//---------------- パターン ----------------//
	/** 注記パターン */
	static Pattern chukiPattern = Pattern.compile("[［＃|<].+?[］|>]");
	/** 外字注記パターン */
	static Pattern gaijiChukiPattern = Pattern.compile("(※［＃.+?］)|(〔.+?〕)|(／″?＼)");
	/** 後述注記パターン */
	static Pattern chukiSufPattern = Pattern.compile("［＃「([^」]+)」(.+?)］");
	
	//---------------- 変換用テーブル ----------------//
	/** 変換関連が初期化済みならtrue */
	static boolean inited = false;
	
	/** 注記→タグ変換用
	 * key=注記文字列 (［＃］除外)
	 * value= { 置換文字列, 行末追加文字列 } */
	static HashMap<String, String[]> chukiMap = new HashMap<String, String[]>();
	
	/** 注記フラグ 改行なし key = 注記名 */
	static HashSet<String> chukiFlagNoBr = new HashSet<String>();
	/** 注記フラグ 圏点開始 key = 注記名 */
	static HashSet<String> chukiFlagNoRubyStart = new HashSet<String>();
	/** 注記フラグ 圏点終了 key = 注記名 */
	static HashSet<String> chukiFlagNoRubyEnd = new HashSet<String>();
	/** 注記フラグ 改ページ処理 key = 注記名 */
	static HashSet<String> chukiFlagPageBreak = new HashSet<String>();
	
	/** 後述注記→タグ変換用
	 * key=注記文字列 (「」内削除)
	 * value= { 前タグ, 後タグ } */
	static HashMap<String, String[]> sufChukiMap = new HashMap<String, String[]>();
	
	static HashMap<String, Pattern> chukiPatternMap = new HashMap<String, Pattern>();
	
	
	/** 「基本ラテン文字のみによる拡張ラテン文字Aの分解表記」の変換クラス */
	static LatinConverter latinConverter;
	
	/** 外字注記タグをUTF-8・グリフタグ・代替文字に変換するクラス */
	static AozoraGaijiConverter ghukiConverter;
	
	/** Epub圧縮出力用クラス */
	Epub3Writer writer;
	
	/** 改ページ後の行数 */
	int pageLineNum = 0;
	
	int idNum = 0;
	int idLineNum = -1;
	
	
	/** 見出し仮対応出力用
	 * 章の最初の本文をsetChapterNameでセットしたらtrue */
	boolean chapterStarted = true;
	
	/** コンストラクタ
	 * 変換テーブルやクラスがstaticで初期化されていなければ初期化
	 * @param _msgBuf ログ出力用バッファ
	 * @throws IOException */
	public AozoraEpub3Converter(Epub3Writer writer) throws IOException
	{
		//初期化されていたら終了
		if (inited) return;
		
		this.writer = writer;
		
		//拡張ラテン変換
		latinConverter = new LatinConverter();
		
		ghukiConverter = new AozoraGaijiConverter();
		
		//注記タグ変換
		File chukiTagFile = new File("chuki_tag.txt");
		BufferedReader src = new BufferedReader(new InputStreamReader(new FileInputStream(chukiTagFile), "UTF-8"));
		String line;
		int lineNum = 0;
		try {
			while ((line = src.readLine()) != null) {
				lineNum++;
				if (line.length() > 0 && line.charAt(0)!='#') {
					try {
						String[] values = line.split("\t");
						//タグ取得 3列目は行末タグ
						String[] tags;
						if (values.length > 2 && values[2].length() > 0) tags = new String[]{values[1], values[2]};
						else tags = new String[]{values[1]};
						chukiMap.put(values[0], tags);
						//注記フラグ
						if (values.length > 3 && values[3].length() > 0) {
							switch (values[3].charAt(0)) {
							case '1': chukiFlagNoBr.add(values[0]); break;
							case '2': chukiFlagNoRubyStart.add(values[0]); break;
							case '3': chukiFlagNoRubyEnd.add(values[0]); break;
							case 'P': chukiFlagPageBreak.add(values[0]); break;
							}
						}
						
					} catch (Exception e) {
						LogAppender.append("[ERROR] "+chukiTagFile.getName()+" ("+lineNum+") : "+line+"\n");
					}
				}
			}
		} finally {
			src.close();
		}
		//TODO パターンとprintfのFormatを設定ファイルから読み込みできるようにする (printfの引数の演算処理はフラグで切り替え？)
		chukiPatternMap.put("折り返し", Pattern.compile("［＃ここから([０-９]+)字下げ、折り返して([０-９]+)字下げ］"));
		chukiPatternMap.put("字下げ字詰め", Pattern.compile("［＃ここから([０-９]+)字下げ、([０-９]+)字詰め］"));
		chukiPatternMap.put("字下げその他", Pattern.compile("［＃ここから([０-９]+)字下げ、(.*)］"));
		
		//前方参照注記
		File chukiSufFile = new File("chuki_tag_suf.txt");
		src = new BufferedReader(new InputStreamReader(new FileInputStream(chukiSufFile), "UTF-8"));
		lineNum = 0;
		try {
			while ((line = src.readLine()) != null) {
				lineNum++;
				if (line.length() > 0 && line.charAt(0)!='#') {
					try {
						String[] values = line.split("\t");
						//タグ取得 3列目は行末タグ
						String[] tags;
						if (values.length > 2 && values[2].length() > 0) tags = new String[]{values[1], values[2]};
						else tags = new String[]{values[1]};
						sufChukiMap.put(values[0], tags);
						//別名
						if (values.length > 3 && values[3].length() > 0) sufChukiMap.put(values[3]+values[0], tags);
						
					} catch (Exception e) {
						LogAppender.append("[ERROR] "+chukiTagFile.getName()+" ("+lineNum+") : "+line+"\n");
					}
				}
			}
		} finally {
			src.close();
		}
		
		inited = true;
	}
	
	/**  栞用id付きspanの出力設定
	 * @param withIdSpan 栞用id付きspanを出力するならtrue */
	public void setWithIdSpan(boolean withIdSpan)
	{
		this.withIdSpan = withIdSpan;
	}
	/**  半角数字!?の回転を設定
	 * @param autoYoko 回転を設定するならtrue */
	public void setAutoYoko(boolean autoYoko)
	{
		this.autoYoko = autoYoko;
	}
	/** 自動強制改行設定 */
	public void setForcePageBreak(int forcePageBreak, int emptyLine, Pattern pattern)
	{
		this.forcePageBreak = forcePageBreak;
		this.forcePageBreakEmptyLine = emptyLine;
		this.forcePageBreakPattern = pattern;
	}
	/** タイトルと著作者を取得. 行番号も保存して出力時に変換出力
	 * @throws IOException */
	public BookInfo getBookInfo(BufferedReader src, TitleType titleType) throws IOException
	{
		BookInfo bookInfo = new BookInfo();
		String line;
		int idx = -1;
		if (titleType.hasTitleAuthor()) {
			//画像があったら表紙にする
			
			// 文字のない行は飛ばす
			line = src.readLine();
			idx++;
			String plainLine = "";
			while (line != null){
				plainLine = line.replaceAll("<[^>]+>", "").replaceAll("^[　| ]*(.*?)[　| ]*$", "$1");//タグと前後空白除去
				//注記も除去して空行チェック
				if (plainLine.replaceAll("［＃.+?］", "").length() > 0) break;
				line = src.readLine();
				idx++;
			}
			boolean titleFirst = titleType==TitleType.TITLE_AUTHOR || titleType==TitleType.TITLE_ONLY;
			// タイトル・著者
			String replaced = this.replaceToPlain(convertGaijiChuki(line, false));
			if (titleFirst) {
				bookInfo.titleLine = idx;
				bookInfo.title = replaced;
			} else {
				bookInfo.creatorLine = idx;
				bookInfo.creator = replaced;
			}
			
			if (titleType == TitleType.TITLE_AUTHOR || titleType == TitleType.AUTHOR_TITLE) {
				// 文字のない行は飛ばす
				line = src.readLine();
				idx++;
				while (line != null) {
					plainLine = line.replaceAll("<[^>]+>", "").replaceAll("^[　| ]*(.*?)[　| ]*$", "$1");//タグと前後空白除去
					//注記も除去して空行チェック
					if (plainLine.replaceAll("［＃.+?］", "").length() > 0) break;
					line = src.readLine();
					idx++;
				}
				// タイトル・著者
				replaced = this.replaceToPlain(convertGaijiChuki(line, false));
				if (!titleFirst) {
					bookInfo.titleLine = idx;
					bookInfo.title = replaced;
				} else {
					bookInfo.creatorLine = idx;
					bookInfo.creator = replaced;
				}
			}
		}
		return bookInfo;
	}
	
	/** 青空テキストをePub3のXHTMLに変換
	 * @param _msgBuf ログ出力用バッファ
	 * @param out 出力先Writer
	 * @param src 入力テキストReader
	 * @param titleType  */
	public void convertTextToEpub3(BufferedWriter out, BufferedReader src, BookInfo metaInfo) throws IOException
	{
		String line;
		int lineNum = -1;
		
		pageLineNum = 0;
		idNum = 0;
		idLineNum = -1;
		
		boolean inComment = false;
		this.chapterStarted = false;
		while ((line = src.readLine()) != null) {
			lineNum++;
			pageLineNum++;
			
			if (lineNum == metaInfo.titleLine) {
				out.write(chukiMap.get("表題前")[0]);
				convertTextLineToEpub3(out, convertGaijiChuki(line, true), lineNum);
				out.write(chukiMap.get("表題後")[0]);
				out.write("\n");
				this.chapterStarted = false; //章が始まっていないことにする
			}
			else if (lineNum == metaInfo.creatorLine) {
				out.write(chukiMap.get("著者前")[0]);
				convertTextLineToEpub3(out, convertGaijiChuki(line, true), lineNum);
				out.write(chukiMap.get("著者後")[0]);
				out.write("\n");
				this.chapterStarted = false; //章が始まっていないことにする
			} else {
				//コメント除外
				if (line.startsWith("-------------------------------------------------------")) {
					if (inComment) { inComment = false; continue;
					} else { inComment = true; continue;  }
				}
				if (inComment) continue;
				
				//強制改行
				if (forcePageBreak >10 && pageLineNum > forcePageBreak) {
					int emptyLineNum = 0;
					while (line.length() == 0) {
						emptyLineNum++;
						line = src.readLine(); if (line == null) return;
					}
					if (forcePageBreakEmptyLine <= emptyLineNum) {
						//空行は出力せずに改ページ
						this.writer.nextSection(out);
						this.pageLineNum = 0;
						this.chapterStarted = false;
					} else {
						//空行出力
						for (int i=0; i<emptyLineNum; i++) convertTextLineToEpub3(out, "", lineNum);
					}
				}
				convertTextLineToEpub3(out, line, lineNum);
			}
		}
	}
	
	/** 文字列内の外字を変換
	 * ・外字はUTF-16文字列に変換
	 * ・特殊文字のうち 《》｜＃ は文字の前に※をつけてエスケープ
	 * @param line 行文字列
	 * @param escape ※での特殊文字のエスケープをするならtrue
	 * @return 外字変換済の行文字列 */
	private String convertGaijiChuki(String line, boolean escape)
	{
		/*
		・外字
		 ※の場合は外字に変換
		 ※［＃「さんずい＋垂」、unicode6DB6］
		 ※［＃「さんずい＋垂」、U+6DB6、235-7］
		 ※［＃「てへん＋劣」、第3水準1-84-77］
		 ※［＃二の字点、1-2-22］
		・特殊文字
		《　→　※［＃始め二重山括弧、1-1-52］
		 》　→　※［＃終わり二重山括弧、1-1-53］
		 ［　→　※［＃始め角括弧、1-1-46］
		 ］　→　※［＃終わり角括弧、1-1-47］
		 〔　→　※［＃始めきっこう（亀甲）括弧、1-1-44］
		 〕　→　※［＃終わりきっこう（亀甲）括弧、1-1-45］
		 ｜　→　※［＃縦線、1-1-35］
		 ＃　→　※［＃井げた、1-1-84］
		 ※　→　※［＃米印、1-2-8］
		・アクセント 〔e'tiquette〕
		・くの字点 〳〴〵
		*/
		//変換後の文字列を出力するバッファ
		StringBuilder buf = null;
		
		Matcher m = gaijiChukiPattern.matcher(line);
		int begin = 0;
		int chukiStart = 0;
		while (m.find()) {
			if (buf == null) buf = new StringBuilder();
			
			String chuki = m.group();
			chukiStart = m.start();
			
			buf.append(line.substring(begin, chukiStart));
			
			//外字はUTF-8に変換してそのまま継続
			if (chuki.charAt(0) == '※') {
				String[] chukiValues = chuki.substring(3, chuki.length()-1).split("、");
				//注記文字グリフ or 代替文字変換
				String gaiji = ghukiConverter.toAlterString(chukiValues[0]);
				//コード変換
				if (gaiji == null && chukiValues.length > 1) {
					gaiji = ghukiConverter.codeToCharString(chukiValues[1]);
				}
				//コード変換
				if (gaiji == null && chukiValues.length > 2) {
					gaiji = ghukiConverter.codeToCharString(chukiValues[2]);
				}
				//コード変換
				if (gaiji == null && chukiValues.length > 3) {
					gaiji = ghukiConverter.codeToCharString(chukiValues[3]);
				}
				//注記名称で変換
				if (gaiji == null) gaiji = ghukiConverter.toUtf(chukiValues[0]);
				
				if (gaiji == null) {
					LogAppender.append("[外字未変換] : "+chuki+"\n");
					gaiji = "〓";
				}
				else if (gaiji.length() == 1 && escape) {
					//特殊文字は 前に※をつけて文字出力時に例外処理
					switch (gaiji.charAt(0)) {
					//case '※': buf.append('※'); break;
					case '》': buf.append('※'); break;
					case '《': buf.append('※'); break;
					case '｜': buf.append('※'); break;
					case '＃': buf.append('※'); break;
					}
				}
				buf.append(gaiji);
				//System.out.println(chuki+" : "+gaiji);
				
			} else if (chuki.charAt(0) == '〔') {
				//拡張ラテン文字変換
				//〔の次が半角でなければそのまま〔を出力
				if (!CharUtils.isHalf(chuki.charAt(1))) {
					buf.append(chuki);
				} else {
					//System.out.println(chuki);
					buf.append(latinConverter.toLatinString(chuki.substring(1, chuki.length()-1)));
				}
			} else if (chuki.charAt(0) == '／') {
				//くの字点
				if (chuki.charAt(1) == '″') buf.append("〴");
				else buf.append("〳");
				buf.append("〵");
			}
			
			begin = chukiStart+chuki.length();
		}
		
		if (buf == null) return line;
		//残りの文字
		buf.append(line.substring(begin));
		return buf.toString();
	}
	
	/** 後述注記をインライン注記変換
	 * 重複等の法則が変則すぎるのでバッファを利用
	 * kentenの中にルビ、font、yokoが入る場合の入れ替えは後でやる */
	private String replaceChukiSufTag(String line)
	{
		//バッファに先に行を格納 マッチしたら後タグを置換して前タグをinsert
		StringBuilder buf = null;
		
		Matcher m = chukiSufPattern.matcher(line);
		int chOffset = 0;
		while (m.find()) {
			if (buf == null) buf = new StringBuilder(line);
			
			//System.out.println(m.group());
			String target = m.group(1);
			String chuki = m.group(2);
			String[] tags = sufChukiMap.get(chuki);
			if (tags  == null) continue;
			
			int targetLength = target.length();
			int chukiStart = m.start();
			int chukiEnd = m.end();
			
			//置換済みの文字列で注記追加位置を探す
			int idx = chukiStart-1+chOffset;
			boolean inTag = false;
			//間にあるタグをスタック
			Stack<String> tagStack = new Stack<String>();
			boolean isEndTag = false;
			int tagEnd = -1;
			while (targetLength > 0 && idx >= 0) {
				switch (buf.charAt(idx)) {
				case '※':
				case '|':
					break;
				case '》':
					inTag = true;
					break;
				case '］':
					inTag = true;
					isEndTag = (idx-3 > 0 && "終わり".equals(buf.substring(idx-3, idx)));
					tagEnd = idx;
					break;
				case '《':
					inTag = false;
					break;
				case '［':
					inTag = false;
					if (isEndTag) {
						String tag = buf.substring(idx+2, tagEnd-3);
						tagStack.push(tag);
						//System.out.println("push: "+tag);
					} else {
						String tag = buf.substring(idx+2, tagEnd);
						//System.out.println("pop: "+tag);
						if (tagStack.size() > 0 && tag.equals(tagStack.peek())) {
							tagStack.pop();
						}
					}
					break;
				default:
					if (!inTag) {
						targetLength--;
					}
				}
				idx--;
			}
			
			//前のタグがStackにあれば含む
			boolean exit = false;
			while (idx >= 0) {
				switch (buf.charAt(idx)) {
				case '］':
					inTag = true;
					tagEnd = idx;
					break;
				case '［':
					inTag = false;
					String tag = buf.substring(idx+2, tagEnd);
					if (tagStack.size() > 0 && tag.equals(tagStack.peek())) {
						tagStack.pop();
					} else {
						idx = tagEnd;
						exit = true;
					}
					break;
				default:
					if (!inTag) exit = true; //注記外で文字があったら終了
				}
				if (exit) break;
				idx--;
			}
			//一つ戻す
			int targetBegin = idx + 1;
			
			//後ろタグ置換
			buf.delete(chukiStart+chOffset, chukiEnd+chOffset);
			buf.insert(chukiStart+chOffset, "［＃"+tags[1]+"］");
			//前タグinsert
			buf.insert(targetBegin, "［＃"+tags[0]+"］");
			
			chOffset += tags[0].length() + tags[1].length() +6 - (chukiEnd-chukiStart);
		}
		//置換なし
		if (buf == null) return line;
		//置換後文字列
		return buf.toString();
	}
	
	/** 青空テキスト行をePub3のXHTMLで出力
	 * @param out 出力先Writer
	 * @param line 変換前の行文字列 */
	private void convertTextLineToEpub3(BufferedWriter out, String line, int lineNum) throws IOException
	{
		convertTextLineToEpub3(out, line, lineNum, false);
	}
	/** 青空テキスト行をePub3のXHTMLで出力
	 * @param out 出力先Writer
	 * @param line 変換前の行文字列
	 * @param noBr 改行を出力しない */
	private void convertTextLineToEpub3(BufferedWriter out, String line, int lineNum, boolean noBr) throws IOException
	{
		//外字変換
		line = convertGaijiChuki(line, true);
		//前方参照注記変換
		line = replaceChukiSufTag(line);
		
		int lineSpanIdx = 1;
		
		char[] ch = line.toCharArray();
		
		//ルビなしタグ開始なら+1
		int noRubyLevel = 0;
		
		StringBuilder bufSuf = new StringBuilder();
		// タグ変換
		Matcher m = chukiPattern.matcher(line);
		int begin = 0;
		int chukiStart = 0;
		
		boolean pPrinted = false;
		//字下げ中でブロック注記(１行に１注記のみ)でなければ行を<p>で括る
		if (inJisageCr) {
			if (line.indexOf('］') != ch.length-1) {
				pPrinted = true;
				out.write("<p>");
			}
		}
		
		while (m.find()) {
			String chukiTag = m.group();
			String lowerChukiTag = chukiTag.toLowerCase();
			chukiStart = m.start();
			
			//fontの入れ子は可、圏点・縦横中はルビも付加
			if (chukiTag.charAt(0) == '<' && !(lowerChukiTag.startsWith("<img ") || lowerChukiTag.startsWith("<a ") || lowerChukiTag.startsWith("</a>"))) {
				//<img <a </a 以外のタグは注記処理せず本文処理
				continue;
			} else {
				//注記の前まで本文出力
				if (begin < chukiStart) {
					if (lineNum != idLineNum) { idNum++; idLineNum = lineNum; };
					if (withIdSpan && noRubyLevel==0) out.write("<span id=\"kobo."+idNum+"."+(lineSpanIdx++)+"\">");//栞用span開始
					this.printRubyText(out, ch, begin, chukiStart, noRubyLevel>0);
					if (withIdSpan && noRubyLevel==0) out.write("</span>");//栞用span閉じる
					
					//改ページ後の章名称変更
					if (!this.chapterStarted) {
						String chapterName = this.replaceToPlain(line.substring(begin,chukiStart));
						chapterName = chapterName.replaceAll("^=+", "").replaceAll("=+$", "");
						chapterName = chapterName.replaceAll("^-+", "").replaceAll("-+$", "");
						if (chapterName.length() >0) {
							
							this.chapterStarted = true;
							this.writer.updateChapterName(chapterName.length()>64 ? chapterName.substring(0, 64) : chapterName);
						}
					}
				}
				
				//注記→タグ変換
				String chukiName = chukiTag.substring(2, chukiTag.length()-1);
				
				//ルビ無効チェック
				if (chukiFlagNoRubyStart.contains(chukiName)) noRubyLevel++;
				else if (chukiFlagNoRubyEnd.contains(chukiName)) noRubyLevel--;
				
				String[] tags = chukiMap.get(chukiName);
				if (tags != null) {
					////////////////////////////////////////////////////////////////
					//改ページ処理
					if (chukiFlagPageBreak.contains(chukiName)) {
						//次の文字が改行ならnoBrで前に</br>\n出力
						if (ch.length == begin+chukiTag.length()) {
							noBr = true;
						}
						this.writer.nextSection(out);
						this.pageLineNum = 0;
						this.chapterStarted = false;
					}
					////////////////////////////////////////////////////////////////
					
					//字下げフラグ処理
					if (chukiTag.endsWith("字下げ］")) {
						if (inJisage) {
							if (pPrinted) {
								pPrinted = false;
								out.write("</p>");
							}
							out.write(chukiMap.get("字下げ省略")[0]);
						}
						if (tags.length > 1) {
							inJisage = false;//インライン
							inJisageCr = false;
						}
						else inJisage = true; //ブロック
						//折り返しで<p>出力を制御
						if (chukiTag.indexOf("、折り返して") != -1) {
							inJisageCr = true;
						}
					}
					else if (chukiTag.equals("［＃ここで字下げ終わり］")) {
						if (pPrinted) {
							pPrinted = false;
							out.write("</p>");
						}
						inJisage = false;
						inJisageCr = false;
					}
					
					//タグ出力
					out.write(tags[0]);
					if (tags.length > 1) {
						bufSuf.insert(0, tags[1]);
					}
					//改行なしチェック
					if (chukiFlagNoBr.contains(chukiName)) noBr = true;
					
				} else {
					//画像 (訓点 ［＃（ス）］ は1-2文字かどうかで判断)
					// <img src="img/filename"/> → <object src="filename"/>
					// ［＃表紙（表紙.jpg）］［＃（表紙.jpg）］［＃「キャプション」（表紙.jpg、横321×縦123）入る）］
					//訓点と区別するため3文字目から（チェック
					int imageStartIdx = chukiTag.indexOf('（', 2);
					if (imageStartIdx > -1) {
						if (chukiTag.indexOf('）', 6) > -1) {
							String srcFilePath = chukiTag.replaceFirst("^.*（(.*?)(、.+)?）.*$", "$1");
							//画像ファイル名置換処理実行
							String fileName = writer.getImageFilePath(srcFilePath.trim());
							out.write(chukiMap.get("画像開始")[0]);
							out.write(fileName);
							out.write(chukiMap.get("画像終了")[0]);
							//LogAppender.append("[画像注記]: "+chukiTag+"\n");
							noBr = true;
						}
					} else if (lowerChukiTag.startsWith("<img")) {
						//src=の値抽出
						String srcFilePath = chukiTag.replaceFirst("^.* [S|s][R|r][C|c]=\"(.+?)\" ?.*>$", "$1");
						if (srcFilePath.length()==0) srcFilePath = chukiTag.replaceFirst("^.* [S|s][R|r][C|c]='(.+?)' ?.*>$", "$1");
						//画像ファイル名置換処理実行
						String fileName = writer.getImageFilePath(srcFilePath.trim());
						out.write(chukiMap.get("画像開始")[0]);
						out.write(fileName);
						out.write(chukiMap.get("画像終了")[0]);
						//LogAppender.append("[画像注記]: "+chukiTag+"\n");
						noBr = true;
					}
					else {
						//インデント字下げ
						try {
							Matcher m2 = chukiPatternMap.get("折り返し").matcher(chukiTag);
							if (m2.find()) {
								int arg0 = Integer.parseInt(CharUtils.fullToHalf(m2.group(1)));
								int arg1 = Integer.parseInt(CharUtils.fullToHalf(m2.group(2)));
								out.write(chukiMap.get("折り返し1")[0]+arg1);
								out.write(chukiMap.get("折り返し2")[0]+(arg0-arg1));
								out.write(chukiMap.get("折り返し3")[0]);
								//字下げフラグ処理
								if (inJisage) {
									if (pPrinted) {
										pPrinted = false;
										out.write("</p>");
									}
									out.write(chukiMap.get("字下げ省略")[0]);
								}
								else {
									inJisage = true;
									inJisageCr = true;
								}
								noBr = true;//ブロック字下げなので改行なし
							}
						} catch (Exception e) { e.printStackTrace(); }
						//インデント字下げ
						try {
							Matcher m2 = chukiPatternMap.get("字下げ字詰め").matcher(chukiTag);
							if (m2.find()) {
								int arg0 = Integer.parseInt(CharUtils.fullToHalf(m2.group(1)));
								int arg1 = Integer.parseInt(CharUtils.fullToHalf(m2.group(2)));
								out.write(chukiMap.get("字下げ字詰め1")[0]+arg0);
								out.write(chukiMap.get("字下げ字詰め2")[0]+arg1);
								out.write(chukiMap.get("字下げ字詰め3")[0]);
								//字下げフラグ処理
								if (inJisage) {
									if (pPrinted) {
										pPrinted = false;
										out.write("</p>");
									}
									out.write(chukiMap.get("字下げ省略")[0]);
								}
								else inJisage = true;
								noBr = true;//ブロック字下げなので改行なし
							}
						} catch (Exception e) { e.printStackTrace(); }
						//字下げ複合は字下げのみに変更
						try {
							Matcher m2 = chukiPatternMap.get("字下げその他").matcher(chukiTag);
							if (m2.find()) {
								int arg0 = Integer.parseInt(CharUtils.fullToHalf(m2.group(1)));
								out.write(chukiMap.get("字下げその他1")[0]+arg0);
								out.write(chukiMap.get("字下げその他2")[0]);
								//字下げフラグ処理
								if (inJisage) {
									if (pPrinted) {
										pPrinted = false;
										out.write("</p>");
									}
									out.write(chukiMap.get("字下げ省略")[0]);
								}
								else inJisage = true;
								noBr = true;//ブロック字下げなので改行なし
							}
						} catch (Exception e) { e.printStackTrace(); }
					}
				}
			}
			
			begin = chukiStart+chukiTag.length();
		}
		//注記の後ろの残りの文字
		if (begin < ch.length) {
			if (lineNum != idLineNum) { idNum++; idLineNum = lineNum; };
			if (withIdSpan) out.write("<span id=\"kobo."+idNum+"."+(lineSpanIdx++)+"\">");//栞用span開始
			this.printRubyText(out, ch, begin, ch.length, false);
			if (withIdSpan) out.write("</span>");//栞用span閉じる
			
			//改ページ後の章名称変更
			if (!this.chapterStarted) {
				String chapterName = this.replaceToPlain(line.substring(begin, ch.length));
				chapterName = chapterName.replaceAll("^=+", "").replaceAll("=+$", "");
				chapterName = chapterName.replaceAll("^-+", "").replaceAll("-+$", "");
				if (chapterName.length() >0) {
					this.chapterStarted = true;
					this.writer.updateChapterName(chapterName.length()>64 ? chapterName.substring(0, 64) : chapterName);
				}
			}
		}
		//行末タグを追加
		if (bufSuf.length() > 0) out.write(bufSuf.toString());
		
		//字下げの場合に<p>が行頭にあったら閉じる
		if (pPrinted) {
			out.write("</p>");
			out.write("\n");
		}
		//改行
		else if (!noBr) {
			out.write(chukiMap.get("改行")[0]);
			out.write("\n");
		}
	}
	
	String replaceToPlain(String str)
	{
		return str.replaceAll("<[^>]+>", "").replaceAll("《[^》]+》", "").replaceAll("［＃.+?］", "").replaceAll("[｜|※]","").replaceAll("^[ |　]+","").replaceAll("[ |　]+$","");
	}
	
	/** ルビタグに変換して出力
	 * 特殊文字は※が前についているので※の後ろの文字を利用しルビ内なら開始位置以降の文字をずらす
	 * ・ルビ （前｜漢字《かんじ》 → 前<ruby><rbase>漢字</rbase><rtop>かんじ</rtop></ruby>）
	 * ・文字置換 （〝〟―）
	 * ・半角2文字のみの数字と!?を縦横中変換
	 * < と > は &lt; &gt; に置換
	 * @param out 出力先Writer
	 * @param ch ルビ変換前の行文字列
	 * @param begin 変換範囲開始位置
	 * @param end 変換範囲終了位置
	 * @param noRuby ルビタグ禁止 縦横中変換も禁止 */
	private void printRubyText(BufferedWriter out, char[] ch, int begin, int end, boolean noRuby) throws IOException
	{
		// ルビと文字変換
		int rubyStart = -1;// ルビ開始位置
		int rubyTopStart = -1;// ぶりがな開始位置
		boolean inRuby = false;
		boolean isAlphaRuby = false; //英字へのルビ
		for (int i = begin; i < end; i++) {
			switch (ch[i]) {
			case '〝': ch[i] = '“'; break;
			case '〟': ch[i] = '”'; break;
			case '―': ch[i] = '─'; break;
			case '※':
				//外字変換処理でルビ文字と注記になる可能性のある＃が ※でエスケープされている (※《 ※》 ※｜ ※＃)
				//ルビ自動判別中は次の文字が漢字でもアルファベットでもないのでルビ対象がとして出力される
				//ルビ内で変換した場合はルビ開始位置の文字を１文字ずらす
				if (i+1 != end) {
					switch (ch[i+1]) {
					case '》':
					case '《':
					case '｜':
					case '＃':
						if (rubyStart > -1) {
							for (int j=i-1; j>=rubyStart; j--) {
								ch[j+1] = ch[j];
							}
							rubyStart++;
						}
						i++;
					}
				}
				break;
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				//数字2文字を縦横中で出力
				if (autoYoko&&!noRuby && !inRuby && i+1<ch.length && '0'<=ch[i+1] && ch[i+1]<='9') {
					//前後が半角かチェック
					if (i>0 && CharUtils.isHalf(ch[i-1])) break;
					if (i+2<ch.length && CharUtils.isHalf(ch[i+2])) break;
					//前まで出力
					if (rubyStart != -1) printText(out, ch, rubyStart, i - rubyStart);
					rubyStart = -1;
					out.write(chukiMap.get("縦中横")[0]);
					out.write(ch[i]);
					out.write(ch[i+1]);
					out.write(chukiMap.get("縦中横終わり")[0]);
					i++;
					continue;
				}
				break;
			case '!': case '?':
				//!?2文字を縦横中で出力
				if (autoYoko&&!noRuby && !inRuby && i+1<ch.length && (ch[i+1]=='!' || ch[i+1]=='?')) {
					//前後が半角かチェック
					if (i!=0 && CharUtils.isHalf(ch[i-1])) break;
					if (i+2<ch.length && CharUtils.isHalf(ch[i+2])) break;
					//前まで出力
					if (rubyStart != -1) printText(out, ch, rubyStart, i - rubyStart);
					rubyStart = -1;
					out.write(chukiMap.get("縦中横")[0]);
					out.write(ch[i]);
					out.write(ch[i+1]);
					out.write(chukiMap.get("縦中横終わり")[0]);
					i++;
					continue;
				}
				break;
			case '｜':
				//前まで出力
				if (rubyStart != -1) printText(out, ch, rubyStart, i - rubyStart);
				rubyStart = i + 1;
				inRuby = true;
				break;
			case '《':
				inRuby = true;
				rubyTopStart = i;
				break;
			}
			
			// ルビ内ならルビの最後でrubyタグ出力
			if (inRuby) {
				// ルビ終わり
				if (ch[i] == '》') {
					if (rubyStart != -1 && rubyTopStart != -1) {
						if (noRuby) 
							printText(out, ch, rubyStart, rubyTopStart - rubyStart); //本文
						else {
							//同じ長さで同じ文字なら一文字づつルビを振る
							if (rubyTopStart-rubyStart == i-rubyTopStart-1 && CharUtils.isSameChars(ch, rubyTopStart+1, i)) {
								for (int j=0; j<rubyTopStart-rubyStart; j++) {
									out.write(chukiMap.get("ルビ開始")[0]);
									printText(out, ch[rubyStart+j]); //本文
									out.write(chukiMap.get("ルビ文字")[0]);
									printText(out, ch[rubyTopStart+1+j]);//ルビ
									out.write(chukiMap.get("ルビ終了")[0]);
								}
							} else {
								out.write(chukiMap.get("ルビ開始")[0]);
								printText(out, ch, rubyStart, rubyTopStart-rubyStart); //本文
								out.write(chukiMap.get("ルビ文字")[0]);
								printText(out, ch, rubyTopStart+1, i-rubyTopStart-1);//ルビ
								out.write(chukiMap.get("ルビ終了")[0]);
							}
						}
					}
					inRuby = false;
					rubyStart = -1;
					rubyTopStart = -1;
				}
			} else {
				// 漢字チェック
				if (rubyStart == -1) {
					// ルビ中でなく漢字ならルビ開始チェック
					if (CharUtils.isKanji(i==0?(char)-1:ch[i-1], ch[i], i+1>=ch.length?(char)-1:ch[i+1])) {
						rubyStart = i; isAlphaRuby = false;
					} else if (CharUtils.isHalf(ch[i]) || ch[i] == ' ') {
						//英字または空白なら英字ルビ
						rubyStart = i; isAlphaRuby = true;
					}
					// ルビ中でなく漢字以外は出力
					else {
						printText(out, ch[i]); isAlphaRuby = false;
					}
				} else {
					// ルビ開始チェック中で漢字以外または英字以外ならキャンセルして出力
					if (!CharUtils.isKanji(i==0?(char)-1:ch[i-1], ch[i], i+1>=ch.length?(char)-1:ch[i+1]) && !(isAlphaRuby && CharUtils.isHalf(ch[i]))) {
						// rubyStartから前までと現在位置の文字を出力するので+1
						printText(out, ch, rubyStart, i - rubyStart + 1);
						rubyStart = -1;
					}
				}
			}
		}
		if (rubyStart != -1) {
			// ルビ開始チェック中で漢字以外ならキャンセルして出力
			printText(out, ch, rubyStart, end - rubyStart);
		}
	}
	
	/** 出力バッファに複数文字出力 ラテン文字はグリフにして出力 */
	private void printText(BufferedWriter out, char[] ch, int begin, int length) throws IOException
	{
		for (int i=begin; i<begin+length; i++) {
			printText(out, ch[i]);
		}
	}
	/** 出力バッファに文字出力 ラテン文字をグリフにして出力 */
	private void printText(BufferedWriter out, char ch) throws IOException
	{
		//String str = latinConverter.toLatinGlyphString(ch);
		//if (str != null) out.write(str);
		//else out.write(ch);
		switch (ch) {
		case '<':
			out.write("&lt;");
			break;
		case '>':
			out.write("&gt;");
			break;
		default:
			out.write(ch);
		}
	}
}
