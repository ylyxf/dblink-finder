package com.jirefox.pgmagic.dblinkfinder.model;

public class ErrorDblinkTable {

    private String sql;

    private String objectName;


    private String objectType;

    private String tableName;

    private String error;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String toString() {
        return "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\r"
                + "~~object name:" + objectName + "\r"
                + "~~object type:" + objectType + "\r"
                + "~~dblink table:" + tableName + "\r"
                + "~~test sql:" + sql + "\r"
                + "~~error:" + error + "\r"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\r";
    }
}
