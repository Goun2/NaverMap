// 북마크 리스트
package com.test.navermap;

public class BookmarkList {

    String list_name;

    public BookmarkList(String list_name){
        this.list_name=list_name;
    }

    public String getList_name(){
        return this.list_name;
    }

    @Override
    public String toString(){
        return list_name;
    }
}
