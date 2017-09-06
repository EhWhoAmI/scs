/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scsserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import static java.awt.Frame.ICONIFIED;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

//This is a bit of a mess, like always. Needs clearing up and documentation.
/**
 * This starts a scs server
 * @author Lam Zyun
 */
public class ScsServer extends JFrame{

    /**
     * Serial version uid
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The file for the repo
     */
    private File repoFile;
    
    /**
     * To check whether the server is running
     */
    boolean serverRunning = false;
    
    /**
     * The settings of the server
     */
    private Properties settings = new Properties();
    
    /**
     * The current instance
     */
    private static ScsServer currentInstance;
    //Logger
    Logging logger = new Logging("/scsserver.log", true, false);
        
    //For things.
    private JButton kill;
    private JButton info;
    private JButton chooseRepo;
    private JButton start;
    private JButton logs;
    private JButton config;
    private JLabel title;
    private JLabel servingRepo;
    private JLabel serverStatus;
    private JPanel pane;
    
    private ScsServer() {
        super("SCS server control panel");
        //Set up window
        setSize(600, 300);
        
        //Window listeners
        addWindowListener(new windowlistener());
        
        //Panel to store all the components
        pane = new JPanel();
        pane.setLayout(new GridLayout(3, 4, 10, 10)); //Set the layout
        
        setLookAndFeel();
        
        //Title
        title = new JLabel("SCS Server Control Panel");
        pane.add(title);
        
        //The repo that we are serving
        servingRepo = new JLabel("Serving:");
        pane.add(servingRepo);
        
        //The server status. Running, starting, not running
        serverStatus = new JLabel("Server status: Not running");
        pane.add(serverStatus);
        
        //Button to start server
        start = new JButton("Start Server");
        start.setEnabled(false);
        start.addActionListener((e) -> {
            startServer();
        });
        pane.add(start);
        
        //Button to start server: Not implemented yet
        kill = new JButton("Kill Server");
        kill.setEnabled(false);
        pane.add(kill);
        
        //Button to choose repo
        chooseRepo = new JButton("Choose Repo");
        chooseRepo.addActionListener((e) -> {
            chooseRepoDialog();
        });
        pane.add(chooseRepo);
        
        //Config
        config = new JButton("SCS Server Config");
        config.addActionListener((e) -> {
           configWin();
        });
        pane.add(config);
        
        //Log show
        logs = new JButton("Show Logs");
        logs.addActionListener((e) -> {
            logWin();
        });
        pane.add(logs);
        
        //About. Nothing to show
        info = new JButton("About scs");
        info.addActionListener((e) -> {
            //Show about
            JOptionPane.showMessageDialog(pane, "SCS\nVersion 0.0.0.0");
        });
        pane.add(info);
        
        //Add all the panes
        add(pane);
        
        setResizable(false);
        setVisible(true);
    }
    
    /**
     * Check if a folder is a scs repo
     * @param fileCheck file to check
     * @return wether of not it is a scs repo
     */
    private boolean isSCSRepo(File fileCheck) {
        //Open the db folder and check if current and UUID exists. Also check version.
        File dbFile = new File(fileCheck.getPath() + "/db");
        File currentFile = new File(dbFile.getPath() + "/current");
        File UUIDFile = new File(dbFile.getPath() + "/UUID");
        File versionFile = new File(dbFile.getPath() + "/version");
        
        return (dbFile.exists() & currentFile.exists() & UUIDFile.exists() & versionFile.exists());
    }
    
    /**
     * SCS Server get instancs
     * @return a new instance of scs server
     */
    public static ScsServer getInstance () {
        if (currentInstance == null)
            currentInstance = new ScsServer();
        return currentInstance;
    }
    
    /**
     * Opens a window to show logs
     * Could add: Logs refresh, clean log file.
     */
    private void logWin() {
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
            //Read from log file, and output all that is in there
            while (fileInput.hasNextLine()) {
                logText.append(fileInput.nextLine() + "\n");
            }

            logPanel.add(scrollPane);
            logWin.add(logPanel);
            
            //Window preferences
            logWin.setPreferredSize(new Dimension(500, 500));
            logWin.setSize(500, 500);
            logWin.setResizable(false);
            logWin.setVisible(true);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Opens a config window for config.
     * To continue work on.
     */
    private void configWin () {
        //Open config file
        File confFile = new File(System.getProperty("user.dir") + "/scsserver/conf.properties");
        try {
            if (confFile.exists()) {
                //Read from config file

                FileInputStream configInputStream = new FileInputStream(confFile);
                settings.load(configInputStream);

            } else {
                //Create file
                confFile.getParentFile().mkdir();
                confFile.createNewFile();

                //Add the default options
                String filename = "";
                if (repoFile != null) 
                    filename = repoFile.getAbsolutePath();
                settings.setProperty("repopath", filename);
                
                //Store settings
                settings.store(new FileOutputStream(confFile), "NONE!!!");
            }
            //Open settings window
            JFrame configWin = new JFrame("Config");

            configWin.setAlwaysOnTop(true);
            configWin.setSize(300, 300);
            
            JPanel winPanel = new JPanel();
            winPanel.setLayout(new GridLayout(3, 3));
            JLabel title = new JLabel("SCS Settings");
            winPanel.add(title, 1, 1);
            
            {
                //The file choose
                JLabel fileChoose = new JLabel("File: ");
                JTextField fileChooseField = new JTextField();
            }
            configWin.add(winPanel);
            configWin.setVisible(true);
        }catch(IOException ioe){
            System.err.println("IO ERROR: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    
    private void setLookAndFeel() {
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
    }
    
    private void startServer () {
        logger.log("Starting server...");
        serverRunning = true;
        serverStatus.setText("Server status: Starting...");
        //Add server start code here

        //When done,
        kill.setEnabled(true);
        start.setEnabled(false);
    }
    
    private void chooseRepoDialog () {
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
                //Set properties
                settings.setProperty("repopath", repoFile.getAbsolutePath());
                //Edit label
                servingRepo.setText("Serving: " + repoFile.getAbsolutePath());
                start.setEnabled(true);
            }
        }
    }
    class windowlistener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {
            //Do nothing
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (!serverRunning) {
                //Kill window
                try {
                    //Store settings
                    settings.store(new FileOutputStream(System.getProperty("user.dir") + "/scsserver/conf.properties"), "NONE!!!");
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                System.exit(0);
            }
            else {
                //Do nothing, just iconify.
                setState(JFrame.ICONIFIED);
            }
        }

        @Override
        public void windowClosed(WindowEvent e) {
            //Skip...
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
    }
    
    /**
     * Main class of the scs server
     * @param args
     */
    public static void main(String[] args) {
        ScsServer server = ScsServer.getInstance();
    }
}
