package com.level.hub.launcher.adapters;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.level.hub.launcher.config.Config;

import org.json.JSONException;
import org.json.JSONObject;

public class NewsAdapter {
    private static String newsImage = null;
    private static String newsHeader = null;
    private static String newsCaption = null;
    public static boolean isLoaded = false;

    public static void loadNews(Context context, final NewsLoadListener listener) {
        if (isLoaded) {
            listener.onNewsLoaded(newsImage, newsHeader, newsCaption);
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.url_news,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            newsImage = jsonObject.getString("image");
                            newsHeader = jsonObject.getString("header");
                            newsCaption = jsonObject.getString("caption");

                            isLoaded = true;
                            listener.onNewsLoaded(newsImage, newsHeader, newsCaption);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onError(e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());
                    }
                }) {
            @Override
            protected Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return Response.error(new VolleyError(e));
                }
            }
        };

        Volley.newRequestQueue(context).add(stringRequest);
    }

    public static boolean isLoaded() {
        return isLoaded;
    }

    public static String getNewsImage() {
        return newsImage;
    }

    public static String getNewsHeader() {
        return newsHeader;
    }

    public static String getNewsCaption() {
        return newsCaption;
    }

    public interface NewsLoadListener {
        void onNewsLoaded(String imageUrl, String header, String caption);
        void onError(String error);
    }
}