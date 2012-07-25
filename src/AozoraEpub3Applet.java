import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.util.LogAppender;
import com.github.hmdev.writer.Epub3Writer;



/**
 * 青空文庫テキスト→ePub3変換操作用アプレット
 * Licence: Non-commercial use only.
 */
public class AozoraEpub3Applet extends JApplet
{
	private static final long serialVersionUID = 1L;
	
	JComboBox jComboTitle;
	JCheckBox jCheckAutoFileName;
	JComboBox jComboExt;
	JCheckBox jCheckIdSpan;
	
	JCheckBox jCheckOverWrite;
	JCheckBox jCheckAutoYoko;
	
	JRadioButton jRadioVertical;
	JRadioButton jRadioHorizontal;
	
	JComboBox jComboEncType;
	
	//JComboBox jComboxPageBreak;
	//JComboBox jComboxPageBreakEmpty;
	
	/** 表紙選択 */
	JComboBox jComboCover;
	
	JScrollPane jScrollPane;
	JTextArea jTextArea;
	
	/** 青空→ePub3変換クラス */
	AozoraEpub3Converter aozoraConverter;
	
	/** ePub3出力クラス */
	Epub3Writer epub3Writer;
	
	/** UTF-8 → グリフタグ変換クラス */
	//GlyphConverter glyphConverter;
	//String initdConverterType = null;
	
	/** 設定ファイル */
	Properties props;
	/** 設定ファイル名 */
	String propFileName = "AozoraEpub3.ini";
	
	File currentPath = null;
	
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
		
