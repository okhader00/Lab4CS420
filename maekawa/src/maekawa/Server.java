package maekawa;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            
            for (int i = 0; i < 4; i++) {		//Creating 4 processes
            	//Quorum for each process will consist of the 2 processes whose ids are 1 above and below the process
            	int[] quorum = {((i-1+4)%4), ((i+1)%4)};
            	Process process = new Process(i, quorum);
                Naming.rebind("//localhost/Process" + i, process);

            }
            System.out.println("Processes are ready.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
