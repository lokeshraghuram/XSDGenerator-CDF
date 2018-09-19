import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ReadExcel {

    public static void main(String[] args) throws IOException, InvalidFormatException{

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;
        String folderName = "Article Master";
        String canonicalName = "ArticleMaster";
        String version = "V"+"2.0";
        String namespaceAppender = canonicalName.toLowerCase();

        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Element xsdRoot;
        xsdRoot = doc.createElement("xs:schema");
        Element rootElement;
        Element complexType;
        Element sequenceTag;
        Element parentSequenceTag;
        sequenceTag = doc.createElement("xs:sequence");
        Element fieldName;
        Element annotation;
        Element documentation;
        Element simpleType;
        Element restriction;
        Element maxLength;

        int currentLevel = 0;
        int columnNumber;

        Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\"+folderName+"\\"+canonicalName+" "+version+".xlsx"));
        //Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Assortment\\Assortment Canonical Model.xlsx"));
       //Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Article Master\\Article Master Canonical Model V1.0.xlsx"));
        //Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Warehouse Tasks Confirmation\\Warehouse Tasks Confirmation V1.0.xlsx"));
        //Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Warehouse Tasks Cancellation\\Warehouse Tasks Cancellation V1.0.xlsx"));
        //Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Warehouse Tasks\\Warehouse Tasks V1.0.xlsx"));
       //  Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Article Hierarchy\\Product Hierarchy Canonical Model.xlsx"));


        Sheet indexSheet = wb.getSheet("Index");

        DataFormatter dataFormatter = new DataFormatter();

        Iterator<Row> rowIterator = indexSheet.rowIterator();
        //Iterator<Row> indexRowsIterator = indexSheet.rowIterator();

        //skipping first 3 rows
        rowIterator.next();
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Row indexRow = rowIterator.next();

            Iterator<Cell> cellIterator = indexRow.cellIterator();

            //next() points to the first column that has data in this row
            Cell cell = cellIterator.next();

            //String segmentName = dataFormatter.formatCellValue(cell);
            String segmentName = cell.getStringCellValue();
            columnNumber = cell.getColumnIndex();
            currentLevel = cell.getColumnIndex() +1;

            if(segmentName.equalsIgnoreCase("SAP Model") || segmentName.equals(null)){
                break;
            }

            System.out.println(segmentName + "\t" + currentLevel);

            //First level - Includes creation of XSDRoot and initialization of Doc builder
            if(currentLevel == 1){

                System.out.println("First Level");
                //Add schema tag to the document
                xsdRoot.setAttribute("xmlns:xs","http://www.w3.org/2001/XMLSchema");
                xsdRoot.setAttribute("version",version.substring(1));
                xsdRoot.setAttribute("xmlns","http://www.coop.dk/integration/"+namespaceAppender);
                xsdRoot.setAttribute("targetNamespace","http://www.coop.dk/integration/"+namespaceAppender);
                xsdRoot.setAttribute("elementFormDefault","unqualified");
                xsdRoot.setAttribute("attributeFormDefault","unqualified");
                doc.appendChild(xsdRoot);

                //root element of canonical model
                rootElement = doc.createElement("xs:element");
                rootElement.setAttribute("name",segmentName);
                rootElement.setAttribute("type",segmentName+"Type");
                xsdRoot.appendChild(rootElement);
            }

            //complex type element creation
            complexType = doc.createElement("xs:complexType");
            complexType.setAttribute("name", segmentName + "Type");
            xsdRoot.appendChild(complexType);

            //sequence tag
            sequenceTag = doc.createElement("xs:sequence");
            complexType.appendChild(sequenceTag);


            //Open the sheet which contains simple fields for this complex segment
            Sheet segmentSheet = wb.getSheet(segmentName);

            System.out.println("segmentsheet:" + segmentSheet.getSheetName());
            DataFormatter dataFormatter1 = new DataFormatter();
            Iterator<Row> segmentRowIterator = segmentSheet.rowIterator();

            //skipping first 4 rows
            segmentRowIterator.next();
            segmentRowIterator.next();


           // Row segmentRow = segmentRowIterator.next();
            segmentRowIterator.next();
            segmentRowIterator.next();
            //xsdRoot.appendChild(complexType);

            //Loop on all rows and add simple fields
            while (segmentRowIterator.hasNext()) {

                Row segmentRow = segmentRowIterator.next();

                //create schema for the fields
                fieldName = doc.createElement("xs:element");
                fieldName.setAttribute("name", segmentRow.getCell(1).toString());
                fieldName.setAttribute("minOccurs", String.valueOf((int) segmentRow.getCell(5).getNumericCellValue()));
                fieldName.setAttribute("maxOccurs", String.valueOf((int) segmentRow.getCell(6).getNumericCellValue()));
                //fieldName.setAttribute("type", "xs:string");
                sequenceTag.appendChild(fieldName);

                //create annotation
                annotation = doc.createElement("xs:annotation");
                fieldName.appendChild(annotation);

                //create documentation as child of annotation
                documentation = doc.createElement("xs:documentation");
                documentation.setTextContent(segmentRow.getCell(2).toString());
                annotation.appendChild(documentation);

                //create simple type
                simpleType = doc.createElement("xs:simpleType");
                fieldName.appendChild(simpleType);

                //create restriction as child of simple type
                restriction = doc.createElement("xs:restriction");
                restriction.setAttribute("base","xs:"+segmentRow.getCell(3).toString());
                simpleType.appendChild(restriction);

                //create max length as child of restriction
                if(!segmentRow.getCell(3).toString().equalsIgnoreCase("date")) {
                    maxLength = doc.createElement("xs:maxLength");
                    maxLength.setAttribute("value", String.valueOf((int) segmentRow.getCell(4).getNumericCellValue()));
                    restriction.appendChild(maxLength);
                }
            }

            //Add references to complex types of immediate next level
            int currentRowNumber = indexRow.getRowNum();

            Iterator<Row> indexRowsIterator = indexSheet.rowIterator();
            int count = 1;

            //Skips rows until indexRowsIterator equals rowIterator
            while(count<=currentRowNumber){
                indexRowsIterator.next();
                count++;
            }

            while (indexRowsIterator.hasNext()) {
                Row indexRow2 = indexRowsIterator.next();
                Iterator<Cell> indexCellIterator = indexRow2.cellIterator();
                Cell indexCell = indexCellIterator.next();

                String segmentName2 = indexCell.getStringCellValue();

                if(segmentName2.equalsIgnoreCase("SAP Model") || segmentName2.equals(null)){
                    break;
                }

                int nextColumnNumber = indexCell.getColumnIndex();

                //Same or previous level - End of child references
                if(nextColumnNumber <= columnNumber){
                    break;
                }

                //Immediate next level - Add child references
                if(nextColumnNumber == columnNumber+1){
                    fieldName = doc.createElement("xs:element");
                    fieldName.setAttribute("name", indexRow2.getCell(nextColumnNumber).toString());
                    fieldName.setAttribute("type", segmentName2 + "Type");

                    //Open the sheet for min and max occurs
                    Sheet segmentSheet2 = wb.getSheet(segmentName2);

                    fieldName.setAttribute("minOccurs", String.valueOf((int)segmentSheet2.getRow(0).getCell(1).getNumericCellValue()));
                    if(segmentSheet2.getRow(1).getCell(1).getCellTypeEnum().toString().equalsIgnoreCase("NUMERIC")){
                        fieldName.setAttribute("maxOccurs", String.valueOf((int)segmentSheet2.getRow(1).getCell(1).getNumericCellValue()));
                    }else{
                        fieldName.setAttribute("maxOccurs", String.valueOf(segmentSheet2.getRow(1).getCell(1)));
                    }


                    sequenceTag.appendChild(fieldName);
                }
            }

        }
        try {

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
           // Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Article Master\\Article Master Canonical Model V1.0.xlsx"));
           // Workbook wb = WorkbookFactory.create(new File("C:\\Lokesh\\Coop\\Canonical Models\\Assortment\\Assortment.xsd"));
            //StreamResult result = new StreamResult(new File("C:\\Lokesh\\Coop\\Canonical Models\\Warehouse Tasks\\Canonical Schema\\WarehouseTasks.xsd"));
            StreamResult result = new StreamResult(new File("C:\\Lokesh\\Coop\\Canonical Models\\"+folderName+"\\"+canonicalName+"_"+version+".xsd"));
            //StreamResult result = new StreamResult(new File("C:\\Lokesh\\Coop\\Canonical Models\\Assortment\\Assortment.xsd"));
            //StreamResult result = new StreamResult(new File("C:\\Lokesh\\Coop\\Canonical Models\\Article Master\\ArticleMaster.xsd"));
            //StreamResult result = new StreamResult(new File("C:\\Lokesh\\Coop\\Canonical Models\\Warehouse Tasks Cancellation\\WarehouseTasksCancellation.xsd"));
            //StreamResult result = new StreamResult(new File("C:\\Lokesh\\Coop\\Canonical Models\\Warehouse Tasks Confirmation\\WarehouseTasksConfirmation.xsd"));
           // StreamResult result = new StreamResult(new File("C:\\Lokesh\\Coop\\Canonical Models\\Article Hierarchy\\ArticleHierarchy.xsd"));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        System.out.println("File saved!");

    }
}
