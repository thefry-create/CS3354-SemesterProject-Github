import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
public class TimeBlock implements Serializable {
    private static final long serialVersionUID = 1L;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    public TimeBlock() {}
    public TimeBlock(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}