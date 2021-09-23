<template>
  <div>
    <el-input v-model="username" placeholder="请输入用户名"></el-input>
    <el-button type="primary" @click="goShare">创建房间</el-button>
    <el-input v-model="roomId" placeholder="请输入房间号"></el-input>
    <el-button type="primary" @click="goRoom">加入房间</el-button>
  </div>
</template>

<script>

export default {
  name: "Index",
  data() {
    return {
      username: null,
      roomId: null,
    }
  },
  methods: {
    goShare() {
      if (this.username) {
        this.$router.push({
          path: "/share",
          name: 'share',
          query: {roomId: this.mathRand(), username: this.username},
          // params: {username: this.username}
        })
      } else {
        this.alert('请填写用户名')
      }
    },
    goRoom() {
      if (this.roomId && this.username) {
        this.$router.push({
          path: '/room',
          name: 'room',
          query: {roomId: this.roomId, username: this.username},
          // params: {username: this.username}
        })
      } else {
        this.alert('参数不合法')
      }
    },
    mathRand() {
      let num = "";
      for (let i = 0; i < 6; i++) {
        num += Math.floor(Math.random() * 10);
      }
      return num
    },
    alert(mes) {
      this.$notify.error({
        title: '提示',
        message: mes,
      });
    },
  }
}
</script>

<style scoped>

</style>
