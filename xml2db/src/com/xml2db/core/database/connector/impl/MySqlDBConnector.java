package com.xml2db.core.database.connector.impl;

import com.xml2db.core.database.connector.DBConnector;

public class MySqlDBConnector extends DBConnector{
	public MySqlDBConnector(){
		this.resetDbConnStr(this.dbUrl, this.dbName, this.dbUsr, this.dbPWD);
		super.setDriverName("com.mysql.jdbc.Driver");
	}

	private void resetDbConnStr(String dbUrl, String dbName, String dbUsr, String dbPWD){
		super.setDbConnStr(String.format(
				"jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect = true", 
				this.dbUrl, 
				this.dbName
			)
		);
		super.setDbUsr(dbUsr);
		super.setDbPWD(dbPWD);
	}
	
	public synchronized String getDbUrl() { return dbUrl; }
	public synchronized void setDbUrl(String dbUrl) { this.dbUrl = dbUrl;this.resetDbConnStr(this.dbUrl, this.dbName, this.dbUsr, this.dbPWD);}
	public synchronized String getDbName() { return dbName; }
	public synchronized void setDbName(String dbName) { this.dbName = dbName;this.resetDbConnStr(this.dbUrl, this.dbName, this.dbUsr, this.dbPWD);}
	public synchronized String getDbPWD() { return dbPWD; }
	public synchronized void setDbPWD(String dbPWD) { this.dbPWD = dbPWD;this.resetDbConnStr(this.dbUrl, this.dbName, this.dbUsr, this.dbPWD);}
	public synchronized String getDbUsr() { return dbUsr; }
	public synchronized void setDbUsr(String dbUsr) { this.dbUsr = dbUsr;this.resetDbConnStr(this.dbUrl, this.dbName, this.dbUsr, this.dbPWD);}

	private String dbUrl = "localhost:3306";
	private String dbName = "db_ilike";
	private String dbPWD = "0000";
	private String dbUsr = "root";
//	private String driverName = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
}
