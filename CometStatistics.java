/**
* CometStatistics.java
* Created in 2012 by Benjamin Gyori & updated in 2025 by Josiah Murray
* National University of Singapore & Medical College of Wisconsin
* e-mail: ben.gyori@gmail.com & jmurray@mcw.edu
*
* When you use this plugin for your work, please cite
* Gyori BM, Venkatachalam G, et al. OpenComet: An automated tool for
* comet assay image analysis
*
* CometAnalyzer.java is the main internal class for comet analysis.
* cometAnalyzerRun takes an image as input. It analyzes the image
* and extracts comets. The comet head and tail are segmented and
* measurements are calculated. The resulting Comet objects are returned.
*
* This plugin is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3
* as published by the Free Software Foundation.
*
* This work is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this plugin; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
import ij.IJ;
import ij.ImagePlus;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class CometStatistics {
    private HashMap<ImagePlus,Comet[]> Comets;
    Statistics cometAreaStats, cometIntensityStats;
    Statistics cometLengthStats, cometDNAStats;
    Statistics headAreaStats, headIntensityStats, headLengthStats;
    Statistics headDNAStats, headDNAPercentStats;
    Statistics tailAreaStats, tailIntensityStats, tailLengthStats;
    Statistics tailDNAStats, tailDNAPercentStats;
    Statistics tailMomentStats, tailOliveMomentStats;

    public CometStatistics(HashMap<ImagePlus,Comet[]> Comets, int flag){
        this.Comets = Comets;
        cometAreaStats = new Statistics();
        cometIntensityStats = new Statistics(); 
        cometLengthStats = new Statistics();
        cometDNAStats = new Statistics();
        headAreaStats = new Statistics();
        headIntensityStats = new Statistics();
        headLengthStats = new Statistics();
        headDNAStats = new Statistics();
        headDNAPercentStats = new Statistics();
        tailAreaStats = new Statistics();
        tailIntensityStats = new Statistics();
        tailLengthStats = new Statistics();
        tailDNAStats = new Statistics();
        tailDNAPercentStats = new Statistics();
        tailMomentStats = new Statistics();
        tailOliveMomentStats = new Statistics();
        for (Map.Entry<ImagePlus,Comet[]> element : Comets.entrySet()) {
                Comet[] imageComets = element.getValue();
                for(int i=0;i<imageComets.length;i++){
                    if((imageComets[i].status & flag)!=0){
                        cometAreaStats.add(imageComets[i].cometArea);
                        cometIntensityStats.add(imageComets[i].cometIntensity);
                        cometLengthStats.add(imageComets[i].cometLength);
                        cometDNAStats.add(imageComets[i].cometDNA);
                        headAreaStats.add(imageComets[i].headArea);
                        headIntensityStats.add(imageComets[i].headIntensity);
                        headLengthStats.add(imageComets[i].headLength);
                        headDNAStats.add(imageComets[i].headDNA);
                        headDNAPercentStats.add(imageComets[i].headDNAPercent);
                        tailAreaStats.add(imageComets[i].tailArea);
                        tailIntensityStats.add(imageComets[i].tailIntensity);
                        tailLengthStats.add(imageComets[i].tailLength);
                        tailDNAStats.add(imageComets[i].tailDNA);
                        tailDNAPercentStats.add(imageComets[i].tailDNApercent);
                        tailMomentStats.add(imageComets[i].tailMoment);
                        tailOliveMomentStats.add(imageComets[i].tailOliveMoment);
                    }
                }
            }
        }

    public String getStatisticsString(String sep, String group){
        String outStr;
        outStr = "Mean" + sep + sep + group + sep + 
                cometAreaStats.getMean() + sep +
                cometIntensityStats.getMean() + sep + 
                cometLengthStats.getMean() + sep + 
                cometDNAStats.getMean() + sep + 
                headAreaStats.getMean() + sep + 
                headIntensityStats.getMean() + sep + 
                headLengthStats.getMean() + sep + 
                headDNAStats.getMean() + sep + 
                headDNAPercentStats.getMean() + sep + 
                tailAreaStats.getMean() + sep + 
                tailIntensityStats.getMean() + sep + 
                tailLengthStats.getMean() + sep + 
                tailDNAStats.getMean() + sep + 
                tailDNAPercentStats.getMean() + sep + 
                tailMomentStats.getMean() + sep + 
                tailOliveMomentStats.getMean() + sep + "\n";

        outStr += "Median" + sep+ sep + group + sep + 
                cometAreaStats.getMedian() + sep +
                cometIntensityStats.getMedian() + sep + 
                cometLengthStats.getMedian() + sep + 
                cometDNAStats.getMedian() + sep + 
                headAreaStats.getMedian() + sep + 
                headIntensityStats.getMedian() + sep + 
                headLengthStats.getMedian() + sep + 
                headDNAStats.getMedian() + sep + 
                headDNAPercentStats.getMedian() + sep + 
                tailAreaStats.getMedian() + sep + 
                tailIntensityStats.getMedian() + sep + 
                tailLengthStats.getMedian() + sep + 
                tailDNAStats.getMedian() + sep + 
                tailDNAPercentStats.getMedian() + sep + 
                tailMomentStats.getMedian() + sep + 
                tailOliveMomentStats.getMedian() + sep + "\n";

        outStr += "Stddev" + sep + sep + group + sep + 
                cometAreaStats.getStddev() + sep +
                cometIntensityStats.getStddev() + sep + 
                cometLengthStats.getStddev() + sep + 
                cometDNAStats.getStddev() + sep + 
                headAreaStats.getStddev() + sep + 
                headIntensityStats.getStddev() + sep + 
                headLengthStats.getStddev() + sep + 
                headDNAStats.getStddev() + sep + 
                headDNAPercentStats.getStddev() + sep + 
                tailAreaStats.getStddev() + sep + 
                tailIntensityStats.getStddev() + sep + 
                tailLengthStats.getStddev() + sep + 
                tailDNAStats.getStddev() + sep + 
                tailDNAPercentStats.getStddev() + sep + 
                tailMomentStats.getStddev() + sep + 
                tailOliveMomentStats.getStddev() + sep + "\n";

        outStr += "Min" + sep + sep + group + sep + 
                cometAreaStats.getMin() + sep +
                cometIntensityStats.getMin() + sep + 
                cometLengthStats.getMin() + sep + 
                cometDNAStats.getMin() + sep + 
                headAreaStats.getMin() + sep + 
                headIntensityStats.getMin() + sep + 
                headLengthStats.getMin() + sep + 
                headDNAStats.getMin() + sep + 
                headDNAPercentStats.getMin() + sep + 
                tailAreaStats.getMin() + sep + 
                tailIntensityStats.getMin() + sep + 
                tailLengthStats.getMin() + sep + 
                tailDNAStats.getMin() + sep + 
                tailDNAPercentStats.getMin() + sep + 
                tailMomentStats.getMin() + sep + 
                tailOliveMomentStats.getMin() + sep + "\n";

        outStr += "Max" + sep + sep + group + sep + 
                cometAreaStats.getMax() + sep +
                cometIntensityStats.getMax() + sep + 
                cometLengthStats.getMax() + sep + 
                cometDNAStats.getMax() + sep + 
                headAreaStats.getMax() + sep + 
                headIntensityStats.getMax() + sep + 
                headLengthStats.getMax() + sep + 
                headDNAStats.getMax() + sep + 
                headDNAPercentStats.getMax() + sep + 
                tailAreaStats.getMax() + sep + 
                tailIntensityStats.getMax() + sep + 
                tailLengthStats.getMax() + sep + 
                tailDNAStats.getMax() + sep + 
                tailDNAPercentStats.getMax() + sep + 
                tailMomentStats.getMax() + sep + 
                tailOliveMomentStats.getMax() + sep + "\n";

        return outStr;
    }

    public Map<String, Double> getStatistics() {
        Map<String, Double> stats = new HashMap<>();
        int validCount = 0;
        int sizeInvalidCount = 0;
        
        // Get all comets from the HashMap
        for (Map.Entry<ImagePlus,Comet[]> entry : Comets.entrySet()) {
            Comet[] imageComets = entry.getValue();
            for (Comet comet : imageComets) {
                if (comet.canCalculateStats()) {
                    validCount++;
                    if (comet.status == Comet.INVALID_SIZE) {
                        sizeInvalidCount++;
                    }
                    // Calculate statistics for valid and size-invalid comets
                    cometAreaStats.add(comet.cometArea);
                    cometIntensityStats.add(comet.cometIntensity);
                    cometLengthStats.add(comet.cometLength);
                    cometDNAStats.add(comet.cometDNA);
                    headAreaStats.add(comet.headArea);
                    headIntensityStats.add(comet.headIntensity);
                    headLengthStats.add(comet.headLength);
                    headDNAStats.add(comet.headDNA);
                    headDNAPercentStats.add(comet.headDNAPercent);
                    tailAreaStats.add(comet.tailArea);
                    tailIntensityStats.add(comet.tailIntensity);
                    tailLengthStats.add(comet.tailLength);
                    tailDNAStats.add(comet.tailDNA);
                    tailDNAPercentStats.add(comet.tailDNApercent);
                    tailMomentStats.add(comet.tailMoment);
                    tailOliveMomentStats.add(comet.tailOliveMoment);
                }
            }
        }
        
        stats.put("Total Valid Count", (double)validCount);
        stats.put("Size Invalid Count", (double)sizeInvalidCount);
        return stats;
    }

    private class Statistics {
        public Statistics(){
            valueList = new ArrayList<Double>();
            updated = false;
            }

        public void add(double val){
            valueList.add(val);
            updated = false;
            }

        public double getMean(){
            if(!updated) updateStatistics();
            return mean;
            }

        public double getMedian(){
            if(!updated) updateStatistics();
            return median;
            }

        public double getStddev(){
            if(!updated) updateStatistics();
            return stddev;
        }

        public double getMin(){
            if(!updated) updateStatistics();
            return minval;
        }

        public double getMax(){
            if(!updated) updateStatistics();
            return maxval;
        }

        private void updateStatistics(){
            Collections.sort(valueList);


            int nElem = valueList.size();

            if(nElem == 1){
                mean = median = minval = maxval = valueList.get(0);
                stddev = 0;
                updated = true;
            }
            else {
                mean = median = stddev = 0;
                minval = valueList.get(0);
                maxval = valueList.get(nElem-1);

                median = (nElem %2 ==1) ? (valueList.get((nElem+1)/2-1)) : 
                     (((valueList.get((nElem+1)/2-1))) + 
                     (valueList.get((nElem+1)/2-1)) / 2.0); 

                double sum1=0.0,sum2=0.0;

                Iterator it=valueList.iterator();
                int i = 0;
                while(it.hasNext()){
                    i++;
                    double val= (Double)it.next();
                    sum1 += val;
                    sum2 += val*val;
                    IJ.log(i+": "+val);
                    }
                mean = sum1/nElem;
                stddev = Math.sqrt((sum2-(sum1*sum1)/nElem)/(nElem-1));
                IJ.log("Mean: "+mean+" Sum1: "+sum1+" n:"+nElem);
            } 
            updated = true;
        }

        private double mean, median, stddev, minval, maxval;
        private ArrayList<Double> valueList;
        private boolean updated;

    }
}
