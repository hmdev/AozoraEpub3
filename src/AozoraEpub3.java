import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.image.ImageInfoReader;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.writer.Epub3ImageWriter;
import com.github.hmdev.writer.Epub3Writer;

/** コマンドライン実行用mainとePub3変換関数 */
public class AozoraEpub3
{
	public static final String VERSION = "1.1.0b28";
	
	/** コマンドライン実行用 */
	public static void main(String args[])
	{
		String jarPath = System.getProperty("java.class.path");
		int idx = jarPath.indexOf(";");
		if (idx > 0) jarPath = jarPath.substring(0, idx);
		if (!jarPath.endsWith(".jar")) jarPath = "";
		else jarPath = jarPath.substring(0, jarPath.lastIndexOf(File.separator)+1);
		//this.cachePath = new File(jarPath+".cache");
		//this.webConfigPath = new File(jarPath+"web");
		
		/** ePub3出力クラス */
		Epub3Writer epub3Writer;
		/** ePub3画像出力クラス */
		Epub3ImageWriter epub3ImageWriter;
		
		/** 設定ファイル */
		Properties props;
		/** 設定ファイル名 */
		String propFileName = "AozoraEpub3.ini";
		/** 出力先パス */
		File dstPath = null;
		
		String helpMsg = "AozoraEpub3 [-options] input_files(txt,zip,cbz)\nversion : "+VERSION;
		
		try {
			//コマンドライン オプション設定
			Options options = new Options();
			options.addOption("h", "help", false, "show usage");
			options.addOption("i", "ini", true, "指定したiniファイルから設定を読み込みます (コマンドラインオプション以外の設定)");
			options.addOption("t", true, "本文内の表題種別\n[0:表題→著者名] (default)\n[1:著者名→表題]\n[2:表題→著者名(副題優先)]\n[3:表題のみ]\n[4:なし]");
			options.addOption("tf", false, "入力ファイル名を表題に利用");
			options.addOption("c", "cover", true, "表紙画像\n[0:先頭の挿絵]\n[1:ファイル名と同じ画像]\n[ファイル名 or URL]");
			options.addOption("ext", true, "出力ファイル拡張子\n[.epub] (default)\n[.kepub.epub]");
			options.addOption("of", false, "出力ファイル名を入力ファイル名に合せる");
			options.addOption("d", "dst", true, "出力先パス");
			options.addOption("enc", true, "入力ファイルエンコード\n[MS932] (default)\n[UTF-8]");
			//options.addOption("id", false, "栞用ID出力 (for Kobo)");
			//options.addOption("tcy", false, "自動縦中横有効");
			//options.addOption("g4", false, "4バイト文字変換");
			//options.addOption("tm", false, "表題を左右中央");
			//options.addOption("cp", false, "表紙画像ページ追加");
			options.addOption("hor", false, "横書き (指定がなければ縦書き)");
			
			CommandLine commandLine;
			try {
				commandLine = new BasicParser().parse(options, args, true);
			} catch (ParseException e) {
				new HelpFormatter().printHelp(helpMsg, options);
				return;
			}
			//オプションの後ろをファイル名に設定
			String[] fileNames = commandLine.getArgs();
			if (fileNames.length == 0) {
				new HelpFormatter().printHelp(helpMsg, options);
				return;
			}
			
			//ヘルプ出力
			if (commandLine.hasOption('h') ) {
				new HelpFormatter().printHelp(helpMsg, options);
				return;
			}
			//iniファイル確認
			if (commandLine.hasOption("i")) {
				propFileName = commandLine.getOptionValue("i");
				File file = new File(propFileName);
				if (file == null || !file.isFile()) {
					LogAppender.println("[ERROR] -i : ini file not exist. "+file.getAbsolutePath());
					return;
				}
			}
			//出力パス確認
			if (commandLine.hasOption("d")) {
				dstPath = new File(commandLine.getOptionValue("d"));
				if (dstPath == null || !dstPath.isDirectory()) {
					LogAppender.println("[ERROR] -d : dst path not exist. "+dstPath.getAbsolutePath());
					return;
				}
			}
			//ePub出力クラス初期化
			epub3Writer = new Epub3Writer(jarPath+"template/");
			epub3ImageWriter = new Epub3ImageWriter(jarPath+"template/");
			
			//propsから読み込み
			props = new Properties();
			try { props.load(new FileInputStream(propFileName)); } catch (Exception e) { }
			
			int titleIndex = 0; //try { titleIndex = Integer.parseInt(props.getProperty("TitleType")); } catch (Exception e) {}//表題
			
			//コマンドラインオプション以外
			boolean coverPage = "1".equals(props.getProperty("CoverPage"));//表紙追加
			int titlePage = BookInfo.TITLE_NONE;
			if ("1".equals(props.getProperty("TitlePageWrite"))) {
				try { titlePage =Integer.parseInt(props.getProperty("TitlePage")); } catch (Exception e) {}
			}
			boolean withMarkId = "1".equals(props.getProperty("MarkId"));
			boolean gaiji32 = "1".equals(props.getProperty("Gaiji32"));
			boolean commentPrint = "1".equals(props.getProperty("CommentPrint"));
			boolean commentConvert = "1".equals(props.getProperty("CommentConvert"));
			boolean autoYoko = "1".equals(props.getProperty("AutoYoko"));
			boolean autoYokoNum1 = "1".equals(props.getProperty("AutoYokoNum1"));
			boolean autoYokoNum3 = "1".equals(props.getProperty("AutoYokoNum3"));
			boolean autoYokoEQ1 = "1".equals(props.getProperty("AutoYokoEQ1"));
			int spaceHyp = 0; try { spaceHyp = Integer.parseInt(props.getProperty("SpaceHyphenation")); } catch (Exception e) {}
			boolean tocPage = "1".equals(props.getProperty("TocPage"));//目次追加
			boolean tocVertical = "1".equals(props.getProperty("TocVertical"));//目次縦書き
			boolean coverPageToc = "1".equals(props.getProperty("CoverPageToc"));
			int removeEmptyLine = 0; try { removeEmptyLine = Integer.parseInt(props.getProperty("RemoveEmptyLine")); } catch (Exception e) {}
			int maxEmptyLine = 0; try { maxEmptyLine = Integer.parseInt(props.getProperty("MaxEmptyLine")); } catch (Exception e) {}
			
			//画面サイズと画像リサイズ
			int dispW = 600; try { dispW =Integer.parseInt(props.getProperty("DispW")); } catch (Exception e) {}
			int dispH = 800; try { dispH =Integer.parseInt(props.getProperty("DispH")); } catch (Exception e) {}
			int coverW = 600; try { coverW = Integer.parseInt(props.getProperty("CoverW")); } catch (Exception e) {}
			int coverH = 800; try { coverH = Integer.parseInt(props.getProperty("CoverH")); } catch (Exception e) {}
			int resizeW = 0; if ("1".equals(props.getProperty("ResizeW"))) try { resizeW = Integer.parseInt(props.getProperty("ResizeNumW")); } catch (Exception e) {}
			int resizeH = 0; if ("1".equals(props.getProperty("ResizeH"))) try { resizeH = Integer.parseInt(props.getProperty("ResizeNumH")); } catch (Exception e) {}
			int singlePageSizeW = 480; try { singlePageSizeW = Integer.parseInt(props.getProperty("SinglePageSizeW")); } catch (Exception e) {}
			int singlePageSizeH = 640; try { singlePageSizeH = Integer.parseInt(props.getProperty("SinglePageSizeH")); } catch (Exception e) {}
			int singlePageWidth = 600; try { singlePageWidth = Integer.parseInt(props.getProperty("SinglePageWidth")); } catch (Exception e) {}
			int imageFloatType = 0; try { imageFloatType = Integer.parseInt(props.getProperty("ImageFloatType")); } catch (Exception e) {}
			int imageFloatW = 0; try { imageFloatW = Integer.parseInt(props.getProperty("ImageFloatW")); } catch (Exception e) {}
			int imageFloatH = 0; try { imageFloatH = Integer.parseInt(props.getProperty("ImageFloatH")); } catch (Exception e) {}
			boolean fitImage = "1".equals(props.getProperty("FitImage"));
			int rotateImage = 0; if ("1".equals(props.getProperty("RotateImage"))) rotateImage = 90; else if ("2".equals(props.getProperty("RotateImage"))) rotateImage = -90;
			float jpegQualty = 0.8f; try { jpegQualty = Integer.parseInt(props.getProperty("JpegQuality"))/100f; } catch (Exception e) {}
			float gamma = 1.0f; if ( "1".equals(props.getProperty("Gamma"))) try { gamma = Float.parseFloat(props.getProperty("GammaValue")); } catch (Exception e) {}
			int autoMarginLimitH = 0;
			int autoMarginLimitV = 0;
			int autoMarginWhiteLevel = 80;
			float autoMarginPadding = 0;
			int autoMarginNombre = 0;
			float nobreSize = 0.03f;
			if ("1".equals(props.getProperty("AutoMargin"))) {
				try { autoMarginLimitH = Integer.parseInt(props.getProperty("AutoMarginLimitH")); } catch (Exception e) {}
				try { autoMarginLimitV = Integer.parseInt(props.getProperty("AutoMarginLimitV")); } catch (Exception e) {}
				try { autoMarginWhiteLevel = Integer.parseInt(props.getProperty("AutoMarginWhiteLevel")); } catch (Exception e) {}
				try { autoMarginPadding = Float.parseFloat(props.getProperty("AutoMarginPadding")); } catch (Exception e) {}
				try { autoMarginNombre = Integer.parseInt(props.getProperty("AutoMarginNombre")); } catch (Exception e) {} 
				try { autoMarginPadding = Float.parseFloat(props.getProperty("AutoMarginNombreSize")); } catch (Exception e) {}
			 }
			epub3Writer.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth, fitImage, rotateImage,
					imageFloatType, imageFloatW, imageFloatH, jpegQualty, gamma, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre, nobreSize);
			epub3ImageWriter.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth, fitImage, rotateImage,
					imageFloatType, imageFloatW, imageFloatH, jpegQualty, gamma, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre, nobreSize);
			//目次階層化設定
			epub3Writer.setTocParam("1".equals(props.getProperty("NavNest")), "1".equals(props.getProperty("NcxNest")));
			
