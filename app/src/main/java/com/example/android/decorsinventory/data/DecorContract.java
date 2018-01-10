package com.example.android.decorsinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Decors Inventory app.
 */

public final class DecorContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DecorContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.decorsinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.decorsinventory/decors/ is a valid path for
     * looking at decor data.
     */
    public static final String PATH_DECORS = "decors";

    /**
     * Inner class that defines constant values for the decors database table.
     * Each entry in the table represents a single decor.
     */
    public static final class DecorEntry implements BaseColumns {

        /**
         * The content URI to access the decor data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DECORS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of decors.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DECORS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single decor.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DECORS;

        /**
         * Name of database table for decors
         */
        public static final String TABLE_NAME = "decors";

        /**
         * Unique ID number for the decor (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the decor.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_DECOR_NAME = "name";

        /**
         * Description of the decor.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_DECOR_DESCRIPTION = "description";

        /**
         * Material of the decor.
         * <p>
         * The only possible values are {@link #MATERIAL_UNSPECIFIED}, {@link #MATERIAL_GLASS},
         * {@link #MATERIAL_WOOD}, {@link #MATERIAL_METAL} or {@link #MATERIAL_FABRIC}.
         * <p>
         * Type: INTEGER
         */
        public static final String COLUMN_DECOR_MATERIAL = "material";

        /**
         * Height of the decor.
         * <p>
         * Type: INTEGER
         */
        public static final String COLUMN_DECOR_HEIGHT = "height";

        /**
         * Price of the decor.
         * <p>
         * Type: REAL
         */
        public static final String COLUMN_DECOR_PRICE = "price";

        /**
         * Quantity of the decor.
         * <p>
         * Type: INTEGER
         */
        public static final String COLUMN_DECOR_QUANTITY = "quantity";

        /**
         * Name of the supplier of the decor.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_DECOR_SUPPLIER_NAME = "supplier_name";

        /**
         * Email address of the supplier of the decor.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_DECOR_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Image of the decor stored as a BLOB type.
         * <p>
         * Type: BLOB
         */
        public static final String COLUMN_DECOR_IMAGE = "image";

        /**
         * Possible values for the material of the decor.
         */
        public static final int MATERIAL_UNSPECIFIED = 0;
        public static final int MATERIAL_GLASS = 1;
        public static final int MATERIAL_WOOD = 2;
        public static final int MATERIAL_METAL = 3;
        public static final int MATERIAL_FABRIC = 4;

        /**
         * Returns whether or not the given material is {@link #MATERIAL_UNSPECIFIED}, {@link #MATERIAL_GLASS},
         * {@link #MATERIAL_WOOD}, {@link #MATERIAL_METAL} or {@link #MATERIAL_FABRIC}.
         */
        public static boolean isValidMaterial(int material) {
            return (material == MATERIAL_UNSPECIFIED || material == MATERIAL_GLASS ||
                    material == MATERIAL_WOOD || material == MATERIAL_METAL || material == MATERIAL_FABRIC);
        }
    }
}
