package com.qari.kamel;

import android.content.Context;
import android.net.Uri;
import org.json.JSONArray;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class ArchiveImporter {
    private ArchiveImporter(){}
    private static boolean image(String file){String name=file.toLowerCase(java.util.Locale.ROOT);return name.endsWith(".jpg")||name.endsWith(".jpeg")||name.endsWith(".png")||name.endsWith(".webp")||name.endsWith(".gif");}
    static long importZip(Context ctx,Db db,Uri uri)throws Exception{
        String bookName="مانجا محلية";try(android.database.Cursor c=ctx.getContentResolver().query(uri,new String[]{android.provider.OpenableColumns.DISPLAY_NAME},null,null,null)){
            if(c!=null&&c.moveToFirst())bookName=c.getString(0);
        }catch(Exception ignored){}
        if(bookName.toLowerCase().endsWith(".cbz")||bookName.toLowerCase().endsWith(".zip"))bookName=bookName.substring(0,bookName.lastIndexOf('.'));
        String uid=UUID.randomUUID().toString();File outDir=new File(ctx.getFilesDir(),"local/"+uid);
        if(!outDir.mkdirs())throw new IOException("تعذر إنشاء مجلد الاستيراد");
        Map<String,List<Page>> groups=new TreeMap<>(ArchiveImporter::natural);long totalBytes=0;int count=0;
        try(InputStream in=ctx.getContentResolver().openInputStream(uri)){
            if(in==null)throw new IOException("تعذر فتح الأرشيف");
            try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(in))){ZipEntry entry;
                while((entry=zip.getNextEntry())!=null){if(entry.isDirectory()||!image(entry.getName()))continue;
                    if(++count>2500)throw new IOException("أرشيف كبير جدًا: حد 2500 صورة");
                    // The original entry path is never used as the output path (Zip Slip prevention).
                    String raw=entry.getName().replace('\\','/');String dir=raw.contains("/")?raw.substring(0,raw.lastIndexOf('/')):"الفصل 1";
                    File target=new File(outDir,String.format(java.util.Locale.US,"%05d.img",count));
                    try(OutputStream o=new BufferedOutputStream(new FileOutputStream(target))){byte[] buf=new byte[16384];int n;
                        while((n=zip.read(buf))!=-1){totalBytes+=n;if(totalBytes>800L*1024*1024)throw new IOException("الأرشيف أكبر من 800 ميغابايت");o.write(buf,0,n);}}
                    groups.computeIfAbsent(dir,k->new ArrayList<>()).add(new Page(raw,Uri.fromFile(target).toString()));
                }
            }
        }
        if(groups.isEmpty())throw new IOException("لا توجد صور في الأرشيف؛ الملفات المدعومة JPG, PNG, WEBP, GIF");
        Models.Manga manga=new Models.Manga();manga.sourceId="local";manga.title=bookName;manga.url="local://"+uid;manga.favorite=true;
        manga.cover=groups.values().iterator().next().get(0).file;
        long id=db.upsertManga(manga);int idx=0;
        for(Map.Entry<String,List<Page>> group:groups.entrySet()){
            List<Page> images=group.getValue();images.sort((a,b)->natural(a.name,b.name));JSONArray arr=new JSONArray();for(Page img:images)arr.put(img.file);
            Models.Chapter chapter=new Models.Chapter();chapter.mangaId=id;chapter.title=group.getKey();chapter.url="local://"+uid+"/"+(idx++);chapter.number=idx;
            chapter.pages=arr.toString();long chId=db.upsertChapter(chapter);db.pages(chId,arr);
        }
        return id;
    }
    private static int natural(String a,String b){
        // Natural sort for page001, page002, page010.
        int i=0,j=0;while(i<a.length()&&j<b.length()){
            char x=a.charAt(i),y=b.charAt(j);
            if(Character.isDigit(x)&&Character.isDigit(y)){
                long n=0,m=0;while(i<a.length()&&Character.isDigit(a.charAt(i))){n=Math.min(1000000000L,n*10+a.charAt(i++)-'0');}
                while(j<b.length()&&Character.isDigit(b.charAt(j))){m=Math.min(1000000000L,m*10+b.charAt(j++)-'0');}
                if(n!=m)return Long.compare(n,m);
            }else{if(x!=y)return Character.compare(x,y);i++;j++;}
        }return Integer.compare(a.length(),b.length());
    }
    private static final class Page{final String name,file;Page(String name,String file){this.name=name;this.file=file;}}
}
