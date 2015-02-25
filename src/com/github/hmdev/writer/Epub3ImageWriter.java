package com.github.hmdev.writer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.velocity.app.Velocity;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.info.SectionInfo;
import com.github.hmdev.util.LogAppender;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

/** ePub3用のファイル一式をZipで固めたファイルを生成.
 * 画像のみのZipの場合こちらで画像専用の処理を行う
 */
public class Epub3ImageWriter extends Epub3Writer
{
	/** コピーのみのファイル */
	final static String[] TEMPLATE_FILE_NAMES_VERTICAL_IMAGE = new String[]{
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"vertical_image.css"
	};
	final static String[] TEMPLATE_FILE_NAMES_HORIZONTAL_IMAGE = new String[]{
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"horizontal_image.css"
	};
	final static String[] TEMPLATE_FILE_NAMES_KINDLE_IMAGE = new String[]{
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"kindle_image.css"
	};
	final static String[] TEMPLATE_FILE_NAMES_SVG_IMAGE = new String[]{
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"svg_image.css"
	};
	String[] getTemplateFiles()
	{
		if (this.isKindle) return TEMPLATE_FILE_NAMES_KINDLE_IMAGE;
		if (this.isSvgImage) return TEMPLATE_FILE_NAMES_SVG_IMAGE;
		if (this.bookInfo != null && this.bookInfo.vertical) return TEMPLATE_FILE_NAMES_VERTICAL_IMAGE;
		return TEMPLATE_FILE_NAMES_HORIZONTAL_IMAGE;
	}
	
	/** 出力先ePubのZipストリーム */
	ZipArchiveOutputStream zos;
	
	/** コンストラクタ
	 * @param templatePath epubテンプレート格納パス文字列 最後は"/"
	 */
	public Epub3ImageWriter(String jarPath)
	{
		super(jarPath);
	}
	
	/** 本文を出力する
	 * setFileNamesで sortedFileNames が設定されている必要がある 
	 * @throws RarException */
	@Override
	void writeSections(AozoraEpub3Converter converter, BufferedReader src, BufferedWriter bw, File srcFile, String srcExt, ZipArchiveOutputStream zos) throws IOException, RarException
	{
		Vector<String> vecFileName = new Vector<>();
		//ファイル名取得してImageInfoのIDを設定
		int pageNum = 0;
		for (String srcFilePath : this.imageInfoReader.getImageFileNames()) {
			if (this.canceled) return;
			pageNum++;
			vecFileName.add(this.getImageFilePath(srcFilePath.trim(), pageNum));
		}
		
		//画像を出力して出力サイズを取得
		zos.setLevel(0);
		//サブパスの文字長
		int archivePathLength = 0;
		if (this.bookInfo.textEntryName != null) archivePathLength = this.bookInfo.textEntryName.indexOf('/')+1;
		
		if ("rar".equals(srcExt)) {
			Archive archive = new Archive(srcFile);
			try {
			for (FileHeader fileHeader : archive.getFileHeaders()) {
				if (!fileHeader.isDirectory()) {
					String entryName = fileHeader.getFileNameW();
					if (entryName.length() == 0) entryName = fileHeader.getFileNameString();
					entryName = entryName.replace('\\', '/');
					//アーカイブ内のサブフォルダは除外
					String srcImageFileName = entryName.substring(archivePathLength);
					InputStream is = archive.getInputStream(fileHeader);
					try {
						this.writeArchiveImage(srcImageFileName, is);
					} finally {
						is.close();
					}
				}
				if (this.canceled) return;
			}
			} finally { archive.close(); }
		} else {
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
			try {
			ArchiveEntry entry;
			while( (entry = zis.getNextZipEntry()) != null ) {
				//アーカイブ内のサブフォルダは除外
				String srcImageFileName = entry.getName().substring(archivePathLength);
				this.writeArchiveImage(srcImageFileName, zis);
				if (this.canceled) return;
			}
			} finally { zis.close(); }
		}
		
		//画像xhtmlを出力
		zos.setLevel(9);
		pageNum = 0;
		for (String srcFilePath : this.imageInfoReader.getImageFileNames()) {
			if (this.canceled) return;
			String fileName = vecFileName.get(pageNum++);
			if (fileName != null) {
				if (isSvgImage) {
					this.printSvgImageSection(srcFilePath);
				} else {
					this.startImageSection(srcFilePath);
					bw.write(String.format(converter.getChukiValue("画像")[0], fileName));
					bw.write(converter.getChukiValue("画像終わり")[0]);
					bw.flush();
					this.endSection();
				}
			}
			if (this.jProgressBar != null) this.jProgressBar.setValue(this.jProgressBar.getValue()+1);
			if (this.canceled) return;
		}
	}
	
