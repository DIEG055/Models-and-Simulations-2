package ejercicio_1_6;

import static simlib.SimLib.*;

import java.io.IOException;
import java.text.DecimalFormat;

import simlib.io.*;
import simlib.collection.*;

public class Ejercicio_1_6 {

    static final int BUSY = 1, IDLE = 0, STREAM_ARRIVE = 5, STREAM_DEPARTURE = 7;
    static final byte EVENT_ARRIVAL = 1, EVENT_DEPARTURE = 2, EVENT_END_SIMULATION = 3;
    static float MEAN_DISTANCE, FIXED_DISTANCE, CONVEYOR_VELOCITY, ARM_LENGTH, MEAN_UNLOAD_TIME, STD_UNLOAD_TIME, CONVEYOR_LENGTH;
    static int TOTAL_UNITS, X, Y; 
    static boolean EXPONENTIAL_CASE;
    static int batch_size;
    static SimReader reader;
    static SimWriter writer;
    static Queue<Box> conveyor;
    static Worker worker;
    
    
    public static void main(String[] args) throws IOException {
        reader = new SimReader("src/Ejercicio_1_6/Ejercicio_1_6.in");
        writer = new SimWriter("src/Ejercicio_1_6/Ejercicio_1_6.out");
        conveyor = new Queue<>("Conveyor of boxes");
        worker = new Worker();
        
        

        CONVEYOR_LENGTH = reader.readFloat();
        MEAN_DISTANCE = reader.readFloat();
        FIXED_DISTANCE = reader.readFloat();
        CONVEYOR_VELOCITY = reader.readFloat(); // se cambia a 0.129 y ya hay una caja que no pasa caso A. para caso B la velocidad es 0.04 para que no pase.
        ARM_LENGTH = reader.readFloat();
        MEAN_UNLOAD_TIME = reader.readFloat();
        STD_UNLOAD_TIME = reader.readFloat();
        TOTAL_UNITS = reader.readInt();
        
        EXPONENTIAL_CASE = (reader.readInt() == 1);
        
        batch_size = TOTAL_UNITS;
        
        writer.write(String.format("                SISTEMA DE BANDA TRANSPORTADORA        " + "\n"));
        writer.write(String.format("-------------------------------------------------------------------" + "\n\n"));
        
        
        writer.write(String.format("   Tamaño del lote (unidades)                   : %d\n\n", TOTAL_UNITS));
        
        writer.write(String.format("   Media del tiempo de descarga (seg)           : %.1f\n",  MEAN_UNLOAD_TIME));
        writer.write(String.format("   Desv. stand. del tiempo de descarga (seg)    : %.1f\n\n",  STD_UNLOAD_TIME));
        
        writer.write(String.format("   Velocidad de la banda transportadora (m/seg) : %.3f\n", CONVEYOR_VELOCITY));
        writer.write(String.format("   Largo de la banda transportadora (m)         : %.2f\n\n", CONVEYOR_LENGTH));
        
        writer.write(String.format("   Inciso a\n"));
        writer.write(String.format("   Distancia fija entre cajas (m)               : %.2f\n\n", FIXED_DISTANCE));
        writer.write(String.format("   Inciso b\n"));
        writer.write(String.format("   Distancia media entre cajas (m)              : %.2f\n\n\n", MEAN_DISTANCE));
        
        writer.write(String.format("   Variable EXPONENTIAL_CASE tiene el valor %b, por lo tanto\n", EXPONENTIAL_CASE));
        if (EXPONENTIAL_CASE)
        	writer.write(String.format("   se usará la distancia con dist. exponencial para inciso b)\n"));
        else
        	writer.write(String.format("   se usará la distancia fija entre cajas del inciso a)\n"));
        writer.write(String.format("-------------------------------------------------------------------" + "\n\n"));
        
        
        
        initSimlib();

        // Dependiento del problema a resolver (inciso a o b) se programa el primer arribo
        if (EXPONENTIAL_CASE)
        	eventSchedule( expon(MEAN_DISTANCE, STREAM_ARRIVE) / CONVEYOR_VELOCITY, EVENT_ARRIVAL);
        else
        	eventSchedule(FIXED_DISTANCE / CONVEYOR_VELOCITY, EVENT_ARRIVAL);
   
        
        do {
            // end of simulation
            if (batch_size == 0 && conveyor.size() == 0) {
            	eventType = EVENT_END_SIMULATION;
            } else {
                timing();
            }
            
            switch (eventType) {
                case EVENT_ARRIVAL:
                    arrive();
                    break;
                case EVENT_DEPARTURE:
                    depart();
                    break;
                case EVENT_END_SIMULATION:
                    report();
                    break;
            }
        } while (eventType != EVENT_END_SIMULATION);
        
        reader.close();
        writer.close();
    }

