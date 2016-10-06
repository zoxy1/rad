package rad.bin;

import rad.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import platform.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.*; //import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xpath.XPathAPI; //import platform.DomEcho;
import platform.Module;
import platform.Splitter;
import org.apache.log4j.*;

public abstract class Runner {

	String wrkPath;

	String mdFileName;

	public String mdWrkPath;

	Node mdNode;

	String execName;

	static final HashMap<String, HashMap<String, Double[]>> CasheOfHmap = new HashMap<String, HashMap<String, Double[]>>();

	public static String ITER_XML = "";

	static Logger runnerLogger = Logger.getLogger(Runner.class);

	/**
	 * @param args
	 */


	public void run(Module module, String wrkPath) throws Exception { // запуск 
		// отдельного
		// модуля
		this.wrkPath = wrkPath;
		// System.out.println("ClusterForever!!!");
		//File f = new File("something.txt");
		//f.createNewFile();
		// this.mdFileName = moduleFileName;
		// DomEcho DE = new DomEcho(wrkPath +"/"+moduleFileName); // парсим файл
		// параметров данного модуля и создаем дерево
		Node MNd = module.mdNode; // извлекаем из дерева узел для данного
		// модуля
		mdNode = MNd;
		Module[] MdArr = null;
		execName = module.ExecName;
		// String moduleFileName =
		try {
			// MdArr = Splitter.Split(MNd); // предпринимаем попытку разделить
			// модуль на подмодули (для переборных задач) - пока не реализовано
			File F = new File(wrkPath + "/.service/" + module.XMLParFileName
					+ "_dir/"); // создаем временный подкаталог для модуля - к
			// имени файла параметра добавляется суффикс
			// "_dir"
			F.mkdirs();
			mdWrkPath = F.getAbsolutePath();
			Runner.copyFile(wrkPath + "/.service/" + module.XMLParFileName,
					this.mdWrkPath + "/" + module.XMLParFileName);
		} catch (Exception e) {
			e.printStackTrace();
			Runtime.getRuntime().exit(3);
		}
		if (MNd == null) {
			runnerLogger.fatal("Module not found");
		} else {
			// String MDname = Splitter.getElementTextByName(MNd, "title");
			runnerLogger.info(Splitter.getElementTextByName(MNd, "title"));
			runnerLogger.info(module.ExecName);
			// go(MNd);
			runModule(module);
			/*******************************************************************
			 * if (execName.equals("rad.bin.RunnerSurface")){ File surfile = new
			 * File(wrkPath+"/surface.out"); BufferedReader Br = new
			 * BufferedReader(new FileReader(surfile)); String surfType =
			 * Br.readLine(); if (surfType.contains("Water (and Goode's
			 * interrupted space) <100.0%>")){ File wrkF = new File(wrkPath);
			 * File newWrkfile = new File(wrkF.getName()
			 * .replace("point","water")); //wrkF.) boolean flg =
			 * wrkF.renameTo(newWrkfile); System.out.println(flg); } }/
			 ******************************************************************/
			Node graph = Splitter.getElementByName(MNd, "graphics");
			runGraphics(graph);
		}
	}

	abstract void runModule(Module modul) throws Exception;

	public void proceesException(Exception e, Logger lg, String className) {
		StackTraceElement T[] = e.getStackTrace();
		String exString = T[0].toString() + "\n";
		// PrintStream PS = new PrintStream();
		for (int i = 0; i <= T.length - 1; i++) {
			if (T[i].getClassName().equals(className)) {
				exString = exString + T[i].toString() + "\n";
				// T[i].
			}
		}
		// lg.fatal(e.getMessage());
		// lg.debug(e.printStackTrace());
		lg.fatal(exString);

	}

	public void throwHashMapException(Object Arr, String key) throws Exception {
		if (Arr == null) {
			throw (new Exception("In HashMap there is No array with key => "
					+ key));
		}
	}

	public Runner() {

	}

	/*
	 * public void setCashe (HashMap Cashe){ CasheOfHmap = Cashe; }
	 */

	public void runGraphics(Node graph) {
		if ((graph == null) || (graph.getTextContent().trim().length() == 0)) {
			runnerLogger.info("No graphics node, or graph node is empty");
			return;
		}
		String script = null;
		Node scr = Splitter.getElementByName(graph, "script");
		script = scr.getTextContent();
		// if (Debug.RUN_DIR_FLAG||Debug.SPLIT_AND_RUN_FLAG){
		// script = " set terminal unknown #added by runner to suspend
		// windowing"+"\n" + script;
		// script = script + "\n" + "set terminal gif #added by runner to save
		// results as gif";
		// script = script + "\n" + "set output " + "\""+execName+".gif" + "\""
		// + " #added by runner to save results as gif";
		// script = script + "\n" + "replot" + " #added by runner to save
		// results as gif" + "\n";

		script = script + "\n" + "unset output" + "\n" + "set terminal windows"
				+ " #added by runner to restore interactive mode" + "\n"
				+ "replot\n";

		// }
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(wrkPath + "/"
					+ execName + "_script.gps"));
			bw.write(script.trim());
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Runtime RE = Runtime.getRuntime();
		Vector<String> LS = new Vector<String>();
		// String cdstring =
		// System.getProperty("user.dir")+"/"+"gnuplot/bin/wgnuplot.exe";
		// String cdstring =
		// System.getProperty("user.dir")+"/"+"gnuplot/bin/wgnuplot_pipes.exe";
		String cdstring = Conf.getProperty(Conf.gnuplot_bin_path); // "../" +
																	// "gnuplot/bin/wgnuplot_pipes.exe";
		LS.add(cdstring);
		if (rad.Main2.isTrue(Conf.stay_chart_flag)) {
			LS.add("-persist");
		}
		LS.add(execName + "_script.gps");
		ProcessBuilder PB = new ProcessBuilder(LS);
		PB.directory(new File(wrkPath));
		Process PR = null;
		// PB.wait();
		try {
			if (rad.Main2.isTrue(Conf.stay_chart_flag)) {
				PR = PB.start(); // RE.exec("gnuplot/bin/wgnuplot.exe");
			}
			// BufferedInputStream InpStream = new BufferedInputStream((PR =
			// PB.start()).getInputStream());
			// BufferedOutputStream OutStream = new
			// BufferedOutputStream(PR.getOutputStream());
			// cdstring = "pwd\r\n";
			// byte[] Bt = script.getBytes();
			// byte[] rBt = new byte[256];
			// OutStream.write(22);
			// OutStream.flush();
			// int c;
			// while((c = InpStream.read(rBt))>0){
			// System.out.println("Read " + c);
			// }
			// System.out.println("Start gnuplot");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile(String dirty_name) {

		String fname = dirty_name.trim(); //replacedPropertyString(dirty_name);

		File file = null;
		int iofast;
		String ns;
		File absFile = new File(fname);
		if ((absFile.isAbsolute() & absFile.exists())
				|| (absFile.exists() & (fname.trim().startsWith("/") || fname
						.trim().startsWith("\\")))) {
			file = absFile;
		} else if ((iofast = fname.indexOf("*")) != -1) {
			// pref = fname.substring(0, iofast);
			ns = fname.replace(".", "[.]");
			ns = ns.replace("*", ".*");

			if ((file = getFileByRelativeName(ns)) == null) {
				file = getFileByRelativeName("[.]service/" + ns);
			}
		} else {
			// ns = fname.replace(".","[.]");
			if ((file = getFileByAbsolutName(fname)) == null) {
				file = getFileByAbsolutName(".service/" + fname);
			}

		}

		if (file != null) {
			runnerLogger.debug("Getting real file for fname: " + fname
					+ " ==> " + file.getAbsolutePath());
		} else {
			runnerLogger.warn("File not found in getFile(): " + fname);
		}

		return file;
	}

