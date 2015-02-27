/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager.mod;

/**
 *
 * @author disease
 */
public enum Type {

    TYPE_NONE(-1), TYPE_SPACEPORT(0), TYPE_KSPFORUM(1), TYPE_JENKINS(2), TYPE_GITHUB(3), TYPE_BITBUCKET(4), TYPE_DROPBOX_FOLDER(5), TYPE_CURSEFORGE(6), TYPE_CURSE(7), TYPE_KERBAL_SPACE_PARTS(8), TYPE_LINK(1000);
    public final int value;

    private Type(int value) {
        this.value = value;
    }

}
