/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.model;

/**
 *
 * @author sergeil
 */
public class SourceAndLine {
    public String filePath;
    public int lineNum;
    public int startPos;
    public SourceAndLine(String filePath, int lineNum, int startPos) {
        this.filePath = filePath;
        this.lineNum = lineNum;
        this.startPos = startPos;
    }
}
