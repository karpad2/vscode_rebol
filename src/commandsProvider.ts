'use strict';

import * as vscode from 'vscode';
import {rebolConfiguration} from  './RebolConfiguration';
import * as path from 'path';

let terminal: vscode.Terminal;

function getTarget(): string {
	if (process.platform === 'win32') {
		return "Windows";
	} else if (process.platform === 'darwin') {
		return "macOS";
	} else {
		return '';
	}
}

function getBuildDir(filePath: string): string {
	let rebolConfigs = rebolConfiguration.getInstance();
	return rebolConfigs.rebolWorkSpace || vscode.workspace.rootPath || path.dirname(filePath);
}

function getOutFileName(buildDir: string, filePath: string): string {
	let outName = path.join(buildDir, path.parse(filePath).name);
	if (process.platform === 'win32') {
		outName = outName + ".exe";
	}
	return outName;
}

function normalFile(value: string): string {
	return value.replace(/\\/g, '/');
}

function execCommand(command: string, args: string) {
	let text: string = "";

	terminal = terminal ? terminal : vscode.window.createTerminal(`rebol`);
	if (process.platform === 'win32') {
		/*if (vscode.window.activeTerminal.name !== 'bash') {
			text = "cmd --% /c \"";
		}*/
	}
	text = text + "\"" + command + "\"";
	text = text + " " + args;
	terminal.sendText(text);
	terminal.show();
}

function getFileName(fileUri?: vscode.Uri): string {
	let filePath = '';
	if (fileUri === null || fileUri === undefined || typeof fileUri.fsPath !== 'string') {
		const activeEditor = vscode.window.activeTextEditor;
		if (activeEditor !== undefined) {
			if (!activeEditor.document.isUntitled) {
				if ((activeEditor.document.languageId === 'rebol') || (activeEditor.document.languageId === 'rebols')) {
					filePath = activeEditor.document.fileName;
				} else {
					vscode.window.showErrorMessage('The active file is not a rebol or rebol/System source file');
				}
			} else {
				vscode.window.showErrorMessage('The active file needs to be saved before it can be run');
			}
		} else {
			vscode.window.showErrorMessage('No open file to run in terminal');
		}
	} else {
		return fileUri.fsPath;
	}
	return filePath;
}

export function rebolRunInConsole(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	let filePath = getFileName(fileUri);
	if (filePath === '') {return;}
	let ext = path.parse(filePath).ext.toLowerCase();
	if (ext !== ".sc") {
		vscode.window.showErrorMessage("don't support " + ext + " file");
		return;
	}
	filePath = normalFile(filePath);
	filePath = "\"" + filePath + "\"";

	let command: string;
	let args: string;
	if (rebolTool === '') {
		command = rebolConfigs.rebolConsole;
		args = filePath;
	} else {
		command = rebolTool;
		args = "--cli " + filePath;
	}
	command = normalFile(command);

	execCommand(command, args);
}

export function rebolRunInGuiConsole(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	let filePath = getFileName(fileUri);
	if (filePath === '') {return;}
	let ext = path.parse(filePath).ext.toLowerCase();
	if (ext !== ".sc") {
		vscode.window.showErrorMessage("don't support " + ext + " file");
		return;
	}
	filePath = normalFile(filePath);
	filePath = "\"" + filePath + "\"";

	let command: string;
	let args: string;
	if (rebolTool === '') {
		command = rebolConfigs.rebolGuiConsole;
		args = filePath;
	} else {
		command = rebolTool;
		args = filePath;
	}
	command = normalFile(command);

	execCommand(command, args);
}

