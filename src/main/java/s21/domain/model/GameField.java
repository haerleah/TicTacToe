package s21.domain.model;

public class GameField {
    private int[][] field;

    public GameField() {
        field = new int[3][3];
    }

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }
}
