package com.jirefox.pgmagic.dblinkfinder.service;

import com.jirefox.pgmagic.dblinkfinder.view.MainWindow;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class GenSqlService {

    private Connection h2;

    private QueryRunner qr = new QueryRunner();

    private MainWindow mainWindow;

    public static final String VIEW_FLAG = "/* dblink-observer-auto-generate */";

    public GenSqlService(MainWindow mainWindow, Connection h2) {
        this.mainWindow = mainWindow;
        this.h2 = h2;
    }

    public void genViewSql(String prefix) {
        try {
            List<String> tableNames = qr.query(h2, "select distinct TABLE_NAME from OBJECT_TABLE order by  TABLE_NAME ASC", new ColumnListHandler<String>());
            String sql = "";
            for (String name : tableNames) {
                String newName = name;
                if (newName.indexOf('@') != -1) {
                    newName = newName.substring(0, newName.indexOf('@'));
                }
                if (newName.indexOf('.') != -1) {
                    newName = newName.substring(newName.indexOf('.') + 1);
                }
                sql += "CREATE  OR REPLACE  VIEW " + VIEW_FLAG + prefix + newName + " as select *from " + name + ";\r\n ";
            }
            mainWindow.println(sql, Color.blue);
        } catch (Exception e) {
            mainWindow.printex(e);
        }
    }

}
