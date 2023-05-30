# workflow

workflow 是一个简单的基于树的工作流引擎。可通过Java代码构造流程树，
提供了节点之间的“与”、“或” 协同审批工能。未提供图形化流程设计器。

# 一，建议库表设计
开发者需要根据业务场景，自行设计数据库表，用于存放工作流定义（工作流模板）、操作人员
（或职称）与流程结点的关系、流程执行状态及日志。
## 1.1 工作流定义表
基础数据，在工作流设计模块，将通过Java构造好的工作流XML保存在库表中。  

| 工作流ID | 工作流名称   | 工作流定义       |
|-------|---------|-------------|
| 1     | 文件审签工作流 | Blob or Xml |

## 1.2 操作节点权限表
基础数据，在工作流设计模块中，通过Java构造工作流XML时， 按下表中数据设定人员与流程节点的关系。

| 工作流节点ID | 人员ID  | 人员姓名 |
|---------|-------|------|
| N001    | S1000 | 张三   |

## 1.3 工作流执行过程表
过程数据，当工作流恢复时（例如服务终止再启动），从过程数据中加载执行过程中的工作流状态。

| 工作流ID | 执行时间                    | 工作流状态       |
|-------|-------------------------|-------------|
| 1     | 2023.01.01 14:21:30 321 | Blob or Xml |


## 1.4 工作执行日志表
过程数据，按需保存工作流操作人员执行日志， 供审计使用。


| 工作流ID | 日志时间                    | 工作流日志       |
|-------|-------------------------|-------------|
| 1     | 2023.01.01 14:21:30 321 | Blob or Xml |


# 二，API 说明
## 2.1 创建工作流
```java
        Flow flow=new Flow();
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

```

## 2.2 设定工作流中节点的操作人
```java
        //构造人员 张三
        Person ry_zs=new Person(flow,"S1000","张三");
        //构造职称 主任职称
        Rank zc_zr=new Rank(flow,"T01","主任职称");
        //构造职称 副主任职称
        Rank zc_fzr=new Rank(flow,"T02","副主任职称");


        //根据节点编码查找发起人提交节点
        baseNode nodeFQRTJ=flow.findNode("FQRTJ");
        //设定 人员张三，职称主任，职称副主任 为发起人提交节点的操作员
        flow.setOP(ry_zs,nodeFQRTJ);
        flow.setOP(zc_zr,nodeFQRTJ);
        flow.setOP(zc_fzr,nodeFQRTJ);

        //根据节点编码查找主任审批节点
        baseNode nodeZRSP=flow.findNode("ZRSP");
        //设定 职称主任，为主任审批节点的操作员
        flow.setOP(zc_zr,nodeZRSP);

        //根据节点编码查找副主任审批节点
        baseNode nodeFZRSP= flow.findNode("FZRSP");
        //设定 职称副主任，为副主任审批节点的操作员
        flow.setOP(zc_fzr,nodeFZRSP);

        //根据节点编码查找文件室手工签收节点
        baseNode nodeWJSSGQS= flow.findNode("WJSGQS");
        //设定 职称主任，为文件室手工签收节点的操作员
        flow.setOP(zc_zr,nodeWJSSGQS);
```

## 2.3 保存工作流模板
````java
       String flowTemplate=flow.getFlowDefineXml();
````
## 2.4 查询当前流程中待处理的节点和有权限的操作人员
```java
       ArrayList<String> arNodes= flow.waitForProcessing();
       for (String sn:arNodes) {
            System.out.println("");
            System.out.print("流程中待处理节点编码:["+sn+"] ，有权限的操作员编码: ");

            ArrayList<String> arOps= flow.findOPSByNode(sn);
            for(String sop:arOps)
            {
                System.out.print("["+sop+"]");
            }
        }
```

## 2.5 执行流程节点
```java
       //执行通过
       flow.exec_Passed("节点编码","操作人员编码");
       //执行驳回
       flow.exec_Rejected("节点编码","操作人员编码","驳回原因");
```

## 2.6 保存工作流状态
````java
       String state=flow.getFlowStateXml();
````

## 2.7 保存工作流执行日志
````java
       String log=flow.getFlowLogXml();
````

## 2.8 从离线数据恢复工作流
````java
       Flow flow=new Flow();
       flow.loadFlowDefineXml(工作流模板Xml);
       flow.loadFlowLogXml(工作流日志Xml);
       flow.loadFlowStateXml(工作流状态Xml);

       //打印新流程对象的流程树
       flow.showInfo();
````
