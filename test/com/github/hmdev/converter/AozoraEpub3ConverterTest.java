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
	public void testReplaceChukiSufTag()
	{
		String ret = converter.replaceChukiSufTag("　　　あ１［＃「あ１」は中見出し］");
		Assert.assertEquals(ret, "　　　［＃中見出し］あ１［＃中見出し終わり］");
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
