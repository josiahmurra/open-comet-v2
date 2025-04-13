/**
* Comet.java
* Created in 2012 by Benjamin Gyori & updated in 2025 by Josiah Murray
* National University of Singapore & Medical College of Wisconsin
* e-mail: ben.gyori@gmail.com & jmurray@mcw.edu
*
* Comet.java defines the Comet class used to represent a
* comet with its parameters and status flag.
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
* When you use this plugin for your work, please cite
* Gyori BM, Venkatachalam G, et al. OpenComet: An automated tool for 
* comet assay image analysis

* You should have received a copy of the GNU General Public License
* along with this plugin; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/


import ij.gui.Roi;

public class Comet {
    public Comet(Roi roi){
        this.cometRoi = (Roi)roi.clone();
        this.status = Comet.VALID;
    } 

    public int id;
    public int status;
    public static final int VALID = 1;
    public static final int INVALID_NO_HEAD = 2;  // Critical - can't calculate stats
    public static final int INVALID_SIZE = 4;     // Non-critical - can still calculate stats
    public static final int OUTLIER = 8;
    public static final int DELETED = 16;

    public boolean canCalculateStats() {
        return status == VALID || status == INVALID_SIZE || status == OUTLIER;
    }

    public Roi cometRoi;
    public Roi headRoi;
    public Roi oldRoi;

    // Position in image
    public int x,y, height, width;

    // Profiles
    public double[] cometProfile;
    public double[] bgProfile;
    public double[] headProfile;
    public double[] tailProfile;
    public double profileMax;

    // Output measurements
    public double cometArea;
    public double cometIntensity;
    public double cometLength;
    public double cometDNA;
    public double headArea;
    public double headIntensity;
    public double headLength;
    public double headDNA;
    public double headDNAPercent;
    public int headCentroid;
    public double tailArea;
    public double tailLength;
    public double tailIntensity;
    public double tailDNA;
    public double tailDNApercent;
    public int tailCentroid;
    public double tailMoment;
    public double tailOliveMoment;

    // Internal parameters
    public int[] histogram;
    public double area;
    public double areaConvexHull;
    public double mean;
    public double convexity;
    public double symmetry;
    public double perimeter;
    public double circularity;
    public double centerlineDiff;
    public double hratio;
    public int headFrontCenterY; // are these needed?
    public int headRoiCenterY;

    public String getMeasurementString(String sep){
        String flagStr;
        if(status == VALID)
            flagStr = "normal";
        else if (status == INVALID_SIZE)
            flagStr = "size_invalid";
        else if (status == INVALID_NO_HEAD)
            flagStr = "no_head";
        else if (status == OUTLIER)
            flagStr = "outlier";
        else if (status == DELETED)
            flagStr = "deleted";
        else
            return "";

        String result = id + sep
                + flagStr + sep 
                + cometLength + sep 
                + headLength + sep 
                + tailLength + sep 
                + headArea + sep 
                + tailArea + sep 
                + cometArea + sep
                + headIntensity + sep 
                + tailIntensity + sep 
                + cometIntensity + sep 
                + headDNA + sep 
                + tailDNA + sep 
                + cometDNA + sep 
                + headDNAPercent + sep
                + tailDNApercent + sep
                + tailMoment + sep
                + tailOliveMoment;
        return result;
    }
}
