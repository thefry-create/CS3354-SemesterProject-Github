import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
public final class StorageUtil {
    private StorageUtil() {}
    public static void saveObject(String fileName, Object object) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(object);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save data to " + fileName, e);
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T loadObject(String fileName, T defaultValue) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return defaultValue;
        }
    }
}