package platform;

import org.w3c.dom.*;

//import com.sun.org.apache.bcel.internal.generic.FNEG;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;

import rad.Main2;
import rad.bin.Runner;

import java.util.*;

//import rad.*;

public class Splitter {

	/**
	 * @param args
	 */
	static Logger logger = Logger.getLogger(Splitter.class);

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: java Splitter xmlfile");
			return;
		}
		File tstFile = new File(args[0]);
		if (!tstFile.exists()) {
			throw new Exception("File: " + args[0] + " Does not exists");
		}
		;
		Document document = DomBuilder.BuildDocumentFromFile(args[0]);
		NodeList NdList;
		NdList = document.getElementsByTagName("worker");
		Module[] MArr;
		for (int i = 0; i < NdList.getLength(); i++) {
			if (NdList.item(i).getNodeName().equals("worker")) {
				try {
					MArr = Splitter.Split(NdList.item(i), System
							.getProperty("user.dir"));
					int mc = MArr.length;
					Debug.println(mc);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			;
		}

	}

	public static Module[] Split(Node Nd, String dstPath) throws Exception {
		NodeList NdList;
		Node N;
		File FilRes;
		int mc = 0;
		// NdList = XPathAPI.selectNodeList(Nd,"//module");
		NdList = Nd.getChildNodes();
		// System.out.println(Nd.getFirstChild().getNodeName());

		if (NdList.getLength() == 0) {
			throw new Exception("No Child Nodes in Worker!!!");
		}
		mc = 0;
		// int rc = 0;
		for (int i = 0; i < NdList.getLength(); i++) {
			N = NdList.item(i);
			if (N.getNodeName().equals("module"))
				mc++;
		}
		if (mc == 0) {
			throw new Exception("No \"module\" Nodes in Worker!!!");
		}
		Module[] MArr = new Module[mc];
		mc = 0;

		for (int i = 0; i < NdList.getLength(); i++) {
			N = NdList.item(i);

			if (N.getNodeName().equals("module")) {

				// rc = 0;
				// rc++;
				String fName = new String(dstPath + "/"); // здесь нужна
				// системная
				// переменная
				FilRes = new File(fName);
				FilRes.mkdir();

				String ExecName = getElementTextByName(N, "exec_name");
				String ExecPath = getElementTextByName(N, "exec_path");
				String ParFileName = null;
				String XMLParFileName = "module" + mc + "_"
						+ getElementTextByName(N, "exec_name") + ".xml";
				String prType = getElementTextByName(N, "parType");
				Node Npar = getElementByName(N, "parameters");

				if (Npar != null)
					ParFileName = getElementTextByName(Npar, "parFile");

				MArr[mc] = new Module(ExecName, ExecPath, ParFileName,
						XMLParFileName, N, prType);
				mc++;
				new File(fName + "/.service/").mkdirs();
				fName = fName + "/.service/" + XMLParFileName;
				FilRes = new File(fName);
				DOMSource source = new DOMSource(N);
				StreamResult result = new StreamResult(FilRes);
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				transformer.transform(source, result);

			}
		}

		// Stylizer.Stylize("resources/gosatSample01.xml", fName, );
		return MArr;
	}

	public static Module[] Split(Node Nd) throws Exception {
		NodeList NdList;
		Node N;
		File FilRes;
		int mc = 0;
		NdList = Nd.getChildNodes();

		if (NdList.getLength() == 0) {
			throw new Exception("No Child Nodes in Worker!!!");
		}
		mc = 0;
		// int rc = 0;
		for (int i = 0; i < NdList.getLength(); i++) {
			N = NdList.item(i);
			if (N.getNodeName().equals("module"))
				mc++;
		}
		if (mc == 0) {
			throw new Exception("No \"module\" Nodes in Worker!!!");
		}
		Module[] MArr = new Module[mc];
		mc = 0;

		for (int i = 0; i < NdList.getLength(); i++) {
			N = NdList.item(i);

			if (N.getNodeName().equals("module")) {

				// rc = 0;
				// rc++;
				String fName = new String("dstPath" + "/"); // здесь нужна
				// системная
				// переменная
				FilRes = new File(fName);
				// FilRes.mkdir();

				String ExecName = getElementTextByName(N, "exec_name");
				String ExecPath = getElementTextByName(N, "exec_path");
				String ParFileName = null;
				String XMLParFileName = "module_"
						+ getElementTextByName(N, "exec_name") + ".xml";

				Node Npar = getElementByName(N, "parameters");

				if (Npar != null)
					ParFileName = getElementTextByName(Npar, "parFile");

				MArr[mc] = new Module(ExecName, ExecPath, ParFileName,
						XMLParFileName, Npar);
				mc++;
				fName = fName + XMLParFileName;

				FilRes = new File(fName);
				DOMSource source = new DOMSource(N);
				StreamResult result = new StreamResult(FilRes);
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				// transformer.transform(source, result);

			}
		}

		// Stylizer.Stylize("resources/gosatSample01.xml", fName, );
		return MArr;
	}

	public static String getElementTextByName(Node N, String ElemName) {
		String Text = "";
		NodeList NdList = N.getChildNodes();
		for (int i = 0; i < NdList.getLength(); i++) {
			if (NdList.item(i).getNodeName().equalsIgnoreCase(ElemName)) {
				NdList.item(i).normalize();
				// NdList.item(i)
				Text = NdList.item(i).getTextContent().trim();
				// Runner.replacedPropertyString(NdList.item(i)
				// .getTextContent().trim());
			}
		}
		// Text = rad.bin.Runner.replacedPropertyString(Text);
		return Text;
	}

	public static org.w3c.dom.Node getElementByName(Node N, String ElemName) {

		NodeList NdList = N.getChildNodes();
		Node Nd = null;
		for (int i = 0; i < NdList.getLength(); i++) {
			if (NdList.item(i).getNodeName().equals(ElemName)) {
				Nd = NdList.item(i);
				break;
			}
		}
		return Nd;
	}

	public static String getExecPath(Node N) {

		return getElementTextByName(N, "exec_path");
	}

	public static void visit(Node node, int level) {
		// Process node

		// If there are any children, visit each one
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			// Get child node
			Node childNode = list.item(i);
			childNode.normalize();
			PrintSpc(level);
			if (childNode.getNodeName().equals("#text")
					&& (childNode.getNodeValue().trim().length() != 0))
				logger.debug(childNode.getNodeName() + " : "
						+ childNode.getNodeValue().trim());
			else if (childNode.getNodeValue() == null)
				logger.debug(childNode.getNodeName());

			if ((childNode.getAttributes() != null)
					&& (childNode.getNodeType() == Node.ELEMENT_NODE)) {
				if (childNode.getAttributes().getLength() != 0) {
					PrintSpc(level);
					logger.debug(childNode.getAttributes().item(0)
							.getNodeName()
							+ " = ");
					PrintSpc(level);
					logger.debug(childNode.getAttributes().item(0)
							.getNodeValue());
				}
			}
			// Visit child node
			visit(childNode, level + 1);
		}
	}

	public static void PrintSpc(int spcs) {
		for (int i = 0; i < spcs; i++) {
			Debug.print(" ");
		}
	}

	public static int splitTree(Node Nd, String dstPath) {
		int fileCnt = 0;
		NodeList NL = null;
		File f = new File(dstPath);
		if (f.exists()) {
			clearDstPath(dstPath);
		}
		f.mkdirs();

		try {
			NL = XPathAPI.selectNodeList(Nd, "//split");
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		if (NL.getLength() == 0) {
			fileCnt = 1;
			// writeDOMtoFile(Nd, dstPath+"/"+"Tree_"+fileCnt+".xml");
			IteRateVectorNode IRVN = new IteRateVectorNode(NL);
			writeDOMtoFile(Nd, dstPath + "/" + IRVN.getCurDirName() + "/"
					+ "non-splitted-copy.xml");
		} else {
			IteRateVectorNode IRVN = new IteRateVectorNode(NL);
			fileCnt = 0;
			/** ******* */
			do {
				fileCnt++;
				new File(dstPath + "/" + IRVN.getCurDirName()).mkdirs();
				logger.info("Writing file: " + IRVN.getCurFileName());
				writeDOMtoFile(Nd, dstPath + "/" + IRVN.getCurDirName() + "/"
						+ IRVN.getCurFileName());// "Tree_"+fileCnt+".xml");
			} while (IRVN.iterateNext());
			IRVN.setOrigin();
		}
		return fileCnt;
	}

	public static void writeDOMtoFile(Node N, String fName) {
		// File fil = new File(fName);
		File FilRes = new File(fName);
		DOMSource source = new DOMSource(N);
		StreamResult result = new StreamResult(FilRes);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = tFactory.newTransformer();
			transformer.transform(source, result);
		} catch (TransformerConfigurationException tce) {
			tce.printStackTrace();
		} catch (TransformerException te) {
			te.printStackTrace();
		}

	}

	public static void clearDstPath(String dst) {
		visitAllDirsAndFiles(new File(dst), new File(dst));
	}

	// Process all files and directories under dir
	public static void visitAllDirsAndFiles(File dir, File dir2) {
		// process(dir);

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllDirsAndFiles(new File(dir, children[i]), null);
			}
		}
		if (dir2 == null) {
			process(dir);
		}
	}

	// Process only directories under dir
	public static void visitAllDirs(File dir) {
		if (dir.isDirectory()) {
			process(dir);

			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllDirs(new File(dir, children[i]));
			}
			process(dir);
		}
	}

	// Process only files under dir
	public static void visitAllFiles(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllFiles(new File(dir, children[i]));
			}
		} else {
			process(dir);
		}
	}

	public static void process(File dir) {
		if (dir.delete()) {
			logger.info("Item deleted: " + dir.getAbsolutePath());
		} else {
			logger.info("Item was not deleted: " + dir.getAbsolutePath());
		}
	}

}

