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
        return false;
    }

    boolean ListaDeIds() {
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
        return false;
    }

    boolean Id() {
        return token.tag == Tag.ID;
    }

    boolean Expressao() {
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

        return false;
    }

    boolean Comp() {
        return token.tag == Tag.EQ || token.tag == Tag.NOT_EQUAL || token.tag == Tag.LOWER || token.tag == Tag.GREATER
                || token.tag == Tag.LOWER_EQUAL || token.tag == Tag.GREATER_EQUAL;
    }

    boolean ExpS() {
        if (token.tag != Tag.MINUS && token.tag != Tag.PLUS) {
            if (T()) {
                readNextToken();
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
        } else {
            readNextToken();
            if (T()) {
                readNextToken();
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
        }
        return false;
    }

    boolean T() {
        if (F()) {
            readNextToken();
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

        } else if (!F()) {
            return true;

        } else if (token.tag == Tag.OPEN_PARENTHESIS) {
            return P();

        } else if (token.tag == Tag.INT) {
            readNextToken();
            return P();

        } else if (token.tag == Tag.FLOAT) {
            readNextToken();
            return P();

        } else {
            exitError();
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

    public static void main(String args[]) throws IOException {
        Parser p = new Parser();
        p.S();
    }
}