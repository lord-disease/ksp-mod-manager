package llorx.kspModManager;

import llorx.kspModManager.mod.Mod;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.SimpleDateFormat;
import javax.swing.table.AbstractTableModel;
import javax.swing.Box;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import llorx.kspModManager.parse.ModDataParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.HttpURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import llorx.kspModManager.mod.Mod.ModFile;
import static llorx.kspModManager.mod.Type.TYPE_CURSEFORGE;
import llorx.kspModManager.utils.Locale;
import static llorx.kspModManager.utils.Locale.getLocalised;

public class Main extends JFrame implements ActionListener {

    //private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    JButton downloadBut;
    JButton installBut;

    JButton configBut;

    JButton mmButton;
    JButton exportButton;
    JButton updateBut;

    Document xmlDoc;
    Element rootElement;

    List<Mod> modList = new ArrayList<>();
    JTable mainList;

    Thread asyncDThread = null;

    List<Mod> modQeue = new ArrayList<>();
    List<Mod> modInstallQeue = new ArrayList<>();

    Object lock = new Object();

    boolean closingApp = false;

    public Main() {
        loadConfigFile();
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent winEvt) {
                closingApp = true;
                if (asyncDThread != null) {
                    while (asyncDThread != null && asyncDThread.isAlive()) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                    }
                }
                saveConfigFile();
                if ((new File("temp")).exists()) {
                    DirIO.clearDir("temp");
                }
            }
        });

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.LINE_AXIS));
        panelTop.setBorder(new EmptyBorder(2, 2, 2, 2));
        add(panelTop, BorderLayout.NORTH);

        JPanel panelConfig = new JPanel(new GridLayout(1, 1, 2, 2));
        panelTop.add(panelConfig);

        ImageIcon config = new ImageIcon(getClass().getResource("/images/config.png"));

        configBut = new JButton(getLocalised("CONFIG_BUTTON"));
        panelConfig.add(configBut);
        configBut.setIcon(config);
        configBut.addActionListener(this);

        panelTop.add(Box.createHorizontalGlue());

        JPanel panelDownload = new JPanel(new GridLayout(2, 1, 2, 2));
        panelTop.add(panelDownload);

        downloadBut = new JButton("[+] " + getLocalised("DOWNLOAD_MOD"));
        panelDownload.add(downloadBut);
        downloadBut.addActionListener(this);

        installBut = new JButton(getLocalised("INSTALL_QUEUED"));
        panelDownload.add(installBut);
        installBut.addActionListener(this);
        installBut.setEnabled(false);

        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.LINE_AXIS));
        panelBottom.setBorder(new EmptyBorder(2, 2, 2, 2));
        add(panelBottom, BorderLayout.SOUTH);

        mmButton = new JButton(getLocalised("DOWNLOAD_MM"));
        panelBottom.add(mmButton);
        mmButton.addActionListener(this);

        panelBottom.add(Box.createHorizontalGlue());

        exportButton = new JButton(getLocalised("EXPORT_LIST"));
        panelBottom.add(exportButton);
        exportButton.addActionListener(this);

        panelBottom.add(Box.createHorizontalGlue());

        updateBut = new JButton(getLocalised("CHECK_MOD_UPDATES"));
        panelBottom.add(updateBut);
        updateBut.addActionListener(this);

        mainList = new JTable(new MyTableModel(modList));
        JScrollPane barraDesplazamiento = new JScrollPane(mainList);
        add(barraDesplazamiento, BorderLayout.CENTER);
        mainList.getTableHeader().setReorderingAllowed(false);

        IconTextCellRenderer cellRenderer = new IconTextCellRenderer();
        mainList.setDefaultRenderer(Object.class, cellRenderer);

        mainList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int r = mainList.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < mainList.getRowCount()) {
                        mainList.setRowSelectionInterval(r, r);
                    } else {
                        mainList.clearSelection();
                    }

                    int rowindex = mainList.getSelectedRow();
                    if (rowindex < 0) {
                        return;
                    }
                    if (e.getComponent() instanceof JTable) {
                        JPopupMenu popup = new MyPopMenu(getSelectedMod(), llorx.kspModManager.Main.this);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    Mod getSelectedMod() {
        synchronized (lock) {
            if (mainList.getSelectedRow() > -1) {
                return modList.get(mainList.getSelectedRow());
            }
        }
        return null;
    }

    void renameSelectedMod() {
        synchronized (lock) {
            Mod mod = getSelectedMod();
            if (mod != null && mod.getStatus().equals("")) {
                String newName = JOptionPane.showInputDialog(null, getLocalised("NEW_NAME_QUESTION"), mod.getName());
                if (newName != null && newName.length() > 0) {
                    mod.setName(newName);
                    setMod(mod);
                    saveConfigFile();
                }
            }
        }
    }

    void updateSelectedMod() {
        synchronized (lock) {
            Mod mod = getSelectedMod();
            if (mod != null && mod.getStatus().equals("")) {
                List<Mod> list = new ArrayList<Mod>();
                list.add(mod);
                updateMods(list);
            }
        }
    }

    void changeLinkSelectedMod() {
        synchronized (lock) {
            Mod mod = getSelectedMod();
            if (mod != null && mod.getStatus().equals("")) {
                String oldLink = mod.getLink();
                do {
                    String cbData = "";
                    try {
                        cbData = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                        if (!(cbData.startsWith("http://") || cbData.startsWith("https://"))) {
                            cbData = "";
                        }
                    } catch (Exception e) {
                    }
                    String newLink = JOptionPane.showInputDialog(null, getLocalised("NEW_LINK_QUESTION"), cbData);
                    if (newLink != null && newLink.length() > 0) {
                        mod.reloadMod(newLink);
                    } else {
                        if (mod.isValid == false) {
                            mod.reloadMod(oldLink);
                        }
                        break;
                    }
                    if (mod.isValid == false) {
                        alertBox(null, getLocalised("ERROR_PARSING_LINK"));
                    }
                } while (mod.isValid == false);
                setMod(mod);
                if (!mod.getLink().equals(oldLink)) {
                    int reply = JOptionPane.showConfirmDialog(null, getLocalised("LINK_CHANGED_DOWNLOAD_AGAIN"), getLocalised("LINK_CHANGED_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (reply == JOptionPane.YES_OPTION) {
                        reinstallSelectedMod(mod);
                        mod.setLastDate(new Date());
                    }
                }
                saveConfigFile();
            }
        }
    }

    void reinstallSelectedMod() {
        reinstallSelectedMod(null);
    }

    void reinstallSelectedMod(Mod mod) {
        synchronized (lock) {
            if (mod == null) {
                mod = getSelectedMod();
            }
            if (mod != null && mod.getStatus().equals("")) {
                mod.setInstallable(true);
                List<Mod> list = new ArrayList<Mod>();
                list.add(mod);
                updateMods(list, true);
            }
        }
    }

    void stopSelectedMod() {
        synchronized (lock) {
            Mod mod = getSelectedMod();
            if (mod != null) {
                mod.stopWork(lock);
            }
        }
    }

    void removeSelectedMod() {
        synchronized (lock) {
            Mod mod = getSelectedMod();
            if (mod != null) {
                mod.stopWork(lock);
                int reply;
                if (mod.isInstallable() == false || mod.isSaved() == false) {
                    reply = JOptionPane.showConfirmDialog(null, getLocalised("DELETE_SURE"), getLocalised("DELETE_MOD_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                } else {
                    reply = JOptionPane.showOptionDialog(null, getLocalised("DELETE_MOD_COMPLETELY_ASK"), getLocalised("DELETE_MOD_TITLE"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{getLocalised("DELETE_COMPLETELY_BUTTON"), getLocalised("KEEP_VERSION_BUTTON"), getLocalised("CANCEL_BUTTON")}, null);
                }
                if (reply == JOptionPane.YES_OPTION || (mod.isInstallable() && mod.isSaved() && reply == JOptionPane.NO_OPTION)) {
                    uninstallMod(mod);
                    if (reply == JOptionPane.YES_OPTION) {
                        removeMod(mod);
                    } else {
                        mod.setInstallable(false);
                        mod.setStatus("");
                        setMod(mod);
                        listUpdate(mainList.getSelectedRow());
                    }
                    saveConfigFile();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == downloadBut) {
            getAddon();
        } else if (e.getSource() == installBut) {
            synchronized (lock) {
                nextInstall();
            }
        } else if (e.getSource() == mmButton) {
            Mod mod = getAddon("Module Manager dll", ManagerConfig.moduleManagerLink);
            if (mod == null) {
                alertBox(null, getLocalised("ERROR_MM"));
            } else {
                mod.isMM = true;
            }
        } else if (e.getSource() == exportButton) {
            try {
                FileWriter f0 = new FileWriter("modlist.txt", true);
                String newLine = System.getProperty("line.separator");
                for (int i = 0; i < modList.size(); i++) {
                    Mod mod = modList.get(i);
                    f0.write(mod.getName() + ": " + mod.getLink() + newLine);
                }
                f0.close();
                alertBox(null, getLocalised("MODLIST_SAVED"));
            } catch (Exception ex) {
                ErrorLog.log(ex);
            }
        } else if (e.getSource() == updateBut) {
            synchronized (lock) {
                updateMods();
            }
        } else if (e.getSource() == configBut) {
            openConfigWindow();
        }
    }

    void uninstallMod(Mod mod) {
        uninstallMod(mod, true);
    }

    void removeFromQueues(Mod mod) {
        for (int i = 0; i < modInstallQeue.size(); i++) {
            if (modInstallQeue.get(i).getId().equals(mod.getId())) {
                modInstallQeue.remove(i);
                break;
            }
        }
        for (int i = 0; i < modQeue.size(); i++) {
            if (modQeue.get(i).getId().equals(mod.getId())) {
                modQeue.remove(i);
                break;
            }
        }
        if (modInstallQeue.size() == 0) {
            installBut.setEnabled(false);
        }
    }

    void uninstallMod(Mod mod, boolean alerts) {
        removeFromQueues(mod);
        if (mod.downloadedFile.length() > 0) {
            DirIO.clearDir(mod.downloadedFile);
        }
        if (mod.isInstallable() == false) {
            return;
        }
        List<Path> updatedFiles = new ArrayList<Path>();
        List<Path> parents = new ArrayList<Path>();
        List<Path> parentsNotRemoved = new ArrayList<Path>();

        Path mainData = Paths.get(ManagerConfig.kspDataFolder);

        for (ModFile f : mod.getInstalledFiles()) {
            Path file = f.getPath();
            if (f.isUpdated() == true) {
                updatedFiles.add(file);
            } else {
                try {
                    Files.deleteIfExists(file);
                } catch (Exception e) {
                }
                boolean found = false;
                for (Path ff : parents) {
                    try {
                        if (Files.isSameFile(ff, file.getParent())) {
                            found = true;
                            break;
                        }
                    } catch (Exception e) {
                    }
                }
                if (found == false) {
                    parents.add(file.getParent());
                }
            }
        }
        mod.clearInstalledFiles();
        for (Path f : parents) {
            boolean sameFile = false;
            try {
                while (f != null && !Files.isSameFile(mainData, f)) {
                    String[] flist = f.toFile().list();
                    if (flist == null || flist.length == 0) {
                        try {
                            Files.deleteIfExists(f);
                        } catch (Exception ee) {
                        }
                        f = f.getParent();
                    } else {
                        boolean found = false;
                        for (Path ff : parentsNotRemoved) {
                            try {
                                if (Files.isSameFile(ff, f)) {
                                    found = true;
                                    break;
                                }
                            } catch (Exception ee) {
                            }
                        }
                        if (found == false) {
                            parentsNotRemoved.add(f);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
        boolean notRemoved = false;
        for (int i = parentsNotRemoved.size() - 1; i >= 0; i--) {
            if (Files.exists(parentsNotRemoved.get(i)) == true) {
                notRemoved = true;
            } else {
                parentsNotRemoved.remove(i);
            }
        }
        if (alerts == true) {
            if (notRemoved == true) {
                try {
                    FileWriter f0 = new FileWriter("log.txt", true);
                    String newLine = System.getProperty("line.separator");
                    Date d = new Date();
                    f0.write(newLine + newLine + "********* " + getCurrentTimeStamp() + " *********" + newLine + " - " + getLocalised("LOG_DIR_EMPTY").replace("%MODNAME%", mod.getName()) + ":" + newLine);
                    for (int i = 0; i < parentsNotRemoved.size(); i++) {
                        try {
                            f0.write(parentsNotRemoved.get(i).toFile().getCanonicalPath() + newLine);
                        } catch (Exception e) {

                        }
                    }
                    f0.close();
                } catch (Exception e) {

                }
                alertBox(null, getLocalised("LOG_DIR_EMPTY_ERROR"));
            }
            if (updatedFiles.size() > 0) {
                try {
                    FileWriter f0 = new FileWriter("log.txt", true);
                    String newLine = System.getProperty("line.separator");
                    Date d = new Date();
                    f0.write(newLine + newLine + "********* " + getCurrentTimeStamp() + " *********" + newLine + " - " + getLocalised("LOG_OVERWRITTEN_FILES").replace("%MODNAME%", mod.getName()) + ":" + newLine);
                    for (int i = 0; i < updatedFiles.size(); i++) {
                        try {
                            f0.write(updatedFiles.get(i).toFile().getCanonicalPath() + newLine);
                        } catch (Exception e) {

                        }
                    }
                    f0.close();
                } catch (Exception e) {
                }
                alertBox(null, getLocalised("LOG_OVERWRITTEN_FILES_ERROR"));
            }
        }
    }

    String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat();
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    boolean removeMod(Mod mod) {
        String modFileName = "data" + File.separator + mod.getUniqueId().toString();
        DirIO.clearDir(modFileName);
        for (int i = 0; i < modList.size(); i++) {
            if (modList.get(i).getUniqueId() == mod.getUniqueId()) {
                modList.remove(i);
                listUpdate();
                return true;
            }
        }
        return false;
    }

    public class MyAsyncModDownload implements Runnable {

        //private final Logger LOGGER = LoggerFactory.getLogger(MyAsyncModDownload.class);
        private Mod mod;

        MyAsyncModDownload(Mod mod) {
            this.mod = mod;
        }

        @Override
        public void run() {
            try {
                this.mod.setWork(true, lock);
                this.mod.setStatus(" - [" + getLocalised("DOWNLOADING") + " - 0%] -");
                setMod(this.mod);
                boolean isDownloaded = downloadMod(this.mod);
                if (isDownloaded) {
                    this.mod.setStatus(" - [" + getLocalised("INSTALL_QUEUE") + "] -");
                    setMod(this.mod);
                    synchronized (lock) {
                        modInstallQeue.add(this.mod);
                    }
                } else if (mod.isSaved() == false) {
                    removeMod(mod);
                } else {
                    mod.setStatus("");
                    setMod(mod);
                }
                this.mod.setWork(false, lock);
                synchronized (lock) {
                    asyncDThread = null;
                    if (isDownloaded) {
                        installBut.setEnabled(true);
                    }
                    nextDownload();
                }
            } catch (Exception e) {
                //LOGGER.error("Error downloading mod", e);
                ErrorLog.log(e);
            }
        }
    }

    boolean downloadMod(Mod mod) {
        if (mod.isInstallable() == false) {
            return true;
        }
        try {
            synchronized (lock) {
                File f = new File("temp");
                if (!f.exists()) {
                    f.mkdirs();
                } else if (!f.isDirectory()) {
                    alertBox(null, mod.getName() + ": " + getLocalised("ERROR_TEMP"));
                    return false;
                }
            }

            if (mod.getDownloadLink().equals("")) {
                String link = ModDataParser.getDownloadLink(mod);
                if (link.equals("")) {
                    alertBox(null, mod.getName() + ": " + getLocalised("ERROR_DOWNLOAD_LINK"));
                    return false;
                } else {
                    mod.setDownloadLink(link);
                }
            }
            String filename = downloadFile(mod.getDownloadLink(), mod);
            if (filename == null) {
                return false;
            } else if (filename.length() == 0) {
                return downloadMod(mod);
            }
            mod.downloadedFile = "temp" + File.separator + filename;
            return true;
        } catch (Exception ex) {
            //LOGGER.error("Error downloading mod", ex);
            ErrorLog.log(ex);
        }
        return false;
    }

    String downloadFile(String link, Mod mod) {
        InputStream in = null;
        FileOutputStream fout = null;
        try {
            if (mod != null) {
                mod.setDownloadLink("");
            }
            HttpURLConnection conn = Http.getConnection(link);
            if (conn == null) {
                alertBox(null, (mod != null ? mod.getName() : link) + ": " + getLocalised("ERROR_DOWNLOAD_LINK"));
            }
            conn.setReadTimeout(1000);
            if (Http.fileType(conn) != Http.ZIP_EXTENSION) {
                boolean validLink = false;
                String dlink = Http.getDownloadLink(link);
                if (dlink == null) {
                    Browser browser = new Browser();
                    browser.show(link, mod);
                    if (browser.downloadFile != null) {
                        validLink = true;
                        conn = browser.downloadFile;
                    }
                    if (browser.modReloaded == true) {
                        return "";
                    }
                } else {
                    conn = Http.getConnection(dlink);
                    if (Http.fileType(conn) == Http.ZIP_EXTENSION) {
                        validLink = true;
                        link = dlink;
                    }
                }
                if (validLink == false) {
                    alertBox(null, (mod != null ? mod.getName() : link) + ": " + getLocalised("ERROR_DOWNLOAD_LINK"));
                    return null;
                }
            }
            Map<String, List<String>> map = conn.getHeaderFields();
            if (conn == null) {
                alertBox(null, (mod != null ? mod.getName() : link) + ": " + getLocalised("ERROR_DOWNLOAD_LINK"));
                return null;
            }
            conn.setReadTimeout(500);

            String filename = Http.parseFileHeader(conn, mod != null ? (mod.getId() + ".zip") : "LKMM.zip");

            int fsize = conn.getContentLength();

            in = conn.getInputStream();
            try {
                new File("temp").mkdir();
            } catch (Exception ee) {
            }
            fout = new FileOutputStream("temp" + File.separator + filename);

            final byte data[] = new byte[64];
            int total = 0;
            int lastPerc = 0;

            while (closingApp == false) {
                int count = -1;
                int errorCount = 0;
                while (errorCount < 10) {
                    if (closingApp == true || (mod != null && mod.continueWork(lock) == false)) {
                        return null;
                    }
                    try {
                        count = in.read(data, 0, 64);
                        break;
                    } catch (Exception e) {
                        errorCount++;
                    }
                }
                if (errorCount >= 10) {
                    alertBox(null, (mod != null ? mod.getName() : link) + ": " + getLocalised("ERROR_DOWNLOAD_LINK"));
                    return null;
                }
                if (count > -1) {
                    total = total + count;
                    int perc = (int) ((total * 100.0f) / fsize);
                    if (lastPerc != perc) {
                        lastPerc = perc;
                        if (mod != null) {
                            mod.setStatus(" - [" + getLocalised("DOWNLOADING") + " - " + lastPerc + "%] -");
                            setMod(mod);
                        }
                    }
                    fout.write(data, 0, count);
                } else {
                    break;
                }
            }

            if (closingApp == true) {
                return null;
            } else {
                return filename;
            }
        } catch (Exception e) {
            //LOGGER.error("", e);
            ErrorLog.log(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                //LOGGER.error("", ex);
                ErrorLog.log(ex);
            }
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception ex) {
                //LOGGER.error("", ex);
                ErrorLog.log(ex);
            }
        }
        return null;
    }

    String[] getExcludeList(Mod mod) {
        String[] undeededFilesArray = new String[]{"(source)", "(sources)", "(.*)(\\.txt)", "(.*)(\\.pdf)", "(.*)(\\.asciidoc)", "(.*)(\\.md)", "(.*)(source)(.*)(\\.zip)"};
        String[] mmArray = new String[]{"(modulemanager)(.*)(\\.dll)"};
        int arraysize = 0;
        if (ManagerConfig.excludeUnneededFiles == true) {
            arraysize += undeededFilesArray.length;
        }
        if (ManagerConfig.excludeModuleManagerDll == true && mod.isMM == false) {
            arraysize += mmArray.length;
        }
        String[] excludeList = new String[arraysize];
        if (ManagerConfig.excludeUnneededFiles == true) {
            System.arraycopy(undeededFilesArray, 0, excludeList, 0, undeededFilesArray.length);
        }
        if (ManagerConfig.excludeModuleManagerDll == true && mod.isMM == false) {
            System.arraycopy(mmArray, 0, excludeList, (ManagerConfig.excludeUnneededFiles ? undeededFilesArray.length : 0), mmArray.length);
        }
        return excludeList;
    }

    public class MyAsyncModInstall implements Runnable {

        private Mod mod;
        private int forceCopy = -1;

        MyAsyncModInstall(Mod mod) {
            this.mod = mod;
        }

        List<ModFile> copyFiles(String mainPath, String copyPath) {
            List<ModFile> copied = new ArrayList<ModFile>();

            Path target = Paths.get(ManagerConfig.kspDataFolder);

            Path dir = Paths.get(mainPath, copyPath);
            Path mainDir = Paths.get(mainPath);

            try {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        String fileNameLower = dir.getFileName().toString().toLowerCase();
                        for (String ex : getExcludeList(mod)) {
                            if (fileNameLower.matches(ex)) {
                                return SKIP_SUBTREE;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = mainDir.relativize(file);
                        Path dest = target.resolve(relativePath);
                        File destFile = dest.toFile();
                        ModFile f = new ModFile(dest, false);

                        String fileNameLower = file.getFileName().toString().toLowerCase();

                        for (String ex : getExcludeList(mod)) {
                            if (fileNameLower.matches(ex)) {
                                return CONTINUE;
                            }
                        }

                        boolean copy = true;
                        if (destFile.exists()) {
                            copy = false;
                            f.setUpdated(true);
                            if (forceCopy == -1) {
                                String relPath = relativePath.toFile().getPath();
                                JCheckBox c = new JCheckBox(getLocalised("SELECTION_REMEMBER"));
                                final JComponent[] inputs = new JComponent[]{
                                    new JLabel(getLocalised("OVERWRITE_ASK")),
                                    new JLabel("GameData" + File.separator + relPath),
                                    c
                                };
                                int reply = JOptionPane.showConfirmDialog(null, inputs, getLocalised("ADD_MOD_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                if (reply == JOptionPane.YES_OPTION) {
                                    copy = true;
                                }
                                if (c.isSelected()) {
                                    forceCopy = reply == JOptionPane.YES_OPTION ? 1 : 0;
                                }
                            } else if (forceCopy == 1) {
                                copy = true;
                            }
                        }
                        if (copy == true) {
                            destFile.mkdirs();
                            Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                            copied.add(f);
                        }
                        return CONTINUE;
                    }
                });
            } catch (Exception e) {
            }
            return copied;
        }

        JPanel getPanel(List<String> gameDatas, List<String> gameTxt) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            JLabel titleTxt = new JLabel(this.mod.getName());
            Font font = titleTxt.getFont().deriveFont(titleTxt.getFont().getSize2D() + 5.0f);
            titleTxt.setFont(font);
            titleTxt.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(titleTxt);

            if (gameTxt.size() > 0) {
                JButton readmeBut = new JButton(getLocalised("OPEN_README"));
                readmeBut.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (gameTxt.size() == 1) {
                            File file = new File(gameTxt.get(0));
                            try {
                                DesktopApi.edit(file);
                            } catch (Exception ex) {
                                //LOGGER.error("", ex);
                                ErrorLog.log(ex);
                            }
                        } else {
                            JPanel readmePanel = new JPanel();
                            readmePanel.setLayout(new BoxLayout(readmePanel, BoxLayout.PAGE_AXIS));
                            for (String txt : gameTxt) {
                                File file = new File(txt);
                                JButton but = new JButton(file.getName());
                                but.setAlignmentX(Component.CENTER_ALIGNMENT);
                                but.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        try {
                                            DesktopApi.edit(file);
                                        } catch (Exception ex) {
                                            //LOGGER.error("", ex);
                                            ErrorLog.log(ex);
                                        }
                                    }
                                });
                                readmePanel.add(but);
                            }
                            JOptionPane.showMessageDialog(null, readmePanel, getLocalised("README_FILES_TITLE"), JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                });
                readmeBut.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(readmeBut);
            }
            JLabel installTxt = new JLabel(getLocalised("MARK_INSTALL"));
            installTxt.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(installTxt);

            int i = 0;
            JPanel gameDatasPanel = new JPanel();
            gameDatasPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            for (String gdata : gameDatas) {
                i++;
                File gdataFile = new File(gdata);
                String gdataTxt = gdataFile.getParentFile().getName();
                if (gdataTxt.equals("temp")) {
                    gdataTxt = "Main GameData";
                }
                JPanel gameDataPanel = new JPanel();
                gameDataPanel.setLayout(new BoxLayout(gameDataPanel, BoxLayout.PAGE_AXIS));

                FileTreeModel f = new FileTreeModel(gdataFile, getExcludeList(this.mod));
                f.setAlignmentX(Component.CENTER_ALIGNMENT);
                TitledBorder title = BorderFactory.createTitledBorder(gdataTxt);
                f.setBorder(BorderFactory.createTitledBorder(gdataTxt));
                gameDataPanel.add(f);

                JButton but = new JButton(getLocalised("INSTALL_GAMEDATA"));
                but.setAlignmentX(Component.CENTER_ALIGNMENT);
                Mod mod = this.mod;
                but.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        but.setText(getLocalised("INSTALL_AGAIN"));
                        TreePath[] files = f.checkTreeManager.getSelectionModel().getSelectionPaths();
                        List<ModFile> installedFiles = new ArrayList<ModFile>();
                        if (files != null) {
                            forceCopy = -1;
                            for (int ii = 0; ii < files.length; ii++) {
                                Object paths[] = files[ii].getPath();
                                String path = "";
                                for (int iii = 1; iii < paths.length; iii++) {
                                    path = path + File.separator + (String) ((DefaultMutableTreeNode) paths[iii]).getUserObject();
                                }
                                List<ModFile> copiedfiles = copyFiles(gdata, path);
                                for (ModFile f : copiedfiles) {
                                    installedFiles.add(f);
                                }
                            }
                        }
                        for (ModFile f : installedFiles) {
                            mod.addInstalledFile(f);
                        }
                        alertBox(null, getLocalised("INSTALLED_COUNT").replace("%NUMBER%", String.valueOf(installedFiles.size())));
                    }
                });
                gameDataPanel.add(but);

                gameDatasPanel.add(gameDataPanel);
            }
            panel.add(gameDatasPanel);

            return panel;
        }

        @Override
        public void run() {
            this.mod.setWork(true, lock);
            this.mod.setStatus(" - [" + getLocalised("EXTRACTING") + "] -");
            setMod(this.mod);
            List<String> gameDatas = new ArrayList<String>();
            List<String> gameTxt = new ArrayList<String>();
            String downloadedFile = this.mod.downloadedFile;
            this.mod.downloadedFile = "";
            int modType = Zip.getModInfo(downloadedFile, gameDatas, gameTxt);
            String modExtract = "temp" + File.separator + "GameData";
            if (gameDatas.size() == 0) {
                gameDatas.add(modExtract);
                if (Zip.test(modType, Zip.NO_MAINFOLDER)) {
                    String modMainDir = replaceCharacters(this.mod.getName()) + "_" + this.mod.getUniqueId().toString();
                    modExtract = modExtract + File.separator + modMainDir;
                }
            } else {
                for (int i = 0; i < gameDatas.size(); i++) {
                    gameDatas.set(i, modExtract + File.separator + gameDatas.get(i));
                }
            }
            for (int i = 0; i < gameTxt.size(); i++) {
                gameTxt.set(i, modExtract + File.separator + gameTxt.get(i));
            }

            try {
                Zip.extract(downloadedFile, modExtract);
            } catch (Exception e) {
                //LOGGER.error("", e);
                ErrorLog.log(e);
            }
            int i = 0;

            uninstallMod(this.mod, false);

            this.mod.setStatus(" - [" + getLocalised("INSTALLING") + "] -");
            setMod(this.mod);
            String found = getLocalised("AFTERINSTALL_1").replace("%GDATANUMBER%", String.valueOf(gameDatas.size()));
            if (gameTxt.size() > 0) {
                found = found + getLocalised("AFTERINSTALL_2").replace("%READMENUMBER%", String.valueOf(gameTxt.size()));
            }
            alertBox(null, this.mod.getName() + ":\n" + found);

            JOptionPane.showMessageDialog(null, getPanel(gameDatas, gameTxt), getLocalised("INSTALL_TITLE"), JOptionPane.PLAIN_MESSAGE);

            this.mod.setStatus("");
            this.mod.setSaved(true);
            setMod(this.mod);
            saveConfigFile();
            DirIO.clearDir("temp" + File.separator + "GameData");
            DirIO.clearDir(downloadedFile);
            this.mod.setWork(false, lock);
            synchronized (lock) {
                asyncDThread = null;
                downloadBut.setEnabled(true);
                nextInstall();
            }
        }
    }

    public class MyAsyncModUpdate implements Runnable {

        List<Mod> updateList;
        public List<Mod> updateList2;
        boolean force;

        MyAsyncModUpdate(List<Mod> updateList, boolean force) {
            this.updateList = new ArrayList(updateList);
            this.force = force;
        }

        @Override
        public void run() {
            List<Mod> noInstallList = new ArrayList();
            int updated = 0;
            int updatedInstall = 0;
            for (Mod mod : this.updateList) {
                if (closingApp == false && mod.getStatus().equals("")) {
                    mod.setWork(true, lock);
                    mod.setStatus(" - [" + getLocalised("CHECKING") + "] -");
                    mod.justUpdated = false;
                    mod.errorUpdate = false;
                    setMod(mod);
                    String oldVersion = mod.getVersion();
                    Date oldDate = mod.getLastDate();
                    boolean newVersion = mod.checkVersion();
                    if (newVersion || force == true) {
                        if (newVersion) {
                            mod.justUpdated = true;
                            mod.setLastDate(new Date());
                        }
                        updated++;
                        if (mod.isInstallable()) {
                            mod.setStatus(" - [" + getLocalised("DOWNLOADING") + " - 0%] -");
                            setMod(mod);
                            if (downloadMod(mod)) {
                                mod.setStatus(" - [" + getLocalised("INSTALL_QUEUE") + "] -");
                                setMod(mod);
                                synchronized (lock) {
                                    modInstallQeue.add(mod);
                                }
                            } else if (mod.isSaved() == false) {
                                removeMod(mod);
                            } else {
                                mod.setVersion(oldVersion);
                                mod.setLastDate(oldDate);
                                mod.errorUpdate = true;
                                mod.justUpdated = false;
                                mod.setStatus("");
                                setMod(mod);
                            }
                        } else {
                            mod.setStatus("");
                            setMod(mod);
                        }
                    } else {
                        mod.setStatus("");
                        setMod(mod);
                    }
                    mod.setWork(false, lock);
                }
            }
            saveConfigFile();
            if (closingApp == false) {
                if (updated > 0 && force == false) {
                    alertBox(null, getLocalised("UPDATED_QUANTITY").replace("%UPDATEDCOUNT%", String.valueOf(updated)));
                }
                /*if (noInstallList.size() > 0) {
                 JPanel panel = new JPanel();
                 panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                 JLabel titleLabel = new JLabel(noInstallList.size() + " mod " + (noInstallList.size()==1?"is":"are") + " marked to not install:");
                 titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                 panel.add(titleLabel);
                 for (Mod m: noInstallList) {
                 JButton b = new JButton(m.getName());
                 b.setAlignmentX(Component.CENTER_ALIGNMENT);
                 b.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                 try {
                 Desktop.getDesktop().browse(new URI(m.getLink()));
                 } catch (Exception ee) {
                 }
                 }
                 });
                 panel.add(b);
                 }
                 JLabel footerLabel = new JLabel("Click the buttons to open website.");
                 footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                 panel.add(footerLabel);
                 JOptionPane.showMessageDialog(null, panel, "Readme files", JOptionPane.PLAIN_MESSAGE);
                 }*/
                synchronized (lock) {
                    asyncDThread = null;
                    if (modInstallQeue.size() > 0) {
                        installBut.setEnabled(true);
                    } else {
                        installBut.setEnabled(false);
                    }
                    nextDownload();
                }
            }
        }
    }

    String replaceCharacters(String str) {
        return str.replaceAll("[\\W]|_", "");
    }

    void nextDownload() {
        if (closingApp == false && (asyncDThread == null || !asyncDThread.isAlive())) {
            if (modQeue.size() > 0) {
                installBut.setEnabled(false);
                updateBut.setEnabled(false);
                Runnable asyncDRunnable = new MyAsyncModDownload(modQeue.remove(0));
                asyncDThread = new Thread(asyncDRunnable);
                asyncDThread.start();
            } else {
                updateBut.setEnabled(true);
            }
        }
    }

    void nextInstall() {
        if (closingApp == false && (asyncDThread == null || !asyncDThread.isAlive())) {
            if (modInstallQeue.size() > 0) {
                downloadBut.setEnabled(false);
                installBut.setEnabled(false);
                updateBut.setEnabled(false);
                Runnable asyncDRunnable = new MyAsyncModInstall(modInstallQeue.remove(0));
                asyncDThread = new Thread(asyncDRunnable);
                asyncDThread.start();
            } else {
                updateBut.setEnabled(true);
                installBut.setEnabled(false);
            }
        }
    }

    void updateMods() {
        updateMods(modList, false);
    }

    void updateMods(List<Mod> list) {
        updateMods(list, false);
    }

    void updateMods(List<Mod> list, boolean force) {
        if (closingApp == false && (asyncDThread == null || !asyncDThread.isAlive())) {
            installBut.setEnabled(false);
            Runnable asyncDRunnable = new MyAsyncModUpdate(list, force);
            asyncDThread = new Thread(asyncDRunnable);
            asyncDThread.start();
        }
    }

    Mod getAddon() {
        return getAddon("", "");
    }

    Mod getAddon(String name, String urlText) {
        JTextField modName = new JTextField();
        JTextField modUrl = new JTextField();
        try {
            String cbData = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (cbData.startsWith("http://") || cbData.startsWith("https://")) {
                modUrl.setText(cbData);
            }
        } catch (Exception e) {
        }
        JCheckBox check = new JCheckBox(getLocalised("WARN_VERSION_CHECK"));
        int reply = JOptionPane.OK_OPTION;

        while ((urlText.length() == 0 || name.length() == 0) && reply == JOptionPane.OK_OPTION) {
            final JComponent[] inputs = new JComponent[]{
                new JLabel("Name this mod"),
                modName,
                new JLabel("URL"),
                modUrl,
                check
            };
            modName.addAncestorListener(new RequestFocusListener());
            reply = JOptionPane.showConfirmDialog(null, inputs, getLocalised("ADD_MOD_TITLE"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (reply == JOptionPane.OK_OPTION) {
                urlText = modUrl.getText();
                name = modName.getText();
            }
        }
        if (reply != JOptionPane.OK_OPTION) {
            return null;
        }

        synchronized (lock) {
            Mod mod = new Mod(name, urlText, !check.isSelected());
            for (Mod m : modList) {
                if (m.getId().equals(mod.getId())) {
                    alertBox(null, getLocalised("ALREADY_ERROR").replace("%MODNAME%", m.getName()));
                    return null;
                }
            }

            if (mod.isInstallable()) {
                mod.setStatus(" - [" + getLocalised("DOWNLOAD_QUEUE") + "] -");
            }
            if (mod.isValid == false) {
                alertBox(null, getLocalised("ERROR_MOD_URL").replace("%MODNAME%", mod.getName()));
            } else {
                setMod(mod);
                if (mod.isInstallable() == false) {
                    mod.setSaved(true);
                    saveConfigFile();
                } else {
                    modQeue.add(mod);
                }
                nextDownload();
                return mod;
            }
        }
        return null;
    }

    void setMod(Mod mod) {
        boolean found = false;
        int tabley = 0;
        synchronized (lock) {
            for (tabley = 0; tabley < modList.size(); tabley++) {
                if (modList.get(tabley).getUniqueId() == mod.getUniqueId()) {
                    modList.set(tabley, mod);
                    found = true;
                    break;
                }
            }
            if (found == true) {
                if (mod.nameChanged) {
                    listUpdate();
                } else {
                    listUpdate(tabley);
                }
            } else {
                modList.add(mod);
                listUpdate();
            }
            mod.nameChanged = false;
        }
    }

    void listUpdate() {
        listUpdate(-1);
    }

    void listUpdate(int row) {
        synchronized (lock) {
            if (row > -1) {
                ((AbstractTableModel) mainList.getModel()).fireTableRowsUpdated(row, row);
            } else {
                Collections.sort(modList, new myComparator());
                ((AbstractTableModel) mainList.getModel()).fireTableDataChanged();
            }
        }
    }

    void alertBox(Component title, String txt) {
        JOptionPane.showMessageDialog(title, txt);
    }

    void saveConfigFile() {
        try {
            File f = new File("data" + File.separator + "config.xml");
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            xmlDoc = docBuilder.newDocument();
            rootElement = xmlDoc.createElement("data");
            xmlDoc.appendChild(rootElement);

            synchronized (lock) {
                for (Mod mlist : modList) {
                    if (mlist.isSaved()) {
                        try {
                            String modFileName = "data" + File.separator + mlist.getUniqueId().toString() + File.separator + "Mod.object";
                            File modFile = new File(modFileName);
                            if (!modFile.getParentFile().exists()) {
                                modFile.getParentFile().mkdirs();
                            }
                            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modFile));
                            out.writeObject(mlist);
                            out.close();
                            Element modFileNameElement = xmlDoc.createElement("modFileName");
                            modFileNameElement.setAttribute("file", modFileName);
                            rootElement.appendChild(modFileNameElement);
                        } catch (Exception e) {
                        }
                    }
                }
            }

            Element configElement = xmlDoc.createElement("config");
            rootElement.appendChild(configElement);

            Element configVersionElement = xmlDoc.createElement("configVersion");
            configVersionElement.appendChild(xmlDoc.createTextNode("8"));
            configElement.appendChild(configVersionElement);

            Element changelogVersionElement = xmlDoc.createElement("changelogVersion");
            changelogVersionElement.appendChild(xmlDoc.createTextNode(String.valueOf(ChangeLog.getVersion())));
            configElement.appendChild(changelogVersionElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xmlDoc);
            StreamResult result = new StreamResult(f);
            transformer.transform(source, result);

            if (this.isVisible()) {
                ManagerConfig.mainWindowWidth = this.getWidth();
                ManagerConfig.mainWindowHeight = this.getHeight();
            }

            FileOutputStream fos = new FileOutputStream("data" + File.separator + "ManagerConfig.object");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new ManagerConfig());
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            //LOGGER.error("", ex);
            ErrorLog.log(ex);
        }
    }

    void loadConfigFile() {
        int changelogVersion = -1;
        try {
            try {
                FileInputStream fis = new FileInputStream("data" + File.separator + "ManagerConfig.object");
                ObjectInputStream ois = new ObjectInputStream(fis);
                ois.readObject();
                ois.close();
            } catch (Exception e) {
            }
            if (ManagerConfig.locale == -1) {
                ManagerConfig.localeSelected = false;
                String locale = System.getProperty("user.language").toLowerCase();
                ManagerConfig.locale = Locale.locales.indexOf(locale);
                if (ManagerConfig.locale == -1) {
                    ManagerConfig.locale = 0;
                }
            } else {
                ManagerConfig.localeSelected = true;
            }
            File stocks = new File("data" + File.separator + "config.xml");
            if (stocks.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(stocks);
                doc.getDocumentElement().normalize();
                NodeList nodes = doc.getElementsByTagName("modFileName");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        try {
                            Element element = (Element) node;
                            String modFileName = element.getAttribute("file");

                            FileInputStream f_in = new FileInputStream(modFileName);

                            ObjectInputStream obj_in = new ObjectInputStream(f_in);

                            Mod mod = (Mod) obj_in.readObject();
                            mod.setStatus("");
                            setMod(mod);
                        } catch (Exception e) {
                        }
                    }
                }

                nodes = doc.getElementsByTagName("config");
                if (nodes.getLength() > 0) {
                    Node node = nodes.item(0);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;

                        String changelogValue = getNodeValue("changelogVersion", element);
                        if (changelogValue != null) {
                            changelogVersion = Integer.parseInt(changelogValue);
                        }

                        int configVersion = Integer.parseInt(getNodeValue("configVersion", element));
                        if (configVersion == 8) {
                            // Config is OK.
                        } else {
                            if (configVersion <= 4) {
                                Mod mod;
                                int tryouts = 0;
                                do {
                                    mod = new Mod("Module manager dll", ManagerConfig.moduleManagerLink, true);
                                    if (mod == null) {
                                        tryouts++;
                                        if (tryouts > 20) {
                                            alertBox(null, getLocalised("ERROR_UPDATING_CONFIG"));
                                            System.exit(0);
                                        }
                                    }
                                } while (mod == null);
                                for (Mod m : modList) {
                                    if (m.getId().equals(mod.getId())) {
                                        m.isMM = true;
                                    }
                                }
                            }
                            if (configVersion < 5) {
                                for (Mod m : modList) {
                                    if (m.getLastDate() == null) {
                                        m.setLastDate(new Date());
                                    }
                                }
                            }
                            if (configVersion == 6) {
                                for (Mod m : modList) {
                                    if (m.getType() == TYPE_CURSEFORGE) {
                                        String id = m.getUnprefixedId();
                                        int index = id.indexOf("-");
                                        if (index > -1) {
                                            id = id.substring(index);
                                            m.setId(id);
                                        }
                                    }
                                }
                            }
                            if (configVersion <= 7) {
                                for (Mod m : modList) {
                                    m.setSaved(true);
                                }
                            }
                            listUpdate();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //LOGGER.error("", ex);
            ErrorLog.log(ex);
        }
        if (changelogVersion > -1 && ChangeLog.anyChanges(changelogVersion)) {
            JOptionPane.showMessageDialog(null, "Changelog:" + ChangeLog.get(changelogVersion), "New version!", JOptionPane.PLAIN_MESSAGE);
        }
        File f = new File(ManagerConfig.kspDataFolder);
        if (ManagerConfig.kspDataFolder.equals("") || !f.exists() || !f.isDirectory()) {
            if (ManagerConfig.selectKspFolder() == false) {
                System.exit(0);
            }
        }
        saveConfigFile();
    }

    void openConfigWindow() {
        ManagerConfig.change();
        saveConfigFile();
    }

    private static String getNodeValue(String tag, Element element) {
        try {
            NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
            Node node = (Node) nodes.item(0);
            return node.getNodeValue();
        } catch (Exception e) {
        }
        return null;
    }

    public void checkVersion() {
        boolean updateFound = false;
        String LMMversion = "v0.1.8.7alpha";
        try {
            org.jsoup.nodes.Document doc = Http.get("http://forum.kerbalspaceprogram.com/threads/78861").parse();
            org.jsoup.nodes.Element title = doc.select("span[class=threadtitle]").first();
            if (title != null) {
                String v = title.text();
                int index = v.lastIndexOf("-");
                if (index > -1) {
                    v = v.substring(index + 1).trim();
                    if (!LMMversion.equals(v)) {
                        int reply = JOptionPane.showConfirmDialog(null, getLocalised("NEW_MANAGER_UPDATE"), getLocalised("NEW_MANAGER_UPDATE_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (reply == JOptionPane.YES_OPTION) {
                            JOptionPane.showMessageDialog(null, getLocalised("UPDATING_TEXT"), getLocalised("UPDATING_TITLE"), JOptionPane.PLAIN_MESSAGE);
                            updateFound = true;
                            org.jsoup.nodes.Element posts = doc.select("ol[id=posts]").first();
                            if (posts != null) {
                                org.jsoup.nodes.Element post = posts.select("li[id^=post_]").first();
                                if (post != null) {
                                    org.jsoup.nodes.Element linkEl = post.select("a[href*=.zip]").first();
                                    if (linkEl != null) {
                                        String link = linkEl.attr("href");
                                        if (link.length() > 0) {
                                            String filename = downloadFile(link, null);
                                            if (filename != null) {
                                                File f = new File("temp" + File.separator + filename);
                                                boolean error = false;
                                                try {
                                                    Zip.extract("temp" + File.separator + filename, "temp" + File.separator + "LMMupdate");
                                                } catch (Exception e) {
                                                    //LOGGER.error("", e);
                                                    ErrorLog.log(e);
                                                    error = true;
                                                }
                                                if (error == false) {

                                                    final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

                                                    final ProcessBuilder builder = new ProcessBuilder(javaBin, "-jar", "temp" + File.separator + "LMMupdate" + File.separator + "LlorxKspModManager.jar", "-u");
                                                    builder.start();

                                                    System.exit(0);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //LOGGER.error("", e);
            ErrorLog.log(e);
        }
        if (updateFound == true) {
            JOptionPane.showMessageDialog(null, getLocalised("ERROR_UPDATING_MANAGER"), getLocalised("ERROR_TITLE"), JOptionPane.PLAIN_MESSAGE);
        }
    }

    public static void main(String[] ar) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                //LOGGER.error("", e);
                ErrorLog.log(e);
            }
        });

        if ((new File("errors.txt")).exists()) {
            DirIO.clearDir("errors.txt");
        }

        if (ar.length > 0 && ar[0].equals("-u")) {
            try {
                int erros = 0;
                while (true) {
                    try {
                        Files.copy(Paths.get("temp" + File.separator + "LMMupdate" + File.separator + "LlorxKspModManager.jar"), Paths.get("LlorxKspModManager.jar"), StandardCopyOption.REPLACE_EXISTING);
                        break;
                    } catch (Exception e) {
                        erros++;
                        if (erros > 20) {
                            JOptionPane.showMessageDialog(null, getLocalised("ERROR_UPDATING_MANAGER"), getLocalised("ERROR_TITLE"), JOptionPane.PLAIN_MESSAGE);
                            System.exit(0);
                        }
                        Thread.sleep(500);
                    }
                }

                final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

                final ProcessBuilder builder = new ProcessBuilder(javaBin, "-jar", "LlorxKspModManager.jar");
                builder.start();

                System.exit(0);
            } catch (Exception e) {
                //LOGGER.error("", e);
                ErrorLog.log(e);
            }
            System.exit(0);
        } else {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            if ((new File("temp")).exists()) {
                DirIO.clearDir("temp");
            }

            Main window = new Main();
            window.setSize(ManagerConfig.mainWindowWidth, ManagerConfig.mainWindowHeight);
            window.setResizable(true);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setVisible(true);

            window.setLocationRelativeTo(null);

            window.checkVersion();
        }
    }
}
