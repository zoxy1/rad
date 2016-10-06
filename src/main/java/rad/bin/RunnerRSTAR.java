package rad.bin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import org.apache.xpath.XPathAPI;

import platform.Debug;
import platform.Module;
import platform.Stylizer;

public class RunnerRSTAR extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerRSTAR.class);

	private int c = -1;

	private byte bt[] = new byte[1024 * 1024];

	private byte processInBuff[] = new byte[1024 * 100];

	HashMap<String, Double[]> coord = new HashMap<String, Double[]>();

	Process PR;

	@Override
	void runModule(Module module) throws Exception {
		try {

			File Water100 = new File(wrkPath + "/" + "100-WATER!!!");
			File Night100 = new File(wrkPath + "/" + "100-NIGHT!!!");

			if (Water100.exists()) {
				setParStringByText("INDG", "0");
			} else {
				setParStringByText("INDG", "1");
			}

			if (Night100.exists()) {
				setParStringByText("ISOL", "0");
			} else {
				setParStringByText("ISOL", "1");
			}

			String coordLF = (String) getParObjectByText("coordinatesListFile",
					"parRunner");

			if (coordLF != null && (new File(coordLF)).exists()) {
				coord = readFileToHashMapOfDoubleArrayWDesc((String) getParObjectByText(
						"coordinatesListFile", "parRunner"));
				Double LatArr[] = coord.get("Latitude");
				Double LonArr[] = coord.get("Longitude");
				if (LatArr[0] >= 66.) {
					setParStringByText("IATM", "5"); // POLAR
				} else if (LatArr[0] <= -66.) {
					setParStringByText("IATM", "4"); // POLAR
				} else if (LatArr[0] <= -23. & LatArr[0] > -66.) {
					setParStringByText("IATM", "3"); // MID_LAT
				} else if (LatArr[0] < 66. & LatArr[0] >= 23.) {
					setParStringByText("IATM", "2"); // MID_LAT
				} else if (LatArr[0] < 23. & LatArr[0] > -23.) {
					setParStringByText("IATM", "1"); // TROPICS
				}

				Double SEA[] = coord.get("SolarElevAngle");

				setParStringByText("TH0", "" + (90. - SEA[0]));

				Double AZA[] = coord.get("SolarAzimut(CW)");

				setParStringByText("FI", "" + AZA[0]);
			}

			File Freq = getFile("albedo.dat");
			if (Freq != null) {
				BufferedReader BR = new BufferedReader(new FileReader(Freq));
				String albd = BR.readLine();
				setParStringByText("GALB", albd); // cut_output не учитывает этот GALB, а берет reflectance.dat
			}

			String RSTAR_path = (String) getParObjectByText("RSTAR_path",
					"parRunner");
			System.out.println("RSTAR_path= " + RSTAR_path);

			String RSTAR_exec_name = (String) getParObjectByText(
					"RSTAR_exec_name", "parRunner");
			System.out.println("RSTAR_exec_name= " + RSTAR_exec_name);

			String RSTAR_data_name = (String) getParObjectByText(
					"RSTAR_data_name", "parRunner");
			System.out.println("RSTAR_data_name= " + RSTAR_data_name);

			String RSTAR_data_copy_path = (String) getParObjectByText(
					"RSTAR_data_copy_path", "parRunner");
			System.out.println("RSTAR_data_copy_path= " + RSTAR_data_copy_path);

			String RSTAR_out_name = (String) getParObjectByText(
					"RSTAR_out_name", "parRunner");
			System.out.println("RSTAR_out_name= " + RSTAR_out_name);

			String RSTAR_out_re_name = (String) getParObjectByText(
					"RSTAR_out_re_name", "parRunner");
			System.out.println("RSTAR_out_re_name= " + RSTAR_out_re_name);

			String RSTAR_data_re_name = (String) getParObjectByText(
					"RSTAR_data_re_name", "parRunner");
			System.out.println("RSTAR_data_re_name= " + RSTAR_data_re_name);

			/*
			 * выбрасываем XML с измененными параметрами дл€ данной географ.
			 * точки
			 */

			File FilPar = getFile(module.XMLParFileName);
			if (FilPar.exists()) {
				FilPar.delete();
			}
			DOMSource source = new DOMSource(module.mdNode);
			StreamResult result = new StreamResult(FilPar);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.transform(source, result);

			/*
			 * «десь код из UniversalExeRunner дл€ запуска EXE RSTAR или HSTAR
			 */

			File execFile = new File(RSTAR_path + "/" + RSTAR_exec_name);
			if (execFile.exists()) {
				System.out.println("File <" + execFile.getAbsolutePath()
						+ "> exist, All Ok.");
				String[] commandLine = getAllParametersFromXML(module);
				commandLine[0] = RSTAR_path.trim() + "/"
						+ RSTAR_exec_name.trim();

				if ((module.ParFileName != null)
						&& (module.ParFileName.length() > 0)) {
					if (module.parType.trim().equalsIgnoreCase("HSTAR")) {
						Stylizer.Stylize("resources/hstar.xsl", FilPar
								.getAbsolutePath(), RSTAR_data_copy_path + "/"
								+ RSTAR_data_name);
						if (RSTAR_exec_name.trim().equalsIgnoreCase("hstar_cut_output.exe")) {
							File gases_file = getFile("GASES.dat");
//							File dest = new File(wrkPath + "/G2.renamed" );
//							gases_file.renameTo(dest);
//							gases_file = null;
//							gases_file = getFile("GASES.dat");
							if (gases_file==null||(gases_file!=null&&!gases_file.exists())) {
								throw new Exception("File GASES.dat not found");
							} else {
								copyFile(gases_file.getAbsolutePath(), RSTAR_data_copy_path + "/" +gases_file.getName());
							}
							File paragFile = getFile(this.wrkPath + "/" + "PARAG");
							if (paragFile.exists()) {
								copyFile(paragFile.getAbsolutePath(), RSTAR_data_copy_path
										+ "/" + paragFile.getName());
							} else {
								throw new Exception("File " + module.ParFileName + " not found");
							}
							//RSTAR_data_copy_path = (String) getParObjectByText("RSTAR_data_copy_path", "parRunner");
							//Runner.copyFile(wrkPath+"/"+"GASES.dat", RSTAR_data_copy_path +"/"+ "GASES.dat");
							
							File reflectFile = getFile(this.wrkPath + "/" + "reflectance.dat");
							if (reflectFile.exists()) {
								copyFile(reflectFile.getAbsolutePath(), RSTAR_data_copy_path
										+ "/" + reflectFile.getName());
							} else {
								throw new Exception("File " + reflectFile.getName() + " not found");
							}
							
							//Runner.copyFile(wrkPath+"/"+"reflectance.dat", RSTAR_data_copy_path+ "/" + "reflectance.dat");
						}
						
						// Stylizer.Stylize("resources/transformWOconsole.xsl",wrkPath
						// + "/" + module.XMLParFileName, RSTAR_data_copy_path
						// +"/"+RSTAR_data_name);
					} else {
						Stylizer.Stylize("resources/rstar.xsl", FilPar
								.getAbsolutePath(), RSTAR_data_copy_path + "/"
								+ RSTAR_data_name);
					}

					Stylizer.Stylize("resources/transformWithName.xsl", FilPar
							.getAbsolutePath(), wrkPath + "/" + RSTAR_data_name
							+ ".txt", false);
				}
				Stylizer.Stylize("resources/transformConsole.xsl", FilPar
						.getAbsolutePath(), this.wrkPath + "/"
						+ module.ExecName + ".console");
				// try {

				ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
				processBuilder.directory(new File(RSTAR_path));

				// process = processBuilder.start();
				// BufferedInputStream errOut = new
				// BufferedInputStream(process.getErrorStream());
				// BufferedInputStream cout = new
				// BufferedInputStream(process.getInputStream());

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
				File DataFile = new File(RSTAR_data_copy_path + "/"
						+ RSTAR_data_name);
				DataFile.renameTo(new File(wrkPath, RSTAR_data_re_name));

				DataFile = new File(RSTAR_path + "/" + RSTAR_out_name);
				if (DataFile.exists()) {
					System.out
							.println("Result File Exist!!! Copying to worker dir ...");
					boolean success = DataFile.renameTo(new File(wrkPath,
							RSTAR_out_re_name));
					if (success) {
						System.out.println("Result File \"" + RSTAR_out_name
								+ "\" was successfully copyied to "
								+ RSTAR_out_re_name);
					} else {
						System.out.println("Result File \"" + RSTAR_out_name
								+ "\" filed to copy to " + RSTAR_out_re_name);
					}
				} else {
					System.out.println("ResultFile is not present");
				}
				// } catch (Exception E) {
				// E.printStackTrace();
				// }
			} else {
				localLogger.debug("Module File Does Not Exist: "
						+ execFile.getAbsolutePath());
				Exception e = new Exception("Module File Does Not Exist: "
						+ execFile.getAbsolutePath());
				throw e;
			}
		} catch (Exception e) {
			// e.fillInStackTrace();

			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
		// }
		/*
		 * «десь окончание блока из UniversalExeRunner дл€ запуска EXE RSTAR или
		 * HSTAR
		 */

	}

	public String[] getAllParametersFromXML(Module module) {
		ArrayList<String> cmd = new ArrayList<String>();
		try {
			NodeList currentModuleNodelist = XPathAPI.selectNodeList(mdNode,
					"parameters");
			if (currentModuleNodelist.getLength() > 0) {
				// read all parameters
				cmd.add(module.ExecPath + "/" + module.ExecName);
				for (int j = 0; j < currentModuleNodelist.item(0)
						.getChildNodes().getLength(); j++) {
					// if
					// (currentModuleNodelist.item(0).getChildNodes().item(j).getNodeName().equalsIgnoreCase("conPar"))
					// {
					// cmd.add(currentModuleNodelist.item(0).getChildNodes().item(j).getTextContent().trim());
					// }
					if (currentModuleNodelist.item(0).getChildNodes().item(j)
							.getNodeName().equalsIgnoreCase("par")
							&& currentModuleNodelist.item(0).getChildNodes()
									.item(j).getAttributes().getNamedItem(
											"console") != null) {
						cmd.add(currentModuleNodelist.item(0).getChildNodes()
								.item(j).getTextContent().trim());
					}
				}
			} else {
				System.err.println("Node \"parameters\" not found in XML");
			}
		} catch (javax.xml.transform.TransformerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] tempbuf = new String[cmd.size()];
		for (int i = 0; i < cmd.size(); i++) {
			tempbuf[i] = cmd.toArray()[i].toString();
		}
		return tempbuf;
	}

	@Override
	protected void finalize() throws Throwable {
		// FIXME Ќе удал€ет при финализации запущенные через exec процессы
		// super.finalize();
		if (PR != null)
			PR.destroy();
	}
}
