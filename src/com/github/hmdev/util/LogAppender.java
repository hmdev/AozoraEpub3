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
	
	static public void println(String log)
	{
		LogAppender.append(log);
		LogAppender.append("\n");
	}
	static public void println()
	{
		LogAppender.append("\n");
	}
	static public void append(String log)
	{
		if (jTextArea != null) {
			jTextArea.append(log);
			jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
		}
		else System.out.print(log);
	}
	
	static public void printStaclTrace(Exception e)
	{
		for (StackTraceElement ste : e.getStackTrace()) {
			LogAppender.append(ste.toString());
			LogAppender.append("\n");
		}
	}
	
	static public void msg(int lineNum, String msg, String desc)
	{
		LogAppender.append(msg);
		LogAppender.append(" ("+(lineNum+1)+")");
		if (desc != null) {
			LogAppender.append(" : ");
			LogAppender.append(desc);
		}
		LogAppender.append("\n");
	}
	
	static public void error(String msg)
	{
		LogAppender.append("[ERROR] ");
		LogAppender.append(msg);
		LogAppender.append("\n");
	}
	static public void error(int lineNum, String msg, String desc)
	{
		LogAppender.append("[ERROR] ");
		LogAppender.msg(lineNum, msg, desc);
	}
	static public void error(int lineNum, String msg)
	{
		LogAppender.append("[ERROR] ");
		LogAppender.msg(lineNum, msg, null);
	}
	
	static public void warn(int lineNum, String msg, String desc)
	{
		LogAppender.append("[WARN] ");
		LogAppender.msg(lineNum, msg, desc);
	}
	static public void warn(int lineNum, String msg)
	{
		LogAppender.append("[WARN] ");
		LogAppender.msg(lineNum, msg, null);
	}
	
	static public void info(int lineNum, String msg, String desc)
	{
		LogAppender.append("[INFO] ");
		LogAppender.msg(lineNum, msg, desc);
	}
	static public void info(int lineNum, String msg)
	{
		LogAppender.append("[INFO] ");
		LogAppender.msg(lineNum, msg, null);
	}
}
