package com.github.hmdev.info;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.hmdev.image.ImageUtils;
import com.github.hmdev.util.CharUtils;

/** タイトル著作者等のメタ情報を格納 */
public class BookInfo
{
	/** タイトル記載種別 */
	public enum TitleType {
		TITLE_AUTHOR, AUTHOR_TITLE, SUBTITLE_AUTHOR, TITLE_ONLY, TITLE_AUTHOR_ONLY, NONE;
		final static public String[] titleTypeNames = {"表題 → 著者名", "著者名 → 表題", "表題 → 著者名(副題優先)", "表題のみ(1行)", "表題+著者のみ(2行)", "なし"};
		static public TitleType indexOf(int idx)
		{
			return values()[idx];
		}
		boolean hasTitleAuthor() {
			switch (this) {
			case TITLE_ONLY: case NONE: return false;
			default: return true;
			}
		}
		boolean hasTitle() {
			switch (this) {
			case NONE: return false;
			default: return true;
			}
		}
		boolean hasAuthor() {
			switch (this) {
			case TITLE_ONLY: case NONE: return false;
			default: return true;
			}
		}
		boolean titleFirst() {
			switch (this) {
			case TITLE_AUTHOR: case SUBTITLE_AUTHOR: case TITLE_ONLY: case TITLE_AUTHOR_ONLY: return true;
			default: return false;
			}
		}
	}
	
	/** 表題は出力しない  */
	public final static int TITLE_NONE = -1;
	/** 表題は別ページにせずそのまま出力  */
	public final static int TITLE_NORMAL = 0;
	/** 表題ページを左右中央 コメント行前の8行まで
	 * コメント行がない場合と9行以上の場合は無効になる */
	public final static int TITLE_MIDDLE = 1;
	/** 表題ページ横書き */
	public final static int TITLE_HORIZONTAL = 2;
	
	/** 表題種別 */
	public int titlePageType = 0;
	
	////////////////////////////////
	/** タイトル等の行 */
	String[] metaLines;
	/** タイトル等の開始行番号 */
	public int metaLineStart;
	
	/** テキストの行数 */
	public int totalLineNum = -1;
	
	/** タイトル */
	public String title;
	/** タイトル行番号 */
	public int titleLine = -1;
	/** タイトル読み */
	public String titleAs;
	
	/** 副題 */
	public String subTitle;
	/** 副題番号 */
	public int subTitleLine = -1;
	
	/** 原題行番号 */
	public int orgTitleLine = -1;
	/** 原副題行番号 */
	public int subOrgTitleLine = -1;
	
	/** 著作者 */
	public String creator;
	/** 著作者行番号 */
	public int creatorLine = -1;
	/** 複著作者行番号 */
	public int subCreatorLine = -1;
	/** 著者読み */
	public String creatorAs;
	
	/** シリーズ名 */
	public int seriesLine = -1;
	/** 刊行者行 */
	public int publisherLine = -1;
	/** 刊行者文字列 */
	public String publisher;
	
	////////////////////////////////
	
	/** タイトル行の最後 */
	public int titleEndLine = -1;
	
	/** コメント先頭行 */
	int firstCommentLineNum = -1;
	
	/** 発刊日時 */
	public Date published;
	/** 更新日時 */
	public Date modified;
	
	/** 縦書きならtrue */
	public boolean vertical = true;
	
	/** 右から左ならtrue */
	public boolean rtl = false;
	
	/** 入力ファイル */
	public File srcFile;
	/** 圧縮ファイル内のテキストファイルエントリー名 */
	public String textEntryName = null;
	
	/** 先頭の画像行番号 */
	public int firstImageLineNum = -1;
	/** 先頭の画像位置 外字等の小さい画像は無視される */
	public int firstImageIdx = -1;
	
