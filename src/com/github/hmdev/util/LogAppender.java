package com.github.hmdev.util;
import javax.swing.JTextArea;

/** ログ出力Wrapperクラス */
public class LogAppender
{
	static JTextArea jTextArea = null;
	
	static public void setTextArea(JTextArea _jTextArea)
	{
		jTextArea = _jTextArea;
	}
	
	static public void append(String log)
	{
		if (jTextArea != null) {
			jTextArea.append(log);
			jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
		}
		else System.out.print(log);
	}
	
}
