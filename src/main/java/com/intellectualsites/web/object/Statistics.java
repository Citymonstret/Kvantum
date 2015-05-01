package com.intellectualsites.web.object;

/**
 * Created 2015-04-30 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Statistics {

    private final View parent;

    public Statistics(final View parent) {
        this.parent = parent;
    }

    public View getParent() {
        return this.parent;
    }

    public static class IntWrapper implements StatisticWrapper<Integer> {

        private int value;

        public IntWrapper() {
            this.value = 0;
        }

        @Override
        public void update(Integer integer) {
            this.value += integer;
        }

        @Override
        public Integer get() {
            return this.value;
        }

        @Override
        public void set(Integer integer) {
            this.value = integer;
        }

    }

    public static class VisitorStatistic extends Statistic<IntWrapper> {

        public VisitorStatistic() {
            super("visitors");
        }

    }

    public static class Statistic<T extends StatisticWrapper> {

        private final String key;
        private T value;


        public Statistic(final String key) {
            this(key, null);
        }

        public Statistic(final String key, final T initialValue) {
            this.key = key;
            this.value = initialValue;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(final T value) {
            this.value = value;
        }

    }

    public interface StatisticWrapper<T> {
        void update(final T t);
        T get();
        void set(final T t);
    }
}
