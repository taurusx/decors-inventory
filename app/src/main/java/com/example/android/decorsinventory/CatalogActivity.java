/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.decorsinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.decorsinventory.data.DecorContract.DecorEntry;

/**
 * Displays list of decors that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Setup an Adapter to create a list item for each row of decor data in the Cursor.
     */
    DecorCursorAdapter decorAdapter;

    /**
     * Id of a Loader used for displaying decor data.
     */
    private static final int DECOR_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find the ListView which will be populated with the decor data
        ListView decorListView = (ListView) findViewById(R.id.list_view_decor);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        decorListView.setEmptyView(emptyView);

        decorAdapter = new DecorCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        decorListView.setAdapter(decorAdapter);

        //Setup the item click listener
        decorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Form the content URI that represents the specific decor that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link DecorEntry#CONTENT_URI}
                Uri currentDecorUri = ContentUris.withAppendedId(DecorEntry.CONTENT_URI, id);
                // Create new Intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Set the URI on the data field of intent
                intent.setData(currentDecorUri);

                // Launch the {@link EditorActivity} to display the data for the current decor.
                startActivity(intent);
            }
        });

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(DECOR_LOADER_ID, null, this);
    }


    /**
     * Helper method to insert hardcoded decor data into the database. For debugging purposes only.
     */
    private void insertDecor() {

        // Create a ContentValues object where column names are the keys,
        // and Decor's decor attributes are the values.
        ContentValues values = new ContentValues();
        values.put(DecorEntry.COLUMN_DECOR_NAME, "Glass Footed Cylinder Vase");
        values.put(DecorEntry.COLUMN_DECOR_DESCRIPTION, "Diameter 9cm, perfect for small bouquets or as candle holder");
        values.put(DecorEntry.COLUMN_DECOR_MATERIAL, DecorEntry.MATERIAL_GLASS);
        values.put(DecorEntry.COLUMN_DECOR_HEIGHT, 30);
        values.put(DecorEntry.COLUMN_DECOR_PRICE, 8.5);
        values.put(DecorEntry.COLUMN_DECOR_QUANTITY, 12);

        // Insert a new row for Decor into the provider using the ContentResolver.
        // Use the {@link DecorEntry#CONTENT_URI} to indicate that we want to insert
        // into the decors database table.
        // Receive the new content URI that will allow us to access decor's data in the future.
        Uri newUri = getContentResolver().insert(DecorEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all decors in the database.
     */
    private void deleteAllDecors() {
        int rowsDeleted = getContentResolver().delete(DecorEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from decor database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDecor();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllDecors();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // code only has one Loader, so we don't care about the ID.

        String[] projection = {
                DecorEntry._ID,
                DecorEntry.COLUMN_DECOR_NAME,
                DecorEntry.COLUMN_DECOR_PRICE,
                DecorEntry.COLUMN_DECOR_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                DecorEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        decorAdapter.swapCursor(data);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        decorAdapter.swapCursor(null);
    }
}
