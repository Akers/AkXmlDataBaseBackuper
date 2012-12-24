package com.xml2db.core.database.connector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.xml2db.core.database.connector.po.DataTable;
import com.xml2db.core.database.connector.po.TableDataRow;
import com.xml2db.core.database.connector.po.TableFieldDetail;
import com.xml2db.core.database.connector.po.TableIndexer;
import com.xml2db.ui.MainUI;


public abstract class DBConnector{
	
	//get safe database connection
	protected Connection getConnection(){
		
		try{
			if(this.conn == null || this.conn.isClosed()){
				Class.forName(driverName);
				this.conn = DriverManager.getConnection(this.dbConnStr, this.dbUsr, this.dbPWD);
				this.status = this.CONNECTOR_NO_ERROR;
			}
		}
		catch (CommunicationsException ec){
			this.status = CONNECTOR_FAILD_TO_COMMUNICAT_WITH_SEVER;
		}
		catch (SQLException e){
            this.setStatus(this.CONNECTOR_FAILD_TO_CREATE_CONNECTION);
            MainUI.LOG_PRINTER.appendError("数据库链接出错："+e.getMessage());
		}
		catch (ClassNotFoundException e){
			this.setStatus(this.CONNECTOR_FAILD_TO_LOAD_DRIVER);
            MainUI.LOG_PRINTER.appendError("数据库驱动加载出错：" + e.getMessage());
		}
		
		return this.conn;
			
	}
	
	//get safe PreparedStatement
	protected PreparedStatement getPreparedStatement(String sql){
		try{
			if(this.preStmt == null || this.preStmt.isClosed()){
				this.preStmt = this.getConnection().prepareStatement(sql);
			}
		}
		catch (SQLException e){
			this.setStatus(this.CONNECTOR_FAILD_TO_CREATE_STATEMENT);
            MainUI.LOG_PRINTER.appendError("创建PreparedStatement失败！！");
		}
		return this.preStmt;
	}
	protected PreparedStatement getPreparedStatement(){
		return this.getPreparedStatement("");
	}
	
	//get safe database statement
	protected Statement getStatement(){
		try{
			this.conn = this.getConnection();
			if(this.stmt == null || this.stmt.isClosed()){
				this.stmt = this.conn.createStatement();
			}
		}
		catch (Exception e){
			this.setStatus(this.CONNECTOR_FAILD_TO_CREATE_STATEMENT);
		}

		return this.stmt;
	}
	
