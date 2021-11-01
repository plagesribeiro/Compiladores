sseg SEGMENT STACK ;in�cio seg. pilha
  byte 4000h DUP(?) ;dimensiona pilha
sseg ENDS ;fim seg. pilha
dseg SEGMENT PUBLIC ;in�cio seg. dados
  byte 4000h DUP(?) ;tempor�rios
  sword 3       ;classe_constante inteiro n223 em 16384
  sword ?     ;classe_variavel inteiro eita em 16386
  sword 100 DUP(?)      ;classe_variavel vet inteiro n1 em 16388
  sword 50       ;classe_variavel inteiro n em 16588
  sword 10       ;classe_variavel inteiro n7 em 16590
  sword 45 DUP(?)      ;classe_variavel vet inteiro teste em 16592
  sword 24 DUP(?)      ;classe_variavel vet inteiro beicola em 16682
  sword 0       ;classe_variavel inteiro maxiter em 16730
  sword ?     ;classe_variavel inteiro n2 em 16732
  sword 100 DUP(?)      ;classe_variavel vet inteiro nome em 16734
  byte 'x'     ;classe_variavel char n4 em 16934
  byte 2047 DUP(?)       ;classe_variavel vet char nome2 em 16935
  byte 20 DUP(?)       ;classe_variavel vet char nome3 em 18982
mov AX, 32766 ; movi para AX um VALORCONST
mov DS:[0], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[0] ;
neg AX
mov DS:[0], AX
  sword 32766       ;classe_variavel inteiro fed em 19002
  sword 0       ;classe_constante inteiro n5 em 19004
  byte 17 DUP("federico o lindo$")     ;classe_constante string n8 em 19006
  byte 'f'     ;classe_constante char oi em 19025
mov AX, 9 ; movi para AX um VALORCONST
mov DS:[2], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[2] ;
neg AX
mov DS:[2], AX
mov AX, 3 ; movi para AX um VALORCONST
mov DS:[4], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[16588]
mov BX, DS:[4]
imul BX ; multiplicacao
mov DS:[6], ax
mov AX, DS:[2]
mov BX, DS:[6]
add AX, BX ; add de AX e BX
mov DS:[6], AX ; 
  sword 9       ;classe_variavel inteiro n9 em 19026
  byte 0     ;classe_variavel char n3 em 19028
dseg ENDS ;fim seg. dados
cseg SEGMENT PUBLIC ;in�cio seg. c�digo
  ASSUME CS:cseg, DS:dseg
strt:
  mov AX, dseg
  mov ds, AX
