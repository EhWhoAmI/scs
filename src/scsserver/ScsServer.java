/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scsserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Lam Zyun
 */
public class ScsServer extends JFrame{
    private File repoFile;
    boolean serverRunning = false;
    private static ScsServer singleton;
    //Logger
    Logging logger = new Logging("/scsserver.log", true, false);
        
    private ScsServer() {
        super("SCS server control panel");
        setSize(600, 300);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                //Do nothing
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (!serverRunning) {
                    //Kill
                    System.exit(0);
                }
                else {
                    //Do nothing, just iconify.
                    setState(ICONIFIED);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("Window event!");
            }

            @Override
            public void windowIconified(WindowEvent e) {
                //Do nothing
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                //Do nothing
            }

            @Override
            public void windowActivated(WindowEvent e) {
                //Chill
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                //Chill
            }
        });
        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(3, 4, 10, 10));
        
        try {
            //Set look and feel
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        JLabel title = new JLabel("SCS Server Control Panel");
        pane.add(title);
        
        JLabel servingRepo = new JLabel("Serving:");
        pane.add(servingRepo);
        
        JLabel serverStatus = new JLabel("Server status: Not running");
        pane.add(serverStatus);
        
        JButton start = new JButton("Start Server");
        start.setEnabled(false);
        pane.add(start);
        
        JButton kill = new JButton("Kill Server");
        kill.setEnabled(false);
        pane.add(kill);
        
        JButton chooseRepo = new JButton("Choose Repo");
        chooseRepo.addActionListener((e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("choosertitle");
            chooser.setAcceptAllFileFilterUsed(false);
            
            if (chooser.showOpenDialog(pane) == JFileChooser.APPROVE_OPTION) {
                repoFile = chooser.getSelectedFile();
                //Check whether it is a scs repo
                String[] fileList = repoFile.list();
                if (fileList.length == 0 | !isSCSRepo(repoFile)) {
                    //Is not an scs repo.
                    JOptionPane.showMessageDialog(pane, repoFile.getAbsolutePath() + "is not an scs repo");
                    repoFile = null;
                } else {
                    //Then pass
                    System.out.println(repoFile.toString() + " is scs repo.");
                    //Edit label
                    servingRepo.setText("Serving: " + repoFile.getAbsolutePath());
                    start.setEnabled(true);
                }
            }
        });
        pane.add(chooseRepo);
        
        JButton logs = new JButton("Show Logs");
        logs.addActionListener((e) -> {
            try {
                //Open the file in a new window
                JFrame logWin = new JFrame("Scs Logs");
                logWin.setLayout(new BorderLayout());
                JPanel logPanel = new JPanel();
                JTextArea logText = new JTextArea(5, 50);
                logText.setEditable(false);
                
                JScrollPane scrollPane = new JScrollPane(logText);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                Scanner fileInput = new Scanner(new File(System.getProperty("user.dir") + "/scsserver.log"));
                while (fileInput.hasNextLine()) {
                    logText.append(fileInput.nextLine() + "\n");
                }
                
                logPanel.add(scrollPane);
                logWin.add(logPanel);
                logWin.setPreferredSize(new Dimension(500, 500));
                logWin.setSize(500, 500);
                logWin.add(scrollPane);
                logWin.setVisible(true);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        pane.add(logs);
        
        JButton info = new JButton("About scs");
        info.addActionListener((e) -> {
            //Show about
            JOptionPane.showMessageDialog(pane, "SCS\nVersion 0.0.0.0");
        });
        pane.add(info);
        add(pane);
        
        setVisible(true);
    }
    
    private boolean isSCSRepo(File fileCheck) {
        //Open the db folder and check if current and UUID exists. Also check version.
        File dbFile = new File(fileCheck.getPath() + "/db");
        File currentFile = new File(dbFile.getPath() + "/current");
        File UUIDFile = new File(dbFile.getPath() + "/UUID");
        File versionFile = new File(dbFile.getPath() + "/version");
        
        return (dbFile.exists() & currentFile.exists() & UUIDFile.exists() & versionFile.exists());
    }
    
    public static ScsServer getInstance () {
        if (singleton == null)
            singleton = new ScsServer();
        return singleton;
    }
    
    public static void main(String[] args) {
        ScsServer server = ScsServer.getInstance();
    }
}
