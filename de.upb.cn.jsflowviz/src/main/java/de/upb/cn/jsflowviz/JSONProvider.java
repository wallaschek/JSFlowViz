package de.upb.cn.jsflowviz;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.topology.ITopology;
import net.beaconcontroller.topology.ITopologyAware;
import net.beaconcontroller.topology.LinkTuple;
import net.beaconcontroller.web.view.BeaconViewResolver;
import net.beaconcontroller.web.view.layout.OneColumnLayout;
import net.beaconcontroller.web.view.section.JspSection;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/jsflowviz/json")
public class JSONProvider  {

	private ITopology topology;
	protected IBeaconProvider beaconProvider;
	private Set<IOFSwitch> switches;
	private ITopologyAware topologyAware;
	private Lock lock;
	public JSONProvider() {
		this.switches= new HashSet<IOFSwitch>();
		this.lock = new ReentrantLock();
	}
	
	
	protected void startUp() {
		
	}
	
	protected void shutDown() {
		
	}
	
	
	
	@RequestMapping("/switches")
    public String switches(Map<String, Object> model) {
		Collection<IOFSwitch> switches = beaconProvider.getSwitches().values();
    	OneColumnLayout layout = new OneColumnLayout();
    	model.put("output", JSONConverter.switches(switches));
    	layout.addSection(new JspSection("raw.jsp", model), null);
        return BeaconViewResolver.SIMPLE_JSON_VIEW;
    }
	@RequestMapping("/links")
    public String links(Map<String, Object> model) {
    	OneColumnLayout layout = new OneColumnLayout();
    	model.put("output", JSONConverter.links(topology.getLinks().keySet()));
    	layout.addSection(new JspSection("raw.jsp", model), null);
        return BeaconViewResolver.SIMPLE_JSON_VIEW;
    }
	
	@RequestMapping("/flows")
    public String flows(Map<String, Object> model) {
		OneColumnLayout layout = new OneColumnLayout();
		
		
		Set<LinkedList<SwFlowTuple>> flows = poll_flows();
		
		
		
    	model.put("output", JSONConverter.flows(flows));
    	layout.addSection(new JspSection("raw.jsp", model), null);
        return BeaconViewResolver.SIMPLE_JSON_VIEW;
	}
	
	protected class SwFlowTuple  {
		IOFSwitch sw;
		OFFlowStatisticsReply flow;
		
		
		public SwFlowTuple(IOFSwitch sw, OFFlowStatisticsReply flow) {
			this.sw = sw;
			this.flow = flow;
		}
		public IOFSwitch getSw() {
			return sw;
		}
		public void setSw(IOFSwitch sw) {
			this.sw = sw;
		}
		public OFFlowStatisticsReply getFlow() {
			return flow;
		}
		public void setFlow(OFFlowStatisticsReply flow) {
			this.flow = flow;
		}
	}
	
