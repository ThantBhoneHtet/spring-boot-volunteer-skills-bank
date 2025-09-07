package group11.volunteer_skills_bank.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityStatsResponse {
    private Long totalActivities;
    private Long todayActivities;
    private Long weeklyActivities;
    private Long monthlyActivities;
    private Map<String, Long> activityTypes;
}
