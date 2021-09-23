import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      component: () => import("@/views/index")
    },
    {
      path: '/index',
      component: () => import("@/views/index")
    },
    {
      path: '/room',
      name: 'room',
      component: () => import("@/views/room")
    },
    {
      path: '/share',
      name: 'share',
      component: () => import("@/views/share")
    },
    {
      path: '/chat',
      component: () => import("@/views/chat")
    }
  ]
})
