import java.io.Serializable;
public class Msg implements Serializable {
    private static final long serialVersionUID = 1L;
    private String msg;
    public Msg() {}
    public Msg(String msg) { this.msg = msg; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public String toString() { return msg; }
}