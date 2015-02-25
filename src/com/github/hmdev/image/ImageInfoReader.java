package com.github.hmdev.image;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.util.FileNameComparator;
import com.github.hmdev.util.LogAppender;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

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
	
	/** Zipならzip内テキストの親entry ルートならnull */
	public String archiveTextParentPath = "";
	
	/** 出力順にファイル名を格納 imageFileInfosのkeyと同じ文字列 */
	Vector<String> imageFileNames;
	
	/** txtならZip内の画像情報 初回にすべて読み込む
	 * txtでなければファイルシステムの画像情報 取得するごとに追加していく */
	HashMap<String, ImageInfo> imageFileInfos;
	
	/** 初期化 画像情報格納用のvectorとマップを生成
	 * @param isFile 圧縮ファイル内ならfalse
	 * @param srcParentPath 変換するソースの親のパス
	 * @param archiveTextParentPath Zipならテキストファイルのentryの親のパス 圧縮ファイルで無い場合やルートならnull */
	public ImageInfoReader(boolean isFile, File srcFile)
	{
		this.isFile = isFile;
		this.srcFile = srcFile;
		this.srcParentPath = srcFile.getParent()+"/";
		this.archiveTextParentPath = "";
		this.imageFileNames = new Vector<String>();
		this.imageFileInfos = new HashMap<String, ImageInfo>();
	}
	
	/** zipの場合はzip内のtxtのentryNameと親のパスを設定 */
	public void setArchiveTextEntry(String archiveTextEntry)
	{
		int idx = archiveTextEntry.lastIndexOf('/');
		if (idx > -1) {
			//アーカイブ内のテキストの親のパスを設定
			this.archiveTextParentPath = archiveTextEntry.substring(0, idx+1);
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
		this.imageFileNames.add(this.archiveTextParentPath+imageFileName);
	}
	
	/** 名前順で並び替え */
	public void sortImageFileNames()
	{
		Collections.sort(this.imageFileNames, new FileNameComparator());
	}
	
	/** 指定位置の画像ファイル名を取得 */
	public String getImageFileName(int idx)
	{
		return this.imageFileNames.get(idx);
	}
	/** 画像ファイル名のVectorを取得 */
	public Vector<String> getImageFileNames()
	{
		return this.imageFileNames;
	}
	
	/** 画像情報のカウント zipの場合はzip内にある画像すべて  */
	public int countImageFileInfos()
	{
		return this.imageFileInfos.size();
		//return this.imageFileNames.size();
	}
	/** テキスト内の画像順で格納された画像ファイル名の件数
	 * zipテキストの場合は、本文以外の画像も後から追加される
	 * zip画像のみ場合はソートされてすべての画像の件数 */
	public int countImageFileNames()
	{
		return this.imageFileNames.size();
	}
	
	/** 指定した順番の画像情報を取得 */
	public ImageInfo getImageInfo(int idx) throws IOException
	{
		if (this.imageFileNames.size()-1 < idx) return null;
		return this.getImageInfo(this.imageFileNames.get(idx));
	}
	
	/** ImageInfoを取得 拡張子違いを吸収
	 * zip内テキストファイルがサブフォルダ以下にある場合はnullになるので本文中のパスに親のパスをつけて再取得
	 * @param srcImageFileName テキスト内の画像注記で指定されている相対ファイル名 */
	public ImageInfo getImageInfo(String srcImageFileName) throws IOException
	{
		return this._getImageInfo(srcImageFileName);
	}
	
	public ImageInfo getCollectImageInfo(String srcImageFileName) throws IOException
	{
		ImageInfo imageInfo = this._getImageInfo(srcImageFileName);
		if (imageInfo == null) imageInfo = this._getImageInfo(this.correctExt(srcImageFileName));
		return imageInfo;
	}
	
	/** 拡張子修正 大文字小文字は3パターンのみ */
	public String correctExt(String srcImageFileName) throws IOException
	{
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		//拡張子修正
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".png");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpg");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpeg");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".gif");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".PNG");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".JPG");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".JPEG");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".GIF");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".Png");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".Jpg");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".Jpeg");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".Gif");
		if (this.hasImage(srcImageFileName)) return srcImageFileName;
		
		return null;
	}
	
	private boolean hasImage(String srcImageFileName)
	{
		if (this.imageFileInfos.containsKey(srcImageFileName)) return true;
		if (isFile) {
			//ファイルシステムから取得
			File imageFile = new File(this.srcParentPath+srcImageFileName);
			if (imageFile.exists()) return true;
		} else {
			if (this.imageFileInfos.containsKey(this.archiveTextParentPath+srcImageFileName)) return true;
		}
		return false;
	}
	
	/** ImageInfoを取得
	 * zip内テキストファイルがサブフォルダ以下にある場合はnullになるので本文中のパスに親のパスをつけて再取得
	 * @param srcImageFileName テキスト内の画像注記で指定されている相対ファイル名 */
	private ImageInfo _getImageInfo(String srcImageFileName) 
	{
		//取得済みならそれを返す zipならすべて取得済み
		ImageInfo imageInfo = this.imageFileInfos.get(srcImageFileName);
		if (imageInfo != null) return imageInfo;
		//zipのサブパスから取得
		if (isFile) {
			//ファイルシステムから取得
			File imageFile = new File(this.srcParentPath+srcImageFileName);
			if (imageFile.exists()) {
				try {
					imageInfo = ImageInfo.getImageInfo(imageFile);
					if (imageInfo != null) {
						this.imageFileInfos.put(srcImageFileName, imageInfo);
						return imageInfo;
					}
				} catch (IOException ioe) { System.err.println(ioe); }
			}
		} else {
			//Zipで中にフォルダがある場合
			imageInfo = this.imageFileInfos.get(this.archiveTextParentPath+srcImageFileName);
			return imageInfo;
		}
		return null;
	}
	
	/** zip内の画像情報をすべて読み込み
	 * @throws IOException */
	public void loadZipImageInfos(File srcFile, boolean addFileName) throws IOException
	{
		ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
		try {
		ArchiveEntry entry;
		int idx = 0;
		while ((entry = zis.getNextEntry()) != null) {
			if (idx++ % 10 == 0) LogAppender.append(".");
			String entryName = entry.getName();
			String lowerName = entryName.toLowerCase();
			if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
				ImageInfo imageInfo = null;
				try {
					imageInfo = ImageInfo.getImageInfo(zis, zis.getCount());
				} catch (Exception e) {
					LogAppender.error("画像が読み込めませんでした: "+srcFile.getPath());
					e.printStackTrace();
				}
				if (imageInfo != null) {
					this.imageFileInfos.put(entryName, imageInfo);
					if (addFileName) this.addImageFileName(entryName);
				}
			}
		}
		} finally {
			LogAppender.println();
			zis.close();
		}
	}
	/** rar内の画像情報をすべて読み込み */
	public void loadRarImageInfos(File srcFile, boolean addFileName) throws IOException, RarException
	{
		Archive archive = new Archive(srcFile);
		try {
		int idx = 0;
		for (FileHeader fileHeader : archive.getFileHeaders()) {
			if (idx++ % 10 == 0) LogAppender.append(".");
			if (!fileHeader.isDirectory()) {
				String entryName = fileHeader.getFileNameW();
				if (entryName.length() == 0) entryName = fileHeader.getFileNameString();
				entryName = entryName.replace('\\', '/');
				String lowerName = entryName.toLowerCase();
				if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
					ImageInfo imageInfo = null;
					InputStream is = null;
					try {
						//読めない場合があるので一旦バイト配列に読み込み
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						archive.extractFile(fileHeader, baos);
						baos.close();
						is = new ByteArrayInputStream(baos.toByteArray());
						imageInfo = ImageInfo.getImageInfo(is);
						if (imageInfo != null) {
							this.imageFileInfos.put(entryName, imageInfo);
							if (addFileName) this.addImageFileName(entryName);
						} else {
							LogAppender.println();
							LogAppender.error("画像が読み込めませんでした: "+entryName);
						}
					} catch (Exception e) {
						LogAppender.println();
						LogAppender.error("画像が読み込めませんでした: "+entryName);
						e.printStackTrace();
					} finally {
						if (is != null) is.close();
					}
				}
			}
		}
		} finally {
			LogAppender.println();
			archive.close();
		}
	}
	
	/** 圧縮ファイル内の画像で画像注記以外の画像も表紙に選択できるように追加 */
	public void addNoNameImageFileName()
	{
		//名前順にソートしてから追加
		Vector<String> names = new Vector<String>();
		for (String name : this.imageFileInfos.keySet()) {
			if (!this.imageFileNames.contains(name)) names.add(name);
		}
		Collections.sort(names, new FileNameComparator());
		for (String name : names) this.imageFileNames.add(name);
	}
	
	/** 指定した順番の画像情報を取得 
	 * @throws RarException */
	public BufferedImage getImage(int idx) throws IOException, RarException
	{
		return this.getImage(this.imageFileNames.get(idx));
	}
	
	
	/** ファイル名から画像を取得
	 * 拡張子変更等は外側で修正しておく
	 * ファイルシステムまたはZipファイルから指定されたファイル名の画像を取得
	 * @param srcImageFileName ファイル名 Zipならエントリ名
	 * ※先頭からシークされるので遅い? 
	 * @throws RarException */
	public BufferedImage getImage(String srcImageFileName) throws IOException, RarException
	{
		if (this.isFile) {
			File file = new File(this.srcParentPath+srcImageFileName);
			if (!file.exists()) {
				//拡張子修正
				srcImageFileName = this.correctExt(srcImageFileName);
				file = new File(this.srcParentPath+srcImageFileName);
				if (!file.exists()) return null;
			}
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 8192);
			try {
				return ImageUtils.readImage(srcImageFileName.substring(srcImageFileName.lastIndexOf('.')+1).toLowerCase(), bis);
			} finally { bis.close(); }
		} else {
			if (this.srcFile.getName().endsWith(".rar")) {
				InputStream is = null;
				Archive archive = new Archive(srcFile);
				try {
				FileHeader fileHeader = archive.nextFileHeader();
				while (fileHeader != null) {
					if (!fileHeader.isDirectory()) {
						String entryName = fileHeader.getFileNameW();
						if (entryName.length() == 0) entryName = fileHeader.getFileNameString();
						entryName = entryName.replace('\\', '/');
						if (srcImageFileName.equals(entryName)) {
							is = archive.getInputStream(fileHeader);
							return ImageUtils.readImage(srcImageFileName.substring(srcImageFileName.lastIndexOf('.')+1).toLowerCase(), is);
						}
					}
					fileHeader = archive.nextFileHeader();
				}
				} finally {
					if (is != null) is.close();
					archive.close();
				}
				
			} else {
				ZipFile zf = new ZipFile(this.srcFile, "MS932");
				ZipArchiveEntry entry = zf.getEntry(srcImageFileName);
				if (entry == null) {
					srcImageFileName = this.correctExt(srcImageFileName);
					entry = zf.getEntry(srcImageFileName);
					if (entry == null) return null;
				}
				InputStream is = zf.getInputStream(entry);
				try {
					return ImageUtils.readImage(srcImageFileName.substring(srcImageFileName.lastIndexOf('.')+1).toLowerCase(), is);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					is.close();
					zf.close();
				}
			}
		}
		return null;
	}
}
