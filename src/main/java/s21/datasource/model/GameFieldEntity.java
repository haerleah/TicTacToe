package s21.datasource.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GameFieldEntity {
    @Column(nullable = false)
    private String field;

    public GameFieldEntity() {
    }

    public GameFieldEntity(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
