/**
* OpenComet_.java
* Created in 2012 by Benjamin Gyori & updated in 2025 by Josiah Murray
* National University of Singapore & Medical College of Wisconsin
* e-mail: ben.gyori@gmail.com & jmurray@mcw.edu
*
* OpenComet_.java is the outside wrapper for the OpenComet plug-in.
* It displays the GUI and manages all input-output functions
* by the plug-in.
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

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

//import CometStatistics.CometAnalyzer;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;


public class OpenComet_ implements PlugIn, MouseListener {
    //private JLabel inFilesLabel, outDirLabel;
    private JLabel inFilesStatusLabel, outDirStatusLabel;
    private JButton inFilesButton, outDirButton, runButton;
    private JButton updateOutputButton;
    private JButton liveButton;
    private ActionListener inFilesButtonLis, outDirButtonLis, runButtonLis;
    private ActionListener updateOutputButtonLis;
    private ActionListener liveButtonLis;
    private JFileChooser inFileChooser, outDirChooser;
    private JTextField outFileNameField;
    private JCheckBox bgCorrectCheck;
    private JRadioButton headFindingAuto, headFindingProfile;
    private JRadioButton headFindingBrightest;
    private JComboBox<String> thresholdMethodCombo;
    private int cometOptions;

    private static Color labelInvalidColor = new Color(200,0,0);
    private static Color labelValidColor = new Color(0,150,0);

    File[] inFiles;
    File outDir;
    String outDirPath;
    String outFileName;

    private static Color cometValidColor = Color.red;
    private static Color cometInvalidColor = Color.gray;
    private static Color cometOutlierColor = Color.orange;
    private static Color cometColor = Color.yellow;
    private static Color headColor = Color.green;
    private static Color tailColor = Color.blue;

    private HashMap<ImagePlus,Comet[]> Comets;

    public OpenComet_(){}

    public void run(String arg) {
        IJ.log("Welcome to OpenComet v2.0 by Josiah Murray!");
        IJ.log("This code is adapted from OpenComet v1.3.1 by Benjamin Gyori.");
        IJ.log("Following analysis, click on a comet to cycle through the following states: ");
        IJ.log("VALID (Red) -> INVALID (Gray)");
        IJ.log("OUTLIER (Orange) -> INVALID (Gray)");
        IJ.log("INVALID (Gray) -> OUTLIER (Orange)");
        IJ.log("Unlike opencomet v1.3.1, comets are never deleted from the output file, ");
        IJ.log("but their status / validity can easily be updated.");
        IJ.log("This version of OpenComet also allows users to select a threhsold method.");
        IJ.log("Opencomet v1.3.1, uses 'Huang' for thresholding, but 'Triangle' often works better.");
        makeLayout();
        updateOutputButton.setEnabled(false);
    }

    private void runOnInput(int type){
        // Make an instance of the comet analyzer class
        CometAnalyzer cometAnalyzer = new CometAnalyzer();
        Comets = new HashMap<ImagePlus,Comet[]>();
        cometOptions = 0;
        // Setup comet analysis options
        if(bgCorrectCheck.isSelected())
            cometOptions |= CometAnalyzer.COMETFIND_BGCORRECT;
        if(headFindingAuto.isSelected())
            cometOptions |= CometAnalyzer.HEADFIND_AUTO;
        if(headFindingProfile.isSelected())
            cometOptions |= CometAnalyzer.HEADFIND_PROFILE;
        if(headFindingBrightest.isSelected())
            cometOptions |= CometAnalyzer.HEADFIND_BRIGHTEST;

        // Set the threshold method
        cometAnalyzer.setThresholdMethod((String)thresholdMethodCombo.getSelectedItem());

        String tmpText = outFileNameField.getText();
        if((tmpText!=null) && (tmpText.length()>1)){
            outFileName = tmpText;
        }

        IJ.log(cometOptions+"");

        if(type==0) {
        // Open each input image and run comet analysis
            // Iterate over each input file
            for(int i=0;i<inFiles.length;i++){
                // Try to open file as image
                ImagePlus imp = IJ.openImage(inFiles[i].getPath());

                // If image could be opened, run comet analysis
                if(imp!=null){
                    String imageKey =  inFiles[i].getName();

                    IJ.log("Run started, image key: "+ imageKey);

                    Comet[] cometsOut =
                        cometAnalyzer.cometAnalyzerRun(imp, cometOptions);
                    IJ.log("Run complete, image key: "+ imageKey);
                    storeComets(cometsOut,imp,imageKey);
                    // if(cometsOut!=null && cometsOut.length>0){
                        // ImageProcessor ip = imp.getProcessor();
                        // int imgType = imp.getType();
                        // ColorProcessor ip_out = null;
                        // if(imgType == 0){
                            // TypeConverter t = new TypeConverter(ip,false);
                            // ip_out = (ColorProcessor)t.convertToRGB();
                            // }
                        //RGB
                        // else if(imgType == 4){
                            // ip_out = (ColorProcessor)(ip.duplicate());
                            // }

                        // String imgOutFileName = imageKey +"_out.tif";
                        // ImagePlus img_out = new ImagePlus(imgOutFileName,ip_out);
                        // Comets.put(img_out, cometsOut);
                        // for(int j=0;j<cometsOut.length;j++){
                            // if(cometsOut[j].status==Comet.VALID)
                                // printArray(cometsOut[j].cometProfile);
                        // }
                        // Overlay cometOverlay = new Overlay();
                        // drawComets(ip_out, cometOverlay, cometsOut);
                        // img_out.setOverlay(cometOverlay);
                        // img_out.show();
                        // img_out.getCanvas().addMouseListener(this);
                        // ImagePlus img_out_save = img_out.flatten();
                        // IJ.save(img_out_save, outDirPath + imgOutFileName);
                        // }
                    // else {
                        // IJ.log("No comets stored");
                    // }
                 }
                else {
                    IJ.log("Could not open " + inFiles[i].getName() +
                           ", unsupported format");
                    }
                }
            }
            else {
                ImageWindow imw = WindowManager.getCurrentWindow();
                if(imw!=null){
                    ImagePlus img = imw.getImagePlus();
                    ImagePlus img2 = imw.getImagePlus().duplicate();
                    if(img!=null){
                        Comet[] cometsOut = 
                            cometAnalyzer.cometAnalyzerRun(img,cometOptions);
                        if(cometsOut==null || cometsOut.length==0){
                            IJ.log("No comets found.");
                        }
                        else {
                            IJ.log("Number of comets found: "+cometsOut.length);
                        }
                        storeComets(cometsOut,img2,outFileName+"_image");
                        }
                    }
                }

            printComets(outFileName);
            updateOutputButton.setEnabled(true);
            runButton.setEnabled(false);
            }

        private void storeComets(Comet[] cometsOut, ImagePlus imp, String imageKey){
            ImageProcessor ip = imp.getProcessor();
            int imgType = imp.getType();
            ColorProcessor ip_out = null;
            if(imgType == ImagePlus.GRAY8){
                TypeConverter t = new TypeConverter(ip,false);
                ip_out = (ColorProcessor)t.convertToRGB();
            }
            else if (imgType == ImagePlus.GRAY16 || imgType == ImagePlus.GRAY32){
                ImageProcessor ip_temp = ip.convertToByte(true); 
                TypeConverter t = new TypeConverter(ip_temp,false);
                ip_out = (ColorProcessor)t.convertToRGB();
            }
            else if(imgType == ImagePlus.COLOR_RGB){
                ip_out = (ColorProcessor)(ip.duplicate());
            }
            else {
                IJ.log("Unhandled image type: "+imgType);
                return;  // Exit if we can't handle the image type
            }

            String imgOutFileName = imageKey +"_out.tif";
            ImagePlus img_out = new ImagePlus(imgOutFileName,ip_out);

            if(cometsOut != null && cometsOut.length > 0){
                // Check each comet's ROIs before storing
                for(int i = 0; i < cometsOut.length; i++) {
                    if(cometsOut[i] == null) continue;
                    
                    // For INVALID_NO_HEAD comets, ensure ROIs are null
                    if(cometsOut[i].status == Comet.INVALID_NO_HEAD) {
                        cometsOut[i].headRoi = null;
                        continue;
                    }
                }
                
                Comets.put(img_out, cometsOut);

                for(int j=0; j<cometsOut.length; j++){
                    if(cometsOut[j] == null) continue;
                    
                    // Skip invalid comets
                    if(cometsOut[j].status == Comet.INVALID_NO_HEAD){
                        continue;
                    }
                    if(cometsOut[j].status == Comet.VALID && cometsOut[j].cometProfile != null) {
                        printArray(cometsOut[j].cometProfile);
                    }
                }

                Overlay cometOverlay = new Overlay();
                drawComets(ip_out, cometOverlay, cometsOut);
                img_out.setOverlay(cometOverlay);
                
                // Show the interactive version
                img_out.show();
                img_out.getCanvas().addMouseListener(this);
                
                // Save a flattened version
                ImagePlus img_out_save = img_out.flatten();
                IJ.save(img_out_save, outDirPath + imgOutFileName);
            } else {
                IJ.log("No comets in image stored.");
                img_out.show();
                img_out.getCanvas().addMouseListener(this);
                IJ.save(img_out, outDirPath + imgOutFileName);
            }
        }

    private void drawComets(ImageProcessor ip, Overlay overlay, Comet[] comets){
        for(int i=0;i<comets.length;i++){
            if(comets[i].status == Comet.INVALID_NO_HEAD){
                continue;
            }
            if(comets[i].status == Comet.OUTLIER){
                if(comets[i].cometRoi != null) {
                    comets[i].cometRoi.setStrokeColor(cometOutlierColor);
                    overlay.add(comets[i].cometRoi);
                }
                if(comets[i].headRoi != null) {
                    comets[i].headRoi.setStrokeColor(cometOutlierColor);
                    overlay.add(comets[i].headRoi);
                }

                // Add ID text to overlay with larger font and better positioning
                TextRoi textRoi = new TextRoi(comets[i].x + comets[i].width + 5,
                                          comets[i].y + comets[i].height/2, 
                                          "" + comets[i].id,
                                          new Font("Arial", Font.BOLD, 24));
                textRoi.setStrokeColor(cometOutlierColor);
                overlay.add(textRoi);

                // Add profile plot for outliers only if they have valid profiles
                if (comets[i].cometProfile != null && comets[i].profileMax > 0) {
                    ImageProcessor ipProfile = getCometProfilePlot(comets[i]);
                    ip.copyBits(ipProfile, comets[i].x,
                            comets[i].y,Blitter.COPY_TRANSPARENT);
                }
            }
            else if(comets[i].status == Comet.INVALID_SIZE){
                if(comets[i].cometRoi != null) {
                    comets[i].cometRoi.setStrokeColor(cometInvalidColor);
                    overlay.add(comets[i].cometRoi);
                }
                if(comets[i].headRoi != null) {
                    comets[i].headRoi.setStrokeColor(cometInvalidColor);
                    overlay.add(comets[i].headRoi);
                }

                // Add ID text to overlay with larger font and better positioning
                TextRoi textRoi = new TextRoi(comets[i].x + comets[i].width + 5,
                                          comets[i].y + comets[i].height/2, 
                                          ""+comets[i].id,
                                          new Font("Arial", Font.BOLD, 24));
                textRoi.setStrokeColor(cometInvalidColor);
                overlay.add(textRoi);
            }
            else if(comets[i].status == Comet.VALID){
                if(comets[i].cometRoi != null) {
                    comets[i].cometRoi.setStrokeColor(cometValidColor);
                    overlay.add(comets[i].cometRoi);
                }
                if(comets[i].headRoi != null) {
                    comets[i].headRoi.setStrokeColor(cometValidColor);
                    overlay.add(comets[i].headRoi);
                }

                // Add ID text to overlay with larger font and better positioning
                TextRoi textRoi = new TextRoi(comets[i].x + comets[i].width + 5,
                                          comets[i].y + comets[i].height/2, 
                                          ""+comets[i].id,
                                          new Font("Arial", Font.BOLD, 24));
                textRoi.setStrokeColor(cometValidColor);
                overlay.add(textRoi);

                // Add profile plot for valid comets only if they have valid profiles
                if (comets[i].cometProfile != null && comets[i].profileMax > 0) {
                    ImageProcessor ipProfile = getCometProfilePlot(comets[i]);
                    ip.copyBits(ipProfile, comets[i].x,
                            comets[i].y,Blitter.COPY_TRANSPARENT);
                }
            }
        }
    }

    private ImageProcessor getCometProfilePlot(Comet comet){
        ImageProcessor ip = new ColorProcessor(comet.width, comet.height);
        ip.invert();

        if (comet.profileMax > 0) {
            double heightStep = comet.height / comet.profileMax;
            
            // Only draw profiles if they exist
            if (comet.cometProfile != null) {
                drawProfilePlot(ip, comet.cometProfile, cometColor, heightStep);
            }
            if (comet.headProfile != null) {
                drawProfilePlot(ip, comet.headProfile, headColor, heightStep);
            }
            if (comet.tailProfile != null) {
                drawProfilePlot(ip, comet.tailProfile, tailColor, heightStep);
            }
        }

        return ip;
    }

    private void drawProfilePlot(ImageProcessor ip, double[] profile,
                                 Color col, double heightStep){
        int height = ip.getHeight();
        int width = ip.getWidth();
        int x1,x2,y1,y2;
        ip.setColor(col);
        for(int x=0;x<width-1;x++){
            x1 = x; x2 = x+1;
            y1 = (int)((height - 1) - profile[x]*heightStep);
            y2 = (int)((height - 1) - profile[x+1]*heightStep);
            ip.drawLine(x1,y1,x2,y2);
            }
        }

    private void printComets(String outFileName){
        try {
            PrintWriter outPrintWriter = new PrintWriter(outDirPath + outFileName + ".csv");
            String sep = ",";
            
            // Helper function to escape CSV values
            Function<String, String> escapeCsv = (String value) -> {
                if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                    return "\"" + value.replace("\"", "\"\"") + "\"";
                }
                return value;
            };
            
            // Print header
            String[] headers = {"Image", "ID", "Status", "Length", "HeadLength", "TailLength", 
                              "HeadArea", "TailArea", "CometArea", "HeadIntensity", "TailIntensity", 
                              "CometIntensity", "HeadDNA", "TailDNA", "CometDNA", "HeadDNA%", 
                              "TailDNA%", "TailMoment", "TailOliveMoment"};
            outPrintWriter.println(String.join(sep, Arrays.stream(headers).map(escapeCsv).collect(Collectors.toList())));

            for (Map.Entry<ImagePlus,Comet[]> element : Comets.entrySet()) {
                ImagePlus imp = element.getKey();
                String imgTitle = imp.getTitle();
                IJ.log("Image title: "+imgTitle);
                int endIdx = imgTitle.lastIndexOf('_');
                if(endIdx > 0){
                    String imageKey = imgTitle.substring(0,endIdx);
                    Comet[] imageComets = element.getValue();
                    for(int i=0;i<imageComets.length;i++){
                        // Include VALID, INVALID_SIZE, and OUTLIER comets
                        if(imageComets[i].status == Comet.VALID || 
                           imageComets[i].status == Comet.INVALID_SIZE ||
                           imageComets[i].status == Comet.OUTLIER){
                            String outstr = escapeCsv.apply(imageKey) + sep;
                            outstr += imageComets[i].getMeasurementString(sep);
                            outPrintWriter.println(outstr);
                        }
                    }
                }
            }

            // Calculate statistics for valid comets
            CometStatistics cometStats = new CometStatistics(Comets,Comet.VALID);
            String outstr = cometStats.getStatisticsString(sep,"normal");
            outPrintWriter.print(outstr);

            // Calculate statistics for valid + outlier comets
            cometStats = new CometStatistics(Comets,Comet.VALID|Comet.OUTLIER);
            outstr = cometStats.getStatisticsString(sep,"normal+outlier");
            outPrintWriter.print(outstr);

            // Calculate statistics for valid + invalid_size comets
            cometStats = new CometStatistics(Comets,Comet.VALID|Comet.INVALID_SIZE);
            outstr = cometStats.getStatisticsString(sep,"normal+invalid_size");
            outPrintWriter.print(outstr);

            // Calculate statistics for all measurable comets
            cometStats = new CometStatistics(Comets,Comet.VALID|Comet.INVALID_SIZE|Comet.OUTLIER);
            outstr = cometStats.getStatisticsString(sep,"all_measurable");
            outPrintWriter.print(outstr);

            outPrintWriter.close();
        }
        catch (Exception ex) {
            IJ.log(ex.toString());
        }
    }

    private void makeLayout(){
        // Create the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add threshold method selection
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdPanel.add(new JLabel("ROI Threshold Method:"));
        String[] thresholdMethods = {"Triangle", "Huang", "Percentile", "Yen", "Mean", "Otsu", "Li", "Shanbhag", "Intermodes", "IsoData", "MaxEntropy", "Moments", "RenyiEntropy"};
        thresholdMethodCombo = new JComboBox<>(thresholdMethods);
        thresholdMethodCombo.setSelectedItem("Triangle"); // Set default
        thresholdPanel.add(thresholdMethodCombo);
        mainPanel.add(thresholdPanel);

        // Add background correction checkbox
        bgCorrectCheck = new JCheckBox("Background Correction");
        bgCorrectCheck.setSelected(true);
        mainPanel.add(bgCorrectCheck);

        // Add head finding options
        JPanel headFindingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headFindingPanel.add(new JLabel("Head Finding Method:"));
        ButtonGroup headFindingGroup = new ButtonGroup();
        headFindingAuto = new JRadioButton("Auto");
        headFindingProfile = new JRadioButton("Profile");
        headFindingBrightest = new JRadioButton("Brightest");
        headFindingAuto.setSelected(true);
        headFindingGroup.add(headFindingAuto);
        headFindingGroup.add(headFindingProfile);
        headFindingGroup.add(headFindingBrightest);
        headFindingPanel.add(headFindingAuto);
        headFindingPanel.add(headFindingProfile);
        headFindingPanel.add(headFindingBrightest);
        mainPanel.add(headFindingPanel);

        // Add input file selection
        JPanel inFilesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inFilesButton = new JButton("Select Input Files");
        inFilesStatusLabel = new JLabel("No files selected");
        inFilesStatusLabel.setForeground(labelInvalidColor);
        inFilesPanel.add(inFilesButton);
        inFilesPanel.add(inFilesStatusLabel);
        mainPanel.add(inFilesPanel);

        // Add output directory selection
        JPanel outDirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        outDirButton = new JButton("Select Output Directory");
        outDirStatusLabel = new JLabel("No directory selected");
        outDirStatusLabel.setForeground(labelInvalidColor);
        outDirPanel.add(outDirButton);
        outDirPanel.add(outDirStatusLabel);
        mainPanel.add(outDirPanel);

        // Add output filename field
        JPanel outFileNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        outFileNamePanel.add(new JLabel("Output Filename:"));
        outFileNameField = new JTextField(20);
        outFileNamePanel.add(outFileNameField);
        mainPanel.add(outFileNamePanel);

        // Add run button
        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        runButton = new JButton("Run Analysis");
        runPanel.add(runButton);
        mainPanel.add(runPanel);

        // Add update output button
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updateOutputButton = new JButton("Update Output");
        updatePanel.add(updateOutputButton);
        mainPanel.add(updatePanel);

        // Create the frame
        JFrame frame = new JFrame("OpenComet v2.0");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        // Add action listeners
        inFilesButtonLis = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                inFileChooser = new JFileChooser();
                inFileChooser.setMultiSelectionEnabled(true);
                int returnVal = inFileChooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    inFiles = inFileChooser.getSelectedFiles();
                    if(inFiles.length>0){
                        inFilesStatusLabel.setText(inFiles.length + " files selected");
                        inFilesStatusLabel.setForeground(labelValidColor);
                    }
                }
            }
        };
        inFilesButton.addActionListener(inFilesButtonLis);

        outDirButtonLis = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                outDirChooser = new JFileChooser();
                outDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = outDirChooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    outDir = outDirChooser.getSelectedFile();
                    outDirPath = outDir.getPath() + File.separator;
                    if(checkWriteAccess(outDirPath)){
                        outDirStatusLabel.setText(outDirPath);
                        outDirStatusLabel.setForeground(labelValidColor);
                    }
                    else {
                        outDirStatusLabel.setText("No write access");
                        outDirStatusLabel.setForeground(labelInvalidColor);
                    }
                }
            }
        };
        outDirButton.addActionListener(outDirButtonLis);

        runButtonLis = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(inFiles==null || inFiles.length==0){
                    IJ.showMessage("Please select input files");
                    return;
                }
                if(outDir==null){
                    IJ.showMessage("Please select output directory");
                    return;
                }
                if(!checkWriteAccess(outDirPath)){
                    IJ.showMessage("No write access to output directory");
                    return;
                }
                String tmpText = outFileNameField.getText();
                if((tmpText==null) || (tmpText.length()<1)){
                    IJ.showMessage("Please enter output filename");
                    return;
                }
                runOnInput(0);
            }
        };
        runButton.addActionListener(runButtonLis);

        updateOutputButtonLis = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String tmpText = outFileNameField.getText();
                if((tmpText==null) || (tmpText.length()<1)){
                    IJ.showMessage("Please enter output filename");
                    return;
                }
                printComets(outFileName + "_update");
            }
        };
        updateOutputButton.addActionListener(updateOutputButtonLis);
    }

    public void mousePressed (MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        ImageCanvas canvas = (ImageCanvas)e.getSource();
        ImagePlus imp = canvas.getImage();
        x = canvas.offScreenX(x);
        y = canvas.offScreenY(y);
        IJ.log(x+","+y);
        Comet comet = findClickedComet(imp, x, y);
        if(comet != null && comet.cometRoi != null && comet.oldRoi != null){
            IJ.log("Comet found");
            // Cycle through states: VALID -> INVALID_SIZE -> OUTLIER -> INVALID_SIZE
            if(comet.status == Comet.VALID) {
                // VALID -> INVALID_SIZE
                comet.status = Comet.INVALID_SIZE;
                if(comet.cometRoi != null) comet.cometRoi.setStrokeColor(Color.gray);
                if(comet.oldRoi != null) comet.oldRoi.setStrokeColor(Color.gray);
                if(comet.headRoi != null){
                    comet.headRoi.setStrokeColor(Color.gray);
                }
                // Update ID color
                Overlay overlay = imp.getOverlay();
                if (overlay != null) {
                    for (Roi roi : overlay) {
                        if (roi instanceof TextRoi) {
                            Rectangle bounds = roi.getBounds();
                            if (bounds.x == comet.x + comet.width + 5) {
                                roi.setStrokeColor(Color.gray);
                                break;
                            }
                        }
                    }
                }
            } else if(comet.status == Comet.INVALID_SIZE) {
                // INVALID_SIZE -> OUTLIER
                comet.status = Comet.OUTLIER;
                if(comet.cometRoi != null) comet.cometRoi.setStrokeColor(cometOutlierColor);
                if(comet.oldRoi != null) comet.oldRoi.setStrokeColor(cometOutlierColor);
                if(comet.headRoi != null){
                    comet.headRoi.setStrokeColor(cometOutlierColor);
                }
                // Update ID color
                Overlay overlay = imp.getOverlay();
                if (overlay != null) {
                    for (Roi roi : overlay) {
                        if (roi instanceof TextRoi) {
                            Rectangle bounds = roi.getBounds();
                            if (bounds.x == comet.x + comet.width + 5) {
                                roi.setStrokeColor(cometOutlierColor);
                                break;
                            }
                        }
                    }
                }
            } else if(comet.status == Comet.OUTLIER) {
                // OUTLIER -> INVALID_SIZE
                comet.status = Comet.INVALID_SIZE;
                if(comet.cometRoi != null) comet.cometRoi.setStrokeColor(Color.gray);
                if(comet.oldRoi != null) comet.oldRoi.setStrokeColor(Color.gray);
                if(comet.headRoi != null){
                    comet.headRoi.setStrokeColor(Color.gray);
                }
                // Update ID color
                Overlay overlay = imp.getOverlay();
                if (overlay != null) {
                    for (Roi roi : overlay) {
                        if (roi instanceof TextRoi) {
                            Rectangle bounds = roi.getBounds();
                            if (bounds.x == comet.x + comet.width + 5) {
                                roi.setStrokeColor(Color.gray);
                                break;
                            }
                        }
                    }
                }
            }
            // Update the display
            imp.updateAndDraw();
        }
    }


    private Comet findClickedComet(ImagePlus imp, int x,int y){
        Comet[] comets = (Comet[])Comets.get(imp);
        if(comets!=null){
            for(int i=0;i<comets.length;i++){
                if(comets[i].cometRoi.contains(x, y)){
                    return comets[i];
                }
            }
        }
        return null;
    }

    public void mouseReleased (MouseEvent e) {}
    public void mouseClicked (MouseEvent e) {}
    public void mouseEntered (MouseEvent e) {}
    public void mouseExited (MouseEvent e) {}

    private boolean checkWriteAccess(String path){
        File tmpFile = new File(path+"tmp");
        try {
            tmpFile.createNewFile();
            tmpFile.delete();
            }
        catch(IOException e) {
            return false;
            }
        return true;
        }

    private void printArray(double[] arr){
        String s = "";
        for(int i=0;i<arr.length;i++){
            s += arr[i];
            if(i<arr.length-1) s+=",";
            }
        IJ.log(s);
        }
}