class IteRateVectorNode {
	NodeList curNL = null;

	iteratedNode iNode[];

	public IteRateVectorNode(NodeList NL) {
		curNL = NL;
		try {
			iNode = new iteratedNode[NL.getLength()];
			for (int i = 0; i <= iNode.length - 1; i++) {
				iNode[i] = new iteratedNode(NL.item(i));
				iNode[i].goBegin();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	public boolean iterateNext() {
		boolean isWasIterated = false;
		// boolean mkDirflg = false;
		int itPoint;
		itPoint = iNode.length - 1;
		while ((isWasIterated == false) && (itPoint >= 0)) {
			isWasIterated = iNode[itPoint].iterate();
			if (isWasIterated == false) {
				iNode[itPoint].goBegin();
				// mkDirflg = true;
			}
			itPoint--;
		}
		return isWasIterated;
	}

	public String getCurFileName() {
		return iNode[iNode.length - 1].getIterateString() + ".xml";
	}

	public String getCurDirName() {
		String DirString = "/";
		if (iNode.length < 2) {
			return "/";
		} else {
			DirString = "";
			for (int i = 0; i <= iNode.length - 2; i++) {
				DirString = DirString + "/" + iNode[i].getIterateString()
						+ ".dir" + "/";
			}
		}
		return DirString;
	}

	public void setOrigin() {
		for (int i = 0; i <= iNode.length - 1; i++) {
			iNode[i].setOrigin();
		}
	}
}

class iteratedNode {
	Quadro Q;

	final int ITER_SINGLE = 0;

	final int ITER_PAIR = 1;

	final int ITER_SET = 2;

	int iter_type = ITER_SINGLE;

	Node beginNode;

	String beginOrigin;

	Node endNode;

	String endOrigin;

	Node stepNode;

	// QuadroDbl QD;

	Quadro makeQuadro(NodeList parNL, NodeList stepNL) throws Exception {
		Quadro statQ;
		// NodeList parNL = null;
		// NodeList stepNL = null;
		String strBegin;
		String strStep;
		String strEnd;
		Double dblBegin;
		Double dblEnd;
		Double dblStep;
		Integer intBegin;
		Integer intEnd;
		Integer intStep;
		String AttrType = parNL.item(0).getAttributes().getNamedItem("type")
				.getNodeValue();
		beginNode = parNL.item(0);
		strBegin = Runner.replacedPropertyString(beginNode.getTextContent()
				.trim());
		beginOrigin = Runner.replacedPropertyString(beginNode.getTextContent());
		endNode = parNL.item(1);
		strEnd = Runner.replacedPropertyString(endNode.getTextContent().trim());
		endOrigin = Runner.replacedPropertyString(endNode.getTextContent());
		stepNode = stepNL.item(0);
		strStep = Runner.replacedPropertyString(stepNode.getTextContent()
				.trim());

		if (AttrType.equalsIgnoreCase("float")) {
			dblBegin = Double.parseDouble(strBegin);
			dblEnd = Double.parseDouble(strEnd);
			dblStep = Double.parseDouble(strStep);
			statQ = new Quadro(dblBegin, dblEnd, dblStep);
		} else if (AttrType.equalsIgnoreCase("integer")) {
			intBegin = Integer.parseInt(strBegin);
			intEnd = Integer.parseInt(strEnd);
			intStep = Integer.parseInt(strStep);
			statQ = new Quadro(intBegin, intEnd, intStep);
		} else {
			throw new Exception("String-type Nodes may not be splited");
		}
		return statQ;
	}

	Quadro makeQuadro(NodeList parNL, NodeList stepNL, NodeList endNL)
			throws Exception {
		Quadro statQ;
		// NodeList parNL = null;
		// NodeList stepNL = null;
		String strBegin;
		String strStep;
		String strEnd;
		Double dblBegin;
		Double dblEnd;
		Double dblStep;
		Integer intBegin;
		Integer intEnd;
		Integer intStep;
		String AttrType = parNL.item(0).getAttributes().getNamedItem("type")
				.getNodeValue();
		beginNode = parNL.item(0);
		strBegin = Runner.replacedPropertyString(beginNode.getTextContent()
				.trim());
		beginOrigin = Runner.replacedPropertyString(beginNode.getTextContent());
		endNode = endNL.item(0);
		strEnd = Runner.replacedPropertyString(endNode.getTextContent().trim());
		endOrigin = Runner.replacedPropertyString(endNode.getTextContent());
		stepNode = stepNL.item(0);
		strStep = Runner.replacedPropertyString(stepNode.getTextContent()
				.trim());

		if (AttrType.equalsIgnoreCase("float")
				|| AttrType.equalsIgnoreCase("double")) {
			dblBegin = Double.parseDouble(strBegin);
			dblEnd = Double.parseDouble(strEnd);
			dblStep = Double.parseDouble(strStep);
			statQ = new Quadro(dblBegin, dblEnd, dblStep);
		} else if (AttrType.equalsIgnoreCase("integer")) {
			intBegin = Integer.parseInt(strBegin);
			intEnd = Integer.parseInt(strEnd);
			intStep = Integer.parseInt(strStep);
			statQ = new Quadro(intBegin, intEnd, intStep);
		} else {
			throw new Exception("String-type Nodes may not be splited");
		}
		return statQ;
	}

	Quadro makeQuadroSet(NodeList parNL, NodeList itemNL) throws Exception {
		Quadro statQ;

		String strBegin;
		Double dblBegin;

		Integer intBegin;
		// Integer intEnd;

		String AttrType = parNL.item(0).getAttributes().getNamedItem("type")
				.getNodeValue();
		beginNode = parNL.item(0);

		strBegin = Runner.replacedPropertyString(beginNode.getTextContent()
				.trim());
		beginOrigin = Runner.replacedPropertyString(beginNode.getTextContent());

		List list = new ArrayList();
		list.add(strBegin);

		for (int i = 0; i <= itemNL.getLength() - 1; i++) {
			list.add(Runner.replacedPropertyString(itemNL.item(i)
					.getTextContent().trim()));
		}

		if (AttrType.equalsIgnoreCase("float")
				|| AttrType.equalsIgnoreCase("double")) {
			dblBegin = Double.parseDouble(strBegin);
			// statQ = new Quadro(dblBegin, dblEnd, dblStep);
			statQ = null;
		} else if (AttrType.equalsIgnoreCase("integer")) {
			intBegin = Integer.parseInt(strBegin);
			statQ = new Quadro(intBegin, list);
		} else {
			throw new Exception("String-type Nodes may not be splited");
		}

		return statQ;
	}

	public iteratedNode(Node Nd) throws Exception {
		NodeList parNL = null;
		NodeList stepNL = null;
		NodeList endNL = null;
		NodeList itemNL = null;
		try {
			Node Attr = Nd.getAttributes().getNamedItem("type");
			String AttrString = Attr.getNodeValue();
			parNL = XPathAPI.selectNodeList(Nd, "par");
			stepNL = XPathAPI.selectNodeList(Nd, "step");
			endNL = XPathAPI.selectNodeList(Nd, "end");
			itemNL = XPathAPI.selectNodeList(Nd, "item");

			if (AttrString.equalsIgnoreCase("pair")) {
				iter_type = ITER_PAIR;
				if (parNL.getLength() != 2) {
					throw new Exception(
							"Number of \"par\" nodes in \"pair\"-split must be two, but found: "
									+ parNL.getLength());
				} else if (stepNL.getLength() != 1) {
					throw new Exception(
							"Number of \"step\" nodes in \"pair\"-split must be one, but found: "
									+ stepNL.getLength());
				} else {
					Q = makeQuadro(parNL, stepNL);
				}
			} else if (AttrString.equalsIgnoreCase("single")) {
				iter_type = ITER_SINGLE;
				if (parNL.getLength() != 1) {
					throw new Exception(
							"Number of \"par\" nodes in \"single\"-split must be one, but found: "
									+ parNL.getLength());
				} else if (stepNL.getLength() != 1) {
					throw new Exception(
							"Number of \"step\" nodes in \"single\"-split must be one, but found: "
									+ stepNL.getLength());
				} else if (endNL.getLength() != 1) {
					throw new Exception(
							"Number of \"end\" nodes in \"single\"-split must be one, but found: "
									+ stepNL.getLength());
				} else {
					Q = makeQuadro(parNL, stepNL, endNL);
				}
			} else if (AttrString.equalsIgnoreCase("set")) {
				iter_type = ITER_SET;
				if (parNL.getLength() != 1) {
					throw new Exception(
							"Number of \"par\" nodes in \"set\"-split must be one, but found: "
									+ parNL.getLength());
				}
				Q = makeQuadroSet(parNL, itemNL);
			}
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	void goBegin() {
		// boolean flg = false;
		switch (iter_type) {
		case ITER_PAIR:
			// if ((flg = Q.iteratePair())==true){
			Q.goBegin();
			beginNode.setTextContent(Q.getBeginStr());
			endNode.setTextContent(Q.getEndStr());
			// }
			// flg = true;
			break;
		case ITER_SINGLE:
			// if ((flg = Q.iterateSingle())==true){
			Q.goBegin();
			beginNode.setTextContent(Q.getBeginStr());
			// }
			// flg = true;
			break;
		case ITER_SET:
			// if ((flg = Q.iterateSingle())==true){
			Q.goBegin();
			beginNode.setTextContent(Q.getBeginStr());
			// }
			// flg = true;
			break;
		}
		// return flg;
	}

	boolean iterate() {
		boolean flg = false;
		switch (iter_type) {
		case ITER_PAIR:
			if ((flg = Q.iteratePair()) == true) {
				beginNode.setTextContent(Q.getBeginStr());
				endNode.setTextContent(Q.getEndStr());
			}
			// flg = true;
			break;
		case ITER_SINGLE:
			if ((flg = Q.iterateSingle()) == true) {
				beginNode.setTextContent(Q.getBeginStr());
			}
			// flg = true;
			break;
		case ITER_SET:
			if ((flg = Q.iterateSet()) == true) {
				beginNode.setTextContent(Q.getBeginStr());
			}
			// flg = true;
			break;
		}
		return flg;
	}

	String getIterateString() {
		String iterstring = new String("");
		String iterstring2 = new String("");
		switch (iter_type) {
		case ITER_SINGLE:
			iterstring = beginNode.getAttributes().getNamedItem("name")
					.getNodeValue().trim();
			iterstring = iterstring + "_(" + beginNode.getTextContent().trim()
					+ ")";
			break;
		case ITER_PAIR:
			iterstring = beginNode.getAttributes().getNamedItem("name")
					.getNodeValue().trim();
			iterstring = iterstring + "_(" + beginNode.getTextContent().trim()
					+ ")";
			iterstring2 = endNode.getAttributes().getNamedItem("name")
					.getNodeValue().trim();
			iterstring2 = iterstring2 + "_(" + endNode.getTextContent().trim()
					+ ")";
			iterstring = iterstring + "__" + iterstring2;
			break;
		case ITER_SET:
			iterstring = beginNode.getAttributes().getNamedItem("name")
					.getNodeValue().trim();
			iterstring = iterstring + "_(" + beginNode.getTextContent().trim()
					+ ")";
			break;
		}
		return iterstring;
	}

	public void setOrigin() {
		beginNode.setTextContent(beginOrigin);
		if (endNode != null) {
			endNode.setTextContent(endOrigin);
		}
	}
};

class Quadro {
	public QuadroInt QI;

	public QuadroDbl QD;

	public Quadro(int beginint, int endint, int stepint) {
		QI = new QuadroInt(beginint, endint, stepint);
	}

	public Quadro(int beginint, List list) {
		QI = new QuadroInt(beginint, list);
	}

	public Quadro(double begindbl, double enddbl, double stepdbl) {
		QD = new QuadroDbl(begindbl, enddbl, stepdbl);
	}

	boolean iterateSingle() {
		boolean flg;
		if (QI != null) {
			flg = QI.iterateSingle();
		} else {
			flg = QD.iterateSingle();
		}
		return flg;
	}

	boolean iteratePair() {
		boolean flg = false;
		if (QI != null) {
			flg = QI.iteratePair();
		} else {
			flg = QD.iteratePair();
		}
		return flg;
	}

	boolean iterateSet() {
		boolean flg = false;
		if (QI != null) {
			flg = QI.iterateSet();
		} else {
			// flg = QD.iterateSet();
		}
		return flg;
	}

	String getBeginStr() {
		String retStr = (QI != null) ? QI.getBeginStr() : QD.getBeginStr();
		return retStr;
	}

	String getEndStr() {
		String retStr = (QI != null) ? QI.getEndStr() : QD.getEndStr();
		return retStr;
	}

	public void goBegin() {
		if (QI != null) {
			QI.goBegin();
		} else {
			QD.goBegin();
		}
	}
}

class QuadroInt {
	public Integer begin;

	public Integer end;

	public Integer step;

	public Integer cur;

	public Integer curBegin;

	public Integer curEnd;

	public Iterator it;

	public List itemList;

	public QuadroInt(int beginint, int endint, int stepint) {
		begin = new Integer(beginint);
		end = new Integer(endint);
		step = new Integer(stepint);
		curBegin = begin;
		curEnd = curBegin + step;
		if (curEnd > end) {
			curEnd = end;
		}
		Debug.println("QuadroInt is created: begin=" + begin + "; end=" + end
				+ "; step=" + step + "; curBegin=" + curBegin + "; curEnd="
				+ curEnd);
	}

	public QuadroInt(int beginint, List list) {
		itemList = list;
		begin = new Integer((String) itemList.get(0));
		end = new Integer((String) itemList.get(itemList.size() - 1));
		step = null;
		curBegin = begin;
		curEnd = null;
		it = itemList.iterator();
		it.next();
		// if (curEnd>end) {
		// curEnd = end;
		// }
		Debug.println("QuadroInt is created: begin=" + begin + "; curBegin="
				+ curBegin + "; List.size()=" + itemList.size());
	}

	boolean iterateSingle() {
		if (step == 0) {
			return false;
		}
		if (curBegin >= end) {
			return false;
		} else {
			curBegin = curBegin + step;
			if (curBegin > end) {
				curBegin = end;
			}
			Debug.println("QI.iterateSingle: begin=" + begin + "; end=" + end
					+ "; step=" + step + "; curBegin=" + curBegin);
			return true;
		}
	}

	boolean iteratePair() {
		// защита от зацикливания, если шаг равен нулю
		if (step == 0) {
			return false;
		}
		if (curEnd >= end) {
			return false;
		} else {
			curBegin = curEnd;
			curEnd = curBegin + step;
			if (curEnd > end) {
				curEnd = end;
			}
			Debug.println("QI.iteratePair: begin=" + begin + "; end=" + end
					+ "; step=" + step + "; curBegin=" + curBegin + "; curEnd="
					+ curEnd);
			return true;
		}
	}

	boolean iterateSet() {
		if (!it.hasNext()) {
			return false;
		} else {
			curBegin = new Integer((String) it.next());
			// curEnd = curBegin + step;
			// if (curEnd>end) {
			// curEnd = end;
			// }
			Debug.println("QI.iterateSet: begin=" + begin + "; curBegin="
					+ curBegin + "; List.size()=" + itemList.size());
			return true;
		}
	}

	void goBegin() {
		if (itemList == null) {
			curBegin = begin;
			// curEnd = curBegin + step;
			// защита, если шаг равен нулю
			curEnd = (step != 0) ? (curBegin + step) : end;
			if (curEnd > end) {
				curEnd = end;
			}
			Debug.println("QI.goBegin: begin=" + begin + "; end=" + end
					+ "; step=" + step + "; curBegin=" + curBegin + "; curEnd="
					+ curEnd);
		} else {
			begin = new Integer((String) itemList.get(0));
			end = new Integer((String) itemList.get(itemList.size() - 1));
			curBegin = begin;
			it = itemList.iterator();
			it.next();

			Debug.println("QI.goBegin: begin=" + begin + "; curBegin="
					+ curBegin + "; List.size()=" + itemList.size());
		}
	}

	String getBeginStr() {
		return curBegin.toString();
	}

	String getEndStr() {
		return curEnd.toString();
	}

}

class QuadroDbl {
	public Double begin;

	public Double end;

	public Double step;

	public Double curBegin;

	public Double curEnd;

	public QuadroDbl(double begindbl, double enddbl, double stepdbl) {
		begin = new Double(begindbl);
		end = new Double(enddbl);
		step = new Double(stepdbl);
		curBegin = begin;
		curEnd = curBegin + step;
		if (curEnd > end) {
			curEnd = end;
		}
		Debug.println("QuadroDbl is created: begin=" + begin + "; end=" + end
				+ "; step=" + step + "; curBegin=" + curBegin + "; curEnd="
				+ curEnd);
	}

	boolean iterateSingle() {
		if (step == 0.) {
			return false;
		}
		if (curBegin >= end) {
			return false;
		} else {
			curBegin = curBegin + step;
			if (curBegin > end) {
				curBegin = end;
			}
			Debug.println("QD.iterateSingle: begin=" + begin + "; end=" + end
					+ "; step=" + step + "; curBegin=" + curBegin);
			return true;
		}
	}

	boolean iteratePair() {
		// защита от зацикливания, если шаг равен нулю
		if (step == 0.) {
			return false;
		}
		if (curEnd >= end) {
			return false;
		} else {
			curBegin = curEnd;
			curEnd = curBegin + step;
			if (curEnd > end) {
				curEnd = end;
			}
			Debug.println("QD.iteratePair: begin=" + begin + "; end=" + end
					+ "; step=" + step + "; curBegin=" + curBegin + "; curEnd="
					+ curEnd);
			return true;
		}
	}

	void goBegin() {
		curBegin = begin;
		// защита, если шаг равен нулю
		curEnd = (step != 0.) ? (curBegin + step) : end;
		if (curEnd > end) {
			curEnd = end;
		}
		Debug.println("QD.goBegin: begin=" + begin + "; end=" + end + "; step="
				+ step + "; curBegin=" + curBegin + "; curEnd=" + curEnd);
	}

	String getBeginStr() {
		return curBegin.toString();
	}

	String getEndStr() {
		return curEnd.toString();
	}

}
