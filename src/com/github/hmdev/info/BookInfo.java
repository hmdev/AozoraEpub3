package com.github.hmdev.info;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.github.hmdev.util.ImageInfoReader;

/** タイトル著作者等のメタ情報を格納 */
public class BookInfo
{
	/** タイトル記載種別 */
	public enum TitleType {
		TITLE_AUTHOR, AUTHOR_TITLE, SUBTITLE_AUTHOR, TITLE_ONLY, NONE;
		final static public String[] titleTypeNames = {"表題→著者名", "著者名→表題", "表題→著者名(副題優先)", "表題のみ", "なし"};
		static public TitleType indexOf(int idx)
		{
			return values()[idx];
		}
		boolean hasTitleAuthor() {
			switch (this) {
			case TITLE_AUTHOR: case SUBTITLE_AUTHOR: case AUTHOR_TITLE: return true;
			default: return false;
			}
		}
		boolean hasTitle() {
			switch (this) {
			case TITLE_AUTHOR: case SUBTITLE_AUTHOR: case AUTHOR_TITLE: case TITLE_ONLY: return true;
			default: return false;
			}
		}
		boolean hasAuthor() {
			switch (this) {
			case TITLE_AUTHOR: case SUBTITLE_AUTHOR: case AUTHOR_TITLE: return true;
			default: return false;
			}
		}
		boolean titleFirst() {
			switch (this) {
			case TITLE_AUTHOR: case SUBTITLE_AUTHOR: case TITLE_ONLY: return true;
			default: return false;
			}
		}
	}
	
	/** テキストの行数 */
	public int totalLineNum = -1;
	
	/** タイトル */
	public String title;
	/** タイトル行番号 */
	public int titleLine = -1;
	
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
	
	/** タイトル行の最後 */
	public int titleEndLine = -1;
	
	/** 発刊日時 */
	public Date published;
	/** 更新日時 */
	public Date modified;
	
	/** 縦書きならtrue */
	public boolean vertical = true;
	
	/** 右から左ならtrue */
	public boolean rtl = false;
	
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
	
	/** txtのない画像のみの場合 */
	public boolean imageOnly = false;
	
	/** タイトルページの改ページ行 前に改ページがなければ0 表題がなければ-1 */
	public int preTitlePageBreak = -1;
	
