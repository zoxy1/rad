package rad.bin;

import platform.Module;
import platform.Debug;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import org.apache.xpath.XPathAPI;
import platform.Stylizer;

/**
 * Created by IntelliJ IDEA. User: crysalis Date: Aug 10, 2006 Time: 4:30:35 PM
 * To change this template use File | Settings | File Templates.
 */

public class UniversalExeWithConsoleRunner extends Runner {

	static Logger localLogger = Logger
			.getLogger(UniversalExeWithConsoleRunner.class);

	private int c = -1;

	private byte bt[] = new byte[1024 * 1024];

	private byte processInBuff[] = new byte[1024 * 100];

	/**
	 * creates command line (String[]) for current module. module must contain
	 * tag "<parameters/>" All values in <par/> will be placed in output array.
	 * Warning! Be carefull with parameter's order! The first element contain
	 * full path to the exec file
	 * 
	 * @return String[]
	 */
	public String[] getAllParametersFromXML(Module module) {
		ArrayList<String> cmd = new ArrayList<String>();
		try {
			NodeList currentModuleNodelist = XPathAPI.selectNodeList(mdNode,
					"parameters");
			if (currentModuleNodelist.getLength() > 0) {
				// read all parameters
				if (module.ExecPath.trim().length() == 0) {
					// execFile = new File(module.ExecName);
					cmd.add(module.ExecName);
				} else {
					// execFile = new File(module.ExecPath + "/" +
					// module.ExecName);
					cmd.add(module.ExecPath + "/" + module.ExecName);
				}

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

	/**
	 * Runs current module (overrided)
	 * 
	 * @param module
	 */
	void runModule(Module module) throws Exception {

		try {
			String retry = null;
			try {
				retry = (String) getParObjectByText("retry_count", "par");
			} catch (Exception e) {
				localLogger.warn(e.getMessage());
			}
			int retry_count;
			if (retry == null) {
				retry_count = 1;
			} else {
				retry_count = Integer.parseInt(retry);
			}

			// File execFile = new File(module.ExecPath + "/" +
			// module.ExecName);
			File execFile;
//			if (module.ExecPath.trim().length() == 0) {
//				execFile = new File(module.ExecName);
//			} else {
//				execFile = new File(module.ExecPath + "/" + module.ExecName);
//				
//			}
			execFile = (module.ExecPath.trim().length() == 0)?getFile(module.ExecName):getFile(module.ExecPath + "/" + module.ExecName);
			//System.out.println((module.ExecPath + "/" + module.ExecName).matches("app[.]"));
			// File execFile = new File(module.ExecPath + module.ExecName);
			if (execFile!=null&&execFile.exists() && execFile.isFile()) {
				System.out.println("File <" + execFile.getAbsolutePath()
						+ "> exist, All Ok.");
				for (int j = 1; j <= retry_count; j++) { // повторяем запуск
															// модуля exe
															// столько раз
															// сколько указано в
															// параметре
															// retry_count
					// потребность в этом возникла из-за того что при доступе к
					// нек. файлам возникает ошибка занятости другим процессом
					String[] commandLine = getAllParametersFromXML(module);
					if ((module.ParFileName != null)
							&& (module.ParFileName.length() > 0)) {
						Stylizer.Stylize("resources/transformWOconsole.xsl",
								wrkPath + "/.service/" + module.XMLParFileName,
								this.wrkPath + "/" + module.ParFileName);
					}
					Stylizer.Stylize("resources/transformConsole.xsl", wrkPath
							+ "/.service/" + module.XMLParFileName, this.wrkPath + "/"
							+ module.ExecName + ".console");
					// try {
					Process process, PR;
					ProcessBuilder processBuilder = new ProcessBuilder(
							commandLine);
					processBuilder.directory(new File(this.wrkPath));

					// process = processBuilder.start();
					// BufferedInputStream errOut = new
					// BufferedInputStream(process.getErrorStream());
					// BufferedInputStream cout = new
					// BufferedInputStream(process.getInputStream());

					FileOutputStream errLogFile = new FileOutputStream(
							(new File(this.mdWrkPath + "/" + module.ExecName))
									.getAbsolutePath()
									+ "_Err.log");

					BufferedInputStream InpStream = new BufferedInputStream(
							(PR = processBuilder.start()).getErrorStream());
					BufferedInputStream ConStream = new BufferedInputStream(PR
							.getInputStream());
					BufferedOutputStream CoutStream = new BufferedOutputStream(
							PR.getOutputStream());

					FileOutputStream OutFile = new FileOutputStream((new File(
							this.mdWrkPath + "/" + module.ExecName))
							.getAbsolutePath()
							+ "_Err.log");
					FileOutputStream OutConFile = new FileOutputStream(
							(new File(this.mdWrkPath + "/" + module.ExecName))
									.getAbsolutePath()
									+ "_Con.log");

					if (PR == null) {
						Debug.println("Can't create process: " + this.mdWrkPath
								+ "/" + module.ExecName);
					} else {
						/*******************************************************
						 * /???????????????!!!blocks!!!?????????????????? while
						 * ((c = errOut.read(bt)) > 0) { errLogFile.write(bt, 0,
						 * c); } /
						 ******************************************************/

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
					if (exitCode != 0) {
						localLogger.warn("Module \"" + module.ExecName
								+ "\" exited with code : " + exitCode);
						if (j < retry_count) {
							localLogger.warn("Retrying to launch module => \""
									+ module.ExecName);
							Thread.sleep(50);
						}
					} else {
						localLogger.info("Module \"" + module.ExecName
								+ "\" exited with code : " + exitCode);
						break; // если все успешно - выходим из цикла
								// повторения запуска модуля
					}
				}
				// } catch (Exception E) {
				// E.printStackTrace();
				// }
			} else {
				localLogger
						.debug("Module File Does Not Exist, or is Not a File: "
								+ module.ExecPath + "/" + module.ExecName + " ("+ execFile + ")\r\n");
				Exception e = new Exception("Module File Does Not Exist, or is Not a File:: "
						+ module.ExecPath + "/" + module.ExecName + " ("+ execFile.getAbsolutePath() + ")\r\n");
				throw e;
			}

		} catch (Exception e) {
			e.printStackTrace();
			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	} // end of runModule
} // end of Class
