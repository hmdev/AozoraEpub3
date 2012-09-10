package com.github.hmdev.util;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import com.github.hmdev.converter.PageBreakTrigger;
import com.github.hmdev.info.ImageInfo;

/**
 * 画像情報を格納するクラス
 * 画像取得関連のメソッドもここで定義
 */
public class ImageInfoReader
{
	/** 画像が圧縮ファイル内でなくファイルならtrue
	 *  画像データ取得時にファイルシステムから取得するかの判別用 */
	boolean isFile = true;
	
	/** 変換するtxtまたは圧縮ファイル */
	File srcFile = null;
	
	/** テキストファイルの親のパス 末尾は"/"または空文字列
	 *  txtなら絶対パス zipならentryのパス */
	String srcParentPath = null;
	
	/** Zipならzip内テキストのentry */
	String zipTextEntry = null;
	/** Zipならzip内テキストの親entry ルートならnull */
	String zipTextParentPath = "";
	
	/** 出力順にファイル名を格納 imageFileInfosのkeyと同じ文字列 */
	Vector<String> imageFileNames;
	
	/** txtならZip内の画像情報 初回にすべて読み込む
	 * txtでなければファイルシステムの画像情報 取得するごとに追加していく */
	HashMap<String, ImageInfo> imageFileInfos;
	
	/** 初期化 画像情報格納用のvectorとマップを生成
	 * @param isFile 圧縮ファイル内ならfalse
	 * @param srcParentPath 変換するソースの親のパス
	 * @param zipTextParentPath Zipならテキストファイルのentryの親のパス 圧縮ファイルで無い場合やルートならnull */
	public ImageInfoReader(boolean isFile, File srcFile)
	{
		this.isFile = isFile;
		this.srcFile = srcFile;
		this.srcParentPath = srcFile.getParent()+"/";
		this.zipTextParentPath = "";
		this.imageFileNames = new Vector<String>();
		this.imageFileInfos = new HashMap<String, ImageInfo>();
	}
	
	/** zipの場合はzip内のtxtのentryNameと親のパスを設定 */
	public void setZipTextEntry(String zipTextEntry)
	{
		this.zipTextEntry = zipTextEntry;
		int idx = zipTextEntry.lastIndexOf('/');
		if (idx > -1) {
			//Zipのテキストの親のパスを設定
			this.zipTextParentPath = this.zipTextEntry.substring(0, idx+1);
		}
	}
	
	/** 画像ファイルを取得 */
	public File getImageFile(String fileName)
	{
		return new File(this.srcParentPath+fileName);
	}
	
	/** 画像出力順にImageInfoを格納 zipの場合は後で並び替える */
	public void addImageFileName(String imageFileName)
	{
		this.imageFileNames.add(this.zipTextParentPath+imageFileName);
	}
	
	/** 名前順で並び替え */
	public void sortImageFileNames()
	{
		Collections.sort(this.imageFileNames, new FileNameComparator());
	}
	
	public String getImageFileName(int idx)
	{
		return this.imageFileNames.get(idx);
	}
	public int countImageFiles()
	{
		return this.imageFileNames.size();
	}
	
	public Vector<String> getImageFileNames()
	{
		return this.imageFileNames;
	}
	
	
	/** 指定した順番の画像情報を取得 */
	public ImageInfo getImageInfo(int idx) throws IOException
	{
		return this.getImageInfo(this.imageFileNames.get(idx));
	}
	
