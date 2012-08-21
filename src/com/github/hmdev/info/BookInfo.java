package com.github.hmdev.info;
import java.util.Date;
import java.util.HashSet;

/** タイトル著作者等のメタ情報を格納 */
public class BookInfo
{
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

	
}
