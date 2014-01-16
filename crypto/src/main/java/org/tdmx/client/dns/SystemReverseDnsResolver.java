package org.tdmx.client.dns;

import java.io.IOException;
import java.net.UnknownHostException;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

public class SystemReverseDnsResolver {

	private String hostIp;

	public SystemReverseDnsResolver(String hostIp) {
		this.hostIp = hostIp;
	}

	public String reverseDns() {
		Resolver res = getResolver();

		Name name;
		try {
			name = ReverseMap.fromAddress(hostIp);
		} catch (UnknownHostException e) {
			return hostIp;
		}
		int type = Type.PTR;
		int dclass = DClass.IN;
		Record rec = Record.newRecord(name, type, dclass);
		Message query = Message.newQuery(rec);
		Message response;
		try {
			response = res.send(query);
		} catch (IOException e) {
			return hostIp;
		}

		Record[] answers = response.getSectionArray(Section.ANSWER);
		if (answers.length == 0)
			return hostIp;
		else
			return answers[0].rdataToString();
	}

	private Resolver getResolver() {
		Resolver dr = Lookup.getDefaultResolver();
		dr.setTCP(true);
		return dr;
	}
}
