package com.github.hmdev.swing;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import com.github.hmdev.info.BookInfo;
import com.github.hmdev.util.ImageInfoReader;

/**
 * 変換前確認ダイアログ
 */
public class JConfirmDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	/** キャンセルボタンが押されたらtrue */
	public boolean canceled = false;
	
	/** 入力ファイル名 */
	public JTextField jTextSrcFileName;
	/** 出力ファイル名 */
	public JTextField jTextDstFileName;
	/** 表題 (+副題)編集用 */
	public JTextField jTextTitle;
	/** 著者名編集用 */
	public JTextField jTextCreator;
	/** プレビューパネル */
	public JCoverImagePanel jCoverImagePanel;
	
	/** 変更前確認チェック */
	public JCheckBox jCheckConfirm2;
	
	public JConfirmDialog(final JApplet applet, Image iconImage, URL replaceImage, URL applyImage, URL cancelImage)
	{
		JButton jButton;
		JPanel panel;
		Border padding0 = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border padding4 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		Border titlePadding4 = BorderFactory.createEmptyBorder(0, 4, 4, 4);
		
		this.setIconImage(iconImage);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setSize(new Dimension(580, 280));
		this.setResizable(false);
		this.setTitle("変換前確認");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) { canceled = true; }
		});
		
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		dialogPanel.setBorder(padding4);
		this.add(dialogPanel);
		
		//入出力ファイル情報とプレビューを横に並べる
		JPanel previewOuter = new JPanel();
		previewOuter.setLayout(new BoxLayout(previewOuter, BoxLayout.X_AXIS));
		dialogPanel.add(previewOuter);
		JPanel previewLeft = new JPanel();
		previewLeft.setLayout(new BoxLayout(previewLeft, BoxLayout.Y_AXIS));
		previewLeft.setBorder(padding4);
		previewOuter.add(previewLeft);
		
		JPanel inputOuter = new JPanel();
		inputOuter.setBorder(BorderFactory.createTitledBorder("入力ファイル"));
		inputOuter.setLayout(new BoxLayout(inputOuter, BoxLayout.X_AXIS));
		inputOuter.setPreferredSize(new Dimension(380, 54));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(titlePadding4);
		panel.setPreferredSize(new Dimension(360, 26));
		jTextSrcFileName = new JTextField();
		jTextSrcFileName.setEditable(false);
		jTextSrcFileName.setMaximumSize(new Dimension(440, 24));
		jTextSrcFileName.setPreferredSize(new Dimension(440, 24));
		panel.add(jTextSrcFileName);
		inputOuter.add(panel);
		previewLeft.add(inputOuter);
		
		JPanel outputOuter = new JPanel();
		outputOuter.setBorder(BorderFactory.createTitledBorder("出力パス"));
		outputOuter.setLayout(new BoxLayout(outputOuter, BoxLayout.X_AXIS));
		outputOuter.setPreferredSize(new Dimension(380, 54));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(titlePadding4);
		panel.setPreferredSize(new Dimension(360, 26));
		jTextDstFileName = new JTextField();
		jTextDstFileName.setEditable(false);
		jTextDstFileName.setMaximumSize(new Dimension(440, 24));
		jTextDstFileName.setPreferredSize(new Dimension(440, 24));
		panel.add(jTextDstFileName);
		outputOuter.add(panel);
		previewLeft.add(outputOuter);
		
		//メタデータ
		JPanel metadataOuter = new JPanel();
		metadataOuter.setBorder(BorderFactory.createTitledBorder("メタデータ設定 (本文は変更されません)"));
		metadataOuter.setLayout(new BoxLayout(metadataOuter, BoxLayout.X_AXIS));
		metadataOuter.setPreferredSize(new Dimension(420, 90));
		JPanel metadataInner = new JPanel();
		metadataInner.setLayout(new BoxLayout(metadataInner, BoxLayout.Y_AXIS));
		metadataOuter.add(metadataInner);
		//表題
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(titlePadding4);
		panel.setPreferredSize(new Dimension(460, 28));
		panel.add(new JLabel("表題 : "));
		jTextTitle = new JTextField();
		jTextTitle.setPreferredSize(new Dimension(460, 26));
		jTextTitle.setMaximumSize(new Dimension(460, 26));
		panel.add(jTextTitle);
		metadataInner.add(panel);
		//著者
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(titlePadding4);
		panel.setPreferredSize(new Dimension(460, 28));
		panel.add(new JLabel("著者 : "));
		jTextCreator = new JTextField();
		jTextCreator.setPreferredSize(new Dimension(460, 26));
		jTextCreator.setMaximumSize(new Dimension(460, 26));
		panel.add(jTextCreator);
		metadataInner.add(panel);
		//入れ替えボタン
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panel.setBorder(padding4);
		panel.setPreferredSize(new Dimension(32, 52));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		jButton = new JButton();
		jButton.setPreferredSize(new Dimension(32, 32));
		jButton.setIcon(new ImageIcon(replaceImage));
		jButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = jTextTitle.getText();
				jTextTitle.setText(jTextCreator.getText());
				jTextCreator.setText(str);
			}
		});
		panel.add(jButton);
		metadataOuter.add(panel);
		previewLeft.add(metadataOuter);
		
		//変換とキャンセルボタン
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(padding4);
		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		//変換前確認 次から確認しない場合にチェックが外せるようにする
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jCheckConfirm2 = new JCheckBox("変換前確認  ", true);
		jCheckConfirm2.setFocusPainted(false);
		panel.add(jCheckConfirm2);
		buttonPanel.add(panel);
		//変換実行
		jButton = new JButton("変換実行");
		jButton.setIcon(new ImageIcon(applyImage));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jTextTitle.getText().replaceFirst("^[ |　]+", "").replaceFirst("[ |　]+$", "").length() == 0) {
					JOptionPane.showMessageDialog(applet, "タイトルを設定してください。");
				} else {
					canceled = false;
					setVisible(false);
				}
			}
		});
		panel.add(jButton);
		buttonPanel.add(panel);
		//キャンセル
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jButton = new JButton("キャンセル");
		jButton.setIcon(new ImageIcon(cancelImage));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				setVisible(false);
			}
		});
		panel.add(jButton);
		buttonPanel.add(panel);
		previewLeft.add(buttonPanel);
		
		
		//右側プレビューパネル
		JPanel previewRight = new JPanel();
		previewRight.setLayout(new BoxLayout(previewRight, BoxLayout.Y_AXIS));
		previewRight.setBorder(padding0);
		previewOuter.add(previewRight);
		
		//プレビューパネル
		panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder(1));
		//panel.setMinimumSize(new Dimension(150, 200));
		//panel.setMaximumSize(new Dimension(150, 200));
		panel.setPreferredSize(new Dimension(154, 200));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		previewRight.add(panel);
		jCoverImagePanel = new JCoverImagePanel();
		jCoverImagePanel.setMinimumSize(new Dimension(150, 200));
		jCoverImagePanel.setMaximumSize(new Dimension(150, 200));
		jCoverImagePanel.setPreferredSize(new Dimension(150, 200));
		panel.add(jCoverImagePanel);
		
		//プレビュー操作ボタン
		
		
		
	}
	
	/** 確認ダイアログを表示 */
	public void showDialog(String srcFile, String dstPath, String title, String creator, BookInfo bookInfo, ImageInfoReader imageInfoReader, Point location)
	{
		this.jTextSrcFileName.setText(srcFile);
		this.jTextSrcFileName.setCaretPosition(0);
		this.jTextDstFileName.setText(dstPath);
		this.jTextDstFileName.setCaretPosition(0);
		this.jTextTitle.setText(title);
		this.jTextCreator.setText(creator);
		
		//変更前確認設定用
		this.jCheckConfirm2.setSelected(true);
		
		//プレビュー表示
		this.jCoverImagePanel.clear();
		try {
			if (bookInfo.coverImageIndex >= 0) {
				bookInfo.coverImage = imageInfoReader.getImage(0);
			}
		} catch (Exception e) { e.printStackTrace(); }
		this.jCoverImagePanel.setBookInfo(bookInfo);
		
		//本情報設定ダイアログ表示
		this.setLocation(location.x+20, location.y+20);
		this.setVisible(true);
		
	}
}