			//自動改ページ
			int forcePageBreakSize = 0;
			int forcePageBreakEmpty = 0;
			int forcePageBreakEmptySize = 0;
			int forcePageBreakChapter = 0;
			int forcePageBreakChapterSize = 0;
			if ("1".equals(props.getProperty("PageBreak"))) {
				try {
					try { forcePageBreakSize = Integer.parseInt(props.getProperty("PageBreakSize")) * 1024; } catch (Exception e) {}
					if ("1".equals(props.getProperty("PageBreakEmpty"))) {
						try { forcePageBreakEmpty = Integer.parseInt(props.getProperty("PageBreakEmptyLine")); } catch (Exception e) {}
						try { forcePageBreakEmptySize = Integer.parseInt(props.getProperty("PageBreakEmptySize")) * 1024; } catch (Exception e) {}
					} if ("1".equals(props.getProperty("PageBreakChapter"))) {
						forcePageBreakChapter = 1;
						try { forcePageBreakChapterSize = Integer.parseInt(props.getProperty("PageBreakChapterSize")) * 1024; } catch (Exception e) {}
					}
				} catch (Exception e) {}
			}
			int maxLength = 64; try { maxLength = Integer.parseInt((props.getProperty("ChapterNameLength"))); } catch (Exception e) {}
			boolean insertTitleToc = "1".equals(props.getProperty("TitleToc"));
			boolean chapterExclude = "1".equals(props.getProperty("ChapterExclude"));
			boolean chapterUseNextLine = "1".equals(props.getProperty("ChapterUseNextLine"));
			boolean chapterSection = !props.containsKey("ChapterSection")||"1".equals(props.getProperty("ChapterSection"));
			boolean chapterH = "1".equals(props.getProperty("ChapterH"));
			boolean chapterH1 = "1".equals(props.getProperty("ChapterH1"));
			boolean chapterH2 = "1".equals(props.getProperty("ChapterH2"));
			boolean chapterH3 = "1".equals(props.getProperty("ChapterH3"));
			boolean chapterName = "1".equals(props.getProperty("ChapterName"));
			boolean chapterNumOnly = "1".equals(props.getProperty("ChapterNumOnly"));
			boolean chapterNumTitle = "1".equals(props.getProperty("ChapterNumTitle"));
			boolean chapterNumParen = "1".equals(props.getProperty("ChapterNumParen"));
			boolean chapterNumParenTitle = "1".equals(props.getProperty("hapterNumParenTitle"));
			String chapterPattern = ""; if ("1".equals(props.getProperty("ChapterPattern"))) chapterPattern = props.getProperty("ChapterPatternText");
			
