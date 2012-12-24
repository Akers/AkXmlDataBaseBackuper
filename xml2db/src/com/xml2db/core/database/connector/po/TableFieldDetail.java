package com.xml2db.core.database.connector.po;

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;

public class TableFieldDetail {
	public TableFieldDetail(){
		this(false, false, "", "", "Object", 0, ResultSetMetaData.columnNullable);
	}
	public TableFieldDetail(String name, String type, int colLength, int nullAble){
        this(false, false, "", name, type, colLength, nullAble);
	}

    public TableFieldDetail(boolean pk, boolean fk, String fkTb, String name, String type, int colLength, int nullAble) {
        isPk = pk;
        isFk = fk;
        this.fkTb = fkTb;
        this.name = name;
        this.type = type;
        this.colLength = colLength;
        this.nullAble = nullAble;
    }

    public synchronized String getName() { return name; }
	public synchronized void setName(String name) { this.name = name; }
	public synchronized String getType() { return type; }
	public synchronized void setType(String type) { this.type = type; }
    public int getColLength() { return colLength; }
    public void setColLength(int colLength) { this.colLength = colLength; }
    public boolean isPk() { return isPk; }
    public void setPk(boolean pk) { isPk = pk; }
    public boolean isFk() { return isFk; }
    public void setFk(boolean fk) { isFk = fk; }
    public String getFkTb() { return fkTb; }
    public void setFkTb(String fkTb) { this.fkTb = fkTb; }
    public short getPkSEQ() { return pkSEQ; }
    public void setPkSEQ(short pkSEQ) { this.pkSEQ = pkSEQ; }
    public int isNullAble() { return nullAble; }
    public void setNullAble(int nullAble) { this.nullAble = nullAble; }
    public short getFkUpdateRule() { return fkUpdateRule; }
    public void setFkUpdateRule(short fkOnUpdateRule) { this.fkUpdateRule = fkOnUpdateRule; }
    public short getFkDeleteRule() { return fkDeleteRule; }
    public void setFkDeleteRule(short fkOnDeleteRule) { this.fkDeleteRule = fkOnDeleteRule; }

    public String getFkColName() {
        return fkColName;
    }

    public void setFkColName(String fkColName) {
        this.fkColName = fkColName;
    }

    public static String parseImportedKeyRule(short rule){
        String rst = "";
        switch (rule){
            case DatabaseMetaData.importedKeyNoAction:
                rst = "NO ACTION";
                break;
            case DatabaseMetaData.importedKeyCascade:
                rst = "CASCADE";
                break;
            case DatabaseMetaData.importedKeySetNull:
                rst = "SET NULL";
                break;
            case DatabaseMetaData.importedKeyRestrict:
                rst = "RESTRICT";
                break;
        }
        return rst;
    }

    private boolean isPk = false;
    private short pkSEQ = 0;//主键排序
    private boolean isFk = false;
    private String fkTb;
    private String fkColName;//外键列名
    private short fkUpdateRule = DatabaseMetaData.importedKeyNoAction;
    private short fkDeleteRule = DatabaseMetaData.importedKeyNoAction;
    private String name;
	private String type;
    private int colLength;
    private int nullAble = ResultSetMetaData.columnNullable;
}
