package com.example.insane;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity {

    private String htmlPageUrl = "";
    private String [] htmlContentInStringFormat= new String[11];
    int photoCount=0;
    String [] mediaType = new String[11];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onButton1Clicked(View v){
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName) ;
        htmlPageUrl = editText.getText().toString() ;

        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();

        new ImageDownload().execute();
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect(htmlPageUrl).get();

                String link = "";
                Elements script = doc.select("script[type=text/javascript]");
                int i = 0;
                for(Element element : script){
                    if(i == 3){
                        link = element.toString();
                        break;
                    }
                    i++;
                }

                int index = 0;
                int isVideoIndex = 0;
                int videoUrlIndex = 0;
                while (link.indexOf("display_url", index) != -1)
                {
                    Character isVideo = link.charAt(link.indexOf("is_video", isVideoIndex) + 10);
                    if (isVideo == 't')
                    {
                        mediaType[photoCount] = "video";
                    }
                    else
                    {
                        mediaType[photoCount] = "image";
                    }

                    int urlStart = link.indexOf("display_url", index) + 14;
                    int urlEnd = link.indexOf("\"", urlStart);

                    if (isVideo == 't')
                    {
                        urlStart = link.indexOf("video_url", videoUrlIndex) + 12;
                        urlEnd = link.indexOf("\"", urlStart);
                    }

                    String urlSublink = link.substring(urlStart, urlEnd);

                    urlSublink = urlSublink.replace("\\u0026","&");
                    urlSublink = urlSublink.replace("&amp","&");
                    htmlContentInStringFormat[photoCount] = urlSublink;

                    index = urlEnd;
                    isVideoIndex = link.indexOf("is_video", isVideoIndex) + 10;
                    videoUrlIndex =urlEnd;
                    photoCount++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute (Void result){

            TextView textView = findViewById(R.id.textview_first);
            textView.setText(valueOf(photoCount) );
        }
    }
    private class ImageDownload extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            int i = 0;
            if (photoCount == 1)
            {
                i = 0;
            }
            else
            {
                i = 1;
            }
            for (;i<photoCount;i++)
            {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddhhmmss");
                String filename = "";

                String mimeType = "";

                switch (mediaType[i]) {
                    case "video":
                        filename = simpleDate.format(date) + ".mp4";
                        mimeType = "video/mp4";
                        break;
                    case "image":
                        filename = simpleDate.format(date) + ".jpg";
                        mimeType = "image/jpg";
                        break;
                    default:
                        System.out.println("Unable to download media file.");
                        break;
                }

                //웹 서버 쪽 파일이 있는 경로
                String fileUrl = htmlContentInStringFormat[i];

                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri downloadUri = Uri.parse(fileUrl);
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(filename)
                        .setMimeType(mimeType)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                                File.separator + "insane" + File.separator + filename);

                dm.enqueue(request);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}