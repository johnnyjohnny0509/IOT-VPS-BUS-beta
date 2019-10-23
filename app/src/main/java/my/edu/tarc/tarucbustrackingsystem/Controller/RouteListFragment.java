package my.edu.tarc.tarucbustrackingsystem.Controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import my.edu.tarc.tarucbustrackingsystem.Constants.Constants;
import my.edu.tarc.tarucbustrackingsystem.Engine.RouteEngine;
import my.edu.tarc.tarucbustrackingsystem.Engine.SqlEngine;

import my.edu.tarc.tarucbustrackingsystem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by WenYang on 17/8/2015.
 */
public class RouteListFragment extends Fragment implements TextToSpeech.OnInitListener {

    private Context mContext;
    //private long listviewSelected;
    private ListView lvRouteList;
    private TextView tvRoute, tvID;

    private TextToSpeech textToSpeech;
    private ProgressDialog progressBar;


    SqlEngine sqlEngine;

    //region Life Cycle
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mContext = activity;
        sqlEngine = new SqlEngine(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        progressBar = new ProgressDialog(this.getActivity());
        progressBar.setCancelable(true);
        progressBar.setMessage("Loading Route...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (!progressBar.isShowing()) {
            progressBar.show();
        }
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        lvRouteList = (ListView) view.findViewById(R.id.listView);

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        if (isConnectingToInternet()) {
            RouteEngine routeEngine = new RouteEngine();

            routeEngine.getRoutesList(mContext, new RouteEngine.RouteEngine_CompletionHandler() {

                @Override
                public void onCompleted(final ArrayList<HashMap<String, String>> routesArrayList) {
                    ;
                    boolean loaded = false;
                    while (!loaded) {

                        if (getActivity() == null) {
                            return;
                        }
                        setupRoutesList(getActivity(), routesArrayList);
                        loaded = true;
                        sqlEngine.removedAllRecords();
                        sqlEngine.insertRoute(routesArrayList);

                        if (progressBar.isShowing()) {
                            progressBar.dismiss();
                        }
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    populateRoutesFromDatabase();
                }

                @Override
                public void onNothingReturn(String errorMessage) {

                }
            });
        } else {
            populateRoutesFromDatabase();
            if (progressBar.isShowing()) {
                progressBar.dismiss();
            }

        }

        tvRoute = (TextView) getActivity().findViewById(R.id.tvRouteName);
        tvRoute.setTextColor(Color.RED);

        tvID = (TextView) getActivity().findViewById(R.id.tvRouteId);
        lvRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//                Log.e("id11",""+parent.getSelectedItemId());

                // getting values from selected ListItem
                //  lvRouteList.setBackgroundColor(Color.WHITE);
                //  view.setBackgroundColor(Color.GREEN);
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();
                String routeName = ((TextView) view.findViewById(R.id.name)).getText()
                        .toString();

                tvRoute.setText(routeName);
                tvRoute.setTextColor(Color.BLACK);
                tvID.setText(pid);
                view.setSelected(true);
                textToSpeech.speak(routeName, TextToSpeech.QUEUE_FLUSH, null);

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        textToSpeech = new TextToSpeech(mContext, this);
    }

    @Override
    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }
    //endregion

    //region Text To Speech
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("id"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }
    //endregion

    public void setupRoutesList(FragmentActivity fragmentActivity, final ArrayList<HashMap<String, String>> routesArrayList) {
        fragmentActivity.runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new SimpleAdapter(
                        mContext, routesArrayList,
                        R.layout.list_route, new String[]{Constants.JSON_TAG_ROUTEID,
                        Constants.JSON_TAG_ROUTENAME},
                        new int[]{R.id.pid, R.id.name});
                // updating listview
                lvRouteList.setAdapter(adapter);
            }
        });
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public void populateRoutesFromDatabase() {
        ArrayList<HashMap<String, String>> routesArrayList = sqlEngine.retrieveRoutes();
        if (routesArrayList != null) {
            setupRoutesList(getActivity(), routesArrayList);
        }
    }
}
