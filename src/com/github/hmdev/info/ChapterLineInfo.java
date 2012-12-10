package com.github.hmdev.info;

public class ChapterLineInfo
{
	/** 見出しの行番号 */
	public int lineNum;
	
	/** 見出し種別 */
	public int type;
	/** 見出しレベル 自動抽出は+10 */
	public int level;
	
	public boolean pageBreakChapter = false;
	
	/** 前の行が空行かどうか */
	public boolean emptyNext;
	
	/** 目次に使う文字列の開始位置 */
	String chapterName;
	
	public static final int TYPE_TITLE = 1;
	public static final int TYPE_PAGEBREAK = 2;
	public static final int TYPE_CHUKI = 10;
	public static final int TYPE_CHAPTER_NAME = 21;
	public static final int TYPE_CHAPTER_NUM = 22;
	public static final int TYPE_PATTERN = 30;
	
	public static final int LEVEL_H1 = 2;
	public static final int LEVEL_H2 = 3;
	public static final int LEVEL_H3 = 4;
	
	public ChapterLineInfo(int lineNum, int type, boolean pageBreak, int level, boolean emptyLineNext)
	{
		this(lineNum, type, pageBreak, level, emptyLineNext, null);
	}
	public ChapterLineInfo(int lineNum, int type, boolean pageBreak, int level, boolean emptyLineNext, String chapterName)
	{
		this.lineNum = lineNum;
		this.type = type;
		this.pageBreakChapter = pageBreak;
		this.level = level;
		this.emptyNext = emptyLineNext;
		this.chapterName = chapterName;
	}
	
	@Override
	public String toString()
	{
		return this.chapterName;
	}
	
	public String getTypeId()
	{
		switch (this.type) {
		case TYPE_TITLE: return "題";
		case TYPE_PAGEBREAK: return "";
		case TYPE_CHUKI: 
			if (this.level == LEVEL_H1) return "大"; 
			if (this.level == LEVEL_H2) return "中"; 
			if (this.level == LEVEL_H3) return "小"; 
			return "見";
		case TYPE_CHAPTER_NAME: return "章";
		//case TYPE_CHAPTER_NUM: return "数";
		case TYPE_PATTERN: return "他";
		}
		return "";
	}
	
	public String getChapterName()
	{
		return this.chapterName;
	}
	public void setChapterName(String chapterName)
	{
		this.chapterName = chapterName;
	}
	public void joinChapterName(String chapterName)
	{
		if (this.chapterName == null) this.chapterName = chapterName;
		else this.chapterName = this.chapterName+"　"+chapterName;
	}
}