dseg SEGMENT PUBLIC
byte "b$"; constante string
dseg ENDS
mov AX, DS:[10] ; peguei o end do exp talvez6 << end do simboloA
mov DS:[16935], AX; salvando o valor no endereco correto
dseg SEGMENT PUBLIC
byte "teste$"; constante string
dseg ENDS
mov AX, DS:[18] ; peguei o end do exp talvez10 << end do simboloA
mov DS:[16935], AX; salvando o valor no endereco correto
mov AX, 9 ; movi para AX um VALORCONST
mov DS:[18], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[18] ;
neg AX
mov DS:[18], AX
mov AX, DS:[18] ; peguei o end do exp talvez18 << end do simboloA
mov DS:[16588], AX; salvando o valor no endereco correto
mov AX, 0 ; movi para AX um VALORCONST
mov DS:[20], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[18982];   Endereco inicial do vetor
mov BX, DS:[20];   Endereco da expressao
mov AX, DS:[22] ; peguei o end do exp talvez18982 << end do simboloA
mov DS:[19028], AX; salvando o valor no endereco correto
mov AX, 5 ; movi para AX um VALORCONST
mov DS:[22], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[18982];   Endereco inicial do vetor
mov BX, DS:[22];   Endereco da expressao
mov AX, DS:[24] ; peguei o end do exp talvez18982 << end do simboloA
mov DS:[19028], AX; salvando o valor no endereco correto
mov AX, 0 ; movi para AX um VALORCONST
mov DS:[24], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[16734];   Endereco inicial do vetor
mov BX, DS:[24];   Endereco da expressao
add BX,BX;   Inteiros ocupam 2 bytes
add AX, BX;  Posicao inicial do vetor + posicao desejada
mov DS:[26], AX;  FINAL
mov AX, DS:[26] ; peguei o end do exp talvez16734 << end do simboloA
mov DS:[16588], AX; salvando o valor no endereco correto
mov AX, 0 ; movi para AX um VALORCONST
mov DS:[26], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[26] ; peguei o end do exp talvez26 << end do simboloA
mov DS:[19028], AX; salvando o valor no endereco correto
mov AX, DS:[19025] ; peguei o end do exp talvez19025 << end do simboloA
mov DS:[19028], AX; salvando o valor no endereco correto
mov AX, 0 ; movi para AX um VALORCONST
mov DS:[27], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[18982];   Endereco inicial do vetor
mov BX, DS:[27];   Endereco da expressao
mov AX, DS:[29] ; peguei o end do exp talvez18982 << end do simboloA
mov DS:[19028], AX; salvando o valor no endereco correto
mov AX, DS:[19006] ; peguei o end do exp talvez19006 << end do simboloA
mov DS:[16935], AX; salvando o valor no endereco correto
mov AX, DS:[19006] ; peguei o end do exp talvez19006 << end do simboloA
mov DS:[16935], AX; salvando o valor no endereco correto
mov AX, 3 ; movi para AX um VALORCONST
mov DS:[29], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[16935];   Endereco inicial do vetor
mov BX, DS:[29];   Endereco da expressao
mov ax, DS:[31]
mov di, 29 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R0 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R0:
mov bx, 10 ;divisor
R1:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R1 ;se nao  0, continua
R2:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R2 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 29
mov ah, 09h
int 21h
mov ax, DS:[16935]
mov di, 31 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R3 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R3:
mov bx, 10 ;divisor
R4:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R4 ;se nao  0, continua
R5:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R5 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 31
mov ah, 09h
int 21h
mov ax, DS:[16588]
mov di, 31 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R6 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R6:
mov bx, 10 ;divisor
R7:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R7 ;se nao  0, continua
R8:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R8 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 31
mov ah, 09h
int 21h
mov ax, DS:[16590]
mov di, 31 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R9 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R9:
mov bx, 10 ;divisor
R10:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R10 ;se nao  0, continua
R11:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R11 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 31
mov ah, 09h
int 21h
mov AX, 4 ; movi para AX um VALORCONST
mov DS:[31], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[16935];   Endereco inicial do vetor
mov BX, DS:[31];   Endereco da expressao
mov ax, DS:[33]
mov di, 31 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R12 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R12:
mov bx, 10 ;divisor
R13:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R13 ;se nao  0, continua
R14:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R14 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 31
mov ah, 09h
int 21h
mov ax, DS:[19028]
mov di, 33 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R15 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R15:
mov bx, 10 ;divisor
R16:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R16 ;se nao  0, continua
R17:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R17 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 33
mov ah, 09h
int 21h
mov ax, DS:[19004]
mov di, 33 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R18 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R18:
mov bx, 10 ;divisor
R19:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R19 ;se nao  0, continua
R20:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R20 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 33
mov ah, 09h
int 21h
mov dx, 19006;
mov ah, 09h;�
int 21h;
mov AX, 2 ; movi para AX um VALORCONST
mov DS:[33], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, 3 ; movi para AX um VALORCONST
mov DS:[35], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, 0 ; movi para AX um VALORCONST
mov DS:[37], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[37]; Atribuicao de valor para o FOR
mov DS:[16588], AX; Atribuicao de valor para o FOR
R21:
mov AX, DS:[16588]; Atribuicao para comparacao do FOR
mov BX, maxiter; Atribuição para comparcao do FOR
cmp AX,BX
jg R22
dseg SEGMENT PUBLIC
byte "ola' $"; constante string
dseg ENDS
mov dx, 47;
mov ah, 09h;�
int 21h;
mov ax, DS:[16935]
mov di, 48 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R23 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R23:
mov bx, 10 ;divisor
R24:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R24 ;se nao  0, continua
R25:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R25 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 48
mov ah, 09h
int 21h
mov ah, 02h
mov dl, 0Dh
int 21h
mov DL, 0Ah
int 21h
dseg SEGMENT PUBLIC
byte "c$"; constante string
dseg ENDS
mov AX, DS:[52] ; peguei o end do exp talvez48 << end do simboloA
mov DS:[16935], AX; salvando o valor no endereco correto
mov AX, DS:[16588]; Contador ++ do for
add AX, 1; Contador ++ do for
mov DS:[16588], AX; Contador ++ do for
jmp R21
R22:
mov AX, 1 ; movi para AX um VALORCONST
mov DS:[52], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, 2 ; movi para AX um VALORCONST
mov DS:[54], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[52]
mov bx, DS:[54]
cmp AX, BX
je R26
mov AX, 0
jmp R27
R26:
mov AX, 1
R27:
mov DS:[56], AX
mov AX, 3 ; movi para AX um VALORCONST
mov DS:[56], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, 4 ; movi para AX um VALORCONST
mov DS:[58], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[56]
mov bx, DS:[58]
cmp AX, BX
je R28
mov AX, 0
jmp R29
R28:
mov AX, 1
R29:
mov DS:[60], AX
mov AX, DS:[58]
mov BX, DS:[56]
or AX, BX ; or
mov DS:[60], AX ; 
mov AX, 5 ; movi para AX um VALORCONST
mov DS:[60], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, 6 ; movi para AX um VALORCONST
mov DS:[62], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[60]
mov bx, DS:[62]
cmp AX, BX
je R30
mov AX, 0
jmp R31
R30:
mov AX, 1
R31:
mov DS:[64], AX
mov AX, DS:[62]
mov BX, DS:[60]
or AX, BX ; or
mov DS:[64], AX ; 
mov BX, DS:[52];
cmp BX, 0; Compara expressao do if
je R32
mov AX, 106 ; movi para AX um VALORCONST
mov DS:[64], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[64] ; peguei o end do exp talvez64 << end do simboloA
mov DS:[16934], AX; salvando o valor no endereco correto
dseg SEGMENT PUBLIC
byte "n$"; constante string
dseg ENDS
mov AX, DS:[69] ; peguei o end do exp talvez65 << end do simboloA
mov DS:[16935], AX; salvando o valor no endereco correto
jmp R33
R32:
mov AX, DS:[19025]
mov bx, DS:[19028]
cmp AX, BX
jg R34
mov AX, 0
jmp R35
R34:
mov AX, 1
R35:
mov DS:[69], AX
mov BX, DS:[19025];
cmp BX, 0; Compara expressao do if
je R36
dseg SEGMENT PUBLIC
byte "m$"; constante string
dseg ENDS
mov AX, DS:[73] ; peguei o end do exp talvez69 << end do simboloA
mov DS:[16935], AX; salvando o valor no endereco correto
dseg SEGMENT PUBLIC
byte "n$"; constante string
dseg ENDS
mov AX, DS:[77] ; peguei o end do exp talvez73 << end do simboloA
mov DS:[18982], AX; salvando o valor no endereco correto
jmp R37
R37:
R33:
mov ah, 4Ch
int 21h
cseg ENDS ;fim seg. c�digo
END strt ;fim programa
