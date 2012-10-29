package com.github.hmdev.writer;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.UUID;
import java.util.Vector;
import java.util.zip.CRC32;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.converter.PageBreakTrigger;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.ChapterInfo;
import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.info.SectionInfo;
import com.github.hmdev.util.ImageInfoReader;
import com.github.hmdev.util.LogAppender;

/** ePub3用のファイル一式をZipで固めたファイルを生成.
 * 本文は改ページでセクション毎に分割されて xhtml/以下に 0001.xhtml 0002.xhtml の連番ファイル名で格納
 * 画像は images/以下に 0001.jpg 0002.png のようにリネームして格納
 */
public class Epub3Writer
{
	/** MIMETYPEパス */
	final static String MIMETYPE_PATH = "mimetype";
	
	/** ORBパス */
	final static String OPS_PATH = "OPS/";
	/** 画像格納パス */
	final static String IMAGES_PATH = "images/";
	/** CSS格納パス */
	final static String CSS_PATH = "css/";
	/** xhtml格納パス */
	final static String XHTML_PATH = "xhtml/";
	
	/** xhtmlヘッダVelocityテンプレート */
	final static String XHTML_HEADER_VM = "xhtml_header.vm";
	/** xhtmlフッタVelocityテンプレート */
	final static String XHTML_FOOTER_VM = "xhtml_footer.vm";
	
	/** navファイル 未対応 */
	final static String XHTML_NAV_FILE = "nav.xhtml";
	/** navファイル Velocityテンプレート 未対応 */
	final static String XHTML_NAV_VM = "xhtml_nav.vm";
	
	/** 表紙XHTMLファイル */
	final static String COVER_FILE = "cover.xhtml";
	/** 表紙ページ Velocityテンプレート */
	final static String COVER_VM = "cover.vm";
	
	/** opfファイル */
	final static String PACKAGE_FILE = "package.opf";
	/** opfファイル Velocityテンプレート */
	final static String PACKAGE_VM = "package.vm";
	
	/** tocファイル */
	final static String TOC_FILE = "toc.ncx";
	/** tocファイル Velocityテンプレート */
	final static String TOC_VM = "toc.ncx.vm";
	
	/** コピーのみのファイル */
	final static String[] TEMPLATE_FILE_NAMES_VERTICAL = new String[]{
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"vertical_text.css",
		OPS_PATH+CSS_PATH+"vertical_middle.css",
		OPS_PATH+CSS_PATH+"vertical_image.css",
		OPS_PATH+CSS_PATH+"vertical_font.css",
		OPS_PATH+CSS_PATH+"vertical.css"
	};
	final static String[] TEMPLATE_FILE_NAMES_HORIZONTAL = new String[]{
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"horizontal_text.css",
		OPS_PATH+CSS_PATH+"horizontal_middle.css",
		OPS_PATH+CSS_PATH+"horizontal_image.css",
		OPS_PATH+CSS_PATH+"horizontal_font.css",
		OPS_PATH+CSS_PATH+"horizontal.css"
	};
	String[] getTemplateFiles()
	{
		if (this.bookInfo != null && this.bookInfo.vertical) return TEMPLATE_FILE_NAMES_VERTICAL;
		return TEMPLATE_FILE_NAMES_HORIZONTAL;
	}
	
	////////////////////////////////
	//Properties
	/** 画面サイズ */
	int dispW = 600;
	int dispH = 800;
	
	/** 画像最大幅 0は指定なし */
	int maxImageW = 0;
	/** 画像最大高さ 0は指定なし */
	int maxImageH = 0;
	/** 最大画素数 10000未満は指定なし */
	int maxImagePixels = 0;
	
	/** 縦横指定サイズ以上を単ページ化する時の横 */
	int singlePageSizeW = 400;
	/** 縦横指定サイズ以上を単ページ化する時の縦 */
	int singlePageSizeH = 600;
	/** 縦に関係なく横がこれ以上なら単ページ化 */
	int singlePageWidth = 550;
	
