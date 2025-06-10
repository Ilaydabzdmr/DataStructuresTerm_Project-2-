import java.io.*;
import java.util.*;

public class Simulation {
    private int MAX_TICKS;
    private int QUEUE_CAPACITY;
    private int TERMINAL_ROTATION_INTERVAL;
    private int PARCEL_PER_TICK_MIN;
    private int PARCEL_PER_TICK_MAX;
    private double MISROUTING_RATE;
    private String[] CITY_LIST;

    private ArrivalBuffer arrivalBuffer;
    private ReturnStack returnStack;
    private DestinationSorter destinationSorter;
    private ParcelTracker parcelTracker;
    private TerminalRotator terminalRotator;

    private int tick = 0;
    private int parcelCounter = 0;

    private Random random = new Random();
    private BufferedWriter logWriter;
    private BufferedWriter reportWriter;

    // BONUS: Timeline for active terminals and dispatch counts
    private Map<Integer, String> terminalTimeline = new TreeMap<>();
    private Map<Integer, Integer> dispatchTimeline = new TreeMap<>();

    public Simulation(String configFile) {
        readConfig(configFile);
        initializeStructures();
    }

    private void readConfig(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("MAX TICKS")) MAX_TICKS = Integer.parseInt(line.split("=")[1].trim());
                else if (line.startsWith("QUEUE CAPACITY")) QUEUE_CAPACITY = Integer.parseInt(line.split("=")[1].trim());
                else if (line.startsWith("TERMINAL ROTATION INTERVAL")) TERMINAL_ROTATION_INTERVAL = Integer.parseInt(line.split("=")[1].trim());
                else if (line.startsWith("PARCEL PER TICK MIN")) PARCEL_PER_TICK_MIN = Integer.parseInt(line.split("=")[1].trim());
                else if (line.startsWith("PARCEL PER TICK MAX")) PARCEL_PER_TICK_MAX = Integer.parseInt(line.split("=")[1].trim());
                else if (line.startsWith("MISROUTING RATE")) MISROUTING_RATE = Double.parseDouble(line.split("=")[1].trim());
                else if (line.startsWith("CITY LIST")) {
                    CITY_LIST = line.split("=")[1].split(",");
                    for (int i = 0; i < CITY_LIST.length; i++)
                        CITY_LIST[i] = CITY_LIST[i].trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read config file: " + e.getMessage());
        }
    }

    private void initializeStructures() {
        arrivalBuffer = new ArrivalBuffer(QUEUE_CAPACITY);
        returnStack = new ReturnStack();
        destinationSorter = new DestinationSorter();
        parcelTracker = new ParcelTracker(100);
        terminalRotator = new TerminalRotator();
        terminalRotator.initializeFromCityList(CITY_LIST);

        try {
            logWriter = new BufferedWriter(new FileWriter("log.txt"));
            reportWriter = new BufferedWriter(new FileWriter("report.txt"));
        } catch (IOException e) {
            System.err.println("Failed to initialize log files.");
        }
    }

    public void run() {
        while (tick < MAX_TICKS) {
            tick++;
            log("[Tick " + tick + "]");

            generateParcels();
            sortParcelsFromQueue();
            dispatchParcels();
            reprocessReturnedParcels();

            if (tick % TERMINAL_ROTATION_INTERVAL == 0) {
                terminalRotator.advanceTerminal();
                log("Rotated to terminal: " + terminalRotator.getActiveTerminal());
            }

            terminalTimeline.put(tick, terminalRotator.getActiveTerminal());

            log("Queue size: " + arrivalBuffer.size());
            log("ReturnStack size: " + returnStack.size());
            log("");
        }

        writeReport();
        parcelTracker.exportToFile("parceltracker_export.txt"); // BONUS: full table export
        closeLogs();
    }

    private void generateParcels() {
        int n = PARCEL_PER_TICK_MIN + random.nextInt(PARCEL_PER_TICK_MAX - PARCEL_PER_TICK_MIN + 1);
        for (int i = 0; i < n; i++) {
            String id = "P" + (++parcelCounter);
            String city = CITY_LIST[random.nextInt(CITY_LIST.length)];
            int priority = 1 + random.nextInt(3);
            String size = switch (random.nextInt(3)) {
                case 0 -> "Small";
                case 1 -> "Medium";
                default -> "Large";
            };

            Parcel p = new Parcel(id, city, priority, size, tick, "InQueue");
            p.status = "InQueue"; // ✅ DÜZELTME: Başlangıç durumu
            arrivalBuffer.enqueue(p);
            parcelTracker.insert(p);
            log("Generated: " + id + " to " + city + " (Priority " + priority + ")");
        }
    }

    private void sortParcelsFromQueue() {
        while (!arrivalBuffer.isEmpty()) {
            Parcel p = arrivalBuffer.dequeue();
            p.status = "Sorted";
            parcelTracker.updateStatus(p.parcelID, "Sorted");
            destinationSorter.insertParcel(p);
            log("Sorted to BST: " + p.parcelID + " → " + p.destinationCity);
        }
    }

    private void dispatchParcels() {
        String terminal = terminalRotator.getActiveTerminal();
        LinkedList<Parcel> cityParcels = destinationSorter.getCityParcels(terminal);
        int dispatchCount = 0;

        if (cityParcels != null && !cityParcels.isEmpty()) {
            Parcel p = cityParcels.poll();
            if (random.nextDouble() < MISROUTING_RATE) {
                if (parcelTracker.getReturnCount(p.parcelID) >= 3) {
                    log(">>> REJECTED: " + p.parcelID + " exceeded max retries and was discarded.");
                } else {
                    p.status = "Returned";
                    parcelTracker.updateStatus(p.parcelID, "Returned");
                    parcelTracker.incrementReturnCount(p.parcelID);
                    returnStack.push(p);
                    log(">>> RETURNED (misrouted): " + p.parcelID);
                }
            } else {
                p.status = "Dispatched";
                parcelTracker.updateStatus(p.parcelID, "Dispatched");
                parcelTracker.setDispatchTick(p.parcelID, tick);
                destinationSorter.removeParcel(terminal, p.parcelID);
                log("Dispatched: " + p.parcelID + " to " + terminal);
                dispatchCount++;
            }
        }

        dispatchTimeline.put(tick, dispatchTimeline.getOrDefault(tick, 0) + dispatchCount);
    }

    private void reprocessReturnedParcels() {
        if (tick % 3 == 0 && !returnStack.isEmpty()) {
            int max = Math.min(2, returnStack.size());
            for (int i = 0; i < max; i++) {
                Parcel p = returnStack.pop();
                p.status = "Sorted";
                parcelTracker.updateStatus(p.parcelID, "Sorted");
                destinationSorter.insertParcel(p);
                log(">>> REPROCESSED from ReturnStack: " + p.parcelID);
            }
        }
    }

    private void writeReport() {
        try {
            reportWriter.write("=== ParcelSortX Simulation Report ===\n");
            reportWriter.write("Total Ticks Executed: " + tick + "\n");
            reportWriter.write("Total Parcels Generated: " + parcelCounter + "\n");
            reportWriter.write("Final Queue Size: " + arrivalBuffer.size() + "\n");
            reportWriter.write("Final ReturnStack Size: " + returnStack.size() + "\n");
            reportWriter.write("Maximum Queue Size Seen: " + arrivalBuffer.getMaxSeen() + "\n");
            reportWriter.write(String.format("Average Parcel Wait Time: %.2f ticks\n", parcelTracker.getAverageWaitTime()));

            // BONUS: Histogram
            reportWriter.write("\nPer-City Parcel Counts (Histogram):\n");
            for (String city : CITY_LIST) {
                int count = destinationSorter.countCityParcels(city);
                reportWriter.write(String.format("  %-10s (%2d): ", city, count));
                for (int i = 0; i < count; i++) reportWriter.write("*");
                reportWriter.write("\n");
            }

            // BONUS: Terminal Timeline
            reportWriter.write("\n--- Terminal Timeline ---\n");
            for (Map.Entry<Integer, String> entry : terminalTimeline.entrySet()) {
                int t = entry.getKey();
                String terminal = entry.getValue();
                int dispatched = dispatchTimeline.getOrDefault(t, 0);
                reportWriter.write("Tick " + t + ": Terminal = " + terminal + ", Dispatched = " + dispatched + "\n");
            }

            // BONUS: Export info
            reportWriter.write("\n(Full ParcelTracker table exported to parceltracker_export.txt)\n");

        } catch (IOException e) {
            System.err.println("Failed to write report.");
        }
    }

    private void closeLogs() {
        try {
            logWriter.close();
            reportWriter.close();
        } catch (IOException e) {
            System.err.println("Error closing log files.");
        }
    }

    private void log(String line) {
        try {
            logWriter.write(line + "\n");
        } catch (IOException e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }
}
