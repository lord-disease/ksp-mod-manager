package llorx.kspModManager;

import java.awt.HeadlessException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.swing.JOptionPane;
import static llorx.kspModManager.utils.Locale.getLocalised;

public class ErrorLog {

    public static void log(Throwable e) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("errors.txt", true));
            e.printStackTrace(ps);
            JOptionPane.showMessageDialog(null, getLocalised("ERROR_OCCURRED_SEND"), getLocalised("ERROR"), JOptionPane.PLAIN_MESSAGE);
        } catch (FileNotFoundException | HeadlessException ee) {
        }
    }
}
