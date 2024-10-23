package maekawa;

public class VectorClock implements VectorClockInterface{
	private int[] clock;

    public VectorClock() {
        this.clock = new int[4]; //For 4 processes
    }

    @Override
    public int[] getClock() {
        return clock.clone();
    }

    @Override
    public void increment(int processId) {
        clock[processId]++;
    }

    @Override
    public void update(int[] remoteClock) {
        for (int i = 0; i < clock.length; i++) {
            clock[i] = Math.max(clock[i], remoteClock[i]);
        }
    }
}
