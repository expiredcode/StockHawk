package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Vale on 20/05/2017.
 */

public class StockDetailActivity extends AppCompatActivity {

    private static String TAG = StockDetailActivity.class.getSimpleName();
    private LineChart mChart;
    private String message;
    private TextView mPrice;
    private TextView mPercentage;
    private TextView mSymbol;
    private Toolbar mToolbar;

    private static String[] projection = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_HISTORY
    };

    private static int INDEX_SYMBOL = 0;
    private static int INDEX_PRICE = 1;
    private static int INDEX_PERC_CHANGE=2;
    private static int INDEX_HISTORY = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_detail_activity);

        mChart = (LineChart) findViewById(R.id.dt_chart);
        mPercentage = (TextView) findViewById(R.id.dt_percentage);
        mPrice = (TextView) findViewById(R.id.dt_price);
        mSymbol = (TextView) findViewById(R.id.dt_symbol);

        Intent intent = getIntent();
        if(intent!=null){
            message = intent.getStringExtra(Intent.EXTRA_TEXT);
            populate(message);
        }
    }

    private void populate(String message){
        Cursor data = getContentResolver().query(Contract.Quote.URI,projection, Contract.Quote.COLUMN_SYMBOL+"=?",new String[]{message},null);
        if(data.moveToFirst()) {
            String history = data.getString(INDEX_HISTORY);

            String symbol = data.getString(INDEX_SYMBOL);
            mSymbol.setText(symbol);
            mSymbol.setContentDescription(getString(R.string.a11y_symbol,symbol));
            String price = Float.toString(data.getFloat(INDEX_PRICE));
            mPrice.setText("$" + price);
            mPrice.setContentDescription(getString(R.string.a11y_price,price));
            float perc = data.getFloat(INDEX_PERC_CHANGE);
            String percentage = Float.toString(perc);
            if(perc <0)
                mPercentage.setBackgroundResource(R.drawable.percent_change_pill_red);
            else
                percentage="+"+percentage;
            mPercentage.setText(percentage + "%");
            mPercentage.setContentDescription(getString(R.string.a11y_percentage,percentage));

            makeGraph(history);
        }
    }

    private void makeGraph(String history){
        List<Entry> entries = new ArrayList<Entry>();
        //int i=0;
        final List<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;
        String[] lines = history.split("\n");
        for(int i=lines.length-1;i>=0;i--){
            String[] v = lines[i].split(",");
            xAxisValues.add(Long.valueOf(v[0]));
            //entries.add(new Entry(Float.parseFloat(v[0]),Float.parseFloat(v[1])));
            entries.add(new Entry((float)(xAxisPosition),Float.parseFloat(v[1])));

            xAxisPosition++;
        }
        LineDataSet dataSet = new LineDataSet(entries, "Chart Graph");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setLineWidth(1.75f);
        dataSet.setCircleRadius(1f);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setValueTextColor(Color.BLACK);
        if(mPercentage.getText().toString().contains("-"))
        {
            dataSet.setColor(Color.RED);
        }else {
            dataSet.setColor(Color.GREEN);
        }
        dataSet.setCircleColor(Color.WHITE);

        mChart.setBackgroundColor(Color.WHITE);
        mChart.setNoDataTextColor(Color.WHITE);
        mChart.setTouchEnabled(false);
        mChart.getXAxis().setTextColor(R.color.colorPrimaryDark);
        mChart.getAxisLeft().setTextColor(R.color.colorPrimaryDark);
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
       // mChart.invalidate(); // refresh
        mChart.animateX(4500);



        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get((int)value));
                return  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
            }
        });
    }
}
