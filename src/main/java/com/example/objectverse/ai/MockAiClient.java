package com.example.objectverse.ai;

import org.springframework.stereotype.Component;

@Component
public class MockAiClient implements AiClient {

    @Override
    public String chat(String prompt) {
        String text = prompt == null ? "" : prompt;
        if (containsAny(text, "代码审查", "code review", "CodeReview", "improvedCode")) {
            return codeReviewJson();
        }
        if (containsAny(text, "VIP", "vip", "会员", "充值", "储值")) {
            return vipJson();
        }
        if (containsAny(text, "物流", "快递", "运输", "配送", "车辆", "司机")) {
            return logisticsMismatchJson();
        }
        if (containsAny(text, "宠物医院", "预约", "诊疗记录", "病历", "支付")) {
            return petHospitalJson();
        }
        return defaultJson();
    }

    private String codeReviewJson() {
        return """
                {
                  "summary": "这段代码可以作为基础示例，但还需要进一步强化封装、职责划分和可扩展性。",
                  "issues": [
                    "字段应优先使用 private，并通过必要的方法暴露行为。",
                    "如果一个类同时承担数据保存、业务计算和输出展示，可能违反单一职责原则。",
                    "条件分支较多时，可以考虑使用策略模式或工厂模式降低扩展成本。"
                  ],
                  "suggestions": [
                    "将对象状态封装在类内部，避免外部直接修改字段。",
                    "把复杂业务规则拆分到独立方法或策略类中。",
                    "为核心业务行为命名清晰的方法，而不是只提供贫血的 getter/setter。"
                  ],
                  "knowledgePoints": [
                    {
                      "title": "封装",
                      "category": "OOP 基础",
                      "explanation": "封装要求隐藏对象内部状态，通过受控方法维护不变量。"
                    },
                    {
                      "title": "单一职责原则",
                      "category": "设计原则",
                      "explanation": "一个类应聚焦一个变化原因，避免职责混杂导致维护困难。"
                    },
                    {
                      "title": "策略模式",
                      "category": "设计模式",
                      "explanation": "策略模式适合把可替换算法封装为独立对象，减少大量 if-else。"
                    }
                  ],
                  "improvedCode": "public class DomainObject {\\n    private String name;\\n\\n    public DomainObject(String name) {\\n        this.name = name;\\n    }\\n\\n    public String getName() {\\n        return name;\\n    }\\n}",
                  "noteContent": "本次审查重点：字段私有化体现封装；类职责要清晰；复杂可变规则可用策略模式或工厂模式扩展。"
                }
                """;
    }

    public String defaultJson() {
        return """
                {
                  "summary": "当前需求描述还比较模糊，暂时无法稳定识别明确的类、属性、方法和关系。",
                  "candidateClasses": [],
                  "candidateRelations": [],
                  "suggestions": [
                    "请补充核心业务对象，例如会员、订单、预约、库存或支付。",
                    "请说明对象需要保存哪些状态，也就是属性。",
                    "请说明对象可以执行哪些行为，也就是方法。",
                    "请说明对象之间的拥有、依赖、协作或继承关系。"
                  ]
                }
                """;
    }

    public String petHospitalJson() {
        return """
                {
                  "summary": "当前项目如果已经存在 Pet、PetOwner、Doctor，可以保留这些核心类，并补充 Appointment、MedicalRecord、Payment，形成宠物医院预约、诊疗和支付闭环。",
                  "candidateClasses": [
                    {
                      "className": "Appointment",
                      "packageName": "com.objectverse.demo.pet",
                      "description": "宠物医院预约对象",
                      "attributes": [
                        {
                          "attributeName": "appointmentTime",
                          "attributeType": "LocalDateTime",
                          "visibility": "PRIVATE",
                          "description": "预约时间"
                        },
                        {
                          "attributeName": "status",
                          "attributeType": "String",
                          "visibility": "PRIVATE",
                          "description": "预约状态"
                        }
                      ],
                      "methods": [
                        {
                          "methodName": "confirmAppointment",
                          "returnType": "void",
                          "parameters": "",
                          "visibility": "PUBLIC",
                          "description": "确认预约",
                          "methodBody": "// TODO: 确认预约状态"
                        },
                        {
                          "methodName": "cancelAppointment",
                          "returnType": "void",
                          "parameters": "",
                          "visibility": "PUBLIC",
                          "description": "取消预约",
                          "methodBody": "// TODO: 取消预约"
                        }
                      ]
                    },
                    {
                      "className": "MedicalRecord",
                      "packageName": "com.objectverse.demo.pet",
                      "description": "宠物诊疗病历对象",
                      "attributes": [
                        {
                          "attributeName": "diagnosis",
                          "attributeType": "String",
                          "visibility": "PRIVATE",
                          "description": "诊断结果"
                        },
                        {
                          "attributeName": "treatmentPlan",
                          "attributeType": "String",
                          "visibility": "PRIVATE",
                          "description": "治疗方案"
                        }
                      ],
                      "methods": [
                        {
                          "methodName": "generateSummary",
                          "returnType": "String",
                          "parameters": "",
                          "visibility": "PUBLIC",
                          "description": "生成病历摘要",
                          "methodBody": "return diagnosis + \\\":\\\" + treatmentPlan;"
                        }
                      ]
                    },
                    {
                      "className": "Payment",
                      "packageName": "com.objectverse.demo.pet",
                      "description": "宠物医院支付对象",
                      "attributes": [
                        {
                          "attributeName": "amount",
                          "attributeType": "Double",
                          "visibility": "PRIVATE",
                          "description": "支付金额"
                        },
                        {
                          "attributeName": "paymentStatus",
                          "attributeType": "String",
                          "visibility": "PRIVATE",
                          "description": "支付状态"
                        }
                      ],
                      "methods": [
                        {
                          "methodName": "pay",
                          "returnType": "Boolean",
                          "parameters": "",
                          "visibility": "PUBLIC",
                          "description": "执行支付",
                          "methodBody": "return true;"
                        }
                      ]
                    }
                  ],
                  "candidateRelations": [
                    {
                      "sourceClassName": "Appointment",
                      "targetClassName": "Payment",
                      "relationType": "DEPENDENCY",
                      "description": "预约确认后依赖支付完成收费"
                    },
                    {
                      "sourceClassName": "MedicalRecord",
                      "targetClassName": "Pet",
                      "relationType": "ASSOCIATION",
                      "description": "病历关联被诊疗的宠物"
                    },
                    {
                      "sourceClassName": "Doctor",
                      "targetClassName": "Appointment",
                      "relationType": "ASSOCIATION",
                      "description": "医生处理预约"
                    }
                  ],
                  "suggestions": [
                    "如果当前项目已经存在 Pet、PetOwner、Doctor，可将它们作为已有核心类继续复用。",
                    "建议补充 Appointment、MedicalRecord、Payment，覆盖预约、诊疗和支付流程。",
                    "建议后续为 Appointment 与 PetOwner、Pet 补充更细的关联关系。"
                  ]
                }
                """;
    }