		Border panelPadding = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		
		BoxLayout boxLayout = new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS);
		this.setLayout(boxLayout);
		
		String propValue;
		JPanel panel;
		JLabel label;
		
		//1段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(panelPadding);
		this.add(panel);
		
		//表題行
		label = new JLabel(" 表題行");
		panel.add(label);
		jComboTitle = new JComboBox(AozoraEpub3Converter.titleType);
		jComboTitle.setFocusable(false);
		jComboTitle.setPreferredSize(new Dimension(120, 22));
		try { jComboTitle.setSelectedIndex(Integer.parseInt(props.getProperty("TitleType"))); } catch (Exception e) {}
		((JLabel)jComboTitle.getRenderer()).setBorder(panelPadding);
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
		jComboExt.setSelectedItem(propValue==null||propValue.length()==0?".epub":propValue);
		panel.add(jComboExt);
		
		//2段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(panelPadding);
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
		
		//4段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(panelPadding);
		this.add(panel);
		//入力文字コード
		label = new JLabel(" 表紙");
		panel.add(label);
		propValue = props.getProperty("Cover");
		jComboCover = new JComboBox(new String[]{"[先頭の挿絵]", "[表紙無し]", "http://"});
		jComboCover.setEditable(true);
		if (propValue==null||propValue.length()==0) jComboCover.setSelectedIndex(0);
		else jComboCover.setSelectedItem(propValue);
		jComboCover.setPreferredSize(new Dimension(340, 22));
		panel.add(jComboCover);
		JButton jButtonCover = new JButton("表紙選択");
		jButtonCover.setPreferredSize(new Dimension(80, 22));
		jButtonCover.setFocusable(false);
		jButtonCover.addActionListener(new CoverChooserListener(this));
		panel.add(jButtonCover);
		
		//5段目
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMaximumSize(new Dimension(1920, 20));
		panel.setBorder(panelPadding);
		this.add(panel);
		//入力文字コード
		label = new JLabel(" 入力文字コード");
		panel.add(label);
		jComboEncType = new JComboBox(new String[]{"MS932", "UTF-8"});
		jComboEncType.setFocusable(false);
		jComboEncType.setPreferredSize(new Dimension(80, 22));
		((JLabel)jComboEncType.getRenderer()).setBorder(panelPadding);
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
		jButtonFile.setPreferredSize(new Dimension(90, 22));
		jButtonFile.setFocusable(false);
		jButtonFile.addActionListener(new FileChooserListener(this));
		panel.add(jButtonFile);
		
		//テキストエリア
		jTextArea = new JTextArea("青空文庫テキストをここにドラッグ＆ドロップまたは「ファイル選択」で変換します。\n");
		jTextArea.setEditable(false);
		jTextArea.setFont(new Font("Default", Font.PLAIN, 12));
		jTextArea.setBorder(new LineBorder(Color.white, 3));
		new DropTarget(jTextArea, DnDConstants.ACTION_COPY_OR_MOVE, new DropListener(), true);
		
		jScrollPane = new JScrollPane(jTextArea);
		this.add(jScrollPane);
		
		//DnDの前にテキストを確定させる
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0)
			{
				jComboCover.transferFocusUpCycle();
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseClicked(MouseEvent arg0) {}
		});
		
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
				File[] files = fileChooser.getSelectedFiles();
				for (File srcFile : files) {
					convertFile(srcFile);
					currentPath = srcFile.getParentFile();
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
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) transfer.getTransferData(DataFlavor.javaFileListFlavor);
				for (File srcFile : files) {
					convertFile(srcFile);
					currentPath = srcFile.getParentFile();
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
		int forcePageBreak = 0;
		int forcePageBreakEmpty = 2;
		Pattern pattern = null;
		/*try {
			forcePageBreak = Integer.parseInt(jComboxPageBreak.getSelectedItem().toString());
			forcePageBreakEmpty = Integer.parseInt(jComboxPageBreakEmpty.getSelectedItem().toString());
		} catch (Exception e) {}*/
		
		this.aozoraConverter.setForcePageBreak(forcePageBreak, forcePageBreakEmpty, pattern);
		String coverFileName = jComboCover.getSelectedItem().toString();
		if (coverFileName.equals(jComboCover.getItemAt(0).toString())) coverFileName = ""; //先頭の挿絵
		if (coverFileName.equals(jComboCover.getItemAt(1).toString())) coverFileName = null; //表紙無し
		
		AozoraEpub3.convertFile(
			srcFile,
			this.aozoraConverter,
			this.epub3Writer,
			jCheckAutoFileName.isSelected(),
			jComboExt.getSelectedItem().toString().trim(),
			jCheckIdSpan.isSelected(),
			jCheckOverWrite.isSelected(),
			jCheckAutoYoko.isSelected(),
			jRadioVertical.isSelected(),
			coverFileName,
			jComboEncType.getSelectedItem().toString(),
			jComboTitle.getSelectedIndex()
		);
	}
	
	/** Jar実行用 */
	public static void main(String args[])
	{
		final AozoraEpub3Applet applet = new AozoraEpub3Applet();
		applet.init();
		
		//フレーム初期化
		final JFrame jFrame = new JFrame("青空文庫テキスト → ePub3変換");
		//アイコン設定
		jFrame.setIconImage(java.awt.Toolkit.getDefaultToolkit().createImage(AozoraEpub3Applet.class.getResource("icon.png")));
		
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
		this.props.setProperty("AutoYoko", this.jCheckAutoYoko.isSelected()?"1":"");
		this.props.setProperty("Vertical", this.jRadioVertical.isSelected()?"1":"");
		this.props.setProperty("AutoFileName", this.jCheckAutoFileName.isSelected()?"1":"");
		this.props.setProperty("Ext", ""+this.jComboExt.getSelectedItem().toString().trim());
		//this.props.setProperty("PageBreak", ""+this.jComboxPageBreak.getSelectedItem().toString().trim());
		//this.props.setProperty("PageBreakEmpty", ""+this.jComboxPageBreakEmpty.getSelectedItem().toString().trim());
		//先頭の挿絵と表紙無しのみ記憶
		if (this.jComboCover.getSelectedIndex() == 0) this.props.setProperty("Cover","");
		else if (this.jComboCover.getSelectedIndex() == 1) this.props.setProperty("Cover", ""+this.jComboCover.getSelectedItem().toString().trim());
		this.props.setProperty("LastDir", this.currentPath==null?"":this.currentPath.getAbsolutePath());
		//設定ファイル更新
		this.props.store(new FileOutputStream(this.propFileName), "AozoraEpub3 Parameters");
		
		super.finalize();
	}
}