	/** 圧縮ファイル内のテキストファイルエントリー名 */
	public String textEntryName = null;
	
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
	public BookInfo()
	{
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
	public void addChapterLine(int lineNum, ChapterLineInfo chapterLineInfo)
	{
		if (this.mapChapterLine == null) this.mapChapterLine = new HashMap<Integer, ChapterLineInfo>();
		this.mapChapterLine.put(lineNum, chapterLineInfo);
	}
	/** 見出し行と削除 */
	public void removeChapterLine(int lineNum)
	{
		if (this.mapChapterLine != null) this.mapChapterLine.remove(lineNum);
	}
	/** 見出し行なら目次階層レベルを返す */
	public int getChapterLevel(int lineNum)
	{
		if (this.mapChapterLine == null) return 0;
		ChapterLineInfo info = this.mapChapterLine.get(lineNum);
		return info == null ? 0 : info.level;
	}
	
	/** 目次ページの自動抽出見出しを除外 */
	public void excludeTocChapter()
	{
		if (this.mapChapterLine == null) return;
		//前2行と後ろ2行が自動抽出見出しの行を抽出 間の行は空行のみ許可
		HashSet<Integer> excludeLine = new HashSet<Integer>();
		for (Integer lineNum : this.mapChapterLine.keySet()) {
			if (this.getChapterLevel(lineNum) >= 10) {
				boolean prevIsPattern = false;
				if (this.getChapterLevel(lineNum-1) >= 10) prevIsPattern = true;
				else if (this.mapChapterLine.get(lineNum).emptyNext && this.getChapterLevel(lineNum-2) >= 10) prevIsPattern = true; //前が空行の場合のみ
				boolean nextIsPattern = false;
				if (this.getChapterLevel(lineNum+1) >= 10) nextIsPattern = true;
				else if (this.getChapterLevel(lineNum+2) >= 10) nextIsPattern = true;
				if (prevIsPattern && nextIsPattern) excludeLine.add(lineNum);
			}
		}
		//先頭と最後
		HashSet<Integer> excludeLine2 = new HashSet<Integer>();
		for (Integer lineNum : this.mapChapterLine.keySet()) {
			if (!excludeLine.contains(lineNum) && this.getChapterLevel(lineNum) >= 10) {
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
	/** 先頭行から表題と著者を取得 */
	public void setMetaInfo(TitleType titleType, String[] firstLines, int firstLineStart, int firstCommentLineNum, int preTitlePageBreak)
	{
		if (titleType != TitleType.NONE) {
			//バッファからタイトルと著者取得
			//連続行数取得
			int linesLength = 0;
			for (int i=0; i<firstLines.length; i++) {
				if (firstLines[i] == null || firstLines[i].length() == 0) {
					linesLength = i; break;
				}
			}
			
			if (linesLength > 0) this.preTitlePageBreak = preTitlePageBreak;
			
			//表題のみ
			if (linesLength > 0 && titleType == TitleType.TITLE_ONLY) {
				this.titleLine = firstLineStart;
				this.title = firstLines[0];
				titleEndLine = firstLineStart;
				return;
			}
			
			switch (linesLength) {
			case 6:
				if (titleType.titleFirst()) {
					this.titleLine = firstLineStart;
					this.orgTitleLine = firstLineStart+1;
					this.subTitleLine = firstLineStart+2;
					this.subOrgTitleLine = firstLineStart+3;
					this.title = firstLines[0]+" "+firstLines[2];
					titleEndLine = firstLineStart+3;
					if (titleType.hasAuthor()) {
						this.creatorLine = firstLineStart+4;
						this.subCreatorLine = firstLineStart+5;
						this.creator = firstLines[4];
						titleEndLine = firstLineStart+5;
					}
				} else {
					this.creatorLine = firstLineStart;
					this.subCreatorLine = firstLineStart+1;
					this.creator = firstLines[0];
					titleEndLine = firstLineStart+1;
					if (titleType.hasTitle()) {
						this.titleLine = firstLineStart+2;
						this.orgTitleLine = firstLineStart+3;
						this.subTitleLine = firstLineStart+4;
						this.subOrgTitleLine = firstLineStart+5;
						this.title = firstLines[2]+" "+firstLines[4];
						titleEndLine = firstLineStart+5;
					}
				}
				break;
			case 5:
				if (titleType.titleFirst()) {
					this.titleLine = firstLineStart;
					this.orgTitleLine = firstLineStart+1;
					this.subTitleLine = firstLineStart+2;
					this.title = firstLines[0]+" "+firstLines[2];
					titleEndLine = firstLineStart+2;
					if (titleType.hasAuthor()) {
						this.creatorLine = firstLineStart+3;
						this.subCreatorLine = firstLineStart+4;
						this.creator = firstLines[3];
						titleEndLine = firstLineStart+4;
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					titleEndLine = firstLineStart;
					if (titleType.hasTitle()) {
						this.titleLine = firstLineStart+1;
						this.orgTitleLine = firstLineStart+2;
						this.subTitleLine = firstLineStart+3;
						this.subOrgTitleLine = firstLineStart+4;
						this.title = firstLines[1]+" "+firstLines[3];
					}
					titleEndLine = firstLineStart+4;
				}
				break;
			case 4:
				if (titleType.titleFirst()) {
					this.titleLine = firstLineStart;
					this.subTitleLine = firstLineStart+1;
					this.title = firstLines[0]+" "+firstLines[1];
					titleEndLine = firstLineStart+1;
					if (titleType.hasAuthor()) {
						this.creatorLine = firstLineStart+2;
						this.subCreatorLine = firstLineStart+3;
						this.creator = firstLines[2];
						titleEndLine = firstLineStart+3;
					}
				} else {
					this.creatorLine = firstLineStart;
					this.subCreatorLine = firstLineStart+1;
					this.creator = firstLines[0];
					titleEndLine = firstLineStart+1;
					if (titleType.hasTitle()) {
						this.titleLine = firstLineStart+2;
						this.subTitleLine = firstLineStart+3;
						this.title = firstLines[2]+" "+firstLines[3];
						titleEndLine = firstLineStart+3;
					}
				}
				break;
			case 3: //表題+副題+著者 または 表題+著者+翻訳者
				if (titleType.titleFirst()) {
					this.titleLine = firstLineStart;
					this.subTitleLine = firstLineStart+1;
					this.title = firstLines[0]+" "+firstLines[1];
					titleEndLine = firstLineStart+1;
					if (titleType.hasAuthor()) {
						//副著者を文字列で判断
						if (titleType != TitleType.SUBTITLE_AUTHOR && !firstLines[1].startsWith("―") &&
							(firstLines[2].endsWith("訳") || firstLines[2].endsWith("編纂") || firstLines[2].endsWith("校訂"))) {
							this.titleLine = firstLineStart;
							this.title = firstLines[0];
							this.subTitleLine = -1;
							this.creatorLine = firstLineStart+1;
							this.creator = firstLines[1];
							this.subCreatorLine = firstLineStart+2;
						} else {
							this.creatorLine = firstLineStart+2;
							this.creator = firstLines[2];
						}
						titleEndLine = firstLineStart+2;
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					titleEndLine = firstLineStart;
					if (titleType.hasTitle()) {
						this.titleLine = firstLineStart+1;
						this.subTitleLine = firstLineStart+2;
						this.title = firstLines[1]+" "+firstLines[2];
						titleEndLine = firstLineStart+2;
					}
				}
				break;
			case 2: //表題+著者 すぐ後にコメント行がある場合のみ表題+副題+空行+著者
				if (titleType.titleFirst()) {
					this.titleLine = firstLineStart;
					this.title = firstLines[0];
					if (titleType.hasAuthor()) {
						if (firstCommentLineNum > 0 && firstCommentLineNum <= 6 && firstLines[3] != null && firstLines[3].length() > 0 && (firstLines[4] == null || firstLines[4].length() == 0)) {
							this.titleLine = firstLineStart;
							this.subTitleLine = firstLineStart+1;
							this.title = firstLines[0]+" "+firstLines[1];
							this.creatorLine = firstLineStart+3;
							this.creator = firstLines[3];
							titleEndLine = firstLineStart+3;
						} else {
							this.creatorLine = firstLineStart+1;
							this.creator = firstLines[1];
							titleEndLine = firstLineStart+1;
						}
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					if (titleType.hasTitle()) {
						this.titleLine = firstLineStart+1;
						this.title = firstLines[1];
					}
					titleEndLine = firstLineStart+1;
				}
				break;
			case 1: //表題のみ または 表題 空行 著者名 空行 も許可 TODO 章番号とかは除外する
				if (titleType.titleFirst()) {
					this.titleLine = firstLineStart;
					this.title = firstLines[0];
					titleEndLine = firstLineStart;
					if (titleType.hasAuthor()) {
						if (firstLines[2] != null && firstLines[2].length() > 0 && (firstLines[3] == null || firstLines[3].length() == 0)) {
							this.creatorLine = firstLineStart+2;
							this.creator = firstLines[2];
							titleEndLine = firstLineStart+2;
						}
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					titleEndLine = firstLineStart;
					if (titleType.hasTitle()) {
						if (firstLines[2] != null && firstLines[2].length() > 0 && (firstLines[3] == null || firstLines[3].length() == 0)) {
							this.titleLine = firstLineStart+2;
							this.title = firstLines[2];
							titleEndLine = firstLineStart+2;
						}
					}
				}
				break;
			}
			
			if (this.creator != null && (this.creator.startsWith("―") || this.creator.startsWith("【"))) this.creator = null;
		}
	}
	
	/** ファイルまたはURLの文字列から画像を読み込んで表紙イメージとして設定 */
	public void loadCoverImage(String path)
	{
		this.coverImage = ImageInfoReader.loadImage(path);
	}
}
