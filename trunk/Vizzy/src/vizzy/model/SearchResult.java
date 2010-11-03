/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.model;

/**
 *
 * @author sergeil
 */
public class SearchResult {
    public int offset;
    public int wordSize;

    public SearchResult(int offset, int wordSize) {
        this.offset = offset;
        this.wordSize = wordSize;
    }

}
