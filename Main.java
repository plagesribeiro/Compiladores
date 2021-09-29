import java.util.Hashtable;

import java.io.IOException;

// T06 -> problema do Gonza!
public class Main {
    public static void main(String[] args) throws IOException {
        // runLexerDebug();
        // runLexer();
        runParser();
    }

    public static void runParser() throws IOException {
        Parser p = new Parser();
        p.S();
    }

    public static void runLexerDebug() throws IOException {
        Lexer lexer = new Lexer();
        Token t;
        do {
            t = lexer.scan();
            if (t != null) {
                System.out.println(t.toString());
            }
        } while (t != null);
    }

    public static void runLexer() throws IOException {
        Lexer lexer = new Lexer();
        Token t;
        do {
            t = lexer.scan();
        } while (t != null);
    }
}

class Parser {
    private Lexer lexer;
    private Token token;

    public Parser() throws IOException {
        this.lexer = new Lexer();
    }

    void readNextToken() {
        try {
            token = lexer.scan();
            if (token == null) {
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void exitError() {
        // PODE ENTREGAR FIM DE ARQUIVO NAO ESPERADO
        String lexeme = token.lexeme;
        System.out.println(lexer.line + "\ntoken nao esperado [" + lexeme + "].");

        // System.out.println("I'm in line #" + new
        // Exception().getStackTrace()[1].getLineNumber());
        System.exit(1);
    }

    public void S() {
        do {
            readNextToken();
            if (!Declaracao() && !Comandos()) {
                exitError();
            }
        } while (true);
    }

    boolean Declaracao() {
        if (token.tag == Tag.CHAR || token.tag == Tag.INT || token.tag == Tag.STRING || token.tag == Tag.FLOAT) {
            readNextToken();
            if (ListaDeIds()) {
                if (token.tag == Tag.SEMICOLON) {
                    return true;
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        } else if (token.tag == Tag.CONST) {
            readNextToken();
            if (token.tag == Tag.ID) {
                readNextToken();
                if (token.tag == Tag.EQ) {
                    readNextToken();
                    if (Expressao()) {
                        if (token.tag == Tag.SEMICOLON) {
                            return true;
                        } else {
                            exitError();
                        }
                    } else {
                        exitError();
                    }
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        }
        return false;
    }

    boolean Comandos() {
        if (token.tag == Tag.OPEN_BRACE) {
            do {
                readNextToken();
            } while (Comando());
            if (token.tag == Tag.CLOSE_BRACE) {
                return true;
            } else {
                exitError();
            }
        } else if (Comando()) {
            return true;
        }
        return false;
    }

    boolean Comando() {
        if (!Atribuicao() && !Repeticao() && !Teste() && !Leitura() && !Escrita()) {
            return true;
        }
        if (token.tag == Tag.SEMICOLON) {
            return true;
        } else {
            exitError();
        }
        return false;

    }

    boolean ListaDeIds() {
        if (Di()) {
            do {
                if (token.tag == Tag.COMMA) {
                    readNextToken();
                    if (!Di()) {
                        exitError();
                    }
                } else {
                    return true;
                }
            } while (true);
        }
        return false;
    }

    boolean Di() {
        if (token.tag == Tag.ID) {
            readNextToken();
            if (token.tag == Tag.ASSIGN) {
                readNextToken();
                if (token.tag == Tag.VALUE_CHAR || token.tag == Tag.VALUE_FLOAT || token.tag == Tag.VALUE_STRING
                        || token.tag == Tag.VALUE_INT) {
                    readNextToken();
                    return true;
                } else {
                    exitError();
                }
            }
            return true;

        }
        return false;
    }

    boolean Atribuicao() {
        if (token.tag == Tag.ID) {
            readNextToken();
            if (token.tag == Tag.OPEN_BRACKET) {
                readNextToken();
                if (Expressao()) {
                    if (token.tag == Tag.CLOSE_BRACKET) {
                        return true;
                    } else {
                        exitError();
                    }
                } else {
                    exitError();
                }
            }
            if (token.tag == Tag.ASSIGN) {
                readNextToken();
                if (Expressao()) {
                    return true;
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        }
        return false;
    }

    boolean Repeticao() {
        if (token.tag == Tag.WHILE) {
            readNextToken();
            if (Expressao()) {
                if (Comandos()) {
                    return true;
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        }
        return false;
    }

    boolean Teste() {
        if (token.tag == Tag.IF) {
            readNextToken();
            if (Comandos()) {
                readNextToken();
                if (token.tag == Tag.ELSE) {
                    readNextToken();
                    if (Comandos()) {
                        return true;
                    } else {
                        exitError();
                    }
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        }
        return false;
    }

    boolean Leitura() {
        if (token.tag == Tag.READLN) {
            readNextToken();
            if (token.tag == Tag.OPEN_PARENTHESIS) {
                readNextToken();
                if (token.tag == Tag.ID || Expressao()) {
                    readNextToken();
                    if (token.tag == Tag.CLOSE_PARENTHESIS) {
                        readNextToken();
                        return true;
                    } else {
                        exitError();
                    }
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        }
        return false;
    }

    boolean Escrita() {
        if (token.tag == Tag.WRITE || token.tag == Tag.WRITELN) {
            readNextToken();
            if (token.tag == Tag.OPEN_PARENTHESIS) {
                readNextToken();
                if (Expressao()) {
                    do {
                        if (token.tag == Tag.COMMA) {
                            readNextToken();
                            if (!Expressao()) {
                                exitError();
                            }
                        } else {
                            if (token.tag == Tag.CLOSE_PARENTHESIS) {
                                readNextToken();
                                return true;
                            } else {
                                exitError();
                            }
                        }
                    } while (true);
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        }
        return false;
    }

    boolean Expressao() {
        if (ExpS()) {
            if (Comp()) {
                readNextToken();
                if (ExpS()) {
                    return true;
                } else {
                    exitError();
                }
            } else {
                return true;
            }
        } else {
            exitError();
        }

        return false;
    }

    boolean Comp() {
        return token.tag == Tag.EQ || token.tag == Tag.NOT_EQUAL || token.tag == Tag.LOWER || token.tag == Tag.GREATER
                || token.tag == Tag.LOWER_EQUAL || token.tag == Tag.GREATER_EQUAL;
    }

    boolean ExpS() {
        if (token.tag == Tag.MINUS || token.tag == Tag.PLUS) {
            readNextToken();
        }
        if (T()) {
            do {
                if (token.tag == Tag.MINUS || token.tag == Tag.PLUS || token.tag == Tag.OR) {
                    readNextToken();
                    if (!T()) {
                        exitError();
                    }
                } else {
                    return true;
                }
            } while (true);
        } else {
            exitError();
        }
        return false;

    }

    boolean T() {
        if (F()) {
            do {
                if (token.tag == Tag.MULTIPLY || token.tag == Tag.SLASH_FORWARD || token.tag == Tag.AND
                        || token.tag == Tag.DIV || token.tag == Tag.MOD) {
                    readNextToken();
                    if (!F()) {
                        exitError();
                    }
                } else {
                    return true;
                }
            } while (true);
        } else {
            exitError();
        }
        return false;
    }

    boolean F() {
        if (token.tag == Tag.ID) {
            readNextToken();
            if (token.tag == Tag.OPEN_BRACKET) {
                readNextToken();
                if (Expressao()) {
                    if (token.tag == Tag.CLOSE_BRACKET) {
                        readNextToken();
                        return true;
                    } else {
                        exitError();
                    }
                } else {
                    exitError();
                }
            } else {
                return true;
            }

        } else if (token.tag == Tag.VALUE_CHAR || token.tag == Tag.VALUE_FLOAT || token.tag == Tag.VALUE_STRING
                || token.tag == Tag.VALUE_INT) {
            readNextToken();
            return true;

        } else if (P()) {
            return true;

        } else if (token.tag == Tag.INT) {
            readNextToken();
            if (P()) {
                return true;
            } else {
                exitError();
            }

        } else if (token.tag == Tag.FLOAT) {
            readNextToken();
            if (P()) {
                return true;
            } else {
                exitError();
            }

        }

        return false;
    }

    boolean P() {
        if (token.tag == Tag.OPEN_PARENTHESIS) {
            readNextToken();
            if (Expressao()) {
                if (token.tag == Tag.CLOSE_PARENTHESIS) {
                    readNextToken();
                    return true;
                } else {
                    exitError();
                }
            } else {
                exitError();
            }
        }
        return false;
    }
}

class Lexer {
    private String lexeme;
    public int line;
    private int c;
    public SymbolTable st;
    private int state;
    private boolean giveBack;

    public Lexer() {
        this.lexeme = "";
        this.line = 1;
        this.state = 1;
        this.giveBack = false;
        this.st = new SymbolTable();
        this.st.insertToken("const", new Token("const", Tag.CONST));
        this.st.insertToken("int", new Token("int", Tag.INT));
        this.st.insertToken("char", new Token("char", Tag.INT));
        this.st.insertToken("while", new Token("while", Tag.WHILE));
        this.st.insertToken("if", new Token("if", Tag.IF));
        this.st.insertToken("float", new Token("float", Tag.FLOAT));
        this.st.insertToken("else", new Token("else", Tag.ELSE));
        this.st.insertToken("&&", new Token("&&", Tag.AND));
        this.st.insertToken("||", new Token("||", Tag.OR));
        this.st.insertToken("!", new Token("!", Tag.NOT));
        this.st.insertToken("<-", new Token("<-", Tag.ASSIGN));
        this.st.insertToken("=", new Token("=", Tag.EQ));
        this.st.insertToken("(", new Token("(", Tag.OPEN_PARENTHESIS));
        this.st.insertToken(")", new Token(")", Tag.CLOSE_PARENTHESIS));
        this.st.insertToken("<", new Token("<", Tag.GREATER));
        this.st.insertToken(">", new Token(">", Tag.LOWER));
        this.st.insertToken("!=", new Token("!=", Tag.NOT_EQUAL));
        this.st.insertToken(">=", new Token(">=", Tag.GREATER_EQUAL));
        this.st.insertToken("<=", new Token("<=", Tag.LOWER_EQUAL));
        this.st.insertToken(",", new Token(",", Tag.COMMA));
        this.st.insertToken("+", new Token("+", Tag.PLUS));
        this.st.insertToken("-", new Token("-", Tag.MINUS));
        this.st.insertToken("*", new Token("*", Tag.MULTIPLY));
        this.st.insertToken("/", new Token("/", Tag.SLASH_FORWARD));
        this.st.insertToken(";", new Token(";", Tag.SEMICOLON));
        this.st.insertToken("{", new Token("{", Tag.OPEN_BRACE));
        this.st.insertToken("}", new Token("}", Tag.CLOSE_BRACE));
        this.st.insertToken("readln", new Token("readln", Tag.READLN));
        this.st.insertToken("div", new Token("div", Tag.DIV));
        this.st.insertToken("write", new Token("write", Tag.WRITE));
        this.st.insertToken("writeln", new Token("writeln", Tag.WRITELN));
        this.st.insertToken("mod", new Token("mod", Tag.MOD));
        this.st.insertToken("[", new Token("[", Tag.OPEN_BRACKET));
        this.st.insertToken("]", new Token("]", Tag.CLOSE_BRACKET));
        this.st.insertToken("string", new Token("string", Tag.STRING));
    }

    private int readch() throws IOException {
        return (int) System.in.read();
    }

    public Token scan() throws IOException {
        while (state != 5) {
            if (!giveBack && state != 5 && state != 23 && state != 24 && state != 25 && state != 26) {
                c = readch();
                if ((char) c == '\n') {
                    line++;
                }
            }
            switch (state) {
                case 1:
                    if (c == -1) {
                        state = 26;
                    } else if (c == '\n' || c == '\t' || c == '\r' || c == ' ') {
                        state = 1;
                    } else {
                        lexeme += (char) c;
                        state = checkStateFrom1(c);
                    }
                    break;
                case 2:
                    if ((char) c == 'x' || (char) c == 'X') {
                        lexeme += (char) c;
                        state = 3;
                    } else if (c == '.') {
                        lexeme += (char) c;
                        state = 11;
                    } else if (isDigit((char) c)) {
                        lexeme += (char) c;
                        state = 10;
                    } else {
                        state = 5;
                        giveBack = true;
                    }
                    break;
                case 3:
                    if (isHexValid((char) c)) {
                        lexeme += (char) c;
                        state = 4;
                    } else if (c == -1) {
                        state = 23;
                    } else if (isValid((char) c)) {
                        lexeme += (char) c;
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 4:
                    if (isHexValid((char) c)) {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 23;
                    } else if (isValid((char) c)) {
                        lexeme += (char) c;
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 6:
                    if (isValid((char) c) && c != '\n') {
                        state = 7;
                        lexeme += (char) c;
                    } else if (c == -1) {
                        state = 23;
                    } else if (isValid((char) c)) {
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 7:
                    if ((char) c == '\'') {
                        state = 5;
                        lexeme += (char) c;
                    } else if (c == -1) {
                        state = 23;
                    } else if (isValid((char) c)) {
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 8:
                    if ((char) c == '\"') {
                        lexeme += (char) c;
                        state = 5;
                    } else if (isValidStr((char) c)) {
                        lexeme += (char) c;
                        if (lexeme.length() > 256) {
                            state = 24;
                        }
                    } else if (c == -1) {
                        state = 23;
                    } else if (c == '\n') {
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 9:
                    if (isDigit((char) c)) {
                        state = 10;
                        lexeme += (char) c;
                    } else if ((char) c == '.') {
                        state = 11;
                        lexeme += (char) c;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 10:
                    if (isDigit((char) c)) {
                        lexeme += (char) c;
                    } else if ((char) c == '.') {
                        lexeme += (char) c;
                        state = 11;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 11:
                    if (isDigit((char) c)) {
                        lexeme += (char) c;
                        state = 12;
                    } else if (c == -1) {
                        state = 23;
                    } else if (isValid((char) c)) {
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 12:
                    if (isDigit((char) c)) {
                        lexeme += (char) c;
                        if (!checkValidPrecision()) {
                            state = 24;
                        }
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 13:
                    if ((char) c == '=') {
                        lexeme += (char) c;
                        state = 5;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 14:
                    if ((char) c == '-' || (char) c == '=') {
                        lexeme += (char) c;
                        state = 5;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 15:
                    if ((char) c == '=') {
                        lexeme += (char) c;
                        state = 5;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 16:
                    if ((char) c == '&') {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 23;
                    } else if (isValid((char) c)) {
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 17:
                    if ((char) c == '|') {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 23;
                    } else if (isValid((char) c)) {
                        state = 24;
                    } else {
                        state = 25;
                    }
                    break;
                case 18:
                    if (isLetter((char) c) || isDigit((char) c) || (char) c == '.' || (char) c == '_') {
                        lexeme += (char) c;
                        if (lexeme.length() > 32) {
                            state = 24;
                        }
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 19:
                    if ((char) c == '*') {
                        state = 20;
                        lexeme = "";
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 20:
                    if ((char) c == '*') {
                        state = 22;
                    } else if (isValid((char) c)) {
                        state = 21;
                    } else if (c == -1) {
                        state = 23;
                    } else {
                        state = 25;
                    }
                    break;
                case 21:
                    if ((char) c == '*') {
                        state = 22;
                    } else if (isValid((char) c)) {
                        break;
                    } else if (c == -1) {
                        state = 23;
                    } else {
                        state = 25;
                    }
                    break;
                case 22:
                    if ((char) c == '/') {
                        lexeme = "";
                        state = 1;
                    } else if (isValid((char) c)) {
                        state = 21;
                    } else if (c == -1) {
                        state = 23;
                    } else {
                        state = 25;
                    }
                    break;
                case 23:
                    errorEOFNotExpected();
                    return null;
                case 24:
                    if (c == '\n') {
                        line--;
                    }
                    errorNotIdentifiedLexeme(lexeme);
                    return null;
                case 25:
                    errorInvalidCharacter();
                    return null;
                case 26:
                    System.out.println(line + " linhas compiladas.");
                    return null;
            }
        }

        Token t = st.findToken(lexeme);
        if (t == null) {
            if (isLetter(lexeme.charAt(0)) || lexeme.charAt(0) == '_')
                t = st.insertToken(lexeme, new Token(lexeme, Tag.ID));
            else if (lexeme.charAt(0) == '\''
                    || (lexeme.length() > 2) && (lexeme.charAt(0) == '0' && lexeme.charAt(1) == 'x'))
                t = st.insertToken(lexeme, new Token(lexeme, Tag.VALUE_CHAR));
            else if (lexeme.charAt(0) == '"')
                t = st.insertToken(lexeme, new Token(lexeme, Tag.VALUE_STRING));
            else if (lexeme.contains("."))
                t = st.insertToken(lexeme, new Token(lexeme, Tag.VALUE_FLOAT));
            else
                t = st.insertToken(lexeme, new Token(lexeme, Tag.VALUE_INT));
        }

        lexeme = "";
        state = 1;
        if (giveBack) {
            if (c == -1) {
                state = 26;
            } else if ((c != '\n') && (char) c != ' ' && (char) c != '\r' && c != '\t') {
                lexeme += (char) c;
                state = checkStateFrom1((char) c);
            }
            giveBack = false;
        }
        return t;
    }

    private boolean checkValidPrecision() {
        int count = 0;
        for (int i = 0; i < lexeme.length(); i++) {
            if (lexeme.charAt(i) == '.') {
                for (int j = i + 1; j < lexeme.length(); j++) {
                    count++;
                    if (count > 6) {
                        return false;
                    }
                }
            }

        }
        return true;
    }

    private boolean isHexValid(char c) {
        String hex = "0123456789abcdefABCDEF";
        if (hex.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    private int checkStateFrom1(int c) {
        if ((char) c == -1) {
            return 26;
        } else if ((char) c == ' ' || (char) c == '\r' || c == '\n' || c == '\t') {
            return 1;
        } else if ((char) c == '0') { // Le char em hexa ou números iniciados em 0
            return 2;
        } else if (isDigit((char) c)) { // Le numeros decimais ou reais
            return 10;
        } else if ((char) c == '.') { // Le numeros reais iniciados com '.'
            return 11;
        } else if ((char) c == '-') { // Le numeros iniciados com '-'
            return 9;
        } else if ((char) c == '/') { // Le comentario ou token '/'
            return 19;
        } else if ((char) c == '"') { // Le String
            return 8;
        } else if ((char) c == '\'') { // Le char
            return 6;
        } else if (isToken((char) c)) { // Le tokens simples
            return 5;
        } else if ((char) c == '!') { // Le ! ou !=
            return 13;
        } else if ((char) c == '<') { // Le < <= ou <-
            return 14;
        } else if ((char) c == '>') { // Le > ou >=
            return 15;
        } else if ((char) c == '&') { // Le & ou &&
            return 16;
        } else if ((char) c == '|') { // Le | ou ||
            return 17;
        } else if (isLetter((char) c) || (char) c == '_') { // Le ID
            return 18;
        } else if (isValid((char) c)) {
            return 24;
        } else {
            return 25;
        }
    }

    private boolean isValid(char c) {
        String valid = " _.,;:()[]{}+-\"\'/|\\&%!?><=\n\r*";
        if (isDigit(c) || isLetter(c) || valid.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    private boolean isValidStr(char c) {
        if (isValid((char) c) && c != '\n' && c != '\r' && c != '$')
            return true;
        return false;
    }

    private boolean isToken(char c) {
        String valid = "=(),+*/;{}[]";
        if (valid.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    private boolean isDigit(char c) {
        String numbers = "0123456789";
        if (numbers.contains(Character.toString(c)))
            return true;
        return false;
    }

    public boolean isLetter(char c) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (letters.contains(Character.toString(c)))
            return true;
        return false;
    }

    void errorNotIdentifiedLexeme(String lexeme) {
        lexeme = lexeme.replace("\n", "");
        System.out.print(line + "\nlexema nao identificado [" + lexeme + "].");
    }

    void errorInvalidCharacter() {
        if (c == '\n')
            line--;
        System.out.print(line + "\ncaractere invalido.");
    }

    void errorEOFNotExpected() {
        System.out.print(line + "\nfim de arquivo nao esperado.");
    }
}

class SymbolTable {
    private Hashtable<String, Token> table;

    public SymbolTable() {
        this.table = new Hashtable<String, Token>();
    }

    public Token insertToken(String lexeme, Token t) {
        table.put(lexeme, t);
        return t;
    }

    public Token findToken(String lexeme) {
        return table.get(lexeme);
    }

    public void listTable() {
        System.out.println(table.toString());
    }
}

class Token {
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

class Tag {
    public final static byte CONST = 1;
    public final static byte INT = 2;
    public final static byte CHAR = 3;
    public final static byte WHILE = 4;
    public final static byte IF = 5;
    public final static byte FLOAT = 6;
    public final static byte ELSE = 7;
    public final static byte AND = 8;
    public final static byte OR = 9;
    public final static byte NOT = 10;
    public final static byte ASSIGN = 11;
    public final static byte EQ = 12;
    public final static byte OPEN_PARENTHESIS = 13;
    public final static byte CLOSE_PARENTHESIS = 14;
    public final static byte LOWER = 15;
    public final static byte GREATER = 16;
    public final static byte NOT_EQUAL = 17;
    public final static byte GREATER_EQUAL = 18;
    public final static byte LOWER_EQUAL = 19;
    public final static byte COMMA = 20;
    public final static byte PLUS = 21;
    public final static byte MINUS = 22;
    public final static byte MULTIPLY = 23;
    public final static byte SLASH_FORWARD = 24;
    public final static byte SEMICOLON = 25;
    public final static byte OPEN_BRACE = 26;
    public final static byte CLOSE_BRACE = 27;
    public final static byte READLN = 28;
    public final static byte WRITELN = 29;
    public final static byte MOD = 30;
    public final static byte OPEN_BRACKET = 31;
    public final static byte CLOSE_BRACKET = 32;
    public final static byte ID = 33;
    public final static byte DIV = 34;
    public final static byte WRITE = 35;
    public final static byte STRING = 37;
    public final static byte VALUE_CHAR = 38;
    public final static byte VALUE_STRING = 39;
    public final static byte VALUE_INT = 40;
    public final static byte VALUE_FLOAT = 41;
}