package llorx.kspModManager.mod;

import llorx.kspModManager.mod.Type;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.*;

import llorx.kspModManager.parse.ModDataParser;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.UUID;
import llorx.kspModManager.Http;

public class Mod implements Serializable {

    static final long serialVersionUID = 2874434645244941775L;

    private UUID uniqueId = UUID.randomUUID();
    private String id = "";
    private String name = "";
    private String status = "";
    private String version = "";
    private List<ModFile> installFiles = new ArrayList<>();
    private String link = "";
    private String downloadLink = "";
    private boolean updatable = true;
    private Type type = Type.TYPE_NONE;
    private boolean installable = false;
    private boolean saved = false;

    public boolean isMM = false;

    public boolean isValid = false;

    private Date lastDate;

    public transient String downloadedFile = "";
    public transient boolean nameChanged = false;
    public transient boolean justUpdated = false;
    public transient boolean errorUpdate = false;
    private transient boolean working = false;
    public transient boolean stopWork = false;

    // Gets
    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getPrefix() {
        String prefix = "";
        switch (this.getType()) {
            case TYPE_SPACEPORT:
                prefix = "SpacePort [DEPRECATED]";
                break;
            case TYPE_KSPFORUM:
                prefix = "KspForum";
                break;
            case TYPE_JENKINS:
                prefix = "Jenkins";
                break;
            case TYPE_GITHUB:
                prefix = "GitHub";
                break;
            case TYPE_BITBUCKET:
                prefix = "BitBucket";
                break;
            case TYPE_DROPBOX_FOLDER:
                prefix = "Dropbox";
                break;
            case TYPE_CURSEFORGE:
                prefix = "CurseForge";
                break;
            case TYPE_CURSE:
                prefix = "Curse.com";
                break;
            case TYPE_KERBAL_SPACE_PARTS:
                prefix = "Kerbal Space Parts";
                break;
        }
        return prefix;
    }

    public String getId() {
        return this.getPrefix() + "_" + this.id;
    }

    public String getUnprefixedId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getStatus() {
        return this.status;
    }

    public String getVersion() {
        return this.version;
    }

    public String getLink() {
        return this.link;
    }

    public String getDownloadLink() {
        return this.downloadLink;
    }

    public String getDownloadedFile() {
        return this.downloadedFile;
    }

    public boolean isUpdatable() {
        return this.updatable;
    }

    public Type getType() {
        return this.type;
    }

    public List<ModFile> getInstalledFiles() {
        return this.installFiles;
    }

    public boolean isInstallable() {
        return this.installable;
    }

    public boolean isSaved() {
        return this.saved;
    }

    public Date getLastDate() {
        return this.lastDate;
    }

    // Sets
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.nameChanged = true;
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setInstallable(boolean installable) {
        this.installable = installable;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public final void setLastDate(Date date) {
        this.lastDate = date;
    }

    public boolean addInstalledFile(Path path, boolean updated) {
        return addInstalledFile(new ModFile(path, updated));
    }

    public boolean addInstalledFile(ModFile file) {
        boolean found = false;
        for (ModFile f : this.installFiles) {
            try {
                if (Files.isSameFile(f.getPath(), file.getPath())) {
                    found = true;
                    break;
                }
            } catch (Exception e) {
            }
        }
        if (found == false) {
            this.installFiles.add(file);
        }
        return !found;
    }

    public void clearInstalledFiles() {
        this.installFiles = new ArrayList<>();
    }

    public boolean checkVersion() {
        String oldVersion = this.getVersion();
        this.reloadMod(this.getLink());
        return !oldVersion.equals(this.getVersion());
    }

    public void setWork(boolean work, Object lock) {
        synchronized (lock) {
            this.working = work;
            if (this.working == false) {
                lock.notify();
            }
        }
    }

    public boolean getWork() {
        return this.working;
    }

    public boolean continueWork(Object lock) {
        synchronized (lock) {
            if (this.stopWork == true) {
                lock.notify();
                return false;
            }
            return true;
        }
    }

    public void stopWork(Object lock) {
        synchronized (lock) {
            this.stopWork = true;
            while (this.working == true) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
            this.stopWork = false;
        }
    }

    public void reloadMod(String link) {
        this.reloadMod(this.getName(), link, this.isInstallable());
    }

    public final void reloadMod(String name, String link, boolean installable) {
        this.setInstallable(installable);
        this.setLink(link);
        this.setDownloadLink("");
        this.setName(name);
        this.isValid = false;
        try {
            if (link.length() == 0) {
                this.setType(Type.TYPE_NONE);
            } else {
                Response res = null;
                if (link.contains("kerbalspaceport.com/")) {
                    this.setType(Type.TYPE_SPACEPORT);
                } else if (link.contains("forum.kerbalspaceprogram.com/threads/")) {
                    this.setType(Type.TYPE_KSPFORUM);
                } else if (link.contains("github.com/")) {
                    this.setType(Type.TYPE_GITHUB);
                } else if (link.contains("bitbucket.org/")) {
                    this.setType(Type.TYPE_BITBUCKET);
                } else if (link.contains("dropbox.com/")) {
                    this.setType(Type.TYPE_DROPBOX_FOLDER);
                } else if (link.contains("kerbal.curseforge.com/")) {
                    this.setType(Type.TYPE_CURSEFORGE);
                } else if (link.contains("curse.com/")) {
                    this.setType(Type.TYPE_CURSE);
                } else if (link.contains("kerbal-space-parts.com/")) {
                    this.setType(Type.TYPE_KERBAL_SPACE_PARTS);
                } else {
                    if (Http.fileType(link) == Http.HTML) {
                        res = Http.get(link);
                        Document doc = res.parse();
                        Element el = doc.select("img[src$=search.png]").first();
                        if (el != null) {
                            el = el.parent();
                            if (el != null) {
                                String href = el.attr("abs:href");
                                if (!href.equals("")) {
                                    if (!href.contains("lastSuccessfulBuild")) {
                                        href = href + "/lastSuccessfulBuild";
                                    }
                                    res = Http.get(href + "/api/xml");
                                    if (res.statusCode() == 200) {
                                        this.setType(Type.TYPE_JENKINS);
                                        link = href;
                                        this.setLink(link);
                                    }
                                }
                            }
                        }
                    }
                    if (this.getType() == Type.TYPE_NONE) {
                        this.setType(Type.TYPE_LINK);
                    }
                }
                ModDataParser.parseModData(this, res);
            }
        } catch (Exception e) {
        }
    }

    public Mod(String name, String link, boolean installable) {
        this.reloadMod(name, link, installable);
        this.setLastDate(new Date());
    }

    public static class ModFile implements Serializable {

        private String path;
        private boolean updated;

        public ModFile(Path path, boolean update) {
            this.setPath(path);
            this.setUpdated(updated);
        }

        public Path getPath() {
            return Paths.get(this.path);
        }

        public boolean isUpdated() {
            return this.updated;
        }

        public final void setPath(Path path) {
            String p = "";
            try {
                p = path.toFile().getCanonicalPath();
            } catch (Exception e) {
            }
            this.path = p;
        }

        public final void setUpdated(boolean updated) {
            this.updated = updated;
        }
    }
}
