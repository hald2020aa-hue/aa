package com.qari.kamel;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class SourceEditActivity extends Activity {
    private final String[] keys={"name","baseUrl","searchPath","catalogPath","itemSelector","titleSelector","linkSelector","coverSelector","chapterSelector","chapterTitleSelector","chapterLinkSelector","pageSelector","pageUrlAttribute","nextSelector","format","itemsPath","jsonTitlePath","jsonLinkPath","jsonCoverPath","chaptersPath","jsonChapterNamePath","jsonChapterLinkPath","pagesPath","jsonPageLinkPath"};
    private final String[] labels={"اسم المصدر","رابط الموقع HTTPS","رابط البحث: {query} و {page}","رابط التصفح: {page}","CSS: العنصر المتكرر","CSS: عنوان المانجا","CSS: الرابط","CSS: الغلاف","CSS: رابط الفصل","CSS: اسم الفصل (اختياري)","CSS: رابط الفصل الداخلي (اختياري)","CSS: صورة صفحة القارئ","صفة رابط الصورة (src / data-src)","CSS: الصفحة التالية (اختياري)","صيغة المصدر: html أو json","JSON: مسار مصفوفة النتائج","JSON: اسم العمل","JSON: رابط العمل","JSON: رابط الغلاف","JSON: مصفوفة الفصول","JSON: اسم الفصل","JSON: رابط الفصل","JSON: مصفوفة الصور","JSON: رابط الصورة"};
    private final Map<String,EditText> fields=new LinkedHashMap<>();private Db db;private Models.Source source;
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);source=db.source(getIntent().getStringExtra("id"));if(source==null){source=new Models.Source();source.id=UUID.randomUUID().toString();}
        LinearLayout root=Ui.container(this,"إعداد مصدر");ScrollView sc=Ui.scrolling(this,root);LinearLayout form=Ui.vertical(this);sc.addView(form);
        try{org.json.JSONObject json=source.json();for(int i=0;i<keys.length;i++){
            TextView label=Ui.muted(this,labels[i],13);form.addView(label,Ui.margins(this,-1,-2,10));
            EditText e=Ui.input(this,labels[i],json.optString(keys[i]));form.addView(e,Ui.margins(this,-1,Ui.dp(this,48),3));fields.put(keys[i],e);
        }}catch(Exception ignored){}
        CheckBox adult=new CheckBox(this);adult.setText("مصدر خاص بمحتوى البالغين (+18)");adult.setTextColor(Ui.TEXT);adult.setChecked(source.adult);form.addView(adult,Ui.margins(this,-1,-2,12));
        TextView note=Ui.muted(this,"هذه حقول CSS قابلة للتعديل؛ لكل موقع بنيته. المصادر المحمية بجافاسكربت قد لا تعمل بالمحلل الثابت.",13);form.addView(note);
        Button save=Ui.button(this,"حفظ المصدر");root.addView(save,Ui.margins(this,-1,Ui.dp(this,54),8));
        save.setOnClickListener(v->{try{org.json.JSONObject j=source.json();for(String key:keys)j.put(key,fields.get(key).getText().toString().trim());j.put("adult",adult.isChecked());Models.Source s=Models.Source.from(j);
            Net.checked(s.baseUrl);if(s.name.isEmpty())throw new IllegalArgumentException("أدخل اسم المصدر");
            if(!s.format.equals("html")&&!s.format.equals("json"))throw new IllegalArgumentException("الصيغة html أو json فقط");
            if(s.format.equals("html")&&(s.itemSelector.isEmpty()||s.pageSelector.isEmpty()))throw new IllegalArgumentException("أكمل محددات CSS للعناصر والصور");
            db.saveSource(s);Ui.toast(this,"تم حفظ المصدر");finish();
        }catch(Exception e){Ui.error(this,e);}});
        Button del=Ui.secondary(this,"حذف المصدر");form.addView(del,Ui.margins(this,-1,Ui.dp(this,46),14));del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("حذف هذا المصدر من القائمة؟").setNegativeButton("إلغاء",null).setPositiveButton("حذف",(d,w)->{db.deleteSource(source.id);finish();}).show());
    }
}
