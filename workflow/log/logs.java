package workflow.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import workflow.Flow;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;

public class logs
{
    private static final Logger log = LoggerFactory.getLogger(logs.class);

    Flow _flow=null;
    public logs(Flow flow)
    {
        _flow=flow;
    }

    //流程中节点操作日志,KEY 为节点编码,VALUE为执行信息
    private HashMap<String, baseExec> m_logs = new HashMap<String, baseExec>();

    //增加执行日志,同一节点在一次流程实例中，只应执行一次,返回 当前已经执行过的节点数
    public  int addLog(baseExec exec)
    {
        m_logs.put(exec.m_node.m_code,exec);
        return m_logs.keySet().size();

    }

    public  void showInfo()
    {
        for(baseExec exec:m_logs.values())
        {
            if(Rejected.class.isInstance(exec)==true) {
                Rejected rej=(Rejected)exec;
                System.out.println("时间: " + exec.m_dt + " 节点名称: " + exec.m_node.m_name + " 操作员: " + exec.m_op.m_name + " 操作: " + exec.m_state.toString() + " 驳回原因: " + rej.m_desc);
            }
            else
                System.out.println("时间: "+exec.m_dt+" 节点名称: "+exec.m_node.m_name+" 操作员: "+exec.m_op.m_name+" 操作: "+exec.m_state.toString());
        }

    }


    public boolean loadXmlExecLog(String xml)
    {
        m_logs.clear();

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

            if(root.getTagName().equals("workflowlog")==false)
                return false;

            if(root.hasChildNodes()==true)
            {
                NodeList nList=root.getChildNodes();
                for(int i=0;i<nList.getLength();i++)
                {
                    Node nNode=nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        if(eElement.getTagName().equals("exec")) {
                            String state=eElement.getAttribute("st");
                            baseExec newExec=baseExec.createBaseExec(_flow,state);
                            if(null==newExec)
                                return false;

                            if(newExec.loadXml(_flow,eElement)==false)
                                return false;

                            m_logs.put(newExec.m_node.m_code,newExec);

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

    public String getXmlExecLog()
    {
        try
        {
            DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
            DocumentBuilder build=factory.newDocumentBuilder();
            Document doc=build.newDocument();

            Element elmRoot=doc.createElement("workflowlog");
            doc.appendChild(elmRoot);

            elmRoot.setAttribute("flow_code",_flow.m_code);
            elmRoot.setAttribute("flow_name",_flow.m_name);


            for(baseExec exec:m_logs.values())
            {
                if(exec.getXml(doc,elmRoot)==false)
                    return "";

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




}
