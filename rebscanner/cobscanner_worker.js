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
exports.ThreadConsoleExternalFeatures = void 0;
const worker_threads_1 = require("worker_threads");
const cobscanner_1 = require("./cobscanner");
const cobscannerdata_1 = require("./cobscannerdata");
const fs = __importStar(require("fs"));
const path = __importStar(require("path"));
const util_1 = __importDefault(require("util"));
const consoleexternalfeatures_1 = require("./consoleexternalfeatures");
const fileutils_1 = require("./fileutils");
// class WorkerUtils {
//     public static msleep(n: number) {
//         Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, n);
//     }
//     public static sleep(n: number) {
//         WorkerUtils.msleep(n * 1000);
//     }
// }
class ThreadConsoleExternalFeatures {
    constructor() {
        this.workspaceFolders = [];
        this.logTimeThreshold = 500;
    }
    static isFile(sdir) {
        return fileutils_1.COBOLFileUtils.isFile(sdir);
    }
    logMessage(message) {
        if (worker_threads_1.parentPort !== null) {
            worker_threads_1.parentPort.postMessage(message);
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
        return consoleexternalfeatures_1.getCOBOLSourceFormat(doc, config, false);
    }
    setWorkspaceFolders(folders) {
        this.workspaceFolders = folders;
    }
    getWorkspaceFolders() {
        return this.workspaceFolders;
    }
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    getFullWorkspaceFilename(sdir, sdirMs) {
        for (const folder of this.workspaceFolders) {
            const possibleFile = path.join(folder, sdir);
            if (ThreadConsoleExternalFeatures.isFile(possibleFile)) {
                const stat4src = fs.statSync(possibleFile, { bigint: true });
                if (sdirMs === stat4src.mtimeMs) {
                    return possibleFile;
                }
            }
        }
        return undefined;
    }
}
exports.ThreadConsoleExternalFeatures = ThreadConsoleExternalFeatures;
ThreadConsoleExternalFeatures.Default = new ThreadConsoleExternalFeatures();
class threadSender {
    sendMessage(message) {
        if (worker_threads_1.parentPort !== null) {
            worker_threads_1.parentPort.postMessage(message);
        }
    }
}
threadSender.Default = new threadSender();
if (worker_threads_1.parentPort !== null) {
    try {
        const features = ThreadConsoleExternalFeatures.Default;
        const wd = worker_threads_1.workerData;
        const scanData = wd.scanData;
        scanData.showStats = false;
        const sd = new cobscannerdata_1.ScanStats();
        cobscanner_1.Scanner.transferScanDataToGlobals(scanData, features);
        cobscanner_1.Scanner.processFiles(scanData, features, threadSender.Default, sd);
        worker_threads_1.parentPort.postMessage(`++${JSON.stringify(sd)}`);
    }
    catch (e) {
        threadSender.Default.sendMessage(e.message);
    }
}
//# sourceMappingURL=cobscanner_worker.js.map