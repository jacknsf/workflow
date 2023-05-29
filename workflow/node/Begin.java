package workflow.node;

import workflow.Flow;

public class Begin extends baseNode {

    public Begin(Flow flow)
    {
        super(flow);

        this.m_code ="BEGIN";
        this.m_name ="开始";
        this.m_Type = NODE_TYPE.BEGIN;

        //开始节点自动通过
        this.m_State = NODE_STATE.PASSED;

    }


}
