package com.app.xmlNormalize;

import javax.xml.stream.*;
import java.io.*;

class XmlNormalize {
    private final String lineSeparator = System.lineSeparator();
    private String filePath;
    private String newFilePath;
    private File file;
    private File newFile;
    private InputStream fileInputStream;
    private OutputStream fileOutputStream;
    private ActionType action;

    void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private InputStream getFileInputStream() throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IOException();
        }
        file = new File(filePath);
        return new BufferedInputStream(new FileInputStream(file));
    }

    private OutputStream getFileOutputStream() throws IOException {
        if (newFilePath == null || newFilePath.isEmpty()) {
            throw new IOException();
        }
        newFile = new File(newFilePath);
        return new BufferedOutputStream(new FileOutputStream(newFile));
    }

    private void closeStreams() throws IOException {
        fileInputStream.close();
        fileOutputStream.close();
    }

    private void writeAuthBlock(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("AuthData");
        xmlStreamWriter.writeStartElement("Login");
        xmlStreamWriter.writeCharacters("dgma-pk@mail.ru");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("Pass");
        xmlStreamWriter.writeCharacters("Fath1436");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
    }

    private void writeStartPackageData(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("PackageData");
        xmlStreamWriter.writeAttribute("xmlns", "", "xs", "http://www.w3.org/2001/XMLSchema");
    }

    private void writeEndPackageData(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeEndElement();
    }

    void normalize() throws IOException, XMLStreamException {
        fileInputStream = getFileInputStream();
        fileOutputStream = getFileOutputStream();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(fileInputStream);
        XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(fileOutputStream, "UTF-8");

        int i, event;
        String newText = "";
        boolean insertAuthData = false;
        xmlStreamWriter.writeStartDocument("UTF-8", "1.0");

        while (xmlStreamReader.hasNext()) {
            event = xmlStreamReader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (xmlStreamReader.getLocalName()) {
                        case "ApplicationID":
                            action = ActionType.SKIP;
                            break;
                        case "dataroot":
                            action = ActionType.RENAME;
                            newText = "root";
                            insertAuthData = true;
                            break;
                        case "IdentityDocumentQQ":
                            action = ActionType.RENAME;
                            newText = "IdentityDocument";
                            break;
                        case "OrphanDocumentQQ":
                            action = ActionType.RENAME;
                            newText = "OrphanDocument";
                            break;
                        case "DocumentDate":
                        case "BirthDate":
                        case "OriginalReceivedDate":
                            action = ActionType.FIX_DATE;
                            break;
                        default:
                            action = ActionType.WRITE;
                            break;
                    }
                    if (action != ActionType.SKIP) {
                        fileOutputStream.write(lineSeparator.getBytes());

                        if (action == ActionType.RENAME) {
                            xmlStreamWriter.writeStartElement(newText);
                        } else {
                            xmlStreamWriter.writeStartElement(xmlStreamReader.getLocalName());
                        }

                        for (i = 0; i < xmlStreamReader.getNamespaceCount(); i++) {
                            xmlStreamWriter.writeNamespace(
                                    xmlStreamReader.getNamespacePrefix(i),
                                    xmlStreamReader.getNamespaceURI(i)
                            );
                        }
                        for (i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            if (xmlStreamReader.getAttributeNamespace(i) == null) {
                                xmlStreamWriter.writeAttribute(
                                        xmlStreamReader.getAttributeLocalName(i),
                                        xmlStreamReader.getAttributeValue(i)
                                );
                            } else {
                                xmlStreamWriter.writeAttribute(
                                        xmlStreamReader.getAttributePrefix(i),
                                        xmlStreamReader.getAttributeNamespace(i),
                                        xmlStreamReader.getAttributeLocalName(i),
                                        xmlStreamReader.getAttributeValue(i)
                                );
                            }
                        }
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (action != ActionType.SKIP) {
                        if (action == ActionType.FIX_DATE) {
                            xmlStreamWriter.writeCharacters(xmlStreamReader.getText().replace("T00:00:00", "").trim());
                        } else {
                            xmlStreamWriter.writeCharacters(xmlStreamReader.getText().trim());
                            if (insertAuthData) {
                                insertAuthData = false;
                                writeAuthBlock(xmlStreamWriter);
                                writeStartPackageData(xmlStreamWriter);
                            }
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (action != ActionType.SKIP) {
                        xmlStreamWriter.writeEndElement();
                    } else {
                        action = ActionType.WRITE;
                    }
                    break;
                default:
                    break;
            }

        }

        writeEndPackageData(xmlStreamWriter);
        xmlStreamWriter.writeEndDocument();

        closeStreams();
    }

    void setNewFilePath(String newFilePath) {
        this.newFilePath = newFilePath;
    }

    private enum ActionType {
        WRITE, SKIP, RENAME, FIX_DATE
    }
}
