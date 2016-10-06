package rad.bin;

import platform.Module;
import rad.geo.strm.SRTMPropertyStorage;
import rad.geo.strm.SRTMregion;
import rad.geo.strm.SubRegion;
import rad.geo.strm.dataBaseWork.SRTM30_Files_DB;
import rad.geo.utils.GeoPoint;

import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 01.08.2006
 * Time: 15:44:59
 */
public class RunnerSRTM  extends Runner {
//	static Logger localLogger = Logger.getLogger(RunnerSRTM.class);
    /**
     * variables
     */
    String demFileName;
    String fullPathToFile;
    SRTM30_Files_DB SRTM30DataBaseFiles = SRTM30_Files_DB.getInstance();

    /**
     * input values
     */
    HashMap<String, Double[]> coord = new HashMap<String, Double[]>();
    /**
     * output Values
     */
    Vector<Double[]> outVect = new Vector<Double[]>();
    Double[] gSlope;
    Double[] gAspect;
    Double[] gLat;
    Double[] gLong;


    /**
     *  load All Parameters for processing SRTM data
     * @return
     * @throws Exception
     */
    boolean loadAllParameters() throws Exception {
        System.out.println("[SRTM Module] Reading parameters");
        SRTMPropertyStorage.coordinatesListFile = getParStringByText("coordinatesListFile");
        SRTMPropertyStorage.pointOfViewRadius = getParDoubleByText("pointOfViewRadius");

        SRTMPropertyStorage.srtmDataBasePath = getParStringByText("srtmDataBasePath");
        SRTMPropertyStorage.pathToResultFiles = getParStringByText("pathToResultFiles");
        SRTMPropertyStorage.slopeResultFileName = getParStringByText("slopeResultFileName");
        SRTMPropertyStorage.elevationDataResultFileName = getParStringByText("elevationDataResultFileName");

//        localLogger.info ("[SRTM Module] work path set to: " + wrkPath);
//        localLogger.info("[SRTM Module] module work path set to: " + mdWrkPath);

        return true;
    }



    /**
     * main method: do all work for srtm data extracting
     * and Slope calculation
     */
    void runModule(Module modul) throws Exception {
    	try {
        System.out.println("[SRTM Module] started.");
        loadAllParameters();
        //own file for result output...
        //PrintStream resultFilePrinter = new PrintStream(new File(wrkPath + "\\" + SRTMPropertyStorage.slopeResultFileName + "#"));
        coord = readFileToHashMapOfDoubleArrayWDesc(SRTMPropertyStorage.coordinatesListFile);

        /*!!!!!!!!!!!!!temp construction!!!!!!!!!!!!!!!*/
//        Double [] temp = {70.0, 71.0, 72.0, 73.0};
//        Double [] temp2= {80.0, 81.0, 82.0, 83.0};
//        coord.put("Latitude", temp);
//        coord.put("Longitude", temp2);
//
        /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

        if (coord != null) {
            GeoPoint povGeoCenter = new GeoPoint();
            if (SRTM30DataBaseFiles.init(SRTMPropertyStorage.srtmDataBasePath)) {
                gSlope = new Double[coord.get("Latitude").length];
                gAspect = new Double[coord.get("Latitude").length];
                gLat = new Double[coord.get("Latitude").length];
                gLong = new Double[coord.get("Latitude").length];
                for (int i = 0; i < coord.get("Latitude").length; i++){ // ! IMPORTANT.
                    if (povGeoCenter.setPoint(coord.get("Latitude")[i], coord.get("Longitude")[i])) {
                        SRTMregion povRegion = new SRTMregion(povGeoCenter,
                                SRTMPropertyStorage.pointOfViewRadius,
                                SRTMPropertyStorage.pointOfViewRadius, wrkPath);
                        povRegion.calcSlopeAspect();
                        gSlope[i] = povRegion.getSlope();
                        gAspect[i] = povRegion.getAspect();
                        gLat[i] = povGeoCenter.getLatitude();
                        gLong[i] = povGeoCenter.getLongitude();
                    }//end IF  setPoint check
                }//end of FOR (subtrace coordinates)
            } // end IF SRTM30DataBaseFiles.init
        }//end IF coordinares not empty



        outVect.add(gSlope);
        outVect.add(gAspect);
        outVect.add(gLat);
        outVect.add(gLong);
        if (outVect.firstElement() != null){
            writeVectorDoubleToTextFileWDesc(outVect, SRTMPropertyStorage.slopeResultFileName ,"%3.3f");
        } else {
            System.err.println("Nothing to write. Unknown error. Path to SRTM DB or calculation algorithm.");
        }

        System.out.println("[SRTM Module] complete.");
    	} catch (Exception e){
			//e.fillInStackTrace();
			
//    		localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
    }
}
