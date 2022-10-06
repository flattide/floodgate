package com.flatide.floodgate.agent.flow.module;

import com.flatide.floodgate.ConfigurationManager;
import com.flatide.floodgate.agent.template.DocumentTemplate;
import com.flatide.floodgate.agent.connector.ConnectorTag;
import com.flatide.floodgate.agent.connector.ConnectorBase;
import com.flatide.floodgate.agent.flow.Flow;
import com.flatide.floodgate.agent.flow.FlowContext;
import com.flatide.floodgate.agent.flow.FlowTag;
import com.flatide.floodgate.agent.connector.ConnectorFactory;
import com.flatide.floodgate.agent.flow.stream.Payload;
import com.flatide.floodgate.agent.flow.rule.MappingRule;
import com.flatide.floodgate.agent.meta.MetaManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Module {
    // NOTE spring boot의 logback을 사용하려면 LogFactory를 사용해야 하나, 이 경우 log4j 1.x와 충돌함(SoapUI가 사용)
    private static final Logger logger = LogManager.getLogger(Module.class);

    private final String id;
    private final Map<String, Object> sequences;
    private final Flow flow;


    public Module(Flow flow, String id, Map<String, Object> data) {
        this.flow = flow;
        this.id = id;
        this.sequences = data;
    }

    /*
        FlowContext의 input에 대한 처리
    */
    public void processBefore(FlowContext context) {
        @SuppressWarnings("unchecked")
        Map<String, Object> before = (Map<String, Object>) this.sequences.get(FlowTag.BEFORE.name());
        if (before != null) {
        }
    }

    public void process(FlowContext context) throws Exception {
        if (this.sequences != null) {
            ConnectorBase connector;

            Object connectRef = this.sequences.get(FlowTag.CONNECT.name());
            Map<String, Object> connInfo;
            if (connectRef == null) {
                logger.info(context.getId() + " : No connect info for module " + this.id);
            } else {
                if (connectRef instanceof String) {
                    String table = (String) ConfigurationManager.shared().getConfig().get("meta.source.tableForConnection");
                    connInfo = MetaManager.shared().read(table, (String) connectRef);
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> temp = (Map<String, Object>) connectRef;
                    connInfo = temp;
                }

                connector = ConnectorFactory.shared().getConnector(connInfo);

                String templateName = (String) this.sequences.get(FlowTag.TEMPLATE.name());
                if( templateName == null ) {
                    String method = (String) connInfo.get(ConnectorTag.CONNECTOR.name());
                    if ("FILE".equals(method)) {
                        templateName = "JSON";
                    } else {
                        templateName = method;
                    }
                }

                DocumentTemplate documentTemplate = DocumentTemplate.get(templateName, false);
                connector.setDocumentTemplate(documentTemplate);

                context.add("CONNECT_INFO", connInfo);
                context.add("SEQUENCE", this.sequences);
                try {
                    connector.connect(context);

                    String action = (String) this.sequences.get(FlowTag.ACTION.name());

                    if (FlowTag.valueOf(action) == FlowTag.CREATE) {
                        String ruleName = (String) this.sequences.get(FlowTag.RULE.name());
                        MappingRule rule = context.getRules().get(ruleName);

                        String dbType = (String) connInfo.get(ConnectorTag.JDBCTag.DBTYPE.toString());
                        rule.setFunctionProcessor(connector.getFunctionProcessor(dbType));

                        Payload payload = context.getCurrent().subscribe();
                        //Payload payload = context.getPayload();

                        //logger.info(data.toString());
                        connector.create(payload, rule);

                        context.getCurrent().unsubscribe(payload);
                            /*case DELETE:
                            con.delete();
                            break;*/
                    }

                    String next = (String) this.sequences.get(FlowTag.CALL.name());
                    context.setNext(next);
                } catch (Exception e) { e.printStackTrace();
                } finally {
                    connector.close();
                }
            }
        }
    }

    /*
        FlowContext의 output에 대한 처리
     */
    public void processAfter(FlowContext context) {
        @SuppressWarnings("unchecked")
        Map<String, Object> after = (Map<String, Object>) this.sequences.get(FlowTag.AFTER.name());
    }


}