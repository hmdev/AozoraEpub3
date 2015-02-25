package com.github.hmdev.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.LookupOp;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.github.hmdev.info.ImageInfo;
import com.github.hmdev.util.LogAppender;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

public class ImageUtils
{
	/** 4bitグレースケール時のRGB階調カラーモデル Singleton */
	static ColorModel GRAY16_COLOR_MODEL;
	/** 8bitグレースケール時のRGB階調カラーモデル Singleton */
	static ColorModel GRAY256_COLOR_MODEL;
	
	public static final int NOMBRE_TOP = 1;
	public static final int NOMBRE_BOTTOM = 2;
	public static final int NOMBRE_TOPBOTTOM = 3;
	
	/** png出力用 */
	static ImageWriter pngImageWriter;
	/** jpeg出力用 */
	static ImageWriter jpegImageWriter;
	
	/** 4bitグレースケール時のRGB階調カラーモデル取得 */
	static ColorModel getGray16ColorModel()
	{
		if (GRAY16_COLOR_MODEL == null) {
			byte[] GRAY16_VALUES = new byte[]{
				0,17,34,51,68,85,102,119,-120,-103,-86,-69,-52,-35,-18,-1};
			GRAY16_COLOR_MODEL = new IndexColorModel(4, GRAY16_VALUES.length, GRAY16_VALUES, GRAY16_VALUES, GRAY16_VALUES);
		}
		return GRAY16_COLOR_MODEL;
	}
	/** 8bitグレースケール時のRGB階調カラーモデル取得 */
	static ColorModel getGray256ColorModel()
	{
		if (GRAY256_COLOR_MODEL == null) {
			byte[] GRAY256_VALUES = new byte[]{
				0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,
				32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,
				64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,
				96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,
				-128,-127,-126,-125,-124,-123,-122,-121,-120,-119,-118,-117,-116,-115,-114,-113,-112,-111,-110,-109,-108,-107,-106,-105,-104,-103,-102,-101,-100,-99,-98,-97,
				-96,-95,-94,-93,-92,-91,-90,-89,-88,-87,-86,-85,-84,-83,-82,-81,-80,-79,-78,-77,-76,-75,-74,-73,-72,-71,-70,-69,-68,-67,-66,-65,
				-64,-63,-62,-61,-60,-59,-58,-57,-56,-55,-54,-53,-52,-51,-50,-49,-48,-47,-46,-45,-44,-43,-42,-41,-40,-39,-38,-37,-36,-35,-34,-33,
				-32,-31,-30,-29,-28,-27,-26,-25,-24,-23,-22,-21,-20,-19,-18,-17,-16,-15,-14,-13,-12,-11,-10,-9,-8,-7,-6,-5,-4,-3,-2,-1};
			GRAY256_COLOR_MODEL = new IndexColorModel(8, GRAY256_VALUES.length, GRAY256_VALUES, GRAY256_VALUES, GRAY256_VALUES);
		}
		return GRAY256_COLOR_MODEL;
	}
	
	
	/** ファイルまたはURLの文字列から画像を読み込む
	 * 読み込めなければnull */
	static public BufferedImage loadImage(String path)
	{
		try {
			InputStream is;
			if (path.startsWith("http")) {
				is = new BufferedInputStream(new URL(path).openStream(), 8192);
			} else {
				File file = new File(path);
				if (!file.exists()) return null;
				is = new BufferedInputStream(new FileInputStream(file), 8192);
			}
			return readImage(path.substring(path.lastIndexOf('.')+1).toLowerCase(), is);
		} catch (Exception e) { return null; }
	}
	
	final static AffineTransform NO_TRANSFORM = AffineTransform.getTranslateInstance(0, 0);
	/** ストリームから画像を読み込み */
	static public BufferedImage readImage(String ext, InputStream is) throws IOException
	{
		BufferedImage image;
		if (ext.equals("jpg") || ext.equals("jpeg")) {
			try {
				ImageDecoder dec = ImageCodec.createImageDecoder("jpeg", is, null);
				RenderedImage ri = dec.decodeAsRenderedImage();
				image = new BufferedImage(ri.getWidth(), ri.getHeight(), BufferedImage.TYPE_INT_RGB);
				image.createGraphics().drawRenderedImage(ri, NO_TRANSFORM);
			} catch (Exception e) {
				image = ImageIO.read(is);
			}
		} else {
			image = ImageIO.read(is);
		}
		is.close();
		return image;
	}
	
