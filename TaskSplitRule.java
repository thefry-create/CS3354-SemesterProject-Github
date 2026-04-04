import java.io.Serializable;
public class TaskSplitRule implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer blockCount;
    private Boolean rolloverEnabled;
    public TaskSplitRule() {}
    public TaskSplitRule(Integer blockCount, Boolean rolloverEnabled) {
        this.blockCount = blockCount;
        this.rolloverEnabled = rolloverEnabled;
    }
    public Integer getBlockCount() { return blockCount; }
    public void setBlockCount(Integer blockCount) { this.blockCount = blockCount; }
    public Boolean getRolloverEnabled() { return rolloverEnabled; }
    public void setRolloverEnabled(Boolean rolloverEnabled) { this.rolloverEnabled = rolloverEnabled; }
}