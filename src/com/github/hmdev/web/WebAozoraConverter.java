package com.github.hmdev.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.compress.utils.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.github.hmdev.util.LogAppender;

/** HTMLを青空txtに変換 */
public class WebAozoraConverter
{
	/** Singletonインスタンス格納 keyはFQDN */
	static HashMap<String, WebAozoraConverter> converters = new HashMap<String, WebAozoraConverter>();
	
	enum ExtractId {
		SERIES, TITLE, AUTHOR, DESCRIPTION,
		CHILD_NODE,HREF, HREF_REGEX, UPDATE,
		CONTENT_CHAPTER, CONTENT_SUBTITLE, CONTENT_ARTICLE, CONTENT_PREAMBLE, CONTENT_APPENDIX;
	}
	
	//設定ファイルから読み込むパラメータ
	/** リストページ抽出対象 HashMap<String key, String[]{cssQuery1, cssQuery2}> キーとJsoupのcssQuery(or配列) */
	HashMap<ExtractId, String[]> queryMap;
	HashMap<ExtractId, Vector<String[]>> replaceMap;
	
	/** テキスト出力先パス 末尾は/ */
	String dstPath;
	
	/** http?://fqdn/ の文字列 */
	String baseUri;
	
	/** 変換中のHTMLファイルのあるパス 末尾は/ */
	String htmlBaseUri;
	
	boolean canceled = false;
	
