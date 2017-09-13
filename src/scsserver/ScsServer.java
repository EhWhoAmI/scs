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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
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
import scstools.scsutils;

//This is a bit of a mess, like always. Needs clearing up and documentation.
//And serious refractoring
/**
 * This starts a scs server
 *
 * @author Lam Zyun
 */
public class ScsServer extends JFrame {

    /**
     * Serial version uid
     */
    private static final long serialVersionUID = 1L;

    /**
     * The file for the repo
     */
    private File repoBaseFile;

    /**
     * To check whether the server is running
     */
    boolean serverRunning = false;

    /**
     * The settings of the server
     */
    private Properties settingsProperties = new Properties();

    /**
     * The current instance
     */
    private static ScsServer currentInstance;

    private static final int serverport = 19319; //SCS in numbers of the alphabet
    //Logger
    Logging logger = new Logging("/scsserver.log", true, true);

    //For things.
    private JButton killServerButton;
    private JButton infoButton;
    private JButton chooseRepoButton;
    private JButton startServerBttn;
    private JButton showLogsButton;
    private JButton configButton;
    private JLabel titleLabel;
    private JLabel servingRepoLabel;
    private JLabel serverStatusLabel;
    private JPanel mainPane;

    //The repo data
    int repoCommitNumber;
    String repoUUID;
    boolean currentPushExists;

    private ScsServer() {
        super("SCS server control panel");
        //Set up window
        setSize(600, 300);

        //Window listeners
        addWindowListener(new windowlistener());

        //Panel to store all the components
        mainPane = new JPanel();
        mainPane.setLayout(new GridLayout(3, 4, 10, 10)); //Set the layout

        setLookAndFeel();

        //Title
        titleLabel = new JLabel("SCS Server Control Panel");
        mainPane.add(titleLabel);

        //The repo that we are serving
        servingRepoLabel = new JLabel("Serving:");
        mainPane.add(servingRepoLabel);

        //The server status. Running, starting, not running
        serverStatusLabel = new JLabel("Server status: Not running");
        mainPane.add(serverStatusLabel);

        //Button to start server
        startServerBttn = new JButton("Start Server");
        startServerBttn.setEnabled(false);
        startServerBttn.addActionListener((e) -> {
            startServer();
        });
        mainPane.add(startServerBttn);

        //Button to start server: Not implemented yet
        killServerButton = new JButton("Kill Server");
        killServerButton.setEnabled(false);
        mainPane.add(killServerButton);

        //Button to choose repo
        chooseRepoButton = new JButton("Choose Repo");
        chooseRepoButton.addActionListener((e) -> {
            chooseRepoDialog();
        });
        mainPane.add(chooseRepoButton);

        //Config
        configButton = new JButton("SCS Server Config");
        configButton.addActionListener((e) -> {
            configWin();
        });
        mainPane.add(configButton);

        //Log show
        showLogsButton = new JButton("Show Logs");
        showLogsButton.addActionListener((e) -> {
            logWin();
        });
        mainPane.add(showLogsButton);

        //About. Nothing to show
        infoButton = new JButton("About scs");
        infoButton.addActionListener((e) -> {
            //Show about
            JOptionPane.showMessageDialog(mainPane, "SCS\nVersion 0.0.0.0");
        });
        mainPane.add(infoButton);

        //Add all the panes
        add(mainPane);

        setResizable(false);
        setVisible(true);
    }

    

    /**
     * SCS Server get instancs
     *
     * @return a new instance of scs server
     */
    public static ScsServer getInstance() {
        if (currentInstance == null) {
            currentInstance = new ScsServer();
        }
        return currentInstance;
    }

