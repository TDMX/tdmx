package org.tdmx.lib.zone.domain;


public class ZoneFacade {

	public static Zone createZone( String zoneApex ) throws Exception  {
		Zone z = new Zone();
		z.setZoneApex(zoneApex);

		return z;
	}

}
