/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.listeners;

/**
 *
 * @author sergeil
 */
public interface ILogFileListener {

    public void onLogFileRead(String content);

    public void onOutOfMemory();

}
