package s21.domain.model;

import java.util.UUID;

public class StatisticModel {
    UUID userId;
    double winPercentage;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setWinPercentage(double winPercentage) {
        this.winPercentage = winPercentage;
    }

    public double getWinPercentage() {
        return winPercentage;
    }
}