    static void arrive() {
        Box arrivingBox = new Box(simTime, CONVEYOR_LENGTH, CONVEYOR_VELOCITY, ARM_LENGTH);
    	conveyor.offer(arrivingBox);
    	
    	// Si existen unidades en el lote programar un nuevo arribo.  
        if (--batch_size > 0)
        	
        	// Dependiento del problema a resolver (inciso a o b) se programa el nuevo arribo
        	if (EXPONENTIAL_CASE) {
        		DecimalFormat df = new DecimalFormat("#.#####");
        		float n = expon(MEAN_DISTANCE, STREAM_ARRIVE);
            	eventSchedule(simTime + n / CONVEYOR_VELOCITY, EVENT_ARRIVAL);
        	} else
            	eventSchedule(simTime + FIXED_DISTANCE / CONVEYOR_VELOCITY, EVENT_ARRIVAL);
        
        // Retirar cajas que salieron de la banda 
        while(conveyor.peek().getMax_unload_time() < simTime) {
        	conveyor.poll();
        	Y++;
        }
        
        
        if (!worker.busy() && !worker.isWaiting())
        	// Verificar si el trabajador puede agarrar la caja
        	if (conveyor.peek().getMin_unload_time() <= simTime) {
        		Box unloadedBox = conveyor.poll();
        		X++;
        		worker.setState(BUSY);
        		eventSchedule(simTime +  Normal(MEAN_UNLOAD_TIME, STD_UNLOAD_TIME, STREAM_DEPARTURE), EVENT_DEPARTURE);
        	} else if (batch_size == 0) {
        		eventSchedule(conveyor.peek().getMin_unload_time() , EVENT_DEPARTURE);
        		worker.setWaitingForBox(true);
        	}
    }

    static void depart() {
    	// Retirar cajas que salieron de la banda 
        while(conveyor.size() != 0 && conveyor.peek().getMax_unload_time() < simTime) {
        	conveyor.poll();
        	Y++;
        }

    	
        if (conveyor.size() != 0) {
        	// Verificar si el trabajador puede agarrar la caja
        	if (conveyor.peek().getMin_unload_time() <= simTime) {
        		Box unloadedBox = conveyor.poll();
        		X++;
        		worker.setState(BUSY);
        		eventSchedule(simTime +  Normal(MEAN_UNLOAD_TIME, STD_UNLOAD_TIME, STREAM_DEPARTURE) , EVENT_DEPARTURE);
        		 
        		return;
        	} else { 
        		eventSchedule(conveyor.peek().getMin_unload_time() , EVENT_DEPARTURE);
        		worker.setWaitingForBox(true);
        		worker.setState(IDLE);
        	}
        } else {
	        worker.setState(IDLE);
	        worker.setWaitingForBox(false);
        }
    }

    
    static void report() throws IOException {    	
    	writer.write("                          DATA OUTPUT           " + "\n");
        writer.write("-------------------------------------------------------------------"+ "\n");
        
        writer.write("   Numero de cartones retirados              : " + X + "\n");
        writer.write("   Numero de cartones NO retirados           : " + Y + "\n\n");
        
        writer.write("   Numero de cartones retirados por hora (X) : " + X / simTime * 3600.0f + "\n\n");
        writer.write("   Numero de cartones que deja de  \n");
        writer.write("   descargar el empleado por hora (Y)        : " + Y / simTime * 3600.0f + "\n\n");
        writer.write("   Maximizar X - Y                           : " + (X - Y) / simTime * 3600.0f + "\n\n");        
        writer.write(String.format("   Tiempo simulacion : %.1f seg, equivalente a %.4f h\n\n", simTime , simTime / 3600.0f));
        writer.write("-------------------------------------------------------------------" + "\n");
    }
}
