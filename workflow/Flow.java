package workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import workflow.log.Passed;
import workflow.log.Rejected;
import workflow.log.logs;
import workflow.node.*;
import workflow.op.baseOP;
import workflow.op.ops;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Flow {


    private static final Logger log = LoggerFactory.getLogger(Flow.class);

    //以节点编码为KEY，索引当前流程所有节点，仅用于内部使用，用于快速找到节点对象
    private HashMap<String,baseNode> _AllNodeIndex =new HashMap<String,baseNode>();

    //流程编码
    public  String m_code ="WORK_FLOW";
    //流程名称
    public  String m_name ="新流程";

    //流程开始节点
    private Begin m_beginNode=null;

    public int _indexNode(baseNode node)
    {
        if(null==node)
            return 0;
        _AllNodeIndex.put(node.m_code,node);

        return _AllNodeIndex.keySet().size();
    }
    public Flow()
    {
    }
    public void setBegin(Begin begin)
    {


        m_beginNode=begin;
        _indexNode(begin);

    }

    private NODE_STATE m_flowState= NODE_STATE.PENDING;

    //根据节点编码，查询流程中可处理该节点的操作员，返回操作员编码
    public ArrayList<String> findOPSByNode(String code_node)
    {
        return m_ops.findOPSByNode(code_node);
    }
    //查询流程中当前等待处理的节点编码数组
    public ArrayList<String> waitForProcessing()
    {
        if(null==m_beginNode)
            return null;

        ArrayList<String> ar=new ArrayList<>();

        NODE_STATE endState= NODE_STATE.PASSED;

        baseNode node=m_beginNode;
        while(node!=null)
        {
            if(node.getState()== NODE_STATE.REJECTED) {
                endState= NODE_STATE.REJECTED;
                break;
            }

            if(node.getType()== NODE_TYPE.END)
                break;

            if(node.getState()== NODE_STATE.PENDING)
            {
                ArrayList<String> ar2=    node.waitForProcessing();
                if(ar2!=null)
                    ar.addAll(ar2);

                endState= NODE_STATE.PENDING;
                break;
            }
            node=node.nextNode();
        }


        node=m_beginNode;
        while(node!=null)
        {
            if(node.getType()== NODE_TYPE.END)
            {
                node.setState(endState);
                break;
            }
            node=node.nextNode();
        }

        return ar;

    }


    //根据操作员编码，查询可处理的流程节点，返回节点编码数组
    // 注意区分： 等待处理节点（流程执行中需要处理）  与 可处理节点（流程定义中有操作权限的）
    public String[] findNodeByOP(String code_op)
    {
        String[] code_nodes= m_ops.findOwnNodeByOP(code_op);
        return code_nodes;

    }

    //根据操作员编码查找操作员
    public baseOP findOP(String codeofOP)
    {
        return m_ops.findOP(codeofOP);
    }

    //为人员设定可操作的节点
    public int setOP(baseOP op, baseNode node){
        m_ops.appendOP(op,node);
        return m_ops.count();
    }

    //根据节点编码查找节点对象
    public baseNode findNode(String codeofNode)
    {

        if(null==codeofNode || codeofNode.isEmpty()==true)
            return null;

        baseNode fnd=null;
        fnd=_AllNodeIndex.get(codeofNode);
        return fnd;
    }

    //操作人员拥有权限的节点
    ops m_ops =new ops(this);

    //流程节点执行日志
    logs m_log_exec =new logs(this);

    //打印工作流结构
    public  void showInfo()
    {
        System.out.println("--------------流程树---------------");

        if(null==m_beginNode)
            return;

        baseNode node=m_beginNode;
        while(node!=null)
        {
            node.showInfo();
            node=node.nextNode();
        }

        System.out.println("流程树共有节点数: "+ String.valueOf(_AllNodeIndex.keySet().size()));
        System.out.println("--------------流程树---------------");


        System.out.println("--------------操作员---------------");
        m_ops.showInfo();
        System.out.println("--------------操作员---------------");

        System.out.println("--------------操作日志---------------");
        m_log_exec.showInfo();
        System.out.println("--------------操作日志---------------");

    }

    public boolean exec_Passed(String codeofNODE, String codeofOP)
    {
        baseNode node =findNode(codeofNODE);
        if(null==node)
            return false;

        baseOP op=findOP(codeofOP);
        if(null==op)
            return  false;

        //操作员无节点权限，则不执行
        if(op.canProcessing(codeofNODE)==false)
            return false;

        //该节点不是当前流程中等处理节点，则不执行
        ArrayList<String> arWaitNodes=waitForProcessing();
        if(null ==arWaitNodes || arWaitNodes.contains(codeofNODE)==false)
            return false;

        NODE_STATE st= node.exec_Passed();


        m_log_exec.addLog(new Passed(this,codeofOP,codeofNODE,st,""));

        return true;
    }
    public boolean exec_Rejected(String codeofNODE, String codeofOP, String rejectedDesc)
    {
        baseNode node =findNode(codeofNODE);
        if(null==node)
            return false;

        baseOP op=findOP(codeofOP);
        if(null==op)
            return  false;

        //操作员无节点权限，则不执行
        if(op.canProcessing(codeofNODE)==false)
            return false;

        //该节点不是当前流程中等处理节点，则不执行
        ArrayList<String> arWaitNodes=waitForProcessing();
        if(null ==arWaitNodes || arWaitNodes.contains(codeofNODE)==false)
            return false;


        NODE_STATE st=node.exec_Rejected();

        m_log_exec.addLog(new Rejected(this,codeofOP,codeofNODE,st,rejectedDesc));

        return true;

    }

    public boolean loadFlowDefineXml(String xml)
    {
        try
        {
            _AllNodeIndex.clear();

            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factory.newDocumentBuilder();

            ByteArrayInputStream input = new ByteArrayInputStream(
                    xml.getBytes("UTF-8"));
            Document doc=builder.parse(input);

            if(null==doc)
                return false;

            Element root = doc.getDocumentElement();
            if(null==root)
                return false;

            if(root.getTagName().equals("workflow")==false)
                return false;

            m_code=root.getAttribute("code");
            m_name=root.getAttribute("name");



            baseNode nodePerv=null;
            if(root.hasChildNodes()==true)
            {
                NodeList nList=root.getChildNodes();
                for(int i=0;i<nList.getLength();i++)
                {
                    Node nNode=nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        if(eElement.getTagName().equals("node")) {

                            baseNode newNode = baseNode.createBaseNode(this,eElement.getAttribute("type"));
                            if (null == newNode)
                                return false;

                            if (newNode.loadXml(this,eElement, 0) == false)
                                return false;

                            if (newNode.getType() == NODE_TYPE.BEGIN)
                                m_beginNode = (Begin) newNode;

                            if (null == nodePerv) {
                                nodePerv = newNode;
                            } else {
                                nodePerv.setNextNode(newNode);
                                nodePerv = newNode;
                            }

                        }
                        else if(eElement.getTagName().equals("ops"))
                        {
                            m_ops=new ops(this);
                            if(m_ops.loadXmlDefine(this,eElement)==false)
                                return false;


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


    public boolean loadFlowStateXml(String xml)
    {
        try
        {

            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factory.newDocumentBuilder();

            ByteArrayInputStream input = new ByteArrayInputStream(
                    xml.getBytes("UTF-8"));
            Document doc=builder.parse(input);

            if(null==doc)
                return false;

            Element root = doc.getDocumentElement();
            if(null==root)
                return false;

            if(root.getTagName().equals("workflowstate")==false)
                return false;

            if(root.hasChildNodes()==true)
            {
                NodeList nList=root.getChildNodes();
                for(int i=0;i<nList.getLength();i++)
                {
                    Node nNode=nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        if(eElement.getTagName().equals("node")) {
                            String state=eElement.getAttribute("st");
                            String snodecode=eElement.getAttribute("code");

                            NODE_STATE eST=NODE_STATE.valueOf(state);
                            _AllNodeIndex.get(snodecode).setState(eST);

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
    public boolean loadFlowLogXml(String xml)
    {
        return m_log_exec.loadXmlExecLog(xml);

    }

    public String getFlowDefineXml()
    {
        try
        {
            DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
            DocumentBuilder build=factory.newDocumentBuilder();
            Document doc=build.newDocument();

            Element elmRoot=doc.createElement("workflow");
            doc.appendChild(elmRoot);

            elmRoot.setAttribute("code",m_code);
            elmRoot.setAttribute("name",m_name);


            //存流程节点信息
            baseNode node=m_beginNode;
            while(node!=null)
            {
                if(node.getXml(doc,elmRoot)==false)
                    return "";

                node=node.nextNode();
            }

            //存操作员信息
            if(m_ops.getXmlDefine(doc,elmRoot)==false)
                return "";



            //从dom doc 得到 xml
            TransformerFactory tfactory= TransformerFactory.newInstance();

            Transformer transformer=tfactory.newTransformer();
            DOMSource source=new DOMSource(doc);
            StringWriter strWtr = new StringWriter();
            StreamResult result=new StreamResult(strWtr);
            transformer.setOutputProperty("encoding","UTF-8");
            transformer.transform(source,result);

            String sxml=result.getWriter().toString();
            return sxml;

        }
        catch (Exception ex)
        {
            log.error(ex.toString());

        }

        return "";
    }

    public String getFlowStateXml()
    {
        try
        {
            DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
            DocumentBuilder build=factory.newDocumentBuilder();
            Document doc=build.newDocument();

            Element elmRoot=doc.createElement("workflowstate");
            doc.appendChild(elmRoot);

            elmRoot.setAttribute("flow_code",m_code);
            elmRoot.setAttribute("flow_name",m_name);


            for(baseNode nd:_AllNodeIndex.values())
            {
                Element eleState=doc.createElement("node");
                eleState.setAttribute("code",nd.m_code);
                eleState.setAttribute("st",nd.getState().toString());
                elmRoot.appendChild(eleState);
            }



            //从dom doc 得到 xml
            TransformerFactory tfactory= TransformerFactory.newInstance();

            Transformer transformer=tfactory.newTransformer();
            DOMSource source=new DOMSource(doc);
            StringWriter strWtr = new StringWriter();
            StreamResult result=new StreamResult(strWtr);
            transformer.setOutputProperty("encoding","UTF-8");
            transformer.transform(source,result);

            String sxml=result.getWriter().toString();
            return sxml;

        }
        catch (Exception ex)
        {
            log.error(ex.toString());

        }

        return "";
    }
    public String getFlowLogXml()
    {
        return m_log_exec.getXmlExecLog();
    }

}
