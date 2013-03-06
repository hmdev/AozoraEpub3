package com.github.hmdev.info;

import com.github.hmdev.util.CharUtils;

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
	public int chapterLevel;
	
	/** 出力前に階層化開始タグを入れる回数 通常は1回 */
	public int levelStart = 0;
	/** 出力後に階層化終了タグを入れる回数 */
	public int levelEnd = 0;
	/** navPointを閉じる回数 */
	public int navClose = 1;
	
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
	public String getNoTagChapterName()
	{
		return CharUtils.removeTag(chapterName);
	}
	
	public int getChapterLevel()
	{
		return chapterLevel;
	}
	
	/** Velocityでループするために配列を返す */
	public int[] getLevelStart()
	{
		if (this.levelStart == 0) return null;
		return new int[this.levelStart];
	}
	/** Velocityでループするために配列を返す */
	public int[] getLevelEnd()
	{
		if (this.levelEnd == 0) return null;
		return new int[this.levelEnd];
	}
	/** Velocityでループするために配列を返す */
	public int[] getNavClose()
	{
		if (this.navClose <= 0) return null;
		return new int[this.navClose];
	}
}
