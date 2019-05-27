package ejercicio_1_6;

public class Box {

    private final float arrivalTime;
    private final float min_unload_time;
    private final float max_unload_time;
    
    
    public Box(float arrivalTime, float CONVEYOR_LENGTH, float CONVEYOR_VELOCITY, float ARM_LENGTH) {
        this.arrivalTime = arrivalTime;
        this.min_unload_time = arrivalTime + (CONVEYOR_LENGTH - ARM_LENGTH) / CONVEYOR_VELOCITY;
        this.max_unload_time = arrivalTime + (CONVEYOR_LENGTH + ARM_LENGTH) / CONVEYOR_VELOCITY;
        /*
        System.out.println("Box created");
        System.out.println(this.arrivalTime);
        System.out.println(this.min_unload_time);
        System.out.println(this.max_unload_time);
        */
        
    }

    public float getArrival_time() {
        return arrivalTime;
    }

    public float getMin_unload_time() {
    	return min_unload_time;
    }
    
    public float getMax_unload_time() {
    	return max_unload_time;
    }
    
    public float timeInSystem(float currentTime) {
        return currentTime - arrivalTime;
    }
}
