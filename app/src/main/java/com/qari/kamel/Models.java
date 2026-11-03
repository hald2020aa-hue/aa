package com.qari.kamel;

import org.json.JSONArray;
import org.json.JSONObject;

final class Models {
    private Models() {}
    static class Source {
        String id="", name="", baseUrl="", searchPath="/?s={query}&page={page}",
            catalogPath="/?page={page}", itemSelector="article", titleSelector="h2",
            linkSelector="a[href]", coverSelector="img", chapterSelector=".chapter-list a[href]",
            chapterTitleSelector="", chapterLinkSelector="", pageSelector=".reading-content img",
            pageUrlAttribute="data-src", nextSelector="a.next",
            format="html", itemsPath="data.results", jsonTitlePath="title", jsonLinkPath="url", jsonCoverPath="cover",
            chaptersPath="data.chapters", jsonChapterNamePath="title", jsonChapterLinkPath="url",
            pagesPath="data.pages", jsonPageLinkPath="url";
        boolean adult;
        JSONObject json() throws Exception {
            JSONObject j=new JSONObject();
            j.put("id",id);j.put("name",name);j.put("baseUrl",baseUrl);
            j.put("searchPath",searchPath);j.put("catalogPath",catalogPath);
            j.put("itemSelector",itemSelector);j.put("titleSelector",titleSelector);
            j.put("linkSelector",linkSelector);j.put("coverSelector",coverSelector);
            j.put("chapterSelector",chapterSelector);j.put("chapterTitleSelector",chapterTitleSelector);
            j.put("chapterLinkSelector",chapterLinkSelector);j.put("pageSelector",pageSelector);
            j.put("pageUrlAttribute",pageUrlAttribute);j.put("nextSelector",nextSelector);j.put("adult",adult);
            j.put("format",format);j.put("itemsPath",itemsPath);j.put("jsonTitlePath",jsonTitlePath);j.put("jsonLinkPath",jsonLinkPath);j.put("jsonCoverPath",jsonCoverPath);
            j.put("chaptersPath",chaptersPath);j.put("jsonChapterNamePath",jsonChapterNamePath);j.put("jsonChapterLinkPath",jsonChapterLinkPath);
            j.put("pagesPath",pagesPath);j.put("jsonPageLinkPath",jsonPageLinkPath);
            return j;
        }
        static Source from(JSONObject j) {
            Source s=new Source();
            s.id=j.optString("id","");s.name=j.optString("name","");s.baseUrl=j.optString("baseUrl","");
            s.searchPath=j.optString("searchPath",s.searchPath);s.catalogPath=j.optString("catalogPath",s.catalogPath);
            s.itemSelector=j.optString("itemSelector",s.itemSelector);s.titleSelector=j.optString("titleSelector",s.titleSelector);
            s.linkSelector=j.optString("linkSelector",s.linkSelector);s.coverSelector=j.optString("coverSelector",s.coverSelector);
            s.chapterSelector=j.optString("chapterSelector",s.chapterSelector);
            s.chapterTitleSelector=j.optString("chapterTitleSelector","");s.chapterLinkSelector=j.optString("chapterLinkSelector","");
            s.pageSelector=j.optString("pageSelector",s.pageSelector);s.pageUrlAttribute=j.optString("pageUrlAttribute",s.pageUrlAttribute);
            s.nextSelector=j.optString("nextSelector",s.nextSelector);s.adult=j.optBoolean("adult",false);
            s.format=j.optString("format",s.format);s.itemsPath=j.optString("itemsPath",s.itemsPath);
            s.jsonTitlePath=j.optString("jsonTitlePath",s.jsonTitlePath);s.jsonLinkPath=j.optString("jsonLinkPath",s.jsonLinkPath);s.jsonCoverPath=j.optString("jsonCoverPath",s.jsonCoverPath);
            s.chaptersPath=j.optString("chaptersPath",s.chaptersPath);s.jsonChapterNamePath=j.optString("jsonChapterNamePath",s.jsonChapterNamePath);s.jsonChapterLinkPath=j.optString("jsonChapterLinkPath",s.jsonChapterLinkPath);
            s.pagesPath=j.optString("pagesPath",s.pagesPath);s.jsonPageLinkPath=j.optString("jsonPageLinkPath",s.jsonPageLinkPath);
            return s;
        }
    }
    static class Manga {
        long id; String sourceId="",title="",url="",cover=""; boolean favorite;
    }
    static class Chapter {
        long id,mangaId; String title="",url="",pages="[]"; int number,progress,downloadStatus;
        JSONArray pageList() {try{return new JSONArray(pages);}catch(Exception e){return new JSONArray();}}
    }
}
