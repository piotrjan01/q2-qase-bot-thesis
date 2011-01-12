/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author piotrrr
 */
public class SimulatedAnnealingProcess extends TuningProcessBase {

    public static final boolean continueAfterVisitedAllNeighbours = true;
    private double t0 = 25.0;
    private double t = t0;
    private int step = 0;
    private double lambda = 0.914;
    private int initialBudget = 10;
    private int budget = initialBudget;
    double[] pert = null;
    double variance = 0.02;
    private final double tau = 2;


    public SimulatedAnnealingProcess(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
        tauThreshold = tau;
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {

        decreaseTemperature();
        List<NavConfig> ret = new LinkedList<NavConfig>();

        if (visited.size() == 0) {
            ret.add(best);
            return ret;
        }

        if (visited.size() == 1) {
            pert = generateRandomVectorPerturbation(best.getParamsCount());
            ret.add(generateLocallyRandomNeighbur(best, pert));
            return ret;
        }

        NavConfig neew = visited.getLast();
        NavConfig curr = best;
        NavConfig toAdd = null;

        double neewScore = resultsOfVisited.getLast();
        double currScore = bestScore;

        String info = "currentScore=" + currScore + " newScore=" + neewScore + " t=" + t;



        if (neewScore == currScore) {
            //we take the new one
            pert = generateRandomVectorPerturbation(best.getParamsCount());
            toAdd = generateLocallyRandomNeighbur(neew, pert);
            info += " found new better config";
        } else {
            double p = getWorseChoiceProbability(neewScore, currScore);
            info += " p=" + p;
            if (rand.nextDouble() < p) {
                //we take the worse one - new
                pert = generateRandomVectorPerturbation(best.getParamsCount());
                toAdd = generateLocallyRandomNeighbur(neew, pert);
                info += " went for worse new config";
            } else { //we take the better - current
                if (pert != null) {
//                    negatePerturbation(pert);
                    toAdd = generateLocallyRandomNeighbur(curr);
                    info += " went for better current config. using -perturbation.";
                    pert = null;
                } else {
                    pert = generateRandomVectorPerturbation(best.getParamsCount());
                    toAdd = generateLocallyRandomNeighbur(curr, pert);
                    info += " went for better current config";
                }
            }
        }

        if (toAdd == null) {
            return generateNextSet(best);
        }

        toAdd.additionalInfo += info;
        ret.add(toAdd);
        return ret;

    }

    private void decreaseTemperature() {
        budget--;
        if (budget == 0) {
            budget = initialBudget;
            step++;
//            t = t0 / Math.log(step);
            t = t*lambda;
        }
    }

    private double getWorseChoiceProbability(double val1, double val2) {
        double diff = Math.abs(val1 - val2);
        return Math.exp(-diff / t);
    }

    private double[] generateRandomVectorPerturbation(int vSize) {
        double[] p = new double[vSize];
        for (int i = 0; i < vSize; i++) {
                p[i] = getRandomNumber(0, variance);
        }
//        p[rand.nextInt(vSize)] = getRandomNumber(0, variance);
        return p;
    }

    private void negatePerturbation(double[] pert) {
        for (int i = 0; i < pert.length; i++) {
            pert[i] = -pert[i];
        }
    }

    private NavConfig generateLocallyRandomNeighbur(NavConfig from, double[] perturbation) {
        NavConfig ret = new NavConfig(from);
        int n = ret.getParamsCount();
        for (int i = 0; i < n; i++) {
            ret.addToParam(i, perturbation[i]);
        }
        return ret;
    }

    private NavConfig generateLocallyRandomNeighbur(NavConfig from) {
        double[] p = generateRandomVectorPerturbation(from.getParamsCount());
        return generateLocallyRandomNeighbur(from, p);
    }

    private double getRandomNumber(double mean, double variance) {
        return mean + rand.nextGaussian() * variance;
    }
}
