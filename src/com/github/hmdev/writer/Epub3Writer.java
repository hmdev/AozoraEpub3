package com.github.hmdev.writer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.ChapterInfo;
import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.info.SectionInfo;
import com.github.hmdev.util.LogAppender;

/** ePub3用のファイル一式をZipで固めたファイルを生成.
 * 本文は改ページでセクション毎に分割されて xhtml/以下に 0001.xhtml 0002.xhtml の連番ファイル名で格納
 * 画像は images/以下に 0001.jpg 0002.png のようにリネームして格納
 */
public class Epub3Writer
{
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
	final static String[] TEMPLATE_FILE_NAMES = new String[]{
		"mimetype",
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"vertical_text.css",
		OPS_PATH+CSS_PATH+"vertical_middle.css",
		OPS_PATH+CSS_PATH+"vertical_image.css",
		OPS_PATH+CSS_PATH+"vertical.css",
		OPS_PATH+CSS_PATH+"horizontal_text.css",
		OPS_PATH+CSS_PATH+"horizontal_middle.css",
		OPS_PATH+CSS_PATH+"horizontal_image.css",
		OPS_PATH+CSS_PATH+"horizontal.css"
	};
	String[] getTemplateFiles()
	{
		return TEMPLATE_FILE_NAMES;
	}
	
	/** 出力先ePubのZipストリーム */
	ZipArchiveOutputStream zos;
	
	/** ファイル名桁揃え用 */
	public static DecimalFormat decimalFormat = new DecimalFormat("0000");
	/** 更新日時フォーマット 2011-06-29T12:00:00Z */
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
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
	/** 画像リネーム情報格納用 重複チェック用にHashに格納 */
	HashMap<String, String> imageFileNames;
	
	/** Velocity変数格納コンテキスト */
	VelocityContext velocityContext;
	
	/** テンプレートパス */
	String templatePath;
	
	/** 出力中の書籍情報 */
	BookInfo bookInfo;
	
	boolean imageMode = false;
	
	HashSet<String> zipImageFileNames;
	
	
	String srcFilePath;
	String zipTextFileName;
	
	/** コンストラクタ
	 * @param templatePath epubテンプレート格納パス文字列 最後は"/"
	 */
	public Epub3Writer(String templatePath)
	{
		this.templatePath = templatePath;
	}
	
