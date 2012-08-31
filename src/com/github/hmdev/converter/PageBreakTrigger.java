/**
 * 改ページ情報
 */
package com.github.hmdev.converter;

public class PageBreakTrigger
{
	final static public int IMAGE_PAGE_NONE = 0;
	final static public int IMAGE_PAGE_W = 1;
	final static public int IMAGE_PAGE_H = 2;
	final static public int IMAGE_PAGE_AUTO = 10;
	
	/** 空のページは無視する */
	boolean ignoreEmptyPage = true;
	/** 次のページは左右中央 */
	boolean isMiddle = false;
	/** 次のページは画像 */
	int imagePageType = IMAGE_PAGE_NONE;
	
	/** 目次に出力しない */
	boolean noChapter = false;
	
	/** 画像単一ページの場合の画像ファイル名 */
	String imageFileName = null;
	
	/**
	 * @param ignoreEmptyPage
	 * @param isMiddle
	 * @param isImage 	 */
	public PageBreakTrigger(boolean ignoreEmptyPage, boolean isMiddle, int imagePageType)
	{
		this(ignoreEmptyPage, isMiddle, imagePageType, false);
	}
	public PageBreakTrigger(boolean ignoreEmptyPage, boolean isMiddle, int imagePageType, boolean noChapter)
	{
		super();
		this.ignoreEmptyPage = ignoreEmptyPage;
		this.isMiddle = isMiddle;
		this.imagePageType = imagePageType;
		this.noChapter = noChapter;
	}
}
