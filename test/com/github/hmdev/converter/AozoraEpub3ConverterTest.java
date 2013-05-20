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
		Assert.assertTrue(converter.checkTcyPrev("10".toCharArray(), -1));
		Assert.assertTrue(converter.checkTcyPrev("<a href=\"aaa\">10</a>".toCharArray(), 13));
		Assert.assertTrue(converter.checkTcyPrev("<a href=\"aaa\"> 10</a>".toCharArray(), 14));
		Assert.assertTrue(converter.checkTcyPrev("<b><a href=\"aaa\">10</a></b>".toCharArray(), 16));
		Assert.assertTrue(converter.checkTcyPrev("あ<b><a href=\"aaa\">10</a></b>".toCharArray(), 17));
		
		Assert.assertFalse(converter.checkTcyPrev("a10".toCharArray(), 0));
		Assert.assertFalse(converter.checkTcyPrev("a 10".toCharArray(), 1));
		Assert.assertFalse(converter.checkTcyPrev("a<b><a href=\"aaa\">10</a></b>".toCharArray(), 17));
	}
	
	@Test
	public void testCheckTcyNext()
	{
		Assert.assertTrue(converter.checkTcyNext("10".toCharArray(), 2));
		Assert.assertTrue(converter.checkTcyNext("<a href=\"aaa\">10</a>".toCharArray(), 16));
		Assert.assertTrue(converter.checkTcyNext("<a href=\"aaa\"> 10 </a>".toCharArray(), 17));
		Assert.assertTrue(converter.checkTcyNext("<b><a href=\"aaa\">10</a></b>".toCharArray(), 19));
		Assert.assertTrue(converter.checkTcyNext("<b><a href=\"aaa\">10</a></b>あ".toCharArray(), 19));
		
		Assert.assertFalse(converter.checkTcyNext("a10a".toCharArray(), 3));
		Assert.assertFalse(converter.checkTcyNext("a 10a".toCharArray(), 4));
		Assert.assertFalse(converter.checkTcyNext("a<b><a href=\"aaa\">10</a></b>a".toCharArray(), 20));
		Assert.assertFalse(converter.checkTcyNext("a<b><a href=\"aaa\">10</a></b> a".toCharArray(), 20));
	}
}
