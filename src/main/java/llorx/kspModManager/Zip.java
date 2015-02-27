/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author disease
 */
public class Zip {

    public static final int NO_MAINFOLDER = 0b00000001;
    //private static final Logger LOGGER = LoggerFactory.getLogger(Zip.class);

    public static boolean test(int flags, int mask) {
        return ((flags & mask) == mask);
    }

    public static int set(int flags, int mask) {
        return (flags |= mask);
    }

    public static int clear(int flags, int mask) {
        return (flags &= ~mask);
    }

    public static void extract(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                new File(newFile.getParent()).mkdirs();

                if (ze.isDirectory()) {
                    if (!newFile.exists()) {
                        newFile.mkdir();
                    }
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer, 0, 1024)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
            }

            zis.closeEntry();
            zis.close();
        } catch (Exception ex) {
            //LOGGER.error("", ex);
            ErrorLog.log(ex);
        }
    }

    public static int getModInfo(String zipFile, List<String> gameDatas, List<String> readmes) {
        int type = 0;
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                String filePath = ze.getName();
                File file = new File(filePath);

                if (gameDatas != null) {
                    File pFile = file;
                    String parentData = "";
                    while ((pFile = pFile.getParentFile()) != null) {
                        if (pFile.getName().toLowerCase().equals("gamedata")) {
                            parentData = pFile.getPath();
                        }
                    }
                    if (!parentData.equals("")) {
                        boolean found = false;
                        for (String d : gameDatas) {
                            if (d.equals(parentData)) {
                                found = true;
                                break;
                            }
                        }
                        if (found == false) {
                            gameDatas.add(parentData);
                        }
                    }
                }

                String fileName = file.getName();
                if (!test(type, NO_MAINFOLDER) && file.getParent() == null) {
                    String[] mainDirs = {"Parts", "Plugins"};
                    for (String dir : mainDirs) {
                        if (fileName.equals(dir)) {
                            type = set(type, NO_MAINFOLDER);
                            break;
                        }
                    }
                }

                if (!ze.isDirectory() && readmes != null) {
                    int i = fileName.lastIndexOf('.');
                    if (i > 0) {
                        String[] readableExtensions = {"txt", "asciidoc", "md"};
                        for (String ext : readableExtensions) {
                            if (ext.equals(fileName.substring(i + 1).toLowerCase())) {
                                readmes.add(filePath);
                                break;
                            }
                        }
                    }
                }
            }
            if (test(type, NO_MAINFOLDER) && gameDatas != null && gameDatas.size() > 0) {
                type = clear(type, NO_MAINFOLDER);
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception ex) {
            //LOGGER.error("", ex);
            ErrorLog.log(ex);
        }
        return type;
    }
}