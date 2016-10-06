package platform;
import org.w3c.dom.Node;

public class Module {
	
public	String ExecName;
public	String ExecPath;
public	String ParFileName;
public	String XMLParFileName;
public Node mdNode;
public String parType;
//public String 
	
public	Module (String ExeName, String ExePath, String ParFName, String XMLParFName, Node mNode){
		ExecName = ExeName;
		ExecPath = ExePath;
		ParFileName = ParFName;
		XMLParFileName = XMLParFName;
		mdNode = mNode;
	};
	
	//Module (){};
	public	Module (String ExeName, String ExePath, String ParFName, String XMLParFName, Node mNode, String prType){
		ExecName = ExeName;
		ExecPath = ExePath;
		ParFileName = ParFName;
		XMLParFileName = XMLParFName;
		mdNode = mNode;
		parType = prType;
	};	

}
