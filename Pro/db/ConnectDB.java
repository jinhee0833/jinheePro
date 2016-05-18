package com.helloants.helloants.db;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * Created by park on 2016-01-12.
 */
public enum ConnectDB {
    INSTANCE;

    public MongoClient mMC;
    public DB mDB;
    public DBCollection mColc;

    private ConnectDB() {
        connect();
    }

    public void connect() {
        //disconnect();

        try {
            if( mMC == null) {
                mMC = new MongoClient("52.69.20.130", 27017);
                mDB = mMC.getDB("helloants");

                mMC.setWriteConcern(WriteConcern.JOURNALED);
            }
        } catch (NoClassDefFoundError e) {}
    }

    private void disconnect() {
        try {
            if (mMC != null) {
                mMC.close();
                mMC = null;
                mDB = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public DB getDB() {
        return mDB;
    }
}