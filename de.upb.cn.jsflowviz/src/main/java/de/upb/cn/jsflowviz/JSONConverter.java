package de.upb.cn.jsflowviz;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.upb.cn.jsflowviz.JSONProvider.SwFlowTuple;

import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.topology.LinkTuple;

public class JSONConverter {
	public static String switches(Collection<IOFSwitch> switches) {
		String str="[";
		for ( IOFSwitch sw : switches) {
			str += String.format("%d,",sw.getId());
		}
		if(str.length()>1)
			str=str.substring(0, str.length()-1);
		str += "]";
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
		if(str.length()>1)
			str=str.substring(0, str.length()-1);
		str += "]";
		return str;
	}
	private static LinkTuple swap(LinkTuple p) {
		return new LinkTuple(p.getDst(), p.getSrc());
	}
	
	public static String flows(Set<LinkedList<SwFlowTuple>> flows) {
		//[{ "name": "hans","path": [1,2,3]},{ "name": "horst","path": [2,3,4]}]
		String str = "[";
		for(LinkedList<SwFlowTuple> flow : flows) {
			str += String.format("{ \"hash\": %d, \"match\": \"%s\", \"bytes\": %d, \"path\": [",flow.get(0).getFlow().getMatch().hashCode(),flow.get(0).getFlow().getMatch().toString(),flow.get(0).getFlow().getByteCount());
			for(SwFlowTuple tup : flow) {
				str+=String.format("%d,",tup.getSw().getId());
			}
			str=str.substring(0, str.length()-1)+" ] },";
		}
		if(str.length()>1)
			str=str.substring(0, str.length()-1);
		str += "]";
		return str;
	}
}