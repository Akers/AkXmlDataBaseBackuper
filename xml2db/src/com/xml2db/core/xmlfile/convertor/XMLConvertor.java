package com.xml2db.core.xmlfile.convertor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.xml2db.core.database.connector.po.TableIndexer;
import com.xml2db.ui.MainUI;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import com.xml2db.core.database.connector.po.DataTable;
import com.xml2db.core.database.connector.po.TableDataRow;
import com.xml2db.core.database.connector.po.TableFieldDetail;
import java.io.File;

public class XMLConvertor
{
	//将DataTable对象转换成XML文件并输出到指定路径savePath
	public boolean tryConvertToXml(String savePath, String dbName, DataTable dbTable, boolean withDataType){
		File saveDir = new File(savePath);
        if(saveDir.isFile()){
            return  false;
        }
        if(!saveDir.exists()) {
            if(!saveDir.mkdir()){
                MainUI.LOG_PRINTER.appendError("输出路径不存在且创建输出文件夹失败！");
                return false;
            }
        }

        boolean execResult = true;
		SimpleDateFormat dateFormator = new SimpleDateFormat("yyyyMMddkkmmss");
		String dateNow = dateFormator.format(new Date());
		String tbName = dbTable.getName();
		String xmlPath = String.format("%s/%s_%s_%s.xml",
				savePath,
				dbName,
				tbName,
				dateNow);
        Format formater = Format.getCompactFormat();
        formater.setEncoding("UTF-8");
        formater.setIndent("    ");
		Document doc = new Document();
		Document schemaDoc = null;


		Element root = new Element("table");
		//建立根节点
		root.setAttribute("name", tbName);
		doc.setRootElement(root);
		//遍历表结构，生成表数据
		List<TableDataRow> rows = dbTable.getRows();
		List<TableFieldDetail> feidDetails = dbTable.getFieldDetails();

        //生成数据描述的Schema文档，弃用，读取时太过复杂！！
//        if(withDataType){
//            schemaDoc = new Document();
//            Namespace xsNs = Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema");
//            Element xsdRoot = new Element("schema", xsNs);
//            xsdRoot.setAttribute("elementFormDefault", "qualified");
//            xsdRoot.setAttribute("attributeFormDefault", "unqualified");
//            xsdRoot.addContent(new Element("simpleType", xsNs).setAttribute("name", "varchar")
//                    .addContent(new Element("restriction", xsNs).setAttribute("base", "xs:string")
//                        .addContent(new Element("maxLength", xsNs).setAttribute("value", "20"))));
//            schemaDoc.setRootElement(xsdRoot);
//            Element _eTable = new Element("element", xsNs);
//            Element _eRow = new Element("element", xsNs).addContent(new Element("complexType", xsNs));
//            //为每个字段创建字段类型
//            Element _eAttr = null;
//            String attrTypeStr = "";
//            for(TableFieldDetail field : feidDetails){
//                _eAttr = new Element("attribute", xsNs)
//                        .setAttribute("name", field.getName())
//                        .setAttribute("type", field.getType());
//
//                _eRow.getChild("complexType", xsNs).addContent(_eAttr);
//                System.out.println(field.getName()+": "+field.getType());
//            }
//            _eTable.setAttribute("name", "table");
//            _eTable.addContent(new Element("complexType", xsNs)
//                    .addContent(new Element("sequence", xsNs)).addContent(_eRow));
//            xsdRoot.addContent(_eTable);
//        }

        //生成数据类型描述元素
        if(withDataType){
            Element typeElement = new Element("feildTypes");
            for(TableFieldDetail field : feidDetails){
                typeElement.addContent(new Element("feild")
                        .setAttribute("fieldName", field.getName())
                        .setAttribute("fieldType", field.getType())
                        .setAttribute("colLength", field.getColLength()+"")
                        .setAttribute("isPk", field.isPk()+"")
                        .setAttribute("isFk", field.isFk()+"")
                        .setAttribute("fkTable", field.getFkTb())
                        .setAttribute("fkColName", field.getFkColName()+"")
                        .setAttribute("fkUpdateRulr", field.getFkUpdateRule()+"")
                        .setAttribute("fkDeleteRulr", field.getFkDeleteRule()+"")
                        .setAttribute("nullAble", field.isNullAble()+"")
                );
            }
            root.addContent(typeElement);
        }

        //生成索引描述元素
        Element indexElement = new Element("indexers");
        for (TableIndexer index : dbTable.getIndexers()){
            indexElement.addContent(new Element("indexer")
                .setAttribute("name", index.getIndexName())
                .setAttribute("col_name", index.getColName())
                .setAttribute("non_unique", index.isNonUniQue() + "")
            );
        }
        root.addContent(indexElement);

		Element element = null;
        for (TableDataRow row : rows) {
            element = new Element("row");
            for (int vc = 0; vc < (row.getAll().size()); vc++) {
                element.setAttribute(feidDetails.get(vc).getName(), this.transNull(row.get(vc)));
            }
            root.addContent(element);
        }
		
		//输出文档：
		XMLOutputter xmlOut = new XMLOutputter(formater);
		try {
//            if(withDataType){
//                xmlOut.output(schemaDoc, new FileOutputStream(xsdPath));
//            }
            FileOutputStream fos = new FileOutputStream(xmlPath);
			xmlOut.output(doc, fos);
            fos.flush();
            fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			execResult = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			execResult = false;
		}
		return execResult;
	}
	
