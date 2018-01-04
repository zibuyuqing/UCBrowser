package com.zibuyuqing.ucbrowser.model.bean.favorite;

import java.util.ArrayList;

/**
 * Created by Xijun.Wang on 2017/12/19.
 */

public class FavoriteFolderInfo extends ItemInfo{
    private ArrayList<FavoriteShortcutInfo> contents = new ArrayList<>();

    public ArrayList<FavoriteShortcutInfo> getContents() {
        return contents;
    }
    public void addItem(FavoriteShortcutInfo info){
        contents.add(info);
    }
    public void removeItem(FavoriteShortcutInfo info){
        contents.remove(info);
    }
}
