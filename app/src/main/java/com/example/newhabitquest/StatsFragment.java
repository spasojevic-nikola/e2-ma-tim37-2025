package com.example.newhabitquest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.Legend;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Calendar;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

public class StatsFragment extends Fragment {
    private TextView tvActiveDaysNumber, tvLongestStreakNumber, tvMissionsCompleted, tvMissionsStarted;
    private PieChart pieTasks;
    private BarChart barCategory;
    private LineChart lineDifficulty, lineXP;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Initialize views with new IDs
        tvActiveDaysNumber = view.findViewById(R.id.tv_active_days_number);
        tvLongestStreakNumber = view.findViewById(R.id.tv_longest_streak_number);
        tvMissionsCompleted = view.findViewById(R.id.tv_missions_completed);
        tvMissionsStarted = view.findViewById(R.id.tv_missions_started);
        pieTasks = view.findViewById(R.id.pie_tasks);
        barCategory = view.findViewById(R.id.bar_category);
        lineDifficulty = view.findViewById(R.id.line_difficulty);
        lineXP = view.findViewById(R.id.line_xp);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // Initialize charts with better styling
        setupCharts();

        if (userId != null) {
            loadUserTasks();
        }
        return view;
    }

    private void setupCharts() {
        // Setup PieChart
        pieTasks.setUsePercentValues(true);
        pieTasks.getDescription().setEnabled(false);
        pieTasks.setDrawHoleEnabled(true);
        pieTasks.setHoleRadius(30f);
        pieTasks.setTransparentCircleRadius(35f);
        pieTasks.setRotationAngle(0);
        pieTasks.setRotationEnabled(true);
        pieTasks.setHighlightPerTapEnabled(true);
        pieTasks.animateY(1000);

        // Setup BarChart
        barCategory.getDescription().setEnabled(false);
        barCategory.setDrawGridBackground(false);
        barCategory.setDrawBarShadow(false);
        barCategory.setHighlightFullBarEnabled(false);
        barCategory.animateY(1000);

        // Setup LineCharts
        lineDifficulty.getDescription().setEnabled(false);
        lineDifficulty.setTouchEnabled(true);
        lineDifficulty.setDragEnabled(true);
        lineDifficulty.setScaleEnabled(true);
        lineDifficulty.setPinchZoom(true);
        lineDifficulty.animateX(1000);

        lineXP.getDescription().setEnabled(false);
        lineXP.setTouchEnabled(true);
        lineXP.setDragEnabled(true);
        lineXP.setScaleEnabled(true);
        lineXP.setPinchZoom(true);
        lineXP.animateX(1000);
    }

    private void showStats(List<Task> tasks) {
        // 1. Analiza aktivnih dana i najdužeg niza
        TreeSet<String> activeDays = new TreeSet<>();
        int longestStreak = calculateLongestStreak(tasks, activeDays);

        // Prikaži osnovne statistike
        tvActiveDaysNumber.setText(String.valueOf(activeDays.size()));
        tvLongestStreakNumber.setText(String.valueOf(longestStreak));

        // 2. PieChart za status zadataka sa poboljšanim dizajnom
        createTaskStatusPieChart(tasks);

        // 3. BarChart za kategorije sa poboljšanim dizajnom
        createCategoryBarChart(tasks);

        // 4. LineChart za prosečnu težinu zadataka
        createDifficultyLineChart(tasks);

        // 5. LineChart za XP napredak u poslednjih 7 dana
        createXPLineChart(tasks);

        // 6. Specijalne misije statistike
        calculateSpecialMissions(tasks);
    }

    private int calculateLongestStreak(List<Task> tasks, TreeSet<String> activeDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, Boolean> daySuccess = new HashMap<>();

        // Pripremi podatke o uspešnosti po danima
        for (Task t : tasks) {
            String d = sdf.format(t.date);
            activeDays.add(d);
            if (!daySuccess.containsKey(d)) daySuccess.put(d, true);
            if ("neuradjen".equals(t.status)) daySuccess.put(d, false);
        }

        // Računanje najdužeg niza uspešnih dana
        int longestStreak = 0, currentStreak = 0;
        String lastDate = null;

        for (String d : activeDays) {
            Boolean success = daySuccess.get(d);
            if (success != null && success) {
                if (lastDate == null) {
                    currentStreak = 1;
                } else {
                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    try {
                        Date date1 = sdf.parse(lastDate);
                        Date date2 = sdf.parse(d);
                        if (date1 != null && date2 != null) {
                            cal1.setTime(date1);
                            cal2.setTime(date2);
                            long diff = (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 60 * 60 * 24);
                            if (diff == 1) {
                                currentStreak++;
                            } else {
                                currentStreak = 1;
                            }
                        } else {
                            currentStreak = 1;
                        }
                    } catch (Exception e) {
                        currentStreak = 1;
                    }
                }
                if (currentStreak > longestStreak) longestStreak = currentStreak;
            } else {
                currentStreak = 0;
            }
            lastDate = d;
        }

        return longestStreak;
    }

    private void createTaskStatusPieChart(List<Task> tasks) {
        int kreiran = 0, uradjen = 0, neuradjen = 0, otkazan = 0;

        for (Task t : tasks) {
            switch (t.status) {
                case "kreiran": kreiran++; break;
                case "uradjen": uradjen++; break;
                case "neuradjen": neuradjen++; break;
                case "otkazan": otkazan++; break;
            }
        }

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        if (kreiran > 0) pieEntries.add(new PieEntry(kreiran, "Kreirani"));
        if (uradjen > 0) pieEntries.add(new PieEntry(uradjen, "Urađeni"));
        if (neuradjen > 0) pieEntries.add(new PieEntry(neuradjen, "Neurađeni"));
        if (otkazan > 0) pieEntries.add(new PieEntry(otkazan, "Otkazani"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(new int[]{
            Color.parseColor("#2196F3"),  // Plava za kreirane
            Color.parseColor("#4CAF50"),  // Zelena za urađene
            Color.parseColor("#F44336"),  // Crvena za neurađene
            Color.parseColor("#9E9E9E")   // Siva za otkazane
        });
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setSliceSpace(2f);

        PieData pieData = new PieData(pieDataSet);
        pieTasks.setData(pieData);

        // Customize legend
        Legend legend = pieTasks.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieTasks.invalidate();
    }

    private void createCategoryBarChart(List<Task> tasks) {
        Map<String, Integer> catMap = new HashMap<>();
        for (Task t : tasks) {
            if ("uradjen".equals(t.status) && t.category != null) {
                Integer count = catMap.get(t.category);
                if (count == null) count = 0;
                catMap.put(t.category, count + 1);
            }
        }

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> catLabels = new ArrayList<>();
        int idx = 0;

        for (Map.Entry<String, Integer> e : catMap.entrySet()) {
            barEntries.add(new BarEntry(idx, e.getValue()));
            catLabels.add(e.getKey());
            idx++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Završeni zadaci");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.8f);

        barCategory.setData(barData);

        // Customize X axis
        XAxis xAxis = barCategory.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < catLabels.size()) {
                    return catLabels.get((int) value);
                }
                return "";
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Customize Y axis
        barCategory.getAxisLeft().setDrawGridLines(true);
        barCategory.getAxisRight().setEnabled(false);

        barCategory.invalidate();
    }

    private void createDifficultyLineChart(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, List<Integer>> diffMap = new HashMap<>();

        for (Task t : tasks) {
            if ("uradjen".equals(t.status)) {
                String d = sdf.format(t.date);
                List<Integer> diffs = diffMap.get(d);
                if (diffs == null) {
                    diffs = new ArrayList<>();
                    diffMap.put(d, diffs);
                }
                diffs.add(t.difficulty);
            }
        }

        ArrayList<Entry> diffEntries = new ArrayList<>();
        int idx = 0;

        for (String d : new TreeSet<>(diffMap.keySet())) {
            List<Integer> diffs = diffMap.get(d);
            if (diffs != null && !diffs.isEmpty()) {
                float avg = 0;
                for (int val : diffs) avg += val;
                avg /= diffs.size();
                diffEntries.add(new Entry(idx++, avg));
            }
        }

        LineDataSet diffDataSet = new LineDataSet(diffEntries, "Prosečna težina");
        diffDataSet.setColor(Color.parseColor("#FF5722"));
        diffDataSet.setCircleColor(Color.parseColor("#FF5722"));
        diffDataSet.setLineWidth(3f);
        diffDataSet.setCircleRadius(5f);
        diffDataSet.setDrawCircleHole(false);
        diffDataSet.setValueTextSize(10f);
        diffDataSet.setDrawFilled(true);
        diffDataSet.setFillColor(Color.parseColor("#FFAB91"));

        LineData lineData = new LineData(diffDataSet);
        lineDifficulty.setData(lineData);

        // Customize axes
        lineDifficulty.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineDifficulty.getXAxis().setDrawGridLines(false);
        lineDifficulty.getAxisRight().setEnabled(false);

        lineDifficulty.invalidate();
    }

    private void createXPLineChart(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, Integer> xpMap = new HashMap<>();

        // Pripremi poslednjih 7 dana
        Calendar cal = Calendar.getInstance();
        ArrayList<String> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String d = sdf.format(cal.getTime());
            last7Days.add(d);
            xpMap.put(d, 0);
        }

        // Dodeli XP za svaki dan
        for (Task t : tasks) {
            if ("uradjen".equals(t.status)) {
                String d = sdf.format(t.date);
                if (xpMap.containsKey(d)) {
                    Integer currentXP = xpMap.get(d);
                    if (currentXP == null) currentXP = 0;
                    xpMap.put(d, currentXP + t.xp);
                }
            }
        }

        ArrayList<Entry> xpEntries = new ArrayList<>();
        for (int i = 0; i < last7Days.size(); i++) {
            String day = last7Days.get(i);
            Integer xp = xpMap.get(day);
            if (xp == null) xp = 0;
            xpEntries.add(new Entry(i, xp));
        }

        LineDataSet xpDataSet = new LineDataSet(xpEntries, "XP po danima");
        xpDataSet.setColor(Color.parseColor("#3F51B5"));
        xpDataSet.setCircleColor(Color.parseColor("#3F51B5"));
        xpDataSet.setLineWidth(3f);
        xpDataSet.setCircleRadius(5f);
        xpDataSet.setDrawCircleHole(false);
        xpDataSet.setValueTextSize(10f);
        xpDataSet.setDrawFilled(true);
        xpDataSet.setFillColor(Color.parseColor("#9FA8DA"));

        LineData lineData = new LineData(xpDataSet);
        lineXP.setData(lineData);

        // Customize X axis with day labels
        lineXP.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < last7Days.size()) {
                    try {
                        Date date = sdf.parse(last7Days.get((int) value));
                        if (date != null) {
                            SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                            return dayFormat.format(date);
                        }
                    } catch (Exception e) {
                        return "";
                    }
                }
                return "";
            }
        });
        lineXP.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXP.getXAxis().setDrawGridLines(false);
        lineXP.getXAxis().setGranularity(1f);
        lineXP.getAxisRight().setEnabled(false);

        lineXP.invalidate();
    }

    private void calculateSpecialMissions(List<Task> tasks) {
        int started = 0, finished = 0;

        for (Task t : tasks) {
            if (t.specialMission) {
                started++;
                if ("uradjen".equals(t.status)) {
                    finished++;
                }
            }
        }

        tvMissionsCompleted.setText(String.valueOf(finished));
        tvMissionsStarted.setText(String.valueOf(started));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != null) {
            listenForTaskUpdates();
        }
    }

    private void loadUserTasks() {
        // Create sample hardcoded data for demonstration
        List<Task> sampleTasks = createSampleTasks();

        // Also try to load real data from Firebase and combine it
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> realTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        task.id = doc.getId();
                        realTasks.add(task);
                    }

                    // Combine sample data with real data
                    List<Task> allTasks = new ArrayList<>(sampleTasks);
                    allTasks.addAll(realTasks);

                    showStats(allTasks);
                })
                .addOnFailureListener(e -> {
                    // If Firebase fails, just show sample data
                    showStats(sampleTasks);
                });
    }

    private List<Task> createSampleTasks() {
        List<Task> sampleTasks = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // Create tasks for the last 30 days with varied data
        for (int i = 29; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date taskDate = cal.getTime();

            // Add 2-4 tasks per day with different patterns
            int tasksPerDay = 2 + (i % 3); // 2-4 tasks per day

            for (int j = 0; j < tasksPerDay; j++) {
                Task task = new Task();
                task.userId = userId;
                task.date = taskDate;

                // Vary categories
                String[] categories = {"Zdravlje", "Učenje", "Posao", "Sport", "Hobi", "Kućni poslovi"};
                task.category = categories[j % categories.length];

                // Vary difficulty (1-5)
                task.difficulty = 1 + (j + i) % 5;

                // Calculate XP based on difficulty
                task.xp = task.difficulty * 10 + (j * 5);

                // Vary status with realistic distribution
                double statusRand = Math.random();
                if (i < 7) { // Last week - more realistic current data
                    if (statusRand < 0.6) task.status = "uradjen";
                    else if (statusRand < 0.8) task.status = "kreiran";
                    else if (statusRand < 0.95) task.status = "neuradjen";
                    else task.status = "otkazan";
                } else { // Older data - mostly completed
                    if (statusRand < 0.75) task.status = "uradjen";
                    else if (statusRand < 0.85) task.status = "neuradjen";
                    else task.status = "otkazan";
                }

                // Some tasks are special missions
                task.specialMission = (j == 0 && i % 5 == 0); // Every 5th day, first task is special mission

                sampleTasks.add(task);
            }
        }

        return sampleTasks;
    }

    private void listenForTaskUpdates() {
        if (userId != null) {
            db.collection("tasks")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null || queryDocumentSnapshots == null) {
                            // If there's an error, show sample data
                            showStats(createSampleTasks());
                            return;
                        }

                        List<Task> realTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Task task = doc.toObject(Task.class);
                            task.id = doc.getId();
                            realTasks.add(task);
                        }

                        // Combine with sample data for richer display
                        List<Task> allTasks = new ArrayList<>(createSampleTasks());
                        allTasks.addAll(realTasks);

                        showStats(allTasks);
                    });
        }
    }
}
