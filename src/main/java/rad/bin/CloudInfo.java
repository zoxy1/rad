package rad.bin;

public class CloudInfo {
	double lat;
	double lon;
	double time;
	double value;
	double albedo;
	double trans;
	public CloudInfo(double lat, double lon, double time, double value,
			double albedo, double trans) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.time = time;
		this.value = value;
		this.albedo = albedo;
		this.trans = trans;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public double getAlbedo() {
		return albedo;
	}
	public void setAlbedo(double albedo) {
		this.albedo = albedo;
	}
	public double getTrans() {
		return trans;
	}
	public void setTrans(double trans) {
		this.trans = trans;
	}
}
