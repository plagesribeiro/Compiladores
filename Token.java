public class Token {
    public String lexeme;
    public byte tag;
    private byte size;

    public Token(String lexeme, byte tag) {
        this.lexeme = lexeme;
        this.tag = tag;
    }

    public Token() {
        this.lexeme = "";
        this.tag = 0;
    }

    public String toString() {
        return "" + lexeme;
    }
}
