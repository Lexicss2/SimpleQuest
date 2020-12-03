package com.lex.simplequest.device.content.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public interface QuestContract {
    String AUTHORITY = "com.lex.simplequest.provider.quest";
    Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    long INCORRECT_ROW_ID = -1;

    interface BaseEntityColumns extends BaseColumns {
        String COLUMN_ID = BaseColumns._ID;
    }

    interface Tracks extends BaseEntityColumns {
        String TABLE_NAME = "tracks";
        Uri CONTENT_URI = Uri.withAppendedPath(QuestContract.CONTENT_URI, TABLE_NAME);

        String COLUMN_NAME = "name";
        String COLUMN_START_TIME = "start_time";
        String COLUMN_END_TIME = "end_time";

        String[] PROJECTION = {COLUMN_ID, COLUMN_NAME, COLUMN_START_TIME, COLUMN_END_TIME};
    }

    interface Points extends BaseEntityColumns {
        String TABLE_NAME = "points";
        Uri CONTENT_URI = Uri.withAppendedPath(QuestContract.CONTENT_URI, TABLE_NAME);

        String COLUMN_TRACK_ID = "track_id";
        String COLUMN_LATITUDE = "latitude";
        String COLUMN_LONGITUDE = "longitude";
        String COLUMN_ALTITUDE = "altitude";
        String COLUMN_TIMESTAMP = "timestamp";

        String[] PROJECTION = {COLUMN_ID, COLUMN_TRACK_ID, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ALTITUDE, COLUMN_TIMESTAMP};
    }

    interface CheckPoints extends  BaseEntityColumns {
        String TABLE_NAME = "check_points";
        Uri CONTENT_URI = Uri.withAppendedPath(QuestContract.CONTENT_URI, TABLE_NAME);

        String COLUMN_TRACK_ID = "track_id";
        String COLUMN_TYPE = "type";
        String COLUMN_TIMESTAMP = "timestamp";
        String COLUMN_TAG = "tag";

        String[] PROJECTION = {COLUMN_ID, COLUMN_TRACK_ID, COLUMN_TYPE, COLUMN_TIMESTAMP, COLUMN_TAG};
    }
}
