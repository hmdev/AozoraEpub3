package com.github.hmdev.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * 表紙画像プレビューとトリミング用パネル
 */
public class JCoverImagePanel extends JPanel implements MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	
	private BufferedImage image; 
	
	public JCoverImagePanel()
	{
		this.addMouseMotionListener(this);
	}
	
	/** パネル描画 */
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		Graphics2D g2 = (Graphics2D)g;
		if (this.image != null) {
			double scale = Math.min((double)this.getWidth()/image.getWidth(), (double)this.getHeight()/image.getHeight());
			AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_BICUBIC);
			int x = (int)Math.max(0, (this.getWidth()-image.getWidth()*scale)/2);
			g2.drawImage(image, ato, x ,0);
		}
	}
	
	/** パネルをクリア */
	public void clear()
	{
		this.image = null;
		this.repaint();
	}
	
	/** ファイルまたはURLの文字列から画像を読み込んで表紙イメージとして設定 */
	public boolean loadImage(String path)
	{
		try {
			InputStream is;
			if (path.startsWith("http")) {
				is = new BufferedInputStream(new URL(path).openStream());
			} else {
				File file = new File(path);
				if (!file.exists()) return false;
				is = new BufferedInputStream(new FileInputStream(file));
			}
			return loadImage(is);
		} catch (Exception e) { return false; }
	}
	/** 画像を読み込んで表紙イメージとして設定 */
	public boolean loadImage(InputStream is)
	{
		try {
			setImage(ImageIO.read(is));
		} catch (Exception e) { return false; }
		return true;
	}
	/** 画像ファイルを設定 */
	public void setImage(BufferedImage image)
	{
		this.image = image;
		this.repaint();
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