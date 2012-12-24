package com.xml2db.core.util;

import com.xml2db.ui.MainFrame;

public class LogPrinter {
	public LogPrinter(MainFrame mf){
		this.mf = mf;
	}
	
	public void appendMsg(String msg){
		mf.appendMsg("Message:"+msg);
	}
	
	public void appendError(String msg){
		mf.appendMsg("Error:"+msg);
	}
	
	private MainFrame mf = null;
}
