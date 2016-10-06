package platform;

import javax.swing.JTextField;
import org.w3c.dom.Node;

public class GuiTextNodeFild extends JTextField {
	private Node myNode; 
	public GuiTextNodeFild (Node nd){
		myNode = nd;
		this.setText(myNode.getTextContent().trim());
	}

}

