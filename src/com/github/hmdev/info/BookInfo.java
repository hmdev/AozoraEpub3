package com.github.hmdev.info;
import java.util.Date;
import java.util.HashSet;

/** タイトル著作者等のメタ情報を格納 */
public class BookInfo
{
	/** タイトル記載種別 */
	//final static public String[] titleType = {"表題+著者名", "著者名＋表題", "表題のみ", "なし", "ファイル名から"};
	public enum TitleType {
		TITLE_AUTHOR, AUTHOR_TITLE, TITLE_ONLY, NONE;
		final static public String[] titleTypeNames = {"表題＋著者名", "著者名＋表題", "表題のみ", "なし"};
		boolean hasTitleAuthor() {
			switch (this) {
			case TITLE_AUTHOR:
			case AUTHOR_TITLE:
			case TITLE_ONLY:
				return true;
			default:
				return false;
			}
		}
	}
	
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
	
	/** 先頭に表紙ページを追加 */
	public boolean insertCoverPage = false;
	
	/** 先頭がページの左右中央ならtrue */
	public boolean startMiddle = false; 
	
	/** txtのない画像のみの場合 */
	public boolean imageOnly = false;
	
	/** 改ページ単位で区切られたセクションの情報を格納 */
	//Vector<SectionInfo> vecSectionInfo;
	
	/** 画像単体ページ開始行 */
	HashSet<Integer> mapImageSectionLine;
	
	/** 改行ページしない行 (［＃ページの左右中央］の前の［＃改ページ］) */
	HashSet<Integer> mapNoPageBreakLine;
	
	/** 出力ページしない行 (左右中央後の空行と改ページ前の空行) */
	HashSet<Integer> mapIgnoreLine;
	
	////////////////////////////////////////////////////////////////
	public BookInfo()
	{
		//this.vecSectionInfo = new Vector<SectionInfo>();
		this.mapImageSectionLine = new HashSet<Integer>();
		this.mapNoPageBreakLine = new HashSet<Integer>();
		this.mapIgnoreLine = new HashSet<Integer>();
		
		this.modified = new Date();
	}
	
