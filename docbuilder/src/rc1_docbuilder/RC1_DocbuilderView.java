/*
 * RC1_DocbuilderView.java
 */
package rc1_docbuilder;

import java.awt.Color;
import java.awt.Font;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import static javax.swing.JOptionPane.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.bounce.text.xml.XMLDocument;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;
import org.w3c.dom.Document;
import rc1_docbuilder.content.application;
import rc1_docbuilder.content.Wikibuilder;
import rc1_docbuilder.utils.domer;
import com.princexml.*;
import java.awt.Cursor;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import rc1_docbuilder.content.WikiImages;
import rc1_docbuilder.content.odtbuilder;

/**
 * The application's main frame.
 */
public class RC1_DocbuilderView extends FrameView {


    //xml directory
    String g_xml_directory = "Documents/xml_documents";

    //html directory
    String g_html_directory = "Documents/html_documents";

    //pdf directory
    String g_pdf_directory = "Documents/pdf_documents";

    //the filename of document working with
    String g_filename = null;

    //the filename to be saved
    String g_savefile_name = null;

    //the filename used for saving as html document
    String g_savefile_name_html = null;

    //the filename used for saving as pdf document
    String g_savefile_name_pdf = null;

    //the url of source
    String g_url_adress = null;

    //global variable for jtree
    String g_headline = null;
    //holds the caret position
    int pos;
    String mode = ""; // Holder styr på om vi tryker på odt eller wiki knappen

