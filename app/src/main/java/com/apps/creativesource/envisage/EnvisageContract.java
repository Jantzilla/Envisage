package com.apps.creativesource.envisage;

import android.provider.BaseColumns;

public class EnvisageContract {

    private EnvisageContract() {}

    public static class Events implements BaseColumns {
        public static final String TABLE_NAME = "events";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_START_TIME = "startTime";
        public static final String COLUMN_END_TIME = "endTime";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SET = "isSet";
        public static final String COLUMN_USED_COUNT = "usedCount";
    }

    public static class History implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_EVENT = "event";
        public static final String COLUMN_DURATION = "duration";
    }
}
