package rad.bin;

import platform.Module;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: Name01 Date: 09.11.2006 Time: 11:10:09 To
 * change this template use File | Settings | File Templates.
 */
public class RunnerSGPImmitator extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerSGPImmitator.class);

	void runModule(Module modul) throws Exception {
		// To change body of implemented methods use File | Settings | File
		// Templates.
		try {
			if (!getParBooleanByText("useExternalTraceFile"))
				 {
				TimeZone tz = TimeZone.getTimeZone("GMT");
				TimeZone.setDefault(tz);
				double startTime = getParDoubleByText("startTime");
				int timeStep = new Long(Math
						.round(getParDoubleByText("timeStep"))).intValue();

				double latMin = getParDoubleByText("latMin");
				double latMax = getParDoubleByText("latMax");
				double lonMin = getParDoubleByText("lonMin");
				double lonMax = getParDoubleByText("lonMax");

				double sunElevMin = getParDoubleByText("sunElevAngleMin");
				double sunElevMax = getParDoubleByText("sunElevAngleMax");

				double stepsNum = getParDoubleByText("stepsNum");

				String outFileName = getParStringByText("outFileName");

				double stepForLat = (latMax - latMin) / stepsNum;
				double stepForLon = (lonMax - lonMin) / stepsNum;
				double stepForSunElev = (sunElevMax - sunElevMin) / stepsNum;

				double currentLat = latMin;
				double currentLon = lonMin;
				double currentSunPos = sunElevMin;

				String temp = "";
				temp = String
						.format(
								"%20s   %20s   %20s   %15s   %15s   %15s   %15s   %15s   %15s",
								"#0Time", "#1JulianTime", "#2Latitude",
								"#3Longitude", "#4Altitude", "#5Hemisphere",
								"#6SolarAzimut(CW)", "#7SolarElevAngle",
								"#8TimeZone");

				PrintStream resultFilePrinter = new PrintStream(new File(
						wrkPath + "\\" + outFileName));
				resultFilePrinter.println(temp);

				GregorianCalendar cal = new GregorianCalendar();
				Date dat = cal.getTime();
				SimpleDateFormat SDF = new SimpleDateFormat(
						"yyyy.MM.dd_HH:mm:ss");
				cal.setTimeInMillis(Math.round(startTime));

				for (int i = 0; i < Math.round(stepsNum); i++) {
					String fakeTime = SDF.format(cal.getTime()); // cal.getTime().toString().replaceAll("
																	// ", "_");
					double jultime = ConvertGregorianToJulianCalendar(cal
							.getTimeInMillis());
					temp = String
							.format(
									"%15s    %20.6f  %20.6f   %15.3f   %15d   %15d   %15.1f   %15.3f   %15.1f  ",
									fakeTime, jultime, currentLat, currentLon,
									0, 0, 0.0, currentSunPos, 0.0);
					resultFilePrinter.println(temp);
					currentLat += stepForLat;
					currentLon += stepForLon;
					currentSunPos += stepForSunElev;
					cal.add(Calendar.SECOND, timeStep);
				}

				resultFilePrinter.close();
			} else {
				String trace_fname = getParStringByText("ExternalTraceFileName");
				if (trace_fname.trim().length()==0) {
					throw new Exception("Parametr ExternalTraceFileName is Empty");
				} else {
					copyFile(trace_fname, wrkPath+"/"+getParStringByText("outFileName").trim());
				}
				
			}

		} catch (Exception e) {
			// e.fillInStackTrace();

			localLogger.fatal(e.toString());
			// proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}

	}
}
