package gg.destiny.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.UserDictionary;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Locale;

/**
 * Created by Hayk on 1/24/2015.
 */
public class EmoteDownloader extends AsyncTask<String, Void, String[]> {
    final static String DESTINY_EMOTES_ENDPOINT = "http://www.destiny.gg/chat/emotes.json";
    final static String CONTENTPROVIDER_APP_ID = "^gg.destiny.app";
    static String[] EMOTICON_LIST = {
            "Dravewin",
            "INFESTINY",
            "FIDGETLOL",
            "Hhhehhehe",
            "GameOfThrows",
            "WORTH",
            "FeedNathan",
            "Abathur",
            "LUL",
            "Heimerdonger",
            "ASLAN",
            "DJAslan",
            "SoSad",
            "DURRSTINY",
            "SURPRISE",
            "NoTears",
            "OverRustle",
            "DuckerZ",
            "Kappa",
            "Klappa",
            "DappaKappa",
            "BibleThump",
            "AngelThump",
            "FrankerZ",
            "BasedGod",
            "TooSpicy",
            "OhKrappa",
            "SoDoge",
            "WhoahDude",
            "MotherFuckinGame",
            "DaFeels",
            "UWOTM8",
            "CallCatz",
            "CallChad",
            "DatGeoff",
            "Disgustiny",
            "FerretLOL",
            "Sippy",
            "DestiSenpaii",
            "KINGSLY",
            "Nappa",
            "DAFUK",
            "AYYYLMAO",
            "DANKMEMES"
    };
    Context mContext;
    String note = null;

