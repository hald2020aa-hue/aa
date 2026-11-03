package com.qari.kamel;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.AbsListView;
import android.graphics.Matrix;
import android.view.ScaleGestureDetector;
import org.json.JSONArray;
import java.util.*;

public final class ReaderActivity extends Activity {
    private Db db;private Models.Chapter chapter;private Models.Manga manga;private JSONArray pages=new JSONArray();
    private LinearLayout root,body,controls;private int position=0;private boolean vertical=true;
    static void open(Context ctx,long id){Intent i=new Intent(ctx,ReaderActivity.class);i.putExtra("chapter_id",id);ctx.startActivity(i);}
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);chapter=db.chapter(getIntent().getLongExtra("chapter_id",-1));if(chapter==null){finish();return;}
        manga=db.manga(chapter.mangaId);if(manga==null){finish();return;}
        vertical=getPreferences(MODE_PRIVATE).getBoolean("vertical",true);
        pages=chapter.pageList();position=Math.max(0,chapter.progress-1);if(pages.length()==0)loadPages();else render();
    }
    private void loadPages(){root=Ui.container(this,chapter.title);root.addView(Ui.muted(this,"تحميل صفحات الفصل...",16));
        Models.Source source=db.source(manga.sourceId);if(source==null){Ui.toast(this,"لا يمكن قراءة الفصل: مصدر غير معروف");return;}
        new Thread(()->{try{JSONArray found=Net.pages(source,chapter.url);if(found.length()==0)throw new IllegalStateException("لم يجد المحرك صورًا. تحقق من محدد CSS في إعدادات المصدر.");
            db.pages(chapter.id,found);pages=found;runOnUiThread(this::render);
        }catch(Exception e){Ui.error(this,e);}}).start();
    }
    private void render(){root=Ui.container(this,chapter.title);controls=Ui.horizontal(this);root.addView(controls,Ui.margins(this,-1,Ui.dp(this,48),0));
        Button mode=Ui.secondary(this,vertical?"عرض أفقي":"تمرير عمودي");controls.addView(mode,new LinearLayout.LayoutParams(0,-1,1));
        mode.setOnClickListener(v->{vertical=!vertical;getPreferences(MODE_PRIVATE).edit().putBoolean("vertical",vertical).apply();render();});
        Button prevChapter=Ui.secondary(this,"الفصل السابق");controls.addView(prevChapter,new LinearLayout.LayoutParams(0,-1,1));prevChapter.setOnClickListener(v->nextChapter(-1));
        Button nextChapter=Ui.secondary(this,"الفصل التالي");controls.addView(nextChapter,new LinearLayout.LayoutParams(0,-1,1));nextChapter.setOnClickListener(v->nextChapter(1));
        body=Ui.vertical(this);root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        if(vertical)verticalPages();else singlePage();
    }
    private void verticalPages(){
        ListView list=new ListView(this);list.setDividerHeight(Ui.dp(this,4));list.setBackgroundColor(Ui.BG);body.addView(list,new LinearLayout.LayoutParams(-1,-1));
        list.setAdapter(new android.widget.BaseAdapter(){
            @Override public int getCount(){return pages.length();}
            @Override public Object getItem(int i){return pages.optString(i);}
            @Override public long getItemId(int i){return i;}
            @Override public View getView(int idx,View reuse,android.view.ViewGroup parent){
                ImageView img=reuse instanceof ImageView?(ImageView)reuse:Ui.cover(ReaderActivity.this);
                img.setScaleType(ImageView.ScaleType.FIT_CENTER);img.setAdjustViewBounds(true);
                img.setMinimumHeight(Ui.dp(ReaderActivity.this,200));
                img.setLayoutParams(new AbsListView.LayoutParams(-1,-2));
                Images.load(ReaderActivity.this,img,pages.optString(idx),chapter.url);return img;
            }
        });
        list.setSelection(Math.min(position,Math.max(0,pages.length()-1)));
        list.setOnScrollListener(new AbsListView.OnScrollListener(){
            public void onScrollStateChanged(AbsListView v,int state){}
            public void onScroll(AbsListView v,int first,int visible,int total){
                if(total>0&&first!=position){position=first;db.progress(chapter.id,position+1);}
            }
        });
        TextView info=Ui.muted(this,"تمرير عمودي · "+pages.length()+" صفحة",12);info.setGravity(Gravity.CENTER);root.addView(info);
    }
    private void singlePage(){
        ImageView img=Ui.cover(this);img.setScaleType(ImageView.ScaleType.FIT_CENTER);body.addView(img,new LinearLayout.LayoutParams(-1,0,1));
        ScaleGestureDetector zoom=new ScaleGestureDetector(this,new ScaleGestureDetector.SimpleOnScaleGestureListener(){float current=1;
            @Override public boolean onScale(ScaleGestureDetector detector){
                current=Math.max(1,Math.min(current*detector.getScaleFactor(),4));img.setScaleType(ImageView.ScaleType.MATRIX);
                Matrix matrix=new Matrix();matrix.setScale(current,current,img.getWidth()/2f,img.getHeight()/2f);img.setImageMatrix(matrix);return true;
            }
        });
        img.setOnTouchListener((v,event)->{zoom.onTouchEvent(event);return event.getPointerCount()>1;});
        TextView counter=Ui.text(this,"",13,false);counter.setGravity(Gravity.CENTER);
        LinearLayout navigation=Ui.horizontal(this);body.addView(navigation,new LinearLayout.LayoutParams(-1,Ui.dp(this,53)));
        Button previous=Ui.secondary(this,"❮");navigation.addView(previous,new LinearLayout.LayoutParams(0,-1,1));
        navigation.addView(counter,new LinearLayout.LayoutParams(0,-1,2));
        Button next=Ui.secondary(this,"❯");navigation.addView(next,new LinearLayout.LayoutParams(0,-1,1));
        Runnable show=()->{position=Math.max(0,Math.min(position,pages.length()-1));counter.setText((position+1)+" / "+pages.length());
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);Images.load(this,img,pages.optString(position),chapter.url);db.progress(chapter.id,position+1);};
        previous.setOnClickListener(v->{position--;show.run();});next.setOnClickListener(v->{position++;show.run();});show.run();
    }
    private void nextChapter(int direction){
        List<Models.Chapter> all=db.chapters(manga.id);int idx=-1;for(int i=0;i<all.size();i++)if(all.get(i).id==chapter.id){idx=i;break;}
        int target=idx-direction; // chapters sorted descending, +1 means next/newer.
        if(target<0||target>=all.size()){Ui.toast(this,"لا يوجد فصل آخر بهذا الاتجاه");return;}
        open(this,all.get(target).id);finish();
    }
}
