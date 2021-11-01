import java.io.BufferedReader;

public class Parser {
   AnalisadorLexico lexico;
   TabelaSimbolo tabela;
   Simbolo s, simboloParaAnalise;
   BufferedReader arquivo;
   GeracaoMemoria geracaoMemoria;
   Rotulo rotuloPJ;
   int endereco = geracaoMemoria.contador;
   
   private int procFend = 0;
   private int procTend = 0;
   private int procExpsend = 0;
   private int procExpend = 0;

   Parser(BufferedReader arquivo,String arq) {
      try {
         this.arquivo = arquivo;
         geracaoMemoria = new GeracaoMemoria(arq);
         tabela = new TabelaSimbolo();
         lexico = new AnalisadorLexico(tabela);
         rotuloPJ = new Rotulo();
         
         s = lexico.analisarLexema(lexico.devolve, arquivo);
         if (s == null) { // comentario
            s = lexico.analisarLexema(lexico.devolve, arquivo);
         }
      } catch (Exception e) {
         checkEOF();
         System.out.print(e.getMessage());
      }
   }

   void casaToken(byte token) {
      try {
         if (s != null) {
            if (s.getToken() == token) {
               simboloParaAnalise = s;
               s = lexico.analisarLexema(lexico.devolve, arquivo);
            } else {
               if (lexico.ehEOF) {
                  System.err.println((lexico.linha + 1) + ":Fim de Arquivo nao esperado.");
                  System.exit(0);
               } else {
                  tokenInesperado();
               }
            }
         } else {
            checkEOF();
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println("casaT" + e.toString());
      }
   }

   // S -> {D}+{C}+
   void S() {
      try {
         if (s != null) {
            geracaoMemoria.linhasCF.add("sseg SEGMENT STACK ;in�cio seg. pilha");
            geracaoMemoria.linhasCF.add("  byte 4000h DUP(?) ;dimensiona pilha");
            geracaoMemoria.linhasCF.add("sseg ENDS ;fim seg. pilha");
            geracaoMemoria.linhasCF.add("dseg SEGMENT PUBLIC ;in�cio seg. dados");
            geracaoMemoria.linhasCF.add("  byte 4000h DUP(?) ;tempor�rios");
            endereco = geracaoMemoria.alocarTemp();
            do {
               checkEOF();
               D();
            } while (ehDeclaracao());
            
            geracaoMemoria.linhasCF.add("dseg ENDS ;fim seg. dados");
            geracaoMemoria.linhasCF.add("cseg SEGMENT PUBLIC ;in�cio seg. c�digo");
            geracaoMemoria.linhasCF.add("  ASSUME CS:cseg, DS:dseg");
            geracaoMemoria.linhasCF.add("strt:");
            geracaoMemoria.linhasCF.add("  mov AX, dseg");
            geracaoMemoria.linhasCF.add("  mov ds, AX");
            
            do {
               checkEOF();
               C();
            } while (ehComando());
            
            geracaoMemoria.linhasCF.add("mov ah, 4Ch");
            geracaoMemoria.linhasCF.add("int 21h");
            geracaoMemoria.linhasCF.add("cseg ENDS ;fim seg. c�digo");
            geracaoMemoria.linhasCF.add("END strt ;fim programa");
            
            geracaoMemoria.criarArquivoASM();
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   }

   // D -> VAR {(integer | char) id [D'] ';'}+ | CONST id( = CONSTV' | '['num']' = '"' string '"') ';'
   void D() {
      Simbolo simboloEconst = new Simbolo();
      Simbolo simboloString = new Simbolo();
      Simbolo simboloId = new Simbolo();
      Simbolo simboloConst = new Simbolo();
      Simbolo c = new Simbolo();
      boolean condicao;
      boolean condGC;
      try {
         checkEOF();
         if (s.getToken() == tabela.VAR) {
            casaToken(tabela.VAR);
            while(s.getToken() == tabela.INTEGER ||s.getToken() == tabela.CHAR){
               if (s.getToken() == tabela.INTEGER) {
                  casaToken(tabela.INTEGER);
                  condicao = acaoSemantica9();
               
               } else {
                  casaToken(tabela.CHAR);
                  condicao = acaoSemantica10();
               }
               casaToken(tabela.ID);
               acaoSemantica1(simboloParaAnalise);
               acaoSemantica50(simboloParaAnalise, condicao);
               Simbolo id = simboloParaAnalise;
               Simbolo retornoD1 = D1(simboloParaAnalise);
               casaToken(tabela.PV);
               if(retornoD1.getToken() == -1){
                  condGC=true;
               } else {
                  condGC=false;
               }
            
               geracaoCodigo4(condGC,id);
            }
         } else if (s.getToken() == tabela.CONST) { //CONST id( = CONSTV' | '['num']' = '"' string '"') ';'
            casaToken(tabela.CONST);
            casaToken(tabela.ID);
            acaoSemantica2(simboloParaAnalise);
            simboloId = simboloParaAnalise;
            if (s.getToken() == tabela.ATT) {
               if (s.getToken() == tabela.ATT) {
                  casaToken(tabela.ATT);
                  simboloConst = CONSTV();
                  c = lexico.simbolos.buscaSimbolo(simboloId.getLexema());
                  c.setTipo(simboloConst.getTipo());// acaoSemantica53
                  acaoSemantica56(c, simboloConst);
                  geracaoCodigo1(simboloId,simboloConst);
               }
            } else {
               casaToken(tabela.ACOL);
               casaToken(tabela.VALORCONST); //@TODO NUM
               simboloEconst = simboloParaAnalise;
               acaoSemantica33(simboloEconst);
               acaoSemantica54(simboloEconst, simboloId);
               casaToken(tabela.FCOL);
               casaToken(tabela.ATT);
               // casaToken(tabela.ASPAS);
               casaToken(tabela.VALORCONST); // @TODO STRING
               simboloString = simboloParaAnalise;
               acaoSemantica48(simboloEconst, simboloString);
               acaoSemantica55(simboloString, simboloId);
               // casaToken(tabela.ASPAS);
               geracaoCodigo2(simboloId,simboloString,simboloEconst);
               
            }
            casaToken(tabela.PV);
         }else {
            tokenInesperado();
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   }

   // D' -> [= CONSTV]{,id[ = CONSTV | '['num']']} | '['num']'{,id[ = CONSTV |
   // '['num']']}
   Simbolo D1(Simbolo id) {
      Simbolo D1 = new Simbolo();
      Simbolo temp;
      Simbolo simboloEvet = new Simbolo();
      Simbolo simboloId = new Simbolo();
      boolean condGC = false;
      try {
         checkEOF();
         if (s.getToken() == tabela.ATT || s.getToken() == tabela.ACOL || s.getToken() == tabela.VIR) {
            if (s.getToken() == tabela.ATT) {
               casaToken(tabela.ATT);
               temp = CONSTV();
               D1 = temp;
               acaoSemantica42(id, temp);
               geracaoCodigo1(id,temp);
               if (s.getToken() == tabela.VIR) {
                  while (s.getToken() != tabela.PV) {
                     casaToken(tabela.VIR);
                     casaToken(tabela.ID);
                     acaoSemantica1(simboloParaAnalise);
                     acaoSemantica51(id, simboloParaAnalise);
                     simboloId = simboloParaAnalise;
                     condGC = acaoSemantica10();
                     if (s.getToken() == tabela.ACOL || s.getToken() == tabela.ATT) {
                        condGC = acaoSemantica9();
                        if (s.getToken() == tabela.ACOL) {
                           casaToken(tabela.ACOL);
                           casaToken(tabela.VALORCONST);
                           simboloEvet = simboloParaAnalise;
                           acaoSemantica33(simboloEvet);
                           acaoSemantica41(id, simboloEvet);
                           acaoSemantica54(simboloEvet, simboloId);
                           casaToken(tabela.FCOL);
                           geracaoCodigo3(simboloId,simboloEvet);
                        } else {
                           casaToken(tabela.ATT);
                           temp = CONSTV();
                           acaoSemantica42(id, temp);
                           geracaoCodigo1(simboloId,temp);
                        }
                     }
                     geracaoCodigo4(condGC,simboloId);
                  }
               }
               /*if(condGC == false){
                  geracaoCodigo4(retornoD1, id);
               }*/
            } else if (s.getToken() == tabela.VIR) {
               geracaoCodigo4(true,id);
               D1.setToken((byte)999); // pois assim numa concatenacao retorna algo <> de -1 
               while (s.getToken() != tabela.PV) {
                  casaToken(tabela.VIR);
                  casaToken(tabela.ID);
                  acaoSemantica1(simboloParaAnalise);
                  acaoSemantica51(id, simboloParaAnalise);
                  simboloId = simboloParaAnalise;
                  condGC = acaoSemantica10();
                  if (s.getToken() == tabela.ACOL || s.getToken() == tabela.ATT) {
                     condGC = acaoSemantica9();
                     if (s.getToken() == tabela.ACOL) {
                        casaToken(tabela.ACOL);
                        casaToken(tabela.VALORCONST);
                        simboloEvet = simboloParaAnalise;
                        acaoSemantica33(simboloEvet);
                        acaoSemantica41(id, simboloEvet);
                        acaoSemantica54(simboloEvet, simboloId);
                        casaToken(tabela.FCOL);
                        geracaoCodigo3(simboloId,simboloEvet);
                     } else {
                        casaToken(tabela.ATT);
                        temp = CONSTV();
                        acaoSemantica42(id, temp);
                        geracaoCodigo1(simboloId,temp);
                     }
                  }
                  geracaoCodigo4(condGC,simboloId);
               }
            } else if (s.getToken() == tabela.ACOL) {
               casaToken(tabela.ACOL);
               casaToken(tabela.VALORCONST);
               simboloEvet = simboloParaAnalise;
               D1 = simboloEvet;
               acaoSemantica33(simboloEvet);
               acaoSemantica54(simboloEvet, id);
               acaoSemantica41(id, simboloEvet);
               casaToken(tabela.FCOL);
               geracaoCodigo3(id,D1);
               if (s.getToken() == tabela.VIR) {
                  while (s.getToken() != tabela.PV) {
                     casaToken(tabela.VIR);
                     casaToken(tabela.ID);
                     acaoSemantica1(simboloParaAnalise);
                     acaoSemantica51(id, simboloParaAnalise);
                     simboloId = simboloParaAnalise;
                     condGC = acaoSemantica10();
                     if (s.getToken() == tabela.ACOL || s.getToken() == tabela.ATT) {
                        condGC = acaoSemantica9();
                        if (s.getToken() == tabela.ACOL) {
                           casaToken(tabela.ACOL);
                           casaToken(tabela.VALORCONST);
                           simboloEvet = simboloParaAnalise;
                           //simboloEvet = E();
                           acaoSemantica33(simboloEvet);
                           acaoSemantica41(id, simboloEvet);
                           acaoSemantica54(simboloEvet, simboloId);
                           casaToken(tabela.FCOL);
                           geracaoCodigo3(simboloId,simboloEvet);
                        } else {
                           casaToken(tabela.ATT);
                           temp = CONSTV();
                           acaoSemantica42(id, temp);
                           geracaoCodigo1(simboloId,temp);
                        }
                     }
                     geracaoCodigo4(condGC,simboloId);
                  }
               }
            }
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
      return D1;
   }

   // CONSTV -> 0x(hexa)(hexa) | char | E
   Simbolo CONSTV() {
      Simbolo constvSimbolo = new Simbolo();
      try {
         checkEOF();
         if (s.getToken() == tabela.VALORCONST) {
            casaToken(tabela.VALORCONST);
            constvSimbolo = simboloParaAnalise; // NOVA acaoSemantica44 e //acaoSemantica45 sera que da certo?
            //constvSimbolo.setTipo(simboloParaAnalise.getTipo()); // acaoSemantica44 e //acaoSemantica45
         } else {
            constvSimbolo = E(); // acaoSemantica43
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
      return constvSimbolo;
   }

   // CONSTV' -> 0x(hexa)(hexa) | char | [-] num
   void CONSTV1() {
      try {
         checkEOF();
         if (s.getToken() == tabela.VALORCONST) { // @TODO Como pegar o 0 ?
            casaToken(tabela.VALORCONST); // HEXA
            // casaToken(tabela.X); // @TODO Como pegar o X ?
            // casaToken(tabela.HEXA); // @TODO Como pegar os hexa ?
            // casaToken(tabela.HEXA); // @TODO Como pegar os hexa ?
         } else if (s.getToken() == tabela.CHAR) {
            casaToken(tabela.CHAR);
         } else if (s.getToken() == tabela.SUB || s.getToken() == tabela.VALORCONST) {
            if (s.getToken() == tabela.SUB) {
               casaToken(tabela.SUB);
            }
            casaToken(tabela.VALORCONST);
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   }

   // C -> id C' ';'| FOR id = E to E [step num] do C'' | if E then C''' | ';' |
   // readln'('id')'';' | write'('E{,E}')'';' | writeln'('E{,E}')'';'
   void C() {
      Simbolo simboloEfor = new Simbolo();
      Simbolo simboloE2for = new Simbolo();
      Simbolo simboloid2 = new Simbolo();
      Simbolo simboloId = new Simbolo();
      Simbolo simboloEif = new Simbolo();
      Simbolo simboloEvet = new Simbolo();
      Simbolo simboloEwr = new Simbolo();
      boolean condicao;
      try {
         checkEOF();
         if (s.getToken() == tabela.ID) {
            casaToken(tabela.ID);
            acaoSemantica3(simboloParaAnalise);
            A(simboloParaAnalise);
            casaToken(tabela.PV);
         } else if (s.getToken() == tabela.FOR) {
            casaToken(tabela.FOR);
            casaToken(tabela.ID);
            acaoSemantica3(simboloParaAnalise);
            acaoSemantica6(simboloParaAnalise);
            simboloId = simboloParaAnalise;
            casaToken(tabela.ATT);
            simboloEfor = E();
            String rotInicio = rotuloPJ.novoRotulo(); // GeracaoCodigo28
            String rotFim = rotuloPJ.novoRotulo(); // GeracaoCodigo28
            //geracaoMemoria.zerarTemp();
            geracaoCodigo29(simboloId, simboloEfor);
            acaoSemantica31(simboloEfor, simboloId);
            acaoSemantica61(simboloEfor);
            // casaToken(tabela.VALORCONST); // @TODOVITAO AQUI DEVERIA SER E()
            casaToken(tabela.TO);
            /*
             * if(s.getToken() == tabela.ID) { casaToken(tabela.ID);
             * acaoSemantica3(simboloParaAnalise); simboloid2 = simboloParaAnalise;
             * acaoSemantica32(simboloEfor,simboloid2,simboloId); } else {
             */
            simboloE2for = E();
            //geracaoMemoria.zerarTemp();
            // casaToken(tabela.VALORCONST); // @TODOVITAO AQUI DEVERIA SER E()
            acaoSemantica32(simboloEfor, simboloE2for, simboloId);
            acaoSemantica61(simboloE2for);
            geracaoCodigo25(rotInicio);
            geracaoCodigo30(simboloId, simboloE2for, rotFim);
            // }
            if (s.getToken() == tabela.STEP) {
               casaToken(tabela.STEP);
               casaToken(tabela.VALORCONST); // @TODO Como pegar o num ?
               // acaoSemantica3(simboloParaAnalise);
               // acaoSemantica36(simboloParaAnalise);
               // acaoSemantica34(); // n�o implementada a 34
            }
            casaToken(tabela.DO);
            H(rotInicio, simboloId);
            geracaoCodigo25(rotFim);
         } else if (s.getToken() == tabela.IF) {
            casaToken(tabela.IF);
            simboloEif = E();
            //geracaoMemoria.zerarTemp();
            acaoSemantica35(simboloEif);
            String rotuloFalse = rotuloPJ.novoRotulo();
            String rotuloFim = rotuloPJ.novoRotulo();
            geracaoCodigo23(simboloEif, rotuloFalse);
            casaToken(tabela.THEN);
            J(rotuloFalse, rotuloFim);
            geracaoCodigo25(rotuloFim);
            // casaToken(tabela.PV);
         } else if (s.getToken() == tabela.PV) {
            casaToken(tabela.PV);
         } else if (s.getToken() == tabela.READLN) {
            casaToken(tabela.READLN);
            casaToken(tabela.APAR);
            casaToken(tabela.ID);
            simboloId = simboloParaAnalise;
            acaoSemantica3(simboloParaAnalise);
            acaoSemantica6(simboloParaAnalise);
            condicao = acaoSemantica9();
            if (s.getToken() == tabela.ACOL) {
               casaToken(tabela.ACOL);
               condicao = acaoSemantica10();
               simboloEvet = E();
               acaoSemantica33(simboloEvet);
               // acaoSemantica41(simboloId,simboloEvet); nao eh mais necessaria
               casaToken(tabela.FCOL);
            }
            acaoSemantica62(simboloId, condicao); // ESCREVER EM UM VETOR POR COMPLETO
            casaToken(tabela.FPAR);
            casaToken(tabela.PV);
         } else if (s.getToken() == tabela.WRITELN) {
            int tempString = geracaoMemoria.novoTemp();
            casaToken(tabela.WRITELN);
            casaToken(tabela.APAR);
            simboloEwr = E();
            acaoSemantica52(simboloEwr);
            geracaoCodigo20(simboloEwr,tempString);
            //geracaoCodigo22();
            while (s.getToken() == tabela.VIR) {
               tempString = geracaoMemoria.novoTemp();
               casaToken(tabela.VIR);
               simboloEwr = E();
               acaoSemantica52(simboloEwr);
               geracaoCodigo20(simboloEwr,tempString);
            }
            casaToken(tabela.FPAR);
            casaToken(tabela.PV);
            geracaoCodigo21();
            //geracaoMemoria.zerarTemp();
         } else if (s.getToken() == tabela.WRITE) {
            int tempString = geracaoMemoria.novoTemp();
            casaToken(tabela.WRITE);
            casaToken(tabela.APAR);
            simboloEwr = E();
            acaoSemantica52(simboloEwr);
            geracaoCodigo20(simboloEwr,tempString);
            //geracaoCodigo22();
            while (s.getToken() == tabela.VIR) {
               tempString = geracaoMemoria.novoTemp();
               casaToken(tabela.VIR);
               simboloEwr = E();
               acaoSemantica52(simboloEwr);
               geracaoCodigo20(simboloEwr,tempString);
               //geracaoCodigo22();
            }
            casaToken(tabela.FPAR);
            casaToken(tabela.PV);
            //geracaoMemoria.zerarTemp();
         } else {
            tokenInesperado();
         }
      
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
      //geracaoMemoria.zerarTemp();
   }

   // A -> = E | '['E']' = E
   void A(Simbolo id) {
      Simbolo simboloA = new Simbolo();
      Simbolo simboloA1 = new Simbolo();
      Simbolo simboloA2 = new Simbolo();
   
      try {
         checkEOF();
      
         if (s.getToken() == tabela.ATT) {
            casaToken(tabela.ATT);
            acaoSemantica5(id);
            simboloA = E(); // acaoSemantica49
            //geracaoMemoria.zerarTemp();
            acaoSemantica57(id, simboloA);
            acaoSemantica59(id, simboloA);
            geracaoMemoria.linhasCF.add("mov AX, DS:[" + procExpend + "] ; peguei o end do exp talvez"+simboloA.getEndereco()+" << end do simboloA");
            geracaoMemoria.linhasCF.add("mov DS:[" + id.getEndereco() + "], AX; salvando o valor no endereco correto");
         } else {
            acaoSemantica65(id);
            casaToken(tabela.ACOL);
            simboloA1 = E();
            //geracaoMemoria.zerarTemp();
            acaoSemantica5(id);
            acaoSemantica64(id, simboloA1);
            casaToken(tabela.FCOL);
            geracaoCodigo7_1(id,simboloA1);
            casaToken(tabela.ATT);
            simboloA2 = E(); // acaoSemantica49
            //geracaoMemoria.zerarTemp();
            acaoSemantica60(id, simboloA2);
            geracaoMemoria.linhasCF.add("mov AX, DS:[" + procExpend + "] ; peguei o end do exp talvez"+simboloA2.getEndereco()+" << end do simboloA");
            geracaoMemoria.linhasCF.add("mov DS:[" + id.getEndereco() + "], AX; salvando o valor no endereco correto");
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   }

   // H -> C | '{' {C} '}'
   void H(String rotInicio, Simbolo contador) {
      try {
         checkEOF();
      
         if (s.getToken() == tabela.ACHAVE) {
            casaToken(tabela.ACHAVE);
            while (ehComando()) {
               C();
            }
            geracaoCodigo31(contador);
            geracaoCodigo27(rotInicio);
            casaToken(tabela.FCHAVE);
         } else {
            C();
            geracaoCodigo27(rotInicio);
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   }

   // J -> C [else ('{' {C} '}' || C)] | '{' {C} '}' [else ('{' {C} '}' || C)]
   void J(String rotuloFalse, String rotuloFim) {
      try {
         checkEOF();
      
         if (s.getToken() == tabela.ACHAVE) {
            casaToken(tabela.ACHAVE);
            do {
               C();
            
            } while (ehComando());
            geracaoCodigo27(rotuloFim);
            casaToken(tabela.FCHAVE);
            if (s != null && s.getToken() == tabela.ELSE) { // caso o opcional seja no EOF
               casaToken(tabela.ELSE);
               geracaoCodigo25(rotuloFalse);
               if (s.getToken() == tabela.ACHAVE) {
                  casaToken(tabela.ACHAVE);
                  do {
                     C();
                  } while (ehComando());
                  casaToken(tabela.FCHAVE);
               } else {
                  C();
               }
            }
         } else {
            C();
            geracaoCodigo27(rotuloFim);
            if (s != null && s.getToken() == tabela.ELSE) {
               casaToken(tabela.ELSE);
               geracaoCodigo25(rotuloFalse);
               if (s.getToken() == tabela.ACHAVE) {
                  casaToken(tabela.ACHAVE);
                  C();
                  casaToken(tabela.FCHAVE);
               } else {
                  C();
               }
            }
         }
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   }

   // E -> E' {('<' | '>' | '<=' | '>=' | '<>' | '=') E'}
   Simbolo E() {
      Simbolo simboloE = new Simbolo();
      Simbolo simboloE2 = new Simbolo();
      Simbolo simboloCloneE = new Simbolo();
      Simbolo simboloCloneE2 = new Simbolo();
      boolean condicao;
      int operacao = 0; /* 1 > / 2 < / 3 >=  / 4 <= / 5 <> / 6 = */
      try {
         checkEOF();
      
         simboloE = E1(); // acaoSemantica7
         procExpend = procExpsend; // geracaoCodigo17
         simboloCloneE = new Simbolo(simboloE.getToken(), simboloE.getLexema(), simboloE.getEndereco(),
               simboloE.getTipo(), simboloE.getClasse(), simboloE.getTamanho());
         if (s.getToken() == tabela.MAIOR || s.getToken() == tabela.MENOR || s.getToken() == tabela.MAIORIG
               || s.getToken() == tabela.MENORIG || s.getToken() == tabela.DIFF || s.getToken() == tabela.ATT) {
            condicao = acaoSemantica9();
            /* 1 > / 2 < / 3 >=  / 4 <= / 5 <> / 6 = */
            if (s.getToken() == tabela.MAIOR) {
               casaToken(tabela.MAIOR);
               acaoSemantica8(simboloE);
               operacao = 1;
            } else if (s.getToken() == tabela.MENOR) {
               casaToken(tabela.MENOR);
               acaoSemantica8(simboloE);
               operacao = 2;
            } else if (s.getToken() == tabela.MAIORIG) {
               casaToken(tabela.MAIORIG);
               acaoSemantica8(simboloE);
               operacao = 3;
            } else if (s.getToken() == tabela.MENORIG) {
               casaToken(tabela.MENORIG);
               acaoSemantica8(simboloE);
               operacao = 4;
            } else if (s.getToken() == tabela.DIFF) {
               casaToken(tabela.DIFF);
               acaoSemantica8(simboloE);
               operacao = 5;
            } else if (s.getToken() == tabela.ATT) {
               casaToken(tabela.ATT);
               condicao = acaoSemantica10();
               operacao = 6;
            }
         
            simboloE2 = E1();
            simboloCloneE2 = new Simbolo(simboloE2.getToken(), simboloE2.getLexema(), simboloE2.getEndereco(),
                  simboloE2.getTipo(), simboloE2.getClasse(), simboloE2.getTamanho());
            acaoSemantica11(simboloCloneE, simboloCloneE2);
            acaoSemantica12(simboloCloneE, condicao);
            acaoSemantica63(simboloCloneE, simboloCloneE2, condicao);
            geracaoCodigo18(operacao);
            simboloCloneE.setTipo("tipo_logico"); // acaoSemantica47
            return simboloCloneE;
         }
      
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   
      return simboloE;
   }

   // E' -> [+ | -] E'' {('+' | '-' | or ) E''}
   Simbolo E1() {
      Simbolo simboloE1 = new Simbolo();
      Simbolo simboloE1_2 = new Simbolo();
      Simbolo simboloCloneE1 = new Simbolo();
      Simbolo simboloCloneE1_2 = new Simbolo();
      boolean condicao;
      int operacao = 0; /* 1 para add , 2 para sub , 3 para or, 0 default */
      try {
         checkEOF();
         condicao = acaoSemantica9();
         if (s.getToken() == tabela.ADD) {
            casaToken(tabela.ADD);
            condicao = acaoSemantica10();
         } else if (s.getToken() == tabela.SUB) {
            casaToken(tabela.SUB);
            condicao = acaoSemantica10();
         }
         simboloE1 = E2(); // acaoSemantica14
         geracaoCodigo15(condicao);
         
         procExpsend = procTend;
         simboloCloneE1 = new Simbolo(simboloE1.getToken(), simboloE1.getLexema(), simboloE1.getEndereco(),
               simboloE1.getTipo(), simboloE1.getClasse(), simboloE1.getTamanho());
         acaoSemantica13(simboloE1, condicao);
         while (s.getToken() == tabela.ADD || s.getToken() == tabela.SUB || s.getToken() == tabela.OR
               || s.getToken() == tabela.MUL) {
            // if (s.getToken() == tabela.ADD || s.getToken() == tabela.SUB || s.getToken()
            // == tabela.OR) {
            if (s.getToken() == tabela.ADD) {
               casaToken(tabela.ADD);
               operacao = acaoSemantica15(simboloE1);
            } else if (s.getToken() == tabela.SUB) {
               casaToken(tabela.SUB);
               operacao = acaoSemantica16(simboloE1);
            } else {
               casaToken(tabela.OR);
               operacao = acaoSemantica17(simboloE1);
            }
            int Tend = procTend;
            simboloE1_2 = E2();
            simboloCloneE1_2 = new Simbolo(simboloE1_2.getToken(), simboloE1_2.getLexema(), simboloE1_2.getEndereco(),
                  simboloE1_2.getTipo(), simboloE1_2.getClasse(), simboloE1_2.getTamanho());
            acaoSemantica18(simboloCloneE1, simboloCloneE1_2);
            acaoSemantica19(simboloCloneE1_2, operacao);
            geracaoCodigo16(operacao);
         }
      
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   
      return simboloE1;
   
   }

   // E'' -> F {('*' | '/' | '%' | and) F}
   Simbolo E2() {
      Simbolo simboloE2 = new Simbolo();
      Simbolo simboloE2_1 = new Simbolo();
      Simbolo simboloCloneE2 = new Simbolo();
      Simbolo simboloCloneE2_1 = new Simbolo();
      int operador = 0;
      
      try {
         checkEOF();
      
         simboloE2 = F(); // acaoSemantica20
         simboloCloneE2 = new Simbolo(simboloE2.getToken(), simboloE2.getLexema(), simboloE2.getEndereco(),
               simboloE2.getTipo(), simboloE2.getClasse(), simboloE2.getTamanho());
         procTend = procFend; // geracaoCodigo14
         while (s.getToken() == tabela.MUL || s.getToken() == tabela.DIV || s.getToken() == tabela.MOD
               || s.getToken() == tabela.AND) {
            // if (s.getToken() == tabela.MUL || s.getToken() == tabela.DIV || s.getToken()
            // == tabela.MOD || s.getToken() == tabela.AND) {
            if (s.getToken() == tabela.MUL) {
               casaToken(tabela.MUL);
               operador = acaoSemantica21(simboloE2);
            } else if (s.getToken() == tabela.DIV) {
               casaToken(tabela.DIV);
               operador = acaoSemantica22(simboloE2);
            } else if (s.getToken() == tabela.MOD) {
               casaToken(tabela.MOD);
               operador = acaoSemantica23(simboloE2);
            } else {
               casaToken(tabela.AND);
               operador = acaoSemantica24(simboloE2);
            }
            simboloE2_1 = F();
            simboloCloneE2_1 = new Simbolo(simboloE2_1.getToken(), simboloE2_1.getLexema(), simboloE2_1.getEndereco(),
                  simboloE2_1.getTipo(), simboloE2_1.getClasse(), simboloE2_1.getTamanho());
            acaoSemantica25(simboloCloneE2, simboloCloneE2_1);
            acaoSemantica26(simboloCloneE2_1, operador);
            geracaoCodigo19(operador);
         }
      
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   
      return simboloE2;
   
   }

   // F -> '(' E ')' | not F | id ['[' E ']']| num
   Simbolo F() {
      Simbolo simboloF = new Simbolo(); // simbolo que vai ser retornado
      Simbolo simboloCloneF = new Simbolo();
      Simbolo simboloF1 = new Simbolo();
      Simbolo simboloE = new Simbolo();
      try {
      
         checkEOF();
      
         if (s.getToken() == tabela.APAR) {
            casaToken(tabela.APAR);
            simboloF = E(); // acaoSemantica27
            procFend = simboloF.getEndereco();
            casaToken(tabela.FPAR);
         } else if (s.getToken() == tabela.NOT) {
            casaToken(tabela.NOT);
            int Fend = procFend;
            simboloF1 = F();
            acaoSemantica28(simboloF1);
            geracaoCodigo13(simboloF1);
            return simboloF1;
         } else if (s.getToken() == tabela.VALORCONST) {
            casaToken(tabela.VALORCONST);
            // \/ acaoSemantica29
            simboloF = new Simbolo(simboloParaAnalise.getToken(), simboloParaAnalise.getLexema(),
                  simboloParaAnalise.getEndereco(), simboloParaAnalise.getTipo(), "classe_variavel", 0);
                 
            geracaoCodigo11(simboloF);
            
         } else {
            casaToken(tabela.ID);
            acaoSemantica3(simboloParaAnalise);
            // acaoSemantica30 
            simboloF = lexico.simbolos.buscaSimbolo(simboloParaAnalise.getLexema());
            procFend = simboloF.getEndereco(); // geracaoCodigo9
            if (s.getToken() == tabela.ACOL) {
               casaToken(tabela.ACOL);
               acaoSemantica38(simboloF);
               // casaToken(tabela.VALORCONST); @TODO VITAO
               simboloE = E();
               acaoSemantica39(simboloF, simboloE);
               simboloCloneF = new Simbolo(simboloF.getToken(), simboloF.getLexema(), simboloF.getEndereco(),
                     simboloF.getTipo(), simboloF.getClasse(), simboloF.getTamanho());
               acaoSemantica58(simboloCloneF);
               casaToken(tabela.FCOL);
               geracaoCodigo7(simboloF);
               return simboloCloneF;
            }
         }
      
      } catch (Exception e) {
         checkEOF();
         System.err.println(e.toString());
      }
   
      return simboloF;
   
   }

   void checkEOF() {
      if (lexico.ehEOF) {
         System.err.println((lexico.linha + 1) + ":Fim de arquivo nao esperado.");
         System.exit(0);
      }
   }

   void tokenInesperado() {
      System.err.println((lexico.linha + 1) + ":Token nao esperado: " + s.getLexema());
      System.exit(0);
   }

   boolean ehDeclaracao() {
      return (s != null && (s.getToken() == tabela.VAR || s.getToken() == tabela.CONST || s.getToken() == tabela.INTEGER
            || s.getToken() == tabela.CHAR));
   }

   boolean ehComando() {
      return (s != null && (s.getToken() == tabela.ID || s.getToken() == tabela.FOR || s.getToken() == tabela.IF
            || s.getToken() == tabela.PV || s.getToken() == tabela.READLN || s.getToken() == tabela.WRITELN
            || s.getToken() == tabela.WRITE));
   }

   void acaoSemantica1(Simbolo simbolo) {
      if (!simbolo.getClasse().equals("")) {
         System.out.println((lexico.linha + 1) + ":identificador ja declarado " + simbolo.getLexema());
         System.exit(0);
      }
   
      simbolo.setClasse("classe_variavel");
   }

   void acaoSemantica2(Simbolo simbolo) {
      if (!simbolo.getClasse().equals("")) {
         System.out.println((lexico.linha + 1) + ":identificador ja declarado " + simbolo.getLexema());
         System.exit(0);
      }
   
      simbolo.setClasse("classe_constante");
   }

   void acaoSemantica3(Simbolo simbolo) {
      if (simbolo.getClasse().equals("")) {
         System.out.println((lexico.linha + 1) + ":identificador nao declarado " + simbolo.getLexema());
         System.exit(0);
      }
   }

   boolean acaoSemantica9() {
      return false;
   }

   boolean acaoSemantica10() {
      return true;
   }

   // Implementada por passagem de parametros do metodo D1
   // void acaoSemantica40(Simbolo id, Simbolo D1) {
   // D1.setTipo(id.getTipo());
   // }

   // Implementada por passagem de parametros do metodo A
   // void acaoSemantica4(Simbolo id, Simbolo A) {
   // D1.setTipo(id.getTipo());
   // }

   void acaoSemantica42(Simbolo simbolo1, Simbolo simbolo2) {
      if (!simbolo1.getTipo().equals(simbolo2.getTipo())) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica50(Simbolo simbolo, boolean condicao) {
      if (condicao) {
         simbolo.setTipo("tipo_caracter");
      } else {
         simbolo.setTipo("tipo_inteiro");
      }
   }

   void acaoSemantica8(Simbolo simbolo) {
      if (simbolo.getTipo() != "tipo_inteiro" && simbolo.getTipo() != "tipo_caracter" || simbolo.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica5(Simbolo simbolo) {
      if (simbolo.getClasse() == "classe_constante") {
         System.out.println((lexico.linha + 1) + ":classe de identificador incompativel " + simbolo.getLexema());
         System.exit(0);
      }
   }

   void acaoSemantica11(Simbolo exps1, Simbolo exps2) {
      if (exps1.getTipo() != exps2.getTipo() /* || exps2.getTamanho() > 0 */) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica12(Simbolo exps1, boolean condicao) {
      if ((exps1.getTipo() == "tipo_string" && condicao == false)) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica13(Simbolo expt1, boolean condicao) {
      if (expt1.getTipo() != "tipo_inteiro" && expt1.getTipo() != "tipo_caracter" && condicao == true /* || expt1.getTamanho() > 0 */) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   int acaoSemantica15(Simbolo expt1) {
      if (expt1.getTipo() != "tipo_inteiro" && expt1.getTipo() != "tipo_caracter" || expt1.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         return 1;
      }
      return 0;
   }

   int acaoSemantica16(Simbolo expt1) {
      if (expt1.getTipo() != "tipo_inteiro" && expt1.getTipo() != "tipo_caracter" || expt1.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         return 2;
      }
      return 0;
   }

   int acaoSemantica17(Simbolo expt1) {
      if (expt1.getTipo() != "tipo_logico") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         return 3;
      }
      return 0;
   }

   void acaoSemantica18(Simbolo expt1, Simbolo expt2) {
      if (expt1.getTipo() != expt2.getTipo() || expt2.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica19(Simbolo expt2, int operacao) {
      /* 1 para add , 2 para sub , 3 para or, 0 default */
      if (expt2.getTipo() != "tipo_logico" && operacao == 3 || expt2.getTipo() != "tipo_inteiro" && expt2.getTipo() != "tipo_caracter" && operacao == 2
            || expt2.getTipo() != "tipo_inteiro" && expt2.getTipo() != "tipo_caracter" && operacao == 1) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica51(Simbolo pai, Simbolo filhoID) {
      filhoID.setTipo(pai.getTipo());
   }

   int acaoSemantica21(Simbolo f1) {
      if (f1.getTipo() != "tipo_inteiro" && f1.getTipo() != "tipo_caracter" || f1.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         return 1;
      }
      return 0;
   }

   int acaoSemantica22(Simbolo f1) {
      if (f1.getTipo() != "tipo_inteiro" && f1.getTipo() != "tipo_caracter" || f1.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         return 2;
      }
      return 0;
   }

   int acaoSemantica23(Simbolo f1) {
      if (f1.getTipo() != "tipo_inteiro" && f1.getTipo() != "tipo_caracter" || f1.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         return 3;
      }
      return 0;
   }

   int acaoSemantica24(Simbolo f1) {
      if (f1.getTipo() != "tipo_logico") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         return 4;
      }
      return 0;
   }

   void acaoSemantica25(Simbolo f1, Simbolo f2) {
      if (f1.getTipo() != f2.getTipo() || f2.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica26(Simbolo f2, int operacao) {
      /* 1 para mul , 2 para div , 3 para mod, 4 para and, 0 default */
      if (f2.getTipo() != "tipo_logico" && operacao == 4 || f2.getTipo() != "tipo_caracter" &&  f2.getTipo() != "tipo_inteiro" && operacao == 3
            || f2.getTipo() != "tipo_caracter" && f2.getTipo() != "tipo_inteiro" && operacao == 2 || f2.getTipo() != "tipo_caracter" && f2.getTipo() != "tipo_inteiro" && operacao == 1) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica28(Simbolo f1) {
      if (f1.getTipo() != "tipo_logico") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica6(Simbolo id) {
      if (id.getClasse() == "classe_constante") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica38(Simbolo id) {
      if (id.getTamanho() <= 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis [tamanho] ");
         System.exit(0);
      }
   }

   void acaoSemantica39(Simbolo id, Simbolo E) {
      if (E.getTipo() != "tipo_inteiro") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else {
         if (Integer.parseInt(E.getLexema()) > id.getTamanho()) {
            System.out.println((lexico.linha + 1) + ":tamanho do vetor excede o maximo permitido.");
            System.exit(0);
         }
      }
   }

   void acaoSemantica31(Simbolo E1, Simbolo id) {
      if (E1.getTipo() != "tipo_inteiro" || E1.getTipo() != id.getTipo()) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica32(Simbolo E1, Simbolo E2, Simbolo id) {
      if (E2.getTipo() != "tipo_inteiro" || E1.getTipo() != id.getTipo()) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica33(Simbolo E) {
      if (E.getTipo() != "tipo_inteiro" && E.getClasse() != "classe_variavel") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica34(Simbolo E) {
      if (Integer.parseInt(E.getLexema()) < 1) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica35(Simbolo E) {
      if (E.getTipo() != "tipo_logico") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica36(Simbolo E) {
      if (E.getTipo() != "tipo_inteiro" || E.getClasse() != "classe_constante") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica41(Simbolo D, Simbolo E) {
      if (D.getTipo() == "tipo_inteiro") {
         if (Integer.parseInt(E.getLexema()) > 2048) {
            System.out.println((lexico.linha + 1) + ":tamanho do vetor excede o maximo permitido.");
            System.exit(0);
         }
      } else if (D.getTipo() == "tipo_caracter") {
         if (Integer.parseInt(E.getLexema()) > 4096) {
            System.out.println((lexico.linha + 1) + ":tamanho do vetor excede o maximo permitido.");
            System.exit(0);
         }
      } else {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica48(Simbolo E, Simbolo string) {
      if ((string.getLexema().length() - 2) > Integer.parseInt(E.getLexema())) {
         System.out.println((lexico.linha + 1) + ":tamanho do vetor excede o maximo permitido.");
         System.exit(0);
      }
   }

   void acaoSemantica52(Simbolo E) {
      /*if (E.getTipo() != "tipo_inteiro" && E.getTipo() != "tipo_caracter" && E.getTipo() != "tipo_string") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }*/
   }

   void acaoSemantica54(Simbolo E, Simbolo id) {
      if (E.getTipo() == "tipo_inteiro") {
         id = lexico.simbolos.buscaSimbolo(id.getLexema());
         id.setTamanho(Integer.parseInt(E.getLexema()));
      } else {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica55(Simbolo E, Simbolo id) {
      id = lexico.simbolos.buscaSimbolo(id.getLexema());
      id.setTipo(E.getTipo());
   }

   void acaoSemantica56(Simbolo id, Simbolo string) {
      if (id.getTamanho() <= 0 && string.getTipo() == "tipo_string") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica57(Simbolo id, Simbolo string) {
      if (id.getTipo() == "tipo_caracter" && string.getTipo() == "tipo_string") {
         if ((string.getLexema().length() - 3) > id.getTamanho()) {
            if (id.getTamanho() == 0) {
               System.out.println((lexico.linha + 1) + ":tipos incompativeis");
               System.exit(0);
            } else {
               System.out.println((lexico.linha + 1) + ":tamanho do vetor excede o maximo permitido.");
               System.exit(0);
            }
         }
      
      } else if (id.getTipo() != string.getTipo()) {
         if (string.getTipo() == "tipo_logico") {
            Simbolo x = lexico.simbolos.buscaSimbolo(string.getLexema());
            if (x.getTipo() == string.getTipo() && x.getTipo() == "tipo_logico") {
               System.out.println((lexico.linha + 1) + ":tipos incompativeis");
               System.exit(0);
            } else {
               return;
            }
         }
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica58(Simbolo E) {
      E.setTamanho(0);
   }

   void acaoSemantica59(Simbolo id, Simbolo E) {
      if (id.getTamanho() > 0 && id.getTipo() == "tipo_inteiro") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      } else if (id.getTamanho() > 0 && id.getTipo() == "tipo_caracter" && E.getTipo() != "tipo_string") {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica60(Simbolo id, Simbolo string) {
      if ((id.getTipo() == "tipo_caracter" && string.getTipo() == "tipo_string")
            || (id.getTipo() == "tipo_inteiro" && string.getTipo() == "tipo_string")
            || (id.getTipo() == "tipo_caracter" && string.getTipo() != "tipo_caracter")
            || ((id.getTipo() == "tipo_inteiro" && string.getTipo() == "tipo_inteiro" && string.getTamanho() > 0))) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica61(Simbolo E) {
      if (E.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica62(Simbolo id, boolean condicao) {
      if (condicao == false && id.getTamanho() > 0 && id.getTipo().equals("tipo_inteiro")) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis");
         System.exit(0);
      }
   }

   void acaoSemantica63(Simbolo E1, Simbolo E2, boolean condicao) {
      if (condicao == true) {
         if ((E1.getTipo() == "tipo_inteiro" && E1.getTamanho() > 0)
               || (E1.getTipo() == "tipo_inteiro" && E1.getTamanho() > 0)) {
            System.out.println((lexico.linha + 1) + ":tipos incompativeis");
            System.exit(0);
         }
      }
   }

   void acaoSemantica64(Simbolo id, Simbolo E) {
      if (E.getTipo().equals("tipo_inteiro") && E.getTamanho() > 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis.");
         System.exit(0);
      }
   }

   void acaoSemantica65(Simbolo id) {
      if (id.getTamanho() == 0) {
         System.out.println((lexico.linha + 1) + ":tipos incompativeis.");
         System.exit(0);
      }
   }
   
   void geracaoCodigo1(Simbolo id, Simbolo constV){
      if(constV.getTipo().equals("tipo_inteiro")){
         endereco = geracaoMemoria.alocarTipoInteiro();
         id.setEndereco(endereco);
         geracaoMemoria.linhasCF.add("  sword " + constV.getLexema() + "       ;"+id.getClasse()+" inteiro " + id.getLexema()+" em "+id.getEndereco()+"");
      } else if(constV.getTipo().equals("tipo_caracter")){
         endereco = geracaoMemoria.alocarTipoChar();
         id.setEndereco(endereco);
         if(constV.getLexema().contains("0x")){
            int value = Integer.parseInt(constV.getLexema().substring(2,4), 16);  
            geracaoMemoria.linhasCF.add("  byte " + value + "     ;"+id.getClasse()+" char " + id.getLexema()+" em "+id.getEndereco()+"");
         } else {
            geracaoMemoria.linhasCF.add("  byte " + constV.getLexema() + "     ;"+id.getClasse()+" char " + id.getLexema()+" em "+id.getEndereco()+"");
         }
      }
   }
   
   void geracaoCodigo2(Simbolo id, Simbolo constV, Simbolo Evet){
      if(constV.getTipo().equals("tipo_string")){
         endereco = geracaoMemoria.alocarTipoString(constV.getLexema().length());
         id.setEndereco(endereco);
         geracaoMemoria.linhasCF.add("  byte " + Evet.getLexema() + " DUP("+constV.getLexema()+")     ;"+id.getClasse()+" string " + id.getLexema()+" em "+id.getEndereco()+"");
      }
   }
   
   void geracaoCodigo3(Simbolo id, Simbolo E){
      if(id.getTipo().equals("tipo_inteiro")){
         endereco = geracaoMemoria.alocarTipoInteiro(Integer.parseInt(E.getLexema()));
         id.setEndereco(endereco);
         geracaoMemoria.linhasCF.add("  sword " + E.getLexema() + " DUP(?)      ;"+id.getClasse()+" vet inteiro " + id.getLexema()+" em "+id.getEndereco()+"");
      } else if(id.getTipo().equals("tipo_caracter")){
         endereco = geracaoMemoria.alocarTipoChar(Integer.parseInt(E.getLexema()));
         id.setEndereco(endereco);
         geracaoMemoria.linhasCF.add("  byte " + E.getLexema() + " DUP(?)       ;"+id.getClasse()+" vet char " + id.getLexema()+" em "+id.getEndereco()+"");
      } 
   }
   
   void geracaoCodigo4(boolean condGC, Simbolo id){
      if(condGC){ // como se fosse flag, ou seja n�o entrou no D1
         if(id.getTipo().equals("tipo_inteiro")){
            endereco = geracaoMemoria.alocarTipoInteiro();
            id.setEndereco(endereco);
            geracaoMemoria.linhasCF.add("  sword ?     ;"+id.getClasse()+" inteiro " + id.getLexema()+" em "+id.getEndereco()+"");
         } else if(id.getTipo().equals("tipo_caracter")){
            endereco = geracaoMemoria.alocarTipoChar();
            id.setEndereco(endereco);
            geracaoMemoria.linhasCF.add("  byte ?      ;"+id.getClasse()+" char " + id.getLexema()+" em "+id.getEndereco()+"");
         }
      }
   }
   
   void geracaoCodigo11(Simbolo f1){
      int value = -999;
      if(f1.getLexema().contains("0x")){
         value = Integer.parseInt(f1.getLexema().substring(2,4), 16); 
         //endereco = geracaoMemoria.alocarTempTipoChar();
      } else if(f1.getLexema().contains("'")){
               //int value = simboloF.getLexema().substring(1,2); 
         value = f1.getLexema().charAt(1);
         //endereco = geracaoMemoria.alocarTempTipoChar();
      } else if(f1.getTipo().equals("tipo_string")){
         value = -999;
      } else {
         value = Integer.parseInt(f1.getLexema()); 
         //endereco = geracaoMemoria.alocarTempTipoInteiro();
      }
            
      if(f1.getTipo().equals("tipo_string")){
         endereco = geracaoMemoria.alocarTempTipoString(f1.getLexema().length());
         procFend = geracaoMemoria.novoTemp();
         
         geracaoMemoria.linhasCF.add("dseg SEGMENT PUBLIC");
      		
         geracaoMemoria.linhasCF.add("byte " + f1.getLexema()+"; constante string");
      		
         geracaoMemoria.linhasCF.add("dseg ENDS");       
         //geracaoMemoria.linhasCF.add("mov AX, "+f1.getLexema()+" ; movi para AX um VALORCONST");
         //geracaoMemoria.linhasCF.add("mov DS:[" + procFend + "], AX ;MOVI PARA END o CONTEUDO DE AX");
      } else {
         procFend = geracaoMemoria.novoTemp();
         geracaoMemoria.linhasCF.add("mov AX, "+value+" ; movi para AX um VALORCONST");
         geracaoMemoria.linhasCF.add("mov DS:[" + procFend + "], AX ;MOVI PARA END o CONTEUDO DE AX");
      }
      
      if(f1.getLexema().contains("0x")){
         value = Integer.parseInt(f1.getLexema().substring(2,4), 16); 
         endereco = geracaoMemoria.alocarTempTipoChar();
      } else if(f1.getLexema().contains("'")){
               //int value = simboloF.getLexema().substring(1,2); 
         value = f1.getLexema().charAt(1);
         endereco = geracaoMemoria.alocarTempTipoChar();
      } else if(f1.getTipo().equals("tipo_string")){
         value = -999;
      } else {
         value = Integer.parseInt(f1.getLexema()); 
         endereco = geracaoMemoria.alocarTempTipoInteiro();
      }
      
      f1.setEndereco(endereco);
   }
   
   void geracaoCodigo13(Simbolo simboloF1){
      int Fend = geracaoMemoria.novoTemp();
      geracaoMemoria.linhasCF.add("mov AX, DS:[" + procFend + "] ;"+simboloF1.getLexema()+" em "+simboloF1.getEndereco());
      geracaoMemoria.linhasCF.add("neg AX");
      geracaoMemoria.linhasCF.add("add AX,1");
      geracaoMemoria.linhasCF.add("mov DS:[" + Fend + "], AX");
      procFend = Fend; //conferir
   }
   
   void geracaoCodigo15(boolean condicao){
      if(condicao){
         procExpsend = geracaoMemoria.novoTemp();
         geracaoMemoria.linhasCF.add("mov AX, DS:[" + procTend + "] ;");
         geracaoMemoria.linhasCF.add("neg AX");
         geracaoMemoria.linhasCF.add("mov DS:[" + procTend + "], AX");
      }
   }
   
   void geracaoCodigo19(int operador){
      geracaoMemoria.linhasCF.add("mov AX, DS:[" + procTend + "]");
      geracaoMemoria.linhasCF.add("mov BX, DS:[" + procFend + "]");
      if(operador == 2 || operador == 4){
      
         geracaoMemoria.linhasCF.add("cwd");
         geracaoMemoria.linhasCF.add("mov cx, ax ; para realizar divs e mods ");
      			
         geracaoMemoria.linhasCF.add("mov ax, DS:[" + procFend + "] ;");
      			
         geracaoMemoria.linhasCF.add("cwd");
      			
         geracaoMemoria.linhasCF.add("mov bx, ax ;");
      			
         geracaoMemoria.linhasCF.add("mov ax, cx ;");
      }
      switch(operador){   /* 1 para mul , 2 para div , 3 para mod, 4 para and, 0 default */
         case 1:
            geracaoMemoria.linhasCF.add("imul BX ; multiplicacao");
            break;
                  
         case 2:
            geracaoMemoria.linhasCF.add("idiv BX ; divisao");
            geracaoMemoria.linhasCF.add("sub AX, 256; divisao");
            break;
                  
         case 3:
            geracaoMemoria.linhasCF.add("idiv BX ; mod");
            geracaoMemoria.linhasCF.add("sub BX, 256; divisao");
            geracaoMemoria.linhasCF.add("mov AX, BX  ; mod");
            break;  
            
         case 4:
            geracaoMemoria.linhasCF.add("and AX, BX ; and");
            break;            
      }
   
      procTend = geracaoMemoria.novoTemp();
      geracaoMemoria.linhasCF.add("mov DS:[" + procTend + "], ax");
   }
   
   void geracaoCodigo16(int operacao){
      geracaoMemoria.linhasCF.add("mov AX, DS:[" + procExpsend + "]");
      geracaoMemoria.linhasCF.add("mov BX, DS:[" + procTend + "]");
            
      /* 1 para add , 2 para sub , 3 para or, 0 default */
      switch(operacao){
         case 1:
            geracaoMemoria.linhasCF.add("add AX, BX ; add de AX e BX");
            break;
                  
         case 2:
            geracaoMemoria.linhasCF.add("sub AX, BX ; sub de AX e BX");
            break;
                  
         case 3:
            geracaoMemoria.linhasCF.add("or AX, BX ; or");
            break;             
      }
            
      procExpsend = geracaoMemoria.novoTemp();
      geracaoMemoria.linhasCF.add("mov DS:[" + procExpsend + "], AX ; ");;
            
   }
   
   void geracaoCodigo18(int operacao){
      geracaoMemoria.linhasCF.add("mov AX, DS:[" + procExpend + "]");
      geracaoMemoria.linhasCF.add("mov bx, DS:[" + procExpsend + "]"); // precisa ver se esse cara eh logico?
            
      geracaoMemoria.linhasCF.add("cmp AX, BX");
            
      String Nrotulo = rotuloPJ.novoRotulo();
            
      switch(operacao){ /* 1 > / 2 < / 3 >=  / 4 <= / 5 <> / 6 = */
         case 1:
            geracaoMemoria.linhasCF.add("jg " + Nrotulo);
               
            break;
         case 2:
            geracaoMemoria.linhasCF.add("jl " + Nrotulo);
               
            break;
         case 3:
            geracaoMemoria.linhasCF.add("jge " + Nrotulo);
               
            break;
         case 4:
            geracaoMemoria.linhasCF.add("jle " + Nrotulo);
               
            break;
         case 5:
            geracaoMemoria.linhasCF.add("jne " + Nrotulo);
               
            break;
         case 6:
            geracaoMemoria.linhasCF.add("je " + Nrotulo);
               
            break;
      }
            
      geracaoMemoria.linhasCF.add("mov AX, 0");
         
         
      String rotFalso = rotuloPJ.novoRotulo();
      geracaoMemoria.linhasCF.add("jmp " + rotFalso);
         
         
      geracaoMemoria.linhasCF.add(Nrotulo + ":");
         
      geracaoMemoria.linhasCF.add("mov AX, 1");
         
      geracaoMemoria.linhasCF.add(rotFalso + ":");
      procExpend = geracaoMemoria.novoTemp();
            
      geracaoMemoria.linhasCF.add("mov DS:[" + procExpend + "], AX");
            
   }
   
   void geracaoCodigo7(Simbolo id){
      procFend = geracaoMemoria.novoTemp();
      geracaoMemoria.linhasCF.add("mov AX, DS:["+id.getEndereco()+"];   Endereco inicial do vetor"); // Endereco inicial do vetor
      geracaoMemoria.linhasCF.add("mov BX, DS:["+procExpend+"];   Endereco da expressao"); // Endereco da expressao
      if(id.getTipo().equals("tipo_inteiro")){
         geracaoMemoria.linhasCF.add("add BX,BX;   Inteiros ocupam 2 bytes"); // Inteiros ocupam 2 bytes
         geracaoMemoria.linhasCF.add("add AX, BX;  Posicao inicial do vetor + posicao desejada"); // Posicao inicial do vetor + posicao desejada
         geracaoMemoria.linhasCF.add("mov DS:["+procFend+"], AX;  FINAL");
      }
   }
   
   void geracaoCodigo7_1(Simbolo id, Simbolo exp){
      procFend = geracaoMemoria.novoTemp();
      geracaoMemoria.linhasCF.add("mov AX, DS:["+id.getEndereco()+"];   Endereco inicial do vetor"); // Endereco inicial do vetor
      geracaoMemoria.linhasCF.add("mov BX, DS:["+exp.getEndereco()+"];   Endereco da expressao"); // Endereco da expressao
      if(id.getTipo().equals("tipo_inteiro")){
         geracaoMemoria.linhasCF.add("add BX,BX;   Inteiros ocupam 2 bytes"); // Inteiros ocupam 2 bytes
         geracaoMemoria.linhasCF.add("add AX, BX;  Posicao inicial do vetor + posicao desejada"); // Posicao inicial do vetor + posicao desejada
         geracaoMemoria.linhasCF.add("mov DS:["+procFend+"], AX;  FINAL");
      }
   }
   
   void geracaoCodigo20(Simbolo simboloEwr,int tempString){
   
      if(simboloEwr.getTipo().equals("tipo_string")){
         geracaoMemoria.linhasCF.add("mov dx, "+simboloEwr.getEndereco()+";");
         geracaoMemoria.linhasCF.add("mov ah, 09h;�");
         geracaoMemoria.linhasCF.add("int 21h;");
      }else {
         geracaoMemoria.linhasCF.add("mov ax, DS:[" + procExpend + "]");
         geracaoMemoria.linhasCF.add("mov di, " + tempString + " ;end. string temp.");
         geracaoMemoria.linhasCF.add("mov cx, 0 ;contador");
         geracaoMemoria.linhasCF.add("cmp ax,0 ;verifica sinal");
            
         String rotuloPos = rotuloPJ.novoRotulo();
         geracaoMemoria.linhasCF.add("jge " + rotuloPos + " ;salta se numero positivo");
         geracaoMemoria.linhasCF.add("mov bl, 2Dh ;senao, escreve sinal ");
         geracaoMemoria.linhasCF.add("mov ds:[di], bl");
         geracaoMemoria.linhasCF.add("add di, 1 ;incrementa indice");
         geracaoMemoria.linhasCF.add("neg ax ;toma modulo do numero");
         geracaoMemoria.linhasCF.add(rotuloPos + ":");
         geracaoMemoria.linhasCF.add("mov bx, 10 ;divisor");
            
         String rotuloPos1 = rotuloPJ.novoRotulo();
         geracaoMemoria.linhasCF.add(rotuloPos1 + ":");
         geracaoMemoria.linhasCF.add("add cx, 1 ;incrementa contador");
         geracaoMemoria.linhasCF.add("mov dx, 0 ;estende 32bits p/ div.");
         geracaoMemoria.linhasCF.add("idiv bx ;divide DXAX por BX");
         geracaoMemoria.linhasCF.add("push dx ;empilha valor do resto");
         geracaoMemoria.linhasCF.add("cmp ax, 0 ;verifica se quoc.  0");
         geracaoMemoria.linhasCF.add("jne " + rotuloPos1 + " ;se nao  0, continua");
            				
         String rotuloPos2 = rotuloPJ.novoRotulo();
         geracaoMemoria.linhasCF.add(rotuloPos2 + ":");
         geracaoMemoria.linhasCF.add("pop dx ;desempilha valor");
         geracaoMemoria.linhasCF.add("add dx, 30h ;transforma em caractere");
         geracaoMemoria.linhasCF.add("mov ds:[di],dl ;escreve caractere");
         geracaoMemoria.linhasCF.add("add di, 1 ;incrementa base");
         geracaoMemoria.linhasCF.add("add cx, -1 ;decrementa contador");
         geracaoMemoria.linhasCF.add("cmp cx, 0 ;verifica pilha vazia");
         geracaoMemoria.linhasCF.add("jne " + rotuloPos2 + " ;se nao pilha vazia, loop");
         geracaoMemoria.linhasCF.add("mov dl, 024h ;fim de string");
         geracaoMemoria.linhasCF.add("mov ds:[di], dl ;grava '$'");
         geracaoMemoria.linhasCF.add("mov dx, " + tempString);
         geracaoMemoria.linhasCF.add("mov ah, 09h");
         geracaoMemoria.linhasCF.add("int 21h");
      }
   }
   
   void geracaoCodigo21(){ // WRITELN
      geracaoMemoria.linhasCF.add("mov ah, 02h");
      geracaoMemoria.linhasCF.add("mov dl, 0Dh");
      geracaoMemoria.linhasCF.add("int 21h");
      geracaoMemoria.linhasCF.add("mov DL, 0Ah");
      geracaoMemoria.linhasCF.add("int 21h");
   }
   
   void geracaoCodigo22(){ // WRITE
      geracaoMemoria.linhasCF.add("mov ah, 09h");
      geracaoMemoria.linhasCF.add("int 21h");
   }
   
   void geracaoCodigo23(Simbolo exp, String rotuloFalse){
      geracaoMemoria.linhasCF.add("mov BX, DS:[" + exp.getEndereco() + "];");
      geracaoMemoria.linhasCF.add("cmp BX, 0; Compara expressao do if");
      geracaoMemoria.linhasCF.add("je " + rotuloFalse);
   }

   void geracaoCodigo25(String rotulo){
      geracaoMemoria.linhasCF.add(rotulo + ":");
   };

   void geracaoCodigo27(String rotulo) {
      geracaoMemoria.linhasCF.add("jmp " + rotulo + "");
   }

   void geracaoCodigo29(Simbolo id, Simbolo E) {
      geracaoMemoria.linhasCF.add("mov AX, DS:["+E.getEndereco()+"]; Atribuicao de valor para o FOR");
      geracaoMemoria.linhasCF.add("mov DS:["+id.getEndereco()+"], AX; Atribuicao de valor para o FOR");
   }

   void geracaoCodigo30(Simbolo id, Simbolo E, String rotuloFim){
      geracaoMemoria.linhasCF.add("mov AX, DS:["+id.getEndereco()+"]; Atribuicao para comparacao do FOR");
      geracaoMemoria.linhasCF.add("mov BX, "+E.getLexema()+"; Atribuição para comparcao do FOR");
      geracaoMemoria.linhasCF.add("cmp AX,BX");
      geracaoMemoria.linhasCF.add("jg " + rotuloFim);
   }

   void geracaoCodigo31(Simbolo contador) {
      geracaoMemoria.linhasCF.add("mov AX, DS:["+contador.getEndereco()+"]; Contador ++ do for");
      geracaoMemoria.linhasCF.add("add AX, 1; Contador ++ do for");
      geracaoMemoria.linhasCF.add("mov DS:["+contador.getEndereco()+"], AX; Contador ++ do for");
   }

}