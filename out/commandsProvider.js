'use strict';
Object.defineProperty(exports, "__esModule", { value: true });
exports.setCommandMenu = exports.rebolCompileUpdate = exports.rebolCompileClear = exports.rebolCompileInRelease = exports.rebolCompileInGuiConsole = exports.rebolCompileInConsole = exports.rebolRunInGuiConsole = exports.rebolRunInConsole = void 0;
const vscode = require("vscode");
const RebolConfiguration_1 = require("./RebolConfiguration");
const path = require("path");
let terminal;
function getTarget() {
    if (process.platform === 'win32') {
        return "Windows";
    }
    else if (process.platform === 'darwin') {
        return "macOS";
    }
    else {
        return '';
    }
}
function getBuildDir(filePath) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    return rebolConfigs.rebolWorkSpace || vscode.workspace.rootPath || path.dirname(filePath);
}
function getOutFileName(buildDir, filePath) {
    let outName = path.join(buildDir, path.parse(filePath).name);
    if (process.platform === 'win32') {
        outName = outName + ".exe";
    }
    return outName;
}
function normalFile(value) {
    return value.replace(/\\/g, '/');
}
function execCommand(command, args) {
    let text = "";
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
function getFileName(fileUri) {
    let filePath = '';
    if (fileUri === null || fileUri === undefined || typeof fileUri.fsPath !== 'string') {
        const activeEditor = vscode.window.activeTextEditor;
        if (activeEditor !== undefined) {
            if (!activeEditor.document.isUntitled) {
                if ((activeEditor.document.languageId === 'rebol') || (activeEditor.document.languageId === 'rebols')) {
                    filePath = activeEditor.document.fileName;
                }
                else {
                    vscode.window.showErrorMessage('The active file is not a rebol or rebol/System source file');
                }
            }
            else {
                vscode.window.showErrorMessage('The active file needs to be saved before it can be run');
            }
        }
        else {
            vscode.window.showErrorMessage('No open file to run in terminal');
        }
    }
    else {
        return fileUri.fsPath;
    }
    return filePath;
}
function rebolRunInConsole(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    let filePath = getFileName(fileUri);
    if (filePath === '') {
        return;
    }
    let ext = path.parse(filePath).ext.toLowerCase();
    if (ext !== ".sc") {
        vscode.window.showErrorMessage("don't support " + ext + " file");
        return;
    }
    filePath = normalFile(filePath);
    filePath = "\"" + filePath + "\"";
    let command;
    let args;
    if (rebolTool === '') {
        command = rebolConfigs.rebolConsole;
        args = filePath;
    }
    else {
        command = rebolTool;
        args = "--cli " + filePath;
    }
    command = normalFile(command);
    execCommand(command, args);
}
exports.rebolRunInConsole = rebolRunInConsole;
function rebolRunInGuiConsole(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    let filePath = getFileName(fileUri);
    if (filePath === '') {
        return;
    }
    let ext = path.parse(filePath).ext.toLowerCase();
    if (ext !== ".sc") {
        vscode.window.showErrorMessage("don't support " + ext + " file");
        return;
    }
    filePath = normalFile(filePath);
    filePath = "\"" + filePath + "\"";
    let command;
    let args;
    if (rebolTool === '') {
        command = rebolConfigs.rebolGuiConsole;
        args = filePath;
    }
    else {
        command = rebolTool;
        args = filePath;
    }
    command = normalFile(command);
    execCommand(command, args);
}
exports.rebolRunInGuiConsole = rebolRunInGuiConsole;
function rebolCompileInConsole(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    if (rebolTool === '') {
        vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
        return;
    }
    let filePath = getFileName(fileUri);
    if (filePath === '') {
        return;
    }
    let ext = path.parse(filePath).ext.toLowerCase();
    if (ext === ".sc") {
        rebolsCompile(fileUri);
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
    let command = rebolTool;
    let args = "-c -o " + outName + " " + filePath;
    command = normalFile(command);
    execCommand(command, args);
}
exports.rebolCompileInConsole = rebolCompileInConsole;
function rebolCompileInGuiConsole(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    if (rebolTool === '') {
        vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
        return;
    }
    let filePath = getFileName(fileUri);
    if (filePath === '') {
        return;
    }
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
    let command = rebolTool;
    let args = "-c -t " + target + " -o " + outName + " " + filePath;
    command = normalFile(command);
    execCommand(command, args);
}
exports.rebolCompileInGuiConsole = rebolCompileInGuiConsole;
function rebolCompileInRelease(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    if (rebolTool === '') {
        vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
        return;
    }
    let filePath = getFileName(fileUri);
    if (filePath === '') {
        return;
    }
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
    let command = rebolTool;
    let args = "-r -t " + target + " -o " + outName + " " + filePath;
    command = normalFile(command);
    execCommand(command, args);
}
exports.rebolCompileInRelease = rebolCompileInRelease;
function rebolCompileClear(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    if (rebolTool === '') {
        vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
        return;
    }
    let filePath = getFileName(fileUri);
    let buildDir = getBuildDir(filePath);
    buildDir = normalFile(buildDir);
    buildDir = "\"" + buildDir + "\"";
    let command = rebolTool;
    let args = "clear " + buildDir;
    command = normalFile(command);
    execCommand(command, args);
}
exports.rebolCompileClear = rebolCompileClear;
function rebolCompileUpdate(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    if (rebolTool === '') {
        vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
        return;
    }
    let filePath = getFileName(fileUri);
    if (filePath === '') {
        return;
    }
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
    let command = rebolTool;
    let args = "-u -c -o " + outName + " " + filePath;
    command = normalFile(command);
    execCommand(command, args);
}
exports.rebolCompileUpdate = rebolCompileUpdate;
function rebolsCompile(fileUri) {
    let rebolConfigs = RebolConfiguration_1.rebolConfiguration.getInstance();
    let rebolTool = rebolConfigs.rebolToolChain;
    if (rebolTool === '') {
        vscode.window.showErrorMessage('No rebol compiler! Please configure the `rebol.rebolPath` in `settings.json`');
        return;
    }
    let filePath = getFileName(fileUri);
    if (filePath === '') {
        return;
    }
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
    let command = rebolTool;
    let args = "-r -o " + outName + " " + filePath;
    command = normalFile(command);
    execCommand(command, args);
}
function setCommandMenu() {
    const options = [
        {
            label: 'Run Current Script',
            description: '',
            command: 'rebol.interpret'
        },
        {
            label: 'Run Current Script in GUI Console',
            description: '',
            command: 'rebol.interpretGUI'
        },
        {
            label: 'Compile Current Script',
            description: '',
            command: 'rebol.compile'
        },
        {
            label: 'Compile Current Script (GUI mode)',
            description: '',
            command: 'rebol.compileGUI'
        },
        {
            label: 'Compile Current Script (Release mode)',
            description: '',
            command: 'rebol.compileRelease'
        },
        {
            label: 'Delete all temporary files (librebolRT, etc)',
            description: '',
            command: 'rebol.clear'
        },
        {
            label: 'Update librebolRT and Compile Current script',
            description: '',
            command: 'rebol.update'
        }
    ];
    vscode.window.showQuickPick(options).then(option => {
        if (!option || !option.command || option.command.length === 0) {
            return;
        }
        vscode.commands.executeCommand(option.command);
    });
}
exports.setCommandMenu = setCommandMenu;
//# sourceMappingURL=commandsProvider.js.map