package llorx.kspModManager;

import llorx.kspModManager.mod.Mod;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import static llorx.kspModManager.utils.Locale.getLocalised;

public class Browser {

    WebEngine webEngine;
    Worker webWorker;

    int width = 0;
    int height = 0;

    public String lastClick = "";
    public HttpURLConnection downloadFile = null;
    public boolean modReloaded = false;

    JLabel loading;

    int dots = 0;
    JDialog dialog;

    Mod mod;

    public void show(String url) {
        show(url, null);
    }

    public void show(String url, Mod m) {
        Platform.setImplicitExit(false);
        mod = m;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screenSize.getWidth();
        height = (int) screenSize.getHeight();

        if (width > 1024 || width == 0) {
            width = 1024;
        }

        if (height > 768 || height == 0) {
            height = 768;
        }

        dialog = new JDialog();

        dialog.getContentPane().setLayout(null);

        JFXPanel fxPanel = new JFXPanel();

        loading = new JLabel("", JLabel.CENTER);
        dialog.add(loading);
        loading.setSize(new Dimension(150, 30));
        loading.setLocation(new Point((width / 2) - (loading.getWidth() / 2), (height / 2) - (loading.getHeight() / 2)));
        loading.setOpaque(true);
        loading.setVisible(false);

        LineBorder line = new LineBorder(Color.darkGray, 1);
        loading.setBorder(line);

        final JButton jButton = new JButton("<< " + getLocalised("BROWSER_BACK"));
        dialog.add(jButton);
        jButton.setSize(new Dimension(100, 27));
        jButton.addActionListener(e -> {
            Platform.runLater(() -> {
                webEngine.executeScript("history.back()");
            });
        });

        dialog.add(fxPanel);
        dialog.setModal(true);

        fxPanel.setSize(new Dimension(width, height));

        dialog.getContentPane().setPreferredSize(new Dimension(width, height));
        dialog.pack();
        dialog.setResizable(false);

        Platform.runLater(() -> {
            initFX(fxPanel, url);
        });

        Point middle = new Point((int) screenSize.getWidth() / 2, (int) screenSize.getHeight() / 2);
        Point newLocation = new Point(middle.x - (dialog.getWidth() / 2),
                middle.y - (dialog.getHeight() / 2));
        dialog.setLocation(newLocation);

        dialog.setVisible(true);
    }

    private void checkLinkChange() {
        String[][] patterns = {{
            "CurseForge",
            "(.*)(kerbal.curseforge.com\\/)([^\\/]*)(\\/)(\\d*)([^\\/]*)"
        }, {
            "Curse",
            "(.*)(curse.com\\/)([^\\/]*)(\\/kerbal\\/)(\\d*)([^\\/]*)"
        }, {
            "Kerbal Space Parts",
            "(.*)(kerbal-space-parts.com\\/space\\/)([^\\/]*)(\\.html)"
        }, {
            "GitHub",
            "(.*)(github.com\\/)([^\\/]*)(\\/[^\\/]*)(\\/releases)"
        }};
        String link = lastClick;
        for (String[] p : patterns) {
            String name = p[0];
            String pat = p[1];
            if (link.matches(pat + "(.*)")) {
                Pattern pattern = Pattern.compile(pat);
                Matcher matcher = pattern.matcher(link);
                if (matcher.find()) {
                    link = matcher.group(0);
                    int reply = JOptionPane.showConfirmDialog(null, getLocalised("LINK_DETECTED_ASK").replace("%SERVERNAME%", name), getLocalised("LINK_DETECTED_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (reply == JOptionPane.YES_OPTION) {
                        modReloaded = true;
                        mod.reloadMod(link);
                        mod.setLastDate(new Date());
                        Platform.runLater(() -> {
                            webWorker.cancel();
                            SwingUtilities.invokeLater(dialog::dispose);
                        });
                    }
                }
            }
        }
    }

    private void initFX(JFXPanel fxPanel, String url) {

        Group group = new Group();
        Scene scene = new Scene(group);
        fxPanel.setScene(scene);

        WebView webView = new WebView();

        group.getChildren().add(webView);
        webView.setMinSize(width, height);
        webView.setMaxSize(width, height);
        webView.setContextMenuEnabled(false);

        webEngine = webView.getEngine();
        webWorker = webEngine.getLoadWorker();

        webWorker.stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == State.SUCCEEDED || newState == State.CANCELLED || newState == State.FAILED) {
                loading.setVisible(false);
                try {
                    HttpURLConnection conn = Http.getConnection(lastClick);
                    int fileType = Http.fileType(conn);
                    if (modReloaded == false && fileType != Http.HTML) {
                        if (fileType == Http.ZIP_EXTENSION) {
                            int reply = JOptionPane.showConfirmDialog(null, getLocalised("LINK_SELECTED_ASK").replace("%LINKCLICKED%", lastClick), getLocalised("SURE_ASK"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (reply == JOptionPane.YES_OPTION) {
                                downloadFile = conn;
                                Platform.runLater(() -> {
                                    webWorker.cancel();
                                    SwingUtilities.invokeLater(dialog::dispose);
                                });
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, getLocalised("FILE_NOT_SUPPORTED"), getLocalised("FILE_NOT_SUPPORTED_TITLE"), JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                }
            } else if (newState == State.RUNNING) {
                loading.setVisible(true);
                dots = 0;
            }
        });

        webWorker.progressProperty().addListener((ov, oldState, newState) -> {
            String dotsString = "";
            for (int i = 0; i < 15; i++) {
                if (i == dots) {
                    dotsString = dotsString + "|";
                } else {
                    dotsString = dotsString + "-";
                }
            }
            dots++;
            if (dots >= 15) {
                dots = 0;
            }
            loading.setText(dotsString);
        });

        webEngine.locationProperty().addListener((observableValue, oldLoc, newLoc) -> {
            lastClick = newLoc;
            loading.setVisible(true);
            try {
                if (mod != null) {
                    checkLinkChange();
                }
            } catch (Exception e) {
            }
        });

        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
        webEngine.load(url);
    }
}
