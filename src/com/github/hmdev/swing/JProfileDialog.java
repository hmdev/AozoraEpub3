package com.github.hmdev.swing;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

/** プロファイル新規作成・編集ダイアログ */
public class JProfileDialog extends JDialog
{

	private static final long serialVersionUID = 1L;
	
	JTextField jTextProfileName;
	
	JButton jButtonCreate;
	JButton jButtonEdit;
	JButton jButtonDelete;
	JButton jButtonCancel;
	
	String orgName = "";
	
	public JProfileDialog(Image iconImage, String imageURLPath)
	{
		this.setIconImage(iconImage);
		this.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
		this.setSize(new Dimension(360, 128));
		this.setResizable(false);
		this.setTitle("プロファイル設定");
		this.setLayout(new GridLayout());
		
		JPanel panel;
		Border paddingButton = BorderFactory.createEmptyBorder(4, 8, 4, 8);
		Border padding4H = BorderFactory.createEmptyBorder(0, 4, 0, 4);
		
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
		outer.setBorder(BorderFactory.createEmptyBorder(3, 6, 0, 6));
		this.add(outer);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		outer.add(panel);
		panel.add(new JLabel("プロファイル名: "));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		outer.add(panel);
		jTextProfileName = new JTextField();
		jTextProfileName.setMinimumSize(new Dimension(100, 22));
		jTextProfileName.setPreferredSize(new Dimension(300, 22));
		jTextProfileName.setMaximumSize(new Dimension(480, 26));
		panel.add(jTextProfileName);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		outer.add(buttonPanel);
		
		jButtonCreate = new JButton("新規作成");
		jButtonCreate.setBorder(paddingButton);
		//jButtonCreate.setPreferredSize(new Dimension(80, 26));
		try { jButtonCreate.setIcon(new ImageIcon(new URL(imageURLPath+"add.png"))); } catch (MalformedURLException e1) {}
		jButtonCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (linstener != null) linstener.actionPerformed(new ActionEvent(jTextProfileName.getText(), 1, "create"));
				setVisible(false);
			}
		});
		panel = new JPanel();
		panel.setBorder(padding4H);
		panel.add(jButtonCreate);
		buttonPanel.add(panel);
		
		jButtonEdit = new JButton("名称変更");
		jButtonEdit.setBorder(paddingButton);
		//jButtonEdit.setPreferredSize(new Dimension(80, 26));
		try { jButtonEdit.setIcon(new ImageIcon(new URL(imageURLPath+"edit.png"))); } catch (MalformedURLException e1) {}
		jButtonEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (linstener != null) linstener.actionPerformed(new ActionEvent(jTextProfileName.getText(), 2, "edit"));
				setVisible(false);
			}
		});
		panel = new JPanel();
		panel.setBorder(padding4H);
		panel.add(jButtonEdit);
		buttonPanel.add(panel);
		
		jButtonDelete = new JButton("削除");
		jButtonDelete.setBorder(paddingButton);
		//jButtonDelete.setPreferredSize(new Dimension(80, 26));
		try { jButtonDelete.setIcon(new ImageIcon(new URL(imageURLPath+"delete.png"))); } catch (MalformedURLException e1) {}
		jButtonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (linstener != null) {
					int ret = JOptionPane.showConfirmDialog(jButtonDelete, orgName+" を削除しますか？", "プロファイル削除", JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						linstener.actionPerformed(new ActionEvent(jTextProfileName.getText(), 3, "delete"));
						setVisible(false);
					}
				}
			}
		});
		panel = new JPanel();
		panel.setBorder(padding4H);
		panel.add(jButtonDelete);
		buttonPanel.add(panel);
		
		jButtonCancel = new JButton("キャンセル");
		jButtonCancel.setBorder(paddingButton);
		//jButtonCancel.setPreferredSize(new Dimension(80, 26));
		try { jButtonCancel.setIcon(new ImageIcon(new URL(imageURLPath+"cross.png"))); } catch (MalformedURLException e1) {}
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jTextProfileName.setText("");
				setVisible(false);
			}
		});
		panel = new JPanel();
		panel.setBorder(padding4H);
		panel.add(jButtonCancel);
		buttonPanel.add(panel);
	}
	
	public void showCreate(Point location, String name)
	{
		this.jButtonCreate.setVisible(true);
		this.jButtonEdit.setVisible(false);
		this.jButtonDelete.setVisible(false);
		
		this.jTextProfileName.setText(name);
		this.setLocation(location.x+16, location.y+8);
		this.setVisible(true);
	}
	
	public void showEdit(Point location, String name, boolean deletable)
	{
		this.jButtonCreate.setVisible(false);
		this.jButtonEdit.setVisible(true);
		this.jButtonDelete.setVisible(true);
		this.jButtonDelete.setEnabled(deletable);
		
		this.orgName = name;
		this.jTextProfileName.setText(name);
		this.setLocation(location.x+48, location.y+8);
		this.setVisible(true);
	}
	
	////////////////////////////////////////////////////////////////
	ActionListener linstener;
	public void addActionListener(ActionListener linstener)
	{
		this.linstener = linstener;
	}
	
}
