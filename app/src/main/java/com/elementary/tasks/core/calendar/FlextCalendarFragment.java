package com.elementary.tasks.core.calendar;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.EventsDataProvider;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.views.MonthView;
import com.elementary.tasks.databinding.FragmentFlextCalBinding;
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;
import timber.log.Timber;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FlextCalendarFragment extends BaseNavigationFragment implements EventsDataProvider.Callback {

    private static final String TAG = "FlextCalendarFragment";

    /**
     * Weekday conventions
     */
    public static int SUNDAY = 1;
    public static int MONDAY = 2;

    /**
     * Flags to display month
     */
    private static final int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;

    public final static int NUMBER_OF_PAGES = 4;

    /**
     * First day of month time
     */
    private GregorianCalendar firstMonthTime = new GregorianCalendar();

    /**
     * Reuse formatter to print "MMMM yyyy" format
     */
    private final StringBuilder monthYearStringBuilder = new StringBuilder(50);
    private Formatter monthYearFormatter = new Formatter(
            monthYearStringBuilder, Locale.getDefault());

    protected int month = -1;
    protected int year = -1;

    protected boolean enableImage = true;
    protected boolean isDark = true;

    /**
     * Initial params key
     */
    public final static String MONTH = "month";
    public final static String YEAR = "year";
    public final static String START_DAY_OF_WEEK = "startDayOfWeek";
    public final static String ENABLE_IMAGES = "enableImages";
    public final static String DARK_THEME = "dark_theme";
    public final static String MONTH_IMAGES = "month_images";

    /**
     * Declare views
     */
    private List<DateGridFragment> fragments;

    /**
     * caldroidData belongs to Caldroid
     */
    private Map<DateTime, Events> mLastMap = new HashMap<>();

    private long[] photosList = new long[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    protected int startDayOfWeek = SUNDAY;

    private MonthView.OnDateClick dateItemClickListener;
    private MonthView.OnDateLongClick dateItemLongClickListener;

    private FlextListener caldroidListener;
    private FragmentFlextCalBinding binding;

    public WeekdayArrayAdapter getNewWeekdayAdapter() {
        return new WeekdayArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,
                getDaysOfWeek(), isDark);
    }

    public List<DateGridFragment> getFragments() {
        return fragments;
    }

    public void setListener(FlextListener caldroidListener) {
        this.caldroidListener = caldroidListener;
    }

    public void setCalendarDateTime(DateTime dateTime) {
        month = dateTime.getMonth();
        year = dateTime.getYear();
        if (caldroidListener != null) {
            caldroidListener.onMonthChanged(month, year);
        }
        refreshView();
    }

    protected ArrayList<String> getDaysOfWeek() {
        ArrayList<String> list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());
        DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
        DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);
        for (int i = 0; i < 7; i++) {
            Date date = FlextHelper.convertDateTimeToDate(nextDay);
            list.add(fmt.format(date).toUpperCase());
            nextDay = nextDay.plusDays(1);
        }
        return list;
    }

    private MonthView.OnDateClick getDateItemClickListener() {
        if (dateItemClickListener == null) {
            dateItemClickListener = dateTime -> {
                if (caldroidListener != null) {
                    Date date = FlextHelper.convertDateTimeToDate(dateTime);
                    caldroidListener.onClickDate(date);
                }
            };
        }
        return dateItemClickListener;
    }

    private MonthView.OnDateLongClick getDateItemLongClickListener() {
        if (dateItemLongClickListener == null) {
            dateItemLongClickListener = dateTime -> {
                if (caldroidListener != null) {
                    Date date = FlextHelper.convertDateTimeToDate(dateTime);
                    caldroidListener.onLongClickDate(date);
                }
            };
        }
        return dateItemLongClickListener;
    }

    protected void refreshMonthTitleTextView() {
        firstMonthTime.set(Calendar.YEAR, year);
        firstMonthTime.set(Calendar.MONTH, month - 1);
        firstMonthTime.set(Calendar.DAY_OF_MONTH, 1);
        long millis = firstMonthTime.getTimeInMillis();
        monthYearStringBuilder.setLength(0);
        String monthTitle = DateUtils.formatDateRange(getActivity(),
                monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString();
        binding.monthYear.setText(StringUtils.capitalize(monthTitle));
        if (caldroidListener != null) {
            caldroidListener.onMonthSelected(month);
        }
        if (binding.imageView != null && enableImage) {
            ImageCheck check = ImageCheck.getInstance();
            if (check.isImage(month - 1, photosList[month - 1])) {
                Picasso.with(getActivity()).load(new File(check.getImage(month - 1, photosList[month - 1]))).into(binding.imageView);
            } else {
                new LoadAsync(getActivity(), month - 1, photosList[month - 1]).execute();
            }
        }
    }

    public void refreshView() {
        if (month == -1 || year == -1) {
            return;
        }
        refreshMonthTitleTextView();
    }

    protected void retrieveInitialArgs() {
        Bundle args = getArguments();
        if (args != null) {
            month = args.getInt(MONTH, -1);
            year = args.getInt(YEAR, -1);
            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1);
            if (startDayOfWeek > 7) {
                startDayOfWeek = startDayOfWeek % 7;
            }
            enableImage = args.getBoolean(ENABLE_IMAGES, true);
            isDark = args.getBoolean(DARK_THEME, true);
            long[] photos = args.getLongArray(MONTH_IMAGES);
            if (photos != null) {
                for (int i = 0; i < photos.length; i++) {
                    long id = photos[i];
                    if (id != -1) photosList[i] = id;
                }
            }
        }
        if (month == -1 || year == -1) {
            DateTime dateTime = DateTime.today(TimeZone.getDefault());
            month = dateTime.getMonth();
            year = dateTime.getYear();
        }
    }

    private void setupDateGridPages() {
        DateTime currentDateTime = new DateTime(year, month, 1, 0, 0, 0, 0);
        DatePageChangeListener pageChangeListener = initMonthPager(currentDateTime);
        final MonthPagerAdapter pagerAdapter = new MonthPagerAdapter(getFragmentManager());
        fragments = pagerAdapter.getFragments();
        fragments.get(0).setDate(currentDateTime.getMonth(), currentDateTime.getYear());
        DateTime nextDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay);
        fragments.get(1).setDate(nextDateTime.getMonth(), nextDateTime.getYear());
        DateTime next2DateTime = nextDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay);
        fragments.get(2).setDate(next2DateTime.getMonth(), next2DateTime.getYear());
        DateTime prevDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay);
        fragments.get(3).setDate(prevDateTime.getMonth(), prevDateTime.getYear());
        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
            DateGridFragment dateGridFragment = fragments.get(i);
            dateGridFragment.setEventsMap(mLastMap);
            dateGridFragment.setOnItemClickListener(getDateItemClickListener());
            dateGridFragment.setOnItemLongClickListener(getDateItemLongClickListener());
        }
        pageChangeListener.setFlextGridAdapters(fragments);
        InfinitePagerAdapter infinitePagerAdapter = new InfinitePagerAdapter(pagerAdapter);
        binding.monthsInfinitePager.setAdapter(infinitePagerAdapter);
    }

    @NonNull
    private DatePageChangeListener initMonthPager(DateTime currentDateTime) {
        DatePageChangeListener pageChangeListener = new DatePageChangeListener();
        pageChangeListener.setCurrentDateTime(currentDateTime);
        binding.monthsInfinitePager.setEnabled(true);
        if (Module.isLollipop()) {
            binding.monthsInfinitePager.addOnPageChangeListener(pageChangeListener);
        } else {
            binding.monthsInfinitePager.setOnPageChangeListener(pageChangeListener);
        }
        return pageChangeListener;
    }

    public static FlextCalendarFragment newInstance() {
        FlextCalendarFragment fragment = new FlextCalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FlextCalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        retrieveInitialArgs();
        binding = FragmentFlextCalBinding.inflate(inflater, container, false);
        WeekdayArrayAdapter weekdaysAdapter = getNewWeekdayAdapter();
        binding.weekdayGridview.setAdapter(weekdaysAdapter);
        setupDateGridPages();
        refreshView();
        if (caldroidListener != null) {
            caldroidListener.onViewCreated();
        }
        return binding.getRoot();
    }

    private void loadData() {
        EventsDataProvider provider = CalendarSingleton.getInstance().getProvider();
        if (provider != null) {
            if (provider.isReady()) {
                Timber.d("loadData: isReady");
                onReady();
            } else {
                Timber.d("loadData: wait");
                provider.addObserver(this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.calendar));
            getCallback().setClick(CalendarSingleton.getInstance().getFabClick());
        }
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventsDataProvider provider = CalendarSingleton.getInstance().getProvider();
        if (provider != null) {
            provider.removeObserver(this);
        }
    }

    @Override
    public void onReady() {
        Timber.d("onReady: ");
        EventsDataProvider provider = CalendarSingleton.getInstance().getProvider();
        if (provider != null) {
            this.mLastMap = provider.getEvents();
        }
        setupDateGridPages();
        refreshView();
    }

    private class DatePageChangeListener implements ViewPager.OnPageChangeListener {

        private int currentPage = InfiniteViewPager.OFFSET;
        private DateTime currentDateTime;
        private List<DateGridFragment> flextGridAdapters;

        void setFlextGridAdapters(List<DateGridFragment> flextGridAdapters) {
            this.flextGridAdapters = flextGridAdapters;
        }

        void setCurrentDateTime(DateTime dateTime) {
            this.currentDateTime = dateTime;
            setCalendarDateTime(currentDateTime);
        }

        /**
         * Return virtual next position
         *
         * @param position position
         * @return position
         */
        private int getNext(int position) {
            return (position + 1) % NUMBER_OF_PAGES;
        }

        /**
         * Return virtual previous position
         *
         * @param position position
         * @return position
         */
        private int getPrevious(int position) {
            return (position + 3) % NUMBER_OF_PAGES;
        }

        /**
         * Return virtual current position
         *
         * @param position position
         * @return position
         */
        int getCurrent(int position) {
            return position % NUMBER_OF_PAGES;
        }

        @Override
        public void onPageScrollStateChanged(int position) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        void refreshAdapters(int position) {
            DateGridFragment currentAdapter = flextGridAdapters.get(getCurrent(position));
            DateGridFragment prevAdapter = flextGridAdapters.get(getPrevious(position));
            DateGridFragment nextAdapter = flextGridAdapters.get(getNext(position));
            if (position == currentPage) {
                currentAdapter.setEventsMap(mLastMap);
                currentAdapter.setDateTime(currentDateTime);
                prevAdapter.setEventsMap(mLastMap);
                prevAdapter.setDateTime(currentDateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
                nextAdapter.setEventsMap(mLastMap);
                nextAdapter.setDateTime(currentDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
            }
            // Detect if swipe right or swipe left
            // Swipe right
            else if (position > currentPage) {
                currentDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                nextAdapter.setEventsMap(mLastMap);
                nextAdapter.setDateTime(currentDateTime.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
            }
            // Swipe left
            else {
                currentDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                prevAdapter.setEventsMap(mLastMap);
                prevAdapter.setDateTime(currentDateTime.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay));
            }
            currentPage = position;
        }

        /**
         * Refresh the fragments
         */
        @Override
        public void onPageSelected(int position) {
            LogUtil.d(TAG, "onPageSelected: " + position);
            refreshAdapters(position);
            setCalendarDateTime(currentDateTime);
        }
    }
}
