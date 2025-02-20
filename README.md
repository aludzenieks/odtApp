
1. json action
    This action creates a JSON file containing data about imoport blocks of ODT files.
    Command:
    java -jar odtapp-0.1.jar "json" "templatesDirectory" "outputFilePath"

    Arguments:
    templatesDirectory: The directory containing the templates.
    outputFilePath: The path where the output JSON file will be created.

    Example:
    java -jar odtapp-0.1.jar "json" "D:\Templates" "D:\OutputDirectory\output.json"
	java -jar odtapp-0.1.jar "json" "D:\Templates\template_bb02.odt" "D:\OutputDirectory\output.json"

2. replace action
    This action performs a import block replacement in the templates.

    Command:
    java -jar odtapp-0.1.jar "replace" "templatesDirectory" "importBlockToReplace" "newImportBlock"

    Arguments:
    templatesDirectory: The directory containing the templates.
    importBlockToReplace: The import block to be replaced.
    newImportBlock: The new import block.

    Example:
    java -jar odtapp-0.1.jar "replace" "D:\Templates" "[import block_1.odt]" "[import block_1_new.odt]"
	java -jar odtapp-0.1.jar "replace" "D:\Templates\template_bb02.odt" "[import block_1.odt]" "[import block_1_new.odt]"

Run tests:
mvn test

Compile:
mvn compile

Package:
mvn package