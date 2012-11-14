import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.compress.utils.IOUtils;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.swing.JConfirmDialog;
import com.github.hmdev.swing.NarrowTitledBorder;
import com.github.hmdev.util.ImageInfoReader;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.writer.Epub3ImageWriter;
import com.github.hmdev.writer.Epub3Writer;

/**
 * 青空文庫テキスト→ePub3変換操作用アプレット
 * Licence: Non-commercial use only.
 */
public class AozoraEpub3Applet extends JApplet
{
	private static final long serialVersionUID = 1L;
	
	/** アプレットが表示されているフレーム */
	JFrame jFrameParent;
	
	Image iconImage;
	
	/** 変換前確認ダイアログ */
	JConfirmDialog jConfirmDialog;
	
	JComboBox jComboTitle;
	JCheckBox jCheckUserFileName;
	JCheckBox jCheckAutoFileName;
	JCheckBox jCheckMiddleTitle;
	
	JCheckBox jCheckConfirm;
	
	JComboBox jComboDstPath;
	JComboBox jComboExt;
	JCheckBox jCheckMarkId;
	
	JCheckBox jCheckOverWrite;
	JCheckBox jCheckGaiji32;
	
	JRadioButton jRadioVertical;
	JRadioButton jRadioHorizontal;
	
	JRadioButton jRadioLtR;
	JRadioButton jRadioRtL;
	
	//入力ファイルエンコード
	JComboBox jComboEncType;
	
	/** 表紙と目次 */
	JComboBox jComboCover;
	JCheckBox jCheckCoverPage;
	JCheckBox jCheckTocPage;
	JRadioButton jRadioTocV;
	JRadioButton jRadioTocH;
	
	/** ファイル選択ボタン */
	JButton jButtonFile;
	JButton jButtonCover;
	JButton jButtonDstPath;
	
	JTextField jTextDispW;
	JTextField jTextDispH;
	
	JTextField jTextSinglePageSizeW;
	JTextField jTextSinglePageSizeH;
	JTextField jTextSinglePageWidth;
	JCheckBox jCheckFitImage;
	
	//画像縮小
	JCheckBox jCheckResizeW;
	JTextField jTextResizeW;
	JCheckBox jCheckResizeH;
	JTextField jTextResizeH;
	JCheckBox jCheckPixel;
	JTextField jTextPixelW;
	JTextField jTextPixelH;
	//表紙サイズ
	JTextField jTextCoverW;
	JTextField jTextCoverH;
	//Jpeg圧縮率
	JTextField jTextJpegQuality;
	
	JRadioButton jRadioSpaceHyp0;
	JRadioButton jRadioSpaceHyp1;
	JRadioButton jRadioSpaceHyp2;
	
	JCheckBox jCheckAutoYoko;
	JCheckBox jCheckAutoYokoNum1;
	JCheckBox jCheckAutoYokoNum3;
	
	JCheckBox jCheckCommentPrint;
	JCheckBox jCheckCommentConvert;
	
	JTextField jTextMaxChapterNameLength;
	JCheckBox jCheckCoverPageToc;
	JCheckBox jCheckChapterSection;
	JCheckBox jCheckChapterH;
	JCheckBox jCheckChapterH1;
	JCheckBox jCheckChapterH2;
	JCheckBox jCheckChapterH3;
	JCheckBox jCheckChapterName;
	JCheckBox jCheckChapterNameTitle;
	JCheckBox jCheckChapterNum;
	JCheckBox jCheckChapterNumOnly;
	JCheckBox jCheckChapterNumParen;
	JCheckBox jCheckChapterNumParenTitle;
	JCheckBox jCheckChapterExclude;
	
	JComboBox jComboxPageBreak;
	JCheckBox jCheckPageBreakEmpty;
	JComboBox jComboxPageBreakEmptyLine;
	JCheckBox jCheckPageBreakChapter;
	
	//テキストエリア
	JScrollPane jScrollPane;
	JTextArea jTextArea;
	
	/** 出力先選択ダイアログ表示イベントactionPerformed(null)で明示的に呼び出す。 */
	DstPathChooserListener dstPathChooser;
	
	
	/** 青空→ePub3変換クラス */
	AozoraEpub3Converter aozoraConverter;
	
	/** ePub3出力クラス */
	Epub3Writer epub3Writer;
	
	/** ePub3画像出力クラス */
	Epub3ImageWriter epub3ImageWriter;
	
	/** UTF-8 → グリフタグ変換クラス */
	//GlyphConverter glyphConverter;
	//String initdConverterType = null;
	
	/** 変換をキャンセルした場合true */
	boolean convertCanceled = false;
	/** 変換実行中 */
	boolean running = false;
	
	/** 設定ファイル */
	Properties props;
	/** 設定ファイル名 */
	String propFileName = "AozoraEpub3.ini";
	
	/** 前回の出力パス */
	File currentPath = null;
	/** キャッシュ保存パス */
	File cachePath = null;
	/** RAR解凍先tmpパス */
	File tmpPath = null;
	
	//UIパラメータ
	int coverW;
	int coverH;
	
	
	private void setFrame(JFrame parent)
	{
		this.jFrameParent = parent;
		
	}
	