	/** epubファイルを出力
	 * @param converter 青空文庫テキスト変換クラス
	 * @param src 青空文庫テキストファイルの入力Stream
	 * @param srcFile 青空文庫テキストファイル zip時の画像取得用
	 * @param epubFile 出力ファイル .epub拡張子
	 * @param bookInfo 書籍情報と縦横書き指定
	 * @throws IOException */
	public void write(AozoraEpub3Converter converter, BufferedReader src, File srcFile, String zipTextFileName, File epubFile, BookInfo bookInfo, HashSet<String> zipImageFileNames) throws IOException
	{
		this.bookInfo = bookInfo;
		this.srcFilePath = srcFile.getParent()+"/";
		this.zipTextFileName = zipTextFileName;
		this.zipImageFileNames = zipImageFileNames;
		
		//インデックス初期化
		this.sectionIndex = 0;
		this.imageIndex = 0;
		this.sectionInfos = new Vector<SectionInfo>();
		this.chapterInfos = new Vector<ChapterInfo>();
		this.imageInfos = new Vector<ImageInfo>();
		this.imageFileNames = new HashMap<String, String>();
		
		//初回実行時のみ有効
		Velocity.init();
		//Velocity用 共通コンテキスト設定
		this.velocityContext = new VelocityContext();
		//IDはタイトル著作者のハッシュで適当に生成
		String title = bookInfo.title==null?"":bookInfo.title;
		String creator = bookInfo.creator==null?"":bookInfo.creator;
		//固有ID
		velocityContext.put("identifier", UUID.nameUUIDFromBytes((title+"-"+creator).getBytes()));
		//目次名称
		velocityContext.put("toc_name", "目次");
		//表紙
		velocityContext.put("cover_name", "表紙");
		//書籍情報 タイトル、著作者、縦書き
		velocityContext.put("bookInfo", bookInfo);
		velocityContext.put("modified", dateFormat.format(bookInfo.modified));
		
		//出力先ePubのZipストリーム生成
		zos = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(epubFile)));
		//mimetypeは非圧縮
		zos.setLevel(0);
		//テンプレートのファイルを格納
		for (String fileName : getTemplateFiles()) {
			zos.putArchiveEntry(new ZipArchiveEntry(fileName));
			FileInputStream fis = new FileInputStream(new File(templatePath+fileName));
			IOUtils.copy(fis, zos);
			fis.close();
			zos.closeArchiveEntry();
			zos.setLevel(9);
		}
		
		//zip出力用Writer
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		//本文を出力
		this.writeSections(converter, src, bw);
		
		//入力ファイル名と同じpng/jpgがあればそのファイルを表紙に指定
		if ("*".equals(bookInfo.coverFileName)) {
			bookInfo.coverFileName = null; //マッチしなかったら表紙無し
			String baseFileName = srcFile.getPath();
			baseFileName = baseFileName.substring(0, baseFileName.lastIndexOf('.'));
			if (new File (baseFileName+".png").exists()) {
				bookInfo.coverFileName = baseFileName+".png";
			} else if (new File (baseFileName+".jpg").exists()) {
				bookInfo.coverFileName = baseFileName+".jpg";
			} else if (new File (baseFileName+".jpeg").exists()) {
				bookInfo.coverFileName = baseFileName+".jpeg";
			} else if (new File (baseFileName+".PNG").exists()) {
				bookInfo.coverFileName = baseFileName+".PNG";
			} else if (new File (baseFileName+".JPG").exists()) {
				bookInfo.coverFileName = baseFileName+".JPG";
			} else if (new File (baseFileName+".JPEG").exists()) {
				bookInfo.coverFileName = baseFileName+".JPEG";
			}
		}
		ImageInfo coverImageInfo = null;
		if (bookInfo.coverFileName == null) {
			//表紙無し
			//表紙設定解除
			for(ImageInfo imageInfo2 : imageInfos) {
				imageInfo2.setIsCover(false);
			}
		}
		else if (bookInfo.coverFileName.length() > 0) {
			//表紙情報をimageInfosに追加
			try {
				//表紙設定解除
				for(ImageInfo imageInfo2 : imageInfos) {
					imageInfo2.setIsCover(false);
				}
				String ext = "";
				try { ext = bookInfo.coverFileName.substring(bookInfo.coverFileName.lastIndexOf('.')+1); } catch (Exception e) {}
				String imageId = "0000";
				String format = "image/"+ext.toLowerCase();
				if ("image/jpg".equals(format)) format = "image/jpeg";
				if (!format.matches("^image\\/(png|jpeg|gif)$")) LogAppender.append("表紙画像フォーマットエラー: "+bookInfo.coverFileName+"\n");
				else {
					coverImageInfo = new ImageInfo(imageId, imageId+"."+ext, format);
					coverImageInfo.setIsCover(true);
					this.imageInfos.add(0, coverImageInfo);
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
		
		//表紙ページ出力 先頭画像表示時は先頭画像は出力しない
		if (bookInfo.insertCoverPage) {
			ImageInfo coverPageImage = coverImageInfo;
			if (coverPageImage == null) {
				for (ImageInfo imageInfo2 : imageInfos) {
					if (imageInfo2.getIsCover()) {
						coverPageImage = imageInfo2;
						break;
					}
				}
			}
			if (coverPageImage != null) {
				velocityContext.put("coverImage", coverPageImage);
				zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+COVER_FILE));
				bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
				Velocity.getTemplate(templatePath+OPS_PATH+XHTML_PATH+COVER_VM).merge(velocityContext, bw);
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
		Velocity.getTemplate(templatePath+OPS_PATH+PACKAGE_VM).merge(velocityContext, bw);
		bw.flush();
		zos.closeArchiveEntry();
		
		//navファイル
		velocityContext.put("chapters", chapterInfos);
		zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+XHTML_NAV_FILE));
		bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.getTemplate(templatePath+OPS_PATH+XHTML_PATH+XHTML_NAV_VM).merge(velocityContext, bw);
		bw.flush();
		zos.closeArchiveEntry();
		
		//tocファイル
		velocityContext.put("chapters", chapterInfos);
		zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+TOC_FILE));
		bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.getTemplate(templatePath+OPS_PATH+TOC_VM).merge(velocityContext, bw);
		bw.flush();
		zos.closeArchiveEntry();
		
		if (src != null) src.close();
		
		zos.setLevel(0);
		//画像ファイルコピー (連番にリネーム)
		//表紙指定があればそれを入力に設定 先頭画像のisCoverはfalseになっている
		if (coverImageInfo != null) {
			try {
				BufferedInputStream bis;
				if (bookInfo.coverFileName.startsWith("http")) {
					bis = new BufferedInputStream(new URL(bookInfo.coverFileName).openStream());
				} else {
					bis = new BufferedInputStream(new FileInputStream(new File(bookInfo.coverFileName)));
				}
				zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+IMAGES_PATH+imageInfos.get(0).getFile()));
				IOUtils.copy(bis, zos);
				zos.closeArchiveEntry();
				bis.close();
				imageInfos.remove(0);//カバー画像情報削除
			} catch (Exception e) {
				e.printStackTrace();
				LogAppender.append("[ERROR] 表紙画像取得エラー: "+bookInfo.coverFileName+"\n");
			}
		}
		if (srcFile.getName().toLowerCase().endsWith(".zip")) {
			int zipPathLength = 0;
			if (zipTextFileName != null) zipPathLength = zipTextFileName.indexOf('/')+1;
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile)), "Shift_JIS", false);
			ArchiveEntry entry;
			while( (entry = zis.getNextEntry()) != null ) {
				String entryName = entry.getName().substring(zipPathLength);
				String dstImageFileName = imageFileNames.get(entryName);
				if (dstImageFileName != null) {
					zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+dstImageFileName));
					IOUtils.copy(zis, zos);
					zos.closeArchiveEntry();
					//チェック用に出力したら削除
					imageFileNames.remove(entryName);
				}
			}
			zis.close();
			//出力されなかった画像をログ出力
			for(Map.Entry<String, String> e : imageFileNames.entrySet()) {
				LogAppender.append("[WARN] 画像ファイルなし: "+e.getKey()+"\n");
			}
		} else {
			//TODO 出力順をimageFiles順にする?
			for(Map.Entry<String, String> e : imageFileNames.entrySet()) {
				String srImagecFileName = e.getKey();
				String dstImageFileName = e.getValue();
				File imageFile = new File(srcFilePath+srImagecFileName);
				if (imageFile.exists()) {
					zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+dstImageFileName));
					FileInputStream fis = new FileInputStream(imageFile);
					IOUtils.copy(fis, zos);
					zos.closeArchiveEntry();
					fis.close();
				} else {
					LogAppender.append("[WARN] 画像ファイルなし: "+srImagecFileName+"\n");
				}
			}
		}
		
		//ePub3出力ファイルを閉じる
		zos.close();
	}
	
	/** 本文を出力する */
	void writeSections(AozoraEpub3Converter converter, BufferedReader src, BufferedWriter bw) throws IOException
	{
		//0001CapterのZipArchiveEntryを設定
		this.startSection(0, bookInfo.startMiddle);
		
		//ePub3変換して出力
		//改ページ時にnextSection() を、画像出力時にgetImageFilePath() 呼び出し
		converter.convertTextToEpub3(bw, src, bookInfo);
		bw.flush();
		
		this.endSection();
	}
	
	/** 次のチャプター用のZipArchiveEntryに切替え 
	 * チャプターのファイル名はcpaterFileNamesに追加される (0001)
	 * @throws IOException */
	public void nextSection(BufferedWriter bw, int lineNum, boolean isMiddle) throws IOException
	{
		bw.flush();
		this.endSection();
		this.startSection(lineNum, isMiddle);
	}
	/** セクション開始. 
	 * @throws IOException */
	void startSection(int lineNum, boolean isMiddle) throws IOException
	{
		this.sectionIndex++;
		String sectionId = decimalFormat.format(this.sectionIndex);
		//package.opf用にファイル名
		SectionInfo sectionInfo = new SectionInfo(sectionId);
		//次の行が単一画像なら画像専用指定
		if (this.bookInfo.isImageSectionLine(lineNum+1)) sectionInfo.setImageFit(true);
		else if (isMiddle) sectionInfo.setMiddle(true);
		this.sectionInfos.add(sectionInfo);
		this.addChapter(sectionId, null); //章の名称はsectionIdを仮に設定
		
		this.zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+sectionId+".xhtml"));
		
		//ヘッダ出力
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		//出力開始するセクションに対応したSectionInfoを設定
		this.velocityContext.put("sectionInfo", sectionInfo);
		Velocity.getTemplate(this.templatePath+OPS_PATH+XHTML_PATH+XHTML_HEADER_VM).merge(this.velocityContext, bw);
		bw.flush();
	}
	/** セクション終了. 
	 * @throws IOException */
	void endSection() throws IOException
	{
		//フッタ出力
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));
		Velocity.getTemplate(this.templatePath+OPS_PATH+XHTML_PATH+XHTML_FOOTER_VM).merge(this.velocityContext, bw);
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
	 *  */
	public String getImageFilePath(String srcImageFileName, int lineNum)
	{
		boolean isCover = false;
		
		//すでに出力済みの画像
		String imageFileName = this.imageFileNames.get(srcImageFileName);
		
		if (imageFileName == null) {
			//画像があるかチェック
			boolean exists = false;
			if (this.zipImageFileNames == null) {
				File imageFile = new File(srcFilePath+srcImageFileName);
				exists = imageFile.exists();
				//拡張子変更
				if (!exists) {
					if (new File(srcFilePath+srcImageFileName.replaceFirst("\\.\\w+$", ".png")).exists()) {
						exists = true;
						srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".png");
					} else if (new File(srcFilePath+srcImageFileName.replaceFirst("\\.\\w+$", ".jpg")).exists()) {
						exists = true;
						srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpg");
					} else if (new File(srcFilePath+srcImageFileName.replaceFirst("\\.\\w+$", ".jpeg")).exists()) {
						exists = true;
						srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpeg");
					}
					if (exists) LogAppender.append("画像拡張子変更: ("+lineNum+") "+srcImageFileName+"\n");
				}
			} else {
				//Zipの場合
				String zipTextParent = zipTextFileName.substring(0, zipTextFileName.indexOf('/')+1);
				exists = this.zipImageFileNames.contains(zipTextParent+srcImageFileName);
				//拡張子変更
				if (!exists) {
					if (this.zipImageFileNames.contains(zipTextParent+srcImageFileName.replaceFirst("\\.\\w+$", ".png"))) {
						exists = true;
						srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".png");
					} else if (this.zipImageFileNames.contains(zipTextParent+srcImageFileName.replaceFirst("\\.\\w+$", ".jpg"))) {
						exists = true;
						srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpg");
					} else if (this.zipImageFileNames.contains(zipTextParent+srcImageFileName.replaceFirst("\\.\\w+$", ".jpeg"))) {
						exists = true;
						srcImageFileName = srcImageFileName.replaceFirst("\\.\\w+$", ".jpeg");
					}
					if (exists) LogAppender.append("画像拡張子変更: ("+lineNum+") "+srcImageFileName+"\n");
				}
			}
			if (!exists) {
				//画像端ページの場合はセクションを出力しない
				if (bookInfo.isImageSectionLine(lineNum)) {
					this.sectionInfos.remove(this.sectionInfos.size()-1);
				}
				//画像タグを出力しない
				LogAppender.append("画像なし: ("+lineNum+") "+srcImageFileName+"\n");
				return null;
			}
			
			//拡張子変更があるので再度ファイル名チェック
			imageFileName = this.imageFileNames.get(srcImageFileName);
			if (imageFileName == null) {
				this.imageIndex++;
				String ext = "";
				try { ext = srcImageFileName.substring(srcImageFileName.lastIndexOf('.')+1); } catch (Exception e) {}
				String imageId = decimalFormat.format(this.imageIndex);
				imageFileName = IMAGES_PATH+imageId+"."+ext;
				this.imageFileNames.put(srcImageFileName, imageFileName);
				String format = "image/"+ext.toLowerCase();
				if ("image/jpg".equals(format)) format = "image/jpeg";
				if (!format.matches("^image\\/(png|jpeg|gif)$")) LogAppender.append("画像フォーマットエラー: ("+lineNum+") "+srcImageFileName+"\n");
				else {
					ImageInfo imageInfo = new ImageInfo(imageId, imageId+"."+ext, format);
					if (this.imageIndex == 1 &&  "".equals(bookInfo.coverFileName)) {
						imageInfo.setIsCover(true);
						isCover = true;
					}
					this.imageInfos.add(imageInfo);
				}
			}
		}
		//先頭に表紙ページ移動の場合でカバーページならnullを返して本文中から削除
		if (bookInfo.insertCoverPage && isCover) return null;
		return "../"+imageFileName;
	}
	
	/** 現在の出力済画像枚数を返す 0なら未出力 */
	public int getImageIndex()
	{
		return this.imageIndex;
	}
}
