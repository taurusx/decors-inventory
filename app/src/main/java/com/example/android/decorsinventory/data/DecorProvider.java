package com.example.android.decorsinventory.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.decorsinventory.data.DecorContract.DecorEntry;


/**
 * {@link ContentProvider} for Decors Inventory app.
 */
public class DecorProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = DecorProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the decors table
     */
    private static final int DECORS = 100;

    /**
     * URI matcher code for the content URI for a single decor in the decors table
     */
    private static final int DECOR_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.decorsinventory/decors" will map to the
        // integer code {@link #DECORS}. This URI is used to provide access to MULTIPLE rows
        // of the decors table.
        sUriMatcher.addURI(DecorContract.CONTENT_AUTHORITY, DecorContract.PATH_DECORS, DECORS);

        // The content URI of the form "content://com.example.android.decorsinventory/decors/#" will map to the
        // integer code {@link #DECOR_ID}. This URI is used to provide access to ONE single row
        // of the decors table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.decorsinventory/decors/3" matches, but
        // "content://com.example.android.decorsinventory/decors" (without a number at the end) doesn't match.
        sUriMatcher.addURI(DecorContract.CONTENT_AUTHORITY, DecorContract.PATH_DECORS + "/#", DECOR_ID);
    }

    /**
     * Database helper object
     */
    private DecorDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Create and initialize a DecorDbHelper object to gain access to the decors database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context.
        mDbHelper = new DecorDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case DECORS:
                // For the DECORS code, query the decors table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the decors table.

                // Perform this raw SQL query "SELECT * FROM decors"
                // to get a Cursor that contains all rows from the decors table.
                cursor = database.query(
                        DecorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case DECOR_ID:
                // For the DECOR_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.decorsinventory/decors/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = DecorEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the decors table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(DecorEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor.
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DECORS:
                return insertDecor(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a decor into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertDecor(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(DecorEntry.COLUMN_DECOR_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Decor requires a name");
        }

        // No need to check the description, any value is valid (including null).

        // Check that the material is not null
        Integer material = values.getAsInteger(DecorEntry.COLUMN_DECOR_MATERIAL);
        if (material == null || !DecorEntry.isValidMaterial(material)) {
            throw new IllegalArgumentException("Decor requires valid material");
        }

        // If the height is provided, check that it's greater than or equal to 0 cm
        Integer height = values.getAsInteger(DecorEntry.COLUMN_DECOR_HEIGHT);
        if (height != null && height < 0) {
            throw new IllegalArgumentException("Height cannot be negative number");
        }

        // Check that the price is not null and not negative
        Double price = values.getAsDouble(DecorEntry.COLUMN_DECOR_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price must be positive value or 0");
        }

        // Check that the quantity is not null and not negative
        Integer quantity = values.getAsInteger(DecorEntry.COLUMN_DECOR_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be positive number or 0");
        }

        // No need to check the supplier's name, email address or decor's image,
        // any value is valid (including null).

        // Insert a new decor into the decors database table with the given ContentValues
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert a new row for decor in the database, returning the ID of that new row.
        // The first argument for db.insert() is the decors table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for new decor
        long id = db.insert(DecorEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (id == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }

        // Notify all listeners that the data has changed for the decor content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DECORS:
                return updateDecor(uri, contentValues, selection, selectionArgs);
            case DECOR_ID:
                // For the DECOR_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = DecorEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDecor(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update decors in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more decors).
     * Return the number of rows that were successfully updated.
     */
    private int updateDecor(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link DecorEntry#COLUMN_DECOR_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(DecorEntry.COLUMN_DECOR_NAME)) {
            String name = values.getAsString(DecorEntry.COLUMN_DECOR_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Decor requires a name");
            }
        }

        // No need to check the description, any value is valid (including null).

        // If the {@link DecorEntry#COLUMN_DECOR_MATERIAL} key is present,
        // check that the material value is valid.
        if (values.containsKey(DecorEntry.COLUMN_DECOR_MATERIAL)) {
            Integer material = values.getAsInteger(DecorEntry.COLUMN_DECOR_MATERIAL);
            if (material == null || !DecorEntry.isValidMaterial(material)) {
                throw new IllegalArgumentException("Decor requires valid material");
            }
        }

        // If the {@link DecorEntry#COLUMN_DECOR_HEIGHT} key is present,
        // check that the height value is valid.
        if (values.containsKey(DecorEntry.COLUMN_DECOR_HEIGHT)) {
            // Check that the height is greater than or equal to 0 cm
            Integer height = values.getAsInteger(DecorEntry.COLUMN_DECOR_HEIGHT);
            if (height != null && height < 0) {
                throw new IllegalArgumentException("Height cannot be negative number");
            }
        }

        // If the {@link DecorEntry#COLUMN_DECOR_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(DecorEntry.COLUMN_DECOR_PRICE)) {
            Double price = values.getAsDouble(DecorEntry.COLUMN_DECOR_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Price must be positive value or 0");
            }
        }

        // If the {@link DecorEntry#COLUMN_DECOR_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(DecorEntry.COLUMN_DECOR_QUANTITY)) {
            Integer quantity = values.getAsInteger(DecorEntry.COLUMN_DECOR_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Quantity must be positive number or 0");
            }
        }


        // No need to check the supplier's name, email address or decor's image,
        // any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, update a decor in the decors database table with the given ContentValues
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(DecorEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DECORS:
                // Delete all rows that match the selection and selection args
                // Returns the number of database rows affected by the delete statement
                // Delete all rows that match the selection and selection args
                // For case DECORS:
                rowsDeleted = db.delete(DecorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DECOR_ID:
                // For the DECOR_ID code, extract out the ID from the URI,
                // so we know which row to delete. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = DecorEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Returns the number of database rows affected by the delete statement
                // For case DECOR_ID:
                // Delete a single row given by the ID in the URI
                rowsDeleted = db.delete(DecorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DECORS:
                return DecorEntry.CONTENT_LIST_TYPE;
            case DECOR_ID:
                return DecorEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}