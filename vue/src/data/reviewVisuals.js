import reviewScenes from "@/assets/imgs/review/review-scenes.jpg"

const scenePositions = {
  landing: "0% 0%",
  register: "33.333% 0%",
  login: "66.666% 0%",
  student: "100% 0%",
  practice: "0% 100%",
  exam: "33.333% 100%",
  result: "66.666% 100%",
  admin: "100% 100%",
}

export const sceneLabels = {
  landing: "服务器入口",
  register: "新人登记",
  login: "安全登录",
  student: "学习基地",
  practice: "红石工坊",
  exam: "正式审核",
  result: "结果公示",
  admin: "后台中枢",
}

export const getSceneStyle = (scene = "landing") => ({
  backgroundImage: `url(${reviewScenes})`,
  backgroundSize: "400% 200%",
  backgroundPosition: scenePositions[scene] || scenePositions.landing,
})

export const referenceVisuals = [
  {
    key: "redstone",
    title: "红石电路参考",
    desc: "适合比较器、中继器、逻辑门、机器稳定性相关题目。",
    src: "https://commons.wikimedia.org/wiki/Special:FilePath/Xbox_Masterclass_Redstone_circuits.png",
    source: "https://commons.wikimedia.org/wiki/File:Xbox_Masterclass_Redstone_circuits.png",
    sourceName: "Wikimedia Commons",
    tags: ["红石", "电路", "中继器", "比较器", "活塞", "逻辑", "机器", "时钟", "线路"],
  },
  {
    key: "logic",
    title: "逻辑关系示意",
    desc: "适合解释与或非、触发条件、信号传递与判断流程。",
    src: "https://commons.wikimedia.org/wiki/Special:FilePath/Logic-gate-index.png",
    source: "https://commons.wikimedia.org/wiki/File:Logic-gate-index.png",
    sourceName: "Wikimedia Commons",
    tags: ["逻辑", "与门", "或门", "非门", "条件", "信号", "判断", "流程"],
  },
  {
    key: "building",
    title: "建筑比例参考",
    desc: "适合建筑比例、外立面、材料层次、动线和观感题目。",
    scene: "landing",
    tags: ["建筑", "比例", "材质", "屋顶", "外观", "装饰", "规划", "动线"],
  },
  {
    key: "server",
    title: "服务器规则场景",
    desc: "适合新人规范、公共设施、沟通协作和审核纪律题目。",
    scene: "register",
    tags: ["规则", "新人", "申请", "沟通", "协作", "纪律", "名声", "违规", "服务器"],
  },
  {
    key: "storage",
    title: "仓储与运维场景",
    desc: "适合公共仓储、机器维护、项目记录和后期交接题目。",
    scene: "admin",
    tags: ["仓储", "分类", "维护", "记录", "后期", "交接", "公共", "物品"],
  },
]

export const getQuestionVisuals = (question = {}, limit = 2) => {
  const text = [
    question.content,
    question.analysis,
    question.categoryName,
    question.subjectName,
    question.type,
  ].filter(Boolean).join(" ").toLowerCase()

  const matched = referenceVisuals.filter(item => {
    return item.tags.some(tag => text.includes(tag.toLowerCase()))
  })

  if (!matched.length) {
    if (question.type === "essay" || question.type === "fillin" || question.type === "fill") {
      return referenceVisuals.filter(item => ["server", "storage"].includes(item.key)).slice(0, limit)
    }
    return referenceVisuals.filter(item => ["server"].includes(item.key)).slice(0, limit)
  }

  return matched.slice(0, limit)
}

export const visualBackgroundStyle = (visual) => {
  if (visual?.src) {
    return { backgroundImage: `url(${visual.src})` }
  }
  return getSceneStyle(visual?.scene || "practice")
}
