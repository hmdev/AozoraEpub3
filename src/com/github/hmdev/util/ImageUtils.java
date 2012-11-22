package com.github.hmdev.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.github.hmdev.info.ImageInfo;
//import com.objectplanet.image.PngEncoder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

public class ImageUtils
{
	/** 4bitグレースケール時のRGB階調カラーモデル Singleton */
	static ColorModel GRAY16_COLOR_MODEL;
	/** 8bitグレースケール時のRGB階調カラーモデル Singleton */
	static ColorModel GRAY256_COLOR_MODEL;
	
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
			ImageDecoder dec = ImageCodec.createImageDecoder("jpeg", is, null);
			RenderedImage ri = dec.decodeAsRenderedImage();
			image = new BufferedImage(ri.getWidth(), ri.getHeight(), BufferedImage.TYPE_INT_RGB);
			image.createGraphics().drawRenderedImage(ri, NO_TRANSFORM);
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
	 * @param autoMarginLimitH 余白除去 最大%
	 * @param autoMarginLimitV 余白除去 最大%
	 * @param autoMarginWhiteLevel 白画素として判別する白さ 100が白
	 * @param autoMarginPadding 余白除去後に追加するマージン */
	static public void writeImage(InputStream is, BufferedImage srcImage, ZipArchiveOutputStream zos, ImageInfo imageInfo,
			float jpegQuality, int maxImagePixels, int maxImageW, int maxImageH,
			int autoMarginLimitH, int autoMarginLimitV, int autoMarginWhiteLevel, float autoMarginPadding) throws IOException
	{
		String ext = imageInfo.getExt();
		
		int w = imageInfo.getWidth();
		int h = imageInfo.getHeight();
		
		int[] margin = null;
		if (autoMarginLimitH > 0 || autoMarginLimitV > 0) {
			int startPixel = (int)(w*0.01); //1%
			int ignorePixels = (int)(w*0.005); //0.5%
			
			//画像がなければ読み込み
			if (srcImage == null) srcImage = readImage(ext, is);
			margin = getPlainMargin(srcImage, autoMarginLimitH/100f, autoMarginLimitV/100f, autoMarginWhiteLevel/100f, autoMarginPadding/100f, startPixel, ignorePixels);
			if (margin[0]==0 && margin[1]==0 && margin[2]==0 && margin[3]==0) margin = null;
			if (margin != null) {
				w = w-margin[0]-margin[2];
				h = h-margin[1]-margin[3];
			}
		}
		//倍率取得
		double scale = 1;
		if (maxImagePixels >= 10000) scale = Math.sqrt((double)maxImagePixels/(w*h)); //最大画素数指定
		if (maxImageW > 0) scale = Math.min(scale, (double)maxImageW/w); //最大幅指定
		if (maxImageH > 0) scale = Math.min(scale, (double)maxImageH/h); //最大高さ指定
		
		if (scale >= 1) {
			if (srcImage == null) {
				//変更なしならそのままファイル出力
				IOUtils.copy(is, zos);
			} else {
				//編集済の画像がある場合 同じ形式で書き出し
				_writeImage(zos, srcImage, ext, margin, jpegQuality);
			}
		} else {
			//縮小
			int scaledW = (int)(w*scale+0.5);
			int scaledH = (int)(h*scale+0.5);
			try {
				//画像がなければ読み込み
				if (srcImage == null) srcImage = readImage(ext, is);
				int imageType = srcImage.getType();
				BufferedImage outImage;
				ColorModel colorModel;
				WritableRaster raster;
				switch (imageType) {
				case BufferedImage.TYPE_BYTE_BINARY:
					colorModel = srcImage.getColorModel();
					colorModel = getGray16ColorModel();
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
				case BufferedImage.TYPE_BYTE_INDEXED:
					colorModel = srcImage.getColorModel();
					raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
					outImage = new BufferedImage(colorModel, raster, true, null);
					break;
				default:
					outImage = new BufferedImage(scaledW, scaledH, imageType);
				}
				Graphics2D g = outImage.createGraphics();
				try {
					AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
					AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
					if (margin == null) g.drawImage(srcImage, ato, 0, 0);
					else g.drawImage(srcImage, ato, (int)(-margin[0]*scale+0.5), (int)(-margin[1]*scale+0.5));
				} finally {
					g.dispose();
				}
				//ImageIO.write(outImage, imageInfo.getExt(), zos);
				_writeImage(zos, outImage, ext, null, jpegQuality);
				LogAppender.append("画像縮小: "+imageInfo.getOutFileName()+" ("+w+","+h+")→("+scaledW+","+scaledH+")\n");
			} catch (Exception e) {
				LogAppender.append("画像読み込みエラー: "+imageInfo.getOutFileName()+"\n");
				e.printStackTrace();
			}
			zos.flush();
		}
	}
	/** 画像を出力 マージン指定があればカット
	 * @param margin カットするピクセル数(left, top, right, bottom) */
	static private void _writeImage(ZipArchiveOutputStream zos, BufferedImage srcImage, String ext, int[] margin, float jpegQuality) throws IOException
	{
		/*if ("png".equals(ext)) {
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
			pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
			if (margin != null) srcImage = srcImage.getSubimage(margin[0], margin[1], srcImage.getWidth()-margin[2]-margin[0], srcImage.getHeight()-margin[3]-margin[1]);
			pngEncoder.encode(srcImage, zos);
		} else {*/
			ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(ext).next();
			ImageWriteParam iwp = imageWriter.getDefaultWriteParam();
			if (iwp.canWriteCompressed()) {
				try {
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					if (ext.charAt(0) == 'j') iwp.setCompressionQuality(jpegQuality);
				} catch (Exception e) { e.printStackTrace(); }
			}
			if (margin != null) srcImage = srcImage.getSubimage(margin[0], margin[1], srcImage.getWidth()-margin[2]-margin[0], srcImage.getHeight()-margin[3]-margin[1]);
			imageWriter.setOutput(ImageIO.createImageOutputStream(zos));
			imageWriter.write(null, new IIOImage(srcImage, null, null), iwp);
		//}
		zos.flush();
	}
	
