/*
 * wxt4runView.java
 */

package wxt4run;

import java.awt.Color;
import java.awt.SplashScreen;
import java.awt.event.WindowEvent;
import java.util.EventObject;
import javax.swing.event.DocumentEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import wxt4utils.JarFileLoader;
import wxt4utils.settings;
import wxt4utils.treeNode;
import org.jdesktop.application.Application;
import org.jdesktop.application.Application.ExitListener;
import org.jdesktop.application.Task;
import org.w3c.dom.Element;
import wxt4utils.resourceHandler;

/**
 * The application's main frame.
 */
public class wxt4runView extends FrameView   implements WindowListener
{
    /** access til custom settings for this app*/
    settings m_Settings=null;
    
    /** the script which has our attention*/
    Script m_currentScript=null;
          
    /** where is wxt */
    String m_wxtPath=null;
    
    /** the established scripthandler object, set in establish structure, used when building*/
    Object m_currentShandler=null;

    /** a resourcehandler to manage versions */
    resourceHandler m_resources=null;

    /** keeping the mainmessage text unformatted*/
    String m_currentMessage="";

    /** where we have the main site*/
    public static final String WXT_ADDRESS="http://www.it.hiof.no/wxt/wxtsite/";
    /** where we have the help*/
    public static final String WXT_HELP_ADDRESS="http://www.it.hiof.no/wxt/wxtsite/wxt/documentation.html";
    /** where we have the  gui help*/
    public static final String WXT_GUI_HELP_ADDRESS="http://www.it.hiof.no/wxt/wxtsite/wxt/gui.html";
    /** where we have the download*/
    public static final String WXT_LAUNCH_ADDRESS="http://www.it.hiof.no/wxt/wxtsite/wxt/download.html";
    //---------------------------
    // when we have established contact with wxts Scripthandler:
    /** The wxt4.Scripthandler class*/
    Class m_Scripthandler=null;
    /** The constructor for wxt.Scripthandler*/
    Constructor m_Constructor=null;
    /** The method for wxte.Scripthandler.BuildModules()*/
    Method m_BuildModules=null;
    /** The method for updating modules with changed content*/
    Method m_UpdateModules=null;
    /** The method for wxt.Scripthandler.GetReport()*/
    Method m_GetReport=null;
    /** The method for wxt.Scripthandler.getModuleDescription()*/
    Method m_getModuleDescription=null;
    /** The method for wxt.Scripthandler.getModuleLocations()*/
    Method m_getModuleLocations=null;
    /** The method for wxt.Scripthandler.m_getModuleAddress()*/
    Method m_getModuleAddress=null;
    /** The method for accessing the current wxt version */
    Method m_getCurWxtVersion=null;
    //----------------------------
    
    /** The selected look and feel menuitem */
    JCheckBoxMenuItem m_currentSelectedLookAndFeel=null;
    
    public wxt4runView(SingleFrameApplication app) {
        super(app);
        SplashScreen SS=SplashScreen.getSplashScreen();
        if(SS!=null)
        {
         try{
             Thread.sleep(1000);
         }
            catch(InterruptedException e) {
            }
        }

        initComponents();


        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        
        // do my initial settings
        postInit();
    }


    @Action
    public void showhelp() {
        wxt4runAboutBox box=new wxt4runAboutBox(null);
        box.launchelp();
    }
    