			//オプション指定を反映
			boolean useFileName = false;//表題に入力ファイル名利用
			String coverFileName = null;
			String encType = "MS932";
			String outExt = ".epub";
			boolean autoFileName = true; //ファイル名を表題に利用
			boolean vertical = true;
			if(commandLine.hasOption("t")) try { titleIndex = Integer.parseInt(commandLine.getOptionValue("t")); } catch (Exception e) {}//表題
			if(commandLine.hasOption("tf")) useFileName = true;
			if(commandLine.hasOption("c")) coverFileName = commandLine.getOptionValue("c");
			if(commandLine.hasOption("enc")) encType = commandLine.getOptionValue("enc");
			if(commandLine.hasOption("ext")) outExt = commandLine.getOptionValue("ext");
			if(commandLine.hasOption("of")) autoFileName = false;
			//if(commandLine.hasOption("id")) withMarkId = true;
			//if(commandLine.hasOption("tcy")) autoYoko = true;
			//if(commandLine.hasOption("g4")) gaiji32 = true;
			//if(commandLine.hasOption("tm")) middleTitle = true;
			//if(commandLine.hasOption("cb")) commentPrint = true;
			//if(commandLine.hasOption("cc")) commentConvert = true;
			//if(commandLine.hasOption("cp")) coverPage = true;
			if(commandLine.hasOption("hor")) vertical = false;
			