	/** 表紙編集情報 */
	public CoverEditInfo coverEditInfo;
	/** 表紙ファイル名 フルパスかURL ""なら先頭の挿絵 nullなら表紙無し */
	public String coverFileName;
	/** 表紙イメージがトリミングされた場合に設定される coverFileNameより優先される */
	public BufferedImage coverImage = null;
	/** 表紙に使う挿絵の本文内Index -1なら本文内の挿絵は使わない */
	public int coverImageIndex = -1;
	/** imageにしたとき用の元ファイルの拡張子 */
	public String coverExt = null;
	
	/** 先頭に表紙ページを追加 */
	public boolean insertCoverPage = false;
	/** 表紙を目次に入れる */
	public boolean insertCoverPageToc = false;
	
	/** 表紙ページを追加した場合は表紙は目次より前に出力 */
	public boolean insertTitlePage = false;
	/** 目次ページを追加 */
	public boolean insertTocPage = false;
	/** 目次縦書きならtrue */
	public boolean tocVertical = false;
	/** 表題を目次に追加 */
	public boolean insertTitleToc = true;
	
	/** txtのない画像のみの場合 */
	public boolean imageOnly = false;
	
	/** タイトルページの改ページ行 前に改ページがなければ-1 表題がなければ-2 */
	//public int preTitlePageBreak = -2;
	
	/** 改ページ単位で区切られたセクションの情報を格納 */
	//Vector<SectionInfo> vecSectionInfo;
	
	/** 画像単体ページ開始行 */
	HashMap<Integer, String> mapImageSectionLine;
	/** 強制改ページ行 */
	HashSet<Integer> mapPageBreakLine;
	/** 改ページしない行 (［＃ページの左右中央］の前の［＃改ページ］) */
	HashSet<Integer> mapNoPageBreakLine;
	/** 出力ページしない行 (左右中央後の空行と改ページ前の空行) */
	HashSet<Integer> mapIgnoreLine;
	/** 見出し行の情報 */
	HashMap<Integer, ChapterLineInfo> mapChapterLine;
	
	////////////////////////////////////////////////////////////////
	public BookInfo(File srcFile)
	{
		this.srcFile = srcFile;
		this.modified = new Date();
	}
	
	public void clear()
	{
		if (this.mapImageSectionLine != null) this.mapImageSectionLine.clear();
		if (this.mapPageBreakLine != null) this.mapPageBreakLine.clear();
		if (this.mapNoPageBreakLine != null) this.mapNoPageBreakLine.clear();
		if (this.mapIgnoreLine != null) this.mapIgnoreLine.clear();
	}
	
	/*public void addSectionInfo(SectionInfo sectionInfo)
	{
		this.vecSectionInfo.add(sectionInfo);
	}
	public SectionInfo getLastSectionInfo()
	{
		return vecSectionInfo.lastElement();
	}*/
	/** 画像単体ページの行数を保存 */
	public void addImageSectionLine(int lineNum, String imageFileName)
	{
		if (this.mapImageSectionLine == null) this.mapImageSectionLine = new HashMap<Integer, String>();
		this.mapImageSectionLine.put(lineNum, imageFileName);
	}
	/** 画像単体ページの行ならtrue */
	public boolean isImageSectionLine(int lineNum)
	{
		if (this.mapImageSectionLine == null) return false;
		return this.mapImageSectionLine.containsKey(lineNum);
	}
	/** 画像単体ページの行の画像ファイル名を返却 */
	public String getImageSectionFileName(int lineNum)
	{
		if (this.mapImageSectionLine == null) return null;
		return this.mapImageSectionLine.get(lineNum);
	}
	
	/** 強制改ページ行数を保存 */
	public void addPageBreakLine(int lineNum)
	{
		if (this.mapPageBreakLine == null) this.mapPageBreakLine = new HashSet<Integer>();
		this.mapPageBreakLine.add(lineNum);
	}
	/** 強制改ページ行ならtrue */
	public boolean isPageBreakLine(int lineNum)
	{
		if (this.mapPageBreakLine == null) return false;
		return this.mapPageBreakLine.contains(lineNum);
	}
	
