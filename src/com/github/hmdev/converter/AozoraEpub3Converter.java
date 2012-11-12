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
import com.github.hmdev.info.BookInfo.TitleType;
import com.github.hmdev.info.ChapterLineInfo;
import com.github.hmdev.util.CharUtils;
import com.github.hmdev.util.ImageInfoReader;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.writer.Epub3Writer;

/**
 * 青空文庫テキストをePub3形式のXHTMLに変換
 * Licence: Non-commercial use only.
 */
public class AozoraEpub3Converter
{
	//---------------- Properties ----------------//
	/** UTF-8以外の文字を代替文字に変換 */
	boolean userAlterCharEscape = false;
	
	/** 半角2文字の数字と!?を縦横中に変換 */
	boolean autoYoko = true;
	/** 半角数字1桁を自動縦中横 */
	boolean autoYokoNum1 = true;
	/** 半角数字3桁を自動縦中横 */
	boolean autoYokoNum3 = true;
	/** !か?の3文字連続を自動縦中横 */
	boolean autoYokoEQ3 = true;
	
	/** 栞用のidを行頭の<p>につけるならtrue */
	boolean withMarkId = false;
	
	/** コメントブロックを非表示 */
	public boolean commentPrint = false;
	/** コメントブロック内注記変換 */
	public boolean commentConvert = false;
	
	/** 改ページ後を目次に追加 */
	boolean chapterSection = true;
	
	/** 4バイト文字を表示 */
	boolean gaiji32 = false;
	
	/** 先頭ページを左右中央 コメント行前の8行まで
	 * コメント行がない場合と9行以上の場合は無効になる */
	boolean middleTitle = false;
	
	/** 奥付を別ページ */
	boolean separateColophon = true;
	
	/** 文中全角スペース(1文字)の禁則処理
	 * 1なら余白付き半角スペース
	 * 2なら追い出しのために前の文字の後ろに余白 */
	int spaceHyphenation = 0;
	
	
	/** 強制改ページバイト数 */
	int forcePageBreak = 0;
	/** 空行でのみ強制改ページ */
	int forcePageBreakEmptyLine = 0;
	/** 見出しでのみ強制改ページ */
	int forcePageBreakChapter = 0;
	/** 強制改行対象の空行後のパターン */
	Pattern forcePageBreakPattern = null;
	
	//---------------- Chapter Properties ----------------//
	boolean autoChapterName = false;
	boolean autoChapterNum = false;
	boolean autoChapterNumOnly = false;
	
	/** 章名の最大文字数 */
	int maxChapterNameLength = 64;
	
	//---------------- Chapter Infos ----------------//
	//TODO パターンはファイルまたは設定から読み込む
	/** 章の数値文字パターン */
	char[] chapterNumChar = {'0','1','2','3','4','5','6','7','8','9',
			'０','１','２','３','４','５','６','７','８','９',
			'〇','一','二','三','四','五','六','七','八','九','十','百',
			'壱','弐','参','肆','伍',
			'Ⅰ','Ⅱ','Ⅲ','Ⅳ','Ⅴ','Ⅵ','Ⅶ','Ⅷ','Ⅸ','Ⅹ','Ⅺ','Ⅻ'};
	/** 章番号の後の証明との間の文字 */
	char[] chapterSeparator = {' ','　','-','－','「','―','『'};
	
	/** 章名数字無し */
	String[] chapterName = new String[]{"プロローグ","エピローグ","序","序章","終章","間章","幕間"};
	/** 章名数字前 suffixのみは空文字 */
	String[] chapterNumPrefix = new String[]{"第","その", ""};//new String[]{"第","その","（", ""};
	/** 章名数字後 prefixに対応する複数のsuffixを指定 指定なしなら空文字 */
	String[][] chapterNumSuffix = new String[][]{{"話","章","篇","部","節","幕"},{""},{"章"}};//new String[][]{{"話","章","篇","部","節","幕"},{""},{"章"}};
	
	/** 章の注記と目次階層レベル指定 大見出し 中見出し 小見出し 見出し */
	HashMap<String, Integer> chapterChukiMap = null;
	
	//---------------- Flags Variables ----------------//
	/** 字下げ 字下げ開始した行番号を入れておく */
	int inJisage = -1;
	/** 縦中横内 */
	boolean inTcy = false;
	/** 横組み内 */
	boolean inYoko = false;
	
	//---------------- パターン ----------------//
	/** 注記パターン */
	final static Pattern chukiPattern = Pattern.compile("(［＃.+?］)|(<.+?>)");
	/** 外字注記パターン */
	final static Pattern gaijiChukiPattern = Pattern.compile("(※［＃.+?］)|(〔.+?〕)|(／″?＼)");
	/** 前方参照注記パターン */
	final static Pattern chukiSufPattern = Pattern. compile("［＃「([^］]+)」([^］]+?)］" );
	
	/** 先頭注記内側のパターン */
	final static Pattern chukiLeftPattern = Pattern.compile("^［＃(.+?)］");
	/** ファイル名 [著作者] 表題.txt から抽出するパターン */
	final static Pattern fileNamePattern = Pattern.compile("\\[(.+?)\\]( |　)*(.+?)(\\(|（|\\.)");
	
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
	public static HashSet<String> chukiFlagPageBreak = new HashSet<String>();
	/** 注記フラグ 左右中央 key = 注記名 */
	static HashSet<String> chukiFlagMiddle = new HashSet<String>();
	
	/** 注記を返す 画像のみの出力用 */
	public String[] getChukiValue(String key)
	{
		return chukiMap.get(key);
	}
	
	/** 後述注記→タグ変換用
	 * key=注記文字列 (「」内削除)
	 * value= { 前タグ, 後タグ } */
	static HashMap<String, String[]> sufChukiMap = new HashMap<String, String[]>();
	
	static HashMap<String, Pattern> chukiPatternMap = new HashMap<String, Pattern>();
	
	/** 文字置換マップ */
	static HashMap<Character, String> replaceMap = null;
	/** 文字置換マップ 2文字用 */
	static HashMap<String, String> replace2Map = null;
	
	/** 「基本ラテン文字のみによる拡張ラテン文字Aの分解表記」の変換クラス */
	static LatinConverter latinConverter;
	
	/** 外字注記タグをUTF-8・グリフタグ・代替文字に変換するクラス */
	static AozoraGaijiConverter ghukiConverter;
	
	/** Epub圧縮出力用クラス */
	Epub3Writer writer;
	
	////////////////////////////////
	// 変換前に初期化すること
	/** 改ページ後の文字数 */
	int pageByteSize;
	/** セクション内の文字数(変換前の注記タグ含む) 空ページチェック用 */
	int sectionCharLength;
	
	/** 現在処理中の行番号 */
	public int lineNum;
	
	/** 栞用ID連番 行内番号 */
	int idNum;
	
	/** BookInfo */
	public BookInfo bookInfo;
	
	////////////////////////////////
	//改ページトリガ ファイル名は入れ替えて利用する
	/** 改ページ通常 */
	final static PageBreakTrigger pageBreakNormal = new PageBreakTrigger(true, false, PageBreakTrigger.IMAGE_PAGE_NONE);
	/** 改ページ左右中央 */
	final static PageBreakTrigger pageBreakMiddle = new PageBreakTrigger(true, true, PageBreakTrigger.IMAGE_PAGE_NONE);
	/** 改ページ画像単一ページ サイズに応じて自動調整 */
	final static PageBreakTrigger pageBreakImageAuto = new PageBreakTrigger(true, false, PageBreakTrigger.IMAGE_PAGE_AUTO);
	/** 改ページ画像単一ページ 幅100% */
	final static PageBreakTrigger pageBreakImageW = new PageBreakTrigger(true, false, PageBreakTrigger.IMAGE_PAGE_W);
	/** 改ページ画像単一ページ 高さ100% */
	final static PageBreakTrigger pageBreakImageH = new PageBreakTrigger(true, false, PageBreakTrigger.IMAGE_PAGE_H);
	/** 改ページ画像単一ページ 拡大しない */
	final static PageBreakTrigger pageBreakImageNoFit = new PageBreakTrigger(true, false, PageBreakTrigger.IMAGE_PAGE_NOFIT);
	/** 改ページ「底本：」の前 */
	final static PageBreakTrigger pageBreakNoChapter = new PageBreakTrigger(true, false, PageBreakTrigger.IMAGE_PAGE_NONE, true);
	
	/** 見出し仮対応出力用
	 * 章の最初の本文をsetChapterNameでセットしたらtrue */
	boolean chapterStarted = true;
	
