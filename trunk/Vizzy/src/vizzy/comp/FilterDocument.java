/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.comp;

import javax.swing.text.PlainDocument;

/**
 *
 * @author user
 */
public class FilterDocument extends PlainDocument {
    public FilterDocument() {
        super();
    }

    public FilterDocument(Content c) {
        super(c);
    }
    
    public void writeLock2() {
        writeLock();
    }
    
    public void writeUnlock2() {
        writeUnlock();
    }
}
