package workflow.node;

import workflow.Flow;

public class Script extends baseNode{
    public Script(Flow flow)
    {
        super(flow);

    }
    public Script(Flow flow,String code, String name)
    {
        super(flow);

        this.m_code =code;
        this.m_name =name;
        this.m_Type = NODE_TYPE.SCRIPT;

    }

}