    private String vipJson() {
        return """
                {
                  "summary": "该需求适合在当前模型中补充会员账户、充值记录和会员等级，用来表达宠物主人的会员身份、储值余额和充值行为。",
                  "candidateClasses": [
                    {
                      "className": "VipMember",
                      "packageName": "com.objectverse.demo.member",
                      "description": "宠物主人会员账户",
                      "attributes": [
                        {
                          "attributeName": "memberNo",
                          "attributeType": "String",
                          "visibility": "PRIVATE",
                          "description": "会员编号"
                        },
                        {
                          "attributeName": "balance",
                          "attributeType": "Double",
                          "visibility": "PRIVATE",
                          "description": "会员储值余额"
                        }
                      ],
                      "methods": [
                        {
                          "methodName": "recharge",
                          "returnType": "void",
                          "parameters": "Double amount",
                          "visibility": "PUBLIC",
                          "description": "为会员账户充值",
                          "methodBody": "this.balance = this.balance + amount;"
                        }
                      ]
                    },
                    {
                      "className": "RechargeRecord",
                      "packageName": "com.objectverse.demo.member",
                      "description": "会员充值记录",
                      "attributes": [
                        {
                          "attributeName": "amount",
                          "attributeType": "Double",
                          "visibility": "PRIVATE",
                          "description": "充值金额"
                        },
                        {
                          "attributeName": "rechargeTime",
                          "attributeType": "LocalDateTime",
                          "visibility": "PRIVATE",
                          "description": "充值时间"
                        }
                      ],
                      "methods": [
                        {
                          "methodName": "markPaid",
                          "returnType": "void",
                          "parameters": "",
                          "visibility": "PUBLIC",
                          "description": "标记充值已支付",
                          "methodBody": "// TODO: 更新充值记录状态"
                        }
                      ]
                    },
                    {
                      "className": "MembershipLevel",
                      "packageName": "com.objectverse.demo.member",
                      "description": "会员等级规则",
                      "attributes": [
                        {
                          "attributeName": "levelName",
                          "attributeType": "String",
                          "visibility": "PRIVATE",
                          "description": "等级名称"
                        },
                        {
                          "attributeName": "discountRate",
                          "attributeType": "Double",
                          "visibility": "PRIVATE",
                          "description": "折扣比例"
                        }
                      ],
                      "methods": [
                        {
                          "methodName": "calculateDiscount",
                          "returnType": "Double",
                          "parameters": "Double amount",
                          "visibility": "PUBLIC",
                          "description": "根据会员等级计算折扣",
                          "methodBody": "return amount * discountRate;"
                        }
                      ]
                    }
                  ],
                  "candidateRelations": [
                    {
                      "sourceClassName": "PetOwner",
                      "targetClassName": "VipMember",
                      "relationType": "ASSOCIATION",
                      "description": "宠物主人可以拥有会员账户"
                    },
                    {
                      "sourceClassName": "RechargeRecord",
                      "targetClassName": "Payment",
                      "relationType": "DEPENDENCY",
                      "description": "充值记录依赖支付完成"
                    },
                    {
                      "sourceClassName": "VipMember",
                      "targetClassName": "MembershipLevel",
                      "relationType": "AGGREGATION",
                      "description": "会员账户引用会员等级规则"
                    }
                  ],
                  "suggestions": [
                    "建议将余额设置为 private，通过充值方法修改，体现封装。",
                    "如果当前模型已有 Payment，可以将 RechargeRecord 与 Payment 建立依赖关系。",
                    "会员等级可独立成类，便于后续扩展不同折扣策略。"
                  ]
                }
                """;
    }

    private String logisticsMismatchJson() {
        return """
                {
                  "summary": "该输入更偏向物流、运输或配送领域，和当前 ObjectVerse 宠物医院建模主题可能不一致。除非需求明确是宠物医院内的药品配送、上门接送或订单配送扩展，否则不建议直接生成候选类。",
                  "candidateClasses": [],
                  "candidateRelations": [],
                  "suggestions": [
                    "请确认该需求是否属于当前项目领域。",
                    "如果是宠物医院配送扩展，请补充配送对象、配送状态、配送行为和与 Appointment、Payment 的关系。",
                    "如果是独立物流系统，建议新建项目而不是混入当前项目模型。"
                  ]
                }
                """;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
