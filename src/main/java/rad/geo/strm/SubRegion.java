package rad.geo.strm;

import rad.geo.utils.GeoPoint;
import rad.geo.strm.dataBaseWork.DEMInfoDB;
import rad.geo.strm.dataBaseWork.SRTM30_Files_DB;

/**
 * Defines sub-region in one dem file
 * Created by IntelliJ IDEA.
 * User: crisalis
 * Date: Nov 2, 2006
 * Time: 1:08:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubRegion {
    private int offsetX;
    private int offsetY;
    private int sizeX;
    private int sizeY;
    private String demName;
    private GeoPoint upLeft;
    private GeoPoint downRight;

    /**
     *
     */
    public SubRegion() {
        this.offsetX = 0;
        this.offsetY = 0;
        this.sizeX = 0;
        this.sizeY = 0;
        this.demName = "";
        upLeft = new GeoPoint();
        downRight = new GeoPoint();
    }


    /**
     * @param center
     * @param x2
     * @param y2
     */
    public SubRegion(GeoPoint center, double x2, double y2) {
        double pixelResolution = 0.8;
        long x = Math.round(x2 / pixelResolution);
        long y = Math.round(y2 / pixelResolution);
        upLeft = new GeoPoint();
        downRight = new GeoPoint();
        SRTM30_Files_DB SRTM30DataBaseFiles = SRTM30_Files_DB.getInstance();
        demName = SRTM30DataBaseFiles.locateDEMForPoint(center);
        DEMInfoDB demInfo = DEMInfoDB.getInstance().getDEMInfo(demName);

        if (demInfo != null) {
            double resolution = DEMInfoDB.angleResolution;
            upLeft.setPoint(center.getLatitude() + (y / 2) * resolution,
                    center.getLongitude() - (x / 2) * resolution);
            downRight.setPoint(center.getLatitude() - (y / 2) * resolution,
                    center.getLongitude() + (x / 2) * resolution);

            // ----------check upper border----------
            if (upLeft.getLatitude() < demInfo.getMaxLatitude()) {
                offsetY = (int) Math.round(Math.abs
                        (upLeft.getLatitude() - demInfo.getMaxLatitude())
                        / 0.008333333333333);
            } else {
                offsetY = 0;
            }
            // ----------check left border----------
            if (upLeft.getLongitude() > demInfo.getMinLongitude()) {
                offsetX = (int) Math.round(Math.abs
                        (upLeft.getLongitude() - demInfo.getMinLongitude())
                        / 0.008333333333333);
            } else {
                offsetX = 0;
            }
            // ----------check lower border----------
            if (downRight.getLatitude() > demInfo.getMinLatitude()) {
                int offsetYdown = (int)Math.round(Math.abs
                        (downRight.getLatitude() - demInfo.getMaxLatitude())
                        / 0.008333333333333);
                sizeY = (int)Math.round(Math.abs(offsetY - offsetYdown));
            } else {
                sizeY = Math.abs(offsetY - demInfo.getDemHeight());                
            }
            // --------check right border----------
            if (downRight.getLongitude() < demInfo.getMaxLongitude()) {
                int offsetXdown = (int) Math.round(Math.abs
                        (downRight.getLongitude() - demInfo.getMinLongitude())
                        / 0.008333333333333);
                sizeX = Math.abs(offsetX - offsetXdown);
            } else {
                sizeX = Math.abs(offsetX - demInfo.getDemWidth());
            }
        }
    }


    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public String getDemName() {
        return demName;
    }


    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }
}