	/** 画像を拡大する */
	boolean fitImage = true;
	
	/** jpeg圧縮率 */
	float jpegQuality = 0.8f;
	
	////////////////////////////////
	//4bitグレースケール時のRGB階調
	byte[] GRAY_VALUES = new byte[]{-113,-97,-81,-65,-49,-33,-17,-1,15,31,47,63,79,95,111,127};
	
	/** 出力先ePubのZipストリーム */
	ZipArchiveOutputStream zos;
	
	/** ファイル名桁揃え用 */
	final static DecimalFormat decimalFormat = new DecimalFormat("0000");
	/** 更新日時フォーマット 2011-06-29T12:00:00Z */
	final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	/** セクション番号自動追加用インデックス */
	int sectionIndex = 0;
	/** 画像番号自動追加用インデックス */
	int imageIndex = 0;
	
	/** 改ページでセクション分割されたセクション番号(0001)を格納 カバー画像(cover)等も含む */
	Vector<SectionInfo> sectionInfos;
	/** 章の名称を格納(仮) */
	Vector<ChapterInfo> chapterInfos;
	
	/** 画像情報リスト Velocity埋め込み */
	Vector<ImageInfo> imageInfos;
	
	/** 出力済みのファイル名 (画像なしチェック用) */
	HashSet<String> outImageFileNames; 
	
	/** Velocity変数格納コンテキスト */
	VelocityContext velocityContext;
	
	/** テンプレートパス */
	String templatePath;
	
	/** 出力中の書籍情報 */
	BookInfo bookInfo;
	/** 出力中の画像情報 */
	ImageInfoReader imageInfoReader;
	
	/** コンストラクタ
	 * @param templatePath epubテンプレート格納パス文字列 最後は"/"
	 */
	public Epub3Writer(String templatePath)
	{
		this.templatePath = templatePath;
		//初回実行時のみ有効
		Velocity.init();
	}
	
	/** 画像のリサイズ用パラメータを設定 */
	public void setImageParam(int dispW, int dispH, int resizeW, int resizeH, int pixels, int singlePageSizeW, int singlePageSizeH, int singlePageWidth, boolean fitImage,  float jpegQuality)
	{
		this.dispW = dispW;
		this.dispH = dispH;
		
		this.maxImageW = resizeW;
		this.maxImageH = resizeH;
		this.maxImagePixels = pixels;
		
		this.singlePageSizeW = singlePageSizeW;
		this.singlePageSizeH = singlePageSizeH;
		this.singlePageWidth = singlePageWidth;
		
		this.fitImage = fitImage;
		
		this.jpegQuality = jpegQuality;
	}
	
