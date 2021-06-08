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
exports.InMemoryGlobalCacheHelper = exports.InMemoryGlobalSymbolCache = void 0;
// eslint-disable-next-line @typescript-eslint/no-var-requires
const path = __importStar(require("path"));
const cobolglobalcache_1 = require("./cobolglobalcache");
const fileutils_1 = require("./fileutils");
exports.InMemoryGlobalSymbolCache = new cobolglobalcache_1.COBOLGlobalSymbolTable();
class InMemoryGlobalCacheHelper {
    static(cacheDirectory) {
        const fn = path.join(cacheDirectory, InMemoryGlobalCacheHelper.globalSymbolFilename);
        const fnStat = fileutils_1.COBOLFileUtils.isFileT(fn);
        if (fnStat[0]) {
            return true;
        }
        return false;
    }
    static getFilenameWithoutPath(fullPath) {
        const lastSlash = fullPath.lastIndexOf(path.sep);
        if (lastSlash === -1) {
            return fullPath;
        }
        return fullPath.substr(1 + lastSlash);
    }
    static addFilename(filename, wsf) {
        if (exports.InMemoryGlobalSymbolCache.sourceFilenameModified.has(filename)) {
            exports.InMemoryGlobalSymbolCache.sourceFilenameModified.delete(filename);
            exports.InMemoryGlobalSymbolCache.sourceFilenameModified.set(filename, wsf);
        }
        else {
            exports.InMemoryGlobalSymbolCache.sourceFilenameModified.set(filename, wsf);
        }
        exports.InMemoryGlobalSymbolCache.isDirty = true;
    }
    static getSourceFilenameModifiedTable() {
        const filenames = [];
        for (const [filename, lastModified] of exports.InMemoryGlobalSymbolCache.sourceFilenameModified) {
            filenames.push(`${filename},${lastModified}`);
        }
        return filenames;
    }
}
exports.InMemoryGlobalCacheHelper = InMemoryGlobalCacheHelper;
InMemoryGlobalCacheHelper.globalSymbolFilename = "globalsymbols.sym";
//# sourceMappingURL=globalcachehelper.js.map