package org.cougaar.tools.techspecs.parser;

import org.cougaar.util.ConfigFinder;
import org.cougaar.tools.techspecs.RelationshipSpec;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * User: wpeng
 * Date: May 5, 2004
 * Time: 3:54:20 PM
 */
public class TechSpecReader
{

    public void parseTechSpec( ConfigFinder finder, String techSpecFileName ) {
        try
        {
            //Document doc = finder.parseXMLConfigFile( techSpecFileName ) ;
            Document doc = null ;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;
            DocumentBuilder parser = factory.newDocumentBuilder() ;
            File f = new File( techSpecFileName ) ;
            doc = parser.parse( f ) ;

            HashMap relationships = parseRelationships( doc ) ;

            HashMap roles = parseRoles( doc ) ;
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private HashMap parseRoles(Document doc)
    {
        HashMap roleSpecs = new HashMap() ;
        NodeList nodeList = doc.getElementsByTagName( Constants.TAG_ROLESPEC ) ;

        for (int i=0;i<nodeList.getLength();i++) {
            Element roleSpecNode = (Element) nodeList.item(i) ;
        }

        return roleSpecs ;
    }

    private HashMap parseRelationships(Document doc)
    {
        HashMap relationshipSpecs = new HashMap() ;

        NodeList nodeList = doc.getElementsByTagName( Constants.TAG_RELATIONSHIP ) ;

        for (int i=0;i<nodeList.getLength();i++) {
            Node relationshipNode = nodeList.item(i) ;
            System.out.println("Node=" + relationshipNode.getNodeName() );
            NamedNodeMap relationshipAttributes = relationshipNode.getAttributes() ;

            Node relationshipNameNode = relationshipAttributes.getNamedItem( Constants.TAG_RELATIONSHIP_ID ) ;

            if ( relationshipNameNode == null ) {
                throw new RuntimeException( Constants.TAG_RELATIONSHIP_ID + " attribute not found ." ) ;
            }

            String relationshipName = relationshipNameNode.getNodeValue() ;
            RelationshipSpec spec = new RelationshipSpec( relationshipName ) ;

            NodeList relationshipNodeList = relationshipNode.getChildNodes() ;

            for (int k=0;k<relationshipNodeList.getLength();k++) {
                Node n2 = relationshipNodeList.item(k) ;
                if ( !( n2 instanceof Element)) {
                    continue;
                }

                Element roleNode = (Element) n2 ;

                if ( !roleNode.getNodeName().equals( Constants.TAG_ROLEINFO ) ) {
                    throw new RuntimeException( "Unexpected node name " + roleNode.getNodeName() + ",expected" + Constants.TAG_ROLEINFO ) ;
                }
                NamedNodeMap roleInfoMap =roleNode.getAttributes() ;
                Attr roleInfoNode = (Attr) roleInfoMap.getNamedItem( Constants.TAG_ROLE_ID ) ;
                String roleId = roleInfoNode.getNodeValue() ;

                Attr cardinalityNode = (Attr) roleInfoMap.getNamedItem( Constants.TAG_CARDINALITY ) ;
                int cardinality ;
                if ( cardinalityNode.getNodeValue().equalsIgnoreCase( Constants.STRING_N ) ) {
                    cardinality = RelationshipSpec.CARDINALITY_N ;
                }
                else {
                    cardinality = Integer.parseInt( cardinalityNode.getNodeValue() ) ;
                }
                RelationshipSpec.RoleInfo info = new RelationshipSpec.RoleInfo( cardinality, roleId, false ) ;
                spec.addRoleInfo( info );
            }
            relationshipSpecs.put( spec.getRelationshipName(), spec ) ;
        }
        return relationshipSpecs ;
    }

    public static final void main( String[] args ) {
        //ConfigFinder cf = new ConfigFinder( "C:\\Software\\DISGroup\\Projects\\Ultralog\\Software\\CPESociety\\configs" ) ;
        //org.apache.log4j.Logger l = org.apache.log4j.LogManager.getLogger( org.cougaar.util.ConfigFinder.class ) ;

        // Document doc = cf.parseXMLConfigFile( "cpespecs.xml" ) ;
        TechSpecReader reader = new TechSpecReader() ;
        reader.parseTechSpec( null, "C:\\Software\\DISGroup\\Projects\\Ultralog\\Software\\CPESociety\\configs\\cpespecs.xml");
    }
}