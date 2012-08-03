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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.converter.AozoraEpub3Converter.TitleType;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.util.LogAppender;
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
	/** 表題 (+副題)編集用 */
	JTextField jTextTitle;
	/** 著者名編集用 */
	JTextField jTextCreator;
	
	/** 設定ダイアログ */
	JDialog jDialogSetting;
	
	JComboBox jComboTitle;
	JCheckBox jCheckConfirm;
	JCheckBox jCheckAutoFileName;
	JComboBox jComboExt;
	JCheckBox jCheckIdSpan;
	
	JCheckBox jCheckOverWrite;
	JCheckBox jCheckAutoYoko;
	
	JRadioButton jRadioVertical;
	JRadioButton jRadioHorizontal;
	
	JRadioButton jRadioLtR;
	JRadioButton jRadioRtL;
	
	JComboBox jComboEncType;
	
	//JComboBox jComboxPageBreak;
	//JComboBox jComboxPageBreakEmpty;
	
	/** 表紙選択 */
	JComboBox jComboCover;
	JCheckBox jCheckCoverPage;
	
	JScrollPane jScrollPane;
	JTextArea jTextArea;
	
	/** 青空→ePub3変換クラス */
	AozoraEpub3Converter aozoraConverter;
	
	/** ePub3出力クラス */
	Epub3Writer epub3Writer;
	
	/** UTF-8 → グリフタグ変換クラス */
	//GlyphConverter glyphConverter;
	//String initdConverterType = null;
	
	boolean convertCanceled = false;
	
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
		//アップレットのレイアウト設定
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		////////////////////////////////
		//1段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(zeroPadding);
		this.add(panel);
		
		//表題行
		label = new JLabel(" 表題行");
		panel.add(label);
		jComboTitle = new JComboBox(AozoraEpub3Converter.TitleType.titleTypeNames);
		jComboTitle.setFocusable(false);
		jComboTitle.setPreferredSize(new Dimension(120, 22));
		try { jComboTitle.setSelectedIndex(Integer.parseInt(props.getProperty("TitleType"))); } catch (Exception e) {}
		((JLabel)jComboTitle.getRenderer()).setBorder(zeroPadding);
		panel.add(jComboTitle);
		//ファイル名設定
		propValue = props.getProperty("AutoFileName");
		jCheckAutoFileName = new JCheckBox("ファイル名に利用", propValue==null||"1".equals(propValue));
		jCheckAutoFileName.setFocusPainted(false);
		panel.add(jCheckAutoFileName);
		
		//拡張子
		label = new JLabel(" 拡張子");
		panel.add(label);
		propValue = props.getProperty("Ext");
		jComboExt = new JComboBox(new String[]{".epub", ".kepub.epub"});
		jComboExt.setEditable(true);
		jComboExt.setPreferredSize(new Dimension(90, 22));
		jComboExt.setSelectedItem(propValue==null||propValue.length()==0?".epub":propValue);
		panel.add(jComboExt);
		
		////////////////////////////////
		//2段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(zeroPadding);
		this.add(panel);
		//栞用idSpan
		propValue = props.getProperty("IdSpan");
		jCheckIdSpan = new JCheckBox("栞用span出力", propValue==null||"1".equals(propValue));
		jCheckIdSpan.setFocusPainted(false);
		panel.add(jCheckIdSpan);
		//半角2文字縦書き
		propValue = props.getProperty("AutoYoko");
		jCheckAutoYoko = new JCheckBox("半角2文字縦中横", propValue==null||"1".equals(propValue));
		jCheckAutoYoko.setFocusPainted(false);
		panel.add(jCheckAutoYoko);
		//縦書き横書き
		label = new JLabel(" ");
		panel.add(label);
		ButtonGroup group = new ButtonGroup();
		propValue = props.getProperty("Vertical");
		boolean propVertical = propValue==null||"1".equals(propValue);
		jRadioVertical = new JRadioButton("縦書き", propVertical);
		jRadioVertical.setFocusPainted(false);
		panel.add(jRadioVertical);
		group.add(jRadioVertical);
		jRadioHorizontal = new JRadioButton("横書き", !propVertical);
		jRadioHorizontal.setFocusPainted(false);
		panel.add(jRadioHorizontal);
		group.add(jRadioHorizontal);
		
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
		panel.setMaximumSize(new Dimension(1920, 20));
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
		//4段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(zeroPadding);
		this.add(panel);
		//入力文字コード
		label = new JLabel(" 表紙");
		panel.add(label);
		propValue = props.getProperty("Cover");
		jComboCover = new JComboBox(new String[]{"[先頭の挿絵]", "[表紙無し]", "http://"});
		jComboCover.setEditable(true);
		if (propValue==null||propValue.length()==0) jComboCover.setSelectedIndex(0);
		else jComboCover.setSelectedItem(propValue);
		jComboCover.setPreferredSize(new Dimension(300, 22));
		panel.add(jComboCover);
		JButton jButtonCover = new JButton("選択");
		jButtonCover.setBorder(zeroPadding);
		jButtonCover.setPreferredSize(new Dimension(50, 22));
		jButtonCover.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/image_add.png")));
		jButtonCover.setFocusable(false);
		jButtonCover.addActionListener(new CoverChooserListener(this));
		panel.add(jButtonCover);
		//表紙ページ
		propValue = props.getProperty("CoverPage");
		jCheckCoverPage = new JCheckBox("表紙ページ", propValue!=null||"1".equals(propValue));
		jCheckCoverPage.setFocusPainted(false);
		panel.add(jCheckCoverPage);
		
		////////////////////////////////
		//5段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(zeroPadding);
		this.add(panel);
		//入力文字コード
		label = new JLabel(" 入力文字コード");
		panel.add(label);
		jComboEncType = new JComboBox(new String[]{"MS932", "UTF-8"});
		jComboEncType.setFocusable(false);
		jComboEncType.setPreferredSize(new Dimension(80, 22));
		((JLabel)jComboEncType.getRenderer()).setBorder(zeroPadding);
		try { jComboEncType.setSelectedIndex(Integer.parseInt(props.getProperty("EncType"))); } catch (Exception e) {}
		panel.add(jComboEncType);
		//ファイルの上書き許可
		propValue = props.getProperty("OverWrite");
		jCheckOverWrite = new JCheckBox("ePubファイル上書き", propValue==null||"1".equals(propValue));
		jCheckOverWrite.setFocusPainted(false);
		panel.add(jCheckOverWrite);
		//開く
		label = new JLabel("   ");
		panel.add(label);
		JButton jButtonFile = new JButton("ファイル選択");
		jButtonFile.setBorder(zeroPadding);
		jButtonFile.setPreferredSize(new Dimension(100, 24));
		jButtonFile.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/folder_page.png")));
		jButtonFile.setFocusable(false);
		jButtonFile.addActionListener(new FileChooserListener(this));
		panel.add(jButtonFile);
		//変換前に確認する
		propValue = props.getProperty("ChkConfirm");
		jCheckConfirm = new JCheckBox("確認", propValue==null||"1".equals(propValue));
		jCheckConfirm.setFocusPainted(false);
		panel.add(jCheckConfirm);
		
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
		jDialogConfirm.setSize(new Dimension(480, 240));
		jDialogConfirm.setResizable(false);
		jDialogConfirm.setTitle("変換前確認");
		jDialogConfirm.setLayout(new BoxLayout(jDialogConfirm.getContentPane(), BoxLayout.Y_AXIS));
		jDialogConfirm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) { convertCanceled = true; }
		});
		Border titlePadding4 = BorderFactory.createEmptyBorder(0, 4, 4, 4);
		Border padding4 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		Border padding4H = BorderFactory.createEmptyBorder(0, 4, 0, 4);
		
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		dialogPanel.setBorder(padding4);
		jDialogConfirm.add(dialogPanel);
		
		JPanel inputOuter = new JPanel();
		inputOuter.setBorder(BorderFactory.createTitledBorder("入力ファイル"));
		inputOuter.setLayout(new BoxLayout(inputOuter, BoxLayout.X_AXIS));
		inputOuter.setPreferredSize(new Dimension(420, 52));
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(titlePadding4);
		panel.setPreferredSize(new Dimension(420, 26));
		jTextSrcFileName = new JTextField();
		jTextSrcFileName.setEditable(false);
		jTextSrcFileName.setPreferredSize(new Dimension(440, 24));
		panel.add(jTextSrcFileName);
		inputOuter.add(panel);
		dialogPanel.add(inputOuter);
		
		JPanel metadataOuter = new JPanel();
		metadataOuter.setBorder(BorderFactory.createTitledBorder("メタデータ設定 (本文は変更されません)"));
		metadataOuter.setLayout(new BoxLayout(metadataOuter, BoxLayout.X_AXIS));
		metadataOuter.setPreferredSize(new Dimension(420, 90));
		dialogPanel.add(metadataOuter);
		JPanel metadataInner = new JPanel();
		metadataInner.setLayout(new BoxLayout(metadataInner, BoxLayout.Y_AXIS));
		metadataOuter.add(metadataInner);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(titlePadding4);
		panel.setPreferredSize(new Dimension(420, 28));
		panel.add(new JLabel("表題 : "));
		jTextTitle = new JTextField();
		jTextTitle.setPreferredSize(new Dimension(420, 26));
		jTextTitle.setMaximumSize(new Dimension(420, 26));
		panel.add(jTextTitle);
		metadataInner.add(panel);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(titlePadding4);
		panel.setPreferredSize(new Dimension(420, 28));
		panel.add(new JLabel("著者 : "));
		jTextCreator = new JTextField();
		jTextCreator.setPreferredSize(new Dimension(420, 26));
		jTextCreator.setMaximumSize(new Dimension(420, 26));
		panel.add(jTextCreator);
		metadataInner.add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panel.setBorder(padding4H);
		panel.setPreferredSize(new Dimension(32, 52));
		panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
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
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		jButton = new JButton("変換実行");
		jButton.setIcon(new ImageIcon(AozoraEpub3Applet.class.getResource("images/apply.png")));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jTextTitle.getText().replaceFirst("^[ |　]+", "").replaceFirst("[ |　]+$", "").length() == 0) {
					JOptionPane.showMessageDialog(jDialogConfirm, "タイトルを設定してください。");
				} else {
					jDialogConfirm.setVisible(false);
				}
			}
		});
		panel.add(jButton);
		buttonPanel.add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
		this.addMouseListener(new MouseListener() {
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
		});
		
		////////////////////////////////////////////////////////////////
		//ログ出力先を設定
		LogAppender.setTextArea(jTextArea);
		
		//初期化
		try {
			//ePub出力クラス初期化
			this.epub3Writer = new Epub3Writer("template/");
			
			//変換テーブルをstaticに生成
			this.aozoraConverter = new AozoraEpub3Converter(this.epub3Writer);
			
		} catch (IOException e) {
			e.printStackTrace();
			jTextArea.append(e.getMessage());
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
			fileChooser.setFileFilter(new FileNameExtensionFilter("表紙画像(jpg,png,gif)", new String[]{"jpg","png","gif"}));
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				jComboCover.setSelectedItem(fileChooser.getSelectedFile().getAbsolutePath());
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
			JFileChooser fileChooser = new JFileChooser(currentPath);
			fileChooser.setFileFilter(new FileNameExtensionFilter("青空文庫テキスト(txt,zip)", new String[]{"txt","zip"}));
			fileChooser.setMultiSelectionEnabled(true);
			int state = fileChooser.showOpenDialog(parent);
			switch (state) {
			case JFileChooser.APPROVE_OPTION:
				convertCanceled = false;
				File[] files = fileChooser.getSelectedFiles();
				for (File srcFile : files) {
					convertFile(srcFile);
					currentPath = srcFile.getParentFile();
					//キャンセル
					if (convertCanceled) return;
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
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable transfer = dtde.getTransferable();
			try {
				convertCanceled = false;
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) transfer.getTransferData(DataFlavor.javaFileListFlavor);
				for (File srcFile : files) {
					convertFile(srcFile);
					currentPath = srcFile.getParentFile();
					//キャンセル
					if (convertCanceled) return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
			}
		}
	}
	
	/** 内部用変換関数 Appletの設定を引数に渡す */
	private void convertFile(File srcFile)
	{
		//パラメータ設定
		//自動改ページ
		int forcePageBreak = 0;
		int forcePageBreakEmpty = 2;
		Pattern pattern = null;
		/*try {
			forcePageBreak = Integer.parseInt(jComboxPageBreak.getSelectedItem().toString());
			forcePageBreakEmpty = Integer.parseInt(jComboxPageBreakEmpty.getSelectedItem().toString());
		} catch (Exception e) {}
		*/
		this.aozoraConverter.setForcePageBreak(forcePageBreak, forcePageBreakEmpty, pattern);
		
		//表紙設定
		String coverFileName = this.jComboCover.getSelectedItem().toString();
		if (coverFileName.equals(this.jComboCover.getItemAt(0).toString())) coverFileName = ""; //先頭の挿絵
		if (coverFileName.equals(this.jComboCover.getItemAt(1).toString())) coverFileName = null; //表紙無し
		
		
		//BookInfo取得
		BookInfo bookInfo = AozoraEpub3.getBookInfo(
			srcFile,
			this.aozoraConverter,
			this.jCheckAutoFileName.isSelected(),
			this.jComboExt.getSelectedItem().toString().trim(),
			this.jCheckOverWrite.isSelected(),
			this.jRadioVertical.isSelected(),
			coverFileName,
			this.jComboEncType.getSelectedItem().toString(),
			TitleType.values()[this.jComboTitle.getSelectedIndex()]
		);
		if (bookInfo == null) return;
		
		bookInfo.insertCoverPage = this.jCheckCoverPage.isSelected();
		
		if (this.jCheckConfirm.isSelected()) {
			//表題と著者設定
			this.jTextSrcFileName.setText(srcFile.getName());
			this.jTextSrcFileName.setCaretPosition(0);
			this.jTextTitle.setText(bookInfo.title==null?"":bookInfo.title);
			this.jTextCreator.setText(bookInfo.creator==null?"":bookInfo.creator);
			//本情報設定ダイアログ
			Point location = this.jFrameParent.getLocation();
			this.jDialogConfirm.setLocation(location.x+20, location.y+20);
			this.jDialogConfirm.setVisible(true);
			
			if (this.convertCanceled) {
				LogAppender.append("キャンセルしました\n");
				return;
			}
			//確認ダイアログの値をBookInfoに設定
			bookInfo.title = this.jTextTitle.getText().trim();
			bookInfo.creator = this.jTextCreator.getText().trim();
			//著者が空欄なら著者行もクリア
			if (bookInfo.creator.length() == 0) bookInfo.creatorLine = -1;
		}
		
		//変換設定
		//栞用span出力
		this.aozoraConverter.setWithIdSpan(this.jCheckIdSpan.isSelected());
		//変換オプション設定
		this.aozoraConverter.setAutoYoko(this.jCheckAutoYoko.isSelected());
		
		AozoraEpub3.convertFile(
			srcFile,
			this.aozoraConverter,
			this.epub3Writer,
			this.jCheckAutoFileName.isSelected(),
			this.jComboExt.getSelectedItem().toString().trim(),
			this.jCheckOverWrite.isSelected(),
			this.jComboEncType.getSelectedItem().toString(),
			bookInfo
		);
	}
	
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
		//アップレット設定の保存
		this.props.setProperty("EncType", ""+this.jComboEncType.getSelectedIndex());
		this.props.setProperty("TitleType", ""+this.jComboTitle.getSelectedIndex());
		this.props.setProperty("OverWrite", this.jCheckOverWrite.isSelected()?"1":"");
		this.props.setProperty("IdSpan", this.jCheckIdSpan.isSelected()?"1":"");
		this.props.setProperty("AutoYoko", this.jCheckAutoYoko.isSelected()?"1":"");
		this.props.setProperty("Vertical", this.jRadioVertical.isSelected()?"1":"");
		//this.props.setProperty("RtL", this.jRadioRtL.isSelected()?"1":"");
		this.props.setProperty("AutoFileName", this.jCheckAutoFileName.isSelected()?"1":"");
		this.props.setProperty("ChkConfirm", this.jCheckConfirm.isSelected()?"1":"");
		this.props.setProperty("Ext", ""+this.jComboExt.getSelectedItem().toString().trim());
		//this.props.setProperty("PageBreak", ""+this.jComboxPageBreak.getSelectedItem().toString().trim());
		//this.props.setProperty("PageBreakEmpty", ""+this.jComboxPageBreakEmpty.getSelectedItem().toString().trim());
		//先頭の挿絵と表紙無しのみ記憶
		this.props.setProperty("CoverPage", this.jCheckCoverPage.isSelected()?"1":"");
		if (this.jComboCover.getSelectedIndex() == 0) this.props.setProperty("Cover","");
		else if (this.jComboCover.getSelectedIndex() == 1) this.props.setProperty("Cover", ""+this.jComboCover.getSelectedItem().toString().trim());
		this.props.setProperty("LastDir", this.currentPath==null?"":this.currentPath.getAbsolutePath());
		//設定ファイル更新
		this.props.store(new FileOutputStream(this.propFileName), "AozoraEpub3 Parameters");
		
		super.finalize();
	}
}
