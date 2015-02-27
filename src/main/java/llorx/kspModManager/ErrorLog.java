package llorx.kspModManager;

import java.awt.HeadlessException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.swing.JOptionPane;

public class ErrorLog {

    public static void log(Throwable e) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("errors.txt", true));
            e.printStackTrace(ps);
            JOptionPane.showMessageDialog(null, Strings.get(Strings.ERROR_OCCURRED_SEND), Strings.get(Strings.ERROR), JOptionPane.PLAIN_MESSAGE);
        } catch (FileNotFoundException | HeadlessException ee) {
        }
    }
}
