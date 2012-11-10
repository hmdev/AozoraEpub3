package com.github.hmdev.info;

public class ChapterLineInfo
{
	/** 見出しレベル 自動抽出は+10 */
	public int level;
	
	/** 前の行が空行かどうか */
	public boolean emptyNext;
	
	/** 目次に使う文字列の開始位置 */
	public int nameStart = 0;

	public ChapterLineInfo(int level, boolean emptyLineNext)
	{
		this(level, emptyLineNext, 0);
	}
	public ChapterLineInfo(int level, boolean emptyLineNext, int nameStart)
	{
		this.level = level;
		this.emptyNext = emptyLineNext;
		this.nameStart = nameStart;
	}
}
