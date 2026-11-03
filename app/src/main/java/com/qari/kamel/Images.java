package com.qari.kamel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Images {
    private static final ExecutorService POOL=Executors.newFixedThreadPool(3);
    private static final LruCache<String,Bitmap> MEMORY=new LruCache<String,Bitmap>(16*1024*1024){
        @Override protected int sizeOf(String k,Bitmap v){return v.getByteCount();}
    };
    private Images(){}
    static void load(Context context,ImageView image,String url,String ref){
        image.setImageDrawable(null);image.setTag(url);
        if(url==null||url.isEmpty())return;
        Bitmap cached=MEMORY.get(url);if(cached!=null){image.setImageBitmap(cached);return;}
        POOL.execute(()->{
            try{
                Bitmap b;
                if(url.startsWith("file://"))b=BitmapFactory.decodeFile(new File(new java.net.URI(url)).getAbsolutePath());
                else {
                    byte[] bytes=Net.fetch(url,ref,12*1024*1024);
                    BitmapFactory.Options opt=new BitmapFactory.Options();opt.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,opt);
                    opt.inSampleSize=1;
                    while((opt.outWidth/opt.inSampleSize)>1600 || (opt.outHeight/opt.inSampleSize)>3200)opt.inSampleSize*=2;
                    opt.inJustDecodeBounds=false;b=BitmapFactory.decodeByteArray(bytes,0,bytes.length,opt);
                }
                if(b!=null){if(b.getByteCount()<8*1024*1024)MEMORY.put(url,b);
                    image.post(()->{if(url.equals(image.getTag()))image.setImageBitmap(b);});}
            }catch(Exception ignored){}
        });
    }
}
