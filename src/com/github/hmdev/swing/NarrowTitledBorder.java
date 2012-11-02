package com.github.hmdev.swing;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.border.TitledBorder;

public class NarrowTitledBorder extends TitledBorder
{
	private static final long serialVersionUID = 1L;
	
	public NarrowTitledBorder(String title)
	{
		super(title);
	}
	
	@Override
	public Insets getBorderInsets(Component c)
	{
		return new Insets(16, 8, 8, 8);
	}
	
	@Override
	public Font getTitleFont()
	{
		Font font = super.getTitleFont();
		return new Font(font.getName(), font.getStyle(), font.getSize()-1);
	}
}
