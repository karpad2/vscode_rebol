'use strict';

const vscode = require('vscode');

function extensionConfig() {
    return vscode.workspace.getConfiguration('rebol');
}

module.exports = {
    extensionConfig: extensionConfig
};
