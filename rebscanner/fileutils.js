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
exports.COBOLFileUtils = void 0;
const fs = __importStar(require("fs"));
class COBOLFileUtils {
    static isFileT(sdir) {
        try {
            if (fs.existsSync(sdir)) {
                const f = fs.statSync(sdir, { bigint: true });
                if (f && f.isFile()) {
                    return [true, f];
                }
            }
        }
        catch (_a) {
            return [false, undefined];
        }
        return [false, undefined];
    }
    static isFile(sdir) {
        try {
            if (fs.existsSync(sdir)) {
                // not on windows, do extra check for +x perms (protects exe & dirs)
                // if (!COBOLFileUtils.isWin32) {
                //     try {
                //         fs.accessSync(sdir, fs.constants.F_OK | fs.constants.X_OK);
                //         return false;
                //     }
                //     catch {
                //         return true;
                //     }
                // }
                return true;
            }
        }
        catch (_a) {
            return false;
        }
        return false;
    }
    static isValidCopybookExtension(filename, settings) {
        const lastDot = filename.lastIndexOf(".");
        let extension = filename;
        if (lastDot !== -1) {
            extension = filename.substr(1 + lastDot);
        }
        const exts = settings.copybookexts;
        for (let extpos = 0; extpos < exts.length; extpos++) {
            if (exts[extpos] === extension) {
                return true;
            }
        }
        return false;
    }
    static isValidProgramExtension(filename, settings) {
        const lastDot = filename.lastIndexOf(".");
        let extension = "";
        if (lastDot !== -1) {
            extension = filename.substr(1 + lastDot);
        }
        const exts = settings.program_extensions;
        for (let extpos = 0; extpos < exts.length; extpos++) {
            if (exts[extpos] === extension) {
                return true;
            }
        }
        return false;
    }
    static isDirectPath(dir) {
        if (dir === undefined && dir === null) {
            return false;
        }
        if (COBOLFileUtils.isWin32) {
            if (dir.length > 2 && dir[1] === ':') {
                return true;
            }
            if (dir.length > 1 && dir[0] === '\\') {
                return true;
            }
            return false;
        }
        if (dir.length > 1 && dir[0] === '/') {
            return true;
        }
        return false;
    }
    // only handle unc filenames
    static isNetworkPath(dir) {
        if (dir === undefined && dir === null) {
            return false;
        }
        if (COBOLFileUtils.isWin32) {
            if (dir.length > 1 && dir[0] === '\\') {
                return true;
            }
        }
        return false;
    }
}
exports.COBOLFileUtils = COBOLFileUtils;
COBOLFileUtils.isWin32 = process.platform === "win32";
//# sourceMappingURL=fileutils.js.map