    @Action
    public void showguihelp() {
        wxt4runAboutBox box=new wxt4runAboutBox(null);
        box.launchguihelp();
    }
    
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = wxt4runApp.getApplication().getMainFrame();
            aboutBox = new wxt4runAboutBox(mainFrame);
        }
        aboutBox.setLocationRelativeTo(wxt4runApp.getApplication().getMainFrame());
        wxt4runApp.getApplication().show(aboutBox);
    }

    @Action
    public void showVersions(){
        JFrame mainFrame = wxt4runApp.getApplication().getMainFrame();
        String wxtrunversion=m_resources.getVersion();
        String wxtversion="";
        if(m_wxtPath==null)
            wxtversion="<not available>";
        else
        {
            try{
                wxtversion=(String)m_getCurWxtVersion.invoke(m_currentShandler, (Object[])null);
            }
            catch(Exception ex)
            {
                wxtversion=null;
            }
        }
        String latestWRVersion=m_resources.getLatestDistVersion();
        String latestWVersion=m_resources.getLatestWxtDistVersion();

        JDialog versionBox = new versionDialog(mainFrame,true,
                                               wxtrunversion,wxtversion,
                                               latestWRVersion,latestWVersion);
        versionBox.setLocationRelativeTo(mainFrame);

        wxt4runApp.getApplication().show(versionBox);

    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jButtonFindWxt = new javax.swing.JButton();
        jSplitPane3 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jCheckBoxIndentOutput = new javax.swing.JCheckBox();
        jCheckBoxExpandAll = new javax.swing.JCheckBox();
        jComboBoxOutputFormat = new javax.swing.JComboBox();
        jLabelOutputFormat = new javax.swing.JLabel();
        jLabelDefaultEncoding = new javax.swing.JLabel();
        jComboBoxdefaultEncoding = new javax.swing.JComboBox();
        jLabelReferenceIndexing = new javax.swing.JLabel();
        jComboBoxReferenceIndexing = new javax.swing.JComboBox();
        jCheckBoxUseBackup = new javax.swing.JCheckBox();
        jCheckBoxAvoid = new javax.swing.JCheckBox();
        jTextFieldAvoid = new javax.swing.JTextField();
        jCheckBoxPreCode = new javax.swing.JCheckBox();
        jCheckBoxVerbose = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemFindWxt = new javax.swing.JMenuItem();
        jMenuItemFindScript = new javax.swing.JMenuItem();
        jMenuItemClearScripts = new javax.swing.JMenuItem();
        jMenuView = new javax.swing.JMenu();
        jMenuUtils = new javax.swing.JMenu();
        jMenuItemModuleList = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        helpmenuItem = new javax.swing.JMenuItem();
        helpguimenuItem = new javax.swing.JMenuItem();
        versionMenuItm = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(wxt4run.wxt4runApp.class).getContext().getResourceMap(wxt4runView.class);
        jTextPane1.setContentType(resourceMap.getString("jTextPane1.contentType")); // NOI18N
        jTextPane1.setFont(resourceMap.getFont("jTextPane1.font")); // NOI18N
        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane3.setViewportView(jTextPane1);

        jSplitPane1.setRightComponent(jScrollPane3);

        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setToolTipText(resourceMap.getString("jList1.toolTipText")); // NOI18N
        jList1.setComponentPopupMenu(jPopupMenu1);
        jList1.setName("jList1"); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jButtonFindWxt.setForeground(resourceMap.getColor("jButtonFindWxt.foreground")); // NOI18N
        jButtonFindWxt.setText(resourceMap.getString("jButtonFindWxt.text")); // NOI18N
        jButtonFindWxt.setName("jButtonFindWxt"); // NOI18N
        jButtonFindWxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFindWxtActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonFindWxt, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonFindWxt)
                .addGap(18, 18, 18))
        );

        jSplitPane2.setLeftComponent(jPanel1);

        jSplitPane3.setName("jSplitPane3"); // NOI18N

        jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 200));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTree1.setToolTipText(resourceMap.getString("jTree1.toolTipText")); // NOI18N
        jTree1.setMinimumSize(new java.awt.Dimension(200, 200));
        jTree1.setName("jTree1"); // NOI18N
        jScrollPane2.setViewportView(jTree1);

        jSplitPane3.setLeftComponent(jScrollPane2);

        jPanel2.setMaximumSize(new java.awt.Dimension(250, 300));
        jPanel2.setMinimumSize(new java.awt.Dimension(200, 300));
        jPanel2.setName("jPanel2"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(wxt4run.wxt4runApp.class).getContext().getActionMap(wxt4runView.class, this);
        jButton1.setAction(actionMap.get("startBuilding")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("startUpdating")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jPanel3.setBackground(resourceMap.getColor("jPanel3.background")); // NOI18N
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jCheckBoxIndentOutput.setBackground(resourceMap.getColor("jCheckBoxIndentOutput.background")); // NOI18N
        jCheckBoxIndentOutput.setText(resourceMap.getString("jCheckBoxIndentOutput.text")); // NOI18N
        jCheckBoxIndentOutput.setName("jCheckBoxIndentOutput"); // NOI18N
        jCheckBoxIndentOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxIndentOutputActionPerformed(evt);
            }
        });

        jCheckBoxExpandAll.setBackground(resourceMap.getColor("jCheckBoxExpandAll.background")); // NOI18N
        jCheckBoxExpandAll.setText(resourceMap.getString("jCheckBoxExpandAll.text")); // NOI18N
        jCheckBoxExpandAll.setName("jCheckBoxExpandAll"); // NOI18N
        jCheckBoxExpandAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxExpandAllActionPerformed(evt);
            }
        });

        jComboBoxOutputFormat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "html", "xml", "text", "xhtml" }));
        jComboBoxOutputFormat.setName("jComboBoxOutputFormat"); // NOI18N
        jComboBoxOutputFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxOutputFormatActionPerformed(evt);
            }
        });

        jLabelOutputFormat.setLabelFor(jComboBoxOutputFormat);
        jLabelOutputFormat.setText(resourceMap.getString("jLabelOutputFormat.text")); // NOI18N
        jLabelOutputFormat.setName("jLabelOutputFormat"); // NOI18N

        jLabelDefaultEncoding.setLabelFor(jComboBoxOutputFormat);
        jLabelDefaultEncoding.setText(resourceMap.getString("jLabelDefaultEncoding.text")); // NOI18N
        jLabelDefaultEncoding.setName("jLabelDefaultEncoding"); // NOI18N

        jComboBoxdefaultEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UTF-8", "ISO-8859-1" }));
        jComboBoxdefaultEncoding.setName("jComboBoxDefaultEncoding"); // NOI18N
        jComboBoxdefaultEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxdefaultEncodingActionPerformed(evt);
            }
        });

        jLabelReferenceIndexing.setLabelFor(jComboBoxReferenceIndexing);
        jLabelReferenceIndexing.setText(resourceMap.getString("jLabelReferenceIndexing.text")); // NOI18N
        jLabelReferenceIndexing.setName("jLabelReferenceIndexing"); // NOI18N

        jComboBoxReferenceIndexing.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "local", "global" }));
        jComboBoxReferenceIndexing.setName("jComboBoxReferenceIndexing"); // NOI18N
        jComboBoxReferenceIndexing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxReferenceIndexingActionPerformed(evt);
            }
        });

        jCheckBoxUseBackup.setBackground(resourceMap.getColor("jCheckBoxUseBackup.background")); // NOI18N
        jCheckBoxUseBackup.setText(resourceMap.getString("jCheckBoxUseBackup.text")); // NOI18N
        jCheckBoxUseBackup.setName("jCheckBoxUseBackup"); // NOI18N
        jCheckBoxUseBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseBackupActionPerformed(evt);
            }
        });

        jCheckBoxAvoid.setBackground(resourceMap.getColor("jCheckBoxAvoid.background")); // NOI18N
        jCheckBoxAvoid.setText(resourceMap.getString("jCheckBoxAvoid.text")); // NOI18N
        jCheckBoxAvoid.setActionCommand(resourceMap.getString("jCheckBoxAvoid.actionCommand")); // NOI18N
        jCheckBoxAvoid.setName("jCheckBoxAvoid"); // NOI18N
        jCheckBoxAvoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAvoidActionPerformed(evt);
            }
        });

        jTextFieldAvoid.setText(resourceMap.getString("jTextFieldAvoid.text")); // NOI18N
        jTextFieldAvoid.setName("jTextFieldAvoid"); // NOI18N

        jCheckBoxPreCode.setBackground(resourceMap.getColor("jCheckBoxPreCode.background")); // NOI18N
        jCheckBoxPreCode.setText(resourceMap.getString("jCheckBoxPreCode.text")); // NOI18N
        jCheckBoxPreCode.setName("jCheckBoxPreCode"); // NOI18N
        jCheckBoxPreCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxPreCodeActionPerformed(evt);
            }
        });

        jCheckBoxVerbose.setBackground(resourceMap.getColor("jCheckBoxVerbose.background")); // NOI18N
        jCheckBoxVerbose.setText(resourceMap.getString("jCheckBoxVerbose.text")); // NOI18N
        jCheckBoxVerbose.setName("jCheckBoxVerbose"); // NOI18N
        jCheckBoxVerbose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxVerboseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabelReferenceIndexing, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                                .addGap(10, 10, 10)
                                .addComponent(jComboBoxReferenceIndexing, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabelDefaultEncoding, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBoxdefaultEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabelOutputFormat, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                                .addGap(36, 36, 36)
                                .addComponent(jComboBoxOutputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(4, 4, 4))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBoxAvoid)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldAvoid, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBoxVerbose, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxIndentOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                            .addComponent(jCheckBoxExpandAll, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                            .addComponent(jCheckBoxPreCode, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxUseBackup, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                        .addGap(31, 31, 31))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jCheckBoxIndentOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxExpandAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxUseBackup)
                .addGap(3, 3, 3)
                .addComponent(jCheckBoxPreCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxVerbose)
                .addGap(8, 8, 8)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxAvoid, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldAvoid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxOutputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelOutputFormat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDefaultEncoding)
                    .addComponent(jComboBoxdefaultEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxReferenceIndexing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelReferenceIndexing))
                .addContainerGap())
        );

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(110, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel3.AccessibleContext.accessibleName")); // NOI18N

        jSplitPane3.setRightComponent(jPanel2);

        jSplitPane2.setRightComponent(jSplitPane3);

        jSplitPane1.setLeftComponent(jSplitPane2);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItemFindWxt.setText(resourceMap.getString("jMenuItemFindWxt.text")); // NOI18N
        jMenuItemFindWxt.setName("jMenuItemFindWxt"); // NOI18N
        jMenuItemFindWxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFindWxtActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemFindWxt);

        jMenuItemFindScript.setText(resourceMap.getString("jMenuItemFindScript.text")); // NOI18N
        jMenuItemFindScript.setName("jMenuItemFindScript"); // NOI18N
        jMenuItemFindScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFindScriptActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemFindScript);

        jMenuItemClearScripts.setText(resourceMap.getString("jMenuItemClearScripts.text")); // NOI18N
        jMenuItemClearScripts.setName("jMenuItemClearScripts"); // NOI18N
        jMenuItemClearScripts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearScriptsActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemClearScripts);

        menuBar.add(jMenu1);

        jMenuView.setText(resourceMap.getString("jMenuView.text")); // NOI18N
        jMenuView.setName("jMenuView"); // NOI18N
        menuBar.add(jMenuView);

        jMenuUtils.setText(resourceMap.getString("jMenuUtils.text")); // NOI18N
        jMenuUtils.setName("jMenuUtils"); // NOI18N

        jMenuItemModuleList.setText(resourceMap.getString("jMenuItemModuleList.text")); // NOI18N
        jMenuItemModuleList.setName("jMenuItemModuleList"); // NOI18N
        jMenuItemModuleList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemModuleListActionPerformed(evt);
            }
        });
        jMenuUtils.add(jMenuItemModuleList);

        menuBar.add(jMenuUtils);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        helpmenuItem.setAction(actionMap.get("showhelp")); // NOI18N
        helpmenuItem.setText(resourceMap.getString("helpmenuItem.text")); // NOI18N
        helpmenuItem.setActionCommand(resourceMap.getString("helpmenuItem.actionCommand")); // NOI18N
        helpmenuItem.setName("helpmenuItem"); // NOI18N
        helpMenu.add(helpmenuItem);

        helpguimenuItem.setAction(actionMap.get("showguihelp")); // NOI18N
        helpguimenuItem.setText(resourceMap.getString("helpguimenuItem.text")); // NOI18N
        helpguimenuItem.setName("helpguimenuItem"); // NOI18N
        helpMenu.add(helpguimenuItem);

        versionMenuItm.setAction(actionMap.get("showVersions")); // NOI18N
        versionMenuItm.setText(resourceMap.getString("versionMenuItm.text")); // NOI18N
        versionMenuItm.setName("versionMenuItm"); // NOI18N
        helpMenu.add(versionMenuItm);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 522, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jPopupMenu1.setInvoker(jList1);
        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem1);

        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem2);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    // popupmenu: add script
    // Add a new script to the scriptlist
    // set up a dialog and find a script
    JFileChooser foc = new JFileChooser();
    foc.setDialogTitle("WXT - Scriptfile");
    FileNameExtensionFilter xmlFilter=new FileNameExtensionFilter("xml-files","xml");
    foc.setFileFilter(xmlFilter);
    
    String startCat=m_Settings.getEntry(settings.SCRIPTPATH);
    if(m_currentScript!=null)
        startCat=m_currentScript.getPath();

    if(startCat!=null)
        foc.setCurrentDirectory(new File(startCat).getParentFile());
    
    int result=foc.showOpenDialog(getComponent());
    String fileName=null;
    if (result==JFileChooser.APPROVE_OPTION)
    {
        fileName=foc.getSelectedFile().toString();
        for(int ix=0;ix<jList1.getModel().getSize();ix++)
        {
            String tmp=(String)jList1.getModel().getElementAt(ix);
            if(fileName.compareTo(tmp)==0)
            {
                JOptionPane.showMessageDialog(mainPanel, fileName+" is already loaded");
                return;
            }
        }
         
        DefaultListModel lm=(DefaultListModel) jList1.getModel();
        lm.addElement(fileName);

        m_currentScript=null;               
        jList1.setSelectedIndex(lm.indexOf(fileName));
        jList1.invalidate();
        m_Settings.setScript(fileName);
     }
}//GEN-LAST:event_jMenuItem1ActionPerformed

