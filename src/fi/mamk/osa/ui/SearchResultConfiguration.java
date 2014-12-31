package fi.mamk.osa.ui;

import java.util.LinkedHashMap;
import java.util.Vector;

public abstract class SearchResultConfiguration {

	public static enum Type {columns, grid}; // Layout type, e.g. columns (table), grid, etc...
	private String pagingType; // Paging type, e.g. scroll, pages etc...
	private Boolean pagingEnabled = true;
	private int rows = -1;
	private Type type;
	private String defaultSort = "[0, 'asc']";
	private Vector<String> layoutTypelist = new Vector<String>();
	private Vector<String> columnNames = new Vector<String>();
	private Vector<Vector<String>> headerKeys = new Vector<Vector<String>>();
	
	public SearchResultConfiguration(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public String getPagingType() {
		return pagingType;
	}
	
	public void setPagingType(String pagingType) {
		this.pagingType = pagingType;
	}
	
	public Boolean getPagingEnabled() {
		return pagingEnabled;
	}
	
	public void setPagingEnabled(Boolean pagingEnabled) {
		this.pagingEnabled = pagingEnabled;
	}
	
	public int getRows() {
		return rows;
	}
	
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * @return the layoutTypelist
	 */
	public Vector<String> getLayoutTypelist() {
		return layoutTypelist;
	}

	public String getColumnNamesJson(){
		String colNames = new flexjson.JSONSerializer().serialize(headerKeys);
		return colNames;
	}
	
	/**
	 * @param layoutTypelist the layoutTypelist to set
	 */
	public void setLayoutTypelist(Vector<String> layoutTypelist) {
		this.layoutTypelist = layoutTypelist;
	}

	/**
	 * @return the columnNames
	 */
	public Vector<String> getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(Vector<String> columnNames) {
		this.columnNames = columnNames;
	}

	/**
	 * @return the headerKeys
	 */
	public Vector<Vector<String>> getHeaderKeys() {
		return headerKeys;
	}

	/**
	 * @param headerKeys the headerKeys to set
	 */
	public void setHeaderKeys(Vector<Vector<String>> headerKeys) {
		this.headerKeys = headerKeys;
	}

	public String getDefaultSort() {
		return defaultSort;
	}

	public void setDefaultSort(String defaultSort) {
		this.defaultSort = defaultSort;
	}
	
}