	private Set<LinkedList<SwFlowTuple>> poll_flows() {
		Map<Long, IOFSwitch> switches = beaconProvider.getSwitches();
		Map<Long, List<OFStatistics>> flows_per_switch = new HashMap<Long, List<OFStatistics>>();
		for(Long dpid : switches.keySet()) {
			flows_per_switch.put(dpid, getSwitchFlows(dpid));
		}
		Map<OFMatch, Set<SwFlowTuple>> flows_per_match = new HashMap<OFMatch, Set<SwFlowTuple>>();
		for(Long dpid : flows_per_switch.keySet()) {
			for(OFStatistics flow : flows_per_switch.get(dpid)) {
				OFMatch match = extract_match_without_inport(flow);
				if(! flows_per_match.containsKey(match)) {
					flows_per_match.put(match, new HashSet<SwFlowTuple>());
				}
				flows_per_match.get(match).add(new SwFlowTuple(switches.get(dpid), (OFFlowStatisticsReply)flow));
			}
		}
		Set<LinkedList<SwFlowTuple>> flows = new HashSet<LinkedList<SwFlowTuple>>();
		for(OFMatch match : flows_per_match.keySet()) {
			flows.addAll(combine_flows(flows_per_match.get(match)));
		}
		return flows;
	}
	
	
	private Collection<LinkedList<SwFlowTuple>> combine_flows(Set<SwFlowTuple> set) {
		Collection<LinkedList<SwFlowTuple>> ret = new HashSet<LinkedList<SwFlowTuple>>();
		while(! set.isEmpty()) {
			SwFlowTuple tup = set.iterator().next();
			boolean flow_increased = true;
			set.remove(tup);
			LinkedList<SwFlowTuple> flow = new LinkedList<JSONProvider.SwFlowTuple>();
			flow.add(tup);
			SwFlowTuple tup_front = tup;
			SwFlowTuple tup_back = tup;
			OFActionOutput tup_front_action = extract_output_action(tup);
			OFActionOutput tup_back_action = extract_output_action(tup);
			while(flow_increased) {
				flow_increased = false;
				SwFlowTuple ntup = null;
				for(SwFlowTuple tup2 : set) {
					OFActionOutput tup2_action = extract_output_action(tup2);
					if(tup2_action != null) {
						if((tup2_action != null) && is_linked(tup2.getSw(),tup2_action.getPort(),tup_front.getSw(),tup_front.getFlow().getMatch().getInputPort())) {
							//tup2 is in front of tup_front
							flow.add(flow.indexOf(tup_front), tup2);
							flow_increased = true;
							tup_front = tup2;
							tup_front_action = tup2_action;
							ntup = tup2;
							break;
						}
						else if((tup_back_action != null) && is_linked(tup_back.getSw(),tup_back_action.getPort(),tup2.getSw(),tup2.getFlow().getMatch().getInputPort())) {
							flow.add(flow.indexOf(tup_back)+1, tup2);
							flow_increased = true;
							tup_back = tup2;
							tup_back_action = tup2_action;
							ntup = tup2;
							break;
						}
					}
				}
				if(flow_increased)
					set.remove(ntup);
			}
			ret.add(flow);
		}
		return ret;
	}


	private boolean is_linked(IOFSwitch sw, short outport, IOFSwitch sw2, short inport) {
		Map<LinkTuple,Long> links = topology.getLinks();
		LinkTuple lt = new LinkTuple(sw, outport, sw2, inport);
		if(links.containsKey(lt))
			return true;
		return false;
	}


	private OFActionOutput extract_output_action(SwFlowTuple tup2) {
		OFActionOutput tup2_action=null;
		for(OFAction tact : tup2.getFlow().getActions()) {
			if(tact instanceof OFActionOutput) {
				tup2_action = (OFActionOutput)tact;
				break;
			}
		}
		return tup2_action;
	}


	private OFMatch extract_match_without_inport(OFStatistics flow) {
		OFMatch match = ((OFFlowStatisticsReply)flow).getMatch().clone();
		match.setInputPort((short)0);
		return match;
	}

	
	/*
	 * Copy from net.beaconcontroller.core.web.CoreWebManageable.java
	 * BEGIN
	 */
	protected interface OFSRCallback {
        OFStatisticsRequest getRequest();
    }
	
	protected List<OFStatistics> getSwitchStats(OFSRCallback f, Long switchId, String statsType) {
        IOFSwitch sw = beaconProvider.getSwitches().get(switchId);
        Future<List<OFStatistics>> future;
        List<OFStatistics> values = null;
        if (sw != null) {
            try {
                future = sw.getStatistics(f.getRequest());
                values = future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                //log.error("Failure retrieving " + statsType, e);
            }
        }
        return values;
    }

    protected class makeFlowStatsRequest implements OFSRCallback {
        public OFStatisticsRequest getRequest() {
            OFStatisticsRequest req = new OFStatisticsRequest();
            OFFlowStatisticsRequest fsr = new OFFlowStatisticsRequest();
            OFMatch match = new OFMatch();
            match.setWildcards(0xffffffff);
            fsr.setMatch(match);
            fsr.setOutPort(OFPort.OFPP_NONE.getValue());
            fsr.setTableId((byte) 0xff);
            req.setStatisticType(OFStatisticsType.FLOW);
            req.setStatistics(fsr);
            req.setLengthU(req.getLengthU() + fsr.getLength());
            return req;
        };
    }
    
    protected List<OFStatistics> getSwitchFlows(Long switchId) {
        return getSwitchStats(new makeFlowStatsRequest(), switchId, "flows");
    }
    
    /*
	 * Copy from net.beaconcontroller.core.web.CoreWebManageable.java
	 * END
	 */
	
	


	/**
     * @param topology the topology to set
     */
    @Autowired
    public void setTopology(ITopology topology) {
        this.topology = topology;
    }
    
    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }
    
}