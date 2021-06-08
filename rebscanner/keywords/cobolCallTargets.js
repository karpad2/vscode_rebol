"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getCallTarget = exports.CallTarget = void 0;
const ile_datetime_1 = __importDefault(require("./ile_datetime"));
const mf_cbl_apis_1 = __importDefault(require("./mf_cbl_apis"));
class CallTarget {
    constructor(_api, _url, _description) {
        this.api = _api;
        this.url = _url;
        this.description = _description;
    }
}
exports.CallTarget = CallTarget;
const callTargets = {};
function addApis(a) {
    const values = Object.keys(a.apis);
    for (let c = 0; c < values.length; c++) {
        const value = values[c];
        callTargets[value] = new CallTarget(value, a.url, a.apis[value]);
    }
}
addApis(ile_datetime_1.default);
addApis(mf_cbl_apis_1.default);
/* inline decl */
function getCallTarget(api) {
    if (typeof callTargets[api] === "undefined") {
        return undefined;
    }
    return callTargets[api];
}
exports.getCallTarget = getCallTarget;
//# sourceMappingURL=cobolCallTargets.js.map