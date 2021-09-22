'use strict';

import { workspace, WorkspaceConfiguration } from 'vscode';
import * as path from 'path';
import * as fs from 'fs';

function folderExists(path: fs.PathLike)
{
	try
	{
		return fs.statSync(path).isDirectory();
	}
	catch (err)
	{
		return false;
	}
}

function getrebolConsole(gui: boolean) {
	let preBuiltPath: string;
	if (process.platform === 'win32') {
		preBuiltPath = path.join(process.env.ALLUSERSPROFILE || "c:", 'rebol');
	} else {
		preBuiltPath = path.join(process.env.HOME || "/tmp", '.rebol');
		if (!folderExists(preBuiltPath)) {
			preBuiltPath = "/tmp/.rebol/";
		}
	}
	try {
		let files = fs.readdirSync(preBuiltPath);
		let _console = '';
		let startsWith = 'console-';
		if (gui) {startsWith = 'gui-console-';}
		for (let i in files) {
			let name = files[i];
			let ext = path.extname(name);
			if (name.startsWith(startsWith) && (ext === '.exe' || ext === '')) {
				if (_console === '') {
					_console = name;
				} else {
					let stamps1 = path.basename(_console, ext).split("-");
					let stamps2 = path.basename(name, ext).split("-");
					for (let j in stamps2) {
						if (+stamps1[j] < +stamps2[j]) {
							_console = name;
							break;
						}
					}
				}
			}
		}
		return path.join(preBuiltPath, _console);
	}
	catch (err) {
		return '';
	}
}

export class rebolConfiguration {
	private static rebolConfigs: rebolConfiguration = new rebolConfiguration();
	public static getInstance(): rebolConfiguration {
		return rebolConfiguration.rebolConfigs;
	}
	public get isIntelligence(): boolean {
		return this.configuration.get<boolean>('rebol.intelligence', true);
	}

	public get needRlsDebug(): boolean {
		return this.configuration.get<boolean>('rebol.rls-debug', false);
	}

	public get rebolToolChain(): string {
		return this.configuration.get<string>('rebol.rebolPath', '');
	}

	public get rebolExcludedPath(): string { 
		return this.configuration.get<string>('rebol.excludedPath', ''); 
	}

	public get rebolConsole(): string {
		return getrebolConsole(false);
	}

	public get rebolGuiConsole(): string {
		return getrebolConsole(true);
	}

	public get rebolWorkSpace(): string {
		return this.configuration.get<string>('rebol.buildDir', '');
	}

	public get allConfigs(): any {
		return this.configuration.get('rebol', {}) as any;
	}

	private readonly configuration: WorkspaceConfiguration;

	constructor() {
		if (rebolConfiguration.rebolConfigs) {
			throw new Error('Singleton class, Use getInstance method');
		}
		this.configuration = workspace.getConfiguration();
	}
}
