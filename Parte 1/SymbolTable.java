import java.util.Hashtable;

public class SymbolTable {
    private Hashtable<String, Token> table;

    public SymbolTable() {
        this.table = new Hashtable<String, Token>();
    }

    public Token insertToken(String lexeme, Token t) {
        return table.put(lexeme, t);
    }

    public Token findToken(String lexeme) {
        return table.get(lexeme);
    }

    public void listTable() {
        System.out.println(table.toString());
    }
}