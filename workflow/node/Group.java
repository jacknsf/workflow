package workflow.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import workflow.Flow;

import java.util.ArrayList;

//组合节点
public class Group extends baseNode {
    private static final Logger log = LoggerFactory.getLogger(Group.class);

    public Group(Flow flow)
    {
        super(flow);

    }
    public Group(Flow flow, String code, String name, REL_IN_GROUP rel, baseNode[] childs)
    {
        super(flow);

        this.m_code =code;
        this.m_name =name;
        this.m_Type = NODE_TYPE.GROUP;
        m_rel=rel;


        m_nodes.clear();
        //将给定的节点数组增加为子节点，并为子节点设定 层级号， 为组节点的层级号+1
        for(baseNode node:childs)
            addChild(node);

    }

    //向组节点内增加节点，返回增加后的节点
    public baseNode addChild(baseNode node)
    {
        //子节点的 层级号， 为组节点的层级号+1
        node.setLevel(m_iLevel+1);
        m_nodes.add(node);

        _flow._indexNode(node);
        return node;
    }

    //当前是否等待处理，返回等待处理节点编码数组
    @Override
    public ArrayList<String> waitForProcessing() {

        //首先计算组节点状态
        getState();

        if(m_State != NODE_STATE.PENDING)
        {
            //如果计算后，组节点状态不为等待处理，则无需返回等待处理节点编码
            return null;
        }

        //否则返回等待处理节点编码
        ArrayList<String> ar=new ArrayList<>();
        for(baseNode node_in_group:m_nodes)
        {
            ArrayList<String> ar2=node_in_group.waitForProcessing();
            if(ar2!=null)
                 ar.addAll(ar2);

        }

        return ar;



    }


    @Override
    public void setLevel(int i) {

        m_iLevel=i;

        for(baseNode node:m_nodes)
        {
            node.setLevel(i+1);

        }
    }

    //组合节点内子节点
    private ArrayList<baseNode> m_nodes=new ArrayList<baseNode>();

    //子节点间关系 ,默认为OR
    private REL_IN_GROUP m_rel= REL_IN_GROUP.OR;

    //GROUOP节点，除从XML定义加载自身节点信息， 还需负责创建子节点
    @Override
    public boolean loadXml(Flow flow, Element eleCurrent, int ilevel) {

        m_nodes.clear();

        try
        {
            if(null==eleCurrent)
                return false;

            if(eleCurrent.getTagName().equals("node")==false)
                return false;

            m_code=eleCurrent.getAttribute("code");
            m_name=eleCurrent.getAttribute("name");
            m_Type= NODE_TYPE.valueOf(eleCurrent.getAttribute("type"));
            m_rel= REL_IN_GROUP.valueOf(eleCurrent.getAttribute("rel"));

            m_iLevel=ilevel;

            flow._indexNode(this);


            baseNode nodePerv=null;
            if(eleCurrent.hasChildNodes()==true)
            {
                NodeList nList=eleCurrent.getChildNodes();
                for(int i=0;i<nList.getLength();i++)
                {
                    Node nNode=nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        baseNode newNode=baseNode.createBaseNode(_flow,eElement.getAttribute("type"));
                        if(null==newNode)
                            return false;

                        if(newNode.loadXml(flow,eElement,m_iLevel+1)==false)
                            return false;

                        m_nodes.add(newNode);

                        if(null==nodePerv)
                        {
                            nodePerv=newNode;
                        }
                        else
                        {
                            nodePerv.setNextNode(newNode);
                            nodePerv=newNode;
                        }

                    }

                }
            }

            return true;

        }
        catch (Exception ex)
        {
            log.error(ex.toString());
            return false;
        }

    }

    //GROUOP节点，除产生自身的XML外，需要负责调用子节点的方法， 产生子节点的XML
    @Override
    public boolean getXml(Document docXml, Element eleParent) {
        try
        {
            Element eleCurrent= docXml.createElement("node");
            eleCurrent.setAttribute("code",m_code);
            eleCurrent.setAttribute("name",m_name);
            eleCurrent.setAttribute("type",m_Type.toString());
            eleCurrent.setAttribute("rel",m_rel.toString());

            eleParent.appendChild(eleCurrent);


            for(baseNode node:m_nodes)
            {
               if( node.getXml(docXml,eleCurrent)==false)
                   return false;

            }


            return true;

        }
        catch (Exception ex)
        {

            log.error(ex.toString());
            return false;
        }
    }

    //节点是否执行通过, true 通过,false 驳回
    @Override
    public NODE_STATE getState() {
        //当组间关系为OR时， 任一组内节点执行通过，则认为通过。任一组内节点执行驳回，则认为驳回，当既有驳回又有通过时，作驳回处理
        //当组间关系为AND时，所有组内节点执行通过，则认为通过。任一组内节点执行驳回，则认为驳回

        if(REL_IN_GROUP.OR==m_rel)
        {
            int irejectedCount=0;
            int ipassedCount=0;

            for(baseNode node:m_nodes)
            {
                if(node.getState()== NODE_STATE.REJECTED)
                    irejectedCount++;
                else if(node.getState()== NODE_STATE.PASSED)
                    ipassedCount++;
            }

            if(irejectedCount>0) {
                m_State = NODE_STATE.REJECTED;
                return m_State;
            }

            if(ipassedCount>0) {
                m_State = NODE_STATE.PASSED;
                return m_State;
            }

            m_State = NODE_STATE.PENDING;
            return m_State;

        }
        else if(REL_IN_GROUP.AND==m_rel)
        {

            int irejectedCount=0;
            int ipassedCount=0;
            int ipendingCouNT=0;

            for(baseNode node:m_nodes)
            {
                if(node.getState()== NODE_STATE.REJECTED)
                    irejectedCount++;
                else if(node.getState()== NODE_STATE.PASSED)
                    ipassedCount++;
                else if(node.getState()== NODE_STATE.PENDING)
                    ipendingCouNT++;
            }

            if(irejectedCount>0) {
                m_State = NODE_STATE.REJECTED;
                return m_State;
            }

            if(m_nodes.size()==ipassedCount) {
                m_State = NODE_STATE.PASSED;
                return m_State;
            }

            m_State = NODE_STATE.PENDING;
            return m_State;

        }

        //当组内关系即不为OR，也不为AND时，直接通过，认为当前组节点无意义。
        m_State = NODE_STATE.PASSED;
        return m_State;

    }

    @Override
    public void showInfo() {

        StringBuilder sb=new StringBuilder();
        for(int i=0;i<m_iLevel;i++)
            sb.append("---");

        sb.append("【");
        sb.append(m_Type);
        sb.append("】");
        sb.append(" 节点名称:【");
        sb.append(m_name);
        sb.append("】");
        sb.append(" 组内关系:【");
        sb.append(m_rel);
        sb.append("】");
        sb.append(" 执行结果:【");
        sb.append(m_State);
        sb.append("】");

        String s=sb.toString();
        System.out.println(s);

        for(baseNode node:m_nodes)
        {
            node.showInfo();
        }
    }
}
