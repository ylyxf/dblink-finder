package com.jirefox.pgmagic.dblinkfinder.model;

import java.util.Objects;

public class ObjectTable {

    private String objectName;

    private String objectType;

    private String tableName;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectTable that = (ObjectTable) o;
        return Objects.equals(objectName, that.objectName) && Objects.equals(objectType, that.objectType) && Objects.equals(tableName, that.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectName, objectType, tableName);
    }
}
