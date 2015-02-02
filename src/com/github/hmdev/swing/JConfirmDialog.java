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
import java.io.File;
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
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;

import com.github.hmdev.image.ImageInfoReader;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.ChapterLineInfo;
import com.github.hmdev.swing.JTocTable.TocTableDataModel;

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
	JCheckBox jCheckPubFirst;
	/** 表題再取得 順番 */
	JButton jButtonTitle;
	/** 表題をファイル名から設定 */
	JButton jButtonTitleFileName;
	
	/** 表題 (+副題)編集用 */
	JTextField jTextTitle;
	/** 表題読み編集用 */
	JTextField jTextTitleAs;
	/** 著者名編集用 */
	JTextField jTextCreator;
	/** 著者名読み編集用 */
	JTextField jTextCreatorAs;
	/** 刊行者編集用 */
	JTextField jTextPublisher;
	
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
	JTocTable jTableToc;
	JScrollPane jScrollToc;
	TocTableDataModel tocDataModel;
	
	/** 目次選択 */
	JCheckBox jCheckChapterAll;
	JCheckBox jCheckChapterSection;
	JCheckBox jCheckChapterH;
	JCheckBox jCheckChapterH1;
	JCheckBox jCheckChapterH2;
	JCheckBox jCheckChapterH3;
	JCheckBox jCheckChapterName;
	JCheckBox jCheckChapterNum;
	JCheckBox jCheckChapterPattern;
	
	////////////////////////////////
	//Preview Controls
	/** 先頭画像 */
	JButton jButtonFirst;
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
	static final int DIALOG_HEIGHT = 420;
	
	static final int LEFT_PANE_WIDTH = 430;
	static final int PREVIEW_WIDTH = 180;
	static final int PREVIEW_HEIGHT = 240;
	
	//初回表示後true
	boolean firstShown = false;
	
	public JConfirmDialog(Image iconImage, String imageURLPath)
	{
		JButton jButton;
		JPanel panel;
		JLabel label;
		Border padding0 = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border paddingButton = BorderFactory.createEmptyBorder(3, 6, 3, 6);
		Border padding4T2 = BorderFactory.createEmptyBorder(4, 2, 2, 2);
		Border padding4 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		Border padding1T = BorderFactory.createEmptyBorder(1, 0, 0, 0);
		Border padding4T = BorderFactory.createEmptyBorder(4, 0, 0, 0);
		Border padding4T2B = BorderFactory.createEmptyBorder(4, 0, 2, 0);
		Border padding2H = BorderFactory.createEmptyBorder(0, 2, 0, 2);
		Border padding3 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		
		final Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setIconImage(iconImage);
		this.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
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
		metadataOuter.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 200));
		//再取得
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T);
		label = new JLabel("本文内");
		panel.add(label);
		jComboTitle = new JComboBox(BookInfo.TitleType.titleTypeNames);
		jComboTitle.setFocusable(false);
		jComboTitle.setMaximumSize(new Dimension(200, 22));
		jComboTitle.setPreferredSize(new Dimension(200, 22));
		jComboTitle.setBorder(padding0);
		((JLabel)jComboTitle.getRenderer()).setBorder(padding2H);
		panel.add(jComboTitle);
		//入力ファイル名優先
		jCheckPubFirst = new JCheckBox("先頭が発行者");
		jCheckPubFirst.setFocusPainted(false);
		panel.add(jCheckPubFirst);
		jButtonTitle = new JButton("再取得");
		jButtonTitle.setBorder(padding3);
		jButtonTitle.setPreferredSize(new Dimension(72, 24));
		try { jButtonTitle.setIcon(new ImageIcon(new URL(imageURLPath+"title_reload.png"))); } catch (MalformedURLException e1) {}
		jButtonTitle.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) { reloadTitle(); } });
		panel.add(jButtonTitle);
		panel.add(new JLabel("   "));
		jButtonTitleFileName = new JButton();
		jButtonTitleFileName.setToolTipText("ファイル名から設定");
		jButtonTitleFileName.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		jButtonTitleFileName.setPreferredSize(new Dimension(130, 24));
		try { jButtonTitleFileName.setIcon(new ImageIcon(new URL(imageURLPath+"filename_copy.png"))); } catch (MalformedURLException e1) {}
		jButtonTitleFileName.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) { userFileName(); } });
		panel.add(jButtonTitleFileName);
		panel.add(new JLabel("     "));
		metadataOuter.add(panel);
		
		//表題
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding1T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("表題: "));
		jTextTitle = new JTextField();
		jTextTitle.setMinimumSize(new Dimension(10, 24));
		jTextTitle.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextTitle.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextTitle);
		metadataOuter.add(panel);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding1T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("ふりがな: "));
		jTextTitleAs = new JTextField();
		jTextTitleAs.setToolTipText("表題のよみを入力します");
		jTextTitleAs.setMinimumSize(new Dimension(10, 24));
		jTextTitleAs.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextTitleAs.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextTitleAs);
		metadataOuter.add(panel);
		//著者
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("著者: "));
		jTextCreator = new JTextField();
		jTextCreator.setToolTipText("著者のよみを入力します");
		jTextCreator.setMinimumSize(new Dimension(10, 24));
		jTextCreator.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextCreator.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextCreator);
		metadataOuter.add(panel);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding1T);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("ふりがな: "));
		jTextCreatorAs = new JTextField();
		jTextCreatorAs.setMinimumSize(new Dimension(10, 24));
		jTextCreatorAs.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextCreatorAs.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextCreatorAs);
		metadataOuter.add(panel);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4T2B);
		panel.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 28));
		panel.add(new JLabel("発行: "));
		jTextPublisher = new JTextField();
		jTextPublisher.setMinimumSize(new Dimension(10, 24));
		jTextPublisher.setMaximumSize(new Dimension(LEFT_PANE_WIDTH, 24));
		jTextPublisher.setPreferredSize(new Dimension(LEFT_PANE_WIDTH, 24));
		panel.add(jTextPublisher);
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
				skip();
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
				cancel();
			}
		});
		panel.add(jButton);
		buttonPanel.add(panel);
		previewLeft.add(buttonPanel);
		
		////////////////////////////////////////////////////////////////
		//目次プレビュー
		jTableToc = new JTocTable();
		jScrollToc = new JScrollPane(jTableToc);
		//tocPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		previewLeft.add(jScrollToc);
		
		//目次選択
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(padding0);
		label = new JLabel("目次選択:");
		label.setBorder(padding2H);
		panel.add(label);
		jCheckChapterAll = new JCheckBox("全");
		jCheckChapterAll.setFocusPainted(false);
		jCheckChapterAll.setBorder(padding2H);
		jCheckChapterAll.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterAll(jCheckChapterAll.isSelected()); } });
		panel.add(jCheckChapterAll);
		jCheckChapterSection = new JCheckBox("改");
		jCheckChapterSection.setFocusPainted(false);
		jCheckChapterSection.setBorder(padding2H);
		jCheckChapterSection.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_PAGEBREAK); } });
		panel.add(jCheckChapterSection);
		jCheckChapterH = new JCheckBox("見");
		jCheckChapterH.setFocusPainted(false);
		jCheckChapterH.setBorder(padding2H);
		jCheckChapterH.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_CHUKI_H); } });
		panel.add(jCheckChapterH);
		jCheckChapterH1 = new JCheckBox("大");
		jCheckChapterH1.setFocusPainted(false);
		jCheckChapterH1.setBorder(padding2H);
		jCheckChapterH1.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_CHUKI_H1); } });
		panel.add(jCheckChapterH1);
		jCheckChapterH2 = new JCheckBox("中");
		jCheckChapterH2.setFocusPainted(false);
		jCheckChapterH2.setBorder(padding2H);
		jCheckChapterH2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_CHUKI_H2); } });
		panel.add(jCheckChapterH2);
		jCheckChapterH3 = new JCheckBox("小");
		jCheckChapterH3.setFocusPainted(false);
		jCheckChapterH3.setBorder(padding2H);
		jCheckChapterH3.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_CHUKI_H3); } });
		panel.add(jCheckChapterH3);
		jCheckChapterName = new JCheckBox("章");
		jCheckChapterName.setFocusPainted(false);
		jCheckChapterName.setBorder(padding2H);
		jCheckChapterName.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_CHAPTER_NAME); } });
		panel.add(jCheckChapterName);
		jCheckChapterNum = new JCheckBox("数");
		jCheckChapterNum.setFocusPainted(false);
		jCheckChapterNum.setBorder(padding2H);
		jCheckChapterNum.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_CHAPTER_NUM); } });
		panel.add(jCheckChapterNum);
		jCheckChapterPattern = new JCheckBox("他");
		jCheckChapterPattern.setFocusPainted(false);
		jCheckChapterPattern.setBorder(padding2H);
		jCheckChapterPattern.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){ selectChapterType(ChapterLineInfo.TYPE_PATTERN); } });
		panel.add(jCheckChapterPattern);
		previewLeft.add(panel);
		
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
			public void actionPerformed(ActionEvent evt) {
				switch (evt.getID()) {
				case JCoverImagePanel.EVT_DBLCLICK:
					jButtonScale.setSelected(!jButtonScale.isSelected()); setCoverPaneSize(jButtonScale.isSelected()?2:1);
					break;
				case JCoverImagePanel.EVT_PAGE_UP:
					if (bookInfo.coverImageIndex == 0 && (bookInfo.coverFileName == null || bookInfo.coverFileName.length() == 0)) return;
					movePreviewImage(-1);
					break;
				case JCoverImagePanel.EVT_PAGE_DOWN:
					if (bookInfo.coverImageIndex >= imageInfoReader.countImageFileNames()) return;
					movePreviewImage(1);
					break;
				case JCoverImagePanel.EVT_CHANGED:
					checkPreviewControlEnabled();
					break;
				}
				repaint();
			}
		});
		
		//操作パネル
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel.setMaximumSize(new Dimension(190, 72));
		panel.setPreferredSize(new Dimension(190, 72));
		previewRight.add(panel);
		//プレビュー操作ボタン
		jButtonFirst = new JButton();
		jButtonFirst.setBorder(padding0);
		jButtonFirst.setPreferredSize(new Dimension(22, 22));
		jButtonFirst.setToolTipText("先頭の画像");
		jButtonFirst.setFocusable(false);
		try { jButtonFirst.setIcon(new ImageIcon(new URL(imageURLPath+"first.png"))); } catch (MalformedURLException e1) {}
		jButtonFirst.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { movePreviewImage(-100000); }
		});
		panel.add(jButtonFirst);
		jButtonPrev = new JButton();
		jButtonPrev.setBorder(padding0);
		jButtonPrev.setPreferredSize(new Dimension(22, 22));
		jButtonPrev.setToolTipText("前の画像 (PageUp)");
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
		jButtonNext.setToolTipText("次の画像 (PageDown)");
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
		jButtonFitH.setToolTipText("画像の高さに拡大 (中ボタン)");
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
		jButtonZoomIn.setToolTipText("画像を拡大 (ホイール)");
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
		jButtonZoomOut.setToolTipText("画像を縮小 (ホイール)");
		jButtonZoomOut.setFocusable(false);
		try { jButtonZoomOut.setIcon(new ImageIcon(new URL(imageURLPath+"zoomout.png"))); } catch (MalformedURLException e1) {}
		jButtonZoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { zoomPreview(1/1.01); }
		});
		panel.add(jButtonZoomOut);
		
		label = new JLabel();
		label.setPreferredSize(new Dimension(4, 22));
		panel.add(label);
		
		jButtonScale = new JToggleButton("x2");
		jButtonScale.setBorder(padding0);
		jButtonScale.setPreferredSize(new Dimension(22, 22));
		jButtonScale.setToolTipText("プレビューを拡大します (ダブルクリック)");
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
		jButtonNarrow.setToolTipText("表紙の幅を狭める (Ctrl+←、右ドラッグ)");
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
		jButtonWide.setToolTipText("表紙の幅を広げる (Ctrl+→、右ドラッグ)");
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
	public String getMetaTitleAs()
	{
		String titleAs = jTextTitleAs.getText().trim();
		if (titleAs.length() == 0) return null;
		return titleAs;
	}
	public String getMetaCreator()
	{
		return jTextCreator.getText().trim();
	}
	public String getMetaCreatorAs()
	{
		String creatorAs = jTextCreatorAs.getText().trim();
		if (creatorAs.length() == 0) return null;
		return creatorAs;
	}
	public String getMetaPublisher()
	{
		String publisher = jTextPublisher.getText().trim();
		if (publisher.length() == 0) return null;
		return publisher;
	}
	
	void reloadTitle()
	{
		this.bookInfo.reloadMetadata(BookInfo.TitleType.indexOf(this.jComboTitle.getSelectedIndex()), jCheckPubFirst.isSelected());
		//テキストから取得できなければファイル名を利用
		this.jTextTitle.setText(this.bookInfo.title);
		this.jTextCreator.setText(this.bookInfo.creator);
		this.jTextPublisher.setText(bookInfo.publisher==null?"":bookInfo.publisher);
	}
	
	void userFileName()
	{
		String[] titleCreator = BookInfo.getFileTitleCreator(this.bookInfo.srcFile.getName());
		//テキストから取得できなければファイル名を利用
		this.bookInfo.title = titleCreator[0]==null?"":titleCreator[0];
		this.bookInfo.creator = titleCreator[1]==null?"":titleCreator[1];
		this.jTextTitle.setText(this.bookInfo.title);
		this.jTextCreator.setText(this.bookInfo.creator);
	}
	
	void checkPreviewControlEnabled()
	{
		int count = this.imageInfoReader.countImageFileNames();
		//ファイル指定されている場合はファイル名
		boolean notFirst = (count > 0 && this.bookInfo.coverImageIndex > 0) || (this.bookInfo.coverImageIndex > -1 && this.bookInfo.coverFileName != null && this.bookInfo.coverFileName.length() > 0);
		this.jButtonFirst.setEnabled(notFirst);
		this.jButtonPrev.setEnabled(notFirst);
		//最後でないor表紙無しでファイル指定か挿絵がある場合はtrue
		this.jButtonNext.setEnabled((count > 0 && this.bookInfo.coverImageIndex < count-1) || (this.bookInfo.coverImage == null && (count > 0 || this.bookInfo.coverFileName != null && this.bookInfo.coverFileName.length() > 0)));
		//this.jButtonMove.setEnabled(this.bookInfo.coverImage != null || this.bookInfo.coverImageIndex > 0);
		//this.jButtonRange.setEnabled(this.bookInfo.coverImage != null || this.bookInfo.coverImageIndex > 0);
		//this.jButtonDelete.setEnabled(this.bookInfo.coverImage != null || this.bookInfo.coverImageIndex > 0);
		this.jCheckReplaceCover.setEnabled(this.bookInfo.coverImageIndex >= 0 && this.jCoverImagePanel.isModified());
	}
	
	void movePreviewImage(int offset)
	{
		this.bookInfo.coverEditInfo = null;
		this.bookInfo.coverImageIndex += offset;
		try {
			if (this.bookInfo.coverImageIndex < 0 && this.bookInfo.coverFileName != null && this.bookInfo.coverFileName.length() > 0) {
				this.bookInfo.coverImageIndex = -1;
				this.bookInfo.loadCoverImage(this.bookInfo.coverFileName);
				jCoverImagePanel.setBookInfo(this.bookInfo);
			} else {
				this.bookInfo.coverImageIndex = Math.max(0, this.bookInfo.coverImageIndex);
				this.bookInfo.coverImage = imageInfoReader.getImage(this.bookInfo.coverImageIndex);
				jCoverImagePanel.setBookInfo(this.bookInfo);
			}
		} catch (Exception e) {
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
		this.bookInfo.coverImage = null;
		this.bookInfo.coverImageIndex = -2;
		this.jCoverImagePanel.setBookInfo(this.bookInfo);
		this.checkPreviewControlEnabled();
	}
	
	/** 変換実行 */
	void convert()
	{	
		if (jTextTitle.getText().replaceFirst("^[ |　]+", "").replaceFirst("[ |　]+$", "").length() == 0) {
			JOptionPane.showMessageDialog(this, "タイトルを設定してください。");
		} else {
			this.canceled = false;
			this.setVisible(false);
			
			//挿絵が選択されていたらファイル名はnull
			if (bookInfo.coverImageIndex > -1) bookInfo.coverFileName = null;
			
			//目次設定
			if (!this.bookInfo.isImageOnly() && this.tocDataModel != null) {
				if (this.jTableToc.isEditing()) this.jTableToc.getCellEditor().stopCellEditing(); //編集中なら確定
				int cnt = this.tocDataModel.getRowCount();
				for (int row=0; row<cnt; row++) {
					int lineNum = this.tocDataModel.getLineNum(row)-1;
					ChapterLineInfo chapterLineInfo = bookInfo.getChapterLineInfo(lineNum);
					if (chapterLineInfo != null) {
						if (!this.tocDataModel.isSelected(row)) {
							this.bookInfo.removeChapterLineInfo(chapterLineInfo.lineNum);
						} else {
							chapterLineInfo.setChapterName(this.tocDataModel.getTocName(row));
						}
					}
				}
			}
			
			//表紙情報保存
			bookInfo.coverEditInfo = this.jCoverImagePanel.getCoverEditInfo();
		}
	}
	
	void skip()
	{
		this.skipped = true;
		this.setVisible(false);
		
		//挿絵が選択されていたらファイル名はnull
		if (bookInfo.coverImageIndex > -1) bookInfo.coverFileName = null;
		
		//表紙情報保存
		bookInfo.coverEditInfo = this.jCoverImagePanel.getCoverEditInfo();
	}
	
	void cancel()
	{
		this.canceled = true;
		this.setVisible(false);
	}
	
	void selectChapterAll(boolean selected)
	{
		for (int row=this.tocDataModel.getRowCount()-1; row>=0; row--) {
			this.tocDataModel.setSelected(row, selected);
		}
		jCheckChapterSection.setSelected(selected);
		jCheckChapterH.setSelected(selected);
		jCheckChapterH1.setSelected(selected);
		jCheckChapterH2.setSelected(selected);
		jCheckChapterH3.setSelected(selected);
		jCheckChapterName.setSelected(selected);
		jCheckChapterNum.setSelected(selected);
		jCheckChapterPattern.setSelected(selected);
	}
	
	void selectChapterType(int chapterType)
	{
		if (this.tocDataModel == null) return;
		boolean sectionSelected = jCheckChapterSection.isSelected();
		for (int row=this.tocDataModel.getRowCount()-1; row>=0; row--) {
			int rowChapterType = this.tocDataModel.getChapterType(row);
			boolean rowSectionSelected = this.tocDataModel.isPageBreak(row);
			if (chapterType == rowChapterType || (chapterType == ChapterLineInfo.TYPE_PAGEBREAK && rowSectionSelected)) {
				switch (rowChapterType) {
				case ChapterLineInfo.TYPE_CHUKI_H:
					this.tocDataModel.setSelected(row, jCheckChapterH.isSelected()||(sectionSelected&&rowSectionSelected)); break;
				case ChapterLineInfo.TYPE_CHUKI_H1:
					this.tocDataModel.setSelected(row, jCheckChapterH1.isSelected()||(sectionSelected&&rowSectionSelected)); break;
				case ChapterLineInfo.TYPE_CHUKI_H2:
					this.tocDataModel.setSelected(row, jCheckChapterH2.isSelected()||(sectionSelected&&rowSectionSelected)); break;
				case ChapterLineInfo.TYPE_CHUKI_H3:
					this.tocDataModel.setSelected(row, jCheckChapterH3.isSelected()||(sectionSelected&&rowSectionSelected)); break;
				case ChapterLineInfo.TYPE_CHAPTER_NAME:
					this.tocDataModel.setSelected(row, jCheckChapterName.isSelected()||(sectionSelected&&rowSectionSelected)); break;
				case ChapterLineInfo.TYPE_CHAPTER_NUM:
					this.tocDataModel.setSelected(row, jCheckChapterNum.isSelected()||(sectionSelected&&rowSectionSelected)); break;
				case ChapterLineInfo.TYPE_PATTERN:
					this.tocDataModel.setSelected(row, jCheckChapterPattern.isSelected()||(sectionSelected&&rowSectionSelected)); break;
				default:
					this.tocDataModel.setSelected(row, (sectionSelected&&rowSectionSelected)); break;
				}
			}
		}
	}
	
	/** 目次一括変更のチェックを設定 */
	public void setChapterCheck(boolean section, boolean h, boolean h1, boolean h2, boolean h3, boolean name, boolean num, boolean pattern)
	{
		jCheckChapterSection.setSelected(section);
		jCheckChapterH.setSelected(h);
		jCheckChapterH1.setSelected(h1);
		jCheckChapterH2.setSelected(h2);
		jCheckChapterH3.setSelected(h3);
		jCheckChapterName.setSelected(name);
		jCheckChapterNum.setSelected(num);
		jCheckChapterPattern.setSelected(pattern);
	}
	
	/** 確認ダイアログを表示
	 * @param location ダイアログ表示位置 */
	public void showDialog(File srcFile, String dstPath, String title, String creator, int titleTypeIndex, boolean pubFirst,
			BookInfo bookInfo, ImageInfoReader imageInfoReader, Point location, int coverW, int coverH)
	{
		//zip内テキストファイル名も表示
		String srcFileName = srcFile.getName();
		if (bookInfo.textEntryName != null) srcFileName += " : "+bookInfo.textEntryName.substring(bookInfo.textEntryName.lastIndexOf('/')+1);
		this.jTextSrcFileName.setText(srcFileName);
		this.jTextSrcFileName.setToolTipText(srcFileName);
		//this.jTextSrcFileName.setCaretPosition(0);
		this.jTextDstFileName.setText(dstPath);
		this.jTextDstFileName.setToolTipText(dstPath);
		//this.jTextDstFileName.setCaretPosition(0);
		
		//メタデータ設定
		this.jTextTitle.setText(title);
		this.jTextTitleAs.setText(bookInfo.titleAs==null?"":bookInfo.titleAs);
		this.jTextCreator.setText(creator);
		this.jTextCreatorAs.setText(bookInfo.creatorAs==null?"":bookInfo.creatorAs);
		this.jTextPublisher.setText(bookInfo.publisher==null?"":bookInfo.publisher);
		
		//this.jCheckReplaceCover.setSelected(false);
		//変更前確認設定用
		this.jCheckConfirm2.setSelected(true);
		
		this.jComboTitle.setSelectedIndex(titleTypeIndex);
		this.jCheckPubFirst.setSelected(pubFirst);
		this.jComboTitle.setEnabled(!bookInfo.isImageOnly());
		this.jButtonTitle.setEnabled(!bookInfo.isImageOnly());
		
		//プレビュー読み込み
		try {
			if (bookInfo.coverImageIndex >= 0 && bookInfo.coverImageIndex < imageInfoReader.countImageFileNames()) {
				bookInfo.coverImage = imageInfoReader.getImage(bookInfo.coverImageIndex);
			}
			if (bookInfo.coverImage == null) {
				if (bookInfo.coverFileName == null) {
					String srcPath = srcFile.getParent();
					File coverFile = new File(srcPath+"/cover.png");
					if (!coverFile.exists()) coverFile = new File(srcPath+"/cover.jpg");
					if (!coverFile.exists()) coverFile = new File(srcPath+"/cover.jpeg");
					if (coverFile.exists()) bookInfo.coverFileName = coverFile.getAbsolutePath();
				}
				if (bookInfo.coverFileName != null) {
					bookInfo.loadCoverImage(bookInfo.coverFileName);
					bookInfo.coverImageIndex = -1;
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		
		//フラグ初期化
		this.canceled = false;
		this.skipped = false;
		
		this.coverW = coverW;
		this.coverH = coverH;
		
		this.bookInfo = bookInfo;
		this.imageInfoReader = imageInfoReader;
		
		//全選択チェック
		jCheckChapterAll.setSelected(true);
		
		//目次に無いものはdisabled
		jCheckChapterH.setEnabled(false);
		jCheckChapterH1.setEnabled(false);
		jCheckChapterH2.setEnabled(false);
		jCheckChapterH3.setEnabled(false);
		jCheckChapterName.setEnabled(false);
		jCheckChapterNum.setEnabled(false);
		jCheckChapterPattern.setEnabled(false);
		jCheckChapterSection.setEnabled(false);
		//目次設定
		if (bookInfo.isImageOnly()) {
			this.tocDataModel = null;
			this.jTableToc.setVisible(false);
			this.jScrollToc.setVisible(false);
		} else {
			
			Vector<ChapterLineInfo> vecChapterLineInfo = bookInfo.getChapterLineInfoList();
			this.tocDataModel = this.jTableToc.getModel();
			this.tocDataModel.setRowCount(0);
			for (ChapterLineInfo chapterLineInfo : vecChapterLineInfo) {
				if (chapterLineInfo.pageBreakChapter) jCheckChapterSection.setEnabled(true);
				switch (chapterLineInfo.type) {
				case ChapterLineInfo.TYPE_CHUKI_H: jCheckChapterH.setEnabled(true); break;
				case ChapterLineInfo.TYPE_CHUKI_H1: jCheckChapterH1.setEnabled(true); break;
				case ChapterLineInfo.TYPE_CHUKI_H2: jCheckChapterH2.setEnabled(true); break;
				case ChapterLineInfo.TYPE_CHUKI_H3: jCheckChapterH3.setEnabled(true); break;
				case ChapterLineInfo.TYPE_CHAPTER_NAME: jCheckChapterName.setEnabled(true); break;
				case ChapterLineInfo.TYPE_CHAPTER_NUM: jCheckChapterNum.setEnabled(true); break;
				case ChapterLineInfo.TYPE_PATTERN: jCheckChapterPattern.setEnabled(true); break;
				}
				//行の値設定
				tocDataModel.addRow(new Object[]{
					true, chapterLineInfo.pageBreakChapter?"改":"", chapterLineInfo.getTypeId(), chapterLineInfo.lineNum+1, chapterLineInfo.getChapterName()
				});
			}
			this.jTableToc.getTableHeader().setPreferredSize(new Dimension(100, 20));
			this.jTableToc.setVisible(true);
			this.jScrollToc.setVisible(true);
			this.jScrollToc.getVerticalScrollBar().setValue(0);
		}
		
		if (this.canceled) return;
		
		//サイズ調整
		this.setCoverPaneSize(1);
		//表示画像設定
		this.jCoverImagePanel.setBookInfo(bookInfo);
		
		this.jButtonScale.setSelected(false);
		
		if (bookInfo.coverEditInfo == null)
			this.jCoverImagePanel.setFitType(JCoverImagePanel.FIT_ALL, true);
		this.checkPreviewControlEnabled();
		
		this.jCheckReplaceCover.setVisible(bookInfo.insertCoverPage);
		
		//本情報設定ダイアログ表示
		if (!this.firstShown) this.setLocation(location.x+100, location.y+20);
		this.firstShown = true;
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
			if (this.jTableToc.getRowCount() > 1) incHeight = Math.max(incHeight, Math.min(16, this.jTableToc.getRowCount()-1)*16);
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
}
