<template>
  <div style="position: relative;">
    <video id="localVideo" width="80%" autoplay="autoplay"></video>
    <div style="position: absolute;width:19%;display: inline-block;">
      <video id="localCamera" width="100%" autoplay="autoplay" style="display: none;"></video>
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>当前在线用户</span>
        </div>
        <div v-for="user in roomUsers" :key="user.id">
          {{ user.name }}
          <el-tooltip class="item" effect="dark" content="和他私聊" placement="right">
            <el-button icon="el-icon-s-comment" @click="drawer=true" style="padding:0"></el-button>
          </el-tooltip>
        </div>
      </el-card>
    </div>
    <!--暂时无法解决动态开启摄像头和声音，暂时屏蔽-->
    <div style="position: fixed;right: 1%;bottom: 10%">
      <button v-if="openCameraFlag" @click="cameraOffer">开启摄像头和声音</button>
      <button v-if="!openCameraFlag" @click="cameraClose">关闭摄像头和声音</button>
    </div>
    <div>
      <el-drawer
        title="聊天窗口"
        :visible.sync="drawer"
        @open="drawOpen"
        size="20%">
        <span>我来啦!</span>
      </el-drawer>
    </div>
  </div>
</template>

<script>
import {MessageType} from '../assets/MessageType.js'

