package workflow.log;

import workflow.Flow;
import workflow.node.NODE_STATE;

public class Rejected extends  baseExec{
    public Rejected(Flow flow)
    {
        super(flow);
    }
    public Rejected(Flow flow, String op, String node, NODE_STATE st,String desc)
    {
        super(flow,op,node,st,desc);
    }

}
