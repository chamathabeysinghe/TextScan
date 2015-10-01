package com.example.chamathabeysinghe.textscan;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    final static int SELECT_PICTURE=1;
    final static String RESULT_TEXT="result";
    private String mImageFullPath;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.fragment_main, container, false);
        Button scanButton= (Button) rootView.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*when user click the button Album app will open and allow to select a photo*/
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,SELECT_PICTURE);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       /*This code executes when user select a photo */
        if(requestCode==SELECT_PICTURE && data!=null && resultCode==-1){
            Uri uri=data.getData();
            String filePathColumn[]={MediaStore.Images.Media.DATA};
            Cursor cursor=getActivity().getContentResolver().query(uri,filePathColumn,null,null,null);
            cursor.moveToFirst();
            mImageFullPath=cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            cursor.close();
            Log.e("", mImageFullPath);

            /*Connect with server to fetch data*/
            FetchData fetchData=new FetchData();
            fetchData.execute();
        }
    }

    class FetchData extends AsyncTask<Void,Void,String> {
        String  out;
        private String sendRequest() throws Exception {

            String idol_ocr_service = "https://api.idolondemand.com/1/api/async/ocrdocument/v1";
            String apikey="6efb5c3b-0ebd-4f6e-94d2-e6eff4d41b3a";
            URI uri = new URI(idol_ocr_service);
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(uri);

            /*attaching image to requese*/
            MultipartEntityBuilder r;
            MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
            reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("apikey", new StringBody(apikey, ContentType.TEXT_PLAIN));
            reqEntity.addBinaryBody("file", new File(mImageFullPath));
            reqEntity.addPart("mode", new StringBody("document_photo", ContentType.TEXT_PLAIN));
            HttpEntity e=reqEntity.build();
            httpPost.setEntity(e);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity=response.getEntity();
            String reply= EntityUtils.toString(entity);

            JSONObject jb=new JSONObject(reply);
            String jobID=jb.getString("jobID");
            return fetchText(jobID);
        }

        private String fetchText(String jobId) throws Exception {
            /*get scanned text*/
            String url="https://api.idolondemand.com/1/job/result/"+jobId+"?apikey=";
            HttpGet getRequst=new HttpGet(url);
            HttpResponse response=new DefaultHttpClient().execute(getRequst);
            HttpEntity entity=response.getEntity();
            String s=EntityUtils.toString(entity);
            JSONObject jb=new JSONObject(s);
            JSONObject j=jb.getJSONArray("actions").getJSONObject(0).getJSONObject("result")
                    .getJSONArray("text_block").getJSONObject(0);
            String scanText=j.getString("text");
            Log.e("Output::",scanText);
            out=scanText;
            return scanText;

        }


        @Override
        protected String doInBackground(Void... params) {
            //sendRequest();
            try {

                String result=sendRequest();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "No data recieved";
        }

        @Override
        protected void onPostExecute(String str) {
            Log.e("POST","EXECUTION");
            Intent intent=new Intent(getActivity(),ResultActivity.class);
            intent.putExtra(RESULT_TEXT,str);
            startActivity(intent);
        }
    }
}
