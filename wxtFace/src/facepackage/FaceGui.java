package facepackage;

import engine.WXTEngine;
import faceutils.TreeMaker;
import faceutils.TreeNode;
import helppackage.Helper;
import helppackage.ModuleLocationView;
import helppackage.ModuleView;
import helppackage.OptionHelp;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreePath;
import org.w3c.dom.Element;


/**
 *
 * @author Administrator
 */
public class FaceGui extends javax.swing.JFrame{
   
    /** title, without scriptpath*/
    static final String TITLE="WXT Face";
    /** the WXT-engine we have connected to*/
    WXTEngine m_Engine=null;
    /** settings as aved on file */
    Settings m_Settings=null;
    /** the look and feel we have active*/
    JCheckBoxMenuItem m_currentSelectedLookAndFeel=null;
    /** content in the message box*/
    String m_currentMessage="";
    /** the script which has our attention*/
    Script m_currentScript=null;
    /** all names on look and feel */
    HashSet LAndFs=new HashSet();


    public FaceGui() {
        initComponents();
        postInit();
    }
    
    public void postInit(){
        
        //-----------------------------
        // handle settings, load if anything to load
        m_Settings=new Settings();
        m_Settings.load();
        

        
        //-----------------
        // set neutral title
        this.setTitle(TITLE);
        
        //------------------
        // size and position
        // get position and status from settings

        
        //--------------------------
        // set up look and feel menu  
        LookAndFeelInfo[] lfi=UIManager.getInstalledLookAndFeels();
        String selectedClassname=m_Settings.getEntry(Settings.LOOK_AND_FEEL);
        if(selectedClassname==null) {
            selectedClassname=UIManager.getSystemLookAndFeelClassName();
            m_Settings.setEntry(Settings.LOOK_AND_FEEL, selectedClassname);
        }
        for(int ix=0;ix<lfi.length;ix++)
        {
            String s=lfi[ix].getName();
            JCheckBoxMenuItem mit=new JCheckBoxMenuItem(s);
            LAndFs.add(s);
 
            mit.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewMenuActionPerformed(evt);
            }
            });
            jMenuView.add(mit);
            if (lfi[ix].getClassName().compareTo(selectedClassname)==0)
            {
                m_currentSelectedLookAndFeel=mit;
                mit.setSelected(true);
            }
        }
        // set look and feel according to settings    
        try{
            javax.swing.UIManager.setLookAndFeel(selectedClassname);
            javax.swing.SwingUtilities.updateComponentTreeUI(this.getRootPane());
        }
        catch(Exception ex)
        {
         // do nothing, leave as is
        }        
        
        //----------------------------------------
        //set up wxt engine
        String wxtpath=m_Settings.getEntry(Settings.WXTPATH);
        if(wxtpath==null) {
            //while testing: wxtpath="C:\\wxtproject\\wxt4\\dist\\wxt4.jar";
            
            // do a file dialog
            m_Engine=WXTEngine.locateWXT(this);
            if(m_Engine!=null){
                wxtpath=m_Engine.getPath();
                m_Settings.setEntry(Settings.WXTPATH, wxtpath);
            }
        }
        else{
            try{
                m_Engine=new WXTEngine(wxtpath);
            }
            catch(Exception ex){
                // report exception
                setMainMessage(ex.getMessage());
                m_Engine=null;
            }
                    
        }
        
        
        //----------------------
        // set up script list
        scriptMakeListFromSettings();
        // noen is selected so we clear modules
        jTreeModules.setModel(TreeMaker.getEmptyTree());
        
        
        //----------------------------
        // fill up the encoding combobox in the options pane
        java.util.SortedMap cm=java.nio.charset.Charset.availableCharsets();
        Object[] vals=cm.values().toArray();
        jComboBoxDefaultEncoding.removeAllItems();
        for(int ix=0;ix< vals.length;ix++) {
            jComboBoxDefaultEncoding.addItem(vals[ix].toString());
        }
        jComboBoxDefaultEncoding.setSelectedItem("UTF-8");
        
        //-------------------------
        // set up windowlisteners
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {
                m_Settings.save();
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {windowGainedFocus(e);}
            @Override
            public void windowDeactivated(WindowEvent e) {windowLostFocus(e);}
            @Override
            public void windowStateChanged(WindowEvent e) {}
            @Override
            public void windowGainedFocus(WindowEvent e) {
               repaint();
               // any script selected ?
               // while testing:System.out.println("focus");
               if(m_currentScript==null) {
                    return;
                }

               // should we do anything with the current script ?
               // Is it removed ?
               File scriptfile=new File(m_currentScript.getPath());
               if(!scriptfile.exists())
               {
                   scriptRemoveSelected();
                   setGUIState();
                   return;
               }

               // Has it been changed ? if so we recreate it
               long t=scriptfile.lastModified();
               if( t > m_currentScript.getLastTimeSeen() )
               {
                     scriptSetUpSelected();
               }           
            }
            
            @Override
            public void windowLostFocus(WindowEvent e) {
                // set status on current script
                if(m_currentScript!=null) {
                    m_currentScript.setLastTimeSeen();
                    // while testing:System.out.println("unfocus");
                }
            }
            }
        );  
        
        //---------------------------------
        // listening to the module tree
         MouseListener ml = new MouseAdapter() {
            @Override
             public void mousePressed(MouseEvent e) {
                 int selRow = jTreeModules.getRowForLocation(e.getX(), e.getY());
                 TreePath selPath = jTreeModules.getPathForLocation(e.getX(), e.getY());
                 // hit and rightbutton, what about mac ?                 
                 if((selRow != -1) && (e.getButton()==MouseEvent.BUTTON3) && (e.getClickCount() == 1)){
                     pressedRightButtonOnModuleTree(selRow, selPath);
                 }
                 // check if something is set for instance with left button clicked ?
                 setGUIState();
             }
         };
         jTreeModules.addMouseListener(ml);
         
         setGUIState();

    }
    
       
    // sentralized setting of all gui-states 
    private void setGUIState(){
        // WXT address
        if(m_Engine==null) {
            jLabelWXTEngine.setForeground(Color.red);
            jLabelWXTEngine.setText("No WXT Engine");
        }
        else {
            jLabelWXTEngine.setForeground(Color.black);
            jLabelWXTEngine.setText(m_Engine.getPath());
        }
        // popup menues
        jMenuItemInPopRemoveScript.setEnabled(true);
        jMenuItemInPopAddScript.setEnabled(true);
        if(jListScripts.getSelectedIndex()==-1){
            jMenuItemInPopRemoveScript.setEnabled(false);
        }       
        
        // file menu items
        jMenuItemRemoveSelected.setEnabled(jMenuItemInPopRemoveScript.isEnabled());
        jMenuItemLoadScript.setEnabled(true);
        jMenuItemExit.setEnabled(true);
        jMenuItemClearScriptlist.setEnabled(true);

        if(jListScripts.getModel().getSize()==0){
            jMenuItemClearScriptlist.setEnabled(false);
        }
        // tool options
        jMenuItemModuleList.setEnabled(true);
        if(m_currentScript==null){
            jMenuItemModuleList.setEnabled(false);
        }
        // build button
        jButtonBuild.setEnabled(true);
        if(jTreeModules.getSelectionPath()==null){
            jButtonBuild.setEnabled(false);
        }
        // show modules button, same as jMenuItemModuleList
        jButtonShowList.setEnabled(m_currentScript!=null);
    }
    
     /**
     * When rightclicking in moduletree to show description of a module
     * @param selRow Selected row
     * @param selPath Selected path in tree to selected treenode
     */
    protected void pressedRightButtonOnModuleTree(int selRow, TreePath selPath){
        Element elt=((TreeNode)selPath.getLastPathComponent()).getElement();
        if(elt==null)
        {
            return;
        }
        String id="<none>";
        if(elt.hasAttribute("id")) {
            id=elt.getAttribute("id");
        }
        else if(elt.hasAttribute("name")) {
            id=elt.getAttribute("name");
        }
       
        String mdesc="nothing";
        String madd="";
        try{
            mdesc=(String)m_Engine.getModuleDescription(id);
            madd=(String)m_Engine.getModuleAddress(id);

            ModuleView mv=new ModuleView(this,true,mdesc,madd);
            mv.setVisible(true);
        }
        catch(Exception e)
        {
            setMainMessage("Failing to get moduledescription "+e.getMessage());
        }        
    }
    
   

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuInScriptList = new javax.swing.JPopupMenu();
        jMenuItemInPopAddScript = new javax.swing.JMenuItem();
        jMenuItemInPopRemoveScript = new javax.swing.JMenuItem();
        jPanelMain = new javax.swing.JPanel();
        jSplitPaneVerticalMain = new javax.swing.JSplitPane();
        jSplitPaneHBig = new javax.swing.JSplitPane();
        jPanelScripts = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListScripts = new javax.swing.JList();
        jButtonShowList = new javax.swing.JButton();
        jSplitPaneRight = new javax.swing.JSplitPane();
        jPanelModules = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTreeModules = new javax.swing.JTree();
        jPanelOptions = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButtonBuild = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jCheckBoxExpandAll = new javax.swing.JCheckBox();
        jCheckBoxUseBackup = new javax.swing.JCheckBox();
        jCheckBoxPreformatCode = new javax.swing.JCheckBox();
        jCheckBoxVerbose = new javax.swing.JCheckBox();
        jCheckBoxIgnoreBooks = new javax.swing.JCheckBox();
        jTextFieldIgnoredBooks = new javax.swing.JTextField();
        jComboBoxOutputFormat = new javax.swing.JComboBox();
        jComboBoxDefaultEncoding = new javax.swing.JComboBox();
        jComboBoxReferenceIndexing = new javax.swing.JComboBox();
        jLabeOutputFormat = new javax.swing.JLabel();
        jLabelDefaultEncoding = new javax.swing.JLabel();
        jLabelRefIndexing = new javax.swing.JLabel();
        jButtonOptionExplain = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanelMessage = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextPaneMainMessage = new javax.swing.JTextPane();
        jProgressBarWorking = new javax.swing.JProgressBar();
        jLabelWXTEngine = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemLoadScript = new javax.swing.JMenuItem();
        jMenuItemRemoveSelected = new javax.swing.JMenuItem();
        jMenuItemClearScriptlist = new javax.swing.JMenuItem();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuTools = new javax.swing.JMenu();
        jMenuItemFindWXT = new javax.swing.JMenuItem();
        jMenuItemModuleList = new javax.swing.JMenuItem();
        jMenuView = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();
        jMenuItemWXTHelp = new javax.swing.JMenuItem();
        jMenuItemGUIHelp = new javax.swing.JMenuItem();
        jMenuItemVersions = new javax.swing.JMenuItem();

        jPopupMenuInScriptList.setInvoker(jListScripts);

        jMenuItemInPopAddScript.setText("Add script...");
        jMenuItemInPopAddScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInPopAddScriptActionPerformed(evt);
            }
        });
        jPopupMenuInScriptList.add(jMenuItemInPopAddScript);

        jMenuItemInPopRemoveScript.setText("Remove selected script");
        jMenuItemInPopRemoveScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInPopRemoveScriptActionPerformed(evt);
            }
        });
        jPopupMenuInScriptList.add(jMenuItemInPopRemoveScript);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximizedBounds(new java.awt.Rectangle(0, 0, 1000, 1000));
        setMaximumSize(new java.awt.Dimension(1000, 1000));
        setMinimumSize(new java.awt.Dimension(600, 500));
        setName("GUIFrame"); // NOI18N

        jPanelMain.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jPanelMain.setPreferredSize(new java.awt.Dimension(800, 600));

        jSplitPaneVerticalMain.setDividerLocation(400);
        jSplitPaneVerticalMain.setDividerSize(3);
        jSplitPaneVerticalMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneVerticalMain.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jSplitPaneVerticalMain.setMinimumSize(new java.awt.Dimension(600, 415));
        jSplitPaneVerticalMain.setPreferredSize(new java.awt.Dimension(800, 630));

        jSplitPaneHBig.setDividerLocation(200);
        jSplitPaneHBig.setMaximumSize(new java.awt.Dimension(505, 1000));
        jSplitPaneHBig.setMinimumSize(new java.awt.Dimension(505, 400));
        jSplitPaneHBig.setPreferredSize(new java.awt.Dimension(505, 400));

        jPanelScripts.setMaximumSize(new java.awt.Dimension(200, 400));
        jPanelScripts.setMinimumSize(new java.awt.Dimension(200, 400));
        jPanelScripts.setPreferredSize(new java.awt.Dimension(200, 400));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Scripts");
        jLabel1.setMaximumSize(new java.awt.Dimension(32, 20));
        jLabel1.setMinimumSize(new java.awt.Dimension(32, 20));

        jListScripts.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListScripts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListScripts.setComponentPopupMenu(jPopupMenuInScriptList);
        jListScripts.setMaximumSize(new java.awt.Dimension(200, 1000));
        jListScripts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListScriptsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jListScripts);

        jButtonShowList.setText("Vis Modulliste");
        jButtonShowList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonShowListActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelScriptsLayout = new javax.swing.GroupLayout(jPanelScripts);
        jPanelScripts.setLayout(jPanelScriptsLayout);
        jPanelScriptsLayout.setHorizontalGroup(
            jPanelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
            .addGroup(jPanelScriptsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonShowList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelScriptsLayout.setVerticalGroup(
            jPanelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScriptsLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonShowList))
        );

        jSplitPaneHBig.setLeftComponent(jPanelScripts);

        jSplitPaneRight.setDividerLocation(200);
        jSplitPaneRight.setMaximumSize(new java.awt.Dimension(700, 1000));
        jSplitPaneRight.setMinimumSize(new java.awt.Dimension(600, 400));
        jSplitPaneRight.setPreferredSize(new java.awt.Dimension(700, 402));

        jPanelModules.setMinimumSize(new java.awt.Dimension(200, 400));
        jPanelModules.setPreferredSize(new java.awt.Dimension(200, 400));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Modules");

        jScrollPane5.setViewportView(jTreeModules);

        javax.swing.GroupLayout jPanelModulesLayout = new javax.swing.GroupLayout(jPanelModules);
        jPanelModules.setLayout(jPanelModulesLayout);
        jPanelModulesLayout.setHorizontalGroup(
            jPanelModulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelModulesLayout.setVerticalGroup(
            jPanelModulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelModulesLayout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
        );

        jSplitPaneRight.setLeftComponent(jPanelModules);

        jPanelOptions.setMinimumSize(new java.awt.Dimension(305, 400));
        jPanelOptions.setPreferredSize(new java.awt.Dimension(305, 400));

        jPanel4.setMaximumSize(new java.awt.Dimension(400, 400));

        jButtonBuild.setText("Build selected modules");
        jButtonBuild.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBuildActionPerformed(evt);
            }
        });

        jPanel6.setBackground(new java.awt.Color(255, 255, 204));
        jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jCheckBoxExpandAll.setBackground(new java.awt.Color(255, 255, 204));
        jCheckBoxExpandAll.setText("Expand all");
        jCheckBoxExpandAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jCheckBoxUseBackup.setBackground(new java.awt.Color(255, 255, 204));
        jCheckBoxUseBackup.setText("Use backup");
        jCheckBoxUseBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jCheckBoxPreformatCode.setBackground(new java.awt.Color(255, 255, 204));
        jCheckBoxPreformatCode.setText("Preformat code");
        jCheckBoxPreformatCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jCheckBoxVerbose.setBackground(new java.awt.Color(255, 255, 204));
        jCheckBoxVerbose.setText("Verbose");
        jCheckBoxVerbose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jCheckBoxIgnoreBooks.setBackground(new java.awt.Color(255, 255, 204));
        jCheckBoxIgnoreBooks.setText("Ignore books:");
        jCheckBoxIgnoreBooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jTextFieldIgnoredBooks.setText("jTextField1");
        jTextFieldIgnoredBooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jComboBoxOutputFormat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "html", "xml", "text", "xhtml" }));
        jComboBoxOutputFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jComboBoxDefaultEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxDefaultEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jComboBoxReferenceIndexing.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "local", "global" }));
        jComboBoxReferenceIndexing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        jLabeOutputFormat.setText("Output Format");

        jLabelDefaultEncoding.setText("Default encoding");

        jLabelRefIndexing.setText("Reference indexing");

        jButtonOptionExplain.setText("?");
        jButtonOptionExplain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOptionExplainActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("<html>overrides options set in script when<span style=\" color:red\"> red</span>");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonOptionExplain)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jCheckBoxIgnoreBooks)
                            .addGap(18, 18, 18)
                            .addComponent(jTextFieldIgnoredBooks))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                            .addGap(15, 15, 15)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jCheckBoxExpandAll)
                                        .addComponent(jCheckBoxUseBackup)
                                        .addComponent(jLabeOutputFormat))
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addGap(18, 18, 18)
                                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jCheckBoxVerbose)
                                                .addComponent(jCheckBoxPreformatCode)))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addGap(22, 22, 22)
                                            .addComponent(jComboBoxOutputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabelDefaultEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jComboBoxDefaultEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabelRefIndexing)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jComboBoxReferenceIndexing, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxExpandAll)
                    .addComponent(jCheckBoxPreformatCode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxUseBackup)
                    .addComponent(jCheckBoxVerbose))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabeOutputFormat)
                    .addComponent(jComboBoxOutputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDefaultEncoding)
                    .addComponent(jComboBoxDefaultEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxReferenceIndexing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRefIndexing))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxIgnoreBooks)
                    .addComponent(jTextFieldIgnoredBooks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonOptionExplain))
                .addContainerGap())
        );

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Options");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonBuild)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 8, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonBuild)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
        jPanelOptions.setLayout(jPanelOptionsLayout);
        jPanelOptionsLayout.setHorizontalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 23, Short.MAX_VALUE))
        );
        jPanelOptionsLayout.setVerticalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jSplitPaneRight.setRightComponent(jPanelOptions);

        jSplitPaneHBig.setRightComponent(jSplitPaneRight);

        jSplitPaneVerticalMain.setLeftComponent(jSplitPaneHBig);

        jPanelMessage.setMinimumSize(new java.awt.Dimension(100, 10));
        jPanelMessage.setPreferredSize(new java.awt.Dimension(700, 226));

        jTextPaneMainMessage.setContentType("text/html"); // NOI18N
        jTextPaneMainMessage.setMinimumSize(new java.awt.Dimension(6, 10));
        jTextPaneMainMessage.setPreferredSize(new java.awt.Dimension(6, 100));
        jScrollPane7.setViewportView(jTextPaneMainMessage);

        jProgressBarWorking.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelWXTEngine.setText("jLabel4");

        javax.swing.GroupLayout jPanelMessageLayout = new javax.swing.GroupLayout(jPanelMessage);
        jPanelMessage.setLayout(jPanelMessageLayout);
        jPanelMessageLayout.setHorizontalGroup(
            jPanelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMessageLayout.createSequentialGroup()
                .addGroup(jPanelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane7)
                    .addGroup(jPanelMessageLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelWXTEngine, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jProgressBarWorking, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 201, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        jPanelMessageLayout.setVerticalGroup(
            jPanelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMessageLayout.createSequentialGroup()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelWXTEngine)
                    .addComponent(jProgressBarWorking, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jSplitPaneVerticalMain.setRightComponent(jPanelMessage);

        javax.swing.GroupLayout jPanelMainLayout = new javax.swing.GroupLayout(jPanelMain);
        jPanelMain.setLayout(jPanelMainLayout);
        jPanelMainLayout.setHorizontalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMainLayout.createSequentialGroup()
                .addComponent(jSplitPaneVerticalMain, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelMainLayout.setVerticalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneVerticalMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE)
        );

        jMenuFile.setText("File");

        jMenuItemLoadScript.setText("Load Script...");
        jMenuItemLoadScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileMenuActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemLoadScript);

        jMenuItemRemoveSelected.setText("Remove selected script");
        jMenuFile.add(jMenuItemRemoveSelected);

        jMenuItemClearScriptlist.setText("Clear Scriptlist");
        jMenuItemClearScriptlist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileMenuActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemClearScriptlist);

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileMenuActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar1.add(jMenuFile);

        jMenuTools.setText("Tools");
        jMenuTools.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToolMenuActionPerformed(evt);
            }
        });

        jMenuItemFindWXT.setText("Find WXT engine...");
        jMenuItemFindWXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToolMenuActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemFindWXT);

        jMenuItemModuleList.setText("Show ModuleList");
        jMenuItemModuleList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToolMenuActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemModuleList);

        jMenuBar1.add(jMenuTools);

        jMenuView.setText("View");
        jMenuBar1.add(jMenuView);

        jMenuHelp.setText("Help");

        jMenuItemAbout.setText("About ...");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHelpMenuActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuItemWXTHelp.setText("WXT Help");
        jMenuItemWXTHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHelpMenuActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemWXTHelp);

        jMenuItemGUIHelp.setText("GUI Help");
        jMenuItemGUIHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHelpMenuActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemGUIHelp);

        jMenuItemVersions.setText("Versions ...");
        jMenuItemVersions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHelpMenuActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemVersions);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    //-------- FILE-----------------
    // doing File menu actions
    private void jFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileMenuActionPerformed
        if(evt.getSource().equals(jMenuItemExit)){
            // exit this application
            m_Settings.save();
            System.exit(0);
        }
        else if(evt.getSource().equals(jMenuItemLoadScript)){
            // load a new script to the scriptlist
            scriptLoadNewToList();            
        }
        else if(evt.getSource().equals(jMenuItemRemoveSelected)){
            // remove selected if any
            scriptRemoveSelected();
        }
        else if(evt.getSource().equals(jMenuItemClearScriptlist)){
            // empty the scriptlist
            m_Settings.removeAllScripts();
            scriptMakeListFromSettings();
            jTreeModules.setModel(TreeMaker.getEmptyTree());
        }
        // update state anyhow
        setGUIState();
    }//GEN-LAST:event_jFileMenuActionPerformed
    
    
    
    //--------------  HELP -------
    // doing Help menu actions
    private void jHelpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jHelpMenuActionPerformed

        Helper hlp=new Helper(this,m_Engine);
        if(evt.getSource().equals(jMenuItemAbout)){
            // show about
            hlp.doAbout();
        }
        else if(evt.getSource().equals(jMenuItemWXTHelp)){
            // show WXTHelp
            hlp.doWXTHelp();
        }
        else if(evt.getSource().equals(jMenuItemGUIHelp)){
            // show gui help
            hlp.doGUIHelp();
        }
        else if(evt.getSource().equals(jMenuItemVersions)){
            // version dialogbox
            hlp.doVersions();
        }
    }//GEN-LAST:event_jHelpMenuActionPerformed
   
    //------------- TOOLS -------
    // doing Tools menu action
    private void jToolMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToolMenuActionPerformed
        if(evt.getSource().equals(jMenuItemFindWXT)){
            // locate or relocate WXT engine
            m_Engine=WXTEngine.locateWXT(this);
            if(m_Engine!=null){
                String wxtpath=m_Engine.getPath();
                m_Settings.setEntry(Settings.WXTPATH, wxtpath);
                // show address
            }
        }
        else if(evt.getSource().equals(jMenuItemModuleList)){
            // user want to get a list of modules: id \t absoluteuri
            // in a dialog box
            showModuleList();
        }
        setGUIState();
    }//GEN-LAST:event_jToolMenuActionPerformed

    private void showModuleList(){
            ModuleLocationView mv=new ModuleLocationView(this,true);
            String s;
            try{
                s=(String)m_Engine.getModuleLocations();
                String[]lines=s.split("\n");
                mv.setData(lines.length,s);
                
            }
            catch(Exception ex)
            {
                s="failed to access module locations";
                mv.setData(0,s);
            }
            mv.setVisible(true);
      
    }
    /**
     * Any options changed, update colormarking on differences
     * between script options and user selected options
     * @param evt 
     */
    private void OptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OptionActionPerformed
        if(m_currentScript!=null) {
            optionsUpdateColors(m_currentScript);
        }
    }//GEN-LAST:event_OptionActionPerformed

    /**
     * Build selected modules
     * @param evt 
     */
    private void jButtonBuildActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBuildActionPerformed
        // we want to build
        String buildlist=getBuildList();
        if(buildlist==null){
            return;
        }
        // we have list of modules as a commaseparated string
        HashMap<String,String> params=new HashMap();
        String tmp=m_currentScript.getPath();
        tmp=tmp.replace('\\', '/');
        params.put("script", tmp);
        params.put("modules", buildlist);       
        params.putAll(optionsCollectFromGUI());
        

        // build all modules
        String report="";
        Waiter w=new Waiter(jProgressBarWorking);       
        try{
           w.setPriority(Thread.MAX_PRIORITY);
           w.start();
           m_Engine.buildModules(params);
           w.stopIt();
         }
         catch(NullPointerException ex)
         {
             report+="Nullpointer exception in WXT build. Debugging case";
         }
         catch(Exception ex)
         {
             report+="Error in WXT build. "+ex.getCause().getMessage();
         }
        //------------------
        // get a report
        try{
            report+=m_Engine.getReport();
        }
        catch(Exception rex)
        {
            report+="WXT could not report. "+rex.getCause().getMessage();
        }
        if(report.indexOf("\\tWarning:Attempt to tidy")!=-1){
            report+="\n\tNOTE: Tidy has happened. The result may be wellformed, but not necessarily what you expect";
        }
        setMainMessage(report);
        w.stopIt();
    }//GEN-LAST:event_jButtonBuildActionPerformed

    /**
     * Clicked in scriptlist, update selected
     * @param evt 
     */
    private void jListScriptsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListScriptsMouseClicked
        scriptSetUpSelected();
    }//GEN-LAST:event_jListScriptsMouseClicked

    /**
     * popup menu add script to list
     * @param evt 
     */
    private void jMenuItemInPopAddScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInPopAddScriptActionPerformed
        // add a new script to list
        scriptLoadNewToList();
    }//GEN-LAST:event_jMenuItemInPopAddScriptActionPerformed

    /**
     * popupmenu remove selected script from list
     * @param evt 
     */
    private void jMenuItemInPopRemoveScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInPopRemoveScriptActionPerformed
        // TODO add your handling code here:
        scriptRemoveSelected();
    }//GEN-LAST:event_jMenuItemInPopRemoveScriptActionPerformed

    /**
     * Get an explanation of options
     * @param evt 
     */
    private void jButtonOptionExplainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOptionExplainActionPerformed
        String tmp=m_Engine.getOptionDescription();
        // while testing:  tmp=null;
        OptionHelp op=new OptionHelp(this,true,tmp);
        op.setVisible(true);
    }//GEN-LAST:event_jButtonOptionExplainActionPerformed

    private void jButtonShowListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonShowListActionPerformed
        // show the module list
        showModuleList();
    }//GEN-LAST:event_jButtonShowListActionPerformed
    
    //--------- VIEW -------------------
    // doing viewmenu actions, that is look and feel
    private void jViewMenuActionPerformed(java.awt.event.ActionEvent evt){
        // is it a look and feel menuitem?
        if(LAndFs.contains(evt.getActionCommand())){
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
                if(m_currentSelectedLookAndFeel!=null) {
                    m_currentSelectedLookAndFeel.setSelected(false);
                }
                javax.swing.UIManager.setLookAndFeel(lfClassName);
                m_Settings.setEntry(Settings.LOOK_AND_FEEL, lfClassName);
                javax.swing.SwingUtilities.updateComponentTreeUI(this.getRootPane());
                m.setSelected(true);
                m_currentSelectedLookAndFeel=m;
            }
            catch(ClassNotFoundException | 
                    InstantiationException | 
                    IllegalAccessException | 
                    UnsupportedLookAndFeelException ex)
            {
                // do nothing
            }
        }
        // nothing else than L&F at the moment
    }
    //---------------------------------------------
    // setting up script
    
    /**
     * produce scriptlist from settings
     */
    protected void scriptMakeListFromSettings()
    {
        List<String> paths=m_Settings.getScripts();
        DefaultListModel listModel = new DefaultListModel();
        setMainMessage("");

        for(int ix=0;ix<paths.size();ix++)
        {
            File f=new File(paths.get(ix));
            if(f.exists()) {
                listModel.addElement(paths.get(ix));
            }
            else
            {
                addToMainMessage("cannot locate script. "+
                                  paths.get(ix)+
                                 " removing it from list\n");
                m_Settings.removeScript(paths.get(ix));
            }
        }
        jListScripts.setModel(listModel);
        m_currentScript=null;
        if(!listModel.isEmpty()) {
            jListScripts.setSelectedIndex(-1);
        } 
        setGUIState();
    }
    
    /**
     * Load a new script to scriptlist
     */
    protected void scriptLoadNewToList(){
        // do a dialog
        String scriptpath=Script.loadScript(this);
        int selected=jListScripts.getSelectedIndex();
        if(scriptpath!=null){
            // we perform a simple test that the script is wellformed and 
            // has wscript as root
            try{
                Script tmp=new Script(scriptpath);
                m_Settings.setScript(scriptpath);               
                scriptMakeListFromSettings();
                if(selected!=-1) {
                    jListScripts.setSelectedIndex(selected);
                }
                setGUIState();
            }
            catch(Exception ex){
                setMainMessage(scriptpath+" "+ex.getMessage());
            }
        }
        setGUIState();
    }
    
    /**
     * Remove selected script
     * Note that the selected line may not have been established as a script
     * (it may have failed inparsing)
     */
    protected void scriptRemoveSelected(){
       if(m_currentScript!=null)
       {
            m_Settings.removeScript(m_currentScript.m_filePath);
            scriptMakeListFromSettings();
            jTreeModules.setModel(TreeMaker.getEmptyTree());
       }
       else{
           int ix=jListScripts.getSelectedIndex();
           String tmp=(String)jListScripts.getSelectedValue();
           if(ix != -1){
               m_Settings.removeScript(tmp);
               scriptMakeListFromSettings();
               jTreeModules.setModel(TreeMaker.getEmptyTree());
            }
      }
       setGUIState();
    }
    
    /**
     * Try to set up selected script
     */
    protected void scriptSetUpSelected(){
        // have a new selection in scriptlist
       // attempt to establish the  script, let WXT validate it and display its modules
       if(m_Engine==null){
           setMainMessage("No WXT- connection, cant investigate script");
           setGUIState();
           return;
       }

       if(jListScripts.getSelectedValue()!=null)
       {
           setMainMessage("parsing script ...");
           Waiter w=new Waiter(jProgressBarWorking);
           try{
               String tmp=jListScripts.getSelectedValue().toString();
               System.out.println("parsing script");
               m_currentScript=new Script(tmp);
               w.setPriority(Thread.MAX_PRIORITY);
               w.start();
               Date t1=new Date();
               m_Engine.constructScript(m_currentScript.getPath());              
               jTreeModules.setModel(m_currentScript.getModuleTree());
               setMainMessage("Done parsing script:\n");
               addToMainMessage(m_Engine.getReport());
               long secs=new Date().getTime()-t1.getTime();
               addToMainMessage(""+secs+" ms");
               w.stopIt();
               optionsSetFromScript(m_currentScript);
               optionsUpdateColors(m_currentScript);
               this.setTitle(TITLE+": "+m_currentScript.getPath());
               m_Settings.setEntry(m_Settings.SCRIPTPATH, m_currentScript.getPath());
           }
           catch(Exception x)
           {
               addToMainMessage(x.getMessage());
               jListScripts.setSelectedIndex(-1);
               jTreeModules.setModel(Script.getEmptyTree());
               m_currentScript=null;
               this.setTitle(TITLE);
               w.stopIt();
           }
           finally{
               setGUIState();
           }
       }
    }

    //----------------------------------------------
    // messaging
     /**
     * Add content to message field
     * @param M The message to add
     */
    protected void addToMainMessage(String M)
    {
        setMainMessage(m_currentMessage+"\n"+M);
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
            if(line.startsWith("\tWarning")) {
                newM+="<span style=color:\"blue\">"+line.replace("\t","   ")+"</span>\n";
            }
            else if(line.startsWith("\t")) {
                newM+="<span style=color:\"red\">"+line.replace("\t","   ")+"</span>\n";
            }
            else {
                newM+=line+"\n";
            }
        }
        jTextPaneMainMessage.setText("<html><body><pre style=\"font-size:9px\">"+newM+"</pre></body></html>");
        jTextPaneMainMessage.setCaretPosition(0);
        jTextPaneMainMessage.invalidate();
 
    }
    
     /**
     * Get the modulenams as selected in the moduletree
     * @return A commaseparated string
     */
    protected String getBuildList()
    {
        String ids="";
        TreePath[] selected=jTreeModules.getSelectionPaths();
        if(selected==null)
        {
            return null;        
        }
        for(int ix=0;ix<selected.length;ix++)
        {
            TreeNode oneselected=(TreeNode)selected[ix].getLastPathComponent();
            String tmp=oneselected.getId();

            // if root node is selected that is all we need
            if(tmp.equals(Script.ALL_MODULES)){
                return tmp;
            }
            if(tmp.compareToIgnoreCase(TreeNode.NO_MODULES_FOUND)==0)
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

    
    //---------------  do options ---------
    // optionsSetFromScript
    // optionsCollectFromGUI
    // upDateOptionsColors

    
    private void optionsSetFromScript(Script script){
         jCheckBoxExpandAll.setSelected(false);
         jCheckBoxExpandAll.setForeground(Color.black);
         
         jCheckBoxUseBackup.setSelected(false);
         jCheckBoxUseBackup.setForeground(Color.black);

         jCheckBoxPreformatCode.setSelected(false);
         jCheckBoxPreformatCode.setForeground(Color.black);

         jComboBoxOutputFormat.setSelectedIndex(0);
         jLabeOutputFormat.setForeground(Color.black);
         
         jCheckBoxVerbose.setSelected(false);
         jCheckBoxVerbose.setForeground(Color.black);

         jComboBoxReferenceIndexing.setSelectedIndex(0);
         jLabelRefIndexing.setForeground(Color.black);
         
         jComboBoxDefaultEncoding.setSelectedItem("UTF-8");
         jLabelDefaultEncoding.setForeground(Color.black);
         
         jCheckBoxIgnoreBooks.setSelected(false);
         jCheckBoxIgnoreBooks.setForeground(Color.BLACK);
         jTextFieldIgnoredBooks.setText("");

         if(script!=null)
         {
             HashMap<String,String>scriptOptions=script.getActiveOptions();
             if(scriptOptions.containsKey(Script.EXPAND_ALL)) {
                 jCheckBoxExpandAll.setSelected(scriptOptions.get(Script.EXPAND_ALL).compareTo(Script.YES)==0);
             }
             if(scriptOptions.containsKey(Script.USE_COPY)) {
                 jCheckBoxUseBackup.setSelected(scriptOptions.get(Script.USE_COPY).compareTo(Script.YES)==0);
             }
             if(scriptOptions.containsKey(Script.PREFORMAT_LANGUAGE)) {
                 jCheckBoxPreformatCode.setSelected(scriptOptions.get(Script.PREFORMAT_LANGUAGE).compareTo(Script.YES)==0);
             }
             if(scriptOptions.containsKey(Script.VERBOSE)) {
                 jCheckBoxVerbose.setSelected(scriptOptions.get(Script.VERBOSE).compareTo(Script.YES)==0);
             }

             if(scriptOptions.containsKey(Script.REFERENCE_INDEXING)) {
                 jComboBoxReferenceIndexing.setSelectedItem(scriptOptions.get(Script.REFERENCE_INDEXING));
             }
             if(scriptOptions.containsKey(Script.OUTPUT_FORMAT)) {
                 jComboBoxOutputFormat.setSelectedItem(scriptOptions.get(Script.OUTPUT_FORMAT));
             }
             if(scriptOptions.containsKey(Script.DEFAULT_ENCODING)) {
                 jComboBoxDefaultEncoding.setSelectedItem(scriptOptions.get(Script.DEFAULT_ENCODING));
             }
             if(scriptOptions.containsKey(Script.DROP_BOOKS))
             {
                 jCheckBoxIgnoreBooks.setSelected(true);
                 jTextFieldIgnoredBooks.setText(scriptOptions.get(Script.DROP_BOOKS));
             }
         }         
    }
    
    private HashMap<String,String> optionsCollectFromGUI(){
        HashMap<String,String>  res=new HashMap(29);
        if(jCheckBoxExpandAll.isSelected()) {
            res.put(Script.EXPAND_ALL, Script.YES);
        } 
        else {
            res.put(Script.EXPAND_ALL, Script.NO);
        }
        
        if(jCheckBoxUseBackup.isSelected()) {
            res.put(Script.USE_COPY, Script.YES);
        }
        else {
            res.put(Script.USE_COPY, Script.NO);
        }

        if(jCheckBoxPreformatCode.isSelected()) {
            res.put(Script.PREFORMAT_LANGUAGE, Script.YES);
        }
        else {
            res.put(Script.PREFORMAT_LANGUAGE, Script.NO);
        }
        
        if(jCheckBoxVerbose.isSelected()) {
            res.put(Script.VERBOSE, Script.YES);
        }
        else {
            res.put(Script.VERBOSE, Script.NO);
        }

        res.put(Script.OUTPUT_FORMAT,(String)jComboBoxOutputFormat.getSelectedItem());
        res.put(Script.DEFAULT_ENCODING,(String)jComboBoxDefaultEncoding.getSelectedItem());
        res.put(Script.REFERENCE_INDEXING,(String)jComboBoxReferenceIndexing.getSelectedItem());

        if(jCheckBoxIgnoreBooks.isSelected())
        {
            String CSVlist=jTextFieldIgnoredBooks.getText();
            if((CSVlist!=null)&&(CSVlist.length()>0)) {
                res.put(Script.DROP_BOOKS, CSVlist);
            }
            else {
                res.put(Script.DROP_BOOKS, Script.DROP_NONE);
            }
        }
        else {
            res.put(Script.DROP_BOOKS, Script.DROP_NONE);
        }
        
        return res; 
    }
    
    private void optionsUpdateColors(Script script){
        
         if(script==null) {
            return;
        }
         
         HashMap<String,String> scriptOptions=script.getActiveOptions();
         
         jCheckBoxExpandAll.setForeground(Color.BLACK);
            boolean userset=jCheckBoxExpandAll.isSelected();
            boolean scriptset=scriptOptions.containsKey(Script.EXPAND_ALL) && scriptOptions.get(Script.EXPAND_ALL).compareTo(Script.YES)==0;
            if( userset != scriptset){
                jCheckBoxExpandAll.setForeground(Color.RED);
            }
         
         jCheckBoxUseBackup.setForeground(Color.BLACK);
            userset=jCheckBoxUseBackup.isSelected();
            scriptset=scriptOptions.containsKey(Script.USE_COPY) && scriptOptions.get(Script.USE_COPY).compareTo(Script.YES)==0;
            if( userset != scriptset){
                jCheckBoxUseBackup.setForeground(Color.RED);
            }
         
         jCheckBoxPreformatCode.setForeground(Color.BLACK);
            userset=jCheckBoxPreformatCode.isSelected();
            scriptset=scriptOptions.containsKey(Script.PREFORMAT_LANGUAGE) && scriptOptions.get(Script.PREFORMAT_LANGUAGE).compareTo(Script.YES)==0;
            if( userset != scriptset){
                jCheckBoxPreformatCode.setForeground(Color.RED);
            }

         jCheckBoxVerbose.setForeground(Color.BLACK);
            userset=jCheckBoxVerbose.isSelected();
            scriptset=scriptOptions.containsKey(Script.VERBOSE) && scriptOptions.get(Script.VERBOSE).compareTo(Script.YES)==0;
            if( userset != scriptset){
                jCheckBoxVerbose.setForeground(Color.RED);
            }

         jLabeOutputFormat.setForeground(Color.black);
            String userChoice=(String)jComboBoxOutputFormat.getSelectedItem();
            String scriptChoice=scriptOptions.get(Script.OUTPUT_FORMAT);
            if(!userChoice.equals(scriptChoice)){
                jLabeOutputFormat.setForeground(Color.RED);
            }
        

         jLabelRefIndexing.setForeground(Color.BLACK);
            userChoice=(String)jComboBoxReferenceIndexing.getSelectedItem();
            scriptChoice=scriptOptions.get(Script.REFERENCE_INDEXING);
            if(!userChoice.equals(scriptChoice)){
               jLabelRefIndexing.setForeground(Color.RED);
            }

        
         jLabelDefaultEncoding.setForeground(Color.BLACK);
            userChoice=(String)jComboBoxDefaultEncoding.getSelectedItem();
            scriptChoice=scriptOptions.get(Script.DEFAULT_ENCODING);
            if(!userChoice.equalsIgnoreCase(scriptChoice)){
               jLabelDefaultEncoding.setForeground(Color.RED);
            }
         
         jCheckBoxIgnoreBooks.setForeground(Color.BLACK);
            userset=jCheckBoxIgnoreBooks.isSelected();
            userChoice=jTextFieldIgnoredBooks.getText();
            if(scriptOptions.containsKey(Script.DROP_BOOKS)){
                scriptChoice=scriptOptions.get(Script.DROP_BOOKS);
                if((!userset) || (!userChoice.equals(scriptChoice))) {
                    jCheckBoxIgnoreBooks.setForeground(Color.RED);
                }
            }
            else if(userset) {
            jCheckBoxIgnoreBooks.setForeground(Color.RED);
        }
        
    }
     
    //----------------------  eof options
    
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                new FaceGui().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBuild;
    private javax.swing.JButton jButtonOptionExplain;
    private javax.swing.JButton jButtonShowList;
    private javax.swing.JCheckBox jCheckBoxExpandAll;
    private javax.swing.JCheckBox jCheckBoxIgnoreBooks;
    private javax.swing.JCheckBox jCheckBoxPreformatCode;
    private javax.swing.JCheckBox jCheckBoxUseBackup;
    private javax.swing.JCheckBox jCheckBoxVerbose;
    private javax.swing.JComboBox jComboBoxDefaultEncoding;
    private javax.swing.JComboBox jComboBoxOutputFormat;
    private javax.swing.JComboBox jComboBoxReferenceIndexing;
    private javax.swing.JLabel jLabeOutputFormat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelDefaultEncoding;
    private javax.swing.JLabel jLabelRefIndexing;
    private javax.swing.JLabel jLabelWXTEngine;
    private javax.swing.JList jListScripts;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemClearScriptlist;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemFindWXT;
    private javax.swing.JMenuItem jMenuItemGUIHelp;
    private javax.swing.JMenuItem jMenuItemInPopAddScript;
    private javax.swing.JMenuItem jMenuItemInPopRemoveScript;
    private javax.swing.JMenuItem jMenuItemLoadScript;
    private javax.swing.JMenuItem jMenuItemModuleList;
    private javax.swing.JMenuItem jMenuItemRemoveSelected;
    private javax.swing.JMenuItem jMenuItemVersions;
    private javax.swing.JMenuItem jMenuItemWXTHelp;
    private javax.swing.JMenu jMenuTools;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelMessage;
    private javax.swing.JPanel jPanelModules;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JPanel jPanelScripts;
    private javax.swing.JPopupMenu jPopupMenuInScriptList;
    private javax.swing.JProgressBar jProgressBarWorking;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSplitPane jSplitPaneHBig;
    private javax.swing.JSplitPane jSplitPaneRight;
    private javax.swing.JSplitPane jSplitPaneVerticalMain;
    private javax.swing.JTextField jTextFieldIgnoredBooks;
    private javax.swing.JTextPane jTextPaneMainMessage;
    private javax.swing.JTree jTreeModules;
    // End of variables declaration//GEN-END:variables

    

}
