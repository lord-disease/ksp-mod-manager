/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager.utils;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import static java.util.ResourceBundle.getBundle;
import llorx.kspModManager.ManagerConfig;

/**
 *
 * @author disease
 */
public class Locale {

    public static final List<String> locales = asList(new String[]{"en", "es", "de"});

    public final static ResourceBundle locale = getBundle("locale");

    public final static String getLocalised(String key) {
        try {
            return locale.getString("locale." + locales.get(ManagerConfig.locale)+"."+key);
        } catch (MissingResourceException e) {
            return locale.getString("locale.en."+key);
        }
    }
}
