package us.bluesakuradev.dms.carscanner;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import us.bluesakuradev.dms.carscanner.packet.SimplePacket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The main interface to a data management system.
 * The purpose of this is to provide the ability to read in CSV files generated
 * by the app "CarScanner". This system will also then organise the read data into different files and folders.
 *
 * The structure is app/project/run/data_files
 * And the data files are custom sfdu data packets.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        String inFile = "", outFile = "";
        int mode = 0;

        for(String arg : args){
            if(arg.contains("if=")){
                inFile = arg.substring(3);
            }
            if(arg.contains("of=")){
                outFile = arg.substring(3);
            }
            if(arg.contains("mode=")){
                mode = Integer.parseInt(arg.substring(5));
            }
        }

        if(mode == 0){
            CSVtoCSV(inFile, outFile);
        }

        if(mode == 1){
            System.out.println("SFDU Currently Unsupported.");
        }
    }

    public static void CSVtoCSV(String inFileName, String outFileName) throws IOException {
        // Open a CarScanner CSV file
        // Read it in row by row, creating a new SimplePacket for every entry
        // Sort the SimplePackets by PID (aka Label)
        // Save SimplePackets to new CSV files by Label

        ArrayList<SimplePacket> packets = new ArrayList<>();

        // Open CSV file
        File inFile = new File(inFileName);
        Scanner inFileReader = new Scanner(inFile);

        // Read it row by row
        if(inFileReader.hasNextLine()) inFileReader.nextLine(); // Skip the header line
        while(inFileReader.hasNextLine()){
            String line = inFileReader.nextLine();
            line.replaceAll("\"", "");
            String[] inData = new String[4];
            inData = line.split(";");
            packets.add(new SimplePacket(inData[1], inData[0], inData[2], inData[3]));
        }

        // Sort SimplePackets by PID
        // Map of ArrayLists for PID Dictionary
        HashMap<String, ArrayList<SimplePacket>> pidMap = new HashMap<String, ArrayList<SimplePacket>>();
        for(SimplePacket p : packets){
            if(pidMap.containsKey(p.getLabel())){ // If we have the key in the map...
                // Pull the arrayList
                ArrayList<SimplePacket> sortedPackets = pidMap.get(p.getLabel());

                // Add the packet to the ArrayList
                sortedPackets.add(p);

                // Replace the label
                pidMap.put(p.getLabel(), sortedPackets);
            }else{ // If we don't have the key in the map...
                // Create a new arrayList
                ArrayList<SimplePacket> sortedPackets = new ArrayList<SimplePacket>();
                // Add the packet to the new ArrayList
                sortedPackets.add(p);
                // Add the ArrayList to the map
                pidMap.put(p.getLabel(), sortedPackets);
            }
        }

        // Output SimplePackets to CSV Files by Label
        ArrayList<String> pidLabels = new ArrayList<String>();
        pidLabels.addAll(pidMap.keySet());
        for(String label : pidLabels){
            ArrayList<SimplePacket> tmpPackets = pidMap.get(label);
            // Create a new file
            String tmpSetName = label.replaceAll(" ","_");
            tmpSetName = tmpSetName.replace("(", "");
            tmpSetName = tmpSetName.replace(")", "");
            String tmpFileName = outFileName + "/" + tmpSetName + ".csv";
            FileWriter outFileWriter = new FileWriter(tmpFileName);

            System.out.println("Writing Data for " + label + " to " + tmpFileName);
            for(SimplePacket p : tmpPackets){
                outFileWriter.append(p.toString() + "\n");
            }
            System.out.println("Done.");
            outFileWriter.flush();
            outFileWriter.close();
        }

        for(String label : pidLabels) {
            System.out.println("Creating Chart for " + label);
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            ArrayList<SimplePacket> tmpPackets = pidMap.get(label);
            for(SimplePacket p : tmpPackets){
                // Plot and save to JPG
                dataset.addValue(Double.parseDouble(p.getTimeStamp()), label, p.getData());
            }

            JFreeChart lineChart = ChartFactory.createLineChart(label,"Time", label, dataset, PlotOrientation.VERTICAL, true, false, false);
            int width = 1920, height = 1080;

            String tmpSetName = label.replaceAll(" ","_");
            tmpSetName = tmpSetName.replace("(", "");
            tmpSetName = tmpSetName.replace(")", "");
            String tmpFileName = outFileName + "/" + tmpSetName + "line.png";

            File outChartFile = new File(tmpFileName);
            ChartUtils.saveChartAsPNG(outChartFile, lineChart, width, height);
        }
    }
}
