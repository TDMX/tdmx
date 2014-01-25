package org.tdmx.core.system.dns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

public class SystemDnsResolver {

	private String hostname;

	public SystemDnsResolver(String hostname) {
		this.hostname = hostname;
	}

	public static List<String> getSearchHostnames() {
		List<String> hosts = new ArrayList<>();

		String[] list = ResolverConfig.getCurrentConfig().servers();
		if (list != null) {
			for (String h : list) {
				hosts.add(h);
			}
		}
		return Collections.unmodifiableList(hosts);
	}

	public void getAuthNameServers() throws Exception {
		Name n = Name.fromString("plus.google.com");

		int numLabels = n.labels();
		StringBuffer b = new StringBuffer();
		for (int max = 0; max < numLabels; max++) {
			// TODO bottom to top lookup of SOA record.
			String l = n.getLabelString(max);
			b.append(l);
			b.append(".");
		}
		String dn = b.toString();

		Resolver r = new SimpleResolver("8.8.8.8");
		Lookup l = new Lookup(dn, Type.SOA);
		l.setResolver(r);
		l.setCache(null);
		l.setSearchPath((Name[]) null);
		Record[] records = l.run();

	}

	private Resolver getResolver() {
		Resolver dr = Lookup.getDefaultResolver();
		dr.setTCP(true);
		return dr;
	}
}
