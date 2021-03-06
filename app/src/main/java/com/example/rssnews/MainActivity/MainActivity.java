package com.example.rssnews.MainActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.rssnews.R;
import com.example.rssnews.DataBase.DatabaseHelper;
import com.example.rssnews.DataBase.EnumTypeNews;
import com.example.rssnews.DataBase.EnumWebSite;
import com.example.rssnews.DataBase.MySharedPreferences;
import com.example.rssnews.DataBase.News;
import com.example.rssnews.DataBase.Utilities;
import com.example.rssnews.LocalStorageActivity.LocalStorageActivity;

import com.example.rssnews.WebActivity.WebActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawer;
    List<News> newsList;
    FloatingActionButton fab;
    ListView listView;
    TextView textViewEmpty;
    AdapterItemNews adapterItemNews;
    DatabaseHelper databaseHelper;
    //    Bi???n l??u gi?? tr??? website ??ang xem
    EnumWebSite enumWebSiteDefault;
    //    Bi???n l??u gi?? tr??? lo???i tin ??ang xem
    EnumTypeNews enumTypeNews;
    ImageView imageViewHeaderDrawer;
    WebView webView;
    Button btn ;
    Menu menuNav;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Menu menu;
        menu = toolbar.getMenu();
        setTitle("Trang ch???");

        init();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews, enumWebSiteDefault).getLink());
            }
        });

        setClick();


        //checkPermission();
    }
    void init() {
        databaseHelper = new DatabaseHelper(this);
        enumWebSiteDefault = EnumWebSite.valueOf(MySharedPreferences.getPrefDefaultWebsite(this));
        enumTypeNews = EnumTypeNews.HOMEPAGE;

//        Ki???m tra xem l???n ?????u m??? app kh??ng
        if (MySharedPreferences.getPrefFirstOpen(this)) {
//            N???u ph???i th?? kh???i t???o danh s??ch link rss
            databaseHelper.initRssLink();
            MySharedPreferences.setPrefFirstOpen(this, false);
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            webView = findViewById(R.id.webview);
        }

        newsList = new ArrayList<>();

        fab = findViewById(R.id.fab);
        listView = findViewById(R.id.listView);
        textViewEmpty = findViewById(R.id.emptyView);
        listView.setEmptyView(textViewEmpty);

        adapterItemNews = new AdapterItemNews(this, newsList);
        listView.setAdapter(adapterItemNews);

        drawer = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);

        NavigationView navigationView = findViewById(R.id.nav_view);
        imageViewHeaderDrawer = navigationView.getHeaderView(0).findViewById(R.id.imageView);


//        C??i ?????t ???nh n???n cho drawer
        setImageViewHeaderDrawer();
        final Menu menu = navigationView.getMenu();
         menuNav = menu;

//        S??? ki???n ???n c??c item trong drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawer.closeDrawers();

