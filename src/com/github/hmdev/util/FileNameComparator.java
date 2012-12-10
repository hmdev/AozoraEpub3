package com.github.hmdev.util;
 
import java.util.Comparator;

/** ファイル名並び替え用 */
public class FileNameComparator implements Comparator<String>
{
	@Override
	public int compare(String o1, String o2)
	{
		char[] c1 = o1.toLowerCase().toCharArray();
		char[] c2 = o2.toLowerCase().toCharArray();
		int len = Math.min(c1.length, c2.length);
		int diff;
		for (int i=0; i<len; i++) {
			diff = replace(c1[i])-replace(c2[i]);
			if (diff != 0) return diff;
		}
		return c1.length-c2.length;
	}
	
	char replace(char c)
	{
		switch (c) {
		case '_': return '/';
		case '一': return '一';
		case '二': return '一'+1;
		case '三': return '一'+2;
		case '四': return '一'+3;
		case '五': return '一'+4;
		case '六': return '一'+5;
		case '七': return '一'+6;
		case '八': return '一'+7;
		case '九': return '一'+8;
		case '十': return '一'+9;
		case '上': return '上';
		case '前': return '上'+1;
		case '中': return '上'+2;
		case '下': return '上'+3;
		case '後': return '上'+4;
		default: return c;
		}
	}
}
