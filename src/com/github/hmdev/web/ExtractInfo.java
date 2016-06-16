package com.github.hmdev.web;

import java.util.regex.Pattern;

public class ExtractInfo
{
	enum ExtractId {
		COOKIE, PAGE_REGEX,
		SERIES, TITLE, AUTHOR, DESCRIPTION, COVER_IMG, COVER_HREF,
		PAGE_NUM, PAGE_URL,
		CHILD_NODE, HREF, HREF_REGEX, UPDATE, SUB_UPDATE, CONTENT_UPDATE_LIST, SUBTITLE_LIST,
		CONTENT_CHAPTER, CONTENT_SUBTITLE, CONTENT_IMG, CONTENT_UPDATE,
		CONTENT_ARTICLE, CONTENT_PREAMBLE, CONTENT_APPENDIX,
		CONTENT_ARTICLE_SEPARATOR,
		CONTENT_ARTICLE_START, CONTENT_ARTICLE_END,
		CONTENT_PREAMBLE_START, CONTENT_PREAMBLE_END,
		CONTENT_APPENDIX_START, CONTENT_APPENDIX_END;
	}

	/** JsoupのDocumentでselectするクエリ */
	public String query;
	
	/** 利用するelementsの位置 */
	public int[] idx;
	
	/** 抽出文字列パターン replacementがあれば置換する */
	Pattern pattern;
	
	/** 抽出パターンを置換する場合に設定 パターン内の()は$1に対応 */
	String replacement;
	
	public ExtractInfo(String queryString, Pattern patter, String replaceString)
	{
		String[] values = queryString.split(":");
		this.query = values[0];
		if (values.length > 1) {
			this.idx = new int [values.length-1];
			for (int i=0; i<this.idx.length; i++)
				this.idx[i] = Integer.parseInt(values[i+1]);
		}
		this.pattern = patter;
		//改行コード \n は改行文字に
		if (replaceString != null)
			this.replacement = replaceString.replaceAll("\\\\n", "\n");
	}
	
	public boolean hasPattern()
	{
		return this.pattern != null;
	}
	public boolean isReplacable()
	{
		return this.pattern != null && this.replacement != null;
	}
	
	public String replace(String str)
	{
		if (this.pattern == null || this.replacement == null) return str;
		return pattern.matcher(str).replaceAll(replacement);
	}
	
	public boolean matches(String str)
	{
		if (this.pattern == null) return false;
		return this.pattern.matcher(str).matches();
	}
	
}
