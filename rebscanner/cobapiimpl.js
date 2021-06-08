"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.CobApiOutput = exports.CobApiHandle = void 0;
class CobApiHandle {
    // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
    constructor(packageJson, features) {
        this.callback = undefined;
        this.packageJson = packageJson;
        this.externalFeatures = features;
        if (packageJson.name !== undefined && packageJson.publisher !== undefined) {
            this.id = `${packageJson.publisher}.${packageJson.name}`;
        }
        else {
            this.id = "";
        }
        if (packageJson.description !== undefined) {
            this.description = `${packageJson.description}`;
        }
        else {
            this.description = "";
        }
        if (packageJson.bugs !== undefined && packageJson.bugs.url !== undefined) {
            this.bugReportUrl = `${packageJson.bugs.url}`;
        }
        else {
            this.bugReportUrl = "";
        }
        if (packageJson.bugs !== undefined && packageJson.bugs.email !== undefined) {
            this.bugReportEmail = `${packageJson.bugs.email}`;
        }
        else {
            this.bugReportEmail = "";
        }
        // validate
        if (this.id.length === 0) {
            throw new Error("Invalid packageJSON, no id present (registerPreprocessor)");
        }
        if (this.bugReportEmail.length === 0) {
            throw new Error("Invalid packageJSON, no bug email address present (registerPreprocessor)");
        }
        if (this.bugReportUrl.length === 0) {
            throw new Error("Invalid packageJSON, no bug url present to report issue (registerPreprocessor)");
        }
        this.info = `${this.id}`;
    }
    logWarningMessage(message) {
        this.externalFeatures.logMessage(`[${this.info}]: ${message}`);
    }
}
exports.CobApiHandle = CobApiHandle;
class CobApiOutput {
    constructor() {
        this.lines = [];
        this.externalFiles = new Map();
    }
    addLine(line) {
        this.lines.push(line);
    }
    addLines(lines) {
        for (const line of lines) {
            this.lines.push(line);
        }
    }
    addFileSymbol(symbol, copybookName) {
        this.externalFiles.set(symbol, copybookName);
    }
}
exports.CobApiOutput = CobApiOutput;
//# sourceMappingURL=cobapiimpl.js.map