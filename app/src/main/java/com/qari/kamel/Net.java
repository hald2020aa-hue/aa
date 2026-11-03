package com.qari.kamel;

import android.content.Context;
import android.webkit.CookieManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import java.io.*;
import java.net.*;
import java.util.*;

final class Net {
    private Net(){}
    static URL checked(String url) throws Exception {
        URL u=new URL(url.trim());
        if(!"https".equalsIgnoreCase(u.getProtocol()))throw new IOException("يجب أن يكون الرابط HTTPS: "+url);
        return u;
    }
    static byte[] fetch(String u,String referer,int max) throws Exception {
        URL url=checked(u);HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setConnectTimeout(18000);c.setReadTimeout(25000);c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36");
        c.setRequestProperty("Accept","*/*");if(referer!=null&&!referer.isEmpty())c.setRequestProperty("Referer",referer);
        String cookie=CookieManager.getInstance().getCookie(u);if(cookie!=null)c.setRequestProperty("Cookie",cookie);
        try{
            int status=c.getResponseCode();if(status<200||status>=300)throw new IOException("HTTP "+status+" عند "+url.getHost());
            // Block redirect downgrade and over-size responses.
            URL destination=c.getURL();checked(destination.toExternalForm());
            int len=c.getContentLength();if(len>max)throw new IOException("الملف أكبر من الحد الآمن");
            try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("الملف أكبر من الحد الآمن");out.write(b,0,n);}return out.toByteArray();
            }
        }finally{c.disconnect();}
    }
    static Document doc(String url) throws Exception {
        byte[] data=fetch(url,url,5*1024*1024);
        return Jsoup.parse(new ByteArrayInputStream(data),null,url);
    }
    static String text(Element el,String css){if(el==null)return "";Element e=css==null||css.isEmpty()?el:el.selectFirst(css);return e==null?"":e.text().trim();}
    static String abs(Element el,String css,String attr){if(el==null)return "";Element e=css==null||css.isEmpty()?el:el.selectFirst(css);if(e==null)return "";
        String v=e.attr(attr).trim();if(v.isEmpty()&&attr.startsWith("data-"))v=e.attr("src").trim();
        if(v.startsWith("//"))v="https:"+v;
        try{return v.isEmpty()?"":new URL(new URL(e.baseUri()),v).toExternalForm();}catch(Exception ex){return "";}
    }
    static String queryUrl(Models.Source s,String q,int page) throws Exception {
        String path=(q==null||q.isEmpty())?s.catalogPath:s.searchPath;
        path=path.replace("{query}",URLEncoder.encode(q==null?"":q,"UTF-8")).replace("{page}",String.valueOf(page));
        return new URL(checked(s.baseUrl),path).toExternalForm();
    }
    static List<Models.Manga> catalog(Models.Source s,String query,int page) throws Exception {
        if("json".equals(s.format))return JsonSource.catalog(s,query,page);
        Document d=doc(queryUrl(s,query,page)); List<Models.Manga> result=new ArrayList<>();
        for(Element e:d.select(s.itemSelector)){
            String title=text(e,s.titleSelector),link=abs(e,s.linkSelector,"href"),cover=abs(e,s.coverSelector,"data-src");
            if(cover.isEmpty())cover=abs(e,s.coverSelector,"src");
            if(title.isEmpty()||link.isEmpty())continue;
            Models.Manga m=new Models.Manga();m.sourceId=s.id;m.title=title;m.url=link;m.cover=cover;result.add(m);
        }return result;
    }
    static List<Models.Chapter> chapters(Models.Source s,Models.Manga manga) throws Exception {
        if("json".equals(s.format))return JsonSource.chapters(s,manga);
        Document d=doc(manga.url);List<Models.Chapter> result=new ArrayList<>();int index=0;
        for(Element e:d.select(s.chapterSelector)){
            String name=text(e,s.chapterTitleSelector),link=abs(e,s.chapterLinkSelector,"href");
            if(name.isEmpty())name=e.text();
            if(link.isEmpty())link=abs(e,"","href");
            if(link.isEmpty())continue;
            Models.Chapter ch=new Models.Chapter();ch.title=name.isEmpty()?"الفصل "+(index+1):name;
            ch.url=link;ch.mangaId=manga.id;ch.number=index++;result.add(ch);
        }return result;
    }
    static JSONArray pages(Models.Source s,String chapterUrl) throws Exception {
        if("json".equals(s.format))return JsonSource.pages(s,chapterUrl);
        Document d=doc(chapterUrl);JSONArray result=new JSONArray();Set<String> unique=new LinkedHashSet<>();
        for(Element e:d.select(s.pageSelector)){
            String url=abs(e,"",s.pageUrlAttribute);
            if(url.isEmpty())url=abs(e,"","data-original");
            if(url.isEmpty())url=abs(e,"","src");
            if(url.startsWith("https://")&&unique.add(url))result.put(url);
        }
        return result;
    }
}
