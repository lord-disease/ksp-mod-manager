/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager;

import llorx.kspModManager.mod.Mod;
import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import static llorx.kspModManager.utils.Locale.getLocalised;

/**
 *
 * @author disease
 */
public class IconTextCellRenderer extends DefaultTableCellRenderer {

    ImageIcon install = new ImageIcon(getClass().getResource("/images/install.png"));
    ImageIcon online = new ImageIcon(getClass().getResource("/images/link.gif"));
    ImageIcon error = new ImageIcon(getClass().getResource("/images/delete.png"));
    SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy - kk:mm:ss");

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Mod mod = (Mod) value;

        switch (column) {
            case 0:
                if (mod.isInstallable()) {
                    setIcon(install);
                } else {
                    setIcon(online);
                }
                setText(mod.getName());
                break;
            case 1:
                setIcon(null);
                Font font = getFont();
                setFont(font.deriveFont(font.getStyle() | Font.BOLD));
                if (!mod.getStatus().equals("")) {
                    setText(mod.getStatus());
                } else {
                    if (mod.errorUpdate == true) {
                        setIcon(error);
                    } else {
                        if (mod.justUpdated == true) {
                            setIcon(online);
                        } else {
                            setFont(font.deriveFont(font.getStyle() & ~Font.BOLD));
                        }
                    }
                    if (mod.getLastDate() != null) {
                        setText((mod.justUpdated == true ? "[" + (mod.isInstallable() ? getLocalised("NEW_VERSION") : getLocalised("NEW_UPDATE")) + "] " : (mod.errorUpdate == true ? "[" + getLocalised("DOWNLOAD_ERROR") + "] " : "")) + this.sdfDate.format(mod.getLastDate()) + " | " + mod.getPrefix());
                    }
                }
                break;
        }
        return this;
    }
}
