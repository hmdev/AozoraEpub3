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
import java.io.IOException;
import java.net.MalformedURLException;
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
import javax.swing.JToggleButton;
import javax.swing.border.Border;

import com.github.hmdev.info.BookInfo;
import com.github.hmdev.util.ImageInfoReader;

/**
 * 変換前確認ダイアログ
 */
public class JConfirmDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	////////////////////////////////
	/** キャンセルボタンが押されたらtrue */
	public boolean canceled = false;
	
	/** スキップボタンが押されたらtrue */
	public boolean skipped = false;
	
	////////////////////////////////
	/** 入力ファイル名 */
	JTextField jTextSrcFileName;
	/** 出力ファイル名 */
	JTextField jTextDstFileName;
	/** 表題 (+副題)編集用 */
	public JTextField jTextTitle;
	/** 著者名編集用 */
	public JTextField jTextCreator;
	
	/** プレビューパネル */
	public JCoverImagePanel jCoverImagePanel;
	/** プレビューパネル外側 */
	JPanel previewOuterPane;
	JPanel previewRight;
	
	/** 表紙画像削除 */
	public JCheckBox jCheckReplaceCover;
	
	/** 変更前確認チェック */
	public JCheckBox jCheckConfirm2;
	
	////////////////////////////////
	//Preview Controls
	/** 前の画像 */
	JButton jButtonPrev;
	/** 次の画像 */
	JButton jButtonNext;
	
	/** プレビュー全体を表示 */
	//JButton jButtonFit;
	/** 横を合せる */
	JButton jButtonFitW;
	/** 縦を合せる */
	JButton jButtonFitH;
	
	/** 拡大 */
	JButton jButtonZoomIn;
	/** 縮小 */
	JButton jButtonZoomOut;
	
	/** 幅縮小 */
	JButton jButtonNarrow;
	/** 幅拡大 */
	JButton jButtonWide;
	/** 幅縮小無し */
	JButton jButtonCoverFull;
	
	JToggleButton jButtonScale;
	/** 移動モード */
	//JButton jButtonMove;
	/** 範囲選択モード */
	//JButton jButtonRange;
	/** 表紙削除 */
	JButton jButtonDelete;
	
	BookInfo bookInfo;
	ImageInfoReader imageInfoReader;
	
	int coverW;
	int coverH;
	
	//Size
	final int DIALOG_WIDTH = 600;
	final int DIALOG_HEIGHT = 320;
	
	int PREVIEW_WIDTH = 150;
	int PREVIEW_HEIGHT = 200;
	
	public JConfirmDialog(final JApplet applet, Image iconImage, String imageURLPath)
	{
		JButton jButton;
		JPanel panel;
		Border padding0 = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border paddingButton = BorderFactory.createEmptyBorder(3, 6, 3, 6);
		Border padding4T2 = BorderFactory.createEmptyBorder(4, 2, 2, 2);
		Border padding4 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		
		final Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setIconImage(iconImage);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setSize(dialogSize);
		this.setResizable(false);
		this.setTitle("変換前確認");
		this.setLayout(new FlowLayout());
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) { canceled = true; }
		});
		
		int LEFT_PANE_WIDTH = 420;
		
		//入出力ファイル情報とプレビューを横に並べる
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer, BoxLayout.X_AXIS));
		//outer.setPreferredSize(dialogSize);
		this.add(outer);
		JPanel previewLeft = new JPanel();
		previewLeft.setLayout(new BoxLayout(previewLeft, BoxLayout.Y_AXIS));
		previewLeft.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, DIALOG_HEIGHT));
		previewLeft.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, DIALOG_HEIGHT));
		previewLeft.setBorder(padding4);
		outer.add(previewLeft);
		
		JPanel inputOuter = new JPanel();
		inputOuter.setBorder(new NarrowTitledBorder("入力ファイル"));
		inputOuter.setLayout(new BoxLayout(inputOuter, BoxLayout.X_AXIS));
		inputOuter.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 54));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding0);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 26));
		jTextSrcFileName = new JTextField();
		jTextSrcFileName.setEditable(false);
		jTextSrcFileName.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextSrcFileName.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextSrcFileName);
		inputOuter.add(panel);
		previewLeft.add(inputOuter);
		
		JPanel outputOuter = new JPanel();
		outputOuter.setBorder(new NarrowTitledBorder("出力パス"));
		outputOuter.setLayout(new BoxLayout(outputOuter, BoxLayout.X_AXIS));
		outputOuter.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 54));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding0);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 26));
		jTextDstFileName = new JTextField();
		jTextDstFileName.setEditable(false);
		jTextDstFileName.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextDstFileName.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextDstFileName);
		outputOuter.add(panel);
		previewLeft.add(outputOuter);
		
		//メタデータ
		JPanel metadataOuter = new JPanel();
		metadataOuter.setBorder(new NarrowTitledBorder("メタデータ設定 (本文は変更されません)"));
		metadataOuter.setLayout(new BoxLayout(metadataOuter, BoxLayout.X_AXIS));
		metadataOuter.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 80));
		metadataOuter.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 80));
		JPanel metadataInner = new JPanel();
		metadataInner.setLayout(new BoxLayout(metadataInner, BoxLayout.Y_AXIS));
		metadataOuter.add(metadataInner);
		//表題
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding0);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("表題 : "));
		jTextTitle = new JTextField();
		jTextTitle.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 26));
		jTextTitle.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 26));
		panel.add(jTextTitle);
		metadataInner.add(panel);
		//著者
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding0);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("著者 : "));
		jTextCreator = new JTextField();
		jTextCreator.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 26));
		jTextCreator.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 26));
		panel.add(jTextCreator);
		metadataInner.add(panel);
		//入れ替えボタン
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panel.setBorder(padding4);
		panel.setPreferredSize(new Dimension(32, 32));
		panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
		jButton = new JButton();
		jButton.setPreferredSize(new Dimension(32, 32));
		try { jButton.setIcon(new ImageIcon(new URL(imageURLPath+"replace.png"))); } catch (MalformedURLException e1) {}
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
		jButton.setBorder(paddingButton);
		jButton.setPreferredSize(new Dimension(80, 26));
		try { jButton.setIcon(new ImageIcon(new URL(imageURLPath+"apply.png"))); } catch (MalformedURLException e1) {}
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
		//スキップ
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jButton = new JButton("スキップ");
		jButton.setBorder(paddingButton);
		jButton.setPreferredSize(new Dimension(80, 26));
		try { jButton.setIcon(new ImageIcon(new URL(imageURLPath+"skip.png"))); } catch (MalformedURLException e1) {}
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				skipped = true;
				setVisible(false);
			}
		});
		panel.add(jButton);
		buttonPanel.add(panel);
		
		//キャンセル
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jButton = new JButton("処理中止");
		jButton.setBorder(paddingButton);
		jButton.setPreferredSize(new Dimension(80, 26));
		try { jButton.setIcon(new ImageIcon(new URL(imageURLPath+"cancel.png"))); } catch (MalformedURLException e1) {}
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
		previewRight = new JPanel();
		previewRight.setLayout(new BoxLayout(previewRight, BoxLayout.Y_AXIS));
		Dimension size = new Dimension(PREVIEW_WIDTH+10, dialogSize.height);
		previewRight.setSize(size);
		previewRight.setBorder(padding4T2);
		outer.add(previewRight);
		
		//プレビューパネル
		previewOuterPane = new JPanel();
		previewOuterPane.setBorder(BorderFactory.createEtchedBorder(1));
		size = new Dimension(PREVIEW_WIDTH+5, PREVIEW_HEIGHT+5);
		previewOuterPane.setMaximumSize(size);
		previewOuterPane.setPreferredSize(size);
		previewOuterPane.setLayout(new BoxLayout(previewOuterPane, BoxLayout.X_AXIS));
		previewRight.add(previewOuterPane);
		jCoverImagePanel = new JCoverImagePanel();
		size = new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		jCoverImagePanel.setMinimumSize(size);
		jCoverImagePanel.setMaximumSize(size);
		jCoverImagePanel.setPreferredSize(size);
		jCoverImagePanel.setSize(size);
		previewOuterPane.add(jCoverImagePanel);
		
		//操作パネル
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel.setMaximumSize(new Dimension(154, 72));
		panel.setPreferredSize(new Dimension(154, 72));
		previewRight.add(panel);
		//プレビュー操作ボタン
		jButtonPrev = new JButton();
		jButtonPrev.setBorder(padding0);
		jButtonPrev.setPreferredSize(new Dimension(22, 22));
		jButtonPrev.setToolTipText("前の画像");
		jButtonPrev.setFocusable(false);
		try { jButtonPrev.setIcon(new ImageIcon(new URL(imageURLPath+"prev.png"))); } catch (MalformedURLException e1) {}
		jButtonPrev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { movePreviewImage(-1); }
		});
		panel.add(jButtonPrev);
		jButtonNext = new JButton();
		jButtonNext.setBorder(padding0);
		jButtonNext.setPreferredSize(new Dimension(22, 22));
		jButtonNext.setToolTipText("次の画像");
		jButtonNext.setFocusable(false);
		try { jButtonNext.setIcon(new ImageIcon(new URL(imageURLPath+"next.png"))); } catch (MalformedURLException e1) {}
		jButtonNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { movePreviewImage(1); }
		});
		panel.add(jButtonNext);
		///表示範囲調整
		/*jButtonFit = new JButton();
		jButtonFit.setBorder(padding0);
		jButtonFit.setPreferredSize(new Dimension(22, 22));
		try { jButtonFit.setIcon(new ImageIcon(new URL(imageURL.toString()+"/arrow_out.png"))); } catch (MalformedURLException e1) {}
		jButtonFit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { fitPreviewImage(JCoverImagePanel.FIT_ALL); }
		});
		panel.add(jButtonFit);*/
		jButtonFitW = new JButton();
		jButtonFitW.setBorder(padding0);
		jButtonFitW.setPreferredSize(new Dimension(22, 22));
		jButtonFitW.setToolTipText("画像の幅に拡大");
		jButtonFitW.setFocusable(false);
		try { jButtonFitW.setIcon(new ImageIcon(new URL(imageURLPath+"arrow_horizontal.png"))); } catch (MalformedURLException e1) {}
		jButtonFitW.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { fitPreviewImage(JCoverImagePanel.FIT_W); }
		});
		panel.add(jButtonFitW);
		jButtonFitH = new JButton();
		jButtonFitH.setBorder(padding0);
		jButtonFitH.setPreferredSize(new Dimension(22, 22));
		jButtonFitH.setToolTipText("画像の高さに拡大");
		jButtonFitH.setFocusable(false);
		try { jButtonFitH.setIcon(new ImageIcon(new URL(imageURLPath+"arrow_vertical.png"))); } catch (MalformedURLException e1) {}
		jButtonFitH.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { fitPreviewImage(JCoverImagePanel.FIT_H); }
		});
		panel.add(jButtonFitH);
		//移動
		/*jButtonMove = new JButton("＋");
		jButtonMove.setBorder(padding0);
		jButtonMove.setPreferredSize(new Dimension(28, 24));
		jButtonMove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {  }
		});
		panel.add(jButtonMove);
		
		//範囲拡大
		jButtonRange = new JButton("□");
		jButtonRange.setBorder(padding0);
		jButtonRange.setPreferredSize(new Dimension(28, 24));
		jButtonRange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {  }
		});
		panel.add(jButtonRange);*/
		
		//拡大
		jButtonZoomIn = new JButton();
		jButtonZoomIn.setBorder(padding0);
		jButtonZoomIn.setPreferredSize(new Dimension(22, 22));
		jButtonZoomIn.setToolTipText("画像を拡大");
		jButtonZoomIn.setFocusable(false);
		try { jButtonZoomIn.setIcon(new ImageIcon(new URL(imageURLPath+"zoomin.png"))); } catch (MalformedURLException e1) {}
		jButtonZoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { zoomPreview(1.01); }
		});
		panel.add(jButtonZoomIn);
		
		//縮小
		jButtonZoomOut = new JButton();
		jButtonZoomOut.setBorder(padding0);
		jButtonZoomOut.setPreferredSize(new Dimension(22, 22));
		jButtonZoomOut.setToolTipText("画像を縮小");
		jButtonZoomOut.setFocusable(false);
		try { jButtonZoomOut.setIcon(new ImageIcon(new URL(imageURLPath+"zoomout.png"))); } catch (MalformedURLException e1) {}
		jButtonZoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { zoomPreview(1/1.01); }
		});
		panel.add(jButtonZoomOut);
		
		//幅縮小
		jButtonNarrow = new JButton();
		jButtonNarrow.setBorder(padding0);
		jButtonNarrow.setPreferredSize(new Dimension(22, 22));
		jButtonNarrow.setToolTipText("表紙の幅を狭める");
		jButtonNarrow.setFocusable(false);
		try { jButtonNarrow.setIcon(new ImageIcon(new URL(imageURLPath+"cover_narrow.png"))); } catch (MalformedURLException e1) {}
		jButtonNarrow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { setVisibleWidthOffset(-1); }
		});
		panel.add(jButtonNarrow);
		
		//幅拡大
		jButtonWide = new JButton();
		jButtonWide.setBorder(padding0);
		jButtonWide.setPreferredSize(new Dimension(22, 22));
		jButtonWide.setToolTipText("表紙の幅を広げる");
		jButtonWide.setFocusable(false);
		try { jButtonWide.setIcon(new ImageIcon(new URL(imageURLPath+"cover_wide.png"))); } catch (MalformedURLException e1) {}
		jButtonWide.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { setVisibleWidthOffset(1); }
		});
		panel.add(jButtonWide);
		
		//幅縮小無し
		jButtonCoverFull = new JButton();
		jButtonCoverFull.setBorder(padding0);
		jButtonCoverFull.setPreferredSize(new Dimension(22, 22));
		jButtonCoverFull.setToolTipText("表紙の幅を元に戻す");
		jButtonCoverFull.setFocusable(false);
		try { jButtonCoverFull.setIcon(new ImageIcon(new URL(imageURLPath+"cover_full.png"))); } catch (MalformedURLException e1) {}
		jButtonCoverFull.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { resetVisibleWidth(); }
		});
		panel.add(jButtonCoverFull);
		
		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(46, 22));
		panel.add(label);
		
		jButtonScale = new JToggleButton("x2");
		jButtonScale.setBorder(padding0);
		jButtonScale.setPreferredSize(new Dimension(22, 22));
		jButtonScale.setToolTipText("プレビューを拡大します");
		jButtonScale.setFocusable(false);
		jButtonScale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { setCoverPaneSize(jButtonScale.isSelected()?2:1); }
		});
		panel.add(jButtonScale);
		
		jCheckReplaceCover = new JCheckBox("元画像を出力");
		jCheckReplaceCover.setPreferredSize(new Dimension(114, 22));
		jCheckReplaceCover.setFocusable(false);
		jCheckReplaceCover.setEnabled(false);
		panel.add(jCheckReplaceCover);
		//削除
		jButtonDelete = new JButton();
		jButtonDelete.setBorder(padding0);
		jButtonDelete.setPreferredSize(new Dimension(22, 22));
		jButtonDelete.setToolTipText("表紙なし");
		jButtonDelete.setFocusable(false);
		try { jButtonDelete.setIcon(new ImageIcon(new URL(imageURLPath+"delete.png"))); } catch (MalformedURLException e1) {}
		jButtonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { deleteCover(); }
		});
		panel.add(jButtonDelete);
	}
	
	void checkPreviewControlEnabled()
	{
		int count = this.imageInfoReader.countImageFileNames();
		this.jButtonPrev.setEnabled(count > 0 && this.bookInfo.coverImageIndex > 0);
		this.jButtonNext.setEnabled(count > 0 && this.bookInfo.coverImageIndex < count-1);
		//this.jButtonMove.setEnabled(this.bookInfo.coverImage != null || this.bookInfo.coverImageIndex > 0);
		//this.jButtonRange.setEnabled(this.bookInfo.coverImage != null || this.bookInfo.coverImageIndex > 0);
		//this.jButtonDelete.setEnabled(this.bookInfo.coverImage != null || this.bookInfo.coverImageIndex > 0);
		this.jCheckReplaceCover.setEnabled(this.bookInfo.coverImageIndex >= 0 && this.jCoverImagePanel.isModified());
	}
	
	void movePreviewImage(int offset)
	{
		this.bookInfo.coverImageIndex += offset;
		try {
			bookInfo.coverImage = imageInfoReader.getImage(bookInfo.coverImageIndex);
			jCoverImagePanel.setBookInfo(bookInfo);
		} catch (IOException e) {
			e.printStackTrace();
			this.bookInfo.coverImage = null;
			this.bookInfo.coverImageIndex = -1;
			this.jCoverImagePanel.setBookInfo(this.bookInfo);
		}
		this.checkPreviewControlEnabled();
	}
	void zoomPreview(double zoom)
	{
		this.jCoverImagePanel.setZoom(zoom);
		this.checkPreviewControlEnabled();
	}
	
	void resetVisibleWidth()
	{
		this.jCoverImagePanel.resetVisibleWidth();
	}
	void setVisibleWidthOffset(int offset)
	{
		this.jCoverImagePanel.setVisibleWidthOffset(offset);
	}
	
	void fitPreviewImage(int fitType)
	{
		this.jCoverImagePanel.setFitType(fitType, false);
		this.checkPreviewControlEnabled();
	}
	
	/** 表紙削除 */
	void deleteCover()
	{
		this.bookInfo.coverFileName = null;
		this.bookInfo.coverImage = null;
		this.bookInfo.coverImageIndex = -1;
		this.jCoverImagePanel.setBookInfo(this.bookInfo);
		this.checkPreviewControlEnabled();
	}
	
	/** 確認ダイアログを表示
	 * @param location ダイアログ表示位置 */
	public void showDialog(String srcFile, String dstPath, String title, String creator, BookInfo bookInfo, ImageInfoReader imageInfoReader, Point location, int coverW, int coverH)
	{
		//zip内テキストファイル名も表示
		if (bookInfo.textEntryName != null) srcFile += " : "+bookInfo.textEntryName.substring(bookInfo.textEntryName.lastIndexOf('/')+1);
		this.jTextSrcFileName.setText(srcFile);
		this.jTextSrcFileName.setToolTipText(srcFile);
		//this.jTextSrcFileName.setCaretPosition(0);
		this.jTextDstFileName.setText(dstPath);
		this.jTextDstFileName.setToolTipText(dstPath);
		//this.jTextDstFileName.setCaretPosition(0);
		this.jTextTitle.setText(title);
		this.jTextCreator.setText(creator);
		this.jCheckReplaceCover.setSelected(false);
		//変更前確認設定用
		this.jCheckConfirm2.setSelected(true);
		
		//フラグ初期化
		this.canceled = false;
		this.skipped = false;
		
		this.coverW = coverW;
		this.coverH = coverH;
		
		this.bookInfo = bookInfo;
		this.imageInfoReader = imageInfoReader;
		
		this.jCoverImagePanel.setBookInfo(bookInfo);
		
		this.setCoverPaneSize(1);
		if (this.canceled) return;
		
		this.jButtonScale.setSelected(false);
		
		//プレビュー表示
		try {
			if (imageInfoReader.countImageFileNames() > 0 && bookInfo.coverImageIndex >= 0) {
				bookInfo.coverImage = imageInfoReader.getImage(0);
			} else if (bookInfo.coverImage == null && bookInfo.coverFileName != null) {
				bookInfo.loadCoverImage(bookInfo.coverFileName);
			}
		} catch (Exception e) { e.printStackTrace(); }
		this.jCoverImagePanel.setFitType(JCoverImagePanel.FIT_ALL, true);
		this.checkPreviewControlEnabled();
		
		this.jCheckReplaceCover.setVisible(bookInfo.insertCoverPage);
		
		//本情報設定ダイアログ表示
		this.setLocation(location.x+20, location.y+20);
		this.setVisible(true);
	}
	
	void setCoverPaneSize(double scale)
	{
		//プレビューパネルとダイアログの幅調整
		int previewWidth = (int)(PREVIEW_WIDTH*scale);
		int previewHeight = (int)(PREVIEW_HEIGHT*scale);
		
		//増加分
		int delta = (int)(((double)coverW/coverH) / ((double)previewWidth/previewHeight)*previewWidth - previewWidth);
		if (delta > 1000 || delta < -1000) {
			JOptionPane.showMessageDialog(this, "表紙サイズを修正してください");
			this.canceled = true;
			return;
		}
		
		int dialogHeight = DIALOG_HEIGHT+previewHeight-PREVIEW_HEIGHT;
		
		this.setResizable(true);
		this.jCoverImagePanel.setPaneSize(previewWidth+delta, previewHeight);
		Dimension size = new Dimension(previewWidth+5+delta, previewHeight+5);
		this.previewOuterPane.setPreferredSize(size);
		this.previewOuterPane.setMaximumSize(size);
		this.previewOuterPane.setMinimumSize(size);
		if (delta > 0) {
			int dialogWidth = DIALOG_WIDTH+previewWidth+delta-PREVIEW_WIDTH;
			this.setSize(dialogWidth, dialogHeight);
			this.previewRight.setPreferredSize(new Dimension(previewWidth+14+delta, dialogHeight));
			this.previewRight.setMaximumSize(new Dimension(previewWidth+14+delta, dialogHeight));
			this.previewRight.setMinimumSize(new Dimension(previewWidth+14+delta, dialogHeight));
		} else {
			int dialogWidth = DIALOG_WIDTH+previewWidth-PREVIEW_WIDTH;
			this.setSize(dialogWidth, dialogHeight);
			this.previewRight.setPreferredSize(new Dimension(previewWidth+14, dialogHeight));
			this.previewRight.setMaximumSize(new Dimension(previewWidth+14, dialogHeight));
			this.previewRight.setMinimumSize(new Dimension(previewWidth+14, dialogHeight));
		}
		this.setResizable(false);
		this.repaint();
	}
}
