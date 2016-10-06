package platform;

import java.io.PrintStream;

import javax.swing.JTextArea;

public class TeeStream extends PrintStream {
    PrintStream out;
    JTextArea localJTA;
    public TeeStream(PrintStream out1, PrintStream out2, JTextArea JTA) {
        super(out1);
        this.out = out2;
        localJTA = JTA;
    }
    public void addJTextArea (JTextArea JTA){
    	localJTA = JTA;
    }
    public void write(byte buf[], int off, int len) {
        try {
            super.write(buf, off, len);
            if (out!=null) {
            out.write(buf, off, len);
            }
            if (localJTA!=null) {
            	localJTA.append(new String(buf, off, len));
            	localJTA.setCaretPosition(localJTA.getText().length());
            }
        } catch (Exception e) {
        }
    }
    public void flush() {
        super.flush();
        if (out!=null) {
        out.flush();
        }
    }
}
