package com.dianiel.pisopatrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TRANSACTION_PREFERENCES = "transaction_prefs";
    private static final String TRANSACTION_LIST_KEY = "transaction_list";
    private static final String SAVINGS_PREFERENCES = "savings_prefs";
    private static final String SAVINGS_KEY = "savings";

    private TextView recentTransactionsTextView;
    private TextView savingsTextView;
    private TextView savingStatusTextView;
    private TextView savingsChangeIndicatorTextView;
    private TextView retainedSavingsTextView;
    private List<Transaction> recentTransactions;
    private List<Transaction> retainedSavingsList = new ArrayList<>();
    private SharedPreferences transactionSharedPreferences;
    private SharedPreferences savingsSharedPreferences;
    private Gson gson;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recentTransactionsTextView = view.findViewById(R.id.text_view_recent_transactions);
        savingsTextView = view.findViewById(R.id.text_view_savings);
        savingStatusTextView = view.findViewById(R.id.text_view_saving_status);
        savingsChangeIndicatorTextView = view.findViewById(R.id.text_view_savings_change_indicator);
        retainedSavingsTextView = view.findViewById(R.id.text_view_retained_savings);

        transactionSharedPreferences = requireContext().getSharedPreferences(TRANSACTION_PREFERENCES, Context.MODE_PRIVATE);
        savingsSharedPreferences = requireContext().getSharedPreferences(SAVINGS_PREFERENCES, Context.MODE_PRIVATE);

        gson = new Gson();
        recentTransactions = getRecentTransactionsFromSharedPreferences();

        float currentSavings = savingsSharedPreferences.getFloat(SAVINGS_KEY, 0);
        float lastSavings = getLastSavings();

        updateRecentTransactionsTextView();
        updateSavingsTextView(currentSavings);
        updateSavingStatusTextView(currentSavings, lastSavings);
        checkAndNotifyExpiredAllowanceTransactions();

        // Initialize the dropdownButton ImageView and set its OnClickListener
        ImageView dropdownButton = view.findViewById(R.id.dropdown_button);
        dropdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropdownMenu(v); // Method to show the dropdown menu
            }
        });

        return view;
    }


    private void showDropdownMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_home, popupMenu.getMenu()); // Inflate your menu resource
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle menu item clicks here
                if (item.getItemId() == R.id.action_reset) {
                    resetData();
                    return true;
                } else if (item.getItemId() == R.id.action_about) {
                    openAboutActivity();
                    return true;
                } else {
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    private void openAboutActivity() {
        Intent intent = new Intent(requireContext(), AboutActivity.class);
        startActivity(intent);
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            // Set custom layout params to adjust width
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    100, // Set a fixed width for the menu item (in pixels)
                    ViewGroup.LayoutParams.MATCH_PARENT); // Set height as match_parent
            item.getActionView().setLayoutParams(params);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reset) {
            resetData();
            return true;
        } else if (item.getItemId() == R.id.action_about) {
            openAboutActivity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void resetData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to reset all data? This action cannot be undone.");
        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Clear recent transactions
                recentTransactions.clear();
                saveRecentTransactionsToSharedPreferences();
                updateRecentTransactionsTextView();

                // Reset savings to 0
                savingsSharedPreferences.edit().putFloat(SAVINGS_KEY, 0).apply();
                updateSavingsTextView(0);

                // Clear retained savings list
                retainedSavingsList.clear();
                displayRetainedSavingsList();

                // Update saving status text view
                updateSavingStatusTextView(0, 0);

                // Notify user
                Toast.makeText(requireContext(), "Data has been reset.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User cancelled the reset action, do nothing
            }
        });
        builder.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void checkAndNotifyExpiredAllowanceTransactions() {
        long currentMillis = System.currentTimeMillis();

        for (Transaction transaction : recentTransactions) {
            if (transaction.getType().equals("Allowance")) {
                long transactionMillis = transaction.getDate().getTimeInMillis();
                long durationMillis = getDurationInMillis(transaction.getDateDuration());

                if (currentMillis >= transactionMillis + durationMillis) {
                    showAllowanceTransactionExpiredDialog(transaction);
                }
            }
        }
    }

    private void showAllowanceTransactionExpiredDialog(final Transaction expiredTransaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Allowance Transaction Expired");
        builder.setMessage("The date duration for the allowance transaction \"" + expiredTransaction.getTitle() + "\" has been reached. Do you want to remove it?");
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeExpiredTransaction(expiredTransaction);
            }
        });
        builder.setNegativeButton("Retain", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                retainTransaction(expiredTransaction);
            }
        });
        builder.show();
    }

    private void removeExpiredTransaction(Transaction expiredTransaction) {
        float lastCurrentSavings = savingsSharedPreferences.getFloat(SAVINGS_KEY, 0);

        recentTransactions.remove(expiredTransaction);
        saveRecentTransactionsToSharedPreferences();
        updateRecentTransactionsTextView();

        float currentSavings = getLastSavings();
        updateSavingsTextView(currentSavings);

        String indicator;
        if (currentSavings > lastCurrentSavings) {
            indicator = "increased";
        } else if (currentSavings < lastCurrentSavings) {
            indicator = "decreased";
        } else {
            indicator = "remained the same";
        }

        savingsChangeIndicatorTextView.setText("Savings " + indicator);
        savingsChangeIndicatorTextView.setVisibility(View.VISIBLE);
    }

    // Method to retain the expired transaction as savings
    private void retainTransaction(Transaction transaction) {
        retainedSavingsList.add(transaction);
        displayRetainedSavingsList();
    }

    private void displayRetainedSavingsList() {
        StringBuilder retainedSavingsText = new StringBuilder();
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

        for (Transaction transaction : retainedSavingsList) {
            String transactionLine = String.format("%s: %s - %s (%s)\n", transaction.getTitle(), pesoFormat.format(transaction.getAmount()), transaction.getType(), transaction.getDateDuration(), transaction.getNote());
            retainedSavingsText.append(transactionLine);
        }

        retainedSavingsTextView.setText(retainedSavingsText.toString());
    }

    private void updateRecentTransactionsTextView() {
        StringBuilder transactionText = new StringBuilder();
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

        for (Transaction transaction : recentTransactions) {
            String transactionLine;
            if (transaction.getType().equals("Allowance")) {
                transactionLine = String.format("%s: %s - %s (%s)\n", transaction.getTitle(), pesoFormat.format(transaction.getAmount()), transaction.getType(), transaction.getDateDuration(), transaction.getNote());
            } else {
                transactionLine = String.format("%s: %s - %s (%s)\n", transaction.getTitle(), pesoFormat.format(transaction.getAmount()), transaction.getType(), transaction.getCategory(), transaction.getDateDuration(), transaction.getNote());
            }
            transactionText.append(transactionLine);
        }

        recentTransactionsTextView.setText(transactionText.toString());
    }

    private void updateSavingsTextView(float currentSavings) {
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        savingsTextView.setText(String.format("Current savings: %s", pesoFormat.format(currentSavings)));
    }

    private void updateSavingStatusTextView(float currentSavings, float lastSavings) {
        float totalExpenses = 0;
        float totalSavings = currentSavings - lastSavings;

        for (Transaction transaction : recentTransactions) {
            if (transaction.getType().equals("Expense")) {
                totalExpenses += transaction.getAmount();
            }
        }

        String savingStatus;

        if (totalSavings == 0) {
            savingStatus = "Poor";
        } else if (totalExpenses > totalSavings) {
            savingStatus = "Very Poor";
        } else if (totalSavings > totalExpenses * 2) {
            savingStatus = "Excellent";
        } else if (totalSavings > totalExpenses) {
            savingStatus = "Very Good";
        } else if (totalSavings >= totalExpenses * 0.5) {
            savingStatus = "Good";
        } else {
            savingStatus = "Poor";
        }

        savingStatusTextView.setText("Savings status: " + savingStatus);
    }

    private List<Transaction> getRecentTransactionsFromSharedPreferences() {
        String json = transactionSharedPreferences.getString(TRANSACTION_LIST_KEY, "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<List<Transaction>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    private float getLastSavings() {
        return 0; // Return last savings, you may implement your logic here
    }

    private long getDurationInMillis(String duration) {
        String[] parts = duration.split(" ");
        if (parts.length != 2) {
            return 0; // Invalid format, return 0
        }

        int value;
        try {
            value = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return 0; // Invalid number, return 0
        }

        long milliseconds;
        if (parts[1].equalsIgnoreCase("week") || parts[1].equalsIgnoreCase("weeks")) {
            milliseconds = value * 7 * 24 * 60 * 60 * 1000L; // Convert weeks to milliseconds
        } else if (parts[1].equalsIgnoreCase("month") || parts[1].equalsIgnoreCase("months")) {
            milliseconds = value * 4 * 7 * 24 * 60 * 60 * 1000L; // Convert months to milliseconds (assuming 4 weeks per month)
        } else {
            return 0; // Unsupported unit, return 0
        }

        return milliseconds;
    }

    private void saveRecentTransactionsToSharedPreferences() {
        String json = gson.toJson(recentTransactions);
        transactionSharedPreferences.edit().putString(TRANSACTION_LIST_KEY, json).apply();
    }
}
