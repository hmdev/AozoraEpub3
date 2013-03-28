package com.github.hmdev.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.github.hmdev.info.ChapterLineInfo;

public class JTocTable extends JTable
{
	private static final long serialVersionUID = 1L;
	
	TocTableDataModel model = null;
	
	public JTocTable()
	{
		super();
		this.model = new TocTableDataModel(new String[]{"", "", "", "行", "目次名称"}, 0, this);
		super.setModel(this.model);
		TableColumnModel columnModel = this.getColumnModel();
		columnModel.getColumn(0).setMaxWidth(22);
		columnModel.getColumn(1).setMaxWidth(30);
		columnModel.getColumn(1).setPreferredWidth(20);
		columnModel.getColumn(2).setMaxWidth(30);
		columnModel.getColumn(2).setPreferredWidth(20);
		columnModel.getColumn(3).setMaxWidth(60);
		columnModel.getColumn(3).setPreferredWidth(35);
		
		this.model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e)
			{
				model.table.repaint();
			}
		});
	}
	
	@Override
	public TocTableDataModel getModel()
	{
		return this.model;
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component component = super.prepareRenderer(renderer, row, column);
		if (this.model.isSelected(row)) component.setForeground(Color.BLACK);
		else component.setForeground(Color.LIGHT_GRAY);
		return component;
	}
	
	public class TocTableDataModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;
		
		JTocTable table;
		
		TocTableDataModel(String[] columnNames, int rowNum, JTocTable table){
			super(columnNames, rowNum);
			this.table = table;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
			switch (col) {
			case 0: return Boolean.class;
			case 3: return Integer.class;
			}
			return String.class;
		}
		@Override
		public boolean isCellEditable(int row, int col)
		{
			switch (col) {
			case 0:
			case 4:
				return true;
			}
			return false;
		}
		
		public void setSelected(int row, boolean select)
		{
			this.setValueAt(select, row, 0);
			table.repaint();
		}
		public boolean isSelected(int row)
		{
			return (Boolean)this.getValueAt(row, 0);
		}
		public boolean isPageBreak(int row)
		{
			return "改".equals((String)this.getValueAt(row, 1));
		}
		public int getChapterType(int row)
		{
			String value = (String)this.getValueAt(row, 2);
			if (value.length() == 0) return 0;
			return ChapterLineInfo.getChapterType(value.charAt(0));
		}
		public int getLineNum(int row)
		{
			return (Integer)this.getValueAt(row, 3);
		}
		public String getTocName(int row)
		{
			return (String)this.getValueAt(row, 4);
		}
	}	
}
