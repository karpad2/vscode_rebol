"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.FileSourceHandler = void 0;
const fs_1 = __importDefault(require("fs"));
const cobolKeywords_1 = require("./keywords/cobolKeywords");
// let detab = require('detab');
// eslint-disable-next-line @typescript-eslint/no-var-requires
const lineByLine = require('n-readlines');
const externalfeatures_1 = require("./externalfeatures");
const url_1 = require("url");
const path_1 = __importDefault(require("path"));
class FileSourceHandler {
    constructor(document, dumpNumbersInAreaA, commentCallback, features) {
        this.document = document;
        this.dumpNumbersInAreaA = dumpNumbersInAreaA;
        this.commentCallback = commentCallback;
        this.dumpAreaBOnwards = false;
        this.lines = [];
        this.commentCount = 0;
        this.isSourceInWorkspace = false;
        this.updatedSource = new Map();
        if (features === undefined) {
            features = externalfeatures_1.EmptyExternalFeature.Default;
        }
        this.shortFilename = this.findShortWorkspaceFilename(document, features);
        const docstat = fs_1.default.statSync(document, { bigint: true });
        const docChunkSize = docstat.size < 4096 ? 4096 : 96 * 1024;
        let line;
        this.documentVersionId = docstat.mtimeMs;
        const startTime = features.performance_now();
        try {
            const liner = new lineByLine(document, { readChunk: docChunkSize });
            while ((line = liner.next())) {
                this.lines.push(line.toString());
            }
            features.logTimedMessage(features.performance_now() - startTime, ' - Loading File ' + document);
        }
        catch (e) {
            features.logException("File failed! (" + document + ")", e);
        }
    }
    getDocumentVersionId() {
        return this.documentVersionId;
    }
    sendCommentCallback(line, lineNumber) {
        if (this.commentCallback !== undefined) {
            this.commentCallback.processComment(line, this.getFilename(), lineNumber);
        }
    }
    getUriAsString() {
        return url_1.pathToFileURL(this.getFilename()).href;
    }
    getLineCount() {
        return this.lines.length;
    }
    getCommentCount() {
        return this.commentCount;
    }
    getLine(lineNumber, raw) {
        let line = undefined;
        try {
            if (lineNumber >= this.lines.length) {
                return undefined;
            }
            line = this.lines[lineNumber];
            if (raw) {
                return line;
            }
            const startComment = line.indexOf("*>");
            if (startComment !== -1) {
                this.sendCommentCallback(line, lineNumber);
                line = line.substring(0, startComment);
                this.commentCount++;
            }
            // drop fixed format line
            if (line.length > 1 && line[0] === '*') {
                this.commentCount++;
                this.sendCommentCallback(line, lineNumber);
                return "";
            }
            // drop fixed format line
            if (line.length > 7 && line[6] === '*') {
                this.commentCount++;
                this.sendCommentCallback(line, lineNumber);
                return "";
            }
            // todo - this is a bit messy and should be revised
            if (this.dumpNumbersInAreaA) {
                if (line.match(FileSourceHandler.paraPrefixRegex1)) {
                    line = "      " + line.substr(6);
                }
                else {
                    if (line.length > 7 && line[6] === ' ') {
                        const possibleKeyword = line.substr(0, 6).trim();
                        if (this.isValidKeyword(possibleKeyword) === false) {
                            line = "       " + line.substr(6);
                        }
                    }
                }
            }
            if (this.dumpAreaBOnwards && line.length >= 73) {
                line = line.substr(0, 72);
            }
        }
        catch (_a) {
            return undefined;
        }
        return line;
    }
    setDumpAreaA(flag) {
        this.dumpNumbersInAreaA = flag;
    }
    setDumpAreaBOnwards(flag) {
        this.dumpAreaBOnwards = flag;
    }
    isValidKeyword(keyword) {
        return cobolKeywords_1.cobolKeywordDictionary.has(keyword);
    }
    getFilename() {
        return this.document;
    }
    setCommentCallback(commentCallback) {
        this.commentCallback = commentCallback;
    }
    resetCommentCount() {
        this.commentCount = 0;
    }
    getIsSourceInWorkSpace() {
        return this.isSourceInWorkspace;
    }
    getShortWorkspaceFilename() {
        return this.shortFilename;
    }
    getUpdatedLine(linenumber) {
        if (this.updatedSource.has(linenumber)) {
            return this.updatedSource.get(linenumber);
        }
        return this.getLine(linenumber, false);
    }
    setUpdatedLine(lineNumber, line) {
        this.updatedSource.set(lineNumber, line);
    }
    findShortWorkspaceFilename(ddir, features) {
        const ws = features.getWorkspaceFolders();
        if (ws === undefined || ws.length === 0) {
            return "";
        }
        const fullPath = path_1.default.normalize(ddir);
        let bestShortName = "";
        for (const folderPath of ws) {
            if (fullPath.startsWith(folderPath)) {
                const possibleShortPath = fullPath.substr(1 + folderPath.length);
                if (bestShortName.length === 0) {
                    bestShortName = possibleShortPath;
                }
                else {
                    if (possibleShortPath.length < possibleShortPath.length) {
                        bestShortName = possibleShortPath;
                    }
                }
            }
        }
        return bestShortName;
    }
}
exports.FileSourceHandler = FileSourceHandler;
FileSourceHandler.paraPrefixRegex1 = /^[0-9 ][0-9 ][0-9 ][0-9 ][0-9 ][0-9 ]/g;
//# sourceMappingURL=filesourcehandler.js.map