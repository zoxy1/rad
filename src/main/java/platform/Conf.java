package platform;



import java.io.BufferedReader;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
//import java.util.MissingResourceException;
import java.util.Properties;
//import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;





public class Conf {
	public static final String debug_mode_flag = "debug.mode.flag";

	public static final String error_stop_flag = "error.stop.flag";

	public static final String add_time_flag = "add.time.flag";

	public static final String run_gui_flag = "run.gui.flag";

	public static final String use_cash_flag = "use.cash.flag";

	public static final String stay_chart_flag = "stay.chart.flag";

	public static final String browser_run_flag = "browser.run.flag";

	public static final String split_dir_path = "split.dir.path";

	public static final String run_par_dir_path = "run.par.dir.path";

	public static final String run_par_file_path = "run.par.file.path";

	public static final String run_iter_file_path = "run.iter.file.path";

	public static final String data_dir_path = "data.dir.path";

	public static final String worker_dir_name = "worker.dir.name";

	public static final String tmp_dir_name = "tmp.dir.name";

	public static final String parent_model_path = "parent.model.path";

	public static final String model_cira86_name = "model.cira86.name";

	public static final String model_seebor_name = "model.seebor.name";

	public static final String model_afgl_name = "model.afgl.name";

	public static final String app_home_path = "app.home.path";

	public static final String data_file_path = "data.file.path";

	public static final String gnuplot_bin_path = "gnuplot.bin.path";
	
	public static final String split_file_path = "split.file.path";

	private static final String PROP_NAME = "conf"; 

	private static final Properties PROP = new Properties();

	private static final int MAX_CYCLE_NUM = 3;

	private static int cur_cycle_num = 0;

	static Logger confLogger = Logger.getLogger(Conf.class);

	public Conf() throws IOException {
		
		
		LoadProp(new InputStreamReader(Conf.class.getResourceAsStream("/rad/all.properties"))) ;
		
		LoadProp(new FileReader(System.getProperty("user.dir")+"/"+"rad.properties"));
		
	}

	public Conf(String pathToConfFile) throws IOException {
		
		confLogger.debug(PROP.toString());
		
		LoadProp(new InputStreamReader(Conf.class.getResourceAsStream("/rad/all.properties"))) ;
		
		LoadProp(new FileReader(pathToConfFile));

		confLogger.debug(new File(pathToConfFile).getCanonicalPath());
		
	}
	
	public void LoadProp(Reader rr) throws IOException{
		BufferedReader BR = new BufferedReader(rr);
		String line;
		while ((line = BR.readLine())!=null){
			line = line.trim();
			if (!(line.startsWith("#")||line.startsWith("!"))&&line.length()!=0) {
				String[] larr = line.split("=");
				if (larr.length>=2){
					PROP.put(larr[0].trim(), larr[1].trim() );
				} else {
					PROP.put(larr[0].trim(), "" );
				}
			}
		}
	}

	public static String getProperty(String key) {
		String string;
		String newString=null;
		try {
		string = PROP.getProperty(key).trim();
		newString = replacedPropertyString(string);
		for (int i = 1; i<=MAX_CYCLE_NUM; i++){
			newString = replacedPropertyString(newString);
		}
		//return newString.trim();
		} catch (Exception e) {
			
			e.printStackTrace();
			confLogger.fatal("Exception in Conf.getProperty() with key:= " + key);
			confLogger.fatal(e.getMessage());
			
		}
		return newString.trim();
//		try {
//			cur_cycle_num++;
//			if (cur_cycle_num > MAX_CYCLE_NUM) {
//				System.out.println("Recursive replacing exceed MAX_CYCLE_NUM: "
//						+ MAX_CYCLE_NUM);
//				cur_cycle_num = 0;
//				return PROP.getProperty(key).trim();
//			}
//			string = PROP.getProperty(key).trim();
//			newString = replacedPropertyString(string);
//
//			cur_cycle_num = 0;
//			//return newString.trim();
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//			confLogger.fatal("Exception in Conf.isEmpty() with key:= " + key);
//			confLogger.fatal(e.getMessage());
//			// TODO: handle exception
//		}
//		return newString.trim();

	}

