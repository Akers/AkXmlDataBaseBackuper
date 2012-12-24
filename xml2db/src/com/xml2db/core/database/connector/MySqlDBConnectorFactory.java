package com.xml2db.core.database.connector;
import com.xml2db.core.database.connector.impl.MySqlDBConnector;

public class MySqlDBConnectorFactory implements DBConnectorFactory{

	@Override
	public DBConnector getDBConnector(){
		return new MySqlDBConnector();
	}

}
