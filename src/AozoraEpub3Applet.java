import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.swing.JCoverImagePanel;
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
	JDialog jDialogConfirm;
	/** 入力ファイル名 */
	JTextField jTextSrcFileName;
	/** 出力ファイル名 */
	JTextField jTextDstFileName;
	/** 表題 (+副題)編集用 */
	JTextField jTextTitle;
	/** 著者名編集用 */
	JTextField jTextCreator;
	JCoverImagePanel jCoverImagePanel;
	
	/** 設定ダイアログ */
	JDialog jDialogSetting;
	
	JComboBox jComboTitle;
	JCheckBox jCheckUserFileName;
	JCheckBox jCheckAutoFileName;
	JCheckBox jCheckMiddleTitle;
	
	JCheckBox jCheckConfirm;
	JCheckBox jCheckConfirm2;
	
	JComboBox jComboDstPath;
	JComboBox jComboExt;
	JCheckBox jCheckMarkId;
	
	JCheckBox jCheckOverWrite;
	JCheckBox jCheckAutoYoko;
	JCheckBox jCheckGaiji32;
	
	JRadioButton jRadioVertical;
	JRadioButton jRadioHorizontal;
	
	JRadioButton jRadioLtR;
	JRadioButton jRadioRtL;
	
	//画像リサイズ
	JCheckBox jCheckResizeW;
	JTextField jTextResizeW;
	JCheckBox jCheckResizeH;
	JTextField jTextResizeH;
	JCheckBox jCheckPixel;
	JTextField jTextPixelW;
	JTextField jTextPixelH;
	
	//入力ファイルエンコード
	JComboBox jComboEncType;
	
	//JComboBox jComboxPageBreak;
	//JComboBox jComboxPageBreakEmpty;
	
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
	
	JScrollPane jScrollPane;
	JTextArea jTextArea;
	
	/** 青空→ePub3変換クラス */
	AozoraEpub3Converter aozoraConverter;
	
	/** ePub3出力クラス */
	Epub3Writer epub3Writer;
	
	/** ePub3画像出力クラス */
	Epub3Writer epub3ImageWriter;
	
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
	
	File currentPath = null;
	
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
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) { e.printStackTrace(); }
		
		String propValue;
		JPanel panel;
		JLabel label;
		JButton jButton;
		Border zeroPadding = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border padding4 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		Border padding4H = BorderFactory.createEmptyBorder(0, 4, 0, 4);
		Border padding9H = BorderFactory.createEmptyBorder(0, 9, 0, 9);
		Border padding3 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		//アップレットのレイアウト設定
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		this.setMinimumSize(new Dimension(640, 320));
		
		////////////////////////////////
		//表題
		////////////////////////////////
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 26));
		panel.setBorder(padding4H);
		this.add(panel);
		
		//表題行
		label = new JLabel("表題: 本文内");
		panel.add(label);
		jComboTitle = new JComboBox(BookInfo.TitleType.titleTypeNames);
		jComboTitle.setFocusable(false);
		jComboTitle.setPreferredSize(new Dimension(130, 22));
		try { jComboTitle.setSelectedIndex(Integer.parseInt(props.getProperty("TitleType"))); } catch (Exception e) {}
		((JLabel)jComboTitle.getRenderer()).setBorder(zeroPadding);
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
		panel.setPreferredSize(new Dimension(1920, 26));
		panel.setMaximumSize(new Dimension(1920, 26));
		panel.setBorder(padding9H);
		this.add(panel);
		//表紙
		label = new JLabel("表紙: ");
		panel.add(label);
		propValue = props.getProperty("Cover");
		jComboCover = new JComboBox(new String[]{"[先頭の挿絵]", "[入力ファイル名と同じ画像(png,jpg)]", "[表紙無し]", "http://"});
		jComboCover.setEditable(true);
		if (propValue==null||propValue.length()==0) jComboCover.setSelectedIndex(0);
		else jComboCover.setSelectedItem(propValue);
		jComboCover.setMaximumSize(new Dimension(1920, 22));
		panel.add(jComboCover);
		new DropTarget(jComboCover.getEditor().getEditorComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new DropCoverListener(), true);
		jButtonCover = new JButton("選択");
		jButtonCover.setBorder(padding3);
		jButtonCover.setPreferredSize(new Dimension(60, 24));
		jButtonCover.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/cover.png")));
		jButtonCover.setFocusable(false);
		jButtonCover.addActionListener(new CoverChooserListener(this));
		panel.add(jButtonCover);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(1920, 26));
		panel.setMaximumSize(new Dimension(1920, 26));
		panel.setBorder(padding9H);
		this.add(panel);
		//ページ出力
		label = new JLabel("ページ出力:");
		panel.add(label);
		propValue = props.getProperty("CoverPage");
		jCheckCoverPage = new JCheckBox("表紙", propValue==null||"1".equals(propValue));
		jCheckCoverPage.setFocusPainted(false);
		panel.add(jCheckCoverPage);
		//左右中央
		propValue = props.getProperty("MiddleTitle");
		jCheckMiddleTitle = new JCheckBox("表題左右中央", propValue==null||"1".equals(propValue));
		jCheckMiddleTitle.setFocusPainted(false);
		panel.add(jCheckMiddleTitle);
		propValue = props.getProperty("TocPage");
		jCheckTocPage = new JCheckBox("目次", propValue!=null&"1".equals(propValue));
		jCheckTocPage.setFocusPainted(false);
		panel.add(jCheckTocPage);
		label = new JLabel("(");
		panel.add(label);
		ButtonGroup buttonGroup = new ButtonGroup();
		propValue = props.getProperty("TocVertical");
		jRadioTocV = new JRadioButton("縦書き", propValue==null||"1".equals(propValue));
		jRadioTocV.setFocusPainted(false);
		jRadioTocV.setBorder(zeroPadding);
		panel.add(jRadioTocV);
		buttonGroup.add(jRadioTocV);
		jRadioTocH = new JRadioButton("横書き", !(propValue==null||"1".equals(propValue)));
		jRadioTocH.setFocusPainted(false);
		jRadioTocH.setBorder(zeroPadding);
		panel.add(jRadioTocH);
		buttonGroup.add(jRadioTocH);
		label = new JLabel(")");
		panel.add(label);
		
		////////////////////////////////
		//出力ファイル設定
		////////////////////////////////
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 26));
		panel.setBorder(padding4H);
		this.add(panel);
		//拡張子
		label = new JLabel("拡張子");
		panel.add(label);
		propValue = props.getProperty("Ext");
		jComboExt = new JComboBox(new String[]{".epub", ".kepub.epub"});
		jComboExt.setEditable(true);
		jComboExt.setPreferredSize(new Dimension(90, 22));
		jComboExt.setSelectedItem(propValue==null||propValue.length()==0?".epub":propValue);
		panel.add(jComboExt);
		//出力ファイル名設定
		propValue = props.getProperty("AutoFileName");
		jCheckAutoFileName = new JCheckBox("出力ファイル名に表題利用", propValue==null||"1".equals(propValue));
		jCheckAutoFileName.setFocusPainted(false);
		panel.add(jCheckAutoFileName);
		//ファイルの上書き許可
		propValue = props.getProperty("OverWrite");
		jCheckOverWrite = new JCheckBox("ePubファイル上書き", propValue==null||"1".equals(propValue));
		jCheckOverWrite.setFocusPainted(false);
		panel.add(jCheckOverWrite);
		
		////////////////////////////////
		//出力先
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(1920, 26));
		panel.setMaximumSize(new Dimension(1920, 26));
		panel.setBorder(padding9H);
		this.add(panel);
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
		jComboDstPath.setMaximumSize(new Dimension(1920, 22));
		propValue = props.getProperty("DstPath");
		if (propValue==null||propValue.length()==0) jComboDstPath.setSelectedIndex(0);
		else jComboDstPath.setSelectedItem(propValue);
		panel.add(jComboDstPath);
		new DropTarget(jComboDstPath.getEditor().getEditorComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new DropDstPathListener(), true);
		jButtonDstPath = new JButton("選択");
		jButtonDstPath.setBorder(padding3);
		jButtonDstPath.setPreferredSize(new Dimension(56, 24));
		jButtonDstPath.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/dst_path.png")));
		jButtonDstPath.setFocusable(false);
		jButtonDstPath.addActionListener(new PathChooserListener(this));
		panel.add(jButtonDstPath);
		
		////////////////////////////////
		//画像サイズ指定
		////////////////////////////////
		ChangeListener resizeChangeLister = new ChangeListener() {
			public void stateChanged(ChangeEvent e) { setResizeTextEnabled(true);  }
		};
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMinimumSize(new Dimension(480, 24));
		panel.setMaximumSize(new Dimension(1920, 24));
		panel.setBorder(padding4H);
		this.add(panel);
		label = new JLabel("画像縮小: ");
		panel.add(label);
		//横
		propValue = props.getProperty("ResizeW");
		jCheckResizeW = new JCheckBox("横", propValue!=null&&"1".equals(propValue));
		jCheckResizeW.setFocusPainted(false);
		jCheckResizeW.setBorder(zeroPadding);
		jCheckResizeW.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeW);
		propValue = props.getProperty("ResizeNumW");
		jTextResizeW = new JTextField(propValue==null?"1600":propValue);
		jTextResizeW.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeW.setInputVerifier(new IntegerInputVerifier());
		jTextResizeW.setPreferredSize(new Dimension(35, 20));
		jTextResizeW.setEditable(jCheckResizeW.isSelected());
		panel.add(jTextResizeW);
		label = new JLabel("以下  ");
		label.setBorder(zeroPadding);
		panel.add(label);
		//縦
		propValue = props.getProperty("ResizeH");
		jCheckResizeH = new JCheckBox("縦", propValue!=null&"1".equals(propValue));
		jCheckResizeH.setFocusPainted(false);
		jCheckResizeH.setBorder(zeroPadding);
		jCheckResizeH.addChangeListener(resizeChangeLister);
		panel.add(jCheckResizeH);
		propValue = props.getProperty("ResizeNumH");
		jTextResizeH = new JTextField(propValue==null?"1600":propValue);
		jTextResizeH.setHorizontalAlignment(JTextField.RIGHT);
		jTextResizeH.setInputVerifier(new IntegerInputVerifier());
		jTextResizeH.setPreferredSize(new Dimension(35, 20));
		panel.add(jTextResizeH);
		label = new JLabel("以下  ");
		label.setBorder(zeroPadding);
		panel.add(label);
		//ピクセル
		//縦
		propValue = props.getProperty("Pixel");
		jCheckPixel = new JCheckBox("画素数", propValue==null||"1".equals(propValue));
		jCheckPixel.setFocusPainted(false);
		jCheckPixel.setBorder(zeroPadding);
		jCheckPixel.addChangeListener(resizeChangeLister);
		panel.add(jCheckPixel);
		propValue = props.getProperty("PixelW");
		jTextPixelW = new JTextField(propValue==null?"1600":propValue);
		jTextPixelW.setHorizontalAlignment(JTextField.RIGHT);
		jTextPixelW.setInputVerifier(new IntegerInputVerifier());
		jTextPixelW.setPreferredSize(new Dimension(35, 20));
		panel.add(jTextPixelW);
		label = new JLabel("x");
		label.setBorder(zeroPadding);
		panel.add(label);
		propValue = props.getProperty("PixelH");
		jTextPixelH = new JTextField(propValue==null?"1600":propValue);
		jTextPixelH.setHorizontalAlignment(JTextField.RIGHT);
		jTextPixelH.setInputVerifier(new IntegerInputVerifier());
		jTextPixelH.setPreferredSize(new Dimension(35, 20));
		panel.add(jTextPixelH);
		label = new JLabel("以下");
		label.setBorder(zeroPadding);
		panel.add(label);
		this.setResizeTextEnabled(true);
		
		////////////////////////////////
		//変換オプション
		////////////////////////////////
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMinimumSize(new Dimension(480, 24));
		panel.setMaximumSize(new Dimension(1920, 24));
		panel.setBorder(padding4H);
		this.add(panel);
		//栞用ID出力
		propValue = props.getProperty("MarkId");
		jCheckMarkId = new JCheckBox("栞用ID出力", propValue==null||"1".equals(propValue));
		jCheckMarkId.setFocusPainted(false);
		panel.add(jCheckMarkId);
		//半角2文字縦書き
		propValue = props.getProperty("AutoYoko");
		jCheckAutoYoko = new JCheckBox("自動縦中横", propValue==null||"1".equals(propValue));
		jCheckAutoYoko.setFocusPainted(false);
		panel.add(jCheckAutoYoko);
		//4バイト文字を変換する
		propValue = props.getProperty("Gaiji32");
		jCheckGaiji32 = new JCheckBox("4バイト文字変換", "1".equals(propValue));
		jCheckGaiji32.setFocusPainted(false);
		panel.add(jCheckGaiji32);
		//縦書き横書き
		label = new JLabel("  ");
		panel.add(label);
		buttonGroup = new ButtonGroup();
		propValue = props.getProperty("Vertical");
		jRadioVertical = new JRadioButton("縦書き", propValue==null||"1".equals(propValue));
		jRadioVertical.setFocusPainted(false);
		panel.add(jRadioVertical);
		buttonGroup.add(jRadioVertical);
		jRadioHorizontal = new JRadioButton("横書き", !(propValue==null||"1".equals(propValue)));
		jRadioHorizontal.setFocusPainted(false);
		panel.add(jRadioHorizontal);
		buttonGroup.add(jRadioHorizontal);
		
		////////////////////////////////
		//自動改ページ
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
		//3段目
		/*panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 26));
		panel.setBorder(panelPadding);
		this.add(panel);
		//入力文字コード
		label = new JLabel(" 自動改ページ : ");
		panel.add(label);
		jComboxPageBreak = new JComboBox(new String[]{"しない", "50", "100", "200", "300", "400", "500", "750", "1000"});
		jComboxPageBreak.setFocusable(false);
		jComboxPageBreak.setPreferredSize(new Dimension(60, 22));
		((JLabel)jComboxPageBreak.getRenderer()).setBorder(panelPadding);
		propValue = props.getProperty("PageBreak");
		jComboxPageBreak.setSelectedItem(propValue==null?"500":propValue);
		panel.add(jComboxPageBreak);
		label = new JLabel("行以上で 空行が");
		panel.add(label);
		jComboxPageBreakEmpty = new JComboBox(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
		jComboxPageBreakEmpty.setFocusable(false);
		jComboxPageBreakEmpty.setPreferredSize(new Dimension(30, 22));
		((JLabel)jComboxPageBreakEmpty.getRenderer()).setBorder(panelPadding);
		propValue = props.getProperty("PageBreakEmpty");
		jComboxPageBreakEmpty.setSelectedItem(propValue==null?"2":propValue);
		panel.add(jComboxPageBreakEmpty);
		label = new JLabel("行の場合");
		panel.add(label);
		*/
		
		////////////////////////////////
		//変換
		////////////////////////////////
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(1920, 34));
		panel.setMinimumSize(new Dimension(1920, 26));
		panel.setMaximumSize(new Dimension(1920, 26));
		panel.setBorder(zeroPadding);
		this.add(panel);
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel1.setPreferredSize(new Dimension(1920, 24));
		panel1.setBorder(padding4H);
		//入力文字コード
		label = new JLabel("入力文字コード");
		panel1.add(label);
		jComboEncType = new JComboBox(new String[]{"MS932", "UTF-8"});
		jComboEncType.setFocusable(false);
		jComboEncType.setPreferredSize(new Dimension(70, 22));
		((JLabel)jComboEncType.getRenderer()).setBorder(zeroPadding);
		try { jComboEncType.setSelectedIndex(Integer.parseInt(props.getProperty("EncType"))); } catch (Exception e) {}
		panel1.add(jComboEncType);
		panel.add(panel1);
		//右パネル
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel2.setPreferredSize(new Dimension(1920, 24));
		panel2.setBorder(padding4H);
		//開く
		jButtonFile = new JButton("ファイル選択");
		jButtonFile.setBorder(padding3);
		jButtonFile.setPreferredSize(new Dimension(90, 24));
		jButtonFile.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/convert.png")));
		jButtonFile.setFocusable(false);
		jButtonFile.addActionListener(new FileChooserListener(this));
		panel2.add(jButtonFile);
		//変換前に確認する
		propValue = props.getProperty("ChkConfirm");
		jCheckConfirm = new JCheckBox("変換前確認", propValue==null||"1".equals(propValue));
		jCheckConfirm.setFocusPainted(false);
		panel2.add(jCheckConfirm);
		panel.add(panel2);
		
		////////////////////////////////
		//テキストエリア
		jTextArea = new JTextArea("青空文庫テキストをここにドラッグ＆ドロップまたは「ファイル選択」で変換します。\n");
		jTextArea.setEditable(false);
		jTextArea.setFont(new Font("Default", Font.PLAIN, 12));
		jTextArea.setBorder(new LineBorder(Color.white, 3));
		new DropTarget(jTextArea, DnDConstants.ACTION_COPY_OR_MOVE, new DropListener(), true);
		
		jScrollPane = new JScrollPane(jTextArea);
		this.add(jScrollPane);
		
		
		////////////////////////////////////////////////////////////////
		//確認ダイアログ
		jDialogConfirm = new JDialog();
		jDialogConfirm.setIconImage(iconImage);
		jDialogConfirm.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		jDialogConfirm.setSize(new Dimension(520, 280));
		jDialogConfirm.setResizable(false);
		jDialogConfirm.setTitle("変換前確認");
		jDialogConfirm.setLayout(new BoxLayout(jDialogConfirm.getContentPane(), BoxLayout.Y_AXIS));
		jDialogConfirm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) { convertCanceled = true; }
		});
		Border titlePadding4 = BorderFactory.createEmptyBorder(0, 4, 4, 4);
		
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		dialogPanel.setBorder(padding4);
		jDialogConfirm.add(dialogPanel);
		
		//入出力ファイル情報とプレビューを横に並べる
		JPanel previewOuter = new JPanel();
		previewOuter.setLayout(new BoxLayout(previewOuter, BoxLayout.X_AXIS));
		dialogPanel.add(previewOuter);
		JPanel previewOuterLeft = new JPanel();
		previewOuterLeft.setLayout(new BoxLayout(previewOuterLeft, BoxLayout.Y_AXIS));
		previewOuterLeft.setBorder(padding4);
		previewOuter.add(previewOuterLeft);
		
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
		previewOuterLeft.add(inputOuter);
		
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
		previewOuterLeft.add(outputOuter);
		
		//プレビューパネル
		panel = new JPanel();
		panel.setBorder(BorderFactory.createBevelBorder(1, Color.LIGHT_GRAY, Color.GRAY));
		//panel.setMinimumSize(new Dimension(96, 128));
		//panel.setMaximumSize(new Dimension(96, 128));
		panel.setPreferredSize(new Dimension(96, 128));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		previewOuter.add(panel);
		jCoverImagePanel = new JCoverImagePanel();
		jCoverImagePanel.setMinimumSize(new Dimension(96, 128));
		jCoverImagePanel.setMaximumSize(new Dimension(96, 128));
		jCoverImagePanel.setPreferredSize(new Dimension(96, 128));
		panel.add(jCoverImagePanel);
		
		//メタデータ
		JPanel metadataOuter = new JPanel();
		metadataOuter.setBorder(BorderFactory.createTitledBorder("メタデータ設定 (本文は変更されません)"));
		metadataOuter.setLayout(new BoxLayout(metadataOuter, BoxLayout.X_AXIS));
		metadataOuter.setPreferredSize(new Dimension(420, 90));
		dialogPanel.add(metadataOuter);
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
		panel.setBorder(padding4H);
		panel.setPreferredSize(new Dimension(32, 52));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		jButton = new JButton();
		jButton.setPreferredSize(new Dimension(32, 32));
		jButton.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/replace.png")));
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
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(padding4H);
		dialogPanel.add(buttonPanel);
		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		//変換前確認 次から確認しない場合にチェックが外せるようにする
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jCheckConfirm2 = new JCheckBox("変換前確認  ", true);
		jCheckConfirm2.setFocusPainted(false);
		panel.add(jCheckConfirm2);
		buttonPanel.add(panel);
		//変換実行
		jButton = new JButton("変換実行");
		jButton.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/apply.png")));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jTextTitle.getText().replaceFirst("^[ |　]+", "").replaceFirst("[ |　]+$", "").length() == 0) {
					JOptionPane.showMessageDialog(jDialogConfirm, "タイトルを設定してください。");
				} else {
					if (!jCheckConfirm2.isSelected()) jCheckConfirm.setSelected(false);
					jDialogConfirm.setVisible(false);
				}
			}
		});
		panel.add(jButton);
		buttonPanel.add(panel);
		//キャンセル
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jButton = new JButton("キャンセル");
		jButton.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/cancel.png")));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				convertCanceled = true;
				jDialogConfirm.setVisible(false);
			}
		});
		panel.add(jButton);
		buttonPanel.add(panel);
		
		////////////////////////////////////////////////////////////////
		//設定ダイアログ
		jDialogSetting = new JDialog();
		jDialogSetting.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		jDialogSetting.setSize(new Dimension(480, 220));
		jDialogSetting.setResizable(false);
		jDialogSetting.setTitle("詳細設定");
		jDialogSetting.setLayout(new BoxLayout(jDialogSetting.getContentPane(), BoxLayout.Y_AXIS));
		/*jDialogSetting.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) { restoreSetting(); }
		});*/
		
		////////////////////////////////////////////////////////////////
		//DnDの前にテキストを確定させる
		/*this.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) { jComboCover.transferFocusUpCycle(); }
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseClicked(MouseEvent arg0) {}
		});*/
		
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
		@Override
		public boolean verify(JComponent c)
		{
			boolean verified = false;
			JTextField textField = (JTextField)c;
			try{
				Integer.parseInt(textField.getText());
				verified = true;
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
	
	/** 表紙画像ドラッグ＆ドロップイベント */
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
	class PathChooserListener implements ActionListener
	{
		Component parent;
		private PathChooserListener(Component parent)
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
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) transfer.getTransferData(DataFlavor.javaFileListFlavor);
				
				//convertFiles((File[])(files.toArray()));
				ConvertFilesWorker convertFilesWorker = new ConvertFilesWorker((File[])(files.toArray()));
				convertFilesWorker.execute();
				
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
		currentPath = srcFiles[0].getParentFile();
		
		//共通パラメータ取得
		//出力先取得
		File dstPath = null;
		if (jComboDstPath.getSelectedIndex() != 0) {
			dstPath = new File(jComboDstPath.getEditor().getItem().toString());
			if (!dstPath.isDirectory()) {
				int ret = JOptionPane.showConfirmDialog(jDialogConfirm, "出力先がありません\nフォルダを作成しますか？", "出力先確認", JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.NO_OPTION) {
					LogAppender.append("変換処理をキャンセルしました\n");
					return;
				} else {
					//フォルダ作成
					dstPath.mkdirs();
				}
			}
		}
		//出力先と履歴保存
		addDstPath();
		
		//自動改ページ
		int forcePageBreak = 0;
		int forcePageBreakEmpty = 2;
		Pattern pattern = null;
		/*try {
			forcePageBreak = Integer.parseInt(jComboxPageBreak.getSelectedItem().toString());
			forcePageBreakEmpty = Integer.parseInt(jComboxPageBreakEmpty.getSelectedItem().toString());
		} catch (Exception e) {}
		*/
		
		////////////////////////////////
		//Appletのパラメータを取得しておく
		//画像リサイズ
		int resizeW = 0;
		if (jCheckResizeW.isSelected()) try { resizeW = Integer.parseInt(jTextResizeW.getText()); } catch (Exception e) {}
		int resizeH = 0;
		if (jCheckResizeH.isSelected()) try { resizeH = Integer.parseInt(jTextResizeH.getText()); } catch (Exception e) {}
		int pixels = 0;
		if (jCheckPixel.isSelected()) try { pixels = Integer.parseInt(jTextPixelW.getText())*Integer.parseInt(jTextPixelH.getText()); } catch (Exception e) {}
		this.epub3Writer.setResizeParam(resizeW, resizeH, pixels);
		this.epub3ImageWriter.setResizeParam(resizeW, resizeH, pixels);
		
		this.aozoraConverter.setForcePageBreak(forcePageBreak, forcePageBreakEmpty, pattern);
		//栞用ID出力
		this.aozoraConverter.setWithMarkId(this.jCheckMarkId.isSelected());
		//変換オプション設定
		this.aozoraConverter.setAutoYoko(this.jCheckAutoYoko.isSelected());
		//4バイト文字出力
		this.aozoraConverter.setGaiji32(this.jCheckGaiji32.isSelected());
		//表題左右中央
		this.aozoraConverter.setMiddleTitle(this.jCheckMiddleTitle.isSelected());
		
		////////////////////////////////
		//すべてのファイルの変換実行
		_convertFiles(srcFiles, dstPath);
		
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
		
		LogAppender.append("----------------------------------------------------------------\n");
		//表紙情報追加
		String coverFileName = this.jComboCover.getEditor().getItem().toString();
		if (coverFileName.equals(this.jComboCover.getItemAt(0).toString())) coverFileName = ""; //先頭の挿絵
		else if (coverFileName.equals(this.jComboCover.getItemAt(1).toString())) {
			coverFileName = AozoraEpub3.getSameCoverFileName(srcFile); //入力ファイルと同じ名前+.jpg/.png
		}
		else if (coverFileName.equals(this.jComboCover.getItemAt(2).toString())) coverFileName = null; //表紙無し
		
		//拡張子
		String ext = srcFile.getName();
		ext = ext.substring(ext.lastIndexOf('.')+1).toLowerCase();
		
		//BookInfo取得
		BookInfo bookInfo = null;
		//cbzは画像のみ
		if (!"cbz".equals(ext)) {
			//テキストファイルからメタ情報や画像単独ページ情報を取得
			bookInfo = AozoraEpub3.getBookInfo(
				srcFile, ext, this.aozoraConverter,
				this.jComboEncType.getSelectedItem().toString(),
				BookInfo.TitleType.values()[this.jComboTitle.getSelectedIndex()],
				coverFileName, this.jCheckCoverPage.isSelected()
			);
		}
		
		Epub3Writer writer = this.epub3Writer;
		//Zip内の画像ファイル一覧を取得
		HashMap<String, ImageInfo> zipImageFileInfos = null;
		try {
			if (!"txt".equals(ext)) {
				if (bookInfo == null) LogAppender.append("画像のみのePubファイルを生成します\n");
				//zip内の画像情報読み込み
				zipImageFileInfos = AozoraEpub3.getZipImageInfos(srcFile);
				if (bookInfo == null) {
					//画像出力用のBookInfo生成
					bookInfo = new BookInfo();
					bookInfo.imageOnly = true;
					String[] titleCreator = AozoraEpub3.getFileTitleCreator(srcFile.getName());
					if (titleCreator[0] != null) bookInfo.title = titleCreator[0];
					if (titleCreator[1] != null) bookInfo.creator = titleCreator[1];
					//Writerを画像出力用派生クラスに入れ替え
					writer = this.epub3ImageWriter;
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
		bookInfo.coverFileName = coverFileName;
		bookInfo.insertCoverPage = this.jCheckCoverPage.isSelected();
		bookInfo.insertTocPage = this.jCheckTocPage.isSelected();
		//目次縦書き
		bookInfo.setTocVertical(this.jRadioTocV.isSelected());
		//縦書き横書き設定追加
		bookInfo.vertical = this.jRadioVertical.isSelected();
		
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
			this.jTextSrcFileName.setText(srcFile.getName());
			this.jTextSrcFileName.setCaretPosition(0);
			this.jTextTitle.setText(title);
			this.jTextCreator.setText(creator);
			this.jTextDstFileName.setText((dstPath!=null ? dstPath.getAbsolutePath() : srcFile.getParentFile().getAbsolutePath())+File.separator);
			this.jTextDstFileName.setCaretPosition(0);
			
			//変更前確認設定用
			jCheckConfirm2.setSelected(true);
			
			//プレビュー表示
			this.jCoverImagePanel.clear();
			if (coverFileName != null && !"".equals(coverFileName)) {
				this.jCoverImagePanel.loadImage(coverFileName);
			}
			
			//本情報設定ダイアログ
			Point location = this.jFrameParent.getLocation();
			this.jDialogConfirm.setLocation(location.x+20, location.y+20);
			this.jDialogConfirm.setVisible(true);
			
			if (this.convertCanceled) {
				LogAppender.append("変換処理をキャンセルしました\n");
				return;
			}
			//確認ダイアログの値をBookInfoに設定
			bookInfo.title = this.jTextTitle.getText().trim();
			bookInfo.creator = this.jTextCreator.getText().trim();
			//著者が空欄なら著者行もクリア
			if (bookInfo.creator.length() == 0) bookInfo.creatorLine = -1;
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
			bookInfo, zipImageFileInfos
		);
		bookInfo.clear();
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
		
		this.jCheckResizeW.setEnabled(enabled);
		this.jCheckResizeH.setEnabled(enabled);
		this.jCheckPixel.setEnabled(enabled);
		this.setResizeTextEnabled(enabled);
		
		this.jCheckMarkId.setEnabled(enabled);
		this.jCheckAutoYoko.setEnabled(enabled);
		this.jRadioVertical.setEnabled(enabled);
		this.jRadioHorizontal.setEnabled(enabled);
		
		this.jComboEncType.setEnabled(enabled);
		this.jButtonFile.setEnabled(enabled);
	}
	
	private void setResizeTextEnabled(boolean enabled)
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
	////////////////////////////////////////////////////////////////
	/** Jar実行用 */
	public static void main(String args[])
	{
		final AozoraEpub3Applet applet = new AozoraEpub3Applet();
		applet.iconImage = java.awt.Toolkit.getDefaultToolkit().createImage(AozoraEpub3Applet.class.getResource("images/icon.png"));
		applet.init();
		
		//フレーム初期化
		final JFrame jFrame = new JFrame("青空文庫テキスト → ePub3変換");
		applet.setFrame(jFrame);
		//アイコン設定
		jFrame.setIconImage(applet.iconImage);
		jFrame.setMinimumSize(new Dimension(460, 240));
		jFrame.setPreferredSize(new Dimension(460, 240));
		
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
		//設定
		this.props.setProperty("MarkId", this.jCheckMarkId.isSelected()?"1":"");
		this.props.setProperty("AutoYoko", this.jCheckAutoYoko.isSelected()?"1":"");
		this.props.setProperty("Gaiji32", this.jCheckGaiji32.isSelected()?"1":"");
		this.props.setProperty("MiddleTitle", this.jCheckMiddleTitle.isSelected()?"1":"");
		this.props.setProperty("Vertical", this.jRadioVertical.isSelected()?"1":"");
		//this.props.setProperty("RtL", this.jRadioRtL.isSelected()?"1":"");
		this.props.setProperty("Ext", ""+this.jComboExt.getEditor().getItem().toString().trim());
		this.props.setProperty("ChkConfirm", this.jCheckConfirm.isSelected()?"1":"");
		//自動改行
		//this.props.setProperty("PageBreak", ""+this.jComboxPageBreak.getSelectedItem().toString().trim());
		//this.props.setProperty("PageBreakEmpty", ""+this.jComboxPageBreakEmpty.getSelectedItem().toString().trim());
		//先頭の挿絵と表紙無しのみ記憶
		if (this.jComboCover.getSelectedIndex() == 0) this.props.setProperty("Cover","");
		else if (this.jComboCover.getSelectedIndex() == 1) this.props.setProperty("Cover", ""+this.jComboCover.getEditor().getItem().toString().trim());
		this.props.setProperty("CoverPage", this.jCheckCoverPage.isSelected()?"1":"");
		this.props.setProperty("TocPage", this.jCheckTocPage.isSelected()?"1":"");
		this.props.setProperty("TocVertical", this.jRadioTocV.isSelected()?"1":"");
		
		this.props.setProperty("ResizeW", this.jCheckResizeW.isSelected()?"1":"");
		this.props.setProperty("ResizeH", this.jCheckResizeH.isSelected()?"1":"");
		this.props.setProperty("Pixel", this.jCheckPixel.isSelected()?"1":"");
		this.props.setProperty("ResizeNumW", this.jTextResizeW.getText());
		this.props.setProperty("ResizeNumH", this.jTextResizeH.getText());
		this.props.setProperty("PixelW", this.jTextPixelW.getText());
		this.props.setProperty("PixelH", this.jTextPixelH.getText());
		
		this.props.setProperty("EncType", ""+this.jComboEncType.getSelectedIndex());
		this.props.setProperty("OverWrite", this.jCheckOverWrite.isSelected()?"1":"");
		this.props.setProperty("LastDir", this.currentPath==null?"":this.currentPath.getAbsolutePath());
		//設定ファイル更新
		this.props.store(new FileOutputStream(this.propFileName), "AozoraEpub3 Parameters");
		
		super.finalize();
	}
}