	/** fqdnに対応したパラメータ取得 
	 * @throws IOException */
	WebAozoraConverter(String fqdn, File configPath) throws IOException
	{
		if (configPath.isDirectory()) {
			for (File file : configPath.listFiles()) {
				if (file.isDirectory() && file.getName().equals(fqdn)) {
					
					//抽出情報
					File extractInfoFile = new File(configPath.getAbsolutePath()+"/"+fqdn+"/extract.txt");
					if (!extractInfoFile.isFile()) return;
					this.queryMap = new HashMap<ExtractId, String[]>();
					String line;
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(extractInfoFile), "UTF-8"));
					try {
						while ((line = br.readLine()) != null) {
							if (line.length() == 0 || line.charAt(0) == '#') continue;
							int idx = line.indexOf("\t");
							if (idx > 0) {
								this.queryMap.put(ExtractId.valueOf(line.substring(0, idx)), line.substring(idx+1).split(","));
							}
						}
					} finally{
						br.close();
					}
					
					//置換情報
					this.replaceMap = new HashMap<ExtractId, Vector<String[]>>();
					File replaceInfoFile = new File(configPath.getAbsolutePath()+"/"+fqdn+"/replace.txt");
					if (replaceInfoFile.isFile()) {
						br = new BufferedReader(new InputStreamReader(new FileInputStream(replaceInfoFile), "UTF-8"));
						try {
							while ((line = br.readLine()) != null) {
								if (line.length() == 0 || line.charAt(0) == '#') continue;
								String[] values = line.split("\t");
								if (values.length > 1) {
									ExtractId extractId = ExtractId.valueOf(values[0]);
									Vector<String[]> vecReplace = this.replaceMap.get(extractId);
									if (vecReplace == null) {
										vecReplace = new Vector<String[]>();
										this.replaceMap.put(extractId, vecReplace);
									}
									vecReplace.add(new String[]{values[1], values.length==2?"":values[2]});
								}
							}
						} finally{
							br.close();
						}
					}
					return;
				}
			}
		}
	}
	
	private boolean isValid()
	{
		return this.queryMap != null;
	}
	
	public void canceled()
	{
		this.canceled = true;
	}
	public boolean isCanceled()
	{
		return this.canceled;
	}
	
	/** fqdnに対応したインスタンスを生成してキャッシュして変換実行 */
	public static WebAozoraConverter createWebAozoraConverter(String urlString, File configPath) throws IOException
	{
		String baseUri = urlString.substring(0, urlString.indexOf('/', urlString.indexOf("//")+2));
		String fqdn = baseUri.substring(baseUri.indexOf("//")+2);
		WebAozoraConverter converter = converters.get(fqdn);
		if (converter == null) {
			converter = new WebAozoraConverter(fqdn, configPath);
			if (!converter.isValid()) {
				LogAppender.append("サイトの定義がありません: "+configPath.getName()+"/"+fqdn);
				LogAppender.append("\n");
				return null;
			}
			converters.put(fqdn, converter);
		}
		return converter;
		//return converter._convertToAozoraText(urlString, baseUri, fqdn, cachePath);
	}
	
	/** 変換実行 */
	public File convertToAozoraText(String urlString, File cachePath) throws IOException
	{
		this.canceled = false;
		this.baseUri = urlString.substring(0, urlString.indexOf('/', urlString.indexOf("//")+2));
		//String fqdn = baseUri.substring(baseUri.indexOf("//")+2);
		String listBaseUrl = urlString.substring(0, urlString.lastIndexOf('/')+1);
		String urlFilePath = urlString.substring(urlString.indexOf("//")+2).replaceAll("(\\?|\\&)", "/").replaceAll("(\\*|\\||\\<|\\>|\"|\\\\)", "_");
		String urlParentPath = urlFilePath;
		if (urlFilePath.endsWith("/")) urlFilePath += "index.html";
		else urlParentPath = urlFilePath.substring(0, urlFilePath.lastIndexOf('/')+1);
		
		//変換結果
		this.dstPath = cachePath.getAbsolutePath()+"/"+urlParentPath;
		File txtFile = new File(dstPath+"converted.txt");
		txtFile.getParentFile().mkdirs();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"));
		try {
		
		//更新情報格納先
		File updateInfoFile = new File(cachePath.getAbsolutePath()+"/"+urlParentPath+"update.txt");
		
		//urlStringのファイルをキャッシュ
		File cacheFile = new File(cachePath.getAbsolutePath()+"/"+urlFilePath);
		//LogAppender.append(urlString);
		cacheFile(urlString, cacheFile);
		//LogAppender.append(" : Loaded\n");
		try { Thread.sleep(500); } catch (InterruptedException e) { }
		
		//TODO パスのマッチングで一覧ページか判断
		boolean isListPage = true;
		
		if (isListPage) {
			//パスならlist.txtの情報を元にキャッシュ後に青空txt変換して改ページで繋げて出力
			Document doc = Jsoup.parse(cacheFile, null);
			
			//タイトル
			boolean hasTitle = false;
			String series = getQueryText(doc, this.queryMap.get(ExtractId.SERIES));
			if (series != null) {
				printText(bw, series);
				bw.append('\n');
				hasTitle = true;
			}
			String title = getQueryText(doc, this.queryMap.get(ExtractId.TITLE));
			if (title != null) {
				printText(bw, title);
				bw.append('\n');
				hasTitle = true;
			}
			if (!hasTitle) {
				LogAppender.append("タイトルがありません\n");
				return null;
			}
			//著者
			String author = getQueryText(doc, this.queryMap.get(ExtractId.AUTHOR));
			if (author != null) {
				author = author.replaceAll("作者：", "");
				printText(bw, author);
			}
			bw.append('\n');
			//説明
			String description = getQueryText(doc, this.queryMap.get(ExtractId.DESCRIPTION));
			if (description != null) {
				bw.append('\n');
				bw.append("［＃区切り線］\n");
				bw.append('\n');
				bw.append("［＃ここから２字下げ］\n");
				bw.append("［＃ここから２字上げ］\n");
				printText(bw, description);
				bw.append('\n');
				bw.append("［＃ここで字上げ終わり］\n");
				bw.append("［＃ここで字下げ終わり］\n");
				bw.append('\n');
				bw.append("［＃区切り線］\n");
				bw.append('\n');
			}
			//章名称 変わった場合に出力
			String preChapterTitle = "";
			
			Elements hrefs = getQueryElements(doc, this.queryMap.get(ExtractId.HREF));
			if (hrefs == null) {
				Element contentDiv = getQueryFirstElement(doc, this.queryMap.get(ExtractId.CONTENT_ARTICLE));
				if (contentDiv != null) docToAozoraText(bw, doc, false);
				else {
					LogAppender.append("一覧のリンクが取得できませんでした\n");
					return null;
				}
			} else {
				//更新情報
				HashMap<String, String> updateMap = new HashMap<String, String>();
				
				//更新分のみ取得するようにするためhrefに対応した日付タグの文字列(innerHTML)を取得して保存しておく
				Elements updates = getQueryElements(doc, this.queryMap.get(ExtractId.UPDATE));
				if (hrefs == null || updates == null ||hrefs.size() != updates.size()) {
					//LogAppender.append("リンクに対応する更新情報が取得できませんでした\n");
				} else {
					if (updateInfoFile.exists()) {
						//前回の更新情報を取得して比較
						BufferedReader updateBr = new BufferedReader(new InputStreamReader(new FileInputStream(updateInfoFile), "UTF-8"));
						try {
							String line;
							while ((line=updateBr.readLine()) != null) {
								int idx = line.indexOf("\t");
								if (idx > 0) {
									updateMap.put(line.substring(0, idx), line.substring(idx+1));
								}
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							updateBr.close();
						}
					}
					
					//ファイルに出力
					BufferedWriter updateBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(updateInfoFile), "UTF-8"));
					try {
						int i = 0;
						for (Element update : updates) {
							updateBw.append(hrefs.get(i++).attr("href"));
							updateBw.append('\t');
							updateBw.append(update.html());
							updateBw.append('\n');
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						updateBw.close();
					}
				}
				
				//全話で更新や追加があるかチェック
				boolean noUpdated = true;
				
				int i=-1;
				for (Element href : hrefs) {
					if (this.canceled) return null;
					i++;
					String hrefString = href.attr("href");
					if (hrefString == null || hrefString.length() == 0) continue;
					String chapterHref = hrefString;
					if (!hrefString.startsWith("http")) {
						if (hrefString.charAt(0) == '/') chapterHref = baseUri+hrefString;
						else chapterHref = listBaseUrl+hrefString;
					}
					//変更がなければhrefsから除外
					String update = updateMap.get(hrefString);
					boolean updated = true;
					if (update != null) {
						if (update.equals(updates.get(i).html())) updated = false;
					}
					//debug
					//updated = false;
					
					if (chapterHref != null && chapterHref.length() > 0) {
						this.htmlBaseUri = chapterHref;
						if (!chapterHref.endsWith("/")) this.htmlBaseUri = chapterHref.substring(0, chapterHref.indexOf('/')+1);
						
						//キャッシュ取得 ロードされたらWait 500ms
						String chapterPath = chapterHref.substring(chapterHref.indexOf("//")+2).replaceAll("(\\?|\\&)", "/").replaceAll("(\\*|\\||\\<|\\>|\"|\\\\)", "_");
						File chapterCacheFile = new File(cachePath.getAbsolutePath()+"/"+chapterPath+(chapterPath.endsWith("/")?"index.html":""));
						if (updated || !chapterCacheFile.exists()) {
							LogAppender.append(chapterHref);
							cacheFile(chapterHref, chapterCacheFile);
							LogAppender.append(" : Loaded.\n");
							try { Thread.sleep(500); } catch (InterruptedException e) { }
							//ファイルがロードされたら更新有り
							noUpdated = false;
						}
						
						//シリーズタイトルを出力
						Document chapterDoc = Jsoup.parse(chapterCacheFile, null);
						Element chapterTitle = getQueryFirstElement(chapterDoc, this.queryMap.get(ExtractId.CONTENT_CHAPTER));
						boolean newChapter = false;
						if (chapterTitle != null && !preChapterTitle.equals(chapterTitle.text())) {
							newChapter = true;
							preChapterTitle = chapterTitle.text();
							bw.append("［＃改ページ］\n");
							bw.append("［＃ここから大見出し］\n");
							bw.append(preChapterTitle);
							bw.append('\n');
							bw.append("［＃ここで大見出し終わり］\n");
							bw.append('\n');
						}
						docToAozoraText(bw, chapterDoc, newChapter);
					}
				}
				if (noUpdated) {
					LogAppender.append(title);
					LogAppender.append(" の更新はありません\n");
				}
			}
		}
		
		} finally {
			bw.close();
		}
		
		this.canceled = false;
		return txtFile;
	}
	
	/** 各話のHTMLの変換 */
	private void docToAozoraText(BufferedWriter bw, Document doc, boolean newChapter) throws IOException
	{
		Element contentDiv = getQueryFirstElement(doc, this.queryMap.get(ExtractId.CONTENT_ARTICLE));
		if (contentDiv != null) {
			if (!newChapter) bw.append("［＃改ページ］\n");
			Element elem = getQueryFirstElement(doc, this.queryMap.get(ExtractId.CONTENT_SUBTITLE));
			if (elem != null) {
				List<TextNode> subTitleNodes = elem.textNodes();
				for (Node node : subTitleNodes) {
					if (node instanceof TextNode) {
						String subTitle = ((TextNode)node).text();
						//TODO 置換パターンファイル利用
						subTitle = subTitle.replaceAll("（改稿版）", "");
						bw.append("［＃ここから中見出し］\n");
						printText(bw, subTitle);
						bw.append('\n');
						bw.append("［＃ここで中見出し終わり］\n\n");
					}
				}
			}
			//前書き
			Element preambleDiv = getQueryFirstElement(doc, this.queryMap.get(ExtractId.CONTENT_PREAMBLE));
			if (preambleDiv != null) {
				bw.append("［＃区切り線］\n");
				bw.append("［＃ここから２字下げ］\n");
				bw.append("［＃ここから２字上げ］\n");
				bw.append("［＃ここから１段階小さな文字］\n");
				bw.append('\n');
				printNode(bw, preambleDiv);
				bw.append('\n');
				bw.append('\n');
				bw.append("［＃ここで小さな文字終わり］\n");
				bw.append("［＃ここで字上げ終わり］\n");
				bw.append("［＃ここで字下げ終わり］\n");
				bw.append("［＃区切り線］\n");
				bw.append('\n');
			}
			//本文
			printNode(bw, contentDiv);
			
			//後書き
			Element appendixDiv = getQueryFirstElement(doc, this.queryMap.get(ExtractId.CONTENT_APPENDIX));
			if (appendixDiv != null) {
				bw.append('\n');
				bw.append('\n');
				bw.append("［＃区切り線］\n");
				bw.append("［＃ここから２字下げ］\n");
				bw.append("［＃ここから２字上げ］\n");
				bw.append("［＃ここから１段階小さな文字］\n");
				bw.append('\n');
				printNode(bw, appendixDiv);
				bw.append('\n');
				bw.append('\n');
				bw.append("［＃ここで小さな文字終わり］\n");
				bw.append("［＃ここで字上げ終わり］\n");
				bw.append("［＃ここで字下げ終わり］\n");
			}
		}
	}
	/** ノードを出力 子ノード内のテキストも出力 */
	private void printNode(BufferedWriter bw, Node parent) throws IOException
	{
		for (Node node : parent.childNodes()) {
			if (node instanceof TextNode) printText(bw, ((TextNode)node).text());
			else if (node instanceof Element) {
				Element elem = (Element)node;
				if ("br".equals(elem.tagName())) bw.append('\n');
				else if ("hr".equals(elem.tagName())) bw.append("［＃区切り線］\n");
				else if ("ruby".equals(elem.tagName())) {
					printRuby(bw, elem);
				} else if ("img".equals(elem.tagName())) {
						printImage(bw, elem);
				} else if ("b".equals(elem.tagName())) {
					bw.append("［＃ここから太字］");
					//子を出力
					printNode(bw, node);
					bw.append("［＃ここで太字終わり］");
				} else {
					//子を出力
					printNode(bw, node);
				}
			} else {
				System.out.println(node.getClass().getName());
			}
		}
	}
	/** ルビを青空ルビにして出力 */
	private void printRuby(BufferedWriter bw, Element ruby) throws IOException
	{
		Elements rb = ruby.getElementsByTag("rb");
		Elements rt = ruby.getElementsByTag("rt");
		if (rb.size() > 0) {
			if (rt.size() > 0) {
				bw.append('｜');
				bw.append(rb.get(0).text());
				bw.append('《');
				bw.append(rt.get(0).text());
				bw.append('》');
			} else {
				bw.append(rb.get(0).text());
			}
		}
	}
	/** 画像をキャッシュして相対パスの注記にする */
	private void printImage(BufferedWriter bw, Element img) throws IOException
	{
		String src = img.attr("src");
		if (src == null || src.length() == 0) return;
		
		String imagePath = null;
		int idx = src.indexOf("//");
		if (idx > 0) imagePath = src.substring(idx+2).replaceAll("\\?\\*\\&\\|\\<\\>\"\\\\", "_");
		else if (src.charAt(0) == '/') {
			src = this.baseUri+src.substring(1);
			imagePath = "_"+src.replaceAll("\\?\\*\\&\\|\\<\\>\"\\\\", "_");
		}
		else {
			src = this.htmlBaseUri+src;
			imagePath = src.replaceAll("\\?\\*\\&\\|\\<\\>\"\\\\", "_");
		}
		
		if (imagePath.endsWith("/")) imagePath += "image.png";
		
		File imageFile = new File(this.dstPath+"images/"+imagePath);
		if (!imageFile.exists()) {
			cacheFile(src, imageFile);
		}
		bw.append("［＃挿絵（");
		bw.append("images/"+imagePath);
		bw.append("）入る］");
	}
	
	/** 文字を出力 特殊文字は注記に変換 */
	private void printText(BufferedWriter bw, String text) throws IOException
	{
		char[] chars = text.toCharArray();
		for (char ch : chars) {
			//青空特殊文字
			switch (ch) {
			case '《': bw.append("※［＃始め二重山括弧、1-1-52］"); break;
			case '》': bw.append("※［＃終わり二重山括弧、1-1-53］"); break;
			case '［': bw.append("※［＃始め角括弧、1-1-46］"); break;
			case '］': bw.append("※［＃終わり角括弧、1-1-47］"); break;
			case '〔': bw.append("※［＃始め亀甲括弧、1-1-44］"); break;
			case '〕': bw.append("※［＃終わり亀甲括弧、1-1-45］"); break;
			case '｜': bw.append("※［＃縦線、1-1-35］"); break;
			case '＃': bw.append("※［＃井げた、1-1-84］"); break;
			case '※': bw.append("※［＃米印、1-2-8］"); break;
			default: bw.append(ch); 
			}
		}
	}
	
	////////////////////////////////////////////////////////////////
	/** cssQueryに対応するノード内の文字列を取得 */
	String getQueryText(Document doc, String[] queries)
	{
		if (queries == null) return null;
		for (String query : queries) {
			String text  = getFirstText(doc.select(query).first());
			if (text != null && text.length() > 0) return text;
		}
		return null;
	}
	
	/** cssQueryに対応するノードを取得 */
	Elements getQueryElements(Document doc, String[] queries)
	{
		if (queries == null) return null;
		for (String query : queries) {
			Elements elements = doc.select(query);
			if (elements.size() > 0) return elements;
		}
		return null;
	}
	/** cssQueryに対応する最初のノードを取得 */
	Element getQueryFirstElement(Document doc, String[] queries)
	{
		if (queries == null) return null;
		Elements elements = getQueryElements(doc, queries);
		if (elements != null) return elements.first(); 
		return null;
	}
	
	/** タグの直下の最初のテキストを取得 */
	String getFirstText(Element elem)
	{
		if (elem != null) {
			List<TextNode> nodes = elem.textNodes();
			for (Node node : nodes) {
				if (node instanceof TextNode) {
					String text = ((TextNode) node).text().trim();
					if (text != null && text.length() > 0) {
						return text;
					}
				}
			}
		}
		return null;
	}
	
	////////////////////////////////////////////////////////////////
	/** htmlをキャッシュ すでにあれば何もしない */
	private boolean cacheFile(String urlString, File cacheFile) throws IOException
	{
		//if (!replace && cacheFile.exists()) return false;
		cacheFile.getParentFile().mkdirs();
		//ダウンロード
		URLConnection conn = new URL(urlString).openConnection();
		conn.setConnectTimeout(5000);//5秒
		BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 8192);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));
		IOUtils.copy(bis, bos);
		bos.close();
		bis.close();
		return true;
	}
	
}
