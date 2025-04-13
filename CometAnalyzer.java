/**
* CometAnalyzer.java
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
import ij.gui.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.measure.Measurements;

import java.awt.*;
import java.util.Arrays;
import java.util.Vector;

public class CometAnalyzer {
    public static int COMETFIND_BGCORRECT = 1;
    public static int HEADFIND_AUTO = 2;
    public static int HEADFIND_PROFILE = 4;
    public static int HEADFIND_BRIGHTEST = 8;
    private int activeChannel;
    private String thresholdMethod = "Triangle"; // Default threshold method

    public void setThresholdMethod(String method) {
        this.thresholdMethod = method;
    }

    public Comet[] cometAnalyzerRun(ImagePlus img_orig, int cometOptions) {
    // ----- Setting up given image
        ImageProcessor ip = img_orig.getProcessor();
        // Type of original image
        int imgOriginalType = img_orig.getType();
        // Make a grayscale template from the imate
        ByteProcessor ip_gs_template = getGrayscaleCopy(ip,imgOriginalType);
        // Make two copies of the grayscale image
        ByteProcessor ip_gs = getGrayscaleCopy(ip_gs_template,ImagePlus.GRAY8);
        ByteProcessor ip_gs2 = getGrayscaleCopy(ip_gs_template,ImagePlus.GRAY8);
        //-----------------------------

        ImagePlus img_gs = new ImagePlus("tmpimg",ip_gs);

        //----- Global background correction-----
        if((cometOptions & COMETFIND_BGCORRECT)!=0){
            RankFilters rf = new RankFilters();
            rf.rank(ip_gs, 10.0, RankFilters.MEDIAN);

            BackgroundSubtracter bSub = new BackgroundSubtracter();
            double radiusRollingBall = Math.min(ip_gs.getHeight(),ip_gs.getWidth())*0.3;
            bSub.rollingBallBackground(ip_gs, radiusRollingBall, false,
                                    false, false, false, true);
        }
    //----------------------------------------
    // ----- First round of Comet finding ------------
        Vector<Comet> Comets = new Vector<Comet>();

        // Threshold finding
        ip_gs.setAutoThreshold(thresholdMethod, true, ImageProcessor.BLACK_AND_WHITE_LUT);
        // Binarization
        double threshValue = ip_gs.getMinThreshold();
        setThreshold(ip_gs, (int)threshValue);
        // Morphology
        open_ntimes(ip_gs,3,0);
        // Setup particle analyzer
        int paopts = ParticleAnalyzer.SHOW_NONE |
                    ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES |
                    ParticleAnalyzer.INCLUDE_HOLES;
        CometParticleAnalyzer pa =
                new CometParticleAnalyzer(paopts,0,null,400,Double.POSITIVE_INFINITY,0,1);
        // Run particle finding
        pa.analyze(img_gs,ip_gs);
        // Get ROIs
        Roi[] cometRois = pa.getCometRois();
        IJ.log("Number of ROIs found: "+cometRois.length);
        // Add ROIs as comets
        for(int i=0;i<cometRois.length;i++){
            Comets.add(new Comet(cometRois[i]));
            }
        // Calculate comet parameters
        for(int i=0; i<Comets.size(); i++){
            setCometParams(Comets.get(i),ip_gs2);
            }

        for(int i=0;i<Comets.size();i++){
            Comet comet = Comets.get(i);
            IJ.log(comet.convexity+","+comet.centerlineDiff);
            }
        // Set validity status of each comet based on parameters
        int validCount = setValidity(Comets,ip_gs2);
        // If there are no valid comets, stop
        if(validCount==0){
            IJ.log("No valid comets found.");
            return null;
            }


        //---Find more comets-----------------
        /*for(int t = (int)threshValue;t<255;t+=10){
            IJ.log("Thresh value:"+(int)threshValue+ " t: "+t);


            ImageProcessor ipTmp = getGrayscaleCopy(ip,imgOriginalType);
            ImagePlus imgTmp = new ImagePlus("imgTmp",ipTmp);

            if((cometOptions & COMETFIND_BGCORRECT)!=0){
                RankFilters rf = new RankFilters();
                rf.rank(ipTmp, 10.0, RankFilters.MEDIAN);

                BackgroundSubtracter bSub = new BackgroundSubtracter();
                double radiusRollingBall = Math.min(ipTmp.getHeight(),ipTmp.getWidth())*0.3;
                bSub.rollingBallBackground(ipTmp, radiusRollingBall, false,
                                        false, false, false, true);
            }


            setThreshold(ipTmp,t);
            ipTmp.invertLut();
            open_ntimes(ipTmp, 3, 1);

            imgTmp.show();

            int paTmpOpts = ParticleAnalyzer.SHOW_NONE | ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES | ParticleAnalyzer.INCLUDE_HOLES;
            CometParticleAnalyzer paTmp = new CometParticleAnalyzer(paTmpOpts,0,null,200,Double.POSITIVE_INFINITY,0,1);
            paTmp.analyze(imgTmp,ipTmp);
            Roi[] cometRoisTmp = paTmp.getCometRois();
            IJ.log("Number of ROIs found: "+cometRoisTmp.length);


            Vector<Roi> addRois = new Vector<Roi>();
            Vector<Integer> addRoisIdx = new Vector<Integer>();
            Vector<Comet> removeComets = new Vector<Comet>();
            for(int j=0;j<Comets.size();j++){
                Comet comet = Comets.get(j);
                if(comet.status==Comet.INVALID_NO_HEAD){
                    addRoisIdx.clear();
                    for(int i=0;i<cometRoisTmp.length;i++){
                        Rectangle br = cometRoisTmp[i].getBounds();
                        int xc = br.x + br.width/2;
                        int yc = br.y + br.height/2;
                        if(comet.cometRoi.contains(xc,yc)){
                            addRoisIdx.add(i);
                        }
                    }
                    if(addRoisIdx.size()>=2){
                        removeComets.add(comet);
                        for(int k=0;k<addRoisIdx.size();k++){
                            addRois.add(cometRoisTmp[addRoisIdx.get(k)]);
                        }
                    }
                }
            }
            for(int i=0;i<removeComets.size();i++){
                Comets.remove(removeComets.get(i));
            }
            for(int i=0;i<addRois.size();i++){
                Comet newComet = new Comet(addRois.get(i));
                setCometParams(newComet,ip_gs2);
                newComet.status = Comet.VALID;
                Comets.add(newComet);
            }
            setValidity(Comets,ip_gs2);
        }*/
        //------------------------------------

        // --- Area and height statistics
        double meanArea = 0, stdArea = 0;
        double meanHeight = 0, stdHeight = 0;
        for(int i=0; i<Comets.size(); i++){
            Comet comet = Comets.get(i);
            if(comet.status == Comet.VALID){
                meanHeight += comet.height;
                meanArea += comet.area;
                }
            }
        meanArea /= validCount;
        meanHeight /= validCount;

        for(int i=0; i<Comets.size(); i++){
            Comet comet = Comets.get(i);
            if(comet.status == Comet.VALID){
                stdArea += Math.pow(comet.area - meanArea,2) / validCount;
                stdHeight += Math.pow(comet.height - meanHeight,2) / validCount;
                }
            }
        stdArea = Math.sqrt(stdArea);
        stdHeight = Math.sqrt(stdHeight);
        // ---------------------------

        // Set outlier comets based on statistics
        for(int i=0; i<Comets.size(); i++){
            Comet comet = Comets.get(i);
            if(comet.status == Comet.VALID){
                IJ.log("Mean height: " + meanHeight + ", std: " + stdHeight +
                       "this height: " + comet.height);
                /*if(comet.height > meanHeight + 2*stdHeight){
                    comet.status = Comet.INVALID;
                    IJ.log(i+" too high invalid");
                    }
                if(comet.height < meanHeight - 2*stdHeight){
                    comet.status = Comet.INVALID;
                    IJ.log(i+" height too small invalid");
                    }*/
                if(Math.abs(comet.area - meanArea) > 2*stdArea){
                    if(comet.status == Comet.VALID)
                        comet.status = Comet.OUTLIER;
                    IJ.log(i + " Suspected outlier based on area");
                    }
                }
            }
        // -----------------

        // ----- Loop over comets and analyze one-by-one ----------
        int idxValid = 0;

        for(int i=0; i<Comets.size(); i++){
            Comet comet = Comets.get(i);
            // Assign an ID to each comet that isn't INVALID_NO_HEAD
            if(comet.status != Comet.INVALID_NO_HEAD) {
                comet.id = idxValid + 1;
                idxValid++;
            }
            
            // Skip comets that can't calculate stats
            if (!comet.canCalculateStats()) continue;

            IJ.log("-----------------");
            IJ.log("ID: "+ comet.id);
            IJ.log(comet.cometRoi.getType()+" type");

            comet.oldRoi = (Roi)comet.cometRoi.clone();
            // Make polygon ROI
            comet.cometRoi = new PolygonRoi(comet.cometRoi.getConvexHull(),
                                            Roi.POLYGON);
            // Calculate parameters of polygon
            setCometParams(comet, ip_gs2);

            // Find head
            setupHead(ip_gs2,comet,cometOptions);

            // Background correction
            correctBackground(ip_gs2, comet);
        // -----------------------

        // --- Measure comets in grayscale image
            // Calculate comet parameters again
            setCometParams(comet, ip_gs2);

            // Comet properties
            comet.cometArea = comet.area;
            comet.cometIntensity = comet.mean;
            comet.cometLength = comet.width;
            comet.cometDNA = comet.area*comet.mean;

            // Head properties
            ip_gs2.setRoi(comet.headRoi);
            ImageStatistics headStats = ImageStatistics.getStatistics(ip_gs2, ij.measure.Measurements.ALL_STATS, null);
            comet.headArea = headStats.area;
            comet.headIntensity = headStats.mean;
            comet.headLength = headStats.roiWidth;
            comet.headDNA = comet.headArea*comet.headIntensity;
            comet.headDNAPercent = ((100*comet.headDNA)/comet.cometDNA);
            comet.headCentroid = getXIntensityCentroid(ip_gs2);

            // Tail properties
            comet.tailArea = comet.cometArea - comet.headArea;
            comet.tailLength = Math.max(comet.cometLength - comet.headLength,0);
            comet.tailDNA = comet.cometDNA - comet.headDNA;
            comet.tailIntensity =
                (comet.tailArea > 0) ? (comet.tailDNA / comet.tailArea) : 0;
            comet.tailDNApercent =
                comet.tailDNA / comet.cometDNA; // Multiply by 100 later
            comet.tailMoment = comet.tailLength * comet.tailDNApercent;
            Roi roiTail =
                (new ShapeRoi(comet.cometRoi)).not(new ShapeRoi(comet.headRoi));
            Rectangle tailBoundRect = roiTail.getBounds();
            if(tailBoundRect.width * tailBoundRect.height ==0){
                comet.tailCentroid = comet.headCentroid;
                }
            else {
                ip_gs2.setRoi(roiTail);
                comet.tailCentroid  = getXIntensityCentroid(ip_gs2);
                }

            comet.tailOliveMoment = comet.tailDNApercent *
                Math.abs(comet.tailCentroid - comet.headCentroid);
            comet.tailDNApercent *= 100;
        //-------------
        }

        // Close original image
        img_orig.changes = false;
        img_orig.close();

        // Return comets
        return (Comet[])Comets.toArray(new Comet[Comets.size()]);
    }

    private void correctBackground(ImageProcessor ip, Comet comet){
        int bgHeight = (int)Math.max(comet.height/5.0, 10);
        Roi bgRoi;
        if ((comet.y - bgHeight) >= 0) {
            bgRoi = new Roi(comet.x, comet.y - bgHeight, comet.width, bgHeight);
            }
        else {
            bgRoi = new Roi(comet.x, comet.y + comet.height, comet.width, bgHeight);
        }

        double[] bgAvgSmooth = getSmoothColumnAvg(ip, bgRoi, bgRoi.getBounds());
        subtractBackground(ip, comet.cometRoi, bgAvgSmooth);

        comet.bgProfile = getColumnAvg(ip, bgRoi, bgRoi.getBounds());
        comet.cometProfile = getColumnAvg(ip, comet.cometRoi, new Rectangle(comet.x,comet.y,comet.width,comet.height));
        comet.headProfile = getColumnAvg(ip, comet.headRoi, new Rectangle(comet.x,comet.y,comet.width,comet.height));
        comet.tailProfile = new double[comet.width];
        double cometAvgMax = 0;
        for(int k = 0; k < comet.width; k++){
            comet.tailProfile[k] = comet.cometProfile[k]-comet.headProfile[k];
            if (comet.cometProfile[k]>cometAvgMax){
                cometAvgMax = comet.cometProfile[k];
                }
            }
        comet.profileMax = cometAvgMax;
    }

    private void setupHead(ImageProcessor ip,Comet comet,int cometOptions){
        // --- Crop the grayscale comet from the original image
            // Make a grayscale copy of the original image
            ByteProcessor ipComet = (ByteProcessor)ip.duplicate();
            // TODO: change this
            //int img_orig_type = 4;
            // Grayscale
            /*if(img_orig_type == 0){
                ipComet = (ByteProcessor)ip.duplicate();
                }
            // RGB
            else if(img_orig_type == 4){
                ipComet = getCometChannel(ip);
                }*/

            // Set the comet as ROI
            ipComet.setRoi(comet.cometRoi);
            // Crop the comet from the image
            ipComet = (ByteProcessor)(ipComet.crop());
        // -------------------------------------------

        // ---- Head finding --------------------------------
            ShapeRoi roiHead = null, roi1, roi2;
            int headX, headY, headCenterY;
            Roi roiHeadCircle;
            Rectangle headBoundRect;
            boolean headValid = true;

        // --- First stage: find brightest part of comet

        // Get statistics from the comet
            IJ.log(cometOptions + "");
            if ((cometOptions & HEADFIND_AUTO)!=0 || (cometOptions & HEADFIND_BRIGHTEST)!=0){
                // Find the threshold at top 5% of histogram intensities
                int threshbin = getLocalThresh(comet.histogram, 255, 0.95);

                IJ.log("Local thresh for " + comet.id + " is " + threshbin);

                // Apply threshold (make binary)
                setThreshold(ipComet, threshbin);

                // Find borders of the brightest area
                Rectangle brightestBoundRect = getBinaryBoundRect(ipComet);

                IJ.log(brightestBoundRect + "");
                if((comet.circularity < 0.9) && (brightestBoundRect.width > brightestBoundRect.height*2)){
                    IJ.log(comet.id + " head is too long invalid");
                    headValid = false;
                    }
                //else {

                // Get statistics of the brightest region of comet
                ImageStatistics brightestStats = ImageStatistics.getStatistics(ipComet,
                                                    ij.measure.Measurements.ALL_STATS, null);

                // Take center of mass of the brightest region
                int xc = (int)brightestStats.xCenterOfMass;
                IJ.log("Center of mass x: " + xc);

                ip.setRoi(comet.cometRoi);
                int headRadius = getHeadHeight(ip, xc)/2;
                int headGap = xc - headRadius;
                if(headGap > 0){
                    IJ.log(comet.id + " head is at wrong place invalid");
                    headValid = false;
                }

                headX = comet.x;
                headCenterY = getFrontCentroid(ip);
                headY = (int)(headCenterY - headRadius);

                roiHeadCircle = new OvalRoi(headX, headY, 2*headRadius, 2*headRadius );

                roi1 = new ShapeRoi(comet.cometRoi);
                roi2 = new ShapeRoi(roiHeadCircle);
                roiHead = roi2.and(roi1);

                // If the comet is elongated, the head should be close to the left hand side

                /*if(comet.hratio < 0.9){
                    if((headBoundRect.x - comet.x) > (double)comet.width*0.1){
                        IJ.log(comet.id+" head is at wrong place invalid");
                        headValid = false;
                        }
                    }*/


                  headBoundRect = roiHead.getBounds();
                if(headBoundRect.width*headBoundRect.height==0) {
                      IJ.log(comet.id + " head area zero invalid");
                      headValid = false;
                  }
                //}
            }

        // ----------------------------------------------

        // --- Second stage: find head based on intensity profile
        if (((cometOptions & HEADFIND_AUTO)!=0 && headValid==false)
                || (cometOptions & HEADFIND_PROFILE)!=0){
            int headEdge = getHeadEdge(ip, comet);
            ip.setRoi(comet.cometRoi);

            int headRadius = headEdge / 2;
            headX = comet.x;
            headCenterY = getFrontCentroid(ip);
            headY = (int)(headCenterY - headRadius);

            roiHeadCircle = new OvalRoi(headX, headY, 2*headRadius, 2*headRadius);
            roi1 = new ShapeRoi(comet.cometRoi);
            roi2 = new ShapeRoi(roiHeadCircle);
            roiHead = roi2.and(roi1);
            }

        comet.headRoi = (Roi)roiHead.clone();
        // ----------------------------------------------

    }

    private void setCometParams(Comet comet, ImageProcessor ip){
        ip.setRoi(comet.cometRoi);
        ImageStatistics stats = ImageStatistics.getStatistics(ip, ij.measure.Measurements.ALL_STATS, null);

        // Position
        comet.x = (int)stats.roiX;
        comet.y = (int)stats.roiY;
        comet.width = (int)stats.roiWidth;
        comet.height = (int)stats.roiHeight;

        // Internal parameters
        comet.histogram = stats.histogram;
        comet.area = stats.area;
        comet.mean = stats.mean;
        comet.symmetry = ySymmetry(ip);;
        comet.perimeter = comet.cometRoi.getLength();
        comet.circularity = 4.0*Math.PI*comet.area / (comet.perimeter*comet.perimeter);;

        comet.hratio = comet.height / comet.width;
        comet.headFrontCenterY = getFrontCentroid(ip);
        comet.headRoiCenterY = comet.y+comet.height/2;
        comet.centerlineDiff = (double)Math.abs(comet.headFrontCenterY-comet.headRoiCenterY)/(double)comet.height;

        PolygonRoi roiConvexHull = new PolygonRoi(comet.cometRoi.getConvexHull(),Roi.POLYGON);
        ip.setRoi(roiConvexHull);
        comet.areaConvexHull = ImageStatistics.getStatistics(ip, ij.measure.Measurements.AREA, null).area;
        comet.convexity = (comet.area / comet.areaConvexHull);
    }

    private Rectangle getBinaryBoundRect(ImageProcessor ip){
        int minX = -1, minY = -1;
        int maxX=0, maxY=0;
        for(int x=0;x<ip.getWidth();x++){
            for(int y=0;y<ip.getHeight();y++){
                if(ip.getPixel(x,y) > 0){
                    if(minX < 0){
                        minX = x;
                        }
                    if(minY < 0){
                        minY = y;
                        }
                    maxX = x;
                    if(y > maxY){
                        maxY = y;
                        }
                    }
                }
            }
        IJ.log(minX+","+maxX+","+minY+","+maxY);
        return new Rectangle(minX,minY,maxX-minX,maxY-minY);
        }

    private void subtractBackground(ImageProcessor ip, Roi r, double[] bgAvg){
        Rectangle boundRect = r.getBounds();
        ip.setRoi(r);
        ImageProcessor maskp = ip.getMask();
        int j = 0;

        for(int x = 0; x < boundRect.width; x++){
            for(int y = 0; y < boundRect.height; y++){
                if(maskp.getPixel(x, y)>0){
                    int pixVal = ((int)(ip.getPixel(x + boundRect.x,y + boundRect.y) - bgAvg[j]));
                    pixVal = (pixVal < 0) ? 0 : pixVal;
                    ip.putPixel(x + boundRect.x, y + boundRect.y, pixVal);
                    }
                }
            j++;
            }
        }

    private double[] getColumnAvg(ImageProcessor ip, Roi roi, Rectangle boundRect){
        double[] colAvg = new double[boundRect.width];
        ip.resetRoi();
        ip.setRoi(roi);
        ImageProcessor maskp = ip.getMask();
        IJ.log(ip.getMask()==null ? "NULL MASK" : "NOT NULL MASK");
        boolean hasMask = (maskp != null);
        if(hasMask){
            IJ.log("Mask: "+ maskp.getWidth()+", "+ maskp.getHeight());
        }
        Rectangle roiBoundRect = roi.getBounds();

        int imgX,imgY,maskX,maskY;
        for(int i = 0; i < boundRect.width; i++){
            colAvg[i] = 0;
            for(int j = 0; j < boundRect.height; j++){
                imgX = i + boundRect.x;
                imgY = j + boundRect.y;
                maskX = i + boundRect.x - roiBoundRect.x;
                maskY = j + boundRect.y - roiBoundRect.y;

                if(!hasMask || (maskp.getPixel(maskX,maskY)>0)){
                    colAvg[i] += ip.getPixel(imgX,imgY);
                    }
                }
                colAvg[i] /= boundRect.height;
            }
        IJ.log("Col average: ");
        printArray(colAvg);
        return colAvg;
        }

    private double[] getSmoothColumnAvg(ImageProcessor ip, Roi r, Rectangle boundRect){
        //Rectangle boundRect = r.getBounds();
        int nCol = boundRect.width;
        double[] colAvgSmooth = new double[nCol];

        double[] colAvg = getColumnAvg(ip, r, boundRect);

        for(int i=0; i < nCol; i++){
            colAvgSmooth[i] = 0;
            int cnt = 0;
            for (int j=Math.max(i-2,0); j <= Math.min(i+2, nCol-1); j++){
                colAvgSmooth[i] += colAvg[j];
                cnt++;
                }
            colAvgSmooth[i] /= cnt;
            }
        return colAvgSmooth;
        }

    private void setThreshold(ImageProcessor ip, int minInt){
        Rectangle roi = ip.getRoi();
        Rectangle br = roi.getBounds();
        double pix;

        for(int y=br.y; y<(br.y+br.height); y++){
            for(int x=br.x; x<(br.x+br.width); x++){
                pix = ip.getPixelValue(x,y);
                if(pix >= minInt){
                    ip.putPixelValue(x,y,255);
                    }
                else {
                    ip.putPixelValue(x,y,0);
                    }
                }
            }
        }

    /*private void drawComet(ImageProcessor ip, Comet comet){
        if(comet.status == Comet.INVALID){
            ip.setColor(cometInvalidColor);
            comet.cometRoi.drawPixels(ip);
        }
        else if (comet.status == Comet.OUTLIER){
            // Draw comet outline on output image, add label
            ip.setColor(cometOutlierColor);
            ip.setRoi(comet.cometRoi);
            comet.cometRoi.drawPixels(ip);
            ip.setRoi(comet.headRoi);
            comet.headRoi.drawPixels(ip);

            TextRoi textRoi = new TextRoi(comet.x+comet.width,
                    comet.y, ""+comet.id, new Font("Arial", Font.BOLD, 22));
            textRoi.drawPixels(ip);
        }
        else if(comet.status ==Comet.VALID){
            // Draw comet outline on output image, add label
            ip.setColor(cometValidColor);
            ip.setRoi(comet.cometRoi);
            comet.cometRoi.drawPixels(ip);
            ip.setRoi(comet.headRoi);
            comet.headRoi.drawPixels(ip);

            TextRoi textRoi = new TextRoi(comet.x+comet.width,
                    comet.y, ""+comet.id, new Font("Arial", Font.BOLD, 22));
            textRoi.drawPixels(ip);
        }

    }*/

    private int setValidity(Vector<Comet> Comets, ImageProcessor ip){
        int validCount = 0;
        for(int i=0;i<Comets.size();i++){
            Comet comet = Comets.get(i);

            // Comet should be convex
            if (comet.convexity < 0.85) {
                comet.status = Comet.INVALID_SIZE;
                IJ.log(i+" convexity invalid ("+ comet.convexity + ")");
                }
            // Comet shouldn't be too asymmetrical
            if (comet.symmetry > 0.5){
                comet.status = Comet.INVALID_SIZE;
                IJ.log(i+" symmetry invalid");
                }
            // Comet shouldn't be higher than wide
            if(comet.hratio > 1.05){
                comet.status = Comet.INVALID_SIZE;
                IJ.log(i+" hratio invalid");
                }
            // Comet shouldn't be on border
            if(isOnEdge(ip,comet.cometRoi)){
                comet.status = Comet.INVALID_SIZE;
                IJ.log(i+" is on edge invalid");
                }
            if(comet.centerlineDiff > 0.15){
                if(comet.status==Comet.VALID) comet.status = Comet.OUTLIER;
                IJ.log(i+" centerline diff too big outlier");
                }
            if(comet.centerlineDiff > 0.2){
                comet.status = Comet.INVALID_SIZE;
                IJ.log(i+" centerline diff too big invalid");
                }
            if(comet.status == Comet.VALID) validCount++;
            }
        return validCount;
    }

    private boolean isOnEdge(ImageProcessor ip, Roi r){
        Rectangle br = r.getBounds();

        if(br.x <= 1){
            return true;
            }
        if(br.y <= 1){
            return true;
            }
        if((br.x + br.width) >= (ip.getWidth()-1)){
            return true;
            }
        if((br.y + br.height) >= (ip.getHeight()-1)){
            return true;
            }
        return false;
        }

    // private void yMoments(ImageProcessor ip){
        // double sumpix = 0.0;
        // double sumy1 = 0.0;
        // double sumy2 = 0.0;
        // double sumy3 = 0.0;
        // double sumy4 = 0.0;
        // double xc, yc;
        // double ymean, yvar;
        // double yskew, ykurt;
        // Rectangle roi = ip.getRoi();
        // byte[] mask = ip.getMaskArray();
        // Rectangle br = roi.getBounds();
        // double pix;
        // int cnt1=0;

        // int counter = 0;
        // for(int y=br.y; y<(br.y+br.height); y++){
            // for(int x=br.x; x<(br.x+br.width); x++){
                // if(mask[counter++]!=0){
                    // pix = ip.getPixelValue(x,y);
                    // xc = x+0.5;
                    // yc = y+0.5;
                    // sumpix += pix;
                    // sumy1 += pix*yc;
                    // cnt1++;
                    // }
                // }
            // }
        // ymean = sumy1/sumpix;

        // IJ.log("Cnt all: "+counter);
        // IJ.log("Cnt no mask: "+cnt1);

        // counter = 0;
        // for(int y=br.y; y<(br.y+br.height); y++){
            // for(int x=br.x; x<(br.x+br.width); x++){
                // if(mask[counter++]!=0){
                    // pix = ip.getPixelValue(x,y);
                    // xc = x+0.5;
                    // yc = y+0.5;
                    // sumpix += pix;
                    // sumy2 += pix*Math.pow((yc-ymean),2);
                    // sumy3 += pix*Math.pow((yc-ymean),3);
                    // sumy4 += pix*Math.pow((yc-ymean),4);
                    // }
                // }
            // }
        // yvar = sumy2 / sumpix;
        // yskew = sumy3 / (sumpix * Math.pow(yvar,1.5));
        // ykurt = sumy4 / (sumpix * Math.pow(yvar,2)) - 3.0;
        //IJ.log(ymean + "\t" + yvar + "\t" + yskew + "\t" + ykurt);
        // }


    private int getXIntensityCentroid(ImageProcessor ip){
        Rectangle roi = ip.getRoi();
        byte[] mask = ip.getMaskArray();
        Rectangle br = roi.getBounds();
        int maskidx;

        ImageStatistics stats = ImageStatistics.getStatistics(ip, ij.measure.Measurements.ALL_STATS, null);
        double fullIntensityHalf = (stats.mean * stats.area)/2.0;
        double sumIntensity = 0.0;
        int x;
        for(x=br.x; x<(br.x+br.width); x++){
            for(int y=br.y; y<(br.y+br.height); y++){
                maskidx = ((y-br.y)*br.width) + (x-br.x);
                if(mask[maskidx]!=0){
                    sumIntensity += ip.getPixelValue(x,y);
                    }
                }
            if(sumIntensity > fullIntensityHalf) break;
            }
        IJ.log("Fullhalf: "+fullIntensityHalf + " Sum intensity: "+ sumIntensity + " x: "+x);
        IJ.log(br+"");
        return x;
        }


    private int getFrontCentroid(ImageProcessor ip){
        double yFrontCentroid = 0.0;
        int cnt = 0;
        Rectangle roi = ip.getRoi();
        byte[] mask = ip.getMaskArray();
        Rectangle br = roi.getBounds();
        int maskidx;

        for(int x=br.x; x<(br.x+br.width*0.1); x++){
            for(int y=br.y; y<(br.y+br.height); y++){
                maskidx = ((y-br.y)*br.width) + (x-br.x);
                if(mask[maskidx]!=0){
                    yFrontCentroid += y;
                    cnt++;
                    }
                }
            }
        yFrontCentroid /= cnt;
        return (int)yFrontCentroid;
        }

    private double ySymmetry(ImageProcessor ip){
        //int counter = 0;
        Rectangle roi = ip.getRoi();
        byte[] mask = ip.getMaskArray();
        Rectangle br = roi.getBounds();
        //double pix;
        int sum1, sum2;
        double absdy = 0.0;
        int cnt1=0;
        int maskidx;

        int yFrontCentroid = getFrontCentroid(ip);

        for(int x=br.x; x<(br.x+br.width); x++){
            sum1=0;sum2=0;
            cnt1 = 0;
            for(int y=br.y; y<(br.y+br.height); y++){
                maskidx = ((y-br.y)*br.width) + (x-br.x);
                if(mask[maskidx]!=0){
                    //if(y < (br.y+ (br.height/2))){
                    if(y < yFrontCentroid){
                        sum1++;
                        }
                    else {
                        sum2++;
                        }
                    cnt1++;
                    }

                }
            absdy += Math.abs(sum2-sum1) / (double)cnt1;
            }
        absdy = absdy / br.width;
        return absdy;
        }

    /*private double xCenterOfMass(ImageProcessor ip){
        double sumpix = 0.0;
        double sumx1 = 0.0;
        double xc, yc;
        double xmean;
        Rectangle roi = ip.getRoi();
        //byte[] mask = ip.getMaskArray();
        Rectangle br = roi.getBounds();
        double pix;
        int cnt1=0, cnt0=0;

        int counter = 0;
        for(int y=br.y; y<(br.y+br.height); y++){
            for(int x=br.x; x<(br.x+br.width); x++){
                pix = (ip.getPixelValue(x,y))>0 ? 1:0;
                xc = x+0.5;
                yc = y+0.5;
                sumpix += pix;
                sumx1 += pix*xc;
                if (pix==255)
                    cnt1++;
                else
                    cnt0++;
                }
            }
        xmean = sumx1/sumpix;

        //IJ.log("Cnt 0: "+ cnt0 + " Cnt 1: "+ cnt1);
        //IJ.log("Cnt no mask: "+cnt1);
        return xmean;
        }*/

    /*private void printCenterLine(ImageProcessor ip){
        int w = ip.getWidth();
        int h = ip.getHeight();
        int[] center_data = new int[w];
        IJ.log("H: "+h+" W: " +w+ "\n");
        ip.getRow(0,(int)(h/2),center_data,w);
        //IJ.log(center_data);
        String s = "";
        for(int i:center_data) s += i + "\t";
        IJ.log(s);
        }*/

    private int getHeadHeight(ImageProcessor ip, int xcenter){
        int height = 0;
        ImageProcessor maskp = ip.getMask();
        for(int y=0;y<maskp.getHeight();y++){
            if(maskp.getPixel(xcenter, y)>0){
                height++;
                }
            }
        return height;
        }

    private void printArray(double[] arr){
        String s = "";
        for(int i=0;i<arr.length;i++){
            s += arr[i];
            if(i<arr.length-1) s+=",";
            }
        IJ.log(s);
        }

    private void open_ntimes(ImageProcessor ip, int n, int dark){
        if(dark==0){
            for(int i=0;i<n;i++){
                ip.dilate();
                }
            for(int i=0;i<n;i++){
                ip.erode();
                }
            }
        else
 {
            for(int i=0;i<n;i++){
                ip.erode();
                }
            for(int i=0;i<n;i++){
                ip.dilate();
                }
            }
        }

    private int getHeadEdge(ImageProcessor ip, Comet comet){
        ip.setRoi(comet.cometRoi);
        Rectangle boundRect = comet.cometRoi.getBounds();

        int yc = getFrontCentroid(ip);
        Rectangle profileRect;
        if(comet.circularity < 0.9){
            profileRect = new Rectangle(boundRect.x, yc-5, boundRect.width, 10);
        }
        else {
            profileRect = new Rectangle(boundRect);
        }

        double[] cometProfile = getColumnAvg(ip, comet.cometRoi, profileRect);

        printArray(cometProfile);

        int kernelWidth = (int)boundRect.width/10;
        double[] smoothKernel = new double[kernelWidth];
        Arrays.fill(smoothKernel, 1.0/kernelWidth);
        double[] diffKernel = {-1.0,1.0};

        // Smooth comet profile
        double[] y1 = convFilter(cometProfile,smoothKernel,true);
        printArray(y1);
        // Differentiate comet profile
        double[] y2 = convFilter(y1,diffKernel,false);
        printArray(y2);
        // Smooth differential
        double[] y3 = convFilter(y2,smoothKernel,true);
        printArray(y3);
        // Differentiate again
        double[] y4 = convFilter(y3,diffKernel,false);
        printArray(y4);
        // Smooth again
        double[] ddCometProfile = convFilter(y4,smoothKernel,true);
        IJ.log("Kernel width: "+kernelWidth);

        /*printArray(ddCometProfile);
        int edge = 0;
        for(int i=0; i < ddCometProfile.length-1; i++){
            if((ddCometProfile[i+1] > 0) && (ddCometProfile[i] < 0)){
                IJ.log("Transition at "+i);
                edge = i + 5;
                break;
                }
            }
        return edge;*/

        int zcross = 0;
        for(int i=0; i < ddCometProfile.length-1; i++){
            if((ddCometProfile[i+1] > 0) && (ddCometProfile[i] < 0)){
                IJ.log("Transition at "+i);
                zcross = i;
                break;
                }
            }

        int ddmax = 0;
        for(int i=1; i < ddCometProfile.length-1; i++){
            if(i<zcross) continue;
            if(ddCometProfile[i+1] <= ddCometProfile[i] &&
                    ddCometProfile[i-1] <= ddCometProfile[i]){
                ddmax = i;
                IJ.log("Head edge at "+i);
                break;
            }
        }
        if(ddmax==0){
            ddmax = boundRect.width;
            }
        return ddmax;
    }

    private double[] convFilter(double[] x, double[] kernel, boolean pad){
        int outLen;
        if(pad){
            outLen = x.length;
            }
        else {
            outLen = x.length + 1 - kernel.length;
            }

        double[] y = new double[outLen];
        int kernRadius = (int)Math.floor(kernel.length / 2.0);
        int kernStart;
        for(int i = 0; i < outLen; i++){
            y[i] = 0.0;
            if(pad){
                kernStart = i - kernRadius;
                }
            else {
                kernStart = i;
                }
            for(int j = 0; j< kernel.length; j++){
                if(kernStart + j >= 0 && kernStart + j < x.length){
                    y[i] += kernel[j]*x[kernStart + j];
                    }
                }

            }


        /*double xval;
        int xIdx;
        for(int i=0;i<outLen;i++){
            y[i] = 0.0;
            for(int j=0;j<kernel.length;j++){
                if(pad){
                    xIdx = i+j-kernRadius;
                    }
                else {
                    xIdx = i+j;
                    }
                if(xIdx < 0){
                    xval = x[0];
                    }
                else if(xIdx >= x.length){
                    xval = x[x.length-1];
                    }
                else {
                    xval = x[xIdx];
                    }
                y[i] += kernel[j]*xval;
            }
        }
*/
        return y;
    }

    private int getLocalThresh(int[] hist, int nBins, double percent){
        double sum = 0;
        for(int i=0; i<nBins; i++){
            sum += hist[i];
            //IJ.log("Sum: "+sum+", hist: "+hist[i]);
            }
        IJ.log("Full sum: "+sum+", top thresh: "+percent*sum);
        double sum2 = 0;
        for(int i=0; i<nBins; i++){
            sum2 += hist[i];
            //IJ.log("Sum: "+sum2+", hist: "+hist[i]);
            if(sum2 > percent*sum)
                return i-1;
            }
        return 0;
        }

    private ByteProcessor getGrayscaleCopy(ImageProcessor ip, int imgType){
        ByteProcessor ipGrayscale = null;

        if(imgType == ImagePlus.GRAY8){
            ipGrayscale = (ByteProcessor)ip.duplicate();
            }
        else if(imgType == ImagePlus.GRAY16){
            ipGrayscale = new ByteProcessor(ip,true);
            }
        // RGB
        else if(imgType == ImagePlus.COLOR_RGB){
            ipGrayscale = getCometChannel(ip);
            }
        return ipGrayscale;
    }

    private ByteProcessor getCometChannel(ImageProcessor ip){
        int w = ip.getWidth();
        int h = ip.getHeight();
        int size = w*h;
        byte[] rPix = new byte[size];
        byte[] gPix = new byte[size];
        byte[] bPix = new byte[size];
        int[] ipPix = (int[])(ip.getPixels());
        ByteProcessor bp = new ByteProcessor(w,h);
        byte[] bpPix = (byte[])bp.getPixels();
        double rAvgInt=0, gAvgInt=0, bAvgInt=0;
        for (int i=0;i<size;i++){
            rPix[i] = (byte)((ipPix[i] & 0xff0000)>>>16);
            rAvgInt += (double)rPix[i]/size;
            gPix[i] = (byte)((ipPix[i] & 0x00ff00)>>>8);
            gAvgInt += (double)gPix[i]/size;
            bPix[i] = (byte)(ipPix[i] & 0x0000ff);
            bAvgInt += (double)bPix[i]/size;
            //bpPix[i] = (byte)((ipPix[i] & 0x00ff00)>>>8);
            }

        IJ.log("Red: " + rAvgInt + ", green:" + gAvgInt + ", blue: " + bAvgInt);
        boolean rg = (rAvgInt > gAvgInt);
        boolean rb = (rAvgInt > bAvgInt);
        boolean gb = (gAvgInt > bAvgInt);

        if(rg && rb){
            for (int i=0;i<size;i++){
                bpPix[i] = rPix[i];
                }
            }
        else if (!rg && gb){
            for (int i=0;i<size;i++){
                bpPix[i] = gPix[i];
                }
            }
        else {
            for (int i=0;i<size;i++){
                bpPix[i] = bPix[i];
                }
            }

        return bp;
        }

    private class CometParticleAnalyzer extends ParticleAnalyzer {
        protected Vector<Roi> cometRois;
        /*protected void saveResults(ImageStatistics stats, Roi roi) {
            cometRois.add(roi);
            }*/
        protected void saveResults(ImageStatistics stats, Roi roi) {
            //super.saveResults(stats,roi);
            cometRois.add((Roi)roi.clone());
        }
        public Roi[] getCometRois(){
            return (Roi[])(cometRois.toArray(new Roi[cometRois.size()]));
            }
        public CometParticleAnalyzer(int options, int measurements,
                                     Object dummy, double minSize,
                                     double maxSize, double minCirc,
                                     double maxCirc){
            super(options, measurements, null, minSize, maxSize,
                  minCirc, maxCirc);
            cometRois = new Vector<Roi>();
        }
    };
}

