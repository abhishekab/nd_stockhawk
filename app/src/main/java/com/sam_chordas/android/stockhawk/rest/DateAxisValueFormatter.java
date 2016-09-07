package com.sam_chordas.android.stockhawk.rest;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//
//Credit to Yasir-Ghunaim
// https://github.com/Yasir-Ghunaim

public class DateAxisValueFormatter implements AxisValueFormatter
{
    private long referenceTimestamp;
    private DateFormat mDataFormat= new SimpleDateFormat("dd-MMM-yy",Locale.getDefault());
    private Date mDate=new Date();

    public DateAxisValueFormatter(long referenceTimestamp) {
        this.referenceTimestamp = referenceTimestamp;

    }



    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        long convertedTimestamp = (long) value;

        long originalTimestamp = referenceTimestamp + convertedTimestamp;

        return getTimeStampString(originalTimestamp);
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }

    private String getTimeStampString(long timestamp){
        try{
            mDate.setTime(timestamp);
            return mDataFormat.format(mDate);
        }
        catch(Exception ex){
            return "xx-xx";
        }
    }
}