	/** 大きすぎる画像は縮小して出力
	 * @param is 画像の入力ストリーム srcImageがあれば利用しないのでnull
	 * @param srcImage 読み込み済の場合は画像をこちらに設定 nullならisから読み込む
	 * @param zos 出力先Zipストリーム
	 * @param imageInfo 画像情報
	 * @param jpegQuality jpeg画質 (低画質 0.0-1.0 高画質)
	 * @param maxImagePixels 縮小する画素数
	 * @param maxImageW 縮小する画像幅
	 * @param maxImageH 縮小する画像高さ
	 * @param dispW 画面幅 余白除去後の縦横比補正用
	 * @param dispH 画面高さ 余白除去後の縦横比補正用
	 * @param autoMarginLimitH 余白除去 最大%
	 * @param autoMarginLimitV 余白除去 最大%
	 * @param autoMarginWhiteLevel 白画素として判別する白さ 100が白
	 * @param autoMarginPadding 余白除去後に追加するマージン */
	static public void writeImage(InputStream is, BufferedImage srcImage, ZipArchiveOutputStream zos, ImageInfo imageInfo,
			float jpegQuality, LookupOp gammaOp, int maxImagePixels, int maxImageW, int maxImageH, int dispW, int dispH,
			int autoMarginLimitH, int autoMarginLimitV, int autoMarginWhiteLevel, float autoMarginPadding, int autoMarginNombre, float nombreSize) throws IOException
	{
		try {
		String ext = imageInfo.getExt();
		
		int imgW = imageInfo.getWidth();
		int imgH = imageInfo.getHeight();
		int w = imgW;
		int h = imgH;
		imageInfo.setOutWidth(imgW);
		imageInfo.setOutHeight(imgH);
		//余白チェック時に読み込んだ画像のバッファ
		byte[] imgBuf = null;
		
		//回転とコントラスト調整なら読み込んでおく
		if (srcImage == null && (imageInfo.rotateAngle != 0 || gammaOp != null)) srcImage = readImage(ext, is);
		
		int[] margin = null;
		if (autoMarginLimitH > 0 || autoMarginLimitV > 0) {
			int startPixel = (int)(w*0.01); //1%
			int ignoreEdge = (int)(w*0.03); //3%
			int dustSize = (int)(w*0.01); //1%
			
			//画像がなければ読み込み 変更なしの時にそのまま出力できるように一旦バッファに読み込む
			if (srcImage == null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(is, baos);
				imgBuf = baos.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(imgBuf);
				try { srcImage = readImage(ext, bais); } finally { bais.close(); }
			}
			margin = getPlainMargin(srcImage, autoMarginLimitH/100f, autoMarginLimitV/100f, autoMarginWhiteLevel/100f, autoMarginPadding/100f, startPixel, ignoreEdge, dustSize, autoMarginNombre, nombreSize);
			if (margin[0]==0 && margin[1]==0 && margin[2]==0 && margin[3]==0) margin = null;
			if (margin != null) {
				//元画像が幅か高さかチェック
				int mw = w-margin[0]-margin[2];
				int mh = h-margin[1]-margin[3];
				double dWH = dispW/(double)dispH;
				double mWH = mw/(double)mh;
				//縦横比で画面の横か縦に合わせる方向が変わらないようにマージンを調整する
				if (w/(double)h < dWH) { //元が縦
					if (mWH > dWH && mw > dispW) { //余白除去で横にはみ出す
						mh = (int)(mw/dWH);
						margin[3] = h-margin[1]-mh;//下マージンを伸ばす
						if (margin[3] < 0) { margin[3] = 0; margin[1] = h-mh; }
					}
				} else { //元が横
					if (mWH < dWH && mh > dispH) { //余白除去で縦にはみ出す
						mw = (int)(mh*dWH);
						double mLR = margin[0]+margin[2];
						margin[0] = (int)((w-mw)*margin[0]/mLR);
						margin[2] = (int)((w-mw)*margin[2]/mLR);
					}
				}
				w = mw;
				h = mh;
			}
		}
		//倍率取得
		double scale = 1;
		if (maxImagePixels >= 10000) scale = Math.sqrt((double)maxImagePixels/(w*h)); //最大画素数指定
		if (maxImageW > 0) scale = Math.min(scale, (double)maxImageW/w); //最大幅指定
		if (maxImageH > 0) scale = Math.min(scale, (double)maxImageH/h); //最大高さ指定
		
		if (scale >= 1 && (gammaOp == null || srcImage.getType() == BufferedImage.TYPE_INT_RGB)) {
			if (srcImage == null) {
				//変更なしならそのままファイル出力
				IOUtils.copy(is, zos);
			} else {
				if (margin == null && imgBuf != null && imageInfo.rotateAngle==0) {
					//余白除去が無く画像も編集されていなければバッファからそのまま出力
					ByteArrayInputStream bais = new ByteArrayInputStream(imgBuf);
					try { IOUtils.copy(bais, zos); } finally { bais.close(); }
				} else {
					//編集済の画像なら同じ画像形式で書き出し 余白があれば切り取る
					if (imageInfo.rotateAngle != 0) {
						BufferedImage outImage = new BufferedImage(h, w, srcImage.getType());
						Graphics2D g = outImage.createGraphics();
						try {
							g.setColor(Color.WHITE);
							g.fillRect(0, 0, h, w);
							g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
							g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
							int x = 0; int y = 0;
							AffineTransform at;
							if (imageInfo.rotateAngle == 90) {
								at = AffineTransform.getQuadrantRotateInstance(1, 0, 0);
								at.translate(0, -imgH);
								if (margin != null) {
									x = -margin[3];
									y = -margin[0];
								}
							} else {
								at = AffineTransform.getQuadrantRotateInstance(-1, 0, 0);
								at.translate(-imgW, 0);
								if (margin != null) {
									x = -margin[1];
									y = -margin[2];
								}
							}
							AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
							g.drawImage(srcImage, ato, x, y);
						} finally {
							g.dispose();
						}
						srcImage = outImage;//入れ替え
					} else if (margin != null) srcImage = srcImage.getSubimage(margin[0], margin[1], srcImage.getWidth()-margin[2]-margin[0], srcImage.getHeight()-margin[3]-margin[1]);
					if (gammaOp != null) {
						BufferedImage filterdImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
						srcImage = gammaOp.filter(srcImage, filterdImage);
						srcImage = filterdImage;
					}
					_writeImage(zos, srcImage, ext, jpegQuality);
					imageInfo.setOutWidth(srcImage.getWidth());
					imageInfo.setOutHeight(srcImage.getHeight());
					if (imageInfo.rotateAngle != 0) LogAppender.println("画像回転"+": "+imageInfo.getOutFileName()+" ("+h+","+w+")");
				}
			}
		} else {
			//縮小
			int scaledW = (int)(w*scale+0.5);
			int scaledH = (int)(h*scale+0.5);
			if (imageInfo.rotateAngle != 0) {
				scaledW = (int)(h*scale+0.5);
				scaledH = (int)(w*scale+0.5);
			}
			//画像がなければ読み込み
			if (srcImage == null) srcImage = readImage(ext, is);
			int imageType = srcImage.getType();
			BufferedImage outImage;
			ColorModel colorModel;
			WritableRaster raster;
			switch (gammaOp==null?imageType:BufferedImage.TYPE_INT_RGB) {
			case BufferedImage.TYPE_BYTE_BINARY:
				colorModel = srcImage.getColorModel();
				colorModel = getGray16ColorModel();
				raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
				outImage = new BufferedImage(colorModel, raster, true, null);
				break;
			case BufferedImage.TYPE_BYTE_INDEXED:
				colorModel = srcImage.getColorModel();
				raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
				outImage = new BufferedImage(colorModel, raster, true, null);
				break;
			/*case BufferedImage.TYPE_BYTE_GRAY:
				//PngEncoderのGRAYが薄くなるのでindexにする
				colorModel = srcImage.getColorModel();
				if (colorModel.getPixelSize() <= 4) colorModel = getGray16ColorModel();
				else colorModel = getGray256ColorModel();
				raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
				outImage = new BufferedImage(colorModel, raster, true, null);
				break;*/
			case BufferedImage.TYPE_BYTE_GRAY:
				outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_BYTE_GRAY);
				break;
			case BufferedImage.TYPE_USHORT_GRAY:
				outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_USHORT_GRAY);
				break;
			default:
				outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
			}
			Graphics2D g = outImage.createGraphics();
			try {
				if (imageType == BufferedImage.TYPE_BYTE_BINARY && imageType == BufferedImage.TYPE_BYTE_INDEXED && imageType == BufferedImage.TYPE_INT_ARGB) {
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, scaledW, scaledH);
				}
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
				int x = 0;
				int y = 0;
				if (imageInfo.rotateAngle == 0) {
					if (margin != null) {
						x = (int)(-margin[0]*scale+0.5);
						y = (int)(-margin[1]*scale+0.5);
					}
				} else if (imageInfo.rotateAngle == 90) {
					at.rotate(Math.toRadians(imageInfo.rotateAngle), 0, 0);
					at.translate(0, -imgH);
					if (margin != null) {
						x = (int)(-margin[3]*scale+0.5);
						y = (int)(-margin[0]*scale+0.5);
					}
				} else {
					at.quadrantRotate(-1, 0, 0);
					at.translate(-imgW, 0);
					if (margin != null) {
						x = (int)(-margin[1]*scale+0.5);
						y = (int)(-margin[2]*scale+0.5);
					}
				}
				AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
				g.drawImage(srcImage, ato, x, y);
			} finally {
				g.dispose();
			}
			//ImageIO.write(outImage, imageInfo.getExt(), zos);
			//コントラスト調整
			if (gammaOp != null) {
				BufferedImage filterdImage = new BufferedImage(outImage.getWidth(), outImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				outImage = gammaOp.filter(outImage, filterdImage);
				outImage = filterdImage;
				filterdImage = null;
				//インデックス化
				switch (imageType) {
				case BufferedImage.TYPE_BYTE_BINARY:
					colorModel = srcImage.getColorModel();
					colorModel = getGray16ColorModel();
					raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
					filterdImage = new BufferedImage(colorModel, raster, true, null);
					break;
				case BufferedImage.TYPE_BYTE_INDEXED:
					colorModel = srcImage.getColorModel();
					raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
					filterdImage = new BufferedImage(colorModel, raster, true, null);
					break;
				case BufferedImage.TYPE_BYTE_GRAY:
					filterdImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_BYTE_GRAY);
					break;
				case BufferedImage.TYPE_USHORT_GRAY:
					filterdImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_USHORT_GRAY);
					break;
				}
				if (filterdImage != null) {
					g = filterdImage.createGraphics();
					try {
						g.drawImage(outImage, 0, 0, null);
					} finally {
						g.dispose();
					}
					outImage = filterdImage;
				}
			}
			_writeImage(zos, outImage, ext, jpegQuality);
			imageInfo.setOutWidth(outImage.getWidth());
			imageInfo.setOutHeight(outImage.getHeight());
			if (scale < 1) {
				LogAppender.append("画像縮小");
				if (imageInfo.rotateAngle!=0) LogAppender.append("回転");
				LogAppender.println(": "+imageInfo.getOutFileName()+" ("+w+","+h+")→("+scaledW+","+scaledH+")");
			}
			zos.flush();
		}
		} catch (Exception e) {
			LogAppender.println("画像読み込みエラー: "+imageInfo.getOutFileName());
			e.printStackTrace();
		}
	}
	/** 画像を出力 マージン指定があればカット
	 * @param margin カットするピクセル数(left, top, right, bottom) */
	static private void _writeImage(ZipArchiveOutputStream zos, BufferedImage srcImage, String ext, float jpegQuality) throws IOException
	{
		if ("png".equals(ext)) {
			/*//PNGEncoder kindlegenでエラーになるのと色が反映されない
			PngEncoder pngEncoder = new PngEncoder();
			int pngColorType = PngEncoder.COLOR_TRUECOLOR;
			switch (srcImage.getType()) {
			case BufferedImage.TYPE_BYTE_BINARY:
				pngColorType = PngEncoder.COLOR_INDEXED; break;
			case BufferedImage.TYPE_BYTE_INDEXED:
				pngColorType = PngEncoder.COLOR_INDEXED; break;
			case BufferedImage.TYPE_BYTE_GRAY:
				pngColorType = PngEncoder.COLOR_GRAYSCALE; break;
			}
			pngEncoder.setColorType(pngColorType);
			pngEncoder.setCompression(PngEncoder.BEST_COMPRESSION);
			pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_AUTO);
			pngEncoder.encode(srcImage, zos);
			*/
			//ImageIO.write(srcImage, "PNG", zos);
			ImageWriter imageWriter = getPngImageWriter();
			imageWriter.setOutput(ImageIO.createImageOutputStream(zos));
			imageWriter.write(srcImage);
		} else if ("jpeg".equals(ext) || "jpg".equals(ext)) {
			ImageWriter imageWriter = getJpegImageWriter();
			imageWriter.setOutput(ImageIO.createImageOutputStream(zos));
			ImageWriteParam iwp = imageWriter.getDefaultWriteParam();
			if (iwp.canWriteCompressed()) {
				try {
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					iwp.setCompressionQuality(jpegQuality);
					imageWriter.write(null, new IIOImage(srcImage, null, null), iwp);
				} catch (Exception e) { e.printStackTrace(); }
			} else {
				imageWriter.write(srcImage);
			}
		} else {
			ImageIO.write(srcImage, ext, zos);
		}
		zos.flush();
	}
	
	static private ImageWriter getPngImageWriter()
	{
		if (pngImageWriter != null) return pngImageWriter;
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
		pngImageWriter = writers.next();
		//jai-imageioのpngの挙動がおかしいのでインストールされていても使わない
		if (writers.hasNext() && pngImageWriter.getClass().getName().endsWith("CLibPNGImageWriter")) pngImageWriter = writers.next();
		return pngImageWriter;
	}
	
	static private ImageWriter getJpegImageWriter()
	{
		if (jpegImageWriter!= null) return jpegImageWriter;
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		jpegImageWriter = writers.next();
		return jpegImageWriter;
	}
	
	/** 余白の画素数取得  左右のみずれ調整
	 * @param image 余白を検出する画像
	 * @param limitH 余白のサイズ横 0.0-1.0
	 * @param limitV 余白のサイズ縦 0.0-1.0
	 * @param whiteLevel 余白と判別する白レベル
	 * @param startPixel 余白をチェック開始しする位置 初回が余白なら中へ違えば外が余白になるまで増やす
	 * @param ignoreEdge 行列のチェック時に両端を無視するピクセル数
	 * @param dustSize ゴミのピクセルサイズ
	 * @return 余白画素数(left, top, right, bottom) */
	static private int[] getPlainMargin(BufferedImage image, float limitH, float limitV, float whiteLevel, float padding, int startPixel, int ignoreEdge, int dustSize, int nombreType, float nombreSize)
	{
		int[] margin = new int[4]; //left, top, right, bottom
		int width = image.getWidth();
		int height = image.getHeight();
		
		//rgbともこれより大きければ白画素とする
		int rgbLimit = (int)(256*whiteLevel);
		
		//余白除去後に追加する余白 (削れ過ぎるので最低で1にしておく)
		int paddingH = Math.max(1, (int)(width*padding));
		int paddingV = Math.max(1, (int)(height*padding));
		
		//除去制限をピクセルに変更 上下、左右それぞれでの最大値
		int limitPxH = (int)(width*limitH);//後で合計から中央に寄せる
		int limitPxV = (int)(height*limitV)/2;
		//ノンブルがあった場合の最大マージン
		int limitPxT = (int)(height*limitV)/2;
		int limitPxB = (int)(height*limitV)/2;
		
		if (nombreType == NOMBRE_TOP || nombreType == NOMBRE_TOPBOTTOM) {
			limitPxT += (int)(height*0.05); //5%加算
		}
		if (nombreType == NOMBRE_BOTTOM || nombreType == NOMBRE_TOPBOTTOM) {
			limitPxB += (int)(height*0.05); //5%加算
		}
		
		int ignoreEdgeR = ignoreEdge;
		//int ignoreEdgeR = (int)(width*0.3); //行の少ないページで問題有り
		//上
		int coloredPixels = getColoredPixelsH(image, width, startPixel, rgbLimit, 0, ignoreEdge, ignoreEdgeR, dustSize);
		if (coloredPixels > 0) {//外側へ
			for (int i=startPixel-1; i>=0; i--) {
				coloredPixels = getColoredPixelsH(image, width, i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, 0);
				margin[1] = i;
				if (coloredPixels == 0) break;
			}
		} else {//内側へ
			margin[1] = startPixel;
			for (int i=startPixel+1; i<=limitPxT; i++) { 
				coloredPixels = getColoredPixelsH(image, width, i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, dustSize);
				if (coloredPixels == 0) margin[1] = i;
				else break;
			}
		}
		//下
		coloredPixels = getColoredPixelsH(image, width, height-1-startPixel, rgbLimit, 0, ignoreEdge, ignoreEdgeR, dustSize);
		if (coloredPixels > 0) {//外側へ
			for (int i=startPixel-1; i>=0; i--) {
				coloredPixels = getColoredPixelsH(image, width, height-1-i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, 0);
				margin[3] = i;
				if (coloredPixels == 0) break;
			}
		} else {//内側へ
			margin[3] = startPixel;
			for (int i=startPixel+1; i<=limitPxB; i++) {
				coloredPixels = getColoredPixelsH(image, width, height-1-i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, dustSize);
				if (coloredPixels == 0) margin[3] = i;
				else break;
			}
		}
		
		//ノンブル除去
		boolean hasNombreT = false;
		boolean hasNombreB = false;
		if (nombreType == NOMBRE_TOP || nombreType == NOMBRE_TOPBOTTOM) {
			//これ以下ならノンブルとして除去
			int nombreLimit = (int)(height * nombreSize)+margin[1];
			int nombreDust = (int)(height * 0.005);
			//ノンブル上
			int nombreEnd = 0;
			for (int i=margin[1]+1; i<=nombreLimit; i++) { 
				coloredPixels = getColoredPixelsH(image, width, i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, 0);
				if (coloredPixels == 0) { nombreEnd = i; if (nombreEnd-margin[1] > nombreDust) break; } //ノンブル上のゴミは無視
			}
			if (nombreEnd > margin[1]+height*0.005 && nombreEnd <= nombreLimit) { //0.5%-3％以下
				int whiteEnd = nombreEnd;
				int whiteLimit = limitPxT;//+(int)(height*0.05); //5%加算
				for (int i=nombreEnd+1; i<=whiteLimit; i++) { 
					coloredPixels = getColoredPixelsH(image, width, i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, dustSize);
					if (coloredPixels == 0) whiteEnd = i;
					else if (i-nombreEnd > nombreDust) break;
				}
				if (whiteEnd > nombreEnd+height*0.01) { //1%より大きい空白
					margin[1] = whiteEnd;
					hasNombreT = true;
				}
			}
		}
		if (nombreType == NOMBRE_BOTTOM || nombreType == NOMBRE_TOPBOTTOM) {
			//これ以下ならノンブルとして除去
			int nombreLimit = (int)(height * nombreSize)+margin[3];
			int nombreDust = (int)(height * 0.005);
			//ノンブル下
			int nombreEnd = 0;
			for (int i=margin[3]+1; i<=nombreLimit; i++) { 
				coloredPixels = getColoredPixelsH(image, width, height-1-i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, 0);
				if (coloredPixels == 0) { nombreEnd = i; if (nombreEnd-margin[3] > nombreDust) break; } //ノンブル下のゴミは無視
			}
			if (nombreEnd > margin[3]+height*0.005 && nombreEnd <= nombreLimit) { //0.5%-3％以下
				int whiteEnd = nombreEnd;
				int whiteLimit = limitPxB;//+(int)(height*0.05); //5%加算
				for (int i=nombreEnd+1; i<=whiteLimit; i++) { 
					coloredPixels = getColoredPixelsH(image, width, height-1-i, rgbLimit, 0, ignoreEdge, ignoreEdgeR, dustSize);
					if (coloredPixels == 0) whiteEnd = i;
					else if (i-nombreEnd > nombreDust) break;
				}
				if (whiteEnd > nombreEnd+height*0.01) { //1%より大きい空白
					margin[3] = whiteEnd;
					hasNombreB = true;
				}
			}
		}
		
		//左右にノンブル分反映
		int ignoreTop = Math.max(ignoreEdge, margin[1]);
		int ignoreBottom = Math.max(ignoreEdge, margin[3]);
		//左
		coloredPixels = getColordPixelsV(image, height, startPixel, rgbLimit, 0, ignoreTop, ignoreBottom, dustSize);
		if (coloredPixels > 0) {//外側へ
			for (int i=startPixel-1; i>=0; i--) {
				coloredPixels = getColordPixelsV(image, height, i, rgbLimit, 0, ignoreTop, ignoreBottom, 0);
				margin[0] = i;
				if (coloredPixels == 0) break;
			}
		} else {//内側へ
			margin[0] = startPixel;
			for (int i=startPixel+1; i<=limitPxH; i++) {
				coloredPixels = getColordPixelsV(image, height, i, rgbLimit, 0, ignoreTop, ignoreBottom, dustSize);
				if (coloredPixels == 0) margin[0] = i;
				else break;
			}
		}
		//右
		coloredPixels = getColordPixelsV(image, height, width-1-startPixel, rgbLimit, 0, ignoreTop, ignoreBottom, dustSize);
		if (coloredPixels > 0) {//外側へ
			for (int i=startPixel-1; i>=0; i--) {
				coloredPixels = getColordPixelsV(image, height, width-1-i, rgbLimit, 0, ignoreTop, ignoreBottom, 0);
				margin[2] = i;
				if (coloredPixels == 0) break;
			}
		} else {//内側へ
			margin[2] = startPixel;
			for (int i=startPixel+1; i<=limitPxH; i++) {
				coloredPixels = getColordPixelsV(image, height, width-1-i, rgbLimit, 05, ignoreTop, ignoreBottom, dustSize);
				if (coloredPixels == 0) margin[2] = i;
				else break;
			}
		}
		//左右のカットは小さい方に合わせる
		//if (margin[0] > margin[2]) margin[0] = margin[2];
		//else margin[2] = margin[0];
		
		//左右の合計が制限を超えていたら調整
		if (margin[0]+margin[2] > limitPxH) {
			double rate = (double)limitPxH/(margin[0]+margin[2]);
			margin[0] = (int)(margin[0]*rate);
			margin[2] = (int)(margin[2]*rate);
		}
		/*if (margin[1]+margin[3] > limitPxV) {
			double rate = (double)limitPxV/(margin[1]+margin[3]);
			margin[1] = (int)(margin[1]*rate);
			margin[3] = (int)(margin[3]*rate);
		}*/
		
		//ノンブルがなければ指定値以下にする
		if (!hasNombreT) margin[1] = Math.min(margin[1], limitPxV);
		if (!hasNombreB) margin[3] = Math.min(margin[3], limitPxV);
		
		//余白分広げる
		margin[0] -= paddingH; if (margin[0] < 0) margin[0] = 0;
		margin[1] -= paddingV; if (margin[1] < 0) margin[1] = 0;
		margin[2] -= paddingH; if (margin[2] < 0) margin[2] = 0;
		margin[3] -= paddingV; if (margin[3] < 0) margin[3] = 0;
		return margin;
	}
	
	/** 指定範囲の白い画素数の比率を返す
	 * @param image 比率をチェックする画像
	 * @param w 比率をチェックする幅
	 * @param offsetY 画像内の縦位置
	 * @param limitPixel これよりも黒部分が多かったら終了 値はlimit+1が帰る
	 * @return 白画素の比率 0.0-1.0 */
	static private int getColoredPixelsH(BufferedImage image, int w, int offsetY, int rgbLimit, int limitPixel, int ignoreEdgeL, int ignoreEdgeR, int dustSize)
	{
		//白でないピクセル数
		int coloredPixels = 0;
		
		for (int x=w-1-ignoreEdgeR; x>=ignoreEdgeL; x--) {
			if (isColored(image.getRGB(x, offsetY), rgbLimit)) {
				//ゴミ除外 ゴミのサイズ分先に移動する
				if (dustSize < 4 || !isDust(image, x, image.getWidth(), offsetY, image.getHeight(), dustSize, rgbLimit)) {
					coloredPixels++;
					if (limitPixel < coloredPixels) return coloredPixels;
				}
			}
		}
		return coloredPixels;
	}
	/** 指定範囲の白い画素数の比率を返す
	 * @param image 比率をチェックする画像
	 * @param h 比率をチェックする高さ
	 * @param offsetX 画像内の横位置
	 * @param limitPixel これよりも白比率が小さくなったら終了 値はlimit+1が帰る
	 * @return 白画素の比率 0.0-1.0 */
	static private int getColordPixelsV(BufferedImage image, int h, int offsetX, int rgbLimit, int limitPixel, int ignoreTop, int ignoreBotttom, int dustSize)
	{
		//白でないピクセル数
		int coloredPixels = 0;
		
		for (int y=h-1-ignoreBotttom; y>=ignoreTop; y--) {
			if (isColored(image.getRGB(offsetX, y), rgbLimit)) {
				//ゴミ除外 ゴミのサイズ分先に移動する
				if (dustSize < 4 || !isDust(image, offsetX, image.getWidth(), y, image.getHeight(), dustSize, rgbLimit)) {
					coloredPixels++;
					if (limitPixel < coloredPixels) return coloredPixels;
				}
			}
		}
		return coloredPixels;
	}
	
	static boolean isColored(int rgb, int rgbLimit)
	{
		return rgbLimit > (rgb>>16 & 0xFF) || rgbLimit > (rgb>>8 & 0xFF) || rgbLimit > (rgb & 0xFF);
	}
	
	/** ゴミをチェック */
	static boolean isDust(BufferedImage image, int curX, int maxX, int curY, int maxY, int dustSize, int rgbLimit)
	{
		if (dustSize == 0) return false;
		
		//ゴミサイズの縦横2倍の範囲
		int minX = Math.max(0, curX-dustSize-1);
		maxX = Math.min(maxX, curX+dustSize+1);
		int minY = Math.max(0, curY-dustSize-1);
		maxY = Math.min(maxY, curY+dustSize+1);
		
		//現在列
		int h = 1;
		for (int y=curY-1; y>=minY; y--) {
			if (isColored(image.getRGB(curX, y), rgbLimit)) h++; else break;
		}
		for (int y=curY+1; y<maxY; y++) {
			if (isColored(image.getRGB(curX, y), rgbLimit)) h++; else break;
		}
		if (h > dustSize) return false;
		
		int w = 1;
		for (int x=curX-1; x>=minX; x--) {
			if (isColored(image.getRGB(x, curY), rgbLimit)) w++; else break;
		}
		for (int x=curX+1; x<maxX; x++) {
			if (isColored(image.getRGB(x, curY), rgbLimit)) w++; else break;
		}
		if (w > dustSize) return false;
		
		//左
		w = 1; //黒画素のある幅
		for (int x=curX-1; x>=minX; x--) {
			h = 0;
			for (int y=maxY-1; y>=minY; y--) {
				if (isColored(image.getRGB(x, y), rgbLimit)) h++;
			}
			if (h > dustSize) return false;
			if (h == 0) break; //すべて白なら抜ける
			w++;
		}
		//右
		for (int x=curX+1; x<maxX; x++) {
			h = 0;
			for (int y=maxY-1; y>=minY; y--) {
				if (isColored(image.getRGB(x, y), rgbLimit)) h++;
			}
			if (h > dustSize) return false;
			if (h == 0) break; //すべて白なら抜ける
			w++;
		}
		if (w > dustSize) return false;
		//上
		h = 1; //黒画素のある高さ
		for (int y=curY-1; y>=minY; y--) {
			w = 0;
			for (int x=maxX-1; x>=minX; x--) {
				if (isColored(image.getRGB(x, y), rgbLimit)) w++;
			}
			if (w > dustSize) return false;
			if (w == 0) break; //すべて白なら抜ける
			h++;
		}
		//下
		for (int y=curY+1; y<maxY; y++) {
			w = 0;
			for (int x=maxX-1; x>=minX; x--) {
				if (isColored(image.getRGB(x, y), rgbLimit)) w++;
			}
			if (w > dustSize) return false;
			if (w == 0) break; //すべて白なら抜ける
			h++;
		}
		return h <= dustSize;
	}
}
