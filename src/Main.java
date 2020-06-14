import com.insa.xml.XMLParser;
import com.insa.xml.XMLNode;

/**
 *
 * @author doria
 */
public class Main {
    
    public static void testAttributes(XMLNode root)
    {
        System.out.println(root.getElementsByAttribute("titi"));
    }
    
    public static void testRemoveChild(XMLNode root, String tag)
    {
        root.removeChildrenByTag(tag);
    }
    
    public static void testRemoveSiblings(XMLNode root)
    {
        root.removeSiblingsByTag("price");
    }
    
    public static void main(String[] args)
    {
        XMLParser parser = XMLParser.getInstance();
        String xml = "<!-- <?xml-stylesheet type=\"text/xsl\" href=\"question1.xsl\"?> -->\n" +
"<Company>\n" +
"un texte directe dans Company\n" +
"	<Employee id=\"01\" salary=\"10000\">\n" +
"		<FirstName>Tim</FirstName>\n" +
"		<LastName>Browne</LastName>\n" +
"		<Position>CEO</Position>\n" +
"		<Email>TBrowne@Infoteria.com</Email>\n" +
"		<Category>Officer</Category>\n" +
"		<Age>25</Age>\n" +
"	</Employee>\n" +
"</Company>";

        XMLNode node = parser.parse(xml);
        System.out.println(node.getElementsByAttribute("salary"));
    }
}
