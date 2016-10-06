package rad.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import rad.Main2;

public class MainFrame extends JFrame  {
	
	private DataFlavor fileFlavor = DataFlavor.javaFileListFlavor; 
	private DataFlavor stringFlavor = DataFlavor.stringFlavor;

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	public JLabel jLabel = null;
	
	public static String[] full_arg_string;

	public static void main(String[] args){
		full_arg_string = args;
//		for (int i = 0; i <= args.length - 1; i++) {
//			full_arg_string = full_arg_string + args[i] + " ";
//		}
		MainFrame MF = new MainFrame();
		MF.setAlwaysOnTop(true);
		MF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FileAndTextTransferHandler newHandler = new FileAndTextTransferHandler(MF);
		//DropTarget dt = new DropTarget(MF, new DropTargetComponent((JComponent)MF));
		MF.setEnabled(true);
		//MF.setDropTarget(dt);
		//MF.setTransferHandler(newHandler);
		MF.jLabel.setTransferHandler(newHandler);
		MF.setVisible(true);
		
	}
	/*public void dragEnter(DropTargetDragEvent evt) {
        // Called when the user is dragging and enters this drop target.
    }
    public void dragOver(DropTargetDragEvent evt) {
        // Called when the user is dragging and moves over this drop target.
    }
    public void dragExit(DropTargetEvent evt) {
        // Called when the user is dragging and leaves this drop target.
    }
    public void dropActionChanged(DropTargetDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    public void drop(DropTargetDropEvent evt) {
        // Called when the user finishes or cancels the drag operation.
    	System.out.println("Drop!!!");
    	Transferable t = evt.getTransferable();
    	
        try {
            if (hasFileFlavor(t.getTransferDataFlavors())) {
           // if (hasStringFlavor(t.getTransferDataFlavors())) {	
                String str = null;
                //str = (String)t.getTransferData(stringFlavor);
                java.util.List files =
                     (java.util.List)t.getTransferData(fileFlavor);
                for (int i = 0; i < files.size(); i++) {
                    File file = (File)files.get(i);
                    //Tell the tabbedpane controller to add
                    //a new tab with the name of this file
                    //on the tab.  The text area that will
                    //display the contents of the file is returned.
                    //tc = tpc.addTab(file.toString());
                    //GUIstarter GUI = new GUIstarter();
                    
                    String fname = file.getName();
                    if (fname.endsWith(".dat")) {
                    	//Main.runGUI(file.getAbsolutePath());
                    	ArrayList<String> cmAL = new ArrayList<String>(); 
                    	
                    	cmAL.add( "-Ddata.file.path="+file.getAbsolutePath());
                    	//cmAL.add( "-cp");
                    	//cmAL.add( ".;../xalan/xalan.jar");
                    	//cmAL.add( ".;rad/bin/traceviewer.jar");
                    	//cmAL.add( "rad.Main");
                    	//cmAL.add( "-gui");
                    	//String cmdKeys = Debug.getCmdKeyString();
                    	//cmAL.add( cmdKeys);
                    	//cmAL.add( file.getAbsolutePath());
                    	//Object[] cmd = cmAL.toArray();
                    	String[] cmd = (String[])cmAL.toArray(new String[cmAL.size()]);
                    	//ProcessBuilder PB = new ProcessBuilder(cmd);
                    	//PB.start();
                    	//PB.
                    	Main2.main(cmd);
                    	
                    } else {
                    	//GS.setDataFile(file);
                    }

                    BufferedReader in = null;

                    try {
                        in = new BufferedReader(new FileReader(file));

                        while ((str = in.readLine()) != null) {
                            //tc.append(str + newline);
                        }
                    } catch (IOException ioe) {
                        System.out.println(
                          "importData: Unable to read from file " +
                           file.toString());
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ioe) {
                                 System.out.println(
                                  "importData: Unable to close file " +
                                   file.toString());
                            }
                        }
                    }
                }
                //return true;
            } else  {
                System.out.println("It is not a file!!!");
            }
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("importData: unsupported data flavor");
        } catch (IOException ieo) {
            System.out.println("importData: I/O exception");
        }
    	
    }*/
	
    private boolean hasFileFlavor(DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (fileFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasStringFlavor(DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (stringFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }
	/**
	 * This is the default constructor
	 */
	public MainFrame() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(270, 50);
		this.setLocation(250, 5);
		this.setContentPane(getJContentPane());
		this.setTitle("RAD LIDAR drop panel");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jLabel.setText("Drag & Drop LIDAR data file");
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(jLabel, BorderLayout.CENTER);
		}
		return jContentPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
