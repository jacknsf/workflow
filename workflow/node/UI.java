package workflow.node;

import workflow.Flow;

public class UI extends baseNode{
    public UI(Flow flow)
    {
        super(flow);

    }
    public UI(Flow flow,String code, String name)
    {
        super(flow);

        this.m_code =code;
        this.m_name =name;
        this.m_Type = NODE_TYPE.UI;

    }
}
