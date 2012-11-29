package com.github.hmdev.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.github.hmdev.util.LogAppender;


/**
 * 青空文庫注記外字をグリフ・UTF-8・代替文字に変換
 * Licence: Non-commercial use only.
 */
public class AozoraGaijiConverter
{
	/** 第3水準 第4水準 SJISをUTF-8に変換するためのクラス */
	JisConverter jisConverter;
	
	/** 青空文庫注記外字をグリフタグに変換 value=CID */
	//HashMap<String, String> chukiCidMap = new HashMap<String, String>();
	
	/** 青空文庫注記外字をUTF-8に変換 */
	HashMap<String, String> chukiUtfMap = new HashMap<String, String>();
	
	/** 青空文庫注記外字を代替文字に変換 */
	HashMap<String, String> chukiAltMap = new HashMap<String, String>();
	
	public AozoraGaijiConverter(String jarPath) throws IOException
	{
		//初期化
		//外字変換
		this.jisConverter = new JisConverter();
		
		//ファイルチェック取得
		this.loadChukiFile(new File(jarPath+"chuki_utf.txt"), chukiUtfMap, false);
		this.loadChukiFile(new File(jarPath+"chuki_alt.txt"), chukiAltMap, true);
	}
	
	/** 注記変換ファイル読み込み 
	 * @throws IOException */
	private void loadChukiFile(File srcFile, HashMap<String, String> chukiMap, boolean hasChukiTag) throws IOException
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
						//System.out.println(values[chukiIdx]+" , "+values[valueIdx]);
						if (hasChukiTag) {
							int end = values[1].indexOf('、');
							if (end == -1) end = values[1].length()-1;
							String chuki = values[1].substring(3, end);
							//System.out.println(chuki+" , "+values[valueIdx]);
							chukiMap.put(chuki, values[0]);
						} else {
							chukiMap.put(values[1], values[0]);
						}
					} catch (Exception e) {
						LogAppender.error(lineNum, srcFile.getName(), line);
					}
				}
			}
		} finally {
			src.close();
		}
	}
	
	/** 注記をグリフタグに変換
	 * @return 変換したUTF-8文字列 変換できなければnull */
	/*public String toGlyphTag(String chuki)
	{
		String cid = chukiCidMap.get(chuki);
		if (cid == null) return null;
		return "<glyph system=\"Adobe-Japan1-6\" code=\""+cid+"\"/>";
	}*/
	
	/** 注記をUTF-8に変換
	 * @return 変換したUTF-8文字列 変換できなければnull */
	public String toUtf(String chuki)
	{
		return chukiUtfMap.get(chuki);
	}
	
	/** 注記を代替文字列に変換
	 * @return 変換したUTF-8文字列 変換できなければnull */
	public String toAlterString(String chuki)
	{
		return chukiAltMap.get(chuki);
	}
	
	/** コードをUTF-8文字に変換 対応文字がなければ'〓' 
	 * @param code UTFまたはJISのコード表記
	 * @return 変換したUTF-8文字列 変換できなければnull */
	public String codeToCharString(String code)
	{
		//System.out.println(code);
		String gaiji = null;
		if (code.startsWith("U+")) {
			//UTF-32コードを文字列に変換
			try {
				int utf8 = Integer.parseInt(code.substring(2), 16);
				gaiji = utfCodeToCharString(utf8);
			} catch (Exception e) {}
		} else if (code.startsWith("UCS-")) {
				//UTF-32コードを文字列に変換
				try {
					int utf8 = Integer.parseInt(code.substring(4), 16);
					gaiji = utfCodeToCharString(utf8);
				} catch (Exception e) {}
		} else if (code.startsWith("unicode")) {
			//UTF-32コードを文字列に変換
			try {
				int utf8 = Integer.parseInt(code.substring(7), 16);
				gaiji = utfCodeToCharString(utf8);
			} catch (Exception e) {}
		} else if (code.startsWith("第3水準") || code.startsWith("第4水準")) {
			//第3第4水準JISを文字列に変換
			try {
				String[] codes = code.substring(4).split("-");
				int utf8 = jisConverter.toUTF8(Integer.parseInt(codes[0]), Integer.parseInt(codes[1]), Integer.parseInt(codes[2]));
				gaiji = utfCodeToCharString(utf8);
			} catch (Exception e) {}
		} else {
			//JISコードを文字列に変換
			try {
				String[] codes = code.split("-");
				int utf8 = jisConverter.toUTF8(Integer.parseInt(codes[0]), Integer.parseInt(codes[1]), Integer.parseInt(codes[2]));
				gaiji = utfCodeToCharString(utf8);
			} catch (Exception e) {}
		}
		return gaiji;
	}
	/** UTF-8コードを文字列に変換
	 * UTF-32の拡張領域は2文字分の文字列になる */
	private String utfCodeToCharString(int utfCode) throws UnsupportedEncodingException
	{
		if (utfCode == 0) return null;
		if (utfCode > 0xFFFF) {
			byte[] b = new byte[]{0, (byte)(utfCode>>16), (byte)(utfCode>>8), (byte)(utfCode)};
			return new String(b, "UTF-32");
		}
		return String.valueOf((char)utfCode);
		
	}
}
