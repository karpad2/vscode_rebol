"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.COBOLSymbolTableEventHelper = void 0;
const globalcachehelper_1 = require("./globalcachehelper");
const cobolsourcescanner_1 = require("./cobolsourcescanner");
const cobolglobalcache_1 = require("./cobolglobalcache");
const cobolglobalcache_file_1 = require("./cobolglobalcache_file");
const externalfeatures_1 = require("./externalfeatures");
const cobolworkspacecache_1 = require("./cobolworkspacecache");
const cobscannerdata_1 = require("./cobscannerdata");
class COBOLSymbolTableEventHelper {
    constructor(config, sender) {
        this.sender = sender;
        this.config = config;
        this.parse_copybooks_for_references = config.parse_copybooks_for_references;
    }
    start(qp) {
        var _a, _b, _c, _d, _e, _f, _g, _h;
        this.qp = qp;
        this.st = new cobolglobalcache_1.COBOLSymbolTable();
        this.st.fileName = qp.filename;
        this.st.lastModifiedTime = qp.lastModifiedTime;
        if (((_a = this.st) === null || _a === void 0 ? void 0 : _a.fileName) !== undefined && this.st.lastModifiedTime !== undefined) {
            globalcachehelper_1.InMemoryGlobalCacheHelper.addFilename((_b = this.st) === null || _b === void 0 ? void 0 : _b.fileName, qp.workspaceFile);
            if (this.sender !== undefined) {
                this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_ADDFILE},${(_c = this.st) === null || _c === void 0 ? void 0 : _c.lastModifiedTime},${(_d = this.st) === null || _d === void 0 ? void 0 : _d.fileName}`);
            }
        }
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.removeAllPrograms((_e = this.st) === null || _e === void 0 ? void 0 : _e.fileName);
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.removeAllProgramEntryPoints((_f = this.st) === null || _f === void 0 ? void 0 : _f.fileName);
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.removeAllTypes((_g = this.st) === null || _g === void 0 ? void 0 : _g.fileName);
        globalcachehelper_1.InMemoryGlobalCacheHelper.addFilename((_h = this.st) === null || _h === void 0 ? void 0 : _h.fileName, qp.workspaceFile);
    }
    processToken(token) {
        // hidden token should not be placed in the symbol table, as they from a different file
        if (token.ignoreInOutlineView) {
            return;
        }
        if (this.st === undefined) {
            return;
        }
        if (this.parse_copybooks_for_references === false) {
            switch (token.tokenType) {
                case cobolsourcescanner_1.COBOLTokenStyle.Union:
                    this.st.variableSymbols.set(token.tokenNameLower, new cobolglobalcache_1.COBOLSymbol(token.tokenName, token.startLine));
                    break;
                case cobolsourcescanner_1.COBOLTokenStyle.Constant:
                    this.st.variableSymbols.set(token.tokenNameLower, new cobolglobalcache_1.COBOLSymbol(token.tokenName, token.startLine));
                    break;
                case cobolsourcescanner_1.COBOLTokenStyle.ConditionName:
                    this.st.variableSymbols.set(token.tokenNameLower, new cobolglobalcache_1.COBOLSymbol(token.tokenName, token.startLine));
                    break;
                case cobolsourcescanner_1.COBOLTokenStyle.Variable:
                    this.st.variableSymbols.set(token.tokenNameLower, new cobolglobalcache_1.COBOLSymbol(token.tokenName, token.startLine));
                    break;
                case cobolsourcescanner_1.COBOLTokenStyle.Paragraph:
                    this.st.labelSymbols.set(token.tokenNameLower, new cobolglobalcache_1.COBOLSymbol(token.tokenName, token.startLine));
                    break;
                case cobolsourcescanner_1.COBOLTokenStyle.Section:
                    this.st.labelSymbols.set(token.tokenNameLower, new cobolglobalcache_1.COBOLSymbol(token.tokenName, token.startLine));
                    break;
            }
        }
        switch (token.tokenType) {
            case cobolsourcescanner_1.COBOLTokenStyle.CopyBook:
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_KNOWNCOPYBOOK},${token.tokenName},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.CopyBookInOrOf:
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_KNOWNCOPYBOOK},${token.tokenName},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.ImplicitProgramId:
                cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.addCalableSymbol(this.st.fileName, token.tokenNameLower, token.startLine);
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_SENDPRGID},${token.tokenName},${token.startLine},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.ProgramId:
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_SENDPRGID},${token.tokenName},${token.startLine},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.EntryPoint:
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_SENDEP},${token.tokenName},${token.startLine},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.InterfaceId:
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_SENDINTERFACE},${token.tokenName},${token.startLine},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.EnumId:
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_SENDENUM},${token.tokenName},${token.startLine},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.ClassId:
                if (this.sender) {
                    this.sender.sendMessage(`${cobscannerdata_1.COBSCANNER_SENDCLASS},${token.tokenName},${token.startLine},${this.st.fileName}`);
                }
                break;
            case cobolsourcescanner_1.COBOLTokenStyle.MethodId:
                // GlobalCachesHelper.addMethodSymbol(this.st.fileName, token.tokenName, token.startLine);
                break;
        }
    }
    finish() {
        if (this.st !== undefined && this.qp !== undefined) {
            if (this.config.cache_metadata !== externalfeatures_1.CacheDirectoryStrategy.Off &&
                this.parse_copybooks_for_references === false) {
                cobolglobalcache_file_1.COBOLSymbolTableHelper.saveToFile(this.qp.deprecatedCacheDirectory, this.st);
            }
        }
    }
}
exports.COBOLSymbolTableEventHelper = COBOLSymbolTableEventHelper;
//# sourceMappingURL=cobolsymboltableeventhelper.js.map