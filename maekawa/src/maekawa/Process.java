package maekawa;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Process extends UnicastRemoteObject implements ProcessInterface {
    private VectorClock vectorClock;
    private boolean inCriticalSection;
    private boolean votedForOther;
    private int[] quorum;
    private int grantsReceived;
    private int processId;
    private Set<Integer> votesGranted;  
    private Queue<Integer> requestQueue;

    public Process(int processId, int[] quorum) throws RemoteException {
        this.vectorClock = new VectorClock();
        this.inCriticalSection = false;
        this.votedForOther = false;
        this.quorum = quorum;
        this.grantsReceived = 0;	
        this.processId = processId;
        this.votesGranted = new HashSet<>();
        this.requestQueue = new LinkedList<>();
    }

    @Override
    public void requestCriticalSection() throws RemoteException {
        vectorClock.increment(processId);
        grantsReceived = 0;  //Reset grants when new request is made
        System.out.println("Process " + processId + " requesting critical section. Quorum size: " + quorum.length);

        //Send request message to all processes in quorum
        for (int quorumMember : quorum) {
            ProcessInterface quorumProcess = null;
			try {
				quorumProcess = (ProcessInterface) Naming.lookup("rmi://localhost/Process" + quorumMember);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
            quorumProcess.receiveRequest(processId, vectorClock.getClock());
        }
    }

    @Override
    public void releaseCriticalSection() throws RemoteException {
        //Set these flags to false so process can vote again once it exits criticalsection
    	inCriticalSection = false;
        votedForOther = false;
        
        grantsReceived = 0;
        votesGranted.clear();

        //Send release message to all processes in quorum
        for (int quorumMember : quorum) {
            ProcessInterface quorumProcess = null;
			try {
				quorumProcess = (ProcessInterface) Naming.lookup("rmi://localhost/Process" + quorumMember);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
            quorumProcess.receiveRelease(processId);
        }
        processNextRequest();
    }

    //Receive request from a process in the quorum
    public synchronized void receiveRequest(int requestingProcessId, int[] remoteClock) throws RemoteException {
        vectorClock.update(remoteClock);

        //Process will grant a vote if it hasnt voted for another process and its not in critical section
        if (!votedForOther && !inCriticalSection) {
            votedForOther = true;
            votesGranted.add(requestingProcessId);
            ProcessInterface requestingProcess = null;
			try {
				requestingProcess = (ProcessInterface) Naming.lookup("rmi://localhost/Process" + requestingProcessId);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
            requestingProcess.receiveGrant(processId);
        }
        else {
        	requestQueue.add(requestingProcessId);
        	System.out.println("Process " + processId + " queued request from Process " + requestingProcessId);
        }
    }

    //Receive a grant vote from a process in the quorum
    public synchronized void receiveGrant(int grantingProcessId) throws RemoteException {
        grantsReceived++;
        System.out.println("Process " + processId + " received grant from Process " + grantingProcessId + ". Votes: " + grantsReceived);

        //Because all quorums are fixed at size 2, only 2 votes needed to enter critical section
        if (grantsReceived >= 2) {
            inCriticalSection = true;
            System.out.println("Process " + processId + " has entered the critical section.");
        }
    }

    //Receive release from process in the quorum
    public synchronized void receiveRelease(int releasingProcessId) throws RemoteException {
        votesGranted.remove(releasingProcessId);
        votedForOther = false;
        System.out.println("Process " + releasingProcessId + " has exited the critical section.");

        processNextRequest();
    }
    
    private synchronized void processNextRequest() throws RemoteException {
        if (!requestQueue.isEmpty()) {
            int nextProcessId = requestQueue.poll();
            votedForOther = true;
            ProcessInterface nextProcess = null;
			try {
				nextProcess = (ProcessInterface) Naming.lookup("rmi://localhost/Process" + nextProcessId);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			//Next process in queue removed from queue and receives vote
            nextProcess.receiveGrant(processId);
            System.out.println("Process " + processId + " granted request to Process " + nextProcessId);
        }
    }
    
    @Override
    public int[] getQuorum() throws RemoteException {
    	return quorum;
    }
}
