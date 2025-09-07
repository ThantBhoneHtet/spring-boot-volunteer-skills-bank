package group11.volunteer_skills_bank.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilteredActivitiesResponse {
    private List<ActivityResponse> activities;
    private Long total;
    private Boolean hasMore;
    private Integer limit;
    private Integer offset;
}
