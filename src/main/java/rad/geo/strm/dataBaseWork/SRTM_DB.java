package rad.geo.strm.dataBaseWork;

import rad.geo.utils.GeoPoint;

import java.util.HashMap;
import java.io.File;

/*---------------------------------------------------------------------------*/
/*------------------------                     ------------------------------*/
/*---------------------    Warning!!! Singleton!!!  -------------------------*/
/*------------------------                     ------------------------------*/
/*---------------------------------------------------------------------------*/

/**
 * Created by IntelliJ IDEA.
 * User: crysalis
 * Date: Aug 7, 2006
 * Time: 2:41:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class SRTM_DB {
    private HashMap <Object, GeoPoint> SrtmDB = new HashMap<Object, GeoPoint>();
    private File [] filesDB;
    private String dbPath;
    static SRTM_DB dbInstance = new SRTM_DB();

    public final String demExt = ".dem";
    public final String hdrExt = ".hdr";
    public final String srcExt = ".src";
    public final String srcHdrExt = ".sch";

    /*-----------------------------------------------------*/
    /*---------------------constructor---------------------*/
    /*-----------------------------------------------------*/
    private SRTM_DB(){
    }

    public static SRTM_DB getInstance(){
        return dbInstance;
    }

    /*-----------------------------------------------------*/
    /*------------------------methods----------------------*/
    /*-----------------------------------------------------*/
    /**
     * SRTM DB Initialization
     * @param pathToDB
     */
    public boolean init(String pathToDB){
        dbPath = pathToDB;
        boolean result = true;
        try{
            filesDB = new File(pathToDB).listFiles();
            for (int i = 0; i < filesDB.length; i++) {
                SrtmDB.put(filesDB[i].getName(), extractCoordinatesFromFileName(filesDB[i].getName()));
            }
            System.out.println("[SRTM DB] Initialized. Files in path found: " + filesDB.length);
        } catch (NullPointerException e) {
            System.err.println("[SRTM_DB] Wrong path to SRTM Data Base!");
            result = false;
        }
        result = !SrtmDB.isEmpty();
        return result;
    }

    /**
     * !!! IMPORTANT !!! for SRTM DB filenames only!!!
     * example: "e060n90.dem"
     */
    private GeoPoint extractCoordinatesFromFileName(String filename){
        boolean result = true;
        Double lat = 0.0;
        Double lon = 0.0;
        //splitting extension
        String [] mass = filename.split("\\.");
        try{
            //splitting latitude
            mass = mass[0].split("n");
            if (mass.length < 2) {
                mass = mass[0].split("s");
                if (mass.length >= 2) {
                    lat = (-1.0) * new Double(mass[1]);
                } else {
                    System.err.println("[SRTM_DB] Can't parse this file name: <" + filename + "> for latitude! Unknown format! GeoPoint damaged!");
                    lat = 1000.0;
                }
            } else {
                lat = new Double(mass[1]);
            }

            //splitting longitude
            mass = mass[0].split("e");
            if (mass.length < 2) {
                mass = mass[0].split("w");
                if (mass.length >= 2) {
                    lon = (-1.0) * new Double(mass[1]);
                } else {
                    System.err.println("[SRTM_DB] Can't parse this file name: <" + filename + ">  for longitude! Unknown format!");
                    lon = 1000.0;
                }
            } else {
                lon = new Double(mass[1]);
            }
        } catch (Exception e){
            System.err.println("[SRTM_DB] Parse error: <" + filename + ">.  Cant extract Number Format");
            lat = 1000.0;
            lon = 1000.0;
        }
        return new GeoPoint(lat, lon);
    }

    /**
     *
     * @param point
     * @return DEM file name with ext.
     */
    public String locateDEMForPoint(GeoPoint point){
        System.out.println("[SRTM_DB] Searching DEM record for coordinates <" + point + ">");
        DEMFileInfoDB demInfo = DEMFileInfoDB.getInstance();
        for (Object key : demInfo.getDemDB().keySet().toArray()){
            if (demInfo.getDEMInfo(key).getMaxLatitude() > point.getLatitude() &&
                    demInfo.getDEMInfo(key).getMinLatitude() <= point.getLatitude() &&
                    demInfo.getDEMInfo(key).getMinLongitude() <= point.getLongitude() &&
                    demInfo.getDEMInfo(key).getMaxLongitude() > point.getLongitude()) {
                System.out.print("[SRTM_DB] DEM record found: <" + key + ">");
                System.out.println("  Full path (must be): " + dbPath + key + "/"+ key + demExt);
                System.out.println("[SRTM_DB] DEM Info: " + demInfo.getDEMInfo(key));
                return key.toString();
            }
        }
        System.err.println("[SRTM_DB] DEM record not found for coordinates <" + point + ">");
        return null;
    };

    /**
     * search appropriate file in specified catalog
     * @param demName
     * @return full path to file, which contains specified point
     */
    public String locateDirectoryForDEM(String demName){
        demName = demName.toLowerCase();
        if (SrtmDB.containsKey(demName)){
            System.out.print("[SRTM_DB] File found: <" + demName + ">  ");
            System.out.println("Full path: " + dbPath + demName + "/"+ demName + ".dem");
            return dbPath + demName + "/"+ demName + demExt;
        }
        System.err.println("[SRTM_DB] File for DEM record <" + demName + "> not found");
        return null;
    }


    /*----------------------------------------------------------*/
    /*-------------------GETTERS--------------------------------*/
    /*----------------------------------------------------------*/
    public Integer getDBsize(){
        return SrtmDB.size();
    }

    public GeoPoint getUpLeftCoordinaresOf(Object filenID){
        if (SrtmDB.containsKey(filenID)){
            return SrtmDB.get(filenID);
        } else {
            System.err.println("[SRTM_DB] No information about this file in SRTM DB.");
            return null;
        }
    }

    public HashMap <Object, GeoPoint> getDB(){
        return SrtmDB;
    }

    public String getDbPath() {
        return dbPath;
    }



}
