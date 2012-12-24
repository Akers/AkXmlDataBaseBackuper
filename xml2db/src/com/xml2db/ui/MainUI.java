package com.xml2db.ui;

import com.xml2db.core.util.LogPrinter;

public class MainUI {
	public static LogPrinter LOG_PRINTER = null;
	
	public static void main(String[] args){
		MainFrame MF = new MainFrame();
		LOG_PRINTER = new LogPrinter(MF);
		MF.setVisible(true);
//		new MainFrame().show();
	}
}
