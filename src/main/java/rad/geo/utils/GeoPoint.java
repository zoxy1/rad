package rad.geo.utils;

/**
 * Created by IntelliJ IDEA.
 * User: crysalis
 * Date: Aug 7, 2006
 * Time: 12:59:22 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Latitude range: -90..+90 (Negative -> to  South)
 * Longitude range: -180..180 (Negative -> to  West)
 */
public class GeoPoint {
    private Double latitude;
    private Double longitude;
    private boolean isCorrect = true;

    /**
     * empty constructor
     */
    public GeoPoint(){
        latitude = 0.0;
        longitude = 0.0;
    }

    /**
     * Constructor for existing coordinates
     * @param lat
     * @param lon
     */
    public GeoPoint(Double lat, Double lon){
        if ((lat >= -90) && (lat <= 90)  &&
            (lon >= -180) && (lon <= 180)) {
            latitude = lat;
            longitude = lon;
        } else {
            System.out.println("[geoPoint] Wrong coordinates. Geo point not available. Setup wrong flag (value = 1000.0) ");
            setIsCorrectFlag(false);
        }
    }


    /**
     * check current coordinates for existence
     * @return boolean
     */
    public boolean isCorrectCoordinates(){
        return ((latitude >= -90) && (latitude <= 90) &&
                (longitude >= -180) && (longitude <= 180));
    }
    /**
     * Write full geo coordinates (TRY to synthesize SRTM file name)
     * @return SRTM filename string
     */
    public String toFullGeoNameString(){
        if (!isCorrect) {
            return "[geoPoint] Damaged GeoPoint. Probably file read error.";
        }
        String lat_suff = (latitude < 0) ? "southen latitude" : "northen latitude";
        String lon_suff = (longitude < 0) ? "western longitude" : "easten longitude";
        return String.format("%1$4.3f degree of %2$-18s %3$4.3f degree of %4$-18s",
                            latitude, lat_suff, longitude, lon_suff);
    }

    public String toString(){
        if (!isCorrect) {
            return "Damaged GeoPoint";
        }
        return String.format("lat %1$4.3f lon %2$4.3f ", latitude, longitude);
    }


    /*----------------------------------------------------------*/
    /*-------------------SETTERS--------------------------------*/
    /*----------------------------------------------------------*/
    public void setIsCorrectFlag(boolean isCorrect){
        this.isCorrect = isCorrect;
    }

    public boolean setLatitude(Double latitude) {
        this.latitude = latitude;
        return isCorrect = isCorrectCoordinates();
    }

    public boolean setLongitude(Double longitude) {
        this.longitude = longitude;
        return isCorrect = isCorrectCoordinates();
    }

    public boolean setPoint(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return isCorrect = isCorrectCoordinates();
    }



    /*----------------------------------------------------------*/
    /*-------------------GETTERS--------------------------------*/
    /*----------------------------------------------------------*/
    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public boolean getIsCorrectFlag(){
        return isCorrect;
    }

}
