package initialConf.jaxb;

import initialConf.jaxb.XMLAppConf.AuxUser.AuxDelRestrictions;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="proxyServerConfiguration")
public class XMLAppConf{

	@XmlElement(name = "defaultServer")
	String defaultServer;

	@XmlElement
	String transformation;
	
	@XmlElementWrapper
	@XmlElement(name = "administrator")
	List<AuxAdministrator> administrators;

	@XmlElement(name = "restrictedIps")
	AuxRestrictedIPs restrictedIPs;
	
	@XmlElement(name = "user")
	List<AuxUser> users;
	
	@XmlElement(name = "globalDelRestrictions")
	AuxDelRestrictions globalDelRest;
	
	public static class AuxUser{
		@XmlElement
		String username;
		@XmlElement
		String server;
		@XmlElement
		List<TimesToLogin> timesToLogin;
		@XmlElement
		Integer countLoginsPerDay;
		@XmlElement(name = "restrictions")
		AuxDelRestrictions delRestrictions;
		
		public static class TimesToLogin{
			@XmlElement
			Integer hourFrom;
			@XmlElement
			Integer hourTo;
		}
		
		public static class AuxDelRestrictions{
			@XmlElement
			Integer cantDays;
			@XmlElement(name = "from")
			List<AuxFrom> from;
			@XmlElement(name = "type")
			List<String> ctype;
			@XmlElement
			Integer size;
			
			public static class AuxFrom{
				@XmlElement
				String email;
				@XmlElement
				String username;
			}
		}
	}

	public static class AuxAdministrator{
		@XmlElement
		String username;

		@XmlElement
		String password;
	}
	
	public static class AuxRestrictedIPs{
		@XmlElement(name = "ip")
		List<String> ips;
		@XmlElement(name = "hostname")
		List<String> hostname;
		@XmlElement(name = "subnet_cidr")
		List<String> subnetCidr;
		@XmlElement(name = "subnet")
		List<Subnet> subnet;
		
		public static class Subnet{
			@XmlElement
			String address;
			@XmlElement
			String submask;
		}
	}
}