	public void clear()
	{
		this.mapImageSectionLine.clear();
		this.mapNoPageBreakLine.clear();
		this.mapIgnoreLine.clear();
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
	public void addImageSectionLine(int lineNum)
	{
		this.mapImageSectionLine.add(lineNum);
	}
	/** 画像単体ページの開始行ならtrue */
	public boolean isImageSectionLine(int lineNum)
	{
		return this.mapImageSectionLine.contains(lineNum);
	}
	
	/** 改ページしない行数を保存 */
	public void addNoPageBreakLine(int lineNum)
	{
		this.mapNoPageBreakLine.add(lineNum);
	}
	/** 改ページしない行ならtrue */
	public boolean isNoPageBreakLine(int lineNum)
	{
		return this.mapNoPageBreakLine.contains(lineNum);
	}
	
	/** 出力しない行数を保存 */
	public void addIgnoreLine(int lineNum)
	{
		this.mapIgnoreLine.add(lineNum);
	}
	/** 出力しない行ならtrue */
	public boolean isIgnoreLine(int lineNum)
	{
		return this.mapIgnoreLine.contains(lineNum);
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

	public void setInsertCoverPage(boolean insertCoverPage)
	{
		this.insertCoverPage = insertCoverPage;
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
	public void setMetaInfo(TitleType titleType, String[] firstLines, int firstLineStart, int firstCommentLineNum)
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
			boolean hasTitle = titleType==TitleType.TITLE_AUTHOR || titleType==TitleType.TITLE_ONLY || titleType==TitleType.AUTHOR_TITLE;
			boolean hasAuthor = titleType==TitleType.TITLE_AUTHOR || titleType==TitleType.AUTHOR_TITLE;
			boolean titleFirst = titleType==TitleType.TITLE_AUTHOR || titleType==TitleType.TITLE_ONLY;
			
			switch (linesLength) {
			case 6:
				if (titleFirst) {
					this.titleLine = firstLineStart;
					this.orgTitleLine = firstLineStart+1;
					this.subTitleLine = firstLineStart+2;
					this.subOrgTitleLine = firstLineStart+3;
					this.title = firstLines[0]+" "+firstLines[2];
					if (hasAuthor) {
						this.creatorLine = firstLineStart+4;
						this.subCreatorLine = firstLineStart+5;
						this.creator = firstLines[4];
					}
				} else {
					this.creatorLine = firstLineStart;
					this.subCreatorLine = firstLineStart+1;
					this.creator = firstLines[0];
					if (hasTitle) {
						this.titleLine = firstLineStart+2;
						this.orgTitleLine = firstLineStart+3;
						this.subTitleLine = firstLineStart+4;
						this.subOrgTitleLine = firstLineStart+5;
						this.title = firstLines[2]+" "+firstLines[4];
					}
				}
				break;
			case 5:
				if (titleFirst) {
					this.titleLine = firstLineStart;
					this.orgTitleLine = firstLineStart+1;
					this.subTitleLine = firstLineStart+2;
					this.title = firstLines[0]+" "+firstLines[2];
					if (hasAuthor) {
						this.creatorLine = firstLineStart+3;
						this.subCreatorLine = firstLineStart+4;
						this.creator = firstLines[3];
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					if (hasTitle) {
						this.titleLine = firstLineStart+1;
						this.orgTitleLine = firstLineStart+2;
						this.subTitleLine = firstLineStart+3;
						this.subOrgTitleLine = firstLineStart+4;
						this.title = firstLines[1]+" "+firstLines[3];
					}
				}
				break;
			case 4:
				if (titleFirst) {
					this.titleLine = firstLineStart;
					this.subTitleLine = firstLineStart+1;
					this.title = firstLines[0]+" "+firstLines[1];
					if (hasAuthor) {
						this.creatorLine = firstLineStart+2;
						this.subCreatorLine = firstLineStart+3;
						this.creator = firstLines[2];
					}
				} else {
					this.creatorLine = firstLineStart;
					this.subCreatorLine = firstLineStart+1;
					this.creator = firstLines[0];
					if (hasTitle) {
						this.titleLine = firstLineStart+2;
						this.subTitleLine = firstLineStart+3;
						this.title = firstLines[2]+" "+firstLines[3];
					}
				}
				break;
			case 3:
				if (titleFirst) {
					this.titleLine = firstLineStart;
					this.subTitleLine = firstLineStart+1;
					this.title = firstLines[0]+" "+firstLines[1];
					if (hasAuthor) {
						this.creatorLine = firstLineStart+2;
						this.creator = firstLines[2];
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					if (hasTitle) {
						this.titleLine = firstLineStart+1;
						this.subTitleLine = firstLineStart+2;
						this.title = firstLines[1]+" "+firstLines[2];
					}
				}
				break;
			case 2: //表題+著者 すぐ後にコメント行がある場合のみ表題+副題+空行+著者
				if (titleFirst) {
					this.titleLine = firstLineStart;
					this.title = firstLines[0];
					if (hasAuthor) {
						if (firstCommentLineNum > 0 && firstCommentLineNum <= 6 && firstLines[3] != null && firstLines[3].length() > 0 && (firstLines[4] == null || firstLines[4].length() == 0)) {
							this.titleLine = firstLineStart;
							this.subTitleLine = firstLineStart+1;
							this.title = firstLines[0]+" "+firstLines[1];
							this.creatorLine = firstLineStart+3;
							this.creator = firstLines[3];
						} else {
							this.creatorLine = firstLineStart+1;
							this.creator = firstLines[1];
						}
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					if (hasTitle) {
						this.titleLine = firstLineStart+1;
						this.title = firstLines[1];
					}
				}
				break;
			case 1: //表題 空行 著者名 空行 も許可
				if (titleFirst) {
					this.titleLine = firstLineStart;
					this.title = firstLines[0];
					if (hasAuthor) {
						if (firstLines[2] != null && firstLines[2].length() > 0 && (firstLines[3] == null || firstLines[3].length() == 0)) {
							this.creatorLine = firstLineStart+2;
							this.creator = firstLines[2];
						}
					}
				} else {
					this.creatorLine = firstLineStart;
					this.creator = firstLines[0];
					if (hasTitle) {
						if (firstLines[2] != null && firstLines[2].length() > 0 && (firstLines[3] == null || firstLines[3].length() == 0)) {
							this.titleLine = firstLineStart+2;
							this.title = firstLines[2];
						}
					}
				}
				break;
			}
			
			if (this.creator != null && (this.creator.startsWith("―") || this.creator.startsWith("【"))) this.creator = null;
		}
	}
}
