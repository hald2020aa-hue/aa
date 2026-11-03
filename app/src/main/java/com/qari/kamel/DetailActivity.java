package com.qari.kamel;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.widget.*;
import java.util.*;

public final class DetailActivity extends Activity {
    private Db db;private Models.Manga manga;private LinearLayout content;
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);manga=db.manga(getIntent().getLongExtra("id",-1));if(manga==null){finish();return;}render();}
    @Override protected void onResume(){super.onResume();if(manga!=null){manga=db.manga(manga.id);render();}}
    private void render(){LinearLayout root=Ui.container(this,manga.title);ScrollView sc=Ui.scrolling(this,root);content=Ui.vertical(this);sc.addView(content);
        LinearLayout header=Ui.horizontal(this);content.addView(header,Ui.margins(this,-1,Ui.dp(this,165),2));
        ImageView image=Ui.cover(this);header.addView(image,new LinearLayout.LayoutParams(Ui.dp(this,115),-1));Images.load(this,image,manga.cover,manga.url);
        LinearLayout actions=Ui.vertical(this);actions.setPadding(Ui.dp(this,12),0,0,0);header.addView(actions,new LinearLayout.LayoutParams(0,-1,1));
        actions.addView(Ui.muted(this,manga.sourceId.equals("local")?"أرشيف محلي":"مصدر: "+manga.sourceId,12));
        Button fav=Ui.button(this,manga.favorite?"إزالة من المكتبة":"إضافة للمكتبة");actions.addView(fav,Ui.margins(this,-1,Ui.dp(this,48),9));
        fav.setOnClickListener(v->{db.favorite(manga.id,!manga.favorite);manga.favorite=!manga.favorite;fav.setText(manga.favorite?"إزالة من المكتبة":"إضافة للمكتبة");});
        if(!manga.sourceId.equals("local")){
            Button update=Ui.secondary(this,"تحديث الفصول");actions.addView(update,Ui.margins(this,-1,Ui.dp(this,48),8));update.setOnClickListener(v->reload());
        }
        List<Models.Chapter> chapters=db.chapters(manga.id);
        content.addView(Ui.text(this,"الفصول · "+chapters.size(),19,true),Ui.margins(this,-1,-2,14));
        if(chapters.isEmpty())content.addView(Ui.muted(this,"لا توجد فصول محملة. اضغط تحديث الفصول.",14));
        for(Models.Chapter ch:chapters){LinearLayout card=Ui.card(this);content.addView(card,Ui.margins(this,-1,-2,9));
            TextView label=Ui.text(this,ch.title,15,true);card.addView(label);if(ch.progress>0)card.addView(Ui.muted(this,"آخر صفحة: "+ch.progress,12));
            LinearLayout btns=Ui.horizontal(this);card.addView(btns,Ui.margins(this,-1,Ui.dp(this,42),8));
            Button read=Ui.button(this,"قراءة");btns.addView(read,new LinearLayout.LayoutParams(0,-1,1));read.setOnClickListener(v->ReaderActivity.open(this,ch.id));
            if(!manga.sourceId.equals("local")){
                Button dl=Ui.secondary(this,"تنزيل");btns.addView(dl,new LinearLayout.LayoutParams(0,-1,1));dl.setOnClickListener(v->{DownloadWorker.enqueue(this,ch.id);Ui.toast(this,"تمت إضافة الفصل إلى قائمة التنزيل");});
            }
        }
    }
    private void reload(){Models.Source source=db.source(manga.sourceId);if(source==null){Ui.toast(this,"المصدر غير موجود");return;}
        Ui.toast(this,"جارٍ تحديث الفصول");new Thread(()->{try{
            List<Models.Chapter> chapters=Net.chapters(source,manga);for(Models.Chapter ch:chapters)db.upsertChapter(ch);
            runOnUiThread(()->{Ui.toast(this,"تم العثور على "+chapters.size()+" فصلًا");render();});
        }catch(Exception e){Ui.error(this,e);}}).start();
    }
}