	public static String replacedPropertyString(String dirty_name) throws Exception {
		String fname = new String(dirty_name);
		ArrayList<String> keys = new ArrayList<String>();
		String[] props;
		do {
			props = getAllPropertyStrings(fname, keys);
			if (props != null) {
				for (int i = 0; i < props.length; i++) {
					fname = fname.replace(keys.get(i), props[i]);
				}
				keys = new ArrayList<String>();
			}
		} while (props != null);
		

		return fname;
	}

	public static String[] getAllPropertyStrings(String string,
			ArrayList<String> kal) throws Exception {
		String inner[] = getAllPropertyStrings(string, kal, Conf
				.getProperties());

		return inner;
	}

	public static String[] getAllPropertyStrings(String string,
			ArrayList<String> kal, Properties Prop) throws Exception {
		String inner[];
		// inner = string.split("[{].[}]");
		// System.out.println(string.matches("[{].*[}]"));
		RETokenizer rt = new RETokenizer(string, "[{][a-zA-Z_0-9[.]]*[}]",
				false);
		ArrayList<String> al = new ArrayList<String>();
		for (int i = 0; rt.hasNext(); i++) {
			String key = ((String) rt.next()).replace("{", "").replace("}", "");
			String getPr = Prop.getProperty(key.trim());
			if (getPr == null) {
				throw new Exception("Key: " + key.trim()+ " not found in Property");
			}
			String prop = getPr.trim();
			kal.add("{" + key.trim() + "}");
			if (prop != null) {
				al.add(prop);
			} else {
				System.out.println("Property not found in Conf :" + key);
				al.add("{" + key.trim() + "}");
			}
		}
		inner = al.isEmpty() ? null : (String[]) al.toArray(new String[al
				.size()]);
		// System.out.println(al);
		return inner;
	}

	public String getOutFile(String fname) {
		File file = null;

		file = getFile(fname);
		String str = null;
		String suf = null;

		suf = Splitter.getElementTextByName(mdNode, "sufix");

		if (suf == null || suf.length() == 0) {
			suf = Splitter.getElementTextByName(mdNode, "title");
		}

		if (file != null) {
			str = file.getName();
			String ext = null;
			int pos = str.lastIndexOf(".");
			switch (pos) {
			case -1:
				str = str + "." + suf.trim();
				break;
			default:
				ext = str.substring(pos, str.length());
				str = str.substring(0, pos) + "." + suf.trim() + ext;
			}
		} else {
			int posast = fname.indexOf("*");
			if (posast == -1) {
				str = fname;
			} else {
				str = fname.substring(0, posast) + ".txt";
			}

		}

		// String path = file.getPath();

		// file = new File(str);
		runnerLogger
				.info("Sucsessfully Getting OutFile name for Mask File Name : "
						+ fname + " := " + str);
		return str;
	}

	/**
	 * Возвращает переменную типа File по заданному имени файла. Файл ищется в
	 * текущем рабочем каталоге wrkPath для данного Module. <br>
	 * Если имя файла начинается подстрокой "user.dir" то эта строка заменяется
	 * на значение, возвращаемое <code> System.getProperty("user.dir") </code>
	 * 
	 * @param fname
	 *            имя файла
	 * @return файловую переменную указывающую на найденный файл
	 */
	public File getFileByAbsolutName(String fname) {
		File dir;
		String newName = fname;
		if (fname.startsWith("user.dir")) {
			dir = new File(System.getProperty("user.dir"));
			newName = dir.getAbsolutePath() + "/"
					+ fname.replace("user.dir", "");
			if (new File(newName).exists()) {
				return new File(newName);
			}
		} else {
			File newDir = new File(fname);
			newName = newDir.getName();
			String nDir = fname.replace(newName, "");
			dir = (fname.contains("/") || fname.contains("\\")) ? (new File(
					wrkPath + "/" + nDir)) : (new File(wrkPath));

		}
		FilenameFilterEq filter = new FilenameFilterEq(newName);
		String[] children;
		children = dir.list(filter);
		if ((children != null) && (children.length != 0))
			dir = new File(dir + "/" + children[0]);
		else
			dir = null;

		return dir;
	}

	public File getFileByRelativeName(String RelFname) {
		File dir = new File(wrkPath);

		String[] children = dir.list();
		/*
		 * if (children == null) { // Either dir does not exist or is not a
		 * directory } else { for (int i=0; i<children.length; i++) { // Get
		 * filename of file or directory String filename = children[i]; } }
		 */

		// It is also possible to filter the list of returned files.
		// This example does not return any files that start with `.'.
		FilenameFilterMatcher filter = new FilenameFilterMatcher(RelFname);
		children = dir.list(filter);

		int moreChars = -1;
		int indexMoreChars = -1;
		if (children != null) {
			if (children.length != 0) {
				for (int i = 0; i < children.length; i++) {
					if (moreChars < children[i].length()) {
						moreChars = children[i].length();
						indexMoreChars = i;
					}
				}
				dir = new File(wrkPath + "/" + children[indexMoreChars]);
			} else {
				dir = null;
			}

		} else
			dir = null;

		return dir;
	}

	public void clearFile(String pref) {
		File dir = new File(wrkPath);
		FilenameFilterContain filter = new FilenameFilterContain(pref);
		String[] children = null;
		children = dir.list(filter);
		if ((children != null) && (children.length != 0)) {
			for (int i = 0; i < children.length; i++) {
				dir = new File(wrkPath + "/" + children[i]);
				if (dir.delete())
					Debug.println("File: " + dir.getName() + " was cleaned up");
			}
		} else
			Debug.println("No files to clean up");

	}

	public static ArrayList<String[]> readAndSplitTextFileToArrayList(File file,
			int firstString) {
		// Double[][] dub = null;
		String rStr = null;
		String[] splt = null;
		int strings = 0;
		ArrayList<String[]> al = new ArrayList<String[]>(600);
		BufferedReader FR = null;
		try {
			FR = new BufferedReader(new FileReader(file));
			for (int i = 1; i < firstString; i++) {
				rStr = FR.readLine();
			}
			while ((rStr = FR.readLine()) != null) {
				if (rStr.trim().length() != 0) {
					strings++;
					// splt = rStr.trim().split("\\s+|!.*|#.*|[*].*|c.*|C.*");
					String rex = "\\s+[:_].*|\\s+[.]\\D.*|\\s+|[[^0-9]&&[^Ee+-.:_]].*|[eE]+[[^0-9]&&[^Ee+-.]].*|[^0-9]+[:_]+[^0-9].*";
					// String[] splt2 = rStr.trim().split(rex);
					// String[] splt3 =
					// rStr.trim().split("\\s+|!.*|#.*|[*].*|c.*|C.*"+"|"+rex);
					// splt =
					// rStr.trim().split("\\s+|!.*|#.*|[*].*|c.*|C.*"+"|"+rex);
					splt = null;
					splt = rStr.trim().split(rex);
					if ((splt != null) && (splt.length > 0)) {
						al.add(splt);
					}

				}
			}
			// System.out.println(al);
			// dub = new Double[strings][splt.length];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				FR.close();
				FR = null;
			} catch (IOException e) {
				// TODO: handle exception
			}
		}

