package workflow.op;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import workflow.Flow;
import workflow.node.baseNode;

import java.util.ArrayList;
import java.util.HashMap;

public class ops {
    private static final Logger log = LoggerFactory.getLogger(ops.class);

    Flow _flow=null;
    public ops(Flow flow)
    {
        _flow=flow;
    }

    //操作员与可节点的对照
    //一个节点可有多个操作员
    //操作员编码:节点编码集合
    private HashMap<String, baseOP> m_ops = new HashMap<String, baseOP>();

    public  void showInfo() {

        for(baseOP op: m_ops.values())
            op.showInfo();
    }

    public boolean loadXmlDefine(Flow flow, Element eleCurrent) {

        m_ops.clear();

        try
        {
            if(null==eleCurrent)
                return false;

            if(eleCurrent.getTagName().equals("ops")==false)
                return false;


            if(eleCurrent.hasChildNodes()==true)
            {
                NodeList nList=eleCurrent.getChildNodes();
                for(int i=0;i<nList.getLength();i++)
                {
                    Node nNode=nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        if(eElement.getTagName().equals("op")==true) {
                            baseOP opNew = baseOP.createBaseOP(flow, eElement.getAttribute("type"));

                            if (null == opNew)
                                return false;

                            if (opNew.loadXml(flow, eElement) == false)
                                return false;

                            m_ops.put(opNew.m_code, opNew);
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

    //产生操作员信息的XML定义，入参为XML文档对象 和 父节点 ，返回值 true成功， false失败
    public boolean getXmlDefine(Document docXml, Element eleParent)
    {
        try
        {
            Element eleCurrent= docXml.createElement("ops");


            for(baseOP op: m_ops.values())
                if(op.getXml(docXml,eleCurrent)==false)
                    return false;

            eleParent.appendChild(eleCurrent);

            return true;

        }
        catch (Exception ex)
        {

            log.error(ex.toString());
            return false;
        }

    }

    //判断操作人员，是否可有操作某节点的权限，返回值 true有， false无
    public boolean canProcessing(String sop, String snode)
    {
        if(m_ops.containsKey(sop)==false) //流程中无该操作员设置
            return false;

        baseOP op=m_ops.get(sop);
        if(null==op)
            return false;

      return op.canProcessing(snode);
    }
    //根据操作员编码查找操作员
    public baseOP findOP(String codeofOP)
    {
        if(m_ops.containsKey(codeofOP)==false)
            return null;
        baseOP op=m_ops.get(codeofOP);
        return op;
    }

    //根据节点编码，查询可处理的操作员编码数组
    public ArrayList<String> findOPSByNode(String codeofNode)
    {
        ArrayList<String> ar=new ArrayList<String>();
        for (baseOP op:m_ops.values()) {
            if(op.canProcessing(codeofNode)==true)
            {
                ar.add(op.m_code);
            }

        }

        return ar;
    }

    //根据操作员编码，查询可处理的流程节点，返回节点编码数组
    public String[] findOwnNodeByOP(String codeofOP)
    {
        if(m_ops.containsKey(codeofOP)==false)
            return null;
        baseOP op=m_ops.get(codeofOP);
        if(null==op)
            return null;

       return op.getOwnNodeCodes();
    }

    //当前流程总计操作员数
    public int count()
    {
        return m_ops.keySet().size();
    }

    //增加操作员，返回当前节点操作员人数
    public int appendOP(baseOP op, baseNode node)
    {
        if(null==op || null==op.m_code || op.m_code.isEmpty()==true)
            return 0;

        //增加操作员
        m_ops.put(op.m_code,op);

        //为操作员增加 有权限节点
        op.addOwnNode(node);

        return m_ops.keySet().size();
    }


}
