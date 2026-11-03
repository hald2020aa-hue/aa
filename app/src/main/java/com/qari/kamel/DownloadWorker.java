package com.qari.kamel;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.work.*;
import org.json.JSONArray;
import java.io.*;
import java.util.concurrent.TimeUnit;

public final class DownloadWorker extends Worker {
    private final Db db;
    public DownloadWorker(@NonNull Context ctx,@NonNull WorkerParameters params){super(ctx,params);db=new Db(ctx);}
    static String name(long chapter){return "qari-download-"+chapter;}
    static void enqueue(Context ctx,long id){
        new Db(ctx).downloadStatus(id,1);
        Constraints constraints=new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest request=new OneTimeWorkRequest.Builder(DownloadWorker.class)
            .setInputData(new Data.Builder().putLong("chapter_id",id).build())
            .setConstraints(constraints).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build();
        WorkManager.getInstance(ctx).enqueueUniqueWork(name(id),ExistingWorkPolicy.REPLACE,request);
    }
    static void cancel(Context ctx,long id){WorkManager.getInstance(ctx).cancelUniqueWork(name(id));new Db(ctx).downloadStatus(id,-1);}
    @NonNull @Override public Result doWork(){
        long chapterId=getInputData().getLong("chapter_id",-1);Models.Chapter ch=db.chapter(chapterId);
        if(ch==null)return Result.failure();Models.Manga manga=db.manga(ch.mangaId);
        if(manga==null)return Result.failure();
        try{
            JSONArray pages=ch.pageList();if(pages.length()==0){
                Models.Source source=db.source(manga.sourceId);if(source==null)throw new IOException("مصدر غير معروف");
                pages=Net.pages(source,ch.url);if(pages.length()==0)throw new IOException("لا توجد صفحات");db.pages(ch.id,pages);
            }
            File dir=new File(getApplicationContext().getFilesDir(),"chapters/"+ch.id);
            if(!dir.exists()&&!dir.mkdirs())throw new IOException("تعذر إنشاء مجلد الفصل");
            JSONArray local=new JSONArray();
            for(int i=0;i<pages.length();i++){
                if(isStopped()){db.downloadStatus(ch.id,-1);return Result.failure();}
                String url=pages.optString(i);if(url.startsWith("file://")){local.put(url);continue;}
                File dst=new File(dir,String.format(java.util.Locale.US,"%05d.img",i));
                if(!dst.exists()||dst.length()==0){byte[] data=Net.fetch(url,ch.url,18*1024*1024);
                    File temp=new File(dir,dst.getName()+".part");try(FileOutputStream out=new FileOutputStream(temp)){out.write(data);out.getFD().sync();}
                    if(!temp.renameTo(dst))throw new IOException("تعذر حفظ الصفحة "+i);
                }
                local.put(Uri.fromFile(dst).toString());
                setProgressAsync(new Data.Builder().putInt("done",i+1).putInt("total",pages.length()).build());
            }
            db.pages(ch.id,local);db.downloadStatus(ch.id,2);return Result.success();
        }catch(Exception ex){db.downloadStatus(ch.id,-1);return getRunAttemptCount()<2?Result.retry():Result.failure();}
    }
}
