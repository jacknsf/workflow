package workflow.op;

import workflow.Flow;

public class Rank extends baseOP {

    public Rank(Flow flow) {
        super(flow);

        m_Type= OP_TYPE.Rank;
    }

    public Rank(Flow flow, String code, String name) {
        super(flow,code, name);

        m_Type= OP_TYPE.Rank;
    }
}