	/** セクション開始. 
	 * @throws IOException */
	private void startImageSection(String srcImageFilePath) throws IOException
	{
		this.sectionIndex++;
		String sectionId = decimalFormat.format(this.sectionIndex);
		//package.opf用にファイル名
		SectionInfo sectionInfo = new SectionInfo(sectionId);
		
		//画像専用指定
		sectionInfo.setImagePage(true);
		//画像サイズが横長なら幅に合わせる
		ImageInfo imageInfo = this.imageInfoReader.getImageInfo(srcImageFilePath);
		if (imageInfo != null) {
			if ((double)imageInfo.getWidth()/imageInfo.getHeight() >= (double)this.dispW/this.dispH) {
				if (this.rotateAngle != 0 && this.dispW < this.dispH && (double)imageInfo.getHeight()/imageInfo.getWidth() < (double)this.dispW/this.dispH) { //縦長画面で横長
					imageInfo.rotateAngle = this.rotateAngle;
					if (this.imageSizeType != SectionInfo.IMAGE_SIZE_TYPE_AUTO) sectionInfo.setImageFitH(true);
				} else {
					//高さでサイズ調整する場合は高さの%指定
					if (this.imageSizeType == SectionInfo.IMAGE_SIZE_TYPE_HEIGHT) sectionInfo.setImageHeight(((double)imageInfo.getHeight()/imageInfo.getWidth())*((double)this.dispW/this.dispH));
					else if (this.imageSizeType == SectionInfo.IMAGE_SIZE_TYPE_ASPECT) sectionInfo.setImageFitW(true);
				}
			}
			else {
				if (this.rotateAngle != 0 && this.dispW > this.dispH && (double)imageInfo.getHeight()/imageInfo.getWidth() > (double)this.dispW/this.dispH) { //横長画面で縦長
					imageInfo.rotateAngle = this.rotateAngle;
					//高さでサイズ調整する場合は高さの%指定
					if (this.imageSizeType == SectionInfo.IMAGE_SIZE_TYPE_HEIGHT) sectionInfo.setImageHeight(((double)imageInfo.getHeight()/imageInfo.getWidth())*((double)this.dispW/this.dispH));
					else if (this.imageSizeType == SectionInfo.IMAGE_SIZE_TYPE_ASPECT) sectionInfo.setImageFitW(true);
				} else {
					if (this.imageSizeType != SectionInfo.IMAGE_SIZE_TYPE_AUTO) sectionInfo.setImageFitH(true);
				}
			}
		}
		
		this.sectionInfos.add(sectionInfo);
		if (this.sectionIndex ==1 || this.sectionIndex % 5 == 0) this.addChapter(null, ""+this.sectionIndex, 0); //目次追加
		super.zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+sectionId+".xhtml"));
		//ヘッダ出力
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(super.zos, "UTF-8"));
		//出力開始するセクションに対応したSectionInfoを設定
		this.velocityContext.put("sectionInfo", sectionInfo);
		Velocity.getTemplate(this.templatePath+OPS_PATH+XHTML_PATH+XHTML_HEADER_VM).merge(this.velocityContext, bw);
		bw.flush();
	}
	
	/** SVGでセクション出力 */
	private void printSvgImageSection(String srcImageFilePath) throws IOException
	{
		this.sectionIndex++;
		String sectionId = decimalFormat.format(this.sectionIndex);
		//package.opf用にファイル名
		SectionInfo sectionInfo = new SectionInfo(sectionId);
		ImageInfo imageInfo = this.imageInfoReader.getImageInfo(srcImageFilePath);
		
		//画像専用指定
		sectionInfo.setImagePage(true);
		this.sectionInfos.add(sectionInfo);
		if (this.sectionIndex ==1 || this.sectionIndex % 5 == 0) this.addChapter(null, ""+this.sectionIndex, 0); //目次追加
		super.zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+sectionId+".xhtml"));
		//ヘッダ出力
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(super.zos, "UTF-8"));
		//出力開始するセクションに対応したSectionInfoを設定
		this.velocityContext.put("sectionInfo", sectionInfo);
		this.velocityContext.put("imageInfo", imageInfo);
		Velocity.getTemplate(this.templatePath+OPS_PATH+XHTML_PATH+SVG_IMAGE_VM).merge(this.velocityContext, bw);
		bw.flush();
	}
	
	@Override
	public String getImageFilePath(String srcImageFileName, int lineNum)
	{
		boolean isCover = false;
		this.imageIndex++; //xhtmlと画像ファイル名の番号を合わせるため先に++
		String ext = "";
		try { ext = srcImageFileName.substring(srcImageFileName.lastIndexOf('.')+1).toLowerCase(); } catch (Exception e) {}
		String imageId = decimalFormat.format(this.imageIndex);
		String imageFileName = IMAGES_PATH+imageId+"."+ext;
		ImageInfo imageInfo;
		try {
			imageInfo = this.imageInfoReader.getImageInfo(srcImageFileName);
			imageInfo.setId(imageId);
			imageInfo.setOutFileName(imageId+"."+ext);
			if (!imageInfo.getExt().matches("^(png|jpeg|gif|jpg)$")) {
				LogAppender.error(lineNum, "画像フォーマットエラー", srcImageFileName);
				return null;
			}
			if (this.imageIndex-1 == bookInfo.coverImageIndex) {
				imageInfo.setIsCover(true);
				isCover = true;
			}
			this.imageInfos.add(imageInfo);
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//先頭に表紙ページ移動の場合でカバーページならnullを返して本文中から削除
		if (bookInfo.insertCoverPage && isCover) return null;
		return "../"+imageFileName;
	}
}