//                        Khi ???n m???t item b???t k??
//                        ?????i bi???n l??u gi?? tr??? lo???i tin ??ang xem
//                        Thay ?????i ti??u ????? m??n h??nh theo lo???i
                        switch (menuItem.getItemId()){
                            case R.id.nav_home: {
                                enumTypeNews = EnumTypeNews.HOMEPAGE;
                                setTitle("Trang ch???");
                                break;
                            }
                            case R.id.nav_news: {
                                enumTypeNews = EnumTypeNews.NEWS;
                                setTitle("Th???i s???");
                                break;
                            }
                            case R.id.nav_world: {
                                enumTypeNews = EnumTypeNews.WORLD;
                                setTitle("Th??? gi???i");
                                break;
                            }
                            case R.id.nav_sport: {
                                enumTypeNews = EnumTypeNews.SPORT;
                                setTitle("Th??? thao");
                                break;
                            }
                            case R.id.nav_science_and_technology: {
                                enumTypeNews = EnumTypeNews.SCIENCEANDTECHNOLOGY;
                                setTitle("Khoa h???c - C??ng ngh???");
                                break;
                            }
                            case R.id.nav_health: {
                                enumTypeNews = EnumTypeNews.HEALTH;
                                setTitle("S???c kh???e");
                                break;
                            }
                            case R.id.nav_economy: {
                                enumTypeNews = EnumTypeNews.ECONOMY;
                                setTitle("Kinh t???");
                                break;
                            }
                            case R.id.nav_law: {
                                enumTypeNews = EnumTypeNews.LAW;
                                setTitle("Lu???t ph??p");
                                break;
                            }
                            case R.id.nav_cultural: {
                                enumTypeNews = EnumTypeNews.CULTURAL;
                                setTitle("V??n h??a");
                                break;
                            }
                            case R.id.nav_education: {
                                enumTypeNews = EnumTypeNews.EDUCATION;
                                setTitle("Gi??o d???c");
                                break;
                            }
                            case R.id.nav_baothaibinh: {
                                enumTypeNews = EnumTypeNews.BAOTHAIBINH;
                                setTitle("B??o Th??i B??nh");
                                break;
                            }
                            case R.id.nav_tinmoi: {
                                enumTypeNews = EnumTypeNews.TINMOI;
                                setTitle("Tin M???i");
                                break;
                            }
                        }

//                        G???i task l???y d??? li???u ??? lo???i tin m???i v???a ch???n
                        new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews,enumWebSiteDefault).getLink());

                        return true;
                    }
                });

        File rootFilePicture = new File(Utilities.ROOT_DIR_STORAGE_PICTURE_CACHE);
        if (!rootFilePicture.exists())
            rootFilePicture.mkdir();

        File rootFileHtml = new File(Utilities.ROOT_DIR_STORAGE_HTML_CACHE);
        if (!rootFileHtml.exists())
            rootFileHtml.mkdir();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission(){
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
    }

    void setClick(){

//        S??? ki???n ???n item trong list tin t???c
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    N???u quay ngang m??n h??nh, hi???n th??? sang b??n ph???i
                    webView.getSettings().setJavaScriptEnabled(true);
                    // Load local HTML from url
                    webView.loadUrl(newsList.get(i).getLink());
                }else {
//                    N???u quay d???c, xem tin trong m??n h??nh m???i
                    Intent intent = new Intent(MainActivity.this, WebActivity.class);
                    intent.putExtra("ITEM",newsList.get(i));
                    startActivity(intent);
                }
            }
        });

