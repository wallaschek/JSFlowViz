package de.upb.cn.jsflowviz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.beaconcontroller.topology.ITopology;
import net.beaconcontroller.topology.LinkTuple;
import net.beaconcontroller.web.IWebManageable;
import net.beaconcontroller.web.view.BeaconViewResolver;
import net.beaconcontroller.web.view.Tab;
import net.beaconcontroller.web.view.layout.Layout;
import net.beaconcontroller.web.view.layout.TwoColumnLayout;
import net.beaconcontroller.web.view.section.TableSection;

import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jsflowviz")
public class JSFlowVizWebManageable implements IWebManageable {
    protected static Logger log = LoggerFactory.getLogger(JSFlowVizWebManageable.class);
    protected List<Tab> tabs;
    protected ITopology topology;

    public JSFlowVizWebManageable() {
        tabs = new ArrayList<Tab>();
        tabs.add(new Tab("Overview", "/wm/jsflowviz/overview.do"));
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
        
        return BeaconViewResolver.SIMPLE_VIEW;
    }


    /**
     * @param topology the topology to set
     */
    @Autowired
    public void setTopology(ITopology topology) {
        this.topology = topology;
    }
}
