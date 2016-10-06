package rad.geo.strm;

import rad.geo.utils.GeoPoint;
import rad.geo.strm.dataBaseWork.DEMInfoDB;
import rad.geo.strm.dataBaseWork.SRTM30_Files_DB;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 18.03.2006
 * Time: 15:17:14
 * To change this template use File | Settings | File Templates.
 */
public class SRTMregion {      //here region that in POV stores

    private final Double DEG_COEFF = 180 / java.lang.Math.PI;
    private Short reliefData[][];
    private Double centerPointLat;
    private Double centerPointLon;
    //private Double upLatitude;
    //private Double leftLongitude;
    private Double slope;
    private Double aspect;
    //private Double resolutionAtLatitude;

    //private String fullPathToSource;

    long skipRows = 0;
    long skipCols = 0;
    int rows = 0; //number of rows in file
    int cols = 0; //number of cols in file
    int size = 0;

    private GeoPoint upLeft = new GeoPoint();
    private GeoPoint upRight = new GeoPoint();
    private GeoPoint downRight = new GeoPoint();
    private GeoPoint downleft = new GeoPoint();
    private ArrayList<SubRegion> slicesMap = new ArrayList<SubRegion>();


    public boolean isDataLoaded = false;

    /*-----------------------------------------------------*/
    /*---------------------constructor---------------------*/
    /*-----------------------------------------------------*/

    /**
     * future release
     * Constructor for rectangle region:
     * center - coordinates of up left corner;
     * xSize, ySize - try to gas;
     * Warning!!! needs to call "loadRegionFromFile" method manually !!!!
     *
     * @param center
     * @param xSize
     * @param ySize
     */
    public SRTMregion(GeoPoint center, Double xSize, Double ySize, String wrkPath) {
        this.centerPointLat = center.getLatitude();
        this.centerPointLon = center.getLongitude();
        SubRegion sub = new SubRegion(center, xSize, ySize);
        this.rows = sub.getSizeY();
        this.cols = sub.getSizeX();
        double resolution = DEMInfoDB.angleResolution;
        this.upLeft.setPoint(center.getLatitude() + (ySize / 2) * resolution,
                center.getLongitude() - (xSize / 2) * resolution);
        this.upRight.setPoint(center.getLatitude() + (ySize / 2) * resolution,
                center.getLongitude() + (xSize / 2) * resolution);
        this.downleft.setPoint(center.getLatitude() - (ySize / 2) * resolution,
                center.getLongitude() - (xSize / 2) * resolution);
        this.downRight.setPoint(center.getLatitude() - (ySize / 2) * resolution,
                center.getLongitude() + (xSize / 2) * resolution);

        reliefData =  loadSubRegionData(sub, wrkPath);

        if (reliefData != null) {
            isDataLoaded = true;
        } else {
            isDataLoaded = false;
        }
    }

    /*-----------------------------------------------------*/
    /*------------------------methods----------------------*/
    /*-----------------------------------------------------*/


    /**
     * loads any subregion from DB
     * @return null
     */
    public Short[][] loadSubRegionData(SubRegion sub, String wrkPath) {
        int plateSizeX = 0;
        try {
            plateSizeX = DEMInfoDB.getInstance().getDEMInfo(
                    sub.getDemName()).getDemWidth();
        } catch (NullPointerException e) {
            System.out.println("[SRTMregion] No info for such DEM record");
            return null;
        }
        String fullPathToFile = "";
        SRTM30_Files_DB SRTM30DataBaseFiles = SRTM30_Files_DB.getInstance();
        if ((fullPathToFile = SRTM30DataBaseFiles.locateDirectoryForDEM(
                sub.getDemName())) != null) {
            RandomAccessFile sourceFile;
            PrintStream resultFilePrinter;
            Short[][] data = new Short[sub.getSizeY()][sub.getSizeX()];
            try {
                resultFilePrinter = new PrintStream(new File(wrkPath +"/" + "demASCII" + ".out"));
                sourceFile = new RandomAccessFile(fullPathToFile, "r");
                sourceFile.seek((sub.getOffsetY()) * plateSizeX + sub.getOffsetX() * 2);
                String str;
                for (int i = 0; i < sub.getSizeY(); i++) { //rows - all point at one longitude
                    for (int j = 0; j < sub.getSizeX(); j++) { //cols - all point at one latitude
                        data[i][j] = sourceFile.readShort();
                        str = String.format("%-6d", data[i][j]);
                        resultFilePrinter.print(str + " ");
                    }
                    sourceFile.seek(((sub.getOffsetY() + i) * (plateSizeX + 0) + sub.getOffsetX())*2);
                    resultFilePrinter.print("\r\n");
                }
                sourceFile.close();
                isDataLoaded = true;
                System.out.print("[SRTMregion] SubRegion data loading complete.");
                System.out.println("Total: rows=" + sub.getSizeY() + "  cols=" + sub.getSizeX());
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.out.println(
                        "[SRTMregion] FileNotFoundException! Loding data Error! Check file name!");
            } catch (IOException e) {
                System.out.println(
                        "[SRTMregion] IOException! cant seek file!");
            }
            return data;
        }
        return null;
    }



