package dai.boot.projeto.controller;

public class AlertStateUpdateRequest {
    private String newStatus;
    private String recommendedActionsJson;
    private String username;

    public AlertStateUpdateRequest() {}

    public AlertStateUpdateRequest(String newStatus, String recommendedActionsJson, String username) {
        this.newStatus = newStatus;
        this.recommendedActionsJson = recommendedActionsJson;
        this.username = username;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getRecommendedActionsJson() {
        return recommendedActionsJson;
    }

    public void setRecommendedActionsJson(String recommendedActionsJson) {
        this.recommendedActionsJson = recommendedActionsJson;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