	/** 改ページしない行数を保存 */
	public void addNoPageBreakLine(int lineNum)
	{
		if (this.mapNoPageBreakLine == null) this.mapNoPageBreakLine = new HashSet<Integer>();
		this.mapNoPageBreakLine.add(lineNum);
	}
	/** 改ページしない行ならtrue */
	public boolean isNoPageBreakLine(int lineNum)
	{
		if (this.mapNoPageBreakLine == null) return false;
		return this.mapNoPageBreakLine.contains(lineNum);
	}
	
	/** 出力しない行数を保存 */
	public void addIgnoreLine(int lineNum)
	{
		if (this.mapIgnoreLine == null) this.mapIgnoreLine = new HashSet<Integer>();
		this.mapIgnoreLine.add(lineNum);
	}
	/** 出力しない行ならtrue */
	public boolean isIgnoreLine(int lineNum)
	{
		if (this.mapIgnoreLine == null) return false;
		return this.mapIgnoreLine.contains(lineNum);
	}
	
	/** 見出し行と階層レベルを保存 */
	public void addChapterLineInfo(ChapterLineInfo chapterLineInfo)
	{
		if (this.mapChapterLine == null) this.mapChapterLine = new HashMap<Integer, ChapterLineInfo>();
		this.mapChapterLine.put(chapterLineInfo.lineNum, chapterLineInfo);
	}
	/** 見出し行と削除 */
	public void removeChapterLineInfo(int lineNum)
	{
		if (this.mapChapterLine != null) this.mapChapterLine.remove(lineNum);
	}
	/** 見出し行の情報取得 */
	public ChapterLineInfo getChapterLineInfo(int lineNum)
	{
		if (this.mapChapterLine == null) return null;
		return this.mapChapterLine.get(lineNum);
	}
	
	/** 見出し行なら目次階層レベルを返す */
	public int getChapterLevel(int lineNum)
	{
		if (this.mapChapterLine == null) return 0;
		ChapterLineInfo info = this.mapChapterLine.get(lineNum);
		return info == null ? 0 : info.level;
	}
	
	/** 行番号順に並び替えた目次一覧リストを生成して返す */
	public Vector<ChapterLineInfo> getChapterLineInfoList()
	{
		Vector<ChapterLineInfo> list = new Vector<ChapterLineInfo>();
		
		if (this.mapChapterLine == null) return list;
		
		int[] lines = new int[this.mapChapterLine.size()];
		int i = 0;
		for (Integer lineNum : this.mapChapterLine.keySet()) {
			lines[i++] = lineNum;
		}
		Arrays.sort(lines);
		for (int lineNum : lines) {
			list.add(this.mapChapterLine.get(lineNum));
		}
		return list;
	}
	
	/** 目次ページの自動抽出見出しを除外 */
	public void excludeTocChapter()
	{
		if (this.mapChapterLine == null) return;
		//前2行と後ろ2行が自動抽出見出しの行を抽出 間の行は空行のみ許可
		HashSet<Integer> excludeLine = new HashSet<Integer>();
		for (Integer lineNum : this.mapChapterLine.keySet()) {
			if (this.isPattern(lineNum)) {
				boolean prevIsPattern = false;
				if (this.isPattern(lineNum-1)) prevIsPattern = true;
				else if (this.mapChapterLine.get(lineNum).emptyNext && this.isPattern(lineNum-2)) prevIsPattern = true; //前が空行の場合のみ
				boolean nextIsPattern = false;
				if (this.isPattern(lineNum+1)) nextIsPattern = true;
				else if (this.isPattern(lineNum+2)) nextIsPattern = true;
				if (prevIsPattern && nextIsPattern) excludeLine.add(lineNum);
			}
		}
		//先頭と最後
		HashSet<Integer> excludeLine2 = new HashSet<Integer>();
		for (Integer lineNum : this.mapChapterLine.keySet()) {
			if (!excludeLine.contains(lineNum) && this.isPattern(lineNum)) {
				if (excludeLine.contains(lineNum-1)) excludeLine2.add(lineNum);
				else if (this.mapChapterLine.get(lineNum).emptyNext && excludeLine.contains(lineNum-2)) excludeLine2.add(lineNum);
				else if (excludeLine.contains(lineNum+1)) excludeLine2.add(lineNum);
				else if (excludeLine.contains(lineNum+2)) excludeLine2.add(lineNum);
			}
		}
		for (Integer lineNum : excludeLine) {
			this.mapChapterLine.remove(lineNum);
		}
		for (Integer lineNum : excludeLine2) {
			this.mapChapterLine.remove(lineNum);
		}
	}
	
