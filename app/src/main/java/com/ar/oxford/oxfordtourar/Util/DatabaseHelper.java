package com.ar.oxford.oxfordtourar.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ar.oxford.oxfordtourar.model.Place;
import com.ar.oxford.oxfordtourar.model.PlaceTrip;
import com.ar.oxford.oxfordtourar.model.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kelvin Khoo on 25/07/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // database version
    private static final int DATABASE_VERSION = 2;
    // database name
    private static final String DATABASE_NAME = "TripManager";

    // Table Names
    private static final String TABLE_TRIP = "trips";
    private static final String TABLE_PLACE = "places";
    private static final String TABLE_TRIP_PLACE = "trip_place";

    // Common column names
    private static final String TRIP_TABLE_ID = "trip_table_id";
    private static final String PLACE_TABLE_ID = "place_table_id";
    private static final String TRIP_PLACE_TABLE_ID = "trip_place_table_id";
    private static final String CREATED_AT = "created_at";

    // Trips Table column names
    private static final String TRIP_NAME = "trip_name";

    // Places Table column names
    public static final String PLACE_ID = "place_id";
    public static final String PLACE_TYPE = "type";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "long";
    public static final String PLACE_NAME = "place_name";
    public static final String PHOTO_REFERENCE = "photo_reference";
    public static final String PRICE_LEVEL = "price_level";
    public static final String RATING = "rating";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String ADDRESS = "address";
    public static final String WEBSITE = "website";

    // TRIP PLACE TABLE
    private static final String KEY_TRIP_ID = "trip_id";
    private static final String KEY_PLACE_ID = "place_id";
    private static final String DURATION = "duration";
    private static final String POSITION = "position";

    // Table create statement
    // TRIP Table create statement
    private static final String CREATE_TABLE_TRIP = "CREATE TABLE "
            + TABLE_TRIP + "(" + TRIP_TABLE_ID + " INTEGER PRIMARY KEY,"
            + TRIP_NAME + " TEXT,"
            + CREATED_AT + " DATETIME"
            + ")";

    // PLACE Table create statement
    private static final String CREATE_PLACE_TABLE = "CREATE TABLE "
            + TABLE_PLACE + "("
            + PLACE_TABLE_ID + " INTEGER PRIMARY KEY,"
            + PLACE_ID + " TEXT,"
            + PLACE_TYPE + " TEXT,"
            + LATITUDE + " TEXT,"
            + LONGITUDE + " TEXT,"
            + PLACE_NAME + " TEXT,"
            + PHOTO_REFERENCE + " TEXT,"
            + PRICE_LEVEL + " TEXT,"
            + RATING + " REAL,"
            + PHONE_NUMBER + " TEXT,"
            + ADDRESS + " TEXT,"
            + WEBSITE + " TEXT,"
            + CREATED_AT + " DATETIME"
            + ")";

    // TRIP_PLACE TABLE CREATE STATEMENT
    private static final String CREATE_TRIP_PLACE_TABLE = "CREATE TABLE "
            + TABLE_TRIP_PLACE + "(" + TRIP_PLACE_TABLE_ID + " INTEGER PRIMARY KEY,"
            + KEY_TRIP_ID + " TEXT,"
            + KEY_PLACE_ID + " TEXT,"
            + POSITION + " INTEGER DEFAULT -1,"
            + DURATION + " INTEGER DEFAULT 0,"
            + CREATED_AT + " DATETIME"
            + ")";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL(CREATE_PLACE_TABLE);
        db.execSQL(CREATE_TABLE_TRIP);
        db.execSQL(CREATE_TRIP_PLACE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on updrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_PLACE);
        // create new table;
        onCreate(db);
    }



    /**
     * get a single place by sqlID
     * @param id
     * @return
     */
    public Place getPlaceBySqlID(long id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_PLACE + " WHERE "
                + PLACE_TABLE_ID + " = " + id;
        Log.e(LOG, selectQuery);
        Cursor result = db.rawQuery(selectQuery,null);
        if(result !=null)
        {
            result.moveToFirst();
        }
        Place place = new Place();
        place.setId(result.getInt(result.getColumnIndex(PLACE_TABLE_ID)));
        place.setPlace_id(result.getString(result.getColumnIndex(PLACE_ID)));
        place.setType(result.getString(result.getColumnIndex(PLACE_TYPE)));
        place.setLat(result.getString(result.getColumnIndex(LATITUDE)));
        place.setLng(result.getString(result.getColumnIndex(LONGITUDE)));
        place.setName(result.getString(result.getColumnIndex(PLACE_NAME)));
        place.setPhoto_reference(result.getString(result.getColumnIndex(PHOTO_REFERENCE)));
        place.setPrice_level(result.getString(result.getColumnIndex(PRICE_LEVEL)));
        place.setRating(result.getFloat(result.getColumnIndex(RATING)));
        place.setPhone_number(result.getString(result.getColumnIndex(PHONE_NUMBER)));
        place.setAddress(result.getString(result.getColumnIndex(ADDRESS)));
        place.setWebsite(result.getString(result.getColumnIndex(WEBSITE)));
        return  place;
    }

    // get all places
    public List<Place> getAllPlaces() {
        List<Place> placeList = new ArrayList<Place>();
        String selectQuery = "SELECT * FROM " + TABLE_PLACE;
        Log.e(LOG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery(selectQuery, null);

        // loop through all rows and add to placelist
        if(result.moveToFirst())
        {
            do{
                Place place = new Place();


                placeList.add(place);
            }while(result.moveToNext());
        }
        return placeList;
    }

    /**
     * Gives a list of place that belongs to a trip
     * @param trip_id
     * @return
     */
    public List<Place> getAllPlacesByTripWithoutPlaceTrip(int trip_id)
    {
        List<Place> placeList = new ArrayList<Place>();


        String selectQuery = "SELECT * FROM " + TABLE_PLACE + " AS P WHERE "
                + "P." + PLACE_TABLE_ID + " IN (SELECT " + KEY_PLACE_ID + " FROM " + TABLE_TRIP_PLACE
                + " WHERE " + KEY_TRIP_ID + " = '" + trip_id + "')";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery(selectQuery, null);
        // loop through all rows and add to list
        if(result.moveToFirst()) {
            do{
                Place place = new Place();
                place.setId(result.getInt(result.getColumnIndex(PLACE_TABLE_ID)));
                place.setPlace_id(result.getString(result.getColumnIndex(PLACE_ID)));
                place.setType(result.getString(result.getColumnIndex(PLACE_TYPE)));
                place.setLat(result.getString(result.getColumnIndex(LATITUDE)));
                place.setLng(result.getString(result.getColumnIndex(LONGITUDE)));
                place.setName(result.getString(result.getColumnIndex(PLACE_NAME)));
                place.setPhoto_reference(result.getString(result.getColumnIndex(PHOTO_REFERENCE)));
                place.setPrice_level(result.getString(result.getColumnIndex(PRICE_LEVEL)));
                place.setRating(result.getFloat(result.getColumnIndex(RATING)));
                place.setPhone_number(result.getString(result.getColumnIndex(PHONE_NUMBER)));
                place.setAddress(result.getString(result.getColumnIndex(ADDRESS)));
                place.setWebsite(result.getString(result.getColumnIndex(WEBSITE)));
                placeList.add(place);
            }while (result.moveToNext());
        }
        return placeList;
    }

    /**
     * Gives a list of place that belongs to a trip
     * @param trip_id
     * @return
     */
    public List<PlaceTrip> getAllPlacesByTrip(int trip_id)
    {
        List<PlaceTrip> placeList = new ArrayList<PlaceTrip>();


        String selectQuery = "SELECT * FROM " + TABLE_PLACE +","+TABLE_TRIP_PLACE + " WHERE "
                + PLACE_TABLE_ID + " IN (SELECT " + KEY_PLACE_ID + " FROM " + TABLE_TRIP_PLACE
                + " WHERE " + KEY_TRIP_ID + " = '" + trip_id + "')"+" AND " + PLACE_TABLE_ID + "=" + TABLE_TRIP_PLACE+"."+KEY_PLACE_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery(selectQuery, null);
        // loop through all rows and add to list
        if(result.moveToFirst()) {
            do{
                Place place = new Place();
                place.setId(result.getInt(result.getColumnIndex(PLACE_TABLE_ID)));
                place.setPlace_id(result.getString(result.getColumnIndex(PLACE_ID)));
                place.setType(result.getString(result.getColumnIndex(PLACE_TYPE)));
                place.setLat(result.getString(result.getColumnIndex(LATITUDE)));
                place.setLng(result.getString(result.getColumnIndex(LONGITUDE)));
                place.setName(result.getString(result.getColumnIndex(PLACE_NAME)));
                place.setPhoto_reference(result.getString(result.getColumnIndex(PHOTO_REFERENCE)));
                place.setPrice_level(result.getString(result.getColumnIndex(PRICE_LEVEL)));
                place.setRating(result.getFloat(result.getColumnIndex(RATING)));
                place.setPhone_number(result.getString(result.getColumnIndex(PHONE_NUMBER)));
                place.setAddress(result.getString(result.getColumnIndex(ADDRESS)));
                place.setWebsite(result.getString(result.getColumnIndex(WEBSITE)));
                int position = result.getInt(result.getColumnIndex(POSITION));
                int duration = result.getInt(result.getColumnIndex(DURATION));
                int placeTripID = result.getInt(result.getColumnIndex(TRIP_PLACE_TABLE_ID));
                PlaceTrip newPlaceTrip = new PlaceTrip(place,position,duration);
                newPlaceTrip.setPlaceTripid(placeTripID);
                placeList.add(newPlaceTrip);
            }while (result.moveToNext());
        }
        return placeList;
    }

    public void deletePlaces(long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLACE, PLACE_TABLE_ID + " = ?",new String[] { String.valueOf(id) });
    }

    // create a trip in the database
    public long createTrip(Trip trip)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_NAME,trip.getTripName());
        values.put(CREATED_AT,getDateTime());
        long id = db.insert(TABLE_TRIP,null,values);
        return id;
    }

    /**
     * add a place to a trip
     * @param trip
     * @param place
     */
    public void addPlaceToTrip(Trip trip, Place place)
    {
        long place_keyID = createPlace(place);
        createTripPlace(trip.getId(),place_keyID);
    }

    /**
     * Create a place in SQL Database
     * @param place
     * @return
     */
    public long createPlace(Place place)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLACE_ID,place.getPlace_id());
        contentValues.put(PLACE_TYPE,place.getType());
        contentValues.put(LATITUDE,place.getLat());
        contentValues.put(LONGITUDE,place.getLng());
        contentValues.put(PLACE_NAME,place.getName());
        contentValues.put(PHOTO_REFERENCE,place.getPhoto_reference());
        contentValues.put(PRICE_LEVEL,place.getPrice_level());
        contentValues.put(RATING,place.getRating());
        contentValues.put(PHONE_NUMBER,place.getPhone_number());
        contentValues.put(ADDRESS,place.getAddress());
        contentValues.put(WEBSITE,place.getWebsite());
        long id = db.insert(TABLE_PLACE,null,contentValues);
        return id;
    }

    /**
     * create a place in a trip
     * @param trip_id
     * @param place_keyid
     * @return
     */
    public long createTripPlace(long trip_id, long place_keyid)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TRIP_ID, trip_id);
        contentValues.put(KEY_PLACE_ID,place_keyid);
        long id = db.insert(TABLE_TRIP_PLACE,null,contentValues);
        return id;
    }


    // return all the trip in the database
    public List<Trip> getAllTrip(){
        List<Trip> tripList = new ArrayList<Trip>();
        String selectQuery = "SELECT *  FROM " + TABLE_TRIP;
        Log.e(LOG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery(selectQuery,null);
        if(result.moveToFirst()) {
            do {
                Trip trip = new Trip();
                trip.setId(result.getInt(result.getColumnIndex(TRIP_TABLE_ID)));
                trip.setTripName(result.getString(result.getColumnIndex(TRIP_NAME)));
                trip.setCreated_at(result.getString(result.getColumnIndex(CREATED_AT)));
                tripList.add(trip);
            }while(result.moveToNext());
        }
        return tripList;
    }

    public int updateTripPlaceWithPosition(int position, long tripPlaceTableID)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(POSITION,position);
        return db.update(TABLE_TRIP_PLACE,values,TRIP_PLACE_TABLE_ID +"=" +tripPlaceTableID,null);
    }


    public int updateTripPlaceDuration(int duration, long tripPlaceTableID )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DURATION,duration);
        return db.update(TABLE_TRIP_PLACE,values,TRIP_PLACE_TABLE_ID +"=" +tripPlaceTableID,null);
    }

    // update name of a trip
    public int updateTrip(Trip trip)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_NAME,trip.getTripName());
        // update db
        return db.update(TABLE_TRIP, values, TRIP_TABLE_ID + " = ?", new String[] {String.valueOf(trip.getId())});
    }

    // close database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if(db !=null && db.isOpen())
            db.close();
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}
