"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.COBOLWorkspaceSymbolCacheHelper = exports.TypeCategory = void 0;
const path_1 = __importDefault(require("path"));
const cobolglobalcache_1 = require("./cobolglobalcache");
const externalfeatures_1 = require("./externalfeatures");
const globalcachehelper_1 = require("./globalcachehelper");
var TypeCategory;
(function (TypeCategory) {
    TypeCategory["ClassId"] = "T";
    TypeCategory["InterfaceId"] = "I";
    TypeCategory["EnumId"] = "E";
})(TypeCategory = exports.TypeCategory || (exports.TypeCategory = {}));
class COBOLWorkspaceSymbolCacheHelper {
    static removeAllProgramSymbols(srcfilename, symbolsCache) {
        for (const [key] of symbolsCache) {
            const symbolList = symbolsCache.get(key);
            if (symbolList !== undefined) {
                const newSymbols = [];
                for (let i = 0; i < symbolList.length; i++) {
                    if (symbolList[i].filename !== srcfilename) {
                        newSymbols.push(symbolList[i]);
                    }
                }
                if (newSymbols.length !== symbolList.length) {
                    if (newSymbols.length !== 0) {
                        symbolsCache.set(key, newSymbols);
                    }
                    else {
                        symbolsCache.delete(key);
                    }
                }
            }
        }
    }
    static addSymbolToCache(srcfilename, symbolUnchanged, lineNumber, symbolsCache) {
        const symbol = symbolUnchanged.toLowerCase();
        if (symbolsCache.has(symbol)) {
            const symbolList = symbolsCache.get(symbol);
            /* search the list of COBOLFileSymbols */
            if (symbolList !== undefined) {
                let foundCount = 0;
                let foundLast = 0;
                let foundLastNonFileSymbol = -1;
                for (let i = 0; i < symbolList.length; i++) {
                    if (symbolList[i].filename === srcfilename) {
                        foundLast = i;
                        foundCount++;
                        // remember last non file line number
                        if (symbolList[i].lnum !== 1) {
                            foundLastNonFileSymbol = i;
                        }
                    }
                }
                // not found?
                if (foundCount === 0) {
                    symbolList.push(new cobolglobalcache_1.COBOLFileSymbol(srcfilename, lineNumber));
                    globalcachehelper_1.InMemoryGlobalSymbolCache.isDirty = true;
                    return;
                }
                // if we have only one symbol, then we can update it
                if (foundCount === 1) {
                    symbolList[foundLast].lnum = lineNumber;
                    globalcachehelper_1.InMemoryGlobalSymbolCache.isDirty = true;
                }
                else {
                    // if we have multiple, never update the filename symbol which has a line number of 1
                    if (foundLastNonFileSymbol !== -1) {
                        symbolList[foundLastNonFileSymbol].lnum = lineNumber;
                        globalcachehelper_1.InMemoryGlobalSymbolCache.isDirty = true;
                    }
                }
                return;
            }
        }
        const symbolList = [];
        symbolList.push(new cobolglobalcache_1.COBOLFileSymbol(srcfilename, lineNumber));
        symbolsCache.set(symbol, symbolList);
        globalcachehelper_1.InMemoryGlobalSymbolCache.isDirty = true;
        return;
    }
    static isValidLiteral(id) {
        if (id === null || id.length === 0) {
            return false;
        }
        if (id.match(COBOLWorkspaceSymbolCacheHelper.literalRegex)) {
            return true;
        }
        return false;
    }
    static addCalableSymbol(srcfilename, symbolUnchanged, lineNumber) {
        if (srcfilename.length === 0 || symbolUnchanged.length === 0) {
            return;
        }
        const fileName = globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename);
        const fileNameNoExt = path_1.default.basename(fileName, path_1.default.extname(fileName));
        const callableSymbolFromFilenameLower = fileNameNoExt.toLowerCase();
        if (COBOLWorkspaceSymbolCacheHelper.isValidLiteral(symbolUnchanged) === false) {
            return;
        }
        if (symbolUnchanged.toLowerCase() == callableSymbolFromFilenameLower) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.defaultCallableSymbols.set(callableSymbolFromFilenameLower, srcfilename);
            return;
        }
        // drop the defaultCallableSymbols if it has a real one
        if (lineNumber !== 0) {
            if (globalcachehelper_1.InMemoryGlobalSymbolCache.defaultCallableSymbols.has(callableSymbolFromFilenameLower)) {
                globalcachehelper_1.InMemoryGlobalSymbolCache.defaultCallableSymbols.delete(callableSymbolFromFilenameLower);
                COBOLWorkspaceSymbolCacheHelper.addSymbolToCache(fileName, symbolUnchanged, lineNumber, globalcachehelper_1.InMemoryGlobalSymbolCache.callableSymbols);
                return;
            }
        }
        if (globalcachehelper_1.InMemoryGlobalSymbolCache.defaultCallableSymbols.has(callableSymbolFromFilenameLower) === false) {
            COBOLWorkspaceSymbolCacheHelper.addSymbolToCache(fileName, symbolUnchanged, lineNumber, globalcachehelper_1.InMemoryGlobalSymbolCache.callableSymbols);
        }
    }
    static addEntryPoint(srcfilename, symbolUnchanged, lineNumber) {
        if (srcfilename.length === 0 || symbolUnchanged.length === 0) {
            return;
        }
        COBOLWorkspaceSymbolCacheHelper.addSymbolToCache(globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename), symbolUnchanged, lineNumber, globalcachehelper_1.InMemoryGlobalSymbolCache.entryPoints);
    }
    static addReferencedCopybook(copybook, fullInFilename) {
        const inFilename = globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(fullInFilename);
        const encodedKey = `${copybook},${inFilename}`;
        if (!globalcachehelper_1.InMemoryGlobalSymbolCache.knownCopybooks.has(encodedKey)) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.knownCopybooks.set(encodedKey, copybook);
            globalcachehelper_1.InMemoryGlobalSymbolCache.isDirty = true;
        }
    }
    static removeAllCopybookReferences(fullInFilename) {
        const inFilename = globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(fullInFilename);
        const keysToRemove = [];
        for (const [encodedKey,] of globalcachehelper_1.InMemoryGlobalSymbolCache.knownCopybooks) {
            const keys = encodedKey.split(",");
            if (keys[1] === inFilename) {
                keysToRemove.push(encodedKey);
            }
        }
        for (const key of keysToRemove) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.knownCopybooks.delete(key);
        }
    }
    static addClass(srcfilename, symbolUnchanged, lineNumber, category) {
        let map = globalcachehelper_1.InMemoryGlobalSymbolCache.types;
        switch (category) {
            case TypeCategory.ClassId:
                map = globalcachehelper_1.InMemoryGlobalSymbolCache.types;
                break;
            case TypeCategory.InterfaceId:
                map = globalcachehelper_1.InMemoryGlobalSymbolCache.interfaces;
                break;
            case TypeCategory.EnumId:
                map = globalcachehelper_1.InMemoryGlobalSymbolCache.enums;
                break;
        }
        COBOLWorkspaceSymbolCacheHelper.addSymbolToCache(globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename), symbolUnchanged, lineNumber, map);
    }
    static removeAllPrograms(srcfilename) {
        COBOLWorkspaceSymbolCacheHelper.removeAllProgramSymbols(globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename), globalcachehelper_1.InMemoryGlobalSymbolCache.callableSymbols);
    }
    static removeAllProgramEntryPoints(srcfilename) {
        COBOLWorkspaceSymbolCacheHelper.removeAllProgramSymbols(globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename), globalcachehelper_1.InMemoryGlobalSymbolCache.entryPoints);
    }
    static removeAllTypes(srcfilename) {
        COBOLWorkspaceSymbolCacheHelper.removeAllProgramSymbols(globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename), globalcachehelper_1.InMemoryGlobalSymbolCache.types);
        COBOLWorkspaceSymbolCacheHelper.removeAllProgramSymbols(globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename), globalcachehelper_1.InMemoryGlobalSymbolCache.enums);
        COBOLWorkspaceSymbolCacheHelper.removeAllProgramSymbols(globalcachehelper_1.InMemoryGlobalCacheHelper.getFilenameWithoutPath(srcfilename), globalcachehelper_1.InMemoryGlobalSymbolCache.interfaces);
    }
    static loadGlobalCacheFromArray(settings, symbols, clear) {
        const depMode = settings.cache_metadata !== externalfeatures_1.CacheDirectoryStrategy.Off;
        if (clear || depMode) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.callableSymbols.clear();
        }
        if (!depMode) {
            for (const symbol of symbols) {
                const symbolValues = symbol.split(",");
                if (symbolValues.length === 2) {
                    COBOLWorkspaceSymbolCacheHelper.addCalableSymbol(symbolValues[1], symbolValues[0], 0);
                }
                if (symbolValues.length === 3) {
                    COBOLWorkspaceSymbolCacheHelper.addCalableSymbol(symbolValues[1], symbolValues[0], Number.parseInt(symbolValues[2]));
                }
            }
        }
    }
    static loadGlobalEntryCacheFromArray(settings, symbols, clear) {
        const depMode = settings.cache_metadata !== externalfeatures_1.CacheDirectoryStrategy.Off;
        if (clear || depMode) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.entryPoints.clear();
        }
        if (!depMode) {
            for (const symbol of symbols) {
                const symbolValues = symbol.split(",");
                if (symbolValues.length === 3) {
                    COBOLWorkspaceSymbolCacheHelper.addEntryPoint(symbolValues[1], symbolValues[0], Number.parseInt(symbolValues[2], 10));
                }
            }
        }
    }
    static loadGlobalKnownCopybooksFromArray(settings, copybookValues, clear) {
        const depMode = settings.cache_metadata !== externalfeatures_1.CacheDirectoryStrategy.Off;
        if (clear || depMode) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.knownCopybooks.clear();
        }
        if (!depMode) {
            for (const symbol of copybookValues) {
                const encodedKey = symbol.split(",");
                if (encodedKey.length === 2) {
                    globalcachehelper_1.InMemoryGlobalSymbolCache.knownCopybooks.set(symbol, encodedKey[1]);
                }
            }
        }
    }
    static loadGlobalTypesCacheFromArray(settings, symbols, clear) {
        const depMode = settings.cache_metadata !== externalfeatures_1.CacheDirectoryStrategy.Off;
        if (clear || depMode) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.enums.clear();
            globalcachehelper_1.InMemoryGlobalSymbolCache.interfaces.clear();
            globalcachehelper_1.InMemoryGlobalSymbolCache.types.clear();
        }
        if (!depMode) {
            for (const symbol of symbols) {
                const symbolValues = symbol.split(",");
                if (symbolValues.length === 4) {
                    let cat = TypeCategory.ClassId;
                    switch (symbolValues[0]) {
                        case "I":
                            cat = TypeCategory.InterfaceId;
                            break;
                        case "T":
                            cat = TypeCategory.ClassId;
                            break;
                        case "E":
                            cat = TypeCategory.EnumId;
                            break;
                    }
                    COBOLWorkspaceSymbolCacheHelper.addClass(symbolValues[2], symbolValues[1], Number.parseInt(symbolValues[3], 10), cat);
                }
            }
        }
    }
    static loadFileCacheFromArray(settings, externalFeatures, files, clear) {
        const depMode = settings.cache_metadata !== externalfeatures_1.CacheDirectoryStrategy.Off;
        if (clear || depMode) {
            globalcachehelper_1.InMemoryGlobalSymbolCache.sourceFilenameModified.clear();
        }
        if (!depMode) {
            for (const symbol of files) {
                const fileValues = symbol.split(",");
                if (fileValues.length === 2) {
                    const ms = BigInt(fileValues[0]);
                    const fullDir = externalFeatures.getFullWorkspaceFilename(fileValues[1], ms);
                    if (fullDir !== undefined) {
                        const cws = new cobolglobalcache_1.COBOLWorkspaceFile(ms, fileValues[1]);
                        globalcachehelper_1.InMemoryGlobalSymbolCache.sourceFilenameModified.set(fullDir, cws);
                    }
                    else {
                        COBOLWorkspaceSymbolCacheHelper.removeAllProgramEntryPoints(fileValues[1]);
                        COBOLWorkspaceSymbolCacheHelper.removeAllTypes(fileValues[1]);
                        // externalFeatures.logMessage(`loadFileCacheFromArray, could not ${fileValues[1]} with ${ms}`);
                    }
                }
            }
        }
    }
}
exports.COBOLWorkspaceSymbolCacheHelper = COBOLWorkspaceSymbolCacheHelper;
COBOLWorkspaceSymbolCacheHelper.literalRegex = /^[#a-zA-Z0-9][a-zA-Z0-9-_]*$/g;
//# sourceMappingURL=cobolworkspacecache.js.map