	//get datatable modle of table named tbName
	public DataTable getTableDetail(String tbName){
		DataTable td = new DataTable();
        if(this.status != this.CONNECTOR_NO_ERROR){
            this.printError();
        }
        else{
            String queryStr = "SELECT * FROM "+tbName+";";
            ResultSet rs = null;
            try {
                rs = this.getStatement().executeQuery(queryStr);
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();

                //get table name
                td.setName(tbName);

                //get field detail
                DatabaseMetaData dbMeta = this.getConnection().getMetaData();

                for(int i=1; i<=colCount; i++){
                    td.addField(new TableFieldDetail(rsmd.getColumnName(i), rsmd.getColumnTypeName(i), rsmd.getColumnDisplaySize(i), rsmd.isNullable(i)));
                }

                ResultSet pkResult = dbMeta.getPrimaryKeys(this.getConnection().getCatalog(), null, tbName);
                ResultSet fkResult = dbMeta.getImportedKeys(this.getConnection().getCatalog(), null, tbName);



                for(TableFieldDetail feild : td.getFieldDetails()){
                    //add field primary key info
                    while (pkResult.next()){
                        if(pkResult.getString("COLUMN_NAME").trim().equalsIgnoreCase(feild.getName())){
                            feild.setPk(true);
                            feild.setPkSEQ(Short.parseShort(pkResult.getString("KEY_SEQ")));
                        }
                    }
                    pkResult.beforeFirst();

                    //add field forgin key info
                    while(fkResult.next()){
                        if (fkResult.getString("FKCOLUMN_NAME").trim().equalsIgnoreCase(feild.getName())) {
                            feild.setFk(true);
                            feild.setFkTb(fkResult.getString("PKTABLE_NAME"));
                            feild.setFkColName(fkResult.getString("PKCOLUMN_NAME"));
                            feild.setFkUpdateRule(Short.parseShort(fkResult.getString("UPDATE_RULE")));
                            feild.setFkDeleteRule(Short.parseShort(fkResult.getString("DELETE_RULE")));
                        }
                    }
                    fkResult.beforeFirst();
                }

                ResultSet indexResult = dbMeta.getIndexInfo(this.getConnection().getCatalog(), null, tbName, false, true);
                while (indexResult.next()){
                    if(!indexResult.getString("INDEX_NAME").equalsIgnoreCase("PRIMARY"))
                        td.addIndexer(new TableIndexer(indexResult.getString("INDEX_NAME"), indexResult.getString("COLUMN_NAME"), indexResult.getBoolean("NON_UNIQUE")));
                }
//                //add field primary key info
//                ResultSet pkResult = dbMeta.getPrimaryKeys(this.getConnection().getCatalog(), null, tbName);
//                while (pkResult.next()){
//                    for(TableFieldDetail feild : td.getFieldDetails()){
//                        if(pkResult.getString("PK_NAME").trim().equalsIgnoreCase(feild.getName())){
//                            feild.setPk(true);
//                            feild.setPkSEQ(Short.parseShort(pkResult.getString("KEY_SEQ")));
//                        }
//                    }
//                }
//                pkResult.close();
//
//                //add field forgin key info
//                ResultSet fkResult = dbMeta.getImportedKeys(this.getConnection().getCatalog(), null, tbName);
//                while(fkResult.next()){
//                    for(TableFieldDetail feild : td.getFieldDetails()){
//                        if (pkResult.getString("FKCOLUMN_NAME").trim().equalsIgnoreCase(feild.getName())) {
//                            feild.setFk(true);
//                            feild.setFkTb(pkResult.getString("PKTABLE_NAME"));
//                        }
//                    }
//                }

                //get data rows
                TableDataRow row = null;
                while(rs.next()){
                    row = new TableDataRow();
                    for(int i=1; i<= colCount; i++){
                        //get Column Value
                        row.add(rs.getString(i));
                    }
                    //add row to td.dataRows
                    td.addRow(row);
                }

                rs.close();
                this.getStatement().close();
                this.getConnection().close();
            }
            catch (NullPointerException npe){
                printError();
            }
            catch (SQLException e) {
                MainUI.LOG_PRINTER.appendError("SQL查询错误！"+e.getMessage());
            }
        }

		return td;
	}
	
