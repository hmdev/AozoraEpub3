package com.github.hmdev.info;

/** セクションの情報 xhtmlに対応 */
public class SectionInfo
{
	/** セクションID 0001～ */
	public String sectionId;
	
	/** 画像のみのページ時にtrue */
	public boolean imagePage = false;
	/** 画像のみのページで幅に合わる場合にtrue */
	public boolean imageFitW = false;
	/** 画像のみのページで高さに合わる場合にtrue */
	public boolean imageFitH = false;
	
	/** ページ左右中央ならtrue */
	public boolean isMiddle = false;
	
	/** ページ左ならtrue */
	public boolean isBottom = false;
	
	/** セクション開始行 */
	public int startLine = 0;
	
	/** セクション終了行 */
	public int endLine = 0;
	
	public SectionInfo(String sectionId)
	{
		this.sectionId = sectionId;
	}
	
	public String getSectionId()
	{
		return sectionId;
	}
	public void setSectionId(String sectionId)
	{
		this.sectionId = sectionId;
	}

	public boolean isImagePage()
	{
		return imagePage;
	}
	public void setImagePage(boolean imagePage)
	{
		this.imagePage = imagePage;
	}

	public boolean isImageFitW()
	{
		return imageFitW;
	}
	public void setImageFitW(boolean imageFitW)
	{
		this.imageFitW = imageFitW;
	}

	public boolean isImageFitH()
	{
		return imageFitH;
	}
	public void setImageFitH(boolean imageFitH)
	{
		this.imageFitH = imageFitH;
	}

	public boolean isMiddle()
	{
		return isMiddle;
	}
	public void setMiddle(boolean isMiddle)
	{
		this.isMiddle = isMiddle;
	}

	public boolean isBottom()
	{
		return isBottom;
	}
	public void setBottom(boolean isBottom)
	{
		this.isBottom = isBottom;
	}

	public int getStartLine()
	{
		return startLine;
	}
	public void setStartLine(int startLine)
	{
		this.startLine = startLine;
	}

	public int getEndLine()
	{
		return endLine;
	}
	public void setEndLine(int endLine)
	{
		this.endLine = endLine;
	}
}
