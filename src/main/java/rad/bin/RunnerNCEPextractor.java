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

import platform.Conf;
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

public class RunnerNCEPextractor extends RunnerMeteoCIRRAsubTrace {

	HashMap<String, NetcdfFile> ncfHM = new HashMap<String, NetcdfFile>();

	// static Logger localLogger =
	// Logger.getLogger(RunnerMeteoCIRRAsubTrace.class);
	@Override
	void runModule(Module module) throws Exception {
		// извлекаем пути и названия
		try {
			HashMap<String, Double[]> coord = new HashMap<String, Double[]>();
			String ncep_parent_path = getParStringByText("ncep_parent_path");
			String NCEP_T_file_name_prefix = getParStringByText("NCEP_T_file_name_prefix");
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
			// if (time_mode.equalsIgnoreCase("manual")) {
			// NCEP_year = getParStringByText("y");
			// NCEP_month = getParStringByText("m");
			// NCEP_day = getParStringByText("d");
			// NCEP_hour = getParStringByText("h");
			// lat = getParDoubleByText("lat");
			// lon = getParDoubleByText("lon");
			// // TimeZoneNames tz = new TimeZoneNames();
			// // tz.
			// h = new Integer(NCEP_hour);
			// y = new Integer(NCEP_year);
			// d = new Integer(NCEP_day);
			// m = new Integer(NCEP_month);
			// cn2 = new GregorianCalendar(y, m - 1, d, h, 0);
			//
			// } else {
			// coord =
			// readFileToHashMapOfDoubleArrayWDesc(getParStringByText("coordinatesListFile"));
			// Double[] latArr = coord.get("Latitude");
			// Double[] lonArr = coord.get("Longitude");
			// Double[] timeArr = coord.get("JulianTime");
			// if (latArr == null || lonArr == null || timeArr == null) {
			// throw new Exception(
			// "Can't extract lat, lon or time from coordinateListFile: "
			// + getParStringByText("coordinatesListFile"));
			// }
			// lat = latArr[0];
			// lon = lonArr[0];
			// Double jtime = timeArr[0];
			// cn2 = new GregorianCalendar();
			// long tl = convertJulianToGregorianCalendar(jtime);
			// cn2.setTimeInMillis(tl); //несмещенное время
			// String shift_debug_year =
			// getParStringByText("shift_debug_year").trim();
			// if
			// (shift_debug_year.length()==0||Integer.parseInt(shift_debug_year)<=0)
			// {
			// y = cn2.get(Calendar.YEAR); // если shift_debug_year не
			// установлен, то
			// авто-время остается не смещенное
			// } else {
			// y = Integer.parseInt(shift_debug_year); // если shift_debug_year
			// не
			// установлен, то время заменяется на это значение
			// localLogger.warn("Auto year was SHIFTED to: " + y);
			// d = cn2.get(Calendar.DAY_OF_MONTH);
			// h = cn2.get(Calendar.DAY_OF_MONTH);
			// m = cn2.get(Calendar.MONTH)+1;
			// Integer s = cn2.get(Calendar.SECOND);
			// Integer ms = cn2.get(Calendar.MILLISECOND);
			// cn2 = new GregorianCalendar(y, m - 1, d, h, s);
			// long ct = cn2.getTimeInMillis();
			// cn2.setTimeInMillis(ct+ms); // смещенное врем
			// localLogger.debug(cn2.getTime().toString());
			// }
			// d = cn2.get(Calendar.DAY_OF_MONTH);
			// h = cn2.get(Calendar.DAY_OF_MONTH);
			// m = cn2.get(Calendar.MONTH)+1;
			//				
			//
			// // localLogger
			// // .info("Only manual mode for NCEP extraction is implemented");
			// }

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
				cn2.setTimeInMillis(tl); // несмещенное время
				{
					File trans_in = new File(wrkPath +"/"+ getOutFile("trans.in"));
					BufferedWriter BWtrans = new BufferedWriter(new FileWriter(trans_in));
					String str = "";
					str = str + cn2.get(Calendar.YEAR);
					BWtrans.write(str+"\r\n");
					
					str = "";
					str = str + cn2.get(Calendar.DAY_OF_YEAR);
					BWtrans.write(str+"\r\n");
					
					str = "";
					str = str + cn2.get(Calendar.HOUR_OF_DAY);
					BWtrans.write(str+"\r\n");
					
					str = "";
					str = str + Conf.getProperty("trans.time.zone");
					BWtrans.write(str+"\r\n");
					
					str = "";
					str = str + latArr[0];
					BWtrans.write(str+"\r\n");
					
					str = "";
					str = str + lonArr[0];
					BWtrans.write(str+"\r\n");
					
					str = "";
					str = str + Conf.getProperty("trans.solar.elev.angle");
					BWtrans.write(str+"\r\n");
					
					BWtrans.flush();
					BWtrans.close();
					BWtrans = null;
				}
				String shift_debug_year = getParStringByText("shift_debug_year")
						.trim();
				if (shift_debug_year.length() == 0
						|| Integer.parseInt(shift_debug_year) <= 0) {
					y = cn2.get(Calendar.YEAR); // если shift_debug_year не
					// установлен, то авто-время
					// остается не смещенное
				} else {
					y = Integer.parseInt(shift_debug_year); // если
					// shift_debug_year
					// установлен, то
					// время заменяется
					// на это значение
					localLogger.warn("Auto year was SHIFTED to: " + y);
					d = cn2.get(Calendar.DAY_OF_MONTH);
					h = cn2.get(Calendar.DAY_OF_MONTH);
					m = cn2.get(Calendar.MONTH) + 1;
					Integer s = cn2.get(Calendar.SECOND);
					Integer ms = cn2.get(Calendar.MILLISECOND);
					cn2 = new GregorianCalendar(y, m - 1, d, h, s);
					long ct = cn2.getTimeInMillis();
					cn2.setTimeInMillis(ct + ms); // смещенное время
					localLogger.debug(cn2.getTime().toString());
				}
				d = cn2.get(Calendar.DAY_OF_MONTH);
				h = cn2.get(Calendar.DAY_OF_MONTH);
				m = cn2.get(Calendar.MONTH) + 1;

				// localLogger
				// .info("Only manual mode for NCEP extraction is implemented");
			}

			// завершение извлечения путей
			String NCEP_time_back = getParStringByText("time_back");

			int cnt = 0;
			int time_back = Integer.parseInt(NCEP_time_back);
			int wrap_points = getParDoubleByText("wrap_points").intValue();
			File time_file = new File(getOutFile(wrkPath + "/time.dat"));
			BufferedWriter BWT = new BufferedWriter(new FileWriter(time_file));

			BWT.write("Target time point: " + cn2.getTime().toString() + "\n");
			BWT.write("\n");
			BWT.write("NCEP time points are: ");
			BWT.write("\n");

			Moment[] Moments = getMomentArray(cn2, "air", time_back);
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
			process2("air", y, m, d, h, lat, lon, time_back, wrap_points, 0.01,
					477.66, "%8.3f");
			process2("rhum", y, m, d, h, lat, lon, time_back, wrap_points,
					0.01, 302.66, "%8.3f");
			process2("uwnd", y, m, d, h, lat, lon, time_back, wrap_points,
					0.01, 202.66, "%8.3f");
			process2("vwnd", y, m, d, h, lat, lon, time_back, wrap_points,
					0.01, 202.66, "%8.3f");
			processHGT("hgt", y, m, d, h, lat, lon, 0, 1, 1., 32066., "%8.3f");
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

			String outFname;
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

			String NCEP_air_fname = constructNCEPfileName(new Integer(year)
					.toString(), getParStringByText("NCEP_T_file_name_prefix"));

			String NCEP_uwnd_fname = constructNCEPfileName(new Integer(year)
					.toString(),
					getParStringByText("NCEP_uwnd_file_name_prefix"));

			String NCEP_vwnd_fname = constructNCEPfileName(new Integer(year)
					.toString(),
					getParStringByText("NCEP_vwnd_file_name_prefix"));

			String NCEP_rhum_fname = constructNCEPfileName(new Integer(year)
					.toString(),
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

			// Поиск по сетке времени ближайшей к заданной точки
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

			// Поиск по сетке широт ближайшей к заданной точки
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

			// Поиск по сетке долгот ближайшей к заданной точке
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

			Array dlev = lev.read(); // чт массив высот (давлений)
			// NCdump.printArray(dlev, "level", System.out, null);

			// формируем интервал времен, скорее
			// всего одна точка, ближайшая к
			// заданной
			Range timerng = new Range(dtIndex.currentElement(), dtIndex
					.currentElement());

			// формируем интервал широт, скорее всего одна точка, ближайшая к
			// заданной

			Range latrng = new Range(latIndex.currentElement(), latIndex
					.currentElement());

			// формируем интервал долгот, скорее всего одна точка, ближайшая к
			// заданной
			Range lonrng = new Range(lonIndex.currentElement(), lonIndex
					.currentElement());

			// формируем интервал высот, здесь чт все точки по высоте
			Range levrng = new Range(0, lev.getShape()[0] - 1);

			// след. 5 строк - формирование списка интервалов для последующей
			// выборки
			List ranges = new ArrayList();
			ranges.add(timerng);
			ranges.add(levrng);
			ranges.add(latrng);
			ranges.add(lonrng);
			Variable air = air_ncfile.findVariable("air"); // извлекаем
			// переменную
			// по CO2
			ArrayShort.D1 dataAir = (ArrayShort.D1) air.read(ranges).reduce(); // читаем
			// массив CO2 в соотв. с установл. ранее интервалами сеток
			// времени, широты, долготы
			// System.out.println(dataCO2.get(0));
			// System.out.println(dataCO2.get(14));
			// NCdump.printArray(dataAir, "air", System.out, null); //
			// распечатываем
			// весь
			// массив
			// co2 на
			// экране
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

			// новый блок формирования сетки координат, извлечения массивов
			// параметров и
			// записи в результирующие файлы

			double fLat = dlat.getDouble(latIndex);
			double fLon = dlon.getDouble(lonIndex);
			int wrap_points = getParDoubleByText("wrap_points").intValue();
			double grid_step = 2.5;

			// находим северо-западный угол покрывающего участка
			double baseLat = fLat + (wrap_points / 2.) * grid_step;
			double baseLon = fLon - (wrap_points / 2. - 1) * grid_step;

			// проходим от с-з угла слева направо сверху-вниз
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

						// формируем интервал широт, скорее всего одна точка,
						// ближайшая к
						// заданной

						latrng = new Range(latIndex.currentElement(), latIndex
								.currentElement());

						// формируем интервал долгот, скорее всего одна точка,
						// ближайшая к
						// заданной
						lonrng = new Range(lonIndex.currentElement(), lonIndex
								.currentElement());

						// формируем интервал высот, здесь чт все точки по
						// высоте
						levrng = new Range(0, lev.getShape()[0] - 1);

						// след. 5 строк - формирование списка интервалов для
						// последующей
						// выборки
						ranges = new ArrayList();
						ranges.add(timerng);
						ranges.add(levrng);
						ranges.add(latrng);
						ranges.add(lonrng);

						air = air_ncfile.findVariable("air"); // извлекаем
						// переменную по
						// air
						dataAir = (ArrayShort.D1) air.read(ranges).reduce(); // читаем

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

			// завершение блока формирования сетки и извлечения данных

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

	public void process2(String ncep_par_name, int year, int month, int day,
			int hour, Double traceLat, Double traceLon, int time_back,
			int wrap_points, double scale_factor, double add_offset,
			String dataFormat) {
		try {
			// перевод заданных координат в координаты, пригодные для поиска в
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

			// -- перевод

			// Проверка есть ли вообще файл для начальной точки и извлечение
			// сетки давлений

			GregorianCalendar begCN = new GregorianCalendar(year, month - 1,
					day, hour, 0, 0);
			NetcdfFile begFile = getNCEPfile(ncep_par_name, begCN);
			Variable lev = begFile.findVariable("level");
			ArrayFloat.D1 dataLev = (ArrayFloat.D1) lev.read();
			Moment[] Moments = getMomentArray(begCN, ncep_par_name, time_back);
			double grid_step = 2.5;
			Point[] Points = getSimplePointArray(begFile, curLat, curLon,
					grid_step, wrap_points);
			// (ncf, curLat, curLon, grid_step, wrap_points)

			// для заданного параметра
			// запись данных в соответствующий файл
			
			BWT.write(traceLat + " "
					+ (traceLon < 0. ? (360. + traceLon) : traceLon) + "\n");
			BWT.write("" + wrap_points * wrap_points);
			// BWT.write("\n");
			BWT.write(" " + dataLev.getSize());
			// BWT.write("\n");
			BWT.write(" " + Moments.length);
			BWT.write("\n");
			for (int i = 0; i < dataLev.getSize(); i++) {
				BWT.write(" " + dataLev.get(i));
			}
			BWT.write("\n");

			// По всем точкам и по всем моментам извлекаем массив значений и
			// записываем в файл
			for (int i = 0; i < Points.length; i++) {
				BWT.write(Points[i].getLat() + " " + Points[i].getLon());
				BWT.write("\n");
				for (int j = 0; j < Moments.length; j++) {
					ArrayShort.D1 data = (ArrayShort.D1) getData4PointAndMoment(
							Points[i], Moments[j], lev);
					for (int k = 0; k < data.getSize(); k++) {
						BWT.write(" "
								+ String.format(dataFormat, (data.get(k)
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

	public void processHGT(String ncep_par_name, int year, int month, int day,
			int hour, Double traceLat, Double traceLon, int time_back,
			int wrap_points, double scale_factor, double add_offset,
			String dataFormat) {
		try {
			// перевод заданных координат в координаты, пригодные для поиска в
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

			// -- перевод

			// Проверка есть ли вообще файл для начальной точки и извлечение
			// сетки давлений

			GregorianCalendar begCN = new GregorianCalendar(year, month - 1,
					day, hour, 0, 0);
			NetcdfFile begFile = getNCEPfile(ncep_par_name, begCN);
			Variable lev = begFile.findVariable("level");
			ArrayFloat.D1 dataLev = (ArrayFloat.D1) lev.read();
			Moment[] Moments = getMomentArray(begCN, ncep_par_name, time_back);
			double grid_step = 2.5;
			Point[] Points = getSimplePointArray(begFile, curLat, curLon,
					grid_step, wrap_points);
			// (ncf, curLat, curLon, grid_step, wrap_points)

			// для заданного параметра
			// запись данных в соответствующий файл

			// BWT.write(traceLat + " " + traceLon + "\n");
			// BWT.write("" + wrap_points * wrap_points);
			// BWT.write("\n");
			// BWT.write(" " + dataLev.getSize());
			// BWT.write("\n");
			// BWT.write(" " + Moments.length);
			// BWT.write("\n");
			// for (int i = 0; i < dataLev.getSize(); i++) {
			// BWT.write(" " + dataLev.get(i));
			// }
			// BWT.write("\n");

			// По всем точкам и по всем моментам извлекаем массив значений и
			// записываем в файл
			for (int i = 0; i < Points.length; i++) {
				// BWT.write(Points[i].getLat() + " " + Points[i].getLon());
				// BWT.write("\n");
				for (int j = 0; j < Moments.length; j++) {
					ArrayShort.D1 data = (ArrayShort.D1) getData4PointAndMoment(
							Points[i], Moments[j], lev);
					for (int k = 0; k < data.getSize(); k++) {

						BWT.write(" "
								+ String.format(dataFormat, (data.get(k)
										* scale_factor + add_offset) / 1000.));
						BWT.write("\t" + dataLev.get(k) + "\n");

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

	private Array getData4PointAndMoment(Point point, Moment moment,
			Variable lev) throws Exception {
		ArrayList<Range> ranges = new ArrayList();
		Range rtime = new Range(moment.getIndex(), moment.getIndex());
		Range rlev = new Range(0, lev.getShape()[0] - 1);
		Range rlat = new Range(point.getLatIndex(), point.getLatIndex());
		Range rlon = new Range(point.getLonIndex(), point.getLonIndex());
		ranges.add(rtime);
		ranges.add(rlev);
		ranges.add(rlat);
		ranges.add(rlon);
		Array dataArr = moment.getMainVar().read(ranges).reduce();
		return dataArr;
	}

	/**
	 * Формирует массив из точек окружающих заданную в <b>curLat</b> и
	 * <b>curLon</b>
	 * 
	 * @param ncf
	 *            файл NetcdfFile в котором производится поиск
	 * @param curLat
	 *            широта заданной точки
	 * @param curLon
	 *            долгота заданной точки
	 * @param grid_step
	 *            шаг сетки
	 * @param wrap_points
	 *            количество окружающих точек (по одной стороне квадрата)
	 * @return массив точек Point[wrap_points * wrap_points] с левого верхнего
	 *         угла по строчно по широтам
	 * @throws Exception
	 *             выбрасывает все внутренние исключения
	 */
	private Point[] getSimplePointArray(NetcdfFile ncf, Double curLat,
			Double curLon, double grid_step, int wrap_points) throws Exception {
		// Поиск по сетке широт ближайшей к заданной точки
		Variable lat = ncf.findVariable("lat");
		Variable lon = ncf.findVariable("lon");

		// чтение массива широт
		Array dlat = lat.read();
		Index latIndex = dlat.getIndex();
		// получение индекса из массива широт
		latIndex.set(0);
		// ищем широту ближайшую к заданной
		if (searchLatIndex(curLat.doubleValue(), latIndex, 0, dlat)) {
			localLogger.debug("Searched Latitude: " + curLat);
			localLogger.debug("Found    Latitude: " + dlat.getDouble(latIndex));
		} else {
			localLogger.debug("Searched Latitude: " + curLat);
			localLogger.debug("But nothing found :-(, sorry ");
			throw new Exception("Searched Latitude not found: " + curLat);
		}

		// Поиск по сетке долгот ближайшей к заданной точке
		Array dlon = lon.read();
		Index lonIndex = dlon.getIndex();
		lonIndex.set(0);
		// ищем долготу ближайшую к заданной
		if (searchLonIndex(curLon.doubleValue(), lonIndex, 0, dlon)) {
			localLogger.debug("Searched Longitude: " + curLon);
			localLogger
					.debug("Found    Longitude: " + dlon.getDouble(lonIndex));
		} else {
			localLogger.debug("Searched Longitude: " + curLon);
			localLogger.debug("But nothing found :-(, sorry ");
			throw new Exception("Searched Longitude not found: " + curLon);
		}
		// Получение координат точки находящейся ближе к данной
		double fLat = dlat.getDouble(latIndex);
		double fLon = dlon.getDouble(lonIndex);

		// широта для поиска должна быть положительной 0..360
		int ifLat = (int) Math.abs(fLat) / 360;
		if (fLat > 0) {
			fLat = fLat - 360 * ifLat;
		} else if (fLat < 0) {
			fLat = fLat + 360 * (ifLat + 1);

		}

		// определение северозападной точки покрывающего квадрата
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
		// выходной массив точек квадрта
		Point gridPoint[] = new Point[wrap_points * wrap_points];
		// счетчик массива точек
		int cntp = 0;
		// определяем четверть по широте, исп. для определения систуации
		// перехода через полюс
		int prevMod90 = (int) fLat / 90;

		// признак перехода полюса если четверть северозападной и ближайшей
		// точки
		// находятся по разную стороны от полюса или сев. или юж.
		boolean overPole = ((((int) baseLat / 90) % 2 != 0) & (prevMod90 % 2 == 0));
		// слагаемое добовляемое к долготе при переходе через полюс
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
	 * Возвращает массив моментов времени для конкретной переменной имя которой
	 * укзано в ncep_par_name
	 * 
	 * @param begCN
	 *            начальный момент времени
	 * @param ncep_par_name
	 *            имя переменной NCEP
	 * @param time_back
	 *            количество часов назад от текущей даты для предсказания погоды
	 * @return массив моментов Moment[]
	 * @throws Exception
	 */
	private Moment[] getMomentArray(GregorianCalendar begCN,
			String ncep_par_name, int time_back) throws Exception {
		GregorianCalendar cCN = new GregorianCalendar();
		cCN
				.setTimeInMillis(begCN.getTimeInMillis()
						- (time_back * 3600 * 1000));
		NetcdfFile curNC;
		ArrayList<Moment> ALM = new ArrayList<Moment>();
		do {
			cCN.setTimeInMillis(cCN.getTimeInMillis() + (6 * 3600 * 1000));
			curNC = getNCEPfile(ncep_par_name, cCN);
			long search_hr = Calendar2EpochHours(cCN);
			Variable time = curNC.findVariable("time");
			Array dtime = time.read();
			Index dtIndex = dtime.getIndex();
			Variable mainVar = curNC.findVariable(ncep_par_name);

			// searchTimeIndex(search_hr, dtIndex, 0, dtime);

			if (searchTimeIndex(search_hr, dtIndex, 0, dtime)) {

				int y = cCN.get(Calendar.YEAR);
				int m = cCN.get(Calendar.MONTH);
				int d = cCN.get(Calendar.DAY_OF_MONTH);
				int h = cCN.get(Calendar.HOUR_OF_DAY);

				localLogger.debug("Searched time: " + search_hr + "(" + y + "."
						+ (m + 1) + "." + d + " " + h + ":00:00" + ")");
				GregorianCalendar fCN = EpochHours2Calendar(dtime
						.getInt(dtIndex));

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

	private GregorianCalendar EpochHours2Calendar(long ehrs) {
		GregorianCalendar GC = new GregorianCalendar(1, 0, 1, 0, 0, 0);
		GC.setTimeInMillis(GC.getTimeInMillis() + (ehrs * 3600 * 1000));
		return GC;
	}

	public static long Calendar2EpochHours(GregorianCalendar ccn) {

		long time_1970 = -(new GregorianCalendar(1, 0, 1, 0, 0)
				.getTimeInMillis());
		long after_1970 = ccn.getTimeInMillis();
		long total = ((time_1970 + after_1970) / 1000) / 3600;
		return total;
	}

	public static long Calendar2EpochHours(Date date) {
		long time_1970 = -(new GregorianCalendar(1, 0, 1, 0, 0)
				.getTimeInMillis());
		long after_1970 = date.getTime();
		long total = ((time_1970 + after_1970) / 1000) / 3600;
		return total;
	}

	private NetcdfFile getNCEPfile(String ncep_par_name, GregorianCalendar begCN)
			throws Exception {

		String ncep_parent_path = getParStringByText("ncep_parent_path");
		String ncName = constructNCEPfileName("" + begCN.get(Calendar.YEAR),
				ncep_par_name);

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
	 * Записывает данные извлеченные ранее в getSimplePointArray в файл
	 * 
	 * @param file
	 *            файл для записи
	 * @param point
	 *            координаты искомой точки
	 * @param points
	 *            список координат точек покрывающего квадрата
	 * @param Data
	 *            данные извлеченные из netCDF
	 * @param Pressure
	 *            давление
	 * @param dataFormat
	 *            формат записи для данных
	 * @throws Exception
	 *             выбрасывает все исключения
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
	 * Записывает данные для отображения в gnuPlot
	 * 
	 * @param file
	 *            файл для записи
	 * @param point
	 *            координаты искомой точки
	 * @param points
	 *            список координат точек покрывающего квадрата
	 * @param Data
	 *            данные извлеченные из netCDF
	 * @param Pressure
	 *            давление
	 * @param dataFormat
	 *            формат записи для данных
	 * @throws Exception
	 *             выбрасывает все исключения
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
	 * Конструирование имени файла NCEP
	 * 
	 * @param year
	 *            год за который извлекаются данные
	 * @param prefix
	 *            префикс имени в соответствии с основной переменной, наприме
	 *            air или rhum и др.
	 * @return возвращает сконструированное имя файла
	 */
	public static String constructNCEPfileName(String year, String prefix) {
		String NCEPfname; // air.2003.nc

		NCEPfname = prefix.trim() + "." + year.trim() + ".nc";

		return NCEPfname;
	}

	/**
	 * Возвращает временную точку сетки файла ближайшую к заданной
	 * 
	 * @param search_hr
	 *            искомое время в часах, например от начала эры или года
	 * @param dtIndex
	 *            индекс массива сетки времени файла netCDF по которому
	 *            производится поиск, при успешном поиске <b>dtIndex </b>
	 *            указывает на найденный элемент в массиве <b> dtime </b>
	 * @param shape_num
	 *            размер массива сетки
	 * @param dtime
	 *            массив времени
	 * @return - true ecли близкое значение найдено, иначе false
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
				break; // если нашли точку на сетки времени nc файла, то
				// выходим из цикла, найденная точка запоминается в
				// dtIndex
			} else if (search_hr == dtime.getDouble(dtIndex.set(i))) {
				strike = true;
				break;
			}
		}
		return strike;
	}

	/**
	 * Производит поиск на сетке долгот точку ближайшую к заданной
	 * 
	 * @param traceLon -
	 *            заданная долгота
	 * @param lonIndex -
	 *            индекс файла netCDF по которому производится поиск, при
	 *            успешном поиске <b>lonIndex </b> указывает на найденный
	 *            элемент в <b> dlon </b>
	 * @param shape_num -
	 *            количество точек сетки
	 * @param dlon -
	 *            массив точек из netCDF
	 * @return - true ecли близкое значение найдено, иначе false
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
				break; // если нашли точку на сетке долгот (lon) nc файла, то
				// выходим из цикла, найденная точка запоминается в
				// LonIndex
			}
		}
		return strike;
	}

	/**
	 * Производит поиск на сетке широт точку ближайшую к заданной
	 * 
	 * @param traceLat -
	 *            заданная широта
	 * @param latIndex -
	 *            индекс файла netCDF по которому производится поиск, при
	 *            успешном поиске <b>latIndex </b> указывает на найденный
	 *            элемент в <b>dlat</b>
	 * @param shape_num -
	 *            количество точек сетки
	 * @param dlat -
	 *            массив точек из netCDF
	 * @return - true ecли близкое значение найдено, иначе false
	 */
	public static boolean searchLatIndex(double traceLat, Index latIndex,
			int shape_num, Array dlat) {
		boolean strike = false;
		int[] latshape = dlat.getShape();
		// Переводим curLat в границы сетки исключаем полное количество оборотов
		// 360 град
		double curLat; // = (360 > Math.abs(traceLon)) ? traceLon
		// : ((traceLon / 360 - (int) (traceLon / 360)) * 360);
		// Умножаем на 10000 для увеличения точности при делении
		int icurLat;// = (int)curLon * 10000;
		int i360 = 360 * 10000;
		int itraceLat = (int) (traceLat * 10000);

		icurLat = (i360 > Math.abs(itraceLat)) ? itraceLat : itraceLat
				- (itraceLat / i360) * i360;
		// возвращаем исходное значениеы
		curLat = icurLat / 10000.;

		// Если значение меньше нуля, прибавляем 360, чтобы получить целое
		if (curLat < 0) {
			curLat = 360 + curLat;
		}

		// Приводим значение к диапазону -90..+90
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
		// поиск точки
		if (curLat >= 0) { // если искомое больше нуля то ищем от начала
			// массива к концу
			for (int i = 0; i <= latshape[0] - 1; i++) {
				if (curLat >= dlat.getFloat(latIndex.set(i))) {
					strike = true;
					break; // если нашли точку на сетке широт nc файла, то
					// выходим из цикла, найденная точка
					// запоминается в latIndex
				}
			}
		} else { // если искомое меньше нуля то ищем от конца массива к
			// началу
			for (int i = latshape[0] - 1; i >= 0; i--) {
				if ((curLat) < (dlat.getFloat(latIndex.set(i)))) {
					latIndex.set(i + 1);
					strike = true;
					break; // если нашли точку на сетке широт nc файла, то
					// выходим из цикла, найденная точка
					// запоминается в latIndex
				}
			}
		}
		return strike;
	}
}

class Point {
	private double Lat;

	private double Lon;

	private int latIndex;

	private int lonIndex;

	Point(double lat, double lon) {
		Lat = lat;
		Lon = lon;
	}

	public double getLat() {
		return Lat;
	}

	public void setLat(double lat) {
		Lat = lat;
	}

	public double getLon() {
		return Lon;
	}

	public void setLon(double lon) {
		Lon = lon;
	}

	public void setLatIndex(int latIndex) {
		this.latIndex = latIndex;
	}

	public void setLonIndex(int lonIndex) {
		this.lonIndex = lonIndex;
	}

	public Point(double lat, double lon, int latIndex, int lonIndex) {
		super();
		Lat = lat;
		Lon = lon;
		this.latIndex = latIndex;
		this.lonIndex = lonIndex;
	}

	public int getLatIndex() {
		return latIndex;
	}

	public int getLonIndex() {
		return lonIndex;
	}
}

/**
 * Хранит значение времени, обычно для MomentArray при формировании
 * последовательности данных для MeteoInt вместе со значениями времени хранит
 * также индекс указывающий на этот момент в netCDF
 * 
 * @author admin
 * 
 */
class Moment {
	private NetcdfFile ncFile;

	private Variable mainVar;

	private int year;

	private int month;

	private int day;

	private int hour;

	private long ncep_hour;

	private int index;

	public Moment(NetcdfFile ncFile, Variable mainVar, int year, int month,
			int day, int hour, long ncep_hour, int index) {
		super();
		this.ncFile = ncFile;
		this.mainVar = mainVar;
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.ncep_hour = ncep_hour;
		this.index = index;
	}

	// public Moment(NetcdfFile ncFile, int year, int month, int day, int hour,
	// long ncep_hour, long index) {
	// super();
	// this.ncFile = ncFile;
	// this.year = year;
	// this.month = month;
	// this.day = day;
	// this.hour = hour;
	// this.ncep_hour = ncep_hour;
	// this.index = index;
	// }

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public long getNcep_hour() {
		return ncep_hour;
	}

	public void setNcep_hour(long ncep_hour) {
		this.ncep_hour = ncep_hour;
	}

	public NetcdfFile getNcFile() {
		return ncFile;
	}

	public void setNcFile(NetcdfFile ncFile) {
		this.ncFile = ncFile;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Variable getMainVar() {
		return mainVar;
	}

	public void setMainVar(Variable mainVar) {
		this.mainVar = mainVar;
	}

}
