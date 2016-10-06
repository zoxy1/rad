package rad.geo.strm.dataBaseWork;
/*---------------------------------------------------------------------------*/
/*------------------------                     ------------------------------*/
/*---------------------    Warning!!! Singleton!!!  -------------------------*/
/*------------------------                     ------------------------------*/
/*---------------------------------------------------------------------------*/
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: crysalis
 * Date: Aug 8, 2006
 * Time: 1:05:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class DEMFileInfoDB {
    private String title;
    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;
    private Double minElevation;
    private Double maxnElevation;
    private Double mean;
    private Double stdDev;
    private HashMap<Object, DEMFileInfoDB> DemDB = new HashMap<Object, DEMFileInfoDB>();
    static private DEMFileInfoDB instance = new DEMFileInfoDB();

    /**
     *
     */
    private DEMFileInfoDB(){
        createDB();
    }


    public static DEMFileInfoDB getInstance(){
        return instance;
    }
    /**
     *
     * @param title
     * @param minLat
     * @param maxLat
     * @param minLon
     * @param maxLon
     * @param minElev
     * @param maxElev
     * @param mean
     * @param stdDev
     */
    private DEMFileInfoDB(String title,
                          Double minLat, Double maxLat, Double minLon, Double maxLon,
                          Double minElev, Double maxElev, Double mean, Double stdDev){
        this.title = title;
        minLatitude = minLat;
        maxLatitude = maxLat;
        minLongitude = minLon;
        maxLongitude = maxLon;
        minElevation = minElev;
        maxnElevation = maxElev;
        this.mean = mean;
        this.stdDev = stdDev;
    }

    public String toString(){
        return String.format("%-9s lat:|%4.1f %4.1f| lon: |%4.1f %4.1f| elev:|%4.1f %4.1f| mean: %4.1f Std.Dev.: %4.1f",
                title, minLatitude, maxLatitude, minLongitude, maxLongitude,
                minElevation, maxnElevation, mean, stdDev);
    }
    /**
     *
     */
    private void createDB(){
        DemDB.put("W180N90", new DEMFileInfoDB("W180N90",     40.0,       90.0,      -180.0,     -140.0,       1.0,       6098.0,   448.0,     482.0));
        DemDB.put("W100N90", new DEMFileInfoDB("W100N90",     40.0,       90.0,      -100.0,     -60.0,         1.0,      2416.0,    333.0,     280.0));
        DemDB.put("W060N90", new DEMFileInfoDB("W060N90",     40.0,       90.0,      -60.0,     -20.0,         1.0,      3940.0,   1624.0,     933.0));
        DemDB.put("W020N90", new DEMFileInfoDB("W020N90",     40.0,       90.0,      -20.0,      20.0,       -30.0,      4536.0,    399.0,     425.0));
        DemDB.put("E020N90", new DEMFileInfoDB("E020N90",     40.0,       90.0,       20.0,      60.0,      -137.0,      5483.0,    213.0,     312.0));
        DemDB.put("E060N90", new DEMFileInfoDB("E060N90",     40.0,       90.0,       60.0,     100.0,      -152.0,      7169.0,    509.0,     698.0));
        DemDB.put("E100N90", new DEMFileInfoDB("E100N90",     40.0,       90.0,       100.0,     140.0,         1.0,      3877.0,    597.0,     455.0));
        DemDB.put("E140N90", new DEMFileInfoDB("E140N90",     40.0,       90.0,       140.0,     180.0,         1.0,      4588.0,    414.0,     401.0));
        DemDB.put("W180N40", new DEMFileInfoDB("W180N40",    -10.0,       40.0,      -180.0,    -140.0,         1.0,      4148.0,    827.0,     862.0));
        DemDB.put("W140N40", new DEMFileInfoDB("W140N40",    -10.0,       40.0,      -140.0,    -100.0,       -79.0,      4328.0,   1321.0,     744.0));
        DemDB.put("W100N40", new DEMFileInfoDB("W100N40",    -10.0,       40.0,      -100.0,     -60.0,         1.0,      6710.0,    375.0,     610.0));
        DemDB.put("W060N40", new DEMFileInfoDB("W060N40",    -10.0,       40.0,      -60.0,     -20.0,         1.0,      2843.0,    212.0,     168.0));
        DemDB.put("W020N40", new DEMFileInfoDB("W020N40",    -10.0,       40.0,      -20.0,      20.0,      -103.0,      4059.0,    445.0,     298.0));
        DemDB.put("E020N40", new DEMFileInfoDB("E020N40",    -10.0,       40.0,       20.0,      60.0,      -407.0,      5825.0,    727.0,     561.0));
        DemDB.put("E060N40", new DEMFileInfoDB("E060N40",    -10.0,       40.0,       60.0,     100.0,         1.0,      8752.0,   1804.0,    1892.0));
        DemDB.put("E100N40", new DEMFileInfoDB("E100N40",    -10.0,       40.0,       100.0,     140.0,       -40.0,      7213.0,    692.0,    910.0));
        DemDB.put("E140N40", new DEMFileInfoDB("E140N40",    -10.0,       40.0,       140.0,     180.0,         1.0,      4628.0,    549.0,     715.0));
        DemDB.put("W180S10", new DEMFileInfoDB("W180S10",    -60.0,      -10.0,      -180.0,    -140.0,         1.0,      2732.0,    188.0,     297.0));
        DemDB.put("W140S10", new DEMFileInfoDB("W140S10",    -60.0,      -10.0,      -140.0,    -100.0,        1.0,       910.0,     65.0,    124.0));
        DemDB.put("W100S10", new DEMFileInfoDB("W100S10",    -60.0,      -10.0,      -100.0,     -60.0,         1.0,      6795.0,   1076.0,    1356.0));
        DemDB.put("W060S10", new DEMFileInfoDB("W060S10",    -60.0,      -10.0,      -60.0,     -20.0,         1.0,      2863.0,    412.0,     292.0));
        DemDB.put("W020S10", new DEMFileInfoDB("W020S10",    -60.0,      -10.0,      -20.0,      20.0,         1.0,      2590.0,   1085.0,     403.0));
        DemDB.put("E020S10", new DEMFileInfoDB("E020S10",    -60.0,      -10.0,       20.0,      60.0,         1.0,      3484.0,    893.0,     450.0));
        DemDB.put("E060S10", new DEMFileInfoDB("E060S10",    -60.0,      -10.0,       60.0,     100.0,         1.0,      2687.0,    246.0,     303.0));
        DemDB.put("E100S10", new DEMFileInfoDB("E100S10",    -60.0,      -10.0,       100.0,     140.0,         1.0,      1499.0,    313.0,     182.0));
        DemDB.put("E140S10", new DEMFileInfoDB("E140S10",    -60.0,      -10.0,       140.0,     180.0,         1.0,      3405.0,    282.0,     252.0));
        DemDB.put("W180S60", new DEMFileInfoDB("W180S60",    -90.0,      -60.0,      -180.0,    -120.0,         1.0,      4009.0,   1616.0,    1043.0));
        DemDB.put("W120S60", new DEMFileInfoDB("W120S60",    -90.0,      -60.0,      -120.0,     -60.0,         1.0,      4743.0,   1616.0,     774.0));
        DemDB.put("W060S60", new DEMFileInfoDB("W060S60",    -90.0,      -60.0,      -60.0,       0.0,         1.0,      2916.0,   1866.0,     732.0));
        DemDB.put("W000S60", new DEMFileInfoDB("W000S60",    -90.0,      -60.0,       0.0,      60.0,         1.0,      3839.0,   2867.0,     689.0));
        DemDB.put("E060S60", new DEMFileInfoDB("E060S60",    -90.0,      -60.0,       60.0,     120.0,         1.0,      4039.0,   2951.0,     781.0));
        DemDB.put("E120S60", new DEMFileInfoDB("E120S60",    -90.0,      -60.0,       120.0,     180.0,         1.0,      4363.0,   2450.0,     665.0));
        DemDB.put("ANTARCPS", new DEMFileInfoDB("ANTARCPS", -90.0,      -60.0,      -180.0,     180.0,         1.0,      4748.0,   2198.0,    1016.0));
    }

    public DEMFileInfoDB getDEMInfo(Object key){
        if (DemDB.containsKey(key)) {
            return DemDB.get(key);
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public Double getMinLatitude() {
        return minLatitude;
    }

    public Double getMaxLatitude() {
        return maxLatitude;
    }

    public Double getMinLongitude() {
        return minLongitude;
    }

    public Double getMaxLongitude() {
        return maxLongitude;
    }

    public Double getMinElevation() {
        return minElevation;
    }

    public Double getMaxnElevation() {
        return maxnElevation;
    }

    public Double getMean() {
        return mean;
    }

    public Double getStdDev() {
        return stdDev;
    }

    public HashMap<Object, DEMFileInfoDB> getDemDB() {
        return DemDB;
    }

} // End of Class

/*----------------------------------------------------------------------------
!!!!!!!!!!!!!!!!!!Data from documentation:!!!!!!!!!!!!!!
            Latitude          Longitude                  Elevation
  Tile    Minimum  Maximum   Minimum  Maximum   Minimum  Maximum  Mean  Std.Dev.
-------  ----------------   ----------------   --------------------------------
*W180N90     40       90       -180    -140         1      6098    448     482
*W140N90     40       90       -140    -100         1      4635    730     596
*W100N90     40       90       -100     -60         1      2416    333     280
*W060N90     40       90        -60     -20         1      3940   1624     933
*W020N90     40       90        -20      20       -30      4536    399     425
*E020N90     40       90         20      60      -137      5483    213     312
*E060N90     40       90         60     100      -152      7169    509     698
*E100N90     40       90        100     140         1      3877    597     455
*E140N90     40       90        140     180         1      4588    414     401
*W180N40    -10       40       -180    -140         1      4148    827     862
*W140N40    -10       40       -140    -100       -79      4328   1321     744
*W100N40    -10       40       -100     -60         1      6710    375     610
*W060N40    -10       40        -60     -20         1      2843    212     168
*W020N40    -10       40        -20      20      -103      4059    445     298
*E020N40    -10       40         20      60      -407      5825    727     561
*E060N40    -10       40         60     100         1      8752   1804    1892
*E100N40    -10       40        100     140       -40      7213    692     910
*E140N40    -10       40        140     180         1      4628    549     715
*W180S10    -60      -10       -180    -140         1      2732    188     297
*W140S10    -60      -10       -140    -100         1       910     65     124
*W100S10    -60      -10       -100     -60         1      6795   1076    1356
*W060S10    -60      -10        -60     -20         1      2863    412     292
*W020S10    -60      -10        -20      20         1      2590   1085     403
*E020S10    -60      -10         20      60         1      3484    893     450
*E060S10    -60      -10         60     100         1      2687    246     303
*E100S10    -60      -10        100     140         1      1499    313     182
*E140S10    -60      -10        140     180         1      3405    282     252
*W180S60    -90      -60       -180    -120         1      4009   1616    1043
*W120S60    -90      -60       -120     -60         1      4743   1616     774
*W060S60    -90      -60        -60       0         1      2916   1866     732
*W000S60    -90      -60          0      60         1      3839   2867     689
*E060S60    -90      -60         60     120         1      4039   2951     781
*E120S60    -90      -60        120     180         1      4363   2450     665
*ANTARCPS   -90      -60       -180     180         1      4748   2198    1016
/*============================================================================*/
