import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
public class WeeklyAvailability implements Serializable {
    private static final long serialVersionUID = 1L;
    private String availabilityId;
    private RepeatMode repeatMode;
    private Integer repeatDurationWeeks;
    private final List<TimeBlock> timeBlocks = new ArrayList<>();
    private final Map<DayOfWeek, Integer> availableHoursPerDay = new EnumMap<>(DayOfWeek.class);
    public WeeklyAvailability() {
        this.repeatMode = RepeatMode.THIS_WEEK_ONLY;
        this.repeatDurationWeeks = 1;
        for (DayOfWeek day : DayOfWeek.values()) {
            availableHoursPerDay.put(day, 8);
        }
    }
    public void addTimeBlock(TimeBlock block) { if (block != null) { timeBlocks.add(block); } }
    public void removeTimeBlock(TimeBlock block) { timeBlocks.remove(block); }
    public int getHoursForDay(DayOfWeek day) { return availableHoursPerDay.getOrDefault(day, 8); }
    public void setHoursForDay(DayOfWeek day, int hours) { availableHoursPerDay.put(day, Math.max(0, hours)); }
    public Map<DayOfWeek, Integer> getAvailableHoursPerDay() { return availableHoursPerDay; }
    public RepeatMode getRepeatMode() { return repeatMode; }
    public void setRepeatMode(RepeatMode repeatMode) { this.repeatMode = repeatMode; }
    public Integer getRepeatDurationWeeks() { return repeatDurationWeeks; }
    public void setRepeatDurationWeeks(Integer repeatDurationWeeks) { this.repeatDurationWeeks = repeatDurationWeeks; }
}