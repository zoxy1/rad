package rad.geo.surfaceType;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: crysalis
 * Date: Oct 27, 2006
 * Time: 12:54:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class LTRegion {

    private Long[] statisticData;
    private HashMap<Integer, Double> composition;
    private int totalTypesInRegion;
    private Double centerLatitude;      //deg
    private Double centerLongitude;     //deg
    private long height;
    private long width;
    private long size;
    private LandType lt = new LandType();

    LTRegion(Double lat, Double lon, Long width, Long height) {
        this.height = height;
        this.width = width;
        this.size = (height * width);
        this.centerLatitude = lat;
        this.centerLongitude = lon;
        statisticData = new Long[height.intValue() * width.intValue()];
        composition = new HashMap<Integer, Double>();
        statisticData = new Long[LandType.TOTAL_TYPES];
    }

    public String toString() {
        if (statisticData != null && statisticData.length > 0) {
            String tmp = "";
            for (int j : getComposition().keySet()) {
                tmp += j + "   " + getComposition().get(j) + "   " + lt.types.get(j) + "\r\n";
            }
            return tmp;
        }
        return null;
    }

    /**
     * add land type for current region
     * byte parameter
     *
     * @param type
     */
    public void addLandType(Byte type) {
        if (statisticData[type] == null) {
            statisticData[type] = new Long(0);
            totalTypesInRegion++;
        }
        statisticData[type]++;
    }

    /**
     * remove land type for current region
     *
     * @param type
     */
    public void removeLandType(int type) {
        statisticData[type] = null;
    }

    /**
     * delete all land types for this region
     */
    public void clearRegionLandTypes() {
        for (int i = 0; i < statisticData.length; i++) {
            statisticData[i] = null;
        }
        composition.clear();
    }

    /**
     * calculate percentage of each land type on given region
     */
    private void calcComposition() {
        for (int i = 0; i < statisticData.length; i++) {
            if (statisticData[i] != null) {
                composition.put(i, statisticData[i].doubleValue() * 100 / size);
            }
        }
    }

    /**   
     * return map of types for region
     * <type, percent>
     * percent calculates from different types count on giver region,
     * not from total count of types
     *
     * @return composition
     */
    public HashMap<Integer, Double> getComposition() {
        calcComposition();
        return composition;
    }

    /**
     * return number of different land types for this region
     *
     * @return totalTypesInRegion
     */
    public int getTotalTypesInRegion() {
        return totalTypesInRegion;
    }


    public Double getCenterLatitude() {
        return centerLatitude;
    }

    public Double getCenterLongitude() {
        return centerLongitude;
    }

    public long getHeight() {
        return height;
    }

    public long getWidth() {
        return width;
    }
}
