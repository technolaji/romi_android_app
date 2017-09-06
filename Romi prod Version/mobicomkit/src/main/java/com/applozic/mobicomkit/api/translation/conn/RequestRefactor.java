package com.applozic.mobicomkit.api.translation.conn;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

/**
 * Created by bolajiabiodun on 02/01/2017.
 */

public class RequestRefactor {

    Connection conn = new Connection();
    private final static String TAG = RequestRefactor.class.getSimpleName();
    RequestQueue mRequestQueue;
    Context mContext;

    public void requestData(String url, Context context, final VolleyCallback callback) {

        mContext = context;

        mRequestQueue = Volley.newRequestQueue(context);
        //if (conn.isConnected(context)) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("Response", response);
                            callback.onSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        addToRequestQueue(stringRequest);


    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }
}

