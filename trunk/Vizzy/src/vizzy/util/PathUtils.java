/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.util;

import java.io.File;

/**
 *
 * @author sergeil
 */
public class PathUtils {

    public static File getDir(File file) {
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.lastIndexOf(File.separator) != -1) {
            absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1);
        } else {
            absolutePath = "";
        }
        return new File(absolutePath);
    }

}
