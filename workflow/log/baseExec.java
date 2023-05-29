package workflow.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import workflow.Flow;
import workflow.node.NODE_STATE;
import workflow.node.NODE_TYPE;
import workflow.node.baseNode;
import workflow.op.OP_TYPE;
import workflow.op.Person;
import workflow.op.Rank;
import workflow.op.baseOP;

import java.text.SimpleDateFormat;
import java.util.Date;

public class baseExec {
    private static final Logger log = LoggerFactory.getLogger(baseExec.class);
    //操作节点
    public baseNode m_node;
    //操作员
    public baseOP m_op;
    //操作时间
    public String m_dt;
    //操作描述
    public String m_desc;

    //操作结果
    public NODE_STATE m_state;

    Flow _flow;

    public baseExec(Flow flow)
    {
        _flow=flow;
    }
    public baseExec(Flow flow, String op, String node,NODE_STATE st,String desc)
    {
        _flow=flow;

        m_op=flow.findOP(op);
        m_node =flow.findNode(node);
        m_state=st;
        m_desc =desc;


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        m_dt=formatter.format(date);

    }

    public static baseExec createBaseExec(Flow flow, String sNodeState)
    {
        NODE_STATE ost= NODE_STATE.valueOf(sNodeState);
        baseExec execNew=null;
        switch (ost)
        {

            //通过
            case PASSED:
                execNew=new Passed(flow);
                break;
            //驳回
            case REJECTED:
                execNew=new Rejected(flow);
                break;

        }
        return execNew;

    }

    //从XML定义加载节点信息
    public boolean loadXml(Flow flow, Element eleCurrent)
    {
        try
        {
            if(null==eleCurrent)
                return false;

            if(eleCurrent.getTagName().equals("exec")==false)
                return false;

            m_dt=eleCurrent.getAttribute("dt");
            m_desc=eleCurrent.getAttribute("desc");
            m_state= NODE_STATE.valueOf(eleCurrent.getAttribute("st"));
            String opcode=eleCurrent.getAttribute("op");
            String nodecode=eleCurrent.getAttribute("node");

            if(null==opcode || null==nodecode || opcode.isEmpty() || nodecode.isEmpty())
                return false;

            m_node=flow.findNode(nodecode);
            m_op=flow.findOP(opcode);

            if(null==m_node)
                return  false;

            m_node.setState(m_state);

            return true;

        }
        catch (Exception ex)
        {
            log.error(ex.toString());
            return false;
        }

    }

    //产生节点的XML定义，入参为XML文档对象 和 父节点 ，返回值 true成功， false失败
    public boolean getXml(Document docXml, Element eleParent)
    {
        try
        {
            Element eleCurrent= docXml.createElement("exec");
            eleCurrent.setAttribute("dt",m_dt);
            eleCurrent.setAttribute("node",m_node.m_code);
            eleCurrent.setAttribute("op",m_op.m_code);
            eleCurrent.setAttribute("st",m_state.toString());
            eleCurrent.setAttribute("desc", m_desc);

            eleParent.appendChild(eleCurrent);

            return true;

        }
        catch (Exception ex)
        {

            log.error(ex.toString());
            return false;
        }

    }




}
