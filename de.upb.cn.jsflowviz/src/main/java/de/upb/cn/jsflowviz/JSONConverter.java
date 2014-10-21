package de.upb.cn.jsflowviz;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.topology.LinkTuple;

public class JSONConverter {
	public static String switches(Collection<IOFSwitch> switches) {
		String str="[";
		for ( IOFSwitch sw : switches) {
			str += String.format("%d,",sw.getId());
		}
		str=str.substring(0, str.length()-1)+"]";
		return str;
	}
	
	public static String links(Collection<LinkTuple> links) {
		String str="[";
		Set<LinkTuple> done= new HashSet<LinkTuple>();
		for ( LinkTuple li : links) {
			if(! done.contains(swap(li))) {
				str += String.format("{ \"source\": %d,\"target\": %d, \"srcport\": %d,\"dstport\": %d },",li.getSrc().getSw().getId(), li.getDst().getSw().getId(),li.getSrc().getPort(),li.getDst().getPort());
				done.add(li);
			}
		}
		str=str.substring(0, str.length()-1)+"]";
		return str;
	}
	private static LinkTuple swap(LinkTuple p) {
		return new LinkTuple(p.getDst(), p.getSrc());
	}
}