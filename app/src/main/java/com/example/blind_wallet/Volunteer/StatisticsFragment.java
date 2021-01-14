package com.example.blind_wallet.Volunteer;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.blind_wallet.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {


    BarChart barChart ;
    ArrayList<BarEntry> barEntryArrayList;
    ArrayList <String> lablesNames;
    ArrayList<MonthDalesData> monthDalesDataArrayList = new ArrayList<>();
    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        barChart = (BarChart)view.findViewById(R.id.barChart);
        barEntryArrayList = new ArrayList<>();
        lablesNames = new ArrayList<>();
//create new object of bare enteries arraylist and lable arraylist
        barEntryArrayList.clear();
        lablesNames.clear();
        fillMonthSales();

        for(int i =0 ; i< monthDalesDataArrayList.size();i++) {
            String month = monthDalesDataArrayList.get(i).getMonth();
            int sales = monthDalesDataArrayList.get(i).getSales();
            barEntryArrayList.add(new BarEntry(i,sales));
            lablesNames.add(month);
        }

        BarDataSet barDataSet = new BarDataSet(barEntryArrayList,"Monthly Sales");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        Description description = new Description();
        description.setText("Months");
        barChart.setDescription(description);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        // we need to set Axis value formater
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(lablesNames));
        // set poition of lables
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(lablesNames.size());
        xAxis.setLabelRotationAngle(270);
        barChart.animateY(2000);
        barChart.invalidate();
        return view;
    }
    private void fillMonthSales(){
        monthDalesDataArrayList.clear();
        monthDalesDataArrayList.add(new MonthDalesData("January",252000));
        monthDalesDataArrayList.add(new MonthDalesData("February",200000));
        monthDalesDataArrayList.add(new MonthDalesData("March",300000));
        monthDalesDataArrayList.add(new MonthDalesData("April",190000));
        monthDalesDataArrayList.add(new MonthDalesData("May",200000));
        monthDalesDataArrayList.add(new MonthDalesData("June",30000));
        monthDalesDataArrayList.add(new MonthDalesData("July",270000));
        monthDalesDataArrayList.add(new MonthDalesData("August",200000));
    }

}

class MonthDalesData {

    String month ;
    int sales ;
    public MonthDalesData(String month , int sales){
        this.month = month;
        this.sales = sales;
    }
    public String getMonth (){
        return month;
    }
    public void setMonth(String month){
        this.month = month;
    }
    public int getSales (){
        return sales;
    }
    public void setSales(int sales){
        this.sales = sales;
    }

}