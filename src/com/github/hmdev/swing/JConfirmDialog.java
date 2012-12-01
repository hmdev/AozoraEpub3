package com.github.hmdev.swing;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import com.github.hmdev.image.ImageInfoReader;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.ChapterLineInfo;

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
	
	/** 表題再取得 順番 */
	JComboBox jComboTitle;
	/** 表題再取得 順番 */
	JButton jButtonTitle;
	/** 表題をファイル名から設定 */
	JButton jButtonTitleFileName;
	
	/** 表題 (+副題)編集用 */
	JTextField jTextTitle;
	/** 著者名編集用 */
	JTextField jTextCreator;
	
	/** 変更前確認チェック */
	public JCheckBox jCheckConfirm2;
	
	/** プレビューパネル */
	public JCoverImagePanel jCoverImagePanel;
	/** プレビューパネル外側 */
	JPanel previewOuterPane;
	JPanel previewLeft;
	JPanel previewRight;
	
	/** 元画像を残す */
	public JCheckBox jCheckReplaceCover;
	
	/** 目次リスト */
	JTable jTableToc;
	JScrollPane jScrollToc;
	TocTableDataModel tocDataModel;
	
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
	static final int DIALOG_WIDTH = 640;
	static final int DIALOG_HEIGHT = 340;
	
	static final int LEFT_PANE_WIDTH = 430;
	static final int PREVIEW_WIDTH = 180;
	static final int PREVIEW_HEIGHT = 240;
	
	public JConfirmDialog(Image iconImage, String imageURLPath)
	{
		JButton jButton;
		JPanel panel;
		JLabel label;
		Border padding0 = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border paddingButton = BorderFactory.createEmptyBorder(3, 6, 3, 6);
		Border padding4T2 = BorderFactory.createEmptyBorder(4, 2, 2, 2);
		Border padding4 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		Border padding4T = BorderFactory.createEmptyBorder(4, 0, 0, 0);
		Border padding2H = BorderFactory.createEmptyBorder(0, 2, 0, 2);
		Border padding3 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		
		final Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setIconImage(iconImage);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setSize(dialogSize);
		this.setResizable(false);
		this.setTitle("変換前確認");
		this.setLayout(new GridLayout());
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) { canceled = true; }
		});
		
		//入出力ファイル情報とプレビューを横に並べる
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer, BoxLayout.X_AXIS));
		//outer.setPreferredSize(dialogSize);
		this.add(outer);
		previewLeft = new JPanel();
		previewLeft.setLayout(new BoxLayout(previewLeft, BoxLayout.Y_AXIS));
		previewLeft.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, DIALOG_HEIGHT));
		previewLeft.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, DIALOG_HEIGHT));
		previewLeft.setBorder(padding4);
		outer.add(previewLeft);
		
		JPanel ioOuter = new JPanel();
		ioOuter.setBorder(new NarrowTitledBorder("入力ファイルと出力パス"));
		ioOuter.setLayout(new BoxLayout(ioOuter, BoxLayout.Y_AXIS));
		ioOuter.setMinimumSize(new Dimension(10, 80));
		ioOuter.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 80));
		ioOuter.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 80));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 26));
		panel.add(new JLabel("入力: "));
		jTextSrcFileName = new JTextField();
		jTextSrcFileName.setEditable(false);
		jTextSrcFileName.setMinimumSize(new Dimension(10, 24));
		jTextSrcFileName.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextSrcFileName.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextSrcFileName);
		ioOuter.add(panel);
		previewLeft.add(ioOuter);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 26));
		panel.add(new JLabel("出力: "));
		jTextDstFileName = new JTextField();
		jTextDstFileName.setEditable(false);
		jTextDstFileName.setMinimumSize(new Dimension(10, 24));
		jTextDstFileName.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextDstFileName.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextDstFileName);
		ioOuter.add(panel);
		previewLeft.add(ioOuter);
		
		//メタデータ
		JPanel metadataOuter = new JPanel();
		metadataOuter.setBorder(new NarrowTitledBorder("メタデータ設定 (本文は変更されません)"));
		metadataOuter.setLayout(new BoxLayout(metadataOuter, BoxLayout.Y_AXIS));
		metadataOuter.setMinimumSize(new Dimension(10, 110));
		metadataOuter.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 110));
		metadataOuter.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 110));
		//再取得
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T);
		label = new JLabel("本文内");
		label.setBorder(padding2H);
		panel.add(label);
		jComboTitle = new JComboBox(BookInfo.TitleType.titleTypeNames);
		jComboTitle.setFocusable(false);
		jComboTitle.setMaximumSize(new Dimension(200, 22));
		jComboTitle.setPreferredSize(new Dimension(200, 22));
		jComboTitle.setBorder(padding0);
		((JLabel)jComboTitle.getRenderer()).setBorder(padding2H);
		panel.add(jComboTitle);
		jButtonTitle = new JButton("再取得");
		jButtonTitle.setBorder(padding3);
		jButtonTitle.setPreferredSize(new Dimension(72, 24));
		try { jButtonTitle.setIcon(new ImageIcon(new URL(imageURLPath+"title_reload.png"))); } catch (MalformedURLException e1) {}
		jButtonTitle.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) { reloadTitle(); } });
		panel.add(jButtonTitle);
		panel.add(new JLabel("    "));
		jButtonTitleFileName = new JButton("ファイル名から設定");
		jButtonTitleFileName.setBorder(padding3);
		jButtonTitleFileName.setPreferredSize(new Dimension(130, 24));
		try { jButtonTitleFileName.setIcon(new ImageIcon(new URL(imageURLPath+"filename_copy.png"))); } catch (MalformedURLException e1) {}
		jButtonTitleFileName.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) { userFileName(); } });
		panel.add(jButtonTitleFileName);
		panel.add(new JLabel("     "));
		metadataOuter.add(panel);
		
		//表題
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("表題: "));
		jTextTitle = new JTextField();
		jTextTitle.setMinimumSize(new Dimension(10, 24));
		jTextTitle.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextTitle.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextTitle);
		metadataOuter.add(panel);
		//著者
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("著者: "));
		jTextCreator = new JTextField();
		jTextCreator.setMinimumSize(new Dimension(10, 24));
		jTextCreator.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextCreator.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextCreator);
		metadataOuter.add(panel);
		previewLeft.add(metadataOuter);
		
		////////////////////////////////////////////////////////////////
		//変換とキャンセルボタン
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(padding0);
		buttonPanel.setMinimumSize(new Dimension(10, 40));
		buttonPanel.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 40));
		buttonPanel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 40));
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
				convert();
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
		
		////////////////////////////////////////////////////////////////
		//目次プレビュー
		jTableToc = new JTable();
		jScrollToc = new JScrollPane(jTableToc);
		//tocPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		previewLeft.add(jScrollToc);
		
		////////////////////////////////////////////////////////////////
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
		
		jCoverImagePanel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) { jButtonScale.setSelected(!jButtonScale.isSelected()); setCoverPaneSize(jButtonScale.isSelected()?2:1); }
		});
		
		//操作パネル
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel.setMaximumSize(new Dimension(190, 72));
		panel.setPreferredSize(new Dimension(190, 72));
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
		
		label = new JLabel();
		label.setPreferredSize(new Dimension(27, 22));
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
		
		jCheckReplaceCover = new JCheckBox("元画像出力");
		jCheckReplaceCover.setPreferredSize(new Dimension(96, 22));
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
	
	public String getMetaTitle()
	{
		return jTextTitle.getText().trim();
	}
	public String getMetaCreator()
	{
		return jTextCreator.getText().trim();
	}
	
	
	void reloadTitle()
	{
		bookInfo.reloadMetadata(BookInfo.TitleType.indexOf(this.jComboTitle.getSelectedIndex()));
		//テキストから取得できなければファイル名を利用
		this.jTextTitle.setText(bookInfo.title);
		this.jTextCreator.setText(bookInfo.creator);
	}
	
	void userFileName()
	{
		String[] titleCreator = BookInfo.getFileTitleCreator(jTextSrcFileName.getText());
		//テキストから取得できなければファイル名を利用
		bookInfo.title = titleCreator[0]==null?"":titleCreator[0];
		bookInfo.creator = titleCreator[1]==null?"":titleCreator[1];
		this.jTextTitle.setText(bookInfo.title);
		this.jTextCreator.setText(bookInfo.creator);
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
		this.checkPreviewControlEnabled();
	}
	void setVisibleWidthOffset(int offset)
	{
		this.jCoverImagePanel.setVisibleWidthOffset(offset);
		this.checkPreviewControlEnabled();
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
	
	/** 変換実行 */
	void convert()
	{	
		if (jTextTitle.getText().replaceFirst("^[ |　]+", "").replaceFirst("[ |　]+$", "").length() == 0) {
			JOptionPane.showMessageDialog(this, "タイトルを設定してください。");
		} else {
			canceled = false;
			setVisible(false);
			
			//目次設定
			if (!this.bookInfo.isImageOnly() && this.tocDataModel != null) {
				if (this.jTableToc.isEditing()) this.jTableToc.getCellEditor().stopCellEditing(); //編集中なら確定
				int cnt = this.tocDataModel.getRowCount();
				for (int row=0; row<cnt; row++) {
					int lineNum = this.tocDataModel.getLineNum(row)-1;
					ChapterLineInfo chapterLineInfo = bookInfo.getChapterLineInfo(lineNum);
					if (!this.tocDataModel.isSelected(row)) {
						this.bookInfo.removeChapterLineInfo(chapterLineInfo.lineNum);
					} else {
						chapterLineInfo.setChapterName(this.tocDataModel.getTocName(row));
					}
				}
			}
		}
	}
	
	/** 確認ダイアログを表示
	 * @param location ダイアログ表示位置 */
	public void showDialog(String srcFile, String dstPath, String title, String creator, int titleTypeIndex,
			BookInfo bookInfo, ImageInfoReader imageInfoReader, Point location, int coverW, int coverH)
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
		
		this.jComboTitle.setSelectedIndex(titleTypeIndex);
		this.jComboTitle.setEnabled(!bookInfo.isImageOnly());
		this.jButtonTitle.setEnabled(!bookInfo.isImageOnly());
		
		//フラグ初期化
		this.canceled = false;
		this.skipped = false;
		
		this.coverW = coverW;
		this.coverH = coverH;
		
		this.bookInfo = bookInfo;
		this.imageInfoReader = imageInfoReader;
		
		this.jCoverImagePanel.setBookInfo(bookInfo);
		
		//目次設定
		if (bookInfo.isImageOnly()) {
			this.tocDataModel = null;
			this.jTableToc.setModel(new DefaultTableModel());
			this.jTableToc.setVisible(false);
			this.jScrollToc.setVisible(false);
		} else {
			Vector<ChapterLineInfo> vecChapterLineInfo = bookInfo.getChapterLineInfoList();
			this.tocDataModel = new TocTableDataModel(new String[]{"", "", "行", "目次名称"}, 0);
			for (ChapterLineInfo chapterLineInfo : vecChapterLineInfo) {
				tocDataModel.addRow(new Object[]{true, chapterLineInfo.getTypeId(), chapterLineInfo.lineNum+1, chapterLineInfo.getChapterName()});
			}
			this.jTableToc.setModel(tocDataModel);
			this.jTableToc.getColumnModel().getColumn(0).setMaxWidth(22);
			this.jTableToc.getColumnModel().getColumn(1).setMaxWidth(30);
			this.jTableToc.getColumnModel().getColumn(1).setPreferredWidth(20);
			this.jTableToc.getColumnModel().getColumn(2).setMaxWidth(60);
			this.jTableToc.getColumnModel().getColumn(2).setPreferredWidth(35);
			this.jTableToc.getTableHeader().setPreferredSize(new Dimension(100, 20));
			this.jTableToc.setVisible(true);
			this.jScrollToc.setVisible(true);
		}
		
		//サイズ調整
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
		this.setLocation(location.x+100, location.y+20);
		this.setVisible(true);
	}
	
	/** 表紙プレビューを指定した倍率に変更します */
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
		
		int incHeight = previewHeight-PREVIEW_HEIGHT;
		//目次サイズの応じてテーブルの高さを調整
		if (!bookInfo.isImageOnly()) {
			if (this.jTableToc.getRowCount() > 2) incHeight = Math.max(incHeight, Math.min(10, this.jTableToc.getRowCount()-2)*18);
		}
		int dialogHeight = DIALOG_HEIGHT+incHeight;
		
		this.setResizable(true);
		Dimension size = new Dimension(previewWidth+5+delta, previewHeight+5);
		this.previewOuterPane.setPreferredSize(size);
		this.previewOuterPane.setMaximumSize(size);
		this.previewOuterPane.setMinimumSize(size);
		if (delta > 0) {
			int dialogWidth = DIALOG_WIDTH+previewWidth+delta-PREVIEW_WIDTH;
			this.setSize(dialogWidth, dialogHeight);
			size = new Dimension(previewWidth+14+delta, dialogHeight);
			this.previewRight.setPreferredSize(size);
			this.previewRight.setMaximumSize(size);
			this.previewRight.setMinimumSize(size);
		} else {
			int dialogWidth = DIALOG_WIDTH+previewWidth-PREVIEW_WIDTH;
			this.setSize(dialogWidth, dialogHeight);
			size = new Dimension(previewWidth+14, dialogHeight);
			this.previewRight.setPreferredSize(size);
			this.previewRight.setMaximumSize(size);
			this.previewRight.setMinimumSize(size);
		}
		size = new Dimension(LEFT_PANE_WIDTH, dialogHeight);
		this.previewLeft.setPreferredSize(size);
		this.previewLeft.setMaximumSize(size);
		this.previewLeft.setMinimumSize(size);
		this.jCoverImagePanel.setPaneSize(previewWidth+delta, previewHeight);
		this.setResizable(false);
		this.repaint();
	}
	
	class TocTableDataModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;
		TocTableDataModel(String[] columnNames, int rowNum){
			super(columnNames, rowNum);
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
			switch (col) {
			case 0: return Boolean.class;
			case 2: return Integer.class;
			}
			return String.class;
		}
		@Override
		public boolean isCellEditable(int row, int col)
		{
			switch (col) {
			case 1:
			case 2:
				return false;
			}
			return true;
		}
		public boolean isSelected(int row)
		{
			return (Boolean)this.getValueAt(row, 0);
		}
		public int getLineNum(int row)
		{
			return (Integer)this.getValueAt(row, 2);
		}
		public String getTocName(int row)
		{
			return (String)this.getValueAt(row, 3);
		}
	}
}
