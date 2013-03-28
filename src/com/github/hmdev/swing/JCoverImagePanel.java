package com.github.hmdev.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLDecoder;
import java.util.List;

import javax.swing.JPanel;

import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.CoverEditInfo;

/**
 * 表紙画像プレビューとトリミング用パネル
 */
public class JCoverImagePanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, DropTargetListener, KeyListener
{
	private static final long serialVersionUID = 1L;
	
	BookInfo bookInfo;
	private BufferedImage previewImage;
	
	private double minScale = 0;
	
	private double scale = 0;
	private double offsetX = 0;
	private double offsetY = 0;
	
	private double visibleWidth = 0;
	
	private int fitType = 0;
	final static int FIT_ALL = 0;
	final static int FIT_W = 1;
	final static int FIT_H = 2;
	final static int FIT_ZOOM = 3;
	
	private int startX = 0;
	private int startY = 0;
	private int prevX = 0;
	private int mouseType = 0;
	
	final static int EVT_CHANGED = 1;
	final static int EVT_DBLCLICK = 10;
	final static int EVT_PAGE_UP = 100;
	final static int EVT_PAGE_DOWN = 101;
	
	public JCoverImagePanel()
	{
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
		this.setBackground(Color.LIGHT_GRAY);
		
		this.setFocusable(true);
		this.addKeyListener(this);
	}
	
