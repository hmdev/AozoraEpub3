import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.writer.Epub3Writer;

/** コマンドライン実行用mainとePub3変換関数 */
public class AozoraEpub3
{
	/** コマンドライン実行用 */
	public static void main(String args[])
	{
		/** 青空→ePub3変換クラス */
		AozoraEpub3Converter aozoraConverter;
		
		/** ePub3出力クラス */
		Epub3Writer epub3Writer;
		
		/** 設定ファイル */
		Properties props;
		/** 設定ファイル名 */
		String propFileName = "AozoraEpub3.ini";
		
		//初期化
		try {
			props = new Properties(); 
			try {
				props.load(new FileInputStream(propFileName));
			} catch (Exception e) { }
			
			//ePub出力クラス初期化
			epub3Writer = new Epub3Writer("template/");
			
			//固定オプション
			boolean withIdSpan = true;
			boolean autoYoko = true;
			boolean gaiji32 = false;
			boolean insertCoverPage = false;//表紙追加
			boolean useFileName = false;//表題に入力ファイル名利用
			BookInfo.TitleType titleType = BookInfo.TitleType.TITLE_AUTHOR;//表題
			
			//propsから取得するオプション
			String propValue = props.getProperty("AutoFileName");
			boolean autoFileName = propValue==null||"1".equals(propValue);
			propValue = props.getProperty("Ext");
			String outExt = propValue==null||propValue.length()==0?".epub":propValue;
			propValue = props.getProperty("Vertical");
			boolean vertical = propValue==null||"1".equals(propValue);
			propValue = props.getProperty("EncType");
			String encType = "1".equals(propValue)?"UTF-8":"MS932";
			
			//変換テーブルをstaticに生成
			aozoraConverter = new AozoraEpub3Converter(epub3Writer);
			//栞用span出力
			aozoraConverter.setWithMarkId(withIdSpan);
			//変換オプション設定
			aozoraConverter.setAutoYoko(autoYoko);
			//4バイト文字出力
			aozoraConverter.setGaiji32(gaiji32);
			
			for (int i=0; i<args.length; i++) {
				File srcFile = new File(args[i]);
				String ext = srcFile.getName();
				ext = ext.substring(ext.lastIndexOf('.')+1).toLowerCase();
				String coverFileName = null;//表紙なし
				//String coverFileName = "";//先頭画像
				//String coverFileName = AozoraEpub3.getSameCoverFileName(srcFile); //ファイル名と同じ
				BookInfo bookInfo = AozoraEpub3.getBookInfo(srcFile, ext, aozoraConverter, encType, titleType, "".equals(coverFileName), insertCoverPage);
				bookInfo.coverFileName = coverFileName;
				bookInfo.insertCoverPage = insertCoverPage;
				bookInfo.vertical = vertical;
				//確認なしなのでnullでなければ上書き
				if (useFileName) {
					String[] titleCreator = getFileTitleCreator(srcFile.getName());
					if (titleCreator[0] != null && titleCreator[0].trim().length() >0) bookInfo.title = titleCreator[0];
					if (titleCreator[1] != null && titleCreator[1].trim().length() >0) bookInfo.creator = titleCreator[1];
				}
				
				HashMap<String, ImageInfo> zipImageFileInfos = null;
				try {
					if (!"txt".equals(ext)) {
						zipImageFileInfos = AozoraEpub3.getZipImageInfos(srcFile, null, null);
					}
				} catch (Exception e) {
					e.printStackTrace();
					LogAppender.append("[ERROR] "+e+"\n");
				}
				
				File outFile = getOutFile(srcFile, null, bookInfo, autoFileName, outExt);
				AozoraEpub3.convertFile(
						srcFile, ext, outFile,
						aozoraConverter, epub3Writer,
						encType, bookInfo, zipImageFileInfos);
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
	static public BookInfo getBookInfo(File srcFile, String ext, AozoraEpub3Converter aozoraConverter,
			String encType, BookInfo.TitleType titleType, boolean firstImageIsCover, boolean insertCoverPage)
	{
		try {
			//Zip内テキストファイルのパス
			String[] textEntryNames = new String[1];
			
			InputStream is = getInputStream(srcFile, ext, textEntryNames);
			if (is == null) {
				return null;
			}
			
			//タイトル取得
			BufferedReader src = new BufferedReader(new InputStreamReader(is, (String)encType));
			BookInfo bookInfo = aozoraConverter.getBookInfo(src, titleType, firstImageIsCover, insertCoverPage);
			bookInfo.textEntryName = textEntryNames[0];
			is.close();
			
			return bookInfo;
			
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.append("エラーが発生しました : ");
			LogAppender.append(e.getMessage());
			LogAppender.append("\n");
		}
		return null;
	}
	
	/** ファイルを変換
	 * @param srcFile 変換するファイル
	 * @param dstPath 出力先パス */
	static public void convertFile(File srcFile, String ext, File outFile, AozoraEpub3Converter aozoraConverter, Epub3Writer epubWriter,
			String encType, BookInfo bookInfo, HashMap<String, ImageInfo> zipImageFileInfos)
	{
		try {
			LogAppender.append("変換開始 : ");
			LogAppender.append(srcFile.getPath());
			LogAppender.append("\n");
			
			//入力Stream再オープン
			BufferedReader src = null;
			if (!bookInfo.imageOnly) {
				src = new BufferedReader(new InputStreamReader(getInputStream(srcFile, ext, null), encType));
			}
			
			//ePub書き出し srcは中でクローズされる
			epubWriter.write(aozoraConverter, src, srcFile, outFile, bookInfo, zipImageFileInfos);
			
			LogAppender.append("変換完了 : ");
			LogAppender.append(outFile.getPath());
			LogAppender.append("\n");
			
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.append("エラーが発生しました : ");
			LogAppender.append(e.getMessage());
			LogAppender.append("\n");
		}
	}
	
	/** ファイル名からタイトルと著者名を取得 */
	static public String[] getFileTitleCreator(String fileName)
	{
		//ファイル名からタイトル取得
		String[] titleCreator = new String[2];
		String noExtName = fileName.substring(0, fileName.lastIndexOf('.'));
		noExtName = noExtName.replaceAll("^(.*)[\\(|（].*?[\\)|）][ |　]*$", "$1");
		Matcher m = Pattern.compile("[\\[|［](.+?)[\\]|］][ |　]*(.*)[ |　]*$").matcher(noExtName);
		if (m.find()) {
			titleCreator[0] = m.group(2);
			titleCreator[1] = m.group(1);
		} else {
			m = Pattern.compile("^(.*?)( |　)*(\\(|（)").matcher(noExtName);
			if (m.find()) {
				titleCreator[0] = m.group(1);
			} else {
				//一致しなければ拡張子のみ除外
				titleCreator[0] = noExtName;
			}
		}
		//trimして長さが0ならnullにする
		if (titleCreator[0] != null) {
			titleCreator[0] = titleCreator[0].trim();
			if (titleCreator[0].length() == 0) titleCreator[0] = null;
		}
		if (titleCreator[1] != null) {
			titleCreator[1] = titleCreator[1].trim();
			if (titleCreator[1].length() == 0) titleCreator[1] = null;
		}
		return titleCreator;
	}
	
	/** 入力ファイルからStreamオープン */
	@SuppressWarnings("resource")
	static private InputStream getInputStream(File srcFile, String ext, String[] textEntryNames) throws IOException
	{
		InputStream is = null;
		if ("zip".equals(ext)) {
			//Zipなら最初のtxt
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
			ArchiveEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().toLowerCase().endsWith(".txt")) {
					if (textEntryNames != null) textEntryNames[0] = entry.getName();
					is = zis; break;
				}
			}
			if (is == null) {
				LogAppender.append("zip内にtxtファイルがありません: ");
				LogAppender.append(srcFile.getName());
				LogAppender.append("\n");
				zis.close();
				return null;
			}
		} else if ("txt".equals(ext)) {
			is = new FileInputStream(srcFile);
		} else {
			LogAppender.append("txt, zipのみ変換可能です: ");
			LogAppender.append(srcFile.getPath());
			LogAppender.append("\n");
		}
		return is;
	}
	
	/** zip内の画像ファイルパスを取得
	 * @param bookInfo 表紙を保存するBookInfo
	 * @param 表紙のエントリ名 
	 * @throws IOException */
	static public HashMap<String, ImageInfo> getZipImageInfos(File srcFile, BookInfo bookInfo, String coverEntryName) throws IOException
	{
		HashMap<String, ImageInfo> zipImageInfos = new HashMap<String, ImageInfo>();
		ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
		ArchiveEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			String fileName = entry.getName();
			String lowerName = entry.getName().toLowerCase();
			if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
				ImageInfo imageInfo = null;
				try {
					if (coverEntryName != null && coverEntryName.equals(fileName)) {
						bookInfo.coverImage = ImageIO.read(zis);
						imageInfo = ImageInfo.getImageInfo(null, fileName, bookInfo.coverImage, zis.getCount());
					} else {
						imageInfo = ImageInfo.getImageInfo(null, fileName, zis, zis.getCount());
					}
				} catch (Exception e) {
					LogAppender.append("[ERROR] 画像が読み込めませんでした: ");
					LogAppender.append(srcFile.getPath());
					LogAppender.append("\n");
					e.printStackTrace();
				}
				if (imageInfo != null) zipImageInfos.put(fileName, imageInfo);
			}
		}
		zis.close();
		
