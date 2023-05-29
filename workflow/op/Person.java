package workflow.op;

import workflow.Flow;

public class Person extends baseOP {
    public Person(Flow flow)
    {
        super(flow);

        m_Type= OP_TYPE.Person;
    }

    public Person(Flow flow, String code, String name)
    {
        super(flow,code,name);

        m_Type= OP_TYPE.Person;
    }
}
