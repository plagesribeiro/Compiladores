import java.io.IOException;
import java.util.Arrays;

public class Parser {
    private Lexer lexer;
    private Token token;

    public Parser() throws IOException {
        this.lexer = new Lexer();
    }

    void readNextToken() {
        try {
            token = lexer.scan();
            if (token == null) {
                exitError(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readNextToken(int i) {
        try {
            token = lexer.scan();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void exitError(int i) {
        System.out.print(lexer.line + "\nfim de arquivo nao esperado.");
        System.exit(1);
    }

    void exitError() {
        System.out.print(lexer.line + "\ntoken nao esperado [" + token.lexeme + "].");
        System.exit(1);
    }

    public void S() {
        readNextToken(1);
        if (token != null) {
            if (Declaracao()) {

            }
        }
    }

    boolean Declaracao() {
        boolean isDeclaracao = false;

        if (token.tag == Tag.CHAR || token.tag == Tag.INT || token.tag == Tag.STRING || token.tag == Tag.FLOAT) {
            readNextToken();
            if (ListaDeIds()) {
                if (token.tag == Tag.SEMICOLON) {
                    return true;
                } else {
                    exitError();
                }
            }
        } else if (token.tag == Tag.CONST) {
            readNextToken();
            if (Id()) {
                readNextToken();
                if (token.tag == Tag.EQ) {
                    if (Expressao()) {
                        readNextToken();
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
        return isDeclaracao;
    }

    boolean ListaDeIds() {
        boolean isListaDeIds = false;
        if (Id()) {
            boolean correct = true;
            do {
                readNextToken();
                if (token.tag == Tag.COMMA) {
                    readNextToken();
                    if (!Id()) {
                        exitError();
                    }
                } else {
                    return true;
                }
            } while (correct);
        }
        return isListaDeIds;
    }

    boolean Id() {
        return token.tag == Tag.ID;
    }

    boolean Expressao() {
        boolean isExpressao = false;
        if (ExpS()) {
            readNextToken();
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

        return isExpressao;
    }

    boolean Comp() {
        return token.tag == Tag.EQ || token.tag == Tag.NOT_EQUAL || token.tag == Tag.LOWER || token.tag == Tag.GREATER
                || token.tag == Tag.LOWER_EQUAL || token.tag == Tag.GREATER_EQUAL;
    }

    boolean ExpS() {
        boolean isExpS = false;
        if (token.tag != Tag.MINUS && token.tag != Tag.PLUS) {
            if (T()) {

            }
        }
        return isExpS;
    }

    public static void main(String args[]) throws IOException {
        Parser p = new Parser();
        p.S();
    }
}