			//変換クラス生成とパラメータ設定
			AozoraEpub3Converter  aozoraConverter = new AozoraEpub3Converter(epub3Writer, jarPath);
			//挿絵なし
			aozoraConverter.setNoIllust("1".equals(props.getProperty("NoIllust"))); 
			//栞用span出力
			aozoraConverter.setWithMarkId(withMarkId);
			//変換オプション設定
			aozoraConverter.setAutoYoko(autoYoko, autoYokoNum1, autoYokoNum3, autoYokoEQ1);
			//4バイト文字出力
			aozoraConverter.setGaiji32(gaiji32);
			//全角スペースの禁則
			aozoraConverter.setSpaceHyphenation(spaceHyp);
			//コメント
			aozoraConverter.setCommentPrint(commentPrint, commentConvert);
			
			aozoraConverter.setRemoveEmptyLine(removeEmptyLine, maxEmptyLine);
			
			//強制改ページ
			aozoraConverter.setForcePageBreak(forcePageBreakSize, forcePageBreakEmpty, forcePageBreakEmptySize, forcePageBreakChapter, forcePageBreakChapterSize);
			//目次設定
			aozoraConverter.setChapterLevel(maxLength, chapterExclude, chapterUseNextLine, chapterSection,
					chapterH, chapterH1, chapterH2, chapterH3,
					chapterName,
					chapterNumOnly, chapterNumTitle, chapterNumParen, chapterNumParenTitle,
					chapterPattern);
			
