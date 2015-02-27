package llorx.kspModManager;

import java.util.LinkedHashSet;
import java.util.Set;

class ChangeLog {
	private static final String[][] changes = {
		{
			"Better Changelog output when updating: Will output changes according to the previous version.",
			"Download filename fix: No more empty downloaded files (finally...)",
			"Bandwith/Connections optimizing: Less connections to get download files.",
		},{
			"Minor mediafire download fix.",
		},{
			"Added German language.",
		},{
			"Export modlist to .txt file to show your modlist (Import still not implemented).",
		},{
			"Uninstall fix.",
		},{
			"kerbal-space-parts.com support added.",
		},
	};
	
	public static String get(int oldVersion) {
		Set<String> log = new LinkedHashSet<>();
		for (int i = oldVersion; i < ChangeLog.changes.length; i++) {
			if (ChangeLog.changes[i] != null) {
                            for (String change : ChangeLog.changes[i]) {
                                if (change != null && change.length() > 0) {
                                    log.add(" - " + change);
                                }
                            }
			}
		}
		String logString = "";
		for (String logLine: log) {
			logString = logString + "\n" + logLine;
		}
		return logString;
	}
	
	public static boolean anyChanges(int oldVersion) {
		return getVersion() > oldVersion;
	}
	
	public static int getVersion() {
		return countNotNulls();
	}
	
	private static int countNotNulls() {
		int c = 0;
            for (String[] change : ChangeLog.changes) {
                if (change != null) {
                    c++;
                }
            }
		return c;
	}
}