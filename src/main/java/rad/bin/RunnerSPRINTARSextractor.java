package rad.bin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList; //import java.util.Calendar;
//import java.util.Date;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;

import platform.Module;
import rad.geo.strm.SRTMPropertyStorage;
//import sun.util.resources.TimeZoneNames;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayShort;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.ArrayShort.D1;
import ucar.nc2.NCdump;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

//import dods.dap.*;

public class RunnerSPRINTARSextractor extends RunnerNCEPextractor {

	HashMap<String, NetcdfFile> ncfHM = new HashMap<String, NetcdfFile>();

	// static Logger localLogger =
	// Logger.getLogger(RunnerMeteoCIRRAsubTrace.class);
	@Override
	void runModule(Module module) throws Exception {
		// ��������� ���� � ��������
		try {
			HashMap<String, Double[]> coord = new HashMap<String, Double[]>();
			// String ncep_parent_path = getParStringByText("ncep_parent_path");
			// String NCEP_T_file_name_prefix =
			// getParStringByText("NCEP_T_file_name_prefix");
			String time_mode = getParStringByText("time_mode");
			String NCEP_year = null;
			String NCEP_month = null;
			String NCEP_day = null;
			String NCEP_hour = null;
			Double lat = null;
			Double lon = null;
			GregorianCalendar cn2;
			Integer h = null; // = Integer.parseInt(NCEP_hour);
			Integer y = null; // = Integer.parseInt(NCEP_year);
			Integer d = null; // = Integer.parseInt(NCEP_day);
			Integer m = null; // = Integer.parseInt(NCEP_month);
			if (time_mode.equalsIgnoreCase("manual")) {
				NCEP_year = getParStringByText("y");
				NCEP_month = getParStringByText("m");
				NCEP_day = getParStringByText("d");
				NCEP_hour = getParStringByText("h");
				lat = getParDoubleByText("lat");
				lon = getParDoubleByText("lon");
				// TimeZoneNames tz = new TimeZoneNames();
				// tz.
				h = new Integer(NCEP_hour);
				y = new Integer(NCEP_year);
				d = new Integer(NCEP_day);
				m = new Integer(NCEP_month);
				cn2 = new GregorianCalendar(y, m - 1, d, h, 0);

			} else {
				coord = readFileToHashMapOfDoubleArrayWDesc(getParStringByText("coordinatesListFile"));
				Double[] latArr = coord.get("Latitude");
				Double[] lonArr = coord.get("Longitude");
				Double[] timeArr = coord.get("JulianTime");
				if (latArr == null || lonArr == null || timeArr == null) {
					throw new Exception(
							"Can't extract lat, lon or time from coordinateListFile: "
									+ getParStringByText("coordinatesListFile"));
				}
				lat = latArr[0];
				lon = lonArr[0];
				Double jtime = timeArr[0];
				cn2 = new GregorianCalendar();
				long tl = convertJulianToGregorianCalendar(jtime);
				cn2.setTimeInMillis(tl);
				h = cn2.get(Calendar.HOUR_OF_DAY);
				y = cn2.get(Calendar.YEAR);
				d = cn2.get(Calendar.DAY_OF_MONTH);
				m = cn2.get(Calendar.MONTH) + 1;

				// localLogger
				// .info("Only manual mode for NCEP extraction is implemented");
			}
			// ���������� ���������� �����
			String SPRINTARS_time_back = getParStringByText("sprintars_time_back");

			int cnt = 0;
			int time_back = Integer.parseInt(SPRINTARS_time_back);
			int wrap_points = getParDoubleByText("sprintars_wrap_points")
					.intValue();
			File time_file = new File(getOutFile(wrkPath + "/sprint_time.dat"));
			BufferedWriter BWT = new BufferedWriter(new FileWriter(time_file));

			BWT.write("SPRINTARS Target time point: "
					+ cn2.getTime().toString() + "\n");
			BWT.write("\n");
			BWT.write("SPRINTARS time points are: ");
			BWT.write("\n");
			String sprint_tau_var_name = getParStringByText("sprintars_tau_var_name");
			String sprint_tau_file_name_prefix = getParStringByText("sprintars_tau_file_name_prefix");
			
			String sprint_ps_var_name = getParStringByText("sprintars_ps_var_name");
			String sprint_ps_file_name_prefix = getParStringByText("sprintars_ps_file_name_prefix");
			String sprint_alfa_var_name = getParStringByText("sprintars_alfa_var_name");
			String sprint_alfa_file_name_prefix = getParStringByText("sprintars_alfa_file_name_prefix");
			
			// NetcdfFile tFileNC =
			// getSPRINTARSfile(sprint_tau_file_name_prefix, cn2);
			Moment[] Moments = getMomentArray(cn2, sprint_tau_var_name,
					sprint_tau_file_name_prefix, time_back);
			GregorianCalendar cn3;

			for (int i = 0; i < Moments.length; i++) {
				cn3 = new GregorianCalendar(Moments[i].getYear(), Moments[i]
						.getMonth(), Moments[i].getDay(), Moments[i].getHour(),
						0, 0);
				BWT.write("(" + (i + 1) + ") ");
				BWT.write(cn3.getTime().toString());
				BWT.write("\n");
			}
			BWT.flush();
			BWT.close();
			this.process2ALFA(sprint_tau_var_name, sprint_tau_file_name_prefix, y,
					m, d, h, lat, lon, time_back, wrap_points, 1., 0., "%8.3f");
			this.process2ALFA(sprint_ps_var_name, sprint_ps_file_name_prefix, y,
					m, d, h, lat, lon, time_back, wrap_points, 1., 0., "%8.3f");
			this.process2ALFA(sprint_alfa_var_name, sprint_alfa_file_name_prefix, y,
					m, d, h, lat, lon, time_back, wrap_points, 1., 0., "%8.3f");
			
			// process2("rhum", y, m, d, h, lat, lon, time_back, wrap_points,
			// 0.01, 302.66, "%8.3f");
			// process2("uwnd", y, m, d, h, lat, lon, time_back, wrap_points,
			// 0.01, 202.66, "%8.3f");
			// process2("vwnd", y, m, d, h, lat, lon, time_back, wrap_points,
			// 0.01, 202.66, "%8.3f");
		} catch (Exception e) {
			// e.fillInStackTrace();
			localLogger.fatal(e.toString());
			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	}

	public long process(String ncep_parent_path, int year, int month, int day,
			int hour, Double traceLat, Double traceLon, int index) {
		// String varName = "lat";
		NetcdfFile air_ncfile = null;
		NetcdfFile uwnd_ncfile = null;
		NetcdfFile vwnd_ncfile = null;
		NetcdfFile rhum_ncfile = null;
		long ret_val = -1;

		try {

			//String outFname;
			Double curLat;
			Double curLon;

			curLon = (360 >= Math.abs(traceLon)) ? traceLon
					: ((traceLon / 360. - (int) (traceLon / 360.)) * 360.);

			if (curLon < 0) {
				curLon = 360 + curLon;
			}

			curLat = (360 >= Math.abs(traceLat)) ? traceLat
					: ((traceLat / 360. - (int) (traceLat / 360.)) * 360.);

			if (curLat < 0) {
				curLat = 360 + curLat;
			}

			switch ((int) (curLat.doubleValue() / 90.)) {
			case 0:
				break;
			case 1:
				curLat = 180. - curLat;
				break;
			case 2:
				curLat = 180. - curLat;
				break;
			case 3:
				curLat = curLat - 360.;
				break;
			}

			// curLat = (90. >= Math.abs(traceLat)) ? traceLat
			// :(Math.abs(traceLat)/360.-Math.floor(Math.abs(traceLat)/360.))*360.;

			String NCEP_air_fname = constructSPRINTARSfileName(
					new Integer(year).toString(),
					getParStringByText("NCEP_T_file_name_prefix"));

			String NCEP_uwnd_fname = constructSPRINTARSfileName(new Integer(
					year).toString(),
					getParStringByText("NCEP_uwnd_file_name_prefix"));

			String NCEP_vwnd_fname = constructSPRINTARSfileName(new Integer(
					year).toString(),
					getParStringByText("NCEP_vwnd_file_name_prefix"));

			String NCEP_rhum_fname = constructSPRINTARSfileName(new Integer(
					year).toString(),
					getParStringByText("NCEP_rhum_file_name_prefix"));

			localLogger.debug("Open NetCDF file with Air Temperature[K]: "
					+ ncep_parent_path + "\\" + NCEP_air_fname);
			air_ncfile = NetcdfFile.open(ncep_parent_path + "\\"
					+ NCEP_air_fname);

			// localLogger.debug(air_ncfile.toString());
			vwnd_ncfile = NetcdfFile.open(ncep_parent_path + "\\"
					+ NCEP_vwnd_fname);
			// localLogger.debug(vwnd_ncfile.toString());
			uwnd_ncfile = NetcdfFile.open(ncep_parent_path + "\\"
					+ NCEP_uwnd_fname);
			// localLogger.debug(uwnd_ncfile.toString());
			rhum_ncfile = NetcdfFile.open(ncep_parent_path + "\\"
					+ NCEP_rhum_fname);
			// localLogger.debug(rhum_ncfile.toString());

			Variable lat = air_ncfile.findVariable("lat");
			Variable lon = air_ncfile.findVariable("lon");
			Variable time = air_ncfile.findVariable("time");
			Variable lev = air_ncfile.findVariable("level");
			if (null == lat || null == lon || null == time)
				return -1;
			Array dtime = time.read();
			Index dtIndex = dtime.getIndex();
			dtIndex.set(0);
			int[] dts = dtime.getShape();
			long search_hr;
			long begin_hr;

			GregorianCalendar cn = new GregorianCalendar(1,
					GregorianCalendar.JANUARY, 1, 0, 0);
			long search_hr_2 = cn.getTimeInMillis() / 1000 / 3600;

			GregorianCalendar cn2 = new GregorianCalendar(year, month - 1, day,
					hour, 0);

			localLogger.debug(cn2.getTime().toString());

			search_hr = cn2.getTimeInMillis() / 1000 / 3600;
			// cn2.setTimeInMillis(millis)
			search_hr = search_hr - search_hr_2;

			// ����� �� ����� ������� ��������� � �������� �����
			begin_hr = dtime.getInt(dtIndex.set(0));
			if (searchTimeIndex(search_hr, dtIndex, 0, dtime)) {
				localLogger.debug("Searched time: " + search_hr);
				ret_val = dtime.getInt(dtIndex);
				localLogger.debug("Found    time: " + ret_val);
				// search_hr = search_hr + 6;
			} else {
				localLogger.debug("Searched time: " + search_hr);
				localLogger.debug("But nothing found :-(, sorry ");
				throw new Exception("Searched time not found: " + search_hr);
			}

			// ����� �� ����� ����� ��������� � �������� �����
			Array dlat = lat.read();
			Index latIndex = dlat.getIndex();
			latIndex.set(0);

			if (searchLatIndex(curLat.doubleValue(), latIndex, 0, dlat)) {
				localLogger.debug("Searched Latitude: " + curLat);
				localLogger.debug("Found    Latitude: "
						+ dlat.getDouble(latIndex));
			} else {
				localLogger.debug("Searched Latitude: " + curLat);
				localLogger.debug("But nothing found :-(, sorry ");
				throw new Exception("Searched Latitude not found: " + curLat);
			}

			// ����� �� ����� ������ ��������� � �������� �����
			Array dlon = lon.read();
			Index lonIndex = dlon.getIndex();
			lonIndex.set(0);

			if (searchLonIndex(curLon.doubleValue(), lonIndex, 0, dlon)) {
				localLogger.debug("Searched Longitude: " + curLon);
				localLogger.debug("Found    Longitude: "
						+ dlon.getDouble(lonIndex));
			} else {
				localLogger.debug("Searched Longitude: " + curLon);
				localLogger.debug("But nothing found :-(, sorry ");
				throw new Exception("Searched Longitude not found: " + curLon);
			}

			Array dlev = lev.read(); // �� ������ ����� (��������)
			// NCdump.printArray(dlev, "level", System.out, null);

			// ��������� �������� ������, ������
			// ����� ���� �����, ��������� �
			// ��������
			Range timerng = new Range(dtIndex.currentElement(), dtIndex
					.currentElement());

			// ��������� �������� �����, ������ ����� ���� �����, ��������� �
			// ��������

			Range latrng = new Range(latIndex.currentElement(), latIndex
					.currentElement());

			// ��������� �������� ������, ������ ����� ���� �����, ��������� �
			// ��������
			Range lonrng = new Range(lonIndex.currentElement(), lonIndex
					.currentElement());

			// ��������� �������� �����, ����� �� ��� ����� �� ������
			Range levrng = new Range(0, lev.getShape()[0] - 1);

			// ����. 5 ����� - ������������ ������ ���������� ��� �����������
			// �������
			List ranges = new ArrayList();
			ranges.add(timerng);
			ranges.add(levrng);
			ranges.add(latrng);
			ranges.add(lonrng);
			Variable air = air_ncfile.findVariable("air"); // ���������
			// ����������
			// �� CO2
			ArrayShort.D1 dataAir = (ArrayShort.D1) air.read(ranges).reduce(); // ������
			// ������ CO2 � �����. � ��������. ����� ����������� �����
			// �������, ������, �������
			// System.out.println(dataCO2.get(0));
			// System.out.println(dataCO2.get(14));
			// NCdump.printArray(dataAir, "air", System.out, null); //
			// �������������
			// ����
			// ������
			// co2 ��
			// ������
			ArrayFloat.D1 dataLev = (ArrayFloat.D1) lev.read();

			int shape_lev[] = lev.getShape();
			Double dblCO2[] = new Double[shape_lev[0]];
			Double dblLev[] = new Double[shape_lev[0]];
			double air_lev[] = new double[shape_lev[0]];
			double rhum_levels[] = null;

			for (int i = 0; i <= shape_lev[0] - 1; i++) {
				dblCO2[i] = (new Double(dataAir.get(i)));// *0.01-477.66;
				dblLev[i] = new Double(dataLev.get(i));
				air_lev[i] = dataLev.get(i);
			}

			Vector<Double[]> vector = new Vector<Double[]>();
			vector.add(dblLev);
			vector.add(dblCO2);
			// writeVectorDoubleToTextFileWDescString(vector, wrkPath,
			// "air.dat",
			// "%12.8e", "#0 pressure #1 air_temp");

			// ����� ���� ������������ ����� ���������, ���������� ��������
			// ���������� �
			// ������ � �������������� �����

			double fLat = dlat.getDouble(latIndex);
			double fLon = dlon.getDouble(lonIndex);
			int wrap_points = getParDoubleByText("wrap_points").intValue();
			double grid_step = 2.5;

			// ������� ������-�������� ���� ������������ �������
			double baseLat = fLat + (wrap_points / 2.) * grid_step;
			double baseLon = fLon - (wrap_points / 2. - 1) * grid_step;

			// �������� �� �-� ���� ����� ������� ������-����
			Point gridPoint[] = new Point[wrap_points * wrap_points];
			double airPoint[][] = new double[wrap_points * wrap_points][];
			double uwndPoint[][] = new double[wrap_points * wrap_points][];
			double vwndPoint[][] = new double[wrap_points * wrap_points][];
			double rhumPoint[][] = new double[wrap_points * wrap_points][];

			int cntp = 0;
			for (int i = 0; i <= (wrap_points - 1); i++) {
				double cLat = baseLat - (grid_step * i);
				for (int j = 0; j <= (wrap_points - 1); j++) {
					double cLon = baseLon + (grid_step * j);
					latIndex.set(0);
					lonIndex.set(0);
					if (searchLatIndex(cLat, latIndex, 0, dlat)
							&& searchLonIndex(cLon, lonIndex, 0, dlon)) {
						localLogger.info("Searched and FOUND cLat, cLon :"
								+ (cLat = dlat.getDouble(latIndex)) + " ,"
								+ (cLon = dlon.getDouble(lonIndex)));

						gridPoint[cntp] = new Point(cLat, cLon);

						timerng = new Range(dtIndex.currentElement(), dtIndex
								.currentElement());

						// ��������� �������� �����, ������ ����� ���� �����,
						// ��������� �
						// ��������

						latrng = new Range(latIndex.currentElement(), latIndex
								.currentElement());

						// ��������� �������� ������, ������ ����� ���� �����,
						// ��������� �
						// ��������
						lonrng = new Range(lonIndex.currentElement(), lonIndex
								.currentElement());

						// ��������� �������� �����, ����� �� ��� ����� ��
						// ������
						levrng = new Range(0, lev.getShape()[0] - 1);

						// ����. 5 ����� - ������������ ������ ���������� ���
						// �����������
						// �������
						ranges = new ArrayList();
						ranges.add(timerng);
						ranges.add(levrng);
						ranges.add(latrng);
						ranges.add(lonrng);

						air = air_ncfile.findVariable("air"); // ���������
						// ���������� ��
						// air
						dataAir = (ArrayShort.D1) air.read(ranges).reduce(); // ������

						Variable uwnd = uwnd_ncfile.findVariable("uwnd");
						// localLogger.debug(uwnd.toString());
						ArrayShort.D1 dataUwnd = (ArrayShort.D1) uwnd.read(
								ranges).reduce();

						Variable vwnd = vwnd_ncfile.findVariable("vwnd");
						// localLogger.debug(vwnd.toString());
						ArrayShort.D1 dataVwnd = (ArrayShort.D1) vwnd.read(
								ranges).reduce();

						Variable rhum = rhum_ncfile.findVariable("rhum");
						// localLogger.debug(rhum.toString());
						Variable rhum_lev = rhum_ncfile.findVariable("level");
						rhum_levels = new double[rhum_lev.getShape()[0]];
						ArrayFloat.D1 rhum_dataLev = (ArrayFloat.D1) rhum_lev
								.read();

						ArrayList ranges_rhum = new ArrayList();
						Range levrng_rhum = new Range(0,
								rhum_lev.getShape()[0] - 1);
						ranges_rhum.add(timerng);
						ranges_rhum.add(levrng_rhum);
						ranges_rhum.add(latrng);
						ranges_rhum.add(lonrng);

						ArrayShort.D1 dataRhum = (ArrayShort.D1) rhum.read(
								ranges_rhum).reduce();

						int air_shp[] = dataAir.getShape();
						int rhum_lev_shp[] = dataRhum.getShape();
						airPoint[cntp] = new double[air_shp[0]];
						uwndPoint[cntp] = new double[air_shp[0]];
						vwndPoint[cntp] = new double[air_shp[0]];
						rhumPoint[cntp] = new double[rhum_lev_shp[0]];

						for (int k = 0; k < air_shp[0]; k++) {
							airPoint[cntp][k] = dataAir.get(k) * 0.01 + 477.66;
							uwndPoint[cntp][k] = dataUwnd.get(k) * 0.01 + 202.66;
							vwndPoint[cntp][k] = dataVwnd.get(k) * 0.01 + 202.66;
						}
						for (int k = 0; k < rhum_lev_shp[0]; k++) {
							rhumPoint[cntp][k] = dataRhum.get(k) * 0.01 + 302.66;
							rhum_levels[k] = rhum_dataLev.get(k);
						}

						cntp++;

					} else {
						throw new Exception(
								"Searched but NOT found cLat, cLon :" + cLat
										+ " ," + cLon);
					}

				}

			}

			writeNCEPdata(new File(getOutFile(wrkPath + "/" + "air" + index
					+ ".dat")), new Point(curLat, curLon), gridPoint, airPoint,
					air_lev, "%8.3f");

			writeNCEPdata(new File(getOutFile(wrkPath + "/" + "uwnd" + index
					+ ".dat")), new Point(curLat, curLon), gridPoint,
					uwndPoint, air_lev, "%8.3f");

			writeNCEPdata(new File(getOutFile(wrkPath + "/" + "vwnd" + index
					+ ".dat")), new Point(curLat, curLon), gridPoint,
					vwndPoint, air_lev, "%8.3f");

			writeNCEPdata(new File(getOutFile(wrkPath + "/" + "rhum" + index
					+ ".dat")), new Point(curLat, curLon), gridPoint,
					rhumPoint, rhum_levels, "%8.3f");

			// writeNCEPdata4GPS(new File(getOutFile(wrkPath + "/"
			// + "air_gps.dat")), new Point(curLat, curLon), gridPoint,
			// airPoint, air_lev, "%8.3f");

			// ���������� ����� ������������ ����� � ���������� ������

		} catch (IOException ioe) {
			ioe.printStackTrace();
			localLogger.fatal(ioe.toString());
			proceesException(ioe, localLogger, this.getClass().getName());
			// log("trying to open " + filename, ioe);
			// throw ioe;
		} catch (InvalidRangeException ire) {
			ire.printStackTrace();
			localLogger.fatal(ire.toString());
			proceesException(ire, localLogger, this.getClass().getName());
			// log("invalid Range for " + varName, e);
		} catch (Exception e) {
			// e.fillInStackTrace();
			e.printStackTrace();
			localLogger.fatal(e.toString());
			proceesException(e, localLogger, this.getClass().getName());
			// throw e;
		} finally {
			if (null != air_ncfile)
				try {
					air_ncfile.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					// log("trying to close " + filename, ioe);
				}
		}
		return ret_val;
	}

	
	public void process2ALFA (String ncep_par_name, String ncep_file_name, int year,
			int month, int day, int hour, Double traceLat, Double traceLon,
			int time_back, int wrap_points, double scale_factor,
			double add_offset, String dataFormat) {
		try {
			// ������� �������� ��������� � ����������, ��������� ��� ������ �
			// netCDF NCEP
			File out = new File(getOutFile(wrkPath + "/" + ncep_par_name.trim()
					+ ".dat"));
			BufferedWriter BWT = new BufferedWriter(new FileWriter(out));
			Double curLat;
			Double curLon;
			{
				curLon = (360 >= Math.abs(traceLon)) ? traceLon
						: ((traceLon / 360. - (int) (traceLon / 360.)) * 360.);

				if (curLon < 0) {
					curLon = 360 + curLon;
				}

				curLat = (360 >= Math.abs(traceLat)) ? traceLat
						: ((traceLat / 360. - (int) (traceLat / 360.)) * 360.);

				if (curLat < 0) {
					curLat = 360 + curLat;
				}

				switch ((int) (curLat.doubleValue() / 90.)) {
				case 0:
					break;
				case 1:
					curLat = 180. - curLat;
					break;
				case 2:
					curLat = 180. - curLat;
					break;
				case 3:
					curLat = curLat - 360.;
					break;
				}
			}

			// -- �������

			// �������� ���� �� ������ ���� ��� ��������� ����� � ����������
			// ����� ��������

			GregorianCalendar begCN = new GregorianCalendar(year, month - 1,
					day, hour, 0, 0);
			NetcdfFile begFile = getSPRINTARSfile(ncep_file_name, begCN);
			// Variable lev = begFile.findVariable("level");
			// ArrayFloat.D1 dataLev = (ArrayFloat.D1) lev.read();
			Moment[] Moments = getMomentArray(begCN, ncep_par_name,
					ncep_file_name, time_back);
			double grid_step = getParDoubleByText("sprintars_grid_step");
			Point[] Points = getSimplePointArray(begFile, curLat, curLon,
					grid_step, wrap_points);
			// (ncf, curLat, curLon, grid_step, wrap_points)

			// ��� ��������� ���������
			// ������ ������ � ��������������� ����

			BWT.write(traceLat + " " + traceLon + "\n");
			BWT.write("" + wrap_points * wrap_points);
			// BWT.write("\n");
			// BWT.write(" " + dataLev.getSize());
			// BWT.write("\n");
			BWT.write(" " + Moments.length);
			BWT.write("\n");
			// for (int i = 0; i < dataLev.getSize(); i++) {
			// BWT.write(" " + dataLev.get(i));
			// }
			//BWT.write("\n");

			// �� ���� ������ � �� ���� �������� ��������� ������ �������� �
			// ���������� � ����

			
			searchClosestPoint("reset");
			Point closPoint = null;
			for (int i = 0; i < Points.length; i++) {
				BWT.write(Points[i].getLat() + " " + Points[i].getLon());
				BWT.write("\n");
				closPoint = searchClosestPoint(traceLat, traceLon, Points[i]);
				for (int j = 0; j < Moments.length; j++) {
					ArrayFloat.D0 data = (ArrayFloat.D0) getData4PointAndMoment(
							Points[i], Moments[j]);
					
					for (int k = 0; k < data.getSize(); k++) {
						BWT.write(" "
								+ String.format(dataFormat, (data.get()
										* scale_factor + add_offset)));

					}
					BWT.write("\n");
				}
				BWT.flush();
			}

			BWT.flush();
			BWT.close();
			File interp = new File(getOutFile(wrkPath + "/" + ncep_file_name +".interp"
					+ ".dat"));
			BufferedWriter BWinterp = new BufferedWriter(new FileWriter(interp));
			ArrayFloat.D0 data = (ArrayFloat.D0) getData4PointAndMoment(
					closPoint, Moments[0]);
			BWinterp.write(String.format("%20s   %20s    %20s", "#0Latitude", "#1Longitude", "#2" +ncep_par_name.trim()+ "\n"));
			BWinterp.write( String.format("%20.6f   %20.6f   %20.6f",closPoint.getLat(),closPoint.getLon(), data.get()) + "\n");
			BWinterp.flush();
			BWinterp.close();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			localLogger.fatal(ioe.toString());
			proceesException(ioe, localLogger, this.getClass().getName());
			// log("trying to open " + filename, ioe);
			// throw ioe;
		} catch (InvalidRangeException ire) {
			ire.printStackTrace();
			localLogger.fatal(ire.toString());
			proceesException(ire, localLogger, this.getClass().getName());
			// log("invalid Range for " + varName, e);
		} catch (Exception e) {
			// e.fillInStackTrace();
			e.printStackTrace();
			localLogger.fatal(e.toString());
			proceesException(e, localLogger, this.getClass().getName());
			// throw e;
		}
	
		
	}

	private double convertLon(double traceLon){
		double curLon; // = (360 > Math.abs(traceLon)) ? traceLon
		// : ((traceLon / 360 - (int) (traceLon / 360)) * 360);

		int icurLon;// = (int)curLon * 10000;
		int i360 = 360 * 10000;
		int itraceLon = (int) (traceLon * 10000);

		icurLon = (i360 > Math.abs(itraceLon)) ? itraceLon : itraceLon
				- (itraceLon / i360) * i360;

		curLon = icurLon / 10000.;

		if (curLon < 0) {
			curLon = 360 + curLon;
		}
		return curLon;
	}
	private double convertLat(double traceLat){
		double curLat; // = (360 > Math.abs(traceLon)) ? traceLon
		// : ((traceLon / 360 - (int) (traceLon / 360)) * 360);
		// �������� �� 10000 ��� ���������� �������� ��� �������
		int icurLat;// = (int)curLon * 10000;
		int i360 = 360 * 10000;
		int itraceLat = (int) (traceLat * 10000);

		icurLat = (i360 > Math.abs(itraceLat)) ? itraceLat : itraceLat
				- (itraceLat / i360) * i360;
		// ���������� �������� ���������
		curLat = icurLat / 10000.;

		// ���� �������� ������ ����, ���������� 360, ����� �������� ����� �������������
		if (curLat < 0) {
			curLat = 360 + curLat;
		}

		// �������� �������� � ��������� -90..+90
		switch ((int) (curLat / 90.)) {
		case 0:
			break;
		case 1:
			curLat = 180. - curLat;
			break;
		case 2:
			curLat = 180. - curLat;
			break;
		case 3:
			curLat = curLat - 360.;
			break;
		}
		return curLat;
	}
	private void searchClosestPoint(String reset) {
		if (reset.equalsIgnoreCase("reset")){
			sCP = null;
		}
		// TODO Auto-generated method stub
		//return null;
	}
	Point sCP = null;
	private Point searchClosestPoint(Double traceLat, Double traceLon,
			Point point) {
		if (sCP==null) { 
			sCP = point;
			return point;
			} else {
				double cLon = convertLon(traceLon);
				double cLat = convertLat(traceLat);
				double latDist = cLat - convertLat(sCP.getLat());
				double lonDist = cLon - convertLon( sCP.getLon());
				double etalon = Math.sqrt(Math.pow(latDist, 2) + Math.pow(lonDist, 2));
				double nLatDist = cLat - convertLat(point.getLat());
				double nLonDist = cLon - convertLon(point.getLon());
				double newdist = Math.sqrt(Math.pow(nLatDist, 2) + Math.pow(nLonDist, 2));
				if (newdist < etalon) {
					sCP = point;
					return point;
				}
			}
		
		// TODO Auto-generated method stub
		return sCP;
	}



	public void process2(String ncep_par_name, String ncep_file_name, int year,
			int month, int day, int hour, Double traceLat, Double traceLon,
			int time_back, int wrap_points, double scale_factor,
			double add_offset, String dataFormat) {
		try {
			// ������� �������� ��������� � ����������, ��������� ��� ������ �
			// netCDF NCEP
			File out = new File(getOutFile(wrkPath + "/" + ncep_par_name.trim()
					+ ".dat"));
			BufferedWriter BWT = new BufferedWriter(new FileWriter(out));
			Double curLat;
			Double curLon;
			{
				curLon = (360 >= Math.abs(traceLon)) ? traceLon
						: ((traceLon / 360. - (int) (traceLon / 360.)) * 360.);

				if (curLon < 0) {
					curLon = 360 + curLon;
				}

				curLat = (360 >= Math.abs(traceLat)) ? traceLat
						: ((traceLat / 360. - (int) (traceLat / 360.)) * 360.);

				if (curLat < 0) {
					curLat = 360 + curLat;
				}

				switch ((int) (curLat.doubleValue() / 90.)) {
				case 0:
					break;
				case 1:
					curLat = 180. - curLat;
					break;
				case 2:
					curLat = 180. - curLat;
					break;
				case 3:
					curLat = curLat - 360.;
					break;
				}
			}

			// -- �������

			// �������� ���� �� ������ ���� ��� ��������� ����� � ����������
			// ����� ��������

			GregorianCalendar begCN = new GregorianCalendar(year, month - 1,
					day, hour, 0, 0);
			NetcdfFile begFile = getSPRINTARSfile(ncep_file_name, begCN);
			// Variable lev = begFile.findVariable("level");
			// ArrayFloat.D1 dataLev = (ArrayFloat.D1) lev.read();
			Moment[] Moments = getMomentArray(begCN, ncep_par_name,
					ncep_file_name, time_back);
			double grid_step = getParDoubleByText("sprintars_grid_step");
			Point[] Points = getSimplePointArray(begFile, curLat, curLon,
					grid_step, wrap_points);
			// (ncf, curLat, curLon, grid_step, wrap_points)

			// ��� ��������� ���������
			// ������ ������ � ��������������� ����

			BWT.write(traceLat + " " + traceLon + "\n");
			BWT.write("" + wrap_points * wrap_points);
			// BWT.write("\n");
			// BWT.write(" " + dataLev.getSize());
			// BWT.write("\n");
			BWT.write(" " + Moments.length);
			BWT.write("\n");
			// for (int i = 0; i < dataLev.getSize(); i++) {
			// BWT.write(" " + dataLev.get(i));
			// }
			//BWT.write("\n");

			// �� ���� ������ � �� ���� �������� ��������� ������ �������� �
			// ���������� � ����
			for (int i = 0; i < Points.length; i++) {
				BWT.write(Points[i].getLat() + " " + Points[i].getLon());
				BWT.write("\n");
				for (int j = 0; j < Moments.length; j++) {
					ArrayFloat.D0 data = (ArrayFloat.D0) getData4PointAndMoment(
							Points[i], Moments[j]);
					for (int k = 0; k < data.getSize(); k++) {
						BWT.write(" "
								+ String.format(dataFormat, (data.get()
										* scale_factor + add_offset)));

					}
					BWT.write("\n");
				}
				BWT.flush();
			}

			BWT.flush();
			BWT.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
			localLogger.fatal(ioe.toString());
			proceesException(ioe, localLogger, this.getClass().getName());
			// log("trying to open " + filename, ioe);
			// throw ioe;
		} catch (InvalidRangeException ire) {
			ire.printStackTrace();
			localLogger.fatal(ire.toString());
			proceesException(ire, localLogger, this.getClass().getName());
			// log("invalid Range for " + varName, e);
		} catch (Exception e) {
			// e.fillInStackTrace();
			e.printStackTrace();
			localLogger.fatal(e.toString());
			proceesException(e, localLogger, this.getClass().getName());
			// throw e;
		}
	}

	private Array getData4PointAndMoment(Point point, Moment moment)
			throws Exception {
		ArrayList<Range> ranges = new ArrayList();
		Range rtime = new Range(moment.getIndex(), moment.getIndex());
		// Range rlev = new Range(0, lev.getShape()[0] - 1);
		Range rlat = new Range(point.getLatIndex(), point.getLatIndex());
		Range rlon = new Range(point.getLonIndex(), point.getLonIndex());
		ranges.add(rtime);
		// ranges.add(rlev);
		ranges.add(rlat);
		ranges.add(rlon);
		Array dataArr = moment.getMainVar().read(ranges).reduce();
		return dataArr;
	}

	/**
	 * ��������� ������ �� ����� ���������� �������� � <b>curLat</b> �
	 * <b>curLon</b>
	 * 
	 * @param ncf
	 *            ���� NetcdfFile � ������� ������������ �����
	 * @param curLat
	 *            ������ �������� �����
	 * @param curLon
	 *            ������� �������� �����
	 * @param grid_step
	 *            ��� �����
	 * @param wrap_points
	 *            ���������� ���������� ����� (�� ����� ������� ��������)
	 * @return ������ ����� Point[wrap_points * wrap_points] � ������ ��������
	 *         ���� �� ������� �� �������
	 * @throws Exception
	 *             ����������� ��� ���������� ����������
	 */
	private Point[] getSimplePointArray(NetcdfFile ncf, Double curLat,
			Double curLon, double grid_step, int wrap_points) throws Exception {
		// ����� �� ����� ����� ��������� � �������� �����
		Variable lat = ncf.findVariable("lat");
		Variable lon = ncf.findVariable("lon");

		// ������ ������� �����
		Array dlat = lat.read();
		Index latIndex = dlat.getIndex();
		// ��������� ������� �� ������� �����
		latIndex.set(0);
		// ���� ������ ��������� � ��������
		if (searchLatIndex(curLat.doubleValue(), latIndex, 0, dlat)) {
			localLogger.debug("Searched Latitude: " + curLat);
			localLogger.debug("Found    Latitude: " + dlat.getDouble(latIndex));
		} else {
			localLogger.debug("Searched Latitude: " + curLat);
			localLogger.debug("But nothing found :-(, sorry ");
			throw new Exception("Searched Latitude not found: " + curLat);
		}

		// ����� �� ����� ������ ��������� � �������� �����
		Array dlon = lon.read();
		Index lonIndex = dlon.getIndex();
		lonIndex.set(0);
		// ���� ������� ��������� � ��������
		if (searchLonIndex(curLon.doubleValue(), lonIndex, 0, dlon)) {
			localLogger.debug("Searched Longitude: " + curLon);
			localLogger
					.debug("Found    Longitude: " + dlon.getDouble(lonIndex));
		} else {
			localLogger.debug("Searched Longitude: " + curLon);
			localLogger.debug("But nothing found :-(, sorry ");
			throw new Exception("Searched Longitude not found: " + curLon);
		}
		// ��������� ��������� ����� ����������� ����� � ������
		double fLat = dlat.getDouble(latIndex);
		double fLon = dlon.getDouble(lonIndex);

		// ������ ��� ������ ������ ���� ������������� 0..360
		int ifLat = (int) Math.abs(fLat) / 360;
		if (fLat > 0) {
			fLat = fLat - 360 * ifLat;
		} else if (fLat < 0) {
			fLat = fLat + 360 * (ifLat + 1);

		}

		// ����������� �������������� ����� ������������ ��������
		double baseLat; // = fLat + (wrap_points / 2.) * grid_step;
		double baseLon; // = fLon - (wrap_points / 2. - 1) * grid_step;

		if (wrap_points % 2 == 0) {
			baseLat = fLat + (grid_step * (int) (wrap_points / 2));
			baseLon = fLon - (grid_step * (int) (wrap_points / 2 - 1));
		} else {
			baseLat = fLat + (grid_step * (int) (wrap_points / 2));
			baseLon = fLon - (grid_step * (int) (wrap_points / 2));
		}
		;
		// �������� ������ ����� �������
		Point gridPoint[] = new Point[wrap_points * wrap_points];
		// ������� ������� �����
		int cntp = 0;
		// ���������� �������� �� ������, ���. ��� ����������� ���������
		// �������� ����� �����
		int prevMod90 = (int) fLat / 90;

		// ������� �������� ������ ���� �������� �������������� � ���������
		// �����
		// ��������� �� ������ ������� �� ������ ��� ���. ��� ��.
		boolean overPole = ((((int) baseLat / 90) % 2 != 0) & (prevMod90 % 2 == 0));
		// ��������� ����������� � ������� ��� �������� ����� �����
		int toggle180 = 0;
		if (overPole) {
			toggle180 = (toggle180 * -1) + 180;
		}
		// System.out.println(" overPole: "+overPole+" prevMod90: "+prevMod90);

		for (int i = 0; i <= (wrap_points - 1); i++) {
			double cLat = baseLat - (grid_step * i);
			ifLat = (int) Math.abs(cLat) / 360;
			if (cLat > 0) {
				cLat = cLat - 360 * ifLat;
			} else if (cLat < 0) {
				cLat = cLat + 360 * (ifLat + 1);

			}

			overPole = ((((int) cLat / 90) % 2 == 0) & (prevMod90 % 2 != 0));

			if (overPole) {
				toggle180 = (toggle180 * -1) + 180;
			}

			// System.out.println(" overPole: " + overPole + " prevMod90: "
			// + prevMod90 + " toggle180: " + toggle180 + " cLat2Search: "
			// + cLat);

			prevMod90 = (int) (cLat / 90);
			for (int j = 0; j <= (wrap_points - 1); j++) {
				double cLon = baseLon + (grid_step * j) + toggle180;
				// cntp++;

				latIndex.set(0);
				lonIndex.set(0);
				if (searchLatIndex(cLat, latIndex, 0, dlat)
						&& searchLonIndex(cLon, lonIndex, 0, dlon)) {
					/*
					 * localLogger.info("Searched and FOUND cLat, cLon :" +
					 * (cLat = dlat.getDouble(latIndex)) + " ," + (cLon =
					 * dlon.getDouble(lonIndex)));
					 */

					cLat = dlat.getDouble(latIndex);
					cLon = dlon.getDouble(lonIndex);
					gridPoint[cntp] = new Point(cLat, cLon, latIndex
							.currentElement(), lonIndex.currentElement());
					// System.out.println(" cntp: " + cntp + " cLat: " + cLat
					// + " cLon: " + cLon);
					cntp++;

					// System.out.println(" cntp: "+cntp+" cLat: "+ cLat + "
					// cLon: " + cLon);
				} else {
					throw new Exception("Searched but NOT found cLat, cLon :"
							+ cLat + " ," + cLon);
				}
			}
		}

		return gridPoint;
	}

	/**
	 * ���������� ������ �������� ������� ��� ���������� ���������� ��� �������
	 * ������ � ncep_par_name
	 * 
	 * @param begCN
	 *            ��������� ������ �������
	 * @param sprintars_par_name
	 *            ��� ���������� NCEP
	 * @param time_back
	 *            ���������� ����� ����� �� ������� ���� ��� ������������ ������
	 * @return ������ �������� Moment[]
	 * @throws Exception
	 */
	private Moment[] getMomentArray(GregorianCalendar begCN,
			String sprintars_par_name, String sprintars_file_name, int time_back)
			throws Exception {
		GregorianCalendar cCN = new GregorianCalendar();
		cCN
				.setTimeInMillis(begCN.getTimeInMillis()
						- (time_back * 3600 * 1000));
		// NetcdfFile curNC;
		NetcdfFile curNC;
		ArrayList<Moment> ALM = new ArrayList<Moment>();
		do {
			cCN.setTimeInMillis(cCN.getTimeInMillis() + (6 * 3600 * 1000));
			curNC = getSPRINTARSfile(sprintars_file_name, cCN);
			long search_hr = Calendar2YearHours(cCN);
			Variable time = curNC.findVariable("time");
			Array dtime = time.read();
			Index dtIndex = dtime.getIndex();
			Variable mainVar = curNC.findVariable(sprintars_par_name);

			// searchTimeIndex(search_hr, dtIndex, 0, dtime);

			if (searchTimeIndex(search_hr, dtIndex, 0, dtime)) {

				int y = cCN.get(Calendar.YEAR);
				int m = cCN.get(Calendar.MONTH);
				int d = cCN.get(Calendar.DAY_OF_MONTH);
				int h = cCN.get(Calendar.HOUR_OF_DAY);

				localLogger.debug("Searched time: " + search_hr + "(" + y + "."
						+ (m + 1) + "." + d + " " + h + ":00:00" + ")");
				GregorianCalendar fCN = YearHours2Calendar(dtime
						.getInt(dtIndex), y);

				y = fCN.get(Calendar.YEAR);
				m = fCN.get(Calendar.MONTH);
				d = fCN.get(Calendar.DAY_OF_MONTH);
				h = fCN.get(Calendar.HOUR_OF_DAY);

				localLogger.debug("Found    time: " + dtime.getInt(dtIndex)
						+ "(" + y + "." + (m + 1) + "." + d + " " + h
						+ ":00:00" + ")");
				ALM.add(new Moment(curNC, mainVar, y, m, d, h, dtime
						.getInt(dtIndex), dtIndex.currentElement()));

				// search_hr = search_hr + 6;
			} else {
				localLogger.debug("Searched time: " + search_hr);
				localLogger.debug("But nothing found :-(, sorry ");
				throw new Exception("Searched time not found: " + search_hr);
			}

		} while (cCN.before(begCN));

		return ALM.toArray(new Moment[ALM.size()]);

	}

	private GregorianCalendar YearHours2Calendar(long ehrs, int year) {
		GregorianCalendar GC = new GregorianCalendar(year, 0, 1, 0, 0, 0);
		GC.setTimeInMillis(GC.getTimeInMillis() + (ehrs * 3600 * 1000));
		return GC;
	}

	public static long Calendar2YearHours(GregorianCalendar ccn) {
		// FIXME ��������� �� ������������� ���������� ����� �� ������ ����
		// �������� � sprint_dat ������������ �����

		// long time_1970 = -(new GregorianCalendar(1, 0, 1, 0, 0)
		// .getTimeInMillis());
		// long after_1970 = ccn.getTimeInMillis();
		// long total = ((time_1970 + after_1970) / 1000) / 3600;
		return ccn.get(ccn.DAY_OF_YEAR) * 24 + ccn.get(ccn.HOUR_OF_DAY);
	}

	public static long Calendar2YearHours(Date date) {
		// FIXME ��������� �� ������������� ���������� ����� �� ������ ����
		// �������� � sprint_dat ������������ �����
		
		// long time_1970 = -(new GregorianCalendar(1, 0, 1, 0, 0)
		// .getTimeInMillis());
		// long after_1970 = date.getTime();
		long total; // = ((time_1970 + after_1970) / 1000) / 3600;

		GregorianCalendar ccn = new GregorianCalendar();
		ccn.setTimeInMillis(date.getTime());
		total = Calendar2YearHours(ccn);
		return total;
	}

	private NetcdfFile getSPRINTARSfile(String ncep_par_name,
			GregorianCalendar begCN) throws Exception {

		String ncep_parent_path = getParStringByText("sprintars_parent_path");
		String ncName = constructSPRINTARSfileName(""
				+ begCN.get(Calendar.YEAR), ncep_par_name);

		NetcdfFile ncFile = ncfHM.get(ncName.trim()); // =
		// NetcdfFile.open(ncep_parent_path
		// + "\\" + ncName);
		if (ncFile == null) {
			ncFile = NetcdfFile.open(ncep_parent_path + "\\" + ncName);
			ncfHM.put(ncName, ncFile);
		}
		return ncFile;
	}

	/**
	 * ���������� ������ ����������� ����� � getSimplePointArray � ����
	 * 
	 * @param file
	 *            ���� ��� ������
	 * @param point
	 *            ���������� ������� �����
	 * @param points
	 *            ������ ��������� ����� ������������ ��������
	 * @param Data
	 *            ������ ����������� �� netCDF
	 * @param Pressure
	 *            ��������
	 * @param dataFormat
	 *            ������ ������ ��� ������
	 * @throws Exception
	 *             ����������� ��� ����������
	 */
	public static void writeNCEPdata(File file, Point point, Point[] points,
			double Data[][], double Pressure[], String dataFormat)
			throws Exception {

		BufferedWriter BW = new BufferedWriter(new FileWriter(file));
		// String LatLon = (new Double(point.getLat())).toString() + " " + (new
		// Double(point.getLon())).toString();
		BW.write("" + point.getLat() + " " + point.getLon() + "\n");
		String wrapPoints = "" + points.length + " ";
		BW.write(wrapPoints);
		if (Data[0].length != Pressure.length) {
			throw new Exception(
					"Data level number NOT equal Pressure level number: "
							+ Data[0].length + " and " + Pressure.length);
		} else {
			BW.write("" + Pressure.length);
		}
		BW.write("\n");
		for (int i = 0; i < Pressure.length; i++) {
			BW.write(" " + Pressure[i]);
		}
		BW.write("\n");
		for (int i = 0; i < points.length; i++) {
			BW.write("" + points[i].getLat() + " " + points[i].getLon() + "\n");
			for (int j = 0; j < Pressure.length; j++) {
				BW.write(" " + String.format(dataFormat, Data[i][j]));
			}
			BW.write("\n");
		}
		BW.flush();
		BW.close();
	}

	/**
	 * ���������� ������ ��� ����������� � gnuPlot
	 * 
	 * @param file
	 *            ���� ��� ������
	 * @param point
	 *            ���������� ������� �����
	 * @param points
	 *            ������ ��������� ����� ������������ ��������
	 * @param Data
	 *            ������ ����������� �� netCDF
	 * @param Pressure
	 *            ��������
	 * @param dataFormat
	 *            ������ ������ ��� ������
	 * @throws Exception
	 *             ����������� ��� ����������
	 */
	public static void writeNCEPdata4GPS(File file, Point point,
			Point[] points, double Data[][], double Pressure[],
			String dataFormat) throws Exception {

		BufferedWriter BW = new BufferedWriter(new FileWriter(file));

		for (int i = 0; i < points.length; i++) { // points.length

			for (int j = 0; j < Pressure.length; j++) { // Pressure.length

				// BW.write(" " + points[i].getLat() + " " +
				// points[i].getLon());
				BW.write(" " + points[i].getLon());

				BW.write(" " + String.format(dataFormat, Data[i][j]) + " "
						+ Pressure[j]);

				BW.write("\n");
			}
			// BW.write("\n");
			// BW.write("\n");
		}
		BW.flush();
		BW.close();
	}

	/**
	 * ��������������� ����� ����� SPRINTARS
	 * 
	 * @param year
	 *            ��� �� ������� ����������� ������
	 * @param prefix
	 *            ������� ����� � ������������ � �������� ����������, ��������
	 *            alfac ��� ps � ��.
	 * @return ���������� ����������������� ��� �����
	 */
	public static String constructSPRINTARSfileName(String year, String prefix) {
		String SPRINTfname; // air.2003.nc

		// SPRINTfname = prefix.trim() + "." + year.trim() + ".nc";
		SPRINTfname = prefix.trim() + ".nc";

		return SPRINTfname;
	}

	/**
	 * ���������� ��������� ����� ����� ����� ��������� � ��������
	 * 
	 * @param search_hr
	 *            ������� ����� � �����, �������� �� ������ ��� ��� ����
	 * @param dtIndex
	 *            ������ ������� ����� ������� ����� netCDF �� ��������
	 *            ������������ �����, ��� �������� ������ <b>dtIndex </b>
	 *            ��������� �� ��������� ������� � ������� <b> dtime </b>
	 * @param shape_num
	 *            ������ ������� �����
	 * @param dtime
	 *            ������ �������
	 * @return - true ec�� ������� �������� �������, ����� false
	 */
	public static boolean searchTimeIndex(double search_hr, Index dtIndex,
			int shape_num, Array dtime) {
		boolean strike = false;
		int[] dts = dtime.getShape();
		for (int i = dts[shape_num] - 1; i >= 0; i--) {
			if (search_hr > dtime.getDouble(dtIndex.set(i))) {
				strike = true;
				if ((i + 1) <= (dts[shape_num] - 1))
					dtIndex.set(i + 1);
				break; // ���� ����� ����� �� ����� ������� nc �����, ��
				// ������� �� �����, ��������� ����� ������������ �
				// dtIndex
			} else if (search_hr == dtime.getDouble(dtIndex.set(i))) {
				strike = true;
				break;
			}
		}
		return strike;
	}

	/**
	 * ���������� ����� �� ����� ������ ����� ��������� � ��������
	 * 
	 * @param traceLon -
	 *            �������� �������
	 * @param lonIndex -
	 *            ������ ����� netCDF �� �������� ������������ �����, ���
	 *            �������� ������ <b>lonIndex </b> ��������� �� ���������
	 *            ������� � <b> dlon </b>
	 * @param shape_num -
	 *            ���������� ����� �����
	 * @param dlon -
	 *            ������ ����� �� netCDF
	 * @return - true ec�� ������� �������� �������, ����� false
	 */
	public static boolean searchLonIndex(double traceLon, Index lonIndex,
			int shape_num, Array dlon) {
		boolean strike = false;
		int[] dts = dlon.getShape();

		double curLon; // = (360 > Math.abs(traceLon)) ? traceLon
		// : ((traceLon / 360 - (int) (traceLon / 360)) * 360);

		int icurLon;// = (int)curLon * 10000;
		int i360 = 360 * 10000;
		int itraceLon = (int) (traceLon * 10000);

		icurLon = (i360 > Math.abs(itraceLon)) ? itraceLon : itraceLon
				- (itraceLon / i360) * i360;

		curLon = icurLon / 10000.;

		if (curLon < 0) {
			curLon = 360 + curLon;
		}

		for (int i = dts[shape_num] - 1; i >= 0; i--) {
			if (curLon >= dlon.getFloat(lonIndex.set(i))) {
				strike = true;
				break; // ���� ����� ����� �� ����� ������ (lon) nc �����, ��
				// ������� �� �����, ��������� ����� ������������ �
				// LonIndex
			}
		}
		return strike;
	}

	/**
	 * ���������� ����� �� ����� ����� ����� ��������� � ��������
	 * 
	 * @param traceLat -
	 *            �������� ������
	 * @param latIndex -
	 *            ������ ����� netCDF �� �������� ������������ �����, ���
	 *            �������� ������ <b>latIndex </b> ��������� �� ���������
	 *            ������� � <b>dlat</b>
	 * @param shape_num -
	 *            ���������� ����� �����
	 * @param dlat -
	 *            ������ ����� �� netCDF
	 * @return - true ec�� ������� �������� �������, ����� false
	 */
	public static boolean searchLatIndex(double traceLat, Index latIndex,
			int shape_num, Array dlat) {
		boolean strike = false;
		int[] latshape = dlat.getShape();
		// ��������� curLat � ������� ����� ��������� ������ ���������� ��������
		// 360 ����
		double curLat; // = (360 > Math.abs(traceLon)) ? traceLon
		// : ((traceLon / 360 - (int) (traceLon / 360)) * 360);
		// �������� �� 10000 ��� ���������� �������� ��� �������
		int icurLat;// = (int)curLon * 10000;
		int i360 = 360 * 10000;
		int itraceLat = (int) (traceLat * 10000);

		icurLat = (i360 > Math.abs(itraceLat)) ? itraceLat : itraceLat
				- (itraceLat / i360) * i360;
		// ���������� �������� ���������
		curLat = icurLat / 10000.;

		// ���� �������� ������ ����, ���������� 360, ����� �������� �����
		if (curLat < 0) {
			curLat = 360 + curLat;
		}

		// �������� �������� � ��������� -90..+90
		switch ((int) (curLat / 90.)) {
		case 0:
			break;
		case 1:
			curLat = 180. - curLat;
			break;
		case 2:
			curLat = 180. - curLat;
			break;
		case 3:
			curLat = curLat - 360.;
			break;
		}
		// ����� �����
		if (curLat >= 0) { // ���� ������� ������ ���� �� ���� �� ������
			// ������� � �����
			for (int i = 0; i <= latshape[0] - 1; i++) {
				if (curLat >= dlat.getFloat(latIndex.set(i))) {
					strike = true;
					break; // ���� ����� ����� �� ����� ����� nc �����, ��
					// ������� �� �����, ��������� �����
					// ������������ � latIndex
				}
			}
		} else { // ���� ������� ������ ���� �� ���� �� ����� ������� �
			// ������
			for (int i = latshape[0] - 1; i >= 0; i--) {
				if ((curLat) < (dlat.getFloat(latIndex.set(i)))) {
					latIndex.set(i + 1);
					strike = true;
					break; // ���� ����� ����� �� ����� ����� nc �����, ��
					// ������� �� �����, ��������� �����
					// ������������ � latIndex
				}
			}
		}
		return strike;
	}
}
