package com.xml2db.core.database.convertor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.xml2db.core.database.connector.DBConnector;
import com.xml2db.core.database.connector.DBConnectorFactory;
import com.xml2db.core.database.connector.MySqlDBConnectorFactory;
import com.xml2db.core.database.connector.po.DataTable;
import com.xml2db.core.xmlfile.convertor.XMLConvertor;
import com.xml2db.ui.MainUI;

public class DataBaseConvertor {
	public DataBaseConvertor(String url, String usr, String pwd, String dbName){
		this.dbcFactory = new MySqlDBConnectorFactory();
		this.connector = dbcFactory.getDBConnector();
		this.connector.setDbUrl(url);
		this.connector.setDbUsr(usr);
		this.connector.setDbPWD(pwd);
		this.connector.setDbName(dbName);
	}
	
	public List<String> getTableNameList(){
		return this.connector.getTableList();
	}
	
	public DataTable getDataTable(String tbName){
		return this.connector.getTableDetail(tbName);
	}
	
	public boolean saveToDB(DataTable table){
		
		return this.connector.updateTable(table);
	}

    public boolean saveToDb(File[] files){
        MainUI.LOG_PRINTER.appendMsg("开始导入["+files.length+"]个数据文件");
        boolean result = false;
        XMLConvertor xmlConvert = new XMLConvertor();
        int sucCount = 0;
        DataTable table = null;
        List<DataTable> tbList = new ArrayList<DataTable>();
        for(File xmlFile : files){
            table = xmlConvert.parse(xmlFile.getAbsolutePath());
            tbList.add(table);
            if(this.connector.updateTable(table)) {
                MainUI.LOG_PRINTER.appendMsg("数据表<"+table.getName()+">数据导入成功！！");
                sucCount ++;
            }
            else
                MainUI.LOG_PRINTER.appendError("数据表<"+table.getName()+">数据导入失败！！");
        }

        //创建外键和索引：
        for (DataTable tb : tbList){
            //创建外键
            if(this.connector.createForeginKey(tb)){
                MainUI.LOG_PRINTER.appendMsg("表<"+tb.getName()+">创建外键成功");
            }
            else
                MainUI.LOG_PRINTER.appendMsg("表<"+tb.getName()+">创建外键失败");

            //创建索引
            if(this.connector.createIndexers(tb)){
                MainUI.LOG_PRINTER.appendMsg("表<"+tb.getName()+">创建索引成功");
            }
            else
                MainUI.LOG_PRINTER.appendMsg("表<"+tb.getName()+">创建索引失败");

        }
        MainUI.LOG_PRINTER.appendMsg("数据文件导入完成（成功"+sucCount+"个失败"+(files.length-sucCount)+"个）");

        //创建索引：

        return result;
    }

	public String getDataBaseName(){
		return this.connector.getDbName();
	}
	
	private DBConnectorFactory dbcFactory = null;
	private DBConnector connector = null;
}
