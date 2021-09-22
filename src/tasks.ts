
import vscode from 'vscode';

export default class ProjectTaskManager {
    static PROVIDER_TYPE = 'Rebol';
    static TASKS_VIEW_ID = 'rebol-ide.projectTasks';
    static AUTO_REFRESH_DELAY = 500; // 0.5 sec
}