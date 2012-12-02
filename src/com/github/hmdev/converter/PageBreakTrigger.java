/**
 * 改ページ情報
 */
package com.github.hmdev.converter;

public class PageBreakTrigger
{
	final static public int IMAGE_PAGE_NONE = 0;
	final static public int IMAGE_PAGE_W = 1;
	final static public int IMAGE_PAGE_H = 2;
	final static public int IMAGE_PAGE_NOFIT = 5;
	final static public int IMAGE_PAGE_AUTO = 10;
	
	final static public int IMAGE_PAGE_TOP = 20;
	final static public int IMAGE_PAGE_BOTTOM = 21;
	
	final static public int PAGE_NORMAL = 0;
	final static public int PAGE_MIDDLE = 1;
	final static public int PAGE_BOTTOM = 2;
	
	/** 空のページは無視する */
	boolean ignoreEmptyPage = true;
	/** 次のページは左右中央 */
	int pageType = PAGE_NORMAL;
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
	public PageBreakTrigger(boolean ignoreEmptyPage, int pageType, int imagePageType)
	{
		this(ignoreEmptyPage, pageType, imagePageType, false);
	}
	public PageBreakTrigger(boolean ignoreEmptyPage, int pageType, int imagePageType, boolean noChapter)
	{
		super();
		this.ignoreEmptyPage = ignoreEmptyPage;
		this.pageType = pageType;
		this.imagePageType = imagePageType;
		this.noChapter = noChapter;
	}
}