		return zipImageInfos;
	}
	
	/** Zipファイルから指定されたEntryの画像を取得
	 * ※先頭からシークされるので遅い */
	static public BufferedImage getZipImage(File srcFile, String coverEntryName) throws IOException
	{
		ZipFile zf = new ZipFile(srcFile);
		InputStream is = zf.getInputStream(zf.getEntry(coverEntryName));
		try {
			return ImageIO.read(is);
		} catch (Exception e) {
			e.printStackTrace(); 
		} finally {
			is.close();
		}
		return null;
		
		/*ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 65536), "MS932", false);
		try {
			ArchiveEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String fileName = entry.getName();
				try {
					if (coverEntryName != null && coverEntryName.equals(fileName)) {
						return ImageIO.read(zis);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			zis.close();
		}
		return null;
		*/
	}
	
	/** 入力ファイルと同じ名前の画像を取得
	 * png, jpg, jpegの順で探す  */
	static public String getSameCoverFileName(File srcFile)
	{
		String coverFileName = null;
		String baseFileName = srcFile.getPath();
		baseFileName = baseFileName.substring(0, baseFileName.lastIndexOf('.'));
		if (new File (baseFileName+".png").exists()) {
			coverFileName = baseFileName+".png";
		} else if (new File (baseFileName+".jpg").exists()) {
			coverFileName = baseFileName+".jpg";
		} else if (new File (baseFileName+".jpeg").exists()) {
			coverFileName = baseFileName+".jpeg";
		} else if (new File (baseFileName+".PNG").exists()) {
			coverFileName = baseFileName+".PNG";
		} else if (new File (baseFileName+".JPG").exists()) {
			coverFileName = baseFileName+".JPG";
		} else if (new File (baseFileName+".JPEG").exists()) {
			coverFileName = baseFileName+".JPEG";
		}
		return coverFileName;
	}
}
