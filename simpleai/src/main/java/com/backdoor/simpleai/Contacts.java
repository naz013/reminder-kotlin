package com.backdoor.simpleai;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Helper class for accessing to contacts.
 */
public class Contacts {

    /**
     * Find contact e-mail by contact name.
     * @param input contact name.
     * @param context application context.
     * @return Phone number
     */
    public static String findMail(String input, Context context) {
        String number = null;
        String[] parts = input.split("\\s");
        for (String part : parts) {
            while (part.length() > 1) {
                String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'";
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Email.DATA};
                Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projection, selection, null, null);
                if (c != null && c.moveToFirst()) {
                    number = c.getString(0);
                    c.close();
                }
                if (number != null)
                    break;
                part = part.substring(0, part.length() - 2);
            }
        }
        return number;
    }

    /**
     * Find contact number by contact name.
     * @param input contact name.
     * @param context application context.
     * @return Phone number
     */
    public static String findNumber(String input, Context context) {
        String number = null;
        String[] parts = input.split("\\s");
        for (String part : parts) {
            while (part.length() > 1) {
                String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'";
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projection, selection, null, null);
                if (c != null && c.moveToFirst()) {
                    number = c.getString(0);
                    c.close();
                }
                if (number != null)
                    break;
                part = part.substring(0, part.length() - 2);
            }
        }
        return number;
    }
}
