public class Token {
    public String lexeme;
    private byte tag;
    public boolean isReserved;

    public Token(String lexeme) {
        this.lexeme = lexeme;
        this.tag = (byte) lexeme.hashCode();
        this.isReserved = false;
    }

    public Token(String lexeme, boolean isReserved) {
        this.lexeme = lexeme;
        this.tag = (byte) lexeme.hashCode();
        this.isReserved = true;
    }

    public String toString() {
        return "" + lexeme;
    }
}
