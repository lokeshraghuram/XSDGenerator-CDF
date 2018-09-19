import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ReadXSD {

    public static void main(String[] args) {

        try {

            //File file = new File("C:\\Lokesh\\Coop\\Canonical Models\\Warehouse Tasks\\WMTOID01.xsd");
            File file = new File("C:\\Lokesh\\Coop\\Docs\\XSD\\SALESORDER_CREATEFROMDAT204.xsd");

            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            Document doc = dBuilder.parse(file);

            if (doc.hasChildNodes()) {

                printNote(doc.getChildNodes());

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private static void printNote(NodeList nodeList) {

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);

            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                //element name
                if(tempNode.getNodeName().equalsIgnoreCase("xsd:element")){
                    System.out.println("");
                    System.out.print(tempNode.getAttributes().getNamedItem("name").getNodeValue());
                }

                // description
                if(tempNode.getNodeName().equalsIgnoreCase("xsd:documentation")){
                    System.out.print("\t"+tempNode.getTextContent());
                }

                // restriction
                if(tempNode.getNodeName().equalsIgnoreCase("xsd:restriction")){
                    System.out.print("\t"+tempNode.getAttributes().getNamedItem("base").getNodeValue().split("[:]")[1]);
                }

                // max length
                if(tempNode.getNodeName().equalsIgnoreCase("xsd:maxLength")){
                    System.out.print("\t"+tempNode.getAttributes().getNamedItem("value").getNodeValue());
                }

                if (tempNode.hasChildNodes()) {

                    // loop again if has child nodes
                    printNote(tempNode.getChildNodes());

                }

            }

        }

    }

}