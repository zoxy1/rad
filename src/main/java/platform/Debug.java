package platform;


public class Debug {
	static String DS = "";
	public static boolean STOP_ON_ERROR_FLAG = false;
	public static boolean RUN_GUI_WITH_XML = false;
	public static boolean USE_CASHE = true;
	public static boolean RUN_DIR_FLAG = false;
	public static boolean STAY_CHART_FLAG = false;
	public static boolean SPLIT_FILE_FLAG = false;
	public static boolean SPLIT_AND_RUN_FLAG = false;
	public static boolean BROWSER_FLAG = false;
	public static String WORKER_NAME = "worker";
	public static String TMP_DIR = "tmp";
	public static String ITER_XML = "";
	public static boolean NO_TIME = false;
	//public static String I
	
	
	static public void setDebugNode(String ds){
		DS = ds;
	}
	static public void print(Object ob){
		if (DS.equals("-d")) {
			System.out.print(ob);
		}
	}
	static public void print(int ob){
		if (DS.equals("-d")) {
			System.out.print(ob);
		}
	}
	static public void print(){
		if (DS.equals("-d")) {
			//System.out.print();
		}
	}
	static public void println(Object ob){
		if (DS.equals("-d")) {
			print(ob);
			System.out.println();
		}
		
		
	}
	static public void println(){
		if (DS.equals("-d")) {
			print();
			System.out.println();
		}
		
		
	}
	
	static public void println(int ob){
		if (DS.equals("-d")) {
			print(ob);
			System.out.println();
		}
	}
	
	static public String getCmdKeyString (){
		String cmdKeys = " ";
		if (STOP_ON_ERROR_FLAG){
			cmdKeys = cmdKeys.concat("-stop ");
		}
		if (Debug.BROWSER_FLAG){
			cmdKeys = cmdKeys.concat("-b ");
		}
		if (!Debug.ITER_XML.equals("")&&Debug.ITER_XML!=null){
			cmdKeys = cmdKeys.concat("-iter_xml="+Debug.ITER_XML.trim()+" ");
		}
		if (Debug.RUN_DIR_FLAG){
			cmdKeys = cmdKeys.concat("-rd ");
		}
		if (Debug.RUN_GUI_WITH_XML){
			cmdKeys = cmdKeys.concat("-gui ");
		}

		if (Debug.SPLIT_AND_RUN_FLAG){
			cmdKeys = cmdKeys.concat("-sr ");
		}

		if (Debug.SPLIT_FILE_FLAG){
			cmdKeys = cmdKeys.concat("-split ");
		}

		if (Debug.STAY_CHART_FLAG){
			cmdKeys = cmdKeys.concat("-sc ");
		}

		if (!Debug.TMP_DIR.equals("")&&Debug.TMP_DIR!=null){
			cmdKeys = cmdKeys.concat("-tmp_dir=" + Debug.TMP_DIR.trim()+" ");
		}

		if (!Debug.USE_CASHE){
			cmdKeys = cmdKeys.concat("-nocash ");
		}

		if (!Debug.WORKER_NAME.equals("")&&Debug.WORKER_NAME!=null){
			cmdKeys = cmdKeys.concat("-wrk_name= "+Debug.WORKER_NAME.trim()+" ");
		}

		
		return cmdKeys;
	}
}
