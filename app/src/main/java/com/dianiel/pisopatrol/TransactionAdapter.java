package com.dianiel.pisopatrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView amountTextView;
        TextView typeTextView;
        TextView noteTextView;
        TextView categoryTextView;
        TextView durationTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_title);
            amountTextView = itemView.findViewById(R.id.text_view_amount);
            typeTextView = itemView.findViewById(R.id.text_view_type);
            noteTextView = itemView.findViewById(R.id.text_view_note);
            categoryTextView = itemView.findViewById(R.id.text_view_category);
            durationTextView = itemView.findViewById(R.id.text_view_duration);
        }

        public void bind(Transaction transaction) {
            // Format for Peso currency
            NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

            titleTextView.setText(transaction.getTitle());
            amountTextView.setText(pesoFormat.format(transaction.getAmount()));
            typeTextView.setText(transaction.getType());
            noteTextView.setText("Note: " + transaction.getNote());
            durationTextView.setText("Date Duration: " + transaction.getDateDuration());

            // Check if the transaction type is "Allowance"
            if (!transaction.getType().equals("Allowance")) {
                categoryTextView.setVisibility(View.VISIBLE);
                categoryTextView.setText("Category: " + transaction.getCategory());
            } else {
                // Hide the category view if the transaction type is "Allowance"
                categoryTextView.setVisibility(View.GONE);
            }
        }
    }
}