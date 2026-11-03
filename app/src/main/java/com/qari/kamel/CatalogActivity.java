package com.qari.kamel;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class CatalogActivity extends Activity {
    private Db db;private Models.Source source;private LinearLayout gridHolder;private EditText search;private int page=1;
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);source=db.source(getIntent().getStringExtra("source"));if(source==null){finish();return;}
        LinearLayout root=Ui.container(this,source.name);search=Ui.input(this,"بحث باسم المانجا","");root.addView(search,Ui.margins(this,-1,Ui.dp(this,48),0));
        LinearLayout actions=Ui.horizontal(this);root.addView(actions,Ui.margins(this,-1,Ui.dp(this,46),8));
        Button run=Ui.button(this,"بحث");actions.addView(run,new LinearLayout.LayoutParams(0,-1,1));run.setOnClickListener(v->{page=1;load(true);});
        Button open=Ui.secondary(this,"فتح الموقع");actions.addView(open,new LinearLayout.LayoutParams(0,-1,1));open.setOnClickListener(v->{Intent i=new Intent(this,BrowserActivity.class);i.putExtra("url",source.baseUrl);startActivity(i);});
        ScrollView sc=Ui.scrolling(this,root);gridHolder=Ui.vertical(this);sc.addView(gridHolder);
        Button next=Ui.secondary(this,"عرض الصفحة التالية");root.addView(next,Ui.margins(this,-1,Ui.dp(this,45),5));next.setOnClickListener(v->{page++;load(false);});load(true);
    }
    private void load(boolean reset){if(reset)gridHolder.removeAllViews();gridHolder.addView(Ui.muted(this,"تحميل الصفحة "+page+"...",13));
        new Thread(()->{try{List<Models.Manga> list=Net.catalog(source,search.getText().toString().trim(),page);
            runOnUiThread(()->{gridHolder.removeAllViews();if(list.isEmpty())gridHolder.addView(Ui.muted(this,"لا توجد نتائج؛ تحقق من محددات CSS أو استخدم متصفح الموقع.",15));
                for(Models.Manga m:list){LinearLayout card=Ui.horizontal(this);card.setBackground(Ui.shape(Ui.SURFACE,Ui.dp(this,10)));gridHolder.addView(card,Ui.margins(this,-1,Ui.dp(this,108),8));
                    ImageView img=Ui.cover(this);card.addView(img,new LinearLayout.LayoutParams(Ui.dp(this,75),-1));Images.load(this,img,m.cover,m.url);
                    TextView t=Ui.text(this,m.title,16,true);t.setPadding(Ui.dp(this,12),0,Ui.dp(this,6),0);card.addView(t,new LinearLayout.LayoutParams(0,-1,1));
                    card.setOnClickListener(v->{long id=db.upsertManga(m);Intent i=new Intent(this,DetailActivity.class);i.putExtra("id",id);startActivity(i);});
                }
            });
        }catch(Exception e){Ui.error(this,e);}}).start();
    }
}
