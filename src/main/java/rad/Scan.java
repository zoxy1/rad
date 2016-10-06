package rad;

import platform.ChargeRAD;
import platform.Splitter;
import platform.Conf;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import rad.bin.Runner;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Scan implements Runnable {

	static final int windowHeight = 500;

	static final int leftWidth = 350;

	static final int rightWidth = 350;

	static final int windowWidth = leftWidth + rightWidth;

	static Boolean wasEntered = null;

	static int mainEntryCounter = 0;

	static public Vector<String> initialArgs = new Vector<String>();

	static private int procfilecnt = 0;
	static int strike1 = 0;
	static int strike3 = 0;
	static int missed = 0;

	File DataFile;

	Document parTree;

	String DocFile;

	// String IteratedFile;
	String WORKER_NAME;

	String TMP_DIR;

	String ITER_XML;

	static Logger logger = Logger.getLogger(Main2.class);

	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS"); // формат

	// времени

	static NetcdfFileWriteable ncFile = null;
	static Array jdtarr = null;
	static Index jdind = null;

	private static int strike2 = 0;

	// private static JPPFClient jppfClient = null;

	public static void main(String[] args) {
		try {
			// jppfClient = new JPPFClient();
			System.out.println("user.dir :=" + System.getProperty("user.dir"));
			mainEntryCounter++;
			if (mainEntryCounter == 1) {
				PropertyConfigurator.configure(System.getProperty("user.dir")
						+ "/log4j/log4j.properties");
			}
			;
			String full_arg_string = "";
			for (int i = 0; i <= args.length - 1; i++) {
				full_arg_string = full_arg_string + args[i] + " ";
			}

			if (!(new File("logs").exists())) {
				new File("logs").mkdir();
			}
			System.out.println("Main entry " + mainEntryCounter
					+ " with parameters: " + full_arg_string);
			logger.info("Main entry " + mainEntryCounter + " with parameters: "
					+ full_arg_string);
			logger.debug(Locale.getDefault().toString());

			Locale.setDefault(Locale.US); // устанавливает локаль в которой
			// десятичным разделителем явл точка

			// устанавливаем временную зону GMT
			// все даты, указанные в формате год месяц день и т.д. будут
			// считаться в зоне GMT
			// для времени в миллисекундах также будем считать для GMT если
			// отдельно не указана дата

			TimeZone tz = TimeZone.getTimeZone("GMT");
			TimeZone.setDefault(tz);

			String cfile;
			if ((cfile = System.getProperty("config.file")) != null) {
				new Conf(cfile);
			} else {
				new Conf();
			}
			try {
				Main2.parseArg(args);

				Conf.setProperty("app.home.path", get_path());
				printConf();

				analizeConf();
			} catch (Exception e) {
				e.printStackTrace();
				// logger.fatal(e.getMessage());
				proceesException(e, logger, "rad.Main2");
				return;
			}
			if (mainEntryCounter == 1) {
				// PropertyConfigurator.configure(System.getProperty("user.dir")+"/log4j/log4j.properties");
				Conf.setProperty("initial.cmd.arg.string", full_arg_string);
			}

			File ncf = new File(Conf.getProperty("ncf.file.path"));

			if (!ncf.exists()) {
				throw new Exception("netCDF file is not exists: "
						+ ncf.getAbsolutePath());
			}

			ncFile = NetcdfFileWriteable.openExisting(ncf.getAbsolutePath(),
					true);

			Variable jdtime = ncFile.findVariable("jdtime");
			jdtarr = jdtime.read();
			jdind = jdtarr.getIndex();
			jdind.set(0);

			// jdind.getSize()
			// jdtime.getDimensionsAll();

			File tmpDir = new File(Conf.getProperty(Conf.tmp_dir_name));
			if (tmpDir.exists()) {
				ScanDir(tmpDir, Conf.getProperty("scan.dir.template"), Conf
						.getProperty("ncf.file.path"));
			} else {
				throw new Exception("tmpDir is not exists: "
						+ tmpDir.getAbsolutePath());
			}

			logger.info("Total dir processed: " + procfilecnt);
			logger.info("Strike1: " + strike1);
			logger.info("Strike2: " + strike2);
			logger.info("Strike3: " + strike3);
			logger.info("Missed: " + missed + " Total: "
					+ (strike1 + strike2 + strike3 + missed));

		} catch (Exception ioe) {
			ioe.printStackTrace();
			logger.fatal(ioe.getMessage());
		}
	}

	private static void analizeConf() throws Exception {

		String sprop = null;

		if (!Main2.isEmpty(Conf.run_par_file_path)
				&& !Main2.isEmpty(Conf.split_file_path)) {
			throw new Exception("Properties: " + Conf.run_par_file_path
					+ " and " + Conf.split_file_path
					+ " can NOT be defined simultaneously, only one is allowed");
		}

		if (!Main2.isEmpty(Conf.run_par_file_path)) {
			logger.warn("Property " + Conf.run_par_file_path
					+ " is defined THAN Properties will be ignored:\n");
			logger.warn(" " + Conf.run_par_dir_path + " = "
					+ Conf.getProperty(Conf.run_par_dir_path) + "\n");
			logger.warn(" " + Conf.split_dir_path + " = "
					+ Conf.getProperty(Conf.split_dir_path) + "\n");
		} else if (!Main2.isEmpty(Conf.split_file_path)) {
			if (Main2.isEmpty(Conf.split_dir_path)) {
				throw new Exception(" Property " + Conf.split_dir_path
						+ " must be defined when Property "
						+ Conf.split_file_path + " is specified");
			}
		} else if (Main2.isEmpty(Conf.run_par_dir_path)) {
			throw new Exception(
					"At least One from Properties must Be specified: "
							+ Conf.run_par_file_path + ", "
							+ Conf.split_file_path + ", "
							+ Conf.run_par_dir_path);
		}

		if (Main2.isEmpty("scan.dir.template")) {
			throw new Exception(
					"Property scan.dir.template is EMPTY, must be defined");
		} else if (Main2.isEmpty("scan.file.indicator")) {
			throw new Exception(
					"Property scan.file.indicator is EMPTY, must be defined");
		} else if (Main2.isEmpty("ncf.file.path")) {
			throw new Exception(
					"Property ncf.file.path is EMPTY, must be defined");
		}

	}

	public static void proceesException(Exception e, Logger lg, String className) {
		StackTraceElement T[] = e.getStackTrace();
		String exString = T[0].toString();
		// PrintStream PS = new PrintStream();
		for (int i = 0; i <= T.length - 1; i++) {
			if (T[i].getClassName().equals(className)) {
				exString = T[i].toString();
				// T[i].
			}
		}
		// lg.fatal(e.getMessage());
		lg.fatal(exString);
	}

	public static void printConf() {
		// Properties PROP =
		// Get all system properties
		Properties props = Conf.getProperties();
		// props.list(System.out);
		logger.info("Settings in conf.properties are:");
		// Enumerate all system properties
		Enumeration enu = props.propertyNames();
		for (; enu.hasMoreElements();) {
			// Get property name
			String propName = (String) enu.nextElement();
			// Get property value
			String propValue = (String) props.get(propName);
			logger.info(propName + " = " + propValue);
		}
	}

	public static String get_path() throws IOException {
		String home_path;

		home_path = System.getProperty("user.dir");

		File path = new File(home_path);
		File parent1 = path.getParentFile();
		if (parent1 == null) {
			return null;
		} else {
			File parent2 = parent1.getParentFile();
			if (parent2 == null) {
				return path.getCanonicalPath();
			} else {
				return parent2.getCanonicalPath();
			}
		}

		// return home_path;
	}

	void initialize() {
		ITER_XML = (!Main2.isEmpty(Conf.run_iter_file_path)) ? Conf
				.getProperty(Conf.run_iter_file_path) : this.DocFile;
		WORKER_NAME = Main2.getProperty(Conf.worker_dir_name);
		TMP_DIR = Main2.getProperty(Conf.tmp_dir_name);
	}

	/*
	 * public Main2(File dataFile, String DocFileXML, String IteratedXML) { //
	 * конструктор // потока // (входной // файл, // дерево // параметров)
	 * DataFile = dataFile; DocFile = DocFileXML; parTree =
	 * getDOMfromXML(DocFileXML); // new // DomEcho(DocFileXML).getDocument();
	 * initialize(); }
	 * 
	 * public Main2(File dataFile, String DocFileXML) { // конструктор потока //
	 * (входной файл, дерево // параметров) DataFile = dataFile; DocFile =
	 * DocFileXML; parTree = getDOMfromXML(DocFileXML); // new //
	 * DomEcho(DocFileXML).getDocument(); initialize(); }
	 * 
	 *//***********************************************************************
		 * public Main (String DocFileXML){ // конструктор потока (файл
		 * параметров) DataFile = null; // входной файл данных отсутствует
		 * DocFile = DocFileXML; // файл параметров расчета parTree = new
		 * DomEcho(DocFileXML).getDocument(); // дерево параметров после
		 * обработки xml файла параметров }/
		 **********************************************************************/
	/*
	 * 
	 * public Main2(Document doc) { // конструктор потока (дерево параметров)
	 * DataFile = null; // входной файл отсутствует DocFile = null; parTree =
	 * doc; initialize(); }
	 * 
	 * public Main2(File dataFile, Document doc) { // конструктор потока (дерево //
	 * параметров) DataFile = dataFile; // входной файл отсутствует DocFile =
	 * null; parTree = doc; initialize(); }
	 */

	// public static Document getDOMfromXML (String xml){
	// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	// factory.setExpandEntityReferences(true);
	// Document document=null;
	// try {
	// DocumentBuilder builder = factory.newDocumentBuilder();
	// document = builder.parse(new File(xml));
	// } catch (SAXParseException spe) {
	// // Error generated by the parser
	// System.out.println("\n** Parsing error" + ", line " +
	// spe.getLineNumber() + ", uri " + spe.getSystemId());
	// System.out.println(" " + spe.getMessage());
	//
	// // Use the contained exception, if any
	// Exception x = spe;
	//
	// if (spe.getException() != null) {
	// x = spe.getException();
	// }
	//
	// x.printStackTrace();
	// } catch (SAXException sxe) {
	// // Error generated during parsing)
	// Exception x = sxe;
	//
	// if (sxe.getException() != null) {
	// x = sxe.getException();
	// }
	//
	// x.printStackTrace();
	// } catch (ParserConfigurationException pce) {
	// // Parser with specified options can't be built
	// pce.printStackTrace();
	// } catch (IOException ioe) {
	// // I/O error
	// ioe.printStackTrace();
	// }
	// return document;
	// }
	public static Document getDOMfromXML(String filename) {
		try {
			boolean validating = false;
			// Create a builder factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setValidating(validating);

			// Prevent expansion of entity references
			factory.setExpandEntityReferences(true);

			// Create the builder and parse the file
			Document doc = factory.newDocumentBuilder().parse(
					new File(filename));
			return doc;
		} catch (SAXException e) {
			// A parsing error occurred; the xml input is not valid
		} catch (ParserConfigurationException e) {
		} catch (IOException e) {
		}
		return null;
	}

	// public Main (File dataFile, Document doc, String cmdKeys){ // конструктор
	// потока (дерево параметров)
	// DataFile = dataFile; // входной файл отсутствует
	// DocFile = null;
	// parTree = doc;
	// initialize();
	// }

	public static void SplitFile(String fname, File dir) {
		Splitter.clearDstPath(dir.getAbsolutePath());
		Document document = getDOMfromXML(fname);
		int fcnt = Splitter.splitTree(document, dir.getAbsolutePath());
		System.out.println("Total: " + fcnt
				+ " files was placed in target directory "
				+ dir.getAbsolutePath());
		return;
	}

	public static void ScanDir(File tmpDir, String scan_dir_template,
			String ncf_path) {

		FilenameFilter dirFilter = new FilenameFilter() {
			private String sig = Conf.getProperty("scan.dir.template").replace(
					"*", "");

			public boolean accept(File dir, String name) {
				boolean ac;
				// String name = file.getName();
				ac = (name.contains(sig) || (new File(dir.getAbsolutePath()
						+ "/" + name).isDirectory()))
						&& (!name.startsWith("."));
				return ac;
			}
		};

		long bt = new Date().getTime();

		System.out.println("Rundir Started in :" + sdf.format(new Date(bt)));
		// if (al.size()==1) {
		String[] children = tmpDir.list(dirFilter);
		for (int i = 0; i < children.length; i++) {
			File visitDir = new File(tmpDir.getAbsolutePath() + "/"
					+ children[i]);
			visitAllFilesAndWrite(visitDir, ncf_path);
		}

		long et = new Date().getTime();
		long hrs = (et - bt) / 3600000;
		long min = ((et - bt) - (hrs * 3600000)) / 60000;
		long sec = ((et - bt) - (hrs * 3600000) - (min * 60000)) / 1000;
		long ms = ((et - bt) - (hrs * 3600000) - (min * 60000) - (sec * 1000));
		GregorianCalendar dtet = new GregorianCalendar();
		dtet.setTimeInMillis(et);
		GregorianCalendar dtbt = new GregorianCalendar();
		dtbt.setTimeInMillis(bt);

		System.out.println("Begin time: " + dtbt.getTime());
		System.out.println("  End time: " + dtet.getTime());
		System.out.println("All files was processed in: " + hrs + " hrs, "
				+ min + " min, " + sec + "." + ms + " sec");

		logger.info("Begin time: " + dtbt.getTime());
		logger.info("  End time: " + dtet.getTime());
		logger.info("All files was processed in: " + hrs + " hrs, " + min
				+ " min, " + sec + "." + ms + " sec");

		return;
	}

	public static void runGUI(String DocFileXML) {
		// runGUI(DocFileXML, null, null);
	}

	// public static void runGUI(String DocFileXML, File DataFile, GUIstarter
	// GS) { // запуск
	// // графической
	// // оболочки
	// String xml = null;
	// if (DocFileXML == null) {
	// xml = "resources/radDefault.xml";
	// } else {
	// xml = DocFileXML;
	// }
	// if (GS == null) {
	// GS = new GUIstarter();
	// } else {
	// GS.getContentPane().removeAll();
	// GS.initialize();
	// }
	// FileAndTextTransferHandler FTTH = new FileAndTextTransferHandler(GS); //
	// добавление
	// // хэндлера
	// // для
	// // Drag-n-Drop
	// DomEcho DE = new DomEcho(xml, FTTH);
	// GS.getContentPane().add("Center", DE); // добавление документа,
	// // размещенного в JPanel на
	// // форму GuiStarter
	// GS.setDomEcho(DE);
	// if (DataFile != null) {
	// GS.setDataFile(DataFile);
	// }
	//
	// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	// int w = windowWidth + 10;
	// int h = windowHeight + 10;
	// GS.setLocation(50, 50);
	// GS.setSize(w, h);
	// GS.setVisible(true);
	// System.out.println("After!");
	// }

	private String result;

	public Object getResult() {
		return result;
	}

	public void run() { // запуск потока

		// Создаем объект, запускающий непосредственно на выполнение узлы дерева
		// параметров (файл_данных, временный каталог, дерево параметров)
		// Document DOC = new DomEcho(DocFile).getDocument();

		File TmpDir = new File(TMP_DIR);
		ChargeRAD ChRAD;
		try {
			replaceAllPropertyStringsInDOM(parTree);
			// TmpDir.getAbsolutePath();
			ChRAD = new ChargeRAD(DataFile, TmpDir.getAbsolutePath(), parTree,
					WORKER_NAME, ITER_XML, (new File(DocFile)).getName());
			// System.
			if (DocFile != null) {
				// Runner.copyFile(DocFile, ChRAD.getWrkPath() + "/"
				// + (new File(DocFile)).getName());

				String docFileStr = new File(DocFile).getParent();
				File docFileDir = new File(docFileStr);

				FilenameFilter fileFilter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						boolean ac;
						// String name = file.getName();
						ac = ((!name.startsWith(".") || (new File(dir
								.getAbsolutePath()
								+ "/" + name).isDirectory())) && (!name
								.startsWith(".")))
								|| (name.startsWith(".") && name
										.endsWith(".xml"));
						return ac;
					}
				};

				String[] children = docFileDir.list(fileFilter);
				if (Main2.isEmpty(Conf.run_par_file_path)) {
					for (int i = 0; i < children.length; i++) {
						File child = new File(docFileDir, children[i]);
						Runner.copyFile(child.getAbsolutePath(), ChRAD
								.getWrkPath()
								+ "/" + children[i]);
					}
				} else {
					Runner.copyFile(DocFile, ChRAD.getWrkPath() + "/"
							+ (new File(DocFile)).getName());
				}

			}
			/*
			 * if (DataFile != null) {
			 * Runner.copyFile(DataFile.getAbsolutePath(), ChRAD.getWrkPath() +
			 * "/" + "subTraceInfo.out"); }
			 */
			logger.info(ChRAD.getDataFile());
			ChRAD.chargeAllNodes(); // запуск на выполнение всех узлов дерева
			// параметров
			String wrkPath = ChRAD.getWrkPath();
			if (Main2.isTrue(Conf.browser_run_flag)) {
				runBrowser(wrkPath);
				// LS.add("-persist");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.fatal("I/O problem in Runner.copyFile or relative calls");
		} catch (Exception e) {
			logger.fatal("Unsuccessfull Main2.run()", e);
		}

	}

	private void replaceAllPropertyStringsInDOM(Document parTree2)
			throws Exception {

		// пройти по всем элементам дерева DOM и заменить во всех текстах
		// строки типа {some.property} на конкретные значения из Conf
		NodeList NL = parTree2.getChildNodes();
		goOveAllNodes(NL);

	}

	private void goOveAllNodes(NodeList NL) throws Exception {

		for (int i = 0; i < NL.getLength(); i++) {
			Node cItem = NL.item(i);
			if (cItem.hasChildNodes()) {
				goOveAllNodes(cItem.getChildNodes());
			} else if (cItem.getNodeType() == Node.TEXT_NODE) {
				String clearStr = Runner.replacedPropertyString(cItem
						.getTextContent());
				if (!clearStr.equals(cItem.getTextContent())) {
					// System.out.println(cItem.getTextContent()+" ==>
					// "+clearStr);
					// System.out.println(cItem.getNodeValue());
					cItem.setNodeValue(clearStr);
				}
			}
		}

	}

	public void runBrowser(String wrkPath) {
		Vector<String> LS = new Vector<String>();
		// java ViewForest/Main "gnuplot/bin/wgnuplot.exe" "../rad/tmp"
		// String cdstring = "java";

		String up = System.getProperty("user.dir");
		// LS.add("java"); //c:/eclipse311/workspace/
		// LS.add("-cp");
		// LS.add("c:/eclipse311/workspace/ViewForest");
		// LS.add("ViewForest/Main");
		LS.add("../gnuplot/bin/wgnuplot.exe");
		File wrkDir = (new File(wrkPath));
		if (wrkDir.exists() && wrkDir.isDirectory()) {
			LS.add(wrkDir.getAbsolutePath());
		} else {
			LS.add("../rad/tmp");
		}

		ProcessBuilder PB = new ProcessBuilder(LS);
		PB.directory(new File(wrkPath));
		Process PR = null;
		// PB.wait();
		/***********************************************************************
		 * try { //if (Debug.STAY_CHART_FLAG){ //PR = PB.start();
		 * //RE.exec("gnuplot/bin/wgnuplot.exe"); //} //BufferedInputStream
		 * InpStream = new BufferedInputStream((PR =
		 * PB.start()).getInputStream()); //BufferedOutputStream OutStream = new
		 * BufferedOutputStream(PR.getOutputStream()); //cdstring = "pwd\r\n";
		 * //byte[] Bt = script.getBytes(); //byte[] rBt = new byte[256];
		 * //OutStream.write(22); //OutStream.flush(); //int c; //while((c =
		 * InpStream.read(rBt))>0){ // System.out.println("Read " + c); //}
		 * //System.out.println("Start gnuplot"); } catch (IOException e) {
		 * e.printStackTrace(); }/
		 **********************************************************************/
		String ar[] = (String[]) LS.toArray(new String[LS.size()]);
		TraceViewer.Main.main(ar);

	}

	// Process all files and directories under dir
	public static void visitAllDirsAndFiles(File dir) {
		// process(dir);

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllDirsAndFiles(new File(dir, children[i]));
			}
		}
	}

	// Process only directories under dir
	public static void visitAllDirs(File dir) {
		if (dir.isDirectory()) {
			// process(dir);

			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllDirs(new File(dir, children[i]));
			}
		}
	}

	// Process only files under dir
	public static void visitAllFilesAndWrite(File visitDir, String ncf_path) {

		FilenameFilter fileFilter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				boolean ac;
				// String name = file.getName();
				ac = (name.endsWith(Conf.getProperty("scan.file.indicator")) || (new File(
						dir.getAbsolutePath() + "/" + name).isDirectory()))
						&& (!name.startsWith("."));
				return ac;
			}
		};

		if (visitDir.isDirectory()) {
			String[] children = visitDir.list(fileFilter);
			for (int i = 0; i < children.length; i++) {
				visitAllFilesAndWrite(new File(visitDir.getAbsolutePath() + "/"
						+ children[i]), ncf_path);
			}
		} else {
			processOneDir(null, visitDir.getAbsolutePath());
		}
	}

	public static void processOneDir(String data, String xml) {
		// Debug.println(data);

		File fd = ((data == null) || (data.length() == 0)) ? null : new File(
				data);
		if (fd != null && fd.isDirectory()) {
			logger.fatal("Path is Directory, expected File => "
					+ fd.getAbsolutePath());
			logger.fatal("Execution stopped due to previous error");
			return;
		}
		File fil = null; // = new File(data); // получаем очередной файл
		// входных данных (не параметры!)
		if ((fd != null) && !fd.exists()) {
			// если входной файл с таким именем не существует, то
			// пропускаем его выводя сообщение
			System.out.println("Input File does not exist: "
					+ fil.getAbsolutePath());
			System.out.println("Sorry, Skipped");
			return;
		}

		// пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅ
		File dir = (new File(xml)).getParentFile();
		HashMap<String, Double[]> THM = SubTraceConverter
				.readFileToHashMapOfDoubleArrayWDesc(dir + "/" + "subTraceInfo.out");
		Double[] jdt = THM.get("JulianTime");
		logger.info("JulianTime = " + jdt[0]);
		logger.info("Ind: " + jdind.getCurrentCounter()[0]);
		int search = fastSearch(jdt[0]);
		search = (search < 0) ? fullSearch(jdt[0]) : search;
		try {
			if (search < 0) {
				logger.warn("JulianTime = " + jdt[0] + "NOT FOUND IN NCF!!!");
				missed++;
			} else {
				Vector<Double[]> vd = readFileToVectorOfDoubleArray(dir + "/"
						+ "trans_sum_rez.dat", 1);
				Double[] d = vd.get(0);
				logger.info("trans_sum_rez.dat " + d.length);
				ArrayDouble fArray = new ArrayDouble.D1(1);
				Index fi = fArray.getIndex();
				
				fArray.setFloat(fi.set(0), d[0].floatValue());
				ncFile.write("hit_sum_ch4", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[1].floatValue());
				ncFile.write("iao_sum_ch4", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[2].floatValue());
				ncFile.write("hit_scat_surf", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[3].floatValue());
				ncFile.write("iao_scat_surf", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[4].floatValue());
				ncFile.write("hit_scat_atm", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[5].floatValue());
				ncFile.write("iao_scat_atm", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[6].floatValue());
				ncFile.write("hit_sum_ch4_band", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[7].floatValue());
				ncFile.write("iao_sum_ch4_band", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[8].floatValue());
				ncFile.write("calc9", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[9].floatValue());
				ncFile.write("calc10", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[10].floatValue());
				ncFile.write("calc11", new int[] { search }, fArray);
				
				fArray.setFloat(fi.set(0), d[11].floatValue());
				ncFile.write("calc12", new int[] { search }, fArray);
				/*
				devf->hit_sum_ch4 = (/trans/)
				devf->iao_sum_ch4 = (/trans/)
				devf->hit_scat_surf = (/trans/)
				devf->iao_scat_surf = (/trans/)
				devf->hit_scat_atm = (/trans/)
				devf->iao_scat_atm = (/trans/)
				devf->hit_sum_ch4_band = (/trans/)
				devf->iao_sum_ch4_band = (/trans/)
				devf->calc9 = (/trans/)
				devf->calc10 = (/trans/)
				devf->calc11 = (/trans/)
				devf->calc12 = (/trans/)
				 */
				
				/*
			    1 Summary signal with CH4 from HITRAN
					2 Summary signal with CH4 from IAO 
					3 Scatterd from surface signal (HITRAN)
					4 Scatterd from surface signal (IAO)
					5 Single scattering in atmosphere signal (HITRAN)
					6 Single scattering in atmosphere signal (IAO)
					7 Summary signal in CH4 absorption band (HITRAN)
					8 Summary signal in CH4 absorption band (IAO)
					9  100*(sum1-sum2)/sum1, %
					10 100*(sum3-sum4)/sum3, %
					11 100*(sum5-sum6)/sum5, %
					12 100*(sum7-sum8)/sum7, %
			 */
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		procfilecnt++;
		logger.info("Processed: " + dir.getAbsolutePath());

	}

	private static int fullSearch(Double jdt) {
		// TODO Auto-generated method stub
		try {
			int begind = 0; // jdind.getCurrentCounter()[0];
			int endind = jdind.getShape()[0] - 1;
			// BufferedWriter BW = new BufferedWriter(new
			// FileWriter("resources/jddump.txt"));
			// BW.append("*****************************************************\n");
			double cdt;
			for (int i = begind; i <= endind; i++) {
				jdind.set(i);
				cdt = jdtarr.getDouble(jdind);
				// BW.append(jdt + " <=> " + cdt + "\n");
				// System.out.println(jdtarr.getDouble(jdind) + "<=>"+jdt);
				if (Math.abs(cdt - jdt) < 1.e-7) {
					// BW.append("fullSearch: Strike3 " + jdt + " <=> " + cdt +
					// "\n");
					logger.info("fullSearch: Strike3");
					strike3++;
					logger.info("delta: " + (cdt - jdt));
					return jdind.getCurrentCounter()[0];
				} else {
					// logger.info("delta: "+
					// String.format("%12.8e",jdtarr.getDouble(jdind)-jdt));
				}
				// BW.flush();
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
		return -1;
	}

	private static int fastSearch(Double jdt) {
		// TODO Auto-generated method stub
		int begind = jdind.getCurrentCounter()[0];
		int endind = begind;
		if ((begind + 1) < jdind.getShape()[0]) {
			jdind.set(begind + 1);
		}
		double cdt;
		cdt = jdtarr.getDouble(jdind);
		if ((Math.abs(cdt - jdt) < 1.e-7)) {
			logger.info("fastSearch: Strike1");
			logger.info("delta: " + (cdt - jdt));
			strike1++;
			return jdind.getCurrentCounter()[0];
		} else {
			jdind.set(begind);
		}

		if (begind <= 4) {
			begind = 0;
		} else {
			begind = begind - 4;
			// endind = endind + 4;
		}

		endind = ((endind + 4) < jdind.getShape()[0]) ? (endind + 4) : (jdind
				.getShape()[0] - 1);

		for (int i = begind; i <= endind; i++) {
			jdind.set(i);
			cdt = jdtarr.getDouble(jdind);
			if (Math.abs(cdt - jdt) < 1.e-7) {
				logger.info("fastSearch: Strike2");
				strike2++;
				return jdind.getCurrentCounter()[0];
			}
		}
		return -1;
	}

	public static Vector<Double[]> readFileToVectorOfDoubleArray(String fname,
			int beginRow) {
		ArrayList<String[]> al;
		Vector<Double[]> vect = null;
		File file = new File(fname);
		al = Runner.readAndSplitTextFileToArrayList(file, beginRow);
		vect = Runner.convertArrayListOfStringToVectorOfDoubleArrays(al);
		return vect;
	};
}