	/** 余白の画素数取得
	 * @param image 余白を検出する画像
	 * @param limit 余白検出制限 0.0-1.0
	 * @param whiteLevel 余白と判別する白レベル
	 * @param startPixel 余白をチェック開始しする位置 初回が余白なら中へ違えば外が余白になるまで増やす
	 * @param ignorePixels 連続で余白でなければ無視するピクセル数
	 * @return 余白画素数(left, top, right, bottom) */
	static private int[] getPlainMargin(BufferedImage image, float limitH, float limitV, float whiteLevel, float padding, int startPixel, int ignorePixels)
	{
		int[] margin = new int[4]; //left, top, right, bottom
		int width = image.getWidth();
		int height = image.getHeight();
		
		//余白除去後に追加する余白 (削れ過ぎるので最低で2にしておく)
		int paddingH = Math.max(2, (int)(width*padding));
		int paddingV = Math.max(2, (int)(height*padding));
		
		//除去制限をピクセルに変更 上下、左右それぞれでの最大値
		int limitPxH = (int)(width*limitH);
		int limitPxV = (int)(height*limitV);
		
		//余白ではないpx数 余白の前がignorePixels以下ならゴミとして無視する
		int noPlainCount = 0;
		//初回判別
		
		for (int i=0; i<=limitPxH; i++) {
			double whiteRate = getWhiteRateV(image, height, i, whiteLevel, 0.95);
			if (whiteRate < 0.999) {
				noPlainCount++;
				if (whiteRate < 0.95 || noPlainCount > ignorePixels) break;
			} else
				margin[0] = i;//left
		}
		noPlainCount = 0;
		for (int i=0; i<=limitPxV; i++) { 
			double whiteRate = getWhiteRateH(image, width, i, whiteLevel, 0.95);
			if (whiteRate < 0.999) {
				noPlainCount++;
				if (whiteRate < 0.95 || noPlainCount > ignorePixels) break;
			} else 
				margin[1] = i;//top
		}
		noPlainCount = 0;
		for (int i=0; i<=limitPxH; i++) {
			double whiteRate = getWhiteRateV(image, height, width-1-i, whiteLevel, 0.95);
			if (whiteRate < 0.999) {
				noPlainCount++;
				if (whiteRate < 0.95 || noPlainCount > ignorePixels) break;
			} else
				margin[2] = i;//right
		}
		noPlainCount = 0;
		for (int i=0; i<=limitPxV; i++) {
			double whiteRate = getWhiteRateH(image, width, height-1-i, whiteLevel, 0.95);
			if (whiteRate < 0.999) {
				noPlainCount++;
				if (whiteRate < 0.95 || noPlainCount > ignorePixels) break;
			} else
				margin[3] = i;//bottom
		}
		//上下、左右の合計が制限を超えていたら調整
		if (margin[0]+margin[2] > limitPxH) {
			double rate = (double)limitPxH/(margin[0]+margin[2]);
			margin[0] = (int)(margin[0]*rate);
			margin[2] = (int)(margin[2]*rate);
		}
		if (margin[1]+margin[3] > limitPxV) {
			double rate = (double)limitPxV/(margin[1]+margin[3]);
			margin[1] = (int)(margin[1]*rate);
			margin[3] = (int)(margin[3]*rate);
		}
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
	 * @param limit これよりも白比率が小さくなったら終了 値は0が帰る
	 * @return 白画素の比率 0.0-1.0 */
	static private double getWhiteRateH(BufferedImage image, int w, int offsetY, double whiteLevel, double limit)
	{
		//rgbともこれより大きければ白画素とする
		int rgbLimit = (int)(256*whiteLevel);
		//白でないピクセル数
		int coloredPixels = 0;
		//これよりピクセル数が多くなったら終了
		int limitPixel = (int)(w*(1.0-limit));
		
		for (int x=w-1; x>=0; x--) {
			int rgb = image.getRGB(x, offsetY);
			if (rgbLimit > (rgb>>16 & 0xFF) || rgbLimit > (rgb>>8 & 0xFF) || rgbLimit > (rgb & 0xFF)) coloredPixels++;
		}
		if (limitPixel < coloredPixels) return 0;
		return (double)(w-coloredPixels)/(w);
	}
	/** 指定範囲の白い画素数の比率を返す
	 * @param image 比率をチェックする画像
	 * @param h 比率をチェックする高さ
	 * @param offsetX 画像内の横位置
	 * @param limit これよりも白比率が小さくなったら終了 値はlimitが帰る
	 * @return 白画素の比率 0.0-1.0 */
	static private double getWhiteRateV(BufferedImage image, int h, int offsetX, double whiteLevel, double limit)
	{
		//rgbともこれより大きければ白画素とする
		int rgbLimit = (int)(256*whiteLevel);
		//白でないピクセル数
		int coloredPixels = 0;
		//これよりピクセル数が多くなったら終了
		int limitPixel = (int)(h*(1.0-limit));
		
		for (int y=h-1; y>=0; y--) {
			int rgb = image.getRGB(offsetX, y);
			if (rgbLimit > (rgb>>16 & 0xFF) || rgbLimit > (rgb>>8 & 0xFF) || rgbLimit > (rgb & 0xFF)) coloredPixels++;
			if (limitPixel < coloredPixels) return 0;
		}
		return (double)(h-coloredPixels)/(h);
	}
}
