public class CustomTask extends Task {
    private static final long serialVersionUID = 1L;
    private Integer customPriority;
    public CustomTask() { super(); setType(TaskType.CUSTOM); }
    public CustomTask(Integer customPriority) { this(); this.customPriority = customPriority; }
    public int getCustomPriorityValue() { return customPriority == null ? 0 : customPriority; }
    public Integer getCustomPriority() { return customPriority; }
    public void setCustomPriority(Integer customPriority) { this.customPriority = customPriority; }
}