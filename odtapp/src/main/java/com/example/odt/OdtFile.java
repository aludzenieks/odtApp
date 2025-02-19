package com.example.odt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.example.util.FileUtil;

public class OdtFile extends File {
    private static final Logger LOGGER = Logger.getLogger(OdtFile.class.getName());

    private static final List<String> XML_FILES_TO_PROCESS = List.of("content.xml", "styles.xml");
    private static final String TEXT_DESCRIPTION_ATTRIBUTE = "text:description";
    private static final String TEXT_DESCRIPTION_ATTRIBUTE_VALUE_IMPORT = "import";
    private static final String TEXT_INPUT_ELEMENT = "text:text-input";

    public OdtFile(String path) {
        super(path);
    }

    /**
     * Gets import blocks from the ODT file.
     *
     * @return A list of import blocks.
     * @throws IOException                  if an I/O error occurs.
     * @throws ParserConfigurationException if a parser configuration error occurs.
     * @throws SAXException                 if a SAX error occurs.
     */
    public Optional<List<String>> getImportBlocks() throws IOException, ParserConfigurationException, SAXException {
        // List to hold the extracted import blocks
        List<String> importBlocks = new ArrayList<>();

        // Retrieve the XML content as a list of strings
        Optional<List<String>> xmlContentList = getXmlContent();

        // If the XML content is present, process each XML string
        if (xmlContentList.isPresent()) {
            for (String xmlContent : xmlContentList.get()) {
                // Extract import blocks from the XML string and add to the list
                importBlocks.addAll(extractImportBlocks(xmlContent));
            }
        }

        // Return an Optional containing the list of distinct import blocks, or an empty
        // Optional if the list is empty
        return importBlocks.isEmpty() ? Optional.empty()
                : Optional.of(importBlocks.stream().distinct().collect(Collectors.toList()));
    }

    public boolean containsImportBlock(String searchImportBlock)
            throws IOException, ParserConfigurationException, SAXException {
        // Check if the import blocks contain the specified block
        return this.getImportBlocks()
                .map(blocks -> blocks.contains(searchImportBlock))
                .orElse(false); // Return false if the list of import blocks is empty
    }

    /**
     * Gets xml content from the ODT file.
     *
     * @return A list of xml strings.
     * @throws IOException if an I/O error occurs.
     */
    private Optional<List<String>> getXmlContent() throws IOException {
        // List to hold the XML content extracted from the ODT (zip) file
        List<String> xmlContent = new ArrayList<>();

        // Open the ODT (zip) file using a try-with-resources statement to ensure it is
        // closed automatically
        try (ZipFile zipFile = new ZipFile(this)) {

            // Iterate through the list of XML files to process
            for (String xmlFile : XML_FILES_TO_PROCESS) {

                // Get the zip entry for the current XML file
                ZipEntry xmlEntry = zipFile.getEntry(xmlFile);

                // If the entry exists, read its content and add it to the xmlContent list
                if (xmlEntry != null) {
                    xmlContent.add(new String(zipFile.getInputStream(xmlEntry).readAllBytes()));
                }
            }
        } catch (ZipException e) {
            // Handle the case where the file is not a valid ODT (zip) file
            throw new IOException(String.format(
                    "Reading error. It might not be a valid ODT file. Error message: %s", e.getMessage()), e);
        } catch (IOException e) {
            // Handle general I/O errors
            throw new IOException(String.format(
                    "Reading error. Error message: %s", e.getMessage()), e);
        }

        // Return an Optional containing the list of XML content, or an empty Optional
        // if the list is empty
        return xmlContent.isEmpty() ? Optional.empty() : Optional.of(xmlContent);
    }

    /**
     * Gets import blocks from the xml string.
     *
     * @return A list of import blocks.
     * @param xmlContent The xml string.
     * @throws IOException                  if an I/O error occurs.
     * @throws ParserConfigurationException if a parser configuration error occurs.
     * @throws SAXException                 if a SAX error occurs.
     */
    private List<String> extractImportBlocks(String xmlContent)
            throws ParserConfigurationException, SAXException, IOException {
        // List to hold the import blocks extracted from the XML content
        List<String> importBlocks = new ArrayList<>();

        // Create a DocumentBuilderFactory and DocumentBuilder to parse the XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the XML content into a Document object
        Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

        // Get all elements with the tag name "text:text-input"
        NodeList nodeList = doc.getElementsByTagName(TEXT_INPUT_ELEMENT);

        // Iterate through the NodeList
        for (int i = 0; i < nodeList.getLength(); i++) {

            // Get the current element
            Element element = (Element) nodeList.item(i);

            // Check if the "text:description" attribute equals "import"
            if (TEXT_DESCRIPTION_ATTRIBUTE_VALUE_IMPORT.equals(element.getAttribute(TEXT_DESCRIPTION_ATTRIBUTE))) {
                // Add the text content of the element to the importBlocks list
                importBlocks.add(element.getTextContent());
            }
        }

        // Return the list of import blocks
        return importBlocks;
    }

