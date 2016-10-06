package rad.bin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import platform.Debug;
import platform.Module;
import platform.Stylizer;
//import sun.util.calendar.JulianCalendar;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.NCdump;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class RunnerMeteoCIRRAsubTrace extends Runner {
	static Logger localLogger = Logger
			.getLogger(RunnerMeteoCIRRAsubTrace.class);

	private byte bt[] = new byte[1024 * 1024];

	private int c = -1;

	@Override
	void runModule(Module module) throws Exception {
		try {
			clearFile("meteo"); // ������� �� �������� ����� � ��������� meteo,
			// �� ��������� ��������
			HashMap<String, Double[]> HMpressParag = null; // ������ ��� ������
			// ����� �� �����
			// PARAG
			String model_path = getParStringByText("model_path");
			String mode = getParStringByText("mode"); // ��������� ��������
			// ������
			// String mode_lat = getParStringByText("mode_lat");
			Double lat = null;
			Double lon = null;
			Double orig_lat = null;
			Double orig_lon = null;
			String month = null;
			Double jultime = null;
			// 1. ����� ���� ���������� ���������� � ���������� � ������� ������
			// �����, ������ ��� �������� �� ����� subTraceInfo.out
			// ������� �������� ������ ���� ������, ����������� splitterom ��
			// ������ subTraceInfo.out
			if (mode.equalsIgnoreCase("manual")) { // ������ ��������� ����� -
				// ������ manual ����� �����
				// � ���������� ����������
				// �������
				lat = getParDoubleByText("latitude"); // ���� ������ manual,
				// ����� ������ ����� �
				// ���������� �� xml
				Debug.println(lat);
				lon = getParDoubleByText("longitude");
				month = getMonthByText(getParStringByText("month").trim()
						.toLowerCase());
			} else { // ���� ��������� ����� - �� ������ (auto) ����� ����� �
				// ���������� ���������� �� ������������� �����
				// (subTraceInfo.out)
				HashMap<String, Double[]> coord = readFileToHashMapOfDoubleArrayWDesc(getParStringByText("coordinatesListFile")); // ������
				// ������������
				// ����
				Double LatArr[] = coord.get("Latitude"); // ��������� ������
				// ��������� ��
				// ������
				Double LonArr[] = coord.get("Longitude"); // ��������� ������
				// ��������� ��
				// �������
				lat = LatArr[0]; // ��������� ����� ��������� �� ������
				 orig_lat = new Double(lat);
				lon = LonArr[0]; // ��������� ����� ��������� �� �������
				 orig_lon = new Double(lon);
				Debug.println(lat);
				Double Time[] = coord.get("JulianTime"); // ���������
				// ��������� �����
				// (������) ��
				// �������������
				// �����
				jultime = Time[0]; // ��������� ����� �� �������
				// JulianCalendar JC = new JulianCalendar();
				// Date dt = new Date();
				month = getMonthByJT(jultime); // ���������� ����� ��
				// ���������� �������, ��
				// 21.01.2007 ��� ��������
				// �����. "jul"
			}

			// String PRESS_out_name = getParStringByText("PRESS_out_name");
			// HMpressParag = readFileToHashMapOfDoubleArray(PRESS_out_name);

			// ���������� ����� 1. ���������� ���������� � ���������� � �������
			// ������ �����, ������ ��� �������� �� ����� subTraceInfo.out

			// ����� ���� ������ ���������� �� CIRA

			String hs; // ������ ���������

			if (lat >= 0.) { // ���� ������ �������. �� ��������
				hs = "n"; // �������� ���������
			} else {
				hs = "s"; // ����� ���������
			}
			lat = Math.abs(lat); // ������ ������

			// ������ ���������������� ����� ������ CIRA � ������ ��������
			HashMap<String, Double[]> Hmodel = readFileToHashMapOfDoubleArray(model_path
					+ "/cira86/" + hs + "ht_" + month + ".txt");
			if (getParStringByText("meteo_model_name").equalsIgnoreCase("cira")) {
				Double[] altModel = Hmodel.get("geom_height"); // ����������
				// ������� �����
				Double[] pressModelmbar = Hmodel.get("pressure"); // ����������
				// �������
				// ��������
				// � mbar
				Double[] pressModelatm = new Double[pressModelmbar.length]; // ����������
				// �������
				// ��������
				// �
				// atm

				// Double[] altSignal = HMpressParag.get("pressure");

				// ����� ��������� ����� ����� - ����� ������� � ����� CIRA
				// ������� ����� ����� � ������ �� �������� ������

				double div = Math.floor(lat / 5);
				long idivLow = Math.round(div * 5);
				long idivHig = Math.round((div + 1) * 5);
				Double[] tempModelHig = Hmodel.get("" + idivHig);
				Double[] tempModelLow = Hmodel.get("" + idivLow);
				Double[] tempModel = new Double[tempModelHig.length];

				// ����� ������������ �������� ������������ ����� ����������
				// �������������� ��������
				// �� ������� ������
				for (int i = 0; i < tempModelHig.length; i++) {

					tempModel[i] = tempModelLow[i]
							+ ((lat - idivLow) * (tempModelHig[i] - tempModelLow[i]))
							/ (idivHig - idivLow);
					pressModelatm[i] = pressModelmbar[i] / 1013;
					// System.out.println(" " + tempModelLow[i] + " " +
					// tempModel[i] + " " + tempModelHig[i]);

				}

				// ���������� ������� ��������� � �������, ��������� �
				// ������������
				// � ������ � cira.dat
				Vector<Double[]> vect = new Vector<Double[]>();

				vect.add(altModel);
				vect.add(pressModelmbar);
				vect.add(tempModel);
				writeVectorDoubleToTextFile(vect, "apt.cira.dat", "%12.8e");
			} else if (getParStringByText("meteo_model_name").equalsIgnoreCase(
					"ncep")) {
				HashMap<String, Double[]> mapMLATMD = readFileToHashMapOfDoubleArrayWDesc("mlatmd_alt.dat");
				Double[] altMLATMD=mapMLATMD.get("altitude"); //press
				throwHashMapException(altMLATMD, "altitude");
				
//				String PRESS_out_name = getParStringByText("PRESS_out_name");
//				HashMap<String, Double[]> mapPRESS_HSTAR = readFileToHashMapOfDoubleArray(PRESS_out_name);
//				Double[] pressHSTAR=mapPRESS_HSTAR.get("pressure"); //press
//				throwHashMapException(pressHSTAR, "pressure");				
				
				HashMap<String, Double[]> mapAP_NCEP = readFileToHashMapOfDoubleArray("hgt.dat");
				Double[] pressNCEP=mapAP_NCEP.get("pressure"); //press
				throwHashMapException(pressNCEP, "pressure");
				
				Double[] altNCEP=mapAP_NCEP.get("altitude"); //press
				throwHashMapException(altNCEP, "altitude");
				
				File resf = getFile("result.dat");
				String st;
				if (resf!=null) {
					BufferedReader BR = new BufferedReader(new FileReader(resf));
					st = BR.readLine().trim();
					if (st.length()==0){
						st = BR.readLine().trim();
					} else {
						String[] starr = st.split("\\s+");
						if (altNCEP.length!=starr.length) {
							throw new Exception("Number of points in altNCEP not equal to ones of result.dat");
						} else {
							Double tempNCEP[] = new Double[starr.length];
							for (int i = 0; i< starr.length; i++){
								tempNCEP[i] = new Double(starr[i]); 
							}
							
							
							Hmodel = readFileToHashMapOfDoubleArray(model_path
									+ "/usa_std_apt.dat");

							if (Hmodel == null) {
								throw new Exception("Can not find/read file" + model_path
										+ "/usa_std_apt.dat");
							}

							Double[] altModel = Hmodel.get("altitude"); // ����������
							// ������� �����
							Double[] pressModelmbar = Hmodel.get("pressure"); // ����������
							// �������
							// ��������
							// � mbar
							Double[] tempModel = Hmodel.get("temperature");
							//Vector<Double[]> vect = new Vector<Double[]>();
							
							double max_alt = altNCEP[altNCEP.length-1];
							int usa_ix = 0;
							for (int i=0;i<altModel.length;i++) {
								if (altModel[i]>max_alt){
									usa_ix = i;
									break;
								}
							}
							double tail_coeff = 1.;
							if (usa_ix>0) {
								double usa_m_temp = tempModel[usa_ix-1];
								double ncep_m_temp = tempNCEP[tempNCEP.length-1];
								tail_coeff = ncep_m_temp/usa_m_temp;
							}
							int new_size = altNCEP.length+altModel.length-usa_ix;
							Double[] altUSA_NCEP = new Double[new_size];
							Double[] pressUSA_NCEP = new Double[new_size];
							Double[] tempUSA_NCEP = new Double[new_size];
							
							for(int i=0;i<altNCEP.length;i++){
								altUSA_NCEP[i] = altNCEP[i];
								pressUSA_NCEP[i] = pressNCEP[i];
								tempUSA_NCEP[i] = tempNCEP[i];
							}
							
							int start = altNCEP.length-1;
							for (int i = usa_ix; i < tempModel.length; i++) {
								start++;
								altUSA_NCEP[start] = altModel[i];
								pressUSA_NCEP[start] = pressModelmbar[i];
								tempUSA_NCEP[start] = tempModel[i]*tail_coeff;
							}
							
							Vector<Double[]> vect = new Vector<Double[]>();
							vect.add(altUSA_NCEP);
							vect.add(pressUSA_NCEP);
							vect.add(tempUSA_NCEP);
							writeVectorDoubleToTextFile(vect, "apt.ncep.dat", "%12.8e");
							
						}
					}
				} else {
					throw new Exception("Ref to result.dat is NULL after getFile invocation");
				}
				
				
				
				/*throw new Exception(
						"Not implemented yet ==> meteo_model_name = ncep");*/
			} else if (getParStringByText("meteo_model_name").equalsIgnoreCase(
					"usa_std")) {
				// throw new Exception("Not implemented yet ==> meteo_model_name
				// = usa_std");
				Hmodel = readFileToHashMapOfDoubleArray(model_path
						+ "/usa_std_apt.dat");

				if (Hmodel == null) {
					throw new Exception("Can not find/read file" + model_path
							+ "/usa_std_apt.dat");
				}

				Double[] altModel = Hmodel.get("altitude"); // ����������
				// ������� �����
				Double[] pressModelmbar = Hmodel.get("pressure"); // ����������
				// �������
				// ��������
				// � mbar
				Double[] tempModel = Hmodel.get("temperature");
				Vector<Double[]> vect = new Vector<Double[]>();

				vect.add(altModel);
				vect.add(pressModelmbar);
				vect.add(tempModel);
				writeVectorDoubleToTextFile(vect, "apt.usa_std.dat", "%12.8e");

			} else {
				throw new Exception(
						"Not implemented yet ==> meteo_model_name = "
								+ getParStringByText("meteo_model_name"));
			}

			/*
			 * 2. ����� ��� �� UniversalExeRunner ��� ������� seeb-afgl �������
			 * ����� ������ ��� ������ � ���������� �������������� � seebor.dat
			 * � afgl.dat
			 */

			// ������������ �������� ����� coord.in ��� seeb-afgl.exe
			File coord_in = new File(wrkPath + "/coord.in");
			BufferedWriter BW = new BufferedWriter(new FileWriter(coord_in));
			Integer day = getDayByTime(jultime);
			BW.write("" + day + "\r\n"); // ���� ���� ��� SEEBOR
			BW.write("" + orig_lat + "\r\n"); // ������
			BW.write("" + orig_lon + "\r\n"); // �������
			File model_afgl = new File(model_path + "/AFGL");
			BW.write("" + model_afgl.getAbsolutePath() + "\\\r\n"); // ���� ��
			// ��������
			// ������
			// AFGL
			File model_seeb = new File(model_path + "/SEEBOR");
			BW.write("" + model_seeb.getAbsolutePath() + "\\\r\n"); // ���� ��
			// ��������
			// ������
			// SEEBOR
			// ����� �������� ������ seeb-afgl.exe
			BW.write("afgl.dat" + "\r\n");
			BW.write("seebor.dat" + "\r\n");
			BW.flush();
			BW.close();

			File execFile = new File("seeb-afgl.exe");
			if (execFile.exists()) {
				System.out.println("File <" + execFile.getAbsolutePath()
						+ "> exist, All Ok.");
				String[] commandLine = new String[1]; // =
				// getAllParametersFromXML(module);
				commandLine[0] = execFile.getAbsolutePath(); // RSTAR_path.trim()
				// + "/"
				// +RSTAR_exec_name.trim();

				Process process, PR;
				ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
				processBuilder.directory(new File(wrkPath));

				FileOutputStream errLogFile = new FileOutputStream((new File(
						this.mdWrkPath + "/" + module.ExecName))
						.getAbsolutePath()
						+ "_Err.log");

				BufferedInputStream InpStream = new BufferedInputStream(
						(PR = processBuilder.start()).getErrorStream());
				BufferedInputStream ConStream = new BufferedInputStream(PR
						.getInputStream());
				BufferedOutputStream CoutStream = new BufferedOutputStream(PR
						.getOutputStream());

				FileOutputStream OutFile = new FileOutputStream((new File(
						this.mdWrkPath + "/" + module.ExecName))
						.getAbsolutePath()
						+ "_Err.log");
				FileOutputStream OutConFile = new FileOutputStream((new File(
						this.mdWrkPath + "/" + module.ExecName))
						.getAbsolutePath()
						+ "_Con.log");

				if (PR == null) {
					Debug.println("Can't create process: " + this.mdWrkPath
							+ "/" + module.ExecName);
				} else {
					/***********************************************************
					 * /???????????????!!!blocks!!!?????????????????? while ((c =
					 * errOut.read(bt)) > 0) { errLogFile.write(bt, 0, c); } /
					 **********************************************************/

					for (int i = 0; i <= 10; i++) {
						byte bt[] = new String("\n").getBytes();
						if (CoutStream != null) {
							CoutStream.write(bt);
							CoutStream.flush();
						}
					}

				} // end of IF (process == null)
				Boolean isRunning = true;
				int exitCode = 0;
				// ?????????????!!!Infinity loop!!!????????????????????
				int cnt = 0;
				while (isRunning) {
					try {
						Thread.sleep(500);
						// cout.read(processInBuff);
						// System.out.println(new String(processInBuff));
						exitCode = PR.exitValue();
						isRunning = false;
					} catch (IllegalThreadStateException ite) {
						if (cnt == 0) {
							System.out
									.println("Process still working... stand by...");
							cnt++;
						} else {
							cnt++;
							System.out.print(".");
							if (Math.round(cnt % 41) == 40) {
								System.out.println(".");
							}
						}
					} finally {

						while (InpStream.available() != 0
								&& (c = InpStream.read(bt)) > 0) {
							OutFile.write(bt, 0, c);
						}

						while (ConStream.available() != 0
								&& (c = ConStream.read(bt)) > 0) {
							// OutFile.write(bt,0,c);
							// System.out.println(new String(bt));
							OutConFile.write(bt, 0, c);
						}
					}

				}
				// end of while (isRunning)
				System.out.println();
				System.out.println("Module \"" + module.ExecName
						+ "\" exited with code : " + exitCode);
				System.out.println();
				// File DataFile = new File
				// (RSTAR_data_copy_path+"/"+RSTAR_data_name);
				// DataFile.renameTo(new File(wrkPath, RSTAR_data_re_name));
				//               
				// DataFile = new File (RSTAR_path+"/"+RSTAR_out_name);
				// if (DataFile.exists()) {
				// System.out.println("Result File Exist!!! Copying to worker
				// dir ...");
				// boolean success = DataFile.renameTo(new File(wrkPath,
				// RSTAR_out_re_name));
				// if (success) {
				// System.out.println("Result File \"" + RSTAR_out_name + "\"
				// was successfully copyied to "+ RSTAR_out_re_name);
				// } else {
				// System.out.println("Result File \"" + RSTAR_out_name + "\"
				// filed to copy to "+ RSTAR_out_re_name);
				// }
				// } else {
				// System.out.println("ResultFile is not present");
				// }

			} else {
				// Debug.println("Module File Does Not Exist: " +
				// execFile.getAbsolutePath());
				// throw new Exception("Execution file not found in "+
				// this.getClass().getName()); // ���� ����������� ���� ��
				// ������ - ������� �� ������
				localLogger.debug("Module File Does Not Exist: "
						+ execFile.getAbsolutePath());
				Exception e = new Exception("Module File Does Not Exist: "
						+ execFile.getAbsolutePath());
				throw e;
			}

			/*
			 * ����� ��������� ����� �� UniversalExeRunner ��� ������� seeb-afgl
			 */

			// ������ ����� ������ CO2 �� netCDF ���� ��� ��������� � ����������
			Boolean readCO2fromNetCDF = getParBooleanByText("readCO2fromNetCDF");
			if (!readCO2fromNetCDF) {
				return;
			}

			String co2_time_mode = getParStringByText("co2_time_mode");

			if (!co2_time_mode.equalsIgnoreCase("manual")) {
				System.out
						.println("Only manual mode for co2_time_mode is implemented");
			}

			String CO2_nc_year = getParStringByText("CO2_nc_year");
			String CO2_nc_month = getParStringByText("CO2_nc_month");
			// CO2_nc_month = getMonthByText
			String CO2_nc_path = getParStringByText("CO2_nc_path");
			String CO2_nc_day = getParStringByText("CO2_nc_day");
			String CO2_nc_hour = getParStringByText("CO2_nc_hour");

			process(CO2_nc_path, Integer.parseInt(CO2_nc_year), Integer
					.parseInt(CO2_nc_month), Integer.parseInt(CO2_nc_day),
					Integer.parseInt(CO2_nc_hour), lat, lon);

			// ���������� ����� ������ �� CO2

		} catch (Exception e) {
			// e.fillInStackTrace();
			localLogger.fatal(e.toString());
			// proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	}

	public void process(String CO2_nc_path, int year, int month, int day,
			int hour, Double traceLat, Double traceLon) {
		// String varName = "lat";
		NetcdfFile ncfile = null;
		String outFname;

		String co2fname = constructCO2fileName(new Integer(year).toString(),
				new Integer(month).toString());

		// String filename = "F:\\co2_model_output\\co2_ps.200101.NIES.nc ";

		try {
			System.out.println("Open NetCDF file with CO2 concentration: "
					+ CO2_nc_path + "\\" + co2fname);
			ncfile = NetcdfFile.open(CO2_nc_path + "\\" + co2fname);
			Variable lat = ncfile.findVariable("lat");
			Variable lon = ncfile.findVariable("lon");
			Variable time = ncfile.findVariable("time");
			Variable lev = ncfile.findVariable("lev");
			if (null == lat || null == lon || null == time)
				return;
			Array dtime = time.read();
			Index dtIndex = dtime.getIndex();
			dtIndex.set(0);
			int[] dts = dtime.getShape();
			int search_hr;
			// Calendar cn = new Calendar();
			// long ldt = Date.parse("12.08.1995 13:30:00");
			// ������������� ���� ��� ����������� ����������� ��������� ����� �
			// netCDF
			GregorianCalendar cn = new GregorianCalendar(year, month - 1, day,
					hour, 0);

			search_hr = cn.get(Calendar.DAY_OF_YEAR) * 24 + hour; // �����������
			// ����������
			// ����� ���
			// ��������
			// ����

			// ����� �� ����� ������� ��������� � �������� �����
			for (int i = dts[0] - 1; i >= 0; i--) {
				if (search_hr >= dtime.getInt(dtIndex.set(i))) {
					break; // ���� ����� ����� �� ����� ������� nc �����, ��
					// ������� �� �����, ��������� ����� ������������ �
					// dtIndex
				}
			}

			// ����� �� ����� ����� ��������� � �������� �����
			Array dlat = lat.read();
			Index latIndex = dlat.getIndex();
			latIndex.set(0);
			int[] latshape = dlat.getShape();

			for (int i = 0; i <= latshape[0] - 1; i++) {
				if (traceLat >= dlat.getInt(latIndex.set(i))) {
					break; // ���� ����� ����� �� ����� ����� nc �����, ��
					// ������� �� �����, ��������� ����� ������������ �
					// latIndex
				}
			}
			// ����� �� ����� ������ ��������� � �������� �����
			Array dlon = lon.read();
			Index lonIndex = dlon.getIndex();
			lonIndex.set(0);
			int[] lonshape = dlon.getShape();

			for (int i = 0; i <= lonshape[0] - 1; i++) {
				if (traceLon >= dlon.getInt(lonIndex.set(i))) {
					break; // ���� ����� ����� �� ����� ������ nc �����, ��
					// ������� �� �����, ��������� ����� ������������ �
					// lonIndex
				}
			}

			// System.out.println(dtIndex.currentElement());

			Array dlev = lev.read(); // �� ������ �����
			NCdump.printArray(dlev, "lev", System.out, null);
			// Array data = lat.read();
			// Index index = data.getIndex();
			// int[] shape = data.getShape();
			// double dval = data.getDouble(index.set(0));
			// System.out.println(dval);
			// dval = data.getDouble(index.set(shape[0]-1));
			// System.out.println(dval);
			// System.out.println(index.currentElement());
			Range timerng = new Range(dtIndex.currentElement(), dtIndex
					.currentElement()); // ��������� �������� ������, ������
			// ����� ���� �����, ��������� �
			// ��������
			Range latrng = new Range(latIndex.currentElement(), latIndex
					.currentElement()); // ��������� �������� �����, ������
			// ����� ���� �����, ��������� �
			// ��������
			Range lonrng = new Range(lonIndex.currentElement(), lonIndex
					.currentElement()); // ��������� �������� ������, ������
			// ����� ���� �����, ��������� �
			// ��������
			Range levrng = new Range(0, lev.getShape()[0] - 1); // ���������
			// ��������
			// �����, �����
			// �� ��� �����
			// �� ������
			// ����. 5 ����� - ������������ ������ ���������� ��� �����������
			// �������
			List ranges = new ArrayList();
			ranges.add(timerng);
			ranges.add(levrng);
			ranges.add(latrng);
			ranges.add(lonrng);
			Variable co2 = ncfile.findVariable("co2"); // ��������� ����������
			// �� CO2
			ArrayFloat.D1 dataCO2 = (ArrayFloat.D1) co2.read(ranges).reduce(); // ������
			// ������
			// CO2
			// �
			// �����.
			// �
			// ��������.
			// �����
			// �����������
			// �����
			// �������,
			// ������,
			// �������
			// System.out.println(dataCO2.get(0));
			// System.out.println(dataCO2.get(14));
			NCdump.printArray(dataCO2, "co2", System.out, null); // �������������
			// ����
			// ������
			// co2 ��
			// ������
			ArrayFloat.D1 dataLev = (ArrayFloat.D1) lev.read();
			int shape_lev[] = lev.getShape();
			Double dblCO2[] = new Double[shape_lev[0]];
			Double dblLev[] = new Double[shape_lev[0]];

			for (int i = 0; i <= shape_lev[0] - 1; i++) {
				dblCO2[i] = new Double(dataCO2.get(i));
				dblLev[i] = new Double(dataLev.get(i));
			}

			Vector<Double[]> vector = new Vector<Double[]>();
			vector.add(dblLev);
			vector.add(dblCO2);
			writeVectorDoubleToTextFileWDescString(vector, wrkPath,
					"nc.co2.dat", "%12.8e", "#0 pressure #1 co2");

		} catch (IOException ioe) {
			ioe.printStackTrace();
			// log("trying to open " + filename, ioe);
		} catch (InvalidRangeException ire) {
			ire.printStackTrace();
			// log("invalid Range for " + varName, e);
		} finally {
			if (null != ncfile)
				try {
					ncfile.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					// log("trying to close " + filename, ioe);
				}
		}
	}

	public static String constructCO2fileName(String year, String month) {
		String co2fname; // �o2_ps.200101.nies.nc
		if (month.trim().length() == 1) {
			co2fname = "co2_ps." + year.trim() + "0" + month.trim()
					+ ".nies.nc";
		} else {
			co2fname = "�o2_ps." + year.trim() + month.trim() + ".nies.nc";
		}
		return co2fname;
	}

	public static String getMonthByText(String month) {
		if (month.equalsIgnoreCase("jan") || month.equalsIgnoreCase("1")) {
			return "jan";
		} else if (month.equalsIgnoreCase("feb") || month.equalsIgnoreCase("2")) {
			return "feb";
		} else if (month.equalsIgnoreCase("mar") || month.equalsIgnoreCase("3")) {
			return "mar";
		} else if (month.equalsIgnoreCase("apr") || month.equalsIgnoreCase("4")) {
			return "apr";
		} else if (month.equalsIgnoreCase("may") || month.equalsIgnoreCase("5")) {
			return "may";
		} else if (month.equalsIgnoreCase("jun") || month.equalsIgnoreCase("6")) {
			return "jun";
		} else if (month.equalsIgnoreCase("jul") || month.equalsIgnoreCase("7")) {
			return "jul";
		} else if (month.equalsIgnoreCase("aug") || month.equalsIgnoreCase("8")) {
			return "aug";
		} else if (month.equalsIgnoreCase("sep") || month.equalsIgnoreCase("9")) {
			return "sep";
		} else if (month.equalsIgnoreCase("oct")
				|| month.equalsIgnoreCase("10")) {
			return "oct";
		} else if (month.equalsIgnoreCase("nov")
				|| month.equalsIgnoreCase("11")) {
			return "nov";
		} else if (month.equalsIgnoreCase("dec")
				|| month.equalsIgnoreCase("12")) {
			return "dec";
		} else {
			return null;
		}
	}

	/**
	 * ���������� �������������� ��������� ����������� ������ �� ���������
	 * ���������� �������
	 * 
	 * @param jultime
	 *            ��������� �����
	 * @return ������������� ��������� ����������� ������ �� ���������
	 *         ���������� �������, <br>
	 *         �� 21.01.2007 - ��� ��������, ������ ������������ "jul"
	 */
	public static String getMonthByJT(Double jultime) {
		return "jul";
	}

	/**
	 * ���������� ����� ��� ���� (0-365(6)) �� ��������� ���������� �������
	 * 
	 * @param jultime
	 *            ��������� �����
	 * @return ����� ��� ����, <br>
	 *         �� 21.01.2007 - ��� ��������, ������ ������������ ����� 2
	 */
	public static Integer getDayByTime(Double jultime) {
		return 2;
	}

}
