package s21.datasource.model;

import java.util.UUID;

public class StatisticModelEntity {
    UUID userId;
    double winPercentage;

    public StatisticModelEntity() {
    }

    public StatisticModelEntity(UUID uuid, double winPercentage) {
        this.userId = uuid;
        this.winPercentage = winPercentage;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public double getWinPercentage() {
        return winPercentage;
    }

    public void setWinPercentage(double winPercentage) {
        this.winPercentage = winPercentage;
    }
}
