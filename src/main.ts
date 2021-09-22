"use strict";
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from "vscode";



import cfunction from "./completion/functions.json";
import cfolders from "./completion/folders.json";
import coperator  from "./completion/operators.json";
import cunits  from "./completion/units.json";
import cconstant  from "./completion/Constant.json";
import ckeyword  from "./completion/keyword.json";
import cvalue  from "./completion/value.json";
import cvariable  from "./completion/variable.json";
import ccolor  from "./completion/color.json";
import cclass  from "./completion/Class.json";

import efunction  from "./completion/endswithslash/functions.json";
import evariable  from "./completion/endswithslash/variable.json";


import * as vscodelc from "vscode-languageclient";


import * as path from "path";

let reboldClient: vscodelc.LanguageClient;


// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {
  //completions(context);

  /*const options = [
    {
      label: "Run Current Script",
      description: "",
      command: "rebol.interpret",
    },
    {
      label: "Run Current Script in GUI Console",
      description: "",
      command: "rebol.interpretGUI",
    },
    {
      label: "Compile Current Script",
      description: "",
      command: "rebol.compile",
    },
    {
      label: "Compile Current Script (GUI mode)",
      description: "",
      command: "rebol.compileGUI",
    },
    {
      label: "Compile Current Script (Release mode)",
      description: "",
      command: "rebol.compileRelease",
    },
    {
      label: "Delete all temporary files (librebolRT, etc)",
      description: "",
      command: "rebol.clear",
    },
    {
      label: "Update librebolRT and Compile Current script",
      description: "",
      command: "rebol.update",
    },
  ];
  /*vscode.window.showQuickPick(options).then((option) => {
    if (!option || !option.command || option.command.length === 0) {
      return;
    }
    vscode.commands.executeCommand(option.command);
  });*/

  /*let config = rebolConfiguration.getInstance();
  let rebolTool = rebolConfigs.rebolToolChain;
  //context.subscriptions.push(vscode.commands.registerCommand("rebol.interpret", () => rebolRunInConsole()));
  //context.subscriptions.push(vscode.commands.registerCommand("rebol.interpretGUI", () => rebolRunInGuiConsole()));
  console.log("isIntelligence", config.isIntelligence);
  if (!config.isIntelligence) {
    return;
  }

  console.log("rebol console path: ", config.rebolConsole);
  //let serverModule = path.join(context.asAbsolutePath("."), "server", "server.red");
  let needlog = "debug-off";
  if (config.needRlsDebug) {
    needlog = "debug-on";
  }
  console.log(needlog);

  /*const serverOptions: vscodelc.ServerOptions = {
		run : { command: config.rebolConsole, args: [serverModule, needlog]},
		debug: { command: config.rebolConsole, args: [serverModule, "debug-on"] }
	};
	const clientOptions: vscodelc.LanguageClientOptions = {
		documentSelector: [
			{scheme: 'file', language: 'sc'}
		],
		initializationOptions: config.allConfigs || {},
		synchronize: {
			configurationSection: ['sc','rebol']
		}
	};*/
  //reboldClient = new vscodelc.LanguageClient('rebol.server', 'rebol Language Server', serverOptions, clientOptions);
  
  //reboldClient.start();

  //context.subscriptions.push(reboldClient);
  const documentSelector: vscode.DocumentSelector = {
    language: "rebol",
  };

  const create_completion=(element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; })=>
  {
    let l=new vscode.CompletionItem(element.completion);
        l.insertText=element.insertText;
        let k=null;
        if(element.kind=="function")
        k=vscode.CompletionItemKind.Function;
        else if(element.kind=="constant")
        k=vscode.CompletionItemKind.Constant;
        else if(element.kind=="class")
        k=vscode.CompletionItemKind.Class;
        else if(element.kind=="color")
        k=vscode.CompletionItemKind.Color;
        else if(element.kind=="keyword")
        k=vscode.CompletionItemKind.Keyword;
        else if(element.kind=="value")
        k=vscode.CompletionItemKind.Value;
        else if(element.kind=="variable")
        k=vscode.CompletionItemKind.Variable;
        else if(element.kind=="unit")
        k=vscode.CompletionItemKind.Unit;
        else if(element.kind=="operator")
        k=vscode.CompletionItemKind.Operator;
        else if(element.kind=="method")
        k=vscode.CompletionItemKind.Method;
        else if(element.kind=="field")
        k=vscode.CompletionItemKind.Field;
        else if(element.kind=="folder")
        k=vscode.CompletionItemKind.Folder;
        else{
        k=vscode.CompletionItemKind.User;
          console.log("Missing completion item kind: "+element.kind);
      }

        l.kind=k;
        return l;
  };
  const provider1 = vscode.languages.registerCompletionItemProvider("rebol", {
    provideCompletionItems(
      document: vscode.TextDocument,
      position: vscode.Position,
      token: vscode.CancellationToken,
      context: vscode.CompletionContext
    ) {
      const linePrefix = document.lineAt(position).text.substr(0, position.character);
      if ((linePrefix.split('"').length-1)%2==1) 
       {
        return undefined;
      }
     let completions=Array();
      
      cfunction.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });

      cconstant.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
      cvalue.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
      cvariable.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
      cunits.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
      coperator.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
      ckeyword.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
      cfolders.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
      ccolor.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });

      cclass.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
        completions.push(create_completion(element))
      });
    return completions;
    },
  });
  const slash = vscode.languages.registerCompletionItemProvider(
    "rebol",
    {
      provideCompletionItems(
        document: vscode.TextDocument,
        position: vscode.Position
      ) {
        // get all text until the `position` and check if it reads `console.`
        // and if so then complete if `log`, `warn`, and `error`
        const linePrefix = document.lineAt(position).text.substr(0, position.character);
        if (!linePrefix.endsWith("/") || (linePrefix.split('"').length-1)%2==1) 
         {
          return undefined;
        }
        console.log(linePrefix.indexOf('"'));
        let diffusedarray=Array();
        let completions=Array();
        diffusedarray.push(efunction);
        diffusedarray.push(evariable);
        diffusedarray.push(cconstant);

        diffusedarray.forEach((element: { completion: string | vscode.CompletionItemLabel; insertText: string | vscode.SnippetString | undefined; kind: string; }) => {
        
          completions.push(create_completion(element))
        });


        return completions;
      },
    },
    "/" // triggered whenever a '/' is being typed
  );

  const sistime = vscode.languages.registerCompletionItemProvider(
    "rebol",
    {
      provideCompletionItems(
        document: vscode.TextDocument,
        position: vscode.Position
      ) {
        // get all text until the `position` and check if it reads `console.`
        // and if so then complete if `log`, `warn`, and `error`
        const linePrefix = document
          .lineAt(position)
          .text.substr(0, position.character);
        if (!linePrefix.endsWith("sistime/")) {
          return undefined;
        }
        return [
          new vscode.CompletionItem("", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem(
            "isodate",
            vscode.CompletionItemKind.Method
          ),
        ];
      },
    },
    "/" // triggered whenever a '/' is being typed
  );

  const doubledot = vscode.languages.registerCompletionItemProvider(
    "rebol",
    {
      provideCompletionItems(
        document: vscode.TextDocument,
        position: vscode.Position
      ) {
        // get all text until the `position` and check if it reads `console.`
        // and if so then complete if `log`, `warn`, and `error`
        const linePrefix = document
          .lineAt(position)
          .text.substr(0, position.character);
        if (!linePrefix.endsWith(":") || (linePrefix.split('"').length-1)%2==1) {
          return undefined;
        }

        const uniqueCompletion = new vscode.CompletionItem("unique ");
        uniqueCompletion.kind = vscode.CompletionItemKind.Function;

        const roundtoCompletion = new vscode.CompletionItem("round/to ");
        roundtoCompletion.kind = vscode.CompletionItemKind.Function;

        const noneCompletion = new vscode.CompletionItem("none ");
        noneCompletion.kind = vscode.CompletionItemKind.Constant;

        const makeCompletion = new vscode.CompletionItem("make ");
        makeCompletion.kind = vscode.CompletionItemKind.Function;

        const trueCompletion = new vscode.CompletionItem("true ");
        trueCompletion.kind = vscode.CompletionItemKind.Constant;

        const falseCompletion = new vscode.CompletionItem("false ");
        falseCompletion.kind = vscode.CompletionItemKind.Constant;

        const copyCompletion = new vscode.CompletionItem("copy ");
        copyCompletion.kind = vscode.CompletionItemKind.Function;

        const funcCompletion = new vscode.CompletionItem("func");
        funcCompletion.kind = vscode.CompletionItemKind.Function;
        funcCompletion.insertText = " func \n\r [\n \t\n ]";
        return [
          new vscode.CompletionItem("", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("skip", vscode.CompletionItemKind.Method),
          uniqueCompletion,
          noneCompletion,
          trueCompletion,
          falseCompletion,
          roundtoCompletion,
          funcCompletion,
          makeCompletion,
          copyCompletion
        ];
      },
    },
    ":" // triggered whenever a '/' is being typed
  );
  context.subscriptions.push(provider1, slash, doubledot, sistime);
}