    /**
     * Replaces import blocks in the ODT file.
     *
     * @param blockToReplace The block to replace.
     * @param newBlock       The new block.
     * @throws IOException                  if an I/O error occurs.
     * @throws ProviderNotFoundException    if a provider supporting the URI scheme
     *                                      is not installed.
     * @throws ParserConfigurationException if a parser configuration error occurs.
     * @throws SAXException                 if a SAX error occurs.
     * @throws TransformerException         if an unrecoverable error occurs during
     *                                      the course of the transformation..
     * @throws SecurityException            In the case of the default provider, and
     *                                      a security manager is installed, throws
     *                                      a security exception.
     */
    public void replaceImportBlocks(String blockToReplace, String newBlock)
            throws ProviderNotFoundException, SecurityException, IOException, Exception {

        // Check if the file is readable, if not throw an AccessDeniedException
        if (!FileUtil.canRead(this)) {
            throw new AccessDeniedException(String.format("File is not readable.", this.getName()));
        }
        // Create a map to hold the environment variables for the file system
        Map<String, String> env = new HashMap<>();
        // Set the 'create' environment variable to 'true'
        env.put("create", "true");
        boolean replacedInFile = false;

        // Convert the current file to a Path object
        Path path = this.toPath();
        // Create a URI for the zip file system
        URI uri = new URI("jar:" + path.toUri());

        // Open a new file system for the zip file and process the XML files
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {

            // Iterate through the list of XML files to process
            for (String xmlFile : XML_FILES_TO_PROCESS) {

                // Get the path for the current XML file within the zip file system
                Path xmlPath = fs.getPath(xmlFile);

                // Replace the import blocks in the XML file if found
                if (replaceImportBlocksInXmlFile(xmlPath, blockToReplace, newBlock)) {
                    replacedInFile = true;
                }
            }
        }
        // Log the result of the replacement operation
        if (replacedInFile) {
            LOGGER.log(Level.INFO, "Replaced in file: {0}", this.getPath().toString());
        } else {
            LOGGER.log(Level.INFO, "No blocks to replace in file: {0}", this.getPath().toString());
        }
    }

    /**
     * Replaces blocks in xml files within the specified directory.
     *
     * @param xmlPath        The path for xml.
     * @param blockToReplace The block to replace.
     * @param newBlock       The new block.
     * @throws IOException                  if an I/O error occurs.
     * @throws ParserConfigurationException if a parser configuration error occurs.
     * @throws SAXException                 if a SAX error occurs.
     * @throws TransformerException         if an unrecoverable error occurs during
     *                                      the course of the transformation..
     * @throws SecurityException            In the case of the default provider, the
     *                                      SecurityManager.checkRead(String) is
     *                                      invoked to check read access to the
     *                                      file.
     */
    private boolean replaceImportBlocksInXmlFile(Path xmlPath, String blockToReplace, String newBlock)
            throws IOException, ParserConfigurationException, SAXException, TransformerException, SecurityException {
        // Check if the file exists at the given path
        if (Files.exists(xmlPath)) {
            // Create a DocumentBuilderFactory and DocumentBuilder to parse the XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file into a Document object
            Document doc = builder.parse(Files.newInputStream(xmlPath));

            // Get all elements with the tag name "text:text-input"
            NodeList nodeList = doc.getElementsByTagName(TEXT_INPUT_ELEMENT);
            boolean found = false;

            // Iterate through the NodeList
            for (int i = 0; i < nodeList.getLength(); i++) {
                // Get the current element
                Element element = (Element) nodeList.item(i);

                // Check if the "text:description" attribute equals "import" and the text
                // content matches the block to replace
                if (TEXT_DESCRIPTION_ATTRIBUTE_VALUE_IMPORT.equals(element.getAttribute(TEXT_DESCRIPTION_ATTRIBUTE))
                        && element.getTextContent().equals(blockToReplace)) {
                    // Set the text content of the element to the new block
                    element.setTextContent(newBlock);
                    found = true;
                }
            }

            // If the block to replace was found and replaced
            if (found) {
                // Create a TransformerFactory and Transformer to write the updated XML content
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                // Write the updated XML content back to the file
                try (OutputStream os = Files.newOutputStream(xmlPath, StandardOpenOption.TRUNCATE_EXISTING)) {
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(os);
                    transformer.transform(source, result);
                    os.flush();
                }

                // Suggest garbage collection to clean up any unused memory
                System.gc();
                return true;
            }
        }

        // Return false if the file doesn't exist or the block to replace wasn't found
        return false;
    }

}
