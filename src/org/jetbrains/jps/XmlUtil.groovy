package org.jetbrains.jps

import groovy.xml.FactorySupport
import org.xml.sax.SAXNotRecognizedException
import org.xml.sax.SAXNotSupportedException

import javax.xml.XMLConstants
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

class XmlUtil {


    public static XmlParser createNonValidatingXmlParser() {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory();
        factory.setNamespaceAware(false)
        factory.setValidating(false)
        setQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true)
        setQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", false)
        setQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
        setQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        return new XmlParser(factory.newSAXParser().getXMLReader())
    }

    private static void setQuietly(SAXParserFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        }
        catch (ParserConfigurationException ignored) {
        }
        catch (SAXNotRecognizedException ignored) {
        }
        catch (SAXNotSupportedException ignored) {
        }
    }
}
