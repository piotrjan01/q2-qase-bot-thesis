/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.common.stats;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import piotrrr.thesis.bots.tuning.DuelEvalResults;
import piotrrr.thesis.bots.tuning.OptResults;
import piotrrr.thesis.common.stats.BotStatistic.Kill;
import piotrrr.thesis.common.stats.BotStatistic.Reward;

/**
 *
 * @author Piotr Gwizdała
 */
public class StatsChartsFactory {

    public static int rewardsChartSegments = 500;
    public static int avgRewardsWindowSize = 500;

    private static class BotSeries {

        XYSeries series;
        int int1 = 0;
        int int2 = 0;
        double d1 = 0;
        String botName = "no name set";

        public BotSeries(XYSeries s, int i1, int i2, String botName) {
            this.series = s;
            this.int1 = i1;
            this.int2 = i2;
            this.botName = botName;
        }
    }

    public static ChartPanel getKillsInTimeByBotType(BotStatistic stats) {

        synchronized (stats) {

            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();

            for (String botName : stats.getAllBotFamilies()) {
                series.add(new BotSeries(new XYSeries(botName, true), 0, 0, botName));
            }

            for (BotSeries s : series) {
                s.series.add(0, 0);
                s.int2 = StatsTools.countBotsOfGivenFamilly(s.botName, stats);
            }

            for (Kill k : stats.kills) {
                for (BotSeries s : series) {
                    if (k.killer.startsWith(s.botName)) {
                        s.int1++;
                        double time = k.time / 10;
                        if (time < s.d1) {
                            time = s.d1 + 1;
                        }
//                    while (time < s.series.)
                        s.series.add(time, (double) s.int1 / s.int2);
                        s.d1 = time;
                    }
                }
            }

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "Kills by bot type in time",
                    "time [s]",
                    "kills",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    private static class KillComparator implements Comparator<Kill> {

        public int compare(Kill o1, Kill o2) {
            if (o1.time <= o2.time) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static ChartPanel getKillsInTimeByBotTypeSorting(BotStatistic stats) {

        synchronized (stats) {

            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();

            for (String botName : stats.getAllBotFamilies()) {
                series.add(new BotSeries(new XYSeries(botName, true), 0, 0, botName));
            }

            for (BotSeries s : series) {
                s.series.add(0, 0);
                s.int2 = StatsTools.countBotsOfGivenFamilly(s.botName, stats);
            }


            TreeSet<Kill> ts = new TreeSet<Kill>(new KillComparator());
            for (Kill k : stats.kills) {
                ts.add(k);
            }

            for (Kill k : ts) {
                for (BotSeries s : series) {
                    if (k.killer.startsWith(s.botName)) {
                        s.int1++;
                        double time = k.time / 10;
                        if (time < s.d1) {
                            time = s.d1 + 1;
                        }
//                    while (time < s.series.)
                        s.series.add(time, (double) s.int1 / s.int2);
                        s.d1 = time;
                    }
                }
            }

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "Kills by bot type in time",
                    "time [s]",
                    "kills",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    public static ChartPanel getDeathsInTimeByBotType(BotStatistic stats) {
        synchronized (stats) {
            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();

            for (String botName : stats.getAllBotFamilies()) {
                series.add(new BotSeries(new XYSeries(botName), 0, 0, botName));
            }

            for (BotSeries s : series) {
                s.series.add(0, 0);
                s.int2 = StatsTools.countBotsOfGivenFamilly(s.botName, stats);
            }

            for (Kill k : stats.kills) {
                for (BotSeries s : series) {
                    if (k.victim.startsWith(s.botName)) {
                        double time = k.time / 10;
                        if (s.d1 > time) {
                            time = s.d1;
                        }
                        s.int1++;
                        s.series.add(time, (double) s.int1 / s.int2);
                        s.d1 = time;
                    }
                }
            }

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "Deaths by bot type in time",
                    "time [s]",
                    "deaths",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    public static ChartPanel getKillsPerEachDeathByBotType(BotStatistic stats) {
        synchronized (stats) {
            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();

            for (String botName : stats.getAllBotFamilies()) {
                series.add(new BotSeries(new XYSeries(botName), 0, 0, botName));
            }

            for (BotSeries s : series) {
                s.series.add(0, 0);
                s.d1 = StatsTools.countBotsOfGivenFamilly(s.botName, stats);
            }

            for (Kill k : stats.kills) {
                for (BotSeries s : series) {
                    if (k.killer.startsWith(s.botName) || k.victim.startsWith(s.botName)) {
                        if (k.killer.startsWith(s.botName)) {
                            s.int1++;
                        }
                        if (k.victim.startsWith(s.botName)) {
                            s.int2++;
                        }
                        float val = 0;
                        if (s.int2 != 0) {
                            val = (float) s.int1 / (float) s.int2;
                        }
                        s.series.add(k.time / 10, val / s.d1);
                    }
                }
            }

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "Kills per each death by bot type in time",
                    "time [s]",
                    "kills / deaths",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    public static ChartPanel getKillsPerEachDeathByBot(BotStatistic stats) {
        synchronized (stats) {
            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();

            for (String botName : stats.getAllKillingBotNames()) {
                series.add(new BotSeries(new XYSeries(botName), 0, 0, botName));
            }

            for (BotSeries s : series) {
                s.series.add(0, 0);
//                s.d1 = StatsTools.countBotsOfGivenFamilly(s.botName, stats);
            }

            for (Kill k : stats.kills) {
                for (BotSeries s : series) {
                    if (k.killer.equals(s.botName) || k.victim.equals(s.botName)) {
                        if (k.killer.equals(s.botName)) {
                            s.int1++;
                        }
                        if (k.victim.equals(s.botName)) {
                            s.int2++;
                        }
                        float val = 0;
                        if (s.int2 != 0) {
                            val = (float) s.int1 / (float) s.int2;
                        }
                        s.series.add(k.time / 10, val);
                    }
                }
            }

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "Kills per each death by each bot in time",
                    "time [s]",
                    "kills / death",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    public static ChartPanel getKillsInTimeByBot(BotStatistic stats) {
        synchronized (stats) {
            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();

            for (String botName : stats.getAllKillingBotNames()) {
                series.add(new BotSeries(new XYSeries(botName), 0, 0, botName));
            }

            for (BotSeries s : series) {
                s.series.add(0, 0);
            }

            for (Kill k : stats.kills) {
                for (BotSeries s : series) {
                    if (s.botName.equals(k.killer)) {
                        s.int1++;
                        s.series.add(k.time / 10, s.int1);
                    }
                }
            }

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "Kills by each bot in time",
                    "time [s]",
                    "kills",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

//        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) ((XYPlot)c.getPlot()).getRenderer();
//        r.setDrawOutlines(true);
//        r.setShapesVisible(true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    private static class WeaponUsage {

        int blaster = 0;
        int sshotgun = 0;
        int shotgun = 0;
        int machinegun = 0;
        int railgun = 0;
        int hyperblaster = 0;
        int chaingun = 0;
        int rocket = 0;
        int other = 0;
        String name = "no-name";
    }

    public static ChartPanel getWeaponUseageByBotBarChart(BotStatistic stat) {
        synchronized (stat) {
            TreeMap<String, TreeMap<String, Integer>> map = new TreeMap<String, TreeMap<String, Integer>>();
            TreeMap<String, Integer> tKills = new TreeMap<String, Integer>();

            DefaultCategoryDataset ds = new DefaultCategoryDataset();

            for (String p : stat.getAllKillingBotNames()) {
                map.put(p, new TreeMap<String, Integer>());
            }

            for (Kill k : stat.kills) {
                TreeMap<String, Integer> usage = map.get(k.killer);
                if (usage.containsKey(k.gunUsed)) {
                    int c = usage.get(k.gunUsed);
                    usage.remove(k.gunUsed);
                    usage.put(k.gunUsed, c + 1);
                } else {
                    usage.put(k.gunUsed, 1);
                }
            }

            for (String b : map.keySet()) {
                int cnt = 0;
                TreeMap<String, Integer> usgs = map.get(b);
                for (String w : usgs.keySet()) {
                    cnt += usgs.get(w);
                }
                tKills.put(b, cnt);
            }

            for (String bn : map.keySet()) {
                TreeMap<String, Integer> usage = map.get(bn);
                for (String wpn : usage.keySet()) {
                    double nr = (double) usage.get(wpn) / (double) tKills.get(bn);
                    ds.addValue((Number) nr, wpn, bn);
                }
            }

            JFreeChart c = ChartFactory.createStackedBarChart(
                    "Weapon use by each bot",
                    "Bot",
                    "Weapon usage",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);



            ChartPanel cp = new ChartPanel(c);
            return cp;
        }
    }

    public static ChartPanel getWhoKillsWhomBarChart(BotStatistic stat) {
        synchronized (stat) {
            TreeMap<String, TreeMap<String, Integer>> map = new TreeMap<String, TreeMap<String, Integer>>();

            DefaultCategoryDataset ds = new DefaultCategoryDataset();

            for (String p : stat.getAllKillingBotNames()) {
                TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
                for (String bn : stat.getAllKillingBotNames()) {
                    tm.put(bn, 0);
                }
                map.put(p, tm);
            }

            for (Kill k : stat.kills) {
                TreeMap<String, Integer> tm = map.get(k.killer);
                int c = tm.get(k.victim);
                tm.remove(k.victim);
                tm.put(k.victim, c + 1);
            }

            for (String bn : map.keySet()) {
                TreeMap<String, Integer> usage = map.get(bn);
                for (String wpn : usage.keySet()) {
                    ds.addValue((Number) usage.get(wpn), wpn, bn);
                }
            }

            JFreeChart c = ChartFactory.createStackedBarChart(
                    "Who how often killed whom",
                    "Killer bot",
                    "Kills by bot",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);



            ChartPanel cp = new ChartPanel(c);
            return cp;
        }
    }

    public static ChartPanel getRewardsInTimeByEachBot(BotStatistic stats) {
        synchronized (stats) {
            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();


            synchronized (stats) {

                for (String botName : stats.getAllRewardedBotNames()) {
                    series.add(new BotSeries(new XYSeries(botName), 0, 0, botName));
                }

                for (BotSeries s : series) {
                    s.series.add(0, 0);
                }

                String allName = "all avg";
                int botsNum = stats.getAllRewardedBotNames().size();
                series.add(new BotSeries(new XYSeries(allName), 0, 0, allName));

//                int segmentSize = stats.rewards.size() / rewardsChartSegments;
//                if (segmentSize < 1) {
//                    segmentSize = 1;
//                }

//                int i = 0;

                for (BotStatistic.Reward k : stats.rewards) {
                    for (BotSeries s : series) {
                        if (s.botName.equals(k.botName)) {
                            s.d1 += k.reward;
                            s.int1++;
                            s.series.add(s.int1, s.d1);
                            break;
                        }
                    }
                    BotSeries as = series.getLast();
                    as.d1 += k.reward / botsNum;

//                    i++;
//                    if (i % segmentSize == 0) {
//                        for (BotSeries s : series) {
//                            s.series.add(s.int1, s.d1);
//                        }
//                    }


                }
            }

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "Rewards by each bot",
                    "action",
                    "rewards",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

//        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) ((XYPlot)c.getPlot()).getRenderer();
//        r.setDrawOutlines(true);
//        r.setShapesVisible(true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    public static ChartPanel getAvgRewardsChart(BotStatistic stats) {
        synchronized (stats) {
            XYSeriesCollection ds = new XYSeriesCollection();

            LinkedList<BotSeries> series = new LinkedList<BotSeries>();
            HashMap<String, LinkedList<Double>> localRewards = new HashMap<String, LinkedList<Double>>();

            for (String botName : stats.getAllRewardedBotNames()) {
                series.add(new BotSeries(new XYSeries(botName), 0, 0, botName));
                localRewards.put(botName, new LinkedList<Double>());
            }

            for (BotSeries s : series) {
                s.int1 = avgRewardsWindowSize;
            }

            String allName = "all RL";
            series.add(new BotSeries(new XYSeries(allName), 0, 0, allName));

            int i = 0;
            for (Reward k : stats.rewards) {
                i++;
                LinkedList<Double> br = localRewards.get(k.botName);
                br.add(k.reward);
                if (br.size() >= avgRewardsWindowSize) {
                    double sum = 0;
                    for (Double d : br) {
                        sum += d;
                    }
                    for (BotSeries s : series) {
                        if (k.botName.equals(s.botName)) {
                            s.int1++;
                            s.series.add(s.int1, sum / avgRewardsWindowSize);
                            break;
                        }
                    }
                    br.removeFirst();
                }

            }
            System.out.println("cnt = " + i);
            i = 0;
            for (BotSeries s : series) {
                System.out.println(s.botName + " = " + s.int1);
                i += s.int1;
            }
            System.out.println("cnt = " + i);

            for (BotSeries s : series) {
                ds.addSeries(s.series);
            }

            JFreeChart c = ChartFactory.createXYLineChart(
                    "vbhjm",
                    "te [s]",
                    "avg  speed",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);

            ChartPanel cp = new ChartPanel(c);
            return cp;

        }
    }

    public static ChartPanel getEvaluationErrorConvergencePlot(OptResults res, String killerNamePrefix) {

        HashMap<String, LinkedList<double[]>> data = new HashMap<String, LinkedList<double[]>>();
        LinkedList<String> seriesNames = new LinkedList<String>();

        int n=0;
        for (DuelEvalResults r : res.iterResults) {
            BotStatistic stats = r.stats;

            HashMap<String, Integer> scores = new HashMap<String, Integer>();
            for (Kill k : stats.kills) {
                if (scores.containsKey(k.killer)) {
                    continue;
                }
                if (k.killer.startsWith(killerNamePrefix)) {
                    int score = StatsTools.getBotScore(k.killer, stats);
                    scores.put(k.killer, score);
                }
            }

            double avg = 0;
            for (int score : scores.values()) {
                avg += score;
            }
            avg = avg / scores.values().size();

            LinkedList<double[]> convgData = new LinkedList<double[]>();
            int i = 0;
            double cAvg = 0;
            for (int score : scores.values()) {
                cAvg += score;
                convgData.add(new double[]{i, (cAvg / (i + 1)) - avg});
                i++;
            }


            seriesNames.add(r.shortName+n);
            data.put(r.shortName+n, convgData);
            n++;
        }

        return getXYChart(seriesNames, data, "Evaluation errors", "Iteration", "Error");

    }

    /**
     *
     * @param seriesNames list of series names
     * @param data map with key of series name and value with list of x, y points (sorted?)
     * @param title title of plot
     * @param xLabel x axis label
     * @param yLabel y axis label
     * @return
     */
    public static ChartPanel getXYChart(LinkedList<String> seriesNames,
            HashMap<String, LinkedList<double[]>> data,
            String title, String xLabel, String yLabel) {

        XYSeriesCollection ds = new XYSeriesCollection();

        for (String sName : seriesNames) {
            XYSeries s = new XYSeries(sName);
            for (double[] val : data.get(sName)) {
                s.add(val[0], val[1]);
            }
            ds.addSeries(s);
        }

        JFreeChart c = ChartFactory.createXYLineChart(
                title,
                xLabel,
                yLabel,
                ds,
                PlotOrientation.VERTICAL,
                true, true, true);

        ChartPanel cp = new ChartPanel(c);
        return cp;

    }
}
