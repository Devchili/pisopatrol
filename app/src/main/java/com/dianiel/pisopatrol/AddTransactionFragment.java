package com.dianiel.pisopatrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.dianiel.pisopatrol.R;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.gson.reflect.TypeToken;

public class AddTransactionFragment extends Fragment {

    private static final String SAVINGS_PREFERENCES = "savings_prefs";
    private static final String SAVINGS_KEY = "savings";
    private static final String TRANSACTION_PREFERENCES = "transaction_prefs";
    private static final String TRANSACTION_LIST_KEY = "transaction_list";

    private EditText titleEditText;
    private EditText amountEditText;
    private RadioGroup transactionTypeRadioGroup;
    private RadioButton expenseRadioButton;
    private RadioButton allowanceRadioButton;
    private TextView categoryTextView;
    private Spinner categorySpinner;
    private EditText noteEditText;
    private Spinner dateDurationSpinner;
    private Button addButton;

    private TextView dateDurationTextView;
    private SharedPreferences savingsSharedPreferences;
    private SharedPreferences transactionSharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        titleEditText = view.findViewById(R.id.edit_text_title);
        amountEditText = view.findViewById(R.id.edit_text_amount);
        transactionTypeRadioGroup = view.findViewById(R.id.radio_group_transaction_type);
        expenseRadioButton = view.findViewById(R.id.radio_button_expense);
        allowanceRadioButton = view.findViewById(R.id.radio_button_allowance);
        categoryTextView = view.findViewById(R.id.text_view_category);
        categorySpinner = view.findViewById(R.id.spinner_category);
        noteEditText = view.findViewById(R.id.edit_text_note);
        dateDurationSpinner = view.findViewById(R.id.spinner_date_duration);
        addButton = view.findViewById(R.id.button_add_transaction);
        dateDurationTextView = view.findViewById(R.id.text_view_date_duration);

        // Populate category spinner
        List<String> categories = new ArrayList<>();
        categories.add("Food");
        categories.add("Rent");
        categories.add("Transit");
        categories.add("School Related");
        categories.add("Other");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(categoryAdapter);

        // Populate date duration spinner
        List<String> dateDurations = new ArrayList<>();
        dateDurations.add("1 week");
        dateDurations.add("2 weeks");
        dateDurations.add("3 weeks");
        dateDurations.add("4 weeks");
        dateDurations.add("1 month");
        ArrayAdapter<String> dateDurationAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dateDurations);
        dateDurationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateDurationSpinner.setAdapter(dateDurationAdapter);

        savingsSharedPreferences = requireContext().getSharedPreferences(SAVINGS_PREFERENCES, Context.MODE_PRIVATE);
        transactionSharedPreferences = requireContext().getSharedPreferences(TRANSACTION_PREFERENCES, Context.MODE_PRIVATE);

        transactionTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_button_expense) {
                    // Expense transaction selected, show category views
                    categoryTextView.setVisibility(View.VISIBLE);
                    categorySpinner.setVisibility(View.VISIBLE);
                    dateDurationSpinner.setVisibility(View.GONE);
                    dateDurationTextView.setVisibility(View.GONE);

                } else {
                    // Allowance transaction selected, hide category views
                    categoryTextView.setVisibility(View.GONE);
                    categorySpinner.setVisibility(View.GONE);
                    dateDurationSpinner.setVisibility(View.VISIBLE);
                    dateDurationTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTransaction();
            }
        });

        return view;
    }

    private void addTransaction() {
        String title = titleEditText.getText().toString();
        String amountString = amountEditText.getText().toString();
        String category = "";
        String dateDuration = dateDurationSpinner.getSelectedItem().toString();

        if (title.isEmpty() || amountString.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        float amount = Float.parseFloat(amountString);
        float currentSavings = savingsSharedPreferences.getFloat(SAVINGS_KEY, 0);

        if (transactionTypeRadioGroup.getCheckedRadioButtonId() == R.id.radio_button_expense) {
            // Expense transaction
            // Subtract from savings
            float newSavings = currentSavings - amount;
            if (newSavings >= 0) {
                SharedPreferences.Editor editor = savingsSharedPreferences.edit();
                editor.putFloat(SAVINGS_KEY, newSavings);
                editor.apply();
                Toast.makeText(requireContext(), "Expense transaction added. Savings updated.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Insufficient savings.", Toast.LENGTH_SHORT).show();
                return; // Return if savings are insufficient
            }

            // Get selected category for expense transaction
            category = categorySpinner.getSelectedItem().toString();
        } else if (transactionTypeRadioGroup.getCheckedRadioButtonId() == R.id.radio_button_allowance) {
            // Allowance transaction
            // Add to savings
            float newSavings = currentSavings + amount;
            SharedPreferences.Editor editor = savingsSharedPreferences.edit();
            editor.putFloat(SAVINGS_KEY, newSavings);
            editor.apply();
            Toast.makeText(requireContext(), "Allowance transaction added. Savings updated.", Toast.LENGTH_SHORT).show();
        }

        // Update recent transactions in SharedPreferences
        Gson gson = new Gson();
        String transactionJson = transactionSharedPreferences.getString(TRANSACTION_LIST_KEY, "");
        Type type = new TypeToken<List<Transaction>>() {}.getType();
        List<Transaction> transactions = gson.fromJson(transactionJson, type);
        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        // Add transaction to the list
        String note = noteEditText.getText().toString(); // Get note text
        transactions.add(new Transaction(title, amount, note, category, dateDuration, expenseRadioButton.isChecked() ? "Expense" : "Allowance"));

        SharedPreferences.Editor transactionEditor = transactionSharedPreferences.edit();
        String updatedTransactionJson = gson.toJson(transactions);
        transactionEditor.putString(TRANSACTION_LIST_KEY, updatedTransactionJson);
        transactionEditor.apply();

        // After updating savings and recent transactions, navigate to HomeFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }


}