	/** コンストラクタ
	 * 変換テーブルやクラスがstaticで初期化されていなければ初期化
	 * @param _msgBuf ログ出力用バッファ
	 * @throws IOException */
	public AozoraEpub3Converter(Epub3Writer writer) throws IOException
	{
		this.writer = writer;
		
		//初期化されていたら終了
		if (inited) return;
		
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
						if (values.length == 1) tags = new String[]{""};
						else if (values.length > 2 && values[2].length() > 0) tags = new String[]{values[1], values[2]};
						else tags = new String[]{values[1]};
						chukiMap.put(values[0], tags);
						//注記フラグ
						if (values.length > 3 && values[3].length() > 0) {
							switch (values[3].charAt(0)) {
							case '1': chukiFlagNoBr.add(values[0]); break;
							case '2': chukiFlagNoRubyStart.add(values[0]); break;
							case '3': chukiFlagNoRubyEnd.add(values[0]); break;
							case 'P': chukiFlagPageBreak.add(values[0]); break;
							case 'M': chukiFlagPageBreak.add(values[0]);
								chukiFlagMiddle.add(values[0]);
								break;
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
		chukiPatternMap.put("折り返し", Pattern.compile("^［＃ここから([０-９]+)字下げ、折り返して([０-９]+)字下げ(.*)］"));
		chukiPatternMap.put("字下げ字詰め", Pattern.compile("^［＃ここから([０-９]+)字下げ、([０-９]+)字詰め.*］"));
		chukiPatternMap.put("字下げ複合", Pattern.compile("^［＃ここから([０-９]+)字下げ.*］"));
		chukiPatternMap.put("字下げ終わり複合", Pattern.compile("^［＃ここで字下げ.*終わり"));
		
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
		
		//単純文字置換
		File replaceFile = new File("replace.txt");
		if (replaceFile.exists()) {
			replaceMap = new HashMap<Character, String>();
			replace2Map = new HashMap<String, String>();
			src = new BufferedReader(new InputStreamReader(new FileInputStream(replaceFile), "UTF-8"));
			lineNum = 0;
			try {
				while ((line = src.readLine()) != null) {
					lineNum++;
					if (line.length() > 0 && line.charAt(0)!='#') {
						try {
							String[] values = line.split("\t");
							if (values[0].length() == 1) {
								replaceMap.put(values[0].charAt(0), values[1]);
							} else if (values[0].length() == 2) {
									replace2Map.put(values[0], values[1]);
							} else {
								LogAppender.append("[ERROR] "+replaceFile.getName()+" ("+lineNum+") too long : "+line+"\n");
							}
						} catch (Exception e) {
							LogAppender.append("[ERROR] "+replaceFile.getName()+" ("+lineNum+") : "+line+"\n");
						}
					}
				}
			} finally {
				src.close();
			}
		}
		
		inited = true;
	}
	
	/**  栞用id付きspanの出力設定
	 * @param withIdSpan 栞用id付きspanを出力するならtrue */
	public void setWithMarkId(boolean withIdSpan)
	{
		this.withMarkId = withIdSpan;
	}
	/**  3文字の半角数字 3文字の!か?の回転を設定
	 * @param autoYoko 回転を設定するならtrue */
	public void setAutoYoko(boolean autoYoko, boolean autoYokoNum1, boolean autoYokoNum3)
	{
		this.autoYoko = autoYoko;
		this.autoYokoNum1 = autoYokoNum1;
		this.autoYokoNum3 = autoYokoNum3;
	}
	/**  4バイト文字変換を設定
	 * @param gaiji32 4バイト文字変換するならtrue */
	public void setGaiji32(boolean gaiji32)
	{
		this.gaiji32 = gaiji32;
	}
	/** 先頭ページを左右中央 コメント行前の8行まで
	 * コメント行がない場合と9行以上の場合は無効になる */
	public void setMiddleTitle(boolean middleFirst)
	{
		this.middleTitle = middleFirst;
	}
	
	/** コメント行内出力設定 */
	public void setCommentPrint(boolean commentPrint, boolean commentConvert)
	{
		this.commentPrint = commentPrint;
		this.commentConvert = commentConvert;
	}
	
	/** コメント行内出力設定 */
	public void setChapterLevel(int maxLength, boolean section, boolean h, boolean h1, boolean h2, boolean h3, boolean chapterName, boolean chapterNum, boolean chapterNumOnly)
	{
		this.maxChapterNameLength = maxLength;
		
		this.chapterSection = section;
		//見出し
		if (chapterChukiMap == null) chapterChukiMap = new HashMap<String, Integer>();
		else chapterChukiMap.clear();
		if (h) {
			chapterChukiMap.put("ここから見出し", 2);
			chapterChukiMap.put("見出し", 2);
		}
		if (h1) {
			chapterChukiMap.put("ここから大見出し", 2);
			chapterChukiMap.put("大見出し", 2);
		}
		if (h2) {
			chapterChukiMap.put("ここから中見出し", 3);
			chapterChukiMap.put("中見出し", 3);
		}
		if (h3) {
			chapterChukiMap.put("ここから小見出し", 4);
			chapterChukiMap.put("小見出し", 4);
		}
		
		this.autoChapterName = chapterName;
		this.autoChapterNum = chapterNum;
		this.autoChapterNumOnly = chapterNumOnly;
		
	}
	
	public void setSpaceHyphenation(int type)
	{
		this.spaceHyphenation = type;
	}
	
	/** 自動強制改行設定 */
	public void setForcePageBreak(int forcePageBreak, int emptyLine, int chapterLevel, Pattern pattern)
	{
		this.forcePageBreak = forcePageBreak;
		this.forcePageBreakEmptyLine = emptyLine;
		this.forcePageBreakChapter = chapterLevel;
		this.forcePageBreakPattern = pattern;
	}
	
	/** タイトルと著作者を取得. 行番号も保存して出力時に変換出力
	 * 章洗濯用に見出しの行もここで取得
	 * @param src 青空テキストファイルのReader
	 * @param imageInfos テキスト内の画像ファイル名を格納して返却
	 * @param titleType 表題種別
	 * @param coverFileName 表紙ファイル名 nullなら表紙無し ""は先頭ファイル "*"は同じファイル名 */
	public BookInfo getBookInfo(BufferedReader src, ImageInfoReader imageInfoReader, TitleType titleType) throws IOException
	{
		BookInfo bookInfo = new BookInfo();
		
		String line;
		this.lineNum = -1;
		//前の行のバッファ [1行前, 2行前]
		String[] preLines = new String[]{null, null};
		
		//コメントブロック内
		boolean inComment = false;
		//コメントが始まったらtrue
		boolean firstCommentStarted = false;
		
		//最初のコメント開始行
		int firstCommentLineNum = -1;
		
		//先頭行
		String[] firstLines = new String[7];
		//先頭行の開始行番号
		int firstLineStart = -1;
		
		//タイトルページ開始行 画像等がなければ0 タイトルなしは-1
		int preTitlePageBreak = titleType==TitleType.NONE ? -1 : 0;
		
		//コメント内の行数
		int commentLineNum = 0;
		//コメント開始行
		int commentLineStart = -1;
		
		//直線の空行
		int lastEmptyLine = -1;
		
		//目次用見出し自動抽出
		boolean autoChapter = this.autoChapterName || this.autoChapterNum || this.autoChapterNumOnly;
		
		//最後まで回す
		while ((line = src.readLine()) != null) {
			this.lineNum++;
			
			//外字変換後に前方参照注記変換
			line = this.replaceChukiSufTag(this.convertGaijiChuki(line, true, false));
			
			//コメント除外 50文字以上をコメントにする
			if (line.startsWith("--------------------------------")) {
				if (!line.startsWith("--------------------------------------------------")) {
					LogAppender.append("[WARN] コメント行の文字数が足りません ("+this.lineNum+")\n");
				} else {
					if (firstCommentLineNum == -1) firstCommentLineNum = this.lineNum;
					//コメントブロックに入ったらタイトル著者終了
					firstCommentStarted = true;
					if (inComment) {
						//コメント行終了
						if (commentLineNum > 20) LogAppender.append("[WARN] コメントが "+commentLineNum+" 行 ("+commentLineStart+"-"+this.lineNum+")\n");
						commentLineNum = 0;
						inComment = false; continue;
					}
					else {
						//コメント行開始
						commentLineStart = this.lineNum;
						inComment = true;
						continue;
					}
				}
				if (inComment) commentLineNum++;
			}
			
			//空行チェック
			if (line.equals("")) {
				lastEmptyLine = lineNum;
				//空行なので次の行へ
				continue;
			}
			
			//2行前が改ページと画像の行かをチェックして行番号をbookInfoに保存
			this.checkImageOnly(bookInfo, preLines, line, this.lineNum);
			
			//画像のファイル名の順番を格納
			Matcher m = chukiPattern.matcher(line);
			while (m.find()) {
				String chukiTag = m.group();
				String chukiName = chukiTag.substring(2, chukiTag.length()-1);
				
				//見出し行のチェック
				if (chapterChukiMap.containsKey(chukiName)) {
					ChapterLineInfo chapterLineInfo = new ChapterLineInfo(chapterChukiMap.get(chukiName), lastEmptyLine==lineNum-1);
					//注記の後に文字がなければブロックなので次の行 (次の行にブロック注記はこない？)
					if (line.length() == m.start()+chukiTag.length()) bookInfo.addChapterLine(lineNum+1, chapterLineInfo);
					else bookInfo.addChapterLine(lineNum, chapterLineInfo);
				}
				
				String lowerChukiTag = chukiTag.toLowerCase();
				int imageStartIdx = chukiTag.indexOf('（', 2);
				if (imageStartIdx > -1) {
					int imageEndIdx = chukiTag.indexOf("）");
					int imageDotIdx = chukiTag.indexOf('.', 2);
					//訓点送り仮名チェック ＃の次が（で.を含まない
					if (imageDotIdx > -1 && imageDotIdx < imageEndIdx) {
						//画像ファイル名を取得し画像情報を格納
						String imageFileName = this.getImageChukiFileName(chukiTag, imageStartIdx);
						//imageInfoReader.getImageInfo(imageFileName);
						imageInfoReader.addImageFileName(imageFileName);
					}
				} else if (lowerChukiTag.startsWith("<img")) {
					//src=の値抽出
					String imageFileName = this.getImageTagFileName(chukiTag);
					//imageInfoReader.getImageInfo(imageFileName);
					imageInfoReader.addImageFileName(imageFileName);
				}
			}
			
			//注記タグのない文字列に変換
			
			//見出し行パターン抽出 パターン抽出時はレベル+10
			//TODO パターンと目次レベルは設定可能にする 空行指定の場合はpreLines利用
			if (autoChapter && bookInfo.getChapterLevel(lineNum) == 0) {
				//文字列から注記と前の空白を除去
				String noChukiLine = line.replaceAll("［＃.+?］", "").replaceFirst("^[ |　|―]*", "");
				int noChukiLineLength = noChukiLine.length();
				if (this.autoChapterName) {
					boolean isChapter = false;
					//数字を含まない章名
					for (int i=0; i<this.chapterName.length; i++) {
						String prefix = this.chapterName[i];
						if (noChukiLine.startsWith(prefix)) {
							if (noChukiLine.length() == prefix.length()) { isChapter = true; break; } 
							else if (isChapterSeparator(noChukiLine.charAt(prefix.length()))) { isChapter = true; break; }
						}
					}
					//数字を含む章名
					if (!isChapter) {
						for (int i=0; i<this.chapterNumPrefix.length; i++) {
							String prefix = this.chapterNumPrefix[i];
							if (noChukiLine.startsWith(prefix)) {
								int idx = prefix.length();
								//次が数字かチェック
								while (noChukiLineLength > idx && isChapterNum(noChukiLine.charAt(idx))) idx++;
								if (idx <= prefix.length()) break; //数字がなければ抽出しない
								//後ろをチェック prefixに対応するsuffixで回す
								for (String suffix : this.chapterNumSuffix[i]) {
									if (!"".equals(suffix)) {
										if (noChukiLine.substring(idx).startsWith(suffix)) {
											idx += suffix.length();
											if (noChukiLine.length() == idx) { isChapter = true; break; } 
											else if (isChapterSeparator(noChukiLine.charAt(idx))) { isChapter = true; break; }
										}
									} else {
										if (noChukiLine.length() == idx) { isChapter = true; break; } 
										else if (isChapterSeparator(noChukiLine.charAt(idx))) { isChapter = true; break; }
									}
								}
							}
						}
					}
					if (isChapter)
						bookInfo.addChapterLine(lineNum, new ChapterLineInfo(13, lastEmptyLine==lineNum-1));
				}
				if (this.autoChapterNum) {
					//数字+章名
					int idx = 0;
					while (noChukiLineLength > idx && isChapterNum(noChukiLine.charAt(idx))) idx++;
					if (idx > 0) {
						if (noChukiLine.length() > idx && isChapterSeparator(noChukiLine.charAt(idx))) { 
							bookInfo.addChapterLine(lineNum, new ChapterLineInfo(13, lastEmptyLine==lineNum-1));
						}
					}
				}
				if (this.autoChapterNumOnly) {
					//数字のみの行
					int idx = 0;
					while (noChukiLineLength > idx && isChapterNum(noChukiLine.charAt(idx))) idx++;
					if (idx > 0) {
						if (noChukiLine.length() == idx) { 
							bookInfo.addChapterLine(lineNum, new ChapterLineInfo(13, lastEmptyLine==lineNum-1));
						}
					}
				}
			}
			//コメント行の後はタイトル取得はしない
			if (!firstCommentStarted) {
				String replaced = this.replaceToPlain(line);
				if (firstLineStart == -1) {
					//改ページチェック
					//タイトル前の改ページ位置を保存
					if (isPageBreakLine(line))
						preTitlePageBreak = lineNum;
					//文字の行が来たら先頭行開始
					if (replaced.length() > 0) {
						firstLineStart = this.lineNum;
						firstLines[0] = replaced;
					}
				} else {
					if (this.lineNum-firstLineStart > firstLines.length-1) {
						firstCommentStarted = true;
					} else {
						firstLines[this.lineNum-firstLineStart] = replaced;
					}
				}
			}
			//前の2行を保存
			preLines[1] = preLines[0];
			preLines[0] = line;
		}
		
		if (inComment) {
			LogAppender.append("[ERROR] コメントが閉じていません ("+commentLineStart+")\n");
		}
		
		//表題と著者を先頭行から設定
		bookInfo.setMetaInfo(titleType, firstLines, firstLineStart, firstCommentLineNum, preTitlePageBreak);
		
		//目次用の見出しに追加されたタイトル行は削除
		if (bookInfo.titleLine > 0) bookInfo.removeChapterLine(bookInfo.titleLine);
		if (bookInfo.orgTitleLine > 0) bookInfo.removeChapterLine(bookInfo.orgTitleLine);
		if (bookInfo.subTitleLine > 0) bookInfo.removeChapterLine(bookInfo.subTitleLine);
		if (bookInfo.subOrgTitleLine > 0) bookInfo.removeChapterLine(bookInfo.subOrgTitleLine);
		
		//左右中央指定で先頭がタイトルなら左右中央に設定
		if (this.middleTitle) {
			if (bookInfo.preTitlePageBreak > -1 && firstLineStart > -1) {
				//表題の後に改ページを設定
				bookInfo.addPageBreakLine(bookInfo.titleEndLine+1);
				//左右中央のタイトルページを目次より前に出す 出力前にsectionInfosからは削除しておく
				bookInfo.insertTitlePage = true;
			}
		}
		
		//目次ページの見出しを除外
		//前後2行前と2行後に3つ以上に抽出した見出しがある場合連続する見出しを除去
		bookInfo.excludeTocChapter();
		
		return bookInfo;
	}
	
	/** 文字が章の数字ならtrue */
	private boolean isChapterNum(char c)
	{
		for (char num : this.chapterNumChar) {
			if (c == num) return true;
		}
		return false;
	}
	/** 文字が章の後の区切り文字ならtrue */
	private boolean isChapterSeparator(char c)
	{
		for (char sep : this.chapterSeparator) {
			if (c == sep) return true;
		}
		return false;
	}
	
	/** 改ページのある行か判別 */
	private boolean isPageBreakLine(String line)
	{
		Matcher m = chukiLeftPattern.matcher(line);
		while (m.find()) {
			return chukiFlagPageBreak.contains(m.group(1));
		}
		return false;
	}
	
	/** 改ページ処理があったら次のセクションの情報をbookInfoに追加 */
	private void checkImageOnly(BookInfo bookInfo, String[] preLines, String line, int lineNum)
	{
		//現在の行が改ページ
		if (preLines[0] == null) return;
		if (line.indexOf('］') <= 3) return;
		String curChuki = line.substring(2, line.indexOf('］')); //現在行の行頭注記
		if (chukiFlagPageBreak.contains(curChuki)) {
			//2行前の行末が改ページまたは現在行が先頭から2行目
			if (preLines[1] == null ||
				(preLines[1].indexOf('］') > 3 && chukiFlagPageBreak.contains(preLines[1].substring(preLines[1].lastIndexOf('＃')+1, preLines[1].length()-1)))
				) {
				//1行前が画像
				if (
					(preLines[0].startsWith("［＃") && preLines[0].matches("^［＃.*（.+\\..+") && preLines[0].indexOf('］') == preLines[0].length()-1) ||
					(preLines[0].toLowerCase().startsWith("<img") && preLines[0].indexOf('>') == preLines[0].length()-1)
				) {
					//画像単一ページの画像行に設定
					String fileName = null;
					if (preLines[0].toLowerCase().startsWith("<img")) {
						fileName = getImageTagFileName(preLines[0]);
					} else {
						fileName = getImageChukiFileName(preLines[0], preLines[0].indexOf('（'));
					}
					bookInfo.addImageSectionLine(lineNum-1, fileName);
				}
			}
		}
	}
	
	/** imgタグからファイル名取得 */
	public String getImageTagFileName(String imgTag)
	{
		String lowerTag = imgTag.toLowerCase();
		int srcIdx = lowerTag.indexOf(" src=");
		if (srcIdx == -1) return null;
		int start = srcIdx+5;
		int end = -1;
		if (imgTag.charAt(start) == '"') end = imgTag.indexOf('"', start+1);
		else if (imgTag.charAt(start) == '\'') end = imgTag.indexOf('\'', start+1);
		if (end == -1) { end = imgTag.indexOf('>', start); start--; }
		if (end == -1) { end = imgTag.indexOf(' ', start); start--; }
		if (end != -1) return imgTag.substring(start+1, end).trim();
		return null;
	}
	/** 画像注記からファイル名取得
	 * @param startIdx 画像注記の'（'の位置 */
	public String getImageChukiFileName(String chukiTag, int startIdx)
	{
		int endIdx = chukiTag.indexOf('、', startIdx+1);
		if (endIdx == -1) endIdx = chukiTag.indexOf('）', startIdx+1);
		else endIdx = Math.min(endIdx, chukiTag.indexOf('）', startIdx+1));
		if (startIdx < endIdx) return chukiTag.substring(startIdx+1, endIdx);
		return null;
	}
	
	/** 青空テキストをePub3のXHTMLに変換
	 * @param _msgBuf ログ出力用バッファ
	 * @param out 出力先Writer
	 * @param src 入力テキストReader
	 * @param titleType  */
	public void convertTextToEpub3(BufferedWriter out, BufferedReader src, BookInfo bookInfo) throws IOException
	{
		//BookInfoの参照を保持
		this.bookInfo = bookInfo;
		
		String line;
		
		////////////////////////////////
		//変換開始字のメンバ変数の初期化
		this.pageByteSize = 0;
		this.sectionCharLength = 0;
		this.lineNum = -1;
		this.idNum = 0;
		//最初のページの改ページフラグを設定
		this.setPageBreakTrigger(pageBreakNormal);
		////////////////////////////////
		
		//コメントブロック内
		boolean inComment = false;
		
		//最初のセクション出力処理でfalseになる
		this.chapterStarted = true;
		//this.chapterFirstImageTitle = null;
		
		while ((line = src.readLine()) != null) {
			lineNum++;
			this.pageByteSize++;
			
			//強制改ページ行なら先頭で改ページ 空のページなら出力しない
			//この行が改ページ注記だと差うう中央が上書きされるので、改ページ注記処理でも左右中央を設定
			if (this.middleTitle && bookInfo.preTitlePageBreak == lineNum) this.setPageBreakTrigger(pageBreakMiddle);
			else if (bookInfo.isPageBreakLine(lineNum) && sectionCharLength > 0) this.setPageBreakTrigger(pageBreakNormal);
			
			//コメント除外
			if (line.startsWith("--------------------------------------------------")) {
				if (commentPrint) {
					if (inComment) { inComment = false; }
					else { inComment = true; }
				} else {
					if (inComment) { inComment = false; continue;
					} else {
						//コメント開始
						inComment = true; continue;
					}
				}
			}
			if (inComment) {
				if (commentPrint) {
					if (!commentConvert) {
						//そのまま出力
						StringBuilder buf = new StringBuilder();
						char[] ch = line.toCharArray();
						for (int idx=0; idx<ch.length; idx++) {
							switch (ch[idx]) {
							case '&': buf.append("&amp;"); break;
							case '<': buf.append("&lt;"); break;
							case '>': buf.append("&gt;"); break;
							default: buf.append(ch[idx]);
							}
						}
						this.printLineBuffer(out, buf, lineNum, false);
						continue;
					}
				} else {
					continue;
				}
			}
			
			//出力しない行を飛ばす
			if (bookInfo.isIgnoreLine(lineNum)) continue;
			
			if (lineNum == bookInfo.titleLine) {
				convertTextLineToEpub3(out, "［＃表題前］");
				convertTextLineToEpub3(out, line);
				convertTextLineToEpub3(out, "［＃表題後］");
			}
			else if (lineNum == bookInfo.orgTitleLine) {
				convertTextLineToEpub3(out, "［＃原題前］");
				convertTextLineToEpub3(out, line);
				convertTextLineToEpub3(out, "［＃原題後］");
			}
			else if (lineNum == bookInfo.subTitleLine) {
				convertTextLineToEpub3(out, "［＃副題前］");
				convertTextLineToEpub3(out, line);
				convertTextLineToEpub3(out, "［＃副題後］");
			}
			else if (lineNum == bookInfo.subOrgTitleLine) {
				convertTextLineToEpub3(out, "［＃副原題前］");
				convertTextLineToEpub3(out, line);
				convertTextLineToEpub3(out, "［＃副原題後］");
			}
			else if (lineNum == bookInfo.creatorLine) {
				convertTextLineToEpub3(out, "［＃著者前］");
				convertTextLineToEpub3(out, line);
				convertTextLineToEpub3(out, "［＃著者後］");
			}
			else if (lineNum == bookInfo.subCreatorLine) {
				convertTextLineToEpub3(out, "［＃副著者前］");
				convertTextLineToEpub3(out, line);
				convertTextLineToEpub3(out, "［＃副著者後］");
			}
			else {
				convertTextLineToEpub3(out, line);
			}
		}
		//終了処理
		this.bookInfo = null;
	}
	
	/** 文字列内の外字を変換
	 * ・外字はUTF-16文字列に変換
	 * ・特殊文字のうち 《》｜＃ は文字の前に※をつけてエスケープ
	 * @param line 行文字列
	 * @param escape ※での特殊文字のエスケープをするならtrue
	 * @return 外字変換済の行文字列 */
	public String convertGaijiChuki(String line, boolean escape)
	{
		return convertGaijiChuki(line, escape, true);
	}
	public String convertGaijiChuki(String line, boolean escape, boolean logged)
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
		
		Matcher m = gaijiChukiPattern.matcher(line);
		int begin = 0;
		int chukiStart = 0;
		
		//外字が無ければそのまま返却
		if (!m.find()) return line;
		
		//変換後の文字列を出力するバッファ
		StringBuilder buf = new StringBuilder();
		
		do {
			String chuki = m.group();
			chukiStart = m.start();
			
			buf.append(line.substring(begin, chukiStart));
			
			//外字はUTF-8に変換してそのまま継続
			if (chuki.charAt(0) == '※') {
				String[] chukiValues = chuki.substring(3, chuki.length()-1).split("、");
				//注記文字グリフ or 代替文字変換
				String gaiji = ghukiConverter.toAlterString(chukiValues[0]);
				//注記内なら注記タグは除外する
				if (gaiji != null) {
					if (isInnerChuki(line, m.start())) {
						gaiji = gaiji.replaceAll(chukiPattern.pattern(), "");
					}
				}
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
				if (gaiji == null) {
					gaiji = ghukiConverter.toUtf(chukiValues[0]);
				}
				
				//未サポート外字
				//if (unsupportGaiji.contains(gaiji)) {
				//Unicode32文字なら後ろに小書きで注記追加
				if (gaiji != null && gaiji.getBytes().length ==4) {
					if (logged) LogAppender.append("外字4バイト: ("+this.lineNum+") "+chuki+"\n");
					if (!gaiji32) {
						gaiji = "〓";
						if (!isInnerChuki(line, m.start())) {
							gaiji += "［＃行右小書き］（"+chukiValues[0]+"）［＃行右小書き終わり］";
						}
					}
				}
				//変換不可 画像指定付き外字なら画像注記に変更
				if (gaiji == null) {
					if (isInnerChuki(line, m.start())) {
						gaiji = "〓";
					} else {
						//画像指定外字
						int imageStartIdx = chuki.indexOf('（', 2);
						if (imageStartIdx > -1 && chuki.indexOf('.', 2) != -1) {
							//※を消して画像注記に変更
							gaiji = chuki.substring(1);
							if (logged) LogAppender.append("外字画像利用: ("+this.lineNum+") "+chuki+"\n");
						} else {
							//画像以外
							if (logged) LogAppender.append("外字未変換: ("+this.lineNum+") "+chuki+"\n");
							gaiji = "〓［＃行右小書き］（"+chukiValues[0]+"）［＃行右小書き終わり］";
						}
					}
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
				String inner = chuki.substring(1, chuki.length()-1);
				//〔の次が半角でなければ〔の中を再度外字変換
				if (!CharUtils.isHalfSpace(inner.toCharArray())) {
					buf.append('〔').append(convertGaijiChuki(inner, true)).append('〕');
				} else {
					//System.out.println(chuki);
					buf.append(latinConverter.toLatinString(inner));
				}
			} else if (chuki.charAt(0) == '／') {
				//くの字点
				if (chuki.charAt(1) == '″') buf.append("〴");
				else buf.append("〳");
				buf.append("〵");
			}
			
			begin = chukiStart+chuki.length();
		} while (m.find());
		
		//残りの文字をつなげて返却
		return buf.toString()+line.substring(begin);
	}
	
	/** 注記内かチェック
	 * @param gaijiStart これより前で注記が閉じていないかをチェック */
	private boolean isInnerChuki(String line, int gaijiStart)
	{
		int chukiStartCount = 0;
		int end = gaijiStart;
		while ((end = line.lastIndexOf("［＃", end-1)) != -1) {
			chukiStartCount++;
		}
		int chukiEndCount = 0;
		end = gaijiStart;
		while ((end = line.lastIndexOf('］', end-1)) != -1) {
			chukiEndCount++;
		}
		return chukiStartCount > chukiEndCount;
	}
	
	/** 前方参照注記をインライン注記に変換
	 * 重複等の法則が変則すぎるのでバッファを利用
	 * kentenの中にルビ、font、yokoが入る場合の入れ替えは後でやる */
	private String replaceChukiSufTag(String line)
	{
		Matcher m = chukiSufPattern.matcher(line);
		
		//マッチしなければそのまま返却
		if (!m.find()) return line;
		
		int chOffset = 0;
		StringBuilder buf = new StringBuilder(line);
		do {
			//System.out.println(m.group());
			String target = m.group(1);
			target = target.replaceAll("《[^》]+》", "");
			String chuki = m.group(2);
			String[] tags = sufChukiMap.get(chuki);
			int targetLength = target.length();
			int chukiTagStart = m.start();
			int chukiTagEnd = m.end();
			if (tags == null) {
				if (chuki.endsWith("の注記付き終わり")) {
					//［＃「」の注記付き終わり］の例外処理
					//［＃左に（はパターンにマッチしないので処理されない
					//ルビに置換
					buf.delete(chukiTagStart+chOffset, chukiTagEnd+chOffset);
					buf.insert(chukiTagStart+chOffset, "《"+target+"》");
					chOffset += targetLength+2 - (chukiTagEnd-chukiTagStart);
				} else if (chuki.endsWith("のルビ")) {
					if (target.indexOf("」に「") > -1) {
						targetLength = target.indexOf('」');
						//［＃「青空文庫」に「あおぞらぶんこ」のルビ］
						int targetStart = this.getTargetStart(buf, chukiTagStart, chOffset, targetLength);
						//後ろタグ置換
						buf.delete(chukiTagStart+chOffset, chukiTagEnd+chOffset);
						buf.insert(chukiTagStart+chOffset, "《"+target.substring(target.indexOf('「')+1)+"》");
						//前に ｜ insert
						buf.insert(targetStart, "｜");
					} else {
						//［＃「青空文庫」の左に「あおぞらぶんこ」のルビ］
						//左ルビ未対応 TODO 行左小書き？
					}
				} else if (chuki.endsWith("に×傍点")) {
					int targetStart = this.getTargetStart(buf, chukiTagStart, chOffset, targetLength);
					//後ろタグ置換
					buf.delete(chukiTagStart+chOffset, chukiTagEnd+chOffset);
					buf.insert(chukiTagStart+chOffset, "》");
					for (int i=0; i<targetLength; i++) buf.insert(chukiTagStart+chOffset, "×");
					buf.insert(chukiTagStart+chOffset, "《");
					//前に ｜ insert
					buf.insert(targetStart, "｜");
				}
				continue;
			}
			
			//置換済みの文字列で注記追加位置を探す
			int targetStart = this.getTargetStart(buf, chukiTagStart, chOffset, targetLength);
			
			//後ろタグ置換
			buf.delete(chukiTagStart+chOffset, chukiTagEnd+chOffset);
			buf.insert(chukiTagStart+chOffset, "［＃"+tags[1]+"］");
			//前タグinsert
			buf.insert(targetStart, "［＃"+tags[0]+"］");
			
			chOffset += tags[0].length() + tags[1].length() +6 - (chukiTagEnd-chukiTagStart);
		} while (m.find());
		
		//置換後文字列を返却
		return buf.toString();
	}
	/** 前方参照注記の前タグ挿入位置を取得 */
	private int getTargetStart(StringBuilder buf, int chukiTagStart, int chOffset, int targetLength)
	{
		//置換済みの文字列で注記追加位置を探す
		int idx = chukiTagStart-1+chOffset;
		boolean inTag = false;
		//間にあるタグをスタック
		Stack<String> tagStack = new Stack<String>();
		boolean isEndTag = false;
		int tagEnd = -1;
		while (targetLength > 0 && idx >= 0) {
			switch (buf.charAt(idx)) {
			case '※':
			case '｜':
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
		return idx + 1;
	}
	
	/** 青空テキスト行をePub3のXHTMLで出力
	 * @param out 出力先Writer
	 * @param line 変換前の行文字列 */
	private void convertTextLineToEpub3(BufferedWriter out, String line) throws IOException
	{
		convertTextLineToEpub3(out, line, lineNum, false);
	}
	/** 青空テキスト行をePub3のXHTMLで出力
	 * @param out 出力先Writer
	 * @param line 変換前の行文字列
	 * @param noBr 改行を出力しない */
	private void convertTextLineToEpub3(BufferedWriter out, String line, int lineNum, boolean noBr) throws IOException
	{
		StringBuilder buf = new StringBuilder();
		
		//外字変換後に前方参照注記変換
		line = this.replaceChukiSufTag(this.convertGaijiChuki(line, true));
		
		char[] ch = line.toCharArray();
		
		//ルビなしタグ開始なら+1
		int noRubyLevel = 0;
		
		StringBuilder bufSuf = new StringBuilder();
		// 注記タグ変換
		Matcher m = chukiPattern.matcher(line);
		int begin = 0;
		int chukiStart = 0;
		
		while (m.find()) {
			String chukiTag = m.group();
			String lowerChukiTag = chukiTag.toLowerCase();
			chukiStart = m.start();
			
			//fontの入れ子は可、圏点・縦横中はルビも付加
			//なぜか【＃マッチするので除外
			if (chukiTag.charAt(0) == '＃') {
				continue;
			}
			//<img </img> <a </a> 以外のタグは注記処理せず本文処理
			if (chukiTag.charAt(0) == '<' &&
				!(lowerChukiTag.startsWith("<img ") || lowerChukiTag.startsWith("</img>") || lowerChukiTag.startsWith("<a ") || lowerChukiTag.startsWith("</a>"))) {
				continue;
			}
			
			//注記の前まで本文出力
			if (begin < chukiStart) {
				this.convertRubyText(buf, ch, begin, chukiStart, noRubyLevel>0);
			}
			
			//注記→タグ変換
			String chukiName = chukiTag.substring(2, chukiTag.length()-1);
			
			//横組みチェック
			if (chukiName.endsWith("横組み")) inYoko = true;
			if (inYoko && chukiName.endsWith("横組み終わり")) inYoko = false;
			//縦中横チェック
			if (chukiName.startsWith("縦中横")) {
				if (chukiName.endsWith("終わり")) inTcy = false;
				else inTcy = true;
			}
			
			//ルビ無効チェック
			if (chukiFlagNoRubyStart.contains(chukiName)) noRubyLevel++;
			else if (chukiFlagNoRubyEnd.contains(chukiName)) noRubyLevel--;
			
			String[] tags = chukiMap.get(chukiName);
			if (tags != null) {
				//タグを出力しないフラグ 字下げエラー用
				boolean noAppend = false;
				
				////////////////////////////////////////////////////////////////
				//改ページ注記
				////////////////////////////////////////////////////////////////
				if (chukiFlagPageBreak.contains(chukiName) && !bookInfo.isNoPageBreakLine(lineNum)) {
					//改ページの前に文字があれば出力
					if (buf.length() > 0) this.printLineBuffer(out, buf, lineNum, true);
					
					noBr = true;
					//改ページの後ろに文字があれば</br>は出力
					if (ch.length > begin+chukiTag.length()) noBr = false;
					
					//字下げ状態エラー出力
					if (inJisage >= 0) {
						LogAppender.append("字下げ注記エラー: ("+inJisage+") \n");
						//字下げ省略処理
						//字下げフラグ処理
						buf.append(chukiMap.get("字下げ省略")[0]);
						inJisage = -1;
					}
					
					//改ページフラグ設定
					if (this.middleTitle && bookInfo.preTitlePageBreak == lineNum)
						this.setPageBreakTrigger(pageBreakMiddle);
					else if (chukiFlagMiddle.contains(chukiName)) {
						//左右中央
						this.setPageBreakTrigger(pageBreakMiddle);
					} else if (bookInfo.isImageSectionLine(lineNum+1)) {
						//次の行が画像単ページの表紙
						if (writer.getImageIndex() == bookInfo.coverImageIndex && bookInfo.insertCoverPage) {
							//先頭画像で表紙に移動なら改ページしない
							this.setPageBreakTrigger(null);
						} else {
							this.setPageBreakTrigger(pageBreakImageAuto);
							pageBreakImageAuto.imageFileName = bookInfo.getImageSectionFileName(lineNum+1);
						}
					} else {
						this.setPageBreakTrigger(pageBreakNormal);
					}
				}
				////////////////////////////////////////////////////////////////
				
				//字下げフラグ処理
				else if (chukiTag.endsWith("字下げ］")) {
					if (inJisage >= 0) {
						buf.append(chukiMap.get("字下げ省略")[0]);
					}
					//タグが閉じていればインラインなのでフラグは立てない
					if (tags.length > 1) inJisage = -1;//インライン
					else inJisage = lineNum; //ブロック開始
				}
				else if (chukiTag.endsWith("字下げ終わり］")) {
					 if (inJisage == -1) {
						 LogAppender.append("字下げ注記エラー：("+lineNum+")\n");
						 noAppend = true;
					 }
					inJisage = -1;
				}
				
				//タグ出力
				if (!noAppend) {
					buf.append(tags[0]);
					if (tags.length > 1) {
						bufSuf.insert(0, tags[1]);
					}
				}
				//ブロック注記チェック
				if (chukiFlagNoBr.contains(chukiName)) noBr = true;
			
			} else {
				
				//画像 (訓点 ［＃（ス）］ は . があるかで判断)
				// <img src="img/filename"/> → <object src="filename"/>
				// ［＃表紙（表紙.jpg）］［＃（表紙.jpg）］［＃「キャプション」（表紙.jpg、横321×縦123）入る）］
				// 
				int imageStartIdx = chukiTag.indexOf('（', 2);
				if (imageStartIdx > -1) {
					//訓点送り仮名チェック ＃の次が（で.を含まない
					if (imageStartIdx == 2 && chukiTag.endsWith("）］") && chukiTag.indexOf('.', 2) == -1) {
						buf.append(chukiMap.get("行右小書き")[0]);
						buf.append(chukiTag.substring(3, chukiTag.length()-2));
						buf.append(chukiMap.get("行右小書き終わり")[0]);
					} else if (chukiTag.indexOf('.', 2) == -1) {
						//拡張子を含まない
						LogAppender.append("注記未変換: ("+lineNum+") "+chukiTag+"\n");
					} else {
						//画像ファイル名置換処理実行
						String srcFilePath = this.getImageChukiFileName(chukiTag, imageStartIdx);
						if (srcFilePath == null) {
							LogAppender.append("注記エラー: ("+lineNum+") "+chukiTag+"\n");
						} else {
							String fileName = writer.getImageFilePath(srcFilePath.trim(), lineNum);
							if (fileName != null) { //先頭に移動してここで出力しない場合はnull
								//単ページ画像の場合は<p>タグを出さない
								if (bookInfo.isImageSectionLine(lineNum)) {
									noBr = true;
									buf.append(chukiMap.get("画像開始")[0]);
									buf.append(fileName);
									buf.append(chukiMap.get("画像終了")[0]);
								} else {
									//画像ページ種別取得
									int imagePageType = this.writer.getImagePageType(srcFilePath, this.tagLevel);
									if (imagePageType != PageBreakTrigger.IMAGE_PAGE_NONE) {
										//改ページの前に文字があれば出力
										if (buf.length() > 0) this.printLineBuffer(out, buf, lineNum, true);
										//単一ページ出力
										buf.append(chukiMap.get("画像開始")[0]);
										buf.append(fileName);
										buf.append(chukiMap.get("画像終了")[0]);
										this.printImagePage(out, buf, lineNum, fileName, imagePageType);
									} else {
										buf.append(chukiMap.get("画像開始")[0]);
										buf.append(fileName);
										buf.append(chukiMap.get("画像終了")[0]);
									}
								}
								//本文がなければ画像ファイル名が目次になる
								/*if (!this.chapterStarted && this.chapterFirstImageTitle == null) {
									String imageTitle = srcFilePath.substring(srcFilePath.lastIndexOf('/')+1);
									if (imageStartIdx > 0) imageTitle = chukiTag.substring(2, imageStartIdx);
									String chapterName = imageTitle;
									this.chapterFirstImageTitle = chapterName.length()>64 ? chapterName.substring(0, 64) : chapterName;
								}*/
							}
						}
					}
				} else if (lowerChukiTag.startsWith("<img")) {
					//src=の値抽出
					String srcFilePath = this.getImageTagFileName(chukiTag);
					if (srcFilePath == null) {
						LogAppender.append("画像注記エラー: ("+lineNum+") "+chukiTag+"\n");
					} else {
						//単ページ画像の場合は<p>タグを出さない
						if (bookInfo.isImageSectionLine(lineNum)) noBr = true;
						String fileName = writer.getImageFilePath(srcFilePath.trim(), lineNum);
						if (fileName != null) { //先頭に移動してここで出力しない場合はnull
							//単ページ画像の場合は<p>タグを出さない
							if (bookInfo.isImageSectionLine(lineNum)) {
								noBr = true;
								buf.append(chukiMap.get("画像開始")[0]);
								buf.append(fileName);
								buf.append(chukiMap.get("画像終了")[0]);
							} else {
								//画像ページ種別取得
								int imagePageType = this.writer.getImagePageType(srcFilePath, this.tagLevel);
								if (imagePageType != PageBreakTrigger.IMAGE_PAGE_NONE) {
									//改ページの前に文字があれば出力
									if (buf.length() > 0) this.printLineBuffer(out, buf, lineNum, true);
									//単一ページ出力
									buf.append(chukiMap.get("画像開始")[0]);
									buf.append(fileName);
									buf.append(chukiMap.get("画像終了")[0]);
									this.printImagePage(out, buf, lineNum, fileName, imagePageType);
								} else {
									buf.append(chukiMap.get("画像開始")[0]);
									buf.append(fileName);
									buf.append(chukiMap.get("画像終了")[0]);
								}
							}
							//本文がなければ画像ファイル名が目次になる
							/*if (!this.chapterStarted && this.chapterFirstImageTitle == null) {
								String imageTitle = srcFilePath.substring(srcFilePath.lastIndexOf('/')+1);
								int altIdx = lowerChukiTag.indexOf(" alt=");
								if (altIdx > -1) {
									start = altIdx + 5;
									end = -1;
									if (chukiTag.charAt(start) == '"') end = chukiTag.indexOf('"', start+1);
									if (chukiTag.charAt(start) == '\'') end = chukiTag.indexOf('\'', start+1);
									if (end > -1) imageTitle = chukiTag.substring(start+1, end);
								}
								String chapterName = imageTitle;
								this.chapterFirstImageTitle = chapterName.length()>64 ? chapterName.substring(0, 64) : chapterName;
							}*/
						}
					}
				}
				else {
					//インデント字下げ
					boolean patternMatched = false;
					Matcher m2 = chukiPatternMap.get("折り返し").matcher(chukiTag);
					if (m2.find()) {
						//字下げフラグ処理
						if (inJisage >= 0) buf.append(chukiMap.get("字下げ省略")[0]);
						inJisage = lineNum;
						
						int arg0 = Integer.parseInt(CharUtils.fullToHalf(m2.group(1)));
						int arg1 = Integer.parseInt(CharUtils.fullToHalf(m2.group(2)));
						buf.append(chukiMap.get("折り返し1")[0]+arg1);
						buf.append(chukiMap.get("折り返し2")[0]+(arg0-arg1));
						buf.append(chukiMap.get("折り返し3")[0]);
						
						noBr = true;//ブロック字下げなので改行なし
						patternMatched = true;
					}
					//インデント字下げ
					if (!patternMatched) {
						m2 = chukiPatternMap.get("字下げ字詰め").matcher(chukiTag);
						if (m2.find()) {
							//字下げフラグ処理
							if (inJisage >= 0) buf.append(chukiMap.get("字下げ省略")[0]);
							inJisage = lineNum;
							
							int arg0 = Integer.parseInt(CharUtils.fullToHalf(m2.group(1)));
							int arg1 = Integer.parseInt(CharUtils.fullToHalf(m2.group(2)));
							buf.append(chukiMap.get("字下げ字詰め1")[0]+arg0);
							buf.append(chukiMap.get("字下げ字詰め2")[0]+arg1);
							buf.append(chukiMap.get("字下げ字詰め3")[0]);
							
							noBr = true;//ブロック字下げなので改行なし
							patternMatched = true;
						}
					}
					//字下げ複合は字下げの後の複合注記をclassに追加
					if (!patternMatched) {
						m2 = chukiPatternMap.get("字下げ複合").matcher(chukiTag);
						if (m2.find()) {
							//字下げフラグ処理
							if (inJisage >= 0) buf.append(chukiMap.get("字下げ省略")[0]);
							inJisage = lineNum;
							
							int arg0 = Integer.parseInt(CharUtils.fullToHalf(m2.group(1)));
							buf.append(chukiMap.get("字下げ複合1")[0]+arg0);
							//複合注記クラス追加
							if (chukiTag.indexOf("破線罫囲み") > 0) buf.append(" ").append(chukiMap.get("字下げ破線罫囲み")[0]);
							else if (chukiTag.indexOf("罫囲み") > 0) buf.append(" ").append(chukiMap.get("字下げ罫囲み")[0]);
							if (chukiTag.indexOf("破線枠囲み") > 0) buf.append(" ").append(chukiMap.get("字下げ破線枠囲み")[0]);
							else if (chukiTag.indexOf("枠囲み") > 0) buf.append(" ").append(chukiMap.get("字下げ枠囲み")[0]);
							if (chukiTag.indexOf("中央揃え") > 0) buf.append(" ").append(chukiMap.get("字下げ中央揃え")[0]);
							//複合字下げclass閉じる
							buf.append(chukiMap.get("字下げ複合2")[0]);
							
							noBr = true;//ブロック字下げなので改行なし
							patternMatched = true;
						}
					}
					//字下げ終わり複合注記
					if (!patternMatched) {
						m2 = chukiPatternMap.get("字下げ終わり複合").matcher(chukiTag);
						if (m2.find()) {
							if (inJisage == -1) LogAppender.append("字下げ注記エラー：("+lineNum+")\n");
							else buf.append(chukiMap.get("ここで字下げ終わり")[0]);
							inJisage = -1;
							
							noBr = true;
							patternMatched = true;
						}
					}
					
					//注記未変換
					if (!patternMatched) {
						if (chukiTag.indexOf("底本では") == -1 && chukiTag.indexOf("に「ママ」") == -1 && chukiTag.indexOf("」はママ") == -1)
							LogAppender.append("注記未変換: ("+lineNum+") "+chukiTag+"\n");
					}
				}
			}
			begin = chukiStart+chukiTag.length();
		}
		//注記の後ろの残りの文字
		if (begin < ch.length) {
			this.convertRubyText(buf, ch, begin, ch.length, false);
		}
		//行末タグを追加
		if (bufSuf.length() > 0) buf.append(bufSuf.toString());
		
		//底本：で前が改ページでなければ改ページ追加
		if (separateColophon) {
			if (this.sectionCharLength > 0 && buf.length() > 2 && buf.charAt(0)=='底' && buf.charAt(1)=='本' && buf.charAt(2)=='：' ) {
				//字下げ状態エラー出力
				if (inJisage >= 0) {
					LogAppender.append("字下げ注記エラー : "+(inJisage+1)+"\n");
				} else {
					this.setPageBreakTrigger(pageBreakNoChapter);
				}
			}
		}
		
		//バッファを出力
		this.printLineBuffer(out, buf, lineNum, noBr);
	}
	
	/** ルビタグに変換して出力
	 * 特殊文字は※が前についているので※の後ろの文字を利用しルビ内なら開始位置以降の文字をずらす
	 * ・ルビ （前｜漢字《かんじ》 → 前<ruby><rbase>漢字</rbase><rtop>かんじ</rtop></ruby>）
	 * ・文字置換 （―）
	 * ・半角2文字のみの数字と!?を縦横中変換
	 * < と > は &lt; &gt; に置換
	 * @param buf 出力先バッファ
	 * @param ch ルビ変換前の行文字列
	 * @param begin 変換範囲開始位置
	 * @param end 変換範囲終了位置
	 * @param noRuby ルビタグ禁止 縦横中変換も禁止 */
	private void convertRubyText(StringBuilder buf, char[] ch, int begin, int end, boolean noRuby) throws IOException
	{
		//事前に《》の代替文字をエスケープ済※《 ※》 に変換
		//全角ひらがな漢字スペースの存在もついでにチェック
		for (int i = begin+1; i < end; i++) {
			switch (ch[i]) {
			case '<':
				if (ch[i-1] == '<' && (i == begin+1 || ch[i-2] != '<') && (end-1==i || ch[i+1] != '<')) {
					ch[i-1] = '※'; ch[i] = '《';
				}
				break;
			case '>':
				if (ch[i-1] == '>' && (i == begin+1 || ch[i-2] != '>') && (end-1==i || ch[i+1] != '>')) {
					ch[i-1] = '※'; ch[i] = '》';
				}
				break;
			case '＜':
				if (ch[i-1] == '＜' && (i == begin+1 || ch[i-2] != '＜') && (end-1==i || ch[i+1] != '＜')) {
					ch[i-1] = '※'; ch[i] = '《';
				}
				break;
			case '＞':
				if (ch[i-1] == '＞' && (i == begin+1 || ch[i-2] != '＞') && (end-1==i || ch[i+1] != '＞')) {
					ch[i-1] = '※'; ch[i] = '》';
				}
				break;
			}
		}
		
		// ルビと文字変換
		int rubyStart = -1;// ルビ開始位置
		int rubyTopStart = -1;// ぶりがな開始位置
		boolean inRuby = false;
		boolean isAlphaRuby = false; //英字へのルビ
		for (int i = begin; i < end; i++) {
			switch (ch[i]) {
			//case '〝': ch[i] = '“'; break;
			//case '〟': ch[i] = '”'; break;
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
				if (this.autoYoko && !(this.inYoko || this.inTcy || noRuby || inRuby)) {
					if (this.autoYokoNum3 && i+2<ch.length && CharUtils.isHalfNum(ch[i+1]) && CharUtils.isHalfNum(ch[i+2])) {
						//数字3文字
						//前後が半角かチェック
						if (i>0 && CharUtils.isHalf(ch[i-1])) break;
						if (i+3<ch.length && CharUtils.isHalf(ch[i+3])) break;
						//半角スペースの前後が半角文字
						if (i>1 && ch[i-1]==' ' && CharUtils.isHalf(ch[i-2])) break;
						if (i+4<ch.length && ch[i+3]==' ' && CharUtils.isHalf(ch[i+3])) break;
						//前まで出力
						if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
						rubyStart = -1;
						buf.append(chukiMap.get("縦中横")[0]);
						buf.append(ch[i]);
						buf.append(ch[i+1]);
						buf.append(ch[i+2]);
						buf.append(chukiMap.get("縦中横終わり")[0]);
						i+=2;
						continue;
					} else if (i+1<ch.length && CharUtils.isHalfNum(ch[i+1])) {
						//数字2文字
						//前後が半角かチェック
						if (i>0 && CharUtils.isHalf(ch[i-1])) break;
						if (i+2<ch.length && CharUtils.isHalf(ch[i+2])) break;
						//半角スペースの前後が半角文字
						if (i>1 && ch[i-1]==' ' && CharUtils.isHalf(ch[i-2])) break;
						if (i+3<ch.length && ch[i+2]==' ' && CharUtils.isHalf(ch[i+3])) break;
						//前まで出力
						if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
						rubyStart = -1;
						buf.append(chukiMap.get("縦中横")[0]);
						buf.append(ch[i]);
						buf.append(ch[i+1]);
						buf.append(chukiMap.get("縦中横終わり")[0]);
						i++;
						continue;
					} else if (this.autoYokoNum1 && (i==0 || !CharUtils.isHalfNum(ch[i-1])) && (i+1==ch.length || !CharUtils.isHalfNum(ch[i+1]))) {
						//数字1文字
						//前後が半角かチェック
						if (i>0 && CharUtils.isHalf(ch[i-1])) break;
						if (i+1<ch.length && CharUtils.isHalf(ch[i+1])) break;
						//半角スペースの前後が半角文字
						if (i>1 && ch[i-1]==' ' && CharUtils.isHalf(ch[i-2])) break;
						if (i+2<ch.length && ch[i+1]==' ' && CharUtils.isHalf(ch[i+2])) break;
						//前まで出力
						if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
						rubyStart = -1;
						buf.append(chukiMap.get("縦中横")[0]);
						buf.append(ch[i]);
						buf.append(chukiMap.get("縦中横終わり")[0]);
						continue;
					}
					//1月1日のような場合
					if (i+3<end && ch[i+1]=='月' && '0'<=ch[i+2] && ch[i+2]<='9' && (
						ch[i+3]=='日' || (i+4<ch.length && '0'<=ch[i+3] && ch[i+3]<='9' && ch[i+4]=='日'))) {
						//1月2日 1月10日 の1を縦中横
						//前まで出力
						if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
						rubyStart = -1;
						buf.append(chukiMap.get("縦中横")[0]);
						buf.append(ch[i]);
						buf.append(chukiMap.get("縦中横終わり")[0]);
						continue;
					}
					if (i>begin+1 && i+1<end && (ch[i-1]=='年' && ch[i+1]=='月' || ch[i-1]=='月' && ch[i+1]=='日' || ch[i-1]=='第' && (ch[i+1]=='刷' || ch[i+1]=='版' || ch[i+1]=='巻'))) {
						//年3月 + 月4日 + 第5刷 + 第6版 + 第7巻 の数字１文字縦中横
						//前まで出力
						if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
						rubyStart = -1;
						buf.append(chukiMap.get("縦中横")[0]);
						buf.append(ch[i]);
						buf.append(chukiMap.get("縦中横終わり")[0]);
						continue;
					}
					if (i>begin+2 && (ch[i-2]=='明'&&ch[i-1]=='治' || ch[i-2]=='大'&&ch[i-1]=='正' || ch[i-2]=='昭'&&ch[i-1]=='和' || ch[i-2]=='平'&&ch[i-1]=='成')) {
						//月5日 の5を縦中横
						//前まで出力
						if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
						rubyStart = -1;
						buf.append(chukiMap.get("縦中横")[0]);
						buf.append(ch[i]);
						buf.append(chukiMap.get("縦中横終わり")[0]);
						continue;
					}
				}
				break;
			case '!': case '?':
				//!?3文字を縦中横で出力
				if (autoYoko && autoYokoEQ3 && !(inYoko || inTcy || noRuby || inRuby) && i+2<ch.length && (ch[i+1]=='!' || ch[i+1]=='?') && (ch[i+2]=='!' || ch[i+2]=='?')) {
					//前後が半角かチェック
					if (i!=0 && CharUtils.isHalf(ch[i-1])) break;
					if (i+3<ch.length && CharUtils.isHalf(ch[i+3])) break;
					//半角スペースの前後が半角文字
					if (i>1 && ch[i-1]==' ' && CharUtils.isHalf(ch[i-2])) break;
					if (i+4<ch.length && ch[i+3]==' ' && CharUtils.isHalf(ch[i+4])) break;
					//前まで出力
					if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
					rubyStart = -1;
					buf.append(chukiMap.get("縦中横")[0]);
					buf.append(ch[i]);
					buf.append(ch[i+1]);
					buf.append(ch[i+2]);
					buf.append(chukiMap.get("縦中横終わり")[0]);
					i+=2;
					continue;
				} else {
					//!?2文字を縦横中で出力
					if (autoYoko && !(inYoko || inTcy || noRuby || inRuby) && i+1<ch.length && (ch[i+1]=='!' || ch[i+1]=='?')) {
						//前後が半角かチェック
						if (i!=0 && CharUtils.isHalf(ch[i-1])) break;
						if (i+2<ch.length && CharUtils.isHalf(ch[i+2])) break;
						//半角スペースの前後が半角文字
						if (i>1 && ch[i-1]==' ' && CharUtils.isHalf(ch[i-2])) break;
						if (i+3<ch.length && ch[i+2]==' ' && CharUtils.isHalf(ch[i+3])) break;
						//前まで出力
						if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
						rubyStart = -1;
						buf.append(chukiMap.get("縦中横")[0]);
						buf.append(ch[i]);
						buf.append(ch[i+1]);
						buf.append(chukiMap.get("縦中横終わり")[0]);
						i++;
						continue;
					}
				}
				break;
			case '｜':
				//前まで出力
				if (rubyStart != -1) convertChars(buf, ch, rubyStart, i - rubyStart, false);
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
							convertChars(buf, ch, rubyStart, rubyTopStart - rubyStart, false); //本文
						else {
							//同じ長さで同じ文字なら一文字づつルビを振る
							if (rubyTopStart-rubyStart == i-rubyTopStart-1 && CharUtils.isSameChars(ch, rubyTopStart+1, i)) {
								for (int j=0; j<rubyTopStart-rubyStart; j++) {
									buf.append(chukiMap.get("ルビ前")[0]);
									convertChar(buf, ch, rubyStart+j, false); //本文
									buf.append(chukiMap.get("ルビ")[0]);
									convertChar(buf, ch, rubyTopStart+1+j, true);//ルビ
									buf.append(chukiMap.get("ルビ後")[0]);
								}
							} else {
								buf.append(chukiMap.get("ルビ前")[0]);
								convertChars(buf, ch, rubyStart, rubyTopStart-rubyStart, false); //本文
								buf.append(chukiMap.get("ルビ")[0]);
								convertChars(buf, ch, rubyTopStart+1, i-rubyTopStart-1, true);//ルビ
								buf.append(chukiMap.get("ルビ後")[0]);
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
						convertChar(buf, ch, i, false); isAlphaRuby = false;
					}
				} else {
					// ルビ開始チェック中で漢字以外または英字以外ならキャンセルして出力
					if (!CharUtils.isKanji(i==0?(char)-1:ch[i-1], ch[i], i+1>=ch.length?(char)-1:ch[i+1]) && !(isAlphaRuby && CharUtils.isHalf(ch[i]))) {
						// rubyStartから前までと現在位置の文字を出力するので+1
						convertChars(buf, ch, rubyStart, i - rubyStart+1, false);
						rubyStart = -1;
					}
				}
			}
		}
		if (rubyStart != -1) {
			// ルビ開始チェック中で漢字以外ならキャンセルして出力
			convertChars(buf, ch, rubyStart, end - rubyStart, false);
		}
	}
	
	/** 出力バッファに複数文字出力 ラテン文字はグリフにして出力 */
	private void convertChars(StringBuilder buf, char[] ch, int begin, int length, boolean inRuby) throws IOException
	{
		for (int i=begin; i<begin+length; i++) {
			convertChar(buf, ch, i, inRuby);
		}
	}
	/** 出力バッファに文字出力 ラテン文字をグリフにして出力 */
	private void convertChar(StringBuilder buf, char[] ch, int idx, boolean inRuby) throws IOException
	{
		//NULL文字なら何も出力しない
		if (ch[idx] == '\0') return;
		
		//String str = latinConverter.toLatinGlyphString(ch);
		//if (str != null) out.write(str);
		//else out.write(ch);
		int length = buf.length();
		if (replaceMap != null) {
			String replaced = replaceMap.get(ch[idx]);
			//置換して終了
			if (replaced != null) {
				buf.append(replaced);
				return;
			}
		}
		if (idx != 0) {
			if (replace2Map != null) {
				String replaced = replace2Map.get(""+ch[idx-1]+ch[idx]);
				//置換して終了
				if (replaced != null) {
					buf.setLength(length-1);//1文字削除
					buf.append(replaced);
					return;
				}
			}
		}
		//文字の間の全角スペースを禁則調整
		if (!(inYoko || inTcy || inRuby)) {
			switch (this.spaceHyphenation) {
			case 1:
				if (ch[idx]=='　' && buf.length()>0 && buf.charAt(buf.length()-1)!='　' && (idx-1==ch.length || idx+1<ch.length && ch[idx+1]!='　')) {
					buf.append("<span class=\"fullsp\"> </span>");
					return;
				}
				break;
			case 2:
				if (ch[idx]=='　' && buf.length()>0 && buf.charAt(buf.length()-1)!='　' && (idx-1==ch.length || idx+1<ch.length && ch[idx+1]!='　')) {
					buf.append((char)(0x2000)).append((char)(0x2000));
					return;
				}
				/*if (idx+1<ch.length && ch[idx]!='　' && +ch[idx+1]=='　') {
					buf.append("<span class=\"withsp\">");
					buf.append(ch[idx]);
					buf.append("</span>");
					ch[idx+1]='\0';//出力しないNULL文字に変更
					return;
				}*/
				break;
			}
		}
		
		if (this.bookInfo.vertical) {
			switch (ch[idx]) {
			case '&': buf.append("&amp;"); break;
			case '<': buf.append("&lt;"); break;
			case '>': buf.append("&gt;"); break;
			case '≪': buf.append("《"); break;
			case '≫': buf.append("》"); break;
			case '“': buf.append("〝"); break;
			case '”': buf.append("〟"); break;
			//ローマ数字等 Readerで正立にする
			//その他右回転する記号: ¶⇔⇒≡√∇∂∃∠⊥⌒∽∝∫∬∮∑∟⊿≠≦≧∈∋⊆⊇⊂⊃∧∨↑↓→←
			case 'Ⅰ': case 'Ⅱ': case 'Ⅲ': case 'Ⅳ': case 'Ⅴ': case 'Ⅵ': case 'Ⅶ': case 'Ⅷ': case 'Ⅸ': case 'Ⅹ': case 'Ⅺ': case 'Ⅻ':
			case 'ⅰ': case 'ⅱ': case 'ⅲ': case 'ⅳ': case 'ⅴ': case 'ⅵ': case 'ⅶ': case 'ⅷ': case 'ⅸ': case 'ⅹ': case 'ⅺ': case 'ⅻ':
			case '⓪': case '①': case '②': case '③': case '④': case '⑤': case '⑥': case '⑦': case '⑧': case '⑨': case '⑩':
			case '⑪': case '⑫': case '⑬': case '⑭': case '⑮': case '⑯': case '⑰': case '⑱': case '⑲': case '⑳':
			case '㉑': case '㉒': case '㉓': case '㉔': case '㉕': case '㉖': case '㉗': case '㉘': case '㉙': case '㉚':
			case '㉛': case '㉜': case '㉝': case '㉞': case '㉟': case '㊱': case '㊲': case '㊳': case '㊴': case '㊵':
			case '㊶': case '㊷': case '㊸': case '㊹': case '㊺': case '㊻': case '㊼': case '㊽': case '㊾': case '㊿':
			case '△': case '▽': case '▲': case '▼': case '☆': case '★':
			case '♂': case '♀': case '♪': case '♭': case '§': case '†': case '‡': 
			case '÷': case '±': case '∀': case '∞': case '∴': case '∵': 
			case '‼': case '⁇': case '⁉': case '⁈':
			case '©': case '®': case '⁑': case '⁂':
			case '◐': case '◑': case '◒': case '◓': case '▷': case '▶': case '◁': case '◀':
			case '♤': case '♠': case '♢': case '♦': case '♡': case '♥': case '♧': case '♣': 
			case '☖': case '☗': case '☎': case '☁': case '☂': case '☃': case '♨': case '▱': case '⊿':
			case '☹': case '☺': case '☻':
			case '✓': case '␣': case '⏎': case '♩': case '♮': case '♫': case '♬': case 'ℓ': case '№': case '℡':
			case 'ℵ': case 'ℏ': case '℧':
				//縦中横の中でなければタグで括る
				if (!(inYoko || inTcy || inRuby)) {
					buf.append(chukiMap.get("縦中横")[0]);
					buf.append(ch[idx]);
					buf.append(chukiMap.get("縦中横終わり")[0]);
				} else {
					buf.append(ch[idx]);
				}
				break;
			default: buf.append(ch[idx]);
			}
		} else {
			switch (ch[idx]) {
			case '&': buf.append("&amp;"); break;
			case '<': buf.append("&lt;"); break;
			case '>': buf.append("&gt;"); break;
			default: buf.append(ch[idx]);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////
	// 画像単一ページチェック
	/** 前後に改ページを入れて画像を出力 
	 * @throws IOException */
	private void printImagePage(BufferedWriter out, StringBuilder buf, int lineNum,  String fileName, int imagePageType) throws IOException
	{
		//画像の前に改ページがある場合
		boolean hasPageBreakTriger = this.pageBreakTrigger != null && !this.pageBreakTrigger.noChapter;
		
		//画像単ページとしてセクション出力
		switch (imagePageType) {
		case PageBreakTrigger.IMAGE_PAGE_W:
			this.setPageBreakTrigger(pageBreakImageW);
			pageBreakImageW.imageFileName = fileName;
			break;
		case PageBreakTrigger.IMAGE_PAGE_H:
			this.setPageBreakTrigger(pageBreakImageH);
			pageBreakImageH.imageFileName = fileName;
			break;
		case PageBreakTrigger.IMAGE_PAGE_NOFIT:
			this.setPageBreakTrigger(pageBreakImageNoFit);
			pageBreakImageNoFit.imageFileName = fileName;
			break;
		default:
			this.setPageBreakTrigger(pageBreakImageAuto);
			pageBreakImageAuto.imageFileName = fileName;
		}
		printLineBuffer(out, buf, lineNum, true);
		
		//タイトル寄り前なら別処理
		if (this.lineNum < this.bookInfo.titleLine && this.middleTitle)
			this.setPageBreakTrigger(pageBreakMiddle);
		else if (hasPageBreakTriger) this.setPageBreakTrigger(pageBreakNormal);
		else this.setPageBreakTrigger(pageBreakNoChapter);
	}
	
	////////////////////////////////////////////////////////////////
	// 出力処理
	/** 本文があれば改ページするフラグ */
	PageBreakTrigger pageBreakTrigger = null;
	/** 左右中央の前の空行を除外するフラグ */
	boolean skipMiddleEmpty;
	/** 改ページ前の空行 */
	int printEmptyLines = 0;
	
	/** タグの階層 */
	int tagLevel = 0;
	
	/** 改ページ用のトリガを設定
	 * 設定済みだが連続行で書かれていたり空行除外で改行されていない場合は上書きされて無視される
	 * @param trigger 改ページトリガ nullなら改ページ設定キャンセル
	 * @param 改ページの後ろに文字がある場合に改行を出すならfalse */
	void setPageBreakTrigger(PageBreakTrigger trigger)
	{
		//改ページ前の空行は無視
		this.printEmptyLines = 0;
		this.pageBreakTrigger = trigger;
		if (this.pageBreakTrigger != null && this.pageBreakTrigger.isMiddle) this.skipMiddleEmpty = true;
	}
	
	/** 行の文字列を出力
	 * 改ページフラグがあれば改ページ処理を行う
	 * @param out 出力先
	 * @param buf 出力する行
	 * @param noBr pタグで括れない次以降の行で閉じるブロック注記がある場合
	 * @param chapterLevel Chapterレベル 指定無し=0, 大見出し=1, 中見出し=2, 見出し=2, 小見出し=3 (パターン抽出時は設定に合わせるか目次リストで選択したレベル)
	 * @throws IOException */
	private void printLineBuffer(BufferedWriter out, StringBuilder buf, int lineNum, boolean noBr) throws IOException
	{
		String line = buf.toString();
		int length = line.length();
		int idIdx = 1;
		String chapterId = null;
		
		if (length == 0) {
			//空行なら行数をカウント 左右中央の時の本文前の空行は無視
			if (!this.skipMiddleEmpty && !noBr) {
				this.printEmptyLines++;
			}
		} else {
			//バッファ内の文字列出力
			//見出し階層レベル
			int chapterLevel = this.bookInfo.getChapterLevel(lineNum);
			
			//タグの階層をチェック (強制改ページ判別用に先にやっておく)
			int tagStart = 0;
			int tagEnd = 0;
			boolean inTag = false;
			for (int i=0; i<length; i++) {
				if (inTag) {
					if (line.charAt(i) == '/' && line.charAt(i+1) == '>') tagEnd++;
					if (line.charAt(i) == '>') inTag = false;
				} else {
					if (line.charAt(i) == '<') {
						if (i<length-1 && line.charAt(i+1) == '/') tagEnd++;
						else tagStart++;
						inTag = true;
					}
				}
			}
			//強制改ページ処理
			//サイズが超えていて改ページトリガが設定されていなければ
			if (this.forcePageBreak > 0 && this.pageBreakTrigger == null && this.pageByteSize > this.forcePageBreak) {
				//タグの外の場合のみ
				if (this.tagLevel == 0) {
					//改ページ制限なし
					if (forcePageBreakChapter == 0 && forcePageBreakEmptyLine == 0) {
						 this.setPageBreakTrigger(pageBreakNoChapter);
					} else {
						//章での分割が有効
						if (forcePageBreakEmptyLine > 0 && this.printEmptyLines >= forcePageBreakEmptyLine) this.setPageBreakTrigger(pageBreakNoChapter);
						else if (forcePageBreakChapter > 0) {
							if (chapterLevel > 0) this.setPageBreakTrigger(pageBreakNoChapter);
							//次の行が見出しでタグの中なら1行前で改ページ
							else if (tagStart-tagEnd > 0 && this.bookInfo.getChapterLevel(lineNum+1) > 0) this.setPageBreakTrigger(pageBreakNoChapter);
						}
					}
				}
			}
			
			//改ページフラグが設定されていて、空行で無い場合
			if (this.pageBreakTrigger != null) {
				//空ページでの改ページ
				//if (sectionCharLength == 0) {
				//	out.write(chukiMap.get("改行")[0]);
				//}
				
				//改ページ処理
				if (this.pageBreakTrigger.isMiddle) {
					//左右中央
					this.writer.nextSection(out, lineNum, true, PageBreakTrigger.IMAGE_PAGE_NONE, null);
				} else {
					//その他
					this.writer.nextSection(out, lineNum, false, this.pageBreakTrigger.imagePageType, this.pageBreakTrigger.imageFileName);
				}
				
				//ページ情報初期化
				this.pageByteSize = 0;
				this.sectionCharLength = 0;
				this.chapterStarted = false;
				if (tagLevel > 0) LogAppender.append("[ERROR] タグが閉じていません ("+lineNum+")");
				this.tagLevel = 0;
				
				//改ページ目次非表示 抽出されていない場合
				if (this.pageBreakTrigger.noChapter) {
					this.writer.updateChapterName(null);
					this.chapterStarted = true;
				}
				this.pageBreakTrigger = null;
			}
			
			this.skipMiddleEmpty = false;
			//改ページの後に空行があれば行数がカウントされているので出力
			if (this.printEmptyLines > 0) {
				String br = chukiMap.get("改行")[0];
				for (int i=0; i<this.printEmptyLines; i++) {
					out.write("<p>");
					out.write(br);
					out.write("</p>\n");
				}
				this.pageByteSize += (br.length()+8)*this.printEmptyLines;
				this.printEmptyLines = 0;
			}
			if (noBr) {
				//見出し用のID設定
				if (chapterLevel > 0) {
					chapterId = "kobo."+lineNum+"."+(idIdx++);
					out.write("<span id=\""+chapterId+"\">");
					this.pageByteSize += (chapterId.length() + 19);
				}
			} else {
				//改行用のp出力 見出しなら強制ID出力
				if (this.withMarkId || chapterLevel > 0) {
					chapterId = "kobo."+lineNum+"."+(idIdx++);
					out.write("<p id=\""+chapterId+"\">");
					this.pageByteSize += (chapterId.length() + 14);
				}
				else {
					out.write("<p>");
					this.pageByteSize += 7;
				}
			}
			out.write(line);
			//ページバイト数加算
			if (this.forcePageBreak > 0) this.pageByteSize += line.getBytes("UTF-8").length;
			
			//改行のpまたは見出しspanを閉じる
			if (noBr) {
				if (chapterLevel > 0) out.write("</span>");
			} else {
				out.write("</p>");
				out.write("\n");
			}
			
			//タグの階層を変更
			this.tagLevel += tagStart-tagEnd;
			
			//見出しのChapter追加
			if (chapterLevel > 0) {
				String name = this.getChapterName(line);
				if (name.length() > 0) {
					//改ページ後のChapter出力を抑止
					this.chapterStarted = true;
					//自動抽出で+10されているのは1桁のレベルに戻す
					this.writer.addChapter(chapterId, name, chapterLevel%10);
				}
			}
			
			//改ページ後の章名を設定
			if (this.chapterSection && !this.chapterStarted) {
				if (lineNum == bookInfo.titleLine || lineNum > bookInfo.titleEndLine) {
					String name = this.getChapterName(line);
					//記号のみの行は除外
					if (name.replaceAll("◇|◆|□|■|＊|＋", "").length() > 0) {
						this.chapterStarted = true;
						this.writer.updateChapterName(name);
					}
				}
			}
			
			this.sectionCharLength += length;
			//バッファクリア
			buf.setLength(0);
		}
	}
	
	/** タグのない文字列に置換 */
	private String replaceToPlain(String str)
	{
		return str.replaceAll("［＃.+?］", "").replaceFirst("^[ |　|―]*", "").replaceAll("《[^》]+?》", "").replaceAll("〳〵", "く").replaceAll("〴〵", "ぐ").replaceAll("〻", "々").replaceFirst("^[ |　]+","").replaceFirst("[ |　]+$","");
	}
	private String getChapterName(String line)
	{
		String name = line.replaceAll("<span class=\"fullsp\"> </span>", "　").replaceAll(String.valueOf((char)(0x2000))+(char)(0x2000), "　")
				.replaceAll("<rt>[^<]+</rt>", "").replaceAll("<[^>]+>", "").replaceFirst("^(=|＝|-|―|─)(=|＝|-|―|─)+", "")
				.replaceFirst("(=|＝|-|―|─)(=|＝|-|―|─)+$", "");
		return name.length()>maxChapterNameLength ? name.substring(0, maxChapterNameLength)+"..." : name;
	}
}
