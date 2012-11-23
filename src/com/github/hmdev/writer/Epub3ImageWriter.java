package com.github.hmdev.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.velocity.app.Velocity;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.info.SectionInfo;
import com.github.hmdev.util.LogAppender;

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
	String[] getTemplateFiles()
	{
		if (this.isKindle) return TEMPLATE_FILE_NAMES_KINDLE_IMAGE;
		if (this.bookInfo != null && this.bookInfo.vertical) return TEMPLATE_FILE_NAMES_VERTICAL_IMAGE;
		return TEMPLATE_FILE_NAMES_HORIZONTAL_IMAGE;
	}
	
	/** 出力先ePubのZipストリーム */
	ZipArchiveOutputStream zos;
	
	/** コンストラクタ
	 * @param templatePath epubテンプレート格納パス文字列 最後は"/"
	 */
	public Epub3ImageWriter(String templatePath)
	{
		super(templatePath);
	}
	
	/** 本文を出力する
	 * setFileNamesで sortedFileNames が設定されている必要がある */
	@Override
	void writeSections(AozoraEpub3Converter converter, BufferedReader src, BufferedWriter bw) throws IOException
	{
		//画像xhtmlを出力
		int pageNum = 0;
		for (String srcFilePath : this.imageInfoReader.getImageFileNames()) {
			pageNum++;
			srcFilePath = srcFilePath.trim();
			String fileName = this.getImageFilePath(srcFilePath, pageNum);
			if (fileName != null) {
				this.startImageSection(srcFilePath);
				bw.write(converter.getChukiValue("画像開始")[0]);
				bw.write(fileName);
				bw.write(converter.getChukiValue("画像終了")[0]);
				bw.flush();
				this.endSection();
			}
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
			if ((double)imageInfo.getWidth()/imageInfo.getHeight() >= (double)this.dispW/this.dispH) sectionInfo.setImageFitW(true);
			else sectionInfo.setImageFitH(true);
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
				LogAppender.append("画像フォーマットエラー: ("+(lineNum+1)+") "+srcImageFileName+"\n");
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
