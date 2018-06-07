package com.example.blink22.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import database.CrimeBaseHelper;
import database.CrimeCursorWrapper;
import database.CrimeDbSchema;
import database.CrimeDbSchema.CrimeTable;

/**
 * Created by blink22 on 5/29/18.
 */
public class CrimeLab {

    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private CrimeLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public void addCrime(Crime c){
        ContentValues v = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME, null, v);
    }

    public static CrimeLab get (Context context){
        if(sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public Crime getCrime(UUID id){
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID +" = ?",
                new String[] {id.toString()}
        );

        try{
            if(cursor.getCount()==0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        }finally {
            cursor.close();
        }
    }

    public void deleteCrime(UUID id){
        mDatabase.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ?",
                new String[] {id.toString()});
    }

    public List<Crime> getCrimes(){
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return crimes;
    }

    public void updateCrime(Crime crime){

        ContentValues values = getContentValues(crime);
        String uuidString = crime.getId().toString();
        mDatabase.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID +"= ?",
                new String[] {uuidString});

    }
    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
        values.put(CrimeTable.Cols.SUSPECT_PHONE, crime.getPhone());

        return values;
    }

    private CrimeCursorWrapper queryCrimes (String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(CrimeTable.NAME, null,
                whereClause, whereArgs, null,
                null, null
        );
        return new CrimeCursorWrapper(cursor);
    }
}
