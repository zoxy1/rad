package rad;

import platform.ChargeRAD; 
import platform.Splitter; 
import platform.Conf;

import java.io.*; 
import java.text.SimpleDateFormat;
import java.util.*; 
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import rad.bin.Runner; 
import org.w3c.dom.Document; 
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException; 
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Стартовый класс. Основная задача считать аргументы командной строки (если есть), 
 * найти все ресурсы, запустить счет. Вывести сообщение об успешности запуска.
 * В некоторых режимах предусмотрено, что вызов метода main может происходить 
 * рекурсивно в процессе расчета. Это нужно в режиме "split", когда необходимо
 * провести перебор ряда входных параметров, и с каждым из них выполнить цепочку 
 * расчета.
 * 
 */

public class Main2 implements Runnable {

	static final int windowHeight = 500;

	static final int leftWidth = 350;

	static final int rightWidth = 350;

	static final int windowWidth = leftWidth + rightWidth;

	static Boolean wasEntered = null;

	static int mainEntryCounter = 0;

	static public Vector<String> initialArgs = new Vector<String>();;

	File DataFile;

	Document parTree;

	String DocFile;

	// String IteratedFile;
	String WORKER_NAME;

	String TMP_DIR;

	String ITER_XML;

	static Logger logger = Logger.getLogger(Main2.class);

	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS"); // формат времени

	/**
	 * Разбор входных параметров, проверка на согласованность. 
	 * Формирует Properties (список параметров)
	 * для быстрого доступа из любой точки программы.
	 * 
	 * Инициализирует ресурсы: все параметры, указанные файлы, пути
	 * Вызывает методы на выполнение расчета.
	 * 
	 * @param args
	 */

	public static void main(String[] args) {
		try {
			
			System.out.println("user.dir :=" + System.getProperty("user.dir"));
			mainEntryCounter++;
			if (mainEntryCounter == 1) { // проверка, это первый вход или рекурсивный
				PropertyConfigurator.configure(System.getProperty("user.dir") // конфигурируем логгер для ведения лога
						+ "/log4j/log4j.properties");
			}
			;
			String full_arg_string = "";
			for (int i = 0; i <= args.length - 1; i++) {
				full_arg_string = full_arg_string + args[i] + " "; // собираем все параметры командной строки в одну 
				//б-а-альшую строку - нужно запомнить если потребуется рекурсивный вход
			}

			if (!(new File("logs").exists())) { // если каталог logs, который будет содержать логи отсутствует - создаем
				new File("logs").mkdir();
			}
			System.out.println("Main entry " + mainEntryCounter
					+ " with parameters: " + full_arg_string);
			logger.info("Main entry " + mainEntryCounter + " with parameters: "
					+ full_arg_string);

			Locale.setDefault(Locale.US); // устанавливает локаль в которой
			// десятичным разделителем явл точка

			// устанавливаем временную зону GMT
			// все даты, указанные в формате год месяц день и т.д. будут
			// считаться в зоне GMT
			// для времени в миллисекундах также будем считать для GMT если
			// отдельно не указана дата

			TimeZone tz = TimeZone.getTimeZone("GMT"); // опираемся на гринвич, а не на локальное время
			TimeZone.setDefault(tz);

			
			/*
			 * Далее идет блок разбора аргументов командной строки и соотв. инициализация
			 */
			String cfile;
			if ((cfile = System.getProperty("config.file")) != null) { // был ли задан имя конфигурационного файла 
				new Conf(cfile); // если да - заполняем Properties из файла по умолч + параметры из cfile
				// свойства с одинаковыми именами будут в окончательном виде взяты из cfile
			} else {
				new Conf(); // если нет, то заполяем Properties из файлов по умолчанию
			}
			try {
				parseArg(args); // разбор аргументов строки

				Conf.setProperty("app.home.path", get_path()); // задание свойства - текущий каталог, от него отсчитываются многие другие относит. пути
				//printConf();
				
				printResolvedConf(); // сводный список всех действующих свойств

				analizeConf(); // анализ заданных параметров на согласованность
			} catch (Exception e) {
				e.printStackTrace();
				proceesException(e, logger, "rad.Main2");
				return;
			}
			if (mainEntryCounter == 1) {
				// запоминаем командную строку в специальном свойстве, 
				//чтобы потом при рекурсии его можно было бы извлеч
				Conf.setProperty("initial.cmd.arg.string", full_arg_string); 
			}
			
			// далее последовательность проверок свойств, выяснение режима в котором будет проведен расчет
			// в каждом из if есть return поэтому после того как сработает хотя бы один из них, происходит выход из main
			if (!isEmpty(Conf.run_par_file_path)) {
				// здесь осущ. запуск на расчет одного файла задачи с одним файлом данных
				// если есть
				startOneFile(getProperty(Conf.data_file_path),
						getProperty(Conf.run_par_file_path));

				return;
			}

			if (!isEmpty(Conf.split_file_path)) {
				// здесь осущь расщепление параметров в специальный каталог

				File dir = new File(getProperty(Conf.split_dir_path));
				SplitFile(getProperty(Conf.split_file_path), dir);
				if (!isEmpty(Conf.split_dir_path)) {
					Conf.setProperty(Conf.run_par_dir_path,
							getProperty(Conf.split_dir_path));
				} else {
					return;
				}
			}

			if (!isEmpty(Conf.run_par_dir_path)&&(new File(getProperty(Conf.run_par_dir_path)).exists())) {
				// здесь осущ. запуск всех файлов рекурсивно из каталога

				File dir = new File(getProperty(Conf.run_par_dir_path));
				RunDir(dir, null);
				return;
			} else {
				throw new Exception("Property " + Conf.run_par_dir_path + " must exist, but not found \n" +
				"value := " + Conf.getProperty(Conf.run_par_dir_path));				
			}

		} catch (Exception ioe) {
			ioe.printStackTrace();
			logger.fatal(ioe.getMessage());
		}
	}

