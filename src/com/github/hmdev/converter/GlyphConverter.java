package com.github.hmdev.converter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import com.github.hmdev.util.LogAppender;

/**
 * パラメータファイルで指定されたUTF-8をグリフタグに変換するクラス
 * XMDF変換で利用していてePub3では未使用
 */
public class GlyphConverter
{
	/** 分解表記文字列→拡張ラテン文字の対応テーブル */
	HashMap<Character, String> cidMap = new HashMap<Character, String>();
	
	/** 初期化 パラメータファイル読み込み 
	 * @throws IOException */
	public GlyphConverter(StringBuilder log, String dir) throws IOException
	{
		File dirFile = new File(dir);
		if (dirFile.isDirectory()) {
			for (File file : dirFile.listFiles()) {
				System.out.println(file.getPath());
				this.loadCidFile(log, file, cidMap);
			}
		}
	}
	
	/** 注記変換ファイル読み込み 
	 * @throws IOException */
	private void loadCidFile(StringBuilder log, File srcFile, HashMap<Character, String> cidMap) throws IOException
	{
		BufferedReader src = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), "UTF-8"));
		String line;
		int lineNum = 0;
		try {
			while ((line = src.readLine()) != null) {
				lineNum++;
				if (line.length() > 0 && line.charAt(0)!='#') {
					try {
						String[] values = line.split("\t");
						char ch = values[0].charAt(0);
						if (!cidMap.containsKey(ch)) cidMap.put(ch, values[1]);
					} catch (Exception e) { LogAppender.error(lineNum, srcFile.getName(), line); }
				}
			}
		} finally {
			src.close();
		}
	}
	
	/** UTF-8をグリフタグに変換 
	 * @throws IOException */
	public void convertGlyph(BufferedReader src, BufferedWriter out) throws IOException
	{
		String line;
		char[] arr;
		while ((line=src.readLine()) != null) {
			arr = line.toCharArray();
			for (int i=0; i<arr.length; i++) this.printGlyphTag(out, arr[i]);
			out.write('\n');
		}
	}
	
	/** 分解表記の文字単体をUTF-8文字に変換 
	 * @throws IOException */
	public void printGlyphTag(BufferedWriter out, char ch) throws IOException
	{
		String cid = this.cidMap.get(ch);
		if (cid == null) out.write(ch);
		else {
			System.out.println(cid);
			out.write("<glyph system=\"Adobe-Japan1-6\" code=\"");
			out.write(cid);
			out.write("\"/>");
		}
	}
	
	public static void main(String[] args)
	{
		StringBuilder log = new StringBuilder();
		try {
			GlyphConverter converter = new GlyphConverter(log, "Glyph_JIS1-4");
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
			converter.printGlyphTag(out, '㎎');
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
