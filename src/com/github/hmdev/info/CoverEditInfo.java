package com.github.hmdev.info;

public class CoverEditInfo
{
	public int panelWidth;
	public int panelHeight;
	public int fitType;
	public double scale;
	public double offsetX;
	public double offsetY;
	public double visibleWidth;
	
	public CoverEditInfo(int panelWidth, int panelHeight, int fitType, double scale, double offsetX, double offsetY, double visibleWidth)
	{
		this.panelWidth = panelWidth;
		this.panelHeight = panelHeight;
		this.fitType = fitType;
		this.scale = scale;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.visibleWidth = visibleWidth;
	}
	
}
