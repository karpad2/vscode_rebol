"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
Object.defineProperty(exports, "__esModule", { value: true });
exports.InMemoryFileSymbolCache = exports.COBOLSymbol = exports.COBOLFileSymbol = exports.COBOLSymbolTable = exports.COBOLGlobalSymbolTable = exports.COBOLWorkspaceFile = void 0;
class COBOLWorkspaceFile {
    constructor(lastModifiedTime, workspaceFilename) {
        this.lastModifiedTime = lastModifiedTime;
        this.workspaceFilename = workspaceFilename;
    }
}
exports.COBOLWorkspaceFile = COBOLWorkspaceFile;
class COBOLGlobalSymbolTable {
    constructor() {
        this.defaultCallableSymbols = new Map();
        this.callableSymbols = new Map();
        this.entryPoints = new Map();
        this.types = new Map();
        this.interfaces = new Map();
        this.enums = new Map();
        this.knownCopybooks = new Map();
        this.isDirty = false;
        this.sourceFilenameModified = new Map();
    }
    // eslint-disable-next-line @typescript-eslint/ban-types
    static fromJSON(d) {
        return Object.assign(new COBOLGlobalSymbolTable(), d);
    }
}
exports.COBOLGlobalSymbolTable = COBOLGlobalSymbolTable;
class COBOLSymbolTable {
    constructor() {
        this.lastModifiedTime = BigInt("0");
        this.fileName = "";
        this.variableSymbols = new Map();
        this.labelSymbols = new Map();
    }
    // eslint-disable-next-line @typescript-eslint/ban-types
    static fromJSON(d) {
        return Object.assign(new COBOLSymbolTable(), d);
    }
}
exports.COBOLSymbolTable = COBOLSymbolTable;
class COBOLFileSymbol {
    constructor(symbol, lineNumber) {
        this.filename = symbol === undefined ? "" : symbol;
        this.lnum = lineNumber === undefined ? 0 : lineNumber;
    }
    // eslint-disable-next-line @typescript-eslint/ban-types
    static fromJSON(d) {
        return Object.assign(new COBOLFileSymbol(), d);
    }
}
exports.COBOLFileSymbol = COBOLFileSymbol;
class COBOLSymbol {
    constructor(symbol, lineNumber) {
        this.symbol = symbol;
        this.lnum = lineNumber;
    }
    static fromJSON(d) {
        return Object.assign(new COBOLSymbol(), d);
    }
}
exports.COBOLSymbol = COBOLSymbol;
exports.InMemoryFileSymbolCache = new Map();
//# sourceMappingURL=cobolglobalcache.js.map