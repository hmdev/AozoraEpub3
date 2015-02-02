package com.github.hmdev.info;

import java.io.File;

/** 外字フォントとクラス名を格納
 * toStringで名称を返す */
public class GaijiInfo
{
	String className;
	File gaijiFile;
	
	public GaijiInfo(String className, File gaijiFile)
	{
		this.className = className;
		this.gaijiFile = gaijiFile;
	}
	
	public String getFileName()
	{
		return gaijiFile.getName();
	}
	public File getFile()
	{
		return gaijiFile;
	}
	public void setFile(File gaijiFile)
	{
		this.gaijiFile = gaijiFile;
	}
	public String getClassName()
	{
		return className;
	}
	public void setClassName(String className)
	{
		this.className = className;
	}
}
