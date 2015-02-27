package llorx.kspModManager;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

public class Http {

    public static final int HTML = -1;
    public static final int ZIP_EXTENSION = 0;
    public static final int OTHER_EXTENSION = 1;

    public static Connection.Response get(String link) {
        int tryouts = 0;
        while (tryouts < 10) {
            tryouts++;
            try {
                Connection.Response res = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36").method(Connection.Method.GET).followRedirects(false).execute();
                String newUrl = parseLocation(res.header("Location"), link);
                if (newUrl != null) {
                    return Http.get(newUrl);
                } else {
                    return res;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static int fileType(String link) {
        return Http.fileType(Http.getConnection(link));
    }

    public static int fileType(URLConnection conn) {
        if (conn != null) {
            String link = conn.getURL().getFile();
            String type = conn.getHeaderField("Content-Type");
            if (type.contains("application/")) {
                if (type.contains("application/zip") || type.contains("application/x-zip-compressed")) {
                    return Http.ZIP_EXTENSION;
                } else {
                    String filename = Http.parseFileHeader(conn, null);
                    if (filename != null) {
                        filename.toLowerCase();
                        if (filename.endsWith(".zip")) {
                            return Http.ZIP_EXTENSION;
                        } else {
                            return Http.OTHER_EXTENSION;
                        }
                    } else {
                        int index = link.lastIndexOf(".");
                        if (index > -1) {
                            String ex = link.substring(index);
                            index = ex.lastIndexOf("?");
                            int index2 = ex.lastIndexOf("#");
                            if (index > -1) {
                                if (index2 > -1 && index2 < index) {
                                    index = index2;
                                }
                                ex = link.substring(0, index);
                            } else if (index2 > -1) {
                                ex = link.substring(0, index2);
                            }
                            if (ex.toLowerCase().equals(".zip")) {
                                return Http.ZIP_EXTENSION;
                            }
                        }
                        return Http.OTHER_EXTENSION;
                    }
                }
            } else {
                return Http.HTML;
            }
        }
        return Http.HTML;
    }

    public static String parseFileHeader(URLConnection conn, String def) {
        String filename = null;
        String header = conn.getHeaderField("Content-Disposition");
        if (header != null && header.contains("=")) {
            String f = header.split("=")[1];
            if (f.contains(";")) {
                f = header.split(";")[0];
            }
            int index = f.indexOf("\"");
            if (index > -1) {
                int index2 = f.indexOf("\"", index + 1);
                if (index2 > -1) {
                    f = f.substring(index + 1, index2);
                }
            }
            index = f.indexOf("'");
            if (index > -1) {
                int index2 = f.indexOf("'", index + 1);
                if (index2 > -1) {
                    f = f.substring(index + 1, index2);
                }
            }
            if (f.length() > 0) {
                filename = f;
            }
        }
        if (filename == null) {
            String f = conn.getURL().getPath();
            int index = f.lastIndexOf("/");
            if (index > -1) {
                f = f.substring(index + 1);
            }
            if (f.contains(".")) {
                filename = f;
            }
        }
        if (filename == null) {
            filename = def;
        }
        filename = filename.replace("\\", "_");
        filename = filename.replace("/", "_");
        filename = filename.replace("\"", "");
        filename = filename.replace("'", "");
        return filename;
    }

    public static String parseLocation(String newLoc, String link) {
        if (newLoc != null) {
            if (newLoc.startsWith("//")) {
                newLoc = link.substring(0, link.indexOf("//")) + newLoc;
            } else if (!newLoc.contains("://")) {
                if (newLoc.startsWith("/")) {
                    newLoc = link.substring(0, link.indexOf("/", link.indexOf("//") + 2)) + newLoc;
                } else {
                    newLoc = link.substring(0, link.lastIndexOf("/") + 1) + newLoc;
                }
            }
        }
        return newLoc;
    }

    public static HttpURLConnection getConnection(String link) {
        int tryouts = 0;
        while (tryouts < 10) {
            link = link.replace("\\", "/");
            link = link.replace(" ", "%20");
            tryouts++;
            try {
                URL website = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) website.openConnection();
                conn.setConnectTimeout(5000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
                String newUrl = parseLocation(conn.getHeaderField("Location"), link);
                if (newUrl != null && newUrl.length() > 0) {
                    return Http.getConnection(newUrl);
                } else {
                    return conn;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static String getDownloadLink(String link) {
        Connection.Response res = Http.get(link);
        if (res != null) {
            try {
                Document doc = res.parse();
                if (link.contains("mediafire.com/")) {
                    Element d = doc.select("div[class=download_link]").first();
                    if (d != null) {
                        String body = d.html();
                        int index = body.indexOf("\"http");
                        if (index > -1) {
                            index = index + 1;
                            int index2 = body.indexOf("\"", index);
                            if (index2 > -1) {
                                String dlink = body.substring(index, index2);
                                return dlink;
                            }
                        }
                    }
                } else if (link.contains("dropbox.com/")) {
                    Element d = doc.select("a[id=default_content_download_button]").first();
                    if (d != null) {
                        String dlink = d.attr("href");
                        if (!dlink.equals("")) {
                            return dlink;
                        }
                    }
                } else if (link.contains("cubby.com/pli/")) {
                    return link.replace("/pli/", "/pl/");
                } else if (link.contains("box.com/")) {
                    Element d = doc.select("ul[data-module=header-shared]").first();
                    if (d != null) {
                        String sharedName = d.attr("data-sharedname");
                        String fileId = d.attr("data-fileid");
                        if (!sharedName.equals("") && !fileId.equals("")) {
                            return Http.parseLocation("/index.php?rm=box_download_shared_file&shared_name=" + sharedName + "&file_id=f_" + fileId, link);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
}
