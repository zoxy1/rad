package rad.geo.surfaceType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: crysalis
 * Date: Oct 27, 2006
 * Time: 1:17:41 PM
 * To change this template use File | Settings | File Templates.
 */

//#        class                             R   G   B
//0  Water (and Goode's interrupted space)  000 000 000
//1  Evergreen Needleleaf Forest            001 100 000
//2  Evergreen Broadleaf Forest             001 130 000
//3  Deciduous Needleleaf Forest            151 191 071
//4  Deciduous Broadleaf Forest             002 220 000
//5  Mixed Forest                           000 255 000
//6  Woodland                               146 174 047
//7  Wooded Grassland                       220 206 000
//8  Closed Shrubland                       255 173 000
//9  Open Shrubland                         255 251 195
//10  Grassland                             140 072 009
//11  Cropland                              247 165 255
//12  Bare Ground                           255 199 174
//13  Urban and Built-up                    000 255 255 

public class LandType {
    public static final int TOTAL_TYPES = 14;
    public static HashMap<Integer, String> types;
//    public static final int SAND  = 0;
//    public static final int WATER = 1;
//    public static final int SOIL  = 2;
//    public static final int WAIST = 3;
//    public static final int SNOW  = 4;
//    public static final int GREEN = 5;

    public LandType(){
        types = new HashMap<Integer, String>();
        types.put(0, "Water (and Goode's interrupted space)");
        types.put(1, "Evergreen Needleleaf Forest");
        types.put(2, "Evergreen Broadleaf Forest");
        types.put(3, "Deciduous Needleleaf Forest");
        types.put(4, "Deciduous Broadleaf Forest");
        types.put(5, "Mixed Forest");
        types.put(6, "Woodland");
        types.put(7, "Wooded Grassland");
        types.put(8, "Closed Shrubland");
        types.put(9, "Open Shrubland");
        types.put(10, "Grassland");
        types.put(11, "Cropland");
        types.put(12, "Bare Ground");
        types.put(13, "Urban and Built-up");
    }

}
