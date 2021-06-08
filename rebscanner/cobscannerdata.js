"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ScanDataHelper = exports.COBSCANNER_KNOWNCOPYBOOK = exports.COBSCANNER_ADDFILE = exports.COBSCANNER_SENDCLASS = exports.COBSCANNER_SENDENUM = exports.COBSCANNER_SENDINTERFACE = exports.COBSCANNER_SENDEP = exports.COBSCANNER_SENDPRGID = exports.COBSCANNER_STATUS = exports.ScanStats = exports.ScanData = void 0;
/* eslint-disable @typescript-eslint/no-explicit-any */
const path_1 = __importDefault(require("path"));
const fs_1 = __importDefault(require("fs"));
class ScanData {
    constructor() {
        this.sendOnCount = 0;
        this.sendOnFileCount = 0;
        this.sendPercent = 0;
        this.scannerBinDir = "";
        this.directoriesScanned = 0;
        this.maxDirectoryDepth = 0;
        this.fileCount = 0;
        this.parse_copybooks_for_references = false;
        this.cache_metadata_show_progress_messages = false;
        this.cacheDirectory = "";
        this.Files = [];
        this.showStats = true;
        this.md_symbols = [];
        this.md_entrypoints = [];
        this.md_types = [];
        this.md_metadata_files = [];
        this.md_metadata_knowncopybooks = [];
        this.workspaceFolders = [];
    }
}
exports.ScanData = ScanData;
class ScanStats {
    constructor() {
        this.directoriesScanned = 0;
        this.directoryDepth = 0;
        this.maxDirectoryDepth = 0;
        this.filesScanned = 0;
        this.copyBookExts = 0;
        this.fileCount = 0;
        this.filesUptodate = 0;
        this.programsDefined = 0;
        this.entryPointsDefined = 0;
        this.start = 0;
        this.endTime = 0;
        this.showMessage = false;
    }
}
exports.ScanStats = ScanStats;
exports.COBSCANNER_STATUS = '@@STATUS';
exports.COBSCANNER_SENDPRGID = '@@SEND.PRGID';
exports.COBSCANNER_SENDEP = '@@SEND.EP';
exports.COBSCANNER_SENDINTERFACE = "@@SEND.INTID";
exports.COBSCANNER_SENDENUM = "@@SEND.ENUMID";
exports.COBSCANNER_SENDCLASS = "@@SEND.CLASSID";
exports.COBSCANNER_ADDFILE = "@@SEND.FILES";
exports.COBSCANNER_KNOWNCOPYBOOK = "@@SEND.KNOWNCOPYBOOK";
function replacer(key, value) {
    if (typeof value === 'bigint') {
        return value.toString();
    }
    return value;
}
function reviver(key, value) {
    if (typeof value === 'bigint' && value !== null) {
        return BigInt(value);
    }
    return value;
}
class ScanDataHelper {
    static save(cacheDirectory, st) {
        const fn = path_1.default.join(cacheDirectory, ScanDataHelper.scanFilename);
        fs_1.default.writeFileSync(fn, JSON.stringify(st, replacer));
        return fn;
    }
    static load(fn) {
        // const fn = path.join(cacheDirectory, ScanDataHelper.scanFilename);
        const str = fs_1.default.readFileSync(fn).toString();
        return JSON.parse(str);
    }
    static parseScanData(str) {
        return JSON.parse(str, reviver);
    }
    static getScanData(st) {
        return JSON.stringify(st, replacer);
    }
    static setupPercent(scanData, numberOfFiles, percentIncrement) {
        scanData.sendOnFileCount = numberOfFiles;
        scanData.sendPercent = percentIncrement;
        scanData.sendOnCount = Math.round(scanData.sendOnFileCount * (scanData.sendPercent / 100));
    }
}
exports.ScanDataHelper = ScanDataHelper;
ScanDataHelper.scanFilename = "cobscanner.json";
//# sourceMappingURL=cobscannerdata.js.map