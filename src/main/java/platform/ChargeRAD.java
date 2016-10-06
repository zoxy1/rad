package platform;

import platform.Charger;
import platform.Splitter;
import platform.Module;
import platform.Debug;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.TransformerException;

import platform.Stylizer;
import rad.bin.Runner;
import org.apache.log4j.Logger;

/*import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;*/

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.org.apache.xerces.internal.impl.xpath.XPath;
import org.apache.xpath.XPathAPI;

// import com.sun.org.apache.bcel.internal.generic.LoadClass;

public class ChargeRAD extends Charger {
	String DataFile;

	public Document document;

	String wrkPath;

	static Logger mainLogger = Logger.getLogger(ChargeRAD.class);

	public ChargeRAD(File DataFil, String tmpDir, Document Doc,
			String OutDirName, String ITER_XML, String par_file_name)
			throws IOException {
		document = Doc;
		String OutDirNameModified = OutDirName;
		Runner.ITER_XML = ITER_XML;
		if (OutDirNameModified.trim().startsWith(":data_file_name")) {

			OutDirNameModified = (DataFil != null) ? DataFil.getName().trim()
					.replace(" ", "_") : "Worker";
		}

		if (OutDirNameModified.trim().startsWith(":par_file_name")) {

			OutDirNameModified = (par_file_name != null) ? par_file_name
					: "Worker";
		}

		if (OutDirNameModified.trim().startsWith(":split_name")) {

			String spln = getSplitName(Doc);
			OutDirNameModified = (spln != null) ? spln : "Worker";
		}

		if (!isTrue(Conf.add_time_flag)
				&& !(new File(OutDirNameModified).isAbsolute())) {
			OutDirNameModified = (new File(tmpDir)).getAbsolutePath()
					+ File.separator + OutDirNameModified + "_dir";
		}

		wrkPath = Charger.makeWorkerDir(tmpDir, "yyyy_MM_dd_HH_mm_ss_SSSS",
				OutDirNameModified);
		if (wrkPath != null) {
			if (DataFil != null) {
				DataFile = Runner.copyFile(DataFil.getAbsolutePath(), wrkPath
						+ "/" + "count.txt");
				if (DataFil.getName().equals("count.txt"))
					Runner.copyFile(DataFil.getAbsolutePath(), wrkPath + "/"
							+ "COPY_" + DataFil.getName());
				else {
					Runner.copyFile(DataFil.getAbsolutePath(), wrkPath + "/"
							+ DataFil.getName());
				}
			}

		} else
			Debug.println("Work dir was not created!!!");
	}

	private String getSplitName(Document doc) {
		
		String spName = null;
		try {
			NodeList spNL = XPathAPI.selectNodeList(doc, "//split");
			if (spNL.getLength() == 0) {
				spName = null;
			} else {
				spName = "";
				for (int i = 0; i < spNL.getLength(); i++) {
					Node par = XPathAPI.selectSingleNode(spNL.item(i), "./par");
					String cpar_name = par.getAttributes().getNamedItem("name")
							.getTextContent().trim();
					String cpar_value = par.getTextContent().trim();
					spName = spName  + cpar_name + "=" + cpar_value + "_";
				}

			}
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return spName;
	}

	public String getWrkPath() {
		return wrkPath;
	}

	public String getDataFile() {
		return DataFile;
	}

	public void chargeAllNodes() {
		// Date dt = new Date();
		long bt = new Date().getTime();
		Module[] mdArr; // массив модулей - единиц которые выполняются отдельно
		// и меют свой временный субкаталог
		try {

			Node workerNode;// = Splitter.getElementByName((Node)document,
			// "worker"); // получение всей совокупности узлов
			// дерева параметров "worker"
			NodeList workerNodeList = document.getElementsByTagName("worker");
			workerNodeList.getLength();
			workerNode = workerNodeList.item(0);
			// Node w2e = Splitter.getElementByName((Node)workerNode, "worker");

			mdArr = Splitter.Split(workerNode, wrkPath); // разделение
			// совокупности
			// узлов "worker" на
			// отдельные модули
			// "module"
			// возвращает массив объектов Module - который является хранителем
			// параметров
			mainLogger.info("Begin charging nodes for current worker");
			for (int i = 0; i < mdArr.length; i++) {
				try {
					rad.bin.Runner Rnnr = null;
					if (mdArr[i].ExecName.contains("rad.bin")) { // определяем
						// что это?
						// модуль-Java
						// или
						// модуль-оболочка-exe
						Rnnr = ((rad.bin.Runner) Class.forName(
								mdArr[i].ExecName).newInstance()); // Runner
						// для
						// модуля-Java
						// создается
						// по имени
						// из дерева
						// параметров
						// Rnnr = new rad.bin.RunnerSRTM();
					} else {
						// Rnnr =
						// ((rad.bin.Runner)Class.forName("rad.bin.RunnerNativeExe").newInstance());
						// //модуль-оболочка-exe для запуска родного exe-файла
						Rnnr = ((rad.bin.Runner) Class.forName(
								"rad.bin.UniversalExeWithConsoleRunner")
								.newInstance()); // модуль-оболочка-exe для
						// запуска родного exe-файла
						// Rnnr = new rad.bin.RunnerSRTM();
					}
					// Rnnr.run(mdArr[i].XMLParFileName ,wrkPath); // запуск
					// Runner (имя файла параметров (только для одного данного
					// модуля), рабочий каталог)
					mainLogger.info("Running Computation Module: "
							+ Rnnr.getClass().getName());
					Rnnr.run(mdArr[i], wrkPath);
				} catch (Exception e) {
					// e.fillInStackTrace();
					// StackTraceElement T[] = e.getStackTrace();
					// mainLogger.fatal(T[0].toString());
					// mainLogger.fatal(e.getMessage());
					e.printStackTrace();
					if (isTrue(Conf.error_stop_flag)) {
						// System.out.println("Error encountered, launching
						// stopped");
						mainLogger
								.fatal("Error encountered, launching stopped");
						break;
					} else {
						// System.out.println("Error encountered, don't stop
						// launching");
						mainLogger
								.warn("Error encountered, don't stop launching");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long et = new Date().getTime();

		// System.out.println("All nodes was charged in " + (et-bt) + " ms");
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss,SSS");
		mainLogger.info("All nodes for current worker finished");
	}
}