//        Set s??? ki???n ???n n??t chon website
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDialogSelectWeb();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    //    S??? ki???n khi ???n n??t back
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()){
            case R.id.action_localstorage:
                startActivity(new Intent(this, LocalStorageActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //    H???p ch???n c??c website
    public void displayDialogSelectWeb() {
        final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_select_website, null);
        Button btn = dialogLayout.findViewById(R.id.btn_test);
//        M???i cardview l?? m???t website
        CardView cardViewVnExpress = dialogLayout.findViewById(R.id.vnexpress);
        CardView cardViewThanhNien = dialogLayout.findViewById(R.id.thanhnien);
        CardView cardViewTuoiTre = dialogLayout.findViewById(R.id.tuoitre);
        CardView cardViewVtc = dialogLayout.findViewById(R.id.vtc);
        CardView cardViewCand = dialogLayout.findViewById(R.id.cand);
        CardView cardView24h = dialogLayout.findViewById(R.id.haitu);

        AlertDialog.Builder editDialog = new AlertDialog.Builder(this);
        editDialog.setView(dialogLayout);
        AlertDialog dialog = editDialog.create();


//        ?????i m??u theo lo???i website ??ang ch???n
        switch (enumWebSiteDefault){
            case VNEXPRESS: cardViewVnExpress.setCardBackgroundColor(0xFF94D5E1); break;
            case THANHNIEN: cardViewThanhNien.setCardBackgroundColor(0xFF94D5E1); break;
            case TUOITRE: cardViewTuoiTre.setCardBackgroundColor(0xFF94D5E1); break;
            case HAITUH: cardView24h.setCardBackgroundColor(0xFF94D5E1); break;
            case VTC: cardViewVtc.setCardBackgroundColor(0xFF94D5E1); break;
            case CAND: cardViewCand.setCardBackgroundColor(0xFF94D5E1); break;
        }

//        C??i ?????t c??c s??? ki???n khi ???n v??o website
//        C??c b?????c
//        ???n h???p ch???n
//        Set ???nh drawer theo website v???a ch???n
//        Thay ?????i bi???n gi?? tr??? web ??ang xem
//        L??u gi?? tr??? web l???n cu???i m??? v??o SharedPreferences
//        G???i h??m l???y d??? li???u theo website m???i ch???n
        cardViewVnExpress.setOnClickListener(view -> {
            dialog.dismiss();
            Picasso.get().load(R.drawable.logo_vnexpress).into(imageViewHeaderDrawer);
            enumWebSiteDefault = EnumWebSite.VNEXPRESS;
            MySharedPreferences.setPrefDefaultWebsite(MainActivity.this,enumWebSiteDefault.name());
            new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews,enumWebSiteDefault).getLink());
        });

        cardViewThanhNien.setOnClickListener(view -> {
            dialog.dismiss();
            Picasso.get().load(R.drawable.logo_thanhnien).into(imageViewHeaderDrawer);
            enumWebSiteDefault = EnumWebSite.THANHNIEN;
            MySharedPreferences.setPrefDefaultWebsite(MainActivity.this,enumWebSiteDefault.name());
            new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews,enumWebSiteDefault).getLink());
        });

        cardViewTuoiTre.setOnClickListener(view -> {
            dialog.dismiss();
            Picasso.get().load(R.drawable.logo_tuoitre).into(imageViewHeaderDrawer);
            enumWebSiteDefault = EnumWebSite.TUOITRE;
            MySharedPreferences.setPrefDefaultWebsite(MainActivity.this,enumWebSiteDefault.name());
            new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews,enumWebSiteDefault).getLink());
        });

        cardViewVtc.setOnClickListener(view -> {
            dialog.dismiss();
            Picasso.get().load(R.drawable.logo_vtc).into(imageViewHeaderDrawer);
            enumWebSiteDefault = EnumWebSite.VTC;
            MySharedPreferences.setPrefDefaultWebsite(MainActivity.this,enumWebSiteDefault.name());
            new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews,enumWebSiteDefault).getLink());
        });

        cardViewCand.setOnClickListener(view -> {
            dialog.dismiss();
            Picasso.get().load(R.drawable.logo_cand).into(imageViewHeaderDrawer);
            enumWebSiteDefault = EnumWebSite.CAND;
            MySharedPreferences.setPrefDefaultWebsite(MainActivity.this,enumWebSiteDefault.name());
            new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews,enumWebSiteDefault).getLink());
        });

        cardView24h.setOnClickListener(view -> {
            dialog.dismiss();
            Picasso.get().load(R.drawable.logo_24h).into(imageViewHeaderDrawer);
            enumWebSiteDefault = EnumWebSite.HAITUH;
            MySharedPreferences.setPrefDefaultWebsite(MainActivity.this,enumWebSiteDefault.name());
            new ReadRss().execute(databaseHelper.getRssLink(enumTypeNews,enumWebSiteDefault).getLink());
        });