	/** ImageInfoを取得 拡張子違いを吸収
	 * zip内テキストファイルがサブフォルダ以下にある場合はnullになるので本文中のパスに親のパスをつけて再取得
	 * @param srcImageFileName テキスト内の画像注記で指定されている相対ファイル名 */
	public ImageInfo getImageInfo(String srcImageFileName) throws IOException
	{
		ImageInfo imageInfo = this._getImageInfo(srcImageFileName);
		return imageInfo;
	}
	/** ImageInfoを取得
	 * zip内テキストファイルがサブフォルダ以下にある場合はnullになるので本文中のパスに親のパスをつけて再取得
	 * @param srcImageFileName テキスト内の画像注記で指定されている相対ファイル名 */
	private ImageInfo _getImageInfo(String srcImageFileName) throws IOException
	{
		//取得済みならそれを返す zipならすべて取得済み
		ImageInfo imageInfo = this.imageFileInfos.get(srcImageFileName);
		if (imageInfo != null) return imageInfo;
		//zipのサブパスから取得
		if (isFile) {
			//ファイルシステムから取得
			File imageFile = new File(this.srcParentPath+srcImageFileName);
			if (imageFile.exists()) {
				imageInfo = ImageInfo.getImageInfo(imageFile);
				if (imageInfo != null) {
					this.imageFileInfos.put(srcImageFileName, imageInfo);
					return imageInfo;
				}
			}
		} else {
			imageInfo = this.imageFileInfos.get(this.zipTextParentPath+srcImageFileName);
			return imageInfo;
		}
		return null;
	}
	
	/** zip内の画像情報をすべて読み込み
	 * @throws IOException */
	public void loadZipImageInfos(File srcFile, boolean addFile) throws IOException
	{
		ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
		ArchiveEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			String entryName = entry.getName();
			String lowerName = entryName.toLowerCase();
			if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
				ImageInfo imageInfo = null;
				try {
					imageInfo = ImageInfo.getImageInfo(zis, zis.getCount());
				} catch (Exception e) {
					LogAppender.append("[ERROR] 画像が読み込めませんでした: ");
					LogAppender.append(srcFile.getPath());
					LogAppender.append("\n");
					e.printStackTrace();
				}
				if (imageInfo != null) {
					this.imageFileInfos.put(entryName, imageInfo);
					if (addFile) this.addImageFileName(entryName);
				}
			}
		}
		zis.close();
	}
	
	/** 指定した順番の画像情報を取得 */
	public BufferedImage getImage(int idx) throws IOException
	{
		return this.getImage(this.imageFileNames.get(idx));
	}
	
	
	/** ファイル名から画像を取得
	 * @param srcImageFileName ファイル名 Zipならエントリ名
	 * 
	 * Zipファイルから指定されたEntryの画像を取得
	 * ※先頭からシークされるので遅い */
	public BufferedImage getImage(String srcImageFileName) throws IOException
	{
		if (this.isFile) {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(this.srcParentPath+srcImageFileName)), 65536);
			try {
				return ImageIO.read(bis);
			} finally { bis.close(); }
		} else {
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
			try {
				ArchiveEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					String entryName = entry.getName();
					if (entryName.equals(srcImageFileName)) {
						try {
							return ImageIO.read(zis);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} finally {
				zis.close();
			}
			/*ZipFile zf = new ZipFile(this.srcFile);
			ZipArchiveEntry entry = zf.getEntry(this.zipTextParentPath+srcImageFileName);
			InputStream is = zf.getInputStream(entry);
			try {
				return ImageIO.read(is);
			} catch (Exception e) {
				e.printStackTrace(); 
			} finally {
				is.close();
			}*/
		}
		return null;
	}
	
	/** 画像が単一ページ画像にできるかチェック
	 * @param srcFilePath テキスト内の画像相対パス文字列
	 * @throws IOException */
	public int getImagePageType(String srcFilePath, int tagLevel)
	{
		//タグ内ならそのまま出力
		if (tagLevel > 0) return PageBreakTrigger.IMAGE_PAGE_NONE;
		try {
			ImageInfo imageInfo = this.getImageInfo(srcFilePath);
			if (imageInfo == null) return PageBreakTrigger.IMAGE_PAGE_NONE;
			
			if (imageInfo.getWidth() >= 400 && imageInfo.getHeight() >= 600) {
				if ((double)imageInfo.getWidth()/imageInfo.getHeight() > 3.0/4)
					return PageBreakTrigger.IMAGE_PAGE_W;
				else return PageBreakTrigger.IMAGE_PAGE_H;
			}
		} catch (Exception e) { e.printStackTrace(); }
		return PageBreakTrigger.IMAGE_PAGE_NONE;
	}
	
}
