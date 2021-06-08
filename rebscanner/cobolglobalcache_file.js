"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.COBOLSymbolTableHelper = exports.reviver = exports.replacer = void 0;
/* eslint-disable @typescript-eslint/no-explicit-any */
const fs = __importStar(require("fs"));
const path = __importStar(require("path"));
const crypto = __importStar(require("crypto"));
const cobolglobalcache_1 = require("./cobolglobalcache");
const fileutils_1 = require("./fileutils");
// eslint-disable-next-line @typescript-eslint/no-var-requires
const lzjs = require('lzjs');
// JSON callbacks to Map to something that can be serialized
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
function replacer(key, value) {
    if (typeof value === 'bigint') {
        return value.toString();
    }
    const originalObject = this[key];
    if (originalObject instanceof Map) {
        return {
            dataType: 'Map',
            value: Array.from(originalObject.entries()), // or with spread: value: [...originalObject]
        };
    }
    return value;
}
exports.replacer = replacer;
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
// eslint-disable-next-line @typescript-eslint/no-explicit-any
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
function reviver(key, value) {
    if (typeof value === 'object' && value !== null) {
        if (value.dataType === 'Map') {
            if (key === 'sourceFilenameModified') {
                return new Map(value.value);
            }
            return new Map(value.value);
        }
    }
    return value;
}
exports.reviver = reviver;
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function logMessage(mesg) {
    return;
}
class COBOLSymbolTableHelper {
    static getHashForFilename(filename) {
        const hash = crypto.createHash('sha256');
        hash.update(filename);
        return hash.digest('hex');
    }
    static saveToFile(cacheDirectory, st) {
        const fn = path.join(cacheDirectory, this.getHashForFilename(st.fileName) + ".sym");
        fs.writeFileSync(fn, lzjs.compress(JSON.stringify(st, replacer)));
    }
    static getSymbolTableGivenFile(cacheDirectory, nfilename) {
        const filename = path.normalize(nfilename);
        if (cobolglobalcache_1.InMemoryFileSymbolCache.has(filename)) {
            const cachedTable = cobolglobalcache_1.InMemoryFileSymbolCache.get(filename);
            if (cachedTable !== undefined) {
                /* is the cache table still valid? */
                try {
                    const stat4src = fs.statSync(filename, { bigint: true });
                    if (stat4src.mtimeMs === cachedTable.lastModifiedTime) {
                        return cachedTable;
                    }
                }
                catch (e) {
                    //
                }
                cobolglobalcache_1.InMemoryFileSymbolCache.delete(filename); /* drop the invalid cache */
            }
        }
        const fn = path.join(cacheDirectory, this.getHashForFilename(filename) + ".sym");
        const fnStat = fileutils_1.COBOLFileUtils.isFileT(fn);
        if (fnStat[0]) {
            try {
                const stat4cache = fnStat[1];
                const stat4src = fs.statSync(filename);
                if (stat4cache !== undefined && stat4cache.mtimeMs < stat4src.mtimeMs) {
                    // never return a out of date cache
                    try {
                        fs.unlinkSync(fn);
                    }
                    catch (e) {
                        //
                    }
                    return undefined;
                }
            }
            catch (e) {
                // never return a out of date cache
                try {
                    fs.unlinkSync(fn);
                }
                catch (e) {
                    //
                }
                return undefined;
            }
            const str = fs.readFileSync(fn).toString();
            try {
                const cacheableTable = JSON.parse(lzjs.decompress(str), reviver);
                cobolglobalcache_1.InMemoryFileSymbolCache.set(filename, cacheableTable);
                return cacheableTable;
            }
            catch (_a) {
                try {
                    fs.unlinkSync(fn);
                }
                catch (_b) {
                    logMessage(`Unable to remove symbol file : ${fn}`);
                }
                logMessage(` Symbol file removed : ${fn}`);
                return undefined;
            }
        }
        return undefined;
    }
    static getSymbolTable_direct(nfilename) {
        const str = fs.readFileSync(nfilename).toString();
        try {
            return JSON.parse(lzjs.decompress(str), reviver);
        }
        catch (_a) {
            try {
                fs.unlinkSync(nfilename);
            }
            catch (_b) {
                logMessage(`Unable to remove symbol file : ${nfilename}`);
            }
            logMessage(`Symbol file removed ${nfilename}`);
        }
    }
}
exports.COBOLSymbolTableHelper = COBOLSymbolTableHelper;
//# sourceMappingURL=cobolglobalcache_file.js.map