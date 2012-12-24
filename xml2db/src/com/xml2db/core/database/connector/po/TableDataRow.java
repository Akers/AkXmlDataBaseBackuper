package com.xml2db.core.database.connector.po;

import java.util.ArrayList;
import java.util.List;

public class TableDataRow {
	public void add(String value){this.fields.add(value);}
	public String get(int pos){return this.fields.get(pos);}
	public List<String> getAll(){return this.fields;}
	List<String> fields = new ArrayList<String>();
}