export function rebolCompileInConsole(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	if (rebolTool === '') {
		vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
		return;
	}
	let filePath = getFileName(fileUri);
	if (filePath === '') {return;}
	let ext = path.parse(filePath).ext.toLowerCase();
	if (ext === ".rebols") {
		//rebolsCompile(fileUri);
		return;
	}
	if (ext !== ".sc") {
		vscode.window.showErrorMessage("don't support " + ext + " file");
		return;
	}

	let buildDir = getBuildDir(filePath);
	let outName = getOutFileName(buildDir, filePath);
	outName = normalFile(outName);
	outName = "\"" + outName + "\"";

	filePath = normalFile(filePath);
	filePath = "\"" + filePath + "\"";

	let command= rebolTool;
	let args = "-c -o " + outName + " " + filePath;
	command = normalFile(command);
	execCommand(command, args);
}
/*
export function rebolCompileInGuiConsole(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	if (rebolTool === '') {
		vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
		return;
	}
	let filePath = getFileName(fileUri);
	if (filePath === '') {return;}
	let ext = path.parse(filePath).ext.toLowerCase();
	if (ext !== ".sc") {
		vscode.window.showErrorMessage("don't support " + ext + " file");
		return;
	}

	let buildDir = getBuildDir(filePath);
	let outName = getOutFileName(buildDir, filePath);
	let target = getTarget();
	outName = normalFile(outName);
	outName = "\"" + outName + "\"";

	filePath = normalFile(filePath);
	filePath = "\"" + filePath + "\"";

	let command= rebolTool;
	let args = "-c -t " + target + " -o " + outName + " " + filePath;
	command = normalFile(command);
	execCommand(command, args);
}

export function rebolCompileInRelease(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	if (rebolTool === '') {
		vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
		return;
	}
	let filePath = getFileName(fileUri);
	if (filePath === '') {return;}
	let ext = path.parse(filePath).ext.toLowerCase();
	if (ext !== ".sc") {
		vscode.window.showErrorMessage("don't support " + ext + " file");
		return;
	}

	let buildDir = getBuildDir(filePath);
	let outName = getOutFileName(buildDir, filePath);
	let target = getTarget();
	outName = normalFile(outName);
	outName = "\"" + outName + "\"";

	filePath = normalFile(filePath);
	filePath = "\"" + filePath + "\"";

	let command= rebolTool;
	let args = "-r -t " + target + " -o " + outName + " " + filePath;
	command = normalFile(command);
	execCommand(command, args);
}

export function rebolCompileClear(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	if (rebolTool === '') {
		vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
		return;
	}
	let filePath = getFileName(fileUri);
	let buildDir = getBuildDir(filePath);
	buildDir = normalFile(buildDir);
	buildDir = "\"" + buildDir + "\"";

	let command= rebolTool;
	let args = "clear " + buildDir;
	command = normalFile(command);
	execCommand(command, args);
}

export function rebolCompileUpdate(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	if (rebolTool === '') {
		vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
		return;
	}
	let filePath = getFileName(fileUri);
	if (filePath === '') {return;}
	let ext = path.parse(filePath).ext.toLowerCase();
	if (ext !== ".sc") {
		vscode.window.showErrorMessage("don't support " + ext + " file");
		return;
	}

	let buildDir = getBuildDir(filePath);
	let outName = getOutFileName(buildDir, filePath);
	outName = normalFile(outName);
	outName = "\"" + outName + "\"";

	filePath = normalFile(filePath);
	filePath = "\"" + filePath + "\"";

	let command= rebolTool;
	let args = "-u -c -o " + outName + " " + filePath;
	command = normalFile(command);
	execCommand(command, args);
}

function rebolsCompile(fileUri?: vscode.Uri) {
	let rebolConfigs = rebolConfiguration.getInstance();
	let rebolTool = rebolConfigs.rebolToolChain;
	if (rebolTool === '') {
		vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
		return;
	}
	let filePath = getFileName(fileUri);
	if (filePath === '') {return;}
	let ext = path.parse(filePath).ext.toLowerCase();
	if (ext !== ".rebols") {
		vscode.window.showErrorMessage("don't support " + ext + " file");
		return;
	}

	let buildDir = getBuildDir(filePath);
	let outName = getOutFileName(buildDir, filePath);
	outName = normalFile(outName);
	outName = "\"" + outName + "\"";

	filePath = normalFile(filePath);
	filePath = "\"" + filePath + "\"";

	let command= rebolTool;
	let args = "-r -o " + outName + " " + filePath;
	command = normalFile(command);
	execCommand(command, args);
}
*/
export function setCommandMenu() {
	
	
}

