package com.github.hmdev.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;

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
	public static File convertToAozoraText(String urlString, File cachePath) throws IOException
	{
		String baseUri = urlString.substring(0, urlString.indexOf('/', urlString.indexOf("//")+2));
		String urlPath = urlString.substring(urlString.indexOf("//")+2).replaceAll("\\?\\*\\&\\|\\<\\>\"\\\\", "_");
		boolean urlIsPath = urlPath.endsWith("/");
		File txtFile = new File(cachePath.getPath()+"/"+urlPath+(urlIsPath?"index.txt":""));
		System.out.println(txtFile);
		txtFile.getParentFile().mkdirs();
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"));
		
		//urlStringのファイルをキャッシュ
		File cacheFile = new File(cachePath.getAbsolutePath()+"/"+urlPath+(urlIsPath?"index.html":""));
		System.out.println(cacheFile);
		cacheFile(urlString, cacheFile, true);
		
		if (urlIsPath) {
			//パスならlist.txtの情報を元にキャッシュ後に青空txt変換して改ページで繋げて出力
			//TODO 更新分のみ取得するようにする
			Document doc = Jsoup.parse(cacheFile, null);
			
			//タイトルと著者
			boolean hasTitle = false;
			Element elem = doc.select(".series a").first();
			if (elem != null) {
				List<TextNode> nodes = elem.textNodes();
				for (Node node : nodes) {
					if (node instanceof TextNode) {
						String title = ((TextNode) node).text().trim();
						if (title != null && title.length() > 0) {
							printText(br, title);
							br.append('\n');
							hasTitle = true;
						}
					}
				}
			}
			elem = doc.select(".novel_title").first();
			if (elem != null) {
				List<TextNode> nodes = elem.textNodes();
				for (Node node : nodes) {
					if (node instanceof TextNode) {
						String title = ((TextNode) node).text().trim();
						if (title != null && title.length() > 0) {
							printText(br, title);
							br.append('\n');
							hasTitle = true;
						}
					}
				}
			}
			if (!hasTitle) {
				LogAppender.append("タイトルがありません\n");
				return null;
			}
			elem = doc.select(".novel_writername").first();
			if (elem != null) printText(br, elem.text().replaceFirst("作者：", ""));
			br.append('\n');
			
			elem = doc.select(".novel_ex").first();
			if (elem != null) {
				br.append('\n');
				br.append("［＃区切り線］\n");
				printText(br, elem.text());
				br.append("［＃区切り線］\n");
				br.append('\n');
			}
			
			String preChapterTitle = "";
			
			Elements hrefs = doc.select(".period_subtitle a");
			if (hrefs.size() == 0) hrefs = doc.select(".long_subtitle a");
			if (hrefs.size() == 0) {
				LogAppender.append("一覧のリンクが取得できませんでした\n");
				return null;
			}
			for (Element href : hrefs) {
				String chapterHref = baseUri+href.attr("href");
				if (chapterHref != null && chapterHref.length() > 0) {
					LogAppender.append(chapterHref);
					LogAppender.append("\n");
					String chapterPath = chapterHref.substring(chapterHref.indexOf("//")+2);
					File chapterCacheFile = new File(cachePath.getAbsolutePath()+"/"+chapterPath+(chapterPath.endsWith("/")?"index.html":""));
					//キャッシュでなければ取得Wait
					boolean loaded = cacheFile(chapterHref, chapterCacheFile, false);
					if (loaded) try { Thread.sleep(250); } catch (InterruptedException e) { }
					
					Document chapterDoc = Jsoup.parse(chapterCacheFile, null);
					Element chapterTitle = chapterDoc.select(".chapter_title").first();
					if (chapterTitle != null && !preChapterTitle.equals(chapterTitle.text())) {
						preChapterTitle = chapterTitle.text();
						br.append("［＃ページの左右中央］\n");
						br.append("［＃ここから大見出し］\n");
						br.append(preChapterTitle);
						br.append('\n');
						br.append("［＃ここで大見出し終わり］\n");
					}
					docToAozoraText(br, chapterDoc, txtFile);
				}
			}
		} else {
			//ファイルなら青空txt変換
		}
		br.close();
		
		return txtFile;
	}
	
	static void docToAozoraText(BufferedWriter br, Document doc, File outFile) throws IOException
	{
		Element contentDiv = doc.select("#novel_view").first();
		if (contentDiv != null) {
			br.append("［＃改ページ］\n");
			Element elem = doc.select(".novel_subtitle").first();
			if (elem != null) {
				List<TextNode> subTitleNodes = elem.textNodes();
				for (Node node : subTitleNodes) {
					if (node instanceof TextNode) {
						String subTitle = ((TextNode)node).text();
						//TODO 置換パターンファイル利用
						subTitle = subTitle.replaceAll("（改稿版）", "");
						br.append("［＃ここから中見出し］\n");
						printText(br, subTitle);
						br.append('\n');
						br.append("［＃ここで中見出し終わり］\n\n");
					}
				}
			}
			//前書き
			Element preambleDiv = doc.select(".novel_p .novel_view").first();
			if (preambleDiv != null) {
				br.append("［＃区切り線］\n");
				br.append('\n');
				br.append("［＃ここから２字下げ］\n");
				br.append("［＃ここから１段階小さな文字］\n");
				printNode(br, preambleDiv);
				br.append('\n');
				br.append("［＃ここで小さな文字終わり］\n");
				br.append("［＃ここで字下げ終わり］\n");
				br.append('\n');
				br.append("［＃区切り線］\n");
				br.append('\n');
			}
			//本文
			printNode(br, contentDiv);
			
			//後書き
			Element appendixDiv = doc.select(".novel_a .novel_view").first();
			if (appendixDiv != null) {
				br.append('\n');
				br.append('\n');
				br.append("［＃区切り線］\n");
				br.append('\n');
				br.append("［＃ここから２字下げ］\n");
				br.append("［＃ここから１段階小さな文字］\n");
				printNode(br, appendixDiv);
				br.append('\n');
				br.append("［＃ここで小さな文字終わり］\n");
				br.append("［＃ここで字下げ終わり］\n");
			}
		}
	}
	static void printNode(BufferedWriter br, Node parent) throws IOException
	{
		for (Node node : parent.childNodes()) {
			if (node instanceof TextNode) printText(br, ((TextNode)node).text());
			else if (node instanceof Element) {
				Element elem = (Element)node;
				if ("br".equals(elem.tagName())) br.append('\n');
				if ("hr".equals(elem.tagName())) br.append("［＃区切り線］\n");
				else if ("ruby".equals(elem.tagName())) {
					printRuby(br, elem);
				}
			} else {
				System.out.println(node.getClass().getName());
			}
		}
	}
	
	static void printRuby(BufferedWriter br, Element ruby) throws IOException
	{
		Elements rb = ruby.getElementsByTag("rb");
		Elements rt = ruby.getElementsByTag("rt");
		if (rb.size() > 0) {
			if (rt.size() > 0) {
				br.append('｜');
				br.append(rb.get(0).text());
				br.append('《');
				br.append(rt.get(0).text());
				br.append('》');
			} else {
				br.append(rb.get(0).text());
			}
		}
	}
	
	static void printText(BufferedWriter br, String text) throws IOException
	{
		char[] chars = text.toCharArray();
		for (char ch : chars) {
			switch (ch) {
			case '《': br.append("※［＃始め二重山括弧、1-1-52］"); break;
			case '》': br.append("※［＃終わり二重山括弧、1-1-53］"); break;
			case '［': br.append("※［＃始め角括弧、1-1-46］"); break;
			case '］': br.append("※［＃終わり角括弧、1-1-47］"); break;
			case '〔': br.append("※［＃始め亀甲括弧、1-1-44］"); break;
			case '〕': br.append("※［＃終わり亀甲括弧、1-1-45］"); break;
			case '｜': br.append("※［＃縦線、1-1-35］"); break;
			case '＃': br.append("※［＃井げた、1-1-84］"); break;
			case '※': br.append("※［＃米印、1-2-8］"); break;
			default: br.append(ch); 
			}
		}
	}
	
	/** htmlをキャッシュ すでにあれば何もしない TODO 更新日時で比較して更新 */
	static boolean cacheFile(String urlString, File cacheFile, boolean replace) throws IOException
	{
		if (!replace && cacheFile.exists()) return false;
		cacheFile.getParentFile().mkdirs();
		//ダウンロード
		BufferedInputStream bis = new BufferedInputStream(new URL(urlString).openStream(), 8192);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));
		IOUtils.copy(bis, bos);
		bos.close();
		bis.close();
		return true;
	}
	
}