//        MenuItem item = optionsMenu.findItem(R.id.nav_baothaibinh);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuItem logoutItem = menuNav.findItem(R.id.nav_baothaibinh);
                logoutItem.setVisible(true);
            }
        });
        dialog.show();
    }

    void setImageViewHeaderDrawer(){
        switch (enumWebSiteDefault){
            case VNEXPRESS: Picasso.get().load(R.drawable.logo_vnexpress).into(imageViewHeaderDrawer); break;
            case THANHNIEN: Picasso.get().load(R.drawable.logo_thanhnien).into(imageViewHeaderDrawer); break;
            case TUOITRE: Picasso.get().load(R.drawable.logo_tuoitre).into(imageViewHeaderDrawer); break;
            case HAITUH: Picasso.get().load(R.drawable.logo_24h).into(imageViewHeaderDrawer); break;
            case VTC: Picasso.get().load(R.drawable.logo_vtc).into(imageViewHeaderDrawer); break;
            case CAND: Picasso.get().load(R.drawable.logo_cand).into(imageViewHeaderDrawer); break;
        }
    }


    //    H??m l???y gi?? tr???
    @SuppressLint("StaticFieldLeak")
    private class ReadRss extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder stringBuilder = new StringBuilder();

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() == null ||
                    !cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                return null;
            }

            try {
//                G???i ?????n url ????? l???y k???t qu??? xml tr??? v??? ch???a c??c th??ng tin b??i b??o
                URL url = new URL(strings[0]);
                InputStreamReader inputStreamReader = new InputStreamReader(url.openConnection().getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();

                newsList.clear();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }

        //        Ti???n h??nh ?????c k???t qu??? tr??? v???
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        protected void onPostExecute(String s) {
            if(s==null) return;
            DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
            Document doc = Jsoup.parse(s, "", Parser.xmlParser());

//            Ti???n h??nh ?????c v???i Jsoup
//            B??c t??ch c??c th??? item ch???a c??c b??i b??o
            Elements itemElements = doc.getElementsByTag("item");
            for (int i = 0; i < itemElements.size(); i++) {
                Element item = itemElements.get(i);
//                L???y th??ng tin th??? title - ti??u ?????
                String title = removeCdata(item.getElementsByTag("title").first().text());
//                L???y th??ng tin ng??y ????ng
                String pubDate = removeCdata(item.getElementsByTag("pubDate").first().text()).trim();
//                C???t ph???n ??u??i m??i gi??? ??? chu???i th???i gian
                pubDate = pubDate.substring(0, pubDate.lastIndexOf(" "));
//                L???y th??ng tin link tin
                String link = removeCdata(item.getElementsByTag("link").first().text());
//                L???y m?? t???
//                M?? t??? c???a website vtc.vn c??c l???y kh??c c??c web c??n l???i
                Document descriptionDoc;
                if(enumWebSiteDefault.equals(EnumWebSite.VTC))
                    descriptionDoc = Jsoup.parse(removeCdata(item.getElementsByTag("description").first().text()));
                else
                    descriptionDoc = Jsoup.parse(removeCdata(item.getElementsByTag("description").first().toString().trim()));

//                L???y ???nh thumbnail
                String imageLink = descriptionDoc.getElementsByTag("img").first()!=null?descriptionDoc.getElementsByTag("img").first().attr("src"):null;
                String description = descriptionDoc.text();

//                Sau khi l???y c??c gi?? tr???, ????a v??o 1 news
                News news = new News();
//                Set c??c gi?? tr??? ti??u ?????, m?? t???, link, ???nh thumbnail, laoij tin, website
                news.setTitle(title);
                news.setDescription(description);
                news.setLink(link);
                news.setImage(imageLink);
                news.setTypeNews(enumTypeNews);
                news.setWebSite(enumWebSiteDefault);
                news.setSaved(false);
                try {
                    news.setPubdate(formatter.parse(pubDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                newsList.add(news);
            }

//            C???p nh???t thay ?????i giao di???n
            adapterItemNews.notifyDataSetChanged();

//            K??o danh s??ch l??n ?????u
            listView.smoothScrollToPosition(0);

//            N???u m??n h??nh quay ngang, load giao di???n tin ?????u danh s??ch sang m??n h??nh b??n ph???i
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setLoadWithOverviewMode(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.setWebViewClient(new WebViewClient(){

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);

                        return true;
                    }
                    @Override
                    public void onPageFinished(WebView view, final String url) {
                    }
                });

                webView.loadUrl(newsList.get(0).getLink());
            }

            super.onPostExecute(s);
        }
    }

    //    X??a ch??? CDATA
    String removeCdata(String data) {
        data = data.replace("<![CDATA[", "");
        data = data.replace("]]>", "");
        return data.trim();
    }

}