    public RC1_DocbuilderView(SingleFrameApplication app) {
        super(app);

        initComponents();

        jPanel9.setVisible(false);











        // this.getFrame().setResizable(false);
        this.getFrame().setResizable(false);
        this.getFrame().setTitle("Docbuilder RC1");





        jEditorPane1.setEditable(true);

        // jEditorPane1.setText(application.readToTextArea("doctemplate.xml"));

        // Instantiate a XMLEditorKit with wrapping enabled.
        XMLEditorKit kit = new XMLEditorKit(true);

        // Set the wrapping style.
        kit.setWrapStyleWord(true);

        jEditorPane1.setEditorKit(kit);

        // Set the font style.
        jEditorPane1.setFont(new Font("Courier", Font.PLAIN, 12));

        // Set the tab size
        jEditorPane1.getDocument().putProperty(PlainDocument.tabSizeAttribute,
                new Integer(4));

        // Enable auto indentation.
        jEditorPane1.getDocument().putProperty(XMLDocument.AUTO_INDENTATION_ATTRIBUTE,
                new Boolean(true));

        // Enable tag completion.
        jEditorPane1.getDocument().putProperty(XMLDocument.TAG_COMPLETION_ATTRIBUTE,
                new Boolean(true));

        kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0),
                Font.BOLD);

        kit.setStyle(XMLStyleConstants.ATTRIBUTE_VALUE, new Color(153, 153, 255),
                Font.BOLD);



        kit.setStyle(XMLStyleConstants.ELEMENT_NAME, new Color(0, 0, 255), Font.BOLD);

        //only choose single from tree
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);


        jEditorPane1.setText(application.readText("doctemplate.xml"));










        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

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
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = RC1_DocbuilderApp.getApplication().getMainFrame();
            aboutBox = new RC1_DocbuilderAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        RC1_DocbuilderApp.getApplication().show(aboutBox);
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
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel11 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jFileChooser1 = new javax.swing.JFileChooser();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rc1_docbuilder.RC1_DocbuilderApp.class).getContext().getResourceMap(RC1_DocbuilderView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainPanelMouseClicked(evt);
            }
        });

        jLayeredPane1.setBackground(resourceMap.getColor("jLayeredPane1.background")); // NOI18N
        jLayeredPane1.setName("jLayeredPane1"); // NOI18N

        jPanel1.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setForeground(resourceMap.getColor("jLabel1.foreground")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel12.setIcon(resourceMap.getIcon("jLabel12.icon")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setToolTipText(resourceMap.getString("jLabel12.toolTipText")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel12MouseClicked(evt);
            }
        });
        jLabel12.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel12MouseMoved(evt);
            }
        });

        jLabel11.setIcon(resourceMap.getIcon("jLabel11.icon")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setToolTipText(resourceMap.getString("jLabel11.toolTipText")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel11MouseClicked(evt);
            }
        });
        jLabel11.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel11MouseMoved(evt);
            }
        });

        jLabel10.setIcon(resourceMap.getIcon("jLabel10.icon")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setToolTipText(resourceMap.getString("jLabel10.toolTipText")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel10MouseClicked(evt);
            }
        });
        jLabel10.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel10MouseMoved(evt);
            }
        });

        jLabel9.setIcon(resourceMap.getIcon("jLabel9.icon")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setToolTipText(resourceMap.getString("jLabel9.toolTipText")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        jLabel9.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel9MouseMoved(evt);
            }
        });
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });

        jLabel16.setIcon(resourceMap.getIcon("jLabel16.icon")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setToolTipText(resourceMap.getString("jLabel16.toolTipText")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel16MouseClicked(evt);
            }
        });
        jLabel16.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel16MouseMoved(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(19, 19, 19)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(42, 42, 42)
                .add(jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel12)
                .add(468, 468, 468)
                .add(jLabel9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel16)
                .add(25, 25, 25))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel16)))
                .addContainerGap())
        );

        jPanel1.setBounds(10, 10, 890, 50);
        jLayeredPane1.add(jPanel1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jEditorPane1.setName("jEditorPane1"); // NOI18N
        jEditorPane1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jEditorPane1CaretUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(jEditorPane1);

        jScrollPane1.setBounds(210, 70, 690, 530);
        jLayeredPane1.add(jScrollPane1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jPanel2.setBackground(resourceMap.getColor("jPanel2.background")); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jPanel3.setBackground(resourceMap.getColor("jPanel3.background")); // NOI18N
        jPanel3.setForeground(resourceMap.getColor("jPanel3.foreground")); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setForeground(resourceMap.getColor("jLabel2.foreground")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(54, 54, 54)
                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        jButton1.setBackground(resourceMap.getColor("jButton1.background")); // NOI18N
        jButton1.setForeground(resourceMap.getColor("jButton1.foreground")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(resourceMap.getColor("jButton2.background")); // NOI18N
        jButton2.setForeground(resourceMap.getColor("jButton2.foreground")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setIcon(resourceMap.getIcon("jLabel3.icon")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButton1)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jButton2)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel2.setBounds(10, 70, 180, 120);
        jLayeredPane1.add(jPanel2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jPanel4.setBackground(resourceMap.getColor("jPanel4.background")); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel5.setForeground(resourceMap.getColor("jLabel5.foreground")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(211, 211, 211)
                .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(175, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
        );

        jPanel4.setBounds(390, 620, 510, 20);
        jLayeredPane1.add(jPanel4, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jPanel5.setBackground(resourceMap.getColor("jPanel5.background")); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        jLabel6.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(resourceMap.getString("jLabel6.toolTipText")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });
        jLabel6.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel6MouseMoved(evt);
            }
        });

        jLabel7.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setToolTipText(resourceMap.getString("jLabel7.toolTipText")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });
        jLabel7.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel7MouseMoved(evt);
            }
        });

        jLabel8.setIcon(resourceMap.getIcon("jLabel8.icon")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setToolTipText(resourceMap.getString("jLabel8.toolTipText")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });
        jLabel8.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel8MouseMoved(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(45, 45, 45)
                .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(47, 47, 47)
                .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 194, Short.MAX_VALUE)
                .add(jLabel8)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(jLabel6)
                    .add(jLabel7))
                .add(63, 63, 63))
        );

        jPanel5.setBounds(390, 640, 510, 100);
        jLayeredPane1.add(jPanel5, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jPanel9.setBackground(resourceMap.getColor("jPanel9.background")); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N

        jPanel8.setBackground(resourceMap.getColor("jPanel8.background")); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        jLabel13.setFont(resourceMap.getFont("jLabel13.font")); // NOI18N
        jLabel13.setForeground(resourceMap.getColor("jLabel13.foreground")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(46, 46, 46)
                .add(jLabel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(65, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jLabel13)
                .addContainerGap())
        );

        jButton6.setBackground(resourceMap.getColor("jButton6.background")); // NOI18N
        jButton6.setForeground(resourceMap.getColor("jButton6.foreground")); // NOI18N
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setBackground(resourceMap.getColor("jButton7.background")); // NOI18N
        jButton7.setForeground(resourceMap.getColor("jButton7.foreground")); // NOI18N
        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setName("jButton7"); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTree1.setName("jTree1"); // NOI18N
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jTree1);

        jCheckBox1.setBackground(resourceMap.getColor("jCheckBox1.background")); // NOI18N
        jCheckBox1.setForeground(resourceMap.getColor("jCheckBox1.foreground")); // NOI18N
        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        jCheckBox2.setBackground(resourceMap.getColor("jCheckBox2.background")); // NOI18N
        jCheckBox2.setForeground(resourceMap.getColor("jCheckBox2.foreground")); // NOI18N
        jCheckBox2.setSelected(true);
        jCheckBox2.setText(resourceMap.getString("jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
            .add(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBox1)
                .addContainerGap(103, Short.MAX_VALUE))
            .add(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBox2)
                .addContainerGap(89, Short.MAX_VALUE))
            .add(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .add(44, 44, 44))
            .add(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .add(44, 44, 44))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 247, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 11, Short.MAX_VALUE)
                .add(jButton6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton7)
                .add(5, 5, 5))
        );

        jPanel9.setBounds(10, 200, 180, 410);
        jLayeredPane1.add(jPanel9, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jPanel11.setBackground(resourceMap.getColor("jPanel11.background")); // NOI18N
        jPanel11.setName("jPanel11"); // NOI18N

        jLabel15.setForeground(resourceMap.getColor("jLabel15.foreground")); // NOI18N
        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .add(138, 138, 138)
                .add(jLabel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(142, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
        );

        jPanel11.setBounds(10, 620, 360, 20);
        jLayeredPane1.add(jPanel11, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jPanel10.setBackground(resourceMap.getColor("jPanel10.background")); // NOI18N
        jPanel10.setName("jPanel10"); // NOI18N

        jLabel14.setIcon(resourceMap.getIcon("jLabel14.icon")); // NOI18N
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextPane1.setName("jTextPane1"); // NOI18N
        jTextPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextPane1MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTextPane1);

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel10.setBounds(10, 640, 360, 100);
        jLayeredPane1.add(jPanel10, javax.swing.JLayeredPane.DEFAULT_LAYER);

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jLayeredPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 919, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .add(jLayeredPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 753, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        menuBar.setBackground(resourceMap.getColor("menuBar.background")); // NOI18N
        menuBar.setForeground(resourceMap.getColor("menuBar.foreground")); // NOI18N
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setBackground(resourceMap.getColor("fileMenu.background")); // NOI18N
        fileMenu.setForeground(resourceMap.getColor("fileMenu.foreground")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N
        fileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem2);

        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem3);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(rc1_docbuilder.RC1_DocbuilderApp.class).getContext().getActionMap(RC1_DocbuilderView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setBackground(resourceMap.getColor("jMenu1.background")); // NOI18N
        jMenu1.setForeground(resourceMap.getColor("jMenu1.foreground")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        menuBar.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem6);

        menuBar.add(jMenu2);

        helpMenu.setBackground(resourceMap.getColor("helpMenu.background")); // NOI18N
        helpMenu.setForeground(resourceMap.getColor("helpMenu.foreground")); // NOI18N
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 939, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 769, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );

        jFileChooser1.setName("jFileChooser1"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        //Button for prince pdf converter


        jLabel8MouseClicked(null);

        try {


            //file containing prince path
            String prince_konf = "Prince_konf/prince.txt";

            //reading path from file
            String prince_path = application.readPrincePath(prince_konf);


            //replacing \ with \\
            String test = prince_path.replaceAll("\\\\", "\\\\\\\\");


            //System.out.println("Resultat etterpå:"+test);

            //trim text to avoid error when searching if file exists
            File n = new File(test.trim());

            //checking if prince.exe exists
            if (n.exists() == true) {



                //System.out.println("Filen fantes.....");
                Prince p = new Prince(test);



                // Bytter om på filstiene
                String tmpFile="Documents"+File.separator+"tmp" +File.separator+"html2pdf.tmp";
                Document tmp = domer.makeDomFromUri(new URI(g_html_directory + "/" + g_savefile_name_html));

                // Bytter ut img src
                WikiImages.switchToFullPathNames(tmp);

                // Lagrer et annet midlertidig document
                Wikibuilder.saveDOM(tmp, new File(tmpFile));


                FileInputStream fis = new FileInputStream(tmpFile);
                if (new File(g_pdf_directory).exists() == false) {

                    //create directory
                    new File(g_pdf_directory).mkdir();
                }


                FileOutputStream fos = new FileOutputStream(g_pdf_directory + "/" + g_savefile_name_pdf);

                p.addStyleSheet("template.css");
                p.convert(fis, fos);
                fis.close();
                fos.close();



                try {

                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + g_pdf_directory + "/" + g_savefile_name_pdf);

                } catch (Exception e) {
                    System.out.println("Error" + e);
                }
            } else {
                String path = showInputDialog(null, "Could not find path for Prince, please type in your path and try again:");
                if (path != null && (new File(path).exists())) {
                    //prince_path=path;
                    application.saveTextToFile(path, "prince.txt", "Prince_konf");
                } else {
                    jTextPane1.setText("Error with Prince, make sure that you have Prince installed and your path is correct");

                }

            }



        } catch (Exception e) {
            System.out.println("Error with converting to pdf");
            e.printStackTrace();
        }



    }//GEN-LAST:event_jLabel6MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Button for wikipage


        try {
            g_url_adress = showInputDialog(null, "URI of wikipage: ");
            if (!(g_url_adress.equals(""))) {

                Document doc = null;
                DefaultMutableTreeNode root = null;
                try {

                    //making dom from uri
                    doc = domer.makeDomFromUri(new URI(g_url_adress));

                    //creating the node of the tree
                    root = Wikibuilder.generateTree(doc);

                } catch (Exception e) {
                    System.out.println("Oh oh somethings is wrong " + e.getMessage());
                    e.printStackTrace();
                }











                //create the treemodel
                DefaultTreeModel treemodel = new DefaultTreeModel(root);


                //set model for JTree
                //jTree1.setModel(treemodel);
                jTree1.setModel(treemodel);


                jPanel9.setVisible(true);
                jTextPane1.setText("Wikipage successfully opened.\nPlease choose from the headlines to add.");

                mode = "wiki";
            }

        } catch (NullPointerException npe) {
            // JOptionPane.showMessageDialog(null, "No wikipage selected !");
            jTextPane1.setText("No wikipage selected!\n Try again and type url adress of wikipage");
            jPanel9.setVisible(false);

        }






    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        //button for odt....

        //opening filechooser in current directory as program...
        jFileChooser1.setCurrentDirectory(new File("."));

        //only allowing to choose odt files to open....
        FileNameExtensionFilter fex = new FileNameExtensionFilter("odt files", "odt");

        //adding and setting filefilter for filechooser..
        jFileChooser1.addChoosableFileFilter(fex);
        jFileChooser1.setFileFilter(fex);

        jFileChooser1.showOpenDialog(null);

        try {
            g_url_adress = jFileChooser1.getSelectedFile().toString();
            //g_url_adress=JOptionPane.showInputDialog(null,"Destination of odt file: ");
            if (!(g_url_adress.equals(""))) {

                Document doc = null;
                DefaultMutableTreeNode root = null;

                try {

                    odtbuilder odt = new odtbuilder();
                    String new_odt_url = odt.transformToWiki(g_url_adress);
                    doc = domer.makeDomFromUri(new URI(new_odt_url));

                    root = Wikibuilder.generateTree(doc);

                } catch (Exception e) {
                    System.out.println("Oh oh somethings is wrong " + e.getMessage());
                    e.printStackTrace();
                }


                //create the treemodel
                DefaultTreeModel treemodel = new DefaultTreeModel(root);


                //set model for JTree
                jTree1.setModel(treemodel);



                jPanel9.setVisible(true);
                jTextPane1.setText("Odt file successfully opened.\nPlease choose from the headlines to add.");

                mode = "odt";
            }

        } catch (NullPointerException npe) {
            // JOptionPane.showMessageDialog(null, "No wikipage selected !");
            jTextPane1.setText("No odt file selected!\n Try again and choose destination of your file");
            jPanel9.setVisible(false);

        }






    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        jPanel9.setVisible(false);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jEditorPane1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jEditorPane1CaretUpdate

        //updating caret...
        pos = jEditorPane1.getCaretPosition();
    }//GEN-LAST:event_jEditorPane1CaretUpdate

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        //Button for adding PI's to document

        try {
            /*jEditorPane1.setCaretPosition(pos);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            jTree1.getLastSelectedPathComponent();

            if (node.isLeaf() || node.isNodeDescendant(node))
            {*/


            String PI = "";

            if (mode.equals("wiki")) {
                PI = "<div>\n<?Document importwiki::";
            } else if (mode.equals("odt")) {
                PI = "<div>\n<?Document importodt::";

            }



            PI += g_headline;

            //adding url of source
            PI += "::" + g_url_adress;

            if (jCheckBox1.isSelected()) {
                PI += "::Keeplinks:yes";
            } else {
                PI += "::Keeplinks:no";
            }

            if (jCheckBox2.isSelected()) {
                PI += "::DownloadImg:yes";
            } else {
                PI += "::DownloadImg:no";
            }


            PI += "?>\n</div>\n";
            jEditorPane1.getDocument().insertString(pos, PI, null);










        } catch (BadLocationException ble) {

            jTextPane1.setText("Could not insert text into editorpanel");
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jTextPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPane1MouseClicked

        // jTextPane1.setText(null);
    }//GEN-LAST:event_jTextPane1MouseClicked

    private void mainPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainPanelMouseClicked
        jTextPane1.setText(null);
    }//GEN-LAST:event_mainPanelMouseClicked

    private void jLabel10MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseMoved
        //mousemoved event for open icon, setting hand cursor

        this.jLabel10.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel10MouseMoved

    private void jLabel11MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseMoved
        //mousemoved event for save icon, setting hand cursor
        this.jLabel11.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel11MouseMoved

    private void jLabel12MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseMoved
        //mousemoved event for save as icon, setting hand cursor
        this.jLabel12.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel12MouseMoved

    private void jLabel10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseClicked
        //open a document and send it to editorpanel.


        //opening filechooser in current directory as program...
        jFileChooser1.setCurrentDirectory(new File("."));

        //only allowing to choose xml files to open....
        FileNameExtensionFilter f = new FileNameExtensionFilter("xml files", "xml");

        //adding and setting filefilter for filechooser..
        jFileChooser1.addChoosableFileFilter(f);
        jFileChooser1.setFileFilter(f);

        try {
            //show filechooser
            jFileChooser1.showOpenDialog(null);

            //get name of opened file and update g_savefile_name
            g_filename = jFileChooser1.getSelectedFile().getName().toString();
            g_savefile_name = g_filename;

            //getting filepath and reading file to editorpanel
            String filepath = jFileChooser1.getSelectedFile().toString();
            jEditorPane1.setText(application.readText(filepath));

            //Updating information
            jTextPane1.setText("File successfully opened.");

        } catch (NullPointerException npe) {

            jTextPane1.setText("No file selected to read!");

        }
    }//GEN-LAST:event_jLabel10MouseClicked

    private void jLabel11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseClicked
        //saving text to same file as opened

        //icons for menu
        ImageIcon icon = new ImageIcon("src\\rc1_docbuilder\\resources\\save_success.jpg");
        ImageIcon icon2 = new ImageIcon("src\\rc1_docbuilder\\resources\\save_error.jpg");

        try {

            //checking that user dont try to save to doctemplate file used as standard template
            if (!g_filename.equalsIgnoreCase("doctemplate.xml")) {
                //saving text to file, giving information and updating icons for menu
                application.saveTextToFile(jEditorPane1.getText(), g_filename, g_xml_directory);
                jTextPane1.setText("Successfully saved to the file: " + g_filename);
                this.jLabel11.setIcon(icon);
            } else {

                jTextPane1.setText("Choose a filename first and try again.");
                this.jLabel11.setIcon(icon2);
            }


        } catch (NullPointerException npe) {
            //telling user to use the button Save As first
            jTextPane1.setText("No filename selected to save.\nUse the button Save As first.");

            icon = new ImageIcon("src\\rc1_docbuilder\\resources\\save_error.jpg");
            this.jLabel11.setIcon(icon);
        }
    }//GEN-LAST:event_jLabel11MouseClicked

    private void jLabel12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseClicked
        //save text from editorpanel to a new document....


        //checking if directory for xml files exists
        if (new File(g_xml_directory).exists() == false) {

            //create directory
            new File(g_xml_directory).mkdir();
        }



        //icons for menu
        ImageIcon icon = new ImageIcon("src\\rc1_docbuilder\\resources\\save_as_success.jpg");
        ImageIcon icon2 = new ImageIcon("src\\rc1_docbuilder\\resources\\save.jpg");
        ImageIcon icon3 = new ImageIcon("src\\rc1_docbuilder\\resources\\save_as_error.jpg");
        try {
            //name of file to save
            g_savefile_name = showInputDialog(null, "Name of xml file to be saved:");
            if (g_savefile_name.endsWith(".xml")) {

                //splitting filename
                String[] input = g_savefile_name.split("\\.");
                g_savefile_name_html = input[0].toString() + ".html";
                g_savefile_name_pdf = input[0].toString() + ".pdf";


            } else {
                g_savefile_name_html = g_savefile_name + ".html";
                g_savefile_name_pdf = g_savefile_name + ".pdf";
                g_savefile_name += ".xml";
            }


            //updating g_filename used for appending to the same file
            g_filename = g_savefile_name;


            //checking that textfield for filename is not empty
            if (!(g_savefile_name.equals(""))) {
                //path and filename for saving document
                File f = new File(g_xml_directory + "/" + g_savefile_name);


                //if file dont exists
                if (!(f.exists()) && !(g_savefile_name.equalsIgnoreCase("doctemplate.xml"))) {
                    //saving text to file
                    application.saveTextToFile(jEditorPane1.getText(), g_savefile_name, g_xml_directory);

                    //updating menu icons
                    this.jLabel12.setIcon(icon);
                    this.jLabel11.setIcon(icon2);

                } else {
                    //notifying user that selected filename exists
                    g_savefile_name = showInputDialog(null, "That file already exists, you want to overwrite?", g_savefile_name);

                    if (!g_savefile_name.equalsIgnoreCase("doctemplate.xml")) {
                        //saving text and updating menu icons
                        application.saveTextToFile(jEditorPane1.getText(), g_savefile_name, g_xml_directory);
                        this.jLabel12.setIcon(icon);
                        this.jLabel11.setIcon(icon2);
                    } else {
                        //information about saving file failed
                        jTextPane1.setText("Unable to save to that filename.Choose another filename and try again.");
                        this.jLabel12.setIcon(icon3);
                    }


                }

            }
        } catch (NullPointerException npe) {
            //information if no filename selected
            jTextPane1.setText("No filename selected, unable to save...");
            this.jLabel12.setIcon(icon3);


        }
    }//GEN-LAST:event_jLabel12MouseClicked

    private void jLabel6MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseMoved
        this.jLabel6.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel6MouseMoved

    private void jLabel7MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseMoved
        this.jLabel7.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel7MouseMoved

    private void jLabel8MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseMoved
        this.jLabel8.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel8MouseMoved

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        //Button for viewing finished document in webbrowser

        jLabel8MouseClicked(null);

        try {
            File f = new File(g_html_directory + "/" + g_savefile_name_html);


            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + f);

        // jTextPane1.setText("Document is opened in your web browser.");


        } catch (IOException ioe) {

            jTextPane1.setText("Error opening document as xhtml.");
        }
    }//GEN-LAST:event_jLabel7MouseClicked

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        //Button for building document


        //checking if xml directory dont exists
        if (new File(g_xml_directory).exists() == false) {

            //create directory
            new File(g_xml_directory).mkdir();
        }
        //checking if html directory dont exists
        if (new File(g_html_directory).exists() == false) {

            //create directory
            new File(g_html_directory).mkdir();
        }


        try {
            //checking that filename is not null and not equal doctemplate.xml
            if (g_savefile_name != null && !(g_savefile_name.equalsIgnoreCase("doctemplate.xml"))) {


                //saving text from editorpanel to file
                application.saveTextToFile(jEditorPane1.getText(), g_savefile_name, g_xml_directory);

                //making DOM object from saved file
                Document master = domer.makeDomFromString(Wikibuilder.readFile(new File(g_xml_directory + "/" + g_savefile_name), "UTF-8"));

                //replacing all PI's
                Wikibuilder.generateDocument(master, "Document");

                //saving the result to an html file
                Wikibuilder.saveDOM(master, new File(g_html_directory + "/" + g_savefile_name_html));

                g_filename = g_savefile_name;

                //updating information
                jTextPane1.setText("Building document finished.\nPlease view it as xhtml or pdf. ");


            } else {
                //inputdialog for saving document
                g_savefile_name = showInputDialog(null, "You have to save your document first, type filename:");

                if (g_savefile_name.endsWith(".xml")) {

                    //splitting filename if ending with .xml
                    String[] input = g_savefile_name.split("\\.");
                    g_savefile_name_html = input[0].toString() + ".html";
                    g_savefile_name_pdf = input[0].toString() + ".pdf";

                } else if (!(g_savefile_name.equals("")) && g_savefile_name != null) {
                    g_savefile_name_html = g_savefile_name + ".html";
                    g_savefile_name_pdf = g_savefile_name + ".pdf";
                    g_savefile_name += ".xml";

                    System.out.println("Resultat hvis den feiler testen:" + g_savefile_name + "::::::" + g_savefile_name + ":::" + g_savefile_name_pdf);
                } else {


                    g_savefile_name = null;
                    System.out.println("Du har klart å komme hit og referansen for filename er lik null");
                }



                //checking that filename is not equal doctemplate.xml and that file already not exists
                if (!(g_savefile_name.equalsIgnoreCase("doctemplate.xml")) && !(new File(g_xml_directory + "/" + g_savefile_name).exists()) && g_savefile_name != null) {
                    //saving text from editorpanel to file
                    application.saveTextToFile(jEditorPane1.getText(), g_savefile_name, g_xml_directory);

                    //making DOM object of saved file
                    Document master = domer.makeDomFromString(Wikibuilder.readFile(new File(g_xml_directory + "/" + g_savefile_name), "UTF-8"));

                    //replacing PI tags
                    Wikibuilder.generateDocument(master, "Document");

                    //saving finished result as html file
                    Wikibuilder.saveDOM(master, new File(g_html_directory + "/" + g_savefile_name_html));

                    //updating g_filename and information
                    g_filename = g_savefile_name;
                    jTextPane1.setText("Building document finished.\nPlease view it as xhtml or pdf. ");
                } //checking that filename is not equal doctemplate.xml and filename exists
                else if (!(g_savefile_name.equalsIgnoreCase("doctemplate.xml")) && (new File(g_xml_directory + "/" + g_savefile_name).exists()) && g_savefile_name != null) {
                    //asking for permission to overwrite file that exists
                    g_savefile_name = showInputDialog(null, "That file already existst, you want to overwrite?", g_savefile_name);

                    if (g_savefile_name != null) {
                        //saving text from editorpanel to file
                        application.saveTextToFile(jEditorPane1.getText(), g_savefile_name, g_xml_directory);

                        //making DOM object of saved file
                        Document master = domer.makeDomFromString(Wikibuilder.readFile(new File(g_xml_directory + "/" + g_savefile_name), "UTF-8"));

                        //replacing PI tags
                        Wikibuilder.generateDocument(master, "Document");

                        //saving finished result as html file
                        Wikibuilder.saveDOM(master, new File(g_html_directory + "/" + g_savefile_name_html));

                        g_filename = g_savefile_name;
                        jTextPane1.setText("Building document finished.\nPlease view it as xhtml or pdf. ");
                    } else {
                        jTextPane1.setText("Error building document.\nNo filename selected. ");
                    }
                }
            }
        } /*catch (NullPointerException npe) {
            jTextPane1.setText("Error replacing PI tags.\nReference to null.Building docuement failed.");
        } */catch (Exception e) {

            jTextPane1.setText("Error building document.\nMake sure that all tags are closed correctly and your document is wellformed.\nCheck log.txt for more information");
            try
            {

                  BufferedWriter bw = new BufferedWriter(new FileWriter("log.txt", false));
                  bw.write("Error log for Docbuilder");
                  bw.newLine();
                java.text.DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                java.util.Date date = new Date();
                String dt = dateFormat.format(date);
                bw.write(dt);
                bw.newLine();
                bw.write(e.getMessage());
                bw.flush();
                bw.close();
            }
            catch(IOException ioe)
            {

            }

            System.out.println("Feil" + e.getMessage());

        }
    }//GEN-LAST:event_jLabel8MouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        //menu for opening a document
        jLabel10MouseClicked(null);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        //menu for saving to same document
        jLabel11MouseClicked(null);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        //menu for saving to another document
        jLabel12MouseClicked(null);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        jLabel6MouseClicked(null);
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jLabel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseClicked
        jLabel7MouseClicked(null);
    }//GEN-LAST:event_jLabel16MouseClicked

    private void jLabel9MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseMoved
        this.jLabel9.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel9MouseMoved

    private void jLabel16MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseMoved
        this.jLabel16.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel16MouseMoved

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        jLabel7MouseClicked(null);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        jLabel6MouseClicked(null);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        // TODO add your handling code here:

        TreePath path = evt.getPath();
        Object[] ob = path.getPath();
        if (ob.length == 1) {
            g_headline = ob[0].toString();
        }
        if (ob.length == 2) {
            g_headline = ob[0].toString() + "," + ob[1].toString();
        }
        if (ob.length == 3) {
            g_headline = ob[0].toString() + "," + ob[1].toString() + "," + ob[2].toString();
        }
        if (ob.length == 4) {
            g_headline = ob[0].toString() + "," + ob[1].toString() + "," + ob[2].toString() + "," + ob[3].toString();
        }
        if (ob.length == 5) {
            g_headline = ob[0].toString() + "," + ob[1].toString() + "," + ob[2].toString() + "," + ob[3].toString() + "," + ob[4].toString();
        }
        if (ob.length == 6) {
            g_headline = ob[0].toString() + "," + ob[1].toString() + "," + ob[2].toString() + "," + ob[3].toString() + "," + ob[4].toString() + "," + ob[5].toString();
        }





    }//GEN-LAST:event_jTree1ValueChanged

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fileMenuActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        //application.deleteDirectory(new File("C:\\Documents and Settings\\HP_Eier\\Mine dokumenter\\My Dropbox\\Siste RC1\\RC1_Docbuilder\\RC1_Docbuilder\\Documents\\xml_documents"));
        application.deleteDirectory(new File(g_xml_directory));
        application.deleteDirectory(new File(g_html_directory));
        application.deleteDirectory(new File(g_pdf_directory));
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
