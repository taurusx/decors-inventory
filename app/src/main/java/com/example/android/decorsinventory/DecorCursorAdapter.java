package com.example.android.decorsinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.decorsinventory.data.DecorContract.DecorEntry;

/**
 * {@link DecorCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of decor data as its data source. This adapter knows
 * how to create list items for each row of decor data in the {@link Cursor}.
 */
public class DecorCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link DecorCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public DecorCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, final ViewGroup parent) {
        // Inflate a list item view using the layout specified in item_decor.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_decor, parent, false);

        // Store Views from item_decor layout in a holder and set this holder
        // as a Tag of inflated view to use Views in the future.
        DecorViewHolder holder = new DecorViewHolder();
        holder.nameTextView = (TextView) view.findViewById(R.id.name);
        holder.priceTextView = (TextView) view.findViewById(R.id.price);
        holder.quantityTextView = (TextView) view.findViewById(R.id.quantity);
        holder.saleButton = (Button) view.findViewById(R.id.sale_button);

        view.setTag(holder);

        return view;

    }

    /**
     * This method binds the decor data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current decor can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final DecorViewHolder holder = (DecorViewHolder) view.getTag();

        // Find the columns of decor attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(DecorEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(DecorEntry.COLUMN_DECOR_NAME);
        int priceColumnIndex = cursor.getColumnIndex(DecorEntry.COLUMN_DECOR_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(DecorEntry.COLUMN_DECOR_QUANTITY);

        // Read the decor attributes from the Cursor for the current decor
        int decorId = cursor.getInt(idColumnIndex);
        String decorName = cursor.getString(nameColumnIndex);
        Double decorPrice = cursor.getDouble(priceColumnIndex);
        int decorQuantity = cursor.getInt(quantityColumnIndex);

        holder.decorId = decorId;
        // Update the price TextView with the given decor value. If the decor price equals 0,
        // then use default text "FREE".
        if (decorPrice == 0) {
            holder.priceTextView.setText(context.getString(R.string.catalog_price_free));
        } else {
            holder.priceTextView.setText(context.getString(R.string.catalog_price_currency_pln, decorPrice));
        }

        // Update the TextViews with the attributes for the current decor
        holder.nameTextView.setText(decorName);
        holder.quantityTextView.setText(context.getString(R.string.catalog_quantity_label, decorQuantity));

        // Set listener on SALE button to reduce quantity by 1.
        final Context c = context.getApplicationContext();
        final int oldQuantity = decorQuantity;
        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (oldQuantity > 0) {
                    // Reduce quantity by 1:
                    int newQuantity = oldQuantity - 1;
                    // Create a ContentValues object where quantity column name is the key,
                    // and new decor quantity is the value.
                    ContentValues values = new ContentValues();
                    values.put(DecorEntry.COLUMN_DECOR_QUANTITY, newQuantity);
                    Uri currentDecorUri = ContentUris.withAppendedId(DecorEntry.CONTENT_URI, holder.decorId);

                    int mRowsUpdated = c.getContentResolver().update(currentDecorUri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (mRowsUpdated == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(c.getApplicationContext(), c.getString(R.string.catalog_sale_update_quantity_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(c.getApplicationContext(), c.getString(R.string.catalog_sale_update_quantity_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // The update is impossible, display a toast.
                    Toast.makeText(c.getApplicationContext(), c.getString(R.string.catalog_sale_quantity_invalid),
                            Toast.LENGTH_LONG).show();
                }

                Log.d("onClick", "_ID of decor in DECORS database: " + holder.decorId);
            }
        });

    }

    private static class DecorViewHolder {
        TextView nameTextView;
        TextView priceTextView;
        TextView quantityTextView;
        Button saleButton;
        int decorId;
    }
}