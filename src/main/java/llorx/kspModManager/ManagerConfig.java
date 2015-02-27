package llorx.kspModManager;

import llorx.kspModManager.mod.Mod;
import java.io.Externalizable;
import java.io.*;

import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import javax.swing.border.EmptyBorder;

import java.awt.Font;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import llorx.kspModManager.utils.Locale;
import static llorx.kspModManager.utils.Locale.getLocalised;

public class ManagerConfig implements Externalizable {

    //private static final Logger LOGGER = LoggerFactory.getLogger(ManagerConfig.class);
    static final long serialVersionUID = 0;

    public static String kspDataFolder = "";
    public static String moduleManagerLink = "http://forum.kerbalspaceprogram.com/threads/55219";
    public static transient String defaultModuleManagerLink = "http://forum.kerbalspaceprogram.com/threads/55219";
    public static boolean excludeUnneededFiles = true;
    public static boolean excludeModuleManagerDll = true;
    public static int locale = -1;
    public static int mainWindowWidth = 500;
    public static int mainWindowHeight = 500;

    public static transient boolean localeSelected = false;
    private static transient JCheckBox excludeFilesCheck;
    private static transient JCheckBox excludeMmCheck;

    @Override
    public void writeExternal(ObjectOutput out) {
        try {
            out.writeObject(ManagerConfig.kspDataFolder);
            out.writeObject(ManagerConfig.moduleManagerLink);
            out.writeBoolean(ManagerConfig.excludeUnneededFiles);
            out.writeBoolean(ManagerConfig.excludeModuleManagerDll);
            if (ManagerConfig.localeSelected == false) {
                out.writeInt(-1);
            } else {
                out.writeInt(ManagerConfig.locale);
            }
            out.writeInt(mainWindowWidth);
            out.writeInt(mainWindowHeight);
        } catch (Exception e) {
        }
    }

    @Override
    public void readExternal(ObjectInput in) {
        try {
            ManagerConfig.kspDataFolder = (String) in.readObject();
            ManagerConfig.moduleManagerLink = (String) in.readObject();
            ManagerConfig.excludeUnneededFiles = in.readBoolean();
            ManagerConfig.excludeModuleManagerDll = in.readBoolean();
            ManagerConfig.locale = in.readInt();
            ManagerConfig.mainWindowWidth = in.readInt();
            ManagerConfig.mainWindowHeight = in.readInt();
        } catch (Exception e) {
        }
    }

    private static void selectLanguage() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        ButtonGroup group = new ButtonGroup();
        Locale.locales.forEach(local -> {
            JRadioButton b = new JRadioButton(Locale.locale
                    .getString("locale." + local + ".localename"));
            b.setActionCommand(String.valueOf(Locale.locales.indexOf(local)));
            panel.add(b);
            group.add(b);
            if (ManagerConfig.locale == Locale.locales.indexOf(local)) {
                group.setSelected(b.getModel(), true);
            }
        });

        panel.add(new JSeparator(JSeparator.HORIZONTAL));

        JLabel bottom = new JLabel(getLocalised("CUSTOM_LANGUAGE_INSTRUCTIONS"));
        panel.add(bottom);

        JOptionPane.showMessageDialog(null, panel, getLocalised("SELECT_LANGUAGE_TITLE"), JOptionPane.PLAIN_MESSAGE);

