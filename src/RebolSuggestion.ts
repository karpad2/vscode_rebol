
import * as vscode from 'vscode';

export function completions (context: vscode.ExtensionContext) {

const provider1 = vscode.languages.registerCompletionItemProvider('plaintext', {

    provideCompletionItems(document: vscode.TextDocument, position: vscode.Position, token: vscode.CancellationToken, context: vscode.CompletionContext) {

        // a simple completion item which inserts `Hello World!`
        const simpleCompletion = new vscode.CompletionItem('Hello World!');

        // a completion item that inserts its text as snippet,
        // the `insertText`-property is a `SnippetString` which will be
        // honored by the editor.
        const snippetCompletion = new vscode.CompletionItem('Good part of the day');
        snippetCompletion.insertText = new vscode.SnippetString('Good ${1|morning,afternoon,evening|}. It is ${1}, right?');
        snippetCompletion.documentation = new vscode.MarkdownString("Inserts a snippet that lets you select the _appropriate_ part of the day for your greeting.");

        // a completion item that can be accepted by a commit character,
        // the `commitCharacters`-property is set which means that the completion will
        // be inserted and then the character will be typed.
        const commitCharacterCompletion = new vscode.CompletionItem('console');
        commitCharacterCompletion.commitCharacters = ['.'];
        commitCharacterCompletion.documentation = new vscode.MarkdownString('Press `.` to get `console.`');

        // a completion item that retriggers IntelliSense when being accepted,
        // the `command`-property is set which the editor will execute after 
        // completion has been inserted. Also, the `insertText` is set so that 
        // a space is inserted after `new`
        const commandCompletion = new vscode.CompletionItem('new');
        commandCompletion.kind = vscode.CompletionItemKind.Keyword;
        commandCompletion.insertText = 'new ';
        commandCompletion.command = { command: 'editor.action.triggerSuggest', title: 'Re-trigger completions...' };

        // return all completion items as array
        return [
            simpleCompletion,
            snippetCompletion,
            commitCharacterCompletion,
            commandCompletion
        ];
    }
});

const provider2 = vscode.languages.registerCompletionItemProvider(
    'plaintext',
    {
        provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {

            // get all text until the `position` and check if it reads `console.`
            // and if so then complete if `log`, `warn`, and `error`
            const linePrefix = document.lineAt(position).text.substr(0, position.character);
            if (!linePrefix.endsWith('console.')) {
                return undefined;
            }

            return [
                new vscode.CompletionItem('log', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('warn', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('error', vscode.CompletionItemKind.Method),
            ];
        }
    },
    '.' // triggered whenever a '.' is being typed
);

const xml_doc_create = vscode.languages.registerCompletionItemProvider(
    'plaintext',
    {
        provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {

            // get all text until the `position` and check if it reads `console.`
            // and if so then complete if `log`, `warn`, and `error`
            const linePrefix = document.lineAt(position).text.substr(0, position.character);
            if (!linePrefix.endsWith('xml_blk:')) {
                return undefined;
            }

            return [
                new vscode.CompletionItem('', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('create_excel_doc', vscode.CompletionItemKind.Method)
            ];
        }
    },
    ':' // triggered whenever a '.' is being typed
);
const xml_doc_slash = vscode.languages.registerCompletionItemProvider(
    'plaintext',
    {
        provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {

            // get all text until the `position` and check if it reads `console.`
            // and if so then complete if `log`, `warn`, and `error`
            const linePrefix = document.lineAt(position).text.substr(0, position.character);
            if (!linePrefix.endsWith('xml_blk/')) {
                return undefined;
            }

            return [
                new vscode.CompletionItem('', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('fname', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('fmode', vscode.CompletionItemKind.Method)
            ];
        }
    },
    '/' // triggered whenever a '/' is being typed
);

const compose_slash = vscode.languages.registerCompletionItemProvider(
    'plaintext',
    {
        provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {

            // get all text until the `position` and check if it reads `console.`
            // and if so then complete if `log`, `warn`, and `error`
            const linePrefix = document.lineAt(position).text.substr(0, position.character);
            if (!linePrefix.endsWith('compose/')) {
                return undefined;
            }

            return [
                new vscode.CompletionItem('', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('only', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('only/deep', vscode.CompletionItemKind.Method)
            ];
        }
    },
    '/' // triggered whenever a '/' is being typed
);
const layout_slash = vscode.languages.registerCompletionItemProvider(
    'plaintext',
    {
        provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {

            // get all text until the `position` and check if it reads `console.`
            // and if so then complete if `log`, `warn`, and `error`
            const linePrefix = document.lineAt(position).text.substr(0, position.character);
            if (!linePrefix.endsWith('layout/')) {
                return undefined;
            }
            return [
                new vscode.CompletionItem('', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('offset', vscode.CompletionItemKind.Method),
               
            ];
        }
    },
    '/' // triggered whenever a '/' is being typed
);

const objectdoubledot = vscode.languages.registerCompletionItemProvider(
    'plaintext',
    {
        provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {

            // get all text until the `position` and check if it reads `console.`
            // and if so then complete if `log`, `warn`, and `error`
            const linePrefix = document.lineAt(position).text.substr(0, position.character);
            if (!linePrefix.endsWith(':')) {
                return undefined;
            }



            return [
                new vscode.CompletionItem('', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem(' copy', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem(' true', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem(' false', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem(' getlist', vscode.CompletionItemKind.Method),
                new vscode.CompletionItem('', vscode.CompletionItemKind.Method),
               
            ];
        }
    },
    '/' // triggered whenever a '/' is being typed
);

const commandCompletions = vscode.languages.registerCompletionItemProvider(
    'plaintext',
    {
        provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {

            // get all text until the `position` and check if it reads `console.`
            // and if so then complete if `log`, `warn`, and `error`
            const centerCompletion = new vscode.CompletionItem('center');
            centerCompletion.kind = vscode.CompletionItemKind.Keyword;
            centerCompletion.insertText = 'center ';

            const notCompletion = new vscode.CompletionItem('not');
            notCompletion.kind = vscode.CompletionItemKind.Keyword;
            notCompletion.insertText = 'not ';

            const rejoinCompletion = new vscode.CompletionItem('rejoin');
            rejoinCompletion.kind = vscode.CompletionItemKind.Keyword;
            rejoinCompletion.insertText = 'rejoin ';
            
            const exitCompletion = new vscode.CompletionItem('exit');
            exitCompletion.kind = vscode.CompletionItemKind.Keyword;
            exitCompletion.insertText = 'exit ';

            const noneCompletion = new vscode.CompletionItem('none');
            noneCompletion.kind = vscode.CompletionItemKind.Keyword;
            noneCompletion.insertText = 'none ';
            
            const findCompletion = new vscode.CompletionItem('find');
            findCompletion.kind = vscode.CompletionItemKind.Keyword;
            findCompletion.insertText = 'find ';

            const trueCompletion = new vscode.CompletionItem('true');
            trueCompletion.kind = vscode.CompletionItemKind.Keyword;
            trueCompletion.insertText = 'true ';

            const falseCompletion = new vscode.CompletionItem('false');
            falseCompletion.kind = vscode.CompletionItemKind.Keyword;
            falseCompletion.insertText = 'false ';

            const parseCompletion = new vscode.CompletionItem('parse');
            parseCompletion.kind = vscode.CompletionItemKind.Keyword;
            parseCompletion.insertText = 'parse ';

            const appendCompletion = new vscode.CompletionItem('parse');
            appendCompletion.kind = vscode.CompletionItemKind.Keyword;
            appendCompletion.insertText = 'append ';

            const emptyCompletion = new vscode.CompletionItem('empty?');
            emptyCompletion.kind = vscode.CompletionItemKind.Keyword;
            emptyCompletion.insertText = 'empty? ';
            
            const sizeCompletion = new vscode.CompletionItem('size?');
            sizeCompletion.kind = vscode.CompletionItemKind.Keyword;
            sizeCompletion.insertText = 'size? ';
            
            const size2Completion = new vscode.CompletionItem('size');
            size2Completion.kind = vscode.CompletionItemKind.Keyword;
            size2Completion.insertText = 'size ';

            const buttonCompletion = new vscode.CompletionItem('button');
            buttonCompletion.kind = vscode.CompletionItemKind.Keyword;
            buttonCompletion.insertText = 'button ';

            const composeCompletion = new vscode.CompletionItem('compose');
            composeCompletion.kind = vscode.CompletionItemKind.Keyword;
            composeCompletion.insertText = 'compose ';

            const makeCompletion = new vscode.CompletionItem('make');
            makeCompletion.kind = vscode.CompletionItemKind.Keyword;
            makeCompletion.insertText = 'make ';

            const tailCompletion = new vscode.CompletionItem('tail?');
            tailCompletion.kind = vscode.CompletionItemKind.Keyword;
            tailCompletion.insertText = 'tail? ';

            const headCompletion = new vscode.CompletionItem('head');
            headCompletion.kind = vscode.CompletionItemKind.Keyword;
            headCompletion.insertText = 'head ';

            


            
           
            
            return [
                centerCompletion,
                notCompletion,
                rejoinCompletion,
                exitCompletion,
                noneCompletion,
                findCompletion,
                trueCompletion,
                falseCompletion,
                parseCompletion,
                appendCompletion,
                emptyCompletion,
                sizeCompletion,
                size2Completion,
                buttonCompletion,
                composeCompletion,
                makeCompletion,
                tailCompletion,
                headCompletion



                
               
            ];
        }
    }
    // triggered whenever a '/' is being typed
);






context.subscriptions.push(xml_doc_create, xml_doc_slash,layout_slash,objectdoubledot,commandCompletions,commandCompletions);
}
