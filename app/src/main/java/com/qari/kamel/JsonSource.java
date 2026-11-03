package com.qari.kamel;

import org.json.*;
import java.net.URL;
import java.util.*;

/** Configurable JSON/REST response adapter; no hard-coded content provider. */
final class JsonSource {
    private JsonSource(){}
    static Object path(Object root,String dot) {
        if(dot==null||dot.trim().isEmpty())return root;
        Object node=root;
        for(String key:dot.split("\\.")){
            if(node instanceof JSONObject)node=((JSONObject)node).opt(key);
            else if(node instanceof JSONArray){try{node=((JSONArray)node).opt(Integer.parseInt(key));}catch(NumberFormatException e){return null;}}
            else return null;
            if(node==JSONObject.NULL)return null;
        }return node;
    }
    static String string(Object row,String path){Object o=path(row,path);return o==null?"":String.valueOf(o);}
    static String url(String base,String value){
        if(value==null||value.isEmpty())return "";
        try{String resolved=new URL(new URL(base),value).toString();Net.checked(resolved);return resolved;}catch(Exception e){return "";}
    }
    private static JSONArray array(Object x){return x instanceof JSONArray?(JSONArray)x:new JSONArray();}
    private static JSONObject fetch(String url)throws Exception{
        byte[] bytes=Net.fetch(url,url,5*1024*1024);
        return new JSONObject(new String(bytes,java.nio.charset.StandardCharsets.UTF_8));
    }
    static List<Models.Manga> catalog(Models.Source s,String query,int page)throws Exception{
        String endpoint=Net.queryUrl(s,query,page);JSONArray rows=array(path(fetch(endpoint),s.itemsPath));
        List<Models.Manga> result=new ArrayList<>();
        for(int i=0;i<rows.length();i++){
            Object row=rows.opt(i);Models.Manga m=new Models.Manga();m.sourceId=s.id;
            m.title=string(row,s.jsonTitlePath);m.url=url(s.baseUrl,string(row,s.jsonLinkPath));
            m.cover=url(s.baseUrl,string(row,s.jsonCoverPath));
            if(!m.title.isEmpty()&&!m.url.isEmpty())result.add(m);
        }return result;
    }
    static List<Models.Chapter> chapters(Models.Source s,Models.Manga m)throws Exception{
        JSONArray rows=array(path(fetch(m.url),s.chaptersPath));List<Models.Chapter> result=new ArrayList<>();
        for(int i=0;i<rows.length();i++){
            Object row=rows.opt(i);Models.Chapter ch=new Models.Chapter();ch.mangaId=m.id;
            ch.title=string(row,s.jsonChapterNamePath);ch.url=url(s.baseUrl,string(row,s.jsonChapterLinkPath));ch.number=i;
            if(!ch.url.isEmpty())result.add(ch);
        }return result;
    }
    static JSONArray pages(Models.Source s,String chapterUrl)throws Exception{
        JSONArray rows=array(path(fetch(chapterUrl),s.pagesPath));JSONArray result=new JSONArray();
        for(int i=0;i<rows.length();i++){
            Object row=rows.opt(i);String v=row instanceof String?(String)row:string(row,s.jsonPageLinkPath);
            v=url(s.baseUrl,v);if(!v.isEmpty())result.put(v);
        }return result;
    }
}
