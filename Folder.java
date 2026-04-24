import java.io.Serializable;
import java.util.UUID;
public class Folder implements Serializable {
    private static final long serialVersionUID = 1L;
    private String folderId;
    private String name;
    public Folder() { this.folderId = UUID.randomUUID().toString(); }
    public Folder(String name) { this(); this.name = name; }
    public String getFolderId() { return folderId; }
    public String getName() { return name; }
}