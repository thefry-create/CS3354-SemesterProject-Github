import java.io.Serializable;
public class WorkloadCap implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer capMinutesPerDay;
    public WorkloadCap() { this.capMinutesPerDay = 480; }
    public WorkloadCap(Integer capMinutesPerDay) { this.capMinutesPerDay = capMinutesPerDay; }
    public Integer getCapMinutesPerDay() { return capMinutesPerDay; }
    public void setCapMinutesPerDay(Integer capMinutesPerDay) { this.capMinutesPerDay = capMinutesPerDay; }
}