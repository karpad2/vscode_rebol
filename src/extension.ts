'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import {rebolConfiguration} from  './RebolConfiguration';
import {rebolRunInConsole, rebolRunInGuiConsole, rebolCompileInConsole, rebolCompileInGuiConsole, setCommandMenu, rebolCompileInRelease, rebolCompileClear, rebolCompileUpdate} from './commandsProvider';
import * as vscodelc from 'vscode-languageclient';
const a = require ('./bookmark');
import {completions} from './RebolSuggestion';
import * as path from 'path';

let reboldClient: vscodelc.LanguageClient;

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {
	a.activate();
	completions(context);
	/*let config = rebolConfiguration.getInstance();
	
	context.subscriptions.push(vscode.commands.registerCommand("rebol.interpret", () => rebolRunInConsole()));
	context.subscriptions.push(vscode.commands.registerCommand("rebol.interpretGUI", () => rebolRunInGuiConsole()));
	context.subscriptions.push(vscode.commands.registerCommand("rebol.commandMenu", setCommandMenu));
	console.log("isIntelligence", config.isIntelligence);
	if (!config.isIntelligence) {return;}

	console.log("rebol console path: ", config.rebolConsole);
	let serverModule = path.join(context.asAbsolutePath("."), "server", "server.rebol");
	let needlog = "debug-off";
	if (config.needRlsDebug) {
		needlog = "debug-on";
	}
	console.log(needlog);
	const serverOptions: vscodelc.ServerOptions = {
		run : { command: config.rebolConsole, args: [serverModule, needlog]},
		debug: { command: config.rebolConsole, args: [serverModule, "debug-on"] }
	};
	const clientOptions: vscodelc.LanguageClientOptions = {
		documentSelector: [
			{scheme: 'file', language: 'rebol'}
		],
		initializationOptions: config.allConfigs || {},
		synchronize: {
			configurationSection: ['rebol', 'rebols']
		}
	};
	reboldClient = new vscodelc.LanguageClient('rebol.server', 'rebol Language Server', serverOptions, clientOptions);
	console.log('rebol Language Server is now active!');
	context.subscriptions.push(reboldClient.start());*/
}
