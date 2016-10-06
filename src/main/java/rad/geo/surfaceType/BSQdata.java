package rad.geo.surfaceType;

import rad.geo.strm.SubRegion;
import rad.geo.strm.dataBaseWork.DEMInfoDB;
import rad.geo.strm.dataBaseWork.SRTM30_Files_DB;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: crysalis
 * Date: Oct 27, 2006
 * Time: 12:54:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class BSQdata {
//Image Size: 				43200 Pixels 21600 Lines
//Quantization:			    8-bit unsigned integer
//Output Georeferenced Units: LONG/LAT E019
//Projection:    				Geographic (geodetic)
//Earth Ellipsoid:			Sphere, rad 6370997 m
//Upper Left Corner:			180d00'00.00" W Lon90d00'00.00" N Lat
//Lower Right Corner:			180d00'00.00" E Lon 90d00'00.00" S Lat
//Pixel Size (in Degrees):	0.00833 Lon 0.00833 Lat
//(Equivalent Deg,Min,Sec):	0d00'30.00"0d00'30.00"
//UpLeftX:					-180
//UpLeftY:      				90
//LoRightX:        			180
//LoRightY:      				-90

    private final long SIZE_X = 43200;                  //px
    private final long SIZE_Y = 21600;                  //px
    private final double RESOLUTION = 0.00833;          //deg
    private final double UP_LEFT_LATITUDE = 90.0;       //deg
    private final double UP_LEFT_LONGITUDE = -180.0;    //deg
    private final double LOWER_RIGHT_LATITUDE = -90.0;  //deg
    private final double LOWER_RIGHT_LONGITUDE = 180.0; //deg

    private String fullPathToSource;

    /**
     * constructor
     *
     * @param path
     */
    public BSQdata(String path) {
        fullPathToSource = path;
    }

    /**
     * loads defined region from whole data array
     *
     * @param centerPointLat
     * @param centerPointLon
     * @param width2
     * @param height2
     * @return LTRegion
     */
    public LTRegion loadRegionData(Double centerPointLat, Double centerPointLon,
                                   double width2, double height2, String wrkPath) {
        long width  = Math.round(width2 / 0.8);
        long height = Math.round(height2 / 0.8);

        LTRegion region = new LTRegion(centerPointLat, centerPointLon, width,
                height);
        long skipRows = Math.round(
                Math.abs(centerPointLat - UP_LEFT_LATITUDE) / 0.00833);
        long skipCols = Math.round(
                Math.abs(centerPointLon - UP_LEFT_LONGITUDE) / 0.00833);


        try {
            Byte byteVal = 0;
            String str = "";
            PrintStream resultFilePrinter = new PrintStream(
                    new File(wrkPath + "/" + "gltASCII" + ".out"));
            RandomAccessFile sourceFile = new RandomAccessFile(fullPathToSource,
                    "r");
            sourceFile.seek(skipRows * SIZE_X + skipCols);
            for (long i = 0; i < width; i++) {       //rows
                for (long j = 0; j < height; j++) {  //cols
                    byteVal = sourceFile.readByte();
                    region.addLandType(byteVal);
                    str = String.format("%-3d", byteVal);
                    resultFilePrinter.print(str + " ");
                }
                resultFilePrinter.print("\r\n");
                sourceFile.seek((skipRows + i) * SIZE_X + skipCols);
            }
            sourceFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error while opening file: " + fullPathToSource);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO error in file: " + fullPathToSource);
            e.printStackTrace();
        }
        return region;
    }

}
