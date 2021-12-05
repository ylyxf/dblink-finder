package com.jirefox.pgmagic.dblinkfinder.view;

import com.jirefox.pgmagic.dblinkfinder.service.GenSqlService;
import com.jirefox.pgmagic.dblinkfinder.service.SearchService;
import com.jirefox.pgmagic.dblinkfinder.service.TreeService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class MainWindow extends JFrame {

    private JLabel urlLabel = new JLabel("Url:");

    private JTextField urlTextField = new JTextField("jdbc:oracle:thin:@localhost:1521:orcl");

    private JLabel userLabel = new JLabel("User:");

    private JTextField userTextField = new JTextField("");

    private JLabel passwordLabel = new JLabel("Password");

    private JPasswordField passwordTextField = new JPasswordField("");

    private JCheckBox skipDblinkValidateCheckBox = new JCheckBox("Skip Validate Dblink Table");

    private JButton searchBtn = new JButton("Search");

    private JLabel displayTypeLabel = new JLabel("Display Type:");

    private JComboBox<String> displayTypeComboBox = new JComboBox<String>();

    private JLabel viewPrefixLabel = new JLabel("Create Dblink Table To View's Prefix:");

    private JTextField viewPrefixTextField = new JTextField("dblv_");

    private JButton genSqlBtn = new JButton("generate sql");

    private JTree dblinkTree = new JTree();

    //private JTextPane consoleTextArea = new JTextPane();
    private JTextArea consoleTextArea = new JTextArea();

    private SearchService searchService;

    private TreeService treeService;

    private GenSqlService genViewService;


    private SimpleAttributeSet style = new SimpleAttributeSet();

    public MainWindow() {
        setTitle("Dblink Table Finder");
        setSize(1024, 768);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        init();
        searchService = new SearchService(this);
        Connection h2 = searchService.initH2();
        treeService = new TreeService(this, h2);
        genViewService = new GenSqlService(this, h2);
    }

    public void init() {
        MigLayout layout = new MigLayout("wrap 2");

        Container mainPane = getContentPane();
        mainPane.setLayout(layout);

        JPanel connPanel = new JPanel();
        mainPane.add(connPanel, "span ");

        connPanel.setLayout(new MigLayout());
        connPanel.add(urlLabel);

        connPanel.add(urlTextField, "w 330!");


        connPanel.add(userLabel);
        connPanel.add(userTextField, "w 120!");

        connPanel.add(passwordLabel);
        connPanel.add(passwordTextField, "w 120!");

        skipDblinkValidateCheckBox.setSelected(true);
        connPanel.add(skipDblinkValidateCheckBox);

        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBtn.setEnabled(false);
                String url = urlTextField.getText();
                String user = userTextField.getText();
                String passwd = passwordTextField.getText();
                boolean skipDblinkValidate = skipDblinkValidateCheckBox.isSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        searchService.search(url, user, passwd, skipDblinkValidate);
                        String displayType = displayTypeComboBox.getSelectedItem().toString();
                        treeService.drawTree(displayType);
                    }
                }).start();
            }
        });
        connPanel.add(searchBtn, "gapleft push");

        JSeparator headSep = new JSeparator();
        headSep.setOrientation(JSeparator.HORIZONTAL);
        headSep.setBackground(new Color(153, 153, 153));
        headSep.setPreferredSize(new Dimension(1024, 5));
        mainPane.add(headSep, "span");

        JPanel operationPanel = new JPanel();
        mainPane.add(operationPanel, "span");

        operationPanel.add(displayTypeLabel);


        displayTypeComboBox.addItem("table first");
        displayTypeComboBox.addItem("object first");
        displayTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> comboBox = (JComboBox) e.getSource();
                String displayType = comboBox.getSelectedItem().toString();
                treeService.drawTree(displayType);
            }
        });


        operationPanel.add(displayTypeComboBox);

        //operationPanel.add(statsCheckBox);

        JSeparator opSep = new JSeparator();
        opSep.setOrientation(JSeparator.VERTICAL);
        opSep.setBackground(new Color(153, 153, 153));
        opSep.setPreferredSize(new Dimension(5, 20));
        operationPanel.add(opSep, "span ");

        operationPanel.add(viewPrefixLabel);
        operationPanel.add(viewPrefixTextField, "w 60!");

        genSqlBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prefix = viewPrefixTextField.getText();
                genViewService.genViewSql(prefix);
            }
        });
        operationPanel.add(genSqlBtn);

        JSeparator bodySep = new JSeparator();
        bodySep.setOrientation(JSeparator.HORIZONTAL);
        bodySep.setBackground(new Color(153, 153, 153));
        bodySep.setPreferredSize(new Dimension(1024, 5));
        mainPane.add(bodySep, "span");


        dblinkTree.setRootVisible(false);
        dblinkTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("ROOT")));
        JScrollPane dblinkScrollTextArea = new JScrollPane(dblinkTree);
        dblinkScrollTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dblinkScrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPane.add(dblinkScrollTextArea, "w 275! , h 600!");

        JScrollPane consoleScrollTextArea = new JScrollPane(consoleTextArea);
        consoleScrollTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        consoleScrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPane.add(consoleScrollTextArea, "w 710!, h 600!");

    }

    public void println(String msg, Color color) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    StyleConstants.setForeground(style, color);
//                    Document doc = consoleTextArea.getStyledDocument();
//                    doc.insertString(doc.getLength(), msg+ "\r\n", style);
                    consoleTextArea.append(msg + "\r\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void printex(Exception ex) {
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            String text = "";
            for (int i = 0; i < stackElements.length; i++) {
                text += stackElements[i].getClassName() + ".";
                text += stackElements[i].getMethodName() + "(";
                text += stackElements[i].getLineNumber() + ")\r\n";
            }
            println(text, Color.red);
        }
        ex.printStackTrace();
    }

    public void clearConsole() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                consoleTextArea.setText("");
            }
        });
    }

    public void afterSearch() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                searchBtn.setEnabled(true);
            }
        });
    }

    public void updateTree(TreeNode root) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dblinkTree.setModel(new DefaultTreeModel(root));
            }
        });
    }
}
