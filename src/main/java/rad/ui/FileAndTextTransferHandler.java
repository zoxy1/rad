package rad.ui;
/*
 * FileAndTextTransferHandler.java is used by the 1.4
 * DragFileDemo.java example.
 */

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import rad.Main2;

//import rad.Main2;

public class  FileAndTextTransferHandler extends TransferHandler {
    private DataFlavor fileFlavor, stringFlavor;
    //private TabbedPaneController tpc;
    private JFrame jf; 
    //private JTree jtr;
    //private GUIstarter GS;
    private JTextArea source;
    private boolean shouldRemove;
    protected String newline = "\n";

    //Start and end position in the source text.
    //We need this information when performing a MOVE
    //in order to remove the dragged text from the source.
    Position p0 = null, p1 = null;

 public   FileAndTextTransferHandler(JFrame JF) {
       jf = JF;
       fileFlavor = DataFlavor.javaFileListFlavor;
       stringFlavor = DataFlavor.stringFlavor;
    }

    
/* public   FileAndTextTransferHandler(JTree t) {
        jtr = t;
        fileFlavor = DataFlavor.javaFileListFlavor;
        stringFlavor = DataFlavor.stringFlavor;
     }*/
    
/* public   FileAndTextTransferHandler(GUIstarter t) {
        GS = t;
        fileFlavor = DataFlavor.javaFileListFlavor;
        stringFlavor = DataFlavor.stringFlavor;
     }*/
    
    public boolean importData(JComponent c, Transferable t) {
        JTextArea tc;

        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        //A real application would load the file in another
        //thread in order to not block the UI.  This step
        //was omitted here to simplify the code.
        try {
            if (hasFileFlavor(t.getTransferDataFlavors())) {
                String str = null;
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
                    if (fname.endsWith(".dat")||fname.endsWith(".txt")) {
                    	//Main.runGUI(file.getAbsolutePath());
                    	ArrayList<String> cmAL = new ArrayList<String>(); 
                    	
                    	for (int j = 0; j < MainFrame.full_arg_string.length; j++) {
                    		cmAL.add(MainFrame.full_arg_string[j]);
						}
                    	
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
                    	System.out.println("File must have *.txt or *.dat extention but found: "+fname);
                    	
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
                return true;
            } else if (hasStringFlavor(t.getTransferDataFlavors())) {
                tc = (JTextArea)c;
                if (tc.equals(source) && (tc.getCaretPosition() >= p0.getOffset()) &&
                                         (tc.getCaretPosition() <= p1.getOffset())) {
                    shouldRemove = false;
                    return true;
                }
                String str = (String)t.getTransferData(stringFlavor);
                tc.replaceSelection(str);
                return true;
            }
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("importData: unsupported data flavor");
        } catch (IOException ieo) {
            System.out.println("importData: I/O exception");
        }
        return false;
    }

    protected Transferable createTransferable(JComponent c) {
        source = (JTextArea)c;
        int start = source.getSelectionStart();
        int end = source.getSelectionEnd();
        Document doc = source.getDocument();
        if (start == end) {
            return null;
        }
        try {
            p0 = doc.createPosition(start);
            p1 = doc.createPosition(end);
        } catch (BadLocationException e) {
            System.out.println(
              "Can't create position - unable to remove text from source.");
        }
        shouldRemove = true;
        String data = source.getSelectedText();
        return new StringSelection(data);
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    //Remove the old text if the action is a MOVE.
    //However, we do not allow dropping on top of the selected text,
    //so in that case do nothing.
    protected void exportDone(JComponent c, Transferable data, int action) {
        if (shouldRemove && (action == MOVE)) {
            if ((p0 != null) && (p1 != null) &&
                (p0.getOffset() != p1.getOffset())) {
                try {
                    JTextComponent tc = (JTextComponent)c;
                    tc.getDocument().remove(
                       p0.getOffset(), p1.getOffset() - p0.getOffset());
                } catch (BadLocationException e) {
                    System.out.println("Can't remove text from source.");
                }
            }
        }
        source = null;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (hasFileFlavor(flavors))   { return true; }
        if (hasStringFlavor(flavors)) { return true; }
        return false;
    }

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
}
