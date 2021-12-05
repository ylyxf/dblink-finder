package com.jirefox.pgmagic.dblinkfinder.service;

import com.jirefox.pgmagic.dblinkfinder.model.ErrorDblinkTable;
import com.jirefox.pgmagic.dblinkfinder.view.MainWindow;
import com.jirefox.pgmagic.dblinkfinder.model.ObjectTable;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class SearchService {

    private MainWindow mainWindow;

    private String url;

    private String user;

    private String password;

    private Connection ora;

    private Connection h2;

    private HashSet<ObjectTable> objectTableHashSet = new HashSet<ObjectTable>();

    QueryRunner qr = new QueryRunner();

    private HashSet<String> dblinks = new HashSet<String>();

    private List<ErrorDblinkTable> errorDblinkTables = new ArrayList<ErrorDblinkTable>();
    private List<String> notDblinkTables = new ArrayList<String>();

    private ScriptEngine engine;

    private String DDL_OBJECT_TABLE = "create table OBJECT_TABLE (object_name VARCHAR(128),object_type VARCHAR(64),table_name VARCHAR(256))";

    public SearchService(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void search(String url, String user, String password, boolean skipDblinkValidation) {
        this.url = url;
        this.user = user;
        this.password = password;


        try {
            mainWindow.clearConsole();
            mainWindow.updateTree(new DefaultMutableTreeNode("ROOT"));
            String file = new File("").getAbsolutePath() + "/parser.js";
            mainWindow.println("Load JavaScript Parser:" + file, Color.magenta);

            FileReader fileReader = new FileReader(new File(file));
            ScriptEngineManager manager = new ScriptEngineManager();
            engine = manager.getEngineByName("javascript");
            engine.eval(fileReader);

            mainWindow.println("Start Connect Database", Color.magenta);
            Class.forName("oracle.jdbc.driver.OracleDriver");
            ora = DriverManager.getConnection(url, user, password);

            qr.update(h2, "delete from OBJECT_TABLE");

            try {
                List dbaDbinks = qr.query(ora, "select DB_LINK as dblink from ALL_DB_LINKS ", new ColumnListHandler<String>());
                dblinks.addAll(dbaDbinks);
                mainWindow.println("query all dblinks : ", Color.magenta);
                for (String dblink : dblinks) {
                    mainWindow.println(dblink, Color.blue);
                }
            } catch (Exception ex) {
                mainWindow.println("WARNING: ERROR READING FROM ALL_DB_LINKS", Color.magenta);
                mainWindow.println("query user dblinks : ", Color.magenta);
                List userDbinks = qr.query(ora, "select OBJECT_NAME as dblink  from user_objects t where t.object_type='DATABASE LINK'", new ColumnListHandler<String>());
                dblinks.addAll(userDbinks);
                for (String dblink : dblinks) {
                    mainWindow.println(dblink, Color.blue);
                }
            }

            String synonymSql = null;
            Connection testConn = null;
            try {
                testConn = DriverManager.getConnection(url, user, password);
                qr.update(testConn, "select 1 from ALL_SYNONYMS t where 1<>1");
                synonymSql = "SELECT t.synonym_name as name ,'SYNONYM' as TYPE, t.table_owner||'.'||t.table_name||'@'||t.db_link as content FROM ALL_SYNONYMS t  where t.db_link is not null";
            } catch (Exception ex) {
                synonymSql = "SELECT t.synonym_name as name ,'SYNONYM' as TYPE, t.table_owner||'.'||t.table_name||'@'||t.db_link as content FROM USER_SYNONYMS t  where t.db_link is not null";
            } finally {
                DbUtils.closeQuietly(testConn);
            }


            String[] objectQuerySqls = {
                    "select view_name as name ,'VIEW' as type, text  as content  from user_views",
                    "select name ,type, text as content from user_source  WHERE text like  '%@%'",
                    synonymSql,

            };
            for (String objectQuerySql : objectQuerySqls) {

                List<Map<String, Object>> rows = qr.query(ora, objectQuerySql, new MapListHandler());
                for (Map<String, Object> row : rows) {
                    String name = (String) row.get("name");
                    String type = (String) row.get("type");
                    String content = (String) row.get("content");
                    mainWindow.println("parsing " + type + " : " + name, Color.magenta);
                    HashSet<String> dblinkTables = new HashSet<String>();
                    dblinkTables.addAll(parseDblinkTableByJs((String) row.get("content")));
                    mainWindow.println("parsed " + type + "  " + name + ": find " + dblinkTables.size() + " dblink tables.", Color.magenta);
                    for (String dblinkTable : dblinkTables) {
                        mainWindow.println("validate  dblink table : " + dblinkTable + "... ", Color.magenta);
                        String trySql = " select 1 from  " + dblinkTable + " where 1<>1 ";
                        Connection tempConn = null;
                        try {
                            if (skipDblinkValidation) {
                                mainWindow.println("validate success dblink table : " + dblinkTable + "", Color.magenta);
                            } else {
                                tempConn = DriverManager.getConnection(url, user, password);
                                qr.update(tempConn, trySql);
                            }
                        } catch (Exception ex) {
                            ErrorDblinkTable errorDblinkTable = new ErrorDblinkTable();
                            errorDblinkTable.setError(ex.getMessage());
                            errorDblinkTable.setTableName(dblinkTable);
                            errorDblinkTable.setObjectName(name);
                            errorDblinkTable.setObjectType(type);
                            errorDblinkTable.setSql(trySql);
                            errorDblinkTables.add(errorDblinkTable);
                            mainWindow.println("validate  error : " + ex.getMessage(), Color.red);
                            continue;
                        } finally {
                            DbUtils.closeQuietly(tempConn);
                        }
                        mainWindow.println("validate  done ", Color.magenta);
                        ObjectTable objectTable = new ObjectTable();
                        objectTable.setObjectName(name);
                        objectTable.setObjectType(type);
                        objectTable.setTableName(dblinkTable);
                        objectTableHashSet.add(objectTable);
                    }
                }
            }
            for (ObjectTable objectTable : objectTableHashSet) {
                String insertSql = "insert into OBJECT_TABLE(object_name,object_type,table_name) values(?,?,?)";
                String objectName = objectTable.getObjectName();
                String objectType = objectTable.getObjectType();
                String tableName = objectTable.getTableName();

                try {
                    qr.update(h2, insertSql, new Object[]{objectName, objectType, tableName});
                } catch (SQLException e) {
                    mainWindow.printex(e);
                }
            }


            //
            mainWindow.println("------------- parsed dblink table is not a real dblink table  -------------------", Color.magenta);
            for (ErrorDblinkTable errorDblinkTable : errorDblinkTables) {
                mainWindow.println(errorDblinkTable.toString(), Color.red);
            }

            //
            mainWindow.println("------------- sql segment contains @ but not parsed as an dblink table -------------------", Color.magenta);
            for (String notDblinkTable : notDblinkTables) {
                mainWindow.println(notDblinkTable, Color.red);
            }

        } catch (Exception ex) {
            if (ora != null) {
                DbUtils.closeQuietly(ora);
            }
            mainWindow.printex(ex);
            ora = null;
        } finally {
            mainWindow.afterSearch();
        }
    }

    public Connection initH2() {
        try {
            Class.forName("org.h2.Driver");
            String dbname = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            h2 = DriverManager.getConnection("jdbc:h2:file:./logs/" + dbname, "sa", "");
            QueryRunner qr = new QueryRunner();
            qr.update(h2, DDL_OBJECT_TABLE);
        } catch (Exception ex) {
            if (h2 != null) {
                DbUtils.closeQuietly(h2);
            }
            mainWindow.printex(ex);
            h2 = null;
        }
        return h2;
    }


    private HashSet<String> parseDblinkTableByJs(String content) {
        HashSet<String> dblinkTables = new HashSet<String>();
        try {
            if (engine instanceof Invocable) {
                Invocable in = (Invocable) engine;
                JSObject result = (JSObject) in.invokeFunction("parse", content, this.dblinks, url, user);
                Collection jsDblinktables = ((JSObject) result.getMember("dblinkTables")).values();
                for (Object dblinkTable : jsDblinktables) {
                    dblinkTables.add(dblinkTable.toString());
                }

                Collection jsNotDblinkTables = ((JSObject) result.getMember("notDblinkTables")).values();
                for (Object notDblinkTable : jsNotDblinkTables) {
                    this.notDblinkTables.add(notDblinkTable.toString());
                }

            }
        } catch (Exception ex) {
            mainWindow.printex(ex);
        }
        return dblinkTables;
    }
}