			////////////////////////////////
			//各ファイルを変換処理
			////////////////////////////////
			for (String fileName : fileNames) {
				LogAppender.println("--------");
				File srcFile = new File(fileName);
				if (srcFile == null || !srcFile.isFile()) {
					LogAppender.println("[ERROR] file not exist. "+srcFile.getAbsolutePath());
					continue;
				}
				String ext = srcFile.getName();
				ext = ext.substring(ext.lastIndexOf('.')+1).toLowerCase();
				
				int coverImageIndex = -1;
				if (coverFileName != null) {
					if ("0".equals(coverFileName)) {
						coverImageIndex = 0;
						coverFileName = "";
					} else if ("1".equals(coverFileName)) {
						coverFileName = AozoraEpub3.getSameCoverFileName(srcFile); //入力ファイルと同じ名前+.jpg/.png
					}
				}
				
				//zipならzip内のテキストを検索
				int txtCount = 1;
				boolean imageOnly = false;
				boolean isFile = "txt".equals(ext);
				if("zip".equals(ext) || "txtz".equals(ext)) { 
					try {
						txtCount = AozoraEpub3.countZipText(srcFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (txtCount == 0) { txtCount = 1; imageOnly = true; }
				} else if ("cbz".equals(ext)) {
					imageOnly = true;
				}
				for (int txtIdx=0; txtIdx<txtCount; txtIdx++) {
					ImageInfoReader imageInfoReader = new ImageInfoReader(isFile, srcFile);
					
					BookInfo bookInfo = null;
					if (!imageOnly) {
						bookInfo = AozoraEpub3.getBookInfo(srcFile, ext, txtIdx, imageInfoReader, aozoraConverter, encType, BookInfo.TitleType.indexOf(titleIndex));
						bookInfo.vertical = vertical;
						bookInfo.insertTocPage = tocPage;
						bookInfo.setTocVertical(tocVertical);
						bookInfo.insertTitleToc = insertTitleToc;
						aozoraConverter.vertical = vertical;
						//表題ページ
						bookInfo.titlePageType = titlePage;
					}
					//表題の見出しが非表示で行が追加されていたら削除
					if (!bookInfo.insertTitleToc && bookInfo.titleLine >= 0) {
						bookInfo.removeChapterLineInfo(bookInfo.titleLine);
					}
					
					Epub3Writer writer = epub3Writer;
					if (!isFile) {
						imageInfoReader.loadZipImageInfos(srcFile, imageOnly);
						if (imageOnly) {
							LogAppender.println("画像のみのePubファイルを生成します");
							//画像出力用のBookInfo生成
							bookInfo = new BookInfo(srcFile);
							bookInfo.imageOnly = true;
							//Writerを画像出力用派生クラスに入れ替え
							writer = epub3ImageWriter;
							
							if (imageInfoReader.countImageFileInfos() == 0) {
								LogAppender.println("[ERROR] 画像がありませんでした");
								return;
							}
							//名前順で並び替え
							imageInfoReader.sortImageFileNames();
						}
					}
					//先頭からの場合で指定行数以降なら表紙無し
					if ("".equals(coverFileName)) {
						try {
							int maxCoverLine = Integer.parseInt(props.getProperty("MaxCoverLine"));
							if (maxCoverLine > 0 && bookInfo.firstImageLineNum >= maxCoverLine) {
								coverImageIndex = -1;
								coverFileName = null;
							}
						} catch (Exception e) {}
					}
					
					//表紙設定
					bookInfo.insertCoverPageToc = coverPageToc;
					bookInfo.insertCoverPage = coverPage;
					bookInfo.coverImageIndex = coverImageIndex;
					if (coverFileName != null && !coverFileName.startsWith("http")) {
						File coverFile = new File(coverFileName);
						if (!coverFile.exists()) {
							coverFileName = srcFile.getParent()+"/"+coverFileName;
							if (!new File(coverFileName).exists()) {
								coverFileName = null;
								LogAppender.println("[WARN] 表紙画像ファイルが見つかりません : "+coverFile.getAbsolutePath());
							}
						}
					}
					bookInfo.coverFileName = coverFileName;
					
					String[] titleCreator = BookInfo.getFileTitleCreator(srcFile.getName());
					if (titleCreator != null) {
						if (useFileName) {
							if (titleCreator[0] != null && titleCreator[0].trim().length() >0) bookInfo.title = titleCreator[0];
							if (titleCreator[1] != null && titleCreator[1].trim().length() >0) bookInfo.creator = titleCreator[1];
						} else {
							//テキストから取得できていない場合
							if (bookInfo.title == null || bookInfo.title.length() == 0) bookInfo.title = titleCreator[0]==null?"":titleCreator[0];
							if (bookInfo.creator == null || bookInfo.creator.length() == 0) bookInfo.creator = titleCreator[1]==null?"":titleCreator[1];
						}
					}
					
					File outFile = getOutFile(srcFile, dstPath, bookInfo, autoFileName, outExt);
					AozoraEpub3.convertFile(
							srcFile, ext, outFile,
							aozoraConverter, writer,
							encType, bookInfo, imageInfoReader, txtIdx);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 出力ファイルを生成 */
	static File getOutFile(File srcFile, File dstPath, BookInfo bookInfo, boolean autoFileName, String outExt)
	{
		//出力ファイル
		if (dstPath == null) dstPath = srcFile.getAbsoluteFile().getParentFile();
		String outFileName = "";
		if (autoFileName && (bookInfo.creator != null || bookInfo.title != null)) {
			outFileName = dstPath.getAbsolutePath()+"/";
			if (bookInfo.creator != null && bookInfo.creator.length() > 0) {
				String str = bookInfo.creator.replaceAll("[\\\\|\\/|\\:|\\*|\\?|\\<|\\>|\\||\\\"|\t]", "");
				if (str.length() > 64) str = str.substring(0, 64);
				outFileName += "["+str+"] ";
			}
			if (bookInfo.title != null) {
				outFileName += bookInfo.title.replaceAll("[\\\\|\\/|\\:|\\*|\\!|\\?|\\<|\\>|\\||\\\"|\t]", "");
			}
			if (outFileName.length() > 250) outFileName = outFileName.substring(0, 250);
		} else {
			outFileName = dstPath.getAbsolutePath()+"/"+srcFile.getName().replaceFirst("\\.[^\\.]+$", "");
		}
		if (outExt.length() == 0) outExt = ".epub";
		File outFile = new File(outFileName + outExt);
		//書き込み許可設定
		outFile.setWritable(true);
		
		return outFile;
	}
	
	/** 前処理で一度読み込んでタイトル等の情報を取得 */
	static public BookInfo getBookInfo(File srcFile, String ext, int txtIdx, ImageInfoReader imageInfoReader, AozoraEpub3Converter aozoraConverter,
			String encType, BookInfo.TitleType titleType)
	{
		try {
			String[] textEntryName = new String[1];
			InputStream is = AozoraEpub3.getInputStream(srcFile, ext, imageInfoReader, textEntryName, txtIdx);
			if (is == null) return null;
			
			//タイトル、画像注記、左右中央注記、目次取得
			BufferedReader src = new BufferedReader(new InputStreamReader(is, (String)encType));
			BookInfo bookInfo = aozoraConverter.getBookInfo(srcFile, src, imageInfoReader, titleType);
			is.close();
			bookInfo.textEntryName = textEntryName[0];
			return bookInfo;
			
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.append("エラーが発生しました : ");
			LogAppender.println(e.getMessage());
		}
		return null;
	}
	
	/** ファイルを変換
	 * @param srcFile 変換するファイル
	 * @param dstPath 出力先パス */
	static public void convertFile(File srcFile, String ext, File outFile, AozoraEpub3Converter aozoraConverter, Epub3Writer epubWriter,
			String encType, BookInfo bookInfo, ImageInfoReader imageInfoReader, int txtIdx)
	{
		try {
			long time = System.currentTimeMillis();
			LogAppender.append("変換開始 : ");
			LogAppender.println(srcFile.getPath());
			
			//入力Stream再オープン
			BufferedReader src = null;
			if (!bookInfo.imageOnly) {
				src = new BufferedReader(new InputStreamReader(getInputStream(srcFile, ext, null, null, txtIdx), encType));
			}
			
			//ePub書き出し srcは中でクローズされる
			epubWriter.write(aozoraConverter, src, srcFile, ext, outFile, bookInfo, imageInfoReader);
			
			LogAppender.append("変換完了["+(((System.currentTimeMillis()-time)/100)/10f)+"s] : ");
			LogAppender.println(outFile.getPath());
			
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.println("エラーが発生しました : "+e.getMessage());
			//LogAppender.printStaclTrace(e);
		}
	}
	
	/** 入力ファイルからStreamオープン
	 * 
	 * @param srcFile
	 * @param ext
	 * @param imageInfoReader
	 * @param txtIdx テキストファイルのZip内の位置
	 * @return テキストファイルのストリーム (close()は呼び出し側ですること)
	 */
	@SuppressWarnings("resource")
	static public InputStream getInputStream(File srcFile, String ext, ImageInfoReader imageInfoReader, String[] textEntryName, int txtIdx) throws IOException
	{
		InputStream is = null;
		if ("zip".equals(ext) || "txtz".equals(ext)) {
			//Zipなら最初のtxt
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
			ArchiveEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String entryName = entry.getName();
				if (entryName.substring(entryName.lastIndexOf('.')+1).equalsIgnoreCase("txt") && txtIdx-- == 0) {
					if (imageInfoReader != null) imageInfoReader.setZipTextEntry(entryName);
					if (textEntryName != null) textEntryName[0] = entryName;
					is = zis; break;
				}
			}
			if (is == null) {
				LogAppender.append("zip内にtxtファイルがありません: ");
				LogAppender.println(srcFile.getName());
				zis.close();
				return null;
			}
		} else if ("txt".equals(ext)) {
			is = new FileInputStream(srcFile);
		} else {
			LogAppender.append("txt, zip, txtzのみ変換可能です: ");
			LogAppender.println(srcFile.getPath());
		}
		return is;
	}
	
	/** Zipファイル内のテキストファイルの数を取得 */
	static public int countZipText(File zipFile) throws IOException
	{
		int txtCount = 0;
		ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile), 65536), "MS932", false);
		ArchiveEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			String entryName = entry.getName();
			if (entryName.substring(entryName.lastIndexOf('.')+1).equalsIgnoreCase("txt")) txtCount++;
		}
		zis.close();
		return txtCount;
	}
	
	/** 入力ファイルと同じ名前の画像を取得
	 * png, jpg, jpegの順で探す  */
	static public String getSameCoverFileName(File srcFile)
	{
		String baseFileName = srcFile.getPath();
		baseFileName = baseFileName.substring(0, baseFileName.lastIndexOf('.')+1);
		for (String ext : new String[]{"png","jpg","jpeg","PNG","JPG","JPEG"}) {
			String coverFileName = baseFileName+ext;
			if (new File(coverFileName).exists()) return coverFileName;
		}
		return null;
	}
}
