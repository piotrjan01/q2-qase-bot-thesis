/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.common.stats;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
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

    private static TreeSet<String> getAllPickedUpItemsNames(BotStatistic stat) {
        TreeSet<String> ret = new TreeSet<String>();
        for (BotStatistic.Pickup k : stat.pickups) {
            ret.add(getPickupItemName(k));
        }
        return ret;
    }

    private static String getPickupItemName(BotStatistic.Pickup p) {
        return p.what.substring(0, p.what.indexOf("#"));
    }

    private static String getPickupBotName(BotStatistic.Pickup p) {
        return p.what.substring(p.what.indexOf("#") + 1);
    }

    public static ChartPanel getPickedUpItemsChart(BotStatistic stat, String botName) {
        synchronized (stat) {
            TreeMap<String, TreeMap<String, Integer>> map = new TreeMap<String, TreeMap<String, Integer>>();

            DefaultCategoryDataset ds = new DefaultCategoryDataset();


            for (String p : stat.getAllKillingBotNames()) {
                TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
                for (String in : getAllPickedUpItemsNames(stat)) {
                    tm.put(in, 0);
                }
                map.put(p, tm);
            }

            for (BotStatistic.Pickup p : stat.pickups) {
                TreeMap<String, Integer> tm = map.get(getPickupBotName(p));
                int c = tm.get(getPickupItemName(p));
                tm.remove(getPickupItemName(p));
                tm.put(getPickupItemName(p), c + 1);
            }

            for (String bn : map.keySet()) {
                TreeMap<String, Integer> usage = map.get(bn);
                for (String wpn : usage.keySet()) {
                    ds.addValue((Number) usage.get(wpn), wpn, bn);
                }
            }

            JFreeChart c = ChartFactory.createBarChart(
                    "",
                    "Item",
                    "Nr of pick ups",
                    ds,
                    PlotOrientation.VERTICAL,
                    true, true, true);



            ChartPanel cp = new ChartPanel(c);
            return cp;
        }
    }

    private static TreeSet<String> getAllPickedUpItemsCategories(BotStatistic stat) {
        TreeSet<String> ret = new TreeSet<String>();
        for (BotStatistic.Pickup k : stat.pickups) {
            ret.add(getPickupItemCategory(k));
        }
        return ret;
    }

    private static String getPickupItemCategory(BotStatistic.Pickup p) {
        String es = p.what;
        if (es.contains("health")) {
            return "health";
        } else if (es.contains("armor")) {
            return "armor";
        } else if (es.contains("weapons")) {
            return "weapon";
        } else if (es.contains("ammo")) {
            return "ammo";
        } else {
            return "UNKNOWN";
        }

    }

    public static ChartPanel getPickedUpItemsByCategoryChart(BotStatistic stat, String botName) {
        synchronized (stat) {
            TreeMap<String, TreeMap<String, Integer>> map = new TreeMap<String, TreeMap<String, Integer>>();

            DefaultCategoryDataset ds = new DefaultCategoryDataset();



            for (String p : stat.getAllKillingBotNames()) {
                TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
                for (String in : getAllPickedUpItemsCategories(stat)) {
                    if (in.equals("UNKNOWN")) {
                        continue;
                    }
                    tm.put(in, 0);
                }
                map.put(p, tm);
            }

            for (BotStatistic.Pickup p : stat.pickups) {
                if (getPickupItemCategory(p).equals("UNKNOWN")) {
                    continue;
                }
                TreeMap<String, Integer> tm = map.get(getPickupBotName(p));
                int c = tm.get(getPickupItemCategory(p));
                tm.remove(getPickupItemCategory(p));
                tm.put(getPickupItemCategory(p), c + 1);
            }

            for (String bn : map.keySet()) {
                TreeMap<String, Integer> usage = map.get(bn);
                for (String wpn : usage.keySet()) {
                    ds.addValue((Number) usage.get(wpn), wpn, bn);
                }
            }

            JFreeChart c = ChartFactory.createBarChart(
                    "",
                    "Item category",
                    "Nr of pick ups",
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

    public static ChartPanel getResultsDistributionPlot(DuelEvalResults res, String killerNamePrefix, int bins) {
        BotStatistic stats = res.stats;

        HistogramDataset ds = new HistogramDataset();

        HashMap<String, Integer> scores = new HashMap<String, Integer>();
        for (Kill k : stats.kills) {
            if (scores.containsKey(k.killer)) {
                continue;
            }
            if (k.killer.startsWith(killerNamePrefix)) {
                int score = StatsTools.getBotScore(k.killer, stats) - StatsTools.getBotScore(k.victim, stats);
                scores.put(k.killer, score);
            }
        }

        double[] vals = new double[scores.values().size()];
        int ind = 0;
        for (int i : scores.values()) {
            vals[ind] = i;
            ind++;
        }
        ds.addSeries("Score", vals, bins);

        JFreeChart c = ChartFactory.createHistogram("Results distribution",
                "Result",
                "Count",
                ds,
                PlotOrientation.VERTICAL,
                true,
                true,
                true);

        ChartPanel cp = new ChartPanel(c);
        return cp;

    }

    public static ChartPanel getEvalFitnessPlot(OptResults optResults) {
        LinkedList<double[]> evalScore = new LinkedList<double[]>();
        LinkedList<double[]> bestScores = new LinkedList<double[]>();
        LinkedList<double[]> maScores = new LinkedList<double[]>();

        int maBackSteps = 40;
        LinkedList<double[]> maData = new LinkedList<double[]>();

        int eval = 1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (DuelEvalResults res : optResults.iterResults) {
            if (res.score > bestScore) {
                bestScore = res.score;
            }
            evalScore.add(new double[]{eval, res.score});
            maData.add(new double[]{eval, res.score});
            if (maData.size() > maBackSteps) {
                maData.pollFirst();
                double avg = 0;
                for (double[] s : maData) {
                    avg += s[1];
                }
                avg /= maBackSteps;
                maScores.add(new double[]{eval, avg});
            }
            bestScores.add(new double[]{eval, bestScore});
            eval++;
        }
        LinkedList<String> series = new LinkedList<String>();
        HashMap<String, LinkedList<double[]>> map = new HashMap<String, LinkedList<double[]>>();
        series.add("best result");
        map.put(series.getLast(), bestScores);        
        series.add("moving avg (" + maBackSteps + ")");
        map.put(series.getLast(), maScores);
        series.add("result");
        map.put(series.getLast(), evalScore);
        ChartPanel p = StatsChartsFactory.getXYChart(series, map, "", "Evaluation", "Result", true);

//        XYPlot pl = (XYPlot) p.getChart().getPlot();
//        XYItemRenderer rend = pl.getRenderer();
//        rend.setToolTipGenerator(new HillClimbinIterLabelsAndTipsGenerator());
//        rend.setBaseItemLabelGenerator(new HillClimbinIterLabelsAndTipsGenerator());
//        rend.setBaseItemLabelsVisible(true);
//        rend.setItemLabelFont(new Font("Serif", Font.PLAIN, 8));

        formatChart(p, 2);

        saveChartAsEMF(p, "fitnessInEvals.emf", 640, 400);

        return p;
    }

    public static ChartPanel getIterFitnessPlot(OptResults optResults) {
        LinkedList<double[]> iterScores = new LinkedList<double[]>();
        int currentIter = -1;
        double iterBest = Double.NEGATIVE_INFINITY;
        boolean firstTime = true;
        for (DuelEvalResults res : optResults.iterResults) {
            if (firstTime) {
                currentIter = res.iterNr;
                iterBest = res.score;
                firstTime = false;
            }

            if (res.iterNr != currentIter) {
                iterScores.add(new double[]{currentIter, iterBest});
                iterBest = Double.NEGATIVE_INFINITY;
                currentIter = res.iterNr;
            }
            if (res.score > iterBest) {
                iterBest = res.score;
            }
        }
        if (optResults.iterResults.getLast().iterNr == currentIter) {
            iterScores.add(new double[]{currentIter, iterBest});
        }
        LinkedList<String> series = new LinkedList<String>();
        series.add("Iter fitness");
        HashMap<String, LinkedList<double[]>> map = new HashMap<String, LinkedList<double[]>>();
        map.put(series.getLast(), iterScores);
        return StatsChartsFactory.getXYChart(series, map, "Fitness in iterations", "Iteration", "Fitness", true);

    }

    public static ChartPanel getEvaluationsVarianceEstimatePlot(OptResults res, String killerNamePrefix) {

        HashMap<String, LinkedList<double[]>> data = new HashMap<String, LinkedList<double[]>>();
        LinkedList<String> seriesNames = new LinkedList<String>();

        int n = 0;
        for (DuelEvalResults r : res.iterResults) {
            BotStatistic stats = r.stats;

            HashMap<String, Integer> scores = new HashMap<String, Integer>();
            for (Kill k : stats.kills) {
                if (scores.containsKey(k.killer)) {
                    continue;
                }
                if (k.killer.startsWith(killerNamePrefix)) {
                    int score = StatsTools.getBotScore(k.killer, stats) - StatsTools.getBotScore(k.victim, stats);
                    scores.put(k.killer, score);
                }
            }

            double avg = 0;
            for (int score : scores.values()) {
                avg += score;
            }
            avg = avg / scores.values().size();

            LinkedList<double[]> varData = new LinkedList<double[]>();
            int i = 0;
            double cVarEst = 0;
            for (int score : scores.values()) {
                double var = (score - avg) * (score - avg);
                cVarEst += var;
                if (i != 0) {
                    varData.add(new double[]{i, cVarEst / i});
                }
                i++;
            }


            seriesNames.add(r.shortName);
            data.put(r.shortName, varData);
            n++;
        }
        return getXYChart(seriesNames, data, "Evaluations variance in function of repetitions", "Game repetitions in each evaluation", "Variance", false);

    }

    public static ChartPanel getEvaluationAvgVarianceInRepetitionsPlot(OptResults res, String killerNamePrefix) {

        HashMap<String, LinkedList<double[]>> data = new HashMap<String, LinkedList<double[]>>();
        LinkedList<String> seriesNames = new LinkedList<String>();

        LinkedList<String> finalSeriesNames = new LinkedList<String>();
        HashMap<String, LinkedList<double[]>> finalPlotData = new HashMap<String, LinkedList<double[]>>();


        int minDataSize = Integer.MAX_VALUE;

        int n = 0;
        for (DuelEvalResults r : res.iterResults) {
            BotStatistic stats = r.stats;

            HashMap<String, Integer> scores = new HashMap<String, Integer>();
            for (Kill k : stats.kills) {
                if (scores.containsKey(k.killer)) {
                    continue;
                }
                if (k.killer.startsWith(killerNamePrefix)) {
                    int score = StatsTools.getBotScore(k.killer, stats) - StatsTools.getBotScore(k.victim, stats);
                    scores.put(k.killer, score);
                }
            }

            double avg = 0;
            for (int score : scores.values()) {
                avg += score;
            }
            avg = avg / scores.values().size();

            LinkedList<double[]> varData = new LinkedList<double[]>();
            int i = 0;
            double cVarEst = 0;
            for (int score : scores.values()) {
                double var = (score - avg) * (score - avg);
                cVarEst += var;
                if (i != 0) {
                    varData.add(new double[]{i, cVarEst / i});
                }
                i++;
            }


            seriesNames.add(r.shortName);
            data.put(r.shortName, varData);
            if (minDataSize > varData.size()) {
                minDataSize = varData.size();
            }
            n++;
        }
        LinkedList<double[]> varData = new LinkedList<double[]>();

        for (int e = 0; e < minDataSize; e++) {
            double avgVariance = 0;
            for (String s : seriesNames) {
                avgVariance += data.get(s).get(e)[1];
            }
            avgVariance /= seriesNames.size();
            varData.add(new double[]{e+1, avgVariance});
        }

        String sn = "avg variance";
        finalSeriesNames.add(sn);
        finalPlotData.put(sn, varData);

        ChartPanel p = getXYChart(finalSeriesNames, finalPlotData, "", "N", "Variance", false);
        formatChart(p, 2);
        saveChartAsEMF(p, "variance-in-n.emf", 640, 400);
        return p;

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
            String title, String xLabel, String yLabel, boolean legend) {

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
                legend, true, true);


        ChartPanel cp = new ChartPanel(c);
        return cp;

    }

    public static void formatChart(ChartPanel p, float we) {
        formatChart(p, new LinkedList<float[]>(), we);
    }

    public static void formatChart(ChartPanel p, LinkedList<float[]> dashes, float we) {

        ChartColor[] myChartColors = new ChartColor[]{
            new ChartColor(0, 0, 255), //intensive blue
            new ChartColor(200, 0, 0), //dark red
            new ChartColor(128, 255, 128), //pastel green
            new ChartColor(255, 128, 0), //orange            
            new ChartColor(128, 255, 255), //v light blue
            new ChartColor(0, 128, 0) //dark green
        };

        JFreeChart c = p.getChart();

        Font f = new Font("Serif", Font.BOLD, 14);
        if (c.getLegend() != null) {
            c.getLegend().setItemFont(f);
            c.getLegend().setBorder(BlockBorder.NONE);
            c.getLegend().setPosition(RectangleEdge.RIGHT);
            c.getLegend().setItemLabelPadding(new RectangleInsets(1, 15, 1, 15));
        }
        c.getXYPlot().getDomainAxis().setLabelFont(f);
        c.setBackgroundPaint(Color.WHITE);
        c.getXYPlot().setBackgroundPaint(Color.WHITE);


//        c.getXYPlot().getRenderer().setBaseOutlineStroke(s);
//        c.getXYPlot().getRenderer().setStroke(s);

        XYPlot pl = c.getXYPlot();

        for (int line = 0; line < pl.getSeriesCount(); line++) {
            if (dashes.size() > line && dashes.get(line) != null) {
                float[] dash = dashes.get(line);
                Stroke s1 = new BasicStroke(we, BasicStroke.CAP_BUTT, 1, BasicStroke.JOIN_BEVEL, dash, 0);
                pl.getRenderer().setSeriesStroke(line, s1);
            } else {
                Stroke s = new BasicStroke(we, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                pl.getRenderer().setSeriesStroke(line, s);
            }
            if (line < myChartColors.length) {
                pl.getRenderer().setSeriesPaint(line, myChartColors[line]);
            }
        }

//        c.getXYPlot().getRenderer().setOutlineStroke(new BasicStroke(0f));
//        c.getXYPlot().getRenderer().setBaseOutlineStroke(new BasicStroke(0f));

//        c.setBorderStroke(new BasicStroke(0f));
//        c.setBorderVisible(false);

        pl.rendererChanged(new RendererChangeEvent(pl.getRenderer(), true));

    }

    public static void saveChartAsEMF(ChartPanel c, String fName, int w, int h) {
        try {
//        ExportDialog exp = new ExportDialog();
//        exp.showExportDialog(c, "Export...", c, new Dimension(800, 600), fName);
            Properties p = new Properties();
            p.setProperty("PageSize", "A4");
//    VectorGraphics g = new PSGraphics2D(new File("Output.eps", new Dimension(400,300));
//            VectorGraphics g = new PSGraphics2D(new File(fName), new Dimension(800, 600));
            VectorGraphics g = new EMFGraphics2D(new File(fName), new Dimension(w, h));
            g.setProperties(p);
            g.startExport();
//            c.print(g);
            c.getChart().draw(g, new Rectangle2D.Double(0, 0, w, h));
            g.endExport();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StatsChartsFactory.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
