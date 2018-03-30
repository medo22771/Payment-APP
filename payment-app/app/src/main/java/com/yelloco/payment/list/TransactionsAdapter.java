package com.yelloco.payment.list;

import static com.yelloco.payment.transaction.TransactionResult.APPROVED;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yelloco.payment.R;
import com.yelloco.payment.data.db.DbConstants;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class TransactionsAdapter extends CursorAdapter {

    private Cursor mCursor;

    public TransactionsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public class ViewHolder {
        public View row;
        public TextView time;
        public TextView id;
        public TextView cardNumber;
        public TextView amount;
        public ImageView status;
        public ImageView receipt;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View retView = LayoutInflater.from(context).inflate(R.layout.history_table_row, parent, false);

        ViewHolder holder;
        holder = new ViewHolder();
        holder.row = retView;
        holder.time = (TextView) retView.findViewById(R.id.history_row_time);
        holder.id = (TextView) retView.findViewById(R.id.history_row_transaction_id);
        holder.cardNumber = (TextView) retView.findViewById(R.id.history_row_card_number);
        holder.amount = (TextView) retView.findViewById(R.id.history_row_amount);
        holder.status = (ImageView) retView.findViewById(R.id.history_row_status);
        holder.receipt = (ImageView) retView.findViewById(R.id.history_row_receipt);

        retView.setTag(holder);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        int timeId = cursor.getColumnIndex(DbConstants.TRANSACTION_DATETIME);
        int transId = cursor.getColumnIndex(DbConstants.ID);
        int cardId = cursor.getColumnIndex(DbConstants.CARD_NUMBER);
        int amountId = cursor.getColumnIndex(DbConstants.AMOUNT);
        int statusId = cursor.getColumnIndex(DbConstants.STATUS);

        String id = cursor.getString(transId);
        String time = cursor.getString(timeId);
        String card = cursor.getString(cardId);
        String amount = cursor.getString(amountId);
        String status = cursor.getString(statusId);

        if (cursor.getPosition() % 2 == 0) {
            holder.row.setBackgroundColor(context.getResources().getColor(R.color.colorGrey));
        } else {
            holder.row.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        }

        holder.time.setText(time);
        holder.id.setText(id);
        holder.cardNumber.setText(card);
        holder.amount.setText(parseAmount(amount));

        holder.status.setImageDrawable(context.getDrawable(status.equals(APPROVED.getResult()) ?
                R.drawable.trx_status_ok : R.drawable.trx_status_nok));

    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    private String parseAmount(String strAmount) {
        return String.valueOf(Double.parseDouble(strAmount) / 100.00);
    }

}
