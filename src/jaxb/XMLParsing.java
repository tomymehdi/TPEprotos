package jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class XMLParsing {

	static AppConf conf;
	public static void loadSettings(){
		try {
		    // setup object mapper using the AppConfig class
		    JAXBContext context = JAXBContext.newInstance(AppConf.class);
		    // parse the XML and return an instance of the AppConfig class
		    conf = (AppConf) context.createUnmarshaller().unmarshal(new InputS("conf.xml"));
		  } catch(JAXBException e) {
		    // if things went wrong...
		    System.out.println("error parsing xml: ");
		    e.printStackTrace();
		    // force quit
		    System.exit(1);
		  }
		
	}
	
	
		
}
