package com.example.android.umclog;

    /**
     * Used to pass values from inner listener classes to adapter
     * @param <T> own value to be passed
     */
    public class Value<T> {
        private T val;

        public Value() {}

        public Value(T v) {
            this.val = v;
        }

        public T getVal() {
            return val;
        }

        public void setVal(T val) {
            this.val = val;
        }
    }
