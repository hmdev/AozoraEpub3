package com.github.hmdev.info;
import java.util.Date;

/** タイトル著作者等のメタ情報を格納 */
public class BookInfo
{
	/** タイトル */
	public String title;
	/** タイトル行番号 */
	public int titleLine = -1;
	
	/** 著作者 */
	public String creator;
	/** 著作者行番号 */
	public int creatorLine = -1;
	
	/** 発刊日時 */
	public Date published;
	/** 更新日時 */
	public Date modified;
	
	/** 縦書きならtrue */
	public boolean vertical = true;
	
	/** 表紙ファイル名 フルパスかURL ""なら先頭の挿絵 nullなら表紙無し */
	public String coverFileName;
	
	public BookInfo()
	{
	}
}
