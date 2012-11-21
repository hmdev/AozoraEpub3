package com.github.hmdev.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.github.hmdev.info.ImageInfo;

public class ImageUtils
{
	/** 4bitグレースケール時のRGB階調 */
	static final private byte[] GRAY_VALUES = new byte[]{-113,-97,-81,-65,-49,-33,-17,-1,15,31,47,63,79,95,111,127};
	
	/** 大きすぎる画像は縮小して出力
	 * @param srcFileName 画像のファイル名 拡張子取得
	 * @param is 画像の入力ストリーム
	 * @param srcImage 読み込み済の場合は画像をこちらに設定 isは利用しないのでnullでOK
	 * @param zos 出力先Zip
	 * @param imageInfo 画像情報
	 * @param jpegQuality jpeg画質 (低画質 0.0-1.0 高画質)
	 * @param maxImagePixels 縮小する画素数
	 * @param maxImageW 縮小する画像幅
	 * @param maxImageH 縮小する画像高さ
	 * @param autoMarginLimitH 余白除去 最大%
	 * @param autoMarginLimitV 余白除去 最大%
	 * @param autoMarginWhiteLevel 白画素として判別する白さ 100が白
	 * @param autoMarginPadding 余白除去後に追加するマージン */
	static public void writeImage(String srcFileName, InputStream is, BufferedImage srcImage, ZipArchiveOutputStream zos, ImageInfo imageInfo,
			float jpegQuality, int maxImagePixels, int maxImageW, int maxImageH,
			int autoMarginLimitH, int autoMarginLimitV, int autoMarginWhiteLevel, float autoMarginPadding) throws IOException
	{
		int w = imageInfo.getWidth();
		int h = imageInfo.getHeight();
		String ext = srcFileName.substring(srcFileName.lastIndexOf('.')+1).toLowerCase();
		
		int[] margin = null;
		if (autoMarginLimitH > 0 || autoMarginLimitV > 0) {
			//画像がなければ読み込み
			if (srcImage == null) srcImage = ImageInfoReader.readImage(ext, is);
			int ignorePixels = (int)(w*0.005); //0.5%
			margin = getPlainMargin(srcImage, autoMarginLimitH/100f, autoMarginLimitV/100f, autoMarginWhiteLevel/100f, autoMarginPadding/100f, ignorePixels);
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
				IOUtils.copy(is, zos);
			} else {
				//編集済の画像がある場合 同じ形式で書き出し
				_writeImage(zos, srcImage, imageInfo.getExt(), margin, jpegQuality);
			}
		} else {
			//縮小
			int scaledW = (int)(w*scale);
			int scaledH = (int)(h*scale);
			try {
				//画像がなければ読み込み
				if (srcImage == null) srcImage = ImageInfoReader.readImage(ext, is);
				int imageType = srcImage.getType();
				BufferedImage outImage;
				if (imageType == BufferedImage.TYPE_BYTE_BINARY) {
					ColorModel colorModel = srcImage.getColorModel();
					colorModel = new IndexColorModel(4, GRAY_VALUES.length, GRAY_VALUES, GRAY_VALUES, GRAY_VALUES);
					WritableRaster raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
					outImage = new BufferedImage(colorModel, raster, true, null);
				}
				else if (imageType == BufferedImage.TYPE_BYTE_INDEXED) {
					ColorModel colorModel = srcImage.getColorModel();
					WritableRaster raster = colorModel.createCompatibleWritableRaster(scaledW, scaledH);
					outImage = new BufferedImage(colorModel, raster, true, null);
				} else {
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
				_writeImage(zos, outImage, imageInfo.getExt(), margin, jpegQuality);
				LogAppender.append("画像縮小: "+imageInfo.getOutFileName()+" ("+w+","+h+")→("+scaledW+","+scaledH+")\n");
			} catch (Exception e) {
				LogAppender.append("画像読み込みエラー: "+imageInfo.getOutFileName()+"\n");
				e.printStackTrace();
			}
			zos.flush();
		}
	}
	/** 画像を出力 圧縮率指定
	 * @throws IOException */
	static private void _writeImage(ZipArchiveOutputStream zos, BufferedImage srcImage, String ext, int[] margin, float jpegQuality) throws IOException
	{
		ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(ext).next();
		ImageWriteParam iwp = imageWriter.getDefaultWriteParam();
		if (iwp.canWriteCompressed()) {
			try {
				iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				if (ext.charAt(0) == 'j') iwp.setCompressionQuality(jpegQuality);
				//else if (ext.startsWith("png")) iwp.setCompressionQuality(0); //PNGは圧縮に対応していない
			} catch (Exception e) { e.printStackTrace(); }
		}
		if (margin != null) srcImage = srcImage.getSubimage(margin[0], margin[1], srcImage.getWidth()-margin[2]-margin[0], srcImage.getHeight()-margin[3]-margin[1]);
		imageWriter.setOutput(ImageIO.createImageOutputStream(zos));
		imageWriter.write(null, new IIOImage(srcImage, null, null), iwp);
		zos.flush();
	}
	
	/** 余白の画素数取得
	 * @param image 余白を検出する画像
	 * @param limit 余白検出制限 0.0-1.0
	 * @param whiteLevel 余白と判別する白レベル
	 * @param ignorePixels 連続で余白でなければ無視するピクセル数
	 * @return 余白画素数(left, top, right, bottom) */
	static private int[] getPlainMargin(BufferedImage image, float limitH, float limitV, float whiteLevel, float padding, int ignorePixels)
	{
		int[] margin = new int[4]; //left, top, right, bottom
		int width = image.getWidth();
		int height = image.getHeight();
		
		//余白除去後に追加する余白 (削れ過ぎるので最低で2にしておく)
		int paddingH = Math.max(2, (int)(width*padding));
		int paddingV = Math.max(2, (int)(height*padding));
		
		//ピクセルに変更 上下、左右それぞれの
		int limitPxH = (int)(width*limitH);
		int limitPxV = (int)(height*limitV);
		
		//この列数以下ならゴミとして無視する
		int noPlainCount = 0;
		
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
