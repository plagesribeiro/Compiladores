sseg SEGMENT STACK		; Início seg. pilha
	byte 4000h DUP(?)	; Dimensiona pilha
sseg ENDS			; Fim seg. pilha

dseg SEGMENT PUBLIC		; Início seg. dados
	byte 4000h DUP(?)	; Temporários
	byte ?			; Var. char em 16384
	byte 97			; Const. char em 16385
	sword 1			; Const. int em 16386
	sword 34			; Const. int em 16388
	byte 98			; Const. char em 16390
	sword ?			; Var. int em 16391
	sword ?			; Var. int em 16393
	sword 100 DUP(?)			; Var. vec. int em 16395
	sword 50			; Var. int em 16595
	sword 10			; Var. int em 16597
	sword 45 DUP(?)			; Var. vec. int em 16599
	sword 24 DUP(?)			; Var. vec. int em 16689
	sword 0			; Var. int em 16737
	sword ?			; Var. int em 16739
	sword 100 DUP(?)			; Var. vec. int em 16741
	byte 120			; Var. char em 16941
	byte ?			; Var. char em 16942
	byte ?			; Var. char em 16943
	byte ?			; Var. char em 16944
	byte 51			; Var. char em 16945
	byte ?			; Var. char em 16946
	byte 110			; Var. char em 16947
	byte ?			; Var. char em 16948
	sword 23			; Var. int em 16949
	sword ?			; Var. int em 16951
	sword 3			; Var. int em 16953
	sword ?			; Var. int em 16955
	sword 55			; Var. int em 16957
	sword ?			; Var. int em 16959
	byte 256 DUP(?)			; Var. vec. char em 16961
	sword 33 DUP(?)			; Var. vec. int em 17217
	sword ?			; Var. int em 17283
	byte ?			; Var. char em 17285
	byte 2047 DUP(?)			; Var. vec. char em 17286
	byte 110 DUP(?)			; Var. vec. char em 19333
	byte 74			; Var. char em 19443
	sword 2047 DUP(?)			; Var. vec. int em 19444
	sword 20 DUP(?)			; Var. vec. int em 23538
	byte 2047 DUP(?)			; Var. vec. char em 23578
	byte 20 DUP(?)			; Var. vec. char em 25625
	sword 31333			; Var. int em 25645
	byte 99			; Const. char em 25647
	byte 120			; Const. char em 25648
	byte 102			; Const. char em 25649
	sword 10			; Var. int em 25650
	sword 3			; Const. int em 25652
dseg ENDS			; Fim seg. dados

cseg SEGMENT PUBLIC		; Início seg. código
	ASSUME CS:cseg, DS:dseg

strt:				; Início do programa
	mov ah, 4Ch		; Finalização do programa
	int 21h			; Finalização do programa
cseg ENDS			; Fim seg. código
END strt			; Fim programa
