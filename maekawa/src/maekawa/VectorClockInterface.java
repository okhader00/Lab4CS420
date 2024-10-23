package maekawa;

public interface VectorClockInterface {
	int[] getClock();
    void increment(int processId);
    void update(int[] remoteClock);
}