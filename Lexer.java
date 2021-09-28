import java.io.IOException;

public class Lexer {
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
                                } else if (!(c >= 0 && c <= 255)){
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
            else if (lexeme.charAt(0) == '\'' || (lexeme.length() > 2) && (lexeme.charAt(0) == '0' && lexeme.charAt(1) == 'x'))
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

    private boolean isValidStr(int c) {
        if ((c >= 0 && c <= 255) && (c != '$' && c != '"' && c != '\n')) {
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

    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer();

        Token t;
        do {
            t = lexer.scan();
            if (t != null) {
                System.out.println(t.toString());
            }
        } while (t != null);
        // lexer.st.listTable();
    }
}