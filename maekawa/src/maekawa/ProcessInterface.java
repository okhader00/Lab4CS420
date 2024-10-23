package maekawa;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProcessInterface extends Remote {
	void requestCriticalSection() throws RemoteException;
    void releaseCriticalSection() throws RemoteException;
    void receiveRequest(int processId, int[] clock) throws RemoteException;
    void receiveGrant(int processId) throws RemoteException;
    void receiveRelease(int processId) throws RemoteException;
	int[] getQuorum() throws RemoteException;
}
