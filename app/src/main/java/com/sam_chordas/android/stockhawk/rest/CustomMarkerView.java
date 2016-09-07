package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.sam_chordas.android.stockhawk.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomMarkerView extends MarkerView {
    private DateFormat mDataFormat= new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

    private TextView tvContent;
    private long mReferenceTimeStamp;

    public CustomMarkerView (Context context, int layoutResource, long referenceTimeStamp) {
        super(context, layoutResource);
        // this markerview only displays a textview

        tvContent = (TextView) findViewById(R.id.tvContent);
        mReferenceTimeStamp=referenceTimeStamp;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvContent.setText(String.format("%s %s %s", e.getY(), getContext().getString(R.string.on), getTimeStampString((long) e.getX()))); // set the entry-value as the display text
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }

    private String getTimeStampString(long timestamp){
        try{
            Date date=new Date();
            date.setTime(timestamp-mReferenceTimeStamp);
            return mDataFormat.format(date);
        }
        catch(Exception ex){
            return "xx-xx";
        }
    }
}