/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.util;

import java.util.HashMap;

/**
 *
 * @author sergeil
 */
public class DefaultHashMap<K,V> extends HashMap<K, V> {

    public Object get(K keyname, Object def) {
        if (containsKey(keyname)) {
            return get(keyname);
        } else {
            return def;
        }
    }

}
