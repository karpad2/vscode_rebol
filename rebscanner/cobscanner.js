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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.workerThreadData = exports.Scanner = void 0;
const cobolsourcescanner_1 = __importStar(require("./cobolsourcescanner"));
const cobolsymboltableeventhelper_1 = require("./cobolsymboltableeventhelper");
const cobscannerdata_1 = require("./cobscannerdata");
const consoleexternalfeatures_1 = require("./consoleexternalfeatures");
const crypto = __importStar(require("crypto"));
const fs = __importStar(require("fs"));
const os = __importStar(require("os"));
const path_1 = __importDefault(require("path"));
const worker_threads_1 = require("worker_threads");
const cobolworkspacecache_1 = require("./cobolworkspacecache");
const globalcachehelper_1 = require("./globalcachehelper");
const filesourcehandler_1 = require("./filesourcehandler");
const iconfiguration_1 = require("./iconfiguration");
const fileutils_1 = require("./fileutils");
const args = process.argv.slice(2);
const settings = new iconfiguration_1.COBOLSettings();
const progressPercentage = 5;
class Utils {
    static msleep(n) {
        Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, n);
    }
    static sleep(n) {
        Utils.msleep(n * 1000);
    }
    static getHashForFilename(filename) {
        const hash = crypto.createHash('sha256');
        hash.update(filename);
        return hash.digest('hex');
    }
    static cacheUpdateRequired(cacheDirectory, nfilename, features) {
        const filename = path_1.default.normalize(nfilename);
        const cachedMtimeWS = globalcachehelper_1.InMemoryGlobalSymbolCache.sourceFilenameModified.get(filename);
        const cachedMtime = cachedMtimeWS === null || cachedMtimeWS === void 0 ? void 0 : cachedMtimeWS.lastModifiedTime;
        if (cachedMtime !== undefined) {
            const stat4src = fs.statSync(filename, { bigint: true });
            if (cachedMtime < stat4src.mtimeMs) {
                features.logMessage(`cacheUpdateRequired : ${nfilename}, cachedMtime=${cachedMtime} < ${stat4src.mtimeMs}`);
                return true;
            }
            return false;
        }
        if (cacheDirectory !== null && cacheDirectory.length !== 0) {
            const fn = path_1.default.join(cacheDirectory, this.getHashForFilename(filename) + ".sym");
            const fnStat = fileutils_1.COBOLFileUtils.isFileT(fn);
            if (fnStat[0]) {
                const stat4cache = fnStat[1];
                const stat4src = fs.statSync(filename, { bigint: true });
                if (stat4cache !== undefined && stat4cache.mtimeMs < stat4src.mtimeMs) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }
    static performance_now() {
        if (!process.env.BROWSER) {
            try {
                // eslint-disable-next-line @typescript-eslint/no-var-requires
                return require('performance-now').performance.now;
            }
            catch (_a) {
                return Date.now();
            }
        }
        return Date.now();
    }
}
class processSender {
    sendMessage(message) {
        if (process.send) {
            process.send(message);
        }
    }
}
processSender.Default = new processSender();
class Scanner {
    static transferScanDataToGlobals(scanData, features) {
        features.setWorkspaceFolders(scanData.workspaceFolders);
        // may need more..
        settings.parse_copybooks_for_references = scanData.parse_copybooks_for_references;
        settings.cache_metadata_show_progress_messages = scanData.cache_metadata_show_progress_messages;
        // TODO: add in other metadata items
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.loadGlobalCacheFromArray(settings, scanData.md_symbols, true);
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.loadGlobalEntryCacheFromArray(settings, scanData.md_entrypoints, true);
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.loadGlobalTypesCacheFromArray(settings, scanData.md_types, true);
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.loadFileCacheFromArray(settings, features, scanData.md_metadata_files, true);
        cobolworkspacecache_1.COBOLWorkspaceSymbolCacheHelper.loadGlobalKnownCopybooksFromArray(settings, scanData.md_metadata_knowncopybooks, true);
    }
    static processFileShowHeader(stats, features) {
        if (stats.directoriesScanned !== 0) {
            features.logMessage(` Directories scanned   : ${stats.directoriesScanned}`);
        }
        if (stats.maxDirectoryDepth !== 0) {
            features.logMessage(` Directory Depth       : ${stats.maxDirectoryDepth}`);
        }
        if (stats.fileCount) {
            features.logMessage(` Files found           : ${stats.fileCount}`);
        }
    }
    static processFileShowFooter(stats, features, aborted) {
        if (stats.filesScanned !== 0) {
            features.logMessage(` Files scanned         : ${stats.filesScanned}`);
        }
        if (stats.filesUptodate !== 0) {
            features.logMessage(` Files up to date      : ${stats.filesUptodate}`);
        }
        if (stats.programsDefined !== 0) {
            features.logMessage(` Program Count         : ${stats.programsDefined}`);
        }
        if (stats.entryPointsDefined !== 0) {
            features.logMessage(` Entry-Point Count     : ${stats.entryPointsDefined}`);
        }
        const completedMessage = (aborted ? `Scan aborted (elapsed time ${stats.endTime})` : 'Scan completed');
        if (features.logTimedMessage(stats.endTime, completedMessage) === false) {
            features.logMessage(completedMessage);
        }
    }
    static processFiles(scanData, features, sender, stats) {
        let aborted = false;
        stats.start = Utils.performance_now();
        stats.directoriesScanned = scanData.directoriesScanned;
        stats.maxDirectoryDepth = scanData.maxDirectoryDepth;
        stats.fileCount = scanData.fileCount;
        if (scanData.showStats) {
            Scanner.processFileShowHeader(stats, features);
        }
        try {
            let fSendCount = 0;
            for (const file of scanData.Files) {
                const cacheDir = scanData.cacheDirectory;
                if (Utils.cacheUpdateRequired(cacheDir, file, features)) {
                    const filesHandler = new filesourcehandler_1.FileSourceHandler(file, false);
                    const config = new iconfiguration_1.COBOLSettings();
                    config.parse_copybooks_for_references = scanData.parse_copybooks_for_references;
                    const symbolCatcher = new cobolsymboltableeventhelper_1.COBOLSymbolTableEventHelper(config, sender);
                    const qcp = new cobolsourcescanner_1.default(filesHandler, config, cacheDir, new cobolsourcescanner_1.SharedSourceReferences(config, true), config.parse_copybooks_for_references, symbolCatcher, features);
                    if (qcp.callTargets.size > 0) {
                        stats.programsDefined++;
                        if (qcp.callTargets !== undefined) {
                            stats.entryPointsDefined += (qcp.callTargets.size - 1);
                        }
                    }
                    if (scanData.cache_metadata_show_progress_messages) {
                        features.logMessage(`  Parse completed: ${file}`);
                    }
                    stats.filesScanned++;
                }
                else {
                    stats.filesUptodate++;
                }
                fSendCount++;
                if (fSendCount === scanData.sendOnCount) {
                    sender.sendMessage(`${cobscannerdata_1.COBSCANNER_STATUS} ${scanData.sendPercent}`);
                    fSendCount = 0;
                }
            }
        }
        catch (e) {
            features.logException("cobscanner", e);
            aborted = true;
        }
        finally {
            stats.endTime = Utils.performance_now() - stats.start;
            if (scanData.showStats) {
                this.processFileShowFooter(stats, features, aborted);
            }
        }
    }
}
exports.Scanner = Scanner;
let lastJsonFile = "";
class workerThreadData {
    constructor(scanData) {
        this.scanData = scanData;
    }
}
exports.workerThreadData = workerThreadData;
for (const arg of args) {
    const argLower = arg.toLowerCase();
    if (argLower.endsWith(".json")) {
        const features = consoleexternalfeatures_1.ConsoleExternalFeatures.Default;
        try {
            lastJsonFile = arg;
            const scanData = cobscannerdata_1.ScanDataHelper.load(arg);
            const scanStats = new cobscannerdata_1.ScanStats();
            cobscannerdata_1.ScanDataHelper.setupPercent(scanData, scanData.Files.length, progressPercentage);
            Scanner.transferScanDataToGlobals(scanData, features);
            Scanner.processFiles(scanData, features, processSender.Default, scanStats);
        }
        catch (e) {
            if (e instanceof SyntaxError) {
                features.logMessage(`Unable to load ${arg}`);
            }
            else {
                features.logException("cobscanner", e);
            }
        }
    }
    else {
        switch (argLower) {
            case 'useenv':
                {
                    const features = consoleexternalfeatures_1.ConsoleExternalFeatures.Default;
                    try {
                        const SCANDATA_ENV = process.env.SCANDATA;
                        if (SCANDATA_ENV !== undefined) {
                            const scanData = cobscannerdata_1.ScanDataHelper.parseScanData(SCANDATA_ENV);
                            cobscannerdata_1.ScanDataHelper.setupPercent(scanData, scanData.Files.length, progressPercentage);
                            const scanStats = new cobscannerdata_1.ScanStats();
                            Scanner.transferScanDataToGlobals(scanData, features);
                            Scanner.processFiles(scanData, features, processSender.Default, scanStats);
                            features.logMessage(`${cobscannerdata_1.COBSCANNER_STATUS} 100`);
                        }
                        else {
                            features.logMessage(`SCANDATA not found in environment`);
                        }
                    }
                    catch (e) {
                        if (e instanceof SyntaxError) {
                            features.logMessage(`Unable to load ${arg}`);
                        }
                        else {
                            features.logException("cobscanner", e);
                        }
                    }
                }
                break;
            case 'useenv_threaded':
                {
                    const features = consoleexternalfeatures_1.ConsoleExternalFeatures.Default;
                    try {
                        const SCANDATA_ENV = process.env.SCANDATA;
                        if (SCANDATA_ENV !== undefined) {
                            const baseScanData = cobscannerdata_1.ScanDataHelper.parseScanData(SCANDATA_ENV);
                            cobscannerdata_1.ScanDataHelper.setupPercent(baseScanData, baseScanData.Files.length, progressPercentage);
                            Scanner.transferScanDataToGlobals(baseScanData, features);
                            let _numCpus = os.cpus().length * 0.75; // only use 75% of CPUs
                            const _numCpuEnv = process.env.SCANDATA_TCOUNT;
                            if (_numCpuEnv !== undefined) {
                                _numCpus = Number.parseInt(_numCpuEnv);
                            }
                            let i, j;
                            const files = baseScanData.Files;
                            const threadCount = _numCpus - 1;
                            const chunkSize = files.length / threadCount;
                            const threadStats = [];
                            const jsFile = path_1.default.join(baseScanData.scannerBinDir, "cobscanner_worker.js");
                            const startTime = Utils.performance_now();
                            const combinedStats = new cobscannerdata_1.ScanStats();
                            combinedStats.start = startTime;
                            combinedStats.directoriesScanned = baseScanData.directoriesScanned;
                            combinedStats.maxDirectoryDepth = baseScanData.maxDirectoryDepth;
                            combinedStats.fileCount = baseScanData.fileCount;
                            Scanner.processFileShowHeader(combinedStats, features);
                            for (i = 0, j = files.length; i < j; i += chunkSize) {
                                const sfFileChunk = files.slice(i, i + chunkSize);
                                const scanData = Object.assign({}, baseScanData);
                                scanData.Files = sfFileChunk;
                                scanData.fileCount = sfFileChunk.length;
                                const wtd = new workerThreadData(scanData);
                                const worker = new worker_threads_1.Worker(jsFile, { workerData: wtd });
                                //Listen for a message from worker
                                worker.on("message", result => {
                                    const resStr = result;
                                    if (resStr.startsWith("++")) {
                                        const s = JSON.parse(resStr.substr(2));
                                        threadStats.push(s);
                                    }
                                    else {
                                        features.logMessage(resStr);
                                    }
                                });
                                worker.on("error", error => {
                                    features.logException("usethreads", error);
                                });
                                // eslint-disable-next-line @typescript-eslint/no-unused-vars
                                worker.on("exit", exitCode => {
                                    if (threadStats.length === threadCount) {
                                        for (const tstat of threadStats) {
                                            combinedStats.filesScanned += tstat.filesScanned;
                                            combinedStats.filesUptodate += tstat.filesUptodate;
                                            combinedStats.programsDefined += tstat.programsDefined;
                                            combinedStats.entryPointsDefined += tstat.entryPointsDefined;
                                        }
                                        threadStats.length = 0;
                                        combinedStats.endTime = Utils.performance_now() - startTime;
                                        features.logMessage(`${cobscannerdata_1.COBSCANNER_STATUS} 100`);
                                        Scanner.processFileShowFooter(combinedStats, features, false);
                                    }
                                });
                            }
                        }
                    }
                    catch (e) {
                        features.logException("cobscanner", e);
                    }
                    finally {
                        //
                    }
                }
                break;
            default:
                // features.logMessage(`INFO: arg passed is ${arg}`);
                break;
        }
    }
}
if (lastJsonFile.length !== 0) {
    try {
        // delete the json file
        fs.unlinkSync(lastJsonFile);
    }
    catch (_a) {
        //continue
    }
}
//# sourceMappingURL=cobscanner.js.map