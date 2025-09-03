package group11.volunteer_skills_bank.dto.opportunity;

public class OpportunityStatusUpdateRequest {
    private String status;

    public OpportunityStatusUpdateRequest() {}

    public OpportunityStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
