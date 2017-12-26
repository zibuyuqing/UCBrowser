package com.zibuyuqing.ucbrowser.model.bean.favorite;

import android.content.pm.ShortcutInfo;

import java.util.ArrayList;

/**
 * Created by Xijun.Wang on 2017/12/19.
 */

public class FavoriteFolderInfo extends ItemInfo{
    private ArrayList<ShortcutInfo> infos;

    public ArrayList<ShortcutInfo> getInfos() {
        return infos;
    }

    public void setInfos(ArrayList<ShortcutInfo> infos) {
        this.infos = infos;
    }
    public void addItem(ShortcutInfo info){
        infos.add(info);
    }
    public void removeItem(ShortcutInfo info){
        infos.remove(info);
    }
}
