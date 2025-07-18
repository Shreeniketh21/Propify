package com.shreeniketh.propify;

import android.content.Context;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyUtils {

    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_SOLD = "SOLD";
    public static final String AD_STATUS_RENTED = "RENTED";

    public static final String USER_TYPE_GOOGLE="Google";
    public static final String USER_TYPE_EMAIL="Email";
    public static final String USER_TYPE_PHONE="Phone";

    public static final String[] propertyTypes = {"Homes", "Plots", "Commmercial"};
    public static final String[] propertyTypesHomes = {"House", "Flat", "Upper Portion", "Lower Portion", "Farm House", "Room", "Penthouse"};
    public static final String[] propertyTypesPlots = {"Residential Plot", "Commercial Plot", "Agricultural Plot", "Industrial Plot", "Plot File", "Plot File", "Plot Form"};
    public static final String[] propertyTypesCommercial = {"Office", "Shop", "Warehouse", "Factory", "Building", "Other"};
    public static final String[] propertyAreaSizeUnit = {"Square Feet", "Square Yards", "Square Meters", "Marla", "Kanal"};

    public static final String PROERTY_PURPOSE_ANY = "Any";
    public static final String PROERTY_PURPOSE_SELL = "Sell";
    public static final String PROERTY_PURPOSE_RENT = "Rent";

    public static void toast(Context context,String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
    public static long timestamp(){
        return System.currentTimeMillis();
    }

    public static String formatTimestammpDate(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return "N/A"; // Handle null/invalid timestamps gracefully
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        return sdf.format(calendar.getTime());
    }

    public static String formatCurrency(Double price){
        NumberFormat numberFormat=NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(price);
    }

}
