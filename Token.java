public class Token {
    private String lexeme;
    private byte tag;

    public Token(String lexeme) {
        this.lexeme = lexeme;
        this.tag = (byte) lexeme.hashCode();
    }

    public String toString() {
        return "" + lexeme;
    }
}
