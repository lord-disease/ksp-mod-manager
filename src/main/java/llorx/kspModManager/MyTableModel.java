/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager;

import llorx.kspModManager.mod.Mod;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import static llorx.kspModManager.utils.Locale.getLocalised;

/**
 *
 * @author disease
 */
public class MyTableModel extends AbstractTableModel {

    private List<Mod> mods;

    public MyTableModel(List<Mod> mods) {
        this.mods = mods;
    }

    @Override
    public int getRowCount() {
        return mods.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return getLocalised("MOD_NAME");
            case 1:
                return getLocalised("LATEST_DATE");
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return mods.get(rowIndex);
    }
}