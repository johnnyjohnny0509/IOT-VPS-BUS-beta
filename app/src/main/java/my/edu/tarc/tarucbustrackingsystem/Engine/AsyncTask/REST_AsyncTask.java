package my.edu.tarc.tarucbustrackingsystem.Engine.AsyncTask;

import android.os.AsyncTask;


import my.edu.tarc.tarucbustrackingsystem.Library.JSONParser;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by leewengyang on 1/15/16.
 * All Right Reserved
 */

public class REST_AsyncTask extends AsyncTask<Void,Void,Void> {

    private String url;
    private List<NameValuePair> parameters;

    private JSONObject responseJsonObject;

    private REST_AsyncTask_CompletionHandler rest_AsyncTask_CompletionHandler;

    public REST_AsyncTask(String url, List<NameValuePair> parameters, REST_AsyncTask_CompletionHandler rest_AsyncTask_CompletionHandler){
        this.url = url;
        this.parameters = parameters;
        this.rest_AsyncTask_CompletionHandler = rest_AsyncTask_CompletionHandler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        JSONParser jsonParser = new JSONParser();

        responseJsonObject = jsonParser.makeHttpRequest(url, "POST", parameters);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.rest_AsyncTask_CompletionHandler.onCompleted(responseJsonObject);

    }


    public interface REST_AsyncTask_CompletionHandler {
        void onCompleted(JSONObject responseJsonObject);
    }
}
