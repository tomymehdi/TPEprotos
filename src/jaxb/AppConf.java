package jaxb;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="proxyServerConfiguration")
public class AppConf{

	@XmlElement
	String defaultServer;

	@XmlElement
	Integer monitoringPort;

	@XmlElement(name = "administrator")
	List<Administrator> administrators;
//	
//	<restrictedIps>
//		<ip>124.125.123.2</ip>
//		<ip>124.1.123.2</ip>
//		<submask>125.21.0.0/16</submask>
//	</restrictedIps>
//	
//	<userRestrictions>
//		<userRestriction>
//			<username>farolfo</username>
//			<server>gmail.pop3.com.ar</server>
//			<restrictions>
//				<timesToLogin><!-- Horarios en los que el usuario se puede registrar, sino esta se puede registrar en cualqueir momento -->
//					<from>16:00</from><to>21:00</to>
//				</timesToLogin>
//				<countLoginsPerDay><!-- Cantidad de veces que se puede loguear el usuario por dia, sino esta no tiene limite de veces -->
//					4
//				</countLoginsPerDay>
//				<deletion>
//					<date><day></day><month></month><year></year></date>
//					<from>username</from>
//					<ctype>/audio</ctype>
//					<size>400</size>		
//				</deletion>
//			</restrictions>
//		</userRestriction>
//	</userRestrictions>
//	
//	<transformations>
//		<path>/bin/echo</path>
//		<path>/bin/prog1</path>
//	</transformations>
	
	public class Administrator{
		@XmlElement
		String username;
		
		@XmlElement
		String password;
	}
	
}
