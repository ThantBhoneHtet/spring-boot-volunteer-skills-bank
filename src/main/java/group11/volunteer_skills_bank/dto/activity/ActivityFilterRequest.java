package group11.volunteer_skills_bank.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityFilterRequest {
    private String type;
    private String userId;
    private String adminId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Integer limit = 10;
    private Integer offset = 0;
}
