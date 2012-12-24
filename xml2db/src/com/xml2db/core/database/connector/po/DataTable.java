package com.xml2db.core.database.connector.po;

import java.util.ArrayList;
import java.util.List;

public class DataTable {
	public DataTable(){
		this.fieldDetails = new ArrayList<TableFieldDetail>();
		this.dataRows = new ArrayList<TableDataRow>();
        this.indexers = new ArrayList<TableIndexer>();
	}
	
	public void addField(TableFieldDetail field){
		this.fieldDetails.add(field);
	}
	public TableFieldDetail getFieldDetail(int pos){
		TableFieldDetail rst = null;
		if(pos >= 0 && pos <= this.fieldDetails.size()){
			rst = this.fieldDetails.get(pos);
		}
		return rst;
	}
	public void addRow(TableDataRow row){this.dataRows.add(row);}
	public TableDataRow getRow(int pos){return this.dataRows.get(pos);}
	public int getRowCount(){return this.dataRows.size();}
	public List<TableDataRow> getRows(){return this.dataRows;}
	public List<TableFieldDetail> getFieldDetails(){return this.fieldDetails;}
	public void appendTableFidleDetail(TableFieldDetail tfd){this.fieldDetails.add(tfd);}
	public void setFieldDetails(List<TableFieldDetail> tfds){this.fieldDetails = tfds;}
	public void setName(String tbName){this.name = tbName;}
	public String getName(){return this.name;}
    public List<TableIndexer> getIndexers(){return this.indexers;}
    public void setIndexers(List<TableIndexer> indexers){ this.indexers = indexers; }
    public void addIndexer(TableIndexer indexer){ this.indexers.add(indexer);}

    private List<TableIndexer> indexers = null;
	private String name = "";
	private List<TableFieldDetail> fieldDetails = null;
	private List<TableDataRow> dataRows = null;
}
