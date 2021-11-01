// Pontifícia Universidade Catolica de Minas Gerais
// 
// Jose Mario de Carvalho Lacerda
// Pedro Lages Ribeiro
// Pedro Gonzaga Prado

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.io.IOException;

// Classe Main, que inicia o parser
public class Main {
    public static void main(String[] args) throws IOException {
        Parser p = new Parser();
        p.S();
    }
}

// Classe Parser
class Parser {
    private Lexer lexer;
    private Token token;
    private List<Token> initializedIDs = new ArrayList<>();

    public Parser() throws IOException {
        this.lexer = new Lexer();
    }

    // Metodo para verificar se o token recebido casa com o token esperado
    void CasaToken(byte esperado) {
        if (token.tag == esperado) {
            readNextToken();
        } else if (token.tag == Token.EOF) {
            errorEOF();
        } else {
            errorNotExpectedToken();
        }
    }

    // Le o proximo token utilizando o analisador lexico
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

    // Erro de fim de arquivo nao esperado
    void errorEOF() {
        System.out.print(lexer.line + "\nfim de arquivo nao esperado.");
        exit();
    }

    // Erro de token nao esperado
    void errorNotExpectedToken() {
        if (token.tag == Token.EOF) {
            errorEOF();
        }

        System.out.print(lexer.line + "\ntoken nao esperado [" + token.lexeme + "].");
        exit();
    }

    void errorNotInitializedId() {
        System.out.print(lexer.line + "\nidentificador nao declarado [" + token.lexeme + "].");
        exit();
    }

    void errorInitializedId() {
        System.out.print(lexer.line + "\nidentificador ja declarado [" + token.lexeme + "].");
        exit();
    }

    void errorIncompatibleClass() {
        System.out.print(lexer.line + "\nclasse de identificador incompatível [" + token.lexeme + "].");
        exit();
    }

    void errorIncompatibleTypes() {
        System.out.print(lexer.line + "\ntipos incompativeis.");
        exit();
    }

    // Finaliza o programa
    void exit() {
        System.exit(1);
    }

    // Termina a execucao com sucesso
    void EOF() {
        if (token.tag == Token.EOF) {
            CasaToken(Token.EOF);
            System.out.print(lexer.line + " linhas compiladas.");
            exit();
        }
    }

    boolean isIdInicialized() {
        for (Token t : initializedIDs)
            if (t.lexeme.equals(token.lexeme))
                return true;

        errorNotInitializedId();
        return false;
    }

    boolean isIdNotInicialized() {
        for (Token t : initializedIDs)
            if (t.lexeme.equals(token.lexeme)) {
                errorInitializedId();
                return false;
            }

        initializedIDs.add(token);
        return true;
    }

    // Estado inicial da gramatica
    // S -> {Declaracao | Comandos}* EOF
    public void S() {
        readNextToken();
        EOF();
        while (Declaracao() || Comandos())
            ;
    }

    // Estado da gramatica responsavel por comandos de declaracao
    // Declaracao -> ( Tipo Lista-de-ids ";" | "const" ID "=" Exp ";")
    boolean Declaracao() {
        ReferenceToken tipoV = new ReferenceToken();
        if (Tipo(tipoV)) {
            if (!ListaDeIds(tipoV))
                errorNotExpectedToken();
            CasaToken(Token.SEMICOLON);
            EOF();
            return true;

        } else if (token.tag == Token.CONST) {
            CasaToken(Token.CONST);
            ReferenceToken constV = new ReferenceToken();
            if (token.classe.equals("")) {
                token.classe = "classe-const";
                constV.endereco = token.lexeme;
            } else {
                errorInitializedId();
            }
            CasaToken(Token.ID);
            CasaToken(Token.EQ);

            if (!Expressao(constV))
                errorNotExpectedToken();
            CasaToken(Token.SEMICOLON);
            EOF();
            return true;
        }
        return false;
    }

