package rad.bin;

import platform.Module;
import rad.geo.surfaceType.LTRegion;
import rad.geo.surfaceType.BSQdata;


import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 01.08.2006
 * Time: 15:45:28
 * To change this template use File | Settings | File Templates.
 */
public class RunnerSurface extends Runner {

    HashMap<String, Double[]> coord = new HashMap<String, Double[]>();
    Double pov;
    static Logger localLogger = Logger.getLogger(RunnerSurface.class);
    
    void runModule(Module modul) throws Exception {
    	try {
        coord = readFileToHashMapOfDoubleArrayWDesc(
                getParStringByText("coordinatesListFile"));
        pov = getParDoubleByText("pointOfViewRadius");

        PrintStream resultFilePrinter = new PrintStream(
                new File(
                        wrkPath + "/" + getParStringByText("resultFileName")));

        BSQdata LTdata = new BSQdata(getParStringByText("gltDataBasePath"));

        for (int i = 0; i < coord.get("Latitude").length; i++) {
            LTRegion regionData = LTdata.loadRegionData(
                    coord.get("Latitude")[i], coord.get("Longitude")[i],
                    pov, pov, wrkPath);
            resultFilePrinter.println(coord.get("Latitude")[i] + "   "
                    + coord.get("Longitude")[i]);
            resultFilePrinter.println(regionData.getTotalTypesInRegion());
            resultFilePrinter.println(regionData);

        }

        File surfile = new File(wrkPath+"/surface.out");
        BufferedReader Br = new BufferedReader(new FileReader(surfile));
        String surfType = Br.readLine();
         surfType = Br.readLine();
         surfType = Br.readLine();
         Br.close();
         Br.close();
         Br = null;
         //Thread.sleep(500);
        //BufferedWriter Bw = new BufferedWriter(new FileWriter(surfile));
        //Bw.append(" ");
        //Bw.close();
        //Bw.
        //Bw = null;
        if (surfType.contains("100.0   Water (and Goode's interrupted space)")){
        	File wrkF = new File(wrkPath);
        	File newWater = new File(wrkPath+"/100-WATER!!!");
        	newWater.createNewFile();
        	//wrkF.)
        	//boolean flg = wrkF.renameTo(newWrkfile);
        	//System.out.println(flg);
        }

        Double SEA[] = coord.get("SolarElevAngle");
        if (SEA[0]<=-9) {
        	File wrkF = new File(wrkPath);
        	File newWater = new File(wrkPath+"/100-NIGHT!!!"); /*SolarElevAngle="+String.format("%5.3f", SEA[0]));*/
        	newWater.createNewFile();
        	//wrkF.)
        	//boolean flg = wrkF.renameTo(newWrkfile);
        	//System.out.println(flg);
        }
    	} catch (Exception e){
			//e.fillInStackTrace();
			
    		localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
        }
    }

