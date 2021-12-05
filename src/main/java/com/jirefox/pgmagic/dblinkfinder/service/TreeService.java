package com.jirefox.pgmagic.dblinkfinder.service;

import com.jirefox.pgmagic.dblinkfinder.view.MainWindow;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import javax.swing.tree.DefaultMutableTreeNode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TreeService {

    private Connection h2;

    private QueryRunner qr = new QueryRunner();

    private MainWindow mainWindow;

    public TreeService(MainWindow mainWindow, Connection h2) {
        this.mainWindow = mainWindow;
        this.h2 = h2;
    }

    public void drawTree(String displayType) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("ROOT");
        try {
            if ("table first".equals(displayType)) {
                String firstSql = "select distinct TABLE_NAME from OBJECT_TABLE order by  TABLE_NAME ASC";
                List<Map<String, Object>> firstLevelItems = qr.query(h2, firstSql, new MapListHandler());
                String secondSql = "select OBJECT_NAME from OBJECT_TABLE where TABLE_NAME =  ? order by  OBJECT_TYPE,OBJECT_NAME ASC";
                for (Map<String, Object> firstLevelItem : firstLevelItems) {
                    String tableName = (String) firstLevelItem.get("TABLE_NAME");
                    DefaultMutableTreeNode firstLevelNode = new DefaultMutableTreeNode(tableName);
                    rootNode.add(firstLevelNode);

                    List<Map<String, Object>> secondLevelItems = qr.query(h2, secondSql, new MapListHandler(), tableName);
                    for (Map<String, Object> secondLevelItem : secondLevelItems) {
                        String objectName = (String) secondLevelItem.get("OBJECT_NAME");
                        String objectType = (String) secondLevelItem.get("OBJECT_TYPE");
                        DefaultMutableTreeNode secondLevelNode = new DefaultMutableTreeNode(objectName);
                        firstLevelNode.add(secondLevelNode);
                    }
                }
            } else if ("object first".equals(displayType)) {
                String firstSql = "select distinct OBJECT_NAME,OBJECT_TYPE from OBJECT_TABLE order by  OBJECT_TYPE,OBJECT_NAME ASC";
                List<Map<String, Object>> firstLevelItems = qr.query(h2, firstSql, new MapListHandler());
                String secondSql = "select TABLE_NAME from OBJECT_TABLE where OBJECT_NAME =  ?  AND OBJECT_TYPE = ? order by  TABLE_NAME ASC";
                for (Map<String, Object> firstLevelItem : firstLevelItems) {
                    String objectName = (String) firstLevelItem.get("OBJECT_NAME");
                    String objectType = (String) firstLevelItem.get("OBJECT_TYPE");
                    DefaultMutableTreeNode firstLevelNode = new DefaultMutableTreeNode(objectName);
                    rootNode.add(firstLevelNode);

                    List<Map<String, Object>> secondLevelItems = qr.query(h2, secondSql, new MapListHandler(), objectName, objectType);
                    for (Map<String, Object> secondLevelItem : secondLevelItems) {
                        String tableName = (String) secondLevelItem.get("TABLE_NAME");
                        DefaultMutableTreeNode secondLevelNode = new DefaultMutableTreeNode(tableName);
                        firstLevelNode.add(secondLevelNode);
                    }
                }
            }
        } catch (SQLException ex) {
            mainWindow.printex(ex);
        }
        mainWindow.updateTree(rootNode);
    }
}
