package rad.bin;

import java.util.GregorianCalendar;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;
import platform.Module;
import rad.geo.strm.SRTMPropertyStorage;
//import sun.util.calendar.JulianCalendar;

import org.apache.log4j.*;
import java.io.*;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.Dimension;
import ucar.nc2.Attribute;
import ucar.ma2.*;
import java.util.*;

//import org.jscience.ml.om.util.DateConverter;

class coefline {
	coefline() {
	};

	double a, b;
}

public class RunnerClouds extends Runner {

	void linecoef(double x1, double y1, double x2, double y2, coefline c) {
		c.a = (y2 - y1) / (x2 - x1);
		c.b = y1 - c.a * x1;
	};

	public void runModule(Module modul) throws Exception,
			java.lang.NullPointerException {
		try {
			// Здесь вставляется основная часть кода по маске облачности
			System.out.println("This is message from RunnerClouds");
			// Это пример извлечения строкового параметра который прописан в
			// par.xml
			NetcdfFile dataFile = null;

			String fname = getParStringByText("DataFileName");
			dataFile = NetcdfFile.open(fname, null);
			if (dataFile == null) {
				throw new Exception("NetcdfFile.open return null with fname = "
						+ fname);
			}
			;

			// все результаты (файлы) пишутся в каталог output/worker_dir
			// пример создания файла в каталоге результатов worker_dir

			HashMap<String, Double[]> coord = readFileToHashMapOfDoubleArrayWDesc(getParStringByText("inputTraceFileName"));

			Double plonc[] = coord.get("Longitude");
			Double ptimec[] = coord.get("JulianTime");
			Double platc[] = coord.get("Latitude");

			String outFileName = wrkPath + "/"
					+ getParStringByText("outCloudMaskFileName");
			File file = new File(outFileName);
			BufferedWriter BW = new BufferedWriter(new FileWriter(file));
			// #0Time #1JulianTime #2Latitude #3Longitude #4Altitude
			// #5Hemisphere #6SolarAzimut(CW) #7SolarElevAngle #8TimeZone
			BW.write("#0Time \t\t #1Latitude \t #2Longitude \t #3Value \t #4Albedo \t #5Transmittance\n");

			for (int i = 0; i < platc.length; i++) {
				long tl = convertJulianToGregorianCalendar(ptimec[i]);
				GregorianCalendar JGD = new GregorianCalendar();
				JGD.setTimeInMillis(tl);
				long ncep_hrs = rad.bin.RunnerNCEPextractor
						.Calendar2EpochHours(JGD);
				GregorianCalendar GC = new GregorianCalendar(JGD
						.get(Calendar.YEAR), 0, 1, 0, 0);
				long year_begin = rad.bin.RunnerNCEPextractor
						.Calendar2EpochHours(GC);

				// CloudInfo CI = formMask(dataFile, platc[i], plonc[i],
				// ptimec[i]);
				// new Double(ncep_hrs-year_begin)
				CloudInfo CI = formMask(dataFile, platc[i], plonc[i], new Double(ncep_hrs-year_begin));
				BW.write(String.format("%6.3f", (new Double(CI.getTime())))
						+ "\t ");
				BW.write(String.format("%12.3f", (new Double(CI.getLat())))
						+ "\t ");
				BW.write(String.format("%12.3f", (new Double(CI.getLon())))
						+ "\t ");
				BW.write(String.format("%8.4f", (new Double(CI.getValue())))
						+ "\t ");
				BW.write(String.format("%8.4f", (new Double(CI.getAlbedo())))
						+ "\t ");
				BW.write(String.format("%8.4f", (new Double(CI.getTrans())))
						+ "\n");
			}

			BW.flush();
			BW.close();

			// CloudInfo CI = formMask(dataFile, platc, plonc, ptimec)
			// BW.write("lat = " + platc.toString()+ " \n");
			// BW.write("lon = " + plonc.toString()+ " \n");
			// BW.write("time = " + p.toString()+ " \n");
			//			
			// BW.write("Value = " + String.valueOf(res)+ " \n");
			// BW.write("Albedo = " + String.valueOf(al)+ " \n");
			// BW.write("Transmission = " + String.valueOf(tr)+ " \n");
			// запись текущего времени

			// при повторном запуске все предыдущие результаты стираются
			// но все файлы просматриваемые в этом каталоге нужно
			// закрывать иначе они не обновятся
			// чтобы результаты обновить в eclipse нужно давить F5

		} catch (Exception e) {
			// обработка ошибок все исключения пишутся в файл logs/global.log
			Logger.getLogger(RunnerClouds.class).fatal(e.getMessage());
			throw e;
		}

	}

