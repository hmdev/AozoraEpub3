import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.converter.AozoraEpub3Converter.TitleType;
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
		
		//初期化
		try {
			//ePub出力クラス初期化
			epub3Writer = new Epub3Writer("template/");
			
			boolean withIdSpan = false;
			boolean autoYoko = true;
			//変換テーブルをstaticに生成
			aozoraConverter = new AozoraEpub3Converter(epub3Writer);
			//栞用span出力
			aozoraConverter.setWithIdSpan(withIdSpan);
			//変換オプション設定
			aozoraConverter.setAutoYoko(autoYoko);
			
			//コマンドラインオプション
			boolean autoFileName = true;
			String outExt = ".epub";
			boolean overWrite = true;
			boolean vertical = true;
			String coverFileName = "";//先頭の挿絵
			String encType = "MS932";
			TitleType titleType = TitleType.FILENAME;
			for (int i=0; i<args.length; i++) {
				String srcFile = args[i];
				AozoraEpub3.convertFile(
						new File(srcFile),
						aozoraConverter,
						epub3Writer,
						autoFileName, outExt, overWrite, vertical, coverFileName,
						encType, titleType);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** ファイルを変換
	 * @param srcFile 変換するファイル */
	static public void convertFile(File srcFile, AozoraEpub3Converter aozoraConverter, Epub3Writer epubWriter,
			boolean autoFileName, String outExt, boolean overWrite,
			boolean vertical, String coverFileName,
			String encType, TitleType titleType)
	{
		try {
			
			//Zip内テキストファイルのパス
			String[] textEntryNames = new String[1];
			String ext = srcFile.getName().substring(srcFile.getName().lastIndexOf('.')+1).toLowerCase();
			//.txt .zip拡張子のみ
			InputStream is = getInputStream(srcFile, ext, textEntryNames);
			if (is == null) return;
			
			//タイトル取得
			BufferedReader src = new BufferedReader(new InputStreamReader(is, (String)encType));
			BookInfo bookInfo = aozoraConverter.getBookInfo(src, titleType);
			bookInfo.modified = new Date();
			//ファイル名からタイトル取得
			if (titleType == TitleType.FILENAME) {
				Matcher m = Pattern.compile("\\[(.+?)\\]( |　)*(.*?)( |　)*(\\(|（|\\.)").matcher(srcFile.getName());
				if (m.find()) {
					bookInfo.title = m.group(3);
					bookInfo.creator = m.group(1);
				}
			}
			
			is.close();
			
			//縦書き横書き設定追加
			bookInfo.vertical = vertical;
			//表紙情報追加
			bookInfo.coverFileName = coverFileName;
			
			//出力ファイル
			String outFileName = "";
			if (autoFileName && (bookInfo.creator != null || bookInfo.title != null)) {
				outFileName = srcFile.getAbsoluteFile().getParentFile().getPath()+"/";
				if (bookInfo.creator != null) outFileName += "["+bookInfo.creator.replaceAll("[\\\\|\\/|\\:|\\*|\\?|\\<|\\>|\\||\\\"]", "")+"] ";
				if (bookInfo.title != null) outFileName += bookInfo.title.replaceAll("[\\\\|\\/|\\:|\\*|\\?|\\<|\\>|\\||\\\"]", "");
			} else {
				outFileName = srcFile.getPath().replaceFirst("\\.[^\\.]+$", "");
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
			
			LogAppender.append("----------------------------------------------------------------\n");
			LogAppender.append("変換開始 : ");
			LogAppender.append(srcFile.getPath());
			LogAppender.append("\n");
			
			//入力Stream再オープン
			is = getInputStream(srcFile, ext, textEntryNames);
			
			src = new BufferedReader(new InputStreamReader(is, (String)encType));
			//BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
			
			// 青空文庫→ePub3タグへ変換
			//aozoraConverter.convertTextToEpub3(out, src, metaInfo);
			
			//ePub書き出し srcは中でクローズされる
			epubWriter.write(aozoraConverter, src, srcFile, textEntryNames[0], outFile, bookInfo);
			
			//src.close();
			//out.close();
			
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
				if (entry.getName().toLowerCase().endsWith(".txt")) { is = zis; break; }
			}
			textEntryNames[0] = entry.getName();
			if (is == null) {
				LogAppender.append("zip内にtxtファイルがありません");
				LogAppender.append("\n");
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
	
}
