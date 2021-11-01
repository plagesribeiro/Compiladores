sseg SEGMENT STACK ;in�cio seg. pilha
  byte 4000h DUP(?) ;dimensiona pilha
sseg ENDS ;fim seg. pilha
dseg SEGMENT PUBLIC ;in�cio seg. dados
  byte 4000h DUP(?) ;tempor�rios
  sword 23       ;classe_variavel inteiro mm3 em 16384
  sword ?     ;classe_variavel inteiro nn3 em 16386
  sword ?     ;classe_variavel inteiro avb3 em 16388
  byte 10 DUP(?)       ;classe_variavel vet char a2 em 16390
dseg ENDS ;fim seg. dados
cseg SEGMENT PUBLIC ;in�cio seg. c�digo
  ASSUME CS:cseg, DS:dseg
strt:
  mov AX, dseg
  mov ds, AX
mov AX, 3 ; movi para AX um VALORCONST
mov DS:[0], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, 2 ; movi para AX um VALORCONST
mov DS:[2], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[0]
mov BX, DS:[2]
add AX, BX ; add de AX e BX
mov DS:[4], AX ; 
mov AX, 5 ; movi para AX um VALORCONST
mov DS:[4], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, 2 ; movi para AX um VALORCONST
mov DS:[6], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[4]
mov BX, DS:[6]
imul BX ; multiplicacao
mov DS:[8], ax
mov AX, 1 ; movi para AX um VALORCONST
mov DS:[8], AX ;MOVI PARA END o CONTEUDO DE AX
mov AX, DS:[8]
mov BX, DS:[8]
cwd
mov cx, ax ; salvar o que tinha em al
mov ax, DS:[8] ; mover F1.end para al
cwd
mov bx, ax ; voltar F1.end para bx
mov ax, cx ;voltar valor anterior de ax
idiv BX ; divisao
sub AX, 256; divisao
mov DS:[10], ax
mov AX, DS:[4]
mov BX, DS:[10]
sub AX, BX ; sub de AX e BX
mov DS:[10], AX ; 
mov AX, DS:[10] ; peguei o end do exp talvez0 << end do simboloA
mov DS:[16384], AX; salvando o valor no endereco correto
mov AX, DS:[16384]
mov bx, DS:[16388]
cmp AX, BX
jl R0
mov AX, 0
jmp R1
R0:
mov AX, 1
R1:
mov DS:[10], AX
mov AX, DS:[16384] ;mm3 em 16384
neg AX
add AX,1
mov DS:[10], AX
mov AX, DS:[10] ; peguei o end do exp talvez16384 << end do simboloA
mov DS:[16386], AX; salvando o valor no endereco correto
mov ax, DS:[16384]
mov di, 10 ;end. string temp.
mov cx, 0 ;contador
cmp ax,0 ;verifica sinal
jge R2 ;salta se numero positivo
mov bl, 2Dh ;senao, escreve sinal 
mov ds:[di], bl
add di, 1 ;incrementa indice
neg ax ;toma modulo do numero
R2:
mov bx, 10 ;divisor
R3:
add cx, 1 ;incrementa contador
mov dx, 0 ;estende 32bits p/ div.
idiv bx ;divide DXAX por BX
push dx ;empilha valor do resto
cmp ax, 0 ;verifica se quoc.  0
jne R3 ;se nao  0, continua
R4:
pop dx ;desempilha valor
add dx, 30h ;transforma em caractere
mov ds:[di],dl ;escreve caractere
add di, 1 ;incrementa base
add cx, -1 ;decrementa contador
cmp cx, 0 ;verifica pilha vazia
jne R4 ;se nao pilha vazia, loop
mov dl, 024h ;fim de string
mov ds:[di], dl ;grava '$'
mov dx, 10
mov ah, 09h
int 21h
mov ah, 4Ch
int 21h
cseg ENDS ;fim seg. c�digo
END strt ;fim programa
