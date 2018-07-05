/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * optionHelp.java
 *
 * Created on 11.jul.2009, 12:05:48
 */

package wxt4run;

/**
 *
 * @author Administrator
 */
public class optionHelp extends javax.swing.JDialog {

    /** Creates new form optionHelp */
    public optionHelp(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        String T="<html><div style=\"margin-left:10px\""
    +"<h3>Indent Output</h3>"
    +"<p>If selected, you will get output code which are easier to read,<br>"
    +"but it may cause problems with PDF-production.</p>";
        T=T+"<h3>Expand All</h3>"
    +"<p>If selected, all Ajax expansions will be done, regardless of the<br> "
    +"attribute expanded in the expand PI.</p>";
        T=T+"<h3>Use Backup</h3>"
    +"<p>If selected WXT will import wiki and odt material from a local copy.<br>"
	+"Otherwise WXT will access the material at the original source, produce a <br>"
	+"local copy and fetch the result.<br>"
    +"Overriding the use-copy attribute in PI importwiki and PI importodt</p>";
        T=T+"<h3>Output Format</h3>"
    +"<p>Possible values are <em>xml</em>, <em>text</em>,<em>html</em> and <em>xhtml</em>. Default is <em>html</em>.<br>"
    +"This will produce modules <strong>without</strong> xml-header in a format that is safe for most browsers. <br>"
    +"<em>xhtml</em> will produce the same as <em>xml</em>,"
	+"but without xml-header.</p>";
        T=T+"<h3>Default Encoding</h3>"
    +"<p>Default is <em>utf-8</em>. This encoding is used<br>"
    +"when there are no other way to decide encoding.</p>";
        T=T+"<h3>Preformat Languges</h3>"
    +"<p>Possible values are <em>yes</em>, <em>no</em>.<br>"
	+"Default is <em>no</em>. Google's prettyprint will be used.<br> "
        +"<em>yes</em> wil turn on WXT's own colorcoding which is less flexible but will prepare<br>"
        + "material independant of webbrowser. Suited for PDF conversion.</p>";
        T=T+"<h3>Reference Indexing</h3>"
    +"<p>Possible values are <em>local</em>, <em>global</em>.<br>"
	+"<em>local</em> will produce reference indices for each module.<br>"
	+"<em>global</em> will index references as encountered during the<br> "
	+"whole building. Default is <em>local</em>.</p>";
        T=T+"<h3>Drop Books</h3>"
    +"<p>Modules with the given books are excluded from build, even if they are selected<br>"
    +"Usefull when you are building scripts with timeconsuming, low requency modules<br>"
    +"The books - value may be a commaseparated list.";
        T=T+"<h3>Verbose</h3>"
    +"<p>The building report will contain more information of empty and failed constructs<br>"
    +"Default is off</p></div>";
        jLabel2.setText(T);


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(wxt4run.wxt4runApp.class).getContext().getResourceMap(optionHelp.class);
        jScrollPane1.setBackground(resourceMap.getColor("jScrollPane1.background")); // NOI18N
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jLabel2.setBackground(resourceMap.getColor("jLabel2.background")); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jLabel2.setOpaque(true);
        jScrollPane1.setViewportView(jLabel2);

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO add your handling code here:
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButton1MouseClicked

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                optionHelp dialog = new optionHelp(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