	/** epubファイルを出力
	 * @param converter 青空文庫テキスト変換クラス 画像のみの場合と切り替えて利用する
	 * @param src 青空文庫テキストファイルの入力Stream
	 * @param srcFile 青空文庫テキストファイル zip時の画像取得用
	 * @param zipTextFileName zipの場合はzip内のテキストファイルのパス付きファイル名
	 * @param epubFile 出力ファイル .epub拡張子
	 * @param bookInfo 書籍情報と縦横書き指定
	 * @param zipImageFileInfos zipの場合はzip内画像の情報 key=サブフォルダ付きの画像ファイル名
	 * @throws IOException */
	public void write(AozoraEpub3Converter converter, BufferedReader src, File srcFile, File epubFile, BookInfo bookInfo, ImageInfoReader imageInfoReader) throws IOException
	{
		this.bookInfo = bookInfo;
		this.imageInfoReader = imageInfoReader;
		//インデックス初期化
		this.sectionIndex = 0;
		this.imageIndex = 0;
		this.sectionInfos = new Vector<SectionInfo>();
		this.chapterInfos = new Vector<ChapterInfo>();
		this.imageInfos = new Vector<ImageInfo>();
		this.outImageFileNames = new HashSet<String>();
		
		//Velocity用 共通コンテキスト設定
		this.velocityContext = new VelocityContext();
		
		//IDはタイトル著作者のハッシュで適当に生成
		String title = bookInfo.title==null?"":bookInfo.title;
		String creator = bookInfo.creator==null?"":bookInfo.creator;
		if ("".equals(bookInfo.creator)) bookInfo.creator = null;
		//固有ID
		velocityContext.put("identifier", UUID.nameUUIDFromBytes((title+"-"+creator).getBytes()));
		//目次名称
		velocityContext.put("toc_name", "目次");
		//表紙
		velocityContext.put("cover_name", "表紙");
		//書籍情報
		velocityContext.put("bookInfo", bookInfo);
		//更新日時
		velocityContext.put("modified", dateFormat.format(bookInfo.modified));
		
		//出力先ePubのZipストリーム生成
		zos = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(epubFile)));
		//mimetypeは非圧縮
		//STOREDで格納しCRCとsizeを指定する必要がある
		ZipArchiveEntry mimeTypeEntry = new ZipArchiveEntry(MIMETYPE_PATH);
		FileInputStream fis = new FileInputStream(new File(templatePath+MIMETYPE_PATH));
		byte[] b = new byte[256];
		int len = fis.read(b);
		fis.close();
		CRC32 crc32 = new CRC32();
		crc32.update(b, 0, len);
		mimeTypeEntry.setMethod(ZipArchiveEntry.STORED);
		mimeTypeEntry.setCrc(crc32.getValue());
		mimeTypeEntry.setSize(len);
		zos.putArchiveEntry(mimeTypeEntry);
		zos.write(b, 0, len);
		b = null;
		zos.closeArchiveEntry();
		
		zos.setLevel(9);
		//テンプレートのファイルを格納
		for (String fileName : getTemplateFiles()) {
			zos.putArchiveEntry(new ZipArchiveEntry(fileName));
			fis = new FileInputStream(new File(templatePath+fileName));
			IOUtils.copy(fis, zos);
			fis.close();
			zos.closeArchiveEntry();
		}
		
		//zip出力用Writer
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		
		//本文を出力
		this.writeSections(converter, src, bw);
		
		//表紙データと表示の画像情報
		byte[] coverImageBytes = null;
		ImageInfo coverImageInfo = null;
		if (bookInfo.coverFileName != null && bookInfo.coverFileName.length() > 0) {
			//表紙情報をimageInfosに追加
			try {
				//表紙設定解除
				for(ImageInfo imageInfo2 : imageInfos) {
					imageInfo2.setIsCover(false);
				}
				BufferedInputStream bis;
				if (bookInfo.coverFileName.startsWith("http")) {
					bis = new BufferedInputStream(new URL(bookInfo.coverFileName).openStream(), 8192);
				} else {
					bis = new BufferedInputStream(new FileInputStream(new File(bookInfo.coverFileName)), 8192);
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(bis, baos);
				coverImageBytes = baos.toByteArray();
				bis.close();
				baos.close();
				ByteArrayInputStream bais = new ByteArrayInputStream(coverImageBytes);
				coverImageInfo = ImageInfo.getImageInfo(bais);
				bais.close();
				String ext = coverImageInfo.getExt();
				if (ext.equals("jpeg")) ext = "jpg";
				coverImageInfo.setId("0000");
				coverImageInfo.setOutFileName("0000."+ext);
				if (!ext.matches("^(png|jpg|jpeg|gif)$")) {
					LogAppender.append("表紙画像フォーマットエラー: "+bookInfo.coverFileName+"\n");
					coverImageInfo = null;
				} else {
					coverImageInfo.setIsCover(true);
					this.imageInfos.add(0, coverImageInfo);
				}
			} catch (Exception e) { e.printStackTrace(); }
		} else if (bookInfo.coverImage != null) {
			//表紙設定解除
			for(ImageInfo imageInfo2 : imageInfos) {
				imageInfo2.setIsCover(false);
			}
			//プレビューでトリミングされた表紙
			String ext = "jpg";
			if (bookInfo.coverExt != null) {
				ext = bookInfo.coverExt;
			} else if (bookInfo.coverImageIndex > -1) {
				ext = imageInfoReader.getImageInfo(bookInfo.coverImageIndex).getExt();
			}
			if (ext.equals("jpeg")) ext = "jpg";
			coverImageInfo = ImageInfo.getImageInfo(ext, bookInfo.coverImage, -1);
			coverImageInfo.setId("0000");
			coverImageInfo.setOutFileName("0000."+ext);
			coverImageInfo.setIsCover(true);
			this.imageInfos.add(0, coverImageInfo);
		}
		
		//表紙ページ出力 先頭画像表示時は画像出力時にカバー指定するので出力しない
		if (bookInfo.insertCoverPage) {
			ImageInfo coverPageImage = coverImageInfo;
			//先頭画像から取得
			if (coverPageImage == null) {
				for (ImageInfo imageInfo2 : imageInfos) {
					if (imageInfo2.getIsCover()) {
						coverPageImage = imageInfo2;
						break;
					}
				}
			}
			if (coverPageImage != null) {
				//画像が横長なら幅100% それ以外は高さ100%
				SectionInfo sectionInfo = new SectionInfo("cover-page");
				if ((double)coverPageImage.getWidth()/coverPageImage.getHeight() >= 3.0/4) sectionInfo.setImageFitW(true);
				this.velocityContext.put("sectionInfo", sectionInfo);
				this.velocityContext.put("coverImage", coverPageImage);
				zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+COVER_FILE));
				bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
				Velocity.mergeTemplate(templatePath+OPS_PATH+XHTML_PATH+COVER_VM, "UTF-8", velocityContext, bw);
				bw.flush();
				zos.closeArchiveEntry();
			} else {
				//画像がなかったら表紙ページ無し
				bookInfo.insertCoverPage = false;
			}
		}
		
		//タイトルページは削除して別途0001.xhmlのみ目次前に出力
		if (bookInfo.insertTitlePage) sectionInfos.remove(0);
		
		//package.opf 出力
		velocityContext.put("sections", sectionInfos);
		velocityContext.put("images", imageInfos);
		zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+PACKAGE_FILE));
		bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.mergeTemplate(templatePath+OPS_PATH+PACKAGE_VM, "UTF-8", velocityContext, bw);
		bw.flush();
		zos.closeArchiveEntry();
		
		//navファイル
		velocityContext.put("chapters", chapterInfos);
		zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+XHTML_NAV_FILE));
		bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.mergeTemplate(templatePath+OPS_PATH+XHTML_PATH+XHTML_NAV_VM, "UTF-8", velocityContext, bw);
		bw.flush();
		zos.closeArchiveEntry();
		
		//tocファイル
		velocityContext.put("chapters", chapterInfos);
		zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+TOC_FILE));
		bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.mergeTemplate(templatePath+OPS_PATH+TOC_VM, "UTF-8", velocityContext, bw);
		bw.flush();
		zos.closeArchiveEntry();
		
		if (src != null) src.close();
		
		zos.setLevel(0);
		////////////////////////////////////////////////////////////////////////////////////////////////
		//画像ファイルコピー (連番にリネーム)
		//表紙指定があればそれを入力に設定 先頭画像のisCoverはfalseになっている
		//プレビューで編集された場合はここで追加する TODO 置き換えずに画像を出力する場合はCover設定を解除してpackage.opfの方で調整
		////////////////////////////////
		//表紙
		if (coverImageInfo != null) {
			try {
				//プレビューで編集されている場合
				ImageInfo imageInfo = imageInfos.get(0);
				if (bookInfo.coverImage != null) {
					zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+imageInfo.getOutFileName()));
					this.writeImage(imageInfo.getOutFileName(), bookInfo.coverImage, zos, coverImageInfo);
					zos.closeArchiveEntry();
					bookInfo.coverImage = null; //同じ画像が使われている場合は以後はファイルから読み込ませる
				} else {
					ByteArrayInputStream bais = new ByteArrayInputStream(coverImageBytes);
					zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+imageInfo.getOutFileName()));
					this.writeImage(imageInfo.getOutFileName(), bais, zos, coverImageInfo);
					zos.closeArchiveEntry();
					bais.close();
				}
				imageInfos.remove(0);//カバー画像は出力済みなので削除
			} catch (Exception e) {
				e.printStackTrace();
				LogAppender.append("[ERROR] 表紙画像取得エラー: "+bookInfo.coverFileName+"\n");
			}
		}
		
		//出力済み画像チェック用
		if (srcFile.getName().toLowerCase().endsWith(".txt")) {
			////////////////////////////////
			//txtの場合はファイルシステムから取得
			for (String srcImageFileName : imageInfoReader.getImageFileNames()) {
				if (outImageFileNames.contains(srcImageFileName)) {
					ImageInfo imageInfo = imageInfoReader.getImageInfo(srcImageFileName);
					if (imageInfo == null) {
						LogAppender.append("[WARN] 画像ファイルなし: "+srcImageFileName+"\n");
					} else {
						File imageFile = imageInfoReader.getImageFile(srcImageFileName);
						if (imageFile.exists()) {
							fis = new FileInputStream(imageFile);
							zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+imageInfo.getOutFileName()));
							//プレビューで編集されていたらイメージを出力
							if (imageInfo.getIsCover() && bookInfo.coverImage != null) {
								this.writeImage(srcImageFileName, bookInfo.coverImage, zos, imageInfo);
							} else {
								this.writeImage(srcImageFileName, new BufferedInputStream(fis, 8192), zos, imageInfo);
							}
							zos.closeArchiveEntry();
							fis.close();
							outImageFileNames.remove(srcImageFileName);
						}
					}
				}
			}
		} else {
			////////////////////////////////
			//zipの場合はzip内画像をすべて出力
			int zipPathLength = 0;
			if (this.bookInfo.textEntryName != null) zipPathLength = this.bookInfo.textEntryName.indexOf('/')+1;
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
			ArchiveEntry entry;
			while( (entry = zis.getNextZipEntry()) != null ) {
				//Zip内のサブフォルダは除外してテキストからのパスにする
				String srcImageFileName = entry.getName().substring(zipPathLength);
				//if (outImageFileNames.contains(srcImageFileName)) {
				ImageInfo imageInfo = imageInfoReader.getImageInfo(srcImageFileName);
				if (imageInfo != null && imageInfo.getId() != null) {
					zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+imageInfo.getOutFileName()));
					//プレビューで編集されていたらイメージを出力
					if (imageInfo.getIsCover() && bookInfo.coverImage != null) {
						this.writeImage(srcImageFileName, bookInfo.coverImage, zos, imageInfo);
					} else {
						//Zipからの直接読み込みは失敗するので一旦バイト配列にする
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOUtils.copy(zis, baos);
						byte[] bytes = baos.toByteArray();
						baos.close();
						ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
						this.writeImage(srcImageFileName, bais, zos, imageInfo);
						bais.close();
					}
					zos.closeArchiveEntry();
				}
				//	outImageFileNames.remove(srcImageFileName);
				//}
			}
			zis.close();
		}
		
		//ePub3出力ファイルを閉じる
		zos.close();
		
		this.velocityContext = null;
		this.sectionInfos = null;
		this.chapterInfos = null;
		this.imageInfos = null;
		this.outImageFileNames = null;
		this.bookInfo = null;
		this.imageInfoReader = null;
	}
	
	void writeImage(String srcFileName, InputStream is,ZipArchiveOutputStream zos, ImageInfo imageInfo) throws IOException
	{
		writeImage(srcFileName, is, null, zos, imageInfo);
	}
	void writeImage(String srcFileName, BufferedImage srcImage, ZipArchiveOutputStream zos, ImageInfo imageInfo) throws IOException
	{
		writeImage(srcFileName, null, srcImage, zos, imageInfo);
	}
	/** 大きすぎる画像は縮小して出力 
	 * @throws IOException */
	void writeImage(String srcFileName, InputStream is, BufferedImage srcImage, ZipArchiveOutputStream zos, ImageInfo imageInfo) throws IOException
	{
		double scale = 1;
		int w = imageInfo.getWidth();
		int h = imageInfo.getHeight();
		if (this.maxImagePixels >= 10000) scale = Math.sqrt((double)this.maxImagePixels/(w*h)); //最大画素数指定
		if (this.maxImageW > 0) scale = Math.min(scale, (double)this.maxImageW/w); //最大幅指定
		if (this.maxImageH > 0) scale = Math.min(scale, (double)this.maxImageH/h); //最大高さ指定
		
		if (scale >= 1) {
			//TODO 画像回転対応
			if (srcImage == null) {
				IOUtils.copy(is, zos);
			} else {
				ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(imageInfo.getExt()).next();
				ImageWriteParam iwp = imageWriter.getDefaultWriteParam();
				if (iwp.canWriteCompressed()) {
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					if (imageInfo.getExt().indexOf('j') == 0) iwp.setCompressionQuality(this.jpegQuality);
				}
				imageWriter.setOutput(ImageIO.createImageOutputStream(zos));
				imageWriter.write(null, new IIOImage(srcImage, null, null), iwp);
				zos.flush();
			}
		} else {
			int scaledW = (int)(imageInfo.getWidth()*scale);
			int scaledH = (int)(imageInfo.getHeight()*scale);
			try {
				if (srcImage == null) srcImage = ImageInfoReader.readImage(srcFileName.substring(srcFileName.lastIndexOf('.')+1).toLowerCase(), is);
				//TODO 画像回転対応
				int imageType = srcImage.getType();
				BufferedImage outImage;
				if (imageType == BufferedImage.TYPE_BYTE_BINARY) {
					ColorModel colorModel = srcImage.getColorModel();
					//4bit未満は縮小するので4bitに
					//if (colorModel.getPixelSize() < 4)
						colorModel = new IndexColorModel(4, GRAY_VALUES.length, GRAY_VALUES, GRAY_VALUES, GRAY_VALUES);
					WritableRaster raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
					outImage = new BufferedImage(colorModel, raster, true, null);
				}
				else if (imageType == BufferedImage.TYPE_BYTE_INDEXED) {
					ColorModel colorModel = srcImage.getColorModel();
					WritableRaster raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
					outImage = new BufferedImage(colorModel, raster, true, null);
				} else {
					outImage = new BufferedImage(scaledW, scaledH, imageType);
				}
				Graphics2D g = outImage.createGraphics();
				try {
					AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_BICUBIC);
					g.drawImage(srcImage, ato, 0, 0);
					ImageIO.write(outImage, imageInfo.getExt(), zos);
					LogAppender.append("画像縮小: "+imageInfo.getOutFileName()+" ("+w+","+h+")→("+scaledW+","+scaledH+")\n");
				} finally {
					g.dispose();
				}
			} catch (Exception e) {
				LogAppender.append("画像読み込みエラー: "+imageInfo.getOutFileName()+"\n");
				e.printStackTrace();
			}
			zos.flush();
		}
	}
	
	/** 本文を出力する */
	void writeSections(AozoraEpub3Converter converter, BufferedReader src, BufferedWriter bw) throws IOException
	{
		//this.startSection(0, bookInfo.startMiddle);
		
		//ePub3変換して出力
		//改ページ時にnextSection() を、画像出力時にgetImageFilePath() 呼び出し
		converter.convertTextToEpub3(bw, src, bookInfo);
		bw.flush();
		
		this.endSection();
	}
	
	/** 次のチャプター用のZipArchiveEntryに切替え 
	 * チャプターのファイル名はcpaterFileNamesに追加される (0001)
	 * @throws IOException */
	public void nextSection(BufferedWriter bw, int lineNum, boolean isMiddle, int imagePageType, String srcImageFilePath) throws IOException
	{
		if (this.sectionIndex >0) {
			bw.flush();
			this.endSection();
		}
		this.startSection(lineNum, isMiddle, imagePageType, srcImageFilePath);
	}
	/** セクション開始. 
	 * @throws IOException */
	void startSection(int lineNum, boolean isMiddle, int imagePageType, String srcImageFilePath) throws IOException
	{
		this.sectionIndex++;
		String sectionId = decimalFormat.format(this.sectionIndex);
		//package.opf用にファイル名
		SectionInfo sectionInfo = new SectionInfo(sectionId);
		//次の行が単一画像なら画像専用指定
		switch (imagePageType) {
		case PageBreakTrigger.IMAGE_PAGE_AUTO:
			//未使用
			sectionInfo.setImagePage(true);
			//画像サイズが横長なら幅に合わせる
			ImageInfo imageInfo = this.imageInfoReader.getImageInfo(srcImageFilePath);
			if (imageInfo != null) {
				//小さい画像をそのまま出す場合
				if (!this.fitImage && imageInfo.getWidth() <= this.dispW && imageInfo.getHeight() < this.dispH) {
				} else {
					//横長ならwidth100％
					if ((double)imageInfo.getWidth()/imageInfo.getHeight() >= (double)this.dispW/this.dispH) sectionInfo.setImageFitW(true);
					//縦がはみ出すならheight:100%
					else sectionInfo.setImageFitH(true);
				}
			}
			break;
		case PageBreakTrigger.IMAGE_PAGE_W:
			sectionInfo.setImagePage(true);
			sectionInfo.setImageFitW(true);
			break;
		case PageBreakTrigger.IMAGE_PAGE_H:
			sectionInfo.setImagePage(true);
			sectionInfo.setImageFitH(true);
			break;
		case PageBreakTrigger.IMAGE_PAGE_NOFIT:
			sectionInfo.setImagePage(true);
			break;
		}
		if (isMiddle) sectionInfo.setMiddle(true);
		this.sectionInfos.add(sectionInfo);
		this.addChapter(sectionId, null); //章の名称はsectionIdを仮に設定
		
		this.zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+sectionId+".xhtml"));
		
		//ヘッダ出力
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		//出力開始するセクションに対応したSectionInfoを設定
		this.velocityContext.put("sectionInfo", sectionInfo);
		Velocity.mergeTemplate(this.templatePath+OPS_PATH+XHTML_PATH+XHTML_HEADER_VM, "UTF-8", velocityContext, bw);
		bw.flush();
	}
	/** セクション終了. 
	 * @throws IOException */
	void endSection() throws IOException
	{
		//フッタ出力
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.mergeTemplate(this.templatePath+OPS_PATH+XHTML_PATH+XHTML_FOOTER_VM, "UTF-8", velocityContext, bw);
		bw.flush();
		
		this.zos.closeArchiveEntry();
	}
	/** 章を追加 */
	public void addChapter(String chapterId, String name)
	{
		SectionInfo sectionInfo = this.sectionInfos.lastElement();
		this.chapterInfos.add(new ChapterInfo(sectionInfo.sectionId, chapterId, name));
	}
	/** 追加済の章の名称を変更 */
	public void updateChapterName(String name)
	{
		this.chapterInfos.lastElement().setChapterName(name);
	}
	
	/** 連番に変更した画像ファイル名を返却.
	 * 重複していたら前に出力したときの連番ファイル名を返す
	 * 返り値はxhtmlからの相対パスにする (../images/0001.jpg)
	 * 変更前と変更後のファイル名はimageFileNamesに格納される (images/0001.jpg)
	 * @return 画像タグを出力しない場合はnullを返す
	 * @throws IOException 
	 *  */
	public String getImageFilePath(String srcImageFileName, int lineNum) throws IOException
	{
		boolean isCover = false;
		
		ImageInfo imageInfo = this.imageInfoReader.getImageInfo(srcImageFileName);
		//拡張子修正
		if (imageInfo == null) {
			//画像があるかチェック
			srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".png");
			imageInfo = this.imageInfoReader.getImageInfo(srcImageFileName);
			if (imageInfo == null) {
				srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpg");
				imageInfo = this.imageInfoReader.getImageInfo(srcImageFileName);
			}
			if (imageInfo == null) {
				srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpeg");
				imageInfo = this.imageInfoReader.getImageInfo(srcImageFileName);
			}
			if (imageInfo != null) {
				LogAppender.append("[WARN] 画像拡張子変更: ("+lineNum+") "+srcImageFileName+"\n");
			}
		}
		if (imageInfo != null) {
			this.imageIndex++; //0001から開始
			String imageId = imageInfo.getId();
			//画像は未だ出力されていない
			if (imageId == null) {
				imageId = decimalFormat.format(this.imageIndex);
				this.imageInfos.add(imageInfo);
				this.outImageFileNames.add(srcImageFileName);
				if (this.imageIndex-1 == this.bookInfo.coverImageIndex) {
					imageInfo.setIsCover(true);
					isCover = true;
				}
			}
			String outImageFileName = imageId+"."+imageInfo.getExt().replaceFirst("jpeg", "jpg");
			imageInfo.setId(imageId);
			imageInfo.setOutFileName(outImageFileName);
			
			//先頭に表紙ページ移動の場合でカバーページならnullを返して本文中から削除
			if (bookInfo.insertCoverPage && isCover) return null;
			return "../"+IMAGES_PATH+outImageFileName;
		} else {
			this.imageIndex++;
			LogAppender.append("[WARN] 画像ファイルなし: ("+lineNum+") "+srcImageFileName+"\n");
		}
		return null;
	}
	
	/** 現在の出力済画像枚数を返す 0なら未出力 */
	public int getImageIndex()
	{
		return this.imageIndex;
	}
	
	/** 画像が単一ページ画像にできるかチェック
	 * @param srcFilePath テキスト内の画像相対パス文字列
	 * @throws IOException */
	public int getImagePageType(String srcFilePath, int tagLevel)
	{
		//タグ内ならそのまま出力
		if (tagLevel > 0) return PageBreakTrigger.IMAGE_PAGE_NONE;
		try {
			ImageInfo imageInfo = this.imageInfoReader.getImageInfo(srcFilePath);
			if (imageInfo == null) return PageBreakTrigger.IMAGE_PAGE_NONE;
			
			if (imageInfo.getWidth() >= this.singlePageWidth || imageInfo.getWidth() >= singlePageSizeW && imageInfo.getHeight() >= singlePageSizeH) {
				//拡大しない＆画面より小さい場合
				if (!this.fitImage && imageInfo.getWidth() <= this.dispW && imageInfo.getHeight() < this.dispH)
					return PageBreakTrigger.IMAGE_PAGE_NOFIT;
				//拡大するか画面より多きい場合
				if ((double)imageInfo.getWidth()/imageInfo.getHeight() > (double)this.dispW/this.dispH)
					return PageBreakTrigger.IMAGE_PAGE_W;
				else return PageBreakTrigger.IMAGE_PAGE_H;
			}
		} catch (Exception e) { e.printStackTrace(); }
		return PageBreakTrigger.IMAGE_PAGE_NONE;
	}
}
