/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.jdesktop.layout.GroupLayout;

/**
 *
 * @author sergeil
 */
public class DialogUtils {
        
    private static JDialog dialog;
    private static JLabel jLabel1;

    public static void setVisible(boolean b) {
        dialog.setVisible(b);
    }

    public static void showDialog(String text) {
        if (dialog == null) {
            jLabel1 = new JLabel();
            jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel1.setText(text);

            dialog = new JDialog();
            dialog.setAlwaysOnTop(true);
            dialog.setTitle("Downloading");
            dialog.setResizable(false);

            GroupLayout jDialog1Layout = new GroupLayout(dialog.getContentPane());
            dialog.getContentPane().setLayout(jDialog1Layout);
            jDialog1Layout.setHorizontalGroup(
                    jDialog1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jDialog1Layout.createSequentialGroup().addContainerGap().add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE).addContainerGap()));
            jDialog1Layout.setVerticalGroup(
                    jDialog1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jDialog1Layout.createSequentialGroup().add(21, 21, 21).add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(22, 22, 22)));

            dialog.pack();

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();

            int x = screenSize.width / 2 - dialog.getWidth() / 2;
            int y = screenSize.height / 2 - dialog.getHeight() / 2;

            dialog.setLocation(x, y);
            dialog.setVisible(true);
        }
        jLabel1.setText(text);
        jLabel1.validate();
    }

    public static void closeDialog() {
        dialog.dispose();
        dialog = null;
        jLabel1 = null;
    }
}
