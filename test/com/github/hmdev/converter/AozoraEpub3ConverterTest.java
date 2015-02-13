package com.github.hmdev.converter;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConvertTextLineToEpub3()
	{
		try {
			String str;
			str = converter.convertTitleLineToEpub3(converter.convertGaijiChuki("｜ルビ※［＃米印］《るび》※［＃米印］※［＃始め二重山括弧］※［＃終わり二重山括弧］", true, true));
			System.out.println(str);
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
			
			str = converter.convertGaijiChuki("※［＃U+845b］U+845b", true, true);
			System.out.println(str);
			str = converter.convertGaijiChuki("※［＃u+845b-e0100］u+845b-e0100", true, true);
			System.out.println(str);
			str = converter.convertGaijiChuki("※［＃U+845b-U+e0100］U+845b-U+ue0100", true, true);
			System.out.println(str);
			str = converter.convertGaijiChuki("※［＃「葛の異体字」、U+845b-e0100］「葛の異体字」、U+845b-e0100", true, true);
			System.out.println(str);
			str = converter.convertGaijiChuki("※［＃「葛の異体字」、u+845b-u+e0100］「葛の異体字」、u+845b-u+e0100", true, true);
			System.out.println(str);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReplaceChukiSufTag()
	{
		try {
			String str;
			
			str = converter.replaceChukiSufTag("星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32図」を指す。］の中に示してある。");
			System.out.println(str);
			Assert.assertEquals(str, "星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32図」を指す。］の中に示してある。");
			str = converter.replaceChukiSufTag("星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32［＃「32」は縦中横］図」を指す。］の中に示してある。");
			System.out.println(str);
			Assert.assertEquals(str, "星状、扇形などの標本図は第一一〇及び一一一頁の一般分類の図［＃「第一一〇及び一一一頁の一般分類の図」は「第32図」を指す。］の中に示してある。");
			
			str = converter.replaceChukiSufTag("第32［＃「32」は縦中横］図［＃「第32［＃「32」は縦中横］図」は太字］");
			System.out.println(str);
			Assert.assertEquals(str, "［＃太字］第［＃縦中横］32［＃縦中横終わり］図［＃太字終わり］");
			
			str = converter.replaceChukiSufTag(converter.convertGaijiChuki("勝安房守［＃「勝安房守」に「本ト麟太郎※［＃コト、1-2-24］」の注記", true, false));
			System.out.println(str);
			Assert.assertEquals(str, "勝安房守［＃「勝安房守」に「本ト麟太郎ヿ」の注記");
			
			converter.chukiRuby = true;
			str = converter.replaceChukiSufTag("［＃５字下げ］第一回　入蔵決心の次第［＃「入蔵決心の次第」に「〔チベット入国の決意〕」の注記］［＃「第一回　入蔵決心の次第」は大見出し］");
			System.out.println(str);
			Assert.assertEquals(str, "［＃５字下げ］［＃大見出し］第一回　｜入蔵決心の次第《〔チベット入国の決意〕》［＃大見出し終わり］");
			converter.chukiRuby = false;
			converter.chukiKogaki = true;
			str = converter.replaceChukiSufTag("［＃５字下げ］第一回　入蔵決心の次第［＃「入蔵決心の次第」に「〔チベット入国の決意〕」の注記］［＃「第一回　入蔵決心の次第」は大見出し］");
			System.out.println(str);
			Assert.assertEquals(str, "［＃５字下げ］［＃大見出し］第一回　入蔵決心の次第［＃小書き］〔チベット入国の決意〕［＃小書き終わり］［＃大見出し終わり］");
			
			str = converter.replaceChukiSufTag("人間は考える｜蘆［＃「人間は考える｜蘆」は太字］《あし》");
			System.out.println(str);
			Assert.assertEquals(str, "［＃太字］人間は考える｜蘆《あし》［＃太字終わり］");
			
			str = converter.replaceChukiSufTag("　　　あ１［＃「あ１」は中見出し］");
			System.out.println(str);
			Assert.assertEquals(str,  "　　　［＃中見出し］あ１［＃中見出し終わり］");
			
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
