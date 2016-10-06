package rad.ui;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JComponent;

public class DropTargetComponent  implements DropTargetListener {
    public DropTargetComponent(JComponent jc) {
        new DropTarget(jc, this);
        
    }
    public void dragEnter(DropTargetDragEvent evt) {
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
    }
}