package platform;

//import org.w3c.dom.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.text.*;

//import javaSCI.*;

public abstract class Charger {

	/**
	 * @param args
	 */
	public static String copyFile(String src, String dst) {
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
			e.fillInStackTrace();
			e.printStackTrace();
		}
		;
		return (new File(dst)).getAbsolutePath();

	}



	static String getProperty(String propName) {
		return Conf.getProperty(propName);
	}

	static boolean isTrue(String propName) {
		return Conf.getProperty(propName).trim().equalsIgnoreCase("true");
	}

	static boolean isEmpty(String propName) {
		String prop = Conf.getProperty(propName);
		if (prop != null && prop.trim().length() != 0) {
			return false;
		} else {
			return true;
		}
	}

	public static String makeWorkerDir(String wrkPath, String DateFormat,
			String OutDirName) {
		SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
		Date date = new Date();
		File filOutDir = new File(OutDirName);
		// filOutDir.i
		if (filOutDir.isAbsolute()) {
			if (filOutDir.exists()) {
				System.out
						.println("Path exsit with the same name, trying to delete=> "
								+ filOutDir.getAbsolutePath());
				Splitter.clearDstPath(filOutDir.getAbsolutePath());
			}
			filOutDir.mkdirs();
			return filOutDir.getAbsolutePath();
		} else {
			wrkPath = wrkPath + "/" + OutDirName.trim() + "_"
					+ sdf.format(date).toString();
			File fil = new File(wrkPath);
			if (fil.isAbsolute()) {
				if (fil.exists()) {
					System.out
							.println("Path exsit with the same name, trying to delete=> "
									+ fil.getAbsolutePath());
					Splitter.clearDstPath(fil.getAbsolutePath());
				}
			}
			fil.mkdirs();
			return fil.getAbsolutePath();
		}

		/*
		 * if ((!fil.mkdirs())&&(!fil.exists())) { Debug.println("Can not create
		 * dir: " + fil.getAbsolutePath()); return null; } else
		 * {Debug.println("Dir was created: " + fil.getAbsolutePath()); return
		 * fil.getAbsolutePath(); }
		 */

	}

	/*
	 * public static void chargeAll (org.w3c.dom.Node Nd){
	 * 
	 * NodeList NdList; int nc = 0; //SimpleDateFormat sdf = new
	 * SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSS");
	 * 
	 * 
	 * String wrkPath = Charger.makeWorkerDir("resources/modules/","yyyy"); // =
	 * new String("resources/modules/");
	 * 
	 * if (Nd.getNodeName().equals("#document")) { NdList = Nd.getChildNodes();
	 * Module[] MArr = null; for(int i=0; i<NdList.getLength();i++){ if
	 * (NdList.item(i).getNodeName().equals("worker")){
	 * //SplitModuleNode(NdList.item(i)); //wrkPath = // new
	 * String("resources/modules/"); //wrkPath =
	 * Charger.makeWorkerDir("resources/modules/","yyyy"); try { MArr =
	 * Splitter.Split(NdList.item(i), wrkPath); Debug.println(MArr.length); }
	 * catch(Exception ex){ ex.printStackTrace(); } }; } for(int i=0; i<MArr.length;i++){
	 * Module Modul = MArr[i]; File fil = new
	 * File(MArr[i].ExecPath+"/"+Modul.ExecName); if (fil.exists())
	 * {Debug.println("File exist, All Ok: " + fil.getAbsolutePath());
	 * //copyFile(fil.getAbsolutePath(),wrkPath + "/" + Modul.ExecName);
	 * Stylizer.Stylize("resources/gosatTransform1.xsl",wrkPath + "/"
	 * +Modul.XMLParFileName, wrkPath + "/" + Modul.ParFileName); Runtime RE =
	 * java.lang.Runtime.getRuntime(); Process PR; int c=-1; byte bt[] = new
	 * byte[1024*1024]; File exeFil = new File (wrkPath + "/" + Modul.ExecName);
	 * try { String[] Cm = new String[1]; Cm[0] = fil.getAbsolutePath();
	 * ProcessBuilder pb = new ProcessBuilder(Cm); pb.directory(new
	 * File(wrkPath)); //PR = pb.start(); BufferedInputStream InpStream = new
	 * BufferedInputStream((PR = pb.start()).getErrorStream()); PR.waitFor();
	 * FileOutputStream OutFile = new FileOutputStream((new File(wrkPath + "/" +
	 * Modul.ExecName)).getAbsolutePath()+"_Err.log"); if (PR==null)
	 * Debug.println("Process not created: "+ wrkPath + "/" + Modul.ExecName);
	 * else { while((c=InpStream.read(bt))>0){ OutFile.write(bt,0,c);
	 * //Debug.println("Read " + c); } } } catch (Exception E){
	 * E.printStackTrace(); }
	 *  } else Debug.println("Module File Does Not Exist: " +
	 * fil.getAbsolutePath()); } //new javaSCI.MainJFrame((new File(wrkPath+"/"+
	 * "transmittance.dat")).getAbsolutePath()); }; }
	 * 
	 * public static void chargeAll (org.w3c.dom.Node Nd, String wrkPath){
	 * 
	 * NodeList NdList; int nc = 0; //SimpleDateFormat sdf = new
	 * SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSS");
	 * 
	 * 
	 * //String wrkPath = Charger.makeWorkerDir("resources/modules/","yyyy"); // =
	 * new String("resources/modules/");
	 * 
	 * if (Nd.getNodeName().equals("#document")) { NdList = Nd.getChildNodes();
	 * Module[] MArr = null; for(int i=0; i<NdList.getLength();i++){ if
	 * (NdList.item(i).getNodeName().equals("worker")){
	 * //SplitModuleNode(NdList.item(i)); //wrkPath = // new
	 * String("resources/modules/"); //wrkPath =
	 * Charger.makeWorkerDir("resources/modules/","yyyy"); try { MArr =
	 * Splitter.Split(NdList.item(i), wrkPath); Debug.println(MArr.length); }
	 * catch(Exception ex){ ex.printStackTrace(); } }; } for(int i=0; i<MArr.length;i++){
	 * Module Modul = MArr[i]; File fil = new
	 * File(MArr[i].ExecPath+"/"+Modul.ExecName); if (fil.exists())
	 * {Debug.println("File exist, All Ok: " + fil.getAbsolutePath());
	 * //copyFile(fil.getAbsolutePath(),wrkPath + "/" + Modul.ExecName);
	 * Stylizer.Stylize("resources/gosatTransform1.xsl",wrkPath + "/"
	 * +Modul.XMLParFileName, wrkPath + "/" + Modul.ParFileName); Runtime RE =
	 * java.lang.Runtime.getRuntime(); Process PR; int c=-1; byte bt[] = new
	 * byte[1024*1024]; File exeFil = new File (wrkPath + "/" + Modul.ExecName);
	 * try { String[] Cm = new String[1]; Cm[0] = fil.getAbsolutePath();
	 * ProcessBuilder pb = new ProcessBuilder(Cm); pb.directory(new
	 * File(wrkPath)); //PR = pb.start(); BufferedInputStream InpStream = new
	 * BufferedInputStream((PR = pb.start()).getErrorStream()); PR.waitFor();
	 * FileOutputStream OutFile = new FileOutputStream((new File(wrkPath + "/" +
	 * Modul.ExecName)).getAbsolutePath()+"_Err.log"); if (PR==null)
	 * Debug.println("Process not created: "+ wrkPath + "/" + Modul.ExecName);
	 * else { while((c=InpStream.read(bt))>0){ OutFile.write(bt,0,c);
	 * //Debug.println("Read " + c); } } } catch (Exception E){
	 * E.printStackTrace(); }
	 *  } else Debug.println("Module File Does Not Exist: " +
	 * fil.getAbsolutePath()); } //new javaSCI.MainJFrame((new File(wrkPath+"/"+
	 * "transmittance.dat")).getAbsolutePath()); }; }
	 */

	/*
	 * public static void chargeNode (org.w3c.dom.Node Nd, String NodeName){
	 * 
	 * NodeList NdList; int nc = 0; //SimpleDateFormat sdf = new
	 * SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSS"); SimpleDateFormat sdf = new
	 * SimpleDateFormat("yyyy"); Date date = new Date(); //String wrkPath = new
	 * String("resources/modules/"); //Debug String wrkPath =
	 * Charger.makeWorkerDir("resources/modules/","yyyy", Debug.WORKER_NAME);
	 * 
	 * if (Nd.getNodeName().equals("#document")) { NdList = Nd.getChildNodes();
	 * Module[] MArr = null; for(int i=0; i<NdList.getLength();i++){ if
	 * (NdList.item(i).getNodeName().equals("worker")){
	 * //SplitModuleNode(NdList.item(i));
	 * 
	 * try { MArr = Splitter.Split(NdList.item(i), wrkPath);
	 * Debug.println(MArr.length); } catch(Exception ex){ ex.printStackTrace(); } }; }
	 * for(int i=0; i<MArr.length;i++){ Module Modul = MArr[i]; if
	 * (Modul.ExecName.equals(NodeName)) { File fil = new
	 * File(MArr[i].ExecPath+"/"+Modul.ExecName); if (fil.exists())
	 * {Debug.println("File exist, All Ok: " + fil.getAbsolutePath());
	 * //copyFile(fil.getAbsolutePath(),wrkPath + "/" + Modul.ExecName);
	 * Stylizer.Stylize("resources/gosatTransform1.xsl",wrkPath + "/"
	 * +Modul.XMLParFileName, wrkPath + "/" + Modul.ParFileName); Runtime RE =
	 * java.lang.Runtime.getRuntime(); Process PR; int c=-1; byte bt[] = new
	 * byte[1024*1024]; File exeFil = new File (wrkPath + "/" + Modul.ExecName);
	 * try { String[] Cm = new String[1]; Cm[0] = fil.getAbsolutePath();
	 * ProcessBuilder pb = new ProcessBuilder(Cm); pb.directory(new
	 * File(wrkPath)); //PR = pb.start(); BufferedInputStream InpStream = new
	 * BufferedInputStream((PR = pb.start()).getErrorStream()); PR.waitFor();
	 * FileOutputStream OutFile = new FileOutputStream((new File(wrkPath + "/" +
	 * Modul.ExecName)).getAbsolutePath()+"_Err.log"); if (PR==null)
	 * Debug.println("Process not created: "+ wrkPath + "/" + Modul.ExecName);
	 * else { while((c=InpStream.read(bt))>0){ OutFile.write(bt,0,c);
	 * //Debug.println("Read " + c); } } } catch (Exception E){
	 * E.printStackTrace(); }
	 *  } else Debug.println("Module File Does Not Exist: " +
	 * fil.getAbsolutePath()); } } //new javaSCI.MainJFrame((new
	 * File(wrkPath+"/"+ "transmittance.dat")).getAbsolutePath()); }; }
	 */

	public abstract void chargeAllNodes();

}
