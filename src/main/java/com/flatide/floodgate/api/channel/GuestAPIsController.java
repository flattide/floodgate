package com.flatide.floodgate.api.channel;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.*;

import com.flatide.floodgate.agent.ChannelAgent;
import com.flatide.floodgate.agent.Context;
import com.flatide.floodgate.agent.flow.stream.FGInputStream;
import com.flatide.floodgate.agent.flow.stream.FGSharableInputCurrent;
import com.flatide.floodgate.agent.flow.stream.carrier.container.JSONContainer;
//import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


@CrossOrigin(origins =  "*")
@RestController
@RequestMapping(path="/api")
public class GuestAPIsController {
    static List<byte[]> holder = new ArrayList<>();
    @PostMapping(path="/{api}")
    public @ResponseBody Map postFlow(@RequestBody Map<String, Object> data,
                                      @PathVariable String api,
                                      @RequestParam Map<String, String> params) throws Exception {

        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        long fullused = mbean.getHeapMemoryUsage().getUsed();
        long fullmax = mbean.getHeapMemoryUsage().getMax();
        long fullfree = fullmax - fullused;

        System.out.println(String.format("max: %d, used: %d, free %d", fullmax, fullused, fullfree));

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        Enumeration<String> headers = request.getHeaderNames();
        while( headers.hasMoreElements() ) {
            String header = headers.nextElement();
            //if( "client_ip".equals(header) || "content-type".equals(header) || "content-length".equals(header) || "transfer-encoding".equals(header)) {
                System.out.println(header + " : " + request.getHeader(header));
            //}
        }
        System.out.println();

        //holder.add(new byte[500000]);
        //return new HashMap<String, String>();
        //return "{item}";
        FGInputStream current = new FGSharableInputCurrent( new JSONContainer(data, "HEADER", "ITEMS"));

        ChannelAgent agent = new ChannelAgent();
        agent.addContext(Context.CONTEXT_KEY.REQUEST_PARAMS, params);
        agent.addContext(Context.CONTEXT_KEY.REQUEST_BODY, data);
        return agent.process(current, api);
    }

    @GetMapping(path="/{api}")
    public @ResponseBody Map get(@RequestBody Map<String, Object> data,
                                      @PathVariable String api,
                                      @RequestParam Map<String, String> params) throws Exception {

        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        long fullused = mbean.getHeapMemoryUsage().getUsed();
        long fullmax = mbean.getHeapMemoryUsage().getMax();
        long fullfree = fullmax - fullused;

        System.out.println(String.format("max: %d, used: %d, free %d", fullmax, fullused, fullfree));

        //holder.add(new byte[500000]);
        Map result = new HashMap<String, byte[]>();
        //System.out.println(data.toJSONString());
        //result.put("result", new byte[600000]);
        return result;
        //return new HashMap<String, byte[]>();
    }

    /*@PostMapping(path="/{owner}/{api}/{resource}")
    public @ResponseBody Map postFlowStream(@RequestBody String data, @PathVariable String owner, @PathVariable String api, @PathVariable String resource, @RequestParam Map<String, String> params) throws Exception {
        BufferedInputStream inputStream = new BufferedInputStream( new ByteArrayInputStream(data.getBytes()));
        InputCurrent stream = new InputCurrent( new JSONPipe(inputStream, data.length(), "", "ITEMS", 1 ));

        ChannelAgent agent = new ChannelAgent();
        agent.addContext("REQUEST_PARAMS", params);
        return agent.process(stream, resource);
    }*/

    @GetMapping(path="/{guest}/{api}/{resource}")
    public @ResponseBody Map getFlow(@RequestBody Map<String, Object> data, @PathVariable String guest, @PathVariable String api, @PathVariable String resource, @RequestParam Map<String, String> params) throws Exception {
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        long fullused = mbean.getHeapMemoryUsage().getUsed();
        long fullmax = mbean.getHeapMemoryUsage().getMax();
        long fullfree = fullmax - fullused;

        System.out.println(String.format("max: %d, used: %d, free %d", fullmax, fullused, fullfree));

        FGInputStream stream = new FGSharableInputCurrent( new JSONContainer(data, "HEADER", "ITEMS"));

        ChannelAgent agent = new ChannelAgent();
        agent.addContext(Context.CONTEXT_KEY.REQUEST_PARAMS, params);
        agent.addContext(Context.CONTEXT_KEY.REQUEST_BODY, data);
        return agent.process(stream, resource);
    }
}