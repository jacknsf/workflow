package workflow.op;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import workflow.Flow;
import workflow.node.*;

import java.util.HashMap;
import java.util.Set;

public class baseOP {
    private static final Logger log = LoggerFactory.getLogger(baseOP.class);

    Flow _flow=null;
    //操作员编码
    public String m_code;
    //操作员名称
    public String m_name ="基础操作员";

    //操作员类型
    public OP_TYPE m_Type;

    public baseOP(Flow flow)
    {
        _flow=flow;
    }
    public  baseOP(Flow flow,String code,String name)
    {
        m_code=code;
        m_name=name;
        _flow=flow;
    }

    public  void showInfo()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("操作员: 【");
        sb.append(m_code);
        sb.append(",");
        sb.append(m_name);
        sb.append("】操作权限:");

        System.out.println(sb.toString());

        for(baseNode node: m_ownNodes.values())
        {
            System.out.println("节点编码: 【"+node.m_code+"】 节点名称: 【"+node.m_name+"】");
        }



    }
    //操作员可操作的节点
    private HashMap<String,baseNode> m_ownNodes=new HashMap<String,baseNode>();

    public static baseOP createBaseOP(Flow flow, String sOPType)
    {
        OP_TYPE ot= OP_TYPE.valueOf(sOPType);
        baseOP opNew=null;
        switch (ot)
        {

            //人员操作员
            case Person:
                opNew=new Person(flow);
                break;
            //职称操作员
            case Rank:
                opNew=new Rank(flow);
                break;

        }
        return opNew;

    }

    //从XML定义加载节点信息
    public boolean loadXml(Flow flow, Element eleCurrent)
    {
        try
        {
            if(null==eleCurrent)
                return false;

            if(eleCurrent.getTagName().equals("op")==false)
                return false;

            m_code=eleCurrent.getAttribute("code");
            m_name=eleCurrent.getAttribute("name");
            m_Type= OP_TYPE.valueOf(eleCurrent.getAttribute("type"));

            m_ownNodes.clear();


            if(eleCurrent.hasChildNodes()==true) {
                NodeList nList = eleCurrent.getChildNodes();
                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        if(eElement.getTagName().equals("node")==true)
                        {
                            String scode_node=eElement.getAttribute("code");
                            baseNode nd=flow.findNode(scode_node);
                            if(null==nd)
                                return false;

                            m_ownNodes.put(scode_node,nd);
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

    //产生操作员的XML定义，入参为XML文档对象 和 父节点 ，返回值 true成功， false失败
    public boolean getXml(Document docXml, Element eleParent)
    {
        try
        {
            Element eleCurrent= docXml.createElement("op");
            eleCurrent.setAttribute("code",m_code);
            eleCurrent.setAttribute("name",m_name);
            eleCurrent.setAttribute("type",m_Type.toString());


            for(baseNode node: m_ownNodes.values())
            {
                Element eleNode= docXml.createElement("node");
                eleNode.setAttribute("code",node.m_code);
                eleCurrent.appendChild(eleNode);

            }

            eleParent.appendChild(eleCurrent);

            return true;

        }
        catch (Exception ex)
        {

            log.error(ex.toString());
            return false;
        }

    }

    //为操作员增加可操作的节点，同一节点多次添加，只有一条记录
    //返回当前操作员有操作权限的节点数
    public  int addOwnNode(baseNode node)
    {

        if(null==node || null==node.m_code || node.m_code.isEmpty()==true)
            return 0;

        m_ownNodes.put(node.m_code,node);
        return m_ownNodes.keySet().size();

    }

    //返回当前操作员拥有权限的节点编码数组
    public String[] getOwnNodeCodes()
    {
        Set<String> hs=m_ownNodes.keySet();
        String[] ar=new String[hs.size()];
        hs.toArray(ar);
        return ar;
    }

    //判断操作人员，是否可有操作某节点的权限，返回值 true有， false无
    public boolean canProcessing(String code_node)
    {
       return  m_ownNodes.containsKey(code_node);
    }


}
