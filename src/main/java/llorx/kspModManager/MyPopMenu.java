/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager;

import llorx.kspModManager.mod.Mod;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import static llorx.kspModManager.utils.Locale.getLocalised;

/**
 *
 * @author disease
 */
public class MyPopMenu extends JPopupMenu implements ActionListener {
    JMenuItem menuItemRename = new JMenuItem(getLocalised("MENU_RENAME"), new ImageIcon(getClass().getResource("/images/rename.gif")));
    JMenuItem menuItemOpenLink = new JMenuItem(getLocalised("MENU_OPEN_BROWSER"), new ImageIcon(getClass().getResource("/images/link.gif")));
    JMenuItem menuItemChangeLink = new JMenuItem(getLocalised("MENU_CHANGE_LINK"), new ImageIcon(getClass().getResource("/images/download_link.png")));
    JMenuItem menuItemReinstall = new JMenuItem(getLocalised("MENU_REDOWNLOAD"), new ImageIcon(getClass().getResource("/images/install.png")));
    JMenuItem menuItemUpdate = new JMenuItem(getLocalised("MENU_CHECK_UPDATE"), new ImageIcon(getClass().getResource("/images/update.png")));
    JMenuItem menuItemDelete = new JMenuItem(getLocalised("MENU_UNINSTALL"), new ImageIcon(getClass().getResource("/images/delete.png")));
    JMenuItem menuItemStop = new JMenuItem(getLocalised("MENU_STOP"), new ImageIcon(getClass().getResource("/images/stop.png")));
    JMenuItem menuItemAllDisabled = new JMenuItem(getLocalised("MENU_DISABLED"));
    Mod mod;
    private final Main outer;

    public MyPopMenu(Mod mod, final Main outer) {
        this.outer = outer;
        this.mod = mod;
        if (this.mod.isInstallable() == false) {
            menuItemReinstall.setText(getLocalised("MENU_DOWNLOAD"));
            menuItemDelete.setText(getLocalised("MENU_REMOVE"));
        }
        if (this.mod.getWork()) {
            this.add(menuItemOpenLink);
            this.addSeparator();
            this.add(menuItemStop);
        } else {
            if (outer.asyncDThread != null) {
                menuItemRename.setEnabled(false);
                menuItemReinstall.setEnabled(false);
                menuItemChangeLink.setEnabled(false);
                menuItemUpdate.setEnabled(false);
                menuItemDelete.setEnabled(false);
                this.add(menuItemAllDisabled);
                this.addSeparator();
            }
            this.add(menuItemRename);
            this.addSeparator();
            this.add(menuItemOpenLink);
            this.add(menuItemChangeLink);
            this.addSeparator();
            this.add(menuItemReinstall);
            this.add(menuItemUpdate);
            this.addSeparator();
            this.add(menuItemDelete);
        }
        menuItemRename.addActionListener(this);
        menuItemOpenLink.addActionListener(this);
        menuItemChangeLink.addActionListener(this);
        menuItemReinstall.addActionListener(this);
        menuItemUpdate.addActionListener(this);
        menuItemDelete.addActionListener(this);
        menuItemStop.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuItemRename) {
            outer.renameSelectedMod();
        } else if (e.getSource() == menuItemOpenLink) {
            try {
                DesktopApi.browse(new URI(this.mod.getLink()));
            } catch (Exception ee) {
            }
        } else if (e.getSource() == menuItemChangeLink) {
            outer.changeLinkSelectedMod();
        } else if (e.getSource() == menuItemReinstall) {
            outer.reinstallSelectedMod();
        } else if (e.getSource() == menuItemUpdate) {
            outer.updateSelectedMod();
        } else if (e.getSource() == menuItemDelete) {
            outer.removeSelectedMod();
        } else if (e.getSource() == menuItemStop) {
            outer.stopSelectedMod();
        }
    }
    
}
