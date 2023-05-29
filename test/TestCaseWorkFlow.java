package test;

import org.junit.jupiter.api.*;
import workflow.Flow;
import workflow.node.*;
import workflow.op.Person;
import workflow.op.Rank;
import workflow.util.FileUtil;

import java.util.ArrayList;
import java.util.Arrays;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class TestCaseWorkFlow {



    static Flow flow=null;
    static Begin begin=null;
    @BeforeAll
    public static void init(){

        flow=new Flow();

        //构造一个工作流， 由以下节点组成：  开始  发起人提交 主任审批(主任 OR 副主任) 结束
        (begin=new Begin(flow))
                .setNextNode(new UI(flow,"FQRTJ","发起人提交"))
                .setNextNode(new Group(flow,"GP_ZRSP","主任审批", REL_IN_GROUP.OR,new baseNode[]{
                        new UI(flow,"ZRSP","主任审批"),
                        new UI(flow,"FZRSP","副主任审批")
                }))
                .setNextNode(new Group(flow,"BAGD","文件归档", REL_IN_GROUP.AND,new baseNode[]{
                        new Group(flow,"BAQS","文件签收", REL_IN_GROUP.OR,new baseNode[]{
                                new UI(flow,"WJSGQS","文件手工签收"),
                                new UI(flow,"WJSMQS","文件扫码签收")

                        }),
                        new UI(flow,"WJSZRQR","文件室主任确认")
                }))
                .setNextNode(new End(flow));


        flow.setBegin(begin);


    }
    @Test
    public void t1_打印初始审批流信息()
    {
        //打印初始流程信息
        //flow.showInfo();

    }

    @Test
    public  void t2_设定节点操作员()
    {




        baseNode nodeFQRTJ=flow.findNode("FQRTJ");
        Assertions.assertEquals(true,nodeFQRTJ!=null,"找到发起人提交模块");
        baseNode nodeZRSP=flow.findNode("ZRSP");
        Assertions.assertEquals(true,nodeZRSP!=null,"找到主任审批模块");
        baseNode nodeFZRSP= flow.findNode("FZRSP");
        Assertions.assertEquals(true,nodeFZRSP!=null,"找到副主任审批模块");
        baseNode nodeWJSSGQS= flow.findNode("WJSGQS");
        Assertions.assertEquals(true,nodeFZRSP!=null,"找到文件室手工签收模块");


        //构造人员 张三
        Person ry_zs=new Person(flow,"S1000","张三");
        //构造职称 主任职称
        Rank zc_zr=new Rank(flow,"T01","主任职称");
        //构造职称 副主任职称
        Rank zc_fzr=new Rank(flow,"T02","副主任职称");


        //设定 人员张三，职称主任，职称副主任 为【发起人提交】的操作员
        Assertions.assertEquals(1,flow.setOP(ry_zs,nodeFQRTJ),"当前流程中总计1操作员");
        Assertions.assertEquals(2,flow.setOP(zc_zr,nodeFQRTJ),"当前流程中总计2操作员");
        Assertions.assertEquals(3,flow.setOP(zc_fzr,nodeFQRTJ),"当前流程中总计3操作员");

        //设定 职称主任，为【主任审批】的操作员
        Assertions.assertEquals(3,flow.setOP(zc_zr,nodeZRSP),"当前流程中总计3操作员");

        //设定 职称副主任，为【副主任审批】的操作员
        Assertions.assertEquals(3,flow.setOP(zc_fzr,nodeFZRSP),"当前流程中总计3操作员");

        //设定 职称主任，为【文件室手工签收】的操作员
        Assertions.assertEquals(3,flow.setOP(zc_zr,nodeWJSSGQS),"当前流程中总计3操作员");


    }

    @Test
    public void t3_根据操作员查询有操作权限的节点()
    {
        //查询张三可处理的节点
        String[] codesNode1= flow.findNodeByOP("S1000");
        Assertions.assertEquals(1,codesNode1.length,"张三可处理的节点数为1");

        //查询主任可处理的节点
        String[] codesNode2= flow.findNodeByOP("T01");
        Assertions.assertEquals(3,codesNode2.length,"主任可处理的节点数为3");

        //查询副主任可处理的节点
        String[] codesNode3= flow.findNodeByOP("T02");
        Assertions.assertEquals(2,codesNode3.length,"副主任可处理的节点数为2");

    }

    @Test
    public void t4_查询流程中等待处理的节点()
    {
        //查询当前流程中，等待处理的节点
       ArrayList<String> arNodes= flow.waitForProcessing();
       Assertions.assertEquals(1,arNodes.size(),"当前等待处理的节点数应为1");
        for (String sn:arNodes) {
            System.out.println("");
            System.out.print("流程中待处理节点编码:【"+sn+"】 ，有权限的操作员编码: ");

            ArrayList<String> arOps= flow.findOPSByNode(sn);
            for(String sop:arOps)
            {
                System.out.print("【"+sop+"】");
            }
        }


       //将发起人提交节点执行
        flow.exec_Passed("FQRTJ","S1000");
        System.out.println("");
        System.out.println("张三提交文件");

        //再次查询当前流程中，等待处理的节点
        arNodes.clear();
        arNodes= flow.waitForProcessing();
        Assertions.assertEquals(2,arNodes.size(),"当前等待处理的节点数应为2");
        for (String sn:arNodes) {
            System.out.println("");
            System.out.print("流程中待处理节点编码:【"+sn+"】 ，有权限的操作员编码: ");

            ArrayList<String> arOps= flow.findOPSByNode(sn);
            for(String sop:arOps)
            {
                System.out.print("【"+sop+"】");
            }
        }

        //将副主任审批节点执行
        flow.exec_Passed("FZRSP","T02");
        System.out.println("");
        System.out.println("副主任职称审批文件");

        //再次查询当前流程中，等待处理的节点
        arNodes.clear();
        arNodes= flow.waitForProcessing();
        Assertions.assertEquals(3,arNodes.size(),"当前等待处理的节点数应为3");
        for (String sn:arNodes) {
            System.out.println("");
            System.out.print("流程中待处理节点编码:【"+sn+"】 ，有权限的操作员编码: ");

            ArrayList<String> arOps= flow.findOPSByNode(sn);
            for(String sop:arOps)
            {
                System.out.print("【"+sop+"】");
            }
        }

        //将文件室手工签收 驳回
        flow.exec_Rejected("WJSGQS","ADMIN","没原因");
        System.out.println("");
        System.out.println("ADMIN驳回文件手工签收");
        arNodes.clear();
        arNodes= flow.waitForProcessing();
        Assertions.assertEquals(3,arNodes.size(),"当前等待处理的节点数应为3，因为操作员ADMIN未在流程中,无权处理");
        for (String sn:arNodes) {
            System.out.println("");
            System.out.print("流程中待处理节点编码:【"+sn+"】 ，有权限的操作员编码: ");

            ArrayList<String> arOps= flow.findOPSByNode(sn);
            for(String sop:arOps)
            {
                System.out.print("【"+sop+"】");
            }
        }

        //将文件室手工签收 驳回
        flow.exec_Rejected("WJSGQS","T01","没原因");
        System.out.println("");
        System.out.println("主任驳回文件手工签收");
        arNodes.clear();
        arNodes= flow.waitForProcessing();
        Assertions.assertEquals(0,arNodes.size(),"当前等待处理的节点数应为0，流程已结束");
        for (String sn:arNodes) {
            System.out.println("");
            System.out.print("流程中待处理节点编码:【"+sn+"】 ，有权限的操作员编码: ");

            ArrayList<String> arOps= flow.findOPSByNode(sn);
            for(String sop:arOps)
            {
                System.out.print("【"+sop+"】");
            }
        }
        System.out.println(" ");



        flow.showInfo();


    }

    @Test
    public  void t5_产生流程定义XML()
    {
        System.out.println("流程定义(XML):");
        String sxml= flow.getFlowDefineXml();
        System.out.println(sxml);

        //将流程定义XML保存为文件
        FileUtil.writeStrToFile(sxml,".//log4j//out.xml");


    }

    @Test
    public  void t6_产生操作记录XML和流程状态XML()
    {
        String sexecLog=flow.getFlowLogXml();

        System.out.println(sexecLog);

        //将流程定义XML保存为文件
        FileUtil.writeStrToFile(sexecLog,".//log4j//out2.xml");

        String sstateXml=flow.getFlowStateXml();

        System.out.println(sstateXml);

        //将流程定义XML保存为文件
        FileUtil.writeStrToFile(sstateXml,".//log4j//out3.xml");



    }

    @Test
    public  void t7_加载已有流程定义XML和执行日志() {

        String sxml= flow.getFlowDefineXml();
        String sexecLog=flow.getFlowLogXml();
        String sstate=flow.getFlowStateXml();

        Flow flow2=new Flow();
        Assertions.assertEquals(true,flow2.loadFlowDefineXml(sxml),"加载流程定义成功");
        Assertions.assertEquals(true,flow2.loadFlowLogXml(sexecLog),"加载流程执行日志成功");
        Assertions.assertEquals(true,flow2.loadFlowStateXml(sstate),"加载流程执行状态成功");

        //打印新流程对象的流程树
        flow2.showInfo();


    }






}
