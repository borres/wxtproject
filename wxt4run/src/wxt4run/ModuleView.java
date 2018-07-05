/*
 * ModuleView.java
 *
 * Created on 19. januar 2009, 16:38
 */

package wxt4run;

import java.net.URI;
import wxt4utils.desktophandler;

/**
 *
 * @author  bs
 */
public class ModuleView extends javax.swing.JDialog {

    URI theUri;
    /** Creates new form ModuleView */
    public ModuleView(java.awt.Frame parent, boolean modal,String description,String uri)
    {
        super(parent, modal);
        initComponents();
        try{
            theUri=new URI(uri);
            jButtonShow.setVisible(true);
        }
        catch(Exception ex)
        {
            jButtonShow.setVisible(false);
        }

        jLabelDisplay.setText(description);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelModule = new javax.swing.JLabel();
        jButtonTakeMeAway = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabelDisplay = new javax.swing.JLabel();
        jButtonShow = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(wxt4run.wxt4runApp.class).getContext().getResourceMap(ModuleView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setModal(true);
        setName("Form"); // NOI18N
        setResizable(false);

        jLabelModule.setFont(resourceMap.getFont("jLabelModule.font")); // NOI18N
        jLabelModule.setText(resourceMap.getString("jLabelModule.text")); // NOI18N
        jLabelModule.setName("jLabelModule"); // NOI18N

        jButtonTakeMeAway.setText(resourceMap.getString("jButtonTakeMeAway.text")); // NOI18N
        jButtonTakeMeAway.setName("jButtonTakeMeAway"); // NOI18N
        jButtonTakeMeAway.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTakeMeAwayActionPerformed(evt);
            }
        });

        jScrollPane1.setBackground(resourceMap.getColor("jScrollPane1.background")); // NOI18N
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jLabelDisplay.setBackground(resourceMap.getColor("jLabelDisplay.background")); // NOI18N
        jLabelDisplay.setText(resourceMap.getString("jLabelDisplay.text")); // NOI18N
        jLabelDisplay.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jLabelDisplay.setName("jLabelDisplay"); // NOI18N
        jLabelDisplay.setOpaque(true);
        jScrollPane1.setViewportView(jLabelDisplay);

        jButtonShow.setText(resourceMap.getString("jButtonShow.text")); // NOI18N
        jButtonShow.setName("jButtonShow"); // NOI18N
        jButtonShow.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonShowMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabelModule)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 561, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonShow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonTakeMeAway)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelModule)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonTakeMeAway)
                    .addComponent(jButtonShow))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButtonTakeMeAwayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTakeMeAwayActionPerformed
// TODO add your handling code here:
    this.setVisible(false);
}//GEN-LAST:event_jButtonTakeMeAwayActionPerformed

private void jButtonShowMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonShowMouseClicked
    // TODO add your handling code here:
    desktophandler.launchUri(theUri);
    jButtonShow.setVisible(false);
}//GEN-LAST:event_jButtonShowMouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonShow;
    private javax.swing.JButton jButtonTakeMeAway;
    private javax.swing.JLabel jLabelDisplay;
    private javax.swing.JLabel jLabelModule;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables


}
