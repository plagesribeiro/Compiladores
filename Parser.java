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

    boolean isReserved() {
        Token temp = lexer.st.findToken(token.lexeme);
        if (temp == null) {
            return false;
        } else {
            return temp.isReserved;
        }
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

        String[] types = new String[] { "char", "int", "float", "string" };
        if (Arrays.asList(types).contains(token.lexeme)) {
            readNextToken();
            if (ListaDeIds()) {
                if (token.lexeme.equals(";")) {
                    return true;
                } else {
                    exitError();
                }
            }
        } else if (token.lexeme.equals("const")) {
            readNextToken();
            if (Id()) {
                readNextToken();
                if (token.lexeme.equals("=")) {
                    if (Expressao()) {
                        // continuar daqui
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
                if (token.lexeme.equals(",")) {
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
        return !isReserved();
    }

    public static void main(String args[]) throws IOException {
        Parser p = new Parser();
        p.S();
    }
}