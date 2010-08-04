/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.common.stats;

import java.util.LinkedList;
import java.util.TreeMap;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import piotrrr.thesis.common.stats.BotStatistic.Kill;
import piotrrr.thesis.common.stats.BotStatistic.Reward;

/**
 *
 * @author Piotr Gwizdała
 */
public class StatsChartsFactory {

    public static int rewardsChartSegments = 500;

    public static int avgRewardsChartSegments = 10;

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
                if (k.killer.startsWith(s.botName)) {
                    s.int1++;
                    s.series.add(k.time / 10, (double) s.int1 / s.int2);
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

    public static ChartPanel getDeathsInTimeByBotType(BotStatistic stats) {
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
                    s.int1++;
                    s.series.add(k.time / 10, (double) s.int1 / s.int2);
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

    public static ChartPanel getKillsPerEachDeathByBotType(BotStatistic stats) {
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

    public static ChartPanel getKillsInTimeByBot(BotStatistic stats) {

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

        TreeMap<String, TreeMap<String, Integer>> map = new TreeMap<String, TreeMap<String, Integer>>();

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

        for (String bn : map.keySet()) {
            TreeMap<String, Integer> usage = map.get(bn);
            for (String wpn : usage.keySet()) {
                ds.addValue((Number) usage.get(wpn), wpn, bn);
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

    public static ChartPanel getWhoKillsWhomBarChart(BotStatistic stat) {

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

    public static ChartPanel getRewardsInTimeByEachBot(BotStatistic stats) {

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

            int segmentSize = stats.rewards.size() / rewardsChartSegments;
            if (segmentSize < 1) {
                segmentSize = 1;
            }

            int i = 0;

            for (BotStatistic.Reward k : stats.rewards) {
                for (BotSeries s : series) {
                    if (k.reward != 0 && s.botName.equals(k.botName)) {
                        s.d1 += k.reward;
                        s.int1 = k.time;
                        break;
                    }
                }
                BotSeries as = series.getLast();
                as.d1 += k.reward / botsNum;

                i++;
                if (i % segmentSize == 0) {
                    for (BotSeries s : series) {
                        s.series.add(s.int1 / 10, s.d1);
                    }
                }


            }
        }

        for (BotSeries s : series) {
            ds.addSeries(s.series);
        }

        JFreeChart c = ChartFactory.createXYLineChart(
                "Rewards by each bot in time",
                "time [s]",
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

    public static ChartPanel getAvgRewardsChart(BotStatistic stats) {
        XYSeriesCollection ds = new XYSeriesCollection();

        LinkedList<BotSeries> series = new LinkedList<BotSeries>();

        for (String botName : stats.getAllRewardedBotNames()) {
            series.add(new BotSeries(new XYSeries(botName), 0, 0, botName));
        }

        String allName = "all RL";
        int botsNum = stats.getAllRewardedBotNames().size();
        series.add(new BotSeries(new XYSeries(allName), 0, 0, allName));

        int segmentSize = stats.rewards.size() / avgRewardsChartSegments;
        if (segmentSize < 1) {
            segmentSize = 1;
        }

        int i = 0;

        for (Reward k : stats.rewards) {
            for (BotSeries s : series) {
                if (k.botName.equals(s.botName)) {
                    s.d1 += k.reward;
                }
            }

            i++;

            if (i % segmentSize == 0) {
                BotSeries as = series.getLast();
                for (BotSeries s : series) {
                    if (s.botName.equals(as.botName)) continue;
                    s.series.add(k.time-segmentSize/2 / 10, s.d1 / segmentSize);
                    as.d1 += s.d1;
                    s.d1 = 0;
                }
                as.series.add(k.time-segmentSize/2 / 10, as.d1 / (botsNum * segmentSize));
                as.d1 = 0;
            }


        }

        for (BotSeries s : series) {
            ds.addSeries(s.series);
        }

        JFreeChart c = ChartFactory.createXYLineChart(
                "avg reward gaining speed",
                "time [s]",
                "avg reward gaining speed",
                ds,
                PlotOrientation.VERTICAL,
                true, true, true);

        ChartPanel cp = new ChartPanel(c);
        return cp;

    }
}