	//将xml文件转换成TableDetail
	public DataTable parse(String xmlPath){
		DataTable rs = null;
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			//内容处理类
			SaxXmlHandler cth = new SaxXmlHandler();
			//绑定内容处理类
			parser.setContentHandler(cth);
			//以第一参数提供路径作为xml文件的路径
			parser.parse(xmlPath);
			
			rs = cth.getParseResult();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}

	
	private String transNull(String input){
		String rs = input;
		if(input == null || input.toLowerCase().equals("null"))
			rs = "";
		else if(input.trim().equals(""))
			rs = "null";
		return rs;
	}
	
	private class SaxXmlHandler extends DefaultHandler{
		@Override
		public void startDocument() throws SAXException {
			this.dbTable = new DataTable();
		}

		@Override
		public void endDocument() throws SAXException {
			this.handlFinished = true;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
//			System.out.println("uri="+uri+"; localName="+localName);
			//qName 为元素名
			//处理根元素获取数据表名
			if(qName.equals(rootElementName)){
				int index = attributes.getIndex("name");
				this.dbTable.setName(attributes.getValue(index));
			}
            if(qName.equals(this.typeElementRootName))
                this.isTypeElement = true;
            if(qName.equals(this.typeElementItemName) && isTypeElement){
                //处理字段类型列表
                TableFieldDetail tfd = new TableFieldDetail(
                        Boolean.parseBoolean(attributes.getValue("isPk")),
                        Boolean.parseBoolean(attributes.getValue("isFk")),
                        attributes.getValue("fkTable"),
                        attributes.getValue(this.attrFieldName),
                        attributes.getValue(this.attrTypeName),
                        Integer.parseInt(attributes.getValue(this.attrColLenghtName)),
                        Integer.parseInt(attributes.getValue("nullAble")));
                if(tfd.isFk()){
                    tfd.setFkDeleteRule(Short.parseShort(attributes.getValue("fkDeleteRulr")));
                    tfd.setFkUpdateRule(Short.parseShort(attributes.getValue("fkUpdateRulr")));
                    tfd.setFkColName(attributes.getValue("fkColName"));
                }
                this.dbTable.addField(tfd);

            }
            if(qName.equals(this.indexRootElementName)){
                //标记处理索引节点的子节点
                this.isIndexerElement = true;
            }
            if(isIndexerElement && qName.equals(this.indexItemName)){
                this.dbTable.addIndexer(new TableIndexer(
                        attributes.getValue("name"),
                        attributes.getValue("col_name"),
                        Boolean.parseBoolean(attributes.getValue("non_unique"))
                ));
            }
			if(qName.equals(this.rowElementName)){
				//处理row元素，获取数据行
				this.row = new TableDataRow();
				//从row节点获取字段
				for(int i=0;i<attributes.getLength();i++){
					row.add(transNull(attributes.getValue(i)));
				}
				
				this.dbTable.addRow(this.row);
			}
		}

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(qName.equals(this.typeElementRootName))
                this.isTypeElement = false;
            if(qName.equals(this.indexRootElementName))
                this.isIndexerElement = false;
        }

        public DataTable getParseResult(){return this.dbTable;}
//		public void setDataTable(TableDetail td){this.dbTable = td;}

        private boolean handlFinished = false;
		private boolean isTypeElement = false;
        private boolean isIndexerElement = false;
		private DataTable dbTable = null;
		private String rootElementName = "table";
		private String rowElementName = "row";
        private String typeElementRootName = "feildTypes";
        private String typeElementItemName = "feild";
        private String attrTypeName = "fieldType";
        private String attrFieldName = "fieldName";
        private String attrColLenghtName = "colLength";
        private String indexRootElementName = "indexers";
        private String indexItemName = "indexer";
        private TableDataRow row = null;
    }
}
