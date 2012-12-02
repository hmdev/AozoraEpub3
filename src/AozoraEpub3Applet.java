import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

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
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.compress.utils.IOUtils;


import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.image.ImageInfoReader;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.swing.JConfirmDialog;
import com.github.hmdev.swing.NarrowTitledBorder;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.web.WebAozoraConverter;
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
	/** アプリケーションのアイコン画像 */
	Image iconImage;
	
	/** 上下分割Split */
	JSplitPane jSplitPane;
	
	/** 上部タブパネル */
	JTabbedPane jTabbedPane;
	
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
	
	//画像関連
	/** 挿絵なし */
	JCheckBox jCheckNoIllust;
	/** 画面幅 */
	JTextField jTextDispW;
	/** 画面高さ */
	JTextField jTextDispH;
	//表紙サイズ
	JTextField jTextCoverW;
	JTextField jTextCoverH;
	
	JTextField jTextSinglePageSizeW;
	JTextField jTextSinglePageSizeH;
	JTextField jTextSinglePageWidth;
	JCheckBox jCheckFitImage;
	
	JComboBox jComboImageFloatType;
	JTextField jTextImageFloatW;
	JTextField jTextImageFloatH;
	
	//画像縮小
	JCheckBox jCheckResizeW;
	JTextField jTextResizeNumW;
	JCheckBox jCheckResizeH;
	JTextField jTextResizeNumH;
	//JCheckBox jCheckPixel;
	//JTextField jTextPixelW;
	//JTextField jTextPixelH;
	//Jpeg圧縮率
	JTextField jTextJpegQuality;
	//余白除去
	JCheckBox jCheckAutoMargin;
	JTextField jTextAutoMarginLimitH;
	JTextField jTextAutoMarginLimitV;
	JTextField jTextAutoMarginWhiteLevel;
	JTextField jTextAutoMarginPadding;
	JComboBox jComboAutoMarginNombre;
	
	JRadioButton jRadioSpaceHyp0;
	JRadioButton jRadioSpaceHyp1;
	JRadioButton jRadioSpaceHyp2;
	
	JCheckBox jCheckAutoYoko;
	JCheckBox jCheckAutoYokoNum1;
	JCheckBox jCheckAutoYokoNum3;
	JCheckBox jCheckAutoEQ1;
	
	JCheckBox jCheckCommentPrint;
	JCheckBox jCheckCommentConvert;
	
	JComboBox jComboxRemoveEmptyLine;
	
	JTextField jTextMaxChapterNameLength;
	JCheckBox jCheckCoverPageToc;
	JCheckBox jCheckChapterSection;
	JCheckBox jCheckChapterH;
	JCheckBox jCheckChapterH1;
	JCheckBox jCheckChapterH2;
	JCheckBox jCheckChapterH3;
	JCheckBox jCheckChapterName;
	JCheckBox jCheckChapterNameTitle;
	JCheckBox jCheckChapterNumTitle;
	JCheckBox jCheckChapterNumOnly;
	JCheckBox jCheckChapterNumParen;
	JCheckBox jCheckChapterNumParenTitle;
	JCheckBox jCheckChapterUseNextLine;
	JCheckBox jCheckChapterExclude;
	JCheckBox jCheckChapterPattern;
	JComboBox jComboChapterPattern;
	
	JCheckBox jCheckPageBreak;
	JTextField jTextPageBreakSize;
	JCheckBox jCheckPageBreakEmpty;
	JComboBox jComboxPageBreakEmptyLine;
	JTextField jTextPageBreakEmptySize;
	JCheckBox jCheckPageBreakChapter;
	JTextField jTextPageBreakChapterSize;
	
	//テキストエリア
	//JScrollPane jScrollPane;
	JTextArea jTextArea;
	
	//プログレスバー
	JProgressBar jProgressBar;
	JButton jButtonCancel;
	
	/** 出力先選択ダイアログ表示イベントactionPerformed(null)で明示的に呼び出す。 */
	DstPathChooserListener dstPathChooser;
	
	
	/** 青空→ePub3変換クラス */
	AozoraEpub3Converter aozoraConverter;
	
	/** Web小説青空変換クラス */
	WebAozoraConverter webConverter;
	
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
	
	Process kindleProcess;
	
	/** 設定ファイル */
	Properties props;
	/** 設定ファイル名 */
	String propFileName = "AozoraEpub3.ini";
	
	/** jarファイルのあるパス文字列 "/"含む */
	String jarPath = null;
	
	/** 前回の出力パス */
	File currentPath = null;
	/** キャッシュ保存パス */
	File cachePath = null;
	/** RAR解凍先tmpパス */
	File tmpPath = null;
	/** Web小説取得情報格納パス */
	File webConfigPath = null;
	
	//UIパラメータ
	int coverW;
	int coverH;
	
	/** コンストラクタ */
	private AozoraEpub3Applet(JFrame parent)
	{
		super();
		this.jFrameParent = parent;
	}
	
	/** アプレット初期化 */
	@Override
	public void init()
	{
		super.init();
		this.setSize(new Dimension(520, 360));
		
		//パス関連初期化
		//this.jarPath = getClass().getClassLoader().getResource("").getFile();
		//this.jarPath = this.jarPath.replaceFirst("\\/bin\\/$", "/");
		//AppletではVelocityでパスがエラーになるのでとりあえず空文字に
		this.jarPath = "";
		
		this.cachePath = new File(this.jarPath+".cache");
		this.webConfigPath = new File(this.jarPath+"web");
		
		//設定ファイル読み込み
		props = new Properties(); 
		try {
			props.load(new FileInputStream(this.jarPath+propFileName));
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
		Border padding2 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		Border padding3B = BorderFactory.createEmptyBorder(0, 0, 3, 0);
		
		Dimension panelSize = new Dimension(1920, 26);
		JTextField text = new JTextField();
		Insets is = text.getInsets();
		FontMetrics fm = this.getFontMetrics(text.getFont());
		Dimension text3 = new Dimension(fm.stringWidth("000")+is.left+is.right+2, 19);
		Dimension text4 = new Dimension(fm.stringWidth("0000")+is.left+is.right+2, 19);
		Dimension text5 = new Dimension(fm.stringWidth("00000")+is.left+is.right+2, 19);
		Dimension text300 = new Dimension(300, 19);
		
		//アップレットのレイアウト設定
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		int dividerLocation = 220;
		try { dividerLocation = Integer.parseInt(props.getProperty("DividerLocation")); } catch (Exception e) {}
		jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		jSplitPane.setDividerLocation(dividerLocation);
		jSplitPane.setDividerSize(3);
		this.add(jSplitPane);
		
		jTabbedPane = new JTabbedPane();
		jTabbedPane.setBorder(padding2H);
		
		jSplitPane.add(jTabbedPane);
		
		ButtonGroup buttonGroup;
		
		int defaultValue;
		////////////////////////////////////////////////////////////////
		//Tab 変換
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
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
		jComboTitle.setBorder(padding0);
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
		try { jComboEncType.setSelectedIndex(Integer.parseInt(props.getProperty("EncType"))); } catch (Exception e) {}
		panel1.add(jComboEncType);
		panel.add(panel1);
		//右パネル
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel2.setPreferredSize(panelSize);
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
		//挿絵なし
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("挿絵画像"));
		tabPanel.add(panel);
		propValue = props.getProperty("NoIllust");
		jCheckNoIllust = new JCheckBox("挿絵なし ", "1".equals(propValue));
		jCheckNoIllust.setFocusPainted(false);
		jCheckNoIllust.setToolTipText("表紙と外字画像以外はePubに格納されません");
		jCheckNoIllust.setBorder(padding2);
		panel.add(jCheckNoIllust);
		
		////////////////////////////////
		//画面サイズ
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画面サイズ (縦横比と小さい画像の判別に利用)"));
		tabPanel.add(panel);
		//画面サイズ
		label = new JLabel("横");
		panel.add(label);
		propValue = props.getProperty("DispW");
		defaultValue = 600; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextDispW = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextDispW.setHorizontalAlignment(JTextField.RIGHT);
		jTextDispW.setMaximumSize(text4);
		jTextDispW.setPreferredSize(text4);
		jTextDispW.addFocusListener(new TextSelectFocusListener(jTextDispW));
		jTextDispW.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		panel.add(jTextDispW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		propValue = props.getProperty("DispH");
		defaultValue = 800; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextDispH = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextDispH.setHorizontalAlignment(JTextField.RIGHT);
		jTextDispH.setMaximumSize(text4);
		jTextDispH.setPreferredSize(text4);
		jTextDispH.addFocusListener(new TextSelectFocusListener(jTextDispH));
		jTextDispH.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		panel.add(jTextDispH);
		label = new JLabel("px");
		label.setBorder(padding2H);
		panel.add(label);
		////////////////////////////////
		//表紙サイズ
		//横x縦
		label = new JLabel("  表紙サイズ: 横");
		panel.add(label);
		propValue = props.getProperty("CoverW");
		defaultValue = 600; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextCoverW = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextCoverW.setHorizontalAlignment(JTextField.RIGHT);
		jTextCoverW.setInputVerifier(new IntegerInputVerifier(defaultValue, 64, 4096));
		jTextCoverW.setMaximumSize(text4);
		jTextCoverW.setPreferredSize(text4);
		jTextCoverW.addFocusListener(new TextSelectFocusListener(jTextCoverW));
		panel.add(jTextCoverW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		propValue = props.getProperty("CoverH");
		defaultValue = 800; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextCoverH = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextCoverH.setHorizontalAlignment(JTextField.RIGHT);
		jTextCoverH.setInputVerifier(new IntegerInputVerifier(defaultValue, 64, 4096));
		jTextCoverH.setMaximumSize(text4);
		jTextCoverH.setPreferredSize(text4);
		jTextCoverH.addFocusListener(new TextSelectFocusListener(jTextCoverH));
		panel.add(jTextCoverH);
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
		defaultValue = 400; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextSinglePageSizeW = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextSinglePageSizeW.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageSizeW.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextSinglePageSizeW.setMaximumSize(text4);
		jTextSinglePageSizeW.setPreferredSize(text4);
		jTextSinglePageSizeW.addFocusListener(new TextSelectFocusListener(jTextSinglePageSizeW));
		panel.add(jTextSinglePageSizeW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		propValue = props.getProperty("SinglePageSizeH");
		defaultValue = 600; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextSinglePageSizeH = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextSinglePageSizeH.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageSizeH.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextSinglePageSizeH.setMaximumSize(text4);
		jTextSinglePageSizeH.setPreferredSize(text4);
		jTextSinglePageSizeH.addFocusListener(new TextSelectFocusListener(jTextSinglePageSizeH));
		panel.add(jTextSinglePageSizeH);
		label = new JLabel("px以上  ");
		label.setBorder(padding2H);
		panel.add(label);
		//横のみ
		label = new JLabel("横のみ");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("SinglePageWidth");
		defaultValue = 600; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextSinglePageWidth = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextSinglePageWidth.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageWidth.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextSinglePageWidth.setMaximumSize(text4);
		jTextSinglePageWidth.setPreferredSize(text4);
		jTextSinglePageWidth.addFocusListener(new TextSelectFocusListener(jTextSinglePageWidth));
		panel.add(jTextSinglePageWidth);
		label = new JLabel("px以上  ");
		label.setBorder(padding2H);
		panel.add(label);
		//拡大しない
		propValue = props.getProperty("FitImage");
		jCheckFitImage = new JCheckBox("拡大表示", propValue==null||"1".equals(propValue));
		jCheckFitImage.setToolTipText("画面サイズより小さい画像を幅・高さに合わせて100%表示します");
		jCheckFitImage.setFocusPainted(false);
		jCheckFitImage.setBorder(padding0);
		panel.add(jCheckFitImage);
		
		////////////////////////////////
		//Jpeg圧縮率
		////////////////////////////////
		//横
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("Jpeg圧縮率"));
		tabPanel.add(panel);
		propValue = props.getProperty("JpegQuality");
		defaultValue = 80; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextJpegQuality = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextJpegQuality.setHorizontalAlignment(JTextField.RIGHT);
		jTextJpegQuality.setInputVerifier(new IntegerInputVerifier(defaultValue, 30, 100));
		jTextJpegQuality.setMaximumSize(text3);
		jTextJpegQuality.setPreferredSize(text3);
		jTextJpegQuality.addFocusListener(new TextSelectFocusListener(jTextJpegQuality));
		panel.add(jTextJpegQuality);
		label = new JLabel(" (30～100) ");
		label.setBorder(padding2H);
		panel.add(label);
		
		////////////////////////////////
		//画像回り込み
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画像回り込み"));
		tabPanel.add(panel);
		propValue = props.getProperty("ImageFloatType");
		jComboImageFloatType = new JComboBox(new String[]{"なし","上/左","下/右"});
		try { jComboImageFloatType.setSelectedIndex(Integer.parseInt(propValue)); } catch (Exception e) {}
		jComboImageFloatType.setToolTipText("64px以上で指定サイズ以下の画像を回り込み設定します");
		jComboImageFloatType.setFocusable(false);
		jComboImageFloatType.setPreferredSize(new Dimension(text4.width+24, 20));
		panel.add(jComboImageFloatType);
		label = new JLabel(" 横");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("ImageFloatW");
		defaultValue = 600; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextImageFloatW = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextImageFloatW.setHorizontalAlignment(JTextField.RIGHT);
		jTextImageFloatW.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextImageFloatW.setMaximumSize(text4);
		jTextImageFloatW.setPreferredSize(text4);
		jTextImageFloatW.addFocusListener(new TextSelectFocusListener(jTextImageFloatW));
		panel.add(jTextImageFloatW);
		label = new JLabel("x 縦");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("ImageFloatH");
		defaultValue = 400; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextImageFloatH = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextImageFloatH.setHorizontalAlignment(JTextField.RIGHT);
		jTextImageFloatH.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextImageFloatH.setMaximumSize(text4);
		jTextImageFloatH.setPreferredSize(text4);
		jTextImageFloatH.addFocusListener(new TextSelectFocusListener(jTextImageFloatH));
		panel.add(jTextImageFloatH);
		label = new JLabel("px以下");
		label.setBorder(padding2H);
		panel.add(label);
		
		////////////////////////////////
		//画像縮小指定
		////////////////////////////////
		ChangeListener resizeChangeLister = new ChangeListener() {
			public void stateChanged(ChangeEvent e) { setResizeTextEditable(true);  }
		};
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画像縮小"));
		tabPanel.add(panel);
		//ピクセル
		/*propValue = props.getProperty("Pixel");
		jCheckPixel = new JCheckBox("画素数", "1".equals(propValue));
		jCheckPixel.setFocusPainted(false);
		jCheckPixel.setBorder(padding2H);
		jCheckPixel.addChangeListener(resizeChangeLister);
		panel.add(jCheckPixel);
		propValue = props.getProperty("PixelW");
		defaultValue = 1600; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextPixelW = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextPixelW.setHorizontalAlignment(JTextField.RIGHT);
		jTextPixelW.setInputVerifier(new IntegerInputVerifier(defaultValue, 100, 9999));
		jTextPixelW.setMaximumSize(text4);
		jTextPixelW.setPreferredSize(text4);
		panel.add(jTextPixelW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("PixelH");
		defaultValue = 1600; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextPixelH = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextPixelH.setHorizontalAlignment(JTextField.RIGHT);
		jTextPixelH.setInputVerifier(new IntegerInputVerifier(defaultValue, 100, 9999));
		jTextPixelH.setMaximumSize(text4);
		jTextPixelH.setPreferredSize(text4);
		panel.add(jTextPixelH);
		label = new JLabel("px以下  ");
		label.setBorder(padding2H);
		panel.add(label);*/
		//横
		propValue = props.getProperty("ResizeW");
		jCheckResizeW = new JCheckBox("横", propValue==null||"1".equals(propValue));
		jCheckResizeW.setFocusPainted(false);
		jCheckResizeW.setBorder(padding2H);
		jCheckResizeW.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeW);
		propValue = props.getProperty("ResizeNumW");
		defaultValue = 2048; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextResizeNumW = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextResizeNumW.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeNumW.setInputVerifier(new IntegerInputVerifier(defaultValue, 100, 9999));
		jTextResizeNumW.setMaximumSize(text4);
		jTextResizeNumW.setPreferredSize(text4);
		jTextResizeNumW.addFocusListener(new TextSelectFocusListener(jTextResizeNumW));
		jTextResizeNumW.setEditable(jCheckResizeW.isSelected());
		panel.add(jTextResizeNumW);
		label = new JLabel("px以下 ");
		label.setBorder(padding2H);
		panel.add(label);
		//縦
		propValue = props.getProperty("ResizeH");
		jCheckResizeH = new JCheckBox("縦", propValue==null||"1".equals(propValue));
		jCheckResizeH.setFocusPainted(false);
		jCheckResizeH.setBorder(padding2H);
		jCheckResizeH.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeH);
		propValue = props.getProperty("ResizeNumH");
		defaultValue = 2048; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextResizeNumH = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextResizeNumH.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeNumH.setInputVerifier(new IntegerInputVerifier(defaultValue, 100, 9999));
		jTextResizeNumH.setMaximumSize(text4);
		jTextResizeNumH.setPreferredSize(text4);
		jTextResizeNumH.addFocusListener(new TextSelectFocusListener(jTextResizeNumH));
		panel.add(jTextResizeNumH);
		label = new JLabel("px以下");
		label.setBorder(padding2H);
		panel.add(label);
		this.setResizeTextEditable(true);
		
		////////////////////////////////
		//余白除去
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("余白除去"));
		tabPanel.add(panel);
		propValue = props.getProperty("AutoMargin");
		jCheckAutoMargin = new JCheckBox("有効","1".equals(propValue));
		jCheckAutoMargin.setFocusPainted(false);
		jCheckAutoMargin.setBorder(padding2H);
		jCheckAutoMargin.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){
			boolean selected = jCheckAutoMargin.isSelected();
			jTextAutoMarginLimitH.setEditable(selected);
			jTextAutoMarginLimitV.setEditable(selected);
			jTextAutoMarginWhiteLevel.setEditable(selected);
			jTextAutoMarginPadding.setEditable(selected);
		}});
		panel.add(jCheckAutoMargin);
		label = new JLabel("  横");
		label.setToolTipText("横方向の余白除去量の制限 左右の余白の合計");
		panel.add(label);
		propValue = props.getProperty("AutoMarginLimitH");
		defaultValue = 15; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextAutoMarginLimitH = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextAutoMarginLimitH.setToolTipText(label.getToolTipText());
		jTextAutoMarginLimitH.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginLimitH.setInputVerifier(new IntegerInputVerifier(defaultValue, 0, 50));
		jTextAutoMarginLimitH.setMaximumSize(text3);
		jTextAutoMarginLimitH.setPreferredSize(text3);
		jTextAutoMarginLimitH.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginLimitH.addFocusListener(new TextSelectFocusListener(jTextAutoMarginLimitH));
		panel.add(jTextAutoMarginLimitH);
		label = new JLabel("%");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel(" 縦");
		label.setToolTipText("縦方向の余白除去量の制限 上下の余白の合計");
		panel.add(label);
		propValue = props.getProperty("AutoMarginLimitV");
		defaultValue = 10; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextAutoMarginLimitV = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextAutoMarginLimitV.setToolTipText(label.getToolTipText());
		jTextAutoMarginLimitV.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginLimitV.setInputVerifier(new IntegerInputVerifier(defaultValue, 0, 50));
		jTextAutoMarginLimitV.setMaximumSize(text3);
		jTextAutoMarginLimitV.setPreferredSize(text3);
		jTextAutoMarginLimitV.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginLimitV.addFocusListener(new TextSelectFocusListener(jTextAutoMarginLimitV));
		panel.add(jTextAutoMarginLimitV);
		label = new JLabel("%");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("  白レベル");
		label.setToolTipText("余白部分の白い画素と判別するレベルを指定します (黒:0～白:100)");
		panel.add(label);
		propValue = props.getProperty("AutoMarginWhiteLevel");
		defaultValue = 80; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextAutoMarginWhiteLevel = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextAutoMarginWhiteLevel.setToolTipText(label.getToolTipText());
		jTextAutoMarginWhiteLevel.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginWhiteLevel.setInputVerifier(new IntegerInputVerifier(defaultValue, 0, 100));
		jTextAutoMarginWhiteLevel.setMaximumSize(text3);
		jTextAutoMarginWhiteLevel.setPreferredSize(text3);
		jTextAutoMarginWhiteLevel.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginWhiteLevel.addFocusListener(new TextSelectFocusListener(jTextAutoMarginWhiteLevel));
		panel.add(jTextAutoMarginWhiteLevel);
		label = new JLabel("%");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("  余白追加");
		label.setToolTipText("余白除去後に追加する余白の量(追加部分の画像はそのまま)");
		panel.add(label);
		propValue = props.getProperty("AutoMarginPadding");
		jTextAutoMarginPadding = new JTextField(propValue==null?"1.0":propValue);
		jTextAutoMarginPadding.setToolTipText(label.getToolTipText());
		jTextAutoMarginPadding.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginPadding.setInputVerifier(new FloatInputVerifier(0, 0, 50));
		jTextAutoMarginPadding.setMaximumSize(text4);
		jTextAutoMarginPadding.setPreferredSize(text4);
		jTextAutoMarginPadding.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginPadding.addFocusListener(new TextSelectFocusListener(jTextAutoMarginPadding));
		panel.add(jTextAutoMarginPadding);
		label = new JLabel("%  ノンブル:");
		label.setBorder(padding2H);
		panel.add(label);
		propValue = props.getProperty("AutoMarginNombre");
		jComboAutoMarginNombre = new JComboBox(new String[]{"なし","上","下","上下"});
		try { jComboAutoMarginNombre.setSelectedIndex(Integer.parseInt(propValue)); } catch (Exception e) {}
		jComboAutoMarginNombre.setFocusable(false);
		jComboAutoMarginNombre.setPreferredSize(new Dimension(text4.width+22, 20));
		
		panel.add(jComboAutoMarginNombre);
		
		////////////////////////////////////////////////////////////////
		//Tab 詳細設定
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.add("詳細設定", tabPanel);
		
		////////////////////////////////
		//文中全角スペースの処理
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("文中全角スペースの処理"));
		tabPanel.add(panel);
		//ピクセル
		label = new JLabel("行末で非表示:(");
		label.setBorder(padding2);
		panel.add(label);
		propValue = props.getProperty("SpaceHyphenation");
		buttonGroup = new ButtonGroup();
		jRadioSpaceHyp1 = new JRadioButton("Kobo・Kindle ", propValue==null||"1".equals(propValue));
		jRadioSpaceHyp1.setToolTipText("Kobo・Kindleで行末で非表示にします Readerではそのままと同じ表示になります");
		jRadioSpaceHyp1.setFocusPainted(false);
		jRadioSpaceHyp1.setBorder(padding2);
		panel.add(jRadioSpaceHyp1);
		buttonGroup.add(jRadioSpaceHyp1);
		jRadioSpaceHyp2 = new JRadioButton("Reader ) ", "2".equals(propValue));
		jRadioSpaceHyp2.setToolTipText("Reader以外では次行に追い出しの禁則処理になります");
		jRadioSpaceHyp2.setFocusPainted(false);
		jRadioSpaceHyp2.setBorder(padding2);
		panel.add(jRadioSpaceHyp2);
		buttonGroup.add(jRadioSpaceHyp2);
		jRadioSpaceHyp0 = new JRadioButton("そのまま  ", "0".equals(propValue));
		jRadioSpaceHyp0.setToolTipText("行の折り返し部分にある全角スペースが行頭に表示されます");
		jRadioSpaceHyp0.setFocusPainted(false);
		jRadioSpaceHyp0.setBorder(padding2);
		panel.add(jRadioSpaceHyp0);
		buttonGroup.add(jRadioSpaceHyp0);
		
		////////////////////////////////
		//空行除去
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("空行除去"));
		tabPanel.add(panel);
		jComboxRemoveEmptyLine = new JComboBox(new String[]{"0", "1", "2", "3", "4", "5"});
		jComboxRemoveEmptyLine.setToolTipText("空行の行数を減らします 見出し行の後ろ3行以内は1行残します");
		jComboxRemoveEmptyLine.setFocusable(false);
		jComboxRemoveEmptyLine.setBorder(padding0);
		jComboxRemoveEmptyLine.setMaximumSize(text5);
		jComboxRemoveEmptyLine.setPreferredSize(text5);
		((JLabel)jComboxRemoveEmptyLine.getRenderer()).setBorder(padding2);
		propValue = props.getProperty("RemoveEmptyLine");
		jComboxRemoveEmptyLine.setSelectedItem(propValue==null?"0":propValue);
		panel.add(jComboxRemoveEmptyLine);
		label = new JLabel("行減らす");
		label.setBorder(padding2);
		panel.add(label);
		
		////////////////////////////////
		//自動縦中横
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("自動縦中横"));
		tabPanel.add(panel);
		//半角2文字縦書き
		propValue = props.getProperty("AutoYoko");
		jCheckAutoYoko = new JCheckBox("有効 ", propValue==null||"1".equals(propValue));
		jCheckAutoYoko.setFocusPainted(false);
		jCheckAutoYoko.setToolTipText("半角の2文字の数字、2～3文字の!?を縦中横で表示します。(前後に半角が無い場合)");
		jCheckAutoYoko.setBorder(padding2);
		panel.add(jCheckAutoYoko);
		label = new JLabel("数字(");
		label.setBorder(padding0);
		panel.add(label);
		//半角数字1文字縦書き
		propValue = props.getProperty("AutoYokoNum1");
		jCheckAutoYokoNum1 = new JCheckBox("1桁 ", "1".equals(propValue));
		jCheckAutoYokoNum1.setFocusPainted(false);
		jCheckAutoYokoNum1.setBorder(padding2);
		panel.add(jCheckAutoYokoNum1);
		//半角数字3文字縦書き
		propValue = props.getProperty("AutoYokoNum3");
		jCheckAutoYokoNum3 = new JCheckBox("3桁", "1".equals(propValue));
		jCheckAutoYokoNum3.setFocusPainted(false);
		jCheckAutoYokoNum3.setBorder(padding2);
		panel.add(jCheckAutoYokoNum3);
		label = new JLabel(")");
		label.setBorder(padding0);
		panel.add(label);
		//!? 1文字
		propValue = props.getProperty("AutoYokoEQ1");
		jCheckAutoEQ1 = new JCheckBox("!? 1文字 ", "1".equals(propValue));
		jCheckAutoEQ1.setFocusPainted(false);
		jCheckAutoEQ1.setBorder(padding2);
		panel.add(jCheckAutoEQ1);
		
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
		jCheckCommentPrint.setBorder(padding2);
		panel.add(jCheckCommentPrint);
		//半角2文字縦書き
		propValue = props.getProperty("CommentConvert");
		jCheckCommentConvert = new JCheckBox("コメント内注記変換", "1".equals(propValue));
		jCheckCommentConvert.setToolTipText("コメント内の注記を変換します");
		jCheckCommentConvert.setFocusPainted(false);
		jCheckCommentConvert.setBorder(padding2);
		panel.add(jCheckCommentConvert);
		
		////////////////////////////////
		//強制改ページ
		////////////////////////////////
		JPanel panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("強制改ページ"));
		tabPanel.add(panelV);
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(padding3B);
		panelV.add(panel);
		
		propValue = props.getProperty("PageBreak");
		jCheckPageBreak = new JCheckBox("有効 (※指定サイズを超えた時点で強制改ページ(ブロック注記の外側のみ))", "1".equals(propValue));
		jCheckPageBreak.setFocusPainted(false);
		jCheckPageBreak.setBorder(padding2);
		jCheckPageBreak.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){
			boolean selected = jCheckPageBreak.isSelected();
			jTextPageBreakSize.setEditable(selected);
			jTextPageBreakEmptySize.setEditable(selected);
			jTextPageBreakChapterSize.setEditable(selected);
		}});
		panel.add(jCheckPageBreak);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(padding0);
		panelV.add(panel);
		label = new JLabel("各行");
		label.setBorder(padding2);
		panel.add(label);
		propValue = props.getProperty("PageBreakSize");
		defaultValue = 400; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextPageBreakSize = new JTextField();
		jTextPageBreakSize.setMaximumSize(text4);
		jTextPageBreakSize.setPreferredSize(text4);
		jTextPageBreakSize.setText(Integer.toString(defaultValue));
		jTextPageBreakSize.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextPageBreakSize.setEditable(jCheckPageBreak.isSelected());
		jTextPageBreakSize.addFocusListener(new TextSelectFocusListener(jTextPageBreakSize));
		panel.add(jTextPageBreakSize);
		label = new JLabel("KB ");
		label.setBorder(padding2);
		panel.add(label);
		
		propValue = props.getProperty("PageBreakEmpty");
		jCheckPageBreakEmpty = new JCheckBox("空行(", "1".equals(propValue));
		jCheckPageBreakEmpty.setFocusPainted(false);
		jCheckPageBreakEmpty.setBorder(padding2);
		panel.add(jCheckPageBreakEmpty);
		jComboxPageBreakEmptyLine = new JComboBox(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"});
		jComboxPageBreakEmptyLine.setFocusable(false);
		jComboxPageBreakEmptyLine.setBorder(padding0);
		jComboxPageBreakEmptyLine.setMaximumSize(text5);
		jComboxPageBreakEmptyLine.setPreferredSize(text5);
		((JLabel)jComboxPageBreakEmptyLine.getRenderer()).setBorder(padding2);
		propValue = props.getProperty("PageBreakEmptyLine");
		jComboxPageBreakEmptyLine.setSelectedItem(propValue==null?"2":propValue);
		panel.add(jComboxPageBreakEmptyLine);
		label = new JLabel("行以上 ");
		label.setBorder(padding2);
		panel.add(label);
		propValue = props.getProperty("PageBreakEmptySize");
		defaultValue = 300; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextPageBreakEmptySize = new JTextField();
		jTextPageBreakEmptySize.setMaximumSize(text4);
		jTextPageBreakEmptySize.setPreferredSize(text4);
		jTextPageBreakEmptySize.setText(Integer.toString(defaultValue));
		jTextPageBreakEmptySize.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextPageBreakEmptySize.setEditable(jCheckPageBreak.isSelected());
		jTextPageBreakEmptySize.addFocusListener(new TextSelectFocusListener(jTextPageBreakEmptySize));
		panel.add(jTextPageBreakEmptySize);
		label = new JLabel("KB) ");
		label.setBorder(padding2);
		panel.add(label);
		
		propValue = props.getProperty("PageBreakChapter");
		jCheckPageBreakChapter = new JCheckBox("見出し前(", "1".equals(propValue));
		jCheckPageBreakChapter.setFocusPainted(false);
		jCheckPageBreakChapter.setBorder(padding2);
		panel.add(jCheckPageBreakChapter);
		propValue = props.getProperty("PageBreakChapterSize");
		defaultValue = 200; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextPageBreakChapterSize = new JTextField();
		jTextPageBreakChapterSize.setMaximumSize(text4);
		jTextPageBreakChapterSize.setPreferredSize(text4);
		jTextPageBreakChapterSize.setText(Integer.toString(defaultValue));
		jTextPageBreakChapterSize.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 9999));
		jTextPageBreakChapterSize.setEditable(jCheckPageBreak.isSelected());
		jTextPageBreakChapterSize.addFocusListener(new TextSelectFocusListener(jTextPageBreakChapterSize));
		panel.add(jTextPageBreakChapterSize);
		label = new JLabel("KB) ");
		label.setBorder(padding2);
		panel.add(label);
		
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
		label.setBorder(padding2);
		panel.add(label);
		propValue = props.getProperty("MaxChapterNameLength");
		defaultValue = 64; try { defaultValue = Integer.parseInt(propValue); } catch (Exception e) {}
		jTextMaxChapterNameLength = new JTextField(propValue==null?""+defaultValue:propValue);
		jTextMaxChapterNameLength.setHorizontalAlignment(JTextField.RIGHT);
		jTextMaxChapterNameLength.setInputVerifier(new IntegerInputVerifier(defaultValue, 1, 999));
		jTextMaxChapterNameLength.setMaximumSize(text3);
		jTextMaxChapterNameLength.setPreferredSize(text3);
		jTextMaxChapterNameLength.addFocusListener(new TextSelectFocusListener(jTextMaxChapterNameLength));
		panel.add(jTextMaxChapterNameLength);
		
		label = new JLabel("  ");
		label.setBorder(padding2);
		panel.add(label);
		
		//表紙
		propValue = props.getProperty("CoverPageToc");
		jCheckCoverPageToc = new JCheckBox("表紙  ", "1".equals(propValue));
		jCheckCoverPageToc.setToolTipText("表紙画像のページを目次を追加します");
		jCheckCoverPageToc.setFocusPainted(false);
		jCheckCoverPageToc.setBorder(padding2);
		panel.add(jCheckCoverPageToc);
		
		propValue = props.getProperty("ChapterUseNextLine");
		jCheckChapterUseNextLine = new JCheckBox("次の行を繋げる  ", "1".equals(propValue));
		jCheckChapterUseNextLine.setToolTipText("次の行が空行でなければ見出しの後ろに繋げます");
		jCheckChapterUseNextLine.setFocusPainted(false);
		jCheckChapterUseNextLine.setBorder(padding2);
		panel.add(jCheckChapterUseNextLine);
		
		propValue = props.getProperty("ChapterExclude");
		jCheckChapterExclude = new JCheckBox("連続する見出しを除外", propValue==null||"1".equals(propValue));
		jCheckChapterExclude.setToolTipText("3つ以上連続する自動抽出された見出しを除外します(空行1行間隔も連続扱い)");
		jCheckChapterExclude.setFocusPainted(false);
		jCheckChapterExclude.setBorder(padding2);
		panel.add(jCheckChapterExclude);
		
		//目次抽出
		panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("目次抽出"));
		tabPanel.add(panelV);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(padding3B);
		panelV.add(panel);
		//改ページ後を目次に追加
		propValue = props.getProperty("ChapterSection");
		jCheckChapterSection = new JCheckBox("改ページ後 ", propValue==null||"1".equals(propValue));
		jCheckChapterSection.setToolTipText("改ページ後の先頭行の文字を目次に出力します");
		jCheckChapterSection.setFocusPainted(false);
		jCheckChapterSection.setBorder(padding2);
		panel.add(jCheckChapterSection);
		//見出し注記
		label = new JLabel(" 注記(");
		label.setBorder(padding2);
		panel.add(label);
		propValue = props.getProperty("ChapterH");
		jCheckChapterH = new JCheckBox("見出し ", propValue==null||"1".equals(propValue));
		jCheckChapterH.setFocusPainted(false);
		jCheckChapterH.setBorder(padding2);
		panel.add(jCheckChapterH);
		propValue = props.getProperty("ChapterH1");
		jCheckChapterH1 = new JCheckBox("大見出し ", propValue==null||"1".equals(propValue));
		jCheckChapterH1.setFocusPainted(false);
		jCheckChapterH1.setBorder(padding2);
		panel.add(jCheckChapterH1);
		propValue = props.getProperty("ChapterH2");
		jCheckChapterH2 = new JCheckBox("中見出し ", propValue==null||"1".equals(propValue));
		jCheckChapterH2.setFocusPainted(false);
		jCheckChapterH2.setBorder(padding2);
		panel.add(jCheckChapterH2);
		propValue = props.getProperty("ChapterH3");
		jCheckChapterH3 = new JCheckBox("小見出し )", propValue==null||"1".equals(propValue));
		jCheckChapterH3.setFocusPainted(false);
		jCheckChapterH3.setBorder(padding2);
		panel.add(jCheckChapterH3);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(padding3B);
		panelV.add(panel);
		propValue = props.getProperty("ChapterName");
		jCheckChapterName = new JCheckBox("章見出し (第～章/その～/～章/序/プロローグ 等)", propValue==null||"1".equals(propValue));
		jCheckChapterName.setToolTipText("第～話/第～章/第～篇/第～部/第～節/第～幕/第～編/その～/～章/プロローグ/エピローグ/モノローグ/序/序章/終章/転章/間章/幕間");
		jCheckChapterName.setFocusPainted(false);
		jCheckChapterName.setBorder(padding2);
		panel.add(jCheckChapterName);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(padding3B);
		panelV.add(panel);
		propValue = props.getProperty("ChapterNumOnly");
		jCheckChapterNumOnly = new JCheckBox("数字のみ", "1".equals(propValue));
		jCheckChapterNumOnly.setFocusPainted(false);
		jCheckChapterNumOnly.setBorder(padding2);
		panel.add(jCheckChapterNumOnly);
		propValue = props.getProperty("ChapterNumTitle");
		jCheckChapterNumTitle = new JCheckBox("数字+見出し  ", "1".equals(propValue));
		jCheckChapterNumTitle.setFocusPainted(false);
		jCheckChapterNumTitle.setBorder(padding2);
		panel.add(jCheckChapterNumTitle);
		propValue = props.getProperty("ChapterNumParen");
		jCheckChapterNumParen = new JCheckBox("括弧内数字のみ", "1".equals(propValue));
		jCheckChapterNumParen.setToolTipText("（）〈〉〔〕【】内の数字"); 
		jCheckChapterNumParen.setFocusPainted(false);
		jCheckChapterNumParen.setBorder(padding2);
		panel.add(jCheckChapterNumParen);
		propValue = props.getProperty("ChapterNumParenTitle");
		jCheckChapterNumParenTitle = new JCheckBox("括弧内数字+見出し", "1".equals(propValue));
		jCheckChapterNumParenTitle.setFocusPainted(false);
		jCheckChapterNumParenTitle.setBorder(padding2);
		panel.add(jCheckChapterNumParenTitle);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		//panel.setBorder(padding4B);
		panelV.add(panel);
		propValue = props.getProperty("ChapterPattern");
		jCheckChapterPattern = new JCheckBox("その他パターン", "1".equals(propValue));
		jCheckChapterPattern.setToolTipText("目次抽出パターンを正規表現で指定します。前後の空白とタグを除いた文字列と比較します。");
		jCheckChapterPattern.setFocusPainted(false);
		jCheckChapterPattern.setBorder(padding2);
		jCheckChapterPattern.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){ jComboChapterPattern.setEditable(jCheckChapterPattern.isSelected()); jComboChapterPattern.repaint(); }});
		panel.add(jCheckChapterPattern);
		propValue = props.getProperty("ChapterPatternText");
		jComboChapterPattern = new JComboBox(new String[]{
				"^(見出し１|見出し２|見出し３)$",
				"^(†|【|●|▼|■)",
				"^(0-9|０-９|一|二|三|四|五|六|七|八|九|十|〇)",
				"^[1|2|１|２]?[0-9|０-９]月[1-3|１-３]?[0-9|０-９]日",
				"^(一|十)?(一|二|三|四|五|六|七|八|九|十|〇)月(一|十|二十?|三十?)?(一|二|三|四|五|六|七|八|九|十|〇)日"});
		jComboChapterPattern.setSelectedItem(propValue==null?"":propValue);
		jComboChapterPattern.setBorder(padding0);
		jComboChapterPattern.setMaximumSize(text300);
		jComboChapterPattern.setPreferredSize(text300);
		jComboChapterPattern.setEditable(jCheckChapterPattern.isSelected());
		panel.add(jComboChapterPattern);
		
		////////////////////////////////////////////////////////////////
		//テキストエリア
		////////////////////////////////////////////////////////////////
		JPanel lowerPane = new JPanel();
		lowerPane.setLayout(new BoxLayout(lowerPane, BoxLayout.Y_AXIS));
		jSplitPane.add(lowerPane);
		
		jTextArea = new JTextArea("青空文庫テキストをここにドラッグ＆ドロップまたは「ファイル選択」で変換します。\n");
		jTextArea.setEditable(false);
		jTextArea.setFont(new Font("Default", Font.PLAIN, 12));
		jTextArea.setBorder(new LineBorder(Color.white, 3));
		new DropTarget(jTextArea, DnDConstants.ACTION_COPY_OR_MOVE, new DropListener(), true);
		
		lowerPane.add(new JScrollPane(jTextArea));
		
		////////////////////////////////////////////////////////////////
		//画面下 ステータス
		////////////////////////////////////////////////////////////////
		JPanel statusPane = new JPanel();
		statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.X_AXIS));
		statusPane.setMaximumSize(new Dimension(1920, 22));
		statusPane.setPreferredSize(new Dimension(1920, 22));
		statusPane.setBorder(padding2H);
		lowerPane.add(statusPane);
		////////////////////////////////
		//プログレスバー
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 2));
		statusPane.add(panel);
		jProgressBar = new JProgressBar(0, 100);
		jProgressBar.setMaximumSize(new Dimension(200, 20));
		jProgressBar.setPreferredSize(new Dimension(200, 20));
		panel.add(jProgressBar);
		label = new JLabel(" ");
		label.setBorder(padding2H);
		panel.add(label);
		jButtonCancel = new JButton("処理中止");
		jButtonCancel.setBorder(padding3V);
		jButtonCancel.setMaximumSize(new Dimension(80, 20));
		jButtonCancel.setPreferredSize(new Dimension(80, 20));
		jButtonCancel.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/cancel.png")));
		jButtonCancel.setFocusable(false);
		jButtonCancel.setEnabled(false);
		jButtonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				epub3Writer.cancel();
				epub3ImageWriter.cancel();
				aozoraConverter.cancel();
				convertCanceled = true;
				if (webConverter != null) webConverter.canceled();
				if (kindleProcess != null) kindleProcess.destroy(); 
			}
		});
		panel.add(jButtonCancel);
		////////////////////////////////
		//ログ関連
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 2));
		statusPane.add(panel);
		label = new JLabel("ログ:");
		label.setBorder(padding2H);
		panel.add(label);
		JButton jButtonLogClear = new JButton();
		jButtonLogClear.setToolTipText("ログを削除してクリップボードにコピーします");
		jButtonLogClear.setBorder(padding3V);
		jButtonLogClear.setMaximumSize(new Dimension(24, 20));
		jButtonLogClear.setPreferredSize(new Dimension(24, 20));
		jButtonLogClear.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/del_cut.png")));
		jButtonLogClear.setFocusable(false);
		jButtonLogClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = jTextArea.getText();
				jTextArea.setText("");
				try {
					Clipboard systemClipboard = getToolkit().getSystemClipboard();
					StringSelection responseURLString = new StringSelection(text);
					systemClipboard.setContents(responseURLString, null);
				} catch (Exception e2) { e2.printStackTrace(); }
				if (!jButtonCancel.isEnabled()) {
					jProgressBar.setValue(0);
					jProgressBar.setStringPainted(false);
				}
			}
		});
		panel.add(jButtonLogClear);
		
		////////////////////////////////////////////////////////////////
		//確認ダイアログ
		jConfirmDialog = new JConfirmDialog(
			iconImage, AozoraEpub3Applet.class.getResource("images/icon.png").toString().replaceFirst("/icon\\.png", "/")
		);
		
		////////////////////////////////////////////////////////////////
		//ログ出力先を設定
		LogAppender.setTextArea(jTextArea);
		
		//初期化
		try {
			//ePub出力クラス初期化
			this.epub3Writer = new Epub3Writer(this.jarPath+"template/");
			this.epub3Writer.setProgressBar(jProgressBar);
			//ePub画像出力クラス初期化
			this.epub3ImageWriter = new Epub3ImageWriter(this.jarPath+"template/");
			this.epub3ImageWriter.setProgressBar(jProgressBar);
			
			//変換テーブルをstaticに生成
			this.aozoraConverter = new AozoraEpub3Converter(this.epub3Writer, this.jarPath);
			
		} catch (IOException e) {
			e.printStackTrace();
			jTextArea.append(e.getMessage());
		}
		
	}
	
	////////////////////////////////////////////////////////////////
	class TextSelectFocusListener implements FocusListener
	{
		JTextField jTextField;
		TextSelectFocusListener(JTextField jTextField)
		{
			this.jTextField = jTextField;
		}
		public void focusLost(FocusEvent e){}
		public void focusGained(FocusEvent e){this.jTextField.setSelectionStart(0);this.jTextField.setSelectionEnd(10);}
	}
	class IntegerInputVerifier extends InputVerifier
	{
		/** 基準値 */
		int def = 0;
		/** 最小値 */
		int min = Integer.MIN_VALUE;
		/** 最大値 */
		int max = Integer.MAX_VALUE;
		
		IntegerInputVerifier(int def, int min)
		{
			this.def = Math.max(def, min);
			this.min = min;
		}
		IntegerInputVerifier(int def, int min, int max)
		{
			this.def = Math.min(Math.max(def, min), max);
			this.min = min;
			this.max = max;
		}
		@Override
		public boolean verify(JComponent c)
		{
			JTextField textField = (JTextField)c;
			try{
				int i = (int)Double.parseDouble(textField.getText());
				if (i >= this.min && i <= this.max) {
					textField.setText(Integer.toString(i));
					return true;
				}
				if (this.max != Integer.MAX_VALUE && i > this.max) {
					textField.setText(Integer.toString(this.max));
					return true;
				} else if (i < this.min) {
					textField.setText(Integer.toString(this.min));
					return true;
				}
			} catch (NumberFormatException e) {
				//UIManager.getLookAndFeel().provideErrorFeedback(c);
			}
			textField.setText(Integer.toString(this.def));
			return true;
		}
	}
	class FloatInputVerifier extends InputVerifier
	{
		/** 基準値 */
		float def = 0;
		/** 最小値 */
		float min = Float.MIN_VALUE;
		/** 最大値 */
		float max = Float.MAX_VALUE;
		
		FloatInputVerifier(float def, float min)
		{
			this.def = Math.max(def, min);
			this.min = min;
		}
		FloatInputVerifier(float def, float min, float max)
		{
			this.def = Math.min(Math.max(def, min), max);
			this.min = min;
			this.max = max;
		}
		@Override
		public boolean verify(JComponent c)
		{
			JTextField textField = (JTextField)c;
			try{
				float f = (float)Double.parseDouble(textField.getText());
				if (f >= this.min && f <= this.max) {
					textField.setText(Float.toString(f)); return true;
				}
				if (this.max != Float.MAX_VALUE && f > this.max) {
					textField.setText(Float.toString(this.max));
					return true;
				} else if (f < this.min) {
					textField.setText(Float.toString(this.min));
					return true;
				}
			} catch (NumberFormatException e) { }
			textField.setText(Float.toString(this.def));
			return true;
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
				if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() > 0) {
						jComboCover.setSelectedItem(files.get(0).getAbsolutePath());
						return;
					}
				}
				if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String path = (String)transfer.getTransferData(DataFlavor.stringFlavor);
					if (path.startsWith("file://"))
						try { path = URLDecoder.decode(path.substring(0, path.indexOf('\n')-1).substring(7).trim(),"UTF-8"); } catch (UnsupportedEncodingException e1) { }
					jComboCover.setSelectedItem(path);
					return;
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
				if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() > 0) {
						File file = files.get(0);
						if (!file.isDirectory()) file = file.getParentFile();
						jComboDstPath.setSelectedItem(file.getAbsolutePath());
						return;
					}
				}
				if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String path = (String)transfer.getTransferData(DataFlavor.stringFlavor);
					if (path.startsWith("file://"))
						try { path = URLDecoder.decode(path.substring(0, path.indexOf('\n')-1).substring(7).trim(),"UTF-8"); } catch (UnsupportedEncodingException e1) { }
					jComboDstPath.setSelectedItem(path);
					return;
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
			fileChooser.setFileFilter(new FileNameExtensionFilter("青空文庫'txt,zip),画像(zip),ショートカット(url)", new String[]{"txt","zip","cbz","txtz","url"}));
			fileChooser.setMultiSelectionEnabled(true);
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				
				Vector<File> vecFiles = new Vector<File>();
				Vector<String> vecUrlString = null;
				File dstPath = null;
				try {
				for (File file : fileChooser.getSelectedFiles()) {
					if (file.getName().endsWith(".url")) {
						if (vecUrlString == null) vecUrlString = new Vector<String>();
						vecUrlString.add(readInternetShortCut(file));
						dstPath = file.getParentFile();
					} else {
						vecFiles.add(file);
					}
				}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if (vecFiles.size() > 0) {
					startConvertFilesWorker(vecFiles);
				}
				if (vecUrlString != null) {
					convertWeb(vecUrlString, dstPath);
				}
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
				Vector<File> vecFiles = new Vector<File>();
				Vector<String> vecUrlString = null;
				File dstPath = null;
				
				if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					//IEはurlはショートカットになってくる
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() > 0) {
						for (File file : files) {
							if (file.getName().endsWith(".url")) {
								if (vecUrlString == null) vecUrlString = new Vector<String>();
								vecUrlString.add(readInternetShortCut(file));
								dstPath = file.getParentFile();
							} else {
								vecFiles.add(file);
							}
						}
						if (vecFiles.size() > 0) {
							startConvertFilesWorker(vecFiles);
						}
					}
				}
				if (vecUrlString != null || transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					//URLかどうか
					String urlString = null;
					try {
						Object transferData = transfer.getTransferData(DataFlavor.stringFlavor);
						if (transferData != null) urlString = transferData.toString();
					} catch (Exception e) {}
					
					if (urlString != null && urlString.startsWith("file://")) {
						//Linux等 ファイルのパスでファイルがあれば変換
						try {
							String[] fileNames = urlString.split("\n");
							vecFiles = new Vector<File>();
							for (String path : fileNames) {
								File file = new File(URLDecoder.decode(path.substring(7).trim(),"UTF-8"));
								if (file.exists()) {
									if (file.getName().endsWith(".url")) {
										if (vecUrlString == null) vecUrlString = new Vector<String>();
										vecUrlString.add(readInternetShortCut(file));
										dstPath = file.getParentFile();
									} else {
										vecFiles.add(file);
									}
								}
							}
							if (vecFiles.size() > 0) {
								startConvertFilesWorker(vecFiles);
							}
						} catch (Exception e) { e.printStackTrace(); }
					}
					else if (urlString != null && urlString.startsWith("http")) {
						//ブラウザからのDnD
						if (vecUrlString == null) vecUrlString = new Vector<String>();
						vecUrlString.add(urlString);
					}
					
					//URL変換 の最後が .zip
					if (urlString != null && urlString.endsWith(".zip")) {
						convertZip(urlString);
						return;
					}
					
					//Webから取得
					if (vecUrlString != null) {
						convertWeb(vecUrlString, dstPath);
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
			}
		}
	}
	
	void convertZip(String urlString) throws IOException
	{
		//出力先が指定されていない
		if (jComboDstPath.getSelectedIndex() == 0) {
			dstPathChooser.actionPerformed(null);
			if (jComboDstPath.getSelectedIndex() == 0) {
				LogAppender.println("変換処理を中止しました");
				return;
			}
		}
		//キャッシュパス
		cachePath.mkdir();
		
		String urlPath = urlString.substring(urlString.indexOf("//")+2).replaceAll("\\?\\*\\&\\|\\<\\>\"\\\\", "_");
		//青空zipのURLをキャッシュして変換
		//出力先 URLと同じパス
		String path = cachePath.getAbsolutePath()+"/"+urlPath;
		File srcFile = new File(path);
		srcFile.getParentFile().mkdirs();
		//ダウンロード
		BufferedInputStream bis = new BufferedInputStream(new URL(urlString).openStream(), 8192);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(srcFile));
		IOUtils.copy(bis, bos);
		bos.close();
		bis.close();
		startConvertFilesWorker(new File[]{srcFile});
	}
	
	void convertWeb(Vector<String> vecUrlString, File dstPath)
	{
		//出力先が指定されていない
		if (jComboDstPath.getSelectedIndex() == 0 && dstPath == null) {
			dstPathChooser.actionPerformed(null);
			if (jComboDstPath.getSelectedIndex() == 0) {
				LogAppender.println("変換処理を中止しました");
				return;
			}
		}
		//キャッシュパス
		cachePath.mkdir();
		
		//web以下に同じ名前のパスがあったらキャッシュ後青空変換
		ConvertWebWorker convertWebWorker = new ConvertWebWorker(vecUrlString, dstPath);
		convertWebWorker.execute();
	}
	
	/** Windowsのインターネットショートカットを読み込み */
	String readInternetShortCut(File file) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("URL=")) return line.substring(4);
			}
			return null;
		} finally {
			br.close();
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
	/** 複数ファイルを変換
	 * @param dstPath srcFileがキャッシュで入力ファイルを同じ場所に出力先指定をする場合 */
	private void convertFiles(File[] srcFiles, File dstPath)
	{
		if (srcFiles.length == 0 ) return;
		
		convertCanceled = false;
		
		//共通パラメータ取得
		//出力先取得
		if (jComboDstPath.getSelectedIndex() == 0) {
			//出力先指定があればそれを設定
			if (dstPath != null) currentPath = dstPath;
			//入力先がキャッシュファイルでなければ設定
			else if (!srcFiles[0].getAbsolutePath().startsWith(cachePath.getAbsolutePath())) currentPath = srcFiles[0].getParentFile();
			
		} else {
			if (dstPath == null) dstPath = new File(jComboDstPath.getEditor().getItem().toString());
			if (!dstPath.isDirectory()) {
				String dstPathName = dstPath.getAbsolutePath();
				if (dstPathName.length() > 70) dstPathName = dstPathName.substring(0, 32)+" ... "+dstPathName.substring(dstPathName.length()-32);
				int ret = JOptionPane.showConfirmDialog(jConfirmDialog, "出力先がありません\n"+dstPathName+"\nにフォルダを作成しますか？", "出力先確認", JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					//フォルダ作成
					dstPath.mkdirs();
				} else {
					LogAppender.println("変換処理を中止しました");
					return;
				}
			}
			//jComboDstPathに出力先履歴保存
			this.addDstPath();
		}
		
		////////////////////////////////////////////////////////////////
		//Appletのパラメータを取得
		////////////////////////////////////////////////////////////////
		//画面サイズと画像リサイズ
		int resizeW = 0;
		if (jCheckResizeW.isSelected()) try { resizeW = Integer.parseInt(jTextResizeNumW.getText()); } catch (Exception e) {}
		int resizeH = 0;
		if (jCheckResizeH.isSelected()) try { resizeH = Integer.parseInt(jTextResizeNumH.getText()); } catch (Exception e) {}
		//int pixels = 0;
		//if (jCheckPixel.isSelected()) try { pixels = Integer.parseInt(jTextPixelW.getText())*Integer.parseInt(jTextPixelH.getText()); } catch (Exception e) {}
		int dispW = Integer.parseInt(jTextDispW.getText());
		int dispH = Integer.parseInt(jTextDispH.getText());
		this.coverW = Integer.parseInt(this.jTextCoverW.getText());
		this.coverH = Integer.parseInt(this.jTextCoverH.getText());
		int singlePageSizeW = Integer.parseInt(jTextSinglePageSizeW.getText());
		int singlePageSizeH = Integer.parseInt(jTextSinglePageSizeH.getText());
		int singlePageWidth = Integer.parseInt(jTextSinglePageWidth.getText());
		int imageFloatType = this.jComboImageFloatType.getSelectedIndex();
		int imageFloatW = 0; try { imageFloatW =Integer.parseInt(jTextImageFloatW.getText()); } catch (Exception e) {}
		int imageFloatH = 0; try { imageFloatH =Integer.parseInt(jTextImageFloatH.getText()); } catch (Exception e) {}
		float jpegQualty = 0.8f; try { jpegQualty = Integer.parseInt(jTextJpegQuality.getText())/100f; } catch (Exception e) {}
		int autoMarginLimitH = 0;
		int autoMarginLimitV = 0;
		int autoMarginWhiteLevel = 0;
		float autoMarginPadding = 0;
		int autoMarginNombre = 0;
		if (jCheckAutoMargin.isSelected()) {
			try { autoMarginLimitH =Integer.parseInt(jTextAutoMarginLimitH.getText()); } catch (Exception e) {}
			try { autoMarginLimitV =Integer.parseInt(jTextAutoMarginLimitV.getText()); } catch (Exception e) {}
			try { autoMarginWhiteLevel =Integer.parseInt(jTextAutoMarginWhiteLevel.getText()); } catch (Exception e) {}
			try { autoMarginPadding =Float.parseFloat(jTextAutoMarginPadding.getText()); } catch (Exception e) {}
			autoMarginNombre = jComboAutoMarginNombre.getSelectedIndex();
		}
		this.epub3Writer.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth, jCheckFitImage.isSelected(),
				imageFloatType, imageFloatW, imageFloatH, jpegQualty, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre);
		this.epub3ImageWriter.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth, jCheckFitImage.isSelected(),
				imageFloatType, imageFloatW, imageFloatH, jpegQualty, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre);
		
		try {
			this.aozoraConverter.setNoIllust(jCheckNoIllust.isSelected()); 
			//栞用ID出力
			this.aozoraConverter.setWithMarkId(this.jCheckMarkId.isSelected());
			//変換オプション設定
			this.aozoraConverter.setAutoYoko(this.jCheckAutoYoko.isSelected(), this.jCheckAutoYokoNum1.isSelected(), this.jCheckAutoYokoNum3.isSelected(), this.jCheckAutoEQ1.isSelected());
			//4バイト文字出力
			this.aozoraConverter.setGaiji32(this.jCheckGaiji32.isSelected());
			//表題左右中央
			this.aozoraConverter.setMiddleTitle(this.jCheckMiddleTitle.isSelected());
			//全角スペースの禁則
			this.aozoraConverter.setSpaceHyphenation(this.jRadioSpaceHyp0.isSelected()?0:(this.jRadioSpaceHyp1.isSelected()?1:2));
			//コメント
			this.aozoraConverter.setCommentPrint(this.jCheckCommentPrint.isSelected(), this.jCheckCommentConvert.isSelected());
			
			int removeEmptyLine = 0; try { removeEmptyLine =  Integer.parseInt(jComboxRemoveEmptyLine.getSelectedItem().toString()); } catch (Exception e) {}
			this.aozoraConverter.setRemoveEmptyLine(removeEmptyLine);
			
			//強制改ページ
			if (jCheckPageBreak.isSelected()) {
				try {
					int forcePageBreakSize = 0;
					int forcePageBreakEmpty = 0;
					int forcePageBreakEmptySize = 0;
					int forcePageBreakChapter = 0;
					int forcePageBreakChapterSize = 0;
					forcePageBreakSize = Integer.parseInt(jTextPageBreakSize.getText().trim()) * 1024;
					if (jCheckPageBreakEmpty.isSelected()) {
						forcePageBreakEmpty = Integer.parseInt(jComboxPageBreakEmptyLine.getSelectedItem().toString());
						forcePageBreakEmptySize = Integer.parseInt(jTextPageBreakEmptySize.getText().trim()) * 1024;
					} if (jCheckPageBreakChapter.isSelected()) {
						forcePageBreakChapter = 1;
						forcePageBreakChapterSize = Integer.parseInt(jTextPageBreakChapterSize.getText().trim()) * 1024;
					}
					//Converterに設定
					this.aozoraConverter.setForcePageBreak(forcePageBreakSize, forcePageBreakEmpty, forcePageBreakEmptySize, forcePageBreakChapter, forcePageBreakChapterSize);
				} catch (Exception e) {
					LogAppender.println("強制改ページパラメータ読み込みエラー");
				}
			}
			
			//目次設定
			int maxLength = 64;
			try { maxLength = Integer.parseInt((jTextMaxChapterNameLength.getText())); } catch (Exception e) {}
			this.aozoraConverter.setChapterLevel(maxLength, jCheckChapterExclude.isSelected(), jCheckChapterUseNextLine.isSelected(), jCheckChapterSection.isSelected(),
					jCheckChapterH.isSelected(), jCheckChapterH1.isSelected(), jCheckChapterH2.isSelected(), jCheckChapterH3.isSelected(),
					jCheckChapterName.isSelected(),
					jCheckChapterNumOnly.isSelected(), jCheckChapterNumTitle.isSelected(), jCheckChapterNumParen.isSelected(), jCheckChapterNumParenTitle.isSelected(),
					jCheckChapterPattern.isSelected()?jComboChapterPattern.getEditor().getItem().toString().trim():"");
			
			
			////////////////////////////////////////////////////////////////
			//すべてのファイルの変換実行
			////////////////////////////////////////////////////////////////
			
			this._convertFiles(srcFiles, dstPath);
			
			if (convertCanceled) {
				this.jProgressBar.setStringPainted(false);
				this.jProgressBar.setValue(0);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.append("エラーが発生しました : ");
			LogAppender.println(e.getMessage());
		}
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
			if (convertCanceled) return;
		}
	}
	
	private void convertFile(File srcFile, File dstPath)
	{
		//拡張子
		String ext = srcFile.getName();
		ext = ext.substring(ext.lastIndexOf('.')+1).toLowerCase();
		
		//zipならzip内のテキストを検索
		int txtCount = 1;
		boolean imageOnly = false;
		if("zip".equals(ext) || "txtz".equals(ext)) { 
			LogAppender.println("--------");
			try {
				txtCount = AozoraEpub3.countZipText(srcFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (txtCount == 0) { txtCount = 1; imageOnly = true; }
		} else if ("cbz".equals(ext)) {
			LogAppender.println("--------");
			imageOnly = true;
		} else if ("txt".equals(ext)) {
			LogAppender.println("--------");
		}
		if (this.convertCanceled){
			LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
			return;
		}
		for (int i=0; i<txtCount; i++) {
			convertFile(srcFile, dstPath, ext, i, imageOnly);
			if (convertCanceled) return;
		}
	}
	/** 内部用変換関数 Appletの設定を引数に渡す
	 * @param srcFile 変換するファイル txt,zip,cbz,(rar,cbr)
	 * @param dstPath 出力先パス
	 * @param txtIdx Zip内テキストファイルの位置
	 */
	private void convertFile(File srcFile, File dstPath, String ext, int txtIdx, boolean imageOnly)
	{
		if (txtIdx > 0) LogAppender.println("--------");
		//パラメータ設定
		if (!"txt".equals(ext) && !"txtz".equals(ext) && !"zip".equals(ext) && !"cbz".equals(ext) ) {
			if (!"png".equals(ext) && !"jpg".equals(ext) && !"jpeg".equals(ext) && !"gif".equals(ext)) {
				LogAppender.println("txt, txtz, zip, cbz 以外は変換できません");
			}
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
			if (!imageOnly) {
				String[] textEntryName = new String[1];
				InputStream is = AozoraEpub3.getInputStream(srcFile, ext, imageInfoReader, textEntryName, txtIdx);
				//テキストファイルからメタ情報や画像単独ページ情報を取得
				bookInfo = AozoraEpub3.getBookInfo(
					is, imageInfoReader, this.aozoraConverter,
					this.jComboEncType.getSelectedItem().toString(),
					BookInfo.TitleType.indexOf(this.jComboTitle.getSelectedIndex())
				);
				bookInfo.textEntryName = textEntryName[0];
			}
		} catch (Exception e) {
			LogAppender.println("[ERROR] ファイルが読み込めませんでした : "+srcFile.getPath());
			return;
		}
		
		if (this.convertCanceled){
			LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
			return;
		}
		
		Epub3Writer writer = this.epub3Writer;
		try {
			if (!isFile) {
				//Zip内の画像情報読み込み
				imageInfoReader.loadZipImageInfos(srcFile, imageOnly);
				if (imageOnly) {
					LogAppender.println("画像のみのePubファイルを生成します");
					//画像出力用のBookInfo生成
					bookInfo = new BookInfo();
					bookInfo.imageOnly = true;
					//Writerを画像出力用派生クラスに入れ替え
					writer = this.epub3ImageWriter;
					
					if (imageInfoReader.countImageFileInfos() == 0) {
						LogAppender.println("[ERROR] 画像がありませんでした");
						return;
					}
					
					//名前順で並び替え
					imageInfoReader.sortImageFileNames();
					//先頭画像をbookInfoに設定しておく
					//if (coverImageIndex == 0) {
					//	bookInfo.coverImage = imageInfoReader.getImage(0);
					//}
					//画像数をプログレスバーに設定
					this.jProgressBar.setMaximum(imageInfoReader.countImageFileInfos()*10);
					jProgressBar.setValue(0);
					jProgressBar.setStringPainted(true);
				} else {
					//画像がなければプレビュー表示しないようにindexを-1に
					if (imageInfoReader.countImageFileNames() == 0) coverImageIndex = -1;
					
					//zipテキストならzip内の注記以外の画像も追加
					imageInfoReader.addNoNameImageFileName();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.println("[ERROR] "+e);
		}
		
		if (bookInfo == null) {
			LogAppender.println("[ERROR] 書籍の情報が取得できませんでした");
			return;
		}
		
		//テキストなら行数/100と画像数をプログレスバーに設定
		if (bookInfo.totalLineNum > 0) {
			if (isFile) this.jProgressBar.setMaximum(bookInfo.totalLineNum/10 + imageInfoReader.countImageFileNames()*10);
			else this.jProgressBar.setMaximum(bookInfo.totalLineNum/10 + imageInfoReader.countImageFileInfos()*10);
			jProgressBar.setValue(0);
			jProgressBar.setStringPainted(true);
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
		
		String[] titleCreator = BookInfo.getFileTitleCreator(srcFile.getName());
		if (jCheckUserFileName.isSelected()) {
			//ファイル名優先ならテキスト側の情報は不要
			bookInfo.title = "";
			bookInfo.creator = "";
			if (titleCreator[0] != null) bookInfo.title = titleCreator[0];
			if (titleCreator[1] != null) bookInfo.creator = titleCreator[1];
		} else {
			//テキストから取得できなければファイル名を利用
			if (bookInfo.title == null || bookInfo.title.length() == 0) bookInfo.title = titleCreator[0]==null?"":titleCreator[0];
			if (bookInfo.creator == null || bookInfo.creator.length() == 0) bookInfo.creator = titleCreator[1]==null?"":titleCreator[1];
		}
		
		if (this.convertCanceled){
			LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
			return;
		}
		
		//確認ページ 変換ボタン押下時にbookInfo更新
		if (this.jCheckConfirm.isSelected()) {
			//表題と著者設定 ファイル名から設定
			String title = "";
			String creator = "";
			if (bookInfo.title != null) title = bookInfo.title;
			if (bookInfo.creator != null) creator = bookInfo.creator;
			this.jConfirmDialog.showDialog(
				srcFile.getName(),
				(dstPath!=null ? dstPath.getAbsolutePath() : srcFile.getParentFile().getAbsolutePath())+File.separator,
				title, creator, this.jComboTitle.getSelectedIndex(),
				bookInfo, imageInfoReader, this.jFrameParent.getLocation(),
				coverW, coverH
			);
			
			//ダイアログが閉じた後に再開
			if (this.jConfirmDialog.canceled) {
				this.convertCanceled = true;
				LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
				return;
			}
			if (this.jConfirmDialog.skipped) {
				LogAppender.println("変換をスキップしました : "+srcFile.getAbsolutePath());
				return;
			}
			
			//変換前確認のチェックを反映
			if (!this.jConfirmDialog.jCheckConfirm2.isSelected()) jCheckConfirm.setSelected(false);
			
			//確認ダイアログの値をBookInfoに設定
			bookInfo.title = this.jConfirmDialog.getMetaTitle();
			bookInfo.creator = this.jConfirmDialog.getMetaCreator();
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
		}
		
		boolean autoFileName = this.jCheckAutoFileName.isSelected();
		boolean overWrite = this.jCheckOverWrite.isSelected();
		String outExt = this.jComboExt.getEditor().getItem().toString().trim();
		
		//出力ファイル
		File outFile = AozoraEpub3.getOutFile(srcFile, dstPath, bookInfo, autoFileName, outExt);
		
		//上書き確認
		if (!overWrite &&  outFile.exists()) {
			LogAppender.println("変換中止: "+srcFile.getAbsolutePath());
			LogAppender.println("ファイルが存在します: "+outFile.getAbsolutePath());
			return;
		}
		/*
		if (overWrite &&  outFile.exists()) {
			int ret = JOptionPane.showConfirmDialog(this, "ファイルが存在します\n上書きしますか？\n(取り消しで変換キャンセル)", "上書き確認", JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret == JOptionPane.NO_OPTION) {
				LogAppender.println("変換中止: "+srcFile.getAbsolutePath());
				return;
			} else if (ret == JOptionPane.CANCEL_OPTION) {
				LogAppender.println("変換中止: "+srcFile.getAbsolutePath());
				convertCanceled = true;
				LogAppender.println("変換処理をキャンセルしました");
				return;
			}
		}*/
		
		////////////////////////////////
		//Kindleチェック
		File kindlegen = new File(this.jarPath+"kindlegen.exe");
		if (!kindlegen.isFile()) {
			kindlegen = new File(this.jarPath+"kindlegen");
			if (!kindlegen.isFile()) {
				kindlegen = null;
			}
		}
		writer.setIsKindle(kindlegen != null);
		
		////////////////////////////////
		//変換実行
		AozoraEpub3.convertFile(
			srcFile, ext, outFile,
			this.aozoraConverter,
			writer,
			this.jComboEncType.getSelectedItem().toString(),
			bookInfo, imageInfoReader, txtIdx
		);
		
		imageInfoReader = null;
		//System.gc();
		
		//変換中にキャンセルされた場合
		if (this.convertCanceled) {
			LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
			return;
		}
		
		////////////////////////////////
		//kindlegen.exeがあれば実行
		try {
			if (kindlegen != null) {
				long time = System.currentTimeMillis();
				String outFileName = outFile.getAbsolutePath();
				LogAppender.println("kindlegenを実行します : "+kindlegen.getName()+" \""+outFileName+"\"");
				ProcessBuilder pb = new ProcessBuilder(kindlegen.getAbsolutePath(), "-locale", "en","-verbose", outFileName);
				this.kindleProcess = pb.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(this.kindleProcess.getInputStream()));
				String line;
				int idx = 0;
				int cnt = 0;
				String msg = null;
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						System.out.println(line);
						msg = line;
						if (idx++ % 2 == 0) {
							if (cnt++ > 100) { cnt = 1; LogAppender.println(); }
							LogAppender.append(".");
						}
					}
				}
				br.close();
				if (convertCanceled) {
					LogAppender.println("\n"+msg+"\nkindlegenの変換を中断しました");
				} else {
					LogAppender.println("\n"+msg+"\nkindlegen変換完了 ["+(((System.currentTimeMillis()-time)/100)/10f)+"s]");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (this.kindleProcess != null) this.kindleProcess.destroy();
			this.kindleProcess = null;
		}
		
		////////////////////////////////
		//終了処理
		bookInfo.clear();
		bookInfo = null;
		
	}
	
	////////////////////////////////////////////////////////////////
	void startConvertFilesWorker(Vector<File> vecFiles)
	{
		File[] files = new File[vecFiles.size()];
		for (int i=0; i<files.length; i++) files[i] = vecFiles.get(i);
		startConvertFilesWorker(files);
	}
	void startConvertFilesWorker(File[] files)
	{
		ConvertFilesWorker convertFilesWorker = new ConvertFilesWorker(files, null);
		convertFilesWorker.execute();
	}
	
	/** 別スレッド実行用SwingWorker */
	class ConvertFilesWorker extends SwingWorker<Object, Object>
	{
		/** 面倒なのでAppletを渡す */
		AozoraEpub3Applet applet;
		/** 変換対象ファイル */
		File[] srcFiles;
		
		//ショートカットのコピー等でsrcFileがキャッシュの場合で出力先が同じ場所を指定したときに利用
		File dstPath = null;
		
		public ConvertFilesWorker(File[] srcFiles, File dstPath)
		{
			this.applet = getApplet();
			this.srcFiles = srcFiles;
			this.dstPath = dstPath;
		}
		
		@Override
		protected Object doInBackground() throws Exception
		{
			this.applet.running = true;
			applet.setConvertEnabled(false);
			try {
				applet.convertFiles(srcFiles, dstPath);
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
	
	/** 別スレッド実行用SwingWorker */
	class ConvertWebWorker extends SwingWorker<Object, Object>
	{
		/** 面倒なのでAppletを渡す */
		AozoraEpub3Applet applet;
		/** 変換対象ファイル */
		Vector<String> vecUrlString;
		
		File dstPath = null;
		
		/** @param dstPath ショートカットファイルなら同じ場所出力用に指定 */
		public ConvertWebWorker(Vector<String> vecUrlString, File dstPath)
		{
			this.applet = getApplet();
			this.vecUrlString = vecUrlString;
			
			this.dstPath = dstPath;
		}
		
		@Override
		protected Object doInBackground() throws Exception
		{
			this.applet.running = true;
			applet.setConvertEnabled(false);
			try {
				for (String urlString : vecUrlString) {
					try {
						LogAppender.println("--------");
						LogAppender.append(urlString);
						LogAppender.println(" を読み込みます");
						
						webConverter = WebAozoraConverter.createWebAozoraConverter(urlString, webConfigPath);
						File srcFile = webConverter.convertToAozoraText(urlString, cachePath);
						
						if (srcFile == null) {
							LogAppender.append(urlString);
							if (webConverter != null && webConverter.isCanceled())
								LogAppender.println(" の変換をキャンセルしました");
							else LogAppender.println(" は変換できませんでした");
							return null;
						}
						//エンコードを変換時のみUTF-8にする
						int encIndex = applet.jComboEncType.getSelectedIndex();
						applet.jComboEncType.setSelectedIndex(1);
						applet.convertFiles(new File[]{srcFile}, dstPath);
						applet.jComboEncType.setSelectedIndex(encIndex);
					} catch (Exception e) {
						e.printStackTrace(); LogAppender.println("エラーが発生しました : "+e.getMessage());
					}
				}
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
		for (Component c : this.jTabbedPane.getComponents()) this.setEnabledAll(c, enabled);
		//変換中に操作不可にしないもの
		if (!enabled) this.jCheckConfirm.setEnabled(true);
		this.jButtonCancel.setEnabled(!enabled);
	}
	/** コンポーネント内をすべてsetEnabled */
	private void setEnabledAll(Component c, boolean b)
	{
		if (c instanceof JPanel) {
			for (Component c2 : ((Container)c).getComponents()) setEnabledAll(c2, b);
		} else {//if (!(c instanceof JLabel)) {
			c.setEnabled(b);
		}
	}
	
	private void setResizeTextEditable(boolean enabled)
	{
		if (enabled) {
			this.jTextResizeNumW.setEditable(jCheckResizeW.isSelected());
			this.jTextResizeNumH.setEditable(jCheckResizeH.isSelected());
			//this.jTextPixelW.setEditable(jCheckPixel.isSelected());
			//this.jTextPixelH.setEditable(jCheckPixel.isSelected());
		} else {
			this.jTextResizeNumW.setEditable(false);
			this.jTextResizeNumH.setEditable(false);
			//this.jTextPixelW.setEditable(false);
			//this.jTextPixelH.setEditable(false);
		}
	}
	
	//ディレクトリ以下を削除  パス注意
	void deleteFiles(File path)
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
			String lafName = UIManager.getSystemLookAndFeelClassName();
			if (lafName.startsWith("com.sun.java.swing.plaf.windows.")) {
				UIManager.setLookAndFeel(lafName);
			} else {
				//Windows以外はMetalのままでFontはPLAIN
				UIDefaults defaultTable = UIManager.getLookAndFeelDefaults();
				for (Object o: defaultTable.keySet()) {
					if (o.toString().toLowerCase().endsWith("font")) {
						FontUIResource font = (FontUIResource)UIManager.get(o);
						font = new FontUIResource(font.getName(), Font.PLAIN, font.getSize());
						UIManager.put(o, font);
					}
				}
			}
			
		} catch(Exception e) { e.printStackTrace(); }
		
		//フレーム初期化
		final JFrame jFrame = new JFrame("青空文庫テキスト → ePub3変換");
		//アップレット生成と初期化
		final AozoraEpub3Applet applet = new AozoraEpub3Applet(jFrame);
		applet.iconImage = java.awt.Toolkit.getDefaultToolkit().createImage(AozoraEpub3Applet.class.getResource("images/icon.png"));
		applet.init();
		
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
		
		//引数にファイルが指定されていたら変換実行
		for (String fileName : args) {
			File file = new File(fileName);
			Vector<File> vecFiles = new Vector<File>();
			if (file.exists()) vecFiles.add(file);
			if (vecFiles.size() > 0) {
				File[] files = new File[vecFiles.size()];
				for (int i=0; i<files.length; i++) files[i] = vecFiles.get(i);
				applet.startConvertFilesWorker(files);
			}
		}
	}
	
	/** アプレット終了時の処理
	 * 設定ファイルを保存 */
	@Override
	protected void finalize() throws Throwable
	{
		this.convertCanceled = true;
		
		/*
		try {
			//tmp削除
			if (tmpPath != null) this.deleteFiles(this.tmpPath);
			//キャッシュファイル削除
			if (cachePath != null) this.deleteFiles(this.cachePath);
		} catch (Exception e) { e.printStackTrace(); }
		*/
		
		this.props.setProperty("DividerLocation", ""+this.jSplitPane.getDividerLocation());
		
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
		//挿絵非表示
		this.props.setProperty("NoIllust", this.jCheckNoIllust.isSelected()?"1":"");
		//画面サイズ
		this.props.setProperty("DispW", this.jTextDispW.getText());
		this.props.setProperty("DispH", this.jTextDispH.getText());
		//画像単ページ
		this.props.setProperty("SinglePageSizeW", this.jTextSinglePageSizeW.getText());
		this.props.setProperty("SinglePageSizeH", this.jTextSinglePageSizeH.getText());
		this.props.setProperty("SinglePageWidth", this.jTextSinglePageWidth.getText());
		this.props.setProperty("FitImage", this.jCheckFitImage.isSelected()?"1":"");
		//画像回り込み
		this.props.setProperty("ImageFloatType", ""+this.jComboImageFloatType.getSelectedIndex());
		this.props.setProperty("ImageFloatW", this.jTextImageFloatW.getText());
		this.props.setProperty("ImageFloatH", this.jTextImageFloatH.getText());
		//画像サイズ
		this.props.setProperty("ResizeW", this.jCheckResizeW.isSelected()?"1":"");
		this.props.setProperty("ResizeH", this.jCheckResizeH.isSelected()?"1":"");
		this.props.setProperty("ResizeNumW", this.jTextResizeNumW.getText());
		this.props.setProperty("ResizeNumH", this.jTextResizeNumH.getText());
		//this.props.setProperty("Pixel", this.jCheckPixel.isSelected()?"1":"");
		//this.props.setProperty("PixelW", this.jTextPixelW.getText());
		//this.props.setProperty("PixelH", this.jTextPixelH.getText());
		//表紙
		this.props.setProperty("CoverW", this.jTextCoverW.getText());
		this.props.setProperty("CoverH", this.jTextCoverH.getText());
		//JPEG画質
		this.props.setProperty("JpegQuality", this.jTextJpegQuality.getText());
		//余白除去
		this.props.setProperty("AutoMargin", this.jCheckAutoMargin.isSelected()?"1":"");
		this.props.setProperty("AutoMarginLimitH", this.jTextAutoMarginLimitH.getText());
		this.props.setProperty("AutoMarginLimitV", this.jTextAutoMarginLimitV.getText());
		this.props.setProperty("AutoMarginWhiteLevel", this.jTextAutoMarginWhiteLevel.getText());
		this.props.setProperty("AutoMarginPadding", this.jTextAutoMarginPadding.getText());
		this.props.setProperty("AutoMarginNombre", ""+this.jComboAutoMarginNombre.getSelectedIndex());
		//空白の禁則処理
		this.props.setProperty("SpaceHyphenation", this.jRadioSpaceHyp0.isSelected()?"0":(this.jRadioSpaceHyp1.isSelected()?"1":"2"));
		//自動縦中横
		this.props.setProperty("AutoYoko", this.jCheckAutoYoko.isSelected()?"1":"");
		this.props.setProperty("AutoYokoNum1", this.jCheckAutoYokoNum1.isSelected()?"1":"");
		this.props.setProperty("AutoYokoNum3", this.jCheckAutoYokoNum3.isSelected()?"1":"");
		this.props.setProperty("AutoYokoEQ1", this.jCheckAutoEQ1.isSelected()?"1":"");
		//コメント出力
		this.props.setProperty("CommentPrint", this.jCheckCommentPrint.isSelected()?"1":"");
		this.props.setProperty("CommentConvert", this.jCheckCommentConvert.isSelected()?"1":"");
		//空行除去
		this.props.setProperty("RemoveEmptyLine", ""+this.jComboxRemoveEmptyLine.getSelectedItem().toString().trim());
		//強制改ページ
		this.props.setProperty("PageBreak", this.jCheckPageBreak.isSelected()?"1":"");
		this.props.setProperty("PageBreakSize", ""+this.jTextPageBreakSize.getText().trim());
		this.props.setProperty("PageBreakEmpty", this.jCheckPageBreakEmpty.isSelected()?"1":"");
		this.props.setProperty("PageBreakEmptyLine", ""+this.jComboxPageBreakEmptyLine.getSelectedItem().toString().trim());
		this.props.setProperty("PageBreakEmptySize", ""+this.jTextPageBreakEmptySize.getText().trim());
		this.props.setProperty("PageBreakChapter", this.jCheckPageBreakChapter.isSelected()?"1":"");
		this.props.setProperty("PageBreakChapterSize", ""+this.jTextPageBreakChapterSize.getText().trim());
		//目次出力
		this.props.setProperty("MaxChapterNameLength", this.jTextMaxChapterNameLength.getText());
		this.props.setProperty("CoverPageToc", this.jCheckCoverPageToc.isSelected()?"1":"");
		this.props.setProperty("ChapterUseNextLine", this.jCheckChapterUseNextLine.isSelected()?"1":"");
		this.props.setProperty("ChapterExclude", this.jCheckChapterExclude.isSelected()?"1":"");
		this.props.setProperty("ChapterSection", this.jCheckChapterSection.isSelected()?"1":"");
		this.props.setProperty("ChapterH", this.jCheckChapterH.isSelected()?"1":"");
		this.props.setProperty("ChapterH1", this.jCheckChapterH1.isSelected()?"1":"");
		this.props.setProperty("ChapterH2", this.jCheckChapterH2.isSelected()?"1":"");
		this.props.setProperty("ChapterH3", this.jCheckChapterH3.isSelected()?"1":"");
		this.props.setProperty("ChapterName", this.jCheckChapterName.isSelected()?"1":"");
		this.props.setProperty("ChapterNumOnly", this.jCheckChapterNumOnly.isSelected()?"1":"");
		this.props.setProperty("ChapterNumTitle", this.jCheckChapterNumTitle.isSelected()?"1":"");
		this.props.setProperty("ChapterNumParen", this.jCheckChapterNumParen.isSelected()?"1":"");
		this.props.setProperty("ChapterNumParenTitle", this.jCheckChapterNumParenTitle.isSelected()?"1":"");
		this.props.setProperty("ChapterPattern", this.jCheckChapterPattern.isSelected()?"1":"");
		this.props.setProperty("ChapterPatternText", this.jComboChapterPattern.getEditor().getItem().toString().trim());
		
		this.props.setProperty("EncType", ""+this.jComboEncType.getSelectedIndex());
		this.props.setProperty("OverWrite", this.jCheckOverWrite.isSelected()?"1":"");
		this.props.setProperty("LastDir", this.currentPath==null?"":this.currentPath.getAbsolutePath());
		//設定ファイル更新
		this.props.store(new FileOutputStream(this.jarPath+this.propFileName), "AozoraEpub3 Parameters");
		
		super.finalize();
	}
}
