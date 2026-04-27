import { createRouter, createWebHistory } from 'vue-router'

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
        { path: 'notice', meta: { name: '系统公告' }, component: () => import('@/views/manager/Notice.vue'), },
        { path: 'person', meta: { name: '个人资料' }, component: () => import('@/views/manager/Person.vue'), },
        { path: 'password', meta: { name: '修改密码' }, component: () => import('@/views/manager/Password.vue'), },
        { path: 'examiner', meta: { name: '阅卷人信息' }, component: () => import('@/views/manager/Examiner.vue'), },
        { path: 'student', meta: { name: '学生信息' }, component: () => import('@/views/manager/Student.vue'), },
        { path: 'registrationApproval', meta: { name: '注册申请审批' }, component: () => import('@/views/manager/RegistrationApproval.vue'), },
        { path: 'invitationCode', meta: { name: '邀请码管理' }, component: () => import('@/views/manager/InvitationCodeManagement.vue'), },
        { path: 'question', meta: { name: '题目管理' }, component: () => import('@/views/manager/Question.vue'), },
        { path: 'questionCategory', meta: { name: '题目分类' }, component: () => import('@/views/manager/QuestionCategory.vue'), },
        { path: 'tutorial', meta: { name: '例题讲解' }, component: () => import('@/views/manager/Tutorial.vue'), },
        { path: 'forumManage', meta: { name: '论坛管理' }, component: () => import('@/views/manager/ForumManage.vue'), },
        { path: 'questionReview', meta: { name: '题目审核' }, component: () => import('@/views/manager/QuestionReview.vue'), },
        { path: 'examPaper', meta: { name: '试卷管理' }, component: () => import('@/views/manager/ExamPaper.vue'), },
        { path: 'exam', meta: { name: '考试管理' }, component: () => import('@/views/manager/Exam.vue'), },
        { path: 'grading', meta: { name: '阅卷管理' }, component: () => import('@/views/manager/Grading.vue'), },
        { path: 'score', meta: { name: '成绩管理' }, component: () => import('@/views/manager/Score.vue'), },
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
        { path: 'examTaking', component: () => import('@/views/front/ExamTaking.vue'),  },
        { path: 'examResult', component: () => import('@/views/front/ExamResult.vue'),  },
        { path: 'myScores', component: () => import('@/views/front/MyScores.vue'),  },
        { path: 'tutorials', component: () => import('@/views/front/TutorialList.vue'),  },
        { path: 'tutorialDetail', component: () => import('@/views/front/TutorialDetail.vue'),  },
        { path: 'forum', component: () => import('@/views/front/ForumList.vue'),  },
        { path: 'forumDetail', component: () => import('@/views/front/ForumDetail.vue'),  },
        { path: 'forumEdit', component: () => import('@/views/front/ForumEdit.vue'),  },
        { path: 'practiceMode', component: () => import('@/views/front/PracticeMode.vue'),  },
        { path: 'wrongQuestions', component: () => import('@/views/front/WrongQuestions.vue'),  },
        { path: 'dashboard', component: () => import('@/views/front/LearningDashboard.vue'),  },
        { path: 'favorites', component: () => import('@/views/front/Favorites.vue'),  },
        { path: 'leaderboard', component: () => import('@/views/front/Leaderboard.vue'),  },
        { path: 'achievements', component: () => import('@/views/front/Achievements.vue'),  },
        { path: 'contributeQuestion', component: () => import('@/views/front/ContributeQuestion.vue'),  }
      ]
    },
    {
      path: '/exam',
      component: () => import('@/views/exam/ExamLayout.vue'),
      redirect: '/exam/dashboard',
      children: [
        { path: 'dashboard', meta: { name: '工作台' }, component: () => import('@/views/exam/Dashboard.vue') },
        { path: 'examManage', meta: { name: '考试管理' }, component: () => import('@/views/exam/ExamManage.vue') },
        { path: 'takeExam', meta: { name: '参加考试' }, component: () => import('@/views/exam/TakeExam.vue') },
        { path: 'gradingCenter', meta: { name: '阅卷中心' }, component: () => import('@/views/exam/GradingCenter.vue') },
        { path: 'approvalCenter', meta: { name: '审批中心' }, component: () => import('@/views/exam/ApprovalCenter.vue') },
        { path: 'resultsCenter', meta: { name: '成绩公示' }, component: () => import('@/views/exam/ResultsCenter.vue') },
        { path: 'announcements', meta: { name: '考试公告' }, component: () => import('@/views/exam/Announcements.vue') },
        { path: 'paperManage', meta: { name: '试卷管理' }, component: () => import('@/views/manager/ExamPaper.vue') },
        { path: 'examAdmin', meta: { name: '考试管理' }, component: () => import('@/views/manager/Exam.vue') },
        { path: 'scoreManage', meta: { name: '成绩管理' }, component: () => import('@/views/manager/Score.vue') },
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') },
    { path: '/404', component: () => import('@/views/404.vue') },
    { path: '/:pathMatch(.*)', redirect: '/404' }
  ]
})

export default router
