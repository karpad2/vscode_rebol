"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.EmptyExternalFeature = exports.CobolLinterProviderSymbols = exports.CacheDirectoryStrategy = exports.ESourceFormat = void 0;
var ESourceFormat;
(function (ESourceFormat) {
    ESourceFormat["unknown"] = "unknown";
    ESourceFormat["fixed"] = "fixed";
    ESourceFormat["free"] = "free";
    ESourceFormat["terminal"] = "terminal";
    ESourceFormat["variable"] = "variable";
    ESourceFormat["jcl"] = "jcl";
})(ESourceFormat = exports.ESourceFormat || (exports.ESourceFormat = {}));
var CacheDirectoryStrategy;
(function (CacheDirectoryStrategy) {
    CacheDirectoryStrategy["Workspace"] = "workspace";
    CacheDirectoryStrategy["UserDefinedDirectory"] = "user_defined_directory";
    CacheDirectoryStrategy["Off"] = "off";
})(CacheDirectoryStrategy = exports.CacheDirectoryStrategy || (exports.CacheDirectoryStrategy = {}));
class CobolLinterProviderSymbols {
}
exports.CobolLinterProviderSymbols = CobolLinterProviderSymbols;
CobolLinterProviderSymbols.NotReferencedMarker_internal = "COBOL_NOT_REF";
CobolLinterProviderSymbols.NotReferencedMarker_external = "ignore";
class EmptyExternalFeature {
    logMessage(message) {
        return;
    }
    logException(message, ex) {
        return;
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    logTimedMessage(timeTaken, message, ...parameters) {
        return false;
    }
    performance_now() {
        return Date.now();
    }
    expandLogicalCopyBookToFilenameOrEmpty(filename, inDirectory, config) {
        return "";
    }
    getCOBOLSourceFormat(doc, config) {
        return ESourceFormat.unknown;
    }
    getFullWorkspaceFilename(sdir, sdirMs) {
        return undefined;
    }
    setWorkspaceFolders(_folders) {
        //
    }
    getWorkspaceFolders() {
        return [];
    }
}
exports.EmptyExternalFeature = EmptyExternalFeature;
EmptyExternalFeature.Default = new EmptyExternalFeature();
//# sourceMappingURL=externalfeatures.js.map