    @Override
    protected String[] doInBackground(String... notes) {
        String[] emotes = EMOTICON_LIST;
        //if(false){

        if (notes.length > 0) {
            note = notes[0];

            return emotes;
        }
        // TODO get emotes from proper endpoint
        //String[] emotes = {};
        if (!DESTINY_EMOTES_ENDPOINT.equals("")) {
            String rawJsonEmotes = Business.HttpGet(DESTINY_EMOTES_ENDPOINT);
            JSONArray jsonEmotes = null;
            try {
                jsonEmotes = new JSONArray(rawJsonEmotes);
                if (jsonEmotes != null && jsonEmotes.length() > 0) {
                    emotes = new String[jsonEmotes.length()];
                    for (int i = 0; i < emotes.length; i++) {
                        String emote = jsonEmotes.getString(i);
                        emotes[i] = emote;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return EMOTICON_LIST;
        }
        return emotes;
    }

    @Override
    protected void onPostExecute(String[] foundEmotes) {
        if (foundEmotes != null && foundEmotes.length > 0) {
            if (note != null) {
                if (note.equals("delete")) {
                    DeleteAllDict(mContext);
                } else if (note.equals("deletebyappid")) {
                    DeleteAppDict(mContext);
                }
            } else {
                AddEmotesToUserDict(mContext, foundEmotes);
                String s = "Done adding emotes total#:" + String.valueOf(foundEmotes.length);
                Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();
                Log.d("EmoteDownload", s);
            }
        } else {
            Log.e("EmoteDownload", "Problem finding or GETting emotes list.");
        }
    }



    public static void AddEmotesToUserDict(Context context, String[] emotes) {
        Locale locale = context.getResources().getConfiguration().locale;
        String APP_ID = CONTENTPROVIDER_APP_ID;
        int iAPP_ID = android.os.Process.myUid();
        //APP_ID = String.valueOf(iAPP_ID);
        //APP_ID = FIXED_APP_ID;
        Log.d("app id", APP_ID);
        ContentResolver userDictResolver = context.getContentResolver();

//		2) Delete all old emotes
        DeleteAppDict(context);


        // Defines a new Uri object that receives the result of the insertion
        Uri mNewUri = null;

        int frequency = 255;
        String shortcut = null;

        for (int i = 0; i < emotes.length; i++) {
            String word = emotes[i];
            //if(currentWords.indexOf(word) != -1){
            //}else{
            //	UserDictionary.Words.addWord(context, word, frequency, shortcut, locale);
            //}

            // Defines an object to contain the new values to insert
            ContentValues mNewValues = new ContentValues();

			/*
             * Sets the values of each column and inserts the word. The arguments to the "put"
			 * method are "column name" and "value"
			 */
            mNewValues.put(UserDictionary.Words.APP_ID, iAPP_ID);
            mNewValues.put(UserDictionary.Words.LOCALE, locale.toString());
            mNewValues.put(UserDictionary.Words.WORD, word);
            mNewValues.put(UserDictionary.Words.SHORTCUT, APP_ID);
            mNewValues.put(UserDictionary.Words.FREQUENCY, frequency);
            //mNewValues.put(UserDictionary.Words.FREQUENCY, String.valueOf(frequency));

            mNewUri = userDictResolver.insert(
                    UserDictionary.Words.CONTENT_URI,   // the user dictionary content URI
                    mNewValues                          // the values to insert
            );
        }
    }


    // NOTE HACK using the shortcut field
    // to identify words added by this app
    // UserDictionary.Words.addWord(Context context, String word, int frequency, String shortcut, Locale locale)
    // UserDictionary.Words.addWord( this , "newMedicalWord", 1, UserDictionary.Words.LOCALE_TYPE_CURRENT);
    // TODO support api level 15 (4.0)
    // TODO maybe move this to a class?

    public static void oldAddEmotesToUserDict(Context context, String[] emotes) {
        Locale locale = context.getResources().getConfiguration().locale;
        for (int i = 0; i < emotes.length; i++) {
            String word = emotes[i];
            //if(currentWords.indexOf(word) != -1){
            //}else{
            UserDictionary.Words.addWord(context, word, 255, null, locale);
            //}
        }

    }

    public static void DeleteAppDict(Context context) {
        int iAPP_ID = android.os.Process.myUid();
        String appid = String.valueOf(iAPP_ID);
        String APP_ID = CONTENTPROVIDER_APP_ID;
        //appid = FIXED_APP_ID;
        //DeleteAllDict(context, appid);
        //DeleteAllDict(context, FIXED_APP_ID);

        ContentResolver userDictResolver = context.getContentResolver();
        //		2) Delete all old emotes
//		// Defines selection criteria for the rows you want to delete

        String mSelectionClause = UserDictionary.Words.SHORTCUT + " LIKE ?";
        String[] genericAppId = {APP_ID}; // this will delete everything

        // Defines a variable to contain the number of rows deleted
        int mRowsDeleted = 0;

        // Deletes the words that match the selection criteria
        mRowsDeleted = userDictResolver.delete(
                UserDictionary.Words.CONTENT_URI,   // the user dictionary content URI
                mSelectionClause,                   // the column to select on
                genericAppId                      // the value to compare to
        );
        String s = "Deleted old emotes. Total=" + String.valueOf(mRowsDeleted);
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
        Log.d("Deleted emotes", s);


    }

    public static void DeleteAllDict(Context context) {
        DeleteAllDict(context, "0");
    }

    public static void DeleteAllDict(Context context, String appid) {
        ContentResolver userDictResolver = context.getContentResolver();
        //		2) Delete all old emotes
//		// Defines selection criteria for the rows you want to delete

        String mSelectionClause = UserDictionary.Words.APP_ID + " LIKE ?";
        String[] genericAppId = {appid}; // this will delete everything

        // Defines a variable to contain the number of rows deleted
        int mRowsDeleted = 0;

        // Deletes the words that match the selection criteria
        mRowsDeleted = userDictResolver.delete(
                UserDictionary.Words.CONTENT_URI,   // the user dictionary content URI
                mSelectionClause,                   // the column to select on
                genericAppId                      // the value to compare to
        );
        String s = "Deleted old emotes. Total=" + String.valueOf(mRowsDeleted);
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
        Log.d("Deleted emotes", s);

    }

    public static boolean hasAutocomplete(Context context) {
        int iAPP_ID = android.os.Process.myUid();
        String APP_ID = String.valueOf(iAPP_ID);
        APP_ID = CONTENTPROVIDER_APP_ID;

        ContentResolver userDictResolver = context.getContentResolver();

        // A "projection" defines the columns that will be returned for each row
        // Contract class constant for the _ID column name
        String[] mProjection = {UserDictionary.Words._ID};
// Initializes an array to contain selection arguments
        String mmSelectionClause = UserDictionary.Words.SHORTCUT + " LIKE ?";
        String[] mmSelectionArgs = {APP_ID};

        // Does a query against the table and returns a Cursor object
        Cursor mCursor = null;
        String mSortOrder = UserDictionary.Words.DEFAULT_SORT_ORDER;
        mCursor = userDictResolver.query(
                UserDictionary.Words.CONTENT_URI,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                mmSelectionClause,                   // Either null, or the word the user entered
                mmSelectionArgs,                    // Either empty, or the string the user entered
                mSortOrder);                       // The sort order for the returned rows

//		http://stackoverflow.com/questions/468211/how-do-i-get-the-count-in-my-content-provider
//		may be more efficient
        return (mCursor != null && mCursor.getCount() > 0);
    }


}
