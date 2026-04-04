import java.io.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Test run starting...");

        Task task = new Task();
        task.setDescription("Homework");
        task.setType(TaskType.ASSIGNMENT);

        // Save to file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("task.ser"))) {
            out.writeObject(task);
            System.out.println("Task saved to file.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load from file
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("task.ser"))) {
            Task loadedTask = (Task) in.readObject();
            System.out.println("Loaded task: " + loadedTask.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}