	public static String getProperty(String propName) {
		return Conf.getProperty(propName);
	}

	public static boolean isTrue(String propName) {
		return Conf.getProperty(propName).trim().equalsIgnoreCase("true");
	}

	public static boolean isEmpty(String propName) {
		String prop = Conf.getProperty(propName);
		if (prop != null && prop.trim().length() != 0) {
			return false;
		} else {
			return true;
		}
	}

	private static void analizeConf() throws Exception {

		String sprop = null;

		if (!isEmpty(Conf.run_par_file_path) && !isEmpty(Conf.split_file_path)) {
			throw new Exception("Properties: " + Conf.run_par_file_path
					+ " and " + Conf.split_file_path
					+ " can NOT be defined simultaneously, only one is allowed");
		}

		if (!isEmpty(Conf.run_par_file_path)) {
			logger.warn("Property " + Conf.run_par_file_path
					+ " is defined THAN Properties will be ignored:\n");
			logger.warn(" " + Conf.run_par_dir_path + " = "
					+ Conf.getProperty(Conf.run_par_dir_path) + "\n");
			logger.warn(" " + Conf.split_dir_path + " = "
					+ Conf.getProperty(Conf.split_dir_path) + "\n");
		} else if (!isEmpty(Conf.split_file_path)) {
			if (isEmpty(Conf.split_dir_path)) {
				throw new Exception(" Property " + Conf.split_dir_path
						+ " must be defined when Property "
						+ Conf.split_file_path + " is specified");
			}
		} else if (isEmpty(Conf.run_par_dir_path)) {
			throw new Exception(
					"At least One from Properties must Be specified: "
							+ Conf.run_par_file_path + ", "
							+ Conf.split_file_path + ", "
							+ Conf.run_par_dir_path);
		} else if (!(new File(Conf.getProperty(Conf.run_par_dir_path)).exists())) {
//			throw new Exception("Property " + Conf.run_par_dir_path + " must exist, but not found \n" +
//					"value := " + Conf.getProperty(Conf.run_par_dir_path));
			logger.warn("Property " + Conf.run_par_dir_path + " pointed to non exsisten path: " + Conf.getProperty(Conf.run_par_dir_path));
		}

		/*
		 * sprop = Conf.getProperty(Conf.run_par_file_path); { if (!(new
		 * File(sprop)).exists()) { throw new Exception("Property " +
		 * Conf.run_par_file_path + " must be specified and exist, but found :" +
		 * "\"" + sprop + "\""); } }
		 */

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
/**
 * Разбор всех аргументов командной строки и занесение их в Properties
 * при этом производится проверка. Все аргументы командной строки должны иметь известные имена.
 * То есть пользователь не может передать аргумент, который программе не известен.
 * Для этого все имена сравниваются с именами из файла свойств по умолчанию. Если пользователь
 * сделал опечатку или задал неизвестный параметр - выброс исключения и выход из программы.
 * @param args
 * @throws Exception
 */
	public static void parseArg(String args[]) throws Exception {
		if (args.length == 0) {
			System.out.println("No command string settings");

			//printConf();
			return;
		} else {
			Properties cProp = Conf.getProperties();
			for (int i = 0; i < args.length; i++) {
				String cstr = args[i].replace("-D", "");
				String patternStr = ".*[=]";
				Pattern pattern = Pattern.compile(patternStr);
				
				int iEq = cstr.indexOf("=");
				String propName = cstr.substring(0, iEq);
				String propValue = cstr.substring(iEq + 1, cstr.length());
				logger.debug(propName + " = " + propValue);
				if (initialArgs == null) {
					initialArgs = new Vector<String>();
				}
				if (cProp.containsKey(propName)) {
					cProp.setProperty(propName, propValue);
					if (mainEntryCounter == 1) {
						initialArgs.add(args[i]);
					}

				} else {
					logger.fatal("Such propery not found in conf.properties: "
							+ propName);
					throw new Exception(
							"Such propery not found in conf.properties: "
									+ propName);
				}
			}
		}
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

		// logger.info(Conf.add_time = PROP.getProperty(Conf.add_time));
		// logger.info(PROP.getProperty(Conf.browser_run));
		// logger.info(PROP.getProperty(Conf.debug_mode));

	}
	
	public static void printResolvedConf() throws Exception {
		// Properties PROP =
		// Get all system properties
		Properties props = Conf.getProperties();
		// props.list(System.out);
		logger.info("Resolved settings in conf.properties are:");
		// Enumerate all system properties
		Enumeration enu = props.propertyNames();
		for (; enu.hasMoreElements();) {
			// Get property name
			String propName = (String) enu.nextElement();
			// Get property value
			String propValue = Conf.getProperty(propName);
			logger.info(propName + " = " + propValue);
		}

		// logger.info(Conf.add_time = PROP.getProperty(Conf.add_time));
		// logger.info(PROP.getProperty(Conf.browser_run));
		// logger.info(PROP.getProperty(Conf.debug_mode));

	}

	public static String get_path() throws IOException {
		String home_path;

		home_path = System.getProperty("user.dir");
		/*Properties props = System.getProperties(); // Enumerate all system properties 
		Enumeration en = props.propertyNames(); 
		 //propName;
		//String propValue;
		for (; en.hasMoreElements(); ) { 
			// Get property name 
			String propName = (String)en.nextElement(); // Get property value 
			String propValue = (String)props.get(propName); 
			System.out.println(propName +" = " + propValue);
		}
		
		
			
		

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
		}*/

		 return home_path;
	}

	void initialize() {
		ITER_XML = (!isEmpty(Conf.run_iter_file_path)) ? Conf
				.getProperty(Conf.run_iter_file_path) : this.DocFile;
		WORKER_NAME = getProperty(Conf.worker_dir_name);
		TMP_DIR = getProperty(Conf.tmp_dir_name);
	}

	public Main2(File dataFile, String DocFileXML, String IteratedXML) { // конструктор
		// потока
		// (входной
		// файл,
		// дерево
		// параметров)
		DataFile = dataFile;
		DocFile = DocFileXML;
		parTree = getDOMfromXML(DocFileXML); // new
		// DomEcho(DocFileXML).getDocument();
		initialize();
	}

	public Main2(File dataFile, String DocFileXML) { // конструктор потока
		// (входной файл, дерево
		// параметров)
		DataFile = dataFile;
		DocFile = new File(DocFileXML).getAbsolutePath();
		parTree = getDOMfromXML(DocFile); // new
		// DomEcho(DocFileXML).getDocument();
		initialize();
	}

	/***************************************************************************
	 * public Main (String DocFileXML){ // конструктор потока (файл параметров)
	 * DataFile = null; // входной файл данных отсутствует DocFile = DocFileXML; //
	 * файл параметров расчета parTree = new DomEcho(DocFileXML).getDocument(); //
	 * дерево параметров после обработки xml файла параметров }/
	 **************************************************************************/

	public Main2(Document doc) { // конструктор потока (дерево параметров)
		DataFile = null; // входной файл отсутствует
		DocFile = null;
		parTree = doc;
		initialize();
	}

	public Main2(File dataFile, Document doc) { // конструктор потока (дерево
		// параметров)
		DataFile = dataFile; // входной файл отсутствует
		DocFile = null;
		parTree = doc;
		initialize();
	}

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

	public static void RunDir(File dir, String data) {
		long bt = new Date().getTime();

		System.out.println("Rundir Started in :" + sdf.format(new Date(bt)));
		// if (al.size()==1) {
		visitAllFilesAndRun(data, dir);
		// } else {
		// visitAllFilesAndRun(al.get(1), dir);
		// }
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
								+ "/" + name).isDirectory()))
								&& (!name.startsWith(".")))||(name.startsWith(".")&&name.endsWith(".xml"));
						return ac;
					}
				};

				String[] children = docFileDir.list(fileFilter);
				if (isEmpty(Conf.run_par_file_path)) {
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
			if (isTrue(Conf.browser_run_flag)) {
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
	public static void visitAllFilesAndRun(String data, File dir) {

		FilenameFilter fileFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				boolean ac;
				// String name = file.getName();
				ac = (name.endsWith(".xml") || (new File(dir.getAbsolutePath()
						+ "/" + name).isDirectory()))
						&& (!name.startsWith("."));
				return ac;
			}
		};

		if (dir.isDirectory()) {
			String[] children = dir.list(fileFilter);
			for (int i = 0; i < children.length; i++) {
				visitAllFilesAndRun(data, new File(dir, children[i]));
			}
		} else {
			startOneFile(data, dir.getAbsolutePath());
		}
	}

	public static void startOneFile(String data, String xml) {
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
		// если файл входных данных существует то запускаем соответствующие
		// процессы

		Main2 M = new Main2(fd, xml);// DE.getDocument()); // создаем

		Thread T = new Thread(M);
		T.start(); // стартуем поток расчетов

		Boolean isRunning = true;
		Thread.State exitCode;
		// ?????????????!!!Infinity loop!!!????????????????????
		while (isRunning) {
			try {
				Thread.sleep(50);
				// cout.read(processInBuff);
				// System.out.println(new String(processInBuff));
				exitCode = T.getState();
				if (exitCode == Thread.State.TERMINATED) {
					isRunning = false;
				}
			} catch (IllegalThreadStateException ite) {
				System.out.println("Process still working... stand by...");
			} catch (Exception e) {
				// System.out.println("Process still working... stand
				// by...");
				e.printStackTrace();
			}
			/*******************************************************************
			 * / finally {
			 * while(InpStream.available()!=0&&(c=InpStream.read(bt))>0){
			 * OutFile.write(bt,0,c); }
			 * while(ConStream.available()!=0&&(c=ConStream.read(bt))>0){
			 * //OutFile.write(bt,0,c); //System.out.println(new String(bt));
			 * OutConFile.write(bt,0,c); } } /
			 ******************************************************************/

		}
	}
}
