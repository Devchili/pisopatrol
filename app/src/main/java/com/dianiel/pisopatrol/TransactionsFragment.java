package com.dianiel.pisopatrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransactionsFragment extends Fragment {

    private static final String TRANSACTION_PREFERENCES = "transaction_prefs";
    private static final String TRANSACTION_LIST_KEY = "transaction_list";

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> recentTransactions;
    private List<Transaction> filteredTransactions;
    private Spinner filterSpinner;
    private TextView totalTransactionsTextView;
    private PieChart pieChart;

    private SharedPreferences transactionSharedPreferences;
    private Gson gson;

    private Map<String, Float> categoryAmountMap;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_transactions);
        totalTransactionsTextView = view.findViewById(R.id.text_total_transactions);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        pieChart = view.findViewById(R.id.pie_chart);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#DADADA")); // Set hole color
        pieChart.setTransparentCircleRadius(61f);


        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        transactionSharedPreferences = requireContext().getSharedPreferences(TRANSACTION_PREFERENCES, Context.MODE_PRIVATE);
        gson = new Gson();

        recentTransactions = getRecentTransactionsFromSharedPreferences();
        filteredTransactions = new ArrayList<>(recentTransactions); // Initialize filteredTransactions with all transactions
        updateRecentTransactionsTextView();
        updateTotalTransactionsTextView();

        setupFilterSpinner();

        calculateCategoryAmounts();
        setupPieChart();

        return view;
    }


    private List<Transaction> getRecentTransactionsFromSharedPreferences() {
        String json = transactionSharedPreferences.getString(TRANSACTION_LIST_KEY, "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<List<Transaction>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    @SuppressLint("DefaultLocale")
    private void updateRecentTransactionsTextView() {
        adapter = new TransactionAdapter(filteredTransactions);
        recyclerView.setAdapter(adapter);
    }

    private void updateTotalTransactionsTextView() {
        int totalTransactions = filteredTransactions.size();
        totalTransactionsTextView.setText("Total Transactions: " + totalTransactions);
    }

    private void setupFilterSpinner() {
        String[] filterOptions = {"All", "Expense", "Allowance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterOptions);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = filterOptions[position];
                filterTransactions(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void filterTransactions(String filter) {
        if (filter.equals("All")) {
            filteredTransactions.clear();
            filteredTransactions.addAll(recentTransactions);
        } else {
            filteredTransactions.clear();
            for (Transaction transaction : recentTransactions) {
                if (transaction.getType().equals(filter)) {
                    filteredTransactions.add(transaction);
                }
            }
        }
        updateRecentTransactionsTextView();
        updateTotalTransactionsTextView();
        calculateCategoryAmounts();
        setupPieChart();
    }

    private void calculateCategoryAmounts() {
        categoryAmountMap = new HashMap<>();
        for (Transaction transaction : filteredTransactions) {
            String category = transaction.getCategory();
            float amount = transaction.getAmount();
            if (transaction.getType().equals("Expense")) {
                // For Expense transactions
                if (categoryAmountMap.containsKey(category)) {
                    float currentAmount = categoryAmountMap.get(category);
                    categoryAmountMap.put(category, currentAmount + amount);
                } else {
                    categoryAmountMap.put(category, amount);
                }
            } else if (transaction.getType().equals("Allowance")) {
                // For Allowance transactions
                if (categoryAmountMap.containsKey("Allowance")) {
                    float currentAmount = categoryAmountMap.get("Allowance");
                    categoryAmountMap.put("Allowance", currentAmount + amount);
                } else {
                    categoryAmountMap.put("Allowance", amount);
                }
            }
        }
    }

    private void setupPieChart() {
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryAmountMap.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(data);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL); // Set orientation to vertical
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Align at the bottom

        pieChart.invalidate();
    }



}