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

    private static final int serverport = 19319; //SCS in numbers of the alphabet
    //Logger
    Logging logger = new Logging("/scsserver.log", true, true);

    //For things.
    private JButton kill;
    private JButton info;
    private JButton chooseRepo;
    private JButton startServerBttn;
    private JButton logs;
    private JButton config;
    private JLabel title;
    private JLabel servingRepo;
    private JLabel serverStatus;
    private JPanel pane;

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
        startServerBttn = new JButton("Start Server");
        startServerBttn.setEnabled(false);
        startServerBttn.addActionListener((e) -> {
            startServer();
        });
        pane.add(startServerBttn);

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
     *
     * @param fileCheck file to check
     * @return whether of not it is a scs repo
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
                if (repoFile != null) {
                    filename = repoFile.getAbsolutePath();
                }
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
        serverStatus.setText("Server status: Starting...");
        //Add server start code here
        Runnable server = () -> {
            //Server code here.
            new ScsServerMainframe();
        };
        Thread t = new Thread(server);
        t.start();
        //When done,
        kill.setEnabled(true);
        startServerBttn.setEnabled(false);
    }

    private void chooseRepoDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose SCS repo");
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
                //Set properties
                settings.setProperty("repopath", repoFile.getAbsolutePath());
                //Edit label
                servingRepo.setText("Serving: " + repoFile.getAbsolutePath());
                startServerBttn.setEnabled(true);
                //Get repo data
                {
                    File repoCommitFile = new File(repoFile.getAbsolutePath() + "/db/current");
                    File repoUUIDFile = new File(repoFile.getAbsolutePath() + "/db/UUID");
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
                        File currentZip = new File(repoFile + "/branches/working/current.zip");
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
                    settings.setProperty("repopath", "");
                    settings.store(new FileOutputStream(configFile), "This is for SCS server");
                } catch (IOException ex) {
                    Logger.getLogger(ScsServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    //Read from config file
                    FileInputStream confin = new FileInputStream(configFile);
                    settings.load(confin);
                    confin.close();

                    //For presetting. Makes things easier
                    repoFile = new File(settings.getProperty("repopath"));
                    if (!isSCSRepo(repoFile)) {
                        //Exit
                        System.err.println("The folder " + repoFile.getAbsolutePath() + " is not a scs repo");
                    } else {
                        //Get uuid and repo commit.
                        //Get repo data
                        {
                            File repoCommitFile = new File(repoFile.getAbsolutePath() + "/db/current");
                            File repoUUIDFile = new File(repoFile.getAbsolutePath() + "/db/UUID");
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
                                File currentZip = new File(repoFile + "/branches/working/current.zip");
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
                        servingRepo.setText("Serving: " + repoFile.getAbsolutePath());

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
                        settings.store(confo, "NONE!!!");
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

    /**
     * This is the mainframe of the scs server.
     */
    public class ScsServerMainframe {

        Logging log = new Logging("/scsserver.log", true, true);

        public ScsServerMainframe() {
            try {
                // Create a non-blocking server socket channel
                ServerSocketChannel sock = ServerSocketChannel.open();
                sock.configureBlocking(false);

                // Set the host and port to monitor
                InetSocketAddress server = new InetSocketAddress(
                        "localhost", 19319);
                ServerSocket socket = sock.socket();
                socket.bind(server);

                // Create the selector and register it on the channel
                Selector selector = Selector.open();
                sock.register(selector, SelectionKey.OP_ACCEPT);

                // Loop forever, looking for client connections
                while (true) {
                    // Wait for a connection
                    selector.select();

                    // Get list of selection keys with pending events
                    Set keys = selector.selectedKeys();
                    Iterator it = keys.iterator();

                    // Handle each key
                    while (it.hasNext()) {

                        // Get the key and remove it from the iteration
                        SelectionKey sKey = (SelectionKey) it.next();

                        it.remove();
                        if (sKey.isAcceptable()) {

                            // Create a socket connection with client
                            ServerSocketChannel selChannel
                                    = (ServerSocketChannel) sKey.channel();
                            ServerSocket sSock = selChannel.socket();
                            Socket connection = sSock.accept();

                            OutputStream outputStream = connection.getOutputStream();
                            BufferedOutputStream write = new BufferedOutputStream(outputStream);

                            InputStream inputStream = connection.getInputStream();
                            BufferedInputStream in = new BufferedInputStream(inputStream);

                            //Read the enter code. ("c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866")
                            byte[] verifyCode = new byte[50];
                            in.read(verifyCode);

                            if (verifyCode.equals("c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866".getBytes("UTF-8"))) {
                                //Kill
                                log.log("Denied access to someone, got " + verifyCode);

                                //Close everything
                                write.close();
                                outputStream.close();
                                connection.close();
                                continue;
                            } else {
                                log.log("Accepted someone: Giving revision");

                                write.write(repoCommitNumber);

                                //Send uuid
                                log.log("Sending uuid");
                                write.write((repoUUID).getBytes("UTF-8"));
                                
                                //Send repo name
                                write.write(repoFile.getName().length());
                                write.write(repoFile.getName().getBytes("UTF-8"));
                            }
                            //Close everything
                            write.close();
                            outputStream.close();
                            connection.close();
                        }
                    }
                }
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
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
