package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.CustomMarkerView;
import com.sam_chordas.android.stockhawk.rest.DateAxisValueFormatter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StocksDetailActivity extends AppCompatActivity {
    public static final String SYMBOL_KEY = "symbol";
    public static final int NO_OF_MONTHS = 6;
    private static final String LOG_TAG = StocksDetailActivity.class.getSimpleName();
    private String mSymbol;
    private OkHttpClient client = new OkHttpClient();

    private LineChart mChart;
    private long mReferenceTime = 0;
    private ProgressBar progressBarFetching;

    void fetchData(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        progressBarFetching.setVisibility(View.VISIBLE);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.toString());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarFetching.setVisibility(View.GONE);
                        Toast.makeText(StocksDetailActivity.this, R.string.error_fetch_stock_history, Toast.LENGTH_SHORT).show();
                        Utils.checkNetworkAvailable(StocksDetailActivity.this);
                    }
                });

            }

            @Override
            public void onResponse(Response response) throws IOException {

                int statusCode = response.code();
                Log.e(LOG_TAG, " status code:" + statusCode);
                final List<Entry> listStockEntry = getEntriesFromJSON(response.body().string());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarFetching.setVisibility(View.GONE);
                        if (listStockEntry == null || listStockEntry.isEmpty()) {
                            Toast.makeText(StocksDetailActivity.this, R.string.error_fetch_stock_history, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        LineDataSet dataSet = new LineDataSet(listStockEntry, getString(R.string.closing));
                        dataSet.setColor(Color.BLUE);
                        dataSet.setLineWidth(2f);
                        dataSet.setValueTextColor(Color.RED);

                        LineData lineData = new LineData(dataSet);
                        AxisValueFormatter xAxisFormatter = new DateAxisValueFormatter(mReferenceTime);
                        XAxis xAxis = mChart.getXAxis();
                        xAxis.setValueFormatter(xAxisFormatter);
                        YAxis yAxisLeft=mChart.getAxisLeft();
                        YAxis yAxisRight=mChart.getAxisRight();
                        AxisValueFormatter YValueFormatter=new AxisValueFormatter() {

                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return "$"+(int)value;
                            }

                            @Override
                            public int getDecimalDigits() {
                                return 0;
                            }
                        };
                        yAxisLeft.setValueFormatter(YValueFormatter);
                        yAxisRight.setValueFormatter(YValueFormatter);
                        CustomMarkerView customMarkerView=new CustomMarkerView(StocksDetailActivity.this,R.layout.marker_view,mReferenceTime);
                        mChart.setMarkerView(customMarkerView);
                        mChart.setData(lineData);
                        mChart.setDescription(mSymbol);
                        mChart.invalidate();

                    }
                });
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mSymbol = getIntent().getStringExtra(SYMBOL_KEY);
        setTitle(mSymbol);
        setContentView(R.layout.activity_stocks_detail);
        progressBarFetching = (ProgressBar) findViewById(R.id.progressBarFetching);
        Log.d(LOG_TAG, getUrlStringFromSymbol(mSymbol));
        mChart = (LineChart) findViewById(R.id.lineChart);
        fetchData(getUrlStringFromSymbol(mSymbol));

    }


    String getUrlStringFromSymbol(String symbol) {
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = "
                    , "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\"" + symbol + "\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode(" and startDate = ", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\"" + Utils.getStartDateForStockDetails(NO_OF_MONTHS) + "\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode(" and endDate = ", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\"" + Utils.getEndDateForStockDetails() + "\"", "UTF-8"));
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");
            return urlStringBuilder.toString();


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<Entry> getEntriesFromJSON(String jsonString) {
        //Log.d(LOG_TAG, jsonString);

        List<Entry> listEntry = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(jsonString);
            JSONArray quotes = object.getJSONObject("query").getJSONObject("results").getJSONArray("quote");
            for (int i = quotes.length() - 1; i >= 0; i--) {
                JSONObject quote = quotes.getJSONObject(i);
                String dateString = quote.getString("Date");
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                if (i == quotes.length()) {
                    mReferenceTime = date.getTime();
                }

                listEntry.add(new Entry(date.getTime() - mReferenceTime, (float) quote.getDouble("Close")));
                //listEntry.add(new Entry(i,(float)quote.getDouble("Close")));

            }
            return listEntry;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }
}
