package rad.geo.strm.srtmUtils;
/*-----------------------------------
Latitude      Ground distance (meters)
(degrees)         E/W        N/S
---------     ------------------------
Equator           928        921
  10              914        922
  20              872        923
  30              804        924
  40              712        925
  50              598        927
  60              465        929
  70              318        930
  73              272        930
  78              193        930
  82              130        931
  /*==================================*/
/**
 * Created by IntelliJ IDEA.
 * User: crysalis
 * Date: Aug 9, 2006
 * Time: 10:51:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class gridCorrector {

    private int lowerParallel = 0;
    private int upperParallel = 1;

    private static Double[] latGridPoints = {10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 73.0, 78.0, 82.0 , 90.0};
    private static Double[] EWsize = {928.0, 914.0, 872.0, 804.0, 712.0, 598.0, 465.0, 318.0, 272.0, 193.0, 130.0, 130.0};
    private static Double[] NSsize = {921.0, 922.0, 923.0, 924.0, 925.0, 927.0, 929.0, 930.0, 930.0, 930.0, 931.0, 931.0};

        
    /**
     * Futere release
     * @param latitude
     * @return size of pixel in East-West direction at Latitude specified
     */
    public static Double getPixelSizeEW(Double latitude){
        if (latitude < 0) latitude =  (-1.0) * latitude;
        if (latitude <= 90){
            return 0.0;
        }
        else {
            System.out.println("[gridCorrector] Coordinates out of grid!");
            return 0.0;
        }
    }


}


