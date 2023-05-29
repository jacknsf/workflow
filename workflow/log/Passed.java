package workflow.log;

import workflow.Flow;
import workflow.node.NODE_STATE;

public class Passed extends baseExec {
    public Passed(Flow flow)
    {
        super(flow);
    }
    public Passed(Flow flow, String op, String node, NODE_STATE st,String desc)
    {
        super(flow,op,node,st,desc);

    }
}
