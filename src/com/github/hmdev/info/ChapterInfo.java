package com.github.hmdev.info;

/** 目次用の章の情報を格納（仮） */
public class ChapterInfo
{
	/** xhtmlファイルのセクション毎の連番ID */
	String sectionId;
	/** 章ID 見出し行につけたspanのID */
	String chapterId;
	/** 章名称 */
	String chapterName;
	/** 目次階層レベル */
	int chapterLevel;
	
	public ChapterInfo(String sectionId, String chapterId, String chapterName, int chapterLevel)
	{
		this.sectionId = sectionId;
		this.chapterId = chapterId;
		this.chapterName = chapterName;
		this.chapterLevel = chapterLevel;
	}

	public String getSectionId()
	{
		return sectionId;
	}
	public void setSectionId(String sectionId)
	{
		this.sectionId = sectionId;
	}

	public String getChapterId()
	{
		return chapterId;
	}
	public void setChapterId(String chapterId)
	{
		this.chapterId = chapterId;
	}

	public String getChapterName()
	{
		return chapterName;
	}
	public void setChapterName(String chapterName)
	{
		this.chapterName = chapterName;
	}
	
	public int getChapterLevel()
	{
		return chapterLevel;
	}
	public void setChapterLevel(int chapterLevel)
	{
		this.chapterLevel = chapterLevel;
	}
}
