package com.github.hmdev.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import com.github.hmdev.util.LogAppender;


/**
 * 青空文庫注記外字をグリフ・UTF-8・代替文字に変換
 */
public class AozoraGaijiConverter
{
	/** 青空文庫注記外字をグリフタグに変換 value=CID */
	//HashMap<String, String> chukiCidMap = new HashMap<String, String>();
	
	/** 青空文庫注記外字をUTF-8に変換 */
	HashMap<String, String> chukiUtfMap = new HashMap<String, String>();
	
	/** 青空文庫注記外字を代替文字に変換 */
	HashMap<String, String> chukiAltMap = new HashMap<String, String>();
	
	public AozoraGaijiConverter(String jarPath) throws IOException
	{
		//初期化
		//ファイルチェック取得 IVS優先
		this.loadChukiFile(new File(jarPath+"chuki_ivs.txt"), chukiUtfMap);
		this.loadChukiFile(new File(jarPath+"chuki_utf.txt"), chukiUtfMap);
		this.loadChukiFile(new File(jarPath+"chuki_alt.txt"), chukiAltMap);
	}
	
	/** 注記変換ファイル読み込み 
	 * @throws IOException */
	private void loadChukiFile(File srcFile, HashMap<String, String> chukiMap) throws IOException
	{
		BufferedReader src = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), "UTF-8"));
		String line;
		int lineNum = 0;
		try {
			while ((line = src.readLine()) != null) {
				if (line.length() > 0 && line.charAt(0)!='#') {
					try {
						int charStart = line.indexOf('\t');
						if (charStart == -1) continue;
						charStart = line.indexOf('\t', charStart+1);
						if (charStart == -1) continue;
						charStart++;
						int chukiStart = line.indexOf('\t', charStart);
						if (chukiStart == -1) continue;
						chukiStart++;
						if (!line.startsWith("※［＃", chukiStart)) continue;
						int chukiEnd = line.indexOf('\t', chukiStart);
						int chukiCode = line.indexOf('、', chukiStart);
						if (chukiCode != -1 && line.charAt(chukiCode+1) == '「') chukiCode = line.indexOf('、', chukiCode+1);//注記内に、がある
						if (chukiCode != -1 && (chukiEnd == -1 || chukiCode < chukiEnd)) chukiEnd = chukiCode+1;
						if (chukiEnd == -1) chukiEnd = line.length();
						
						String utfChar = line.substring(charStart, chukiStart-1);
						String chuki = line.substring(chukiStart+3, chukiEnd-1);
						if (chukiMap.containsKey(chuki)) LogAppender.warn(lineNum, "外字注記定義重複", chuki);
						else chukiMap.put(chuki, utfChar);
						
					} catch (Exception e) {
						LogAppender.error(lineNum, srcFile.getName(), line);
					}
				}
				lineNum++;
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
		try {
		if (code.startsWith("U+") || code.startsWith("u+")) {
			//Unicodeを文字列に変換
			//IVSがある場合は U+845B-U+E0100 または U+845B-E0100 の表記
			int idx = code.indexOf("-");
			if (idx == -1) {
				return codeToCharString(Integer.parseInt(code.substring(2), 16));
			} else {
				String ivs = code.substring(idx+1);
				if (ivs.startsWith("U+") || ivs.startsWith("u+")) ivs = ivs.substring(2);
				return codeToCharString(Integer.parseInt(code.substring(2, idx), 16))
						+codeToCharString(Integer.parseInt(ivs, 16));
			}
		} else if (code.startsWith("UCS-")) {
			//UTF-32コードを文字列に変換
			return codeToCharString(Integer.parseInt(code.substring(4), 16));
		} else if (code.startsWith("unicode")) {
			//UTF-32コードを文字列に変換
			return codeToCharString(Integer.parseInt(code.substring(7), 16));
		} else {
			//第3第4水準JISを文字列に変換
			String[] codes;
			if (code.startsWith("第3水準") || code.startsWith("第4水準")) {
				codes = code.substring(4).split("-");
			} else {
				codes = code.split("-");
			}
			return JisConverter.getConverter().toCharString(Integer.parseInt(codes[0]), Integer.parseInt(codes[1]), Integer.parseInt(codes[2]));
		}
		} catch (Exception e) {}
		return null;
	}
	
	/** UTF-8コードを文字列に変換
	 * UTF-32の拡張領域は2文字分の文字列になる */
	public String codeToCharString(int unicode) throws UnsupportedEncodingException
	{
		if (unicode == 0) return null;
		if (unicode > 0xFFFF) {
			byte[] b = new byte[]{0, (byte)(unicode>>16), (byte)(unicode>>8), (byte)(unicode)};
			return new String(b, "UTF-32");
		}
		return String.valueOf((char)unicode);
	}
	
	/** UTF-8のバイト配列のコード数値に変換 */
	public int charStringToCode(String charString)
	{
		try {
			return toCode(charString.getBytes("UTF-32"));
		} catch (UnsupportedEncodingException e) {}
		return 0;
	}
	public int toCode(byte[] utf32Bytes)
	{
		return ByteBuffer.wrap(utf32Bytes).getInt();
	}
	
}
