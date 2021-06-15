'use strict';
Object.defineProperty(exports, "__esModule", { value: true });
exports.rebolConfiguration = void 0;
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
function getrebolConsole(gui) {
    let preBuiltPath;
    if (process.platform === 'win32') {
        preBuiltPath = path.join(process.env.ALLUSERSPROFILE || "c:", 'rebol');
    }
    else {
        preBuiltPath = path.join(process.env.HOME || "/tmp", '.rebol');
        if (!folderExists(preBuiltPath)) {
            preBuiltPath = "/tmp/.rebol/";
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
class rebolConfiguration {
    constructor() {
        if (rebolConfiguration.rebolConfigs) {
            throw new Error('Singleton class, Use getInstance method');
        }
        this.configuration = vscode_1.workspace.getConfiguration();
    }
    static getInstance() {
        return rebolConfiguration.rebolConfigs;
    }
    get isIntelligence() {
        return this.configuration.get('rebol.intelligence', true);
    }
    get needRlsDebug() {
        return this.configuration.get('rebol.rls-debug', false);
    }
    get rebolToolChain() {
        return this.configuration.get('rebol.rebolPath', '');
    }
    get rebolExcludedPath() {
        return this.configuration.get('rebol.excludedPath', '');
    }
    get rebolConsole() {
        return getrebolConsole(false);
    }
    get rebolGuiConsole() {
        return getrebolConsole(true);
    }
    get rebolWorkSpace() {
        return this.configuration.get('rebol.buildDir', '');
    }
    get allConfigs() {
        return this.configuration.get('rebol', {});
    }
}
exports.rebolConfiguration = rebolConfiguration;
rebolConfiguration.rebolConfigs = new rebolConfiguration();
//# sourceMappingURL=RebolConfiguration.js.map