    /**
     * Opens a window to show logs Could add: Logs refresh, clean log file.
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
     * Opens a config window for config. To continue work on.
     */
    private void configWin() {
        //Open config file
        File configPropertiesFile = new File(System.getProperty("user.dir") + "/scsserver/conf.properties");
        try {
            if (configPropertiesFile.exists()) {
                //Read from config file

                FileInputStream configInputStream = new FileInputStream(configPropertiesFile);
                settingsProperties.load(configInputStream);

            } else {
                //Create file
                configPropertiesFile.getParentFile().mkdir();
                configPropertiesFile.createNewFile();

                //Add the default options
                String filename = "";
                if (repoBaseFile != null) {
                    filename = repoBaseFile.getAbsolutePath();
                }
                settingsProperties.setProperty("repopath", filename);

                //Store settings
                settingsProperties.store(new FileOutputStream(configPropertiesFile), "NONE!!!");
            }
            //Open settings window
            JFrame configWin = new JFrame("Config");

            configWin.setAlwaysOnTop(true);
            configWin.setSize(300, 300);

            JPanel winPanel = new JPanel();
            winPanel.setLayout(new GridLayout(3, 3));
            JLabel title = new JLabel("SCS Settings");
            winPanel.add(title);

            {
                //The file choose
                JLabel fileChoose = new JLabel("File: ");
                JTextField fileChooseField = new JTextField();
            }
            configWin.add(winPanel);
            configWin.setVisible(true);
        } catch (IOException ioe) {
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

    private void startServer() {
        logger.log("Starting server...");
        serverRunning = true;
        serverStatusLabel.setText("Server status: Starting...");
        //Add server start code here
        Runnable server = () -> {
            //Server code here.
            new ServerMainframe(repoUUID, repoCommitNumber, repoBaseFile);
        };
        Thread t = new Thread(server);
        t.start();
        //When done,
        killServerButton.setEnabled(true);
        startServerBttn.setEnabled(false);
    }

    private void chooseRepoDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose SCS repo");
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(mainPane) == JFileChooser.APPROVE_OPTION) {
            repoBaseFile = chooser.getSelectedFile();
            //Check whether it is a scs repo
            String[] fileList = repoBaseFile.list();
            if (fileList.length == 0 | !scsutils.isSCSRepo(repoBaseFile)) {
                //Is not an scs repo.
                JOptionPane.showMessageDialog(mainPane, repoBaseFile.getAbsolutePath() + "is not an scs repo");
                repoBaseFile = null;
            } else {
                //Set properties
                settingsProperties.setProperty("repopath", repoBaseFile.getAbsolutePath());
                //Edit label
                servingRepoLabel.setText("Serving: " + repoBaseFile.getAbsolutePath());
                startServerBttn.setEnabled(true);
                //Get repo data
                {
                    File repoCommitFile = new File(repoBaseFile.getAbsolutePath() + "/db/current");
                    File repoUUIDFile = new File(repoBaseFile.getAbsolutePath() + "/db/UUID");
                    try {
                        if (repoCommitFile.exists()) {
                            Scanner repocireader = new Scanner(repoCommitFile);
                            //Get int
                            repoCommitNumber = repocireader.nextInt();
                        } else {
                            //Then kill the application
                            throw new FileNotFoundException();
                        }

                        //Then get the uuid
                        if (repoUUIDFile.exists()) {
                            Scanner repoUUIDScanner = new Scanner(repoUUIDFile);

                            //Get string
                            repoUUID = repoUUIDScanner.nextLine();

                        } else {
                            throw new FileNotFoundException();
                        }
                        //Check if current.zip in working exists
                        File currentZip = new File(repoBaseFile + "/branches/working/current.zip");
                        if (currentZip.exists()) {
                            currentPushExists = true;
                        } else {
                            currentPushExists = false;
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                        //Cannot find file
                        JOptionPane.showMessageDialog(null, "Unable to open repo", "The format of the repo must not be correct. Please try again.", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    class windowlistener implements WindowListener {

        @Override
        public void windowOpened(WindowEvent e) {
            //Open config file. If it exists, good, check it, if it doesn't, make it.
            File configFile = new File(System.getProperty("user.dir") + "/scsserver/conf.properties");
            if (!configFile.exists()) {
                try {
                    //Create parent dir. It will complain if I don't
                    if (!configFile.getParentFile().exists()) {
                        configFile.getParentFile().mkdirs();
                    }
                    //Create file
                    configFile.createNewFile();
                    //Write default config
                    settingsProperties.setProperty("repopath", "");
                    settingsProperties.store(new FileOutputStream(configFile), "This is for SCS server");
                } catch (IOException ex) {
                    Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    //Read from config file
                    FileInputStream confin = new FileInputStream(configFile);
                    settingsProperties.load(confin);
                    confin.close();

                    //For presetting. Makes things easier
                    repoBaseFile = new File(settingsProperties.getProperty("repopath"));
                    if (!scsutils.isSCSRepo(repoBaseFile)) {
                        //Exit
                        System.err.println("The folder " + repoBaseFile.getAbsolutePath() + " is not a scs repo");
                    } else {
                        //Get uuid and repo commit.
                        //Get repo data
                        {
                            File repoCommitFile = new File(repoBaseFile.getAbsolutePath() + "/db/current");
                            File repoUUIDFile = new File(repoBaseFile.getAbsolutePath() + "/db/UUID");
                            try {
                                if (repoCommitFile.exists()) {
                                    Scanner repocireader = new Scanner(repoCommitFile);
                                    //Get int
                                    repoCommitNumber = repocireader.nextInt();
                                } else {
                                    //Then kill the application
                                    throw new FileNotFoundException();
                                }

                                //Then get the uuid
                                if (repoUUIDFile.exists()) {
                                    Scanner repoUUIDScanner = new Scanner(repoUUIDFile);

                                    //Get string
                                    repoUUID = repoUUIDScanner.nextLine();

                                } else {
                                    throw new FileNotFoundException();
                                }
                                //Check if current.zip in working exists
                                File currentZip = new File(repoBaseFile + "/branches/working/current.zip");
                                if (currentZip.exists()) {
                                    currentPushExists = true;
                                } else {
                                    currentPushExists = false;
                                }
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                                //Cannot find file
                                JOptionPane.showMessageDialog(null, "Unable to open repo", "The format of the repo must not be correct. Please try again.", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                        servingRepoLabel.setText("Serving: " + repoBaseFile.getAbsolutePath());

                        startServerBttn.setEnabled(true);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (!serverRunning) {
                //Kill window
                try {
                    try ( //Store settings
                            FileOutputStream confo = new FileOutputStream(System.getProperty("user.dir") + "/scsserver/conf.properties")) {
                        settingsProperties.store(confo, "NONE!!!");
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.exit(0);
            } else {
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


    private synchronized void haltServer() {
        serverRunning = false;
    }

    /**
     * Main class of the scs server
     *
     * @param args
     */
    public static void main(String[] args) {
        ScsServer server = new ScsServer();
    }
}