	//update a table use DataTable modle
	public boolean updateTable(DataTable table){
		boolean rs = false;
        if(this.status != this.CONNECTOR_NO_ERROR){
            this.printError();
        }
        else{
            StringBuffer queryStr = new StringBuffer();
            queryStr.append("INSERT INTO ")
                    .append(table.getName())
                    .append(" VALUES(");
            List<TableFieldDetail> tableFields = table.getFieldDetails();
            int fieldsCount = tableFields.size();
            for(int i=0; i<fieldsCount; i++){
                queryStr.append("?");
                if(i < fieldsCount - 1){
                    queryStr.append(",");
                }
            }
            queryStr.append(")");
            try {
//			this.getStatement().setString(1, table.getName());
                //if table is not exist crate it:
//            ResultSet rsTmp = this.getStatement().executeQuery("SELECT * FROM "+table.getName());
                String createTbSql = "CREATE TABLE "+table.getName()+"($fields)";
                StringBuffer fieldsSql = new StringBuffer();
                //ToDo: 或许应该找个温柔点的方法来处理这个....
                this.getStatement().executeUpdate("DROP TABLE IF EXISTS " + table.getName());
                List<TableFieldDetail> fieldList = table.getFieldDetails();
                //读取字段信息列表并创建字段信息
                TableFieldDetail field = null;
                boolean ignoreLength = false;
                for(int i=0;i<fieldList.size();i++){
                    ignoreLength = false;
                    field = fieldList.get(i);
                    if(field.getType().equalsIgnoreCase("DOUBLE"))
                        field.setType("FLOAT");
                    if(field.getType().equalsIgnoreCase("DATE")){
                        field.setType("DATETIME");
                    }
                    if(field.getType().equalsIgnoreCase("DATETIME")){
                        ignoreLength = true;
                    }
                    if(field.getType().equalsIgnoreCase("VARCHAR") && field.getColLength() >= 21845){
                        field.setType("TEXT");
                        ignoreLength = true;
                    }
                    fieldsSql.append(field.getName()).append(" ").append(field.getType());
                    if(!ignoreLength && field.getColLength() > 0)
                        fieldsSql.append("(").append(field.getColLength()).append(")");

                    if(field.isPk())
                        fieldsSql.append(" ").append("PRIMARY KEY");
                    switch (field.isNullAble()){
                        case ResultSetMetaData.columnNullable:
                        case ResultSetMetaData.columnNullableUnknown:break;
                        case ResultSetMetaData.columnNoNulls:
                            fieldsSql.append(" NOT NULL");
                    }
                    if(i >= 0 && i < fieldList.size() - 1)
                        fieldsSql.append(",");
                }
                createTbSql = createTbSql.replace("$fields", fieldsSql.toString());
//                System.out.println(createTbSql);
                this.getStatement().execute(createTbSql);

                this.getStatement().close();



                //读取并写入表数据
                this.preStmt = this.getConnection().prepareStatement(queryStr.toString());
                for(TableDataRow row : table.getRows()){
                    for(int i=0; i<row.getAll().size(); i++){
                        this.preStmt.setString(i+1, row.get(i));
                    }

                    this.preStmt.executeUpdate();
                }
                this.preStmt.close();
                this.getConnection().close();
                rs = true;
            }
            catch (NullPointerException npe){
                this.printError();
            }
            catch (SQLException e) {
                rs = false;
                System.out.println("ErrorCode:"+e.getErrorCode()+";msg:"+e.getMessage());
                e.printStackTrace();
            }
        }

		return rs;
	}

    //create foreign key
    public boolean createForeginKey(DataTable table){
        boolean rs = false;
        //add foreign keys
        MainUI.LOG_PRINTER.appendMsg("开始创建表<"+table.getName()+">的外键");
        try{
            for(TableFieldDetail f : table.getFieldDetails()){
                if(f.isFk()){
                    String str = "ALTER TABLE " + table.getName() + " ADD INDEX (" + f.getName()+")";
    //                        System.out.println(str);
                    this.getStatement().executeUpdate(str);
                    String fkSql = "ALTER TABLE " + table.getName() + " ADD FOREIGN KEY "
                            + "("+f.getName()+") REFERENCES " + f.getFkTb() + "("+f.getFkColName()+")"
                            + " ON DELETE " + TableFieldDetail.parseImportedKeyRule(f.getFkDeleteRule())
                            + " ON UPDATE " + TableFieldDetail.parseImportedKeyRule(f.getFkUpdateRule());
    //                        System.out.println(fkSql);
//                    System.out.println("sql:" + fkSql);
                    this.getStatement().executeUpdate(fkSql);
                }
            }
            rs = true;
        } catch (SQLException e) {
            this.printError();
            MainUI.LOG_PRINTER.appendMsg("表<"+table.getName()+">创建外键时出错："+e.getMessage());
            rs = false;
        }

        return rs;
    }

    public boolean createIndexers(DataTable table){
        boolean rs = false;
        MainUI.LOG_PRINTER.appendMsg("开始创建表"+table.getName()+"的索引");
        try{
            for (TableIndexer indexer : table.getIndexers()){
                StringBuilder sqlStr = new StringBuilder("ALTER TABLE ");
                sqlStr.append(table.getName()).append(" ADD ");
                if(!indexer.isNonUniQue()){
                    sqlStr.append("UNIQUE");
                }
                sqlStr.append(" INDEX ").append(indexer.getIndexName())
                        .append("(").append(indexer.getColName()).append(")");

                System.out.println(sqlStr.toString());
                this.getStatement().executeUpdate(sqlStr.toString());
            }
            rs = true;
        }
        catch (SQLException e){
            this.printError();
            MainUI.LOG_PRINTER.appendMsg("表<"+table.getName()+">创建索引时出错："+e.getMessage());
            rs = false;
        }
        return rs;
    }

