/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.tools;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import piotrrr.thesis.bots.tuning.ConfigEvaluator;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsChartsFactory;
import piotrrr.thesis.common.stats.StatsTools;

/**
 *
 * @author piotrrr
 */
public class PlottingTool {

    private static Random rand = new Random();

    private static double noisePower = 0.5;

    private static void errorRepetitionsPlot() {
        LinkedList<String> seriesNames = new LinkedList<String>();
        HashMap<String, LinkedList<double[]>> data = new HashMap<String, LinkedList<double[]>>();
        LinkedList<float[]> dashes = new LinkedList<float[]>();
        LinkedList<double[]> line1 = new LinkedList<double[]>();
        LinkedList<double[]> line2 = new LinkedList<double[]>();
        double a = Double.NaN;
        for (int i = 1; i < 200; i += 3) {
            double mes = getAvgAvgMeasurementsError(50, i);
            line1.add(new double[]{i, mes});
            double sqrt = 1 / Math.sqrt(i);
            if (Double.isNaN(a)) {
                a = mes / sqrt;
            }
            line2.add(new double[]{i, a * sqrt});
            System.out.println("i=" + i);
        }
        data.put("a/sqrt(N)", line2);
        seriesNames.add("a/sqrt(N)");
        dashes.add(new float[]{3f});
        data.put("error", line1);
        seriesNames.add("error");
        dashes.add(null);
        ChartPanel chart = StatsChartsFactory.getXYChart(seriesNames, data, "", "N", "Error", true);
        StatsChartsFactory.formatChart(chart, dashes, 2);
        ChartFrame frame = new ChartFrame("Error in function of repetitions", chart.getChart());
        frame.pack();
        StatsChartsFactory.saveChartAsEMF(chart, "test.emf", 700, 400);
        frame.setVisible(true);
    }

    private static double getFunctionValue(double x) {
        return Math.sin(x);
    }

    private static double getFunctionValueWithNoise(double x) {
        return getFunctionValue(x)+noisePower*(rand.nextDouble()-0.5);
    }    

    private static double getAvgMeasurementsError(int reps) {
        double noisy = 0;
        double x = 10*rand.nextDouble();
        for (int i=0; i<reps; i++) {
            noisy+=getFunctionValueWithNoise(x);
        }
        noisy/=reps;
        return Math.abs(getFunctionValue(x)-noisy);
    }


    private static double getAvgAvgMeasurementsError(int reps1, int reps2) {
        double avg=0;
        for (int i=0; i<reps1; i++) {
            avg+=getAvgMeasurementsError(reps2);
        }
        avg/=reps1;
        return avg;
    }

    public static void main(String[] args) {
//        errorRepetitionsPlot();
        initialExperimentsPlot();
    }

    public static void initialExperimentsPlot() {
        LinkedList<BotStatistic> stats = new LinkedList<BotStatistic>();
        String path = "H:\\workspace\\NetBeans\\Thesis\\the latest experiments\\vs eraser\\";

        String b1 = "LearnBot";
        String b2 = "EraserBot";

        String alg = "init2";
        int firstn = 1;
        int lastn = 6;

        BotStatistic all = new BotStatistic();

        for (int i=firstn; i<lastn+1; i++) {

            String name = alg+"-"+i;

            BotStatistic s = null;
            try {
                s = (BotStatistic) CommFun.readFromFile(path + name);
            } catch (Exception ex) {
                Logger.getLogger(PlottingTool.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            stats.add(s);

            all.kills.addAll(s.kills);

            Dbg.prn(b1+" score: "+StatsTools.getBotScore(b1, s));
            Dbg.prn(b2+" score: "+StatsTools.getBotScore(b2, s));
        }

        ChartPanel p = StatsChartsFactory.getKillsInTimeByBotTypeSorting(all);
        StatsChartsFactory.formatChart(p, 3);

        StatsChartsFactory.saveChartAsEMF(p, alg+".emf", 640, 400);

        ChartFrame f = new ChartFrame("With EraserBot", p.getChart());
        f.pack();
        f.setVisible(true);
    }

    


}
