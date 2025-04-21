package s21.web.model;

public class GameFieldDTO {
    private final int[][] field;

    public GameFieldDTO(int[][] field) {
        this.field = field;
    }

    public int[][] getField() {
        return field;
    }
}
