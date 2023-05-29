package workflow.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import workflow.Flow;

import java.util.ArrayList;

public class baseNode {
    private static final Logger log = LoggerFactory.getLogger(baseNode.class);

    public baseNode(Flow flow)
    {
        _flow=flow;
    }
    Flow _flow=null;
    //节点编码
    public String m_code="BASE_NODE";
    //节点名称
    public String m_name ="基础节点";
    //节点类型
    protected NODE_TYPE m_Type;
    //下一节点
    protected baseNode m_nextNode=null;
    //当前节点层级号，内部使用，当前用于打印节点信息时 格式化缩进效果用
    protected  int m_iLevel=0;

    //节点执行通过状态，待处理， 通过, 驳回，默认为 待处理
    protected NODE_STATE m_State = NODE_STATE.PENDING;

    //节点是否执行通过, true 通过,false 驳回, 派生节点需重载
    public NODE_STATE getState()
    {
        return m_State;
    }
    public void setState(NODE_STATE state)
    {
        m_State=state;
    }

    public void setLevel(int i)
    {
        m_iLevel=i;
    }
    public NODE_STATE exec_Passed()
    {
        m_State = NODE_STATE.PASSED;
        return m_State;

    }
    public NODE_STATE exec_Rejected()
    {
        m_State = NODE_STATE.REJECTED;
        return m_State;

    }
    public NODE_TYPE getType()
    {
        return m_Type;
    }


    //根据字符串的节点类型，创建并返回新节点
    public static baseNode createBaseNode(Flow flow,String sNodeType)
    {
        NODE_TYPE nt= NODE_TYPE.valueOf(sNodeType);
        baseNode nodeNew=null;
        switch (nt)
        {
            //启始节点
            case BEGIN:
                nodeNew=new Begin(flow);
                break;
            //终末节点
            case END:
                nodeNew=new End(flow);
                break;
            //组合节点
            case GROUP:
                nodeNew=new Group(flow);
                break;
            //UI节点
            case UI:
                nodeNew=new UI(flow);
                break;
            //脚本节点
            case SCRIPT:
                nodeNew=new Script(flow);
                break;

        }

        return nodeNew;

    }

    //从XML定义加载节点信息
    public boolean loadXml(Flow flow, Element eleCurrent, int ilevel)
    {
        try
        {
            if(null==eleCurrent)
                return false;

            if(eleCurrent.getTagName().equals("node")==false)
                return false;

            m_code=eleCurrent.getAttribute("code");
            m_name=eleCurrent.getAttribute("name");
            m_Type= NODE_TYPE.valueOf(eleCurrent.getAttribute("type"));

            m_iLevel=ilevel;

            flow._indexNode(this);

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
            Element eleCurrent= docXml.createElement("node");
            eleCurrent.setAttribute("code",m_code);
            eleCurrent.setAttribute("name",m_name);
            eleCurrent.setAttribute("type",m_Type.toString());

            eleParent.appendChild(eleCurrent);

            return true;

        }
        catch (Exception ex)
        {

            log.error(ex.toString());
            return false;
        }

    }
    //向当前节点增加下一平级节点，返回增加后的节点
    public baseNode setNextNode(baseNode nextNode)
    {
        this.m_nextNode =nextNode;
        _flow._indexNode(nextNode);
        return nextNode;
    }

    //当前是否等待处理，返回等待处理节点编码数组
    public ArrayList<String> waitForProcessing()
    {
        if(NODE_STATE.PENDING== m_State)
        {
            ArrayList<String> ar=new ArrayList<>();
            ar.add(m_code);
            return ar;

        }
        return null;


    }

    //打印当前节点信息
    public void showInfo()
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<m_iLevel;i++)
            sb.append("---");

        sb.append("【");
        sb.append(m_Type);
        sb.append("】");
        sb.append(" 节点名称:【");
        sb.append(m_name);
        sb.append("】");
        sb.append(" 执行结果:【");
        sb.append(m_State);
        sb.append("】");

        String s=sb.toString();
        System.out.println(s);


    }

    public baseNode nextNode()
    {
        return m_nextNode;
    }


}
