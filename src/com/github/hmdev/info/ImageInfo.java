package com.github.hmdev.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** 画像情報
 * Velocity内でも利用するための情報も格納する */
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
	
	/** Zip内ファイルentryの位置 */
	int zipIndex = -1;
	
	/** カバー画像ならtrue */
	boolean isCover;
	
	/** 幅高さ無しの画像情報を生成 */
	public ImageInfo(String id, String fileName, String format)
	{
		this(id, fileName, format, -1, -1, -1);
	}
	/** 画像の情報を生成
	 * @param fotmat image/png の形式に自動的に変換する 拡張子の場合は自動的に変換される */
	public ImageInfo(String id, String fileName, String format, int width, int height, int zipIndex)
	{
		super();
		this.id = id;
		this.file = fileName;
		this.format = format.toLowerCase();
		if (!this.format.startsWith("image/")) {
			this.format = "image/"+(this.format.equals("jpg")?"jpeg":this.format);
		}
		this.width = width;
		this.height = height;
		this.zipIndex = zipIndex;
	}
	
	/** 画像ストリームから画像情報を生成 */
	static public ImageInfo getImageInfo(String id, String fileName, InputStream is) throws IOException
	{
		return getImageInfo(id, fileName, is, -1);
	}
	/** 画像ストリームから画像情報を生成
	 * @param zipIndex Zipファイルの場合はエントリの位置 (再読込や読み飛ばし時のファイル名比較の省略用)
	 * @throws IOException */
	static public ImageInfo getImageInfo(String id, String fileName, InputStream is, int zipIndex) throws IOException
	{
		ImageInputStream iis = ImageIO.createImageInputStream(is);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
		if (readers.hasNext()) {
			ImageReader reader = readers.next();
			reader.setInput(iis);
			String format = reader.getFormatName();
			return new ImageInfo(id, fileName, format, reader.getWidth(0), reader.getHeight(0), zipIndex);
		}
		return null;
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
	public int getWidth()
	{
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}
	public int getHeight()
	{
		return height;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}
	public int getZipIndex()
	{
		return zipIndex;
	}
	public void setZipIndex(int zipIndex)
	{
		this.zipIndex = zipIndex;
	}
}
