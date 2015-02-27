/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager;

import llorx.kspModManager.mod.Mod;
import java.util.Comparator;

/**
 *
 * @author disease
 */
public class myComparator implements Comparator<Mod> {

        @Override
        public int compare(Mod a, Mod b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }
