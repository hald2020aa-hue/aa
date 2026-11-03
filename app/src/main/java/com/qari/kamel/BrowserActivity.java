package com.qari.kamel;

import android.app.*;
import android.os.Bundle;
import android.webkit.*;
import android.widget.*;

public final class BrowserActivity extends Activity {
    private WebView web;
    @Override public void onCreate(Bundle b){super.onCreate(b);
        String url=getIntent().getStringExtra("url");try{Net.checked(url);}catch(Exception e){Ui.error(this,e);finish();return;}
        LinearLayout root=Ui.container(this,"متصفح المصدر");web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);
        web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
            return !"https".equalsIgnoreCase(r.getUrl().getScheme());}});
        web.loadUrl(url);
    }
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(web!=null)web.destroy();super.onDestroy();}
}
