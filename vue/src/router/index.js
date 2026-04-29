import { createRouter, createWebHistory } from 'vue-router'

const ADMIN_ROLES = ['OWNER', 'ADMIN']
const REVIEW_ROLES = ['OWNER', 'ADMIN', 'HELPER']
const ALL_ROLES = ['OWNER', 'ADMIN', 'HELPER', 'USER']

const getUser = () => {
  try {
    return JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
  } catch {
    return {}
  }
}

const homeByRole = (role) => {
  if (ADMIN_ROLES.includes(role)) return '/manager/home'
  if (role === 'HELPER') return '/exam/dashboard'
  if (role === 'USER') return '/front/home'
  return '/login'
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: () => import('@/views/HomePage.vue') },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      children: [
        { path: 'home', meta: { name: '控制台' }, component: () => import('@/views/manager/Home.vue'),  },
        { path: 'admin', meta: { name: '管理员信息' }, component: () => import('@/views/manager/Admin.vue'), },
        { path: 'person', meta: { name: '个人资料' }, component: () => import('@/views/manager/Person.vue'), },
        { path: 'password', meta: { name: '修改密码' }, component: () => import('@/views/manager/Password.vue'), },
        { path: 'student', meta: { name: '玩家信息' }, component: () => import('@/views/manager/Student.vue'), },
        { path: 'registrationApproval', meta: { name: '注册申请审批' }, component: () => import('@/views/manager/RegistrationApproval.vue'), },
        { path: 'invitationCode', meta: { name: '邀请码管理' }, component: () => import('@/views/manager/InvitationCodeManagement.vue'), },
        { path: 'question', meta: { name: '题目管理' }, component: () => import('@/views/manager/Question.vue'), },
        { path: 'questionReview', meta: { name: '题目审核' }, component: () => import('@/views/manager/QuestionReview.vue'), },
        { path: 'questionCategory', meta: { name: '题目分类' }, component: () => import('@/views/manager/QuestionCategory.vue'), },
        { path: 'examPaper', meta: { name: '试卷管理' }, component: () => import('@/views/manager/ExamPaper.vue'), },
        { path: 'exam', meta: { name: '审核管理' }, component: () => import('@/views/manager/Exam.vue'), },
      { path: 'grading', meta: { name: '批阅管理' }, component: () => import('@/views/manager/Grading.vue'), },
        { path: 'score', meta: { name: '结果管理' }, component: () => import('@/views/manager/Score.vue'), },
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue'),  },
        { path: 'subjects', component: () => import('@/views/front/SubjectBrowse.vue'),  },
        { path: 'person', component: () => import('@/views/front/Person.vue'),  },
        { path: 'examList', component: () => import('@/views/front/ExamList.vue'),  },
        { path: 'practiceMode', component: () => import('@/views/front/PracticeMode.vue'),  },
        { path: 'wrongQuestions', component: () => import('@/views/front/WrongQuestions.vue'),  },
        { path: 'contributeQuestion', component: () => import('@/views/front/ContributeQuestion.vue'),  },
        { path: 'leaderboard', component: () => import('@/views/front/Leaderboard.vue'),  },
        { path: 'examTaking', component: () => import('@/views/front/ExamTaking.vue'),  },
        { path: 'examResult', component: () => import('@/views/front/ExamResult.vue'),  },
        { path: 'myScores', component: () => import('@/views/front/MyScores.vue'),  }
      ]
    },
    {
      path: '/exam',
      component: () => import('@/views/exam/ExamLayout.vue'),
      redirect: '/exam/dashboard',
      children: [
        { path: 'dashboard', meta: { name: '工作台' }, component: () => import('@/views/exam/Dashboard.vue') },
        { path: 'examManage', meta: { name: '审核管理' }, component: () => import('@/views/exam/ExamManage.vue') },
        { path: 'takeExam', meta: { name: '参加审核' }, component: () => import('@/views/exam/TakeExam.vue') },
      { path: 'gradingCenter', meta: { name: '批阅中心' }, component: () => import('@/views/exam/GradingCenter.vue') },
        { path: 'approvalCenter', meta: { name: '审批中心' }, component: () => import('@/views/exam/ApprovalCenter.vue') },
        { path: 'resultsCenter', meta: { name: '结果公示' }, component: () => import('@/views/exam/ResultsCenter.vue') },
        { path: 'paperManage', meta: { name: '试卷管理' }, component: () => import('@/views/manager/ExamPaper.vue') },
        { path: 'examAdmin', meta: { name: '审核管理' }, component: () => import('@/views/manager/Exam.vue') },
        { path: 'scoreManage', meta: { name: '结果管理' }, component: () => import('@/views/manager/Score.vue') },
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') },
    { path: '/404', component: () => import('@/views/404.vue') },
    { path: '/:pathMatch(.*)', redirect: '/404' }
  ]
})

const protectedPathRoles = [
  { prefix: '/manager', roles: ADMIN_ROLES },
  { prefix: '/exam/gradingCenter', roles: REVIEW_ROLES },
  { prefix: '/exam/approvalCenter', roles: REVIEW_ROLES },
  { prefix: '/exam/paperManage', roles: ADMIN_ROLES },
  { prefix: '/exam/examAdmin', roles: ADMIN_ROLES },
  { prefix: '/exam/scoreManage', roles: ADMIN_ROLES },
  { prefix: '/exam', roles: ALL_ROLES },
  { prefix: '/front', roles: ALL_ROLES }
]

router.beforeEach((to) => {
  const rule = protectedPathRoles.find(item => to.path.startsWith(item.prefix))
  if (!rule) return true

  const user = getUser()
  if (!user?.token || !user?.role) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }

  if (!rule.roles.includes(user.role)) {
    return homeByRole(user.role)
  }

  return true
})

export default router
