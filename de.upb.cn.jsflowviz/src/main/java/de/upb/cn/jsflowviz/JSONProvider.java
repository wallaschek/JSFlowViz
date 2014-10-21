package de.upb.cn.jsflowviz;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.topology.ITopology;
import net.beaconcontroller.topology.ITopologyAware;
import net.beaconcontroller.topology.LinkTuple;
import net.beaconcontroller.web.view.BeaconViewResolver;
import net.beaconcontroller.web.view.layout.OneColumnLayout;
import net.beaconcontroller.web.view.section.JspSection;
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