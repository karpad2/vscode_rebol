"use strict";
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from "vscode";
import { rebolConfiguration } from "./RebolConfiguration";
import {
  rebolRunInConsole,
  rebolRunInGuiConsole,
  rebolCompileInConsole,
  setCommandMenu,
} from "./commandsProvider";

import * as vscodelc from "vscode-languageclient";

import { completions } from "./RebolSuggestion";
import * as path from "path";

let reboldClient: vscodelc.LanguageClient;
let rebolConfigs = rebolConfiguration.getInstance();

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {
  completions(context);

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
  console.log("rebol Language Server is now active!");
  //reboldClient.start();

  //context.subscriptions.push(reboldClient);
  const documentSelector: vscode.DocumentSelector = {
    language: "rebol",
  };

  const provider1 = vscode.languages.registerCompletionItemProvider("rebol", {
    provideCompletionItems(
      document: vscode.TextDocument,
      position: vscode.Position,
      token: vscode.CancellationToken,
      context: vscode.CompletionContext
    ) {
      // a simple completion item which inserts `Hello World!`
      /*const simpleCompletion = new vscode.CompletionItem('Hello World!');

			// a completion item that inserts its text as snippet,
			// the `insertText`-property is a `SnippetString` which will be
			// honored by the editor.
			const snippetCompletion = new vscode.CompletionItem('Good part of the day');
			snippetCompletion.insertText = new vscode.SnippetString('Good ${1|morning,afternoon,evening|}. It is ${1}, right?');
			snippetCompletion.documentation = new vscode.MarkdownString("Inserts a snippet that lets you select the _appropriate_ part of the day for your greeting.");

			// a completion item that can be accepted by a commit character,
			// the `commitCharacters`-property is set which means that the completion will
			// be inserted and then the character will be typed.
			const commitCharacterCompletion = new vscode.CompletionItem('console');
			commitCharacterCompletion.commitCharacters = ['.'];
			commitCharacterCompletion.documentation = new vscode.MarkdownString('Press `.` to get `console.`');

			// a completion item that retriggers IntelliSense when being accepted,
			// the `command`-property is set which the editor will execute after 
			// completion has been inserted. Also, the `insertText` is set so that 
			// a space is inserted after `new`
			const commandCompletion = new vscode.CompletionItem('new');
			commandCompletion.kind = vscode.CompletionItemKind.Keyword;
			commandCompletion.insertText = 'new ';
			commandCompletion.command = { command: 'editor.action.triggerSuggest', title: 'Re-trigger completions...' };*/

      const centerCompletion = new vscode.CompletionItem("center ");
      centerCompletion.kind = vscode.CompletionItemKind.Keyword;

      const middleCompletion = new vscode.CompletionItem("middle ");
      middleCompletion.kind = vscode.CompletionItemKind.Keyword;

      

      const leftCompletion = new vscode.CompletionItem("left ");
      leftCompletion.kind = vscode.CompletionItemKind.Keyword;

      const rightCompletion = new vscode.CompletionItem("right ");
      rightCompletion.kind = vscode.CompletionItemKind.Keyword;

      const andCompletion = new vscode.CompletionItem("and ");
      andCompletion.kind = vscode.CompletionItemKind.Operator;

      const orCompletion = new vscode.CompletionItem("or ");
      orCompletion.kind = vscode.CompletionItemKind.Operator;

      const ifCompletion = new vscode.CompletionItem("if ");
      ifCompletion.kind = vscode.CompletionItemKind.Function;

      const eitherCompletion = new vscode.CompletionItem("either ");
      eitherCompletion.kind = vscode.CompletionItemKind.Function;

      const objectCompletion = new vscode.CompletionItem("object ");
      objectCompletion.kind = vscode.CompletionItemKind.Class;

      const object1Completion = new vscode.CompletionItem("object! ");
      object1Completion.kind = vscode.CompletionItemKind.Class;
     

      const modeCompletion = new vscode.CompletionItem("mode ");
      modeCompletion.kind = vscode.CompletionItemKind.Variable;

      const atCompletion = new vscode.CompletionItem("at ");
      atCompletion.kind = vscode.CompletionItemKind.Function;

      const textCompletion = new vscode.CompletionItem("text ");
      textCompletion.kind = vscode.CompletionItemKind.Function;

      const edgeCompletion = new vscode.CompletionItem("edge: ");
      edgeCompletion.kind = vscode.CompletionItemKind.Keyword;

	    const notCompletion = new vscode.CompletionItem("not ");
      notCompletion.kind = vscode.CompletionItemKind.Operator;

      const rejoinCompletion = new vscode.CompletionItem("rejoin");
      rejoinCompletion.kind = vscode.CompletionItemKind.Function;
      rejoinCompletion.insertText = "rejoin  []";

      const dbCompletion = new vscode.CompletionItem("db ");
      dbCompletion.kind = vscode.CompletionItemKind.Class;

      const exitCompletion = new vscode.CompletionItem("exit ");
      exitCompletion.kind = vscode.CompletionItemKind.Function;

      const noneCompletion = new vscode.CompletionItem("none ");
      noneCompletion.kind = vscode.CompletionItemKind.Constant;

      const trueCompletion = new vscode.CompletionItem("true ");
      trueCompletion.kind = vscode.CompletionItemKind.Constant;

	  const block1Completion = new vscode.CompletionItem("block! ");
      block1Completion.kind = vscode.CompletionItemKind.Class;

      const falseCompletion = new vscode.CompletionItem("false ");
      falseCompletion.kind = vscode.CompletionItemKind.Constant;

      const findCompletion = new vscode.CompletionItem("find ");
      findCompletion.kind = vscode.CompletionItemKind.Function;

      const parseCompletion = new vscode.CompletionItem("parse ");
      parseCompletion.kind = vscode.CompletionItemKind.Function;

      const appendCompletion = new vscode.CompletionItem("append");
      appendCompletion.kind = vscode.CompletionItemKind.Function;

      const emptyCompletion = new vscode.CompletionItem("empty? ");
      emptyCompletion.kind = vscode.CompletionItemKind.Function;

      const lengthCompletion = new vscode.CompletionItem("length? ");
      lengthCompletion.kind = vscode.CompletionItemKind.Function;

      const length2Completion = new vscode.CompletionItem("length ");
      length2Completion.kind = vscode.CompletionItemKind.Function;

      const sizeCompletion = new vscode.CompletionItem("size? ");
      sizeCompletion.kind = vscode.CompletionItemKind.Function;

      const tailqCompletion = new vscode.CompletionItem("tail? ");
      tailqCompletion.kind = vscode.CompletionItemKind.Function;

      const tailCompletion = new vscode.CompletionItem("tail ");
      tailCompletion.kind = vscode.CompletionItemKind.Function;

      

      const size2Completion = new vscode.CompletionItem("size ");
      size2Completion.kind = vscode.CompletionItemKind.Function;

      const buttonCompletion = new vscode.CompletionItem("button ");
      buttonCompletion.kind = vscode.CompletionItemKind.Keyword;

      const composeCompletion = new vscode.CompletionItem("compose ");
      composeCompletion.kind = vscode.CompletionItemKind.Function;

      const makeCompletion = new vscode.CompletionItem("make ");
      makeCompletion.kind = vscode.CompletionItemKind.Function;

      const headCompletion = new vscode.CompletionItem("head ");
      headCompletion.kind = vscode.CompletionItemKind.Function;

      const copyCompletion = new vscode.CompletionItem("copy ");
      copyCompletion.kind = vscode.CompletionItemKind.Function;

      const firstCompletion = new vscode.CompletionItem("first ");
      firstCompletion.kind = vscode.CompletionItemKind.Keyword;

      const alignCompletion = new vscode.CompletionItem("align ");
      alignCompletion.kind = vscode.CompletionItemKind.Field;

      const mergeCompletion = new vscode.CompletionItem("merge ");
      mergeCompletion.kind = vscode.CompletionItemKind.Function;

      const tointegerCompletion = new vscode.CompletionItem("to-integer ");
      tointegerCompletion.kind = vscode.CompletionItemKind.Function;

      const endsheetCompletion = new vscode.CompletionItem("endsheet ");
      endsheetCompletion.kind = vscode.CompletionItemKind.Function;

      const lessbiggerCompletion = new vscode.CompletionItem("<> ");
      endsheetCompletion.kind = vscode.CompletionItemKind.Operator;

      const nextCompletion = new vscode.CompletionItem("next ");
      nextCompletion.kind = vscode.CompletionItemKind.Function;

      const withCompletion = new vscode.CompletionItem("with []");
      withCompletion.kind = vscode.CompletionItemKind.Function;

      const hide_popupCompletion = new vscode.CompletionItem("hide-popup ");
      hide_popupCompletion.kind = vscode.CompletionItemKind.Function;

      const write_xml_docCompletion = new vscode.CompletionItem(
        "write_xml_doc "
      );
      write_xml_docCompletion.kind = vscode.CompletionItemKind.Function;

      const aliasCompletion = new vscode.CompletionItem("alias ");
      aliasCompletion.kind = vscode.CompletionItemKind.Function;

      const cell1Completion = new vscode.CompletionItem('cell ("")');
      cell1Completion.insertText = new vscode.SnippetString('cell ("")');
      cell1Completion.kind = vscode.CompletionItemKind.Function;

      const cell2Completion = new vscode.CompletionItem("cell ()");
      cell2Completion.insertText = new vscode.SnippetString("cell ()");
      cell2Completion.kind = vscode.CompletionItemKind.Function;

      const uniqueCompletion = new vscode.CompletionItem("unique ");
      uniqueCompletion.kind = vscode.CompletionItemKind.Function;

      const asCompletion = new vscode.CompletionItem("as ");
      asCompletion.kind = vscode.CompletionItemKind.Function;

      const declareCompletion = new vscode.CompletionItem("declare ");
      declareCompletion.kind = vscode.CompletionItemKind.Function;

      const caseCompletion = new vscode.CompletionItem("case ");
      caseCompletion.kind = vscode.CompletionItemKind.Method;

      const continueCompletion = new vscode.CompletionItem("continue");
      continueCompletion.kind = vscode.CompletionItemKind.Function;

      const breakCompletion = new vscode.CompletionItem("break ");
      breakCompletion.kind = vscode.CompletionItemKind.Function;

      const nullCompletion = new vscode.CompletionItem("null ");
      nullCompletion.kind = vscode.CompletionItemKind.Constant;

      const string1Completion = new vscode.CompletionItem("string? ");
      string1Completion.kind = vscode.CompletionItemKind.Constant;

      const string2Completion = new vscode.CompletionItem("string! ");
      string2Completion.kind = vscode.CompletionItemKind.Constant;

      

      const assertCompletion = new vscode.CompletionItem("assert ");
      assertCompletion.kind = vscode.CompletionItemKind.Function;

      const selectCompletion = new vscode.CompletionItem("select ");
      selectCompletion.kind = vscode.CompletionItemKind.Function;

      const acrossCompletion = new vscode.CompletionItem("across ");
      acrossCompletion.kind = vscode.CompletionItemKind.Function;

      const close_btn_imgCompletion = new vscode.CompletionItem(
        "close_btn_img "
      );
      close_btn_imgCompletion.kind = vscode.CompletionItemKind.Unit;

      const polishedCompletion = new vscode.CompletionItem("polished ");
      polishedCompletion.kind = vscode.CompletionItemKind.Unit;

      const color_g2Completion = new vscode.CompletionItem("color_g2 ");
      color_g2Completion.kind = vscode.CompletionItemKind.Color;

      const color_m2Completion = new vscode.CompletionItem("color_m2 ");
      color_m2Completion.kind = vscode.CompletionItemKind.Color;

      const color_m4Completion = new vscode.CompletionItem("color_m4 ");
      color_m4Completion.kind = vscode.CompletionItemKind.Color;

      const color_l2Completion = new vscode.CompletionItem("color_l2 ");
      color_l2Completion.kind = vscode.CompletionItemKind.Color;

      const color_b2Completion = new vscode.CompletionItem("color_b2 ");
      color_b2Completion.kind = vscode.CompletionItemKind.Color;

      const color_t1Completion = new vscode.CompletionItem("color_t1 ");
      color_t1Completion.kind = vscode.CompletionItemKind.Color;

      const color_t2Completion = new vscode.CompletionItem("color_t2 ");
      color_t2Completion.kind = vscode.CompletionItemKind.Color;

      const color_h1Completion = new vscode.CompletionItem("color_h1 ");
      color_t1Completion.kind = vscode.CompletionItemKind.Color;

      const color_h2Completion = new vscode.CompletionItem("color_h2 ");
      color_t2Completion.kind = vscode.CompletionItemKind.Color;

	  const color_f4Completion = new vscode.CompletionItem("color_f4 ");
      color_f4Completion.kind = vscode.CompletionItemKind.Color;

	  const color_g4Completion = new vscode.CompletionItem("color_g4 ");
      color_g4Completion.kind = vscode.CompletionItemKind.Color;

	  

      const colorvCompletion = new vscode.CompletionItem("color: ");
      colorvCompletion.kind = vscode.CompletionItemKind.Variable;

      const sizevCompletion = new vscode.CompletionItem("size: ");
      sizevCompletion.kind = vscode.CompletionItemKind.Variable;

	  const fontvCompletion = new vscode.CompletionItem("font: ");
      fontvCompletion.kind = vscode.CompletionItemKind.Variable;
	
	  const paravCompletion = new vscode.CompletionItem("para: ");
      paravCompletion.kind = vscode.CompletionItemKind.Variable;

	  const spacevCompletion = new vscode.CompletionItem("space: ");
      spacevCompletion.kind = vscode.CompletionItemKind.Variable;

	  const originCompletion = new vscode.CompletionItem("origin: ");
      originCompletion.kind = vscode.CompletionItemKind.Variable;

      const dataCompletion = new vscode.CompletionItem("data: ");
      dataCompletion.kind = vscode.CompletionItemKind.Variable;

      

	  const boldCompletion = new vscode.CompletionItem("bold ");
      boldCompletion.kind = vscode.CompletionItemKind.Value;
	  
	  

      const sistimeCompletion = new vscode.CompletionItem("sistime");
      sistimeCompletion.kind = vscode.CompletionItemKind.Function;

      const mod_fakturaCompletion = new vscode.CompletionItem("mod_faktura");
      mod_fakturaCompletion.kind = vscode.CompletionItemKind.Function;

      const layoutoffsetCompletion = new vscode.CompletionItem("layout/offset");
      layoutoffsetCompletion.kind = vscode.CompletionItemKind.Function;

      const program_nazadCompletion = new vscode.CompletionItem(
        "program_nazad "
      );
      program_nazadCompletion.kind = vscode.CompletionItemKind.Function;

      const tabla_viewCompletion = new vscode.CompletionItem("tabla_view ");
      tabla_viewCompletion.kind = vscode.CompletionItemKind.Function;

      const fieldCompletion = new vscode.CompletionItem("field ");
      fieldCompletion.kind = vscode.CompletionItemKind.Function;

      const insertCompletion = new vscode.CompletionItem("insert ");
      insertCompletion.kind = vscode.CompletionItemKind.Function;

      const reduceCompletion = new vscode.CompletionItem("reduce ");
      reduceCompletion.kind = vscode.CompletionItemKind.Function;

      const checkCompletion = new vscode.CompletionItem("check ");
      checkCompletion.kind = vscode.CompletionItemKind.Function;

      const korisnikCompletion = new vscode.CompletionItem("korisnik ");
      korisnikCompletion.kind = vscode.CompletionItemKind.Value;

      const t_nevCompletion = new vscode.CompletionItem("t_nev ");
      t_nevCompletion.kind = vscode.CompletionItemKind.Value;

	  const t_dateCompletion = new vscode.CompletionItem("t_date ");
      t_dateCompletion.kind = vscode.CompletionItemKind.Value;

	  const t_iznosCompletion = new vscode.CompletionItem("t_iznos ");
      t_iznosCompletion.kind = vscode.CompletionItemKind.Value;

	  

	  const row1Completion = new vscode.CompletionItem("row1 ");
      row1Completion.kind = vscode.CompletionItemKind.Value;

      const apjelCompletion = new vscode.CompletionItem("apjel ");
      apjelCompletion.kind = vscode.CompletionItemKind.Value;

	  

      const blkCompletion = new vscode.CompletionItem("blk ");
      blkCompletion.kind = vscode.CompletionItemKind.Value;

      const styleCompletion = new vscode.CompletionItem("style ");
      styleCompletion.kind = vscode.CompletionItemKind.Function;

      const arrowCompletion = new vscode.CompletionItem("arrow ");
      arrowCompletion.kind = vscode.CompletionItemKind.Function;

      const replaceCompletion = new vscode.CompletionItem("replace ");
      replaceCompletion.kind = vscode.CompletionItemKind.Function;

      

      const focusCompletion = new vscode.CompletionItem("focus ");
      focusCompletion.kind = vscode.CompletionItemKind.Function;

      const get_rekordCompletion = new vscode.CompletionItem("get_rekord ");
      get_rekordCompletion.kind = vscode.CompletionItemKind.Function;

      const backtileCompletion = new vscode.CompletionItem("backtile ");
      backtileCompletion.kind = vscode.CompletionItemKind.Function;

      const removeCompletion = new vscode.CompletionItem("remove ");
      removeCompletion.kind = vscode.CompletionItemKind.Function;

      const alertCompletion = new vscode.CompletionItem('alert "" ');
      alertCompletion.kind = vscode.CompletionItemKind.Function;

      const alert1Completion = new vscode.CompletionItem("alert ");
      alert1Completion.kind = vscode.CompletionItemKind.Function;

      const showCompletion = new vscode.CompletionItem("show ");
      showCompletion.kind = vscode.CompletionItemKind.Function;

      const parseallCompletion = new vscode.CompletionItem("parse/all ");
      parseallCompletion.kind = vscode.CompletionItemKind.Function;

      const setCompletion = new vscode.CompletionItem("set ");
      setCompletion.kind = vscode.CompletionItemKind.Function;

      const ablak_brajzCompletion = new vscode.CompletionItem("ablak_brajz ");
      ablak_brajzCompletion.kind = vscode.CompletionItemKind.Function;

      const main_windowCompletion = new vscode.CompletionItem(
        "main_window/user-data "
      );
      main_windowCompletion.kind = vscode.CompletionItemKind.Function;

      const faktura_kiirasCompletion = new vscode.CompletionItem(
        "faktura_kiiras "
      );
	  
      faktura_kiirasCompletion.kind = vscode.CompletionItemKind.Function;

      const ablak_cezarCompletion = new vscode.CompletionItem("ablak_cezar ");
      ablak_cezarCompletion.kind = vscode.CompletionItemKind.Function;

      const sortCompletion = new vscode.CompletionItem("sort ");
      sortCompletion.kind = vscode.CompletionItemKind.Function;

      const retvalCompletion = new vscode.CompletionItem("retval ");
      retvalCompletion.kind = vscode.CompletionItemKind.Variable;

      const panelCompletion = new vscode.CompletionItem("panel ");
      panelCompletion.kind = vscode.CompletionItemKind.Variable;

      const faceCompletion = new vscode.CompletionItem("face ");
      faceCompletion.kind = vscode.CompletionItemKind.Variable;

      const infoCompletion = new vscode.CompletionItem("info ");
      infoCompletion.kind = vscode.CompletionItemKind.Variable;

	  const rekCompletion = new vscode.CompletionItem("rek ");
      rekCompletion.kind = vscode.CompletionItemKind.Variable;

	  const tlabCompletion = new vscode.CompletionItem("tlab ");
      tlabCompletion.kind = vscode.CompletionItemKind.Variable;
      const keycodeCompletion = new vscode.CompletionItem("keycode ");
      keycodeCompletion.kind = vscode.CompletionItemKind.Function;

      

      const write_modeCompletion = new vscode.CompletionItem("write_mode ");
      write_modeCompletion.kind = vscode.CompletionItemKind.Variable;

      const user_dataCompletion = new vscode.CompletionItem("user-data ");
      user_dataCompletion.kind = vscode.CompletionItemKind.Variable;
	  const w_dateCompletion = new vscode.CompletionItem("w_date ");
      w_dateCompletion.kind = vscode.CompletionItemKind.Variable;

	  const btnCompletion = new vscode.CompletionItem("btn ");
      btnCompletion.kind = vscode.CompletionItemKind.Operator;

	  const money_formCompletion = new vscode.CompletionItem("money_form ");
      money_formCompletion.kind = vscode.CompletionItemKind.Function;
      const clearCompletion = new vscode.CompletionItem("clear ");
      clearCompletion.kind = vscode.CompletionItemKind.Function;
      const sliderCompletion = new vscode.CompletionItem("slider ");
      sliderCompletion.kind = vscode.CompletionItemKind.Function;

      const pairCompletion = new vscode.CompletionItem("pair! ");
      pairCompletion.kind = vscode.CompletionItemKind.Interface;
      

      const maxCompletion = new vscode.CompletionItem("max ");
      maxCompletion.kind = vscode.CompletionItemKind.Function;

      const minCompletion = new vscode.CompletionItem("min ");
      minCompletion.kind = vscode.CompletionItemKind.Function;

      const uppercaseCompletion = new vscode.CompletionItem("uppercase ");
      uppercaseCompletion.kind = vscode.CompletionItemKind.Function;

      const lowercaseCompletion = new vscode.CompletionItem("lowercase ");
      lowercaseCompletion.kind = vscode.CompletionItemKind.Function;
      
      const dok_pathCompletion = new vscode.CompletionItem("dok_path ");
      dok_pathCompletion.kind = vscode.CompletionItemKind.Folder;

      const dir_specialCompletion = new vscode.CompletionItem("dir_special ");
      dir_specialCompletion.kind = vscode.CompletionItemKind.Folder;
      
      const dokument_adatok_feltoltCompletion = new vscode.CompletionItem("dokument_adatok_feltolt ");
      dokument_adatok_feltoltCompletion.kind = vscode.CompletionItemKind.Function;

      const ftp_cr_arCompletion = new vscode.CompletionItem("ftp_create_arhivpath ");
      ftp_cr_arCompletion.kind = vscode.CompletionItemKind.Function;

      const systemCompletion = new vscode.CompletionItem("system ");
      systemCompletion.kind = vscode.CompletionItemKind.Class;

      const as_pairCompletion = new vscode.CompletionItem("as-pair ");
      as_pairCompletion.kind = vscode.CompletionItemKind.Function;

      const evid_purCompletion = new vscode.CompletionItem("evid_pur ");
      evid_purCompletion.kind = vscode.CompletionItemKind.Function;

      const mysql_bulkCompletion = new vscode.CompletionItem("mysql_bulk ");
      mysql_bulkCompletion.kind = vscode.CompletionItemKind.Function;

      const returnCompletion = new vscode.CompletionItem("return ");
      returnCompletion.kind = vscode.CompletionItemKind.Function;
      
      const row0Completion = new vscode.CompletionItem("row0 ");
      row0Completion.kind = vscode.CompletionItemKind.Variable;

      const tar0Completion = new vscode.CompletionItem("tar0 ");
      tar0Completion.kind = vscode.CompletionItemKind.Variable;

      

      const tar1Completion = new vscode.CompletionItem("tar1 ");
      tar1Completion.kind = vscode.CompletionItemKind.Variable;
      
      const escapeCompletion = new vscode.CompletionItem("escape ");
      escapeCompletion.kind = vscode.CompletionItemKind.Value; 

      const coalCompletion = new vscode.CompletionItem("coal ");
      coalCompletion.kind = vscode.CompletionItemKind.Color; 
      
      const funcCompletion = new vscode.CompletionItem("func");
      funcCompletion.kind = vscode.CompletionItemKind.Function;
      funcCompletion.insertText = " func \n\r [\n \t\n ]";

      const formatCompletion = new vscode.CompletionItem("format");
      formatCompletion.insertText = new vscode.SnippetString(
        'format "f:###,##0.00" align "h:${1|Left,Center,Right|}"'
      );
      formatCompletion.kind = vscode.CompletionItemKind.Function;
      
      const load_pdf_fontCompletion = new vscode.CompletionItem("load_pdf_font");
      load_pdf_fontCompletion.insertText= new vscode.SnippetString(
        'load_pdf_font "${1|T1,TB,A1,AB,AIB|}"'
      )
      load_pdf_fontCompletion.kind= vscode.CompletionItemKind.Function;

      const new_pdf_pageCompletion = new vscode.CompletionItem("new_pdf_page ");
      new_pdf_pageCompletion.kind = vscode.CompletionItemKind.Function;

      const to_reg_dateCompletion = new vscode.CompletionItem("to-reg-date ");
      to_reg_dateCompletion.kind = vscode.CompletionItemKind.Function;

      const txt_szoveg_tordelCompletion = new vscode.CompletionItem("txt_szoveg_tordel ");
      txt_szoveg_tordelCompletion.kind = vscode.CompletionItemKind.Function;

      const autosizeCompletion = new vscode.CompletionItem("autosize ");
      autosizeCompletion.kind = vscode.CompletionItemKind.Function;

      const unviewCompletion = new vscode.CompletionItem("unview ");
      unviewCompletion.kind = vscode.CompletionItemKind.Function;

      
    

      
      const rowCompletion = new vscode.CompletionItem("row");
      rowCompletion.insertText = new vscode.SnippetString(
        'row interior 0 align "h:${1|Left,Center,Right|}" font "b:${2|0,1,2|}" '
      );
      rowCompletion.kind = vscode.CompletionItemKind.Function;

      const taCompletion = new vscode.CompletionItem("ta");
      taCompletion.insertText = new vscode.SnippetString(
        'ta ${1|left,center,right|} () ${2|(as-pair 25 y1)|}'
      );
      taCompletion.kind = vscode.CompletionItemKind.Function;
      
      

      const  fnCompletion = new vscode.CompletionItem("fn");
      fnCompletion.insertText = new vscode.SnippetString(
        'fn "${1|A1,A2,AB|}"'
      );
      fnCompletion.kind = vscode.CompletionItemKind.Function;

      const  nlCompletion = new vscode.CompletionItem("nl");
      nlCompletion.insertText = new vscode.SnippetString(
        'nl ${1|(as-pair 0 y1),0x10|} ${2|fn|} ${3|cb 1.0|} ${4|cg 1.0|} tv middle'
      );
      nlCompletion.kind = vscode.CompletionItemKind.Function;
      
      const  tvCompletion = new vscode.CompletionItem("tv");
      tvCompletion.insertText = new vscode.SnippetString(
        'tv ${1|middle,bottom|} '
      );
      tvCompletion.kind = vscode.CompletionItemKind.Function;
      


      // return all completion items as array
      return [
        /*simpleCompletion,
				snippetCompletion,
				commitCharacterCompletion,
				commandCompletion,*/
        centerCompletion,
        andCompletion,
        orCompletion,
        atCompletion,
        blkCompletion,
        leftCompletion,
        textCompletion,
        rightCompletion,
        notCompletion,
        rejoinCompletion,
        exitCompletion,
        noneCompletion,
        findCompletion,
        edgeCompletion,
        trueCompletion,
        falseCompletion,
        parseCompletion,
        appendCompletion,
        emptyCompletion,
        sizeCompletion,
        size2Completion,
        buttonCompletion,
        composeCompletion,
        makeCompletion,
        headCompletion,
        copyCompletion,
        fieldCompletion,
        cell1Completion,
        cell2Completion,
        firstCompletion,
        insertCompletion,
        alertCompletion,
        alert1Completion,
        parseallCompletion,
        focusCompletion,
        reduceCompletion,
        checkCompletion,
        lengthCompletion,
        panelCompletion,
        dbCompletion,
        layoutoffsetCompletion,
        tointegerCompletion,
        formatCompletion,
        alignCompletion,
        endsheetCompletion,
        write_xml_docCompletion,
        mergeCompletion,
        lessbiggerCompletion,
        nextCompletion,
        uniqueCompletion,
        length2Completion,
        asCompletion,
        assertCompletion,
        breakCompletion,
        declareCompletion,
        nullCompletion,
        caseCompletion,
        funcCompletion,
        aliasCompletion,
        selectCompletion,
        rowCompletion,
        sistimeCompletion,
        hide_popupCompletion,
        acrossCompletion,
        polishedCompletion,
        mod_fakturaCompletion,
        korisnikCompletion,
        withCompletion,
        close_btn_imgCompletion,
        styleCompletion,
        color_g2Completion,
        color_m4Completion,
        color_l2Completion,
        color_t2Completion,
        color_t1Completion,
        color_b2Completion,
        color_h1Completion,
        color_h2Completion,
		color_g4Completion,
        backtileCompletion,
        t_nevCompletion,
		t_dateCompletion,
		t_iznosCompletion,
        showCompletion,
        get_rekordCompletion,
        removeCompletion,
        ablak_brajzCompletion,
        retvalCompletion,
        setCompletion,
        faceCompletion,
        main_windowCompletion,
        faktura_kiirasCompletion,
        ablak_cezarCompletion,
        infoCompletion,
        write_modeCompletion,
        colorvCompletion,
        sizevCompletion,
        program_nazadCompletion,
        tabla_viewCompletion,
        user_dataCompletion,
        arrowCompletion,
		rekCompletion,
		block1Completion,
		fontvCompletion,
		paravCompletion,
		spacevCompletion,
		originCompletion,
		w_dateCompletion,
		row1Completion,
		tlabCompletion,
		boldCompletion,
		btnCompletion,
		money_formCompletion,
    clearCompletion,
    string1Completion,
    string2Completion,
    keycodeCompletion,
    sortCompletion,
    sliderCompletion,
    maxCompletion,
    replaceCompletion,
    uppercaseCompletion,
    lowercaseCompletion,
    dok_pathCompletion,
    dir_specialCompletion,
    ftp_cr_arCompletion,
    systemCompletion,
    pairCompletion,
    as_pairCompletion,
    dataCompletion,
    evid_purCompletion,
    mysql_bulkCompletion,
    returnCompletion,
    ifCompletion,
    modeCompletion,
    eitherCompletion,
    row0Completion,
    tar0Completion,
    row1Completion,
    tar1Completion,
    dokument_adatok_feltoltCompletion,
    tailqCompletion,
    escapeCompletion,
    coalCompletion,
    tailCompletion,
    objectCompletion,
    object1Completion,
    middleCompletion,
    load_pdf_fontCompletion,
    new_pdf_pageCompletion,
    to_reg_dateCompletion,
    taCompletion,
    taCompletion,
    fnCompletion,
    nlCompletion,
    txt_szoveg_tordelCompletion,
    tvCompletion,
    autosizeCompletion,
    unviewCompletion,
    apjelCompletion
      ];
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
        const linePrefix = document
          .lineAt(position)
          .text.substr(0, position.character);
        if (!linePrefix.endsWith("/")) {
          return undefined;
        }
        return [
          /*Funs */
          new vscode.CompletionItem("", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("skip ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("only ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("all ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("part ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("offset ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("filter ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("file ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("keep ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("local ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("first ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("last ", vscode.CompletionItemKind.Method),
          new vscode.CompletionItem("size ", vscode.CompletionItemKind.Method),
                 
                   
          /*Vars */
          new vscode.CompletionItem("text ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("data ", vscode.CompletionItemKind.Variable),
		      new vscode.CompletionItem("confirm ", vscode.CompletionItemKind.Variable),
		      new vscode.CompletionItem("faza ", vscode.CompletionItemKind.Variable),
		      new vscode.CompletionItem("tip_fak ", vscode.CompletionItemKind.Variable),
		      new vscode.CompletionItem("veza_dok ", vscode.CompletionItemKind.Variable),
		      new vscode.CompletionItem("datum ", vscode.CompletionItemKind.Variable),
		      new vscode.CompletionItem("rok ", vscode.CompletionItemKind.Variable),
		      new vscode.CompletionItem("value ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("filter ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("sif_part ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("sif_fak ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("faza ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("sif_mag ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("datum_dok ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("dat_val ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("namena ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("opis ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("dat_insert ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("kor_insert ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("dat_modif ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("kor_modif ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("uplata ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("user-data ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("title ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("default ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("fejlec ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("name ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("lista ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("nevek ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("oszlop ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("tipus ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("order ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("index ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("pane ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("arhiv_map ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("file_tips ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("network ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("host ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("br_ugov ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("serija ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("deep ", vscode.CompletionItemKind.Variable),
          new vscode.CompletionItem("cena ", vscode.CompletionItemKind.Variable),   
      
          new vscode.CompletionItem("host-address ", vscode.CompletionItemKind.Variable),          
          new vscode.CompletionItem("dok_path ", vscode.CompletionItemKind.File),
      
		  
          new vscode.CompletionItem(
            "br_dok",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "datum_dok",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "sif_mag",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "param",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "prevoz",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "iznos",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "ukupno",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "osn_por",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "troskovi",
            vscode.CompletionItemKind.Variable
          ),
          new vscode.CompletionItem(
            "rabat",
            vscode.CompletionItemKind.Variable
          ),
		  new vscode.CompletionItem(
            "size",
            vscode.CompletionItemKind.Variable
          ),];
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
        if (!linePrefix.endsWith(":")) {
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
