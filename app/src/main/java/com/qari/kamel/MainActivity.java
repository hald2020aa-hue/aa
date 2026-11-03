package com.qari.kamel;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.util.List;

public final class MainActivity extends Activity {
    private static final int PICK_ARCHIVE=10, EXPORT_JSON=11, IMPORT_JSON=12;
    private Db db;private LinearLayout root,content;private int section=0;
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);openSection(0);
        Intent i=getIntent();if(i!=null&&i.getData()!=null&&Intent.ACTION_VIEW.equals(i.getAction()))importArchive(i.getData());}
    @Override protected void onResume(){super.onResume();if(root!=null)openSection(section);}
    private void openSection(int n){section=n;root=Ui.container(this,"قارئ كامل");
        TextView subtitle=Ui.muted(this,"مكتبتك · مصادرك · قراءتك",12);root.addView(subtitle);
        ScrollView sc=Ui.scrolling(this,root);content=Ui.vertical(this);content.setPadding(0,Ui.dp(this,12),0,Ui.dp(this,16));sc.addView(content);
        switch(section){case 0:library();break;case 1:sources();break;case 2:updates();break;case 3:downloads();break;default:settings();}
        LinearLayout nav=Ui.horizontal(this);String[] names={"المكتبة","المصادر","الفصول","التنزيلات","الإعدادات"};
        for(int idx=0;idx<names.length;idx++){final int pos=idx;TextView b=Ui.text(this,names[idx],12,idx==section);
            b.setGravity(Gravity.CENTER);b.setTextColor(idx==section?Ui.GOLD:Ui.MUTED);
            nav.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));b.setOnClickListener(v->openSection(pos));}
        root.addView(nav);
    }
    private void library(){
        TextView title=Ui.text(this,"المفضلة",19,true);content.addView(title);
        List<Models.Manga> list=db.mangas(true);
        if(list.isEmpty())content.addView(Ui.muted(this,"لا توجد مانجا محفوظة بعد. افتح المصادر، اختر عنوانًا، واضغط إضافة للمكتبة.",15));
        mangaGrid(list);
        Button all=Ui.secondary(this,"كل العناوين التي تمت زيارتها");content.addView(all,Ui.margins(this,-1,Ui.dp(this,52),16));
        all.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("كل العناوين").setItems(db.mangas(false).stream().map(m->m.title).toArray(String[]::new),(d,w)->{
            List<Models.Manga> a=db.mangas(false);if(w<a.size())openManga(a.get(w).id);}).show());
    }
    private void mangaGrid(List<Models.Manga> list){
        int width=getResources().getDisplayMetrics().widthPixels;
        int columns=width>Ui.dp(this,700)?4:width>Ui.dp(this,430)?3:2;
        android.widget.GridLayout grid=new android.widget.GridLayout(this);grid.setColumnCount(columns);content.addView(grid);
        int itemW=Math.max(Ui.dp(this,110),(width-Ui.dp(this,40)-(columns-1)*Ui.dp(this,10))/columns);
        for(Models.Manga m:list){LinearLayout item=Ui.vertical(this);android.widget.GridLayout.LayoutParams gp=new android.widget.GridLayout.LayoutParams();gp.width=itemW;gp.setMargins(0,Ui.dp(this,10),Ui.dp(this,6),0);grid.addView(item,gp);
            ImageView cover=Ui.cover(this);item.addView(cover,new LinearLayout.LayoutParams(itemW-Ui.dp(this,10),Ui.dp(this,175)));
            Images.load(this,cover,m.cover,m.url);TextView t=Ui.text(this,m.title,13,true);t.setMaxLines(2);item.addView(t);
            item.setOnClickListener(v->openManga(m.id));}
    }
    private void openManga(long id){Intent i=new Intent(this,DetailActivity.class);i.putExtra("id",id);startActivity(i);}
    private void sources(){
        Button add=Ui.button(this,"+ إضافة مصدر / موقع جديد");content.addView(add,Ui.margins(this,-1,Ui.dp(this,50),0));
        add.setOnClickListener(v->startActivity(new Intent(this,SourceEditActivity.class)));
        for(Models.Source src:db.sources()){
            LinearLayout card=Ui.card(this);content.addView(card,Ui.margins(this,-1,-2,10));
            TextView name=Ui.text(this,src.name+(src.adult?" · 18+":""),17,true);card.addView(name);
            card.addView(Ui.muted(this,src.baseUrl,12));
            LinearLayout row=Ui.horizontal(this);card.addView(row,Ui.margins(this,-1,Ui.dp(this,45),8));
            Button search=Ui.secondary(this,"تصفح");row.addView(search,new LinearLayout.LayoutParams(0,-1,1));
            search.setOnClickListener(v->{Intent i=new Intent(this,CatalogActivity.class);i.putExtra("source",src.id);startActivity(i);});
            Button edit=Ui.secondary(this,"تعديل");row.addView(edit,new LinearLayout.LayoutParams(0,-1,1));
            edit.setOnClickListener(v->{Intent i=new Intent(this,SourceEditActivity.class);i.putExtra("id",src.id);startActivity(i);});
            Button browser=Ui.secondary(this,"متصفح");row.addView(browser,new LinearLayout.LayoutParams(0,-1,1));
            browser.setOnClickListener(v->{Intent i=new Intent(this,BrowserActivity.class);i.putExtra("url",src.baseUrl);startActivity(i);});
        }
        if(db.sources().isEmpty())content.addView(Ui.muted(this,"المحرك يدعم إضافة مواقع للمانجا والمانهوا عبر محددات CSS. لا توجد مواقع مضمنة مسبقًا، وتحتاج كل إضافة إلى ضبط محدداتها حسب الموقع.",14));
    }
    private void updates(){
        content.addView(Ui.muted(this,"الفصول المتاحة محليًا. لتحديث فصل من الإنترنت افتح صفحة العمل ثم اضغط تحديث الفصول.",14));
        for(Models.Manga m:db.mangas(false)){
            List<Models.Chapter> cs=db.chapters(m.id);if(cs.isEmpty())continue;
            LinearLayout card=Ui.card(this);content.addView(card,Ui.margins(this,-1,-2,9));
            card.addView(Ui.text(this,m.title,15,true));card.addView(Ui.muted(this,cs.size()+" فصلًا",12));
            card.setOnClickListener(v->openManga(m.id));
        }
    }
    private void downloads(){
        Button refresh=Ui.secondary(this,"تحديث الحالات");content.addView(refresh);refresh.setOnClickListener(v->openSection(3));
        List<Models.Chapter> dl=db.downloads();if(dl.isEmpty())content.addView(Ui.muted(this,"لا توجد تنزيلات حتى الآن.",15));
        for(Models.Chapter ch:dl){
            LinearLayout card=Ui.card(this);content.addView(card,Ui.margins(this,-1,-2,8));
            card.addView(Ui.text(this,ch.title,16,true));String status=ch.downloadStatus==2?"مكتمل":ch.downloadStatus==1?"قيد التنزيل / انتظار":"متوقف أو فشل";
            card.addView(Ui.muted(this,status,12));
            LinearLayout actions=Ui.horizontal(this);card.addView(actions);
            Button resume=Ui.secondary(this,"استئناف");actions.addView(resume,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));resume.setOnClickListener(v->{DownloadWorker.enqueue(this,ch.id);openSection(3);});
            Button read=Ui.secondary(this,"قراءة");actions.addView(read,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));read.setOnClickListener(v->ReaderActivity.open(this,ch.id));
            Button stop=Ui.secondary(this,"إيقاف");actions.addView(stop,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));stop.setOnClickListener(v->{DownloadWorker.cancel(this,ch.id);openSection(3);});
        }
    }
    private void settings(){
        Button file=Ui.button(this,"استيراد CBZ / ZIP محلي");content.addView(file,Ui.margins(this,-1,Ui.dp(this,50),0));file.setOnClickListener(v->select("application/zip",PICK_ARCHIVE));
        Button export=Ui.secondary(this,"تصدير نسخة احتياطية JSON");content.addView(export,Ui.margins(this,-1,Ui.dp(this,50),10));
        export.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"qari-kamel-backup.json");startActivityForResult(i,EXPORT_JSON);});
        Button restore=Ui.secondary(this,"استعادة نسخة / استيراد مصدر JSON");content.addView(restore,Ui.margins(this,-1,Ui.dp(this,50),10));restore.setOnClickListener(v->select("application/json",IMPORT_JSON));
        content.addView(Ui.muted(this,"النسخة الحالية مستقلة عن التطبيق الأصلي. تشمل المصادر والمفضلة والتقدم في القراءة والتنزيل المحلي للفصول. المواقع التي تتطلب JavaScript قد تحتاج تشغيلها في المتصفح أو مكوّن مخصص.",14));
    }
    private void select(String type,int request){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(type);i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{type,"application/octet-stream"});startActivityForResult(i,request);}
    @Override protected void onActivityResult(int r,int result,Intent i){super.onActivityResult(r,result,i);if(result!=RESULT_OK||i==null||i.getData()==null)return;
        Uri u=i.getData();if(r==PICK_ARCHIVE)importArchive(u);else if(r==EXPORT_JSON){
            try(OutputStream out=getContentResolver().openOutputStream(u)){if(out==null)throw new IOException("لا يمكن فتح الملف");out.write(db.backup().toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));Ui.toast(this,"تم تصدير النسخة الاحتياطية");}catch(Exception e){Ui.error(this,e);}
        }else if(r==IMPORT_JSON){new Thread(()->{
            try(InputStream in=getContentResolver().openInputStream(u)){
                if(in==null)throw new IOException("تعذر قراءة الملف");byte[] data=readLimited(in,4*1024*1024);JSONObject j=new JSONObject(new String(data,java.nio.charset.StandardCharsets.UTF_8));
                if(j.has("baseUrl")){Models.Source s=Models.Source.from(j);if(s.id.isEmpty())s.id=java.util.UUID.randomUUID().toString();Net.checked(s.baseUrl);db.saveSource(s);}else db.restore(j);
                runOnUiThread(()->{Ui.toast(this,"تم الاستيراد");openSection(1);});
            }catch(Exception e){Ui.error(this,e);}
        }).start();}
    }
    static byte[] readLimited(InputStream in,int max)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("ملف كبير للغاية");out.write(b,0,n);}return out.toByteArray();}
    private void importArchive(Uri u){Ui.toast(this,"جارٍ استيراد أرشيف المانجا");new Thread(()->{
        try{long id=ArchiveImporter.importZip(this,db,u);runOnUiThread(()->{Ui.toast(this,"تم استيراد الملف");openManga(id);});}
        catch(Exception e){Ui.error(this,e);}
    }).start();}
}
