package com.github.hmdev.info;

/** 前回の変換時の履歴
 * 表示編集情報
 *  */
public class BookInfoHistory
{
	/** 表紙編集情報 */
	public CoverEditInfo coverEditInfo;
	/** 表紙ファイル名 フルパスかURL ""なら先頭の挿絵 nullなら表紙無し */
	public String coverFileName;
	/** 表紙に使う挿絵の本文内Index -1なら本文内の挿絵は使わない */
	public int coverImageIndex = -1;
	/** imageにしたとき用の元ファイルの拡張子 */
	public String coverExt = null;
	
	public String title;
	public String titleAs;
	public String creator;
	public String creatorAs;
	public String publisher;
	
	public BookInfoHistory(BookInfo bookInfo)
	{
		this.coverEditInfo = bookInfo.coverEditInfo;
		this.coverFileName = bookInfo.coverFileName;
		this.coverImageIndex = bookInfo.coverImageIndex;
		this.coverExt = bookInfo.coverExt;
		
		this.title = bookInfo.title;
		this.titleAs = bookInfo.titleAs;
		this.creator = bookInfo.creator;
		this.creatorAs = bookInfo.creatorAs;
		this.publisher = bookInfo.publisher;
	}
}
