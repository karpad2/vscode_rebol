'use strict';
Object.defineProperty(exports, "__esModule", { value: true });
exports.activate = void 0;
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
const vscode = require("vscode");
const RebolConfiguration_1 = require("./RebolConfiguration");
const commandsProvider_1 = require("./commandsProvider");
const vscodelc = require("vscode-languageclient");
const path = require("path");
let reboldClient;
// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
function activate(context) {
    let config = RebolConfiguration_1.rebolConfiguration.getInstance();
    context.subscriptions.push(vscode.commands.registerCommand("rebol.interpret", () => commandsProvider_1.rebolRunInConsole()));
    context.subscriptions.push(vscode.commands.registerCommand("rebol.interpretGUI", () => commandsProvider_1.rebolRunInGuiConsole()));
    //context.subscriptions.push(vscode.commands.registerCommand("rebol.compile", () => rebolCompileInConsole()));
    //context.subscriptions.push(vscode.commands.registerCommand("rebol.compileGUI", () => rebolCompileInGuiConsole()));
    //context.subscriptions.push(vscode.commands.registerCommand("rebol.compileRelease", () => rebolCompileInRelease()));
    //context.subscriptions.push(vscode.commands.registerCommand("rebol.clear", () => rebolCompileClear()));
    //context.subscriptions.push(vscode.commands.registerCommand("rebol.update", () => rebolCompileUpdate()));
    context.subscriptions.push(vscode.commands.registerCommand("rebol.commandMenu", commandsProvider_1.setCommandMenu));
    console.log("isIntelligence", config.isIntelligence);
    if (!config.isIntelligence) {
        return;
    }
    console.log("rebol console path: ", config.rebolConsole);
    let serverModule = path.join(context.asAbsolutePath("."), "server", "server.rebol");
    let needlog = "debug-off";
    if (config.needRlsDebug) {
        needlog = "debug-on";
    }
    console.log(needlog);
    const serverOptions = {
        run: { command: config.rebolConsole, args: [serverModule, needlog] },
        debug: { command: config.rebolConsole, args: [serverModule, "debug-on"] }
    };
    const clientOptions = {
        documentSelector: [
            { scheme: 'file', language: 'rebol' }
        ],
        initializationOptions: config.allConfigs || {},
        synchronize: {
            configurationSection: ['rebol', 'rebols']
        }
    };
    reboldClient = new vscodelc.LanguageClient('rebol.server', 'rebol Language Server', serverOptions, clientOptions);
    console.log('rebol Language Server is now active!');
    context.subscriptions.push(reboldClient.start());
}
exports.activate = activate;
//# sourceMappingURL=extension.js.map