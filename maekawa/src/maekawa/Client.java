package maekawa;

import java.rmi.Naming;

public class Client {
    public static void main(String[] args) {
        try {
            //Assigning names to processes created in server
            ProcessInterface process0 = (ProcessInterface) Naming.lookup("//localhost/Process0");
            ProcessInterface process1 = (ProcessInterface) Naming.lookup("//localhost/Process1");
            ProcessInterface process2 = (ProcessInterface) Naming.lookup("//localhost/Process2");
            ProcessInterface process3 = (ProcessInterface) Naming.lookup("//localhost/Process3");

            System.out.println("Processes are ready.");
            System.out.println(process0.getQuorum()[0]);
            System.out.println(process0.getQuorum()[1]);
            
            process0.requestCriticalSection();
            process2.requestCriticalSection();
            process3.requestCriticalSection();
            process0.releaseCriticalSection();
            process3.releaseCriticalSection();
            process2.releaseCriticalSection();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}