	//get table name list
	public List<String> getTableList() {
        ArrayList<String> rst = new ArrayList<String>();
        try{
            if(this.status == this.CONNECTOR_NO_ERROR){
                Connection conn = this.getConnection();
                if(this.status == CONNECTOR_NO_ERROR){
                    java.sql.DatabaseMetaData dbmd = conn.getMetaData();
                    ResultSet rs = dbmd.getTables("", null, null, null);
                    while(rs.next()){
                        rst.add(rs.getString("TABLE_NAME"));
                    }
                    rs.close();
                }
                conn.close();
            }
            else
                this.printError();
        } catch (SQLException e) {
            MainUI.LOG_PRINTER.appendError("SQL对象关闭出错！");
        }
        return rst;
	}

    protected boolean hasTable(String _tbName){
        boolean result = false;
        try{
            DatabaseMetaData rsmd = this.getConnection().getMetaData();
            ResultSet rs = rsmd.getTables(null, null, _tbName, null);
            if(rs.next()){
                result = true;
            }
            rs.close();

        }
        catch (NullPointerException npe){
            this.printError();
        }
        catch (SQLException e){
            MainUI.LOG_PRINTER.appendError("SQL错误："+e.getMessage());
        }
        return result;
    }

    public void printError(){
        switch (this.status){
            case  CONNECTOR_FAILD_TO_LOAD_DRIVER :
                MainUI.LOG_PRINTER.appendError("加载数据库驱动出错");
            case CONNECTOR_FAILD_TO_COMMUNICAT_WITH_SEVER :
                MainUI.LOG_PRINTER.appendError("无法与数据库服务器建立链接，可能是服务器未启动");
            case CONNECTOR_FAILD_TO_CREATE_CONNECTION :
                MainUI.LOG_PRINTER.appendError("建立数据库链接失败");
            case  CONNECTOR_FAILD_TO_CREATE_STATEMENT :
                MainUI.LOG_PRINTER.appendError("无法创建Statement，可能是数据库链接失败");
            default:
                MainUI.LOG_PRINTER.appendError("未知错误");
        }
    }

	public abstract String getDbName();
	public abstract void setDbName(String dbName);
	public abstract String getDbUrl();
	public abstract void setDbUrl(String dbName);
	
	protected void setDbConnStr(String connStr){ this.dbConnStr = connStr; }
	protected String getDbConnStr(){ return this.dbConnStr; }
	protected void setDriverName(String _driveName) { this.driverName = _driveName; }
	protected String getDriverName() { return this.driverName; }
	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
	public String getDbPWD() { return dbPWD; }
	public void setDbPWD(String dbPWD) { this.dbPWD = dbPWD; }
	public String getDbUsr() { return dbUsr; }
	public void setDbUsr(String dbUsr) { this.dbUsr = dbUsr; }
	
	
	private String driverName = "";
	private String dbConnStr = "";
	private Connection conn = null;
	private Statement stmt = null;
	private PreparedStatement preStmt = null;
	private String dbPWD = "0000";
	private String dbUsr = "root";
	private int status = this.CONNECTOR_NO_ERROR;
	public final int CONNECTOR_NO_ERROR = 0;
	public final int CONNECTOR_FAILD_TO_LOAD_DRIVER = -1;
    public final int CONNECTOR_FAILD_TO_CREATE_CONNECTION = -2;
	public final int CONNECTOR_FAILD_TO_CREATE_STATEMENT = -3;
	public final int CONNECTOR_FAILD_TO_COMMUNICAT_WITH_SEVER = -4;
}
