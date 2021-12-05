package com.jirefox.pgmagic.dblinkfinder;


import com.formdev.flatlaf.FlatLightLaf;
import com.jirefox.pgmagic.dblinkfinder.view.MainWindow;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.getDefaults().put("Tree.lineTypeDashed", Boolean.TRUE);
        MainWindow mainWindow = new MainWindow();
        mainWindow.setVisible(true);
    }
}
