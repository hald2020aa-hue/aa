package com.qari.kamel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

final class Ui {
    static final int BG=0xff101115, SURFACE=0xff1a1c22, BORDER=0xff363941, GOLD=0xffe8b976, TEXT=0xfff3f2f1, MUTED=0xffb0b4be;
    private Ui(){}
    static int dp(Context c,float n){return (int)(c.getResources().getDisplayMetrics().density*n+0.5f);}
    static GradientDrawable shape(int bg,int radius){GradientDrawable g=new GradientDrawable();g.setColor(bg);g.setCornerRadius(radius);return g;}
    static LinearLayout vertical(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.VERTICAL);return v;}
    static LinearLayout horizontal(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    static TextView text(Context c,String str,int sp,boolean bold){TextView t=new TextView(c);t.setText(str);t.setTextColor(TEXT);t.setTextSize(sp);if(bold)t.setTypeface(null,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    static Button button(Context c,String str){Button b=new Button(c);b.setText(str);b.setTextColor(BG);b.setAllCaps(false);b.setBackground(shape(GOLD,dp(c,12)));return b;}
    static Button secondary(Context c,String str){Button b=new Button(c);b.setText(str);b.setTextColor(TEXT);b.setAllCaps(false);b.setBackground(shape(SURFACE,dp(c,12)));return b;}
    static EditText input(Context c,String hint,String text){EditText e=new EditText(c);e.setSingleLine(true);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setText(text);e.setHint(hint);e.setTextSize(15);e.setPadding(dp(c,12),dp(c,10),dp(c,12),dp(c,10));e.setBackground(shape(SURFACE,dp(c,10)));return e;}
    static LinearLayout container(Activity a,String title){
        LinearLayout root=vertical(a);root.setBackgroundColor(BG);root.setPadding(dp(a,14),dp(a,10),dp(a,14),dp(a,8));
        TextView heading=text(a,title,24,true);heading.setPadding(dp(a,3),dp(a,8),dp(a,3),dp(a,13));root.addView(heading);a.setContentView(root);return root;
    }
    static ScrollView scrolling(Context c,LinearLayout root){ScrollView sc=new ScrollView(c);sc.setFillViewport(false);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));return sc;}
    static LinearLayout card(Context c){LinearLayout v=vertical(c);v.setBackground(shape(SURFACE,dp(c,12)));v.setPadding(dp(c,12),dp(c,12),dp(c,12),dp(c,12));return v;}
    static LinearLayout.LayoutParams margins(Context c,int width,int height,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,height);p.topMargin=dp(c,top);return p;}
    static TextView muted(Context c,String str,int sp){TextView t=text(c,str,sp,false);t.setTextColor(MUTED);return t;}
    static void toast(Context c,String s){Toast.makeText(c,s,Toast.LENGTH_LONG).show();}
    static void error(Activity a,Exception e){a.runOnUiThread(()->new android.app.AlertDialog.Builder(a).setTitle("تعذر تنفيذ العملية").setMessage(e.getMessage()).setPositiveButton("موافق",null).show());}
    static ImageView cover(Context c){ImageView i=new ImageView(c);i.setBackground(shape(BORDER,dp(c,8)));i.setScaleType(ImageView.ScaleType.CENTER_CROP);return i;}
}
