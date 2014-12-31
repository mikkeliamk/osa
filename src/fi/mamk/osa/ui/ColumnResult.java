package fi.mamk.osa.ui;

import java.util.LinkedHashMap;
import java.util.Vector;

public class ColumnResult extends SearchResultConfiguration{

	public ColumnResult(Type type) {
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return super.getType();
	}

	@Override
	public void setType(Type type) {
		// TODO Auto-generated method stub
		super.setType(type);
	}

	@Override
	public String getPagingType() {
		// TODO Auto-generated method stub
		return super.getPagingType();
	}

	@Override
	public void setPagingType(String pagingType) {
		// TODO Auto-generated method stub
		super.setPagingType(pagingType);
	}

	@Override
	public Boolean getPagingEnabled() {
		// TODO Auto-generated method stub
		return super.getPagingEnabled();
	}

	@Override
	public void setPagingEnabled(Boolean pagingEnabled) {
		// TODO Auto-generated method stub
		super.setPagingEnabled(pagingEnabled);
	}

	@Override
	public int getRows() {
		// TODO Auto-generated method stub
		return super.getRows();
	}

	@Override
	public void setRows(int rows) {
		// TODO Auto-generated method stub
		super.setRows(rows);
	}

	@Override
	public Vector<String> getLayoutTypelist() {
		// TODO Auto-generated method stub
		return super.getLayoutTypelist();
	}

	@Override
	public void setLayoutTypelist(Vector<String> layoutTypelist) {
		// TODO Auto-generated method stub
		super.setLayoutTypelist(layoutTypelist);
	}

	@Override
	public Vector<String> getColumnNames() {
		// TODO Auto-generated method stub
		return super.getColumnNames();
	}

	@Override
	public void setColumnNames(Vector<String> columnNames) {
		// TODO Auto-generated method stub
		super.setColumnNames(columnNames);
	}

	@Override
	public Vector<Vector<String>> getHeaderKeys() {
		// TODO Auto-generated method stub
		return super.getHeaderKeys();
	}

	@Override
	public void setHeaderKeys(Vector<Vector<String>> headerKeys) {
		// TODO Auto-generated method stub
		super.setHeaderKeys(headerKeys);
	}	
	
	@Override
	public String getDefaultSort() {
		return super.getDefaultSort();
	}
	
	@Override
	public void setDefaultSort(String defaultSort) {
		super.setDefaultSort(defaultSort);
	}
}
