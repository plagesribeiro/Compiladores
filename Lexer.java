import java.io.IOException;

public class Lexer {
    private String lexeme;
    public int line;
    private int c;
    private int last;
    public SymbolTable st;
    private int state;
    private boolean finalState = false;
    private boolean giveBack;

    public Lexer() {
        this.lexeme = "";
        this.line = 1;
        this.state = 1;
        this.giveBack = false;
        this.st = new SymbolTable();
        this.st.insertToken("const", new Token("const", true));
        this.st.insertToken("int", new Token("int", true));
        this.st.insertToken("while", new Token("while", true));
        this.st.insertToken("if", new Token("if", true));
        this.st.insertToken("float", new Token("float", true));
        this.st.insertToken("else", new Token("else", true));
        this.st.insertToken("and", new Token("&&", true));
        this.st.insertToken("or", new Token("||", true));
        this.st.insertToken("not", new Token("!", true));
        this.st.insertToken("assign", new Token("<-", true));
        this.st.insertToken("equals", new Token("=", true));
        this.st.insertToken("(", new Token("(", true));
        this.st.insertToken(")", new Token(")", true));
        this.st.insertToken("<", new Token("<", true));
        this.st.insertToken(">", new Token(">", true));
        this.st.insertToken("ne", new Token("!=", true));
        this.st.insertToken("ge", new Token(">=", true));
        this.st.insertToken("le", new Token("<=", true));
        this.st.insertToken("comma", new Token(",", true));
        this.st.insertToken("plus", new Token("+", true));
        this.st.insertToken("minus", new Token("-", true));
        this.st.insertToken("star", new Token("*", true));
        this.st.insertToken("bar", new Token("/", true));
        this.st.insertToken("semicolon", new Token(";", true));
        this.st.insertToken("{", new Token("{", true));
        this.st.insertToken("}", new Token("}", true));
        this.st.insertToken("readln", new Token("readln", true));
        this.st.insertToken("div", new Token("div", true));
        this.st.insertToken("write", new Token("write", true));
        this.st.insertToken("writeln", new Token("writeln", true));
        this.st.insertToken("mod", new Token("mod", true));
        this.st.insertToken("[", new Token("[", true));
        this.st.insertToken("]", new Token("]", true));
    }

    private int readch() throws IOException {
        return (int) System.in.read();
    }

    public Token scan() throws IOException {
        while (state != 5) {
            if (!giveBack && state != 5) {
                c = readch();
                if (c == '\n' && state != 6) {
                    line++;
                }
            }
            switch (state) {
                case 1:
                    if (c == -1) {
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
                        if (c != -1) {
                            lexeme += (char) c;
                        }
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
                        if (c != -1) {
                            lexeme += (char) c;
                        }
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                case 6:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (isAsciiExt(c)) {
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
                        lexeme += (char) c;
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    break;
                case 8:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    }
                    int count = 0;
                    for (; (char) c != '"'; c = readch()) {
                        if (c != '\n' || c != '"' || c != '$') {
                            if (isValidStr(c) && count <= 254) {
                                lexeme += (char) c;
                            } else {
                                if (count > 254) {
                                    errorNotIdentifiedLexeme(lexeme);
                                } else {
                                    errorInvalidCharacter();
                                }
                                return null;
                            }
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
                        lexeme += (char) c;
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
                        if (c != -1) {
                            lexeme += (char) c;
                        }
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    }
                    break;
                case 18:
                    if (c == -1) {
                        errorEOFNotExpected();
                        return null;
                    } else if (lexeme.length() > 32) {
                        if (c == '\n') {
                            line--;
                        }
                        errorNotIdentifiedLexeme(lexeme);
                        return null;
                    } else if (isLetter((char) c) || isDigit((char) c) || (char) c == '.' || (char) c == '_') {
                        lexeme += (char) c;
                    } else if (isValid((char) c)) {
                        giveBack = true;
                        state = 5;
                    } else {
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
            if (c == -1) {
                System.out.println(line + " linhas compiladas.");
                return null;
            }
        }

        Token t = new Token(lexeme);
        if (st.findToken(lexeme) == null) {
            st.insertToken(lexeme, t);
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
}