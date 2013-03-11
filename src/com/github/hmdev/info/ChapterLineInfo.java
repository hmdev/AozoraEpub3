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
	public static final int TYPE_CHUKI_H = 10;
	public static final int TYPE_CHUKI_H1 = 11;
	public static final int TYPE_CHUKI_H2 = 12;
	public static final int TYPE_CHUKI_H3 = 13;
	public static final int TYPE_CHAPTER_NAME = 21;
	public static final int TYPE_CHAPTER_NUM = 22;
	public static final int TYPE_PATTERN = 30;
	
	public static final int LEVEL_TITLE = 0;
	public static final int LEVEL_SECTION = 1;
	public static final int LEVEL_H1 = 1;
	public static final int LEVEL_H2 = 2;
	public static final int LEVEL_H3 = 3;
	
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
		case TYPE_PAGEBREAK: return ""; //一覧で表示しない
		case TYPE_CHUKI_H: return "見";
		case TYPE_CHUKI_H1: return "大";
		case TYPE_CHUKI_H2: return "中";
		case TYPE_CHUKI_H3: return "小";
		case TYPE_CHAPTER_NAME: return "章";
		case TYPE_CHAPTER_NUM: return "数";
		case TYPE_PATTERN: return "他";
		}
		return "";
	}
	static public int getChapterType(char typeId)
	{
		switch (typeId) {
		case '題': return TYPE_TITLE;
		case '改': return TYPE_PAGEBREAK;
		case '見': return TYPE_CHUKI_H; 
		case '大': return TYPE_CHUKI_H1; 
		case '中': return TYPE_CHUKI_H2; 
		case '小': return TYPE_CHUKI_H3; 
		case '章': return TYPE_CHAPTER_NAME;
		case '数': return TYPE_CHAPTER_NUM;
		case '他': return TYPE_PATTERN;
		}
		return 0;
	}
	static public int getLevel(int type)
	{
		switch (type) {
		case TYPE_TITLE: return LEVEL_TITLE;
		case TYPE_PAGEBREAK: return LEVEL_SECTION;
		case TYPE_CHUKI_H1: return LEVEL_H1;
		case TYPE_CHUKI_H2: return LEVEL_H2;
		case TYPE_CHUKI_H3: return LEVEL_H3;
		case TYPE_CHAPTER_NUM: return LEVEL_H2;
		}
		return LEVEL_H1;
	}
	
	/** 章名や数字やパターンでマッチした行ならtrue */
	public boolean isPattern()
	{
		switch (type) {
		case TYPE_TITLE:
		case TYPE_PAGEBREAK:
		case TYPE_CHUKI_H1:
		case TYPE_CHUKI_H2:
		case TYPE_CHUKI_H3: return false;
		}
		return true;
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
