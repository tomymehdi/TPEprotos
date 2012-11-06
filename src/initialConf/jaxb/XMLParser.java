package initialConf.jaxb;

import initialConf.AppConf;
import initialConf.jaxb.XMLAppConf.AuxRestrictedIPs.Subnet;
import initialConf.jaxb.XMLAppConf.AuxAdministrator;
import initialConf.jaxb.XMLAppConf.AuxUser;
import initialConf.jaxb.XMLAppConf.AuxUser.AuxDelRestrictions.AuxFrom;
import initialConf.jaxb.XMLAppConf.AuxUser.TimesToLogin;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import pop3.RestrictedIPs;
import pop3.User;
import pop3.restriction.DateRestriction;
import pop3.restriction.FromRestriction;
import pop3.restriction.Restriction;
import pop3.restriction.SizeRestriction;

public class XMLParser implements AppConf {

	XMLAppConf conf;

	public boolean loadSettings() {
		try {
			JAXBContext context = JAXBContext.newInstance(XMLAppConf.class);
			conf = (XMLAppConf) context.createUnmarshaller().unmarshal(
					new File("conf.xml"));
			if(getDefaultServer() == null){
				System.out.println("Error: falta indicar server default en el xml.");
				return false;
			}
		} catch (JAXBException e) {
			System.out
					.println("Error al parsear el XML en la configuracion inicial.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public Map<String, User> getUsersMap() {
		Map<String, User> ans = new HashMap<String, User>();
		if (conf.users == null) {
			return ans;
		}
		for (AuxUser u : conf.users) {
			if (u.username != null) {
				User user = new User(u.username);
				if (u.countLoginsPerDay != null) {
					user.setMaxConnections(u.countLoginsPerDay);
				}
				if (u.server != null) {
					user.setServer(u.server);
				}
				if (u.timesToLogin != null) {
					for(TimesToLogin t : u.timesToLogin){
						user.addUnallowedConnectionInterval(t.hourFrom, t.hourTo);
					}
				}
				if (u.delRestrictions != null) {
					if (u.delRestrictions.from != null) {
						for (AuxFrom from : u.delRestrictions.from) {
							if (from.email != null) {
								// Si existe el mail, banea ese mail con
								// exactMatch.
								// Si no banea el username sin exactMatch
								user.addRestriction(new FromRestriction(
										from.email, true));
							} else if (from.username != null) {
								user.addRestriction(new FromRestriction(
										from.username, false));
							}
						}
					}
					if (u.delRestrictions.ctype != null) {
						// TODO Para cuando se agregue la type restriction
					}
					if (u.delRestrictions.size != null) {
						user.addRestriction(new SizeRestriction(
								u.delRestrictions.size));
					}
					if (u.delRestrictions.cantDays != null) {
						user.addRestriction(new DateRestriction(
								u.delRestrictions.cantDays));
					}
				}
				ans.put(u.username, user);
			}
		}
		return ans;
	}

	@Override
	public String getTransformerPath() {
		return conf.transformation;
	}

	@Override
	public List<Restriction> getGlobalRestrictions() {
		List<Restriction> ans = new LinkedList<Restriction>();
		if (conf.globalDelRest != null) {
			if (conf.globalDelRest.from != null) {
				for (AuxFrom from : conf.globalDelRest.from) {
					if (from.email != null) {
						// Si existe el mail, banea ese mail con
						// exactMatch.
						// Si no banea el username sin exactMatch
						ans.add(new FromRestriction(
								from.email, true));
					} else if (from.username != null) {
						ans.add(new FromRestriction(
								from.username, false));
					}
				}
			}
			if (conf.globalDelRest.ctype != null) {
				// TODO Para cuando se agregue la type restriction
			}
			if (conf.globalDelRest.size != null) {
				ans.add(new SizeRestriction(
						conf.globalDelRest.size));
			}
			if (conf.globalDelRest.cantDays != null) {
				ans.add(new DateRestriction(
						conf.globalDelRest.cantDays));
			}
		}
		return ans;
	}

	@Override
	public String getDefaultServer() {
		return conf.defaultServer;
	}

	@Override
	public void loadRestrictedIPs() {
		if (conf.restrictedIPs == null) {
			return;
		}
		if (conf.restrictedIPs.hostname != null) {
			for (String hostname : conf.restrictedIPs.hostname) {
				RestrictedIPs.banIPByName(hostname);
			}
		}
		if (conf.restrictedIPs.ips != null) {
			for (String ip : conf.restrictedIPs.ips) {
				RestrictedIPs.banIPByAddress(ip);
			}
		}
		if (conf.restrictedIPs.subnetCidr != null) {
			for (String sn : conf.restrictedIPs.subnetCidr) {
				RestrictedIPs.banIPBySubnet(sn);
			}
		}
		if (conf.restrictedIPs.subnet != null) {
			for (Subnet sn : conf.restrictedIPs.subnet) {
				if (sn.address != null && sn.submask != null) {
					RestrictedIPs.banIPBySubnet(sn.address, sn.submask);
				}
			}
		}
	}

	@Override
	public Map<String, String> getAdministratorsMap() {
		Map<String, String> ans = new HashMap<String, String>();
		if(conf.administrators == null){
			return ans;
		}
		for(AuxAdministrator a : conf.administrators){
			if( a.username != null && a.password != null ){
				ans.put(a.username, a.password);
			}
		}
		return ans;
	}

}
