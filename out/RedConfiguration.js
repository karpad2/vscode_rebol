'use strict';
Object.defineProperty(exports, "__esModule", { value: true });
exports.RedConfiguration = void 0;
const vscode_1 = require("vscode");
const path = require("path");
const fs = require("fs");
function folderExists(path) {
    try {
        return fs.statSync(path).isDirectory();
    }
    catch (err) {
        return false;
    }
}
function getRedConsole(gui) {
    let preBuiltPath;
    if (process.platform === 'win32') {
        preBuiltPath = path.join(process.env.ALLUSERSPROFILE || "c:", 'Red');
    }
    else {
        preBuiltPath = path.join(process.env.HOME || "/tmp", '.red');
        if (!folderExists(preBuiltPath)) {
            preBuiltPath = "/tmp/.red/";
        }
    }
    try {
        let files = fs.readdirSync(preBuiltPath);
        let _console = '';
        let startsWith = 'console-';
        if (gui) {
            startsWith = 'gui-console-';
        }
        for (let i in files) {
            let name = files[i];
            let ext = path.extname(name);
            if (name.startsWith(startsWith) && (ext === '.exe' || ext === '')) {
                if (_console === '') {
                    _console = name;
                }
                else {
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
class RedConfiguration {
    constructor() {
        if (RedConfiguration.redConfigs) {
            throw new Error('Singleton class, Use getInstance method');
        }
        this.configuration = vscode_1.workspace.getConfiguration();
    }
    static getInstance() {
        return RedConfiguration.redConfigs;
    }
    get isIntelligence() {
        return this.configuration.get('red.intelligence', true);
    }
    get needRlsDebug() {
        return this.configuration.get('red.rls-debug', false);
    }
    get redToolChain() {
        return this.configuration.get('red.redPath', '');
    }
    get redExcludedPath() {
        return this.configuration.get('red.excludedPath', '');
    }
    get redConsole() {
        return getRedConsole(false);
    }
    get redGuiConsole() {
        return getRedConsole(true);
    }
    get redWorkSpace() {
        return this.configuration.get('red.buildDir', '');
    }
    get allConfigs() {
        return this.configuration.get('red', {});
    }
}
exports.RedConfiguration = RedConfiguration;
RedConfiguration.redConfigs = new RedConfiguration();
//# sourceMappingURL=RedConfiguration.js.map