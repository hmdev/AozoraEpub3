package com.github.hmdev.info;
/** Velocity用画像情報 */
public class ImageInfo
{
	/** ファイルのID 0001 */
	String id;
	/** ファイル名拡張子付き 0001.png */
	String file;
	/** 画像フォーマット image/png */
	String format;
	
	/** 画像幅 */
	int width = -1;
	/** 画像高さ */
	int height = -1;
	
	/** カバー画像ならtrue */
	boolean isCover;
	
	public ImageInfo(String id, String file, String format)
	{
		super();
		this.id = id;
		this.file = file;
		this.format = format;
	}

	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getFile()
	{
		return file;
	}
	
	public void setFile(String file)
	{
		this.file = file;
	}
	
	public String getFormat()
	{
		return format;
	}
	
	public void setFormat(String format)
	{
		this.format = format;
	}
	
	public boolean getIsCover()
	{
		return this.isCover;
	}
	
	public void setIsCover(boolean isCover)
	{
		this.isCover = isCover;
	}
}