	/** パネル描画 */
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		if (this.bookInfo != null && this.bookInfo.coverImage != null) {
			if (this.scale <= 0) this.setScale();
			if (this.previewImage == null) {
				this.createPreviewImage(this.scale);
			}
			if (this.previewImage != null) {
				if (this.visibleWidth <= 0) this.visibleWidth = this.getWidth();
				double minX = this.visibleWidth-this.previewImage.getWidth();
				double maxX = 0;
				double x = minX/2;
				if (minX < 0) x = Math.max(minX, Math.min(maxX, this.offsetX));
				this.offsetX = x;
				int minY = this.getHeight()-this.previewImage.getHeight();
				int maxY = 0;
				int y = minY/2;
				if (minY < 0) y = Math.max(minY,  Math.min(maxY, (int)this.offsetY));
				else y = 0;
				this.offsetY = y;
				g.setClip(0, 0, (int)this.visibleWidth, this.getHeight());
				g.setColor(Color.WHITE);
				g.fillRect((int)x, 0, (int)Math.min(this.previewImage.getWidth()-x, this.visibleWidth), this.previewImage.getHeight());
				g.drawImage(this.previewImage, (int)x, (int)y, this);
			}
		}
	}
	
	/** プレビュー用の小さい画像を生成 */
	private void createPreviewImage(double scale)
	{
		BufferedImage previewImage = new BufferedImage((int)Math.round(this.bookInfo.coverImage.getWidth()*scale), (int)Math.round(this.bookInfo.coverImage.getHeight()*scale), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = previewImage.createGraphics();
		try {
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, previewImage.getWidth(), previewImage.getHeight());
			AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_BICUBIC);
			g2.drawImage(this.bookInfo.coverImage, ato, 0 ,0);
		} finally {
			g2.dispose();
		}
		this.previewImage = previewImage;
	}
	
	/** パネルの表示高さ変更 */
	public void setPaneSize(int width, int height)
	{
		//int w = this.getWidth();
		int h = this.getHeight();
		if (h == 0) return;
		double scale = (double)height/h;
		Dimension size = new Dimension(width, height);
		this.setPreferredSize(size);
		this.setMaximumSize(size);
		this.setMinimumSize(size);
		this.setSize(width, height);
		if (scale != 1) {
			this.offsetX *= scale;
			this.offsetY *= scale;
			this.visibleWidth *= scale;
			this.scale *= scale;
		}
		this.setScale();
		this.previewImage = null;
		this.repaint();
	}
	
	/** 画像ファイルを設定
	 * 画像入れ替え時もこれで行う
	 * プレビューはpaintで生成 */
	public void setBookInfo(BookInfo bookInfo)
	{
		this.bookInfo = bookInfo;
		//表紙の情報がbookInfoにあれば再現
		if (this.bookInfo.coverEditInfo != null) {
			//縦の比率で再計算
			CoverEditInfo coverEditInfo  = this.bookInfo.coverEditInfo;
			double rate = this.getHeight() / (double)coverEditInfo.panelHeight;
			this.offsetX = coverEditInfo.offsetX * rate;
			this.offsetY = coverEditInfo.offsetY * rate;
			this.visibleWidth = coverEditInfo.visibleWidth * rate;
			this.scale = coverEditInfo.scale * rate;
			//this.fitType = coverEditInfo.fitType;
			this.fitType = FIT_ZOOM;
		} else {
			this.offsetX = 0;
			this.offsetY = 0;
			this.visibleWidth = 0;
			if (this.fitType == FIT_ZOOM) this.fitType = FIT_ALL;
		}
		
		this.setScale();
		this.previewImage = null;
		this.repaint();
	}
	/** 表紙編集情報を取得 */
	public CoverEditInfo getCoverEditInfo()
	{
		return new CoverEditInfo(this.getWidth(), this.getHeight(), this.fitType, this.scale, this.offsetX, this.offsetY, this.visibleWidth);
	}
	
	/** 幅高さに合わせる */
	public void setFitType(int fitType, boolean force)
	{
		if (!force && this.fitType == fitType) return;
		this.fitType = fitType;
		this.previewImage = null;
		this.offsetX = 0;
		this.offsetY = 0;
		if (fitType == FIT_W) {
			this.visibleWidth = this.getWidth();
		}
		this.setScale();
		this.repaint();
	}
	/** 倍率変更
	 * 幅調整中は高さ100%まで */
	public void setZoom(double zoom)
	{
		if (this.bookInfo.coverImage == null) return;
		if (this.visibleWidth < this.getWidth()) {
			//幅調整中なら高さ制限
			if (this.scale*zoom*this.bookInfo.coverImage.getHeight() < this.getHeight()) return;
		} else {
			//最小スケールに制限
			if (this.scale*zoom < this.minScale) return;
		}
		//2倍より大きくしない
		if (zoom > 1 && this.scale > 2) { return; }
		
		this.fitType = FIT_ZOOM;
		this.previewImage = null;
		this.scale *= zoom;
		this.offsetX *= zoom;
		this.offsetY *= zoom;
		this.repaint();
	}
	
	void resetVisibleWidth()
	{
		this.visibleWidth = this.getWidth();
		//プレビューが狭かったら倍率変更
		this.setScale();
		this.previewImage = null;
		this.repaint();
	}
	
	void setVisibleWidthOffset(int offset)
	{
		double width = this.visibleWidth+offset;
		//プレビュー幅の方が狭ければプレビューを狭くする
		if (offset < 0) {
			if (this.previewImage != null && width > this.previewImage.getWidth()) width = this.previewImage.getWidth()-offset;
			if (width < 10) return;
		} else {
			if (width > this.getWidth()) return;
		}
		//高さより低かったら縦に合わせる
		switch (this.fitType) {
		case FIT_ALL:
		case FIT_W:
			this.fitType = FIT_H;
			break;
		case FIT_ZOOM:
			if (this.previewImage != null && this.previewImage.getHeight() < this.getHeight()) {
				this.fitType = FIT_H;
			}
		}
		this.visibleWidth = width;
		this.setScale();
		this.previewImage = null;
		this.repaint();
	}
	
	private void setScale()
	{
		if (bookInfo == null || bookInfo.coverImage == null) {
			this.minScale = 0;
			return;
		}
		if (this.visibleWidth <= 0) this.visibleWidth = this.getWidth();
		this.minScale = Math.min((double)this.visibleWidth/bookInfo.coverImage.getWidth(), (double)this.getHeight()/this.bookInfo.coverImage.getHeight());
		switch (this.fitType) {
		case FIT_ALL:
			this.scale = minScale; break;
		case FIT_W:
			this.scale = Math.max(this.minScale, (double)this.visibleWidth/bookInfo.coverImage.getWidth());
			break;
		case FIT_H:
			this.scale = Math.max(this.minScale, (double)this.getHeight()/this.bookInfo.coverImage.getHeight()); break;
		}
	}
	
	/** 画像範囲が変更されているか 表紙がなければfalse */
	public boolean isModified()
	{
		if (bookInfo.coverImage == null) return false;
		//幅変更
		if (this.visibleWidth != this.getWidth()) return true;
		//トリミングなし
		double scale = Math.min((double)this.getWidth()/bookInfo.coverImage.getWidth(), (double)this.getHeight()/this.bookInfo.coverImage.getHeight());
		return this.scale != scale;
		
	}
	/** 編集された表紙を取得 */
	public BufferedImage getModifiedImage(double coverW, double coverH)
	{
		try {
		//表紙なし
		if (this.bookInfo.coverImage == null) return null;
		
		double scale = Math.min((double)this.getWidth()/bookInfo.coverImage.getWidth(), (double)this.getHeight()/this.bookInfo.coverImage.getHeight());
		if (this.visibleWidth <= 0) this.visibleWidth = this.getWidth();
		
		//トリミングと幅変更なしで、表紙サイズならnullを返す
		if (this.scale == scale && this.visibleWidth == this.getWidth()) {
			//if ()
			return null;
		}
		
		if (this.previewImage == null) this.createPreviewImage(this.scale);
		
		//縮尺に合せてリサイズ 大きければ縮小
		double coverScale = 1;
		if (coverW > 0 && coverH > 0) coverScale = Math.min(coverW/this.visibleWidth, coverH/this.getHeight()) * this.scale;
		else if (coverW > 0) coverScale = (coverW/this.visibleWidth) * this.scale;
		else if (coverH > 0) coverScale = (coverH/this.getHeight()) * this.scale;
		coverW = Math.min(coverW, this.bookInfo.coverImage.getWidth()*coverScale);
		coverH = Math.min(coverH, this.bookInfo.coverImage.getHeight()*coverScale);
		if (coverScale > 1) {
			coverW /= coverScale;
			coverH /= coverScale;
			coverScale = 1;
		}
		coverW *= this.visibleWidth/Math.min(this.getWidth(), this.previewImage.getWidth());
		int x = 0;
		if (this.visibleWidth < this.previewImage.getWidth()) x = (int)Math.round(this.offsetX * coverW/this.visibleWidth);
		int y = 0;
		if (this.getHeight() < this.previewImage.getHeight()) y = (int)Math.round(this.offsetY * coverH/this.getHeight());
		
		BufferedImage coverImage = new BufferedImage((int)coverW, (int)coverH, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = coverImage.createGraphics();
		try {
			//リサイズのatoとoffset指定でdrawImage
			AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(coverScale, coverScale), AffineTransformOp.TYPE_BICUBIC);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, (int)coverW, (int)coverH);
			g2.drawImage(this.bookInfo.coverImage, ato, x, y);
		} finally {
			g2.dispose();
		}
		return coverImage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	////////////////////////////////////////////////////////////////
	ActionListener linstener;
	public void addActionListener(ActionListener linstener)
	{
		this.linstener = linstener;
	}
	
	ActionEvent changeAction = new ActionEvent(this, EVT_CHANGED, null);
	ActionEvent dblclickAction = new ActionEvent(this, EVT_DBLCLICK, null);
	ActionEvent pageDownAction = new ActionEvent(this, EVT_PAGE_DOWN, null);
	ActionEvent pageUpAction = new ActionEvent(this, EVT_PAGE_UP, null);
	
	////////////////////////////////////////////////////////////////
	//Events
	////////////////////////////////////////////////////////////////
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
					bookInfo.coverFileName = files.get(0).getAbsolutePath();
					bookInfo.loadCoverImage(bookInfo.coverFileName);
					bookInfo.coverImageIndex = -1;
					this.fitType = FIT_ZOOM;
					this.setFitType(FIT_ALL, true);
					repaint();
					if (this.linstener != null) this.linstener.actionPerformed(this.changeAction);
					return;
				}
			}
			if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				bookInfo.coverFileName = transfer.getTransferData(DataFlavor.stringFlavor).toString();
				if (bookInfo.coverFileName.startsWith("file://"))
					bookInfo.coverFileName = URLDecoder.decode(bookInfo.coverFileName.substring(0, bookInfo.coverFileName.indexOf('\n')-1).substring(7).trim(),"UTF-8");
				bookInfo.loadCoverImage(bookInfo.coverFileName);
				bookInfo.coverImageIndex = -1;
				this.setFitType(FIT_ALL, true);
				repaint();
				if (this.linstener != null) this.linstener.actionPerformed(changeAction);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		this.requestFocus();
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.getClickCount() == 2) {
			//2倍表示をコールバック
			if (this.linstener != null) this.linstener.actionPerformed(dblclickAction);
		}
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		this.requestFocus();
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
	}
	@Override
	public void mousePressed(MouseEvent e)
	{
		this.startX = e.getX();
		this.prevX = e.getX();
		this.startX -= this.offsetX;
		this.startY = e.getY();
		if (this.offsetY != 0) this.startY -= this.offsetY;
		this.requestFocus();
		this.mouseType = e.getButton();
		if (e.isControlDown()) this.mouseType = MouseEvent.BUTTON3;
		//中ボタンは縦合わせ
		if (this.mouseType == MouseEvent.BUTTON2) {
			setFitType(FIT_H, false);
			if (this.linstener != null) this.linstener.actionPerformed(changeAction);
		}
	}
	@Override
	public void mouseReleased(MouseEvent e)
	{
		this.startX = Integer.MIN_VALUE;
		if (this.linstener != null) this.linstener.actionPerformed(changeAction);
	}
	/** マウスドラッグイベント */
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (this.startX != Integer.MIN_VALUE) {
			if (this.mouseType == MouseEvent.BUTTON3) {
				setVisibleWidthOffset(e.getX()-this.prevX);
			} else {
				this.offsetX = e.getX()-this.startX;
				this.offsetY = e.getY()-this.startY;
			}
			repaint();
			this.prevX = e.getX();
		}
	}
	/** マウス移動イベント */
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}
	
	/** マウスホイールで拡大縮小 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		double rate = 1.01;
		if (e.isControlDown()) rate = 1.001;
		else if (e.isShiftDown()) rate = 1.05;
		if (e.getWheelRotation() > 0) {
			this.setZoom(1/rate);
		} else {
			this.setZoom(rate);
		}
		if (this.linstener != null) this.linstener.actionPerformed(changeAction);
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode()) {
		case KeyEvent.VK_PAGE_DOWN:
			if (this.linstener != null) this.linstener.actionPerformed(pageDownAction);
			return;
		case KeyEvent.VK_PAGE_UP:
			if (this.linstener != null) this.linstener.actionPerformed(pageUpAction);
			return;
		}
		
		if (previewImage == null) return;
		int delta = 1;
		if (e.isControlDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT: setVisibleWidthOffset(-1); break;
			case KeyEvent.VK_RIGHT: setVisibleWidthOffset(1); break;
			default: return;
			}
		} else {
			if (e.isShiftDown()) delta = 5;
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP: this.offsetY-=delta; break;
			case KeyEvent.VK_DOWN: this.offsetY+=delta; break;
			case KeyEvent.VK_LEFT: this.offsetX-=delta; break;
			case KeyEvent.VK_RIGHT: this.offsetX+=delta; break;
			case KeyEvent.VK_HOME: this.offsetX=0; break;
			default: return;
			}
		}
		repaint();
	}
	@Override
	public void keyReleased(KeyEvent arg0)
	{
	}
	@Override
	public void keyTyped(KeyEvent e)
	{
	}
}