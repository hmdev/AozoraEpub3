package com.github.hmdev.converter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import com.github.hmdev.info.BookInfo;
import com.github.hmdev.writer.Epub3Writer;

public class AozoraEpub3ConverterTest
{
	static AozoraEpub3Converter converter;
	
	@Test
	public void test()
	{
		Epub3Writer writer = new Epub3Writer("");
		try {
			converter = new AozoraEpub3Converter(writer, "");
			converter.writer = new TestEpub3Writer("");
			converter.bookInfo = new BookInfo(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class TestEpub3Writer extends Epub3Writer
	{
		public TestEpub3Writer(String templatePath)
		{
			super(templatePath);
		}
		public String getImageFilePath(String srcImageFileName, int lineNum) throws IOException
		{
			return "test.png";
		}
	}
	
	@Test
	public void testConvertTextLineToEpub3()
	{
		try {
			String str;
			str = converter.convertTitleLineToEpub3(converter.convertGaijiChuki("｜ルビ※［＃米印］《るび》※［＃米印］※［＃始め二重山括弧］※［＃終わり二重山括弧］", true, true));
			System.out.println(str);
			
			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			String line = converter.convertGaijiChuki("外字の後のルビ※［＃（外字.tif）］《がいじ》", true, true);
			System.out.println(line);
			converter.convertTextLineToEpub3(bw, line, 0, false, false);
			bw.close();
			System.out.println(sw.toString());
			
			sw = new StringWriter();
			bw = new BufferedWriter(sw);
			line = converter.convertGaijiChuki("外字の後の｜ルビ※［＃（外字.tif）］《がいじ》", true, true);
			System.out.println(line);
			converter.convertTextLineToEpub3(bw, line, 0, false, false);
			bw.close();
			System.out.println(sw.toString());
			
			sw = new StringWriter();
			bw = new BufferedWriter(sw);
			line = converter.convertGaijiChuki("※［＃（外字.tif）］《がいじ》", true, true);
			System.out.println(line);
			converter.convertTextLineToEpub3(bw, line, 0, false, false);
			bw.close();
			System.out.println(sw.toString());
			
			sw = new StringWriter();
			bw = new BufferedWriter(sw);
			line = converter.convertGaijiChuki("外字の後の｜ルビ《るび》※［＃（外字.tif）］《るび》", true, true);
			System.out.println(line);
			converter.convertTextLineToEpub3(bw, line, 0, false, false);
			bw.close();
			System.out.println(sw.toString());
			
			sw = new StringWriter();
			bw = new BufferedWriter(sw);
			line = converter.convertGaijiChuki("その上方に※［＃逆三角形と三角形が向き合っている形（fig1317_26.png、横26×縦59）入る］《デアボロ》", true, true);
			System.out.println(line);
			converter.convertTextLineToEpub3(bw, line, 0, false, false);
			bw.close();
			System.out.println(sw.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConvertRubyText()
	{
		try {
			converter.vertical = true;
			StringBuilder buf;
			buf = converter.convertRubyText("※《29※》");
			System.out.println(buf);
			buf = converter.convertRubyText("※※＃※》");
			System.out.println(buf);
			
			buf = converter.convertRubyText("｜※｜縦線《たてせん》※｜");
			System.out.println(buf);
			Assert.assertEquals(buf.toString(), "<ruby>｜縦線<rt>たてせん</rt></ruby>｜");
			buf = converter.convertRubyText("※｜縦線《たてせん》※｜");
			System.out.println(buf);
			Assert.assertEquals(buf.toString(), "｜<ruby>縦線<rt>たてせん</rt></ruby>｜");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConvertGaijiChuki()
	{
		try {
			String str;
			str = converter.convertGaijiChuki("｜※［＃縦線］縦線※［＃縦線］《※［＃縦線］たてせん※［＃縦線］》", true, true);
			System.out.println(str);
			Assert.assertEquals(str, "｜※｜縦線※｜《※｜たてせん※｜》");
			
			str = converter.convertGaijiChuki("※［＃U+845b］U+845b", true, true);
			System.out.println(str);
			Assert.assertEquals(str, "葛U+845b");
			str = converter.convertGaijiChuki("※［＃u+845b-e0100］u+845b-e0100", true, true);
			System.out.println(str);
			Assert.assertEquals(str, "葛󠄀u+845b-e0100");
			str = converter.convertGaijiChuki("※［＃U+845b-U+e0100］U+845b-U+ue0100", true, true);
			System.out.println(str);
			Assert.assertEquals(str, "葛󠄀U+845b-U+ue0100");
			str = converter.convertGaijiChuki("※［＃「葛の異体字」、U+845b-e0100］「葛の異体字」、U+845b-e0100", true, true);
			System.out.println(str);
			Assert.assertEquals(str, "葛󠄀「葛の異体字」、U+845b-e0100");
			str = converter.convertGaijiChuki("※［＃「葛の異体字」、u+845b-u+e0100］「葛の異体字」、u+845b-u+e0100", true, true);
			System.out.println(str);
			Assert.assertEquals(str, "葛󠄀「葛の異体字」、u+845b-u+e0100");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReplaceChukiSufTag()
	{
		try {
			String str;
			
			str = converter.replaceChukiSufTag(converter.convertGaijiChuki("蕎麦《そば》の延びたのと、人間の間《ま》が抜けたのは由来たのもしくないもんだよ」と薬味《やくみ》をツユ［＃「ツユ」に傍点］の中へ入れて無茶苦茶に掻《か》き廻わす。「君そんなに山葵《わさび》を入れると辛《か》らいぜ」と主人は心配そうに注意した。「蕎麦はツユ［＃「ツユ」に傍点］と山葵で食うもんだあね。君は蕎麦が嫌いなんだろう」「僕は饂飩《うどん》が好きだ」「饂飩は馬子《まご》が食うもんだ。蕎麦の味を解しない人ほど気の毒な事はない」と云いながら杉箸《すぎばし》をむざと突き込んで出来るだけ多くの分量を二寸ばかりの高さにしゃくい上げた。「奥さん蕎麦を食うにもいろいろ流儀がありますがね。初心《しょしん》の者に限って、無暗《むやみ》にツユ［＃「ツユ」に傍点］を着けて、そうして口の内でくちゃくちゃやっていますね。あれじゃ蕎麦の味はないですよ。何でも、こう、一《ひ》としゃくいに引っ掛けてね」と云いつつ箸を上げると、長い奴が勢揃《せいぞろ》いをして一尺ばかり空中に釣るし上げられる。迷亭先生もう善かろうと思って下を見ると、まだ十二三本の尾が蒸籠の底を離れないで簀垂《すだ》れの上に纏綿《てんめん》している。「こいつは長いな、どうです奥さん、この長さ加減は」とまた奥さんに相の手を要求する。奥さんは「長いものでございますね」とさも感心したらしい返事をする。「この長い奴へツユ［＃「ツユ」に傍点］を三分一《さんぶいち》つけて、一口に飲んでしまうんだね。噛《か》んじゃいけない。噛んじゃ蕎麦の味がなくなる。つるつると咽喉《のど》を滑《すべ》り込むところがねうちだよ」と思い切って箸《はし》を高く上げると蕎麦はようやくの事で地を離れた。左手《ゆんで》に受ける茶碗の中へ、箸を少しずつ落して、尻尾の先からだんだんに浸《ひた》すと、アーキミジスの理論によって、蕎麦の浸《つか》った分量だけツユ［＃「ツユ」に傍点］の嵩《かさ》が増してくる。ところが茶碗の中には元からツユ［＃「ツユ」に傍点］が八分目｜這入《はい》っているから、迷亭の箸にかかった蕎麦の四半分《しはんぶん》も浸《つか》らない先に茶碗はツユで一杯になってしまった。迷亭の箸は茶碗を去《さ》る五寸の上に至ってぴたりと留まったきりしばらく動かない。動かないのも無理はない。少しでも卸《おろ》せばツユ［＃「ツユ」に傍点］が溢《こぼ》れるばかりである。",true, false));
			System.out.println(str);
			
			str = converter.replaceChukiSufTag(converter.convertGaijiChuki("※［＃始め二重山括弧］１［＃「※［＃米印］※［＃始め二重山括弧］１」は中見出し］",true, false));
			System.out.println(str);
			
			str = converter.replaceChukiSufTag(converter.convertGaijiChuki("　　　　　　あ※［＃米印］※［＃始め二重山括弧］１［＃「あ※［＃米印］※［＃始め二重山括弧］１」は中見出し］",true, false));
			System.out.println(str);
			Assert.assertEquals(str,  "　　　　　　［＃中見出し］あ※※※《１［＃中見出し終わり］");
			
			str = converter.replaceChukiSufTag("星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32図」を指す。］の中に示してある。");
			System.out.println(str);
			Assert.assertEquals(str, "星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32図」を指す。］の中に示してある。");
			str = converter.replaceChukiSufTag("星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32［＃「32」は縦中横］図」を指す。］の中に示してある。");
			System.out.println(str);
			Assert.assertEquals(str, "星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32図」を指す。］の中に示してある。");
			
			str = converter.replaceChukiSufTag("第32［＃「32」は縦中横］図［＃「第32［＃「32」は縦中横］図」は太字］");
			System.out.println(str);
			Assert.assertEquals(str, "［＃太字］第［＃縦中横］32［＃縦中横終わり］図［＃太字終わり］");
			
			str = converter.replaceChukiSufTag("人間は考える｜蘆［＃「人間は考える｜蘆」は太字］《あし》人間は考える｜蘆［＃「考える｜蘆」は太字］《あし》");
			System.out.println(str);
			Assert.assertEquals(str, "［＃太字］人間は考える｜蘆《あし》［＃太字終わり］人間は［＃太字］考える｜蘆《あし》［＃太字終わり］");
			
			str = converter.replaceChukiSufTag("　　　あ１［＃「あ１」は中見出し］");
			System.out.println(str);
			Assert.assertEquals(str, "　　　［＃中見出し］あ１［＃中見出し終わり］");
			
			//2重の「「」」
			str = converter.replaceChukiSufTag("テスト「夕陽新聞」年極購読者に限る［＃「「夕陽新聞」年極購読者に限る」は太字］");
			System.out.println(str);
			Assert.assertEquals(str, "テスト［＃太字］「夕陽新聞」年極購読者に限る［＃太字終わり］");
			
			//注記 変換しない
			str = converter.replaceChukiSufTag(converter.convertGaijiChuki("勝安房守［＃「勝安房守」に「本ト麟太郎※［＃コト、1-2-24］」の注記］", true, false));
			System.out.println(str);
			Assert.assertEquals(str, "勝安房守［＃「勝安房守」に「本ト麟太郎ヿ」の注記］");
			
			//注記 ルビに変換
			converter.chukiRuby = true;
			str = converter.replaceChukiSufTag("［＃５字下げ］第一回　入蔵決心の次第［＃「入蔵決心の次第」に「〔チベット入国の決意〕」の注記］［＃「第一回　入蔵決心の次第」は大見出し］");
			System.out.println(str);
			Assert.assertEquals(str, "［＃５字下げ］［＃大見出し］第一回　｜入蔵決心の次第《〔チベット入国の決意〕》［＃大見出し終わり］");
			
			//［＃注記付き］はあとで ｜ に変換される
			str = converter.replaceChukiSufTag(converter.convertGaijiChuki("［＃注記付き］○○［＃「××※［＃米印］」の注記付き終わり］", true, false));
			System.out.println(str);
			Assert.assertEquals(str, "｜○○《××※※》");
			str = converter.replaceChukiSufTag(converter.convertGaijiChuki("［＃注記付き］勝安房守［＃「本ト麟太郎※［＃コト、1-2-24］」の注記付き終わり］あああ［＃注記付き］○○［＃「××※［＃米印］」の注記付き終わり］", true, false));
			System.out.println(str);
			Assert.assertEquals(str, "｜勝安房守《本ト麟太郎ヿ》あああ｜○○《××※※》");
			
			str = converter.replaceChukiSufTag("青空文庫［＃「文庫」に「ぶんこ」のルビ］");
			System.out.println(str);
			Assert.assertEquals(str, "青空｜文庫《ぶんこ》");
			
			//左にルビ
			str = converter.replaceChukiSufTag("青空文庫《あおぞらぶんこ》［＃「青空文庫」の左に「aozora bunko」のルビ］");
			System.out.println(str);
			Assert.assertEquals(str, "青空文庫《あおぞらぶんこ》［＃「青空文庫」の左に「aozora bunko」のルビ］");
			
			//注記 小書きに変換
			converter.chukiRuby = false;
			converter.chukiKogaki = true;
			str = converter.replaceChukiSufTag("［＃５字下げ］第一回　入蔵決心の次第［＃「入蔵決心の次第」に「〔チベット入国の決意〕」の注記］［＃「第一回　入蔵決心の次第」は大見出し］");
			System.out.println(str);
			Assert.assertEquals(str, "［＃５字下げ］［＃大見出し］第一回　入蔵決心の次第［＃小書き］〔チベット入国の決意〕［＃小書き終わり］［＃大見出し終わり］");
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testCheckTcyPrev()
	{
		String prev, cur, next;
		prev = ""; cur = "10"; next = "";
		Assert.assertTrue(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
		prev = "<a href=\"aaa\">"; cur = "10"; next = "</a>";
		Assert.assertTrue(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
		prev = "<a href=\"aaa\"> "; cur = "10"; next = " </a>";
		Assert.assertTrue(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
		prev = "<b> <a href=\"aaa\"> "; cur = "10"; next = " </a></b>";
		Assert.assertTrue(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
		prev = " あ<b> <a href=\"aaa\"> "; cur = "10"; next = " </a></b>";
		Assert.assertTrue(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
		
		prev = "a"; cur = "10"; next = "";
		Assert.assertFalse(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
		prev = "a "; cur = "10"; next = "";
		Assert.assertFalse(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
		prev = "a<b><a href=\"aaa\">"; cur = "10"; next = "</a></b>";
		Assert.assertFalse(converter.checkTcyPrev((prev+cur+next).toCharArray(), prev.length()-1));
	}
	
	@Test
	public void testCheckTcyNext()
	{
		String prev, cur, next;
		prev = ""; cur = "10"; next = "";
		Assert.assertTrue(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		prev = "<a href=\"aaa\">"; cur = "10"; next = "</a>";
		Assert.assertTrue(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		prev = "<a href=\"aaa\"> "; cur = "10"; next = "</a>";
		Assert.assertTrue(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		prev = "<b><a href=\"aaa\">"; cur = "10"; next = "</a></b>";
		Assert.assertTrue(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		prev = "<b><a href=\"aaa\">"; cur = "10"; next = "</a></b>あ";
		Assert.assertTrue(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		
		prev = ""; cur = "10"; next = "a";
		Assert.assertFalse(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		prev = " "; cur = "10"; next = " a";
		Assert.assertFalse(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		prev = "a<b><a href=\"aaa\">"; cur = "10"; next = "</a></b>a";
		Assert.assertFalse(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
		prev = "<b><a href=\"aaa\">"; cur = "10"; next = "</a></b> a";
		Assert.assertFalse(converter.checkTcyNext((prev+cur+next).toCharArray(), prev.length()+cur.length()));
	}
}
