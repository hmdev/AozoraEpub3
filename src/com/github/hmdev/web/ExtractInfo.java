package com.github.hmdev.web;

public class ExtractInfo
{
	/** JsoupのDocumentでselectするクエリ */
	public String query;
	
	/** 利用するelementsの位置 */
	public int[] idx;
	
	public ExtractInfo(String queryString)
	{
		String[] values = queryString.split(":");
		this.query = values[0];
		if (values.length > 1) {
			this.idx = new int [values.length-1];
			for (int i=0; i<this.idx.length; i++)
				this.idx[i] = Integer.parseInt(values[i+1]);
		}
	}
}