		return al;

	}

	public HashMap<Integer, Double[]> convertArrayListOfStringToDoubleArrays(
			ArrayList<String[]> al) {
		HashMap<Integer, Double[]> map = new HashMap<Integer, Double[]>();
		String st[] = al.get(0);
		for (int i = 0; i <= st.length - 1; i++) {
			map.put((new Integer(i)), (new Double[al.size()]));
		}
		for (Integer i = new Integer(0); i <= al.size() - 1; i++) {
			st = al.get(i);
			for (Integer j = new Integer(0); j < st.length; j++) {
				// System.out.print(st[0]+" ");
				Double[] dub = map.get(j);
				if (dub != null) {
					// System.out.println(i+ " " + j+ " " + st[j]);
					dub[i] = Double.valueOf(st[j]);
				}
			}
			// System.out.println();
		}

		return map;
	}

	public static HashMap<String, Double[]> convertArrayListOfStringToHashMapOfDoubleArrays(
			ArrayList<String[]> al, ArrayList<String> nameList,
			boolean isReverse) {
		HashMap<String, Double[]> map = new HashMap<String, Double[]>();
		String st[] = al.get(0);
		for (int i = 0; i <= st.length - 1; i++) {
			// if ((nameList.get(i)=)
			try {
				map.put(nameList.get(i), (new Double[al.size()]));
			} catch (IndexOutOfBoundsException e) {
				map.put("unknown" + i, (new Double[al.size()]));
				nameList.add("unknown" + i);
			}
		}
		Double[] dub = null;
		for (int i = 0; i <= al.size() - 1; i++) {
			if (isReverse) {
				st = al.get(al.size() - 1 - i);
			} else {
				st = al.get(i);
			}

			for (int j = 0; j < st.length; j++) {
				// System.out.print(st[0]+" ");
				try {
					dub = null;
					dub = map.get(nameList.get(j));
				} catch (IndexOutOfBoundsException e) {
					dub = null;
				} finally {
					if (dub != null) {
						// Debug.println(i+ " " + j+ " " + st[j]);
						try {
							dub[i] = Double.valueOf(st[j]);
						} catch (NumberFormatException e) {
							runnerLogger.warn("Index j = " + j + " st[j] = "
									+ st[j]);
//							System.out.println("Index j = " + j + " st[j] = "
//									+ st[j]);
						}

					}
				}

			}
			// Debug.println();
		}

		return map;
	}

	public static Vector<Double[]> convertArrayListOfStringToVectorOfDoubleArrays(
			ArrayList<String[]> al) {
		Vector<Double[]> vect = new Vector<Double[]>();
		String st[] = al.get(0);
		for (int i = 0; i <= st.length - 1; i++) {
			// map.put((new Integer(i)), (new Double[al.size()]));
			vect.add(new Double[al.size()]);
		}
		for (Integer i = new Integer(0); i <= al.size() - 1; i++) {
			st = al.get(i);
			Double[] dub = null;
			for (Integer j = new Integer(0); j < st.length; j++) {
				// Debug.print(st[0]+" ");
				dub = null;
				try {
					dub = vect.get(j);
				} catch (ArrayIndexOutOfBoundsException e) {
					// e.printStackTrace();
				} finally {
					if (dub != null) {
						dub[i] = Double.valueOf(st[j]);
					}
				}

			}
			// Debug.println();
		}

		return vect;
	}

	protected void copyOutFilesToWrkPath() {
		try {
			NodeList outFileNode = XPathAPI.selectNodeList(mdNode,
					".//outFile/name"); // [normalize-space(./name)=\""+fName.trim()+"\"]");
			for (int i = 0; i <= outFileNode.getLength() - 1; i++) {
				String fname = outFileNode.item(i).getTextContent().trim();
				Runner.copyFile(mdWrkPath + "/" + fname, wrkPath + "/" + fname);
				ArrayList<String> nl = getColArrayListForOutFileNodeByName(outFileNode
						.item(i));
				writeArrayListOfStringToFile(nl, new BufferedWriter(
						(new FileWriter(wrkPath + "/" + fname + ".dsc"))));
				// NodeList cn = XPathAPI.selectNodeList(outFileNode.item(i),
				// "col/name");
			}
		} catch (TransformerException te) {
			te.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	protected void copyInFilesToMdWrkPath() {
		try {
			NodeList inFileNode = XPathAPI.selectNodeList(mdNode,
					".//inFile/name"); // [normalize-space(./name)=\""+fName.trim()+"\"]");
			if ((inFileNode == null) || (inFileNode.getLength() == 0)) {
				System.out.println("No input file Nodes found");
			} else {

				for (int i = 0; i <= inFileNode.getLength() - 1; i++) {
					String fname = inFileNode.item(i).getTextContent().trim();
					Runner.copyFile(wrkPath + "/" + fname, mdWrkPath + "/"
							+ fname);
					ArrayList<String> nl = getColArrayListForInFileNodeByName(inFileNode
							.item(i));
					writeArrayListOfStringToFile(nl, new BufferedWriter(
							new FileWriter(mdWrkPath + "/" + fname + ".dsc")));
					// NodeList cn =
					// XPathAPI.selectNodeList(outFileNode.item(i), "col/name");
				}
			}
		} catch (TransformerException te) {
			te.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public  Vector<Double[]> readFileToVectorOfDoubleArray(String fname,
			int beginRow) {
		ArrayList<String[]> al;
		Vector<Double[]> vect = null;
		File file = getFile(fname);
		al = readAndSplitTextFileToArrayList(file, beginRow);
		vect = convertArrayListOfStringToVectorOfDoubleArrays(al);
		return vect;
	};

	/**
	 * Заполняет хэшмап массивами Double - из заданного текстового файла
	 * разбитого на колонки. Ключи - имена массивов должны быть описаны для
	 * заданного файла в параметрическом XML в разделе &lt;inFile&gt;
	 * 
	 * @param fName
	 *            имя файла с колоночной структурой, загружаемого в HashMap
	 * @return хэшмап массивов Double.
	 */
	public HashMap<String, Double[]> readFileToHashMapOfDoubleArray(String fName)
			throws Exception {
		ArrayList<String[]> al;
		ArrayList<String> nl = null;
		Node inFileNode = null;
		HashMap<String, Double[]> hm = null;
		File file = getFile(fName.trim());
		if (file == null) {
			throw new Exception(
					"Method readFileToHashMapOfDoubleArray can not find File: "
							+ fName.trim());
		}
		String searchFname = file.getName();
		try {
			mdNode.normalize();

//			inFileNode = XPathAPI.selectSingleNode(
//					mdNode,
//					".//inFile/name[contains(normalize-space(./text()), \""
//							+ searchFname.trim() + "\")]").getParentNode();
			
			inFileNode = XPathAPI.selectSingleNode(mdNode,
					".//inFile/name[normalize-space(./text())=\""
							+ fName.trim() + "\"]").getParentNode();;
			

			// inFileNode = XPathAPI.selectSingleNode(
			// mdNode,
			// ".//inFile/name[normalize-space(./text())=\""
			// + searchFname.trim() + "\"]").getParentNode();
			// NodeList NL = XPathAPI.selectNodeList(mdNode,
			// ".//inFile/*[normalize-space(./text())=\""+fName.trim()+"\"]");
			// file = getFile(fName.trim());
			String beginRowText;
			Node beginRowNode = XPathAPI.selectSingleNode(inFileNode,
					"startRow");
			Node reverseNode = XPathAPI.selectSingleNode(inFileNode,
					"isReverse");
			Boolean isReverse = (reverseNode != null) ? true : false;

			if (beginRowNode != null) {
				al = readAndSplitTextFileToArrayList(file, Integer
						.parseInt(beginRowNode.getTextContent().trim()));
			} else {
				al = readAndSplitTextFileToArrayList(file, 1);
			}
			// System.cl

			nl = getColArrayListForInFileNodeByName(inFileNode);

			hm = convertArrayListOfStringToHashMapOfDoubleArrays(al, nl,
					isReverse);
			// writeArrayListOfStringToFile(nl, file.getAbsolutePath()+".dsc" );
		} catch (Exception e) {
			// e.printStackTrace();
			// runnerLogger.fatal(e.toString());
			// proceesException(e, runnerLogger, this.getClass().getName());
			runnerLogger
					.fatal("Args \"fName\" of Runner.readFileToHashMapOfDoubleArray method => "
							+ fName);
			throw e;
		}
		return hm;
	};

	/**
	 * Заполняет хэшмап массивами Double - из заданного текстового файла
	 * разбитого на колонки. Ключи - имена массивов должны быть описаны в первой
	 * строке заданного файла, в отличие от readFileToHashMapOfDoubleArray для
	 * которого имена столбцов задаются в параметрическом XML в разделе
	 * &lt;inFile&gt;
	 * 
	 * @param fName
	 *            имя файла с колоночной структурой, загружаемого в HashMap
	 * @return хэшмап массивов Double.
	 */
	public HashMap<String, Double[]> readFileToHashMapOfDoubleArrayWDesc(
			String fName) {
		ArrayList<String[]> al;
		ArrayList<String> nl = null;
		Node inFileNode = null;
		HashMap<String, Double[]> hm = null;
		File file = null;

		try {
			mdNode.normalize();
			inFileNode = XPathAPI.selectSingleNode(mdNode,
					".//inFile/name[normalize-space(./text())=\""
							+ fName.trim() + "\"]");// .getParentNode();
			// NodeList NL = XPathAPI.selectNodeList(mdNode,
			// ".//inFile/*[normalize-space(./text())=\""+fName.trim()+"\"]");
			file = getFile(fName.trim());
			if (Debug.USE_CASHE && CasheOfHmap != null) {
				if ((hm = CasheOfHmap.get(file.getAbsolutePath().trim()
						.toLowerCase())) != null) {
					return hm;
				}

			}
			// String beginRowText;
			Node beginRowNode = null;
			Node reverseNode = null;
			if (inFileNode != null) {
				inFileNode = inFileNode.getParentNode();
				beginRowNode = XPathAPI
						.selectSingleNode(inFileNode, "startRow");
				reverseNode = XPathAPI
						.selectSingleNode(inFileNode, "isReverse");
			}
			Boolean isReverse = (reverseNode != null) ? true : false;
			String descString;
			String[] namesString;
			BufferedReader BR = new BufferedReader(new FileReader(file));
			descString = BR.readLine();
			// namesString = descString.trim().split("#\\d\\s");
			// namesString = descString.trim().split("\\d\\s+");
			namesString = descString.trim().split("\\s*#\\d\\s*");
			nl = new ArrayList<String>();
			for (int i = 0; i < namesString.length; i++) {
				if (namesString[i].length() != 0) {
					nl.add(namesString[i].trim());
				}

			}

			if (beginRowNode != null) {
				al = readAndSplitTextFileToArrayList(file, Integer
						.parseInt(beginRowNode.getTextContent().trim()));
			} else {
				al = readAndSplitTextFileToArrayList(file, 1);
			}
			// System.cl

			// nl = getColArrayListForInFileNodeByName(inFileNode);

			hm = convertArrayListOfStringToHashMapOfDoubleArrays(al, nl,
					isReverse);
			// writeArrayListOfStringToFile(nl, file.getAbsolutePath()+".dsc" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (Debug.USE_CASHE) {
			/*
			 * if (CasheOfHmap == null) { CasheOfHmap = new HashMap<String,
			 * HashMap<String, Double[]>>(); }
			 */
			CasheOfHmap.put(file.getName(), hm);
		}
		return hm;
	};

	public ArrayList<String> getColArrayListForInFileNodeByName(Node inFileNode) {
		ArrayList<String> nl = null;
		try {
			// Node inFileNode = XPathAPI.selectSingleNode(mdNode,
			// ".//inFile[normalize-space(./name)=\""+fName.trim()+"\"]");
			NodeList cn = XPathAPI.selectNodeList(inFileNode, ".//col/name");
			nl = new ArrayList<String>();
			for (int i = 0; i <= cn.getLength() - 1; i++) {
				nl.add(cn.item(i).getTextContent().trim());
			}
			;
		} catch (TransformerException te) {
			nl = null;
			te.printStackTrace();
		}
		return nl;
	}

	public ArrayList<String> getColArrayListForOutFileNodeByName(Node inFileNode) {
		ArrayList<String> nl = null;
		try {
			// Node inFileNode = XPathAPI.selectSingleNode(mdNode,
			// ".//outFile[normalize-space(./name)=\""+fName.trim()+"\"]");
			NodeList cn = XPathAPI.selectNodeList(inFileNode, ".//col/name");
			nl = new ArrayList<String>();
			for (int i = 0; i <= cn.getLength() - 1; i++) {
				nl.add(cn.item(i).getTextContent().trim());
			}
			;
		} catch (TransformerException te) {
			nl = null;
			te.printStackTrace();
		}
		return nl;
	}

	public Double getParDoubleByText(String parName) throws Exception {
		String xpath = ".//par[@name=\"" + parName.trim() + "\"]";
		String parText;
		try {
			parText = getParText(xpath);
		} catch (Exception e) {
			xpath = "//par[@name=\"" + parName.trim() + "\"]";
			parText = getParText(xpath);
		}

		Double parDouble = Double.valueOf(parText);
		return parDouble;
	}
	
	public int getParIntegerByText(String parName) throws Exception {
		String xpath = ".//par[@name=\"" + parName.trim() + "\"]";
		String parText;
		try {
			parText = getParText(xpath);
		} catch (Exception e) {
			xpath = "//par[@name=\"" + parName.trim() + "\"]";
			parText = getParText(xpath);
		}

		int parInt = Integer.valueOf(parText);
		return parInt;
	}
	public int getParIntByText(String parName) throws Exception {
		String xpath = ".//par[@name=\"" + parName.trim() + "\"]";
		String parText;
		try {
			parText = getParText(xpath);
		} catch (Exception e) {
			xpath = "//par[@name=\"" + parName.trim() + "\"]";
			parText = getParText(xpath);
		}

		int parInt = Integer.valueOf(parText);
		return parInt;
	}

	public Boolean getParBooleanByText(String parName) throws Exception {
		String xpath = ".//par[@name=\"" + parName.trim() + "\"]";
		String parText;
		try {
			parText = getParText(xpath);
		} catch (Exception e) {
			xpath = "//par[@name=\"" + parName.trim() + "\"]";
			parText = getParText(xpath);
		}

		Boolean parBoolean = Boolean.valueOf(parText);
		return parBoolean;
	}

	public String getParStringByText(String parName) throws Exception {
		String xpath = ".//par[@name=\"" + parName.trim() + "\"]";
		String parText;
		try {
			parText = getParText(xpath);
		} catch (Exception e) {
			xpath = "//par[@name=\"" + parName.trim() + "\"]";
			parText = getParText(xpath);
		}

		// Double parDouble = Double.valueOf(parText);
		return parText;
	}

	public Object getParObjectByText(String parName, String parType)
			throws Exception {
		String xpath = ".//" + parType.trim() + "[@name=\"" + parName.trim()
				+ "\"]";
		String parText;
		try {
			parText = getParText(xpath);
		} catch (Exception e) {
			xpath = "//" + parType.trim() + "[@name=\"" + parName.trim()
					+ "\"]";
			parText = getParText(xpath);
		}

		// Double parDouble = Double.valueOf(parText);
		return parText;
	}

	/**
	 * Считывает из параметрического XML массив параметров типа parType с
	 * одинаковым именем parName. Возвращает этот массив значений в виде
	 * Object[].
	 * 
	 * @param parName
	 *            имя парметра, например для &lt;par name="someName"&gt; это
	 *            будет "someName"
	 * @param parType
	 *            тип параметра, например для &lt;par name="someName"&gt; это
	 *            будет "par"
	 * @return массив объектов Object[] значений параметров XML, в вызывающем
	 *         коде обычно приводится к String[]
	 */
	public Object[] getParObjectArrayByText(String parName, String parType)
			throws Exception {
		String xpath = ".//" + parType.trim() + "[@name=\"" + parName.trim()
				+ "\"]";
		String[] parText;
		try {
			parText = getParTextArray(xpath);
		} catch (Exception e) {
			xpath = "//" + parType.trim() + "[@name=\"" + parName.trim()
					+ "\"]";
			parText = getParTextArray(xpath);
		}

		// Double parDouble = Double.valueOf(parText);
		return parText;
	}

	public Double setParDoubleByText(String parName, Double parDouble)
			throws Exception {
		String xpath = ".//par[@name=\"" + parName.trim() + "\"]";
		try {
			setParText(xpath, parDouble.toString());
		} catch (Exception e) {
			xpath = "//par[@name=\"" + parName.trim() + "\"]";
			setParText(xpath, parDouble.toString());
		}
		return parDouble;
	}
	


	public String setParStringByText(String parName, String parString)
			throws Exception {
		String xpath = ".//par[@name=\"" + parName.trim() + "\"]";
		try {
			setParText(xpath, parString);
		} catch (Exception e) {
			xpath = "//par[@name=\"" + parName.trim() + "\"]";
			setParText(xpath, parString);
		}
		return parString;
	}

	public String getParText(String parName) throws Exception {
		String xpath = parName.trim();
		NodeList nl;
		String parText = null;
		nl = null;
		try {
			nl = XPathAPI.selectNodeList(mdNode, xpath);
		} catch (javax.xml.transform.TransformerException e) {
			e.printStackTrace();
		}

		switch (nl.getLength()) {
		case 1:
			parText = nl.item(0).getTextContent().trim();
			break;
		case 0:
			parText = null;
			throw (new Exception("No such parameter name in DOM tree: "
					+ parName));
			// break;
		default:
			parText = null;
			throw (new Exception("Ambiguous parameters name: " + parName));
		}
		parText = replacedPropertyString(parText);
		return parText;
	}

	public String[] getParTextArray(String parName) throws Exception {
		String xpath = parName.trim();
		NodeList nl;
		String[] parText = null;
		nl = null;
		try {
			nl = XPathAPI.selectNodeList(mdNode, xpath);
		} catch (javax.xml.transform.TransformerException e) {
			e.printStackTrace();
		}

		switch (nl.getLength()) {
		case 0:
			parText = null;
			throw (new Exception("No such parameter name in DOM tree: "
					+ parName));
			// break;
		default:
			parText = new String[nl.getLength()];
			for (int i = 0; i <= nl.getLength() - 1; i++) {
				parText[i] = nl.item(i).getTextContent().trim();
			}
		}

		return parText;
	}

	public String setParText(String parName, String parText) throws Exception {
		String xpath = parName.trim();
		NodeList parList, textList = null;
		// String parText = null;
		parList = null;
		try {
			parList = XPathAPI.selectNodeList(mdNode, xpath);
		} catch (javax.xml.transform.TransformerException e) {
			e.printStackTrace();
		}

		switch (parList.getLength()) {
		case 1:
			xpath = ".//text()";
			try {
				textList = XPathAPI.selectNodeList(parList.item(0), xpath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			switch (textList.getLength()) {
			case 1:
				textList.item(0).setNodeValue(parText);
				break;
			case 0:
				Document doc = parList.item(0).getOwnerDocument();// DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Node tNode = doc.createTextNode(parText);
				parList.item(0).appendChild(tNode);
				break;
			default:
				throw (new Exception("Multiple text subnode in par node: "
						+ parName));
			}
			break;
		default:
			throw (new Exception("No such parameters name: " + parName));
			// parText = null;
		}

		return parText;
	}

	public void writeArrayListOfStringToFile(ArrayList<String> al,
			BufferedWriter fil) {
		try {
			// BufferedWriter fil = new BufferedWriter(new FileWriter(fname));
			String st = "";
			for (int i = 0; i <= al.size() - 1; i++) {
				st = st + "#" + i + " " + al.get(i) + " ";
			}
			fil.write(st + "\n");
			fil.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeNodeToFile(Node doc, String fname) {
		DOMSource source = new DOMSource(doc);
		File prFile = new File(fname);
		StreamResult result = new StreamResult(prFile);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = tFactory.newTransformer();
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public void writeArrToTextFile(Double[] Arr, String fname) {
		try {
			BufferedWriter fil = new BufferedWriter(new FileWriter(fname));
			for (int i = 0; i <= Arr.length - 1; i++) {
				fil.write(String.format("%8.3f", Arr[i]) + "\r");
			}
			fil.flush();
			fil.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeArrToTextFile(Double[] Arr, String fname,
			String formatString) {
		try {
			BufferedWriter fil = new BufferedWriter(new FileWriter(fname));
			for (int i = 0; i <= Arr.length - 1; i++) {
				fil.write(String.format(formatString, Arr[i]) + "\r\n");
			}
			fil.flush();
			fil.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeMapDoubleToTextFile(HashMap map, String fname,
			String formatString) {
		Double[] arr = null;
		try {
			BufferedWriter fil = new BufferedWriter(new FileWriter(fname));

			/*
			 * for(int i=0; i<=Arr.length-1; i++){
			 * fil.write(String.format(formatString , Arr[i])+"\r\n"); }
			 */
			arr = (Double[]) map.get(new Integer(0));
			for (int j = 0; j <= arr.length - 1; j++) {
				String wrStr = "";
				for (Integer i = new Integer(0); i <= map.size() - 1; i++) {
					arr = (Double[]) map.get(i);
					wrStr = wrStr + "\t" + String.format(formatString, arr[j]);
				}
				;
				wrStr = wrStr.trim() + "\r\n";
				fil.write(wrStr);
			}
			fil.flush();
			fil.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Записывает все массивы Double из заданного вектора vect в текстовый файл
	 * fname, каждый массив в столбец.<br>
	 * Текущим каталогом является рабочий каталог wrkPath для данного Module.
	 * <br>
	 * Имена столбцов в файл fname не записываются, но сохраняются отдельно в
	 * файле с аналогичным именем fname + расширение ".dsc". <br>
	 * Имена столбцов должны быть прописаны в параметрическом XML в теге
	 * &lt;outFile&gt; Столбцы нумеруются в том порядке в каком они были
	 * размещены в vect.
	 * 
	 * @param vect
	 *            вектор массивов Double, которые должны быть записаны в текст
	 *            fname
	 * @param fname
	 *            имя текстового файла в который производится запись
	 * @param formatString
	 *            строка форматирования для значений Double записыв. в файл
	 *            fname
	 * @return void
	 */
	public void writeVectorDoubleToTextFile(Vector<Double[]> vect,
			String fname, String formatString) {

		writeVectorDoubleToTextFile(vect, wrkPath, fname, formatString);

	}

	public void writeVectorDoubleToTextFileWDesc(Vector<Double[]> vect,
			String fname, String formatString) {

		writeVectorDoubleToTextFileWDesc(vect, wrkPath, fname, formatString);

	}

	/**
	 * Записывает все массивы Double из заданного вектора vect в текстовый файл
	 * fname, каждый массив в столбец.<br>
	 * Текущим каталогом является рабочий каталог wrkPath для данного Module.
	 * <br>
	 * Имена столбцов в файл fname не записываются, но сохраняются отдельно в
	 * файле с аналогичным именем fname + расширение ".dsc". <br>
	 * Имена столбцов должны быть прописаны в параметрическом XML в теге
	 * &lt;outFile&gt; Столбцы нумеруются в том порядке в каком они были
	 * размещены в vect.
	 * 
	 * @param vect
	 *            вектор массивов Double, которые должны быть записаны в текст
	 *            fname
	 * @param fname
	 *            имя текстового файла в который производится запись
	 * @param formatString
	 *            строка форматирования для значений Double записыв. в файл
	 *            fname
	 * @return void
	 */

	public void writeVectorDoubleToTextFile(Vector<Double[]> vect, String path,
			String fname, String formatString) {
		Double[] arr = null;
		ArrayList<String> nl = null;
		try {
			String rFname = getOutFile(fname);
			FileWriter  fw = new FileWriter(path + "/" + rFname);
			//fw.sy
			BufferedWriter fil = new BufferedWriter(fw);
			Node inFileNode = XPathAPI.selectSingleNode(
					mdNode,
					".//outFile/name[normalize-space(./text())=\""
							+ fname.trim() + "\"]").getParentNode();
			nl = getColArrayListForOutFileNodeByName(inFileNode);

			arr = (Double[]) vect.get(0);
			String str = "";
			for (int j = 0; j <= arr.length - 1; j++) {
				String wrStr = "";
				str = "";
				for (int i = 0; i <= vect.size() - 1; i++) {
					arr = vect.get(i);
					wrStr = wrStr + "\t" + String.format(formatString, arr[j]);
				}
				;

				wrStr = wrStr.trim() + "\r\n";
				fil.write(wrStr);
			}

			for (int i = 0; i <= vect.size() - 1; i++) {
				try {
					nl.get(i);
				} catch (IndexOutOfBoundsException e) {
					nl.add("unknown" + i);
				}
			}
			// writeArrayListOfStringToFile(nl, (new
			// File(path+"/"+rFname)).getAbsolutePath()+".dsc");
			writeArrayListOfStringToFile(nl, new BufferedWriter(new FileWriter(
					path + "/" + rFname + ".dsc")));

			fil.flush();
			fil.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public void writeVectorDoubleToTextFileWDesc(Vector<Double[]> vect,
			String path, String fname, String formatString) {
		Double[] arr = null;
		ArrayList<String> nl = null;
		try {
			String rFname = getOutFile(fname);
			BufferedWriter fil = new BufferedWriter(new FileWriter(path + "/"
					+ rFname));
			Node inFileNode = XPathAPI.selectSingleNode(
					mdNode,
					".//outFile/name[normalize-space(./text())=\""
							+ fname.trim() + "\"]").getParentNode();
			nl = getColArrayListForOutFileNodeByName(inFileNode);
			for (int i = 0; i <= vect.size() - 1; i++) {
				try {
					nl.get(i);
				} catch (IndexOutOfBoundsException e) {
					nl.add("unknown" + i);
				}
			}

			writeArrayListOfStringToFile(nl, new BufferedWriter(new FileWriter(
					path + "/" + rFname + ".dsc")));
			writeArrayListOfStringToFile(nl, fil);

			arr = (Double[]) vect.get(0);
			String str = "";
			int ub = arr.length;
			for (int j = 0; j <= ub - 1; j++) {
				String wrStr = "";
				str = "";
				for (int i = 0; i <= vect.size() - 1; i++) {
					arr = vect.get(i);
					wrStr = wrStr + "\t" + String.format(formatString, arr[j]);
				}
				;

				wrStr = wrStr.trim() + "\r\n";
				fil.append(wrStr);
				// fil.
			}

			fil.flush();
			fil.flush();
			fil.close();
			fil = null;

			if (Debug.USE_CASHE) {
				HashMap<String, Double[]> hm = new HashMap<String, Double[]>();
				// if (CasheOfHmap == null) {
				// CasheOfHmap = new HashMap<String, HashMap<String,
				// Double[]>>();
				// }
				for (int i = 0; i <= vect.size() - 1; i++) {
					arr = vect.get(i);
					hm.put(nl.get(i), arr);
				}
				;
				CasheOfHmap.put((path + "/" + rFname).trim().toLowerCase(), hm);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Записывает все массивы Double из заданного вектора vect в текстовый файл
	 * fname, каждый массив в столбец. Текущим каталогом является каталог path.
	 * Имена столбцов указываются в строке DescString и записываются в первую
	 * строку файла fname.
	 * 
	 * @param vect
	 *            вектор массивов Double, которые должны быть записаны в текст
	 *            fname
	 * @param path
	 *            каталог для записи fname
	 * @param fname
	 *            имя текстового файла в который производится запись
	 * @param formatString
	 *            строка форматирования для значений Double записыв. в файл
	 *            fname
	 * @param DescString
	 *            строка дескриптора, помещаемая в начало файла
	 * @return void
	 */

	public void writeVectorDoubleToTextFileWDescString(Vector<Double[]> vect,
			String path, String fname, String formatString, String DescString) {
		Double[] arr = null;
		// ArrayList<String> nl = null;
		try {
			String rFname = getOutFile(fname);
			BufferedWriter fil = new BufferedWriter(new FileWriter(path + "/"
					+ rFname));
			// Node inFileNode = XPathAPI.selectSingleNode(mdNode,
			// ".//outFile/name[normalize-space(./text())=\"" + fname.trim()
			// +"\"]").getParentNode();
			// nl = getColArrayListForOutFileNodeByName(inFileNode);
			// for(int i = 0; i<= vect.size()-1; i++){
			// try {
			// nl.get(i);
			// } catch (IndexOutOfBoundsException e){
			// nl.add("unknown" + i);
			// }
			// }
			//	
			// writeArrayListOfStringToFile(nl, new BufferedWriter(new
			// FileWriter(path+"/"+rFname+".dsc")));
			// writeArrayListOfStringToFile(nl, fil);

			fil.write(DescString + "\r\n");
			arr = (Double[]) vect.get(0);
			String str = "";
			int ub = arr.length;
			for (int j = 0; j <= ub - 1; j++) {
				String wrStr = "";
				str = "";
				for (int i = 0; i <= vect.size() - 1; i++) {
					arr = vect.get(i);
					wrStr = wrStr + "\t" + String.format(formatString, arr[j]);
				}
				;

				wrStr = wrStr.trim() + "\r\n";
				fil.append(wrStr);
				// fil.
			}

			fil.flush();
			fil.close();

			// if (Debug.USE_CASHE) {
			// HashMap<String, Double[]> hm = new HashMap<String, Double[]>();
			// //if (CasheOfHmap == null) {
			// //CasheOfHmap = new HashMap<String, HashMap<String, Double[]>>();
			// //}
			// for(int i = 0; i<= vect.size()-1; i++){
			// arr = vect.get(i);
			// hm.put(nl.get(i),arr);
			// };
			// CasheOfHmap.put((path+"/"+rFname).trim().toLowerCase(),hm);
			// }

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeVectorDoubleToTextFile(Vector<Double[]> vect, int initKey,
			int finalKey, String fname, String formatString) {
		Double[] arr = null;
		Vector<Double[]> newVect = new Vector<Double[]>();
		// try {
		// BufferedWriter fil = new BufferedWriter(new FileWriter(fname));

		/*
		 * for(int i=0; i<=Arr.length-1; i++){
		 * fil.write(String.format(formatString , Arr[i])+"\r\n"); }
		 */
		arr = (Double[]) vect.get(0);
		// for (int j=0; j<=arr.length-1; j++) {
		// String wrStr = "";
		for (int i = initKey; i <= finalKey; i++) {
			arr = vect.get(i);
			newVect.add(arr);
			// wrStr = wrStr + "\t" + String.format(formatString , arr[j]);
		}
		;

		writeVectorDoubleToTextFile(newVect, wrkPath, fname, formatString);

		// wrStr = wrStr.trim() + "\r\n";
		// fil.write(wrStr);
		// }
		// fil.flush();
		// fil.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	public void writeMapDoubleToTextFile(HashMap map, int initKey,
			int finalKey, String fname, String formatString) {
		Double[] arr = null;
		try {
			BufferedWriter fil = new BufferedWriter(new FileWriter(fname));

			/*
			 * for(int i=0; i<=Arr.length-1; i++){
			 * fil.write(String.format(formatString , Arr[i])+"\r\n"); }
			 */
			arr = (Double[]) map.get(new Integer(0));
			for (int j = 0; j <= arr.length - 1; j++) {
				String wrStr = "";
				for (Integer i = initKey; i <= finalKey; i++) {
					arr = (Double[]) map.get(i);
					wrStr = wrStr + "\t" + String.format(formatString, arr[j]);
				}
				;
				wrStr = wrStr + "\r\n";
				fil.write(wrStr);
			}
			fil.flush();
			fil.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static String copyFile(String src, String dst) throws IOException {
		try {
			// Create channel on the source
			FileChannel srcChannel = new FileInputStream(src).getChannel();

			// Create channel on the destination
			FileChannel dstChannel = new FileOutputStream(dst).getChannel();

			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

			// Close the channels
			srcChannel.close();
			// dstChannel.
			dstChannel.close();
		} catch (IOException e) {
			// e.fillInStackTrace();
			// e.printStackTrace();
			throw e;
		}
		;
		return (new File(dst)).getAbsolutePath();

	}

	public static long convertJulianToGregorianCalendar(long jd) {
		return convertJulianToGregorianCalendar((new Double(jd)).doubleValue());
	}

	/**
	 * Конвертирует время из формата juliantime - астрономический формат времени
	 * в грегорианское время
	 * 
	 * @param jd
	 *            juliantime в днях
	 * @return возвращает миллисекунды эпохи UNIX
	 */
	public static long convertJulianToGregorianCalendar(double jd) {

		{
			// // Let J be the Julian day number from which we want to compute
			// the
			// // date components.
			// // double jd = Double.parseDouble(args[0]);
			// int J = new Double(jd).intValue(); //Integer.parseInt(args[0]);
			// // With J, compute a relative Julian day number j from a
			// Gregorian
			// // epoch starting on March 1 ?4800 (i.e. March 1 4801 BC in the
			// // proleptic Gregorian Calendar), the beginning of the Gregorian
			// // quadricentennial 32,044 days before the epoch of the Julian
			// // Period.
			// int j = J + 32044;
			// // With j, compute the number g of Gregorian quadricentennial
			// cycles
			// // elapsed (there are exactly 146,097 days per cycle) since the
			// // epoch; subtract the days for this number of cycles, it leaves
			// dg
			// // days since the beginning of the current cycle.
			// int g = j / 146097;
			//
			// int dg = j % 146097;
			// // With dg, compute the number c (from 0 to 4) of Gregorian
			// // centennial cycles (there are exactly 36,524 days per Gregorian
			// // centennial cycle) elapsed since the beginning of the current
			// // Gregorian quadricentennial cycle, number reduced to a maximum
			// of
			// // 3 (this reduction occurs for the last day of a leap centennial
			// // year where c would be 4 if it were not reduced); subtract the
			// // number of days for this number of Gregorian centennial cycles,
			// it
			// // leaves dc days since the beginning of a Gregorian century.
			// int c = (dg / 36524 + 1) * 3 / 4;
			//
			// int dc = dg - c * 36524;
			// // With dc, compute the number b (from 0 to 24) of Julian
			// // quadrennial cycles (there are exactly 1,461 days in 4 years,
			// // except for the last cycle which may be incomplete by 1 day)
			// since
			// // the beginning of the Gregorian century; subtract the number of
			// // days for this number of Julian cycles, it leaves db days in
			// the
			// // Gregorian century.
			// int b = dc / 1461;
			// int db = dc % 1461;
			// // With db, compute the number a (from 0 to 4) of Roman annual
			// // cycles (there are exactly 365 days per Roman annual cycle)
			// since
			// // the beginning of the Julian quadrennial cycle, number reduced
			// to
			// // a maximum of 3 (this reduction occurs for the leap day, if
			// any,
			// // where a would be 4 if it was not reduced); subtract the number
			// of
			// // days for this number of annual cycles, it leaves da days in
			// the
			// // Julian year (that begins on March 1).
			// int a = (db / 365 + 1) * 3 / 4;
			// int da = db - a * 365;
			// // Convert the four components g, c, b, a into the number y of
			// years
			// // since the epoch, by summing their values weighted by the
			// number
			// // of years that each component represents (respectively 400
			// years,
			// // 100 years, 4 years, and 1 year).
			// int y = g * 400 + c * 100 + b * 4 + a;
			// // With da, compute the number m (from 0 to 11) of months since
			// // March (there are exactly 153 days per 5-month cycle, however
			// // these 5-month cycles are offset by 2 months within the year,
			// i.e.
			// // the cycles start in May, and so the year starts with an
			// initial
			// // fixed number of days on March 1, the month can be computed
			// from
			// // this cycle by a Euclidian division by 5); subtract the number
			// of
			// // days for this number of months (using the formula above), it
			// // leaves d days past since the beginning of the month.
			// int m = (da * 5 + 308) / 153 - 2;
			// int d = da - (m + 4) * 153 / 5 + 122;
			// // You can then deduce the Gregorian date (Y, M, D) by simple
			// shifts
			// // from (y, m, d)
			// int Y = y - 4800 + (m + 2) / 12;
			// int M = (m + 2) % 12; // int M = (m + 2) % 12 + 1; in Java
			// January has number 0
			// int D = d + 1;
			// Double fd = jd - J;
			// Double hrs = 24 * fd;
			// Double min = 60 * (hrs - Math.floor(hrs));
			// Double sec = 60 * (min - Math.floor(min));
			// Double ms = 1000 * (sec - Math.floor(sec));
			// System.out.println("Year: " + Y + " Month: " + M + " Day: " + D);
			// System.out.println("Hours: " + Math.floor(hrs) + " Min: " +
			// Math.floor(min) + " Sec: " + Math.floor(sec) + " ms: " + ms);
			// TimeZone tz = TimeZone.getTimeZone("GMT");
			// TimeZone.setDefault(tz);
			// GregorianCalendar GC = new GregorianCalendar(Y, M, D,
			// hrs.intValue(), min.intValue(), sec.intValue());
			// //System.out.println(GC.getTime());
			// //System.out.println(GC.getTimeInMillis()+ms);
			// Date dat = new Date(GC.getTimeInMillis()+ms.longValue());
			// System.out.println(dat);
			double t = jd - jt1970;
			double mss = t * 24 * 3600 * 1000;
			mss = Math.round(mss);
			return new Double(mss).longValue();
		}

	}

	static double jt1970 = 2440587.5; // время jultime для даты 1.01.1970 

	/**
	 * Конвертирует время из грегорианского календаря в jultime
	 * 
	 * @param year
	 *            год
	 * @param month
	 *            месяц
	 * @param dd
	 *            день
	 * @param HH
	 *            час
	 * @param mm
	 *            минута
	 * @param ss
	 *            секунда
	 * @return время jultime
	 */
	public static double ConvertGregorianToJulianCalendar(int year, int month,
			int dd, int HH, int mm, int ss) {
		// if (args.length < 6) {
		// System.out.println("Usage: GJ <YYYY MM dd HH mm ss>");
		// } else {
		// year = new Integer(args[0]);
		// month = new Integer(args[1]);
		// dd = new Integer(args[2]);
		// HH = new Integer(args[3]);
		// mm = new Integer(args[4]);
		// ss = new Integer(args[5]);
		TimeZone dz = TimeZone.getDefault();
		TimeZone tz = TimeZone.getTimeZone("GMT");
		TimeZone.setDefault(tz);
		GregorianCalendar gc = new GregorianCalendar(year, month - 1, dd, HH,
				mm, ss);
		long time = gc.getTimeInMillis();
		double cd = time / (24. * 3600. * 1000.);
		TimeZone.setDefault(dz);
		// System.out.println("Corresponding juliantime: "+(cd+jt1970+1));

		return cd + jt1970;

	}

	public static double ConvertGregorianToJulianCalendar(long timeInMillis) {
		// if (args.length < 6) {
		// System.out.println("Usage: GJ <YYYY MM dd HH mm ss>");
		// } else {
		// year = new Integer(args[0]);
		// month = new Integer(args[1]);
		// dd = new Integer(args[2]);
		// HH = new Integer(args[3]);
		// mm = new Integer(args[4]);
		// ss = new Integer(args[5]);

		double cd = timeInMillis / (24. * 3600. * 1000.);
		// System.out.println("Corresponding juliantime: "+(cd+jt1970+1));

		return cd + jt1970;

	}
}

class FilenameFilterContain implements FilenameFilter {
	private String strPref;

	public FilenameFilterContain(String prefix) {
		strPref = prefix;
	}

	public boolean accept(File dir, String name) {
		return name.contains(strPref);
	}
};

class FilenameFilterSW implements FilenameFilter {
	private String strPref;

	public FilenameFilterSW(String prefix) {
		strPref = prefix;
	}

	public boolean accept(File dir, String name) {
		return name.startsWith(strPref);
	}
};

class FilenameFilterEq implements FilenameFilter {
	private String strEq;

	public FilenameFilterEq(String fname) {
		strEq = fname;
	}

	public boolean accept(File dir, String name) {
		return name.equalsIgnoreCase(strEq);
	}
};

class FilenameFilterMatcher implements FilenameFilter {
	private String strMatch;

	public FilenameFilterMatcher(String fname) {
		strMatch = fname;
	}

	public boolean accept(File dir, String name) {
		return Pattern.matches(strMatch, name);
	}
};

class RETokenizer implements Iterator {
	// Holds the original input to search for tokens
	private CharSequence input;

	// Used to find tokens
	private Matcher matcher;

	// If true, the String between tokens are returned
	private boolean returnDelims;

	// The current delimiter value. If non-null, should be returned
	// at the next call to next()
	private String delim;

	// The current matched value. If non-null and delim=null,
	// should be returned at the next call to next()
	private String match;

	// The value of matcher.end() from the last successful match.
	private int lastEnd = 0;

	// patternStr is a regular expression pattern that identifies tokens.
	// If returnDelims delim is false, only those tokens that match the
	// pattern are returned. If returnDelims true, the text between
	// matching tokens are also returned. If returnDelims is true, the
	// tokens are returned in the following sequence - delimiter, token,
	// delimiter, token, etc. Tokens can never be empty but delimiters might
	// be empty (empty string).
	public RETokenizer(CharSequence input, String patternStr,
			boolean returnDelims) {
		// Save values
		this.input = input;
		this.returnDelims = returnDelims;

		// Compile pattern and prepare input
		Pattern pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(input);
	}

	// Returns true if there are more tokens or delimiters.
	public boolean hasNext() {
		if (matcher == null) {
			return false;
		}
		if (delim != null || match != null) {
			return true;
		}
		if (matcher.find()) {
			if (returnDelims) {
				delim = input.subSequence(lastEnd, matcher.start()).toString();
			}
			match = matcher.group();
			lastEnd = matcher.end();
		} else if (returnDelims && lastEnd < input.length()) {
			delim = input.subSequence(lastEnd, input.length()).toString();
			lastEnd = input.length();

			// Need to remove the matcher since it appears to automatically
			// reset itself once it reaches the end.
			matcher = null;
		}
		return delim != null || match != null;
	}

	// Returns the next token (or delimiter if returnDelims is true).
	public Object next() {
		String result = null;

		if (delim != null) {
			result = delim;
			delim = null;
		} else if (match != null) {
			result = match;
			match = null;
		}
		return result;
	}

	// Returns true if the call to next() will return a token rather
	// than a delimiter.
	public boolean isNextToken() {
		return delim == null && match != null;
	}

	// Not supported.
	public void remove() {
		throw new UnsupportedOperationException();
	}
}