private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
    // popupmenu: remove script
    // Remove the selected script from the scriptlist
    if(m_currentScript!=null)
    {
        m_Settings.removeScript(m_currentScript.getPath());
        DefaultListModel lm=(DefaultListModel) jList1.getModel();
        lm.removeElement(m_currentScript.getPath());
        m_currentScript=null;
        jTree1.setVisible(false);
        setMainMessage("-");
    }
    else if(jList1.getSelectedValue()!=null)
    {
        // remove a non-script entry
        String tmp=jList1.getSelectedValue().toString();
        m_Settings.removeScript(tmp);
        DefaultListModel lm=(DefaultListModel) jList1.getModel();
        lm.removeElementAt(jList1.getSelectedIndex());
        m_currentScript=null;
        jTree1.setVisible(false);
        setMainMessage("-");
    }
}//GEN-LAST:event_jMenuItem2ActionPerformed

private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
    // have a new selection in scriptlist
    // attempt to establish the  script and display it
    if(jList1.getSelectedValue()!=null)
    {
            setMainMessage("parsing script ...");
            try{
                String tmp=jList1.getSelectedValue().toString();
                m_currentScript=new Script(tmp, m_wxtPath);
            }
            catch(Exception x)
            {
                addToMainMessage("Could not establish "+jList1.getSelectedValue()+" : "+x.getMessage());
                //jList1.setSelectedIndex(-1);
                jTree1.setVisible(false);
                m_currentScript=null;
                return;
            }
        
            
        m_Settings.setEntry(settings.SCRIPTPATH, m_currentScript.getPath());
        // use it
        if(m_currentScript.getDoc()!=null)
        {
            // establish action
            this.getFrame().setTitle("Wxtrunner: "+m_currentScript.getPath());
            jTree1.setVisible(true);
            if(!setUpStructure())
            {
                jTree1.setVisible(false);
            }
        }
    }
    else
    {
        this.getFrame().setTitle("Wxtrunner");
        m_currentScript=null;
    }

}//GEN-LAST:event_jList1ValueChanged

private void jCheckBoxIndentOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxIndentOutputActionPerformed
// TODO add your handling code here:
    jCheckBoxIndentOutput.setForeground(Color.BLACK);
    if(m_currentScript==null)
        return;
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    boolean userset=jCheckBoxIndentOutput.isSelected();
    if(scriptOptions.containsKey(Script.INDENT_OUTPUT))
    {
        if((scriptOptions.get(Script.INDENT_OUTPUT).compareTo(Script.YES)==0)
                &&(!userset))
            jCheckBoxIndentOutput.setForeground(Color.RED);
        else if ((scriptOptions.get(Script.INDENT_OUTPUT).compareTo(Script.NO)==0)
                &&(userset))
            jCheckBoxIndentOutput.setForeground(Color.RED);

    }
}//GEN-LAST:event_jCheckBoxIndentOutputActionPerformed

