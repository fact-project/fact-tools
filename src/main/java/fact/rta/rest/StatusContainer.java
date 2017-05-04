package fact.rta.rest;

import java.io.File;
import java.time.OffsetDateTime;

import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

/**
 * Stores system information. Memory is stored as bytes.
 * Created by kai on 24.05.16.
 */
public final class StatusContainer{
    final long usedMemory;
    final long memoryLimit;
    final int availableProcessors;
    final long totalSpace;
    final long freeSpace;
    final double loadAverage;
    final OffsetDateTime timestamp;

    private StatusContainer(long usedMemory, long memoryLimit, int availableProcessors, long totalSpace, long freeSpace, double loadAverage){
        this.usedMemory = usedMemory;
        this.memoryLimit = memoryLimit;
        this.availableProcessors = availableProcessors;
        this.totalSpace = totalSpace;
        this.freeSpace = freeSpace;
        this.loadAverage = loadAverage;
        this.timestamp = OffsetDateTime.now();
    }


    public static StatusContainer create(){
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();

        long memoryLimit = runtime.maxMemory();

        int availableProcessors = runtime.availableProcessors();

        double loadAverage = getOperatingSystemMXBean().getSystemLoadAverage();
        long totalSpace = 0;
        long freeSpace = 0;
        File[] roots = File.listRoots();
        for (File root : roots) {
            totalSpace += root.getTotalSpace();
            freeSpace += root.getFreeSpace();
        }
        return new StatusContainer(usedMemory, memoryLimit, availableProcessors, totalSpace, freeSpace, loadAverage);
    }
    public static double GB = 1.0/(1024L * 1024L * 1024L);
    public static double MB = 1.0/(1024L * 1024L);
    public static double KB = 1.0/(1024L);
}