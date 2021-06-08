"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.COBOLSettings = exports.formatOnReturn = exports.outlineFlag = void 0;
const externalfeatures_1 = require("./externalfeatures");
var outlineFlag;
(function (outlineFlag) {
    outlineFlag["On"] = "on";
    outlineFlag["Off"] = "off";
    outlineFlag["Partial"] = "partial";
    outlineFlag["Skeleton"] = "skeleton";
})(outlineFlag = exports.outlineFlag || (exports.outlineFlag = {}));
var formatOnReturn;
(function (formatOnReturn) {
    formatOnReturn["Off"] = "off";
    formatOnReturn["CamelCase"] = "camelcase";
    formatOnReturn["UpperCase"] = "uppercase";
})(formatOnReturn = exports.formatOnReturn || (exports.formatOnReturn = {}));
class COBOLSettings {
    constructor() {
        this.init_required = true;
        this.enable_tabstop = true;
        this.pre_parse_line_limit = 25;
        this.ignorecolumn_b_onwards = false;
        this.copybooks_nested = false;
        this.fuzzy_variable_search = false;
        this.fileformat_strategy = "normal";
        this.outline = outlineFlag.Off;
        this.copybookdirs = [];
        this.copybookexts = [];
        this.program_extensions = [];
        this.invalid_copybookdirs = [];
        this.tabstops = [];
        this.linter = false;
        this.line_comment = false;
        this.enable_data_provider = true;
        this.disable_unc_copybooks_directories = false;
        this.intellisense_include_unchanged = true;
        this.intellisense_include_camelcase = false;
        this.intellisense_include_uppercase = false;
        this.intellisense_include_lowercase = false;
        this.intellisense_item_limit = 30;
        this.process_metadata_cache_on_start = false;
        this.cache_metadata = externalfeatures_1.CacheDirectoryStrategy.Off;
        this.cache_metadata_inactivity_timeout = 5000;
        this.cache_metadata_max_directory_scan_depth = 32;
        this.cache_metadata_show_progress_messages = false;
        this.parse_copybooks_for_references = false;
        this.workspacefolders_order = [];
        this.linter_mark_as_information = false;
        this.linter_unused_paragraphs_or_sections = true;
        this.linter_house_standards = true;
        this.linter_house_standards_rules = [];
        this.linter_ignore_missing_copybook = false;
        this.ignore_unsafe_extensions = false;
        this.scan_comments_for_hints = false;
        this.scan_comment_copybook_token = "source-dependency";
        this.coboldoc_workspace_folder = "coboldoc";
        this.process_metadata_cache_on_file_save = false;
        this.cache_metadata_user_directory = "";
        this.editor_maxTokenizationLineLength = 20000;
        this.sourceview = false;
        this.sourceview_include_jcl_files = true;
        this.sourceview_include_hlasm_files = true;
        this.sourceview_include_pli_files = true;
        this.sourceview_include_doc_files = true;
        this.sourceview_include_script_files = true;
        this.sourceview_include_object_files = true;
        this.linter_ignore_section_before_entry = true;
        this.format_on_return = formatOnReturn.Off;
        this.format_constants_to_uppercase = true;
        this.metadata_symbols = [];
        this.metadata_entrypoints = [];
        this.metadata_types = [];
        this.preprocessor_extensions = [];
        this.metadata_files = [];
        this.metadata_knowncopybooks = [];
        this.maintain_metadata_cache = true;
        this.maintain_metadata_cache_single_folder = false;
        this.maintain_metadata_recursive_search = false;
        this.enable_semantic_token_provider = false;
        this.enable_text_replacement = false;
    }
}
exports.COBOLSettings = COBOLSettings;
//# sourceMappingURL=iconfiguration.js.map