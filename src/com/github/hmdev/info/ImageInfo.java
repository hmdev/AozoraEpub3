package com.github.hmdev.info;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
	/** 出力ファイル名 拡張子付き 0001.png */
	String outFileName;
	/** 画像フォーマット png jpg gif */
	String ext;
	
	/** 画像幅 */
	int width = -1;
	/** 画像高さ */
	int height = -1;
	
	/** 出力画像幅 */
	int outWidth = -1;
	/** 出力画像高さ */
	int outHeight = -1;
	
	/** Zip内ファイルentryの位置 */
	int zipIndex = -1;
	
	/** カバー画像ならtrue */
	boolean isCover;
	
	/** 回転角度 右 90 左 -90 */
	public int rotateAngle = 0;
	
	/** 画像の情報を生成
	 * @param ext png jpg gif の文字列 */
	public ImageInfo(String ext, int width, int height, int zipIndex)
	{
		super();
		this.ext = ext.toLowerCase();
		this.width = width;
		this.height = height;
		this.zipIndex = zipIndex;
	}
	
	/** ファイルから画像情報を生成 */
	static public ImageInfo getImageInfo(File imageFile) throws IOException
	{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageFile));
		ImageInfo imageInfo = ImageInfo.getImageInfo(bis, -1);
		bis.close();
		return imageInfo;
	}
	
	/** 画像ストリームから画像情報を生成 */
	static public ImageInfo getImageInfo(InputStream is) throws IOException
	{
		return getImageInfo(is, -1);
	}
	
	
	
	/** 画像ストリームから画像情報を生成
	 * @param zipIndex Zipファイルの場合はエントリの位置 (再読込や読み飛ばし時のファイル名比較の省略用)
	 * @throws IOException */
	static public ImageInfo getImageInfo(InputStream is, int zipIndex) throws IOException
	{
		ImageInfo imageInfo = null;
		ImageInputStream iis = ImageIO.createImageInputStream(is);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
		if (readers.hasNext()) {
			ImageReader reader = readers.next();
			if (readers.hasNext() && reader.getClass().getName().endsWith("CLibPNGImageReader")) readers.next();
			reader.setInput(iis);
			String ext = reader.getFormatName();
			imageInfo = new ImageInfo(ext, reader.getWidth(0), reader.getHeight(0), zipIndex);
			reader.dispose();
		}
		return imageInfo;
	}
	
	static public ImageInfo getImageInfo(String ext, BufferedImage image, int zipIndex) throws IOException
	{
		return new ImageInfo(ext, image.getWidth(), image.getHeight(), zipIndex);
	}
	
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getOutFileName()
	{
		return outFileName;
	}
	
	public void setOutFileName(String file)
	{
		this.outFileName = file;
	}
	
	public void setExt(String ext)
	{
		this.ext = ext;
	}
	public String getExt()
	{
		return this.ext;
	}
	/** mime形式(image/png)の形式フォーマット文字列を返却 */
	public String getFormat()
	{
		return "image/"+(this.ext.equals("jpg")?"jpeg":this.ext);
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
	
	public int getOutWidth()
	{
		return outWidth;
	}
	public void setOutWidth(int outWidth)
	{
		this.outWidth = outWidth;
	}
	public int getOutHeight()
	{
		return outHeight;
	}
	public void setOutHeight(int outHeight)
	{
		this.outHeight = outHeight;
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
