/**
 * 改ページ情報
 */
package com.github.hmdev.converter;

public class PageBreakFlag
{
	/** 空のページは無視する */
	boolean ignoreEmptyPage = true;
	/** 次のページは左右中央 */
	boolean isMiddle = false;
	/** 次のページは画像 */
	boolean isImage = false;
	
	/** 改ページの後ろに文字があれば改行を出力 */
	boolean noBr = true;
	/**
	 * @param ignoreEmptyPage
	 * @param isMiddle
	 * @param isImage 	 */
	public PageBreakFlag(boolean ignoreEmptyPage, boolean isMiddle, boolean isImage)
	{
		super();
		this.ignoreEmptyPage = ignoreEmptyPage;
		this.isMiddle = isMiddle;
		this.isImage = isImage;
	}
}
