package com.github.hmdev.writer;

import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
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

import javax.swing.JProgressBar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.converter.PageBreakType;
import com.github.hmdev.image.ImageInfoReader;
import com.github.hmdev.image.ImageUtils;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.ChapterInfo;
import com.github.hmdev.info.ChapterLineInfo;
import com.github.hmdev.info.GaijiInfo;
import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.info.SectionInfo;
import com.github.hmdev.util.CharUtils;
import com.github.hmdev.util.LogAppender;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

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
	
	/** フォントファイル格納パス */
	final static String FONTS_PATH = "fonts/";
	/** 外字フォント格納パス */
	final static String GAIJI_PATH = "gaiji/";
	
	/** 縦書きcss */
	final static String VERTICAL_TEXT_CSS = "vertical_text.css";
	/** 縦書きcss Velocityテンプレート */
	final static String VERTICAL_TEXT_CSS_VM = "vertical_text.vm";
	/** 横書きcss */
	final static String HORIZONTAL_TEXT_CSS = "horizontal_text.css";
	/** 横書きcss Velocityテンプレート */
	final static String HORIZONTAL_TEXT_CSS_VM = "horizontal_text.vm";
	
	/** xhtmlヘッダVelocityテンプレート */
	final static String XHTML_HEADER_VM = "xhtml_header.vm";
	/** xhtmlフッタVelocityテンプレート */
	final static String XHTML_FOOTER_VM = "xhtml_footer.vm";
	
	/** タイトルページxhtml */
	final static String TITLE_FILE = "title.xhtml";
	/** タイトル縦中央 Velocityテンプレート */
	final static String TITLE_M_VM = "title_middle.vm";
	/** タイトル横書き Velocityテンプレート */
	final static String TITLE_H_VM = "title_horizontal.vm";
	
	/** navファイル */
	final static String XHTML_NAV_FILE = "nav.xhtml";
	/** navファイル Velocityテンプレート */
	final static String XHTML_NAV_VM = "xhtml_nav.vm";
	
	/** 表紙XHTMLファイル */
	final static String COVER_FILE = "cover.xhtml";
	/** 表紙ページ Velocityテンプレート */
	final static String COVER_VM = "cover.vm";
	
	/** SVG画像ページ Velocityテンプレート */
	final static String SVG_IMAGE_VM = "svg_image.vm";
	
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
		//OPS_PATH+CSS_PATH+"vertical_text.css",
		OPS_PATH+CSS_PATH+"vertical_middle.css",
		OPS_PATH+CSS_PATH+"vertical_image.css",
		OPS_PATH+CSS_PATH+"vertical_font.css",
		OPS_PATH+CSS_PATH+"vertical.css"
	};
	final static String[] TEMPLATE_FILE_NAMES_HORIZONTAL = new String[]{
		"META-INF/container.xml",
		//OPS_PATH+CSS_PATH+"horizontal_text.css",
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
	/** 画面サイズ 横 */
	int dispW = 600;
	/** 画面サイズ 縦 */
	int dispH = 800;
	
	/** 画像最大幅 0は指定なし */
	int maxImageW = 0;
	/** 画像最大高さ 0は指定なし */
	int maxImageH = 0;
	/** 最大画素数 10000未満は指定なし */
	int maxImagePixels = 0;
	
	/** 画像拡大表示倍率 0なら無効 */
	float imageScale = 1;
	
	/** 画像回り込み設定 0:なし 1:上 2:下 */
	int imageFloatType = 0;
	/** 画像回り込み幅 幅高さが以下なら回り込み */
	int imageFloatW = 0;
	/** 画像回り込み高さ 幅高さが以下なら回り込み */
	int imageFloatH = 0;
	
	/** 縦横指定サイズ以上を単ページ化する時の横 */
	int singlePageSizeW = 400;
	/** 縦横指定サイズ以上を単ページ化する時の縦 */
	int singlePageSizeH = 600;
	/** 縦に関係なく横がこれ以上なら単ページ化 */
	int singlePageWidth = 550;
	
	/** 単ページ表示時のサイズ指定方法 */
	int imageSizeType = SectionInfo.IMAGE_SIZE_TYPE_HEIGHT;
	
	/** 単ページで画像を拡大する */
	boolean fitImage = true;
	
	/** 画像を縦横比に合わせて回転する 右 90 左 -90 */
	int rotateAngle = 0;
	
	/** 余白自動調整 横 除去% */
	int autoMarginLimitH = 0;
	/** 余白自動調整 縦 除去% */
	int autoMarginLimitV = 0;
	/** 余白の白画素判別レベル 黒:0～白:100 */
	int autoMarginWhiteLevel = 100;
	/** 余白除去後に追加する余白 */
	float autoMarginPadding = 0;
	/** ノンブル除去種別 */
	int autoMarginNombre = 0;
	/** ノンブルの大きさ */
	float autoMarginNombreSize = 0.03f;
	
	/** 表紙サイズ 横 */
	int coverW = 600;
	/** 表紙サイズ 縦 */
	int coverH = 800;
	
	/** jpeg圧縮率 */
	float jpegQuality = 0.8f;
	
	/** ガンマフィルタ */
	LookupOp gammaOp;
	
	/** nav.xhtml階層化 */
	boolean navNest = false;
	/** toc.ncx階層化 */
	boolean ncxNest = false;
	
	/** svgタグのimageでxhtml出力 */
	boolean isSvgImage = false;
	
	/** 拡張子に.mobiが選択されていてkindlegenがある場合 */
	boolean isKindle = false;
	
	/** page余白 単位含む */
	String[] pageMargin = {"0", "0", "0", "0"};
	/** body余白 */
	String[] bodyMargin = {"0", "0", "0", "0"};
	/** 行の高さ em */
	float lineHeight;
	/** 文字サイズ % */
	int fontSize = 100;
	/** 太字注記を太字ゴシックで表示 */
	boolean boldUseGothic = true;
	/** ゴシック体注記を太字ゴシックで表示 */
	boolean gothicUseBold = true;
	
	////////////////////////////////
	/** 出力先ePubのZipストリーム ConverterからのnextSection呼び出しで利用 */
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
	
	/** 外字フォント情報 */
	Vector<GaijiInfo> vecGaijiInfo;
	/** 外字フォント重複除去 */
	HashSet<String> gaijiNameSet;
	
	/** 画像情報リスト Velocity埋め込み */
	Vector<ImageInfo> imageInfos;
	
	/** 出力対象のファイル名 (青空テキストの挿絵注記で追加され 重複出力のチェックに利用) */
	HashSet<String> outImageFileNames; 
	
	/** Velocity変数格納コンテキスト */
	VelocityContext velocityContext;
	
	/** テンプレートパス */
	String templatePath;
	
	/** 出力中の書籍情報 */
	BookInfo bookInfo;
	/** 出力中の画像情報 */
	ImageInfoReader imageInfoReader;
	
	/** プログレスバー AozoraConverterからも使う 利用しない場合はnull */
	public JProgressBar jProgressBar;
	
	/** 処理キャンセルフラグ */
	boolean canceled = false;
	
	/** コンストラクタ
	 * @param templatePath epubテンプレート格納パス文字列 最後は"/"
	 */
	public Epub3Writer(String templatePath)
	{
		this.templatePath = templatePath;
		//初回実行時のみ有効
		Velocity.init();
		this.sectionInfos = new Vector<SectionInfo>();
		this.chapterInfos = new Vector<ChapterInfo>();
		this.vecGaijiInfo = new Vector<GaijiInfo>();
		this.gaijiNameSet = new HashSet<String>();
		this.imageInfos = new Vector<ImageInfo>();
		this.outImageFileNames = new HashSet<String>();
	}
	/** プログレスバー設定 */
	public void setProgressBar(JProgressBar jProgressBar)
	{
		this.jProgressBar = jProgressBar;
	}
	/** 画像のリサイズ用パラメータを設定 */
	public void setImageParam(int dispW, int dispH, int coverW, int coverH,
			int resizeW, int resizeH,
			int singlePageSizeW, int singlePageSizeH, int singlePageWidth,
			int imageSizeType, boolean fitImage, boolean isSvgImage, int rotateAngle,
			float imageScale, int imageFloatType, int imageFloatW, int imageFloatH,
			float jpegQuality, float gamma,
			int autoMarginLimitH, int autoMarginLimitV, int autoMarginWhiteLevel, float autoMarginPadding, int autoMarginNombre, float nombreSize)
	{
		this.dispW = dispW;
		this.dispH = dispH;
		
		this.maxImageW = resizeW;
		this.maxImageH = resizeH;
		
		this.singlePageSizeW = singlePageSizeW;
		this.singlePageSizeH = singlePageSizeH;
		this.singlePageWidth = singlePageWidth;
		
		//0なら無効
		this.imageScale = imageScale;
		this.imageFloatType = imageFloatType;
		this.imageFloatW = imageFloatW;
		this.imageFloatH = imageFloatH;
		
		this.imageSizeType = imageSizeType;
		this.fitImage = fitImage;
		this.isSvgImage = isSvgImage;
		this.rotateAngle = rotateAngle;
		
		this.coverW = coverW;
		this.coverH = coverH;
		
		this.jpegQuality = jpegQuality;
		
		/*
		if (gamma < 1 && gamma > 0) gammaOp = new RescaleOp(1/gamma, -256*1/gamma+256, null);
		else if (gamma > 1) gammaOp = new RescaleOp(gamma, 0, null);*/
		if (gamma != 1) {
			byte[] table = new byte[256];
			for (int i=0; i<256; i++) {
				table[i] = (byte)Math.min(255, Math.round(255*Math.pow((i/255.0), 1/gamma)));
			}
			gammaOp = new LookupOp(new ByteLookupTable(0, table), null);
		} else gammaOp = null;
		
		this.autoMarginLimitH = autoMarginLimitH;
		this.autoMarginLimitV = autoMarginLimitV;
		this.autoMarginWhiteLevel = autoMarginWhiteLevel;
		this.autoMarginPadding = autoMarginPadding;
		this.autoMarginNombre = autoMarginNombre;
		this.autoMarginNombreSize = nombreSize;
	}
	
	public void setTocParam(boolean navNest, boolean ncxNest)
	{
		this.navNest = navNest;
		this.ncxNest = ncxNest;
	}
	
	public void setStyles(String[] pageMargin, String[] bodyMargin, float lineHeight, int fontSize, boolean boldUseGothic, boolean gothicUseBold)
	{
		this.pageMargin = pageMargin;
		this.bodyMargin = bodyMargin;
		this.lineHeight = lineHeight;
		this.fontSize = fontSize;
		this.boldUseGothic = boldUseGothic;
		this.gothicUseBold = gothicUseBold;
	}
	
	/** 処理を中止 */
	public void cancel()
	{
		this.canceled = true;
	}
	
	private void writeFile(ZipArchiveOutputStream zos, String fileName) throws IOException
	{
		zos.putArchiveEntry(new ZipArchiveEntry(fileName));
		//customファイル優先
		File file = new File(templatePath+fileName);
		int idx = fileName.lastIndexOf('/');
		if (idx > 0) { 
			File customFile = new File(templatePath+fileName.substring(0, idx)+"_custom/"+fileName.substring(idx+1));
			if (customFile.exists()) file = customFile;
		}
		FileInputStream fis = new FileInputStream(file);
		IOUtils.copy(fis, zos);
		fis.close();
		zos.closeArchiveEntry();
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
	public void write(AozoraEpub3Converter converter, BufferedReader src, File srcFile, String srcExt, File epubFile, BookInfo bookInfo, ImageInfoReader imageInfoReader) throws Exception
	{
		try {
		
		this.canceled = false;
		this.bookInfo = bookInfo;
		this.imageInfoReader = imageInfoReader;
		//インデックス初期化
		this.sectionIndex = 0;
		this.imageIndex = 0;
		this.sectionInfos.clear();
		this.chapterInfos.clear();
		this.vecGaijiInfo.clear();
		this.gaijiNameSet.clear();
		this.imageInfos.clear();
		this.outImageFileNames.clear();
		
		//Velocity用 共通コンテキスト設定
		this.velocityContext = new VelocityContext();
		
		//IDはタイトル著作者のハッシュで適当に生成
		String title = bookInfo.title==null?"":bookInfo.title;
		String creator = bookInfo.creator==null?"":bookInfo.creator;
		if ("".equals(bookInfo.creator)) bookInfo.creator = null;
		
		//固有ID
		velocityContext.put("identifier", UUID.nameUUIDFromBytes((title+"-"+creator).getBytes()));
		//表紙の目次表示名
		velocityContext.put("cover_name", "表紙");
		
		//タイトル &<>はエスケープ
		velocityContext.put("title", CharUtils.escapeHtml(title));
		//タイトル読み &<>はエスケープ
		if (bookInfo.titleAs != null) velocityContext.put("titleAs", CharUtils.escapeHtml(bookInfo.titleAs));
		//著者 &<>はエスケープ
		velocityContext.put("creator", CharUtils.escapeHtml(creator));
		//著者読み &<>はエスケープ
		if (bookInfo.creatorAs != null) velocityContext.put("creatorAs", CharUtils.escapeHtml(bookInfo.creatorAs));
		//刊行者情報
		if (bookInfo.publisher != null) velocityContext.put("publisher", bookInfo.publisher);
		
		//書籍情報
		velocityContext.put("bookInfo", bookInfo);
		//更新日時
		velocityContext.put("modified", dateFormat.format(bookInfo.modified));
		
		//目次階層化
		velocityContext.put("navNest", this.navNest);
		
		//端末種別
		if (this.isKindle) velocityContext.put("kindle", true);
		
		//SVG画像出力
		if (this.isSvgImage) velocityContext.put("svgImage", true);
		
		//スタイル
		velocityContext.put("pageMargin", this.pageMargin);
		velocityContext.put("bodyMargin", this.bodyMargin);
		velocityContext.put("lineHeight", this.lineHeight);
		velocityContext.put("fontSize", this.fontSize);
		velocityContext.put("boldUseGothic", this.boldUseGothic);
		velocityContext.put("gothicUseBold", this.gothicUseBold);
		
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
			writeFile(zos, fileName);
		}
		
		//サブパスの文字長
		int archivePathLength = 0;
		if (this.bookInfo.textEntryName != null) archivePathLength = this.bookInfo.textEntryName.indexOf('/')+1;
		
		//zip出力用Writer
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		
		//本文を出力
		this.writeSections(converter, src, bw, srcFile, srcExt, zos);
		if (this.canceled) return;
		
		//外字のcssを格納
		velocityContext.put("vecGaijiInfo", this.vecGaijiInfo);
		
		//スタイルと外字のcssを格納
		if (bookInfo.vertical) {
			zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+CSS_PATH+VERTICAL_TEXT_CSS));
			bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
			Velocity.mergeTemplate(templatePath+OPS_PATH+CSS_PATH+VERTICAL_TEXT_CSS_VM, "UTF-8", velocityContext, bw);
			bw.flush();
			zos.closeArchiveEntry();
		} else {
			zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+CSS_PATH+HORIZONTAL_TEXT_CSS));
			bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
			Velocity.mergeTemplate(templatePath+OPS_PATH+CSS_PATH+HORIZONTAL_TEXT_CSS_VM, "UTF-8", velocityContext, bw);
			bw.flush();
			zos.closeArchiveEntry();
		}
		
		//表紙をテンプレート＋メタ情報から生成 先に出力すると外字画像出力で表紙の順番が狂う
		if (!bookInfo.imageOnly && (bookInfo.titlePageType == BookInfo.TITLE_MIDDLE || bookInfo.titlePageType == BookInfo.TITLE_HORIZONTAL)) {
			String vmFilePath = templatePath+OPS_PATH+XHTML_PATH+TITLE_M_VM;
			if (bookInfo.titlePageType == BookInfo.TITLE_HORIZONTAL) {
				converter.vertical = false;
				vmFilePath = templatePath+OPS_PATH+XHTML_PATH+TITLE_H_VM;
			}
			//ルビと外字画像注記と縦中横注記(縦書きのみ)のみ変換する
			String line = bookInfo.getTitleText();
			if (line != null) velocityContext.put("TITLE", converter.convertTitleLineToEpub3(line));
			line = bookInfo.getSubTitleText();
			if (line != null) velocityContext.put("SUBTITLE", converter.convertTitleLineToEpub3(line));
			line = bookInfo.getOrgTitleText();
			if (line != null) velocityContext.put("ORGTITLE", converter.convertTitleLineToEpub3(line));
			line = bookInfo.getSubOrgTitleText();
			if (line != null) velocityContext.put("SUBORGTITLE", converter.convertTitleLineToEpub3(line));
			line = bookInfo.getCreatorText();
			if (line != null) velocityContext.put("CREATOR", converter.convertTitleLineToEpub3(line));
			line = bookInfo.getSubCreatorText();
			if (line != null) velocityContext.put("SUBCREATOR", converter.convertTitleLineToEpub3(line));
			line = bookInfo.getSeriesText();
			if (line != null) velocityContext.put("SERIES", converter.convertTitleLineToEpub3(line));
			line = bookInfo.getPublisherText();
			if (line != null) velocityContext.put("PUBLISHER", converter.convertTitleLineToEpub3(line));
			
			//package.opf内で目次前に出力
			zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+TITLE_FILE));
			bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
			Velocity.mergeTemplate(vmFilePath, "UTF-8", velocityContext, bw);
			bw.flush();
			zos.closeArchiveEntry();
			
			velocityContext.put("title_page", true);
			
			//表題行を目次に出力するならtitle.xhtmlを追加 （本文内の行はchapterinfosに追加されていない）
			ChapterLineInfo titleLineInfo = bookInfo.getChapterLineInfo(bookInfo.titleLine);
			if (titleLineInfo != null) {
				chapterInfos.add(0, new ChapterInfo("title", null, bookInfo.title, ChapterLineInfo.LEVEL_TITLE));
			}
		}
		
		if (this.canceled) return;
		
		//表紙データと表示の画像情報
		byte[] coverImageBytes = null;
		ImageInfo coverImageInfo = null;
		if (bookInfo.coverFileName != null && bookInfo.coverFileName.length() > 0) {
			//外部画像ファイルを表紙にする
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
				if (isKindle || ext.equals("jpeg")) ext = "jpg";
				coverImageInfo.setId("0000");
				coverImageInfo.setOutFileName("0000."+ext);
				if (!ext.matches("^(png|jpg|jpeg|gif)$")) {
					LogAppender.println("表紙画像フォーマットエラー: "+bookInfo.coverFileName);
					coverImageInfo = null;
				} else {
					coverImageInfo.setIsCover(true);
					this.imageInfos.add(0, coverImageInfo);
				}
			} catch (Exception e) { e.printStackTrace(); }
		} else if (bookInfo.coverImage != null) {
			//表紙画像が編集されていた場合
			//すべてのページの表紙設定解除
			for(ImageInfo imageInfo2 : imageInfos) {
				imageInfo2.setIsCover(false);
			}
			//プレビューでトリミングされた表紙
			String ext = "jpg";
			if (bookInfo.coverExt != null) {
				ext = bookInfo.coverExt;
			} else if (bookInfo.coverImageIndex > -1) {
				ImageInfo imageInfo = imageInfoReader.getImageInfo(bookInfo.coverImageIndex);
				if (imageInfo != null) ext = imageInfo.getExt();
			}
			if (isKindle || ext.equals("jpeg")) ext = "jpg";
			coverImageInfo = ImageInfo.getImageInfo(ext, bookInfo.coverImage, -1);
			coverImageInfo.setId("0000");
			coverImageInfo.setOutFileName("0000."+ext);
			coverImageInfo.setIsCover(true);
			this.imageInfos.add(0, coverImageInfo);
		} else {
			//本文にないzip内の表紙を出力対象に追加 (テキストからの相対パス)
			if (bookInfo.coverImageIndex > -1 && imageInfoReader.countImageFileNames() > bookInfo.coverImageIndex) {
				if (!"txt".equals(srcExt)) {
					String imageFileName = imageInfoReader.getImageFileName(bookInfo.coverImageIndex);
					if (imageFileName != null) {
						ImageInfo imageInfo = imageInfoReader.getImageInfo(imageFileName);
						if (imageInfo != null) {
							imageFileName = imageFileName.substring(archivePathLength);
							outImageFileNames.add(imageFileName);
							//表紙フラグも設定
							for(ImageInfo imageInfo2 : imageInfos) {
								imageInfo2.setIsCover(false);
							}
							imageInfo.setIsCover(true);
							if (!this.imageInfos.contains(imageInfo)) this.imageInfos.add(imageInfo);
						}
					}
				}
			}
		}
		
		//表紙ページ出力 先頭画像表示時は画像出力時にカバー指定するので出力しない
		if (bookInfo.insertCoverPage) {
			//追加用の情報取得にのみ使う
			ImageInfo insertCoverInfo = coverImageInfo;
			if (insertCoverInfo == null && bookInfo.coverImageIndex > -1) {
				//本文中の挿絵の場合
				insertCoverInfo = imageInfoReader.getImageInfo(bookInfo.coverImageIndex);
				if (insertCoverInfo != null) {
					insertCoverInfo.setIsCover(true);
					if (!bookInfo.imageOnly && insertCoverInfo.getId() == null) {
						//zip内の画像で追加処理されていない
						this.imageIndex++;
						String imageId = decimalFormat.format(this.imageIndex);
						insertCoverInfo.setId(imageId);
						String ext = insertCoverInfo.getExt();
						if (isKindle) ext = "jpg";
						insertCoverInfo.setOutFileName(imageId+"."+ext);
					}
				}
			}
			if (insertCoverInfo != null) {
				SectionInfo sectionInfo = new SectionInfo("cover-page");
				if (this.imageSizeType != SectionInfo.IMAGE_SIZE_TYPE_AUTO) {
					//画像が横長なら幅100% それ以外は高さ100%
					if ((double)insertCoverInfo.getWidth()/insertCoverInfo.getHeight() >= (double)this.coverW/this.coverH) sectionInfo.setImageFitW(true);
					else sectionInfo.setImageFitH(true);
				} else {
					sectionInfo.setImageFitW(false);
					sectionInfo.setImageFitH(false);
				}
				this.velocityContext.put("sectionInfo", sectionInfo);
				this.velocityContext.put("coverImage", insertCoverInfo);
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
		
		//package.opf 出力
		velocityContext.put("sections", sectionInfos);
		velocityContext.put("images", imageInfos);
		zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+PACKAGE_FILE));
		bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.mergeTemplate(templatePath+OPS_PATH+PACKAGE_VM, "UTF-8", velocityContext, bw);
		bw.flush();
		zos.closeArchiveEntry();
		
		//nullを除去
		for (int i=chapterInfos.size()-1; i>=0; i--) {
			if (chapterInfos.get(i).getChapterName() == null) chapterInfos.remove(i);
		}
		
		//表題のレベルを2つめと同じにする
		if (bookInfo.insertTitleToc && chapterInfos.size() >= 2) {
			chapterInfos.get(0).chapterLevel = chapterInfos.get(1).chapterLevel;
		}
		
		//目次の階層情報を設定
		//レベルを0から開始に変更
		int[] chapterCounts = new int[10];
		for (ChapterInfo chapterInfo : chapterInfos) {
			chapterCounts[chapterInfo.getChapterLevel()]++;
		}
		int[] newLevel = new int[10];
		int level = 0;
		for (int i=0; i<chapterCounts.length; i++) {
			if (chapterCounts[i] > 0) newLevel[i] = level++;
		}
		for (ChapterInfo chapterInfo : chapterInfos) {
			chapterInfo.chapterLevel = newLevel[chapterInfo.chapterLevel];
		}
		
		//開始終了情報を追加 nav用
		ChapterInfo preChapterInfo = new ChapterInfo(null, null, null, 0); //レベル0
		for (ChapterInfo chapterInfo : chapterInfos) {
			if (preChapterInfo != null) {
				//開始
				chapterInfo.levelStart = Math.max(0, chapterInfo.chapterLevel - preChapterInfo.chapterLevel);
				//終了
				preChapterInfo.levelEnd = Math.max(0, preChapterInfo.chapterLevel - chapterInfo.chapterLevel);
			}
			preChapterInfo = chapterInfo;
		}
		//一番最後は閉じる
		if (chapterInfos.size() > 0) {
			ChapterInfo chapterInfo = chapterInfos.lastElement();
			if (chapterInfo != null) chapterInfo.levelEnd = chapterInfo.chapterLevel;
		}
		
		int ncxDepth = 1;
		if (this.ncxNest) {
			int minLevel = 99; int maxLevel = 0;
			//navPointを閉じる回数をlevelEndに設定
			int[] navPointLevel = new int[10]; //navPointを開始したレベルidxに1を設定
			preChapterInfo = null;
			for (ChapterInfo chapterInfo : chapterInfos) {
				if (preChapterInfo != null) {
					int preLevel = preChapterInfo.chapterLevel;
					int curLevel = chapterInfo.chapterLevel;
					minLevel = Math.min(minLevel, curLevel);
					maxLevel = Math.max(maxLevel, curLevel);
					navPointLevel[preLevel] = 1;
					if (preLevel < curLevel) {
						//前より小さい場合
						preChapterInfo.navClose = 0;
					} else if (preLevel > curLevel) {
						//前より大きい
						int close = 0;
						for (int i=curLevel; i<navPointLevel.length; i++) {
							if (navPointLevel[i] == 1) {
								close++;
								navPointLevel[i] = 0;
							}
						}
						preChapterInfo.navClose = close;
					} else {
						preChapterInfo.navClose = 1;
						navPointLevel[preLevel] = 0;
					}
				}
				preChapterInfo = chapterInfo;
			}
			if (minLevel<maxLevel) ncxDepth = maxLevel-minLevel+1;
			
			//一番最後は閉じる
			if (chapterInfos.size() > 0) {
				ChapterInfo chapterInfo = chapterInfos.lastElement();
				if (chapterInfo != null) {
					int close = 1;
					for (int i=0; i<navPointLevel.length; i++) {
						if (navPointLevel[i] == 1) {
							close++;
						}
					}
					chapterInfo.navClose = close;
				}
			}
		}
		
		//velocityに設定 1～
		velocityContext.put("ncx_depth", ncxDepth);
		
		//出力前に縦中横とエスケープ処理
		if (!bookInfo.imageOnly) {
			converter.vertical = bookInfo.tocVertical;
			int spaceHyphenation = converter.getSpaceHyphenation();
			converter.setSpaceHyphenation(0);
			StringBuilder buf = new StringBuilder();
			for (ChapterInfo chapterInfo : chapterInfos) {
				buf.setLength(0);
				String converted = CharUtils.escapeHtml(chapterInfo.getChapterName());
				if (bookInfo.tocVertical) {
					converted = converter.convertTcyText(converted);
				}
				chapterInfo.setChapterName(converted);
			}
			//戻す
			converter.vertical = bookInfo.vertical;
			converter.setSpaceHyphenation(spaceHyphenation);
		}
		
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
		
		if (this.canceled) return;
		//プログレスバーにテキスト進捗分を追加
		if (this.jProgressBar != null && !bookInfo.imageOnly) this.jProgressBar.setValue(bookInfo.totalLineNum/10);
		
		//フォントファイル格納
		if (!bookInfo.imageOnly) {
			File fontsPath = new File(templatePath+OPS_PATH+FONTS_PATH);
			if (fontsPath.exists()) {
				for (File fontFile : fontsPath.listFiles()) {
					String outFileName = OPS_PATH+FONTS_PATH+fontFile.getName();
					zos.putArchiveEntry(new ZipArchiveEntry(outFileName));
					fis = new FileInputStream(new File(templatePath+outFileName));
					IOUtils.copy(fis, zos);
					fis.close();
					zos.closeArchiveEntry();
				}
			}
		}
		
		//外字ファイル格納
		for (GaijiInfo gaijiInfo : this.vecGaijiInfo) {
			File gaijiFile = gaijiInfo.getFile();
			if (gaijiFile.exists()) {
				String outFileName = OPS_PATH+GAIJI_PATH+gaijiFile.getName();
				zos.putArchiveEntry(new ZipArchiveEntry(outFileName));
				fis = new FileInputStream(gaijiFile);
				IOUtils.copy(fis, zos);
				fis.close();
				zos.closeArchiveEntry();
			}
		}
		
		zos.setLevel(0);
		////////////////////////////////////////////////////////////////////////////////////////////////
		//表紙指定があればそれを入力に設定 先頭画像のisCoverはfalseになっている
		//プレビューで編集された場合はここで追加する
		////////////////////////////////
		//表紙編集時のイメージ出力
		if (coverImageInfo != null) {
			try {
				//kindleの場合は常にjpegに変換
				if (isKindle) {
					String imgExt = coverImageInfo.getExt();
					if (!imgExt.startsWith("jp")) {
						if (bookInfo.coverImage == null) {
							ByteArrayInputStream bais = new ByteArrayInputStream(coverImageBytes);
							bookInfo.coverImage = ImageUtils.readImage(imgExt, bais);
							bais.close();
						}
						coverImageInfo.setExt("jpeg");
					}
				}
				if (bookInfo.coverImage != null) {
					//プレビューで編集されている場合
					zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+coverImageInfo.getOutFileName()));
					this.writeCoverImage(bookInfo.coverImage, zos, coverImageInfo);
					zos.closeArchiveEntry();
					bookInfo.coverImage = null; //同じ画像が使われている場合は以後はファイルから読み込ませる
				} else {
					ByteArrayInputStream bais = new ByteArrayInputStream(coverImageBytes);
					zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+coverImageInfo.getOutFileName()));
					this.writeCoverImage(bais, zos, coverImageInfo);
					zos.closeArchiveEntry();
					bais.close();
				}
				imageInfos.remove(0);//カバー画像は出力済みなので削除
				if (this.jProgressBar != null) this.jProgressBar.setValue(this.jProgressBar.getValue()+10);
			} catch (Exception e) {
				e.printStackTrace();
				LogAppender.error("表紙画像取得エラー: "+bookInfo.coverFileName);
			}
		}
		if (this.canceled) return;
		
		//本文画像出力 (画像のみの場合は出力済)
		if ("txt".equals(srcExt)) {
			////////////////////////////////
			//txtの場合はファイルシステムから取得
			for (String srcImageFileName : imageInfoReader.getImageFileNames()) {
				srcImageFileName = imageInfoReader.correctExt(srcImageFileName); //拡張子修正
				if (outImageFileNames.contains(srcImageFileName)) {
					ImageInfo imageInfo = imageInfoReader.getImageInfo(srcImageFileName);
					if (imageInfo == null) {
						LogAppender.println("[WARN] 画像ファイルなし: "+srcImageFileName);
					} else {
						File imageFile = imageInfoReader.getImageFile(srcImageFileName);
						if (imageFile.exists()) {
							fis = new FileInputStream(imageFile);
							zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+imageInfo.getOutFileName()));
							this.writeImage(new BufferedInputStream(fis, 8192), zos, imageInfo);
							zos.closeArchiveEntry();
							fis.close();
							outImageFileNames.remove(srcImageFileName);
						}
					}
				}
				if (this.canceled) return;
				if (this.jProgressBar != null) this.jProgressBar.setValue(this.jProgressBar.getValue()+10);
			}
		} else if (!bookInfo.imageOnly) {
			if ("rar".equals(srcExt)) {
				////////////////////////////////
				//Rar
				Archive archive = new Archive(srcFile);
				try {
				for (FileHeader fileHeader : archive.getFileHeaders()) {
					if (!fileHeader.isDirectory()) {
						String entryName = fileHeader.getFileNameW();
						if (entryName.length() == 0) entryName = fileHeader.getFileNameString();
						entryName = entryName.replace('\\', '/');
						//アーカイブ内のサブフォルダは除外してテキストからのパスにする
						String srcImageFileName = entryName.substring(archivePathLength);
						if (outImageFileNames.contains(srcImageFileName)) {
							InputStream is = archive.getInputStream(fileHeader);
							try {
								this.writeArchiveImage(srcImageFileName, is);
							} finally {
								is.close();
							}
						}
					}
				}
				} finally { archive.close(); }
			} else {
				////////////////////////////////
				//Zip
				ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
				try {
				ArchiveEntry entry;
				while( (entry = zis.getNextZipEntry()) != null ) {
					//アーカイブ内のサブフォルダは除外してテキストからのパスにする
					String srcImageFileName = entry.getName().substring(archivePathLength);
					if (outImageFileNames.contains(srcImageFileName)) {
						this.writeArchiveImage(srcImageFileName, zis);
					}
				}
				} finally { zis.close(); }
			}
		}
		
		//エラーがなければ100%
		if (this.jProgressBar != null) this.jProgressBar.setValue(this.jProgressBar.getMaximum());
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			//ePub3出力ファイルを閉じる
			if (zos != null) zos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			//メンバ変数解放
			this.velocityContext = null;
			this.bookInfo = null;
			this.imageInfoReader = null;
		}
	}
	
	/** アーカイブ内の画像を出力 */
	void writeArchiveImage(String srcImageFileName, InputStream is) throws IOException
	{
		srcImageFileName = imageInfoReader.correctExt(srcImageFileName); //拡張子修正
		ImageInfo imageInfo = imageInfoReader.getImageInfo(srcImageFileName);
		//Zip内テキストの場合はidと出力ファイル名が登録されていなければ出力しない。
		if (imageInfo != null) {
			if (imageInfo.getId() != null) {
				//回転チェック
				if ((double)imageInfo.getWidth()/imageInfo.getHeight() >= (double)this.dispW/this.dispH) {
					if (this.rotateAngle != 0 && this.dispW < this.dispH && (double)imageInfo.getHeight()/imageInfo.getWidth() < (double)this.dispW/this.dispH) { //縦長画面で横長
						imageInfo.rotateAngle = this.rotateAngle;
					}
				} else {
					if (this.rotateAngle != 0 && this.dispW > this.dispH && (double)imageInfo.getHeight()/imageInfo.getWidth() > (double)this.dispW/this.dispH) { //横長画面で縦長
						imageInfo.rotateAngle = this.rotateAngle;
					}
				}
				zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+imageInfo.getOutFileName()));
				//Zip,Rarからの直接読み込みは失敗するので一旦バイト配列にする
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(new BufferedInputStream(is, 16384), baos);
				byte[] bytes = baos.toByteArray();
				baos.close();
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				this.writeImage(bais, zos, imageInfo);
				bais.close();
				zos.closeArchiveEntry();
			}
			if (this.canceled) return;
			if (this.jProgressBar != null) this.jProgressBar.setValue(this.jProgressBar.getValue()+10);
		}
	}
	
	/** 表紙画像を出力 編集済の画像なのでリサイズしない */
	void writeCoverImage(BufferedImage srcImage, ZipArchiveOutputStream zos, ImageInfo imageInfo) throws IOException
	{
		imageInfo.rotateAngle = 0; //回転させない
		ImageUtils.writeImage(null, srcImage, zos,imageInfo, this.jpegQuality, this.gammaOp,
				0, 0, 0, this.dispW, this.dispH,
				0, 0, 0, 0, 0, 0);
	}
	/** 表紙画像を出力 */
	void writeCoverImage(InputStream is, ZipArchiveOutputStream zos, ImageInfo imageInfo) throws IOException
	{
		imageInfo.rotateAngle = 0; //回転させない
		ImageUtils.writeImage(is, null, zos,imageInfo, this.jpegQuality, this.gammaOp,
				0, this.coverW, this.coverH, this.dispW, this.dispH,
				0, 0, 0, 0, 0, 0);
	}
	/** 画像を出力 */
	void writeImage(InputStream is,ZipArchiveOutputStream zos, ImageInfo imageInfo) throws IOException
	{
		ImageUtils.writeImage(is, null, zos, imageInfo, this.jpegQuality, this.gammaOp,
				this.maxImagePixels, this.maxImageW, this.maxImageH, this.dispW, this.dispH,
				this.autoMarginLimitH, this.autoMarginLimitV, this.autoMarginWhiteLevel, this.autoMarginPadding, this.autoMarginNombre, this.autoMarginNombreSize);
	}
	/** 画像を出力 */
	void writeImage(BufferedImage srcImage, ZipArchiveOutputStream zos, ImageInfo imageInfo) throws IOException
	{
		ImageUtils.writeImage(null, srcImage, zos, imageInfo, this.jpegQuality, this.gammaOp,
				this.maxImagePixels, this.maxImageW, this.maxImageH, this.dispW, this.dispH,
				this.autoMarginLimitH,  this.autoMarginLimitV, this.autoMarginWhiteLevel, this.autoMarginPadding, this.autoMarginNombre, this.autoMarginNombreSize);
	}
	
	/** 本文を出力する */
	void writeSections(AozoraEpub3Converter converter, BufferedReader src, BufferedWriter bw, File srcFile, String srcExt, ZipArchiveOutputStream zos) throws Exception
	{
		//this.startSection(0, bookInfo.startMiddle);
		
		//ePub3変換して出力
		//改ページ時にnextSection() を、画像出力時にgetImageFilePath() 呼び出し
		converter.vertical = bookInfo.vertical;
		converter.convertTextToEpub3(bw, src, bookInfo);
		bw.flush();
		
		this.endSection();
	}
	
	/** 次のチャプター用のZipArchiveEntryに切替え 
	 * チャプターのファイル名はcpaterFileNamesに追加される (0001)
	 * @throws IOException */
	public void nextSection(BufferedWriter bw, int lineNum, int pageType, int imagePageType, String srcImageFilePath) throws IOException
	{
		//タイトル置き換え時は出力しない
		if (this.sectionIndex >0) {
			bw.flush();
			this.endSection();
		}
		this.startSection(lineNum, pageType, imagePageType, srcImageFilePath);
	}
	/** セクション開始. 
	 * @throws IOException */
	void startSection(int lineNum, int pageType, int imagePageType, String srcImageFilePath) throws IOException
	{
		this.sectionIndex++;
		String sectionId = decimalFormat.format(this.sectionIndex);
		//package.opf用にファイル名
		SectionInfo sectionInfo = new SectionInfo(sectionId);
		//次の行が単一画像なら画像専用指定
		switch (imagePageType) {
		case PageBreakType.IMAGE_PAGE_W:
			//幅100％指定
			sectionInfo.setImagePage(true);
			sectionInfo.setImageFitW(true);
			break;
		case PageBreakType.IMAGE_PAGE_H:
			//高さ100%指定
			sectionInfo.setImagePage(true);
			sectionInfo.setImageFitH(true);
			break;
		case PageBreakType.IMAGE_PAGE_NOFIT:
			sectionInfo.setImagePage(true);
			break;
		}
		if (pageType == PageBreakType.PAGE_MIDDLE) sectionInfo.setMiddle(true);
		else if (pageType == PageBreakType.PAGE_BOTTOM) sectionInfo.setBottom(true);
		this.sectionInfos.add(sectionInfo);
		//セクション開始は名称がnullなので改ページ処理で文字列が設定されなければ出力されない 階層レベルは1
		//this.addChapter(null, null, 1);
		
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
	public void addChapter(String chapterId, String name, int chapterLevel)
	{
		SectionInfo sectionInfo = this.sectionInfos.lastElement();
		this.chapterInfos.add(new ChapterInfo(sectionInfo.sectionId, chapterId, name, chapterLevel));
	}
	/** 追加済の章の名称を変更 */
	/*public void updateChapterName(String name)
	{
		this.chapterInfos.lastElement().setChapterName(name);
	}
	/** 最後の章の情報を返却 */
	/*public ChapterInfo getLastChapterInfo()
	{
		if (this.chapterInfos.size() == 0) return null;
		return this.chapterInfos.lastElement();
	}*/
	
	/** 外字用フォントを追加 */
	public void addGaijiFont(String className, File gaijiFile)
	{
		if (this.gaijiNameSet.contains(className)) return;
		this.vecGaijiInfo.add(new GaijiInfo(className, gaijiFile));
		this.gaijiNameSet.add(className);
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
			String altImageFileName = this.imageInfoReader.correctExt(srcImageFileName);
			imageInfo = this.imageInfoReader.getImageInfo(altImageFileName);
			if (imageInfo != null) {
				LogAppender.warn(lineNum, "画像拡張子変更", srcImageFileName);
				srcImageFileName = altImageFileName;
			}
		}
		this.imageIndex++; //0001から開始 (本文内の順番に合せるため、ファイルが無くてもカウント)
		if (imageInfo != null) {
			String imageId = imageInfo.getId();
			//画像は未だ出力されていない
			if (imageId == null) {
				imageId = decimalFormat.format(this.imageIndex);
				this.imageInfos.add(imageInfo);
				this.outImageFileNames.add(srcImageFileName);
				if (this.imageIndex-1 == this.bookInfo.coverImageIndex) {
					//imageInfo.setIsCover(true);
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
			LogAppender.warn(lineNum, "画像ファイルなし", srcImageFileName);
		}
		return null;
	}
	
	public boolean isCoverImage()
	{
		return (this.imageIndex == this.bookInfo.coverImageIndex); 
	}
	
	/** 現在の出力済画像枚数を返す 0なら未出力 */
	public int getImageIndex()
	{
		return this.imageIndex;
	}
	
	/** 画像が単一ページ画像にできるかチェック
	 * @param srcFilePath テキスト内の画像相対パス文字列
	 * @throws IOException */
	public int getImagePageType(String srcFilePath, int tagLevel, int lineNum, boolean hasCaption)
	{
		try {
			ImageInfo imageInfo = this.imageInfoReader.getImageInfo(srcFilePath);
			//拡張子修正
			if (imageInfo == null) imageInfo = this.imageInfoReader.getImageInfo(this.imageInfoReader.correctExt(srcFilePath));
			
			if (imageInfo == null) return PageBreakType.IMAGE_PAGE_NONE;
			
			float imageOrgWidth = imageInfo.getWidth();
			float imageOrgHeight = imageInfo.getHeight();
			float imageWidth = imageOrgWidth;
			float imageHeight = imageOrgHeight;
			if (this.imageScale > 0) {
				imageWidth *= imageScale;
				imageHeight *= imageScale;
			}
			
			//回り込みサイズ以下
			if (this.imageFloatType != 0 &&
				(imageOrgWidth >= 64 || imageOrgHeight >= 64) &&
				imageOrgWidth <= this.imageFloatW && imageOrgHeight <= this.imageFloatH) {
				if (this.imageFloatType==1) {
					if (imageWidth > dispW) return PageBreakType.IMAGE_INLINE_TOP_W;
					return PageBreakType.IMAGE_INLINE_TOP;
				} else {
					if (imageWidth > dispW) return PageBreakType.IMAGE_INLINE_BOTTOM_W;
					return PageBreakType.IMAGE_INLINE_BOTTOM;
				}
			}
			//指定サイズ以下なら単ページ化 (タグ外かつキャプションが無い場合のみ)
			if (imageOrgWidth >= this.singlePageWidth || imageOrgWidth >= singlePageSizeW && imageOrgHeight >= singlePageSizeH) {
				if (tagLevel == 0) {
					if (!hasCaption) {
						if (imageWidth <= this.dispW && imageHeight < this.dispH) {
							//画面より小さい場合
							if (!this.fitImage) return PageBreakType.IMAGE_PAGE_NOFIT;
						} else {
							//画面より大きく、サイズ指定無し
							if (this.imageSizeType == SectionInfo.IMAGE_SIZE_TYPE_AUTO) return PageBreakType.IMAGE_PAGE_NOFIT;
						}
						//拡大または縮小指定
						//画面より横長
						if (imageWidth/imageHeight > (double)this.dispW/this.dispH) {
							if (this.rotateAngle != 0 && this.dispW < this.dispH && imageWidth > imageHeight*1.1) { //縦長画面で110%以上横長
								imageInfo.rotateAngle = this.rotateAngle;
								if (imageHeight/imageWidth > (double)dispW/dispH) return PageBreakType.IMAGE_PAGE_W; //回転後画面より横長
								return PageBreakType.IMAGE_PAGE_H;
							} else {
								return PageBreakType.IMAGE_PAGE_W;
							}
						}
						//画面より縦長
						else {
							if (this.rotateAngle != 0 && this.dispW > this.dispH && imageWidth*1.1 < imageHeight) { //横長画面で110%以上縦長
								imageInfo.rotateAngle = this.rotateAngle;
								if (imageHeight/imageWidth > (double)dispW/dispH) return PageBreakType.IMAGE_PAGE_W; //回転後画面より横長
								return PageBreakType.IMAGE_PAGE_H;
							} else {
								return PageBreakType.IMAGE_PAGE_H;
							}
						}
					} else {
						LogAppender.warn(lineNum, "キャプションがあるため画像単ページ化されません");
					}
				} else {
					LogAppender.warn(lineNum, "タグ内のため画像単ページ化できません");
				}
			}
			
			//単ページ化も回り込みもない
			if (imageWidth > dispW) { //横がはみ出している
				if (imageWidth/imageHeight > (double)dispW/dispH) return PageBreakType.IMAGE_INLINE_W;
				else return PageBreakType.IMAGE_INLINE_H; //縦の方が長い
			}
			if (imageHeight > dispH) return PageBreakType.IMAGE_INLINE_H; //縦がはみ出している
			
			
		} catch (Exception e) { e.printStackTrace(); }
		return PageBreakType.IMAGE_PAGE_NONE;
	}
	
	/** 画像の画面内の比率を取得 表示倍率指定反映後
	 * @return 画面幅にタイする表示比率% 倍率1の場合は0 小さい画像は-1を返す */
	public double getImageWidthRatio(String srcFilePath, boolean hasCaption)
	{
		//0なら無効
		if (this.imageScale == 0) return 0;
		
		double ratio = 0;
		try {
			ImageInfo imageInfo = this.imageInfoReader.getImageInfo(srcFilePath);
			if (imageInfo != null) {
				//外字や数式は除外 行方向に64px以下
				if (this.bookInfo.vertical) {
					if (imageInfo.getWidth() <= 64) return -1;
				} else if (imageInfo.getHeight() <= 64) return -1;
				
				//回転時は縦横入れ替え
				int imgW = imageInfo.getWidth();
				int imgH = imageInfo.getHeight();
				if (imageInfo.rotateAngle == 90 || imageInfo.rotateAngle == 270) {
					imgW = imageInfo.getHeight();
					imgH = imageInfo.getWidth();
				}
				double wRatio = (double)imgW/this.dispW*this.imageScale*100;
				double hRatio = (double)imgH/this.dispH*this.imageScale*100;
				//縦がはみ出ている場合は調整
				if (hasCaption) {
					//キャプションがある場合は高さを90%にする
					if (hRatio >= 90) {
						wRatio *= 100/hRatio;
						wRatio *= 0.9;
					}
				} else if (hRatio >= 100) {
					wRatio *= 100/hRatio;
					
				}
				ratio = wRatio;
			}
		} catch (Exception e) { e.printStackTrace(); }
		return Math.min(100, ratio);
	}
	
	/** Kindleかどうかを設定 Kindleなら例外処理を行う */
	public void setIsKindle(boolean isKindle)
	{
		this.isKindle = isKindle;
		
	}
	
	/** 外字フォントファイルが格納されているテンプレートパスを取得 */
	public String getGaijiFontPath()
	{
		return GAIJI_PATH;
	}
	
}
