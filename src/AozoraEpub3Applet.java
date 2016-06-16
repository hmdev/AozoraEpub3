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
import java.awt.Toolkit;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.apache.commons.compress.utils.IOUtils;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.image.ImageInfoReader;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.BookInfoHistory;
import com.github.hmdev.info.ProfileInfo;
import com.github.hmdev.info.SectionInfo;
import com.github.hmdev.swing.JConfirmDialog;
import com.github.hmdev.swing.JProfileDialog;
import com.github.hmdev.swing.NarrowTitledBorder;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.web.WebAozoraConverter;
import com.github.hmdev.writer.Epub3ImageWriter;
import com.github.hmdev.writer.Epub3Writer;

/**
 * 青空文庫テキスト→ePub3変換操作用アプレット
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
	
	/** タブの上のパネル */
	JPanel topPanel;
	/** 上部タブパネル */
	JTabbedPane jTabbedPane;
	
	/** 変換前確認ダイアログ */
	JConfirmDialog jConfirmDialog;
	
	/** プロファイル選択ダイアログ */
	JProfileDialog jProfileDialog;
	
	/** プロファイル選択 */
	JComboBox jComboProfile;
	JButton jButtonProfileCreate;
	JButton jButtonProfileEdit;
	JButton jButtonProfileUp;
	JButton jButtonProfileDown;
	
	/** 端末プリセット読み込み */
	JButton jButtonPreset;
	JPopupMenu jPopupPreset;
	
	/** 表題 */
	JComboBox jComboTitle;
	JCheckBox jCheckPubFirst;
	JCheckBox jCheckUseFileName;
	JCheckBox jCheckAutoFileName;
	JCheckBox jCheckTitlePage;
	JRadioButton jRadioTitleNormal;
	JRadioButton jRadioTitleMiddle;
	JRadioButton jRadioTitleHorizontal;
	
	JCheckBox jCheckConfirm;
	
	//出力先
	/** 入力と同じ */
	JCheckBox jCheckSamePath;
	JComboBox jComboDstPath;
	JButton jButtonDstPath;
	
	JComboBox jComboExt;
	JCheckBox jCheckMarkId;
	
	JCheckBox jCheckOverWrite;
	//JCheckBox jCheckGaiji32;
	
	JRadioButton jRadioVertical;
	JRadioButton jRadioHorizontal;
	
	JRadioButton jRadioLtR;
	JRadioButton jRadioRtL;
	
	//入力ファイルエンコード
	JComboBox jComboEncType;
	
	/** 表紙 */
	JComboBox jComboCover;
	JCheckBox jCheckCoverHistory;
	JButton jButtonCover;
	JTextField jTextMaxCoverLine;
	JLabel jLabelMaxCoverLine;
	
	/** ページ出力 */
	JCheckBox jCheckCoverPage;
	JCheckBox jCheckTocPage;
	JRadioButton jRadioTocV;
	JRadioButton jRadioTocH;
	
	/** ファイル選択ボタン */
	JButton jButtonFile;
	
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
	
	//回り込み
	JCheckBox jCheckImageFloat;
	JComboBox jComboImageFloatType;
	JTextField jTextImageFloatW;
	JTextField jTextImageFloatH;
	
	//単ページ
	JTextField jTextSinglePageSizeW;
	JTextField jTextSinglePageSizeH;
	JTextField jTextSinglePageWidth;
	
	JRadioButton jRadioImageSizeType1;
	JRadioButton jRadioImageSizeType3;
	JCheckBox jCheckFitImage;
	
	//Float指定
	JCheckBox jCheckImageFloatPage;
	JCheckBox jCheckImageFloatBlock;

	JCheckBox jCheckSvgImage;
	
	JComboBox jComboRotateImage;
	
	//倍率
	JCheckBox jCheckImageScale;
	JTextField jTextImageScale;
	
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
	//ガンマ補正
	JCheckBox jCheckGamma;
	JTextField jTextGammaValue;
	//余白除去
	JCheckBox jCheckAutoMargin;
	JTextField jTextAutoMarginLimitH;
	JTextField jTextAutoMarginLimitV;
	JTextField jTextAutoMarginWhiteLevel;
	JTextField jTextAutoMarginPadding;
	JComboBox jComboAutoMarginNombre;
	JTextField jTextAutoMarginNombreSize;
	
	JRadioButton jRadioSpaceHyp0;
	JRadioButton jRadioSpaceHyp1;
	JRadioButton jRadioSpaceHyp2;
	
	JRadioButton jRadioChukiRuby0;
	JRadioButton jRadioChukiRuby1;
	JRadioButton jRadioChukiRuby2;
	
	JCheckBox jCheckAutoYoko;
	JCheckBox jCheckAutoYokoNum1;
	JCheckBox jCheckAutoYokoNum3;
	JCheckBox jCheckAutoEQ1;
	
	JCheckBox jCheckCommentPrint;
	JCheckBox jCheckCommentConvert;
	
	JComboBox jComboxRemoveEmptyLine;
	JComboBox jComboxMaxEmptyLine;
	
	JCheckBox jCheckForceIndent;
	
	JCheckBox jCheckPageBreak;
	JTextField jTextPageBreakSize;
	JCheckBox jCheckPageBreakEmpty;
	JComboBox jComboxPageBreakEmptyLine;
	JTextField jTextPageBreakEmptySize;
	JCheckBox jCheckPageBreakChapter;
	JTextField jTextPageBreakChapterSize;
	
	JTextField jTextMaxChapterNameLength;
	JCheckBox jCheckChapterUseNextLine;
	JCheckBox jCheckChapterExclude;
	JCheckBox jCheckCoverPageToc;
	
	JCheckBox jCheckNavNest;
	JCheckBox jCheckNcxNest;
	
	JCheckBox jCheckTitleToc;
	JCheckBox jCheckChapterSection;
	JCheckBox jCheckChapterH;
	JCheckBox jCheckChapterH1;
	JCheckBox jCheckChapterH2;
	JCheckBox jCheckChapterH3;
	JCheckBox jCheckSameLineChapter;
	JCheckBox jCheckChapterName;
	JCheckBox jCheckChapterNumTitle;
	JCheckBox jCheckChapterNumOnly;
	JCheckBox jCheckChapterNumParen;
	JCheckBox jCheckChapterNumParenTitle;
	JCheckBox jCheckChapterPattern;
	JComboBox jComboChapterPattern;
	
	//スタイル
	JComboBox jComboLineHeight;
	JComboBox jComboFontSize;
	JCheckBox jCheckBoldUseGothic;
	JCheckBox jCheckGothicUseBold;
	
	JRadioButton jRadioPageMarginUnit0;
	JRadioButton jRadioPageMarginUnit1;
	JTextField[] jTextPageMargins;
	JRadioButton jRadioBodyMarginUnit0;
	JRadioButton jRadioBodyMarginUnit1;
	JTextField[] jTextBodyMargins;
	
	//文字
	JRadioButton jRadioDakutenType0;
	JRadioButton jRadioDakutenType1;
	JRadioButton jRadioDakutenType2;
	JCheckBox jCheckIvsBMP;
	JCheckBox jCheckIvsSSP;
	
	//Web
	JTextField jTextWebInterval;
	JTextField jTextCachePath;
	JButton jButtonCachePath;
	JCheckBox jCheckWebConvertUpdated;
	JCheckBox jCheckWebBeforeChapter;
	JTextField jTextWebBeforeChapterCount;
	JCheckBox jCheckWebModifiedOnly;
	JCheckBox jCheckWebModifiedTail;
	JTextField jTextWebModifiedExpire;
	
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
	/** Web小説取得情報格納パス */
	File webConfigPath = null;
	
	/** 選択されているプロファイル */
	ProfileInfo selectedProfile;
	/** プロファイル格納パス */
	File profilePath;
	
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
		this.setSize(new Dimension(520, 460));
		
		//パス関連初期化
		//this.jarPath = getClass().getClassLoader().getResource("").getFile();
		//this.jarPath = this.jarPath.replaceFirst("\\/bin\\/$", "/");
		//AppletではVelocityでパスがエラーになるのでとりあえず空文字に
		this.jarPath = "";
		
		this.cachePath = new File(this.jarPath+".cache");
		this.webConfigPath = new File(this.jarPath+"web");
		this.profilePath = new File(this.jarPath+"profiles");
		this.profilePath.mkdir();
		
		//設定ファイル読み込み
		props = new Properties(); 
		try {
			FileInputStream fos = new FileInputStream(this.jarPath+this.propFileName);
			props.load(fos);
			fos.close();
		} catch (Exception e) { }
		String path = props.getProperty("LastDir");
		if (path != null && path.length() >0) this.currentPath = new File(path);
		
		JPanel tabPanel;
		JPanel panel;
		JPanel panelV;
		JPanel panelH;
		JLabel label;
		Border padding0 = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border padding1 = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		Border padding2 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		Border padding3 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		Border padding5H3V = BorderFactory.createEmptyBorder(3, 5, 3, 5);
		Border padding2H = BorderFactory.createEmptyBorder(0, 2, 0, 2);
		Border padding4H = BorderFactory.createEmptyBorder(0, 4, 0, 4);
		Border padding4H2V = BorderFactory.createEmptyBorder(2, 4, 2, 4);
		Border padding3B = BorderFactory.createEmptyBorder(0, 0, 3, 0);
		Border padding4B = BorderFactory.createEmptyBorder(0, 0, 4, 0);
		Border iconPadding = BorderFactory.createEmptyBorder(0, 14, 0, 0);
		
		Dimension panelSize = new Dimension(1920, 26);
		Dimension panelSize28 = new Dimension(1920, 28);
		Dimension panelVMaxSize = new Dimension(640, 22);
		JTextField text = new JTextField();
		Insets is = text.getInsets();
		FontMetrics fm = this.getFontMetrics(text.getFont());
		Dimension text3 = new Dimension(fm.stringWidth("000")+is.left+is.right+2, 20);
		Dimension text4 = new Dimension(fm.stringWidth("0000")+is.left+is.right+2, 20);
		Dimension text5 = new Dimension(fm.stringWidth("00000")+is.left+is.right+2, 20);
		Dimension text300 = new Dimension(300, 20);
		Dimension combo3 = new Dimension(text3.width+20, 20);
		
		//アップレットのレイアウト設定
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		int dividerLocation = 230;
		try { dividerLocation = Integer.parseInt(props.getProperty("DividerLocation")); } catch (Exception e) {}
		jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		jSplitPane.setDividerLocation(dividerLocation);
		jSplitPane.setDividerSize(3);
		this.add(jSplitPane);
		
		//Tab icons
		ImageIcon epubIcon = new ImageIcon(AozoraEpub3Applet.class.getResource("images/epub.png"));
		ImageIcon imageIcon = new ImageIcon(AozoraEpub3Applet.class.getResource("images/image.png"));
		ImageIcon pageSettingIcon = new ImageIcon(AozoraEpub3Applet.class.getResource("images/page_setting.png"));
		ImageIcon tocIcon = new ImageIcon(AozoraEpub3Applet.class.getResource("images/toc.png"));
		ImageIcon styleIcon = new ImageIcon(AozoraEpub3Applet.class.getResource("images/style.png"));
		ImageIcon webIcon = new ImageIcon(AozoraEpub3Applet.class.getResource("images/web.png"));
		ImageIcon charIcon = new ImageIcon(AozoraEpub3Applet.class.getResource("images/char.png"));
		
		ButtonGroup buttonGroup;
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));
		jSplitPane.add(topPane);
		
		////////////////////////////////////////////////////////////////
		//プリセットとプロファイル
		////////////////////////////////////////////////////////////////
		//プロファイルダイアログ
		jProfileDialog = new JProfileDialog(
			iconImage, AozoraEpub3Applet.class.getResource("images/icon.png").toString().replaceFirst("/icon\\.png", "/") );
		jProfileDialog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				try {
				switch (evt.getID()) {
				case 1:
					addProfile(evt.getSource().toString());
					break;
				case 2:
					editProfile(evt.getSource().toString());
					break;
				case 3:
					deleteProfile();
					break;
				}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		////////////////////////////////
		panel = new JPanel();
		topPanel = panel;
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMinimumSize(panelSize28);
		panel.setMaximumSize(panelSize28);
		panel.setPreferredSize(panelSize28);
		panel.setBorder(padding4H2V);
		topPane.add(panel);
		//プロファイル
		label = new JLabel("プロファイル: ");
		panel.add(label);
		jComboProfile = new JComboBox();
		jComboProfile.setEditable(false);
		jComboProfile.setFocusable(false);
		jComboProfile.setPreferredSize(new Dimension(320, 22));
		jComboProfile.setBorder(padding0);
		panel.add(jComboProfile);
		panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panel.add(panelV);
		//上
		jButtonProfileUp = new JButton(new ImageIcon(AozoraEpub3Applet.class.getResource("images/spin_up.png")));
		jButtonProfileUp.setToolTipText("選択中のプロファイルを選択リスト内で上に移動します");
		jButtonProfileUp.setBorder(padding2);
		jButtonProfileUp.setFocusPainted(false);
		jButtonProfileUp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) {
			int idx = jComboProfile.getSelectedIndex();
			if (idx > 0) {
				Object item = jComboProfile.getItemAt(idx-1);
				jComboProfile.removeItemAt(idx-1);
				jComboProfile.insertItemAt(item, idx);
				//移動ボタン有効化
				setProfileMoveEnable();
			}
		}});
		panelV.add(jButtonProfileUp);
		//下
		jButtonProfileDown = new JButton(new ImageIcon(AozoraEpub3Applet.class.getResource("images/spin_down.png")));
		jButtonProfileDown.setToolTipText("選択中のプロファイルを選択リスト内で下に移動します");
		jButtonProfileDown.setBorder(padding2);
		jButtonProfileDown.setFocusPainted(false);
		jButtonProfileDown.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) {
			int idx = jComboProfile.getSelectedIndex();
			if (idx < jComboProfile.getItemCount()-1) {
				Object item = jComboProfile.getItemAt(idx+1);
				jComboProfile.removeItemAt(idx+1);
				jComboProfile.insertItemAt(item, idx);
				//移動ボタン有効化
				setProfileMoveEnable();
			}
		}});
		panelV.add(jButtonProfileDown);
		//新規
		jButtonProfileCreate = new JButton(new ImageIcon(AozoraEpub3Applet.class.getResource("images/add.png")));
		jButtonProfileCreate.setToolTipText("現在の設定でプロファイルを新規作成します");
		jButtonProfileCreate.setBorder(padding3);
		jButtonProfileCreate.setFocusPainted(false);
		jButtonProfileCreate.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) {
			jProfileDialog.showCreate(jFrameParent.getLocation(), jComboProfile.getSelectedItem().toString()+"のコピー");
		}});
		panel.add(jButtonProfileCreate);
		//編集
		jButtonProfileEdit = new JButton(new ImageIcon(AozoraEpub3Applet.class.getResource("images/edit.png")));
		jButtonProfileEdit.setToolTipText("選択中のプロファイルを名称変更・削除します");
		jButtonProfileEdit.setBorder(padding3);
		jButtonProfileEdit.setFocusPainted(false);
		jButtonProfileEdit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) {
			jProfileDialog.showEdit(jFrameParent.getLocation(), jComboProfile.getSelectedItem().toString(), jComboProfile.getItemCount() > 1);
		}});
		panel.add(jButtonProfileEdit);
		
		//端末プリセット
		label = new JLabel("  ");
		panel.add(label);
		jPopupPreset = new JPopupMenu();
		
		//presetsファイルから名称を取得してPopupに追加
		for (File presetFile : new File(jarPath+"presets").listFiles()) {
			Properties presetProps = new Properties();
			try {
				FileInputStream fos = new FileInputStream(presetFile);
				presetProps.load(fos);
				fos.close();
			} catch (Exception e) {}
			String name = presetProps.getProperty("PresetName");
			if (name != null && name.length() > 0) {
				JMenuItem menu = new JMenuItem(name);
				String iconName = presetProps.getProperty("PresetIcon");
				if (iconName != null) {
					try { menu.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/"+iconName))); } catch (Exception r) {}
				}
				menu.addActionListener(new LoadPresetActionListener(presetProps));
				jPopupPreset.add(menu);
			}
		}
		jButtonPreset = new JButton("端末設定", new ImageIcon(AozoraEpub3Applet.class.getResource("images/viewer.png")));
		jButtonPreset.setToolTipText("端末に合わせた画面サイズと機種依存の最低限の設定を反映します");
		jButtonPreset.setBorder(padding3);
		jButtonPreset.setFocusPainted(false);
		jButtonPreset.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) {
			jPopupPreset.show(jButtonPreset, 8, 20);
		}});
		panel.add(jButtonPreset);
		
		
		jTabbedPane = new JTabbedPane();
		jTabbedPane.setBorder(padding2H);
		topPane.add(jTabbedPane);
		
		////////////////////////////////////////////////////////////////
		//Tab 変換
		////////////////////////////////////////////////////////////////
		
		tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		jTabbedPane.setPreferredSize(new Dimension(1920, 220));
		jTabbedPane.addTab("変換 ", epubIcon, tabPanel);
		
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
		jComboTitle.setMaximumSize(new Dimension(200, 22));
		jComboTitle.setBorder(padding0);
		((JLabel)jComboTitle.getRenderer()).setBorder(padding2H);
		panel.add(jComboTitle);
		//入力ファイル名優先
		jCheckPubFirst = new JCheckBox("先頭が発行者");
		jCheckPubFirst.setFocusPainted(false);
		panel.add(jCheckPubFirst);
		//入力ファイル名優先
		panel.add(new JLabel("  "));
		jCheckUseFileName = new JCheckBox("ファイル名優先 ");
		jCheckUseFileName.setFocusPainted(false);
		panel.add(jCheckUseFileName);
		
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
		
		jTextMaxCoverLine = new JTextField("10");
		jTextMaxCoverLine.setToolTipText("先頭の挿絵に利用する画像注記を取得する最大行数 0なら制限なし");
		jTextMaxCoverLine.setHorizontalAlignment(JTextField.RIGHT);
		jTextMaxCoverLine.setMinimumSize(text4);
		jTextMaxCoverLine.setMaximumSize(text4);
		jTextMaxCoverLine.setPreferredSize(text4);
		jTextMaxCoverLine.addFocusListener(new TextSelectFocusListener(jTextMaxCoverLine));
		jTextMaxCoverLine.setInputVerifier(new IntegerInputVerifier(10, 0, 9999));
		panel.add(jTextMaxCoverLine);
		jLabelMaxCoverLine = new JLabel("行目までの");
		jLabelMaxCoverLine.setToolTipText(jTextMaxCoverLine.getToolTipText());
		jLabelMaxCoverLine.setBorder(padding2H);
		panel.add(jLabelMaxCoverLine);
		
		jComboCover = new JComboBox(new String[]{"[先頭の挿絵]", "[入力ファイル名と同じ画像(png,jpg)]", "[表紙無し]", "http://"});
		jComboCover.setEditable(true);
		jComboCover.setPreferredSize(new Dimension(320, 24));
		jComboCover.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) {
			boolean visible = jComboCover.getSelectedIndex() == 0;
			jTextMaxCoverLine.setVisible(visible);
			jLabelMaxCoverLine.setVisible(visible);
		}});
		panel.add(jComboCover);
		new DropTarget(jComboCover.getEditor().getEditorComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new DropCoverListener(), true);
		boolean visible = jComboCover.getSelectedIndex() == 0;
		jTextMaxCoverLine.setVisible(visible);
		jLabelMaxCoverLine.setVisible(visible);
		
		jButtonCover = new JButton("選択");
		jButtonCover.setBorder(padding3);
		jButtonCover.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/cover.png")));
		jButtonCover.setFocusPainted(false);
		jButtonCover.addActionListener(new CoverChooserListener(this));
		panel.add(jButtonCover);
		jCheckCoverHistory = new JCheckBox("表紙履歴利用", true);
		jCheckCoverHistory.setToolTipText("前回の変換(またはスキップ)で設定した表紙を利用します ※履歴は再起動時に初期化されます");
		jCheckCoverHistory.setFocusPainted(false);
		panel.add(jCheckCoverHistory);
		
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
		jCheckCoverPage = new JCheckBox("表紙画像 ", true);
		jCheckCoverPage.setFocusPainted(false);
		panel.add(jCheckCoverPage);
		//左右中央
		jCheckTitlePage = new JCheckBox("表題", true);
		jCheckTitlePage.setToolTipText("表題を単独のページで出力します。チェック無し時は表題等は出力されません");
		jCheckTitlePage.setFocusPainted(false);
		panel.add(jCheckTitlePage);
		label = new JLabel("(");
		panel.add(label);
		buttonGroup = new ButtonGroup();
		jRadioTitleNormal = new JRadioButton("本文内 ");
		jRadioTitleNormal.setToolTipText("別ページ処理せずに本文中に表題等を出力します。 目次は表題前に出力されます");
		jRadioTitleNormal.setBorder(padding0);
		jRadioTitleNormal.setIconTextGap(1);
		panel.add(jRadioTitleNormal);
		buttonGroup.add(jRadioTitleNormal);
		jRadioTitleMiddle = new JRadioButton("中央 ", true);
		jRadioTitleMiddle.setToolTipText("中央寄せの表題ページを出力します");
		jRadioTitleMiddle.setBorder(padding0);
		jRadioTitleMiddle.setIconTextGap(1);
		panel.add(jRadioTitleMiddle);
		buttonGroup.add(jRadioTitleMiddle);
		jRadioTitleHorizontal = new JRadioButton("横書き");
		jRadioTitleHorizontal.setToolTipText("横書きの表題ページを出力します");
		jRadioTitleHorizontal.setBorder(padding0);
		jRadioTitleHorizontal.setIconTextGap(1);
		panel.add(jRadioTitleHorizontal);
		buttonGroup.add(jRadioTitleHorizontal);
		label = new JLabel(") ");
		panel.add(label);
		jCheckTocPage = new JCheckBox("目次");
		jCheckTocPage.setToolTipText("目次ページを表題ページの次に追加します");
		jCheckTocPage.setFocusPainted(false);
		panel.add(jCheckTocPage);
		label = new JLabel("(");
		panel.add(label);
		buttonGroup = new ButtonGroup();
		jRadioTocV = new JRadioButton("縦 ", true);
		jRadioTocV.setFocusPainted(false);
		jRadioTocV.setIconTextGap(2);
		jRadioTocV.setBorder(padding0);
		panel.add(jRadioTocV);
		buttonGroup.add(jRadioTocV);
		jRadioTocH = new JRadioButton("横");
		jRadioTocH.setFocusPainted(false);
		jRadioTocH.setIconTextGap(2);
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
		jComboExt = new JComboBox(new String[]{".epub", ".kepub.epub", ".fxl.kepub.epub", ".mobi", ".mobi+.epub"});
		jComboExt.setToolTipText("出力するファイルの拡張子を選択します。 mobi出力時はKindlegenが必要になります");
		jComboExt.setEditable(true);
		jComboExt.setMaximumSize(new Dimension(110, 24));
		jComboExt.setPreferredSize(new Dimension(110, 24));
		panel.add(jComboExt);
		label = new JLabel("  ");
		panel.add(label);
		//出力ファイル名設定
		jCheckAutoFileName = new JCheckBox("出力ファイル名に表題利用", true);
		jCheckAutoFileName.setFocusPainted(false);
		panel.add(jCheckAutoFileName);
		label = new JLabel("  ");
		panel.add(label);
		//ファイルの上書き許可
		jCheckOverWrite = new JCheckBox("出力ファイル上書き", true);
		jCheckOverWrite.setFocusPainted(false);
		panel.add(jCheckOverWrite);
		
		////////////////////////////////
		//出力先 履歴をpropsから読み込んで設定
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
		jCheckSamePath = new JCheckBox("入力と同じ", true);
		jCheckSamePath.setToolTipText("入力ファイルと同じ場所に出力します");
		panel.add(jCheckSamePath);
		jCheckSamePath.addChangeListener(new ChangeListener() { public void stateChanged(ChangeEvent e){
			jComboDstPath.setEditable(!jCheckSamePath.isSelected());
			jComboDstPath.setForeground(jCheckSamePath.isSelected()?Color.gray:Color.black);
			jComboDstPath.repaint();
		}});
		jComboDstPath = new JComboBox();
		jComboDstPath.setToolTipText("出力先を指定します。変換時に履歴に追加されます。フォルダのドロップでも設定できます");
		jComboDstPath.setEditable(false);
		jComboDstPath.setForeground(Color.gray);
		jComboDstPath.setPreferredSize(new Dimension(260, 24));
		//パスを追加
		//vecDstPath.add("[入力ファイルと同じ場所]");
		String propValue = props.getProperty("DstPathList");
		String dstPath = props.getProperty("DstPath");
		if (propValue!=null && propValue.length()>0) {
			for (String listPath : propValue.split(",")) {
				if (!"".equals(listPath)) jComboDstPath.addItem(listPath);
			}
		}
		if (dstPath!=null && !dstPath.equals("")) {
			jComboDstPath.setSelectedItem(dstPath);
		}
		panel.add(jComboDstPath);
		new DropTarget(jComboDstPath.getEditor().getEditorComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new DropDstPathListener(), true);
		//選択解除
		if ("".equals(props.getProperty("SamePath"))) jCheckSamePath.setSelected(false);
		
		jButtonDstPath = new JButton("選択");
		jButtonDstPath.setBorder(padding3);
		jButtonDstPath.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/dst_path.png")));
		jButtonDstPath.setFocusPainted(false);
		jButtonDstPath.addActionListener(dstPathChooser);
		panel.add(jButtonDstPath);
		
		////////////////////////////////
		/*group = new ButtonGroup();
		jRadioRtL = new JRadioButton("右→左");
		jRadioRtL.setFocusPainted(false);
		panel.add(jRadioRtL);
		group.add(jRadioRtL);
		jRadioLtR = new JRadioButton("左→右");
		jRadioLtR.setFocusPainted(false);
		panel.add(jRadioLtR);
		group.add(jRadioLtR);*/
		
		////////////////////////////////
		//変換
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMaximumSize(new Dimension(1920, 32));
		panel.setPreferredSize(new Dimension(1920, 32));
		panel.setBorder(padding4H);
		tabPanel.add(panel);
		//左パネル
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//panel1.setPreferredSize(panelSize);
		panel1.setBorder(padding0);
		//入力文字コード
		label = new JLabel("入力文字コード");
		label.setBorder(padding0);
		panel1.add(label);
		jComboEncType = new JComboBox(new String[]{"MS932", "UTF-8"});
		jComboEncType.setToolTipText("入力ファイルのテキストファイルの文字コード。青空文庫の標準はMS932(SJIS)です");
		jComboEncType.setFocusable(false);
		jComboEncType.setPreferredSize(new Dimension(100, 22));
		panel1.add(jComboEncType);
		panel.add(panel1);
		
		//右パネル
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		//panel2.setPreferredSize(panelSize);
		panel.setBorder(padding0);
		//縦書き横書き
		buttonGroup = new ButtonGroup();
		jRadioVertical = new JRadioButton();
		jRadioVertical.setSelected(true);
		jRadioVertical.setFocusPainted(false);
		//jRadioVertical.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 0));
		jRadioVertical.setBorder(padding0);
		jRadioVertical.setIconTextGap(0);
		label = new JLabel("縦書き", new ImageIcon(AozoraEpub3Applet.class.getResource("images/page_vertical.png")), JLabel.LEFT);
		label.setBorder(iconPadding);
		jRadioVertical.add(label);
		panel2.add(jRadioVertical);
		buttonGroup.add(jRadioVertical);
		jRadioHorizontal = new JRadioButton();
		jRadioHorizontal.setFocusPainted(false);
		jRadioHorizontal.setBorder(padding0);
		jRadioHorizontal.setIconTextGap(0);
		label = new JLabel("横書き ", new ImageIcon(AozoraEpub3Applet.class.getResource("images/page_horizontal.png")), JLabel.LEFT);
		label.setBorder(iconPadding);
		jRadioHorizontal.add(label);
		panel2.add(jRadioHorizontal);
		buttonGroup.add(jRadioHorizontal);
		//ファイル選択
		jButtonFile = new JButton("ファイル選択");
		jButtonFile.setToolTipText("ファイル選択後に変換処理を開始します");
		jButtonFile.setBorder(padding5H3V);
		//jButtonFile.setPreferredSize(new Dimension(100, 24));
		jButtonFile.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/convert.png")));
		jButtonFile.setFocusPainted(false);
		jButtonFile.addActionListener(new FileChooserListener(this));
		panel2.add(jButtonFile);
		panel.add(panel2);
		
		////////////////////////////////////////////////////////////////
		//Tab 画像配置
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.addTab("画像1", imageIcon, tabPanel);
		
		////////////////////////////////
		//挿絵なし
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画像注記"));
		tabPanel.add(panel);
		jCheckNoIllust = new JCheckBox("挿絵除外 ");
		jCheckNoIllust.setFocusPainted(false);
		jCheckNoIllust.setToolTipText("テキストの画像注記は表紙と外字画像以外はePubに格納されません");
		jCheckNoIllust.setBorder(padding2);
		panel.add(jCheckNoIllust);
		
		////////////////////////////////
		//画面サイズ
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画面・表紙サイズ"));
		tabPanel.add(panel);
		//画面サイズ
		label = new JLabel(" 画面: 横");
		panel.add(label);
		jTextDispW = new JTextField("600");
		jTextDispW.setHorizontalAlignment(JTextField.RIGHT);
		jTextDispW.setMaximumSize(text4);
		jTextDispW.setPreferredSize(text4);
		jTextDispW.addFocusListener(new TextSelectFocusListener(jTextDispW));
		jTextDispW.setInputVerifier(new IntegerInputVerifier(600, 1, 9999));
		panel.add(jTextDispW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		jTextDispH = new JTextField("800");
		jTextDispH.setHorizontalAlignment(JTextField.RIGHT);
		jTextDispH.setMaximumSize(text4);
		jTextDispH.setPreferredSize(text4);
		jTextDispH.addFocusListener(new TextSelectFocusListener(jTextDispH));
		jTextDispH.setInputVerifier(new IntegerInputVerifier(800, 1, 9999));
		panel.add(jTextDispH);
		label = new JLabel("px");
		label.setBorder(padding2H);
		panel.add(label);
		////////////////////////////////
		//表紙サイズ
		//横x縦
		label = new JLabel("  表紙: 横");
		panel.add(label);
		jTextCoverW = new JTextField("600");
		jTextCoverW.setHorizontalAlignment(JTextField.RIGHT);
		jTextCoverW.setInputVerifier(new IntegerInputVerifier(600, 64, 4096));
		jTextCoverW.setMaximumSize(text4);
		jTextCoverW.setPreferredSize(text4);
		jTextCoverW.addFocusListener(new TextSelectFocusListener(jTextCoverW));
		panel.add(jTextCoverW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		jTextCoverH = new JTextField("800");
		jTextCoverH.setHorizontalAlignment(JTextField.RIGHT);
		jTextCoverH.setInputVerifier(new IntegerInputVerifier(800, 64, 4096));
		jTextCoverH.setMaximumSize(text4);
		jTextCoverH.setPreferredSize(text4);
		jTextCoverH.addFocusListener(new TextSelectFocusListener(jTextCoverH));
		panel.add(jTextCoverH);
		label = new JLabel("px");
		label.setBorder(padding2H);
		panel.add(label);
		
		
		////////////////////////////////
		//画像倍率
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画像表示倍率"));
		tabPanel.add(panel);
		jCheckImageScale = new JCheckBox("有効 ", true);
		jCheckImageScale.setToolTipText("画面の解像度に合わせて画像の幅を％指定します。画像キャプションがはみ出る場合も指定してください");
		jCheckImageScale.setFocusPainted(false);
		jCheckImageScale.setBorder(padding2);
		jCheckImageScale.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) { jTextImageScale.setEditable(jCheckImageScale.isSelected()); }
		});
		panel.add(jCheckImageScale);
		jTextImageScale = new JTextField("1.0");
		jTextImageScale.setToolTipText("指定倍率で画像を拡大表示します。64px以下の画像は変更されません");
		jTextImageScale.setHorizontalAlignment(JTextField.RIGHT);
		jTextImageScale.setInputVerifier(new FloatInputVerifier(1, 0.01f, 30));
		jTextImageScale.setMaximumSize(text4);
		jTextImageScale.setPreferredSize(text4);
		jTextImageScale.addFocusListener(new TextSelectFocusListener(jTextImageScale));
		panel.add(jTextImageScale);
		label = new JLabel("倍");
		panel.add(label);
		
		////////////////////////////////
		//画像回り込み
		////////////////////////////////
		//画像回り込み
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画像回り込み (※単ページ化より優先)"));
		tabPanel.add(panel);
		
		jCheckImageFloat = new JCheckBox("回り込み有効 ");
		jCheckImageFloat.setToolTipText("画像の実サイズが指定サイズ以下の画像を回り込み設定します");
		jCheckImageFloat.setFocusPainted(false);
		jCheckImageFloat.setBorder(padding2);
		jCheckImageFloat.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) { jTextImageFloatW.setEditable(jCheckImageFloat.isSelected()); jTextImageFloatH.setEditable(jCheckImageFloat.isSelected());}
		});
		panel.add(jCheckImageFloat);
		label = new JLabel("横");
		panel.add(label);
		jTextImageFloatW = new JTextField("600");
		jTextImageFloatW.setHorizontalAlignment(JTextField.RIGHT);
		jTextImageFloatW.setInputVerifier(new IntegerInputVerifier(600, 1, 9999));
		jTextImageFloatW.setMaximumSize(text4);
		jTextImageFloatW.setPreferredSize(text4);
		jTextImageFloatW.addFocusListener(new TextSelectFocusListener(jTextImageFloatW));
		jTextImageFloatW.setEditable(jCheckImageFloat.isSelected());
		panel.add(jTextImageFloatW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		jTextImageFloatH = new JTextField("400");
		jTextImageFloatH.setHorizontalAlignment(JTextField.RIGHT);
		jTextImageFloatH.setInputVerifier(new IntegerInputVerifier(400, 1, 9999));
		jTextImageFloatH.setMaximumSize(text4);
		jTextImageFloatH.setPreferredSize(text4);
		jTextImageFloatH.addFocusListener(new TextSelectFocusListener(jTextImageFloatH));
		jTextImageFloatH.setEditable(jCheckImageFloat.isSelected());
		panel.add(jTextImageFloatH);
		label = new JLabel("px以下");
		label.setBorder(padding2H);
		panel.add(label);
		
		label = new JLabel(" 配置");
		panel.add(label);
		jComboImageFloatType = new JComboBox(new String[]{"上/左","下/右"});
		jComboImageFloatType.setFocusable(false);
		jComboImageFloatType.setBorder(padding0);
		jComboImageFloatType.setPreferredSize(new Dimension(text4.width+24, 20));
		panel.add(jComboImageFloatType);
		
		////////////////////////////////
		//画像単ページ化
		////////////////////////////////
		panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("画像単ページ化"));
		tabPanel.add(panelV);
		//上段
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setMaximumSize(panelVMaxSize);
		panel.setBorder(padding4B);
		panelV.add(panel);
		
		//横x縦
		label = new JLabel("横");
		panel.add(label);
		jTextSinglePageSizeW = new JTextField("400");
		jTextSinglePageSizeW.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageSizeW.setInputVerifier(new IntegerInputVerifier(400, 1, 9999));
		jTextSinglePageSizeW.setMaximumSize(text4);
		jTextSinglePageSizeW.setPreferredSize(text4);
		jTextSinglePageSizeW.addFocusListener(new TextSelectFocusListener(jTextSinglePageSizeW));
		panel.add(jTextSinglePageSizeW);
		label = new JLabel("x");
		label.setBorder(padding2H);
		panel.add(label);
		label = new JLabel("縦");
		panel.add(label);
		jTextSinglePageSizeH = new JTextField("600");
		jTextSinglePageSizeH.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageSizeH.setInputVerifier(new IntegerInputVerifier(600, 1, 9999));
		jTextSinglePageSizeH.setMaximumSize(text4);
		jTextSinglePageSizeH.setPreferredSize(text4);
		jTextSinglePageSizeH.addFocusListener(new TextSelectFocusListener(jTextSinglePageSizeH));
		panel.add(jTextSinglePageSizeH);
		label = new JLabel("px以上 ");
		label.setBorder(padding2H);
		panel.add(label);
		//横のみ
		label = new JLabel("横のみ");
		label.setBorder(padding2H);
		panel.add(label);
		jTextSinglePageWidth = new JTextField("600");
		jTextSinglePageWidth.setHorizontalAlignment(JTextField.RIGHT);
		jTextSinglePageWidth.setInputVerifier(new IntegerInputVerifier(600, 1, 9999));
		jTextSinglePageWidth.setMaximumSize(text4);
		jTextSinglePageWidth.setPreferredSize(text4);
		jTextSinglePageWidth.addFocusListener(new TextSelectFocusListener(jTextSinglePageWidth));
		panel.add(jTextSinglePageWidth);
		label = new JLabel("px以上");
		label.setBorder(padding2H);
		panel.add(label);
		
		//下段
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelV.add(panel);
		
		label = new JLabel("縮小表示 (");
		label.setBorder(padding2H);
		panel.add(label);
		
		buttonGroup = new ButtonGroup();
		jRadioImageSizeType1 = new JRadioButton("指定無し");
		jRadioImageSizeType1.setToolTipText("画像のサイズを指定しません。 端末が自動で縮小します(Kindle, Kobo)");
		jRadioImageSizeType1.setFocusPainted(false);
		jRadioImageSizeType1.setBorder(padding2);
		jRadioImageSizeType1.setIconTextGap(1);
		panel.add(jRadioImageSizeType1);
		buttonGroup.add(jRadioImageSizeType1);
		/*jRadioImageSizeType2 = new JRadioButton("高さ%", true);
		jRadioImageSizeType2.setToolTipText("画面の縦横比に合せて画像の高さのみ%指定します。画面設定より縦長の端末でははみ出すか縦長に表示されます");
		jRadioImageSizeType2.setFocusPainted(false);
		jRadioImageSizeType2.setBorder(padding2);
		jRadioImageSizeType2.setIconTextGap(1);
		panel.add(jRadioImageSizeType2);
		buttonGroup.add(jRadioImageSizeType2);*/
		jRadioImageSizeType3 = new JRadioButton("縦横比");
		jRadioImageSizeType3.setToolTipText("画面の縦横比に合せて幅または高さを100%指定します。画面回転で画像がはみ出す場合があります");
		jRadioImageSizeType3.setFocusPainted(false);
		jRadioImageSizeType3.setBorder(padding2);
		jRadioImageSizeType3.setIconTextGap(1);
		panel.add(jRadioImageSizeType3);
		buttonGroup.add(jRadioImageSizeType3);
		
		label = new JLabel(")  ");
		label.setBorder(padding2H);
		panel.add(label);
		
		jCheckFitImage = new JCheckBox("拡大表示", true);
		jCheckFitImage.setToolTipText("画面サイズより小さい画像を幅高さに合わせて拡大表示します。画面回転で画像がはみ出す場合があります");
		jCheckFitImage.setFocusPainted(false);
		jCheckFitImage.setBorder(padding2);
		panel.add(jCheckFitImage);
		buttonGroup = new ButtonGroup();
		
		//Float表示
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("Float指定 (Readerのみ)"));
		tabPanel.add(panel);
		jCheckImageFloatPage = new JCheckBox("単ページ画像");
		jCheckImageFloatPage.setToolTipText("単ページ対象の画像をfloat表示します。 xhtmlは分割されません");
		jCheckImageFloatPage.setFocusPainted(false);
		jCheckImageFloatPage.setBorder(padding2);
		panel.add(jCheckImageFloatPage);
		
		jCheckImageFloatBlock = new JCheckBox("通常画像");
		jCheckImageFloatBlock.setToolTipText("回り込み、単ページ以外の画像をfloat表示します。 64px以上の画像のみ");
		jCheckImageFloatBlock.setFocusPainted(false);
		jCheckImageFloatBlock.setBorder(padding2);
		panel.add(jCheckImageFloatBlock);
		
		////////////////////////////////////////////////////////////////
		//Tab 画像2
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.addTab("画像2", imageIcon, tabPanel);
		
		////////////////////////////////
		//全画面＋SVG
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("全画面表示"));
		tabPanel.add(panel);
		jCheckSvgImage = new JCheckBox("SVGタグ出力（画像zipのみ） ");
		jCheckSvgImage.setFocusPainted(false);
		jCheckSvgImage.setToolTipText("画像のみのzipの場合、固定レイアウト＋SVGタグで出力します");
		jCheckSvgImage.setBorder(padding2);
		panel.add(jCheckSvgImage);
		
		////////////////////////////////
		//画像調整
		////////////////////////////////
		//Jpeg圧縮率
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("Jpeg圧縮率"));
		tabPanel.add(panel);
		jTextJpegQuality = new JTextField("85");
		jTextJpegQuality.setToolTipText("表紙編集、縮小、回転、余白除去時のJpeg保存時の画質(100が最高画質)");
		jTextJpegQuality.setHorizontalAlignment(JTextField.RIGHT);
		jTextJpegQuality.setInputVerifier(new IntegerInputVerifier(85, 30, 100));
		jTextJpegQuality.setMaximumSize(text3);
		jTextJpegQuality.setPreferredSize(text3);
		jTextJpegQuality.addFocusListener(new TextSelectFocusListener(jTextJpegQuality));
		panel.add(jTextJpegQuality);
		panel.add(new JLabel(" (30～100)"));
		
		////////////////////////////////
		//ガンマ補正
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("色調整"));
		tabPanel.add(panel);
		jCheckGamma = new JCheckBox("ガンマ補正");
		jCheckGamma.setToolTipText("画像の濃さを変更します (濃:0.2～1.8:淡)");
		jCheckGamma.setFocusPainted(false);
		jCheckGamma.setBorder(padding2);
		jCheckGamma.setIconTextGap(2);
		jCheckGamma.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){
			jTextGammaValue.setEditable(jCheckGamma.isSelected());
		}});
		panel.add(jCheckGamma);
		jTextGammaValue = new JTextField("1.0");
		jTextGammaValue.setToolTipText(jCheckGamma.getToolTipText());
		jTextGammaValue.setHorizontalAlignment(JTextField.RIGHT);
		jTextGammaValue.setInputVerifier(new FloatInputVerifier(1.0f, 0.2f, 1.8f));
		jTextGammaValue.setMaximumSize(text3);
		jTextGammaValue.setPreferredSize(text3);
		jTextGammaValue.setEditable(jCheckGamma.isSelected());
		jTextGammaValue.addFocusListener(new TextSelectFocusListener(jTextGammaValue));
		panel.add(jTextGammaValue);
		
		////////////////////////////////
		//画像縮小
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("画像縮小回転"));
		tabPanel.add(panel);
		ChangeListener resizeChangeLister = new ChangeListener() {
			public void stateChanged(ChangeEvent e) { setResizeTextEditable(true);  }
		};
		//横
		jCheckResizeW = new JCheckBox("横");
		jCheckResizeW.setFocusPainted(false);
		jCheckResizeW.setBorder(padding2);
		jCheckResizeW.setIconTextGap(2);
		jCheckResizeW.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeW);
		jTextResizeNumW = new JTextField("2048");
		jTextResizeNumW.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeNumW.setInputVerifier(new IntegerInputVerifier(2048, 100, 9999));
		jTextResizeNumW.setMaximumSize(text4);
		jTextResizeNumW.setPreferredSize(text4);
		jTextResizeNumW.addFocusListener(new TextSelectFocusListener(jTextResizeNumW));
		jTextResizeNumW.setEditable(jCheckResizeW.isSelected());
		panel.add(jTextResizeNumW);
		label = new JLabel("px以下 ");
		label.setBorder(padding2H);
		panel.add(label);
		//縦
		jCheckResizeH = new JCheckBox("縦");
		jCheckResizeH.setFocusPainted(false);
		jCheckResizeH.setBorder(padding2);
		jCheckResizeH.setIconTextGap(2);
		jCheckResizeH.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeH);
		jTextResizeNumH = new JTextField("2048");
		jTextResizeNumH.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeNumH.setInputVerifier(new IntegerInputVerifier(2048, 100, 9999));
		jTextResizeNumH.setMaximumSize(text4);
		jTextResizeNumH.setPreferredSize(text4);
		jTextResizeNumH.addFocusListener(new TextSelectFocusListener(jTextResizeNumH));
		panel.add(jTextResizeNumH);
		label = new JLabel("px以下");
		label.setBorder(padding2H);
		panel.add(label);
		this.setResizeTextEditable(true);
		
		//自動画像回転
		label = new JLabel(" 自動回転");
		label.setBorder(padding2H);
		panel.add(label);
		jComboRotateImage = new JComboBox(new String[]{"なし","右","左"});
		jComboRotateImage.setToolTipText("単ページ時画面の縦横比に合わせて画像を回転します");
		jComboRotateImage.setFocusable(false);
		jComboRotateImage.setBorder(padding0);
		jComboRotateImage.setPreferredSize(new Dimension(text4.width+24, 20));
		panel.add(jComboRotateImage);
		
		////////////////////////////////
		//余白除去
		////////////////////////////////
		panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("余白除去"));
		tabPanel.add(panelV);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(padding4B);
		panelV.add(panel);
		jCheckAutoMargin = new JCheckBox("有効 ");
		jCheckAutoMargin.setFocusPainted(false);
		jCheckAutoMargin.setBorder(padding2);
		jCheckAutoMargin.setIconTextGap(2);
		jCheckAutoMargin.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){
			boolean selected = jCheckAutoMargin.isSelected();
			jTextAutoMarginLimitH.setEditable(selected);
			jTextAutoMarginLimitV.setEditable(selected);
			jTextAutoMarginWhiteLevel.setEditable(selected);
			jTextAutoMarginPadding.setEditable(selected);
			jTextAutoMarginNombreSize.setEditable(selected);
		}});
		panel.add(jCheckAutoMargin);
		label = new JLabel(" 横");
		label.setToolTipText("横方向の余白除去量の制限 左右の余白の合計");
		panel.add(label);
		jTextAutoMarginLimitH = new JTextField("15");
		jTextAutoMarginLimitH.setToolTipText(label.getToolTipText());
		jTextAutoMarginLimitH.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginLimitH.setInputVerifier(new IntegerInputVerifier(15, 0, 50));
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
		jTextAutoMarginLimitV = new JTextField("15");
		jTextAutoMarginLimitV.setToolTipText(label.getToolTipText());
		jTextAutoMarginLimitV.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginLimitV.setInputVerifier(new IntegerInputVerifier(15, 0, 50));
		jTextAutoMarginLimitV.setMaximumSize(text3);
		jTextAutoMarginLimitV.setPreferredSize(text3);
		jTextAutoMarginLimitV.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginLimitV.addFocusListener(new TextSelectFocusListener(jTextAutoMarginLimitV));
		panel.add(jTextAutoMarginLimitV);
		label = new JLabel("%");
		label.setBorder(padding2H);
		panel.add(label);
		panel.add(label);
		label = new JLabel("  余白追加");
		label.setToolTipText("余白除去後に追加する余白の量(追加部分の画像はそのまま)");
		panel.add(label);
		jTextAutoMarginPadding = new JTextField("1.0");
		jTextAutoMarginPadding.setToolTipText(label.getToolTipText());
		jTextAutoMarginPadding.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginPadding.setInputVerifier(new FloatInputVerifier(1.0f, 0, 50));
		jTextAutoMarginPadding.setMaximumSize(text3);
		jTextAutoMarginPadding.setPreferredSize(text3);
		jTextAutoMarginPadding.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginPadding.addFocusListener(new TextSelectFocusListener(jTextAutoMarginPadding));
		panel.add(jTextAutoMarginPadding);
		label = new JLabel("%");
		label.setBorder(padding2H);
		panel.add(label);
		
		label = new JLabel("  白レベル");
		label.setToolTipText("余白部分の白い画素と判別するレベルを指定します (黒:0～白:100)");
		panel.add(label);
		jTextAutoMarginWhiteLevel = new JTextField("80");
		jTextAutoMarginWhiteLevel.setToolTipText(label.getToolTipText());
		jTextAutoMarginWhiteLevel.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginWhiteLevel.setInputVerifier(new IntegerInputVerifier(80, 0, 100));
		jTextAutoMarginWhiteLevel.setMaximumSize(text3);
		jTextAutoMarginWhiteLevel.setPreferredSize(text3);
		jTextAutoMarginWhiteLevel.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginWhiteLevel.addFocusListener(new TextSelectFocusListener(jTextAutoMarginWhiteLevel));
		panel.add(jTextAutoMarginWhiteLevel);
		label = new JLabel("%");
		label.setBorder(padding2H);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelV.add(panel);
		label = new JLabel("ノンブル除去 (位置");
		label.setBorder(padding2H);
		panel.add(label);
		jComboAutoMarginNombre = new JComboBox(new String[]{"なし","上","下","上下"});
		jComboAutoMarginNombre.setToolTipText("ノンブルを除去します。除去した場合は除去制限が5%追加されます");
		jComboAutoMarginNombre.setFocusable(false);
		jComboAutoMarginNombre.setMaximumSize(new Dimension(text3.width+24, 20));
		jComboAutoMarginNombre.setPreferredSize(new Dimension(text3.width+24, 20));
		panel.add(jComboAutoMarginNombre);
		label = new JLabel(" 高さ");
		label.setBorder(padding2H);
		panel.add(label);
		jTextAutoMarginNombreSize = new JTextField("3.0");
		jTextAutoMarginNombreSize.setToolTipText("ノンブルの文字部分の高さを指定します。これより大きい場合はノンブル除去されません");
		jTextAutoMarginNombreSize.setHorizontalAlignment(JTextField.RIGHT);
		jTextAutoMarginNombreSize.setInputVerifier(new FloatInputVerifier(3.0f, 0.5f, 10));
		jTextAutoMarginNombreSize.setMaximumSize(text3);
		jTextAutoMarginNombreSize.setPreferredSize(text3);
		jTextAutoMarginNombreSize.setEditable(jCheckAutoMargin.isSelected());
		jTextAutoMarginNombreSize.addFocusListener(new TextSelectFocusListener(jTextAutoMarginPadding));
		panel.add(jTextAutoMarginNombreSize);
		label = new JLabel("% )");
		label.setBorder(padding2H);
		panel.add(label);
		
		////////////////////////////////////////////////////////////////
		//Tab 詳細設定
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.addTab("詳細設定", pageSettingIcon, tabPanel);
		
		////////////////////////////////
		//文中全角スペースの処理
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("文中全角スペースの処理"));
		tabPanel.add(panel);
		//ピクセル
		label = new JLabel("行末で非表示(");
		label.setBorder(padding2);
		panel.add(label);
		buttonGroup = new ButtonGroup();
		jRadioSpaceHyp1 = new JRadioButton("Kobo・Kindle ");
		jRadioSpaceHyp1.setToolTipText("Kobo・Kindleで行末で非表示にします Readerではそのままと同じ表示になります");
		jRadioSpaceHyp1.setFocusPainted(false);
		jRadioSpaceHyp1.setIconTextGap(2);
		jRadioSpaceHyp1.setBorder(padding2);
		panel.add(jRadioSpaceHyp1);
		buttonGroup.add(jRadioSpaceHyp1);
		jRadioSpaceHyp2 = new JRadioButton("Reader ) ");
		jRadioSpaceHyp2.setToolTipText("Reader以外では次行に追い出しの禁則処理になります");
		jRadioSpaceHyp2.setFocusPainted(false);
		jRadioSpaceHyp2.setIconTextGap(2);
		jRadioSpaceHyp2.setBorder(padding2);
		panel.add(jRadioSpaceHyp2);
		buttonGroup.add(jRadioSpaceHyp2);
		jRadioSpaceHyp0 = new JRadioButton("そのまま", true);
		jRadioSpaceHyp0.setToolTipText("行の折り返し部分にある全角スペースが行頭に表示されます");
		jRadioSpaceHyp0.setFocusPainted(false);
		jRadioSpaceHyp0.setIconTextGap(2);
		jRadioSpaceHyp0.setBorder(padding2);
		panel.add(jRadioSpaceHyp0);
		buttonGroup.add(jRadioSpaceHyp0);
		
		////////////////////////////////
		//「○○」に「××」の注記
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("「○○」に「××」の注記"));
		tabPanel.add(panel);
		buttonGroup = new ButtonGroup();
		jRadioChukiRuby0 = new JRadioButton("非表示", true);
		jRadioChukiRuby0.setFocusPainted(false);
		jRadioChukiRuby0.setIconTextGap(2);
		jRadioChukiRuby0.setBorder(padding2);
		panel.add(jRadioChukiRuby0);
		buttonGroup.add(jRadioChukiRuby0);
		jRadioChukiRuby1 = new JRadioButton("ルビ");
		jRadioChukiRuby1.setToolTipText("○○のルビとして××を表示します(「ママ」の注記は非表示)");
		jRadioChukiRuby1.setFocusPainted(false);
		jRadioChukiRuby1.setIconTextGap(2);
		jRadioChukiRuby1.setBorder(padding2);
		panel.add(jRadioChukiRuby1);
		buttonGroup.add(jRadioChukiRuby1);
		jRadioChukiRuby2 = new JRadioButton("小書き");
		jRadioChukiRuby2.setToolTipText("○○の後ろに××を小書きで表示します(「ママ」の注記は非表示)");
		jRadioChukiRuby2.setFocusPainted(false);
		jRadioChukiRuby2.setIconTextGap(2);
		jRadioChukiRuby2.setBorder(padding2);
		panel.add(jRadioChukiRuby2);
		buttonGroup.add(jRadioChukiRuby2);
		
		////////////////////////////////
		//自動縦中横
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("自動縦中横"));
		tabPanel.add(panel);
		//半角2文字縦書き
		jCheckAutoYoko = new JCheckBox("有効 ", true);
		jCheckAutoYoko.setFocusPainted(false);
		jCheckAutoYoko.setToolTipText("半角の2文字の数字、2～3文字の!?を縦中横で表示します。(前後に半角が無い場合)");
		jCheckAutoYoko.setBorder(padding2);
		panel.add(jCheckAutoYoko);
		label = new JLabel("+数字(");
		label.setBorder(padding0);
		panel.add(label);
		//半角数字1文字縦書き
		jCheckAutoYokoNum1 = new JCheckBox("1桁 ");
		jCheckAutoYokoNum1.setFocusPainted(false);
		jCheckAutoYokoNum1.setIconTextGap(1);
		jCheckAutoYokoNum1.setBorder(padding2);
		panel.add(jCheckAutoYokoNum1);
		//半角数字3文字縦書き
		jCheckAutoYokoNum3 = new JCheckBox("3桁");
		jCheckAutoYokoNum3.setFocusPainted(false);
		jCheckAutoYokoNum3.setIconTextGap(1);
		jCheckAutoYokoNum3.setBorder(padding2);
		panel.add(jCheckAutoYokoNum3);
		label = new JLabel(") +");
		label.setBorder(padding0);
		panel.add(label);
		//!? 1文字
		jCheckAutoEQ1 = new JCheckBox("!? 1文字");
		jCheckAutoEQ1.setFocusPainted(false);
		jCheckAutoEQ1.setIconTextGap(3);
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
		jCheckCommentPrint = new JCheckBox("コメント出力 ");
		jCheckCommentPrint.setToolTipText("コメント行の間を出力します");
		jCheckCommentPrint.setFocusPainted(false);
		jCheckCommentPrint.setBorder(padding2);
		panel.add(jCheckCommentPrint);
		//半角2文字縦書き
		jCheckCommentConvert = new JCheckBox("コメント内注記変換");
		jCheckCommentConvert.setToolTipText("コメント内の注記を変換します");
		jCheckCommentConvert.setFocusPainted(false);
		jCheckCommentConvert.setBorder(padding2);
		panel.add(jCheckCommentConvert);
		
		////////////////////////////////
		//栞用ID出力
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("栞用ID"));
		tabPanel.add(panel);
		jCheckMarkId = new JCheckBox("各行に出力");
		jCheckMarkId.setToolTipText("Kobo向けの栞を記憶するためのIDを各行に設定します");
		jCheckMarkId.setFocusPainted(false);
		jCheckMarkId.setBorder(padding2);
		panel.add(jCheckMarkId);
		
		////////////////////////////////
		//4バイト文字変換
		////////////////////////////////
		/*panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("4バイト文字"));
		tabPanel.add(panel);
		//4バイト文字を変換する
		jCheckGaiji32 = new JCheckBox("出力する");
		jCheckGaiji32.setToolTipText("u+20000以降の4バイト拡張漢字をそのまま出力します。Kobo等で表示できない場合はチェックを外します");
		jCheckGaiji32.setFocusPainted(false);
		jCheckGaiji32.setBorder(padding2);
		panel.add(jCheckGaiji32);
		*/
		
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
		panel.add(jComboxRemoveEmptyLine);
		label = new JLabel("行減らす");
		label.setBorder(padding2);
		panel.add(label);
		
		label = new JLabel(" 最大");
		label.setBorder(padding2);
		panel.add(label);
		jComboxMaxEmptyLine = new JComboBox(new String[]{"-", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
		jComboxMaxEmptyLine.setToolTipText("空行の連続を指定行数以下に制限します");
		jComboxMaxEmptyLine.setFocusable(false);
		jComboxMaxEmptyLine.setBorder(padding0);
		jComboxMaxEmptyLine.setMaximumSize(text5);
		jComboxMaxEmptyLine.setPreferredSize(text5);
		panel.add(jComboxMaxEmptyLine);
		label = new JLabel("行");
		label.setBorder(padding2);
		panel.add(label);
		
		////////////////////////////////
		//行頭字下げ追加
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("行頭字下げ"));
		tabPanel.add(panel);
		jCheckForceIndent = new JCheckBox("有効     ");
		jCheckForceIndent.setToolTipText("行頭が「『―”（〈〔【と全角空白以外なら行頭に全角空白を追加します 半角空白のみは全角に置き換えます");
		jCheckForceIndent.setFocusPainted(false);
		jCheckForceIndent.setBorder(padding2);
		panel.add(jCheckForceIndent);
		
		////////////////////////////////
		//強制改ページ
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("強制改ページ"));
		tabPanel.add(panel);
		
		jCheckPageBreak = new JCheckBox("有効", true);
		jCheckPageBreak.setToolTipText("指定サイズを超えた時点で強制改ページ(ブロック注記の外側のみ)");
		jCheckPageBreak.setFocusPainted(false);
		jCheckPageBreak.setBorder(padding2);
		jCheckPageBreak.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){
			boolean selected = jCheckPageBreak.isSelected();
			jTextPageBreakSize.setEditable(selected);
			jTextPageBreakEmptySize.setEditable(selected);
			jTextPageBreakChapterSize.setEditable(selected);
		}});
		panel.add(jCheckPageBreak);
		
		jTextPageBreakSize = new JTextField("400");
		jTextPageBreakSize.setMaximumSize(text4);
		jTextPageBreakSize.setPreferredSize(text4);
		jTextPageBreakSize.setInputVerifier(new IntegerInputVerifier(400, 1, 9999));
		jTextPageBreakSize.setEditable(jCheckPageBreak.isSelected());
		jTextPageBreakSize.addFocusListener(new TextSelectFocusListener(jTextPageBreakSize));
		panel.add(jTextPageBreakSize);
		label = new JLabel("KB ");
		label.setBorder(padding2);
		panel.add(label);
		
		jCheckPageBreakEmpty = new JCheckBox("空行(");
		jCheckPageBreakEmpty.setFocusPainted(false);
		jCheckPageBreakEmpty.setBorder(padding2);
		panel.add(jCheckPageBreakEmpty);
		jComboxPageBreakEmptyLine = new JComboBox(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"});
		jComboxPageBreakEmptyLine.setSelectedIndex(1);
		jComboxPageBreakEmptyLine.setFocusable(false);
		jComboxPageBreakEmptyLine.setBorder(padding0);
		jComboxPageBreakEmptyLine.setMaximumSize(text5);
		jComboxPageBreakEmptyLine.setPreferredSize(text5);
		((JLabel)jComboxPageBreakEmptyLine.getRenderer()).setBorder(padding2);
		panel.add(jComboxPageBreakEmptyLine);
		label = new JLabel("行以上 ");
		label.setBorder(padding2);
		panel.add(label);
		jTextPageBreakEmptySize = new JTextField("300");
		jTextPageBreakEmptySize.setMaximumSize(text4);
		jTextPageBreakEmptySize.setPreferredSize(text4);
		jTextPageBreakEmptySize.setInputVerifier(new IntegerInputVerifier(300, 1, 9999));
		jTextPageBreakEmptySize.setEditable(jCheckPageBreak.isSelected());
		jTextPageBreakEmptySize.addFocusListener(new TextSelectFocusListener(jTextPageBreakEmptySize));
		panel.add(jTextPageBreakEmptySize);
		label = new JLabel("KB) ");
		label.setBorder(padding2);
		panel.add(label);
		
		jCheckPageBreakChapter = new JCheckBox("見出し前(");
		jCheckPageBreakChapter.setFocusPainted(false);
		jCheckPageBreakChapter.setBorder(padding2);
		panel.add(jCheckPageBreakChapter);
		jTextPageBreakChapterSize = new JTextField("200");
		jTextPageBreakChapterSize.setMaximumSize(text4);
		jTextPageBreakChapterSize.setPreferredSize(text4);
		jTextPageBreakChapterSize.setInputVerifier(new IntegerInputVerifier(200, 1, 9999));
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
		jTabbedPane.addTab("目次", tocIcon, tabPanel);
		
		////////////////////////////////
		//目次設定
		////////////////////////////////
		//目次出力
		//目次抽出
		panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("目次設定"));
		tabPanel.add(panelV);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		//最大文字数
		label = new JLabel(" 最大文字数");
		label.setBorder(padding2);
		panel.add(label);
		jTextMaxChapterNameLength = new JTextField("64");
		jTextMaxChapterNameLength.setHorizontalAlignment(JTextField.RIGHT);
		jTextMaxChapterNameLength.setInputVerifier(new IntegerInputVerifier(64, 1, 999));
		jTextMaxChapterNameLength.setMaximumSize(text3);
		jTextMaxChapterNameLength.setPreferredSize(text3);
		jTextMaxChapterNameLength.addFocusListener(new TextSelectFocusListener(jTextMaxChapterNameLength));
		panel.add(jTextMaxChapterNameLength);
		
		label = new JLabel("  ");
		label.setBorder(padding2);
		panel.add(label);
		
		//表紙
		jCheckCoverPageToc = new JCheckBox("表紙 ");
		jCheckCoverPageToc.setToolTipText("表紙画像のページを目次に追加します");
		jCheckCoverPageToc.setFocusPainted(false);
		jCheckCoverPageToc.setBorder(padding2);
		panel.add(jCheckCoverPageToc);
		
		//表紙
		jCheckTitleToc = new JCheckBox("表題 ", true);
		jCheckTitleToc.setToolTipText("表題の行を目次に追加します");
		jCheckTitleToc.setFocusPainted(false);
		jCheckTitleToc.setBorder(padding2);
		panel.add(jCheckTitleToc);
		
		jCheckChapterUseNextLine = new JCheckBox("次の行を繋げる ");
		jCheckChapterUseNextLine.setToolTipText("次の行が空行でなければ見出しの後ろに繋げます");
		jCheckChapterUseNextLine.setFocusPainted(false);
		jCheckChapterUseNextLine.setBorder(padding2);
		panel.add(jCheckChapterUseNextLine);
		
		jCheckChapterExclude = new JCheckBox("連続する見出しを除外", true);
		jCheckChapterExclude.setToolTipText("3つ以上連続する自動抽出された見出しを除外します(空行1行間隔も連続扱い)");
		jCheckChapterExclude.setFocusPainted(false);
		jCheckChapterExclude.setBorder(padding2);
		panel.add(jCheckChapterExclude);
		
		panelV.add(panel);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		
		//nav階層化
		jCheckNavNest = new JCheckBox("目次ページ階層化 ");
		jCheckNavNest.setToolTipText("目次ページ(nav.xhtml)を階層化します");
		jCheckNavNest.setFocusPainted(false);
		jCheckNavNest.setBorder(padding2);
		panel.add(jCheckNavNest);
		
		//表紙
		jCheckNcxNest = new JCheckBox("目次(ncx)階層化 ");
		jCheckNcxNest.setToolTipText("目次(toc.ncx)を階層化します");
		jCheckNcxNest.setFocusPainted(false);
		jCheckNcxNest.setBorder(padding2);
		panel.add(jCheckNcxNest);
		
		panelV.add(panel);
		
		//目次抽出
		panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.Y_AXIS));
		panelV.setBorder(new NarrowTitledBorder("目次抽出"));
		tabPanel.add(panelV);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setMaximumSize(panelVMaxSize);
		panel.setBorder(padding3B);
		panelV.add(panel);
		//見出し注記
		label = new JLabel("注記(");
		label.setBorder(padding2);
		panel.add(label);
		jCheckChapterH = new JCheckBox("見出し ", true);
		jCheckChapterH.setFocusPainted(false);
		jCheckChapterH.setBorder(padding2);
		panel.add(jCheckChapterH);
		jCheckChapterH1 = new JCheckBox("大見出し ", true);
		jCheckChapterH1.setFocusPainted(false);
		jCheckChapterH1.setBorder(padding2);
		panel.add(jCheckChapterH1);
		jCheckChapterH2 = new JCheckBox("中見出し ", true);
		jCheckChapterH2.setFocusPainted(false);
		jCheckChapterH2.setBorder(padding2);
		panel.add(jCheckChapterH2);
		jCheckChapterH3 = new JCheckBox("小見出し ) ", true);
		jCheckChapterH3.setFocusPainted(false);
		jCheckChapterH3.setBorder(padding2);
		panel.add(jCheckChapterH3);
		
		jCheckSameLineChapter = new JCheckBox("同行見出し含む", false);
		jCheckSameLineChapter.setFocusPainted(false);
		jCheckSameLineChapter.setBorder(padding2);
		panel.add(jCheckSameLineChapter);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setMaximumSize(panelVMaxSize);
		panel.setBorder(padding3B);
		panelV.add(panel);
		//改ページ後を目次に追加
		jCheckChapterSection = new JCheckBox("改ページ後 ", true);
		jCheckChapterSection.setToolTipText("改ページ後の先頭行の文字を目次に出力します");
		jCheckChapterSection.setFocusPainted(false);
		jCheckChapterSection.setBorder(padding2);
		panel.add(jCheckChapterSection);
		jCheckChapterName = new JCheckBox("章見出し (第～章/その～/～章/序/プロローグ 等)", true);
		jCheckChapterName.setToolTipText("第～話/第～章/第～篇/第～部/第～節/第～幕/第～編/その～/～章/プロローグ/エピローグ/モノローグ/序/序章/終章/転章/間章/幕間");
		jCheckChapterName.setFocusPainted(false);
		jCheckChapterName.setBorder(padding2);
		panel.add(jCheckChapterName);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setMaximumSize(panelVMaxSize);
		panel.setBorder(padding3B);
		panelV.add(panel);
		jCheckChapterNumOnly = new JCheckBox("数字のみ");
		jCheckChapterNumOnly.setFocusPainted(false);
		jCheckChapterNumOnly.setBorder(padding2);
		panel.add(jCheckChapterNumOnly);
		jCheckChapterNumTitle = new JCheckBox("数字+見出し  ");
		jCheckChapterNumTitle.setFocusPainted(false);
		jCheckChapterNumTitle.setBorder(padding2);
		panel.add(jCheckChapterNumTitle);
		jCheckChapterNumParen = new JCheckBox("括弧内数字のみ");
		jCheckChapterNumParen.setToolTipText("（）〈〉〔〕【】内の数字"); 
		jCheckChapterNumParen.setFocusPainted(false);
		jCheckChapterNumParen.setBorder(padding2);
		panel.add(jCheckChapterNumParen);
		jCheckChapterNumParenTitle = new JCheckBox("括弧内数字+見出し");
		jCheckChapterNumParenTitle.setFocusPainted(false);
		jCheckChapterNumParenTitle.setBorder(padding2);
		panel.add(jCheckChapterNumParenTitle);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		panel.setMaximumSize(panelVMaxSize);
		panel.setBorder(padding0);
		panelV.add(panel);
		jCheckChapterPattern = new JCheckBox("その他パターン");
		jCheckChapterPattern.setToolTipText("目次抽出パターンを正規表現で指定します。前後の空白とタグを除いた文字列と比較します。");
		jCheckChapterPattern.setFocusPainted(false);
		jCheckChapterPattern.setBorder(padding2);
		jCheckChapterPattern.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){
			jComboChapterPattern.setEditable(jCheckChapterPattern.isSelected()); jComboChapterPattern.repaint();
		}});
		panel.add(jCheckChapterPattern);
		jComboChapterPattern = new JComboBox(new String[]{
				"^(見出し１|見出し２|見出し３)$",
				"^(†|【|●|▼|■)",
				"^(0-9|０-９|一|二|三|四|五|六|七|八|九|十|〇)",
				"^[1|2|１|２]?[0-9|０-９]月[1-3|１-３]?[0-9|０-９]日",
				"^(一|十)?(一|二|三|四|五|六|七|八|九|十|〇)月(一|十|二十?|三十?)?(一|二|三|四|五|六|七|八|九|十|〇)日"});
		jComboChapterPattern.setBorder(padding0);
		jComboChapterPattern.setMaximumSize(text300);
		jComboChapterPattern.setPreferredSize(text300);
		jComboChapterPattern.setEditable(jCheckChapterPattern.isSelected());
		panel.add(jComboChapterPattern);
		
		
		////////////////////////////////////////////////////////////////
		//Tab スタイル
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.addTab("スタイル", styleIcon, tabPanel);
		
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("行の高さ"));
		tabPanel.add(panel);
		jComboLineHeight = new JComboBox(new String[]{"1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "2.0"});
		jComboLineHeight.setBorder(padding0);
		jComboLineHeight.setMaximumSize(combo3);
		jComboLineHeight.setPreferredSize(combo3);
		jComboLineHeight.setEditable(true);
		jComboLineHeight.setInputVerifier(new FloatInputVerifier(1.8f, 1));
		jComboLineHeight.setSelectedItem("1.8");
		panel.add(jComboLineHeight);
		label = new JLabel("文字");
		label.setBorder(padding2);
		panel.add(label);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("文字サイズ"));
		tabPanel.add(panel);
		jComboFontSize = new JComboBox(new String[]{"75", "80", "85", "90", "95", "100", "105", "110", "115", "120", "125"});
		jComboFontSize.setToolTipText("標準フォントからの倍率を設定します"); 
		jComboFontSize.setBorder(padding0);
		jComboFontSize.setMaximumSize(combo3);
		jComboFontSize.setPreferredSize(combo3);
		jComboFontSize.setEditable(true);
		jComboFontSize.setInputVerifier(new IntegerInputVerifier(100, 10));
		jComboFontSize.setSelectedItem("100");
		panel.add(jComboFontSize);
		label = new JLabel("%");
		label.setBorder(padding2);
		panel.add(label);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("太字ゴシック表示"));
		tabPanel.add(panel);
		jCheckBoldUseGothic = new JCheckBox("太字注記", false);
		jCheckBoldUseGothic.setToolTipText("太字注記を太字ゴシックで表示します"); 
		jCheckBoldUseGothic.setFocusPainted(false);
		jCheckBoldUseGothic.setBorder(padding2);
		panel.add(jCheckBoldUseGothic);
		
		jCheckGothicUseBold = new JCheckBox("ゴシック体注記", false);
		jCheckGothicUseBold.setToolTipText("ゴシック体注記を太字ゴシックで表示します"); 
		jCheckGothicUseBold.setFocusPainted(false);
		jCheckGothicUseBold.setBorder(padding2);
		panel.add(jCheckGothicUseBold);
		
		////////////////////////////////
		//余白設定 横に並べる
		panelH = new JPanel();
		panelH.setLayout(new BoxLayout(panelH, BoxLayout.X_AXIS));
		tabPanel.add(panelH);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("テキスト余白 (@page margin)"));
		panelH.add(panel);
		String[] marginLabels = {"上","右","下","左"};
		jTextPageMargins = new JTextField[4];
		NumberVerifier numberVerifier0 = new NumberVerifier(0, 0);
		for (int i=0; i<jTextPageMargins.length; i++) {
			label = new JLabel(marginLabels[i]);
			label.setBorder(padding2);
			panel.add(label);
			JTextField jTextField = new JTextField("0.5");
			jTextPageMargins[i] = jTextField;
			jTextField.setHorizontalAlignment(JTextField.RIGHT);
			jTextField.addFocusListener(new TextSelectFocusListener(jTextField));
			jTextField.setInputVerifier(numberVerifier0);
			jTextField.setMaximumSize(text3);
			jTextField.setPreferredSize(text3);
			panel.add(jTextField);
		}
		panel.add(new JLabel("  "));
		ButtonGroup group = new ButtonGroup();
		jRadioPageMarginUnit0 = new JRadioButton("字 ", true);
		jRadioPageMarginUnit0.setBorder(padding0);
		jRadioPageMarginUnit0.setFocusPainted(false);
		panel.add(jRadioPageMarginUnit0);
		group.add(jRadioPageMarginUnit0);
		jRadioPageMarginUnit1 = new JRadioButton("%");
		jRadioPageMarginUnit1.setBorder(padding0);
		jRadioPageMarginUnit1.setFocusPainted(false);
		panel.add(jRadioPageMarginUnit1);
		group.add(jRadioPageMarginUnit1);
		
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("テキスト余白 (html margin) Reader用"));
		panelH.add(panel);
		jTextBodyMargins = new JTextField[4];
		for (int i=0; i<jTextBodyMargins.length; i++) {
			label = new JLabel(marginLabels[i]);
			label.setBorder(padding2);
			panel.add(label);
			JTextField jTextField = new JTextField("0");
			jTextBodyMargins[i] = jTextField;
			jTextField.setHorizontalAlignment(JTextField.RIGHT);
			jTextField.addFocusListener(new TextSelectFocusListener(jTextField));
			jTextField.setInputVerifier(numberVerifier0);
			jTextField.setMaximumSize(text3);
			jTextField.setPreferredSize(text3);
			panel.add(jTextField);
		}
		panel.add(new JLabel("  "));
		group = new ButtonGroup();
		jRadioBodyMarginUnit0 = new JRadioButton("字 ", true);
		jRadioBodyMarginUnit0.setBorder(padding0);
		jRadioBodyMarginUnit0.setFocusPainted(false);
		panel.add(jRadioBodyMarginUnit0);
		group.add(jRadioBodyMarginUnit0);
		jRadioBodyMarginUnit1 = new JRadioButton("%");
		jRadioBodyMarginUnit1.setBorder(padding0);
		jRadioBodyMarginUnit1.setFocusPainted(false);
		panel.add(jRadioBodyMarginUnit1);
		group.add(jRadioBodyMarginUnit1);
		
		////////////////////////////////////////////////////////////////
		//Tab 文字出力
		////////////////////////////////////////////////////////////////
		/*tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.addTab("文字", charIcon, tabPanel);
		*/
		//スタイルタブの下に追加
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("濁点/半濁点文字"));
		tabPanel.add(panel);
		group = new ButtonGroup();
		jRadioDakutenType0 = new JRadioButton("そのまま");
		jRadioDakutenType0.setToolTipText("結合文字は通常の文字に変換されます"); 
		jRadioDakutenType0.setBorder(padding2);
		jRadioDakutenType0.setFocusPainted(false);
		panel.add(jRadioDakutenType0);
		group.add(jRadioDakutenType0);
		jRadioDakutenType1 = new JRadioButton("重ねる", true);
		jRadioDakutenType1.setToolTipText("Reader,Kobo,Kindle以外はずれる場合があります。ルビ内はそのまま出力します"); 
		jRadioDakutenType1.setBorder(padding2);
		jRadioDakutenType1.setFocusPainted(false);
		panel.add(jRadioDakutenType1);
		group.add(jRadioDakutenType1);
		jRadioDakutenType2 = new JRadioButton("フォント", true);
		jRadioDakutenType2.setToolTipText("一文字フォントを利用します。端末によっては太字斜体表示できません"); 
		jRadioDakutenType2.setBorder(padding2);
		jRadioDakutenType2.setFocusPainted(false);
		panel.add(jRadioDakutenType2);
		group.add(jRadioDakutenType2);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("IVS出力(Kobo,Kindle非対応)"));
		tabPanel.add(panel);
		jCheckIvsBMP = new JCheckBox("英数字用(U+FE00-FE0E)", false);
		jCheckIvsBMP.setToolTipText("英数字、絵文字向けのIVSを出力します"); 
		jCheckIvsBMP.setFocusPainted(false);
		jCheckIvsBMP.setBorder(padding2);
		panel.add(jCheckIvsBMP);
		jCheckIvsSSP = new JCheckBox("漢字用(U+E0100-E01EF)", false);
		jCheckIvsSSP.setToolTipText("漢字用のIVSを出力します"); 
		jCheckIvsSSP.setFocusPainted(false);
		jCheckIvsSSP.setBorder(padding2);
		panel.add(jCheckIvsSSP);
		
		////////////////////////////////////////////////////////////////
		//Tab Web小説
		////////////////////////////////////////////////////////////////
		tabPanel = new JPanel();
		//tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		jTabbedPane.addTab("Web", webIcon, tabPanel);
		
		////////////////////////////////
		//取得間隔
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("取得設定"));
		tabPanel.add(panel);
		label = new JLabel("取得間隔");
		label.setBorder(padding2);
		label.setToolTipText("Web小説の取得間隔を設定します");
		panel.add(label);
		jTextWebInterval = new JTextField("0.5");
		jTextWebInterval.setToolTipText(label.getToolTipText());
		jTextWebInterval.setHorizontalAlignment(JTextField.RIGHT);
		jTextWebInterval.setInputVerifier(new FloatInputVerifier(0.5f, 0, 60));
		jTextWebInterval.setMaximumSize(text3);
		jTextWebInterval.setPreferredSize(text3);
		jTextWebInterval.addFocusListener(new TextSelectFocusListener(jTextWebInterval));
		panel.add(jTextWebInterval);
		label = new JLabel("秒");
		label.setBorder(padding1);
		panel.add(label);
		
		//キャッシュ保存先
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("キャッシュ保存パス"));
		tabPanel.add(panel);
		jTextCachePath = new JTextField(".cache");
		jTextCachePath.setToolTipText("キャッシュファイルを保存するパスです。フルパスまたは起動パスからの相対パスを指定します");
		jTextCachePath.setMaximumSize(text300);
		jTextCachePath.setPreferredSize(text300);
		jTextCachePath.addFocusListener(new TextSelectFocusListener(jTextCachePath));
		panel.add(jTextCachePath);
		jButtonCachePath = new JButton("選択");
		jButtonCachePath.setBorder(padding2);
		jButtonCachePath.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/dst_path.png")));
		jButtonCachePath.setFocusPainted(false);
		jButtonCachePath.addActionListener(new CachePathChooserListener(jButtonCachePath));
		panel.add(jButtonCachePath);
		
		//未更新時のスキップ
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("更新判定"));
		tabPanel.add(panel);
		jTextWebModifiedExpire = new JTextField("24");
		jTextWebModifiedExpire.setToolTipText("この時間以内に取得したキャッシュを更新分として処理します");
		jTextWebModifiedExpire.setHorizontalAlignment(JTextField.RIGHT);
		jTextWebModifiedExpire.setInputVerifier(new NumberVerifier(24, 0, 9999));
		jTextWebModifiedExpire.setMaximumSize(text4);
		jTextWebModifiedExpire.setPreferredSize(text4);
		jTextWebModifiedExpire.addFocusListener(new TextSelectFocusListener(jTextWebModifiedExpire));
		panel.add(jTextWebModifiedExpire);
		label = new JLabel("時間以内");
		label.setBorder(padding1);
		label.setToolTipText(jTextWebModifiedExpire.getToolTipText());
		panel.add(label);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("ePub出力設定"));
		tabPanel.add(panel);
		jCheckWebConvertUpdated = new JCheckBox("更新時のみ出力");
		jCheckWebConvertUpdated.setToolTipText("新規追加または一覧ページで更新がある場合のみePubファイルを出力します");
		jCheckWebConvertUpdated.setFocusPainted(false);
		jCheckWebConvertUpdated.setBorder(padding2);
		panel.add(jCheckWebConvertUpdated);
		
		//変換対象
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new NarrowTitledBorder("変換対象"));
		tabPanel.add(panel);
		jCheckWebBeforeChapter = new JCheckBox("最新");
		jCheckWebBeforeChapter.setToolTipText("最新話から指定話数のみ出力します。追加更新分のみの出力がある場合はそれに追加されます");
		jCheckWebBeforeChapter.setFocusPainted(false);
		jCheckWebBeforeChapter.setBorder(padding0);
		jCheckWebBeforeChapter.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e){
			jTextWebBeforeChapterCount.setEditable(jCheckWebBeforeChapter.isSelected()); jTextWebBeforeChapterCount.repaint();
		}});
		panel.add(jCheckWebBeforeChapter);
		jTextWebBeforeChapterCount = new JTextField("1");
		jTextWebBeforeChapterCount.setToolTipText(jCheckWebBeforeChapter.getToolTipText());
		jTextWebBeforeChapterCount.setEditable(false);
		jTextWebBeforeChapterCount.setHorizontalAlignment(JTextField.RIGHT);
		jTextWebBeforeChapterCount.setInputVerifier(new IntegerInputVerifier(0, 0, 999));
		jTextWebBeforeChapterCount.setMaximumSize(text3);
		jTextWebBeforeChapterCount.setPreferredSize(text3);
		jTextWebBeforeChapterCount.addFocusListener(new TextSelectFocusListener(jTextWebBeforeChapterCount));
		panel.add(jTextWebBeforeChapterCount);
		label = new JLabel("話 +");
		label.setBorder(padding1);
		panel.add(label);
		jCheckWebModifiedOnly = new JCheckBox("更新分");
		jCheckWebModifiedOnly.setToolTipText("追加更新のあった話のみ変換します");
		jCheckWebModifiedOnly.setFocusPainted(false);
		jCheckWebModifiedOnly.setBorder(padding2);
		panel.add(jCheckWebModifiedOnly);
		panel.add(new JLabel("("));
		jCheckWebModifiedTail = new JCheckBox("連続");
		jCheckWebModifiedTail.setToolTipText("最新話から連続した更新分のみ変換します。途中話の更新は変換されません");
		jCheckWebModifiedTail.setFocusPainted(false);
		jCheckWebModifiedTail.setBorder(padding2);
		panel.add(jCheckWebModifiedTail);
		panel.add(new JLabel(")"));
		
		////////////////////////////////////////////////////////////////
		//テキストエリア
		////////////////////////////////////////////////////////////////
		JPanel lowerPane = new JPanel();
		lowerPane.setLayout(new BoxLayout(lowerPane, BoxLayout.Y_AXIS));
		jSplitPane.add(lowerPane);
		
		jTextArea = new JTextArea("AozoraEpub3: "+AozoraEpub3.VERSION);
		jTextArea.append("  ( Java "+System.getProperty("java.version"));
		jTextArea.append("  /  "+System.getProperty("os.name"));
		jTextArea.append(" )\n対応ファイル: 青空文庫txt(txt,zip,rar), 画像(zip,rar,cbz), URLショートカット(url)\n");
		jTextArea.append("ファイルまたはURL文字列をここにドラッグ＆ドロップ／ペーストで変換します。\n");
		jTextArea.setEditable(false);
		jTextArea.setFont(new Font("Default", Font.PLAIN, 12));
		jTextArea.setBorder(new LineBorder(Color.white, 3));
		//new DropTarget(jTextArea, DnDConstants.ACTION_COPY_OR_MOVE, new DropListener(), true);
		jTextArea.setTransferHandler(new TextAreaTransferHandler("text"));
		jTextArea.getActionMap().put("copy-text", new CopyTextAction());
		jTextArea.getActionMap().put("paste-url", new PasteUrlAction());
		// create new inputmap locally
		InputMap jtxInputMap = new InputMap();
		jtxInputMap.setParent(jTextArea.getInputMap());
		jtxInputMap.put(KeyStroke.getKeyStroke("ctrl C"), "copy-text");
		jtxInputMap.put(KeyStroke.getKeyStroke("ctrl V"), "paste-url");
		jTextArea.setInputMap(JComponent.WHEN_FOCUSED, jtxInputMap);
		//メニュー
		JMenuItem jCopyMenu = new JMenuItem("コピー");
		jCopyMenu.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/copy.png")));
		jCopyMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				String selected = jTextArea.getSelectedText();
				if (!"".equals(selected)) {
					getToolkit().getSystemClipboard().setContents(new StringSelection(selected), null);
				}
			}
		});
		JMenuItem jPasteMenu = new JMenuItem("貼り付け");
		jPasteMenu.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/paste.png")));
		jPasteMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				handleTextAreaTransfer(getToolkit().getSystemClipboard().getContents(jTextArea));
			}
		});
		final JPopupMenu jTextPopup = new JPopupMenu();
		jTextPopup.add(jCopyMenu);
		jTextPopup.add(jPasteMenu);
		jTextArea.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e)) {
					jTextPopup.show(jTextArea, e.getX(), e.getY());
				}
			}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
		});
		
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
		jButtonCancel.setBorder(padding2);
		jButtonCancel.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/cancel.png")));
		jButtonCancel.setFocusPainted(false);
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
		//右パネル
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 2));
		statusPane.add(panel);
		
		//変換前に確認
		jCheckConfirm = new JCheckBox("変換前確認", true);
		jCheckConfirm.setToolTipText("変換前にタイトルと表紙の設定が可能な確認画面を表示します");
		jCheckConfirm.setFocusPainted(false);
		jCheckConfirm.setBorder(padding0);
		panel.add(jCheckConfirm);
		
		//ログ関連
		label = new JLabel("    ログ:");
		label.setBorder(padding2H);
		panel.add(label);
		JButton jButtonLogClear = new JButton();
		jButtonLogClear.setToolTipText("ログを削除してクリップボードにコピーします");
		jButtonLogClear.setBorder(padding2);
		jButtonLogClear.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/del_cut.png")));
		jButtonLogClear.setFocusPainted(false);
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
		////////////////////////////////////////////////////////////////
		jConfirmDialog = new JConfirmDialog(
			iconImage, AozoraEpub3Applet.class.getResource("images/icon.png").toString().replaceFirst("/icon\\.png", "/") );
		if ("1".equals(props.getProperty("ReplaceCover"))) jConfirmDialog.jCheckReplaceCover.setSelected(true);
		
		////////////////////////////////////////////////////////////////
		//iniファイルの中身を設定
		////////////////////////////////////////////////////////////////
		loadProperties(this.props);
		
		//変換前確認の設定
		setPropsSelected(this.jCheckConfirm, props, "ChkConfirm");
		
		////////////////////////////////////////////////////////////////
		//ログ出力先を設定
		////////////////////////////////////////////////////////////////
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
		
		////////////////////////////////////////////////////////////////
		//すべて初期化にプロファイル読み込み
		////////////////////////////////////////////////////////////////
		//前回選択されていたプロファイル
		String selectedProfileFileName = this.props.getProperty("ProfileFileName");
		
		//並び順取得
		Vector<File> propFiles = new Vector<File>();
		HashSet<String> profileFileNameMap = new HashSet<String>();
		if (this.props.containsKey("ProfileList")) {
			for (String fileName : this.props.getProperty("ProfileList").split(",")) {
				File file = new File(profilePath.getPath()+"/"+fileName);
				if (file.isFile()) {
					propFiles.add(file);
					profileFileNameMap.add(file.getName());
				}
			}
		}
		//まだ追加されていないファイルを追加
		for (File file : profilePath.listFiles()) {
			if (!profileFileNameMap.contains(file.getName())) {
				propFiles.add(file);
			}
		}
		profileFileNameMap.clear();
		
		//保存済プロファイルの名称を取得してコンボボックスに追加
		for (File profile : propFiles) {
			Properties profileProps = new Properties(); 
			try {
				FileInputStream fos = new FileInputStream(profile);
				profileProps.load(fos);
				fos.close();
			} catch (Exception e) { }
			String name = profileProps.getProperty("ProfileName");
			if (name != null && name.length() > 0) {
				jComboProfile.addItem(new ProfileInfo(profile.getName(), name, profileProps));
				//選択
				if (profile.getName().equals(selectedProfileFileName)) {
					jComboProfile.setSelectedIndex(jComboProfile.getItemCount()-1);
				}
			} else {
				//名前がなければ起動時に削除
				profile.delete();
			}
		}
		//何もなければデフォルト設定を追加
		if (jComboProfile.getItemCount() == 0) {
			try { addProfile("デフォルト"); } catch (Exception e) { e.printStackTrace(); }
		}
		//選択済プロファイル保存
		selectedProfile = (ProfileInfo)jComboProfile.getSelectedItem();
		
		//追加後にイベント有効
		jComboProfile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) {
			try {
				if (jComboProfile.getSelectedItem() instanceof ProfileInfo) {
					//選択済のプロファイルを更新
					if (selectedProfile != null) {
						setProperties(selectedProfile.getProperties());
						selectedProfile.update(profilePath);
					}
					ProfileInfo propInfo = (ProfileInfo)jComboProfile.getSelectedItem();
					loadProperties(propInfo.getProperties());
					//選択済入れ替え
					selectedProfile = propInfo;
				}
				//移動ボタン有効化
				setProfileMoveEnable();
			} catch (Exception e) { e.printStackTrace(); }
		}});
		
		//移動ボタン有効化
		setProfileMoveEnable();
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
		
		//NumberFormat format = NumberFormat.getNumberInstance();
		
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
					textField.setText(Float.toString(f));
					return true;
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
	
	/** 小数点以下が0なら表示しない */
	class NumberVerifier extends InputVerifier
	{
		/** 基準値 */
		float def = 0;
		/** 最小値 */
		float min = Float.MIN_VALUE;
		/** 最大値 */
		float max = Float.MAX_VALUE;
		
		NumberFormat format = NumberFormat.getNumberInstance();
		
		NumberVerifier(float def, float min)
		{
			this.def = Math.max(def, min);
			this.min = min;
		}
		NumberVerifier(float def, float min, float max)
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
					textField.setText(this.format.format(f));
					return true;
				}
				if (this.max != Float.MAX_VALUE && f > this.max) {
					textField.setText(this.format.format(this.max));
					return true;
				} else if (f < this.min) {
					textField.setText(this.format.format(this.min));
					return true;
				}
			} catch (NumberFormatException e) { }
			textField.setText(this.format.format(this.def));
			return true;
		}
	}
	
	/** Preset選択イベント */
	class LoadPresetActionListener implements ActionListener
	{
		Properties props;
		
		LoadPresetActionListener(Properties props)
		{
			this.props = props;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			loadProperties(this.props);
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
	
	/** キャッシュパス選択ボタンイベント */
	class CachePathChooserListener implements ActionListener
	{
		Component parent;
		private CachePathChooserListener(Component parent)
		{
			this.parent = parent;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			File path = new File(jTextCachePath.getText());
			if (!path.isDirectory()) path = path.getParentFile();
			if (path != null && !path.isDirectory()) path = path.getParentFile();
			JFileChooser fileChooser = new JFileChooser(path);
			fileChooser.setDialogTitle("キャッシュ出力先を選択");
			fileChooser.setApproveButtonText("選択");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				String pathString = fileChooser.getSelectedFile().getAbsolutePath();
				try {
					//パス調整
					String rootPath = new File("").getCanonicalPath();
					if (pathString.startsWith(rootPath)) {
						pathString = pathString.substring(rootPath.length()+1);
					}
				} catch (IOException e1) { }
				jTextCachePath.setText(pathString);
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
						jCheckSamePath.setSelected(false);
						jComboDstPath.setEditable(true);
						jComboDstPath.setSelectedItem(file.getAbsolutePath());
						return;
					}
				}
				if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String path = (String)transfer.getTransferData(DataFlavor.stringFlavor);
					if (path.startsWith("file://"))
						try { path = URLDecoder.decode(path.substring(0, path.indexOf('\n')-1).substring(7).trim(),"UTF-8"); } catch (UnsupportedEncodingException e1) { }
					jCheckSamePath.setSelected(false);
					jComboDstPath.setEditable(true);
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
			File path = null;
			String dstPath = null;
			Object obj = jCheckSamePath.isSelected() ? jComboDstPath.getSelectedItem() : jComboDstPath.getEditor().getItem();
			if (obj != null) dstPath = obj.toString();
			//パス修正
			if (dstPath == null || "".equals(dstPath)) path = currentPath;
			else {
				path = new File(dstPath);
				if (path != null && !path.isDirectory()) path = path.getParentFile();
				if (path != null && !path.isDirectory()) path = path.getParentFile();
				if (path != null && !path.isDirectory()) path = currentPath;
			}
			JFileChooser fileChooser = new JFileChooser(path);
			fileChooser.setDialogTitle("出力先を選択");
			fileChooser.setApproveButtonText("選択");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				String pathString = fileChooser.getSelectedFile().getAbsolutePath();
				jCheckSamePath.setSelected(false);
				jComboDstPath.setEditable(true);
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
			fileChooser.setFileFilter(new FileNameExtensionFilter("青空文庫(txt,zip,rar,txtz),画像(zip,rar,cbz),ショートカット(url)", new String[]{"txt","zip","rar","cbz","txtz","url"}));
			fileChooser.setMultiSelectionEnabled(true);
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				
				Vector<File> vecFiles = new Vector<File>();
				Vector<String> vecUrlString = null;
				Vector<File> vecUrlSrcFile = null;
				File dstPath = null;
				try {
				for (File file : fileChooser.getSelectedFiles()) {
					if (file.getName().toLowerCase().endsWith(".url")) {
						if (vecUrlString == null) vecUrlString = new Vector<String>();
						vecUrlString.add(readInternetShortCut(file));
						vecUrlSrcFile.add(file);
						dstPath = file.getParentFile();
					} else {
						vecFiles.add(file);
					}
				}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				startConvertWorker(vecFiles, vecUrlString, vecUrlSrcFile, dstPath);
			}
		}
	}
	/** ドラッグ＆ドロップイベント
	 * 複数ファイルに対応 */
	/*class DropListener implements DropTargetListener
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
					//IEはurlはショートカットとURL文字列の両方がくる
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() > 0) {
						for (File file : files) {
							if (file.getName().toLowerCase().endsWith(".url")) {
								if (vecUrlString == null) vecUrlString = new Vector<String>();
								vecUrlString.add(readInternetShortCut(file));
								dstPath = file.getParentFile();
							} else {
								vecFiles.add(file);
							}
						}
						if (vecFiles.size() > 0) {
							startConvertFilesWorker(vecFiles, dstPath);
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
									if (file.getName().toLowerCase().endsWith(".url")) {
										if (vecUrlString == null) vecUrlString = new Vector<String>();
										vecUrlString.add(readInternetShortCut(file));
										dstPath = file.getParentFile();
									} else {
										vecFiles.add(file);
									}
								}
							}
							if (vecFiles.size() > 0) {
								startConvertFilesWorker(vecFiles, dstPath);
							}
						} catch (Exception e) { e.printStackTrace(); }
					}
					else if (urlString != null && urlString.toLowerCase().startsWith("http")) {
						//ブラウザからのDnD
						vecUrlString = new Vector<String>();
						vecUrlString.add(urlString);
						dstPath = null;
					}
					
					//URL変換 の最後が .zip .txtz .rar
					if (urlString != null) {
						String ext = urlString.substring(urlString.lastIndexOf('.')+1).toLowerCase();
						if (ext.equals("zip") || ext.equals("txtz") || ext.equals("rar")) {
							convertArchive(urlString, dstPath);
							return;
						}
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
	}*/
	
	/** TextAreaの選択文字をコピーするTextAction */
	@SuppressWarnings("serial")
	class CopyTextAction extends TextAction
	{
		CopyTextAction() { super("copy-text"); }
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);
			if (target != null) {
				String selected = target.getSelectedText();
				if (!"".equals(selected)) {
					getToolkit().getSystemClipboard().setContents(new StringSelection(selected), null);
				}
			}
		}
	}
	/** TextAreaにペーストするTextAction */
	@SuppressWarnings("serial")
	class PasteUrlAction extends TextAction
	{
		PasteUrlAction() { super("paste-url"); }
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);
			if (target != null) {
				target.getTransferHandler().importData(
					new TransferHandler.TransferSupport(target, Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null))
				);
			}
		}
	}
	
	/** ドラッグ＆ドロップとペーストイベント */
	@SuppressWarnings("serial")
	class TextAreaTransferHandler extends TransferHandler
	{
		//@Override
		protected TextAreaTransferHandler() { super(); }
		//@Override
		TextAreaTransferHandler(String property) { super(property); }
		// dropのみ
		@Override
		public boolean canImport(TransferSupport support)
		{
			if (isRunning()) return false;
			
			DataFlavor[] dfs = support.getDataFlavors();
			for (DataFlavor df : dfs) {
				if (DataFlavor.stringFlavor.equals(df)) {
					return true;
				} else if (DataFlavor.javaFileListFlavor.equals(df)) {
					int action = support.getDropAction();
					return (action == COPY || action == MOVE);
				}
			}
			return false;
		}
		@Override
		public boolean importData(TransferSupport support)
		{
			if (support.isDrop()) {
				// ドロップ処理
				//LogAppender.println("ドロップ");
				int action = support.getDropAction();
				if (action == COPY || action == MOVE) {
					return handleTextAreaTransfer(support.getTransferable());
				}
			} else {
				// ペースト処理
				//LogAppender.println("ペースト");
				return handleTextAreaTransfer(support.getTransferable());
			}
			return false;
		}
		// dropのみ
		//@Override
		//protected void exportDone(JComponent source, Transferable data, int action) {}
	}
	////////////////
	/** ファイルまたはURLを取得して変換処理を実行 */
	boolean handleTextAreaTransfer(Transferable transfer)
	{
		if (isRunning()) return false;
		try {
			Vector<File> vecFiles = new Vector<File>();
			//Web変換対象URLを格納
			Vector<String> vecUrlString = new Vector<String>();
			//ショートカットファイルを格納(同名の表紙取得に利用)
			Vector<File> vecUrlSrcFile = new Vector<File>();
			File dstPath = null;
			
			if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				//ブラウザからだとStringとFileの両方が来る Linuxは file:// 文字列
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
								if (dstPath == null && !isCacheFile(file)) dstPath = file.getParentFile();
								if (file.getName().toLowerCase().endsWith(".url")) {
									String urlLine = readInternetShortCut(file);
									if (urlLine != null && (urlLine.startsWith("http://") || urlLine.startsWith("https://"))) {
										vecUrlString.add(urlLine);
										vecUrlSrcFile.add(file);
									}
								} else {
									vecFiles.add(file);
								}
							}
						}
					} catch (Exception e) { e.printStackTrace(); }
				}
				else if (urlString != null) {
					//ブラウザからのDnD
					dstPath = null;
					try {
						String[] urlLines = urlString.split("\n| ");
						for (String urlLine : urlLines) {
							if (urlLine != null) {
								if (urlLine.startsWith("http://") || urlLine.startsWith("https://")) {
									//Webから取得で処置
									vecUrlString.add(urlLine);
									vecUrlSrcFile.add(null);
								} else if (urlLine.endsWith(".txt")) {
									File file = new File(urlLine);
									if (file.isFile()) {
										if (dstPath == null && !isCacheFile(file)) dstPath = file.getParentFile();
										vecFiles.add(file);
									}
								}
							}
						}
					} catch (Exception e) { e.printStackTrace(); }
				}
			}
			else if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				//ローカルファイルはFileのみ
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
				if (files.size() > 0) {
					for (File file : files) {
						if (file.exists()) {
							if (dstPath == null && !isCacheFile(file)) dstPath = file.getParentFile();
							if (file.getName().toLowerCase().endsWith(".url")) {
								String urlLine = readInternetShortCut(file);
								if (urlLine != null && (urlLine.startsWith("http://") || urlLine.startsWith("https://"))) {
									vecUrlString.add(urlLine);
									vecUrlSrcFile.add(file);
								}
							} else {
								vecFiles.add(file);
							}
						}
					}
				}
			}
			
			//何も変換しなければfalse
			if (vecFiles.size() == 0 && vecUrlString.size() == 0) return false;
			//変換実行
			startConvertWorker(vecFiles, vecUrlString, vecUrlSrcFile, dstPath);
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
		}
		return true;
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
		String dstPath = this.jComboDstPath.getEditor().getItem().toString().trim();
		if (dstPath.equals("") && jComboDstPath.getSelectedItem() != null) dstPath = this.jComboDstPath.getSelectedItem().toString().trim();
		if (dstPath.equals("")) return;
		
		int count = Math.min(10, this.jComboDstPath.getItemCount());
		for (int i=0; i<count; i++) {
			String item = (String)this.jComboDstPath.getItemAt(i);
			if (dstPath.equals(item)) {
				//先頭に移動して終了
				this.jComboDstPath.removeItemAt(i);
				this.jComboDstPath.insertItemAt(item, 0);
				this.jComboDstPath.setSelectedIndex(0);
				return;
			}
		}
		//ファイルがあれば先頭に追加
		this.jComboDstPath.insertItemAt(dstPath, 0);
		this.jComboDstPath.setSelectedIndex(0);
	}
	
	////////////////////////////////////////////////////////////////
	/** 複数ファイルを変換
	 * @param dstPath srcFileがキャッシュで入力ファイルを同じ場所に出力先指定をする場合 */
	private void convertFiles(Vector<File> vecSrcFiles, File dstPath)
	{
		File[] srcFiles = new File[vecSrcFiles.size()];
		for (int i=0; i<srcFiles.length; i++) {
			srcFiles[i] = vecSrcFiles.get(i);
		}
		this.convertFiles(srcFiles, dstPath);
	}
	/** 複数ファイルを変換
	 * @param dstPath srcFileがキャッシュで入力ファイルを同じ場所に出力先指定をする場合 */
	private void convertFiles(File[] srcFiles, File dstPath)
	{
		if (srcFiles.length == 0 ) return;
		
		convertCanceled = false;
		
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
		
		float imageScale = 0;
		if (jCheckImageScale.isSelected()) try { imageScale = Float.parseFloat(jTextImageScale.getText()); } catch (Exception e) {}
		int imageFloatType = 0; //0=無効 1=上 2=下
		int imageFloatW = 0;
		int imageFloatH = 0;
		if (jCheckImageFloat.isSelected()) {
			imageFloatType = this.jComboImageFloatType.getSelectedIndex()+1;
			try { imageFloatW =Integer.parseInt(jTextImageFloatW.getText()); } catch (Exception e) {}
			try { imageFloatH =Integer.parseInt(jTextImageFloatH.getText()); } catch (Exception e) {}
		}
		float jpegQualty = 0.8f; try { jpegQualty = Integer.parseInt(jTextJpegQuality.getText())/100f; } catch (Exception e) {}
		float gamma = 1.0f; if (jCheckGamma.isSelected()) try { gamma = Float.parseFloat(jTextGammaValue.getText()); } catch (Exception e) {}
		int autoMarginLimitH = 0;
		int autoMarginLimitV = 0;
		int autoMarginWhiteLevel = 0;
		float autoMarginPadding = 0;
		int autoMarginNombre = 0;
		float autoMarginNombreSize = 0.03f;
		if (jCheckAutoMargin.isSelected()) {
			try { autoMarginLimitH =Integer.parseInt(jTextAutoMarginLimitH.getText()); } catch (Exception e) {}
			try { autoMarginLimitV =Integer.parseInt(jTextAutoMarginLimitV.getText()); } catch (Exception e) {}
			try { autoMarginWhiteLevel =Integer.parseInt(jTextAutoMarginWhiteLevel.getText()); } catch (Exception e) {}
			try { autoMarginPadding =Float.parseFloat(jTextAutoMarginPadding.getText()); } catch (Exception e) {}
			autoMarginNombre = jComboAutoMarginNombre.getSelectedIndex();
			try { autoMarginNombreSize =Float.parseFloat(jTextAutoMarginNombreSize.getText())*0.01f; } catch (Exception e) {}
		}
		int rorateAngle = 0; if (jComboRotateImage.getSelectedIndex() == 1) rorateAngle = 90; else if (jComboRotateImage.getSelectedIndex() == 2) rorateAngle = -90;
		
		int imageSizeType = SectionInfo.IMAGE_SIZE_TYPE_ASPECT;
		if (jRadioImageSizeType1.isSelected()) imageSizeType = SectionInfo.IMAGE_SIZE_TYPE_AUTO;
		//else if (jRadioImageSizeType2.isSelected()) imageSizeType = SectionInfo.IMAGE_SIZE_TYPE_HEIGHT;
		
		//int imageFitType = SectionInfo.IMAGE_SIZE_TYPE_ASPECT;
		//if (jRadioImageFitType2.isSelected()) imageFitType = SectionInfo.IMAGE_SIZE_TYPE_HEIGHT;
		
		this.epub3Writer.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth,
				imageSizeType, jCheckFitImage.isSelected(), jCheckSvgImage.isSelected(), rorateAngle,
				imageScale, imageFloatType, imageFloatW, imageFloatH, jpegQualty, gamma, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre, autoMarginNombreSize);
		this.epub3ImageWriter.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth,
				imageSizeType, jCheckFitImage.isSelected(), jCheckSvgImage.isSelected(), rorateAngle,
				imageScale, imageFloatType, imageFloatW, imageFloatH, jpegQualty, gamma, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre, autoMarginNombreSize);
		//目次階層化設定
		this.epub3Writer.setTocParam(jCheckNavNest.isSelected(), jCheckNcxNest.isSelected());
		
		//スタイル設定
		String[] pageMargin = new String[4];
		String pageMarginUnit = jRadioPageMarginUnit0.isSelected()?"em":"%";
		for (int i=0; i<pageMargin.length; i++) {
			pageMargin[i] = jTextPageMargins[i].getText()+pageMarginUnit;
		}
		String[] bodyMargin = new String[4];
		String bodyMarginUnit = jRadioBodyMarginUnit0.isSelected()?"em":"%";
		for (int i=0; i<bodyMargin.length; i++) {
			bodyMargin[i] = jTextBodyMargins[i].getText()+bodyMarginUnit;
		}
		float lineHeight = 1.8f;
		try { lineHeight = Float.parseFloat(jComboLineHeight.getEditor().getItem().toString()); } catch (Exception e) {}
		int fontSize = 100;
		try { fontSize = (int)Float.parseFloat(jComboFontSize.getEditor().getItem().toString()); } catch (Exception e) {}
		
		int dakutenType = jRadioDakutenType0.isSelected() ? 0 : (jRadioDakutenType1.isSelected() ? 1 : 2);
		
		this.epub3Writer.setStyles(pageMargin, bodyMargin, lineHeight, fontSize, jCheckBoldUseGothic.isSelected(), jCheckGothicUseBold.isSelected());
		
		try {
			//挿絵なし
			this.aozoraConverter.setNoIllust(jCheckNoIllust.isSelected()); 
			//栞用ID出力
			this.aozoraConverter.setWithMarkId(this.jCheckMarkId.isSelected());
			//変換オプション設定
			this.aozoraConverter.setAutoYoko(this.jCheckAutoYoko.isSelected(), this.jCheckAutoYokoNum1.isSelected(), this.jCheckAutoYokoNum3.isSelected(), this.jCheckAutoEQ1.isSelected());
			//文字出力設定
			this.aozoraConverter.setCharOutput(dakutenType, jCheckIvsBMP.isSelected(), jCheckIvsSSP.isSelected());
			//全角スペースの禁則
			this.aozoraConverter.setSpaceHyphenation(this.jRadioSpaceHyp0.isSelected()?0:(this.jRadioSpaceHyp1.isSelected()?1:2));
			//注記のルビ表示
			this.aozoraConverter.setChukiRuby(this.jRadioChukiRuby1.isSelected(), this.jRadioChukiRuby2.isSelected());
			//コメント
			this.aozoraConverter.setCommentPrint(this.jCheckCommentPrint.isSelected(), this.jCheckCommentConvert.isSelected());
			
			//float表示
			this.aozoraConverter.setImageFloat(this.jCheckImageFloatPage.isSelected(), this.jCheckImageFloatBlock.isSelected());
			
			//空行除去
			int removeEmptyLine = jComboxRemoveEmptyLine.getSelectedIndex();
			int maxEmptyLine = jComboxMaxEmptyLine.getSelectedIndex();
			this.aozoraConverter.setRemoveEmptyLine(removeEmptyLine, maxEmptyLine);
			
			//行頭字下げ
			this.aozoraConverter.setForceIndent(this.jCheckForceIndent.isSelected());
			
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
					jCheckChapterH.isSelected(), jCheckChapterH1.isSelected(), jCheckChapterH2.isSelected(), jCheckChapterH3.isSelected(), jCheckSameLineChapter.isSelected(),
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
		LogAppender.append("------ ");
		if("zip".equals(ext) || "txtz".equals(ext)) { 
			try {
				txtCount = AozoraEpub3.countZipText(srcFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (txtCount == 0) { txtCount = 1; imageOnly = true; }
		} else if ("rar".equals(ext)) {
			try {
				txtCount = AozoraEpub3.countRarText(srcFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (txtCount == 0) { txtCount = 1; imageOnly = true; }
		} else if ("cbz".equals(ext)) {
			imageOnly = true;
		} else if ("txt".equals(ext)) {
			LogAppender.println();
		}
		if (this.convertCanceled){
			LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
			return;
		}
		
		//キャッシュパスのファイルならエンコードを変換時のみUTF-8にする
		String encType = (String)jComboEncType.getSelectedItem();
		if (this.isCacheFile(srcFile)) jComboEncType.setSelectedItem("UTF-8");
		try {
			for (int i=0; i<txtCount; i++) {
				convertFile(srcFile, dstPath, ext, i, imageOnly);
				if (convertCanceled) return;
			}
		} finally {
			//設定を戻す
			jComboEncType.setSelectedItem(encType);
		}
		
	}
	/** 内部用変換関数 Appletの設定を引数に渡す
	 * @param srcFile 変換するファイル txt,zip,cbz,(rar,cbr)
	 * @param dstPath 出力先パス
	 * @param txtIdx Zip内テキストファイルの位置
	 */
	private void convertFile(File srcFile, File dstPath, String ext, int txtIdx, boolean imageOnly)
	{
		//パラメータ設定
		if (!"txt".equals(ext) && !"txtz".equals(ext) && !"zip".equals(ext) && !"cbz".equals(ext) && !"rar".equals(ext) ) {
			if (!"png".equals(ext) && !"jpg".equals(ext) && !"jpeg".equals(ext) && !"gif".equals(ext)) {
				LogAppender.println("txt, txtz, zip, cbz rar 以外は変換できません");
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
		
		boolean isFile = "txt".equals(ext);
		ImageInfoReader imageInfoReader = new ImageInfoReader(isFile, srcFile);
		
		//zip内の画像をロード
		try {
			if (!isFile) {
				if ("rar".equals(ext)) {
					//Rar内の画像情報読み込み 画像のみならファイル順も格納
					imageInfoReader.loadRarImageInfos(srcFile, imageOnly);
				} else {
					//Zip内の画像情報読み込み 画像のみならファイル順も格納
					imageInfoReader.loadZipImageInfos(srcFile, imageOnly);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogAppender.error(e.getMessage());
		}
		
		//BookInfo取得
		BookInfo bookInfo = null;
		try {
			if (!imageOnly) {
				//テキストファイルからメタ情報や画像単独ページ情報を取得
				bookInfo = AozoraEpub3.getBookInfo(
					srcFile, ext, txtIdx, imageInfoReader, this.aozoraConverter,
					this.jComboEncType.getSelectedItem().toString(),
					BookInfo.TitleType.indexOf(this.jComboTitle.getSelectedIndex()),
					jCheckPubFirst.isSelected()
				);
			}
		} catch (Exception e) {
			LogAppender.error("ファイルが読み込めませんでした : "+srcFile.getPath());
			return;
		}
		
		if (this.convertCanceled){
			LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
			return;
		}
		
		Epub3Writer writer = this.epub3Writer;
		try {
			if (!isFile) {
				//Zip内の画像情報をbookInfoに設定
				if (imageOnly) {
					LogAppender.println("画像のみのePubファイルを生成します");
					//画像出力用のBookInfo生成
					bookInfo = new BookInfo(srcFile);
					bookInfo.imageOnly = true;
					//Writerを画像出力用派生クラスに入れ替え
					writer = this.epub3ImageWriter;
					
					if (imageInfoReader.countImageFileInfos() == 0) {
						LogAppender.error("画像がありませんでした");
						return;
					}
					
					//名前順で並び替え
					imageInfoReader.sortImageFileNames();
					//先頭画像をbookInfoに設定しておく
					//if (coverImageIndex == 0) {
					//	bookInfo.coverImage = imageInfoReader.getImage(0);
					//}
					//画像数をプログレスバーに設定 xhtml出力で+1 画像出力で+10
					this.jProgressBar.setMaximum(imageInfoReader.countImageFileInfos()*11);
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
			LogAppender.error(e.getMessage());
		}
		
		if (bookInfo == null) {
			LogAppender.error("書籍の情報が取得できませんでした");
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
		bookInfo.insertTocPage = this.jCheckTocPage.isSelected();
		bookInfo.insertCoverPageToc = this.jCheckCoverPageToc.isSelected();
		bookInfo.insertTitleToc = this.jCheckTitleToc.isSelected();
		//表題の見出しが非表示で行が追加されていたら削除
		if (!bookInfo.insertTitleToc && bookInfo.titleLine >= 0) {
			bookInfo.removeChapterLineInfo(bookInfo.titleLine);
		}
		
		//目次縦書き
		bookInfo.setTocVertical(this.jRadioTocV.isSelected());
		//縦書き横書き設定追加
		bookInfo.vertical = this.jRadioVertical.isSelected();
		this.aozoraConverter.vertical = bookInfo.vertical;
		
		//表紙設定
		//表題左右中央
		if (!this.jCheckTitlePage.isSelected()) {
			bookInfo.titlePageType = BookInfo.TITLE_NONE;
		} else if (this.jRadioTitleNormal.isSelected()) {
			bookInfo.titlePageType = BookInfo.TITLE_NORMAL;
		} else if (this.jRadioTitleMiddle.isSelected()) {
			bookInfo.titlePageType = BookInfo.TITLE_MIDDLE;
		} else if (this.jRadioTitleHorizontal.isSelected()) {
			bookInfo.titlePageType = BookInfo.TITLE_HORIZONTAL;
		}
		
		//先頭からの場合で指定行数以降なら表紙無し
		if ("".equals(coverFileName) && !imageOnly) {
			try {
				int maxCoverLine = Integer.parseInt(this.jTextMaxCoverLine.getText());
				if (maxCoverLine > 0 && (bookInfo.firstImageLineNum == -1 || bookInfo.firstImageLineNum >= maxCoverLine)) {
					coverImageIndex = -1;
					coverFileName = null;
				} else {
					coverImageIndex = bookInfo.firstImageIdx;
				}
			} catch (Exception e) {}
		}
		
		//表紙ページの情報をbookInfoに設定
		bookInfo.coverFileName = coverFileName;
		bookInfo.coverImageIndex = coverImageIndex;
		
		String[] titleCreator = BookInfo.getFileTitleCreator(srcFile.getName());
		if (jCheckUseFileName.isSelected()) {
			//ファイル名優先ならテキスト側の情報は不要
			bookInfo.title = "";
			bookInfo.creator = "";
			if (titleCreator[0] != null) bookInfo.title = titleCreator[0];
			if (titleCreator[1] != null) bookInfo.creator = titleCreator[1];
		} else {
			//テキストから取得できなければファイル名を利用
			if (bookInfo.title == null || bookInfo.title.length() == 0) {
				bookInfo.title = titleCreator[0]==null?"":titleCreator[0];
				if (bookInfo.creator == null || bookInfo.creator.length() == 0) bookInfo.creator = titleCreator[1]==null?"":titleCreator[1];
			}
		}
		
		if (this.convertCanceled){
			LogAppender.println("変換処理を中止しました : "+srcFile.getAbsolutePath());
			return;
		}
		
		//前回の変換設定を反映
		BookInfoHistory history = this.getBookInfoHistory(bookInfo);
		if (history != null) {
			if (bookInfo.title.length() == 0) bookInfo.title = history.title;
			bookInfo.titleAs = history.titleAs;
			if (bookInfo.creator.length() == 0) bookInfo.creator = history.creator;
			bookInfo.creatorAs = history.creatorAs;
			if (bookInfo.publisher == null) bookInfo.publisher = history.publisher;
			//表紙設定
			if (jCheckCoverHistory.isSelected()) {
				bookInfo.coverEditInfo = history.coverEditInfo;
				bookInfo.coverFileName = history.coverFileName;
				bookInfo.coverExt = history.coverExt;
				bookInfo.coverImageIndex = history.coverImageIndex;
				
				//確認ダイアログ表示しない場合はイメージを生成
				if (!this.jCheckConfirm.isSelected() && bookInfo.coverEditInfo != null) {
					try {
						this.jConfirmDialog.jCoverImagePanel.setBookInfo(bookInfo);
						if (bookInfo.coverImageIndex >= 0 && bookInfo.coverImageIndex < imageInfoReader.countImageFileNames()) {
							bookInfo.coverImage = imageInfoReader.getImage(bookInfo.coverImageIndex);
						} else if (bookInfo.coverImage == null && bookInfo.coverFileName != null) {
							bookInfo.loadCoverImage(bookInfo.coverFileName);
						}
						bookInfo.coverImage = this.jConfirmDialog.jCoverImagePanel.getModifiedImage(this.coverW, this.coverH);
					} catch (Exception e) { e.printStackTrace(); }
				}
			}
		}
		
		String outExt = this.jComboExt.getEditor().getItem().toString().trim();
		////////////////////////////////
		//Kindleチェック
		File kindlegen = null;
		writer.setIsKindle(false);
		if (outExt.startsWith(".mobi")) {
			kindlegen = new File(this.jarPath+"kindlegen.exe");
			if (!kindlegen.isFile()) {
				kindlegen = new File(this.jarPath+"kindlegen");
				if (!kindlegen.isFile()) {
					kindlegen = null;
				}
			}
			if (kindlegen == null) {
				JOptionPane.showMessageDialog(this, "kindlegenがありません\nkindlegen.exeをjarファイルの場所にコピーしてください", "kindlegenエラー", JOptionPane.WARNING_MESSAGE);
				LogAppender.println("変換処理をキャンセルしました");
				return;
			}
			writer.setIsKindle(true);
		}
		
		//確認ダイアログ 変換ボタン押下時にbookInfo更新
		if (this.jCheckConfirm.isSelected()) {
			//表題と著者設定 ファイル名から設定
			String title = "";
			String creator = "";
			if (bookInfo.title != null) title = bookInfo.title;
			if (bookInfo.creator != null) creator = bookInfo.creator;
			this.jConfirmDialog.setChapterCheck(jCheckChapterSection.isSelected(),
					jCheckChapterH.isSelected(), jCheckChapterH1.isSelected(), jCheckChapterH2.isSelected(), jCheckChapterH3.isSelected(),
					jCheckChapterName.isSelected(),
					jCheckChapterNumOnly.isSelected()||jCheckChapterNumTitle.isSelected()||jCheckChapterNumParen.isSelected()||jCheckChapterNumParenTitle.isSelected(),
					jCheckChapterPattern.isSelected());
			this.jConfirmDialog.showDialog(
				srcFile,
				(dstPath!=null ? dstPath.getAbsolutePath() : srcFile.getParentFile().getAbsolutePath())+File.separator,
				title, creator, this.jComboTitle.getSelectedIndex(), jCheckPubFirst.isSelected(),
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
				this.setBookInfoHistory(bookInfo);
				LogAppender.println("変換をスキップしました : "+srcFile.getAbsolutePath());
				return;
			}
			
			//変換前確認のチェックを反映
			if (!this.jConfirmDialog.jCheckConfirm2.isSelected()) jCheckConfirm.setSelected(false);
			
			//確認ダイアログの値をBookInfoに設定
			bookInfo.title = this.jConfirmDialog.getMetaTitle();
			bookInfo.creator = this.jConfirmDialog.getMetaCreator();
			bookInfo.titleAs = this.jConfirmDialog.getMetaTitleAs();
			bookInfo.creatorAs = this.jConfirmDialog.getMetaCreatorAs();
			bookInfo.publisher = this.jConfirmDialog.getMetaPublisher();
			
			//著者が空欄なら著者行もクリア
			if (bookInfo.creator.length() == 0) bookInfo.creatorLine = -1;
			
			//プレビューでトリミングされていたらbookInfo.coverImageにBufferedImageを設定 それ以外はnullにする
			BufferedImage coverImage = this.jConfirmDialog.jCoverImagePanel.getModifiedImage(this.coverW, this.coverH);
			if (coverImage != null) {
				//Epub3Writerでイメージを出力
				bookInfo.coverImage = coverImage;
				//bookInfo.coverFileName = null;
				//元の表紙は残す
				if (this.jConfirmDialog.jCheckReplaceCover.isSelected()) bookInfo.coverImageIndex = -1;
			} else {
				bookInfo.coverImage = null;
			}
			
			this.setBookInfoHistory(bookInfo);
		} else {
			//表題の見出しが非表示で行が追加されていたら削除
			if (!bookInfo.insertTitleToc && bookInfo.titleLine >= 0) {
				bookInfo.removeChapterLineInfo(bookInfo.titleLine);
			}
		}
		
		boolean autoFileName = this.jCheckAutoFileName.isSelected();
		boolean overWrite = this.jCheckOverWrite.isSelected();
		
		//出力ファイル
		File outFile = null;
		
		//Kindleは一旦tmpファイルに出力
		File outFileOrg = null;
		if (kindlegen != null) {
			outFile = AozoraEpub3.getOutFile(srcFile, dstPath, bookInfo, autoFileName, ".epub");
			File mobiFile = new File(outFile.getAbsolutePath().substring(0, outFile.getAbsolutePath().length()-4)+"mobi");
			if (!overWrite && (mobiFile.exists() || (outExt.endsWith(".epub") && outFile.exists()))) {
				LogAppender.println("変換中止: "+srcFile.getAbsolutePath());
				if (mobiFile.exists()) LogAppender.println("ファイルが存在します: "+mobiFile.getAbsolutePath());
				else LogAppender.println("ファイルが存在します: "+outFile.getAbsolutePath());
				return;
			}
			outFileOrg = outFile;
			try {
				outFile = File.createTempFile("kindle", ".epub", outFile.getParentFile());
				if (!outExt.endsWith(".epub")) outFile.deleteOnExit();
			} catch (IOException e) {
				outFile = outFileOrg;
				outFileOrg = null;
			}
		} else {
			outFile = AozoraEpub3.getOutFile(srcFile, dstPath, bookInfo, autoFileName, outExt);
			//上書き確認
			if (!overWrite &&  outFile.exists()) {
				LogAppender.println("変換中止: "+srcFile.getAbsolutePath());
				LogAppender.println("ファイルが存在します: "+outFile.getAbsolutePath());
				return;
			}
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
		//変換実行
		AozoraEpub3.convertFile(
			srcFile, ext, outFile,
			this.aozoraConverter,
			writer,
			this.jComboEncType.getSelectedItem().toString(),
			bookInfo, imageInfoReader, txtIdx
		);
		
		imageInfoReader = null;
		//画像は除去
		bookInfo.coverImage = null;
		
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
				String msg = "";
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						System.out.println(line);
						if (msg.startsWith("Error")) msg += line;
						else msg = line;
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
					if (outFileOrg != null) {
						//mobiリネーム
						File mobiTmpFile = new File(outFile.getAbsolutePath().substring(0, outFile.getAbsolutePath().length()-4)+"mobi");
						File mobiFile = new File(outFileOrg.getAbsolutePath().substring(0, outFileOrg.getAbsolutePath().length()-4)+"mobi");
						if (mobiFile.exists()) mobiFile.delete();
						mobiTmpFile.renameTo(mobiFile);
						if (outExt.endsWith(".epub")) {
							//epubリネーム
							if (outFileOrg.exists()) outFileOrg.delete();
							outFile.renameTo(outFileOrg);
						} else {
							outFile.delete();
						}
						LogAppender.println("\n"+msg+"\nkindlegen変換完了 ["+(((System.currentTimeMillis()-time)/100)/10f)+"s] -> "+mobiFile.getName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (this.kindleProcess != null) this.kindleProcess.destroy();
			this.kindleProcess = null;
		}
	}
	
	/** Web変換
	 * @param vecUrlString 青空文庫テキストのzipまたは対応サイトのリンクURL
	 * @param vecUrlSrcFile ショートカットファイルのURLならファイルが指定されている */
	private void convertWeb(Vector<String> vecUrlString, Vector<File> vecUrlSrcFile, File dstPath) throws IOException
	{
		for (int i=0; i<vecUrlString.size(); i++) {
			String urlString = vecUrlString.get(i);
			File urSrcFile = null;
			if (vecUrlSrcFile != null && vecUrlSrcFile.size() > i) urSrcFile = vecUrlSrcFile.get(i);
			//URL変換 の最後が .zip .txtz .rar
			String ext = urlString.substring(urlString.lastIndexOf('.')+1).toLowerCase();
			if (ext.equals("zip") || ext.equals("txtz") || ext.equals("rar")) {
				
				String urlPath = urlString.substring(urlString.indexOf("//")+2).replaceAll("\\?\\*\\&\\|\\<\\>\"\\\\", "_");
				//青空zipのURLをキャッシュして変換
				//出力先 出力パスに保存
				File srcFile = new File(dstPath+"/"+new File(urlPath).getName());
				LogAppender.println("出力先にダウンロードします : "+srcFile.getCanonicalPath());
				srcFile.getParentFile().mkdirs();
				//ダウンロード
				BufferedInputStream bis = new BufferedInputStream(new URL(urlString).openStream(), 8192);
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(srcFile));
				IOUtils.copy(bis, bos);
				bos.close();
				bis.close();
				
				//変換実行
				this.convertFiles(new File[]{srcFile}, dstPath);
				
				continue;
			}
			
			try {
				LogAppender.println("--------");
				LogAppender.append(urlString);
				LogAppender.println(" を読み込みます");
				
				webConverter = WebAozoraConverter.createWebAozoraConverter(urlString, webConfigPath);
				if (webConverter == null) {
					LogAppender.append(urlString);
					LogAppender.println(" は変換できませんでした");
					continue;
				}
				
				int interval = 500;
				try { interval = (int)(Float.parseFloat(jTextWebInterval.getText())*1000); } catch (Exception e) {}
				int beforeChapter = 0;
				if (this.jCheckWebBeforeChapter.isSelected()) {
					try { beforeChapter = Integer.parseInt(jTextWebBeforeChapterCount.getText()); } catch (Exception e) {}
				}
				float modifiedExpire = 0;
				try { modifiedExpire = Float.parseFloat(jTextWebModifiedExpire.getText()); } catch (Exception e) {}
				//キャッシュパス
				if (!this.cachePath.isDirectory()) {
					this.cachePath.mkdirs();
					LogAppender.println("キャッシュパスを作成します : "+this.cachePath.getCanonicalPath());
				}
				if (!this.cachePath.isDirectory()) {
					LogAppender.println("キャッシュパスが作成できませんでした");
					return;
				}
				
				File srcFile = webConverter.convertToAozoraText(urlString, this.cachePath, interval, modifiedExpire,
					this.jCheckWebConvertUpdated.isSelected(), this.jCheckWebModifiedOnly.isSelected(), jCheckWebModifiedTail.isSelected(),
					beforeChapter);
				
				if (srcFile == null) {
					LogAppender.append(urlString);
					if (jCheckWebConvertUpdated.isSelected() && !webConverter.isUpdated()
						|| jCheckWebModifiedOnly.isSelected() && !webConverter.isUpdated())
						LogAppender.println(" の変換をスキップしました");
					else if (webConverter.isCanceled())
						LogAppender.println(" の変換をキャンセルしました");
					else
						LogAppender.println(" は変換できませんでした");
					continue;
				}
				
				//エンコードを変換時のみUTF-8にする
				String encType = (String)jComboEncType.getSelectedItem();
				jComboEncType.setSelectedItem("UTF-8");
				int titleTypeIdx = jComboTitle.getSelectedIndex();
				jComboTitle.setSelectedIndex(0);
				boolean checkUseFileName = jCheckUseFileName.isSelected();
				jCheckUseFileName.setSelected(false);
				//コメント出力
				boolean commentPrint = jCheckCommentPrint.isSelected();
				jCheckCommentPrint.setSelected(true);
				boolean commentConvert = jCheckCommentConvert.isSelected();
				jCheckCommentConvert.setSelected(true);
				
				//表紙画像はconverted.pngで保存される 指定がない場合はそれを利用する
				Object coverItem = jComboCover.getSelectedItem();
				//入力ファイルと同じ表紙の指定の場合 ショートカットファイルのパスにファイルがあればファイルパスを指定に変更
				if (jComboCover.getSelectedIndex() == 1 && urSrcFile != null) {
					String coverFileName = AozoraEpub3.getSameCoverFileName(urSrcFile);
					jComboCover.setSelectedItem(coverFileName);
				}
				//同名のファイルが無い場合はconverted.pngを利用する設定に変更
				if (jComboCover.getSelectedIndex() == 0 || jComboCover.getSelectedIndex() == 1) jComboCover.setSelectedIndex(1);
				
				//変換処理実行
				convertFiles(new File[]{srcFile}, dstPath);
				
				//設定を戻す
				jComboEncType.setSelectedItem(encType);
				jComboTitle.setSelectedIndex(titleTypeIdx);
				jCheckUseFileName.setSelected(checkUseFileName);
				jCheckCommentPrint.setSelected(commentPrint);
				jCheckCommentConvert.setSelected(commentConvert);
				jComboCover.setSelectedItem(coverItem);
				
			} catch (Exception e) {
				e.printStackTrace(); LogAppender.println("エラーが発生しました : "+e.getMessage());
			}
		}
	}
	
	/** キャッシュパスを取得 */
	private File getCachePath()
	{
		String cachePathString = this.jTextCachePath.getText().trim();
		if("".equals(cachePathString)) cachePathString = this.jarPath+".cache"; 
		return new File(cachePathString);
	}
	/** キャッシュパスを以下のファイルならtrue */
	private boolean isCacheFile(File file)
	{
		try {
			return file.getCanonicalPath().startsWith(this.getCachePath().getCanonicalPath());
		} catch (IOException e) {
		}
		return false;
	}
	////////////////////////////////////////////////////////////////
	/** 別スレッド実行用SwingWorkerを実行
	 * @param dstPath 出力先 ブラウザからまたはURLペーストの場合はnull */
	void startConvertWorker(Vector<File> vecFiles, Vector<String> vecUrlString, Vector<File> vecUrlSrcFile, File dstPath)
	{
		//出力先が指定されていない場合は選択させる
		if (dstPath == null && jCheckSamePath.isSelected() || !jCheckSamePath.isSelected() && "".equals(jComboDstPath.getEditor().getItem().toString().trim())) {
			if (dstPath != null) this.currentPath = dstPath;
			dstPathChooser.actionPerformed(null);
			if (jCheckSamePath.isSelected() || "".equals(jComboDstPath.getEditor().getItem().toString().trim())) {
				LogAppender.println("変換処理を中止しました : "+(vecFiles.size()>0?vecFiles.get(0).getAbsoluteFile():vecUrlString.size()>0?vecUrlString.get(0):""));
				return;
			}
		}
		if (!jCheckSamePath.isSelected()) {
			dstPath = new File(jComboDstPath.getEditor().getItem().toString());
		}
		
		if (!dstPath.isDirectory()) {
			dstPath = new File(jComboDstPath.getEditor().getItem().toString());
			String dstPathName = dstPath.getAbsolutePath();
			if (dstPathName.length() > 70) dstPathName = dstPathName.substring(0, 32)+" ... "+dstPathName.substring(dstPathName.length()-32);
			int ret = JOptionPane.showConfirmDialog(jConfirmDialog, "出力先がありません\n"+dstPathName+"\nにフォルダを作成しますか？", "出力先確認", JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION) {
				//フォルダ作成
				dstPath.mkdirs();
				if (!dstPath.isDirectory()) {
					LogAppender.error("フォルダを作成できませんでした");
					return;
				}				
			} else {
				LogAppender.error("変換処理を中止しました : "+(vecFiles.size()>0?vecFiles.get(0).getAbsoluteFile():vecUrlString.size()>0?vecUrlString.get(0):""));
				return;
			}
		}
		
		//jComboDstPathに出力先履歴保存
		if (!jCheckSamePath.isSelected()) {
			this.addDstPath();
		}
		//キャッシュパスを先にTextから取得
		this.cachePath = this.getCachePath();
		
		//web以下に同じ名前のパスがあったらキャッシュ後青空変換
		ConvertWorker convertWorker = new ConvertWorker(vecFiles, vecUrlString, vecUrlSrcFile, dstPath);
		convertWorker.execute();
	}
	
	/** 別スレッド実行用SwingWorker */
	class ConvertWorker extends SwingWorker<Object, Object>
	{
		/** 面倒なのでAppletを渡す */
		AozoraEpub3Applet applet;
		/** 変換対象ファイル */
		Vector<File> vecFiles;
		/** 変換対象URL */
		Vector<String> vecUrlString;
		/** ショートカットファイル */
		Vector<File> vecUrlSrcFile;
		
		File dstPath = null;
		
		/** @param dstPath ショートカットファイルなら同じ場所出力用に指定 */
		public ConvertWorker(Vector<File> vecFiles, Vector<String> vecUrlString, Vector<File> vecUrlSrcFile, File dstPath)
		{
			this.applet = getApplet();
			
			this.vecFiles = vecFiles;
			this.vecUrlString = vecUrlString;
			this.vecUrlSrcFile = vecUrlSrcFile;
			
			this.dstPath = dstPath;
		}
		
		@Override
		protected Object doInBackground() throws Exception
		{
			this.applet.running = true;
			this.applet.setConvertEnabled(false);
			try {
				
				//ファイルの変換
				if (this.vecFiles != null && vecFiles.size() >0) {
					this.applet.convertFiles(vecFiles, dstPath);
				}
				if (vecUrlString != null && vecUrlString.size() >0) {
					this.applet.convertWeb(vecUrlString, vecUrlSrcFile, dstPath);
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogAppender.println("エラーが発生しました");
			} finally {
				this.applet.setConvertEnabled(true);
				this.applet.running = false;
			}
			return null;
		}
		
		@Override
		protected void done()
		{
			super.done();
			this.applet.setConvertEnabled(true);
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
		for (Component c : this.topPanel.getComponents()) this.setEnabledAll(c, enabled);
		for (Component c : this.jTabbedPane.getComponents()) this.setEnabledAll(c, enabled);
		jRadioVertical.getComponent(0).setEnabled(enabled);
		jRadioHorizontal.getComponent(0).setEnabled(enabled);
		//変換中に操作不可にしないもの
		if (!enabled) this.jCheckConfirm.setEnabled(true);
		this.jButtonCancel.setEnabled(!enabled);
		//disabledになっているものは再チェック
		if (enabled) {
			this.setProfileMoveEnable();
		}
		
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
	//Properties
	////////////////////////////////////////////////////////////////
	/** "1"が設定されている場合のみチェックをON nullなら変更しない */
	private boolean setPropsSelected(JToggleButton button, Properties props, String name)
	{
		if (props.containsKey(name)) {
			boolean selected = "1".equals(props.getProperty(name));
			button.setSelected(selected);
			return selected;
		}
		return button.isSelected();
	}
	/** "1"が設定されている場合のみチェックをON nullなら変更しない */
	private boolean setPropsSelected(JToggleButton button, Properties props, String name, boolean nullSelect)
	{
		if (props.containsKey(name)) {
			boolean selected = "1".equals(props.getProperty(name));
			button.setSelected(selected);
			return selected;
		} else {
			button.setSelected(nullSelect);
		}
		return button.isSelected();
	}
	/** テキスト値を設定 null なら設定しない */
	private void setPropsText(JTextField jText, Properties props, String name)
	{
		try {
			if (!props.containsKey(name)) return;
			jText.setText(props.getProperty(name));
		} catch (Exception e) {}
	}
	/** int値を設定 null なら設定しない */
	private void setPropsIntText(JTextField jText, Properties props, String name)
	{
		try {
			if (!props.containsKey(name)) return;
			jText.setText(Integer.toString(Integer.parseInt(props.getProperty(name))));
		} catch (Exception e) {}
	}
	/** float値を設定 null なら設定しない */
	private void setPropsFloatText(JTextField jText, Properties props, String name)
	{
		try {
			if (!props.containsKey(name)) return;
			jText.setText(Float.toString(Float.parseFloat(props.getProperty(name))));
		} catch (Exception e) {}
	}
	/** 数値を設定 null なら設定しない */
	private void setPropsNumberText(JTextField jText, Properties props, String name)
	{
		try {
			if (!props.containsKey(name)) return;
			jText.setText(NumberFormat.getNumberInstance().format(Float.parseFloat(props.getProperty(name))));
		} catch (Exception e) {}
	}
	
	/** プロファイルを新規保存 
	 * @throws IOException 
	 * @throws FileNotFoundException */
	private void addProfile(String name) throws FileNotFoundException, IOException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		File profile = new File(profilePath.getPath()+"/"+dateFormat.format(new Date())+".ini");
		int i = 1;
		while (profile.exists()) {
			profile = new File(profilePath.getPath()+"/"+dateFormat.format(new Date())+i+".ini");
			i++;
		}
		
		Properties profileProps = new Properties();
		//名前設定
		profileProps.setProperty("ProfileName", name);
		//画面の設定をPropertiesに設定
		this.setProperties(profileProps);
		//プロファイル保存
		FileOutputStream fos = new FileOutputStream(profile);
		profileProps.store(fos, "AozoraEpub3 Profile");
		fos.close();
		//コンボボックスに追加
		jComboProfile.addItem(new ProfileInfo(profile.getName(), name, profileProps));
		jComboProfile.setSelectedIndex(jComboProfile.getItemCount()-1);
		//移動ボタン有効化
		setProfileMoveEnable();
	}
	/** プロファイルを削除 */
	private void deleteProfile()
	{
		if (jComboProfile.getItemCount() == 1) return;
		ProfileInfo propInfo = (ProfileInfo)jComboProfile.getSelectedItem();
		if (propInfo.getFileName() != null) {
			File profile = new File(profilePath.getPath()+"/"+propInfo.getFileName());
			if (profile.exists()) {
				//名前を削除して正常に終了しなかった場合も追加されないようにする
				propInfo.getProperties().remove("ProfileName");
				profile.deleteOnExit();
				profile.delete();
			}
			jComboProfile.removeItemAt(jComboProfile.getSelectedIndex());
			//移動ボタン有効化
			setProfileMoveEnable();
		}
	}
	private void editProfile(String name) throws IOException
	{
		if (name == null) return;
		name = name.trim();
		if (name.length() == 0) return;
		ProfileInfo propInfo = (ProfileInfo)jComboProfile.getSelectedItem();
		Properties profileProps = propInfo.getProperties();
		//名前設定
		propInfo.setName(name);
		profileProps.setProperty("ProfileName", name);
		//画面の設定をPropertiesに設定
		this.setProperties(profileProps);
		//プロファイル更新
		FileOutputStream fos = new FileOutputStream(profilePath.getPath()+"/"+propInfo.getFileName());
		profileProps.store(fos, "AozoraEpub3 Profile");
		fos.close();
		jComboProfile.repaint();
	}
	
	/** 移動ボタン有効化 */
	private void setProfileMoveEnable()
	{
		jButtonProfileUp.setEnabled(jComboProfile.getSelectedIndex() > 0); 
		jButtonProfileDown.setEnabled(jComboProfile.getSelectedIndex() < jComboProfile.getItemCount()-1);
	}
	
	/** propsの値をアプレットに設定 */
	private void loadProperties(Properties props)
	{
		boolean selected;
		
		//表題
		try { jComboTitle.setSelectedIndex(Integer.parseInt(props.getProperty("TitleType"))); } catch (Exception e) {}
		setPropsSelected(jCheckPubFirst, props, "PubFirst");
		setPropsSelected(jCheckUseFileName, props, "UseFileName");
		//表紙
		if (props.getProperty("Cover")==null||props.getProperty("Cover").length()==0) jComboCover.setSelectedIndex(0);
		else jComboCover.setSelectedItem(props.getProperty("Cover"));
		//表紙履歴
		setPropsSelected(jCheckCoverHistory, props, "CoverHistory");
		//有効行数
		setPropsIntText(jTextMaxCoverLine, props, "MaxCoverLine");
		
		setPropsSelected(jCheckCoverPage, props, "CoverPage");
		//表題ページ
		setPropsSelected(jCheckTitlePage, props, "TitlePageWrite");
		String propValue = props.getProperty("TitlePage");
		if (propValue != null) {
			jRadioTitleNormal.setSelected(Integer.toString(BookInfo.TITLE_NORMAL).equals(propValue));
			jRadioTitleMiddle.setSelected(Integer.toString(BookInfo.TITLE_MIDDLE).equals(propValue));
			jRadioTitleHorizontal.setSelected(Integer.toString(BookInfo.TITLE_HORIZONTAL).equals(propValue));
		}
		
		setPropsSelected(jCheckTocPage, props, "TocPage");
		selected = setPropsSelected(jRadioTocV, props, "TocVertical");
		jRadioTocH.setSelected(!selected);
		//出力ファイル設定
		//拡張子
		if (props.getProperty("Ext") != null && props.getProperty("Ext").length() > 0)
			jComboExt.setSelectedItem(props.getProperty("Ext"));
		//出力ファイル名設定
		setPropsSelected(jCheckAutoFileName, props, "AutoFileName");
		//ファイルの上書き許可
		setPropsSelected(jCheckOverWrite, props, "OverWrite");
		//栞用ID出力
		setPropsSelected(jCheckMarkId, props, "MarkId");
		//4バイト文字を変換する
		//setPropsSelected(jCheckGaiji32, props, "Gaiji32");
		//縦書き横書き
		selected= setPropsSelected(jRadioVertical, props, "Vertical");
		jRadioHorizontal.setSelected(!selected);
		//入力文字コード
		try { jComboEncType.setSelectedIndex(Integer.parseInt(props.getProperty("EncType"))); } catch (Exception e) {}
		
		////////////////////////////////////////////////////////////////
		//画像設定
		setPropsSelected(jCheckNoIllust, props, "NoIllust");
		//画面サイズ
		setPropsIntText(jTextDispW, props, "DispW");
		setPropsIntText(jTextDispH, props, "DispH");
		//表紙サイズ
		setPropsIntText(jTextCoverW, props, "CoverW");
		setPropsIntText(jTextCoverH, props, "CoverH");
		//画像単ページ化
		setPropsIntText(jTextSinglePageSizeW, props, "SinglePageSizeW");
		setPropsIntText(jTextSinglePageSizeH, props, "SinglePageSizeH");
		//横のみ
		setPropsIntText(jTextSinglePageWidth, props, "SinglePageWidth");
		//サイズ指定
		propValue = props.getProperty("ImageSizeType");
		if (propValue != null) {
			jRadioImageSizeType1.setSelected(Integer.toString(SectionInfo.IMAGE_SIZE_TYPE_AUTO).equals(propValue));
			jRadioImageSizeType3.setSelected(Integer.toString(SectionInfo.IMAGE_SIZE_TYPE_ASPECT).equals(propValue));
		}
		//拡大しない
		setPropsSelected(jCheckFitImage, props, "FitImage");
		//SVG画像タグ出力
		setPropsSelected(jCheckSvgImage, props, "SvgImage");
		try { jComboRotateImage.setSelectedIndex(Integer.parseInt(props.getProperty("RotateImage"))); } catch (Exception e) {}
		//画像倍率
		setPropsSelected(jCheckImageScale, props, "ImageScaleChecked", false);
		setPropsFloatText(jTextImageScale, props, "ImageScale");
		//画像回り込み
		setPropsSelected(jCheckImageFloat, props, "ImageFloat");
		setPropsIntText(jTextImageFloatW, props, "ImageFloatW");
		setPropsIntText(jTextImageFloatH, props, "ImageFloatH");
		try { jComboImageFloatType.setSelectedIndex(Integer.parseInt(props.getProperty("ImageFloatType"))); } catch (Exception e) {}
		//画像縮小指定
		setPropsSelected(jCheckResizeW, props, "ResizeW");
		setPropsIntText(jTextResizeNumW, props, "ResizeNumW");
		setPropsSelected(jCheckResizeH, props, "ResizeH");
		setPropsIntText(jTextResizeNumH, props, "ResizeNumH");
		//Float表示 (デフォルトOFF)
		setPropsSelected(jCheckImageFloatPage, props, "ImageFloatPage", false);
		setPropsSelected(jCheckImageFloatBlock, props, "ImageFloatBlock", false);
		//Jpeg圧縮率
		setPropsIntText(jTextJpegQuality, props, "JpegQuality");
		//ガンマ補正
		setPropsSelected(jCheckGamma, props, "Gamma");
		setPropsFloatText(jTextGammaValue, props, "GammaValue");
		//余白除去
		setPropsSelected(jCheckAutoMargin, props, "AutoMargin");
		setPropsIntText(jTextAutoMarginLimitH, props, "AutoMarginLimitH");
		setPropsIntText(jTextAutoMarginLimitV, props, "AutoMarginLimitV");
		setPropsIntText(jTextAutoMarginWhiteLevel, props, "AutoMarginWhiteLevel");
		setPropsFloatText(jTextAutoMarginPadding, props, "AutoMarginPadding");
		try { jComboAutoMarginNombre.setSelectedIndex(Integer.parseInt(props.getProperty("AutoMarginNombre"))); } catch (Exception e) {}
		setPropsFloatText(jTextAutoMarginNombreSize, props, "AutoMarginNombreSize");
		
		////////////////////////////////////////////////////////////////
		//詳細設定
		propValue = props.getProperty("SpaceHyphenation");
		if (propValue != null) {
			jRadioSpaceHyp1.setSelected("1".equals(propValue));
			jRadioSpaceHyp2.setSelected("2".equals(propValue));
			jRadioSpaceHyp0.setSelected("0".equals(propValue));
		}
		//注記のルビ表示
		propValue = props.getProperty("ChukiRuby");
		if (propValue != null) {
			jRadioChukiRuby0.setSelected("0".equals(propValue));
			jRadioChukiRuby1.setSelected("1".equals(propValue));
			jRadioChukiRuby2.setSelected("2".equals(propValue));
		}
		//自動縦中横
		//半角2文字縦書き
		setPropsSelected(jCheckAutoYoko, props, "AutoYoko");
		//半角数字1文字縦書き
		setPropsSelected(jCheckAutoYokoNum1, props, "AutoYokoNum1");
		//半角数字3文字縦書き
		setPropsSelected(jCheckAutoYokoNum3, props, "AutoYokoNum3");
		//!? 1文字
		setPropsSelected(jCheckAutoEQ1, props, "AutoYokoEQ1");
		//コメント出力
		setPropsSelected(jCheckCommentPrint, props, "CommentPrint");
		setPropsSelected(jCheckCommentConvert, props, "CommentConvert");
		//空行除去
		try { jComboxRemoveEmptyLine.setSelectedIndex(Integer.parseInt(props.getProperty("RemoveEmptyLine"))); } catch (Exception e) {}
		propValue = props.getProperty("MaxEmptyLine");
		try { jComboxMaxEmptyLine.setSelectedIndex(Integer.parseInt(propValue)); } catch (Exception e) {}
		//行頭字下げ追加
		setPropsSelected(jCheckForceIndent, props, "ForceIndent");
		//強制改ページ
		setPropsSelected(jCheckPageBreak, props, "PageBreak");
		try { jTextPageBreakSize.setText(Integer.toString(Integer.parseInt(props.getProperty("PageBreakSize")))); } catch (Exception e) {}
		setPropsSelected(jCheckPageBreakEmpty, props, "PageBreakEmpty");
		propValue = props.getProperty("PageBreakEmptyLine");
		if (propValue != null) jComboxPageBreakEmptyLine.setSelectedItem(propValue);
		setPropsIntText(jTextPageBreakEmptySize, props, "PageBreakEmptySize");
		setPropsSelected(jCheckPageBreakChapter, props, "PageBreakChapter");
		setPropsIntText(jTextPageBreakChapterSize, props, "PageBreakChapterSize");
		
		////////////////////////////////////////////////////////////////
		//目次設定
		//最大文字数
		setPropsIntText(jTextMaxChapterNameLength, props, "MaxChapterNameLength");
		//表紙
		setPropsSelected(jCheckCoverPageToc, props, "CoverPageToc");
		setPropsSelected(jCheckTitleToc, props, "TitleToc");
		setPropsSelected(jCheckChapterUseNextLine, props, "ChapterUseNextLine");
		setPropsSelected(jCheckChapterExclude, props, "ChapterExclude");
		//目次階層化
		setPropsSelected(jCheckNavNest, props, "NavNest");
		setPropsSelected(jCheckNcxNest, props, "NcxNest");
		//改ページ後を目次に追加
		setPropsSelected(jCheckChapterSection, props, "ChapterSection");
		//見出し注記
		setPropsSelected(jCheckChapterH, props, "ChapterH");
		setPropsSelected(jCheckChapterH1, props, "ChapterH1");
		setPropsSelected(jCheckChapterH2, props, "ChapterH2");
		setPropsSelected(jCheckChapterH3, props, "ChapterH3");
		setPropsSelected(jCheckSameLineChapter, props, "SameLineChapter");
		//章番号、数字、パターン
		setPropsSelected(jCheckChapterName, props, "ChapterName");
		setPropsSelected(jCheckChapterNumOnly, props, "ChapterNumOnly");
		setPropsSelected(jCheckChapterNumTitle, props, "ChapterNumTitle");
		setPropsSelected(jCheckChapterNumParen, props, "ChapterNumParen");
		setPropsSelected(jCheckChapterNumParenTitle, props, "ChapterNumParenTitle");
		setPropsSelected(jCheckChapterPattern, props, "ChapterPattern");
		if (props.containsKey("ChapterPatternText")) jComboChapterPattern.setSelectedItem(props.getProperty("ChapterPatternText"));
		
		////////////////////////////////////////////////////////////////
		//スタイル
		propValue = props.getProperty("PageMargin");
		if (propValue != null) {
			String[] pageMargins = propValue.split(",");
			for (int i=0; i<pageMargins.length; i++) jTextPageMargins[i].setText(pageMargins[i]);
		}
		propValue = props.getProperty("PageMarginUnit");
		if (propValue != null) {
			jRadioPageMarginUnit0.setSelected("0".equals(propValue));
			jRadioPageMarginUnit1.setSelected("1".equals(propValue));
			//jRadioPageMarginUnit2.setSelected("2".equals(propValue));
		}
		propValue = props.getProperty("BodyMargin");
		if (propValue != null) {
			String[] bodyMargins = propValue.split(",");
			for (int i=0; i<bodyMargins.length; i++) jTextBodyMargins[i].setText(bodyMargins[i]);
		}
		propValue = props.getProperty("BodyMarginUnit");
		if (propValue != null) {
			jRadioBodyMarginUnit0.setSelected("0".equals(propValue));
			jRadioBodyMarginUnit1.setSelected("1".equals(propValue));
			//jRadioBodyMarginUnit2.setSelected("2".equals(propValue));
		}
		propValue = props.getProperty("LineHeight");
		if (propValue != null && !"".equals(propValue)) jComboLineHeight.setSelectedItem(propValue);
		propValue = props.getProperty("FontSize");
		if (propValue != null && !"".equals(propValue)) jComboFontSize.setSelectedItem(propValue);
		setPropsSelected(jCheckBoldUseGothic, props, "BoldUseGothic");
		setPropsSelected(jCheckGothicUseBold, props, "GothicUseBold");
		
		//文字
		propValue = props.getProperty("DakutenType");
		if (propValue != null) {
			jRadioDakutenType0.setSelected("0".equals(propValue));
			jRadioDakutenType1.setSelected("1".equals(propValue));
			jRadioDakutenType2.setSelected("2".equals(propValue));
		}
		setPropsSelected(jCheckIvsBMP, props, "IvsBMP");
		setPropsSelected(jCheckIvsSSP, props, "IvsSSP");
		
		////////////////////////////////////////////////////////////////
		//Web
		setPropsFloatText(jTextWebInterval, props, "WebInterval");
		setPropsText(jTextCachePath, props, "CachePath");
		if ("".equals(jTextCachePath.getText())) jTextCachePath.setText(".cache");
		setPropsNumberText(jTextWebModifiedExpire, props, "WebModifiedExpire");
		setPropsSelected(jCheckWebConvertUpdated, props, "WebConvertUpdated");
		setPropsSelected(jCheckWebModifiedOnly, props, "WebModifiedOnly");
		setPropsSelected(jCheckWebModifiedTail, props, "WebModifiedTail");
		setPropsSelected(jCheckWebBeforeChapter, props, "WebBeforeChapter");
		setPropsIntText(jTextWebBeforeChapterCount, props, "WebBeforeChapterCount");
	}
	
	/** アプレットの設定状態をpropsに保存 */
	private void setProperties(Properties props)
	{
		//アップレット設定の保存
		props.setProperty("TitleType", ""+this.jComboTitle.getSelectedIndex());
		props.setProperty("PubFirst", this.jCheckPubFirst.isSelected()?"1":"");
		props.setProperty("UseFileName", this.jCheckUseFileName.isSelected()?"1":"");
		props.setProperty("AutoFileName", this.jCheckAutoFileName.isSelected()?"1":"");
		//変換設定
		props.setProperty("MarkId", this.jCheckMarkId.isSelected()?"1":"");
		//props.setProperty("Gaiji32", this.jCheckGaiji32.isSelected()?"1":"");
		props.setProperty("Vertical", this.jRadioVertical.isSelected()?"1":"");
		//props.setProperty("RtL", this.jRadioRtL.isSelected()?"1":"");
		props.setProperty("Ext", ""+this.jComboExt.getEditor().getItem().toString().trim());
		props.setProperty("ChkConfirm", this.jCheckConfirm.isSelected()?"1":"");
		
		//先頭の挿絵と表紙無しのみ記憶
		if (this.jComboCover.getSelectedIndex() == 0) props.setProperty("Cover","");
		else if (this.jComboCover.getSelectedIndex() == 1) props.setProperty("Cover", ""+this.jComboCover.getEditor().getItem().toString().trim());
		props.setProperty("CoverHistory", this.jCheckCoverHistory.isSelected()?"1":"");
		
		props.setProperty("MaxCoverLine", this.jTextMaxCoverLine.getText());
		
		//ページ出力
		props.setProperty("CoverPage", this.jCheckCoverPage.isSelected()?"1":"");
		
		props.setProperty("TitlePageWrite", this.jCheckTitlePage.isSelected()?"1":"");
		if (this.jRadioTitleNormal.isSelected()) {
			props.setProperty("TitlePage", ""+BookInfo.TITLE_NORMAL);
		} else if (this.jRadioTitleMiddle.isSelected()) {
			props.setProperty("TitlePage", ""+BookInfo.TITLE_MIDDLE);
		} else if (this.jRadioTitleHorizontal.isSelected()) {
			props.setProperty("TitlePage", ""+BookInfo.TITLE_HORIZONTAL);
		}
		props.setProperty("TocPage", this.jCheckTocPage.isSelected()?"1":"");
		props.setProperty("TocVertical", this.jRadioTocV.isSelected()?"1":"");
		//挿絵非表示
		props.setProperty("NoIllust", this.jCheckNoIllust.isSelected()?"1":"");
		//画面サイズ
		props.setProperty("DispW", this.jTextDispW.getText());
		props.setProperty("DispH", this.jTextDispH.getText());
		//表紙
		props.setProperty("CoverW", this.jTextCoverW.getText());
		props.setProperty("CoverH", this.jTextCoverH.getText());
		//画像倍率
		props.setProperty("ImageScaleChecked", this.jCheckImageScale.isSelected()?"1":"");
		props.setProperty("ImageScale", this.jTextImageScale.getText());
		//画像回り込み
		props.setProperty("ImageFloat", this.jCheckImageFloat.isSelected()?"1":"");
		props.setProperty("ImageFloatType", ""+this.jComboImageFloatType.getSelectedIndex());
		props.setProperty("ImageFloatW", this.jTextImageFloatW.getText());
		props.setProperty("ImageFloatH", this.jTextImageFloatH.getText());
		//画像サイズ
		props.setProperty("ResizeW", this.jCheckResizeW.isSelected()?"1":"");
		props.setProperty("ResizeH", this.jCheckResizeH.isSelected()?"1":"");
		props.setProperty("ResizeNumW", this.jTextResizeNumW.getText());
		props.setProperty("ResizeNumH", this.jTextResizeNumH.getText());
		//画像単ページ
		props.setProperty("SinglePageSizeW", this.jTextSinglePageSizeW.getText());
		props.setProperty("SinglePageSizeH", this.jTextSinglePageSizeH.getText());
		props.setProperty("SinglePageWidth", this.jTextSinglePageWidth.getText());
		
		props.setProperty("ImageSizeType", ""+(this.jRadioImageSizeType1.isSelected()?SectionInfo.IMAGE_SIZE_TYPE_AUTO:SectionInfo.IMAGE_SIZE_TYPE_ASPECT));
		props.setProperty("FitImage", this.jCheckFitImage.isSelected()?"1":"");
		props.setProperty("SvgImage", this.jCheckSvgImage.isSelected()?"1":"");
		props.setProperty("RotateImage", ""+this.jComboRotateImage.getSelectedIndex());
		//画像回り込み
		props.setProperty("ImageFloatPage", this.jCheckImageFloatPage.isSelected()?"1":"");
		props.setProperty("ImageFloatBlock", this.jCheckImageFloatBlock.isSelected()?"1":"");
		//JPEG画質
		props.setProperty("JpegQuality", this.jTextJpegQuality.getText());
		//ガンマ補正
		props.setProperty("Gamma", this.jCheckGamma.isSelected()?"1":"");
		props.setProperty("GammaValue", this.jTextGammaValue.getText());
		//余白除去
		props.setProperty("AutoMargin", this.jCheckAutoMargin.isSelected()?"1":"");
		props.setProperty("AutoMarginLimitH", this.jTextAutoMarginLimitH.getText());
		props.setProperty("AutoMarginLimitV", this.jTextAutoMarginLimitV.getText());
		props.setProperty("AutoMarginWhiteLevel", this.jTextAutoMarginWhiteLevel.getText());
		props.setProperty("AutoMarginPadding", this.jTextAutoMarginPadding.getText());
		props.setProperty("AutoMarginNombre", ""+this.jComboAutoMarginNombre.getSelectedIndex());
		props.setProperty("AutoMarginNombreSize", this.jTextAutoMarginNombreSize.getText());
		//空白の禁則処理
		props.setProperty("SpaceHyphenation", this.jRadioSpaceHyp0.isSelected()?"0":(this.jRadioSpaceHyp1.isSelected()?"1":"2"));
		//注記のルビ表示
		props.setProperty("ChukiRuby", this.jRadioChukiRuby0.isSelected()?"0":(this.jRadioChukiRuby1.isSelected()?"1":"2"));
		//自動縦中横
		props.setProperty("AutoYoko", this.jCheckAutoYoko.isSelected()?"1":"");
		props.setProperty("AutoYokoNum1", this.jCheckAutoYokoNum1.isSelected()?"1":"");
		props.setProperty("AutoYokoNum3", this.jCheckAutoYokoNum3.isSelected()?"1":"");
		props.setProperty("AutoYokoEQ1", this.jCheckAutoEQ1.isSelected()?"1":"");
		//コメント出力
		props.setProperty("CommentPrint", this.jCheckCommentPrint.isSelected()?"1":"");
		props.setProperty("CommentConvert", this.jCheckCommentConvert.isSelected()?"1":"");
		//空行除去
		props.setProperty("RemoveEmptyLine", ""+this.jComboxRemoveEmptyLine.getSelectedIndex());
		props.setProperty("MaxEmptyLine", ""+this.jComboxMaxEmptyLine.getSelectedIndex());
		//行頭字下げ
		props.setProperty("ForceIndent", this.jCheckForceIndent.isSelected()?"1":"");
		//強制改ページ
		props.setProperty("PageBreak", this.jCheckPageBreak.isSelected()?"1":"");
		props.setProperty("PageBreakSize", ""+this.jTextPageBreakSize.getText().trim());
		props.setProperty("PageBreakEmpty", this.jCheckPageBreakEmpty.isSelected()?"1":"");
		props.setProperty("PageBreakEmptyLine", ""+this.jComboxPageBreakEmptyLine.getSelectedItem().toString().trim());
		props.setProperty("PageBreakEmptySize", ""+this.jTextPageBreakEmptySize.getText().trim());
		props.setProperty("PageBreakChapter", this.jCheckPageBreakChapter.isSelected()?"1":"");
		props.setProperty("PageBreakChapterSize", ""+this.jTextPageBreakChapterSize.getText().trim());
		//目次出力
		props.setProperty("MaxChapterNameLength", this.jTextMaxChapterNameLength.getText());
		props.setProperty("CoverPageToc", this.jCheckCoverPageToc.isSelected()?"1":"");
		props.setProperty("TitleToc", this.jCheckTitleToc.isSelected()?"1":"");
		props.setProperty("ChapterUseNextLine", this.jCheckChapterUseNextLine.isSelected()?"1":"");
		props.setProperty("ChapterExclude", this.jCheckChapterExclude.isSelected()?"1":"");
		props.setProperty("NavNest", this.jCheckNavNest.isSelected()?"1":"");
		props.setProperty("NcxNest", this.jCheckNcxNest.isSelected()?"1":"");
		props.setProperty("ChapterSection", this.jCheckChapterSection.isSelected()?"1":"");
		props.setProperty("ChapterH", this.jCheckChapterH.isSelected()?"1":"");
		props.setProperty("ChapterH1", this.jCheckChapterH1.isSelected()?"1":"");
		props.setProperty("ChapterH2", this.jCheckChapterH2.isSelected()?"1":"");
		props.setProperty("ChapterH3", this.jCheckChapterH3.isSelected()?"1":"");
		props.setProperty("SameLineChapter", this.jCheckSameLineChapter.isSelected()?"1":"");
		props.setProperty("ChapterName", this.jCheckChapterName.isSelected()?"1":"");
		props.setProperty("ChapterNumOnly", this.jCheckChapterNumOnly.isSelected()?"1":"");
		props.setProperty("ChapterNumTitle", this.jCheckChapterNumTitle.isSelected()?"1":"");
		props.setProperty("ChapterNumParen", this.jCheckChapterNumParen.isSelected()?"1":"");
		props.setProperty("ChapterNumParenTitle", this.jCheckChapterNumParenTitle.isSelected()?"1":"");
		props.setProperty("ChapterPattern", this.jCheckChapterPattern.isSelected()?"1":"");
		props.setProperty("ChapterPatternText", this.jComboChapterPattern.getEditor().getItem().toString().trim());
		//スタイル
		props.setProperty("PageMargin", this.jTextPageMargins[0].getText()+","+this.jTextPageMargins[1].getText()+","+this.jTextPageMargins[2].getText()+","+this.jTextPageMargins[3].getText());
		props.setProperty("PageMarginUnit", this.jRadioPageMarginUnit0.isSelected()?"0":(this.jRadioPageMarginUnit1.isSelected()?"1":"2"));
		props.setProperty("BodyMargin", this.jTextBodyMargins[0].getText()+","+this.jTextBodyMargins[1].getText()+","+this.jTextBodyMargins[2].getText()+","+this.jTextBodyMargins[3].getText());
		props.setProperty("BodyMarginUnit", this.jRadioBodyMarginUnit0.isSelected()?"0":(this.jRadioBodyMarginUnit1.isSelected()?"1":"2"));
		props.setProperty("LineHeight", this.jComboLineHeight.getEditor().getItem().toString().trim());
		props.setProperty("FontSize", this.jComboFontSize.getEditor().getItem().toString().trim());
		props.setProperty("BoldUseGothic", this.jCheckBoldUseGothic.isSelected()?"1":"");
		props.setProperty("GothicUseBold", this.jCheckGothicUseBold.isSelected()?"1":"");
		//文字
		props.setProperty("DakutenType", this.jRadioDakutenType0.isSelected()?"0":(this.jRadioDakutenType1.isSelected()?"1":"2"));
		props.setProperty("IvsBMP", this.jCheckIvsBMP.isSelected()?"1":"");
		props.setProperty("IvsSSP", this.jCheckIvsSSP.isSelected()?"1":"");
		
		//Web
		props.setProperty("WebInterval", this.jTextWebInterval.getText());
		props.setProperty("CachePath", this.jTextCachePath.getText());
		props.setProperty("WebModifiedExpire", this.jTextWebModifiedExpire.getText());
		props.setProperty("WebConvertUpdated", this.jCheckWebConvertUpdated.isSelected()?"1":"");
		props.setProperty("WebModifiedOnly", this.jCheckWebModifiedOnly.isSelected()?"1":"");
		props.setProperty("WebModifiedTail", this.jCheckWebModifiedTail.isSelected()?"1":"");
		props.setProperty("WebBeforeChapter", this.jCheckWebBeforeChapter.isSelected()?"1":"");
		props.setProperty("WebBeforeChapterCount", this.jTextWebBeforeChapterCount.getText());
		
		//確認ダイアログの元画像を残す
		props.setProperty("ReplaceCover", this.jConfirmDialog.jCheckReplaceCover.isSelected()?"1":"");
		
		props.setProperty("EncType", ""+this.jComboEncType.getSelectedIndex());
		props.setProperty("OverWrite", this.jCheckOverWrite.isSelected()?"1":"");
	}
	
	////////////////////////////////////////////////////////////////
	// JFrame
	////////////////////////////////////////////////////////////////
	/** Jar実行用 */
	public static void main(String args[])
	{
		//LookAndFeel変更
		try {
			String lafName = UIManager.getSystemLookAndFeelClassName();
			//lafName = "";
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
		final JFrame jFrame = new JFrame("AozoraEpub3");
		//アップレット生成と初期化
		final AozoraEpub3Applet applet = new AozoraEpub3Applet(jFrame);
		applet.iconImage = java.awt.Toolkit.getDefaultToolkit().createImage(AozoraEpub3Applet.class.getResource("images/icon.png"));
		applet.init();
		
		//アイコン設定
		jFrame.setIconImage(applet.iconImage);
		//最小サイズ
		jFrame.setMinimumSize(new Dimension(520, 320));
		jFrame.setPreferredSize(new Dimension(520, 400));
		
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
				applet.startConvertWorker(vecFiles, null, null, null);
			}
		}
		
		//Focus
		applet.jTabbedPane.requestFocus();
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
		
		//Spliterの位置
		this.props.setProperty("DividerLocation", ""+this.jSplitPane.getDividerLocation());
		
		//選択中のプロファイル
		ProfileInfo propInfo = (ProfileInfo)jComboProfile.getSelectedItem();
		this.props.setProperty("ProfileFileName", propInfo.getFileName());
		//プロファイルの順番を記憶
		try {
			StringBuilder propList = new StringBuilder();
			for (int i=0; i<this.jComboProfile.getItemCount(); i++) {
				propInfo = (ProfileInfo)this.jComboProfile.getItemAt(i);
				propList.append(",").append(propInfo.getFileName());
			}
			propList.deleteCharAt(0);
			this.props.setProperty("ProfileList", propList.toString());
		} catch (Exception e) { e.printStackTrace(); }
		//出力先と履歴保存
		try {
			this.props.setProperty("SamePath", this.jCheckSamePath.isSelected()?"1":"");
			String dstPath = this.jComboDstPath.getEditor().getItem().toString().trim();
			if (dstPath.equals("") && jComboDstPath.getSelectedItem() != null) dstPath = this.jComboDstPath.getSelectedItem().toString().trim();
			this.props.setProperty("DstPath", ""+dstPath);
			//履歴
			String dstPathList = this.props.getProperty("DstPathList");
			if (dstPathList == null) dstPathList = dstPath;
			else {
				//最大10件
				dstPathList = dstPath;
				int count = Math.min(10, this.jComboDstPath.getItemCount());
				for (int i=0; i<count; i++) {
					String item = (String)this.jComboDstPath.getItemAt(i);
					if (!dstPath.equals(item)) dstPathList += ","+item;
				}
				if (dstPathList.startsWith(",")) dstPathList = dstPathList.substring(1);
			}
			this.props.setProperty("DstPathList", dstPathList);
		} catch (Exception e) { e.printStackTrace(); }
		this.props.setProperty("LastDir", this.currentPath==null?"":this.currentPath.getAbsolutePath());
		
		//アプレットの設定をPropertiesに反映
		this.setProperties(this.props);
		
		//設定ファイル更新
		FileOutputStream fos = new FileOutputStream(this.jarPath+this.propFileName);
		this.props.store(fos, "AozoraEpub3 Parameters");
		fos.close();
		
		super.finalize();
	}
	
	////////////////////////////////////////////////////////////////
	//変換履歴
	////////////////////////////////////////////////////////////////
	/** 変換履歴格納用 最大255件 */
	LinkedHashMap<String, BookInfoHistory> mapBookInfoHistory = new LinkedHashMap<String, BookInfoHistory>(){
		private static final long serialVersionUID = 1L;
		@SuppressWarnings("rawtypes")
		protected boolean removeEldestEntry(Map.Entry eldest) { return size() > 256; }
	};
	
	//以前の変換情報取得
	BookInfoHistory getBookInfoHistory(BookInfo bookInfo)
	{
		String key = bookInfo.srcFile.getAbsolutePath();
		if (bookInfo.textEntryName != null) key += "/"+bookInfo.textEntryName;
		return mapBookInfoHistory.get(key);
	}
	
	void setBookInfoHistory(BookInfo bookInfo)
	{
		String key = bookInfo.srcFile.getAbsolutePath();
		if (bookInfo.textEntryName != null) key += "/"+bookInfo.textEntryName;
		mapBookInfoHistory.put(key, new BookInfoHistory(bookInfo));
	}
	
}
