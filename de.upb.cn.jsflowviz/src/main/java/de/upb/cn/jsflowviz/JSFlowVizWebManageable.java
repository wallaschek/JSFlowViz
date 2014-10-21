package de.upb.cn.jsflowviz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.topology.ITopology;
import net.beaconcontroller.topology.LinkTuple;
import net.beaconcontroller.web.IWebManageable;
import net.beaconcontroller.web.view.BeaconViewResolver;
import net.beaconcontroller.web.view.Tab;
import net.beaconcontroller.web.view.layout.Layout;
import net.beaconcontroller.web.view.layout.OneColumnLayout;
import net.beaconcontroller.web.view.layout.TwoColumnLayout;
import net.beaconcontroller.web.view.section.JspSection;
import net.beaconcontroller.web.view.section.TableSection;

import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jsflowviz")
public class JSFlowVizWebManageable implements IWebManageable, IOFSwitchListener {
    protected static Logger log = LoggerFactory.getLogger(JSFlowVizWebManageable.class);
    protected List<Tab> tabs;
    protected ITopology topology;
    protected List<IOFSwitch> switches;

    public JSFlowVizWebManageable() {
        tabs = new ArrayList<Tab>();
        tabs.add(new Tab("Overview", "/wm/jsflowviz/overview.do"));
        switches = new LinkedList<IOFSwitch>();
    }

    @Override
    public String getName() {
        return "JSFlowViz";
    }

    @Override
    public String getDescription() {
        return "View the discovered topology in fancy javascript.";
    }

    @Override
    public List<Tab> getTabs() {
        return tabs;
    }

    @RequestMapping("/overview")
    public String overview(Map<String, Object> model) {
    	OneColumnLayout layout = new OneColumnLayout();
        model.put("title", "Current Topology state");
        model.put("layout", layout);
        layout.addSection(new JspSection("graph.jsp", model), null);
        return BeaconViewResolver.SIMPLE_VIEW;
    }
    
    @RequestMapping("/switches")
    public String switches(Map<String, Object> model) {
    	OneColumnLayout layout = new OneColumnLayout();
    	model.put("output", switches.toString());
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

	@Override
	public void addedSwitch(IOFSwitch sw) {
		this.switches.add(sw);
		
	}

	@Override
	public void removedSwitch(IOFSwitch sw) {
		this.switches.remove(sw);
		
	}
}
