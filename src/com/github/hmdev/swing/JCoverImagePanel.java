package com.github.hmdev.swing;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.JPanel;

import com.github.hmdev.info.BookInfo;

/**
 * 表紙画像プレビューとトリミング用パネル
 */
public class JCoverImagePanel extends JPanel implements MouseMotionListener, DropTargetListener
{
	private static final long serialVersionUID = 1L;
	
	private BookInfo bookInfo;
	private BufferedImage previewImage; 
	
	private double scale = -1;
	
	public JCoverImagePanel()
	{
		this.addMouseMotionListener(this);
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
	}
	
	/** パネル描画 */
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		//g.clearRect(0, 0, this.getWidth(), this.getHeight());
		if (this.bookInfo != null && this.bookInfo.coverImage != null) {
			double scale = Math.min((double)this.getWidth()/bookInfo.coverImage.getWidth(), (double)this.getHeight()/this.bookInfo.coverImage.getHeight());
			if (this.previewImage == null || this.scale != scale) {
				this.scale = scale;
				this.createPreviewImage(scale);
			}
			if (this.previewImage != null) {
				int x = (int)Math.max(0, (this.getWidth()-this.previewImage.getWidth())/2);
				g.drawImage(this.previewImage, x ,0, this);
			}
		}
	}
	
	/** パネルをクリア */
	public void clear()
	{
		this.bookInfo = null;
		this.previewImage = null;
		this.repaint();
	}
	
	/** 画像ファイルを設定
	 * プレビューはpaintで生成 */
	public void setBookInfo(BookInfo bookInfo)
	{
		this.bookInfo = bookInfo;
		this.previewImage = null;
		this.scale = -1;
		this.repaint();
	}
	/** プレビュー用の小さい画像を生成 */
	private void createPreviewImage(double scale)
	{
		BufferedImage previewImage = new BufferedImage((int)(this.bookInfo.coverImage.getWidth()*scale), (int)(this.bookInfo.coverImage.getHeight()*scale), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = previewImage.createGraphics();
		try {
			AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_BICUBIC);
			g2.drawImage(this.bookInfo.coverImage, ato, 0 ,0);
		} finally {
			g2.dispose();
		}
		this.previewImage = previewImage;
	}
	
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
					if (bookInfo != null) {
						bookInfo.coverFileName = files.get(0).getAbsolutePath();
						bookInfo.loadCoverImage(bookInfo.coverFileName);
					}
					repaint();
				}
			} else {
				for (DataFlavor flavar : flavars) {
					if (flavar.isFlavorTextType()) {
						bookInfo.coverFileName = transfer.getTransferData(DataFlavor.stringFlavor).toString();
						bookInfo.loadCoverImage(bookInfo.coverFileName);
						repaint();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	/** マウスドラッグイベント */
	@Override
	public void mouseDragged(MouseEvent e)
	{
	}
	/** マウス移動イベント */
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}
	
	
}