	/** アプレット初期化 */
	@Override
	public void init()
	{
		super.init();
		this.setSize(new Dimension(520, 360));
		
		//設定ファイル読み込み
		props = new Properties(); 
		try {
			props.load(new FileInputStream(propFileName));
		} catch (Exception e) { }
		String path = props.getProperty("LastDir");
		if (path != null && path.length() >0) this.currentPath = new File(path);
		
		String propValue;
		JPanel tabPanel;
		JPanel panel;
		JLabel label;
		Border padding0 = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border padding3V = BorderFactory.createEmptyBorder(3, 0, 3, 0);
		Border padding2H = BorderFactory.createEmptyBorder(0, 2, 0, 2);
		Border padding4H = BorderFactory.createEmptyBorder(0, 4, 0, 4);
		Border padding4H2V = BorderFactory.createEmptyBorder(2, 4, 2, 4);
		Border padding2H2T3B = BorderFactory.createEmptyBorder(2, 2, 3, 2);
		Border padding4B = BorderFactory.createEmptyBorder(0, 0, 4, 0);
		
		Dimension panelSize = new Dimension(1920, 26);
		Dimension text3 = new Dimension(30, 20);
		Dimension text4 = new Dimension(38, 20);
		
		//アップレットのレイアウト設定
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		JTabbedPane jTabbedPane = new JTabbedPane();
		jTabbedPane.setBorder(padding2H);
		this.add(jTabbedPane);
		
		ButtonGroup buttonGroup;
		
		////////////////////////////////////////////////////////////////
		//Tab 変換
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		jTabbedPane.setMaximumSize(new Dimension(1920, 260));
		jTabbedPane.setPreferredSize(new Dimension(1920, 220));
		jTabbedPane.add("  変換  ", tabPanel);
		
		////////////////////////////////
		//表題
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setPreferredSize(panelSize);
		panel.setBorder(padding4H2V);
		tabPanel.add(panel);
		label = new JLabel("表題: ");
		panel.add(label);
		label = new JLabel("本文内");
		label.setBorder(padding2H);
		panel.add(label);
		jComboTitle = new JComboBox(BookInfo.TitleType.titleTypeNames);
		jComboTitle.setFocusable(false);
		jComboTitle.setMaximumSize(new Dimension(180, 22));
		try { jComboTitle.setSelectedIndex(Integer.parseInt(props.getProperty("TitleType"))); } catch (Exception e) {}
		((JLabel)jComboTitle.getRenderer()).setBorder(padding2H);
		panel.add(jComboTitle);
		//入力ファイル名優先
		propValue = props.getProperty("UseFileName");
		jCheckUserFileName = new JCheckBox("ファイル名優先 ", "1".equals(propValue));
		jCheckUserFileName.setFocusPainted(false);
		panel.add(jCheckUserFileName);
		
		////////////////////////////////
		//表紙
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setPreferredSize(panelSize);
		panel.setBorder(padding4H2V);
		tabPanel.add(panel);
		//表紙
		label = new JLabel("表紙: ");
		panel.add(label);
		propValue = props.getProperty("Cover");
		jComboCover = new JComboBox(new String[]{"[先頭の挿絵]", "[入力ファイル名と同じ画像(png,jpg)]", "[表紙無し]", "http://"});
		jComboCover.setEditable(true);
		if (propValue==null||propValue.length()==0) jComboCover.setSelectedIndex(0);
		else jComboCover.setSelectedItem(propValue);
		jComboCover.setPreferredSize(new Dimension(320, 24));
		panel.add(jComboCover);
		new DropTarget(jComboCover.getEditor().getEditorComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new DropCoverListener(), true);
		jButtonCover = new JButton("選択");
		jButtonCover.setBorder(padding3V);
		jButtonCover.setPreferredSize(new Dimension(56, 24));
		jButtonCover.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/cover.png")));
		jButtonCover.setFocusable(false);
		jButtonCover.addActionListener(new CoverChooserListener(this));
		panel.add(jButtonCover);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setPreferredSize(panelSize);
		panel.setBorder(padding4H2V);
		tabPanel.add(panel);
		//ページ出力
		label = new JLabel("ページ出力:");
		panel.add(label);
		propValue = props.getProperty("CoverPage");
		jCheckCoverPage = new JCheckBox("表紙画像", propValue==null||"1".equals(propValue));
		jCheckCoverPage.setFocusPainted(false);
		panel.add(jCheckCoverPage);
		label = new JLabel("  ");
		panel.add(label);
		//左右中央
		propValue = props.getProperty("MiddleTitle");
		jCheckMiddleTitle = new JCheckBox("表題左右中央", propValue==null||"1".equals(propValue));
		jCheckMiddleTitle.setFocusPainted(false);
		panel.add(jCheckMiddleTitle);
		label = new JLabel("  ");
		panel.add(label);
		propValue = props.getProperty("TocPage");
		jCheckTocPage = new JCheckBox("目次", propValue!=null&"1".equals(propValue));
		jCheckTocPage.setFocusPainted(false);
		panel.add(jCheckTocPage);
		label = new JLabel("(");
		panel.add(label);
		buttonGroup = new ButtonGroup();
		propValue = props.getProperty("TocVertical");
		jRadioTocV = new JRadioButton("縦書き ", propValue==null||"1".equals(propValue));
		jRadioTocV.setFocusPainted(false);
		jRadioTocV.setBorder(padding0);
		panel.add(jRadioTocV);
		buttonGroup.add(jRadioTocV);
		jRadioTocH = new JRadioButton("横書き", !(propValue==null||"1".equals(propValue)));
		jRadioTocH.setFocusPainted(false);
		jRadioTocH.setBorder(padding0);
		panel.add(jRadioTocH);
		buttonGroup.add(jRadioTocH);
		label = new JLabel(")");
		panel.add(label);
		
		
		////////////////////////////////
		//出力ファイル設定
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setPreferredSize(panelSize);
		panel.setBorder(padding4H2V);
		tabPanel.add(panel);
		//拡張子
		label = new JLabel("拡張子: ");
		panel.add(label);
		propValue = props.getProperty("Ext");
		jComboExt = new JComboBox(new String[]{".epub", ".kepub.epub"});
		jComboExt.setEditable(true);
		jComboExt.setMaximumSize(new Dimension(110, 24));
		jComboExt.setPreferredSize(new Dimension(110, 24));
		jComboExt.setSelectedItem(propValue==null||propValue.length()==0?".epub":propValue);
		panel.add(jComboExt);
		label = new JLabel("  ");
		panel.add(label);
		//出力ファイル名設定
		propValue = props.getProperty("AutoFileName");
		jCheckAutoFileName = new JCheckBox("出力ファイル名に表題利用", propValue==null||"1".equals(propValue));
		jCheckAutoFileName.setFocusPainted(false);
		panel.add(jCheckAutoFileName);
		label = new JLabel("  ");
		panel.add(label);
		//ファイルの上書き許可
		propValue = props.getProperty("OverWrite");
		jCheckOverWrite = new JCheckBox("ePubファイル上書き", propValue==null||"1".equals(propValue));
		jCheckOverWrite.setFocusPainted(false);
		panel.add(jCheckOverWrite);
		
		////////////////////////////////
		//出力先
		////////////////////////////////
		dstPathChooser = new DstPathChooserListener(this);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setPreferredSize(panelSize);
		panel.setBorder(padding4H2V);
		tabPanel.add(panel);
		//出力先
		label = new JLabel("出力先: ");
		panel.add(label);
		Vector<String> vecDstPath = new Vector<String>();
		vecDstPath.add("[入力ファイルと同じ場所]");
		propValue = props.getProperty("DstPathList");
		if (propValue!=null&&propValue.length()>0) {
			for (String dstPath : propValue.split(",")) { vecDstPath.add(dstPath); }
		}
		jComboDstPath = new JComboBox(vecDstPath);
		jComboDstPath.setEditable(true);
		jComboDstPath.setPreferredSize(new Dimension(320, 24));
		propValue = props.getProperty("DstPath");
		if (propValue==null||propValue.length()==0) jComboDstPath.setSelectedIndex(0);
		else jComboDstPath.setSelectedItem(propValue);
		panel.add(jComboDstPath);
		new DropTarget(jComboDstPath.getEditor().getEditorComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new DropDstPathListener(), true);
		jButtonDstPath = new JButton("選択");
		jButtonDstPath.setBorder(padding3V);
		jButtonDstPath.setPreferredSize(new Dimension(56, 24));
		jButtonDstPath.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/dst_path.png")));
		jButtonDstPath.setFocusable(false);
		jButtonDstPath.addActionListener(dstPathChooser);
		panel.add(jButtonDstPath);
		
		////////////////////////////////
		//変換オプション
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setPreferredSize(panelSize);
		panel.setBorder(padding4H2V);
		tabPanel.add(panel);
		//栞用ID出力
		propValue = props.getProperty("MarkId");
		jCheckMarkId = new JCheckBox("栞用ID出力", propValue==null||"1".equals(propValue));
		jCheckMarkId.setToolTipText("Kobo向けの栞を記憶するためのIDを各行に設定します");
		jCheckMarkId.setFocusPainted(false);
		panel.add(jCheckMarkId);
		//4バイト文字を変換する
		propValue = props.getProperty("Gaiji32");
		jCheckGaiji32 = new JCheckBox("4バイト文字変換", "1".equals(propValue));
		jCheckGaiji32.setToolTipText("Kobo等で4バイト文字より後ろが表示できない場合はチェックを外します");
		jCheckGaiji32.setFocusPainted(false);
		panel.add(jCheckGaiji32);
		//縦書き横書き
		buttonGroup = new ButtonGroup();
		propValue = props.getProperty("Vertical");
		jRadioVertical = new JRadioButton("縦書き", propValue==null||"1".equals(propValue));
		jRadioVertical.setFocusPainted(false);
		jRadioVertical.setBorder(BorderFactory.createEmptyBorder(0, 80, 0, 0));
		panel.add(jRadioVertical);
		buttonGroup.add(jRadioVertical);
		jRadioHorizontal = new JRadioButton("横書き", !(propValue==null||"1".equals(propValue)));
		jRadioHorizontal.setFocusPainted(false);
		panel.add(jRadioHorizontal);
		buttonGroup.add(jRadioHorizontal);
		
		////////////////////////////////
		/*group = new ButtonGroup();
		propValue = props.getProperty("RtL");
		boolean propLtR = propValue==null||"1".equals(propValue);
		jRadioRtL = new JRadioButton("右→左", propLtR);
		jRadioRtL.setFocusPainted(false);
		panel.add(jRadioRtL);
		group.add(jRadioRtL);
		jRadioLtR = new JRadioButton("左→右", !propLtR);
		jRadioLtR.setFocusPainted(false);
		panel.add(jRadioLtR);
		group.add(jRadioLtR);*/
		
		////////////////////////////////
		//変換
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMaximumSize(new Dimension(1920, 40));
		panel.setPreferredSize(new Dimension(1920, 40));
		panel.setBorder(padding4H);
		tabPanel.add(panel);
		//左パネル
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel1.setPreferredSize(panelSize);
		panel1.setBorder(padding0);
		//入力文字コード
		label = new JLabel("入力文字コード");
		label.setBorder(padding0);
		panel1.add(label);
		jComboEncType = new JComboBox(new String[]{"MS932", "UTF-8"});
		jComboEncType.setToolTipText("入力ファイルのテキストファイルの文字コード 青空文庫の標準はMS932(SJIS) 外字等変換済テキストはUTF-8を選択");
		jComboEncType.setFocusable(false);
		jComboEncType.setPreferredSize(new Dimension(100, 22));
		((JLabel)jComboEncType.getRenderer()).setBorder(padding2H);
		try { jComboEncType.setSelectedIndex(Integer.parseInt(props.getProperty("EncType"))); } catch (Exception e) {}
		panel1.add(jComboEncType);
		panel.add(panel1);
		//右パネル
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel2.setPreferredSize(new Dimension(640, 40));
		panel.setBorder(padding0);
		//開く
		jButtonFile = new JButton("ファイル選択");
		jButtonFile.setToolTipText("ファイル選択後に変換処理を開始します");
		jButtonFile.setBorder(padding3V);
		jButtonFile.setPreferredSize(new Dimension(100, 24));
		jButtonFile.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/convert.png")));
		jButtonFile.setFocusable(false);
		jButtonFile.addActionListener(new FileChooserListener(this));
		panel2.add(jButtonFile);
		//変換前に確認する
		propValue = props.getProperty("ChkConfirm");
		jCheckConfirm = new JCheckBox("変換前確認", propValue==null||"1".equals(propValue));
		jCheckConfirm.setToolTipText("変換前にタイトルと表紙の設定が可能な確認画面を表示します");
		jCheckConfirm.setFocusPainted(false);
		jCheckConfirm.setBorder(padding0);
		panel2.add(jCheckConfirm);
		panel.add(panel2);
		
		////////////////////////////////////////////////////////////////
		//Tab 画像設定
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.add("画像設定", tabPanel);
		
		////////////////////////////////
		//画面サイズ
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画面サイズ"));
		tabPanel.add(panel);
		//画面サイズ
		label = new JLabel("横");
		panel.add(label);
		propValue = props.getProperty("DispW");
		jTextDispW = new JTextField(propValue==null?"600":propValue);
		jTextDispW.setHorizontalAlignment(JTextField.RIGHT);
		jTextDispW.setInputVerifier(new IntegerInputVerifier(1));
		jTextDispW.setMaximumSize(text4);
		jTextDispW.setPreferredSize(text4);
		panel.add(jTextDispW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		propValue = props.getProperty("DispH");
		jTextDispH = new JTextField(propValue==null?"800":propValue);
		jTextDispH.setHorizontalAlignment(JTextField.RIGHT);
		jTextDispH.setInputVerifier(new IntegerInputVerifier(1));
		jTextDispH.setMaximumSize(text4);
		jTextDispH.setPreferredSize(text4);
		panel.add(jTextDispH);
		label = new JLabel("px");
		label.setBorder(padding2H);
		panel.add(label);
		
		////////////////////////////////
		//画像単ページ
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画像単ページ化"));
		tabPanel.add(panel);
		//横x縦
		label = new JLabel("横");
		panel.add(label);
		propValue = props.getProperty("SinglePageSizeW");
		jTextSinglePageSizeW = new JTextField(propValue==null?"400":propValue);
		jTextSinglePageSizeW.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageSizeW.setInputVerifier(new IntegerInputVerifier(0));
		jTextSinglePageSizeW.setMaximumSize(text4);
		jTextSinglePageSizeW.setPreferredSize(text4);
		panel.add(jTextSinglePageSizeW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		propValue = props.getProperty("SinglePageSizeH");
		jTextSinglePageSizeH = new JTextField(propValue==null?"600":propValue);
		jTextSinglePageSizeH.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageSizeH.setInputVerifier(new IntegerInputVerifier(0));
		jTextSinglePageSizeH.setMaximumSize(text4);
		jTextSinglePageSizeH.setPreferredSize(text4);
		panel.add(jTextSinglePageSizeH);
		label = new JLabel("px以上  ");
		label.setBorder(padding2H);
		panel.add(label);
		//横のみ
		label = new JLabel("横のみ");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("SinglePageWidth");
		jTextSinglePageWidth = new JTextField(propValue==null?"600":propValue);
		jTextSinglePageWidth.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageWidth.setInputVerifier(new IntegerInputVerifier(0));
		jTextSinglePageWidth.setMaximumSize(text4);
		jTextSinglePageWidth.setPreferredSize(text4);
		panel.add(jTextSinglePageWidth);
		label = new JLabel("px以上  ");
		label.setBorder(padding2H);
		panel.add(label);
		//拡大しない
		propValue = props.getProperty("FitImage");
		jCheckFitImage = new JCheckBox("小さい画像を拡大表示", propValue==null||"1".equals(propValue));
		jCheckFitImage.setToolTipText("画面サイズより小さい画像も画面に合うように幅または高さを100%に設定します");
		jCheckFitImage.setFocusPainted(false);
		jCheckFitImage.setBorder(padding0);
		panel.add(jCheckFitImage);
		
		////////////////////////////////
		//画像縮小指定
		////////////////////////////////
		ChangeListener resizeChangeLister = new ChangeListener() {
			public void stateChanged(ChangeEvent e) { setResizeTextEditable(true);  }
		};
		JPanel panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("画像縮小"));
		tabPanel.add(panelV);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4B);
		panelV.add(panel);
		//ピクセル
		propValue = props.getProperty("Pixel");
		jCheckPixel = new JCheckBox("画素数", propValue==null||"1".equals(propValue));
		jCheckPixel.setFocusPainted(false);
		jCheckPixel.setBorder(padding2H);
		jCheckPixel.addChangeListener(resizeChangeLister);
		panel.add(jCheckPixel);
		propValue = props.getProperty("PixelW");
		jTextPixelW = new JTextField(propValue==null?"1600":propValue);
		jTextPixelW.setHorizontalAlignment(JTextField.RIGHT);
		jTextPixelW.setInputVerifier(new IntegerInputVerifier(100));
		jTextPixelW.setMaximumSize(text4);
		jTextPixelW.setPreferredSize(text4);
		panel.add(jTextPixelW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("PixelH");
		jTextPixelH = new JTextField(propValue==null?"1600":propValue);
		jTextPixelH.setHorizontalAlignment(JTextField.RIGHT);
		jTextPixelH.setInputVerifier(new IntegerInputVerifier(100));
		jTextPixelH.setMaximumSize(text4);
		jTextPixelH.setPreferredSize(text4);
		panel.add(jTextPixelH);
		label = new JLabel("px以下  ");
		label.setBorder(padding2H);
		panel.add(label);
		//横
		propValue = props.getProperty("ResizeW");
		jCheckResizeW = new JCheckBox("横", propValue!=null&&"1".equals(propValue));
		jCheckResizeW.setFocusPainted(false);
		jCheckResizeW.setBorder(padding2H);
		jCheckResizeW.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeW);
		propValue = props.getProperty("ResizeNumW");
		jTextResizeW = new JTextField(propValue==null?"1600":propValue);
		jTextResizeW.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeW.setInputVerifier(new IntegerInputVerifier(100));
		jTextResizeW.setMaximumSize(text4);
		jTextResizeW.setPreferredSize(text4);
		jTextResizeW.setEditable(jCheckResizeW.isSelected());
		panel.add(jTextResizeW);
		label = new JLabel("px以下  ");
		label.setBorder(padding2H);
		panel.add(label);
		//縦
		propValue = props.getProperty("ResizeH");
		jCheckResizeH = new JCheckBox("縦", propValue!=null&"1".equals(propValue));
		jCheckResizeH.setFocusPainted(false);
		jCheckResizeH.setBorder(padding2H);
		jCheckResizeH.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeH);
		propValue = props.getProperty("ResizeNumH");
		jTextResizeH = new JTextField(propValue==null?"1600":propValue);
		jTextResizeH.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeH.setInputVerifier(new IntegerInputVerifier(100));
		jTextResizeH.setMaximumSize(text4);
		jTextResizeH.setPreferredSize(text4);
		panel.add(jTextResizeH);
		label = new JLabel("px以下");
		label.setBorder(padding2H);
		panel.add(label);
		this.setResizeTextEditable(true);
		
