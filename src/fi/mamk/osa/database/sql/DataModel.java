package fi.mamk.osa.database.sql;

import java.util.HashMap;

public abstract class DataModel {
    
    public abstract HashMap<String,String> getResourceColumns();
    public abstract HashMap<String,String> getSimpleTables();
    public abstract HashMap<String,HashMap<String,String>> getComplexTables();

}