private void jCheckBoxExpandAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExpandAllActionPerformed
// TODO add your handling code here:
    jCheckBoxExpandAll.setForeground(Color.BLACK);
    if(m_currentScript==null)
        return;
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    boolean userset=jCheckBoxExpandAll.isSelected();
    if(scriptOptions.containsKey(Script.EXPAND_ALL))
    {
        if((scriptOptions.get(Script.EXPAND_ALL).compareTo(Script.YES)==0)
                &&(!userset))
            jCheckBoxExpandAll.setForeground(Color.RED);
        else if ((scriptOptions.get(Script.EXPAND_ALL).compareTo(Script.NO)==0)
                &&(userset))
            jCheckBoxExpandAll.setForeground(Color.RED);

    }

}//GEN-LAST:event_jCheckBoxExpandAllActionPerformed

private void jComboBoxOutputFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOutputFormatActionPerformed
//TODO add your handling code here:
   String userset=(String)jComboBoxOutputFormat.getSelectedItem();
   jLabelOutputFormat.setForeground(Color.BLACK);
   if(m_currentScript==null)
        return;
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    if(scriptOptions.containsKey(Script.OUTPUT_FORMAT))
    {
        String val=scriptOptions.get(Script.OUTPUT_FORMAT);
        if(userset.compareTo(val)!=0)
            jLabelOutputFormat.setForeground(Color.RED);
        
    }
    
}//GEN-LAST:event_jComboBoxOutputFormatActionPerformed

private void jComboBoxdefaultEncodingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxdefaultEncodingActionPerformed
// TODO add your handling code here:
   String userset=(String)jComboBoxdefaultEncoding.getSelectedItem();
   jLabelDefaultEncoding.setForeground(Color.BLACK);
   if(m_currentScript==null)
        return;
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    if(scriptOptions.containsKey(Script.DEFAULT_ENCODING))
    {
        String val=scriptOptions.get(Script.DEFAULT_ENCODING);
        if(userset.compareTo(val)!=0)
            jLabelDefaultEncoding.setForeground(Color.RED);
        
    }
    
}//GEN-LAST:event_jComboBoxdefaultEncodingActionPerformed

private void jMenuItemFindWxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFindWxtActionPerformed
    // try to identify the wxt catalog
    
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("WXT4 - path");
    fc.setAcceptAllFileFilterUsed(true);
    FileNameExtensionFilter jarFilter=new FileNameExtensionFilter("jarfiles","jar");
    fc.setFileFilter(jarFilter);
    
    int result=fc.showOpenDialog(getComponent());
    if (result==JFileChooser.APPROVE_OPTION)
    {
        m_wxtPath=fc.getSelectedFile().toString();        
        // set up wxt contact
        if(!setupWxt4Contract(m_wxtPath))
        {
            setMainMessage("Could not contact wxt at: "+ m_wxtPath);
            m_wxtPath=null;

            jLabel6.setForeground(Color.RED);
            jLabel6.setText("-- wxt not connected --");
        }
        else
        {
           jLabel6.setForeground(Color.BLACK);
           jLabel6.setText(m_wxtPath);
           m_Settings.setEntry(settings.WXTPATH, m_wxtPath);
        }
    }
}//GEN-LAST:event_jMenuItemFindWxtActionPerformed

private void jMenuItemFindScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFindScriptActionPerformed
    jMenuItem1ActionPerformed(evt);
}//GEN-LAST:event_jMenuItemFindScriptActionPerformed

private void jButtonFindWxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFindWxtActionPerformed
    jMenuItemFindWxtActionPerformed(evt);
}//GEN-LAST:event_jButtonFindWxtActionPerformed

private void jComboBoxReferenceIndexingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxReferenceIndexingActionPerformed
// TODO add your handling code here:
   String userset=(String)jComboBoxReferenceIndexing.getSelectedItem();
   jLabelReferenceIndexing.setForeground(Color.BLACK);
   if(m_currentScript==null)
        return;
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    if(scriptOptions.containsKey(Script.REFERENCE_INDEXING))
    {
        String val=scriptOptions.get(Script.REFERENCE_INDEXING);
        if(userset.compareTo(val)!=0)
            jLabelReferenceIndexing.setForeground(Color.RED);        
    }

}//GEN-LAST:event_jComboBoxReferenceIndexingActionPerformed

private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
    // clicked when one is selected
    jList1ValueChanged(null);
}//GEN-LAST:event_jList1MouseClicked

private void jMenuItemModuleListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemModuleListActionPerformed
    // user want to get a list of modules: id \t absoluteuri
    // from a dialog box
    ModuleLocationView mv=new ModuleLocationView(this.getFrame(),true);
    String s;
    try{
        s=(String)m_getModuleLocations.invoke(m_currentShandler, (Object[])null);
        mv.setData(s);
    }
    catch(Exception ex)
    {
        s="failed to access module locations";
    }
    mv.setVisible(true);
}//GEN-LAST:event_jMenuItemModuleListActionPerformed

private void jCheckBoxUseBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseBackupActionPerformed
    // TODO add your handling code here:
    jCheckBoxUseBackup.setForeground(Color.BLACK);
    if(m_currentScript==null)
        return;
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    boolean userset=jCheckBoxUseBackup.isSelected();
    if(scriptOptions.containsKey(Script.USE_COPY))
    {
        if((scriptOptions.get(Script.USE_COPY).compareTo(Script.YES)==0)
                &&(!userset))
            jCheckBoxUseBackup.setForeground(Color.RED);
        else if ((scriptOptions.get(Script.USE_COPY).compareTo(Script.NO)==0)
                &&(userset))
            jCheckBoxUseBackup.setForeground(Color.RED);

    }


}//GEN-LAST:event_jCheckBoxUseBackupActionPerformed

private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
    // TODO add your handling code here:
    optionHelp op=new optionHelp(this.getFrame(), true);
    op.setVisible(true);
}//GEN-LAST:event_jButton3MouseClicked

private void jCheckBoxAvoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAvoidActionPerformed
    // TODO add your handling code here:
    jCheckBoxAvoid.setForeground(Color.BLACK);
    if(m_currentScript==null)
        return;   
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    boolean userset=jCheckBoxAvoid.isSelected();
    String uservalue=jTextFieldAvoid.getText();
    if(scriptOptions.containsKey(Script.DROP_BOOKS))
    {
        String scriptvalue=scriptOptions.get(Script.DROP_BOOKS);
        if((!userset)||(uservalue.compareTo(scriptvalue)!=0))
            jCheckBoxAvoid.setForeground(Color.RED);
    }
    else if(userset)
            jCheckBoxAvoid.setForeground(Color.RED);

}//GEN-LAST:event_jCheckBoxAvoidActionPerformed

private void jMenuItemClearScriptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearScriptsActionPerformed
    // TODO add your handling code here:
    // clear the scriptlist
    System.out.println("removing");
    DefaultListModel lm=(DefaultListModel) jList1.getModel();
    lm.removeAllElements();
    m_Settings.removeAllScripts();
    jList1.invalidate();
    m_currentScript=null;
    jTree1.setVisible(false);
    setMainMessage("");
}//GEN-LAST:event_jMenuItemClearScriptsActionPerformed

