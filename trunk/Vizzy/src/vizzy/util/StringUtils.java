/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.util;

/**
 *
 * @author sergeil
 */
public class StringUtils {

    public static String trimStart(String line) {
        int pos = 0;
        while (pos < line.length()) {
            char c = line.charAt(pos);
            if (c == '\t' || c == '\n' || c == '\r' || c == ' ') {
                pos++;
            } else {
                break;
            }
        }
        return line.substring(pos);
    }

}