    // Estado da gramatica responsaval por validacao do tipo da variavel
    // Tipo -> "char" | "string" | "int" | "float"
    boolean Tipo(ReferenceToken tipoV) {
        if (token.tag == Token.INT) {
            tipoV.tipo = "int";
            tipoV.endereco = token.lexeme;
            CasaToken(Token.INT);
            return true;
        } else if (token.tag == Token.FLOAT) {
            tipoV.tipo = "float";
            tipoV.endereco = token.lexeme;
            CasaToken(Token.FLOAT);
            return true;
        } else if (token.tag == Token.STRING) {
            tipoV.tipo = "string";
            tipoV.endereco = token.lexeme;
            CasaToken(Token.STRING);
            return true;
        } else if (token.tag == Token.CHAR) {
            tipoV.tipo = "char";
            tipoV.endereco = token.lexeme;
            CasaToken(Token.CHAR);
            return true;
        }
        return false;
    }

    // Estado da gramatica responsavel pela geracao de comandos ou bloco de comandos
    // Comandos -> "{" Comando* "}" | Comando
    boolean Comandos() {
        ReferenceToken comandoV;
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

    // Estado da gramatica responsavel pela geracao do comando
    // Comando -> [Atribuicao | Repeticao | Teste | Leitura | Escrita] ";"
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

    // Estado da gramatica responsavel pela geracao das listas de IDs
    // Lista-de-ids -> Di {"," Di}*
    boolean ListaDeIds(ReferenceToken tipoV) {
        if (Di(tipoV)) {
            do {
                if (token.tag == Token.COMMA) {
                    CasaToken(Token.COMMA);
                    if (!Di(tipoV))
                        errorNotExpectedToken();
                } else {

                    return true;
                }
            } while (true);
        }
        return false;
    }

    // Estado da gramatica responsavel pela geracao de ID
    // Di -> ID[<-Const]
    boolean Di(ReferenceToken tipoV) {
        if (token.tag == Token.ID) {
            if (token.classe.equals("")) {
                token.classe = "classe-var";
                token.type = tipoV.tipo;
            } else {
                errorInitializedId();
            }

            CasaToken(Token.ID);
            if (token.tag == Token.ASSIGN) {
                CasaToken(Token.ASSIGN);
                ReferenceToken constV = new ReferenceToken();
                if (!Const(constV)) {
                    errorNotExpectedToken();
                }
                if (!constV.tipo.equals(tipoV.tipo)) {
                    errorIncompatibleTypes();
                }
            }
            return true;
        }
        return false;
    }

    // Estado da gramatica responsavel pelo recebimento de valores
    // Const -> int | float | char | string
    boolean Const(ReferenceToken constV) {
        if (token.tag == Token.VALUE_INT) {
            constV.endereco = token.lexeme;
            constV.tipo = "int";
            CasaToken(Token.VALUE_INT);
            return true;

        } else if (token.tag == Token.VALUE_FLOAT) {
            constV.endereco = token.lexeme;
            constV.tipo = "float";
            CasaToken(Token.VALUE_FLOAT);
            return true;

        } else if (token.tag == Token.VALUE_STRING) {
            constV.endereco = token.lexeme;
            constV.tipo = "string";
            CasaToken(Token.VALUE_STRING);
            return true;

        } else if (token.tag == Token.VALUE_CHAR) {
            constV.endereco = token.lexeme;
            constV.tipo = "char";
            CasaToken(Token.VALUE_CHAR);
            return true;
        }
        return false;
    }

    // Estado da gramatica responsavel por atribuicoes
    // Atribuicao -> ID ["["Exp"]"] "<-" Exp
    boolean Atribuicao() {
        ReferenceToken expV = new ReferenceToken();
        if (token.tag == Token.ID) {
            if (token.classe.equals("")) {
                errorNotInitializedId();
            }
            CasaToken(Token.ID);
            if (token.tag == Token.OPEN_BRACKET) {
                CasaToken(Token.OPEN_BRACKET);
                if (!Expressao(expV))
                    errorNotExpectedToken();
                CasaToken(Token.CLOSE_BRACKET);
                return true;
            }
            CasaToken(Token.ASSIGN);
            if (!Expressao(expV))
                errorNotExpectedToken();
            return true;
        }
        return false;
    }

    // Estado da gramatica responsavel pelos comandos de repeticao
    // Repeticao -> while Exp Comandos
    boolean Repeticao() {
        ReferenceToken repV = new ReferenceToken();
        if (token.tag == Token.WHILE) {
            CasaToken(Token.WHILE);
            if (!Expressao(repV))
                errorNotExpectedToken();
            if (!Comandos())
                errorNotExpectedToken();
            return true;
        }
        return false;
    }

    // Estado da gramatica responsavel por testes
    // Teste -> if Exp Comandos else Comandos
    boolean Teste() {
        ReferenceToken testeV = new ReferenceToken();
        if (token.tag == Token.IF) {
            CasaToken(Token.IF);
            if (!Expressao(testeV))
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

    // Estado da gramatica responsavel pelo readln
    // Leitura -> readln"(" ID | Exp ")"
    boolean Leitura() {
        ReferenceToken leituraV = new ReferenceToken();
        if (token.tag == Token.READLN) {
            CasaToken(Token.READLN);
            CasaToken(Token.OPEN_PARENTHESIS);
            if (token.tag == Token.ID) {
                CasaToken(Token.ID);
            } else if (!Expressao(leituraV)) {
                errorNotExpectedToken();
            }
            CasaToken(Token.CLOSE_PARENTHESIS);
            return true;
        }
        return false;
    }

    // Estado da gramatica responsavel pelo writeln
    // Escrita -> (write|writeln) "("Exp {"," Exp}*")"
    boolean Escrita() {
        ReferenceToken escritaV = new ReferenceToken();
        if (token.tag == Token.WRITE || token.tag == Token.WRITELN) {
            CasaToken(token.tag);
            CasaToken(Token.OPEN_PARENTHESIS);
            if (!Expressao(escritaV))
                errorNotExpectedToken();
            do {
                if (token.tag == Token.COMMA) {
                    CasaToken(Token.COMMA);
                    if (!Expressao(escritaV))
                        errorNotExpectedToken();
                } else {
                    CasaToken(Token.CLOSE_PARENTHESIS);
                    return true;
                }
            } while (true);
        }
        return false;
    }

    // Estado da gramatica responsavel por gerar expressoes
    // Exp -> ExpS {Comp ExpS}
    boolean Expressao(ReferenceToken expV) {
        ReferenceToken expsV1 = new ReferenceToken();
        ReferenceToken expsV2 = new ReferenceToken();
        if (ExpS(expsV1)) {
            do {
                ReferenceToken compV = new ReferenceToken();
                if (Comp(compV)) {
                    if (!ExpS(expsV2))
                        errorNotExpectedToken();
                    acaoSemanticaComp(expsV1, expsV2, compV);
                    expV.tipo = "boolean";
                } else {
                    return true;
                }
                expsV1 = expsV2;
                expsV2 = new ReferenceToken();
            } while (true);
        } else {
            errorNotExpectedToken();
        }

        return false;
    }

    void acaoSemanticaComp(ReferenceToken e1, ReferenceToken e2, ReferenceToken comp) {
        if (e1.tipo.equals("string") && e2.tipo.equals("string") && comp.tipo.equals("="))
            return;
        if (e1.tipo.equals("char") && e2.tipo.equals("char"))
            return;
        if (e1.tipo.equals("int") && e2.tipo.equals("float"))
            return;
        if (e1.tipo.equals("float") && e2.tipo.equals("int"))
            return;
        if (e1.tipo.equals("int") && e2.tipo.equals("int"))
            return;
        if (e1.tipo.equals("float") && e2.tipo.equals("float"))
            return;
        errorIncompatibleTypes();
    }

    // Estado da gramatica responsavel por comparacoes
    // Comp -> (= | != | < | > | <= | >=)
    boolean Comp(ReferenceToken compV) {
        if (token.tag == Token.EQ) {
            compV.endereco = token.lexeme;
            CasaToken(Token.EQ);
            return true;

        } else if (token.tag == Token.NOT_EQUAL) {
            compV.endereco = token.lexeme;
            CasaToken(Token.NOT_EQUAL);
            return true;

        } else if (token.tag == Token.LOWER) {
            compV.endereco = token.lexeme;
            CasaToken(Token.LOWER);
            return true;

        } else if (token.tag == Token.GREATER) {
            compV.endereco = token.lexeme;
            CasaToken(Token.GREATER);
            return true;

        } else if (token.tag == Token.LOWER_EQUAL) {
            compV.endereco = token.lexeme;
            CasaToken(Token.LOWER_EQUAL);
            return true;
        } else if (token.tag == Token.GREATER_EQUAL) {
            compV.endereco = token.lexeme;
            CasaToken(Token.GREATER_EQUAL);
            return true;
        }
        return false;
    }

    // Estado da gramatica responsavel pelas expressoes secundarias
    // ExpS -> [+|-] T {(+|-|"||") T}
    boolean ExpS(ReferenceToken expsV) {
        if (token.tag == Token.MINUS || token.tag == Token.PLUS) {
            CasaToken(token.tag);
        }
        ReferenceToken tV = new ReferenceToken();
        if (T(tV)) {
            do {
                if (token.tag == Token.MINUS || token.tag == Token.PLUS || token.tag == Token.OR) {
                    CasaToken(token.tag);
                    if (!T(tV))
                        errorNotExpectedToken();
                } else {
                    expsV = tV;
                    return true;
                }
            } while (true);
        }
        return false;
    }

    // Estado da gramatica responsavel pelo termo
    // T -> F {Op F}
    boolean T(ReferenceToken tV) {
        ReferenceToken fV = new ReferenceToken();
        if (F(fV)) {
            tV = fV;
            do {
                if (Op()) {
                    if (!F(fV))
                        errorNotExpectedToken();
                } else {
                    return true;
                }
            } while (true);
        }

        return false;
    }

    // Estado da gramatica responsavel por operacoes
    // Op -> *|/|&&|div|mod
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

    // Estado da gramatica responsavel pelo fator
    // F -> ID["[" Exp "]"] | Const | !F | P | "int" P | "float" P
    boolean F(ReferenceToken fV) {
        if (token.tag == Token.ID) {
            fV.tipo = token.type;
            fV.endereco = token.lexeme;
            CasaToken(Token.ID);
            ReferenceToken expV = new ReferenceToken();
            if (token.tag == Token.OPEN_BRACKET) {
                CasaToken(Token.OPEN_BRACKET);
                if (!Expressao(expV))
                    errorNotExpectedToken();
                CasaToken(Token.CLOSE_BRACKET);
            }
            return true;

        } else if (Const(fV)) {
            return true;

        } else if (P(fV)) {
            return true;

        } else if (token.tag == Token.INT) {
            fV.tipo = token.type;
            CasaToken(Token.INT);
            if (!P(fV))
                errorNotExpectedToken();
            return true;

        } else if (token.tag == Token.FLOAT) {
            CasaToken(Token.FLOAT);
            if (!P(fV))
                errorNotExpectedToken();
            return true;
        }

        return false;
    }

    // Estado da gramatica responsavel pelos parenteses
    // P -> "("Exp")"
    boolean P(ReferenceToken pV) {
        if (token.tag == Token.OPEN_PARENTHESIS) {
            CasaToken(Token.OPEN_PARENTHESIS);
            if (!Expressao(pV))
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
        this.st.insertToken("char", new Token("char", Token.CHAR));
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
        this.st.insertToken("<", new Token("<", Token.LOWER));
        this.st.insertToken(">", new Token(">", Token.GREATER));
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
        return System.in.read();
    }

    // O metodo segue o automato gerado para resolver a analise lexica
    // e retorna um Token valido ou quebra execucao caso encontre algum problema
    public Token scan() throws IOException {
        while (state != 5) {
            if (isNextCharReadable(c)) {
                c = readch();
                if (c == '\n') {
                    line++;
                }
            }
            switch (state) {
            case 1: // Define qual estado seguir a partir do caractere lido
                if (isEOF(c)) {
                    changeState(26); // Programa compilado com sucesso
                } else if (isBlank(c)) { // Leitura de espacos em branco e \n
                    initialState();
                } else {
                    concatLexeme();
                    changeState(checkStateFrom1(c)); // Gera proximo estado do automato
                }
                break;

            case 2: // Leitura de char (hexa) ou inteiros e reais iniciados com 0
                if (c == 'x' || c == 'X') {
                    concatLexeme();
                    changeState(3); // char hexa
                } else if (c == '.') {
                    concatLexeme();
                    changeState(11); // numeros reais iniciados com 0.
                } else if (isDigit(c)) {
                    concatLexeme();
                    changeState(10); // numeros iniciados com 0 e seguidos de '.' ou numeros
                } else {
                    finalState(); // Estado final. Encontrou apenas '0'
                    giveBack(); // Devolve c
                }
                break;

            case 3: // Leitura de char (hexa)
                if (isHexValid(c)) {
                    concatLexeme();
                    changeState(4); // Continua a leitura de um hexa
                } else if (isValid(c) || isEOF(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 4: // Leitura de char (hexa)
                if (isHexValid(c)) {
                    concatLexeme();
                    finalState(); // Terminou a leitura de um hexa
                } else if (isValid(c) || isEOF(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 6: // Char no formato 'c'
                if (isValid(c) && c != '\n') {
                    concatLexeme();
                    changeState(7); // Continua leitura de char no formato 'c'
                } else if (isValid(c) || isEOF(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 7: // Char no formato 'c'
                if (c == '\'') {
                    concatLexeme();
                    finalState(); // Fim de leitura de char
                } else if (isEOF(c)) {
                    changeState(23); // Fim de arquivo nao esperado
                } else if (isValid(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 8: // Leitura de string no formato "string"
                if (c == '\"') {
                    lexeme += '$'; // Concatena flag de fim de string
                    concatLexeme();
                    finalState(); // Fim de leitura de string
                } else if (isValidStr(c)) {
                    concatLexeme();
                    if (lexeme.length() > 256) { // Verifica tamanho de string
                        changeState(24); // Lexema nao identificado
                    }
                } else if (isValid(c) || isEOF(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 9: // Numeros inteiros e reais iniciados com '-', ou apenas '-'
                if (isDigit(c)) {
                    concatLexeme();
                    changeState(10); // Leitura de numeros reais ou inteiros negativos
                } else if (c == '.') {
                    concatLexeme();
                    changeState(11); // Numeros reais negativos iniciados com '-.' (-.1, -.2, -.3, ...)
                } else {
                    giveBack();
                    finalState(); // Fim de leitura do token '-'
                }
                break;

            case 10: // Continua leitura de inteiros ou reais (positivos ou negativos)
                if (isDigit(c)) {
                    concatLexeme(); // Concatena 'n' numeros inteiros
                } else if (c == '.') {
                    concatLexeme();
                    changeState(11); // Concatena '.' e segue para estado de leitura de tokens reais
                } else {
                    giveBack();
                    finalState(); // Fim de leitura de numero inteiro positivo ou negativo
                }
                break;

            case 11: // Numeros reais
                if (isDigit(c)) {
                    concatLexeme();
                    changeState(12); // Continua leitura de numero real
                } else if (isValid(c) || isEOF(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 12: // Leitura de numeros reais positivos ou negativos
                if (isDigit(c)) {
                    concatLexeme();
                    if (!checkFloatPrecision()) {
                        changeState(24); // Lexema nao identificado
                    }
                } else {
                    giveBack();
                    finalState(); // Fim de leitura de numero real
                }
                break;

            case 13: // Leitura de '!' ou '!='
                if (c == '=') {
                    concatLexeme();
                    finalState(); // Fim de leitura de '!='
                } else {
                    giveBack();
                    finalState(); // Fim de leitura de '!'
                }
                break;

            case 14: // Leitura de '<', '<=' ou '<-'
                if (c == '-' || c == '=') {
                    concatLexeme();
                    finalState(); // Fim de leitura de '<-' ou '<='
                } else {
                    giveBack();
                    finalState(); // Fim de leitura de '<'
                }
                break;

            case 15: // Leitura de '>' ou '>='
                if (c == '=') {
                    concatLexeme();
                    finalState(); // Fim de leitura de '>='
                } else {
                    giveBack();
                    finalState(); // Fim de leitura de '>'
                }
                break;

            case 16: // Leitura de '&&'
                if (c == '&') {
                    concatLexeme();
                    finalState(); // Fim de leitura de '&&'
                } else if (isValid(c) || isEOF(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 17: // Leitura de '||'
                if (c == '|') {
                    concatLexeme();
                    finalState(); // Fim de leitura de '||'
                } else if (isValid(c) || isEOF(c)) {
                    changeState(24); // Lexema nao identificado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 18: // Leitura de ID e tokens
                if (isLetter(c) || isDigit(c) || c == '.' || c == '_') {
                    concatLexeme();
                    if (lexeme.length() > 32) { // Verifica se ID possui tamanho permitido
                        changeState(24); // Lexema nao identificado
                    }
                } else {
                    giveBack();
                    finalState(); // Retorna ID lido
                }
                break;

            case 19: // Le '/' ou comentario
                if (c == '*') {
                    lexeme = "";
                    changeState(20); // Le comentario
                } else {
                    giveBack();
                    finalState(); // Retorna token '/'
                }
                break;

            case 20: // Fecha ou continua comentario
                if (c == '*') {
                    changeState(21); // Tenta fechar comentario
                } else if (isValid(c)) {
                    break;
                } else if (isEOF(c)) {
                    changeState(23); // Fim de arquivo nao esperado
                } else {
                    changeState(25); // Caractere invalido
                }
                break;

            case 21: // Fecha ou continua comentario
                if (c == '*') {
                    break; // Continua esperando para fechar o comentario
                } else if (c == '/') {
                    lexeme = "";
                    initialState(); // Termina leitura de comentario e volta ao estado inicial
                } else {
                    changeState(20); // Continua a ler comentario
                }
                break;

            case 23: // ERRO: Fim de arquivo nao esperado
                errorEOFNotExpected();
                break;

            case 24: // ERRO: Lexema nao identificado
                if (c == '\n') {
                    line--;
                }
                errorNotIdentifiedLexeme(lexeme);
                break;

            case 25: // ERRO: Caractere invalido
                errorInvalidCharacter();
                break;

            case 26: // Retorna ao analisador sintatico que chegou ao fim do arquivo sem erro
                     // lexico
                return new Token("EOF", Token.EOF);
            }
        }

        // Pesquisa token na tabela de simbolos. Caso nao exista, ele deve ser inserido
        Token t = st.findToken(lexeme);
        if (t == null) {
            t = new Token();
            t.lexeme = lexeme;
            if (isId()) {
                t.tag = Token.ID;
                t = insertToken(t); // Token e' ID
            } else if (isChar()) {
                t.type = "char";
                t.tag = Token.VALUE_CHAR;
                t = insertToken(t); // Token e' char
            } else if (isString()) {
                t.type = "string";
                t.tag = Token.VALUE_STRING;
                t = insertToken(t); // Token e' String
            } else if (isFloat()) {
                t.type = "float";
                t.tag = Token.VALUE_FLOAT;
                t = insertToken(t); // Token e' float
            } else if (isInt()) {
                t.type = "int";
                t.tag = Token.VALUE_INT;
                t = insertToken(t); // Token e' int
            }
        }

        lexeme = ""; // Reseta valor atual de lexema para leitura do proximo token
        initialState(); // Reseta o automato para o estado inicial
        if (giveBack) {
            if (isEOF(c)) {
                changeState(26); // Fim de arquivo sem erro lexico
            } else if (!isBlank(c)) {
                concatLexeme();
                changeState(checkStateFrom1(c)); // Verifica o proximo estado caso o caractere tenha sido devolvido
            }
            giveBack = false;
        }
        return t; // Retorna o Token lido
    }

    // Insere token na tabela de simbolo com sua respectiva tag
    Token insertToken(Token t) {
        return st.insertToken(lexeme, t);
    }

    // Verifica se lexema lido e' float.
    private boolean isFloat() {
        if (lexeme.contains("."))
            return true;
        return false;
    }

    private boolean isInt() {
        for (int i = 0; i < lexeme.length(); i++) {
            if (!isDigit(lexeme.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // Verifica se lexema lido e' String.
    private boolean isString() {
        if (lexeme.charAt(0) == '"')
            return true;
        return false;
    }

    // Verifica se lexema lido e' char.
    private boolean isChar() {
        if (lexeme.charAt(0) == '\'' || (lexeme.length() > 2)
                && (lexeme.charAt(0) == '0' && (lexeme.charAt(1) == 'x' || lexeme.charAt(1) == 'X')))
            return true;
        return false;
    }

    // Verifica se lexema lido e' ID.
    private boolean isId() {
        if (isLetter(lexeme.charAt(0)) || lexeme.charAt(0) == '_')
            return true;
        return false;
    }

    // Determina o proximo estado do automato
    void changeState(int newState) {
        state = newState;
    }

    // Apenas le o proximo caractere caso nao precise devolver, ir para o estado
    // final ou estado de erro
    private boolean isNextCharReadable(int c) {
        if (!giveBack && state != 5 && state != 23 && state != 24 && state != 25)
            return true;
        return false;
    }

    // Valida a precisao para numeros float
    private boolean checkFloatPrecision() {
        int count = 0;
        if (lexeme.length() > 7) {
            return false;
        } else {
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
        }
        return true;
    }

    // Concatena o lexeme
    void concatLexeme() {
        lexeme += (char) c;
    }

    // Devolve caractere lido
    void giveBack() {
        giveBack = true;
    }

    // Reseta o estado para o inicial
    void initialState() {
        state = 1;
    }

    // Define estado como estado final
    void finalState() {
        state = 5;
    }

    // Verifica se e' fim de arquivo
    private boolean isEOF(int c) {
        return c == -1;
    }

    // Verifica se os digitos sao validos para numeros hexadecimais
    private boolean isHexValid(int c) {
        String hex = "0123456789abcdefABCDEF";
        if (hex.contains(Character.toString((char) c))) {
            return true;
        }
        return false;
    }

    // Metodo para decidir qual estado seguir a partir do estado 1
    private int checkStateFrom1(int c) {
        if (isEOF(c))
            return 26;

        else if (isBlank(c)) // Le espaços em branco e quebra de linha
            return 1;

        else if (c == '0') // Le char (hexa) ou números iniciados em 0
            return 2;

        else if (isDigit(c)) // Le numeros inteiros ou reais
            return 10;

        else if (c == '.') // Le numeros reais iniciados com '.'
            return 11;

        else if (c == '-') // Le numeros inteiros ou reais iciados com '-'
            return 9;

        else if (c == '/') // Le comentario ou token '/'
            return 19;

        else if (c == '"') // Le String
            return 8;

        else if (c == '\'') // Le char
            return 6;

        else if (isToken(c)) // Le tokens simples
            return 5;

        else if (c == '!') // Le ! ou !=
            return 13;

        else if (c == '<') // Le <, <= ou <-
            return 14;

        else if (c == '>') // Le > ou >=
            return 15;

        else if (c == '&') // Le &&
            return 16;

        else if (c == '|') // Le ||
            return 17;

        else if (isLetter(c) || c == '_') // Le ID
            return 18;

        else if (isValid(c)) // Verifica se o caractere e' valido dentro do arquivo da linguagem
            return 24;

        else
            return 25; // Caractere invalido
    }

    // Verifica se o caractere e' branco ou enter
    private boolean isBlank(int c) {
        if (c == '\n' || c == '\t' || c == '\r' || c == ' ')
            return true;
        return false;
    }

    // Verifica se o caractere e' valido dentro do arquivo da linguagem
    private boolean isValid(int c) {
        String valid = " _.,;:()[]{}+-\"\'/|\\&%!?><=\n\r*";
        if (isDigit(c) || isLetter(c) || valid.contains(Character.toString((char) c)))
            return true;
        return false;
    }

    // Verifica a validade dos caracteres inseridos numa string
    private boolean isValidStr(int c) {
        if (isValid(c) && c != '\n' && c != '\r' && c != '$')
            return true;
        return false;
    }

    // Verifica se o caractere e' um token unico
    private boolean isToken(int c) {
        String valid = "=(),+*/;{}[]";
        if (valid.contains(Character.toString((char) c)))
            return true;
        return false;
    }

    // Verifica se o caractere e' um numero
    private boolean isDigit(int c) {
        String numbers = "0123456789";
        if (numbers.contains(Character.toString((char) c)))
            return true;
        return false;
    }

    // Verifica se o caractere e' uma letra
    public boolean isLetter(int c) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (letters.contains(Character.toString((char) c)))
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
        return table.get(lexeme);
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
    public String classe;
    public String type;
    public byte tag;
    private byte size;

    public Token(String lexeme, byte tag) {
        this.lexeme = lexeme;
        this.tag = tag;
    }

    public Token() {
        this.lexeme = "";
        this.tag = 0;
        this.classe = "";
        this.type = "";
        this.size = 0;
    }

    public String toString() {
        return "" + lexeme;
    }
}

class ReferenceToken {
    public String tipo;
    public String endereco;

    public ReferenceToken() {
        this.tipo = "";
        this.endereco = "";
    }
}