import java.util.Hashtable;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        Parser p = new Parser();
        p.S();
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
        System.out.print(lexer.line + "\ntoken nao esperado [" + token.lexeme + "].");
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
        if (Atribuicao() || Repeticao() || Teste() || Leitura() || Escrita()) {
            readNextToken();
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
                readNextToken();
            } while (true);
        }
        return false;
    }

    boolean Di(){
        if(token.tag == Tag.ID){
            readNextToken();
            if(token.tag == Tag.ASSIGN){
                readNextToken();
                if(token.tag == Tag.VALUE_CHAR || token.tag == Tag.VALUE_FLOAT || token.tag == Tag.VALUE_STRING
                || token.tag == Tag.VALUE_INT){
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
                    readNextToken();
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
                readNextToken();
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
                readNextToken();
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
                    readNextToken();
                    if (token.tag == Tag.CLOSE_BRACKET) {
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
            return true;

            // } else if (!F()) {
            // return true;

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
                readNextToken();
                if (token.tag == Tag.CLOSE_PARENTHESIS) {
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
            if (!giveBack && state != 5) {
                c = readch();
                if (c == '\n') {
                    line++;
                }
            }
            switch (state) {
                case 1:
                    if (c == -1) {
                        System.out.println(line + " linhas compiladas.");
                        return null;
                    }
                    state = checkStateFrom1(c);
                    if (state == 1) {
                        c = readBlank();
                        state = checkStateFrom1(c);
                    } else if (state == 0) {
                        if (c != -1) {
                            lexeme += (char) c;
                        }
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    if (c != -1) {
                        lexeme += (char) c;
                    }
                    break;
                case 2:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == 'x') {
                        lexeme += (char) c;
                        state = 3;
                    } else if (c == '.') {
                        lexeme += (char) c;
                        state = 11;
                    } else if (isDigit((char) c)) {
                        lexeme += (char) c;
                        state = 10;
                    } else if ((char) c != '\n') {
                        state = 5;
                        giveBack = true;
                    }
                    break;
                case 3:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (isHexValid((char) c)) {
                        lexeme += (char) c;
                        state = 4;
                    } else {
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    break;
                case 4:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (isHexValid((char) c)) {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    } else {
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    break;
                case 6:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (isAsciiExt(c) && c != '\n') {
                        state = 7;
                        lexeme += (char) c;
                    } else {
                        errorInvalidCharacter();
                        return null;
                    }
                    break;
                case 7:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '\'') {
                        state = 5;
                        lexeme += (char) c;
                    } else {
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    break;
                case 8:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (c == '\n') {
                        errorInvalidCharacter();
                        return null;
                    }
                    int count = 0;
                    for (; (char) c != '"'; c = readch()) {
                        if (c != '\n' && c != '"' && c != '$') {
                            if (c >= 0 && c <= 255 && count <= 254) {
                                lexeme += (char) c;
                            } else {
                                if (c == -1) {
                                    errorEOFNotExpected();
                                } else if (!(c >= 0 && c <= 255)) {
                                    errorInvalidCharacter();
                                } else if (count > 254) {
                                    errorNotIdentifiedLexeme(lexeme);
                                }
                                return null;
                            }
                        } else if (c == '\n' || c == '$') {
                            errorInvalidCharacter();
                            return null;
                        }
                        count++;
                    }
                    lexeme += (char) c;
                    lexeme = lexeme.substring(0, lexeme.length() - 1) + "$\"";
                    state = 5;
                    break;
                case 9:
                    if (isDigit((char) c)) {
                        state = 10;
                        lexeme += (char) c;
                    } else if ((char) c == '.') {
                        state = 11;
                        lexeme += (char) c;
                    } else if (isValid((char) c)) {
                        giveBack = true;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    }
                    break;
                case 10:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (isDigit((char) c)) {
                        lexeme += (char) c;
                    } else if ((char) c == '.') {
                        lexeme += (char) c;
                        state = 11;
                    } else if (isValid((char) c)) {
                        giveBack = true;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    } else {
                        errorInvalidCharacter();
                        return null;
                    }
                    break;
                case 11:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (isDigit((char) c)) {
                        lexeme += (char) c;
                        state = 12;
                    } else {
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    break;
                case 12:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (isDigit((char) c)) {
                        lexeme += (char) c;
                    } else if (isValid((char) c)) {
                        giveBack = true;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    } else {
                        errorInvalidCharacter();
                        return null;
                    }
                    break;
                case 13:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '=') {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 14:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '-' || (char) c == '=') {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 15:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '=') {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 16:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '&') {
                        lexeme += (char) c;
                        state = 5;
                    } else if (c == -1) {
                        state = 5;
                    } else {
                        giveBack = true;
                        state = 5;
                    }
                    break;
                case 17:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '|') {
                        lexeme += (char) c;
                        state = 5;
                    } else {
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    break;
                case 18:
                    if (lexeme.length() > 32) {
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    } else if (isLetter((char) c) || isDigit((char) c) || (char) c == '.' || (char) c == '_') {
                        lexeme += (char) c;
                    } else if (isValid((char) c)) {
                        giveBack = true;
                        state = 5;
                    } else if (c != -1) {
                        errorInvalidCharacter();
                        return null;
                    }
                    break;
                case 19:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '*') {
                        state = 20;
                        lexeme = "";
                    } else {
                        if (c != -1) {
                            giveBack = true;
                            state = 5;
                        }
                    }
                    break;
                case 20:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '*') {
                        state = 22;
                    } else if (isValid((char) c)) {
                        state = 21;
                    } else {
                        errorInvalidCharacter();
                        return null;
                    }
                    break;
                case 21:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '*') {
                        state = 22;
                    } else if (isValid((char) c)) {
                        continue;
                    } else {
                        errorInvalidCharacter();
                        return null;
                    }
                    break;
                case 22:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if ((char) c == '/') {
                        lexeme = "";
                        state = 1;
                    } else if (isValid((char) c)) {
                        state = 21;
                    } else {
                        errorInvalidCharacter();
                        return null;
                    }
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
            if ((char) c != '\n' && (char) c != ' ') {
                lexeme += (char) c;
                state = checkStateFrom1((char) c);
            }
            giveBack = false;
        }
        return t;
    }

    private boolean isHexValid(char c) {
        String hex = "0123456789abcdefABCDEF";
        if (hex.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    private int readBlank() throws IOException {
        for (c = readch();; c = readch()) {
            if ((char) c == ' ' || c == '\t') {
                continue;
            } else if ((char) c == '\n') {
                line++;
            } else {
                break;
            }
        }
        return c;
    }

    private int checkStateFrom1(int c) {
        if (isInvalidTokenBegin((char) c)) {
            return 0;
        } else if ((char) c == ' ') {
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
        }
        return 1;
    }

    void errorNotIdentifiedLexeme(String lexeme) {
        if (c == '\n')
            line--;
        System.out.print(line + "\nlexema nao identificado [" + lexeme + "].");
    }

    void errorInvalidCharacter() {
        System.out.print(line + "\ncaractere invalido.");
    }

    void errorEOFNotExpected() {
        System.out.print(line + "\nfim de arquivo nao esperado.");
    }

    private boolean isInvalidTokenBegin(char c) {
        String invalid = ":\\%?";
        if (invalid.contains(Character.toString(c)))
            return true;
        return false;
    }

    private boolean isAsciiExt(int c) {
        if (c >= 0 && c <= 255) {
            return true;
        }
        return false;
    }

    private boolean isValid(char c) {
        String valid = " _.,;:()[]{}+-\"\'/|\\&%!?><=\n\r*";
        if (isDigit(c) || isLetter(c) || valid.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    private boolean isToken(char c) {
        String valid = "=(),+-*/;{}[]";
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
