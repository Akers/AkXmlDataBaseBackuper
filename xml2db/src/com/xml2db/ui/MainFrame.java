package com.xml2db.ui;

import com.xml2db.core.database.connector.po.DataTable;
import com.xml2db.core.database.convertor.DataBaseConvertor;
import com.xml2db.core.xmlfile.convertor.XMLConvertor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainFrame extends JFrame {
	public MainFrame(){
		super("XML数据库备份工具");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//窗口大小
		this.setSize(300, 350);
		this.setResizable(false);
		
		//布局管理器
		this.pMain = new JPanel();
		this.pMain.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		//获取屏幕宽度和高度
		double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	    double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    //设置主窗口屏幕居中 
		this.setLocation((int) (width - this.getWidth()) / 2, (int) (height - this.getHeight()) / 2);
		
		//Add Components
		this.pMain.add(new JLabel("数据库Url："));
		this.txtDBUrl = new JTextField("localhost:3306");
		this.txtDBUrl.setPreferredSize(new Dimension(200, 20));
		this.pMain.add(this.txtDBUrl);
		
		this.pMain.add(new JLabel("数据库用户名："));
		this.txtDBUsr = new JTextField("root");
		this.txtDBUsr.setPreferredSize(new Dimension(150, 20));
		this.pMain.add(this.txtDBUsr);
		
		this.pMain.add(new JLabel("数据库密码："));
		this.txtDBPwd = new JTextField("0000");
		this.txtDBPwd.setPreferredSize(new Dimension(180, 20));
		this.pMain.add(this.txtDBPwd);
		
		this.pMain.add(new JLabel("数据库名："));
		this.txtDBName = new JTextField();
		this.txtDBName.setPreferredSize(new Dimension(200, 20));
		this.pMain.add(this.txtDBName);
		
		this.pMain.add(new JLabel("Xml文件路径："));
		this.txtXmlPath = new JTextField("");
		this.txtXmlPath.setPreferredSize(new Dimension(210, 25));
		this.pMain.add(this.txtXmlPath);
		this.btnBrowser = new JButton("浏览...");
		this.setPreferredSize(new Dimension(50, 25));
		this.pMain.add(this.btnBrowser);
		
		this.btnImport = new JButton("XML导入到数据库");
		this.btnExport = new JButton("数据库导出到XML");
		this.pMain.add(this.btnExport);
		this.pMain.add(this.btnImport);
		this.btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnImportOnClick();
			}
		});
		this.btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnExportOnClick();
			}
		});
		
		this.txtaLogMsg = new JTextArea();
		this.txtaLogMsg.setLineWrap(true);
		this.txtaLogMsg.setEditable(false);
//		this.txtaLogMsg.setPreferredSize(new Dimension(285, 130));
        JScrollPane jsp = new JScrollPane(this.txtaLogMsg);
        jsp.setPreferredSize(new Dimension(285, 130));
		this.pMain.add(jsp);
		this.btnBrowser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnBrowserOnClick();
			}
		});
		this.getContentPane().add(pMain);
	}
	
//导入数据按钮单击
private void btnImportOnClick(){
	String url = this.txtDBUrl.getText();
	String usr = this.txtDBUsr.getText();
	String pwd = this.txtDBPwd.getText();
	String dbName = this.txtDBName.getText();
	String xmlPath = this.txtXmlPath.getText();
	if(url == null || url.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入数据库url");
	else if(usr == null || usr.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入用户名");
	else if(pwd == null || pwd.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入用户密码");
	else if(dbName == null || dbName.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入数据库名");
	else if(xmlPath == null || xmlPath.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请选择需要导入的xml文件/或文件夹");
//	else if(!xmlPath.endsWith(".xml"))
//		JOptionPane.showMessageDialog(this,"请选择需要导入的xml文件，请勿选择其它类型文件和目录");
	else{
		DataBaseConvertor dbConver = new DataBaseConvertor(url, usr, pwd, dbName);
		XMLConvertor xmlConvert = new XMLConvertor();
        File xmlFile = new File(xmlPath);

        if(xmlFile.exists()){
            if(xmlFile.isDirectory()){
                dbConver.saveToDb(xmlFile.listFiles());
            }
            else{
                dbConver.saveToDb(new File[]{xmlFile});
            }
        }
        else{
            MainUI.LOG_PRINTER.appendError("选取的路径不存在！！！");
        }

	}
}

//导出数据按钮单击
private void btnExportOnClick(){
	String url = this.txtDBUrl.getText();
	String usr = this.txtDBUsr.getText();
	String pwd = this.txtDBPwd.getText();
	String dbName = this.txtDBName.getText();
	String xmlPath = this.txtXmlPath.getText();
	if(url == null || url.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入数据库url");
	else if(usr == null || usr.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入用户名");
	else if(pwd == null || pwd.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入用户密码");
	else if(dbName == null || dbName.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请输入数据库名");
	else if(xmlPath == null || xmlPath.trim().equals(""))
		JOptionPane.showMessageDialog(this,"请选择需要导出的路径");
	else if(xmlPath.endsWith(".xml") || xmlPath.substring(xmlPath.lastIndexOf("\\"), xmlPath.length()).contains("."))
		JOptionPane.showMessageDialog(this,"请选择需要XML导出的目录");
	else{
		DataBaseConvertor dbConver = new DataBaseConvertor(url, usr, pwd, dbName);
		XMLConvertor xmlConvert = new XMLConvertor();
		MainUI.LOG_PRINTER.appendMsg("开始导出数据库'"+dbName+"'内的数据");
		for(String tbName : dbConver.getTableNameList()){
			if(xmlConvert.tryConvertToXml(xmlPath, dbConver.getDataBaseName(), dbConver.getDataTable(tbName), true))
				MainUI.LOG_PRINTER.appendMsg("数据表<"+tbName+">数据导出成功！！");
			else
				MainUI.LOG_PRINTER.appendError("数据表<"+tbName+">数据导出失败！！");
		}
		MainUI.LOG_PRINTER.appendMsg("数据库<"+dbName+">内的数据导出完毕");
	}
}
	
	private void btnBrowserOnClick(){
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("选择需要导入/导出的XML路径");
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			this.txtXmlPath.setText(jfc.getSelectedFile().getAbsolutePath());
		}
	}
	
	synchronized public void appendMsg(String msg){
		this.txtaLogMsg.append(msg+"\n");
        this.txtaLogMsg.setCaretPosition(this.txtaLogMsg.getText().length());
	}
	
	synchronized public void clsMsg(){
		this.txtaLogMsg.setText("");
        this.txtaLogMsg.setCaretPosition(this.txtaLogMsg.getText().length());
	}
	
	private JPanel pMain = null;
	private JTextField txtDBPwd = null;
	private JTextField txtDBUsr = null;
	private JTextField txtDBUrl = null;
	private JTextField txtDBName = null;
	private JTextField txtXmlPath = null;
	private JTextArea txtaLogMsg = null;
	private JButton btnImport = null;
	private JButton btnExport = null;
	private JButton btnBrowser = null;
	private static final long serialVersionUID = 1L;
}
