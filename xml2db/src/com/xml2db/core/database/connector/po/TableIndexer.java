package com.xml2db.core.database.connector.po;

import java.sql.DatabaseMetaData;


/**
 * Created with IntelliJ IDEA Community Edition
 * User: Akers
 * Date: 12-12-24
 * Time: 上午10:34
 */
public class TableIndexer {
    public TableIndexer(String indexName, String colName, boolean nonUniQue) {
        this.indexName = indexName;
        this.colName = colName;
        this.nonUniQue = nonUniQue;
    }

    public static String parseIndexType(short type){
        String rst = "Other";
        switch (type){
            case DatabaseMetaData.tableIndexStatistic :
                break;
            case DatabaseMetaData.tableIndexClustered :
                break;
            case DatabaseMetaData.tableIndexHashed :
                break;

        }
        return rst;
    }

    public boolean isNonUniQue() {
        return nonUniQue;
    }

    public void setNonUniQue(boolean nonUniQue) {
        this.nonUniQue = nonUniQue;
    }

    public String getIndexQualifier() {
        return indexQualifier;
    }

    public void setIndexQualifier(String indexQualifier) {
        this.indexQualifier = indexQualifier;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    private boolean nonUniQue = false;//索引是否可以不唯一
    private String indexQualifier = null;//索引类别
    private String indexName = "";
    private short type = DatabaseMetaData.tableIndexOther;
    private String colName = "";//索引列名
}