	public static Properties getProperties() {
		return PROP;
	}

	public static void setProperty(String key, String value) {
		PROP.setProperty(key, value);
		confLogger.debug(PROP.toString());

	}
	public static String replacedPropertyString(String dirty_name) throws Exception {
		String fname = new String(dirty_name);
		ArrayList<String> keys = new ArrayList<String>();
		String[] props = getAllPropertyStrings(dirty_name, keys);
		if (props != null) {
			for (int i = 0; i < props.length; i++) {
				fname = fname.replace(keys.get(i), props[i]);
			}
		}
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
	
}
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

/*package platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import rad.bin.Runner;

public class Conf {
	public static final String debug_mode_flag = "debug.mode.flag";

	public static final String error_stop_flag = "error.stop.flag";

	public static final String add_time_flag = "add.time.flag";

	public static final String run_gui_flag = "run.gui.flag";

	public static final String use_cash_flag = "use.cash.flag";

	public static final String stay_chart_flag = "stay.chart.flag";

	public static final String browser_run_flag = "browser.run.flag";

	public static final String split_dir_path = "split.dir.path";
	
	public static final String split_file_path = "split.file.path";

	public static final String run_par_dir_path = "run.par.dir.path";

	public static final String run_par_file_path = "run.par.file.path";

	public static final String run_iter_file_path = "run.iter.file.path";

	public static final String data_dir_path = "data.dir.path";

	public static final String worker_dir_name = "worker.dir.name";

	public static final String tmp_dir_name = "tmp.dir.name";

	public static final String parent_model_path = "parent.model.path";

	public static final String model_cira86_name = "model.cira86.name";

	public static final String model_seebor_name = "model.seebor.name";

	public static final String model_afgl_name = "model.afgl.name";

	public static final String app_home_path = "app.home.path";

	public static final String data_file_path = "data.file.path";

	public static final String gnuplot_bin_path = "gnuplot.bin.path";

	private static final String PROP_NAME = "conf"; //$NON-NLS-1$

	private static final Properties PROP = new Properties();

	private static final int MAX_CYCLE_NUM = 5;

	private static int cur_cycle_num = 0;

	static Logger confLogger = Logger.getLogger(Conf.class);

	public Conf() throws IOException {
		// try {
		PROP.load(new FileInputStream(System.getProperty("user.dir")
				+ "/rad/all.properties"));
		// Main2.printConf();
		PROP.load(new FileInputStream("rad.properties"));
		// Main2.printConf();
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }
	}

	public Conf(String pathToConfFile) throws IOException {
		// try {
		confLogger.debug(PROP.toString());
		PROP.load(new FileInputStream(System.getProperty("user.dir")
				+ "/rad/all.properties"));
		PROP.load(new FileInputStream(pathToConfFile));
		confLogger.debug(new File(pathToConfFile).getCanonicalPath());
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }
	}

	public static String getProperty(String key) {
		String string;
		String newString=null;
		try {
			cur_cycle_num++;
			if (cur_cycle_num > MAX_CYCLE_NUM) {
				System.out.println("Recursive replacing exceed MAX_CYCLE_NUM: "
						+ MAX_CYCLE_NUM);
				cur_cycle_num = 0;
				return PROP.getProperty(key).trim();
			}
			string = PROP.getProperty(key).trim();
			newString = Runner.replacedPropertyString(string);

			cur_cycle_num = 0;
			//return newString.trim();
		} catch (Exception e) {
			e.printStackTrace();
			confLogger.fatal("getProperty asked key is: " + key);
			confLogger.fatal(e.getMessage(), e);
			
		}
		return newString.trim();

	}

	public static Properties getProperties() {
		return PROP;
	}

	public static void setProperty(String key, String value) {
		PROP.setProperty(key, value);
		confLogger.debug(PROP.toString());

	}
}
*/