    /**
     * Probe
     *
     * @param center
     * @param w
     * @param h
     */      /*
    public void loadRegionFromFile(GeoPoint center, Integer w, Integer h) {
        PrintStream resultFilePrinter = null;
        try {
            resultFilePrinter = new PrintStream(
                    new File("c:\\" + "!!!!!!!!!" + "#.out"));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        reliefData = new Short[w][h];
        GeoPoint p = new GeoPoint();
        SRTM30_Files_DB srtmDataBase = SRTM30_Files_DB.getInstance();
        DEMInfoDB db = DEMInfoDB.getInstance();
        String demFileName = "";
        String fullPathToFile = "";
        for (int i = -w / 2, l = 0; i < w / 2; i++, l++) {
            for (int j = -h / 2, k = 0; j < h / 2; j++, k++) {
                if (p.setPoint(center.getLatitude() + (i * 0.008333333),
                        center.getLongitude() + (j * 0.008333333))) {
                    if ((demFileName = srtmDataBase.locateDEMForPoint(p))
                            != null) {
                        if ((fullPathToFile = srtmDataBase.locateDirectoryForDEM(
                                demFileName)) != null) {
                            /////
                            upLatitude = db.getDEMInfo(
                                    demFileName).getMinLatitude();
                            leftLongitude = db.getDEMInfo(
                                    demFileName).getMinLongitude();

                            skipRows = Math.round(
                                    (p.getLatitude() - upLatitude)
                                            / 0.008333333333333);
                            skipCols = Math.round(
                                    (p.getLongitude() - leftLongitude)
                                            / 0.008333333333333);

                            try {
                                RandomAccessFile sourceFile = new RandomAccessFile(
                                        fullPathToSource,
                                        "r");
                                sourceFile.seek(skipRows * 4800 + skipCols);
                                reliefData[l][k] = sourceFile.readShort();
                                resultFilePrinter.print(
                                        reliefData[l][k].toString() + " ");
                                sourceFile.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            resultFilePrinter.print("\n");
        }
        isDataLoaded = true;
    }
     /**/


    /**
     * calculate Slope and Aspect ratio for current region if data loaded
     */
    public void calcSlopeAspect() throws NullPointerException {
        double resolutionAtLatitude = 800.0;
        if (isDataLoaded) {
            int i, j;
            Double a[] = new Double[3];
            Double b[] = new Double[3];
            Double normal[] = new Double[3];
            Double cos_fi = 0.0;
            slope = 0.0;
            aspect = 0.0;
            Double summ = 0.0;
            Double slopeOfOnePoint;
            Double aspectOfOnePoint;
            Double z_center, z_north, z_south, z_east, z_west;

            for (i = 1; i < rows-1; i++) {
                for (j = 1; j < cols-1; j++) {
                    try {
                        z_center = reliefData[i][j].doubleValue();
                        z_north = reliefData[i][j - 1].doubleValue();
                        z_south = reliefData[i][j + 1].doubleValue();
                        z_east = reliefData[i - 1][j].doubleValue();
                        z_west = reliefData[i + 1][j].doubleValue();
                    } catch (NullPointerException e) {
                        System.out.println(
                                "[SRTMregion] Damaged data array. Calculation aborted.");
                        z_center = 0.0;
                        z_north = 0.0;
                        z_south = 0.0;
                        z_east = 0.0;
                        z_west = 0.0;
                    }
                    a[0] = 0.;
                    a[1] = z_north - z_south;
                    a[2] = 2 * resolutionAtLatitude;
                    b[0] = 2 * resolutionAtLatitude;
                    b[1] = z_west - z_east;
                    b[2] = 0.;
                    normal[0] = (a[1] * b[2] - a[2] * b[1].doubleValue());
                    normal[1] = (a[2] * b[0] - a[0] * b[2].doubleValue());
                    normal[2] = (a[0] * b[1] - a[1] * b[0].doubleValue());
                    summ = java.lang.Math.sqrt(normal[0] * normal[0] +
                            normal[1] * normal[1] +
                            normal[2] * normal[2]);

                    //angle between OX and n: cos(gamma) = normal[0] / summ;
                    //angle between OY and n: cos(gamma) = normal[1] / summ;   -> slope relatively to
                    //                                                          surface nornal
                    //angle between OZ and n: cos(gamma) = normal[2] / summ;   -> aspect relatively to
                    //                                                          azimuth
                    slopeOfOnePoint =
                            java.lang.Math.acos(normal[1] / summ) * DEG_COEFF;
                    aspectOfOnePoint =
                            java.lang.Math.acos(normal[2] / summ) * DEG_COEFF;
                    //System.out.println("Cos_fi= "+cos_fi+" (or angle="+cos_fi*57.7+" deg.)");
                    slope += slopeOfOnePoint;
                    aspect += aspectOfOnePoint;
                }
            }
            slope = slope / (rows * cols) * 1.0;
            aspect = aspect / (rows * cols) * 1.0;
            System.out.println(
                    "[SRTMregion] Slope calculation complete. Average: slope: "
                            + slope + " aspect: " + aspect);
        } else {
            System.out.println(
                    "[SRTMregion] Slope calculation aborted. No data loaded.");
        }
    }

    boolean isLoaded() {
        return isDataLoaded;
    }

    /*----------------------------------------------------------*/
    /*-------------------GETTERS--------------------------------*/
    /*----------------------------------------------------------*/
    public Double getSlope() {
        return slope;
    }

    public Double getAspect() {
        return aspect;
    }

    public Short[][] getReliefData() {
        return reliefData;
    }

    public Double getCenterPointLat() {
        return centerPointLat;
    }

    public Double getCenterPointLon() {
        return centerPointLon;
    }

//    public String getFullPathToSource() {
//        return fullPathToSource;
//    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return size;
    }

    public long getSkipCols() {
        return skipCols;
    }

    public long getSkipRows() {
        return skipRows;
    }


}
