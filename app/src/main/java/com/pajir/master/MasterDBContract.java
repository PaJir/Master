package com.pajir.master;

import android.provider.BaseColumns;

public final class MasterDBContract {
    public static final String RECORD_ID = "record_id";

    public static abstract class RecordEntry implements BaseColumns{
        public static final String TABLE_NAME = "record";
        public static final String TIME_FROM = "time";
        public static final String TIME_LENGTH = "length";
    }
}
