"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ConsoleExternalFeatures = exports.getCOBOLSourceFormat = void 0;
const util_1 = __importDefault(require("util"));
const path_1 = __importDefault(require("path"));
const fs_1 = __importDefault(require("fs"));
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
const externalfeatures_1 = require("./externalfeatures");
const fileutils_1 = require("./fileutils");
const inline_sourceformat = ['sourceformat', '>>source format'];
function isNumber(value) {
    if (value.toString().length === 0) {
        return false;
    }
    return !isNaN(Number(value.toString()));
}
function isValidFixedLine(line) {
    if (line.length > 7) {
        switch (line[6]) {
            case '*': return true;
            case 'D': return true;
            case '/': return true;
            case ' ': return true;
            case '-': return true;
        }
    }
    return false;
}
function getCOBOLSourceFormat(doc, config, checkForTerminalFormat) {
    if (config.fileformat_strategy === "always_fixed") {
        return externalfeatures_1.ESourceFormat.fixed;
    }
    let linesWithJustNumbers = 0;
    let linesWithIdenticalAreaB = 0;
    const maxLines = doc.getLineCount() > 10 ? 10 : doc.getLineCount();
    let defFormat = externalfeatures_1.ESourceFormat.unknown;
    let prevRightMargin = "";
    let validFixedLines = 0;
    for (let i = 0; i < maxLines; i++) {
        const lineText = doc.getLine(i, true);
        if (lineText === undefined) {
            break;
        }
        const line = lineText.toLowerCase();
        const validFixedLine = isValidFixedLine(line);
        if (validFixedLine) {
            validFixedLines++;
        }
        // acu
        if (defFormat === externalfeatures_1.ESourceFormat.unknown && checkForTerminalFormat) {
            if (line.startsWith("*") || line.startsWith("|") || line.startsWith("\\D")) {
                defFormat = externalfeatures_1.ESourceFormat.terminal;
            }
        }
        // non-acu
        if (defFormat === externalfeatures_1.ESourceFormat.unknown && !checkForTerminalFormat) {
            const newcommentPos = line.indexOf("*>");
            if (newcommentPos !== -1 && defFormat === externalfeatures_1.ESourceFormat.unknown) {
                defFormat = externalfeatures_1.ESourceFormat.variable;
            }
        }
        let pos4sourceformat_after = 0;
        for (let isf = 0; isf < inline_sourceformat.length; isf++) {
            const pos4sourceformat = line.indexOf(inline_sourceformat[isf]);
            if (pos4sourceformat !== -1) {
                pos4sourceformat_after = pos4sourceformat + inline_sourceformat[isf].length + 1;
                break;
            }
        }
        // does it contain a inline comments? no
        if (pos4sourceformat_after === 0) {
            if (line.length > 80) {
                defFormat = externalfeatures_1.ESourceFormat.variable;
                continue;
            }
            else {
                if (isValidFixedLine(line)) {
                    if (line.length > 72) {
                        const rightMargin = line.substr(72).trim();
                        if (prevRightMargin === rightMargin) {
                            linesWithIdenticalAreaB++;
                        }
                        else {
                            if (isNumber(rightMargin)) {
                                linesWithJustNumbers++;
                            }
                        }
                        prevRightMargin = rightMargin;
                    }
                }
                else {
                    // if we cannot be sure, then let the default be variable
                    defFormat = externalfeatures_1.ESourceFormat.variable;
                }
            }
            continue;
        }
        else {
            // got a inline comment,yes
            const line2right = line.substr(pos4sourceformat_after);
            if (line2right.indexOf("fixed") !== -1) {
                return externalfeatures_1.ESourceFormat.fixed;
            }
            if (line2right.indexOf("variable") !== -1) {
                return externalfeatures_1.ESourceFormat.variable;
            }
            if (line2right.indexOf("free") !== -1) {
                return externalfeatures_1.ESourceFormat.free;
            }
        }
    }
    if (validFixedLines == maxLines) {
        return externalfeatures_1.ESourceFormat.fixed;
    }
    //it might well be...
    if (linesWithJustNumbers > 7 || linesWithIdenticalAreaB > 7) {
        return externalfeatures_1.ESourceFormat.fixed;
    }
    return defFormat;
}
exports.getCOBOLSourceFormat = getCOBOLSourceFormat;
class ConsoleExternalFeatures {
    constructor() {
        this.workspaceFolders = [];
        this.logTimeThreshold = 500;
    }
    static isFile(sdir) {
        return fileutils_1.COBOLFileUtils.isFile(sdir);
    }
    logMessage(message) {
        if (process.send) {
            process.send(message);
        }
        else {
            console.log(message);
        }
        return;
    }
    logException(message, ex) {
        this.logMessage(ex.name + ": " + message);
        if (ex !== undefined && ex.stack !== undefined) {
            this.logMessage(ex.stack);
        }
        return;
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    logTimedMessage(timeTaken, message, ...parameters) {
        const fixedTimeTaken = " (" + timeTaken.toFixed(2) + "ms)";
        if (timeTaken < this.logTimeThreshold) {
            return false;
        }
        if ((parameters !== undefined || parameters !== null) && parameters.length !== 0) {
            const m = util_1.default.format(message, parameters);
            this.logMessage(m.padEnd(60) + fixedTimeTaken);
        }
        else {
            this.logMessage(message.padEnd(60) + fixedTimeTaken);
        }
        return true;
    }
    performance_now() {
        return Date.now();
    }
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    expandLogicalCopyBookToFilenameOrEmpty(filename, inDirectory, config) {
        return "";
    }
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    getCOBOLSourceFormat(doc, config) {
        return getCOBOLSourceFormat(doc, config, false);
    }
    setWorkspaceFolders(folders) {
        this.workspaceFolders = folders;
    }
    getWorkspaceFolders() {
        return this.workspaceFolders;
    }
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    getFullWorkspaceFilename(sdir, sdirMs) {
        if (this.workspaceFolders.length === 0) {
            this.logMessage("getFullWorkspaceFilename: workspaceFolders.length === 0");
        }
        for (const folder of this.workspaceFolders) {
            const possibleFile = path_1.default.join(folder, sdir);
            if (ConsoleExternalFeatures.isFile(possibleFile)) {
                const stat4src = fs_1.default.statSync(possibleFile, { bigint: true });
                if (sdirMs === stat4src.mtimeMs) {
                    return possibleFile;
                }
                this.logMessage(`getFullWorkspaceFilename: found ${possibleFile} ${sdirMs} !== ${stat4src.mtimeMs}`);
            }
        }
        return undefined;
    }
}
exports.ConsoleExternalFeatures = ConsoleExternalFeatures;
ConsoleExternalFeatures.Default = new ConsoleExternalFeatures();
//# sourceMappingURL=consoleexternalfeatures.js.map