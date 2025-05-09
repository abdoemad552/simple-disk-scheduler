import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {
    enum OptionKey {
        CYLINDERS("cylinders"),
        QUEUE("queue"),
        RANDOM("random"),
        ALGORITHM("algorithm"),
        INIT("init"),
        DIRECTION("direction");
        
        private final String key;
        
        OptionKey(String key) {
            this.key = key;
        }
        
        public String getKey() { return key; }
    }
    
    enum Algorithm {
        FCFS("fcfs"),
        SCAN("scan"),
        CSCAN("cscan");
        
        private final String name;
        
        Algorithm(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
    }
    
    public record Result(int totalSeekTime, List<Integer> order) {
        @Override
        public String toString() {
            return "Total seek time: %s\nOrder: %s"
                .formatted(totalSeekTime, order) ;
        }
    }
    
    private static final int ARGS_COUNT = 6;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_SUCCESS = 1;
    
    private static final Scanner input = new Scanner(System.in);
    private static final Map<String, String> options = new HashMap<>();
    
    public static void main(String[] args) {
        
        if (args.length != ARGS_COUNT) {
            System.err.printf("Invalid args count\n");
            System.exit(EXIT_FAILURE);
        }
        
        for (String arg : args) {
            String[] option = arg.split("=");
            options.put(option[0], option[1]);
        }
        
        for (OptionKey optionKey : OptionKey.values()) {
            if (!options.containsKey(optionKey.getKey())) {
                System.err.printf("Missing arg: '%s'\n", optionKey.getKey());
                System.exit(EXIT_FAILURE);
            }
        }
        
        String algorithm = options.get(OptionKey.ALGORITHM.getKey()).toLowerCase();
        String direction = options.get(OptionKey.DIRECTION.getKey()).toLowerCase();
        int cylinders = Integer.parseInt(options.get(OptionKey.CYLINDERS.getKey()));
        int queue = Integer.parseInt(options.get(OptionKey.QUEUE.getKey()));
        int init = Integer.parseInt(options.get(OptionKey.INIT.getKey()));
        boolean random = Boolean.parseBoolean(options.get(OptionKey.RANDOM.getKey()));
        
        ArrayList<Integer> cylindersQueue = new ArrayList<>();
        if (random) {
            Random rand = new Random();
            for (int i = 0; i < queue; i++) {
                cylindersQueue.set(i, rand.nextInt(cylinders));
            }
        } else {
            while (true) {
                try {
                    System.out.printf("Enter values in cylinders queue: ");
                    cylindersQueue = Arrays.stream(input.nextLine().split(" "))
                        .map(Integer::parseInt)
                        .collect(Collectors.toCollection(ArrayList::new));
                    break;
                } catch(NumberFormatException e) {
                    System.out.printf("Please enter valid cylinders values\n");
                }
            }
        }
        
        System.out.printf("Cylinders queue: %s\n", cylindersQueue);
        
        Result result = null;
        if (algorithm.equals(Algorithm.FCFS.getName())) {
            result = executeFCFS(init, cylindersQueue, cylinders);
        } else if (algorithm.equals(Algorithm.SCAN.getName())) {
            result = executeSCAN(init, cylindersQueue, cylinders, direction);
        } else if (algorithm.equals(Algorithm.CSCAN.getName())) {
            result = executeCSCAN(init, cylindersQueue, cylinders, direction);
        } else {
            System.err.printf("%s is not a valid algorithm\n".formatted(algorithm));
            System.exit(EXIT_FAILURE);
        }
        
        System.out.printf("%s\n", result);
    }
    
    public static Result executeFCFS(int init, ArrayList<Integer> queue, int cylinders) {
        int totalSeekTime = 0;
        ArrayList<Integer> order = new ArrayList<>();
        
        int prev = init;
        for (int cylinder : queue) {
            order.add(prev);
            totalSeekTime += Math.abs(cylinder - prev);
            prev = cylinder;
        }
        
        order.add(prev);
        
        return new Result(totalSeekTime, order);
    }
    
    public static Result executeSCAN(int init, ArrayList<Integer> queue, int cylinders, String direction) {
        queue.sort(Integer::compareTo);
        
        int totalSeekTime = 0;
        ArrayList<Integer> order = new ArrayList<>();
        
        int prev = init;
        order.add(prev);
        
        if (direction.equals("top")) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i) > init) {
                    totalSeekTime += Math.abs(queue.get(i) - prev);
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
            
            if (prev != cylinders - 1) {
                totalSeekTime += (cylinders - 1) - prev;
                prev = cylinders - 1;
                order.add(prev);
            }
            
            for (int i = queue.size() - 1; i >= 0; i--) {
                if (queue.get(i) < init) {
                    totalSeekTime += Math.abs(prev - queue.get(i));
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
        } else if (direction.equals("down")) {
            for (int i = queue.size() - 1; i >= 0; i--) {
                if (queue.get(i) < init) {
                    totalSeekTime += Math.abs(prev - queue.get(i));
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
            
            if (prev != 0) {
                totalSeekTime += prev;
                prev = 0;
                order.add(prev);
            }
            
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i) > init) {
                    totalSeekTime += Math.abs(queue.get(i) - prev);
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
        } else {
            System.err.printf("%s is not a valid direction\n".formatted(direction));
            System.exit(EXIT_FAILURE);
        }
        
        return new Result(totalSeekTime, order);
    }
    
    public static Result executeCSCAN(int init, ArrayList<Integer> queue, int cylinders, String direction) {
        queue.sort(Integer::compareTo);
        
        int totalSeekTime = 0;
        ArrayList<Integer> order = new ArrayList<>();
        
        int prev = init;
        order.add(prev);
        
        if (direction.equals("top")) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i) > init) {
                    totalSeekTime += queue.get(i) - prev;
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
            
            if (prev != cylinders - 1) {
                totalSeekTime += (cylinders - 1) - prev;
                prev = cylinders - 1;
                order.add(prev);
            }
            
            totalSeekTime += prev;
            prev = 0;
            order.add(prev);
            
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i) < init) {
                    totalSeekTime += queue.get(i) - prev;
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
            
        } else if (direction.equals("down")) {
            for (int i = queue.size() - 1; i >= 0; i--) {
                if (queue.get(i) < init) {
                    totalSeekTime += prev - queue.get(i);
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
            
            if (prev != 0) {
                totalSeekTime += prev;
                prev = 0;
                order.add(prev);
            }
            
            totalSeekTime += (cylinders - 1);
            prev = cylinders - 1;
            order.add(prev);
            
            for (int i = queue.size() - 1; i >= 0; i--) {
                if (queue.get(i) > init) {
                    totalSeekTime += prev - queue.get(i);
                    prev = queue.get(i);
                    order.add(prev);
                }
            }
        } else {
            System.err.printf("%s is not a valid direction\n".formatted(direction));
            System.exit(EXIT_FAILURE);
        }
        
        return new Result(totalSeekTime, order);
    }
}
