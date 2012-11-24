package com.github.hmdev.info;

public class ChapterLineInfo
{
	public int lineNum;
	
	/** 見出しレベル 自動抽出は+10 */
	public int level;
	
	/** 前の行が空行かどうか */
	public boolean emptyNext;
	
	/** 目次に使う文字列の開始位置 */
	String chapterName;
	
	/** 改ページ後の先頭ならtrue で目次にアンカー追加しない */
	public boolean sectionChapter;

	public ChapterLineInfo(int lineNum, int level, boolean emptyLineNext)
	{
		this(lineNum, level, emptyLineNext, null);
	}
	public ChapterLineInfo(int lineNum, int level, boolean emptyLineNext, String chapterName)
	{
		this(lineNum, level, emptyLineNext, chapterName, false);
	}
	public ChapterLineInfo(int lineNum, int level, boolean emptyLineNext, String chapterName, boolean sectionChapter)
	{
		this.lineNum = lineNum;
		this.level = level;
		this.emptyNext = emptyLineNext;
		this.chapterName = chapterName;
		this.sectionChapter = sectionChapter;
	}
	
	@Override
	public String toString()
	{
		return this.chapterName;
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
