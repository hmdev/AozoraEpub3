import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.BookInfo;
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
			boolean withIdSpan = false;
			boolean autoYoko = true;
			boolean overWrite = true;
			String coverFileName = "";//先頭の挿絵
			boolean insertCoverPage = false;
			BookInfo.TitleType titleType = BookInfo.TitleType.TITLE_AUTHOR;
			boolean useFileName = true;
			
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
			
			for (int i=0; i<args.length; i++) {
				File srcFile = new File(args[i]);
				BookInfo bookInfo = AozoraEpub3.getBookInfo(srcFile, aozoraConverter, encType, titleType, coverFileName);
				bookInfo.coverFileName = coverFileName;
				bookInfo.insertCoverPage = insertCoverPage;
				bookInfo.vertical = vertical;
				//確認なしなのでnullでなければ上書き
				if (useFileName) {
					String[] titleCreator = getFileTitleCreator(srcFile.getName());
					if (titleCreator[0] != null && titleCreator[0].trim().length() >0) bookInfo.title = titleCreator[0];
					if (titleCreator[1] != null && titleCreator[1].trim().length() >0) bookInfo.creator = titleCreator[1];
				}
				
				HashSet<String> zipImageFileNames = null;
				try {
					if (srcFile.getName().toLowerCase().endsWith("zip")) {
						zipImageFileNames = AozoraEpub3.getZipImageNames(srcFile);
					}
				} catch (Exception e) {
					e.printStackTrace();
					LogAppender.append("[ERROR] "+e+"\n");
				}
				
				AozoraEpub3.convertFile(
						srcFile, null,
						aozoraConverter, epub3Writer,
						autoFileName, outExt, overWrite,
						encType, bookInfo, zipImageFileNames);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 前処理で一度読み込んでタイトル等の情報を取得 */
	static public BookInfo getBookInfo(File srcFile, AozoraEpub3Converter aozoraConverter,
			String encType, BookInfo.TitleType titleType, String coverFileName)
	{
		try {
			//Zip内テキストファイルのパス
			String[] textEntryNames = new String[1];
			String ext = srcFile.getName().substring(srcFile.getName().lastIndexOf('.')+1).toLowerCase();
			
			InputStream is = getInputStream(srcFile, ext, textEntryNames);
			if (is == null) {
				return null;
			}
			
			//タイトル取得
			BufferedReader src = new BufferedReader(new InputStreamReader(is, (String)encType));
			BookInfo bookInfo = aozoraConverter.getBookInfo(src, titleType, coverFileName);
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
	static public void convertFile(File srcFile, File dstPath, AozoraEpub3Converter aozoraConverter, Epub3Writer epubWriter,
			boolean autoFileName, String outExt, boolean overWrite,
			String encType, BookInfo bookInfo, HashSet<String> zipImageFileNames)
	{
		try {
			
			//Zip内テキストファイルのパス
			String[] textEntryNames = new String[1];
			String ext = srcFile.getName().substring(srcFile.getName().lastIndexOf('.')+1).toLowerCase();
			//.txt .zip拡張子のみ
			
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
					outFileName += bookInfo.title.replaceAll("[\\\\|\\/|\\:|\\*|\\?|\\<|\\>|\\||\\\"|\t]", "");
				}
				if (outFileName.length() > 250) outFileName = outFileName.substring(0, 250);
			} else {
				outFileName = dstPath.getAbsolutePath()+"/"+srcFile.getName().replaceFirst("\\.[^\\.]+$", "");
			}
			
			if (outExt.length() == 0) outExt = ".epub";
			File outFile = new File(outFileName + outExt);
			//書き込み許可設定
			outFile.setWritable(true);
			
			if (!overWrite &&  outFile.exists()) {
				LogAppender.append("ファイルが存在します: ");
				LogAppender.append(outFile.getPath());
				LogAppender.append("\n");
				return;
			}
			
			LogAppender.append("変換開始 : ");
			LogAppender.append(srcFile.getPath());
			LogAppender.append("\n");
			
			//入力Stream再オープン
			BufferedReader src = null;
			if (!bookInfo.imageOnly) {
				src = new BufferedReader(new InputStreamReader(getInputStream(srcFile, ext, textEntryNames), encType));
			}
			
			//ePub書き出し srcは中でクローズされる
			epubWriter.write(aozoraConverter, src, srcFile, textEntryNames[0], outFile, bookInfo, zipImageFileNames);
			
			LogAppender.append("変換完了 : ");
			LogAppender.append(outFile.getPath());
			LogAppender.append("\n");
			src.close();
			
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
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile)), "Shift_JIS", false);
			ArchiveEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().toLowerCase().endsWith(".txt")) {
					textEntryNames[0] = entry.getName();
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
	 * @throws IOException */
	static public HashSet<String> getZipImageNames(File srcFile) throws IOException
	{
		HashSet<String> zipImageNames = new HashSet<String>();
		ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile)), "Shift_JIS", false);
		ArchiveEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			String lowerName = entry.getName().toLowerCase();
			if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
				zipImageNames.add(entry.getName());
			}
		}
		zis.close();
		return zipImageNames;
	}
	
}
