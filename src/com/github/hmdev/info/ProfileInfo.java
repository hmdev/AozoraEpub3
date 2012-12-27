package com.github.hmdev.info;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** iniファイルの設定を格納するクラス
 * toStringで名称を返す */
public class ProfileInfo
{
	String name;
	String fileName;
	Properties props;
	
	public ProfileInfo(String fileName, String name, Properties props)
	{
		this.fileName = fileName;
		this.name = name;
		this.props = props;
	}
	
	/** 更新 */
	public void update(File profilePath) throws FileNotFoundException, IOException
	{
		if (fileName != null) {
			FileOutputStream fos = new FileOutputStream(profilePath+"/"+fileName);
			this.props.store(fos, "AozoraEpub3 Profile");
			fos.close();
		}
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public Properties getProperties()
	{
		return props;
	}
	public void setProps(Properties props)
	{
		this.props = props;
	}
}
