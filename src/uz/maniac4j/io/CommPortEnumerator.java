
package uz.maniac4j.io;

import java.util.Enumeration;


class CommPortEnumerator implements Enumeration<CommPortIdentifier> {

    private CommPortIdentifier index;

    @Override
    public CommPortIdentifier nextElement() {
        synchronized (CommPortIdentifier.Sync) {
            if (index != null) {
                index = index.next;
            } else {
                index = CommPortIdentifier.CommPortIndex;
            }
            return index;
        }
    }

    @Override
    public boolean hasMoreElements() {
        synchronized (CommPortIdentifier.Sync) {
            if (index != null) {
                return index.next != null;
            } else {
                return CommPortIdentifier.CommPortIndex != null;
            }
        }
    }
}