export default {
  name: "Share",
  data() {
    return {
      drawer: false,
      websocket: null,
      configuration: {
        iceServers: [{
          'urls': 'stun:stun.l.google.com:19302'
        }]
      },
      cameraConstraints: {
        audio: false,
        video: true
      },
      screenConstraints: {
        video: {
          cursor: 'always' | 'motion' | 'never',
          displaySurface: 'application' | 'browser' | 'monitor' | 'window'
        }
      },
      cameraStream: null,
      screenStream: null,
      //参考https://developer.mozilla.org/zh-CN/docs/Web/API/RTCPeerConnection/createOffer#rtcofferoptions_dictionary
      offerOptions: {
        iceRestart: true,
        offerToReceiveAudio: false,
        offerToReceiveVideo: true
      },
      roomId: null,
      username: null,
      peers: [],
      roomUsers: [],
      openCameraFlag: true
    }
  },
  mounted() {
    this.roomId = this.$route.query.roomId
    this.username = this.$route.query.username
    // this.username = this.$route.params.username
    if (!this.roomId || !this.username) {
      window.location.href = '/index'
    }
    this.connSocket()
  },

  methods: {
    connSocket() {
      let url
      let isHttps = 'https:' === document.location.protocol
      if (isHttps) {
        url = 'wss://127.0.0.1:8000/meeting'
      } else {
        url = 'ws://127.0.0.1:8000/meeting'
      }
      this.websocket = new WebSocket(url)
      this.websocket.onopen = () => {
        if (this.roomId) {
          this.messageBox(this.roomId)
        }
      }
      this.websocket.onclose = () => {
        console.log("connection closed")
      }
      this.websocket.onerror = () => {
        console.log("websocket error")
      }
      this.websocket.onmessage = this.handleMessage
    },
    handleMessage(event) {
      const message = JSON.parse(event.data)
      if (message.type === MessageType.CREATE) {
        const ids = message.fromUserId.split(",")
        let sleep = 0
        for (const id of ids) {
          if (id != '') {
            message.fromUserId = id
            let t = this
            setTimeout(() => t.sendOffer(message), 100 * sleep)
            sleep++
          }
        }
      } else if (message.type === MessageType.OFFER) {
        this.sendOffer(message)
      } else if (message.type === MessageType.ANSWER) {
        this.relayAnswer(message)
      } else if (message.type === MessageType.CANDIDATE) {
        this.relayCandidate(message)
      } else if (message.type === MessageType.ALERT) {
        this.alert(message.obj)
      } else if (message.type === MessageType.ROOM_GET_USER) {
        let obj = JSON.parse(message.obj)
        for (const k in obj) {
          const v = obj[k]
          const user = {
            id: k,
            name: v
          }
          this.roomUsers.push(user)
        }
      } else if (message.type === MessageType.ROOM_ADD_USER) {
        let obj = JSON.parse(message.obj)
        for (const k in obj) {
          const v = obj[k]
          const user = {
            id: k,
            name: v
          }
          this.roomUsers.push(user)
        }
      } else if (message.type === MessageType.ROOM_REMOVE_USER) {
        for (let i = 0; i < this.roomUsers.length; i++) {
          if (this.roomUsers[i].id == message.obj) {
            this.roomUsers.splice(i, 1);
          }
        }
      }
    },
    sendMessage(data) {
      this.websocket.send(JSON.stringify(data))
    },
    share() {
      this.createRoom()
      this.openScreenMedia()
    },
    createRoom() {
      if (this.username) {
        let message = {
          type: MessageType.CREATE,
          roomId: this.roomId,
          username: this.username
        }
        this.sendMessage(message)
      } else {
        this.alert('参数不合法，请重新进入')
      }
    },
    createCamera() {
      let message = {
        type: MessageType.CREATE,
        camera: true,
        roomId: this.roomId
      }
      this.sendMessage(message)
    },
    // 打开本地音视频,用promise这样在打开视频成功后，再进行下一步操作
    openScreenMedia() {
      return new Promise((resolve, reject) => {
        this.openScreen()
      })
    },
    openCameraMedia() {
      return new Promise((resolve, reject) => {
        this.openCamera()
        this.openCameraFlag = false
      })
    },
    openCamera() {
      let t = this
      // 摄像头
      navigator.mediaDevices.getUserMedia(this.cameraConstraints)
        .then((stream) => {
          t.cameraStream = stream
          let localVideo = document.getElementById("localCamera")
          localVideo.srcObject = stream
          localVideo.play()
          localVideo.style.display = 'block'
          t.createCamera()
        }).then(() => console.log("打开本地音视频设备成功"))
        .catch(() => console.log("打开本地音视频设备失败"))
    },
    openScreen() {
      let t = this
      // 屏幕共享
      navigator.mediaDevices.getDisplayMedia(this.screenConstraints)
        .then((stream) => {
          t.screenStream = stream
          let localVideo = document.getElementById("localVideo")
          localVideo.srcObject = stream
          localVideo.play()
        }).then(() => console.log("打开本地音视频设备成功"))
        .catch(() => console.log("打开本地音视频设备失败"))
    },
    createRTCPeer(isCamera) {
      let t = this
      const localPeerConnection = new RTCPeerConnection(this.configuration)
      if (isCamera) {
        for (const track of t.cameraStream.getTracks()) {
          localPeerConnection.addTrack(track, t.cameraStream)
        }
      } else {
        for (const track of t.screenStream.getTracks()) {
          localPeerConnection.addTrack(track, t.screenStream)
        }
      }
      return localPeerConnection
    },
    cameraOffer() {
      this.openCameraMedia()
    },
    sendOffer(message) {
      const t = this
      const fromUserId = message.fromUserId
      const isCamera = message.camera
      const localPeerConnection = this.createRTCPeer(isCamera)
      localPeerConnection.onicecandidate = function (e) {
        t.sendCandidate(e, fromUserId, isCamera)
      }
      localPeerConnection.createOffer(this.offerOptions).then(function (sdp) {
        t.sendMessage({
          type: MessageType.ANSWER,
          sdp: sdp,
          roomId: t.roomId,
          fromUserId: fromUserId,
          camera: isCamera
        })
        localPeerConnection.setLocalDescription(sdp)
      }).catch(function (reason) {
        console.log(reason)
      })
      let id = fromUserId
      if (isCamera) {
        id = fromUserId + '_camera'
      }
      let flag = true
      for (const peer of this.peers) {
        if (peer.fromUserId === id) {
          peer.conn = localPeerConnection
          flag = false
        }
      }
      if (flag) {
        this.peers.push({
          fromUserId: id,
          conn: localPeerConnection
        })
      }
      if (!isCamera && this.cameraStream) {
        message.camera = true
        this.sendOffer(message)
      }
    },
    relayAnswer(message) {
      const t = this
      let sdp = JSON.parse(message.sdp);
      let fromUserId = message.fromUserId
      if (message.camera) {
        fromUserId = fromUserId + '_camera'
      }
      for (let peer of this.peers) {
        if (peer.fromUserId === fromUserId) {
          peer.conn.setRemoteDescription(new RTCSessionDescription(sdp))
        }
      }
    },
    sendCandidate(event, fromUserId, isCamera) {
      if (event.candidate) {
        this.sendMessage({
          candidate: event.candidate,
          type: MessageType.CANDIDATE,
          roomId: this.roomId,
          fromUserId: fromUserId,
          camera: isCamera
        })
      }
    },
    relayCandidate(data) {
      let candidate = JSON.parse(data.candidate)
      for (let peer of this.peers) {
        if (peer.fromUserId === data.fromUserId) {
          peer.conn.addIceCandidate(new RTCIceCandidate(candidate)).catch((e) => {
            console.log(e)
          })
        }
      }
    },
    alert(mes) {
      this.$notify.error({
        title: '提示',
        message: mes
      });
    },
    messageBox(roomId) {
      let t = this
      this.$alert(roomId, '房间号', {
        confirmButtonText: '确定',
        callback: action => {
          t.share()
        }
      });
    },
    cameraClose() {
      this.cameraStream.getTracks().forEach(track => track.stop())
      let localVideo = document.getElementById("localCamera")
      localVideo.style.display = 'none'
      this.openCameraFlag = true
    },
    drawOpen() {
      const x = document.getElementsByClassName("el-drawer");
      for (let i = 0; i < x.length; i++) {
        x[i].style.height = "90%"
        x[i].style.top = "5%"
      }
    }
  }
}
</script>

<style scoped>
</style>