	private boolean isPattern(int num)
	{
		ChapterLineInfo chapterLineInfo = this.getChapterLineInfo(num);
		if (chapterLineInfo == null) return false;
		return chapterLineInfo.isPattern();	
	}
	
	////////////////////////////////////////////////////////////////
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	public int getTitleLine()
	{
		return titleLine;
	}
	public void setTitleLine(int titleLine)
	{
		this.titleLine = titleLine;
	}

	public String getCreator()
	{
		return creator;
	}
	public void setCreator(String creator)
	{
		this.creator = creator;
	}
	public int getCreatorLine()
	{
		return creatorLine;
	}
	public void setCreatorLine(int creatorLine)
	{
		this.creatorLine = creatorLine;
	}

	public Date getPublished()
	{
		return published;
	}
	public void setPublished(Date published)
	{
		this.published = published;
	}

	public Date getModified()
	{
		return modified;
	}
	public void setModified(Date modified)
	{
		this.modified = modified;
	}

	public boolean isVertical()
	{
		return vertical;
	}
	public void setVertical(boolean vertical)
	{
		this.vertical = vertical;
	}

	public boolean isRtl()
	{
		return rtl;
	}
	public void setRtl(boolean rtl)
	{
		this.rtl = rtl;
	}

	public String getCoverFileName()
	{
		return coverFileName;
	}
	public void setCoverFileName(String coverFileName)
	{
		this.coverFileName = coverFileName;
	}

	public boolean isInsertCoverPage()
	{
		return insertCoverPage;
	}
	
	public boolean isInsertCoverPageToc()
	{
		return insertCoverPageToc;
	}
	
	public boolean isInsertTitlePage()
	{
		return insertTitlePage;
	}

	public void setInsertTitlePage(boolean insertTitlePage)
	{
		this.insertTitlePage = insertTitlePage;
	}

	public boolean isInsertTocPage()
	{
		return insertTocPage;
	}

	public void setInsertTocPage(boolean insertTocPage)
	{
		this.insertTocPage = insertTocPage;
	}

	public boolean isTocVertical()
	{
		return tocVertical;
	}

	public void setTocVertical(boolean tocVertical)
	{
		this.tocVertical = tocVertical;
	}

	public boolean isImageOnly()
	{
		return imageOnly;
	}

	public void setImageOnly(boolean imageOnly)
	{
		this.imageOnly = imageOnly;
	}
	
