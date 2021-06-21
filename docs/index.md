# A programról
A program egy TS alapú rendszer amely a rebol programnyelvet támogatja, a RED VSCode extension forkja.

# Módosítások
A program filenak módosítása.

 A program 4 fő részből áll:
 - Szintakszis /syntaxes/Rebol.tmLanguage.json
 Itt található a programnak Regexekkel felismert részei, pl az objektumok, funkciók stb...
 -  Snipettek: Az automatizált blokkok létrehozása
 pl. foreach, while, either stb
 A File a ./snippets/rebol.json fileban található.
 -  Intellisense: Ez a modul ./src/extension.ts fileban található.
 Úgy adhatsz hozzá új függvényt, hogy megkeresed, hogy melyik részre szeretnéd hozzáadni, pl. ha globálishoz, akkor provider1 konstanshoz.
 Ha a : után folytatódokhozz akkor doubledot konstanshoz,
 Ha a / után akkor a slash konstanshoz.
-   Beállítások: Ezek a beállítások is szintén több fajták lehetnek.
pl ha szeretnétek változtatni a kódoláson akkor package.json fileban lehet ezeket változatni.

## Intellisense hozzáadása
Az intellisense-t először is definiálni kell az alábbi módon
```
load_pdf_fontCompletion.insertText=new vscode.SnippetString('load_pdf_font "${1|T1,TB,A1,AB,AIB|}"')
```

Ammenyiben több változós az eset akkor a számozást folytatod

```
const taCompletion = new vscode.CompletionItem("ta");
      taCompletion.insertText = new vscode.SnippetString('ta ${1|left,center,right|} () ${2|(as-pair 25 y1)|}');
      taCompletion.kind = vscode.CompletionItemKind.Function;
      
```