private void jCheckBoxPreCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxPreCodeActionPerformed
    jCheckBoxPreCode.setForeground(Color.BLACK);
    if(m_currentScript==null)
        return;
    HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
    boolean userset=jCheckBoxPreCode.isSelected();
    if(scriptOptions.containsKey(Script.PREFORMAT_LANGUAGE))
    {
        if((scriptOptions.get(Script.PREFORMAT_LANGUAGE).compareTo(Script.YES)==0)
                &&(!userset))
            jCheckBoxPreCode.setForeground(Color.RED);
        else if ((scriptOptions.get(Script.PREFORMAT_LANGUAGE).compareTo(Script.NO)==0)
                &&(userset))
            jCheckBoxPreCode.setForeground(Color.RED);
    }
}//GEN-LAST:event_jCheckBoxPreCodeActionPerformed

    private void jCheckBoxVerboseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxVerboseActionPerformed
        // TODO add your handling code here:
        jCheckBoxVerbose.setForeground(Color.BLACK);
        if(m_currentScript==null)
            return;
        HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
        boolean userset=jCheckBoxVerbose.isSelected();
        if(scriptOptions.containsKey(Script.VERBOSE))
        {
            if((scriptOptions.get(Script.VERBOSE).compareTo(Script.YES)==0)
                    &&(!userset))
                jCheckBoxVerbose.setForeground(Color.RED);
            else if ((scriptOptions.get(Script.VERBOSE).compareTo(Script.NO)==0)
                    &&(userset))
                jCheckBoxVerbose.setForeground(Color.RED);

        }
    }//GEN-LAST:event_jCheckBoxVerboseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem helpguimenuItem;
    private javax.swing.JMenuItem helpmenuItem;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonFindWxt;
    private javax.swing.JCheckBox jCheckBoxAvoid;
    private javax.swing.JCheckBox jCheckBoxExpandAll;
    private javax.swing.JCheckBox jCheckBoxIndentOutput;
    private javax.swing.JCheckBox jCheckBoxPreCode;
    private javax.swing.JCheckBox jCheckBoxUseBackup;
    private javax.swing.JCheckBox jCheckBoxVerbose;
    private javax.swing.JComboBox jComboBoxOutputFormat;
    private javax.swing.JComboBox jComboBoxReferenceIndexing;
    private javax.swing.JComboBox jComboBoxdefaultEncoding;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelDefaultEncoding;
    private javax.swing.JLabel jLabelOutputFormat;
    private javax.swing.JLabel jLabelReferenceIndexing;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItemClearScripts;
    private javax.swing.JMenuItem jMenuItemFindScript;
    private javax.swing.JMenuItem jMenuItemFindWxt;
    private javax.swing.JMenuItem jMenuItemModuleList;
    private javax.swing.JMenu jMenuUtils;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTextField jTextFieldAvoid;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem versionMenuItm;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    
    
    /**
     * Set up initial settings
     */
    protected final void postInit()
    {
        // do all handmade things
        
        //------------------------
        // set up a resourcehandler for version access
        m_resources=new resourceHandler();
        
        //------------------------
        // set header
        String version=m_resources.getVersion();
        this.getFrame().setTitle("Wxt Runner - version "+version);
        
        //---------------------
        // hide module tree
        jTree1.setVisible(false);

        //-------------------------
        // get hold of settings, default filepath
        m_Settings=new settings();
        m_Settings.load();
             
        //--------------------------------------
        // listen to the window for focus
        this.getFrame().addWindowListener(this);
    
        //--------------------------------------
        // contact wxt
        jButtonFindWxt.setVisible(false);
        m_wxtPath=m_Settings.getEntry(settings.WXTPATH);
        
        // default 
        if(m_wxtPath==null)
            m_wxtPath="c:\\wxtproject\\wxt4\\dist\\wxt4.jar";

        // set up wxt contact
        if(!setupWxt4Contract(m_wxtPath))
        {
            jButtonFindWxt.setVisible(true);
            m_wxtPath=null;
            jLabel6.setForeground(Color.RED);
            jLabel6.setText("-- wxt4 not connected --");
        }
        else
        {
            jLabel6.setForeground(Color.BLACK);
            jLabel6.setText(m_wxtPath);
        }
 
        //-------------------------------
        // set up exitlistener
        ExitListener exlist=new ExitListener() {
            @Override
                public boolean canExit(EventObject arg0) {
                    return wrapItUp();
                }

            @Override
                public void willExit(EventObject arg0) {

                }

         };
        Application app=this.getApplication();
        app.addExitListener(exlist);


        //-----------------------------------
        // listening to change in textfield Drop books so we can uppdate red marking
        jTextFieldAvoid.getDocument().addDocumentListener(
                new DocumentListener()
                {
            @Override
                public void changedUpdate(DocumentEvent e)
                {
                jCheckBoxAvoidActionPerformed(null);
                }
            @Override
                public void removeUpdate(DocumentEvent e)
                {
                jCheckBoxAvoidActionPerformed(null);
                }
            @Override
                public void insertUpdate(DocumentEvent e)
                {
                jCheckBoxAvoidActionPerformed(null);
                }

                }
        );
        //---------------------------------
        // listening to the jTree1, the module tree
         MouseListener ml = new MouseAdapter() {
            @Override
             public void mousePressed(MouseEvent e) {
                 int selRow = jTree1.getRowForLocation(e.getX(), e.getY());
                 TreePath selPath = jTree1.getPathForLocation(e.getX(), e.getY());
                 // hit and rightbutton, what about mac ?                 
                 if((selRow != -1) && (e.getButton()==MouseEvent.BUTTON3) && (e.getClickCount() == 1)){
                     pressedSingleOnModuleTree(selRow, selPath);
                 }
             }
         };
         jTree1.addMouseListener(ml);
        
        //----------------------------
        // fill up the encoding combobox
        java.util.SortedMap cm=java.nio.charset.Charset.availableCharsets();
        Object[] vals=cm.values().toArray();
        for(int ix=0;ix< vals.length;ix++)
            jComboBoxdefaultEncoding.addItem(vals[ix].toString());
                
        //----------------------------
        // set up look and feel menues
        LookAndFeelInfo[] lfi=UIManager.getInstalledLookAndFeels();
        String classname=m_Settings.getEntry(settings.LOOK_AND_FEEL);
        if(classname==null)
            classname=UIManager.getSystemLookAndFeelClassName();
        for(int ix=0;ix<lfi.length;ix++)
        {
            String s=lfi[ix].getName();
            JCheckBoxMenuItem mit=new JCheckBoxMenuItem(s);
 
            mit.addActionListener(new java.awt.event.ActionListener() {
                @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLookAndFeelAddActionPerformed(evt);
            }
            });
            jMenuView.add(mit);
            if (lfi[ix].getClassName().compareTo(classname)==0)
            {
                m_currentSelectedLookAndFeel=mit;
                mit.setSelected(true);
            }
        }
        // set look and feel according to settings    
        try{
            javax.swing.UIManager.setLookAndFeel(classname);
            javax.swing.SwingUtilities.updateComponentTreeUI(this.mainPanel.getParent());
        }
        catch(Exception ex)
        {
         // do nothing, leave as is
        }

        //----------------------------------
        // load scripts
        makeScriptListFromSettings();   
    }
    
    /**
     * Change look and feel
     * @param evt
     */
    private void jMenuLookAndFeelAddActionPerformed(java.awt.event.ActionEvent evt)
    {
        String lfClassName= "javax.swing.plaf.metal.MetalLookAndFeel";
        JCheckBoxMenuItem m=(JCheckBoxMenuItem)evt.getSource();
        String lfName=m.getText();
        javax.swing.UIManager.LookAndFeelInfo all[]=javax.swing.UIManager.getInstalledLookAndFeels();
         for(int ix=0;ix< all.length; ix++){
             if(all[ix].getName().equals(lfName)){
                 lfClassName=all[ix].getClassName();
             }
        }      
        try{
            if(m_currentSelectedLookAndFeel!=null)
                m_currentSelectedLookAndFeel.setSelected(false);
            javax.swing.UIManager.setLookAndFeel(lfClassName);
            m_Settings.setEntry(settings.LOOK_AND_FEEL, lfClassName);
            javax.swing.SwingUtilities.updateComponentTreeUI(this.mainPanel.getParent());
            m.setSelected(true);
            m_currentSelectedLookAndFeel=m;
        }
        catch(Exception ex)
        {
            
        }   
    }
    
    
    /**
     * When rightclicking in moduletree
     * @param selRow Selected row
     * @param selPath Selected path in tree to selected treenode
     */
    protected void pressedSingleOnModuleTree(int selRow, TreePath selPath)
    {        
        Element elt=((treeNode)selPath.getLastPathComponent()).getElement();
        if(elt==null)
        {
            // reload script
            
            //jList1ValueChanged(null);
            return;
        }
        String id="<none>";
        if(elt.hasAttribute("id"))
            id=elt.getAttribute("id");
        else if(elt.hasAttribute("name"))
            id=elt.getAttribute("name");
       
        String mdesc="nothing";
        String madd="";
        try{
            mdesc=(String)m_getModuleDescription.invoke(m_currentShandler,id);
            madd=(String)m_getModuleAddress.invoke(m_currentShandler,id);

            ModuleView mv=new ModuleView(this.getFrame(),true,mdesc,madd);
            mv.setVisible(true);
        }
        catch(Exception e)
        {
            setMainMessage("Failing to get moduledescription "+e.getMessage());
        }
    }
        
    /**
     * Window is loosing focus, remember when 
     */
    protected void focusLost()
    {
        // set status on current script
        if(m_currentScript!=null)
            m_currentScript.setLastTimeSeen();
    }
    
    /**
     * Window is gaining focus, we must check for changes in current script
     */
    protected void focusGained()
    {
        this.getRootPane().repaint();
        // we have got focus on the application
        // any script selected ?
        if(m_currentScript==null)
            return;
        
        // should we do anything with the current script ?
        // is it removed ?
        File scriptfile=new File(m_currentScript.getPath());
        if(!scriptfile.exists())
        {
            setMainMessage("cannot find :"+m_currentScript.getPath()+" removing it");
            DefaultListModel lm=(DefaultListModel) jList1.getModel();
            lm.removeElement(m_currentScript.getPath());
            m_Settings.removeScript(m_currentScript.getPath());
            jList1.setModel(lm);
            jList1.setSelectedIndex(-1);
            jTree1.setVisible(false);
            return;
        }

        // Has it been changed ? if so we recreate it
        long t=scriptfile.lastModified();
        if(t > m_currentScript.getLastTimeSeen())
        {
           String path=m_currentScript.getPath();
           try{
                m_currentScript=new Script(path,m_wxtPath);
                setUpStructure();
            }
            catch(Exception x)
            {
                setMainMessage("Could not establish "+path+" : "+x.getMessage());
                jList1.setSelectedIndex(-1);
                jTree1.setModel(Script.getEmptyTree());
                return;
            }
         }
    }
    
    @Override
    public void windowOpened(WindowEvent e)      {}
    @Override
    public void windowClosing(WindowEvent e)     {}
    @Override
    public void windowClosed(WindowEvent e)      {}
    @Override
    public void windowIconified(WindowEvent e)   {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e)   {focusGained();}
    @Override
    public void windowDeactivated(WindowEvent e) { focusLost();}
     
    
    /**
     * Contact wxt and setup all contracts via Scripthandler:
     * @param wxt4path
     * @return true if success, false otherwise
     */
    protected boolean setupWxt4Contract(String wxt4path)
    {
        if(wxt4path==null)
            return false;
        try{
            URL urls[]={};
            JarFileLoader jfl=new JarFileLoader(urls);
            String oadr=wxt4path;
            jfl.addFile(oadr);
            m_Scripthandler=jfl.loadClass("wxt4.Scripthandler");
            m_Constructor=m_Scripthandler.getDeclaredConstructor(HashMap.class);
            m_Constructor.setAccessible(true);       
            m_BuildModules=m_Scripthandler.getMethod("buildModules", HashMap.class);
            m_UpdateModules=m_Scripthandler.getMethod("updateModules", HashMap.class);
            m_GetReport=m_Scripthandler.getMethod("getReport", (Class[])null);
            m_getModuleDescription=m_Scripthandler.getMethod("getModuleDescription", String.class);
            m_getModuleLocations=m_Scripthandler.getMethod("getModuleLocations", (Class[])null);
            m_getModuleAddress=m_Scripthandler.getMethod("getModuleAddress", String.class);
            m_getCurWxtVersion=m_Scripthandler.getMethod("getVersion", (Class[])null);
            setMainMessage("Wxt connected");
            m_Settings.setEntry(settings.WXTPATH, wxt4path);
            jButtonFindWxt.setVisible(false);
            

            // move files from wxt4? no! trouble with user.dir
 
            return true;
        }
        catch(Exception ex)
        {
            setMainMessage(ex.toString()+" : "+ex.getMessage());
            jButtonFindWxt.setVisible(true);
            return false;
        }
    }
        
    /**
     * Set content in message field
     * @param M The message
     */
    protected void setMainMessage(String M)
    {
        // All messageline that starts with a \t is either error or warning
        // \tWarning is warning
        // current message is always the unformatted message
        // GUI-spec for jTextPane1 is text/html for this to work
        m_currentMessage=M;
        // make sure since we may have been sloppy in WXT
        M=M.replaceAll("\r", "\n");
        M=M.replaceAll("\n\n", "\n");
        M=M.replaceAll("\n\n", "\n");
        // take the ones that probably will exist in messages
        M=M.replaceAll("<", "&lt;");
        M=M.replaceAll(">", "&gt;");
        String newM="";
        String[] lines=M.split("\n");
        for (int ix=0;ix<lines.length;ix++)
        {
            String line=lines[ix];
            if(line.startsWith("\tWarning"))
                newM+="<span style=color:\"blue\">"+line.replace("\t","   ")+"</span>\n";
            else if(line.startsWith("\t"))
                newM+="<span style=color:\"red\">"+line.replace("\t","   ")+"</span>\n";
            else
                newM+=line+"\n";
        }
        jTextPane1.setText("<html><body><pre style=\"font-size:9px\">"+newM+"</pre></body></html>");
        jTextPane1.setCaretPosition(0);
        //jTextPane1.paintImmediately(jTextPane1.getBounds());
    }
    
    /**
     * Add content to message field
     * @param M The message to add
     */
    protected void addToMainMessage(String M)
    {
        setMainMessage(m_currentMessage+"\n"+M);
    }

    /**
     * Make sure we set all scripts in settings
     * @return
     */
    protected boolean wrapItUp()
    {
        DefaultListModel lm=(DefaultListModel) jList1.getModel();
        for(int ix=0;ix<lm.getSize();ix++)
            m_Settings.setScript((String)lm.elementAt(ix));
        m_Settings.save();
        return true;
     } 
 
    /**
     * Read all scripts from settings
     * and fill scriptlist
     */
    protected void makeScriptListFromSettings()
    {
        List<String> paths=m_Settings.getScripts();
        DefaultListModel listModel = new DefaultListModel();
        setMainMessage("");

        for(int ix=0;ix<paths.size();ix++)
        {
            File f=new File(paths.get(ix));
            if(f.exists())
                listModel.addElement(paths.get(ix));
            else
            {
                addToMainMessage("cannot locate script. "+
                                  paths.get(ix)+
                                 " removing it from list\n");
                m_Settings.removeScript(paths.get(ix));
            }
        }
        jList1.setModel(listModel);
        m_currentScript=null;
        if(!listModel.isEmpty())
            jList1.setSelectedIndex(-1);       
    }
    
    @Action
    public Task  startBuilding() {
         return new BuildTask(getApplication());
    }
   
    /**
     * Attempt to build selected modules
     */
    private class BuildTask extends org.jdesktop.application.Task<Object, Void> 
    {
        Object[]pars=null;
        String report="";
        boolean doIt=true;
        String buildlist="SCRIPT";

        BuildTask(org.jdesktop.application.Application app){
            super(app);
           if((m_wxtPath==null)||(m_Constructor==null))
            {
                report="Cannot build. Dont know wxt.\nYou must set up path to catalog containing wxt4.jar first";
                doIt=false;
                return;                
            }
            if(m_currentScript==null)
            {
                report="No script selected";
                doIt=false;
                return;            
            }
            if(m_currentShandler==null)
            {
                report="No scriptstructure established";
                doIt=false;
                return;            
            }
            buildlist=getBuildList();
            if(buildlist==null)
            {
                report="No modules selected";
                doIt=false;
                return;
            }
           HashMap<String,String> params=new HashMap<String,String>();
            String tmp=m_currentScript.getPath();
            tmp=tmp.replace('\\', '/');
            params.put("script", tmp);
            params.put("modules", buildlist);
            
            params.putAll(getActualOptions());

            pars=new Object[]{params};

            setBuildMode(true);
            
        }
        @Override protected Object doInBackground() throws Exception {
           if(!doIt)
           {
                return report;
           }
           //-------------------
           // build all modules
           try{
               m_BuildModules.invoke(m_currentShandler,pars);
            }
            catch(NullPointerException ex)
            {
                report+="Nullpointer exception in wxt4 build. Debugging case";
            }
            catch(Exception ex)
            {
                report+="Error in wxt build. "+ex.getCause().getMessage();
            }
           //------------------
           // get a report
           try{
               report+=(String)m_GetReport.invoke(m_currentShandler, (Object[])null);
           }
           catch(Exception rex)
           {
               report+="wxt could not report. "+rex.getCause().getMessage();
           }

            return report;  // return your result
        }
        
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
           
            setMainMessage(result.toString());
            setBuildMode(false);
        }
        
         protected void setBuildMode(boolean on)
        {
            jButton1.setEnabled(!on);
            jButton2.setEnabled(!on);
            if(on)
            {
                setMainMessage("Building modules ...");
                statusMessageLabel.setText(" waiting for wxt to finnish ...");
            }
            else
                statusMessageLabel.setText("");
        }
    }

    
    /**
     * Attempt to get wxt to build the modulestructure
     * no actual modulebuilding, just the structure. 
     * That means constructing the wxt4:scripthandler
     * @return true if we succed, false otherwise
     */
    public boolean setUpStructure()
    {
       String report;
       Object[]pars=null;
       if((m_wxtPath==null)||(m_Constructor==null))
        {
            setMainMessage("Cannot build. Dont know wxt.\nYou must set up path to  wxt4.jar first");
            return false;                
        }
        if(m_currentScript==null)
        {
            setMainMessage("No script selected");
            return false;                
        }
       
       // set up params necessary for establishing scriptHandler
       HashMap<String,String> params=new HashMap<String,String>();
       String tmp=m_currentScript.getPath();
       tmp=tmp.replace('\\', '/');
       params.put("script", tmp);
       
       pars=new Object[]{params};
       
       // validate
       // validate is done when constructing swcripthandler
       //setMainMessage("Scriptvalidation:\n"+m_currentScript.validateScript());

       setMainMessage("");
       // establish scripthandler and parse script
       m_currentShandler=null;

       try{
            m_currentShandler=m_Constructor.newInstance(pars);
            try{
                report=(String)m_GetReport.invoke(m_currentShandler, (Object[])null);
            }
            catch(Exception rex)
            {
                addToMainMessage("wxt could not report. "+rex.getCause().getMessage());
                return false;
            }
       }
       catch(Exception bex)
       {
           addToMainMessage("wxt could not parse script. "+bex.getCause().getMessage());
           return false;
       }
       
       // display tree       
       jTree1.setModel(m_currentScript.getModuleTree());
       addToMainMessage(report);
       
       
       // set options according to script       
       setOptionsFromScript();
       
       return true;
    }
    
    
    /**
     * Get the modulenams as selected in the moduletree
     * @return A commaseparated string
     */
    protected String getBuildList()
    {
        String ids="";
        TreePath[] selected=jTree1.getSelectionPaths();
        if(selected==null)
        {
            return null;        
        }
        for(int ix=0;ix<selected.length;ix++)
        {
            treeNode oneselected=(treeNode)selected[ix].getLastPathComponent();
            String tmp=oneselected.getId();
            if(tmp.compareToIgnoreCase(treeNode.NO_MODULES_FOUND)==0)
            {
                return null;
            }
            ids+=","+tmp;
        }
        if(ids.isEmpty())
        {
            setMainMessage("No modules selected");
            return null;
        }

        ids=ids.substring(1);
        return ids;
     }

     /**
      * Update the panel according to options set in the script
      * Start with defaults. Must match defaults as in wxt
      */
    private void setOptionsFromScript()
     {
         
         jCheckBoxExpandAll.setSelected(false);
         jCheckBoxExpandAll.setForeground(Color.black);
         
         jCheckBoxIndentOutput.setSelected(false);
         jCheckBoxIndentOutput.setForeground(Color.black);

         jCheckBoxUseBackup.setSelected(false);
         jCheckBoxUseBackup.setForeground(Color.black);

         jCheckBoxPreCode.setSelected(false);
         jCheckBoxPreCode.setForeground(Color.black);

         jComboBoxOutputFormat.setSelectedIndex(0);
         jLabelOutputFormat.setForeground(Color.black);
         
         jCheckBoxVerbose.setSelected(false);
         jCheckBoxVerbose.setForeground(Color.black);

         jComboBoxReferenceIndexing.setSelectedIndex(0);
         jLabelReferenceIndexing.setForeground(Color.black);
         
         jComboBoxdefaultEncoding.setSelectedItem("UTF-8");
         jLabelDefaultEncoding.setForeground(Color.black);
         
         jCheckBoxAvoid.setSelected(false);
         jCheckBoxAvoid.setForeground(Color.BLACK);
         jTextFieldAvoid.setText("");

         if(m_currentScript!=null)
         {
             HashMap<String,String>scriptOptions=m_currentScript.getActiveOptions();
             if(scriptOptions.containsKey(Script.EXPAND_ALL))
                 jCheckBoxExpandAll.setSelected(scriptOptions.get(Script.EXPAND_ALL).compareTo(Script.YES)==0);
             if(scriptOptions.containsKey(Script.INDENT_OUTPUT))
                 jCheckBoxIndentOutput.setSelected(scriptOptions.get(Script.INDENT_OUTPUT).compareTo(Script.YES)==0);
             if(scriptOptions.containsKey(Script.USE_COPY))
                 jCheckBoxUseBackup.setSelected(scriptOptions.get(Script.USE_COPY).compareTo(Script.YES)==0);
             if(scriptOptions.containsKey(Script.PREFORMAT_LANGUAGE))
                 jCheckBoxPreCode.setSelected(scriptOptions.get(Script.PREFORMAT_LANGUAGE).compareTo(Script.YES)==0);
             if(scriptOptions.containsKey(Script.VERBOSE))
                 jCheckBoxVerbose.setSelected(scriptOptions.get(Script.VERBOSE).compareTo(Script.YES)==0);

             if(scriptOptions.containsKey(Script.REFERENCE_INDEXING))
                 jComboBoxReferenceIndexing.setSelectedItem(scriptOptions.get(Script.REFERENCE_INDEXING));
             if(scriptOptions.containsKey(Script.OUTPUT_FORMAT))
                 jComboBoxOutputFormat.setSelectedItem(scriptOptions.get(Script.OUTPUT_FORMAT));
             if(scriptOptions.containsKey(Script.DEFAULT_ENCODING))
                 jComboBoxdefaultEncoding.setSelectedItem(scriptOptions.get(Script.DEFAULT_ENCODING));
             if(scriptOptions.containsKey(Script.DROP_BOOKS))
             {
                 jCheckBoxAvoid.setSelected(true);
                 jTextFieldAvoid.setText(scriptOptions.get(Script.DROP_BOOKS));
             }
         }            
     }
     
     /**
      * Get all the options as is on the panel
      * @return The options as a hashmap
      */
     private HashMap<String,String>getActualOptions()
     {
        HashMap<String,String>res=new HashMap<String,String>(6);

        
        if(jCheckBoxExpandAll.isSelected()) 
            res.put(Script.EXPAND_ALL, Script.YES); 
        else 
            res.put(Script.EXPAND_ALL, Script.NO);
        
        if(jCheckBoxIndentOutput.isSelected()) 
            res.put(Script.INDENT_OUTPUT, Script.YES); 
        else 
            res.put(Script.INDENT_OUTPUT, Script.NO);

        if(jCheckBoxUseBackup.isSelected())
            res.put(Script.USE_COPY, Script.YES);
        else
            res.put(Script.USE_COPY, Script.NO);

        if(jCheckBoxPreCode.isSelected())
            res.put(Script.PREFORMAT_LANGUAGE, Script.YES);
        else
            res.put(Script.PREFORMAT_LANGUAGE, Script.NO);
        
        if(jCheckBoxVerbose.isSelected())
            res.put(Script.VERBOSE, Script.YES);
        else
            res.put(Script.VERBOSE, Script.NO);

        res.put(Script.OUTPUT_FORMAT,(String)jComboBoxOutputFormat.getSelectedItem());
        res.put(Script.DEFAULT_ENCODING,(String)jComboBoxdefaultEncoding.getSelectedItem());
        res.put(Script.REFERENCE_INDEXING,(String)jComboBoxReferenceIndexing.getSelectedItem());

        if(jCheckBoxAvoid.isSelected())
        {
            String CSVlist=jTextFieldAvoid.getText();
            if((CSVlist!=null)&&(CSVlist.length()>0))
                res.put(Script.DROP_BOOKS, CSVlist);
            else
                res.put(Script.DROP_BOOKS, Script.DROP_NONE);
        }
        else
            res.put(Script.DROP_BOOKS, Script.DROP_NONE);
        
        return res;
     }

    @Action
    public Task startUpdating() {
        return new StartUpdatingTask(getApplication());
    }

    private class StartUpdatingTask extends org.jdesktop.application.Task<Object, Void> {
        Object[]pars=null;
        String report="";
        boolean doIt=true;

       StartUpdatingTask(org.jdesktop.application.Application app) {
           super(app);
           if((m_wxtPath==null)||(m_Constructor==null))
            {
                report="Cannot build. Dont know wxt4.\nYou must set up path to wxt4.jar first";
                doIt=false;
                return;                
            }
            if(m_currentScript==null)
            {
                report="No script selected";
                doIt=false;
                return;            
            }
            if(m_currentShandler==null)
            {
                report="No scriptstructure established";
                doIt=false;
                return;            
            }

           HashMap<String,String> params=new HashMap<String,String>();
            String tmp=m_currentScript.getPath();
            tmp=tmp.replace('\\', '/');
            
            params.putAll(getActualOptions());

            pars=new Object[]{params};

            setBuildMode(true);
            
        }
        @Override protected Object doInBackground() {
           if(!doIt)
           {
                return report;
           }
           //-------------------
           // build all modules
           try{
               m_UpdateModules.invoke(m_currentShandler,pars);
               //report=(String)m_GetReport.invoke(shandler, (Object[])null);

            }
            catch(NullPointerException ex)
            {
                report+="Nullpointer exception in wxt build. Debugging case";
            }
            catch(Exception ex)
            {
                report+="Error in wxt build. "+ex.getCause().getMessage();
            }
           //------------------
           // get a report
           try{
               report+=(String)m_GetReport.invoke(m_currentShandler, (Object[])null);
           }
           catch(Exception rex)
           {
               report+="wxt could not report. "+rex.getCause().getMessage();
           }

            return report;  
        }
        @Override protected void succeeded(Object result) {
            setMainMessage(result.toString());
            setBuildMode(false);
        }
        protected void setBuildMode(boolean on)
        {
            jButton1.setEnabled(!on);
            jButton2.setEnabled(!on);

            if(on)
            {
                setMainMessage("Updating modules ...");
                statusMessageLabel.setText(" waiting for wxt to finnish ...");
            }
            else
            {
                statusMessageLabel.setText("");
            }
        }
    }


}