        if (ManagerConfig.locale != Integer.parseInt(group.getSelection().getActionCommand())) {
            ManagerConfig.locale = Integer.parseInt(group.getSelection().getActionCommand());
            ManagerConfig.localeSelected = true;
            JOptionPane.showMessageDialog(null, getLocalised("LANGUAGE_CHANGED_WARN"), getLocalised("SELECT_LANGUAGE_TITLE"), JOptionPane.PLAIN_MESSAGE);
        }
    }

    private static JPanel getPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 1));

        JLabel titleTxt = new JLabel(getLocalised("CONFIG_MANAGER_TITLE"));
        Font font = titleTxt.getFont().deriveFont(titleTxt.getFont().getSize2D() + 5.0f);
        titleTxt.setFont(font);
        panel.add(titleTxt);

        panel.add(new JSeparator(JSeparator.HORIZONTAL));

        ImageIcon lang = new ImageIcon(ManagerConfig.class.getResource("/images/lang.png"));
        JButton changeLanguage = new JButton(getLocalised("CHANGE_LANGUAGE"));
        changeLanguage.setIcon(lang);
        changeLanguage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ManagerConfig.selectLanguage();
            }
        });
        panel.add(changeLanguage);

        JButton changeKspDataBut = new JButton(getLocalised("CHANGE_KSP_FOLDER"));
        changeKspDataBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ManagerConfig.selectKspFolder();
            }
        });
        panel.add(changeKspDataBut);

        JButton changeMmLinkBut = new JButton(getLocalised("CHANGE_MM_LINK"));
        changeMmLinkBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Mod newMm;
                String newUrl;
                do {
                    newMm = null;
                    newUrl = JOptionPane.showInputDialog(null, getLocalised("CHANGE_MM_LINK_TEXT"), ManagerConfig.moduleManagerLink);
                    if (newUrl != null && newUrl.length() > 0) {
                        newMm = new Mod("Module Manager dll", newUrl, true);
                        if (!newMm.isValid) {
                            JOptionPane.showMessageDialog(null, getLocalised("URL_NOT_VALID"), getLocalised("ERROR_TITLE"), JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                } while ((newMm != null && !newMm.isValid));
                if (newMm != null && newMm.isValid) {
                    ManagerConfig.moduleManagerLink = newUrl;
                    JOptionPane.showMessageDialog(null, getLocalised("URL_UPDATED"), getLocalised("UPDATED_TITLE"), JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        panel.add(changeMmLinkBut);

        JButton restoreMmLinkBut = new JButton(getLocalised("RESTORE_MM_LINK"));
        restoreMmLinkBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ManagerConfig.moduleManagerLink = ManagerConfig.defaultModuleManagerLink;
                JOptionPane.showMessageDialog(null, getLocalised("URL_RESTORED"), getLocalised("UPDATED_TITLE"), JOptionPane.PLAIN_MESSAGE);
            }
        });
        panel.add(restoreMmLinkBut);

        excludeFilesCheck = new JCheckBox(getLocalised("EXCLUDE_FILES"));
        excludeFilesCheck.setSelected(ManagerConfig.excludeUnneededFiles);
        excludeFilesCheck.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(excludeFilesCheck);

        panel.add(new JSeparator(JSeparator.HORIZONTAL));

        JLabel warnText = new JLabel(getLocalised("CONFIG_WARNING"));
        warnText.setBorder(new EmptyBorder(5, 0, 0, 0));
        panel.add(warnText);

        excludeMmCheck = new JCheckBox(getLocalised("EXCLUDE_MM"));
        excludeMmCheck.setSelected(ManagerConfig.excludeModuleManagerDll);
        excludeMmCheck.setBorder(new EmptyBorder(0, 0, 5, 0));
        panel.add(excludeMmCheck);

        panel.add(new JSeparator(JSeparator.HORIZONTAL));

        return panel;
    }

    private static boolean askForKspFolder() {
        try {
            JFileChooser j = new JFileChooser();
            j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int opt = j.showSaveDialog(null);
            if (opt == JFileChooser.APPROVE_OPTION) {
                String path = j.getSelectedFile().getCanonicalPath() + File.separator + "GameData";
                File f = new File(path);
                if (f.exists() && f.isDirectory()) {
                    ManagerConfig.kspDataFolder = f.getCanonicalPath();
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, getLocalised("KSP_FOLDER_ERROR"), getLocalised("ERROR_TITLE"), JOptionPane.PLAIN_MESSAGE);
                }
            }
        } catch (Exception ex) {
            //LOGGER.error("Error when trying to ask for KSP folder", ex);
            ErrorLog.log(ex);
        }
        return false;
    }

    public static boolean selectKspFolder() {
        boolean ok = false;
        do {
            int reply = JOptionPane.showConfirmDialog(null, getLocalised("SELECT_KSP_FOLDER"), getLocalised("SELECT_KSP_FOLDER_TITLE"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (reply == JOptionPane.OK_OPTION) {
                ok = ManagerConfig.askForKspFolder();
            } else {
                return false;
            }
        } while (ok == false);
        return true;
    }

    public static void change() {
        JOptionPane.showMessageDialog(null, ManagerConfig.getPanel(), "Config", JOptionPane.PLAIN_MESSAGE);
        ManagerConfig.excludeUnneededFiles = excludeFilesCheck.isSelected();
        ManagerConfig.excludeModuleManagerDll = excludeMmCheck.isSelected();
    }
}