	public CloudInfo formMask(NetcdfFile dataFile, Double platc, Double plonc,
			Double ptimec) throws Exception {
		CloudInfo CI = null;
		// Double platc = getParDoubleByText("first_par");
		Logger.getLogger(RunnerClouds.class).info(platc.toString());
		// Это пример извлечения дробного параметра который прописан в par.xml
		// Double plonc = getParDoubleByText("second_par");
		Logger.getLogger(RunnerClouds.class).info(plonc.toString());
		// System.out.println(plonc);
		// Это пример извлечения дробного параметра который прописан в par.xml
		// Double ptimec = getParDoubleByText("third_par");
		Logger.getLogger(RunnerClouds.class).info(ptimec.toString());
		// System.out.println(ptimec);
		double lonc, latc, timec;
		lonc = plonc.doubleValue();
		latc = platc.doubleValue();
		timec = ptimec.doubleValue();

		Variable tcdc = dataFile.findVariable("tcdc");
		Variable lat = dataFile.findVariable("lat");
		Variable lon = dataFile.findVariable("lon");
		Variable time = dataFile.findVariable("time");

		double maxlat, minlat, maxlon, minlon, maxtime, mintime, mxtime, mntime;
		int timed, lond, latd;
		double dtime, dlon, dlat;
		int timei, loni, lati;
		Array lonar = lon.read();
		Array latar = lat.read();
		Array timear = time.read();

		Index lonind = lonar.getIndex();
		Index latind = latar.getIndex();
		Index timeind = timear.getIndex();

		lond = lon.getDimension(0).getLength() - 1;
		lonind.set(0);
		minlon = lonar.getDouble(lonind);
		lonind.set(lond);
		maxlon = lonar.getDouble(lonind);

		latd = lat.getDimension(0).getLength() - 1;
		latind.set(latd);
		minlat = latar.getDouble(latind);
		latind.set(0);
		maxlat = latar.getDouble(latind);

		timed = time.getDimension(0).getLength() - 1;
		timeind.set(0);
		mintime = timear.getDouble(timeind);
		timeind.set(timed);
		maxtime = timear.getDouble(timeind);

		dtime = (maxtime - mintime) / (timed + 1);
		dlon = (maxlon - minlon) / (lond + 1);
		dlat = (maxlat - minlat) / (latd + 1);

		if (lonc > maxlon)
			lonc = maxlon;
		if (lonc < minlon)
			lonc = minlon;

		if (latc > maxlat)
			latc = maxlat;
		if (latc < minlat)
			latc = minlat;
		mntime = dtime;
		mxtime = dtime * timed - dtime;
		if (timec < mntime)
			timec = mntime;
		if (timec > mxtime)
			timec = mxtime;

		loni = (int) ((lonc - minlon) / dlon);
		lati = (int) ((latc - minlat) / dlat);
		timei = (int) ((timec) / dtime);
		int[] shape = tcdc.getShape();
		int[] origin = new int[tcdc.getRank()];

		origin[0] = timei;
		shape[0] = 2;
		origin[1] = 0;
		shape[1] = latd;
		origin[2] = 0;
		shape[2] = lond;

		Array ar = tcdc.read(origin, shape);
		Index index = ar.getIndex();

		double p[][] = new double[2][4];
		double[] tt = new double[2];
		coefline c1 = new coefline();
		coefline c2 = new coefline();

		double x1lon = loni * dlon + minlon;
		double x2lon = x1lon + dlon;
		double x1lat = lati * dlat + minlat;
		double x2lat = x1lat + dlat;

		for (int i = 0; i < 2; i++) {

			p[i][0] = (ar.getShort(index.set(i, lati, loni)) + 32766) * 1e-4;
			p[i][1] = (ar.getShort(index.set(i, lati, loni + 1)) + 32766) * 1e-4;
			p[i][2] = (ar.getShort(index.set(i, lati + 1, loni + 1)) + 32766) * 1e-4;
			p[i][3] = (ar.getShort(index.set(i, lati + 1, loni)) + 32766) * 1e-4;

			linecoef(x1lon, p[i][0], x2lon, p[i][1], c1);
			linecoef(x1lon, p[i][3], x2lon, p[i][2], c2);
			double np1 = c1.a * lonc + c1.b;
			double np2 = c2.a * lonc + c2.b;
			linecoef(x1lat, np1, x2lat, np2, c1);
			tt[i] = c1.a * latc + c1.b;

		}

		linecoef(dtime * timei, tt[0], dtime * (timei + 1), tt[1], c1);
		double res = c1.a * timec + c1.b;
		double al = 1 - Math.exp(-(6.42 - 5.4 * res) * res);
		double tr = Math.exp(-(6.85 - 3.4 * res) * res);
		CI = new CloudInfo(platc.doubleValue(), plonc.doubleValue(), ptimec
				.doubleValue(), res, al, tr);
		return CI;
	}

}