		////////////////////////////////
		//表紙サイズ
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding0);
		panelV.add(panel);
		//横x縦
		label = new JLabel("表紙画像: 横");
		panel.add(label);
		propValue = props.getProperty("CoverW");
		jTextCoverW = new JTextField(propValue==null?"600":propValue);
		jTextCoverW.setHorizontalAlignment(JTextField.RIGHT);
		jTextCoverW.setInputVerifier(new IntegerInputVerifier(32));
		jTextCoverW.setMaximumSize(text4);
		jTextCoverW.setPreferredSize(text4);
		panel.add(jTextCoverW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		propValue = props.getProperty("CoverH");
		jTextCoverH = new JTextField(propValue==null?"800":propValue);
		jTextCoverH.setHorizontalAlignment(JTextField.RIGHT);
		jTextCoverH.setInputVerifier(new IntegerInputVerifier(64));
		jTextCoverH.setMaximumSize(text4);
		jTextCoverH.setPreferredSize(text4);
		panel.add(jTextCoverH);
		label = new JLabel("px以下");
		label.setBorder(padding2H);
		panel.add(label);
		
		////////////////////////////////
		//Jpeg圧縮率
		////////////////////////////////
		//横
		label = new JLabel("   Jpeg圧縮率:");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("JpegQuality");
		jTextJpegQuality = new JTextField(propValue==null?"80":propValue);
		jTextJpegQuality.setHorizontalAlignment(JTextField.RIGHT);
		jTextJpegQuality.setInputVerifier(new IntegerInputVerifier(30, 100));
		jTextJpegQuality.setMaximumSize(text3);
		jTextJpegQuality.setPreferredSize(text3);
		panel.add(jTextJpegQuality);
		label = new JLabel(" (30～100) ");
		label.setBorder(padding2H);
		panel.add(label);
		
		////////////////////////////////////////////////////////////////
		//Tab 詳細設定
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.add("詳細設定", tabPanel);
		
		////////////////////////////////
		//詳細設定
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("文中全角スペースの処理"));
		tabPanel.add(panel);
		//ピクセル
		propValue = props.getProperty("SpaceHyphenation");
		buttonGroup = new ButtonGroup();
		jRadioSpaceHyp1 = new JRadioButton("行末非表示 (Kobo・Kindle用)  ", propValue==null||"1".equals(propValue));
		jRadioSpaceHyp1.setToolTipText("Readerでは何もしないと同じ表示になります");
		jRadioSpaceHyp1.setFocusPainted(false);
		jRadioSpaceHyp1.setBorder(padding2H2T3B);
		panel.add(jRadioSpaceHyp1);
		buttonGroup.add(jRadioSpaceHyp1);
		jRadioSpaceHyp2 = new JRadioButton("行末非表示 (Reader用)  ", "2".equals(propValue));
		jRadioSpaceHyp2.setToolTipText("Reader以外では追い出しの禁則処理になります");
		jRadioSpaceHyp2.setFocusPainted(false);
		jRadioSpaceHyp2.setBorder(padding2H2T3B);
		panel.add(jRadioSpaceHyp2);
		buttonGroup.add(jRadioSpaceHyp2);
		jRadioSpaceHyp0 = new JRadioButton("何もしない", "0".equals(propValue));
		jRadioSpaceHyp0.setToolTipText("行の折り返し部分にある全角スペースが行頭に表示されます");
		jRadioSpaceHyp0.setFocusPainted(false);
		jRadioSpaceHyp0.setBorder(padding2H2T3B);
		panel.add(jRadioSpaceHyp0);
		buttonGroup.add(jRadioSpaceHyp0);
		
		////////////////////////////////
		//自動縦中横
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("自動縦中横"));
		tabPanel.add(panel);
		//半角2文字縦書き
		propValue = props.getProperty("AutoYoko");
		jCheckAutoYoko = new JCheckBox("自動縦中横有効 ", propValue==null||"1".equals(propValue));
		jCheckAutoYoko.setFocusPainted(false);
		jCheckAutoYoko.setToolTipText("半角の2文字の数字、2～3文字の!?を縦中横で表示します。(前後に半角が無い場合)");
		jCheckAutoYoko.setBorder(padding2H2T3B);
		panel.add(jCheckAutoYoko);
		label = new JLabel("数字:");
		label.setBorder(padding2H2T3B);
		panel.add(label);
		//半角数字1文字縦書き
		propValue = props.getProperty("AutoYokoNum1");
		jCheckAutoYokoNum1 = new JCheckBox("1桁 ", "1".equals(propValue));
		jCheckAutoYokoNum1.setFocusPainted(false);
		jCheckAutoYokoNum1.setBorder(padding2H2T3B);
		panel.add(jCheckAutoYokoNum1);
		//半角数字3文字縦書き
		propValue = props.getProperty("AutoYokoNum3");
		jCheckAutoYokoNum3 = new JCheckBox("3桁", "1".equals(propValue));
		jCheckAutoYokoNum3.setFocusPainted(false);
		jCheckAutoYokoNum3.setBorder(padding2H2T3B);
		panel.add(jCheckAutoYokoNum3);
		
		////////////////////////////////
		//コメント出力
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("コメントブロック出力"));
		tabPanel.add(panel);
		//半角2文字縦書き
		propValue = props.getProperty("CommentPrint");
		jCheckCommentPrint = new JCheckBox("コメント出力 ", "1".equals(propValue));
		jCheckCommentPrint.setToolTipText("コメント行の間を出力します");
		jCheckCommentPrint.setFocusPainted(false);
		jCheckCommentPrint.setBorder(padding2H2T3B);
		panel.add(jCheckCommentPrint);
		//半角2文字縦書き
		propValue = props.getProperty("CommentConvert");
		jCheckCommentConvert = new JCheckBox("コメント内注記変換", "1".equals(propValue));
		jCheckCommentConvert.setToolTipText("コメント内の注記を変換します");
		jCheckCommentConvert.setFocusPainted(false);
		jCheckCommentConvert.setBorder(padding2H2T3B);
		panel.add(jCheckCommentConvert);
		
		////////////////////////////////
		//強制改ページ
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("強制改ページ"));
		tabPanel.add(panel);
		
		jComboxPageBreak = new JComboBox(new String[]{"しない", "50", "100", "150", "200", "300", "500"});
		jComboxPageBreak.setEditable(true);
		jComboxPageBreak.setToolTipText("このKB数を超えた場合に改ページ処理を行います（タグの外側でのみ改ページされます）");
		//jComboxPageBreak.setFocusable(false);
		jComboxPageBreak.setPreferredSize(new Dimension(60, 19));
		((JLabel)jComboxPageBreak.getRenderer()).setBorder(padding2H2T3B);
		propValue = props.getProperty("PageBreak");
		if (propValue!=null) jComboxPageBreak.setSelectedItem(propValue);
		panel.add(jComboxPageBreak);
		label = new JLabel("KBを超えた場合 ");
		label.setBorder(padding2H2T3B);
		panel.add(label);
		propValue = props.getProperty("PageBreakEmpty");
		jCheckPageBreakEmpty = new JCheckBox("空行のみ", "1".equals(propValue));
		jCheckPageBreakEmpty.setFocusPainted(false);
		jCheckPageBreakEmpty.setBorder(padding2H2T3B);
		panel.add(jCheckPageBreakEmpty);
		jComboxPageBreakEmptyLine = new JComboBox(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
		jComboxPageBreakEmptyLine.setFocusable(false);
		jComboxPageBreakEmptyLine.setPreferredSize(new Dimension(45, 19));
		((JLabel)jComboxPageBreakEmptyLine.getRenderer()).setBorder(padding2H2T3B);
		propValue = props.getProperty("PageBreakEmptyLine");
		jComboxPageBreakEmptyLine.setSelectedItem(propValue==null?"2":propValue);
		panel.add(jComboxPageBreakEmptyLine);
		label = new JLabel("行以上 ");
		label.setBorder(padding2H2T3B);
		panel.add(label);
		propValue = props.getProperty("PageBreakChapter");
		jCheckPageBreakChapter = new JCheckBox("見出しのみ", "1".equals(propValue));
		jCheckPageBreakChapter.setFocusPainted(false);
		jCheckPageBreakChapter.setBorder(padding2H2T3B);
		panel.add(jCheckPageBreakChapter);
		
		////////////////////////////////////////////////////////////////
		//Tab 目次設定
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.add("目次設定", tabPanel);
		
		////////////////////////////////
		//目次設定
		////////////////////////////////
		//目次出力
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("目次出力"));
		tabPanel.add(panel);
		
		//最大文字数
		label = new JLabel(" 最大文字数");
		label.setBorder(padding2H2T3B);
		panel.add(label);
		propValue = props.getProperty("MaxChapterNameLength");
		jTextMaxChapterNameLength = new JTextField(propValue==null?"64":propValue);
		jTextMaxChapterNameLength.setHorizontalAlignment(JTextField.RIGHT);
		jTextMaxChapterNameLength.setInputVerifier(new IntegerInputVerifier(1, 999));
		jTextMaxChapterNameLength.setMaximumSize(text3);
		jTextMaxChapterNameLength.setPreferredSize(text3);
		panel.add(jTextMaxChapterNameLength);
		
		label = new JLabel("  ");
		label.setBorder(padding2H2T3B);
		panel.add(label);
		
		//表紙
		propValue = props.getProperty("CoverPageToc");
		jCheckCoverPageToc = new JCheckBox("表紙  ", "1".equals(propValue));
		jCheckCoverPageToc.setToolTipText("表紙画像のページを目次を追加します");
		jCheckCoverPageToc.setFocusPainted(false);
		jCheckCoverPageToc.setBorder(padding2H2T3B);
		panel.add(jCheckCoverPageToc);
		
		propValue = props.getProperty("ChapterExclude");
		jCheckChapterExclude = new JCheckBox("連続する見出しを除外", propValue==null||"1".equals(propValue));
		jCheckChapterExclude.setToolTipText("3つ以上連続する自動抽出された見出しを除外します(空行1行間隔も連続扱い)");
		jCheckChapterExclude.setFocusPainted(false);
		jCheckChapterExclude.setBorder(padding2H2T3B);
		panel.add(jCheckChapterExclude);
		
		//目次抽出
		panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("目次抽出"));
		tabPanel.add(panelV);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(padding4B);
		panelV.add(panel);
		//改ページ後を目次に追加
		propValue = props.getProperty("ChapterSection");
		jCheckChapterSection = new JCheckBox("改ページ後 ", propValue==null||"1".equals(propValue));
		jCheckChapterSection.setToolTipText("改ページ後の先頭行の文字を目次に出力します");
		jCheckChapterSection.setFocusPainted(false);
		jCheckChapterSection.setBorder(padding2H2T3B);
		panel.add(jCheckChapterSection);
		//見出し注記
		label = new JLabel(" 注記(");
		label.setBorder(padding2H2T3B);
		panel.add(label);
		propValue = props.getProperty("ChapterH");
		jCheckChapterH = new JCheckBox("見出し ", propValue==null||"1".equals(propValue));
		jCheckChapterH.setFocusPainted(false);
		jCheckChapterH.setBorder(padding2H2T3B);
		panel.add(jCheckChapterH);
		propValue = props.getProperty("ChapterH1");
		jCheckChapterH1 = new JCheckBox("大見出し ", propValue==null||"1".equals(propValue));
		jCheckChapterH1.setFocusPainted(false);
		jCheckChapterH1.setBorder(padding2H2T3B);
		panel.add(jCheckChapterH1);
		propValue = props.getProperty("ChapterH2");
		jCheckChapterH2 = new JCheckBox("中見出し ", propValue==null||"1".equals(propValue));
		jCheckChapterH2.setFocusPainted(false);
		jCheckChapterH2.setBorder(padding2H2T3B);
		panel.add(jCheckChapterH2);
		propValue = props.getProperty("ChapterH3");
		jCheckChapterH3 = new JCheckBox("小見出し )", propValue==null||"1".equals(propValue));
		jCheckChapterH3.setFocusPainted(false);
		jCheckChapterH3.setBorder(padding2H2T3B);
		panel.add(jCheckChapterH3);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(padding4B);
		panelV.add(panel);
		propValue = props.getProperty("ChapterName");
		jCheckChapterName = new JCheckBox("章見出し (第～章/その～/～章/序/プロローグ 等)", propValue==null||"1".equals(propValue));
		jCheckChapterName.setToolTipText("第～話/第～章/第～篇/第～部/第～節/第～幕/その～/～章/プロローグ/エピローグ/序/序章/終章/間章/幕間");
		jCheckChapterName.setFocusPainted(false);
		jCheckChapterName.setBorder(padding2H2T3B);
		panel.add(jCheckChapterName);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panelV.add(panel);
		propValue = props.getProperty("ChapterNumOnly");
		jCheckChapterNumOnly = new JCheckBox("数字のみ", "1".equals(propValue));
		jCheckChapterNumOnly.setFocusPainted(false);
		jCheckChapterNumOnly.setBorder(padding2H2T3B);
		panel.add(jCheckChapterNumOnly);
		propValue = props.getProperty("ChapterNum");
		jCheckChapterNum = new JCheckBox("数字+見出し  ", "1".equals(propValue));
		jCheckChapterNum.setFocusPainted(false);
		jCheckChapterNum.setBorder(padding2H2T3B);
		panel.add(jCheckChapterNum);
		propValue = props.getProperty("ChapterNumParen");
		jCheckChapterNumParen = new JCheckBox("数字(括弧内)", "1".equals(propValue));
		jCheckChapterNumParen.setFocusPainted(false);
		jCheckChapterNumParen.setBorder(padding2H2T3B);
		panel.add(jCheckChapterNumParen);
		propValue = props.getProperty("ChapterNumParenTitle");
		jCheckChapterNumParenTitle = new JCheckBox("数字(括弧内)+見出し", "1".equals(propValue));
		jCheckChapterNumParenTitle.setFocusPainted(false);
		jCheckChapterNumParenTitle.setBorder(padding2H2T3B);
		panel.add(jCheckChapterNumParenTitle);
		
		////////////////////////////////////////////////////////////////
		//テキストエリア
		////////////////////////////////////////////////////////////////
		jTextArea = new JTextArea("青空文庫テキストをここにドラッグ＆ドロップまたは「ファイル選択」で変換します。\n");
		jTextArea.setEditable(false);
		jTextArea.setFont(new Font("Default", Font.PLAIN, 12));
		jTextArea.setBorder(new LineBorder(Color.white, 3));
		new DropTarget(jTextArea, DnDConstants.ACTION_COPY_OR_MOVE, new DropListener(), true);
		
		jScrollPane = new JScrollPane(jTextArea);
		this.add(jScrollPane);
		
		
		////////////////////////////////////////////////////////////////
		//確認ダイアログ
		jConfirmDialog = new JConfirmDialog((JApplet)this,
			iconImage, AozoraEpub3Applet.class.getResource("images/icon.png").toString().replaceFirst("/icon\\.png", "/")
		);
		
		////////////////////////////////////////////////////////////////
		//ログ出力先を設定
		LogAppender.setTextArea(jTextArea);
		
		//初期化
		try {
			//ePub出力クラス初期化
			this.epub3Writer = new Epub3Writer("template/");
			//ePub画像出力クラス初期化
			this.epub3ImageWriter = new Epub3ImageWriter("template/");
			
			//変換テーブルをstaticに生成
			this.aozoraConverter = new AozoraEpub3Converter(this.epub3Writer);
			
		} catch (IOException e) {
			e.printStackTrace();
			jTextArea.append(e.getMessage());
		}
	}
	
	class IntegerInputVerifier extends InputVerifier
	{
		/** 最小値 */
		int min = Integer.MIN_VALUE;
		/** 最大値 */
		int max = Integer.MAX_VALUE;
		
		IntegerInputVerifier(int min)
		{
			this.min = min;
		}
		IntegerInputVerifier(int min, int max)
		{
			this.min = min;
			this.max = max;
		}
		@Override
		public boolean verify(JComponent c)
		{
			boolean verified = false;
			JTextField textField = (JTextField)c;
			try{
				int i = Integer.parseInt(textField.getText());
				if (i >= this.min && i <= this.max) verified = true;
				else UIManager.getLookAndFeel().provideErrorFeedback(c);
			} catch (NumberFormatException e) {
				UIManager.getLookAndFeel().provideErrorFeedback(c);
			}
			return verified;
		}
	}
	
	/** 表紙選択ボタンイベント */
	class CoverChooserListener implements ActionListener
	{
		Component parent;
		private CoverChooserListener(Component parent)
		{
			this.parent = parent;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser fileChooser = new JFileChooser(currentPath);
			fileChooser.setDialogTitle("表紙画像を選択");
			fileChooser.setApproveButtonText("選択");
			fileChooser.setFileFilter(new FileNameExtensionFilter("表紙画像(jpg,png,gif)", new String[]{"jpg","png","gif"}));
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				jComboCover.setSelectedItem(fileChooser.getSelectedFile().getAbsolutePath());
			}
		}
	}
	/** 表紙画像ドラッグ＆ドロップイベント */
	class DropCoverListener implements DropTargetListener
	{
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {}
		@Override
		public void dragOver(DropTargetDragEvent dtde) {}
		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {}
		@Override
		public void dragExit(DropTargetEvent dte) {}
		@Override
		public void drop(DropTargetDropEvent dtde)
		{
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable transfer = dtde.getTransferable();
			try {
				DataFlavor[] flavars = transfer.getTransferDataFlavors();
				if (flavars.length == 0) return;
				if (flavars[0].isFlavorJavaFileListType()) {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() > 0) {
						jComboCover.setSelectedItem(files.get(0).getAbsolutePath());
					}
				} else {
					for (DataFlavor flavar : flavars) {
						if (flavar.isFlavorTextType()) jComboCover.setSelectedItem(transfer.getTransferData(DataFlavor.stringFlavor));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}
	
	/** 出力先ドラッグ＆ドロップイベント */
	class DropDstPathListener implements DropTargetListener
	{
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {}
		@Override
		public void dragOver(DropTargetDragEvent dtde) {}
		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {}
		@Override
		public void dragExit(DropTargetEvent dte) {}
		@Override
		public void drop(DropTargetDropEvent dtde)
		{
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable transfer = dtde.getTransferable();
			try {
				DataFlavor[] flavars = transfer.getTransferDataFlavors();
				if (flavars.length == 0) return;
				if (flavars[0].isFlavorJavaFileListType()) {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() > 0) {
						File file = files.get(0);
						if (!file.isDirectory()) file = file.getParentFile();
						jComboDstPath.setSelectedItem(file.getAbsolutePath());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}
	
	/** 出力先選択ボタンイベント */
	class DstPathChooserListener implements ActionListener
	{
		Component parent;
		private DstPathChooserListener(Component parent)
		{
			this.parent = parent;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			//キャッシュパスならnullにする
			File path = currentPath;
			File selectedPath = new File((String)jComboDstPath.getEditor().getItem());
			if (selectedPath.isDirectory()) path = selectedPath;
			else if (selectedPath.isFile()) path = selectedPath.getParentFile();
			JFileChooser fileChooser = new JFileChooser(path);
			fileChooser.setDialogTitle("出力先を選択");
			fileChooser.setApproveButtonText("選択");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				String pathString = fileChooser.getSelectedFile().getAbsolutePath();
				jComboDstPath.setSelectedItem(pathString);
			}
		}
	}
	
	/** ファイル選択ボタンイベント */
	class FileChooserListener implements ActionListener
	{
		Component parent;
		private FileChooserListener(Component parent)
		{
			this.parent = parent;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (isRunning()) return;
			
			JFileChooser fileChooser = new JFileChooser(currentPath);
			fileChooser.setDialogTitle("変換する青空文庫テキストを開く");
			fileChooser.setFileFilter(new FileNameExtensionFilter("青空文庫テキスト(txt,zip,cbz)", new String[]{"txt","zip","cbz"}));
			fileChooser.setMultiSelectionEnabled(true);
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				//convertFiles(fileChooser.getSelectedFiles());
				//Worker初期化
				ConvertFilesWorker convertFilesWorker = new ConvertFilesWorker(fileChooser.getSelectedFiles());
				convertFilesWorker.execute();
			}
		}
	}
	/** ドラッグ＆ドロップイベント
	 * 複数ファイルに対応 */
	class DropListener implements DropTargetListener
	{
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {}
		@Override
		public void dragOver(DropTargetDragEvent dtde) {}
		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {}
		@Override
		public void dragExit(DropTargetEvent dte) {}
		@Override
		public void drop(DropTargetDropEvent dtde)
		{
			if (isRunning()) return;
			
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable transfer = dtde.getTransferable();
			try {
				DataFlavor[] flavars = transfer.getTransferDataFlavors();
				if (flavars.length == 0) return;
				if (flavars[0].isFlavorJavaFileListType()) {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) transfer.getTransferData(DataFlavor.javaFileListFlavor);
					
					//convertFiles((File[])(files.toArray()));
					ConvertFilesWorker convertFilesWorker = new ConvertFilesWorker((File[])(files.toArray()));
					convertFilesWorker.execute();
				} else {
					for (DataFlavor flavar : flavars) {
						if (flavar.isFlavorTextType()) {
							//URLかどうか
							String urlString = transfer.getTransferData(DataFlavor.stringFlavor).toString();
							if (urlString.startsWith("http")) {
								//出力先が指定されていない
								if (jComboDstPath.getSelectedIndex() == 0) {
									dstPathChooser.actionPerformed(null);
									if (jComboDstPath.getSelectedIndex() == 0) {
										LogAppender.append("変換処理を中止しました\n");
										return;
									}
								}
								if (cachePath == null) {
									//キャッシュパス
									cachePath = new File(".cache");
									cachePath .mkdir();
								}
								//出力先 URLと同じパス
								String path = cachePath.getAbsolutePath()+"/"+urlString.substring(urlString.indexOf("//")+2).replaceAll("\\?\\*\\&\\|\\<\\>\"\\\\", "_");
								File file = new File(path);
								file.getParentFile().mkdirs();
								//ダウンロード
								BufferedInputStream bis = new BufferedInputStream(new URL(urlString).openStream(), 8192);
								BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
								IOUtils.copy(bis, bos);
								bos.close();
								bis.close();
								ConvertFilesWorker convertFilesWorker = new ConvertFilesWorker(new File[]{file});
								convertFilesWorker.execute();
								return;
							}
							//JOptionPane.showMessageDialog(jFrameParent, "");
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
			}
		}
	}
	
	/** 変換実行時出力先リストの先頭に追加または移動 */
	protected void addDstPath()
	{
		//パス指定がなければ終了
		if (this.jComboDstPath.getSelectedIndex() == 0) return;
		String dstPath = this.jComboDstPath.getEditor().getItem().toString().trim();
		if (dstPath.equals("")) return;
		
		int count = Math.min(10, this.jComboDstPath.getItemCount());
		for (int i=1; i<count; i++) {
			String item = (String)this.jComboDstPath.getItemAt(i);
			if (dstPath.equals(item)) {
				//先頭に移動して終了
				this.jComboDstPath.removeItemAt(i);
				this.jComboDstPath.insertItemAt(item, 1);
				this.jComboDstPath.setSelectedIndex(1);
				return;
			}
		}
		//ファイルがあれば先頭に追加
		this.jComboDstPath.insertItemAt(dstPath, 1);
		this.jComboDstPath.setSelectedIndex(1);
	}
	
	////////////////////////////////////////////////////////////////
	/** 複数ファイルを変換 */
	private void convertFiles(File[] srcFiles)
	{
		if (srcFiles.length == 0 ) return;
		
		//テキスト入力を確定
		//jComboExt.transferFocusUpCycle();
		//jComboCover.transferFocusUpCycle();
		//jComboDstPath.transferFocusUpCycle();
		
		convertCanceled = false;
		
		//共通パラメータ取得
		//出力先取得
		File dstPath = null;
		if (jComboDstPath.getSelectedIndex() != 0) {
			dstPath = new File(jComboDstPath.getEditor().getItem().toString());
			if (!dstPath.isDirectory()) {
				String dstPathName = dstPath.getAbsolutePath();
				if (dstPathName.length() > 70) dstPathName = dstPathName.substring(0, 32)+" ... "+dstPathName.substring(dstPathName.length()-32);
				int ret = JOptionPane.showConfirmDialog(jConfirmDialog, "出力先がありません\n"+dstPathName+"\nにフォルダを作成しますか？", "出力先確認", JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					//フォルダ作成
					dstPath.mkdirs();
				} else {
					LogAppender.append("変換処理を中止しました\n");
					return;
				}
			}
		}
		//jComboDstPathに出力先履歴保存
		this.addDstPath();
		
		//入力先がファイルなら現在パスに設定
		if (cachePath == null || !srcFiles[0].getAbsolutePath().startsWith(cachePath.getAbsolutePath())) currentPath = srcFiles[0].getParentFile();
		if (currentPath == null) currentPath = dstPath;
		
		//自動改ページ
		int forcePageBreak = 0;
		int forcePageBreakEmpty = 0;
		int forcePageBreakChapter = 0;
		Pattern pattern = null;
		try {
			forcePageBreak = Integer.parseInt(jComboxPageBreak.getEditor().getItem().toString().trim()) * 1024;
			if (jCheckPageBreakEmpty.isSelected()) {
				forcePageBreakEmpty = Integer.parseInt(jComboxPageBreakEmptyLine.getSelectedItem().toString());
			}
			forcePageBreakChapter = jCheckPageBreakChapter.isSelected() ? 1 : 0;
		} catch (Exception e) {}
		
		////////////////////////////////
		//Appletのパラメータを取得しておく
		//画面サイズと画像リサイズ
		int resizeW = 0;
		if (jCheckResizeW.isSelected()) try { resizeW = Integer.parseInt(jTextResizeW.getText()); } catch (Exception e) {}
		int resizeH = 0;
		if (jCheckResizeH.isSelected()) try { resizeH = Integer.parseInt(jTextResizeH.getText()); } catch (Exception e) {}
		int pixels = 0;
		if (jCheckPixel.isSelected()) try { pixels = Integer.parseInt(jTextPixelW.getText())*Integer.parseInt(jTextPixelH.getText()); } catch (Exception e) {}
		int dispW = Integer.parseInt(jTextDispW.getText());
		int dispH = Integer.parseInt(jTextDispH.getText());
		int singlePageSizeW = Integer.parseInt(jTextSinglePageSizeW.getText());
		int singlePageSizeH = Integer.parseInt(jTextSinglePageSizeH.getText());
		int singlePageWidth = Integer.parseInt(jTextSinglePageWidth.getText());
		this.coverW = Integer.parseInt(this.jTextCoverW.getText());
		this.coverH = Integer.parseInt(this.jTextCoverH.getText());
		float jpegQualty = Integer.parseInt(jTextJpegQuality.getText())/100f;
		this.epub3Writer.setImageParam(dispW, dispH, resizeW, resizeH, pixels, singlePageSizeW, singlePageSizeH, singlePageWidth, jCheckFitImage.isSelected(), coverW, coverH, jpegQualty);
		this.epub3ImageWriter.setImageParam(dispW, dispH, resizeW, resizeH, pixels, singlePageSizeW, singlePageSizeH, singlePageWidth, jCheckFitImage.isSelected(), coverW, coverH, jpegQualty);
		
		try {
			this.aozoraConverter.setForcePageBreak(forcePageBreak, forcePageBreakEmpty, forcePageBreakChapter, pattern);
			//栞用ID出力
			this.aozoraConverter.setWithMarkId(this.jCheckMarkId.isSelected());
			//変換オプション設定
			this.aozoraConverter.setAutoYoko(this.jCheckAutoYoko.isSelected(), this.jCheckAutoYokoNum1.isSelected(), this.jCheckAutoYokoNum3.isSelected());
			//4バイト文字出力
			this.aozoraConverter.setGaiji32(this.jCheckGaiji32.isSelected());
			//表題左右中央
			this.aozoraConverter.setMiddleTitle(this.jCheckMiddleTitle.isSelected());
			//全角スペースの禁則
			this.aozoraConverter.setSpaceHyphenation(this.jRadioSpaceHyp0.isSelected()?0:(this.jRadioSpaceHyp1.isSelected()?1:2));
			//コメント
			this.aozoraConverter.setCommentPrint(this.jCheckCommentPrint.isSelected(), this.jCheckCommentConvert.isSelected());
			
			int maxLength = 64;
			try { maxLength = Integer.parseInt((jTextMaxChapterNameLength.getText())); } catch (Exception e) {}
			this.aozoraConverter.setChapterLevel(maxLength, jCheckChapterExclude.isSelected(), jCheckChapterSection.isSelected(),
					jCheckChapterH.isSelected(), jCheckChapterH1.isSelected(), jCheckChapterH2.isSelected(), jCheckChapterH3.isSelected(),
					jCheckChapterName.isSelected(),
					jCheckChapterNumOnly.isSelected(), jCheckChapterNum.isSelected(), jCheckChapterNumParen.isSelected(), jCheckChapterNumParenTitle.isSelected());
			
			////////////////////////////////
			//すべてのファイルの変換実行
			_convertFiles(srcFiles, dstPath);
		
		} catch (Exception e) { e.printStackTrace(); }
		////////////////////////////////
		System.gc();
		
	}
	/** サブディレクトリ再帰用 */
	private void _convertFiles(File[] srcFiles, File dstPath)
	{
		for (File srcFile : srcFiles) {
			if (srcFile.isDirectory()) {
				//サブディレクトリ 再帰
				_convertFiles(srcFile.listFiles(), dstPath);
			} else if (srcFile.isFile()) {
				convertFile(srcFile, dstPath);
			}
			//キャンセル
			if (convertCanceled)
				return;
		}
	}
	
	/** 内部用変換関数 Appletの設定を引数に渡す
	 * TODO 共通パラメータは事前に取得しておく */
	private void convertFile(File srcFile, File dstPath)
	{
		//パラメータ設定
		
		LogAppender.append("--------\n");
		//拡張子
		String ext = srcFile.getName();
		ext = ext.substring(ext.lastIndexOf('.')+1).toLowerCase();
		
		if (!"txt".equals(ext) && !"zip".equals(ext) && !"cbz".equals(ext) ) {
			LogAppender.append("txt, zip, cbz 以外は変換できません\n");
			return;
		}
		//表紙にする挿絵の位置-1なら挿絵は使わない
		int coverImageIndex = -1;
		//表紙情報追加
		String coverFileName = this.jComboCover.getEditor().getItem().toString();
		if (coverFileName.equals(this.jComboCover.getItemAt(0).toString())) {
			coverFileName = ""; //先頭の挿絵
			coverImageIndex = 0;
		} else if (coverFileName.equals(this.jComboCover.getItemAt(1).toString())) {
			coverFileName = AozoraEpub3.getSameCoverFileName(srcFile); //入力ファイルと同じ名前+.jpg/.png
		} else if (coverFileName.equals(this.jComboCover.getItemAt(2).toString())) {
			coverFileName = null; //表紙無し
		}
		
		//BookInfo取得
		BookInfo bookInfo = null;
		
		boolean isFile = "txt".equals(ext);
		ImageInfoReader imageInfoReader = new ImageInfoReader(isFile, srcFile);
		try {
			//cbzは画像のみ
			if (!"cbz".equals(ext)) {
				InputStream is = AozoraEpub3.getInputStream(srcFile, ext, imageInfoReader);
				//テキストファイルからメタ情報や画像単独ページ情報を取得
				bookInfo = AozoraEpub3.getBookInfo(
					is, imageInfoReader, this.aozoraConverter,
					this.jComboEncType.getSelectedItem().toString(),
					BookInfo.TitleType.indexOf(this.jComboTitle.getSelectedIndex())
				);
			}
		} catch (Exception e) {
			LogAppender.append("[ERROR] ファイルが読み込めませんでした\n");
			return;
		}
		
		Epub3Writer writer = this.epub3Writer;
		try {
			if (!"txt".equals(ext)) {
				//Zip内の画像情報読み込み
				imageInfoReader.loadZipImageInfos(srcFile, bookInfo == null);
				if (bookInfo == null) {
					LogAppender.append("画像のみのePubファイルを生成します\n");
					//画像出力用のBookInfo生成
					bookInfo = new BookInfo();
					bookInfo.imageOnly = true;
					String[] titleCreator = AozoraEpub3.getFileTitleCreator(srcFile.getName());
					if (titleCreator[0] != null) bookInfo.title = titleCreator[0];
					if (titleCreator[1] != null) bookInfo.creator = titleCreator[1];
					//Writerを画像出力用派生クラスに入れ替え
					writer = this.epub3ImageWriter;
					
					if (imageInfoReader.countImageFiles() == 0) {
						LogAppender.append("[ERROR] 画像がありませんでした\n");
						return;
					}
					
					//名前順で並び替え
					imageInfoReader.sortImageFileNames();
					//先頭画像を取得してbookInfoに設定
					if (coverImageIndex != -1) {
						bookInfo.coverImage = imageInfoReader.getImage(0);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.append("[ERROR] "+e+"\n");
		}
		
		if (bookInfo == null) {
			LogAppender.append("[ERROR] 書籍の情報が取得できませんでした\n");
			return;
		}
		
		//表紙目次ページ出力設定
		bookInfo.insertCoverPage = this.jCheckCoverPage.isSelected();
		bookInfo.insertCoverPageToc = this.jCheckCoverPageToc.isSelected();
		bookInfo.insertTocPage = this.jCheckTocPage.isSelected();
		//目次縦書き
		bookInfo.setTocVertical(this.jRadioTocV.isSelected());
		//縦書き横書き設定追加
		bookInfo.vertical = this.jRadioVertical.isSelected();
		
		//表紙ページの情報をbookInfoに設定
		bookInfo.coverFileName = coverFileName;
		bookInfo.coverImageIndex = coverImageIndex;
		
		//確認ページ 変換ボタン押下時にbookInfo更新
		if (this.jCheckConfirm.isSelected()) {
			//表題と著者設定 ファイル名から設定
			String[] titleCreator = AozoraEpub3.getFileTitleCreator(srcFile.getName());
			String title = "";
			String creator = "";
			if (titleCreator[0] != null) title = titleCreator[0];
			if (titleCreator[1] != null) creator = titleCreator[1];
			if (!jCheckUserFileName.isSelected()) {
				if (bookInfo.title !=null) title = bookInfo.title;
				if (bookInfo.creator !=null) creator = bookInfo.creator;
			}
			this.jConfirmDialog.showDialog(
				srcFile.getName(),
				(dstPath!=null ? dstPath.getAbsolutePath() : srcFile.getParentFile().getAbsolutePath())+File.separator,
				title, creator, bookInfo, imageInfoReader, this.jFrameParent.getLocation() 
			);
			
			//ダイアログが閉じた後に再開
			if (this.jConfirmDialog.canceled) {
				this.convertCanceled = true;
				LogAppender.append("変換処理を中止しました : "+srcFile.getAbsolutePath()+"\n");
				return;
			}
			if (this.jConfirmDialog.skipped) {
				LogAppender.append("変換をスキップしました : "+srcFile.getAbsolutePath()+"\n");
				return;
			}
			
			//変換前確認のチェックを反映
			if (!this.jConfirmDialog.jCheckConfirm2.isSelected()) jCheckConfirm.setSelected(false);
			
			//確認ダイアログの値をBookInfoに設定
			bookInfo.title = this.jConfirmDialog.jTextTitle.getText().trim();
			bookInfo.creator = this.jConfirmDialog.jTextCreator.getText().trim();
			//著者が空欄なら著者行もクリア
			if (bookInfo.creator.length() == 0) bookInfo.creatorLine = -1;
			
			//プレビューでトリミングされていたらbookInfo.coverImageにBufferedImageを設定 それ以外はnullにする
			BufferedImage coverImage = this.jConfirmDialog.jCoverImagePanel.getModifiedImage(this.coverW, this.coverH);
			if (coverImage != null) {
				//Epub3Writerでイメージを出力
				bookInfo.coverImage = coverImage;
				bookInfo.coverFileName = null;
				//元の表紙は残す
				if (this.jConfirmDialog.jCheckReplaceCover.isSelected()) bookInfo.coverImageIndex = -1;
			} else {
				bookInfo.coverImage = null;
			}
			
		} else {
			//確認なしなのでnullでなければbookInfo上書き
			String[] titleCreator = AozoraEpub3.getFileTitleCreator(srcFile.getName());
			if (jCheckUserFileName.isSelected()) {
				//ファイル名優先ならテキスト側の情報は不要
				bookInfo.title = "";
				bookInfo.creator = "";
				if (titleCreator[0] != null) bookInfo.title = titleCreator[0];
				if (titleCreator[1] != null) bookInfo.creator = titleCreator[1];
			} else {
				//テキストから取得できなければファイル名を利用
				if (bookInfo.title == null || bookInfo.title.length() == 0) bookInfo.title = titleCreator[0];
				if (bookInfo.creator == null || bookInfo.creator.length() == 0) bookInfo.creator = titleCreator[1];
			}
		}
		
		boolean autoFileName = this.jCheckAutoFileName.isSelected();
		boolean overWrite = this.jCheckOverWrite.isSelected();
		String outExt = this.jComboExt.getEditor().getItem().toString().trim();
		
		//出力ファイル
		File outFile = AozoraEpub3.getOutFile(srcFile, dstPath, bookInfo, autoFileName, outExt);
		
		//上書き確認
		if (!overWrite &&  outFile.exists()) {
			LogAppender.append("変換中止: "+srcFile.getAbsolutePath()+"\n");
			LogAppender.append("ファイルが存在します: "+outFile.getAbsolutePath()+"\n");
			return;
		}
		/*
		if (overWrite &&  outFile.exists()) {
			int ret = JOptionPane.showConfirmDialog(this, "ファイルが存在します\n上書きしますか？\n(取り消しで変換キャンセル)", "上書き確認", JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret == JOptionPane.NO_OPTION) {
				LogAppender.append("変換中止: "+srcFile.getAbsolutePath()+"\n");
				return;
			} else if (ret == JOptionPane.CANCEL_OPTION) {
				LogAppender.append("変換中止: "+srcFile.getAbsolutePath()+"\n");
				convertCanceled = true;
				LogAppender.append("変換処理をキャンセルしました\n");
				return;
			}
		}*/
		
		AozoraEpub3.convertFile(
			srcFile, ext, outFile,
			this.aozoraConverter,
			writer,
			this.jComboEncType.getSelectedItem().toString(),
			bookInfo, imageInfoReader
		);
		
		//終了処理
		bookInfo.clear();
		bookInfo = null;
		imageInfoReader = null;
		//System.gc();
	}
	
	////////////////////////////////////////////////////////////////
	/** 別スレッド実行用SwingWorker */
	class ConvertFilesWorker extends SwingWorker<Object, Object>
	{
		/** 面倒なのでAppletを渡す */
		AozoraEpub3Applet applet;
		/** 変換対象ファイル */
		File[] srcFiles;
		
		public ConvertFilesWorker(File[] srcFiles)
		{
			this.applet = getApplet();
			this.srcFiles = srcFiles;
		}
		
		@Override
		protected Object doInBackground() throws Exception
		{
			this.applet.running = true;
			applet.setConvertEnabled(false);
			try {
				applet.convertFiles(srcFiles);
			} finally {
				applet.setConvertEnabled(true);
				this.applet.running = false;
			}
			return null;
		}
		
		@Override
		protected void done()
		{
			super.done();
			applet.setConvertEnabled(true);
			this.applet.running = false;
		}
	}
	/** イベント内でWorker初期化する用 */
	private AozoraEpub3Applet getApplet()
	{
		return this;
	}
	/** Worker実行中フラグ取得 */
	private boolean isRunning()
	{
		return this.running;
	}
	/** 変換中に操作不可にするコンポーネントのenabledを設定 */
	private void setConvertEnabled(boolean enabled)
	{
		this.jComboTitle.setEnabled(enabled);
		this.jCheckAutoFileName.setEnabled(enabled);
		this.jCheckMiddleTitle.setEnabled(enabled);
		
		this.jComboCover.setEnabled(enabled);
		this.jButtonCover.setEnabled(enabled);
		this.jCheckCoverPage.setEnabled(enabled);
		this.jCheckTocPage.setEnabled(enabled);
		this.jRadioTocV.setEnabled(enabled);
		this.jRadioTocH.setEnabled(enabled);
		
		this.jComboExt.setEnabled(enabled);
		this.jCheckUserFileName.setEnabled(enabled);
		this.jCheckOverWrite.setEnabled(enabled);
		
		this.jComboDstPath.setEnabled(enabled);
		this.jButtonDstPath.setEnabled(enabled);
		
		this.jCheckMarkId.setEnabled(enabled);
		this.jCheckGaiji32.setEnabled(enabled);
		this.jRadioVertical.setEnabled(enabled);
		this.jRadioHorizontal.setEnabled(enabled);
		
		this.jComboEncType.setEnabled(enabled);
		this.jButtonFile.setEnabled(enabled);
		
		this.jTextDispW.setEnabled(enabled);
		this.jTextDispH.setEnabled(enabled);
		this.jTextSinglePageSizeW.setEnabled(enabled);
		this.jTextSinglePageSizeH.setEnabled(enabled);
		this.jTextSinglePageWidth.setEnabled(enabled);
		this.jCheckFitImage.setEnabled(enabled);
		
		this.jCheckResizeW.setEnabled(enabled);
		this.jCheckResizeH.setEnabled(enabled);
		this.jCheckPixel.setEnabled(enabled);
		this.setResizeTextEditable(enabled);
		this.jTextResizeW.setEnabled(enabled);
		this.jTextResizeH.setEnabled(enabled);
		this.jTextPixelW.setEnabled(enabled);
		this.jTextPixelH.setEnabled(enabled);
		
		this.jTextCoverW.setEnabled(enabled);
		this.jTextCoverH.setEnabled(enabled);
		this.jTextJpegQuality.setEnabled(enabled);
		
		this.jRadioSpaceHyp0.setEnabled(enabled);
		this.jRadioSpaceHyp1.setEnabled(enabled);
		this.jRadioSpaceHyp2.setEnabled(enabled);
		
		this.jCheckAutoYoko.setEnabled(enabled);
		this.jCheckAutoYokoNum1.setEnabled(enabled);
		this.jCheckAutoYokoNum3.setEnabled(enabled);
		
		this.jCheckCommentPrint.setEnabled(enabled);
		this.jCheckCommentConvert.setEnabled(enabled);
		
		this.jComboxPageBreak.setEnabled(enabled);
		this.jCheckPageBreakEmpty.setEnabled(enabled);
		this.jComboxPageBreakEmptyLine.setEnabled(enabled);
		this.jCheckPageBreakChapter.setEnabled(enabled);
		
		this.jCheckCoverPageToc.setEnabled(enabled);
		this.jCheckChapterExclude.setEnabled(enabled);
		this.jCheckChapterSection.setEnabled(enabled);
		this.jCheckChapterH.setEnabled(enabled);
		this.jCheckChapterH1.setEnabled(enabled);
		this.jCheckChapterH2.setEnabled(enabled);
		this.jCheckChapterH3.setEnabled(enabled);
		this.jCheckChapterName.setEnabled(enabled);
		this.jCheckChapterNum.setEnabled(enabled);
		this.jCheckChapterNumOnly.setEnabled(enabled);
		this.jCheckChapterNumParen.setEnabled(enabled);
		this.jCheckChapterNumParenTitle.setEnabled(enabled);
		this.jTextMaxChapterNameLength.setEnabled(enabled);
	}
	
	private void setResizeTextEditable(boolean enabled)
	{
		if (enabled) {
			this.jTextResizeW.setEditable(jCheckResizeW.isSelected());
			this.jTextResizeH.setEditable(jCheckResizeH.isSelected());
			this.jTextPixelW.setEditable(jCheckPixel.isSelected());
			this.jTextPixelH.setEditable(jCheckPixel.isSelected());
		} else {
			this.jTextResizeW.setEditable(false);
			this.jTextResizeH.setEditable(false);
			this.jTextPixelW.setEditable(false);
			this.jTextPixelH.setEditable(false);
		}
	}
	
	//ディレクトリ以下を削除  パス注意
	private void deleteFiles(File path)
	{
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory()) deleteFiles(file);
				else if (file.isFile()) file.delete();
			}
			path.delete();
		}
	}
	
	////////////////////////////////////////////////////////////////
	/** Jar実行用 */
	public static void main(String args[])
	{
		//LookAndFeel変更
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) { e.printStackTrace(); }
		
		final AozoraEpub3Applet applet = new AozoraEpub3Applet();
		applet.iconImage = java.awt.Toolkit.getDefaultToolkit().createImage(AozoraEpub3Applet.class.getResource("images/icon.png"));
		applet.init();
		
		//フレーム初期化
		final JFrame jFrame = new JFrame("青空文庫テキスト → ePub3変換");
		applet.setFrame(jFrame);
		//アイコン設定
		jFrame.setIconImage(applet.iconImage);
		//最小サイズ
		jFrame.setMinimumSize(new Dimension(500, 320));
		jFrame.setPreferredSize(new Dimension(500, 320));
		
		try {
			int x = (int)Float.parseFloat(applet.props.getProperty("PosX"));
			int y = (int)Float.parseFloat(applet.props.getProperty("PosY"));
			jFrame.setLocation(x, y);
		} catch (Exception e) {}
		
		jFrame.setSize(applet.getSize());
		try {
			int w = (int)Float.parseFloat(applet.props.getProperty("SizeW"));
			int h = (int)Float.parseFloat(applet.props.getProperty("SizeH"));
			jFrame.setSize(w, h);
		} catch (Exception e) {}
		
		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				try {
					//Window位置を割くに設定しておく
					Point location = jFrame.getLocation();
					Dimension size = jFrame.getSize();
					applet.props.setProperty("PosX", ""+location.getX());
					applet.props.setProperty("PosY", ""+location.getY());
					applet.props.setProperty("SizeW", ""+size.getWidth());
					applet.props.setProperty("SizeH", ""+size.getHeight());
					//props保存と終了処理
					applet.finalize();
				} catch (Throwable e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
		});
		jFrame.add(applet);
		jFrame.setVisible(true);
	}
	
	/** アプレット終了時の処理
	 * 設定ファイルを保存 */
	@Override
	protected void finalize() throws Throwable
	{
		this.convertCanceled = true;
		
		try {
			//tmp削除
			if (tmpPath != null) this.deleteFiles(this.tmpPath);
			//キャッシュファイル削除
			if (cachePath != null) this.deleteFiles(this.cachePath);
		} catch (Exception e) { e.printStackTrace(); }
		
		//アップレット設定の保存
		this.props.setProperty("TitleType", ""+this.jComboTitle.getSelectedIndex());
		this.props.setProperty("UseFileName", this.jCheckUserFileName.isSelected()?"1":"");
		this.props.setProperty("AutoFileName", this.jCheckAutoFileName.isSelected()?"1":"");
		//出力先と履歴保存
		if (this.jComboDstPath.getSelectedIndex() == 0) this.props.setProperty("DstPath","");
		else {
			String dstPath = this.jComboDstPath.getEditor().getItem().toString().trim();
			this.props.setProperty("DstPath", ""+dstPath);
			//履歴
			String dstPathList = this.props.getProperty("DstPathList");
			if (dstPathList == null) dstPathList = dstPath;
			else {
				//最大10件
				dstPathList = dstPath;
				int count = Math.min(10, this.jComboDstPath.getItemCount());
				for (int i=1; i<count; i++) {
					String item = (String)this.jComboDstPath.getItemAt(i);
					if (!dstPath.equals(item)) dstPathList += ","+item;
				}
			}
			this.props.setProperty("DstPathList", dstPathList);
		}
		//変換設定
		this.props.setProperty("MarkId", this.jCheckMarkId.isSelected()?"1":"");
		this.props.setProperty("Gaiji32", this.jCheckGaiji32.isSelected()?"1":"");
		this.props.setProperty("MiddleTitle", this.jCheckMiddleTitle.isSelected()?"1":"");
		this.props.setProperty("Vertical", this.jRadioVertical.isSelected()?"1":"");
		//this.props.setProperty("RtL", this.jRadioRtL.isSelected()?"1":"");
		this.props.setProperty("Ext", ""+this.jComboExt.getEditor().getItem().toString().trim());
		this.props.setProperty("ChkConfirm", this.jCheckConfirm.isSelected()?"1":"");
		
		//先頭の挿絵と表紙無しのみ記憶
		if (this.jComboCover.getSelectedIndex() == 0) this.props.setProperty("Cover","");
		else if (this.jComboCover.getSelectedIndex() == 1) this.props.setProperty("Cover", ""+this.jComboCover.getEditor().getItem().toString().trim());
		this.props.setProperty("CoverPage", this.jCheckCoverPage.isSelected()?"1":"");
		this.props.setProperty("TocPage", this.jCheckTocPage.isSelected()?"1":"");
		this.props.setProperty("TocVertical", this.jRadioTocV.isSelected()?"1":"");
		//画面サイズ
		this.props.setProperty("DispW", this.jTextDispW.getText());
		this.props.setProperty("DispH", this.jTextDispH.getText());
		//画像単ページ
		this.props.setProperty("SinglePageSizeW", this.jTextSinglePageSizeW.getText());
		this.props.setProperty("SinglePageSizeH", this.jTextSinglePageSizeH.getText());
		this.props.setProperty("SinglePageWidth", this.jTextSinglePageWidth.getText());
		this.props.setProperty("FitImage", this.jCheckFitImage.isSelected()?"1":"");
		//画像サイズ
		this.props.setProperty("ResizeW", this.jCheckResizeW.isSelected()?"1":"");
		this.props.setProperty("ResizeH", this.jCheckResizeH.isSelected()?"1":"");
		this.props.setProperty("Pixel", this.jCheckPixel.isSelected()?"1":"");
		this.props.setProperty("ResizeNumW", this.jTextResizeW.getText());
		this.props.setProperty("ResizeNumH", this.jTextResizeH.getText());
		this.props.setProperty("PixelW", this.jTextPixelW.getText());
		this.props.setProperty("PixelH", this.jTextPixelH.getText());
		//表紙
		this.props.setProperty("CoverW", this.jTextCoverW.getText());
		this.props.setProperty("CoverH", this.jTextCoverH.getText());
		//JPEG画質
		this.props.setProperty("JpegQuality", this.jTextJpegQuality.getText());
		//空白の禁則処理
		this.props.setProperty("SpaceHyphenation", this.jRadioSpaceHyp0.isSelected()?"0":(this.jRadioSpaceHyp1.isSelected()?"1":"2"));
		//自動縦中横
		this.props.setProperty("AutoYoko", this.jCheckAutoYoko.isSelected()?"1":"");
		this.props.setProperty("AutoYokoNum1", this.jCheckAutoYokoNum1.isSelected()?"1":"");
		this.props.setProperty("AutoYokoNum3", this.jCheckAutoYokoNum3.isSelected()?"1":"");
		//コメント出力
		this.props.setProperty("CommentPrint", this.jCheckCommentPrint.isSelected()?"1":"");
		this.props.setProperty("CommentConvert", this.jCheckCommentConvert.isSelected()?"1":"");
		//強制改ページ
		this.props.setProperty("PageBreak", ""+this.jComboxPageBreak.getSelectedItem().toString().trim());
		this.props.setProperty("PageBreakEmpty", this.jCheckPageBreakEmpty.isSelected()?"1":"");
		this.props.setProperty("PageBreakEmptyLine", ""+this.jComboxPageBreakEmptyLine.getSelectedItem().toString().trim());
		this.props.setProperty("PageBreakChapter", this.jCheckPageBreakChapter.isSelected()?"1":"");
		//目次出力
		this.props.setProperty("MaxChapterNameLength", this.jTextMaxChapterNameLength.getText());
		this.props.setProperty("CoverPageToc", this.jCheckCoverPageToc.isSelected()?"1":"");
		this.props.setProperty("ChapterExclude", this.jCheckChapterExclude.isSelected()?"1":"");
		this.props.setProperty("ChapterSection", this.jCheckChapterSection.isSelected()?"1":"");
		this.props.setProperty("ChapterH", this.jCheckChapterH.isSelected()?"1":"");
		this.props.setProperty("ChapterH1", this.jCheckChapterH1.isSelected()?"1":"");
		this.props.setProperty("ChapterH2", this.jCheckChapterH2.isSelected()?"1":"");
		this.props.setProperty("ChapterH3", this.jCheckChapterH3.isSelected()?"1":"");
		this.props.setProperty("ChapterName", this.jCheckChapterName.isSelected()?"1":"");
		this.props.setProperty("ChapterNum", this.jCheckChapterNum.isSelected()?"1":"");
		this.props.setProperty("ChapterNumOnly", this.jCheckChapterNumOnly.isSelected()?"1":"");
		this.props.setProperty("ChapterNumParen", this.jCheckChapterNumParen.isSelected()?"1":"");
		this.props.setProperty("ChapterNumParenTitle", this.jCheckChapterNumParenTitle.isSelected()?"1":"");
				
		this.props.setProperty("EncType", ""+this.jComboEncType.getSelectedIndex());
		this.props.setProperty("OverWrite", this.jCheckOverWrite.isSelected()?"1":"");
		this.props.setProperty("LastDir", this.currentPath==null?"":this.currentPath.getAbsolutePath());
		//設定ファイル更新
		this.props.store(new FileOutputStream(this.propFileName), "AozoraEpub3 Parameters");
		
		super.finalize();
	}
}
