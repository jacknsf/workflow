package workflow.node;

import workflow.Flow;

public class End extends baseNode{

    public End(Flow flow)
    {
        super(flow);

        this.m_code ="END";
        this.m_name ="结束";
        this.m_Type = NODE_TYPE.END;


    }

}