	////////////////////////////////////////////////////////////////
	public String getTitleText()
	{
		if (this.titleLine == -1) return null;
		try {
			return metaLines[this.titleLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	public String getSubTitleText()
	{
		if (this.subTitleLine == -1) return null;
		try {
			return metaLines[this.subTitleLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	public String getOrgTitleText()
	{
		if (this.orgTitleLine == -1) return null;
		try {
			return metaLines[this.orgTitleLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	public String getSubOrgTitleText()
	{
		if (this.subOrgTitleLine == -1) return null;
		try {
			return metaLines[this.subOrgTitleLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	public String getCreatorText()
	{
		if (this.creatorLine == -1) return null;
		try {
			return metaLines[this.creatorLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	public String getSubCreatorText()
	{
		if (this.subCreatorLine == -1) return null;
		try {
			return metaLines[this.subCreatorLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	public String getSeriesText()
	{
		if (this.seriesLine == -1) return null;
		try {
			return metaLines[this.seriesLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	public String getPublisherText()
	{
		if (this.publisherLine == -1) return null;
		try {
			return metaLines[this.publisherLine-this.metaLineStart];
		} catch (Exception e) {}
		return null;
	}
	
	////////////////////////////////////////////////////////////////
	/** 先頭行から表題と著者を取得 */
	public void setMetaInfo(TitleType titleType, boolean pubFirst, String[] metaLines, int metaLineStart, int firstCommentLineNum)
	{
		this.firstCommentLineNum = firstCommentLineNum;
		
		this.titleLine = -1;
		this.orgTitleLine = -1;
		this.subTitleLine = -1;
		this.subOrgTitleLine = -1;
		this.creatorLine = -1;
		this.subCreatorLine = -1;
		this.publisherLine = -1;
		this.title = "";
		this.titleAs = null;
		this.creator = "";
		this.creatorAs = null;
		this.publisher = null;
		
		if (titleType != TitleType.NONE) {
			
			//バッファからタイトルと著者取得
			this.metaLines = metaLines;
			this.metaLineStart = metaLineStart;
			//連続行数取得
			int linesLength = 0;
			for (int i=0; i<metaLines.length; i++) {
				if (metaLines[i] == null || metaLines[i].length() == 0) {
					linesLength = i; break;
				}
			}
			
			int arrIndex = 0;
			//先頭に発行者で2行以上
			if (pubFirst && linesLength >= 2) {
				this.publisherLine = metaLineStart;
				this.publisher = metaLines[0];
				metaLineStart++;
				linesLength--;
				arrIndex++;
			}
			//表題のみ
			if (linesLength > 0 && titleType == TitleType.TITLE_ONLY) {
				this.titleLine = metaLineStart;
				this.title = metaLines[0+arrIndex];
				titleEndLine = metaLineStart;
			} else if (linesLength > 0 && titleType == TitleType.TITLE_AUTHOR_ONLY) {
				this.titleLine = metaLineStart;
				this.title = metaLines[0+arrIndex];
				this.creator = metaLines[1+arrIndex];
				titleEndLine = metaLineStart+1;
			} else {
				switch (Math.min(6, linesLength)) {
				case 6:
					if (titleType.titleFirst()) {
						this.titleLine = metaLineStart;
						this.orgTitleLine = metaLineStart+1;
						this.subTitleLine = metaLineStart+2;
						this.subOrgTitleLine = metaLineStart+3;
						this.title = metaLines[0+arrIndex]+" "+metaLines[2+arrIndex];
						titleEndLine = metaLineStart+3;
						if (titleType.hasAuthor()) {
							this.creatorLine = metaLineStart+4;
							this.subCreatorLine = metaLineStart+5;
							this.creator = metaLines[4+arrIndex];
							titleEndLine = metaLineStart+5;
						}
					} else {
						this.creatorLine = metaLineStart;
						this.subCreatorLine = metaLineStart+1;
						this.creator = metaLines[0+arrIndex];
						titleEndLine = metaLineStart+1;
						if (titleType.hasTitle()) {
							this.titleLine = metaLineStart+2;
							this.orgTitleLine = metaLineStart+3;
							this.subTitleLine = metaLineStart+4;
							this.subOrgTitleLine = metaLineStart+5;
							this.title = metaLines[2+arrIndex]+" "+metaLines[4+arrIndex];
							titleEndLine = metaLineStart+5;
						}
					}
					break;
				case 5:
					if (titleType.titleFirst()) {
						this.titleLine = metaLineStart;
						this.orgTitleLine = metaLineStart+1;
						this.subTitleLine = metaLineStart+2;
						this.title = metaLines[0+arrIndex]+" "+metaLines[2+arrIndex];
						titleEndLine = metaLineStart+2;
						if (titleType.hasAuthor()) {
							this.creatorLine = metaLineStart+3;
							this.subCreatorLine = metaLineStart+4;
							this.creator = metaLines[3+arrIndex];
							titleEndLine = metaLineStart+4;
						}
					} else {
						this.creatorLine = metaLineStart;
						this.creator = metaLines[0+arrIndex];
						titleEndLine = metaLineStart;
						if (titleType.hasTitle()) {
							this.titleLine = metaLineStart+1;
							this.orgTitleLine = metaLineStart+2;
							this.subTitleLine = metaLineStart+3;
							this.subOrgTitleLine = metaLineStart+4;
							this.title = metaLines[1+arrIndex]+" "+metaLines[3+arrIndex];
						}
						titleEndLine = metaLineStart+4;
					}
					break;
				case 4:
					if (titleType.titleFirst()) {
						this.titleLine = metaLineStart;
						this.subTitleLine = metaLineStart+1;
						this.title = metaLines[0+arrIndex]+" "+metaLines[1+arrIndex];
						titleEndLine = metaLineStart+1;
						if (titleType.hasAuthor()) {
							this.creatorLine = metaLineStart+2;
							this.subCreatorLine = metaLineStart+3;
							this.creator = metaLines[2+arrIndex];
							titleEndLine = metaLineStart+3;
						}
					} else {
						this.creatorLine = metaLineStart;
						this.subCreatorLine = metaLineStart+1;
						this.creator = metaLines[0+arrIndex];
						titleEndLine = metaLineStart+1;
						if (titleType.hasTitle()) {
							this.titleLine = metaLineStart+2;
							this.subTitleLine = metaLineStart+3;
							this.title = metaLines[2+arrIndex]+" "+metaLines[3+arrIndex];
							titleEndLine = metaLineStart+3;
						}
					}
					break;
				case 3: //表題+副題+著者 または 表題+著者+翻訳者
					if (titleType.titleFirst()) {
						this.titleLine = metaLineStart;
						this.subTitleLine = metaLineStart+1;
						this.title = metaLines[0+arrIndex]+" "+metaLines[1+arrIndex];
						titleEndLine = metaLineStart+1;
						if (titleType.hasAuthor()) {
							//副著者を文字列で判断
							if (titleType != TitleType.SUBTITLE_AUTHOR && !metaLines[1].startsWith("―") &&
								(metaLines[2+arrIndex].endsWith("訳") || metaLines[2+arrIndex].endsWith("編纂") || metaLines[2+arrIndex].endsWith("校訂"))) {
								this.titleLine = metaLineStart;
								this.title = metaLines[0+arrIndex];
								this.subTitleLine = -1;
								this.creatorLine = metaLineStart+1;
								this.creator = metaLines[1+arrIndex];
								this.subCreatorLine = metaLineStart+2;
							} else {
								this.creatorLine = metaLineStart+2;
								this.creator = metaLines[2+arrIndex];
							}
							titleEndLine = metaLineStart+2;
						}
					} else {
						this.creatorLine = metaLineStart;
						this.creator = metaLines[0+arrIndex];
						titleEndLine = metaLineStart;
						if (titleType.hasTitle()) {
							this.titleLine = metaLineStart+1;
							this.subTitleLine = metaLineStart+2;
							this.title = metaLines[1+arrIndex]+" "+metaLines[2+arrIndex];
							titleEndLine = metaLineStart+2;
						}
					}
					break;
				case 2: //表題+著者 すぐ後にコメント行がある場合のみ表題+副題+空行+著者
					if (titleType.titleFirst()) {
						this.titleLine = metaLineStart;
						this.title = metaLines[0+arrIndex];
						if (titleType.hasAuthor()) {
							if (firstCommentLineNum > 0 && firstCommentLineNum <= 6 && metaLines[3+arrIndex] != null && metaLines[3+arrIndex].length() > 0 && (metaLines[4+arrIndex] == null || metaLines[4+arrIndex].length() == 0)) {
								this.titleLine = metaLineStart;
								this.subTitleLine = metaLineStart+1;
								this.title = metaLines[0+arrIndex]+" "+metaLines[1+arrIndex];
								this.creatorLine = metaLineStart+3;
								this.creator = metaLines[2+arrIndex];
								titleEndLine = metaLineStart+3;
							} else {
								this.creatorLine = metaLineStart+1;
								this.creator = metaLines[1+arrIndex];
								titleEndLine = metaLineStart+1;
							}
						}
					} else {
						this.creatorLine = metaLineStart;
						this.creator = metaLines[0+arrIndex];
						if (titleType.hasTitle()) {
							this.titleLine = metaLineStart+1;
							this.title = metaLines[1+arrIndex];
						}
						titleEndLine = metaLineStart+1;
					}
					break;
				case 1: //表題のみ または 表題 空行 著者名 空行 も許可 TODO 章番号とかは除外する
					if (titleType.titleFirst()) {
						this.titleLine = metaLineStart;
						this.title = metaLines[0+arrIndex];
						titleEndLine = metaLineStart;
						if (titleType.hasAuthor()) {
							if (metaLines[2+arrIndex] != null && metaLines[2+arrIndex].length() > 0 && (metaLines[3+arrIndex] == null || metaLines[3+arrIndex].length() == 0)) {
								this.creatorLine = metaLineStart+2;
								this.creator = metaLines[2+arrIndex];
								titleEndLine = metaLineStart+2;
							}
						}
					} else {
						this.creatorLine = metaLineStart;
						this.creator = metaLines[0+arrIndex];
						titleEndLine = metaLineStart;
						if (titleType.hasTitle()) {
							if (metaLines[2+arrIndex] != null && metaLines[2+arrIndex].length() > 0 && (metaLines[3+arrIndex] == null || metaLines[3+arrIndex].length() == 0)) {
								this.titleLine = metaLineStart+2;
								this.title = metaLines[2+arrIndex];
								titleEndLine = metaLineStart+2;
							}
						}
					}
					break;
				}
			}
			
			if (this.creator != null && (this.creator.startsWith("―") || this.creator.startsWith("【"))) this.creator = null;
			
			if (this.title != null) {
				this.title = CharUtils.getChapterName(CharUtils.removeRuby(this.title), 0, false);
			}
			if (this.creator != null) this.creator = CharUtils.getChapterName(CharUtils.removeRuby(this.creator), 0);
		}
	}
	/** 本文内のタイトル再読み込み */
	public void reloadMetadata(TitleType titleType, boolean pubFirst)
	{
		setMetaInfo(titleType, pubFirst, this.metaLines, this.metaLineStart, this.firstCommentLineNum);
	}
	
	/** ファイル名からタイトルと著者名を取得 */
	static public String[] getFileTitleCreator(String fileName)
	{
		//ファイル名からタイトル取得
		String[] titleCreator = new String[2];
		String noExtName = fileName.replaceAll("\\.([A-Z]|[a-z]|[0-9])+$", "").replaceAll("\\.([A-Z]|[a-z]|[0-9])+$", "");
		//後ろの括弧から校正情報等を除外
		noExtName = noExtName.replaceAll("（","\\(").replaceAll("）","\\)");
		noExtName = noExtName.replaceAll("\\(青空[^\\)]*\\)", "");
		noExtName = noExtName.replaceAll("\\([^\\)]*(校正|軽量|表紙|挿絵|補正|修正|ルビ|Rev|rev)[^\\)]*\\)", "");
		
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
	
	/** ファイルまたはURLの文字列から画像を読み込んで表紙イメージとして設定 */
	public void loadCoverImage(String path)
	{
		this.coverImage = ImageUtils.loadImage(path);
	}
}
