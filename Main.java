import java.util.Hashtable;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        runParser();
    }

    public static void runParser() throws IOException {
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

    void CasaToken(byte esperado) {
        if (token.tag == esperado) {
            readNextToken();
        } else if (token.tag == Token.EOF) {
            errorEOF();
        } else {
            errorNotExpectedToken();
        }
    }

    void readNextToken() {
        try {
            token = lexer.scan();
            if (token == null) {
                exit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void errorEOF() {
        System.out.print(lexer.line + "\nfim de arquivo nao esperado.");
        exit();
    }

    void errorNotExpectedToken() { // T07 esta com problema aqui!
        // PODE ENTREGAR FIM DE ARQUIVO NAO ESPERADO
        if (token.tag == Token.EOF) {
            errorEOF();
        }

        System.out.print(lexer.line + "\ntoken nao esperado [" + token.lexeme + "].");
        exit();
    }

    void exit() {
        System.exit(1);
    }

    public void S() {
        readNextToken();
        EOF();
        while (Declaracao() || Comandos())
            ;
    }

    boolean Declaracao() {
        if (Tipo()) {
            if (!ListaDeIds())
                errorNotExpectedToken();
            CasaToken(Token.SEMICOLON);
            EOF();
            return true;

        } else if (token.tag == Token.CONST) {
            CasaToken(Token.CONST);
            CasaToken(Token.ID);
            CasaToken(Token.EQ);
            if (!Expressao())
                errorNotExpectedToken();
            CasaToken(Token.SEMICOLON);
            EOF();
            return true;

        }
        return false;
    }

    boolean Tipo() {
        if (token.tag == Token.INT) {
            CasaToken(Token.INT);
            return true;
        } else if (token.tag == Token.FLOAT) {
            CasaToken(Token.FLOAT);
            return true;
        } else if (token.tag == Token.STRING) {
            CasaToken(Token.STRING);
            return true;
        } else if (token.tag == Token.CHAR) {
            CasaToken(Token.CHAR);
            return true;
        }
        return false;
    }

    boolean Comandos() {
        if (token.tag == Token.OPEN_BRACE) {
            CasaToken(Token.OPEN_BRACE);
            while (Comando())
                ;
            CasaToken(Token.CLOSE_BRACE);
            EOF();
            return true;
        } else if (Comando()) {
            EOF();
            return true;
        }
        return false;
    }

    void EOF() {
        if (token.tag == Token.EOF) {
            CasaToken(Token.EOF);
            System.out.print(lexer.line + " linhas compiladas.");
            exit();
        }
    }

    boolean Comando() {
        Atribuicao();
        Repeticao();
        Teste();
        Leitura();
        Escrita();
        if (token.tag == Token.SEMICOLON) {
            CasaToken(Token.SEMICOLON);
            return true;
        } else if (token.tag == Token.EOF) {
            errorEOF();
        }
        return false;
    }

    boolean ListaDeIds() {
        if (Di()) {
            do {
                if (token.tag == Token.COMMA) {
                    CasaToken(Token.COMMA);
                    if (!Di())
                        errorNotExpectedToken();
                } else {
                    return true;
                }
            } while (true);
        }
        return false;
    }

    boolean Di() {
        if (token.tag == Token.ID) {
            CasaToken(Token.ID);
            if (token.tag == Token.ASSIGN) {
                CasaToken(Token.ASSIGN);
                if (!Const())
                    errorNotExpectedToken();
            }
            return true;
        }
        return false;
    }

    boolean Const() {
        if (token.tag == Token.VALUE_INT) {
            CasaToken(Token.VALUE_INT);
            return true;

        } else if (token.tag == Token.VALUE_FLOAT) {
            CasaToken(Token.VALUE_FLOAT);
            return true;

        } else if (token.tag == Token.VALUE_STRING) {
            CasaToken(Token.VALUE_STRING);
            return true;

        } else if (token.tag == Token.VALUE_CHAR) {
            CasaToken(Token.VALUE_CHAR);
            return true;
        }
        return false;
    }

    boolean Atribuicao() {
        if (token.tag == Token.ID) {
            CasaToken(Token.ID);
            if (token.tag == Token.OPEN_BRACKET) {
                CasaToken(Token.OPEN_BRACKET);
                if (!Expressao())
                    errorNotExpectedToken();
                CasaToken(Token.CLOSE_BRACKET);
                return true;
            }
            CasaToken(Token.ASSIGN);
            if (!Expressao())
                errorNotExpectedToken();
            return true;
        }
        return false;
    }

    boolean Repeticao() {
        if (token.tag == Token.WHILE) {
            CasaToken(Token.WHILE);
            if (!Expressao())
                errorNotExpectedToken();
            if (!Comandos())
                errorNotExpectedToken();
            return true;
        }
        return false;
    }

    boolean Teste() {
        if (token.tag == Token.IF) {
            CasaToken(Token.IF);
            if (!Expressao())
                errorNotExpectedToken();
            if (!Comandos())
                errorNotExpectedToken();
            CasaToken(Token.ELSE);
            if (!Comandos())
                errorNotExpectedToken();
            return true;
        }
        return false;
    }

    boolean Leitura() {
        if (token.tag == Token.READLN) {
            CasaToken(Token.READLN);
            CasaToken(Token.OPEN_PARENTHESIS);
            if (token.tag == Token.ID) {
                CasaToken(Token.ID);
            } else if (!Expressao()) {
                errorNotExpectedToken();
            }
            CasaToken(Token.CLOSE_PARENTHESIS);
            return true;
        }
        return false;
    }

    boolean Escrita() {
        if (token.tag == Token.WRITE || token.tag == Token.WRITELN) {
            CasaToken(token.tag);
            CasaToken(Token.OPEN_PARENTHESIS);
            if (!Expressao())
                errorNotExpectedToken();
            do {
                if (token.tag == Token.COMMA) {
                    CasaToken(Token.COMMA);
                    if (!Expressao())
                        errorNotExpectedToken();
                } else {
                    CasaToken(Token.CLOSE_PARENTHESIS);
                    return true;
                }
            } while (true);
        }
        return false;
    }

    boolean Expressao() {
        if (ExpS()) {
            do {
                if (Comp()) {
                    if (!ExpS())
                        errorNotExpectedToken();
                } else {
                    return true;
                }
            } while (true);
        } else {
            errorNotExpectedToken();
        }

        return false;
    }

    boolean Comp() {
        if (token.tag == Token.EQ) {
            CasaToken(Token.EQ);
            return true;

        } else if (token.tag == Token.NOT_EQUAL) {
            CasaToken(Token.NOT_EQUAL);
            return true;

        } else if (token.tag == Token.LOWER) {
            CasaToken(Token.LOWER);
            return true;

        } else if (token.tag == Token.GREATER) {
            CasaToken(Token.GREATER);
            return true;

        } else if (token.tag == Token.LOWER_EQUAL) {
            CasaToken(Token.LOWER_EQUAL);
            return true;
        } else if (token.tag == Token.GREATER_EQUAL) {
            CasaToken(Token.GREATER_EQUAL);
            return true;
        }
        return false;
    }

    boolean ExpS() {
        if (token.tag == Token.MINUS || token.tag == Token.PLUS) {
            CasaToken(token.tag);
        }
        if (T()) {
            do {
                if (token.tag == Token.MINUS || token.tag == Token.PLUS || token.tag == Token.OR) {
                    CasaToken(token.tag);
                    if (!T())
                        errorNotExpectedToken();
                } else {
                    return true;
                }
            } while (true);
        }
        return false;

    }

    boolean T() {
        if (F()) {
            do {
                if (Op()) {
                    if (!F())
                        errorNotExpectedToken();
                } else {
                    return true;
                }
            } while (true);
        }
        return false;
    }

    boolean Op() {
        if (token.tag == Token.MULTIPLY) {
            CasaToken(Token.MULTIPLY);
            return true;

        } else if (token.tag == Token.SLASH_FORWARD) {
            CasaToken(Token.SLASH_FORWARD);
            return true;

        } else if (token.tag == Token.AND) {
            CasaToken(Token.AND);
            return true;

        } else if (token.tag == Token.DIV) {
            CasaToken(Token.DIV);
            return true;

        } else if (token.tag == Token.MOD) {
            CasaToken(Token.MOD);
            return true;
        }
        return false;
    }

    boolean F() {
        if (token.tag == Token.ID) {
            CasaToken(Token.ID);
            if (token.tag == Token.OPEN_BRACKET) {
                CasaToken(Token.OPEN_BRACKET);
                if (!Expressao())
                    errorNotExpectedToken();
                CasaToken(Token.CLOSE_BRACKET);
            }
            return true;

        } else if (Const()) {
            return true;

        } else if (P()) {
            return true;

        } else if (token.tag == Token.INT) {
            CasaToken(Token.FLOAT);
            if (!P())
                errorNotExpectedToken();
            return true;

        } else if (token.tag == Token.FLOAT) {
            CasaToken(Token.FLOAT);
            if (!P())
                errorNotExpectedToken();
            return true;
        }

        return false;
    }

    boolean P() {
        if (token.tag == Token.OPEN_PARENTHESIS) {
            CasaToken(Token.OPEN_PARENTHESIS);
            if (!Expressao())
                errorNotExpectedToken();
            CasaToken(Token.CLOSE_PARENTHESIS);
            return true;
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

    // Contrutor da classe Lexer
    public Lexer() {
        this.lexeme = "";
        this.line = 1;
        this.state = 1;
        this.giveBack = false;
        this.st = new SymbolTable();

        // Insere as palavras reservadas na Tabela de Simbolos
        this.st.insertToken("const", new Token("const", Token.CONST));
        this.st.insertToken("int", new Token("int", Token.INT));
        this.st.insertToken("char", new Token("char", Token.INT));
        this.st.insertToken("while", new Token("while", Token.WHILE));
        this.st.insertToken("if", new Token("if", Token.IF));
        this.st.insertToken("float", new Token("float", Token.FLOAT));
        this.st.insertToken("else", new Token("else", Token.ELSE));
        this.st.insertToken("&&", new Token("&&", Token.AND));
        this.st.insertToken("||", new Token("||", Token.OR));
        this.st.insertToken("!", new Token("!", Token.NOT));
        this.st.insertToken("<-", new Token("<-", Token.ASSIGN));
        this.st.insertToken("=", new Token("=", Token.EQ));
        this.st.insertToken("(", new Token("(", Token.OPEN_PARENTHESIS));
        this.st.insertToken(")", new Token(")", Token.CLOSE_PARENTHESIS));
        this.st.insertToken("<", new Token("<", Token.GREATER));
        this.st.insertToken(">", new Token(">", Token.LOWER));
        this.st.insertToken("!=", new Token("!=", Token.NOT_EQUAL));
        this.st.insertToken(">=", new Token(">=", Token.GREATER_EQUAL));
        this.st.insertToken("<=", new Token("<=", Token.LOWER_EQUAL));
        this.st.insertToken(",", new Token(",", Token.COMMA));
        this.st.insertToken("+", new Token("+", Token.PLUS));
        this.st.insertToken("-", new Token("-", Token.MINUS));
        this.st.insertToken("*", new Token("*", Token.MULTIPLY));
        this.st.insertToken("/", new Token("/", Token.SLASH_FORWARD));
        this.st.insertToken(";", new Token(";", Token.SEMICOLON));
        this.st.insertToken("{", new Token("{", Token.OPEN_BRACE));
        this.st.insertToken("}", new Token("}", Token.CLOSE_BRACE));
        this.st.insertToken("readln", new Token("readln", Token.READLN));
        this.st.insertToken("div", new Token("div", Token.DIV));
        this.st.insertToken("write", new Token("write", Token.WRITE));
        this.st.insertToken("writeln", new Token("writeln", Token.WRITELN));
        this.st.insertToken("mod", new Token("mod", Token.MOD));
        this.st.insertToken("[", new Token("[", Token.OPEN_BRACKET));
        this.st.insertToken("]", new Token("]", Token.CLOSE_BRACKET));
        this.st.insertToken("string", new Token("string", Token.STRING));
    }

    // Le proximo caractere
    private int readch() throws IOException {
        return (int) System.in.read();
    }

    // O metodo segue o automato gerado para resolver a analise lexica
    // e retorna um Token valido ou null para Token com erro
    public Token scan() throws IOException {
        while (state != 5) {
            if (!giveBack && state != 5 && state != 23 && state != 24 && state != 25 && state != 26) {
                c = readch();
                if ((char) c == '\n') {
                    line++;
                }
            }
            switch (state) {
                case 1: // Define qual estado seguir a partir do caractere lido
                    if (c == -1) {
                        state = 26; // Programa compilado com sucesso
                    } else if (c == '\n' || c == '\t' || c == '\r' || c == ' ') { // Leitura de espacoes em branco e \n
                        state = 1;
                    } else {
                        lexeme += (char) c;
                        state = checkStateFrom1(c); // Gera proximo estado do automato
                    }
                    break;
                case 2: // Leitura de char (hexa) ou inteiros e reais iniciados com 0
                    if ((char) c == 'x' || (char) c == 'X') {
                        lexeme += (char) c;
                        state = 3; // char hexa
                    } else if (c == '.') {
                        lexeme += (char) c;
                        state = 11; // numeros reais iniciados com 0.
                    } else if (isDigit((char) c)) {
                        lexeme += (char) c;
                        state = 10; // numeros iniciados com 0 e seguidos de . ou numeros
                    } else {
                        state = 5; // Estado final. Encontrou apenas 0
                        giveBack = true; // Devolve c
                    }
                    break;
                case 3: // Leitura de char (hexa)
                    if (isHexValid((char) c)) {
                        lexeme += (char) c;
                        state = 4; // Continua a leitura de um hexa
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        lexeme += (char) c;
                        state = 24; // Lexema nao identificado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 4: // Leitura de char (hexa)
                    if (isHexValid((char) c)) {
                        lexeme += (char) c;
                        state = 5; // Terminou a leitura de um hexa
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        lexeme += (char) c;
                        state = 24; // Lexema nao identificado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 6: // Char no formato 'c'
                    if (isValid((char) c) && c != '\n') {
                        state = 7; // Continua leitura de char no formato 'c'
                        lexeme += (char) c;
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        state = 24; // Lexema nao identificado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 7: // Char no formato 'c'
                    if ((char) c == '\'') {
                        state = 5; // Fim de leitura de char
                        lexeme += (char) c;
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        state = 24; // Lexema nao identificado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 8: // Leitura de string no formato "string"
                    if ((char) c == '\"') {
                        lexeme += (char) c;
                        state = 5; // Fim de leitura de string
                    } else if (isValidStr((char) c)) {
                        lexeme += (char) c;
                        if (lexeme.length() > 256) { // Verifica tamanho de string
                            state = 24; // Lexema nao identificado
                        }
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        state = 24; // Lexema nao identificado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 9: // Numeros inteiros e reais iniciados com '-', ou apenas '-'
                    if (isDigit((char) c)) {
                        state = 10; // Leitura de numeros reais ou inteiros negativos
                        lexeme += (char) c;
                    } else if ((char) c == '.') {
                        state = 11; // Numeros reais negativos iniciados com '-.''
                        lexeme += (char) c;
                    } else {
                        giveBack = true;
                        state = 5; // Fim de leitura do token '-'
                    }
                    break;
                case 10: // Continua leitura de inteiros ou reais positivos ou negativos
                    if (isDigit((char) c)) {
                        lexeme += (char) c; // Concatena n numeros inteiros
                    } else if ((char) c == '.') {
                        lexeme += (char) c;
                        state = 11; // Concatena '.' e segue para estado de leitura de tokens reais
                    } else {
                        giveBack = true;
                        state = 5; // Fim de leitura de numero inteiro positivo ou negativo
                    }
                    break;
                case 11: // Numeros reais
                    if (isDigit((char) c)) {
                        lexeme += (char) c;
                        state = 12; // Continua leitura de numero real
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        state = 24; // Lexema nao esperado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 12: // Leitura de numeros reais positivos ou negativos
                    if (isDigit((char) c)) {
                        lexeme += (char) c;
                        if (!checkValidPrecision()) {
                            state = 24; // Lexema nao esperado (precisao acima de 6 digitos)
                        }
                    } else {
                        giveBack = true;
                        state = 5; // Fim de leitura de numero real
                    }
                    break;
                case 13: // Leitura de '!' ou '!='
                    if ((char) c == '=') {
                        lexeme += (char) c;
                        state = 5; // Fim de leitura de '!='
                    } else {
                        giveBack = true;
                        state = 5; // Fim de leitura de '!'
                    }
                    break;
                case 14: // Leitura de '<', '<=' ou '<-'
                    if ((char) c == '-' || (char) c == '=') {
                        lexeme += (char) c;
                        state = 5; // Fim de leitura de '<-' ou '<='
                    } else {
                        giveBack = true;
                        state = 5; // Fim de leitura de '<'
                    }
                    break;
                case 15: // Leitura de '>' ou '>='
                    if ((char) c == '=') {
                        lexeme += (char) c;
                        state = 5; // Fim de leitura de '>='
                    } else {
                        giveBack = true;
                        state = 5; // Fim de leitura de '>'
                    }
                    break;
                case 16: // Leitura de '&&'
                    if ((char) c == '&') {
                        lexeme += (char) c;
                        state = 5; // Fim de leitura de '&&'
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        state = 24; // Caractere nao esperado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 17: // Leitura de '||'
                    if ((char) c == '|') {
                        lexeme += (char) c;
                        state = 5; // Fim de leitura de '||'
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else if (isValid((char) c)) {
                        state = 24; // Caractere nao esperado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 18: // Leitura de ID e tokens
                    if (isLetter((char) c) || isDigit((char) c) || (char) c == '.' || (char) c == '_') {
                        lexeme += (char) c;
                        if (lexeme.length() > 32) {
                            state = 24; // Verifica se ID possui tamanho permitido
                        }
                    } else {
                        giveBack = true;
                        state = 5; // Retorna ID lido
                    }
                    break;
                case 19: // Le '/' ou comentario
                    if ((char) c == '*') {
                        state = 20; // Le comentario
                        lexeme = "";
                    } else {
                        giveBack = true;
                        state = 5; // Retorna token '/'
                    }
                    break;
                case 20: // Fecha ou continua comentario
                    if ((char) c == '*') {
                        state = 22; // Tenta fechar comentario
                    } else if (isValid((char) c)) {
                        state = 21; // Le comentario
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 21: // Le comentario
                    if ((char) c == '*') {
                        state = 22; // Tenta fechar comentario
                    } else if (isValid((char) c)) {
                        break; // Continua leitura de comentario
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 22: // Fecha ou continua comentario
                    if ((char) c == '/') {
                        lexeme = "";
                        state = 1; // Fecha comentario e volta ao estado inicial
                    } else if (isValid((char) c)) {
                        state = 21; // Continua comentario
                    } else if (c == -1) {
                        state = 23; // Fim de arquivo nao esperado
                    } else {
                        state = 25; // Caractere invalido
                    }
                    break;
                case 23: // ERRO: Fim de arquivo nao esperado
                    errorEOFNotExpected();
                    return null;
                case 24: // ERRO: Lexema nao identificado
                    if (c == '\n') {
                        line--;
                    }
                    errorNotIdentifiedLexeme(lexeme);
                    return null;
                case 25: // ERRO: Caractere invalido
                    errorInvalidCharacter();
                    return null;
                case 26:
                    return new Token("EOF", Token.EOF);
            }
        }

        // Pesquisa token na tabela de simbolos. Caso nao exista, ele deve ser inserido
        Token t = st.findToken(lexeme);
        if (t == null) {
            if (isLetter(lexeme.charAt(0)) || lexeme.charAt(0) == '_')
                t = st.insertToken(lexeme, new Token(lexeme, Token.ID)); // Token e' ID
            else if (lexeme.charAt(0) == '\''
                    || (lexeme.length() > 2) && (lexeme.charAt(0) == '0' && lexeme.charAt(1) == 'x'))
                t = st.insertToken(lexeme, new Token(lexeme, Token.VALUE_CHAR)); // Token e' char
            else if (lexeme.charAt(0) == '"')
                t = st.insertToken(lexeme, new Token(lexeme, Token.VALUE_STRING)); // Token e' String
            else if (lexeme.contains("."))
                t = st.insertToken(lexeme, new Token(lexeme, Token.VALUE_FLOAT)); // Token e' float
            else
                t = st.insertToken(lexeme, new Token(lexeme, Token.VALUE_INT)); // Token e' int
        }

        lexeme = ""; // Reseta valor atual de lexema para leitura do proximo token
        state = 1; // Reseta o automato para o estado inicial
        if (giveBack) {
            if (c == -1) {
                state = 26; // Fim de arquivo sem erro lexico
            } else if ((c != '\n') && (char) c != ' ' && (char) c != '\r' && c != '\t') {
                lexeme += (char) c;
                state = checkStateFrom1((char) c); // Verifica o proximo estado caso o caractere tenha sido devolvido
            }
            giveBack = false;
        }
        return t; // Retorna o Token lido
    }

    // Valida a precisao para numeros float
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

    // Verifica se os digitos sao validos para numeros hexadecimais
    private boolean isHexValid(char c) {
        String hex = "0123456789abcdefABCDEF";
        if (hex.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    // Metodo para decidir qual estado seguir a partir do estado 1
    private int checkStateFrom1(int c) {
        if ((char) c == -1) {
            return 26;
        } else if ((char) c == ' ' || (char) c == '\r' || c == '\n' || c == '\t') { // Ler espaços em branco e quebra de
                                                                                    // linha
            return 1;
        } else if ((char) c == '0') { // Le char (hexa) ou números iniciados em 0
            return 2;
        } else if (isDigit((char) c)) { // Le numeros inteiros ou reais
            return 10;
        } else if ((char) c == '.') { // Le numeros reais iniciados com '.'
            return 11;
        } else if ((char) c == '-') { // Le numeros inteiros ou reais iciados com '-'
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
        } else if ((char) c == '&') { // Le &&
            return 16;
        } else if ((char) c == '|') { // Le ||
            return 17;
        } else if (isLetter((char) c) || (char) c == '_') { // Le ID
            return 18;
        } else if (isValid((char) c)) { // Verifica se o caractere e valido dentro do arquivo da linguagem
            return 24;
        } else {
            return 25;
        }
    }

    // Verifica se o caractere e valido dentro do arquivo da linguagem
    private boolean isValid(char c) {
        String valid = " _.,;:()[]{}+-\"\'/|\\&%!?><=\n\r*";
        if (isDigit(c) || isLetter(c) || valid.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    // Verifica a validade dos caracteres inseridos numa string
    private boolean isValidStr(char c) {
        if (isValid((char) c) && c != '\n' && c != '\r' && c != '$')
            return true;
        return false;
    }

    // Verifica se o caractere e um token unico
    private boolean isToken(char c) {
        String valid = "=(),+*/;{}[]";
        if (valid.contains(Character.toString(c))) {
            return true;
        }
        return false;
    }

    // Verifica se o caractere e um numero
    private boolean isDigit(char c) {
        String numbers = "0123456789";
        if (numbers.contains(Character.toString(c)))
            return true;
        return false;
    }

    // Verifica se o caractere e uma letra
    public boolean isLetter(char c) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (letters.contains(Character.toString(c)))
            return true;
        return false;
    }

    // Erro para lexemas nao identificados:
    // nn
    // lexema nao identificado[lex].
    void errorNotIdentifiedLexeme(String lexeme) {
        System.out.print(line + "\nlexema nao identificado [" + lexeme + "].");
        System.exit(1);
    }

    // Erro para caracteres invalidos:
    // nn
    // caractere invalido.
    void errorInvalidCharacter() {
        if (c == '\n')
            line--;
        System.out.print(line + "\ncaractere invalido.");
        System.exit(1);
    }

    // Erro para fim de arquivo nao esperado:
    // nn
    // fim de arquivo nao esperado.
    void errorEOFNotExpected() {
        System.out.print(line + "\nfim de arquivo nao esperado.");
        System.exit(1);
    }
}

// Classe tabela de simbolos
class SymbolTable {
    // Tabela Hash com Chave String e Valor Token
    private Hashtable<String, Token> table;

    // Construtor
    public SymbolTable() {
        this.table = new Hashtable<String, Token>();
    }

    // Insere um Token na tabela de simbolos
    public Token insertToken(String lexeme, Token t) {
        table.put(lexeme, t);
        return t;
    }

    // Pesquisa um Token na tabela de simbolos
    public Token findToken(String lexeme) {
        return table.get(lexeme);
    }

    // Lista a tabela de simbolos
    public void listTable() {
        System.out.println(table.toString());
    }
}

// Classe Token
class Token {
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
    public final static byte EOF = 42;

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