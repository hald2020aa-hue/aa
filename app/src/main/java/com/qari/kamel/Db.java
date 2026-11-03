package com.qari.kamel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class Db extends SQLiteOpenHelper {
    Db(Context ctx){super(ctx,"qari_kamel.db",null,1);}
    @Override public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE sources(id TEXT PRIMARY KEY,json TEXT NOT NULL)");
        d.execSQL("CREATE TABLE manga(id INTEGER PRIMARY KEY AUTOINCREMENT,source TEXT,title TEXT,url TEXT UNIQUE,cover TEXT,favorite INTEGER DEFAULT 0,updated INTEGER DEFAULT 0)");
        d.execSQL("CREATE TABLE chapters(id INTEGER PRIMARY KEY AUTOINCREMENT,manga_id INTEGER,title TEXT,url TEXT UNIQUE,idx INTEGER,pages TEXT DEFAULT '[]',progress INTEGER DEFAULT 0,download_status INTEGER DEFAULT 0)");
        d.execSQL("CREATE INDEX manga_source ON manga(source)");
        d.execSQL("CREATE INDEX chapter_manga ON chapters(manga_id)");
    }
    @Override public void onUpgrade(SQLiteDatabase d,int oldVersion,int newVersion) { /* Migrations added as schema evolves. */ }
    synchronized void saveSource(Models.Source s) throws Exception {
        ContentValues v=new ContentValues();v.put("id",s.id);v.put("json",s.json().toString());getWritableDatabase().insertWithOnConflict("sources",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    synchronized void deleteSource(String id){getWritableDatabase().delete("sources","id=?",new String[]{id});}
    synchronized List<Models.Source> sources(){
        List<Models.Source> list=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT json FROM sources ORDER BY id",null)){
            while(c.moveToNext())try{list.add(Models.Source.from(new JSONObject(c.getString(0))));}catch(Exception ignored){}
        }return list;
    }
    synchronized Models.Source source(String id){
        try(Cursor c=getReadableDatabase().rawQuery("SELECT json FROM sources WHERE id=?",new String[]{id})){
            if(c.moveToFirst())return Models.Source.from(new JSONObject(c.getString(0)));
        }catch(Exception ignored){}return null;
    }
    synchronized long upsertManga(Models.Manga m){
        SQLiteDatabase d=getWritableDatabase();
        try(Cursor c=d.rawQuery("SELECT id FROM manga WHERE url=?",new String[]{m.url})){if(c.moveToFirst()){long id=c.getLong(0);
            ContentValues v=new ContentValues();v.put("title",m.title);v.put("cover",m.cover);v.put("updated",System.currentTimeMillis());d.update("manga",v,"id=?",new String[]{String.valueOf(id)});return id;}}
        ContentValues v=new ContentValues();v.put("source",m.sourceId);v.put("title",m.title);v.put("url",m.url);v.put("cover",m.cover);v.put("favorite",m.favorite?1:0);v.put("updated",System.currentTimeMillis());return d.insert("manga",null,v);
    }
    synchronized Models.Manga manga(long id){
        try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM manga WHERE id=?",new String[]{""+id})){return c.moveToFirst()?readManga(c):null;}
    }
    private Models.Manga readManga(Cursor c){Models.Manga m=new Models.Manga();m.id=c.getLong(c.getColumnIndexOrThrow("id"));m.sourceId=c.getString(c.getColumnIndexOrThrow("source"));m.title=c.getString(c.getColumnIndexOrThrow("title"));m.url=c.getString(c.getColumnIndexOrThrow("url"));m.cover=c.getString(c.getColumnIndexOrThrow("cover"));m.favorite=c.getInt(c.getColumnIndexOrThrow("favorite"))==1;return m;}
    synchronized List<Models.Manga> mangas(boolean favorites){
        List<Models.Manga> out=new ArrayList<>();String where=favorites?" WHERE favorite=1":"";
        try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM manga"+where+" ORDER BY updated DESC",null)){while(c.moveToNext())out.add(readManga(c));}return out;
    }
    synchronized void favorite(long id,boolean yes){ContentValues v=new ContentValues();v.put("favorite",yes?1:0);getWritableDatabase().update("manga",v,"id=?",new String[]{""+id});}
    synchronized long upsertChapter(Models.Chapter ch){
        SQLiteDatabase d=getWritableDatabase();try(Cursor c=d.rawQuery("SELECT id FROM chapters WHERE url=?",new String[]{ch.url})){
            if(c.moveToFirst()){long id=c.getLong(0);ContentValues v=new ContentValues();v.put("title",ch.title);v.put("idx",ch.number);d.update("chapters",v,"id=?",new String[]{""+id});return id;}}
        ContentValues v=new ContentValues();v.put("manga_id",ch.mangaId);v.put("title",ch.title);v.put("url",ch.url);v.put("idx",ch.number);v.put("pages",ch.pages);return d.insert("chapters",null,v);
    }
    private Models.Chapter readChapter(Cursor c){Models.Chapter ch=new Models.Chapter();ch.id=c.getLong(c.getColumnIndexOrThrow("id"));ch.mangaId=c.getLong(c.getColumnIndexOrThrow("manga_id"));ch.title=c.getString(c.getColumnIndexOrThrow("title"));ch.url=c.getString(c.getColumnIndexOrThrow("url"));ch.number=c.getInt(c.getColumnIndexOrThrow("idx"));ch.pages=c.getString(c.getColumnIndexOrThrow("pages"));ch.progress=c.getInt(c.getColumnIndexOrThrow("progress"));ch.downloadStatus=c.getInt(c.getColumnIndexOrThrow("download_status"));return ch;}
    synchronized Models.Chapter chapter(long id){try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM chapters WHERE id=?",new String[]{""+id})){return c.moveToFirst()?readChapter(c):null;}}
    synchronized List<Models.Chapter> chapters(long mangaId){List<Models.Chapter> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM chapters WHERE manga_id=? ORDER BY idx DESC",new String[]{""+mangaId})){while(c.moveToNext())out.add(readChapter(c));}return out;}
    synchronized List<Models.Chapter> downloads(){List<Models.Chapter> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM chapters WHERE download_status!=0 ORDER BY id DESC",null)){while(c.moveToNext())out.add(readChapter(c));}return out;}
    synchronized void pages(long chId,JSONArray list){ContentValues v=new ContentValues();v.put("pages",list.toString());getWritableDatabase().update("chapters",v,"id=?",new String[]{""+chId});}
    synchronized void progress(long chId,int p){ContentValues v=new ContentValues();v.put("progress",p);getWritableDatabase().update("chapters",v,"id=?",new String[]{""+chId});}
    synchronized void downloadStatus(long chId,int status){ContentValues v=new ContentValues();v.put("download_status",status);getWritableDatabase().update("chapters",v,"id=?",new String[]{""+chId});}
    synchronized JSONObject backup() throws Exception {
        JSONObject json=new JSONObject();JSONArray src=new JSONArray(),lib=new JSONArray(),chap=new JSONArray();
        for(Models.Source s:sources())src.put(s.json());
        for(Models.Manga m:mangas(false)){JSONObject j=new JSONObject();j.put("source",m.sourceId);j.put("title",m.title);j.put("url",m.url);j.put("cover",m.cover);j.put("favorite",m.favorite);lib.put(j);
            for(Models.Chapter ch:chapters(m.id)){JSONObject cj=new JSONObject();cj.put("mangaUrl",m.url);cj.put("title",ch.title);cj.put("url",ch.url);cj.put("number",ch.number);cj.put("pages",ch.pages);cj.put("progress",ch.progress);chap.put(cj);}}
        json.put("format","qari-kamel-1");json.put("sources",src);json.put("manga",lib);json.put("chapters",chap);return json;
    }
    synchronized void restore(JSONObject j) throws Exception {
        JSONArray src=j.optJSONArray("sources");if(src!=null)for(int i=0;i<src.length();i++)saveSource(Models.Source.from(src.getJSONObject(i)));
        JSONArray lib=j.optJSONArray("manga");if(lib!=null)for(int i=0;i<lib.length();i++){
            JSONObject x=lib.getJSONObject(i);Models.Manga m=new Models.Manga();m.sourceId=x.optString("source");m.title=x.optString("title");m.url=x.optString("url");m.cover=x.optString("cover");m.favorite=x.optBoolean("favorite");long id=upsertManga(m);if(m.favorite)favorite(id,true);
        }
        JSONArray chap=j.optJSONArray("chapters");if(chap!=null)for(int i=0;i<chap.length();i++){
            JSONObject x=chap.getJSONObject(i);String url=x.optString("mangaUrl");long mangaId=-1;
            try(Cursor c=getReadableDatabase().rawQuery("SELECT id FROM manga WHERE url=?",new String[]{url})){if(c.moveToFirst())mangaId=c.getLong(0);}
            if(mangaId<0)continue;Models.Chapter ch=new Models.Chapter();ch.mangaId=mangaId;ch.title=x.optString("title");ch.url=x.optString("url");ch.number=x.optInt("number");ch.pages=x.optString("pages","[]");long id=upsertChapter(ch);pages(id,new JSONArray(ch.pages));progress(id,x.optInt("progress"));
        }
    }
}
