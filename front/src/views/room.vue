<template>
  <div style="position: relative;">
    <video id="screen" width="80%" autoplay="autoplay"></video>
    <div style="position: absolute;width:19%;display: inline-block;">
      <video id="camera" width="100%" autoplay="autoplay" style="display: none;"></video>
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>当前在线用户</span>
        </div>
        <div v-for="user in roomUsers" :key="user.id">
          {{ user.name }}
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>

import {MessageType} from "../assets/MessageType";

export default {
  name: "Room",
  data() {
    return {
      websocket: null,
      localPeerConnection: null,
      configuration: {
        iceServers: [{
          'urls': 'stun:stun.l.google.com:19302'
        }]
      },
      roomId: null,
      username: null,
      //参考https://developer.mozilla.org/zh-CN/docs/Web/API/RTCPeerConnection/createOffer#rtcofferoptions_dictionary
      offerOptions: {
        iceRestart: false,
        offerToReceiveAudio: false,
        offerToReceiveVideo: true
      },
      isCameraStream: false,
      peers: {
        camera: null,
        screen: null
      },
      roomUsers: []
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
        this.getOffer()
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
      let message = JSON.parse(event.data)
      if (message.type === MessageType.ANSWER) {
        this.relayAnswer(message)
      } else if (message.type === MessageType.CANDIDATE) {
        this.relayCandidate(message)
      } else if (message.type === MessageType.ALERT) {
        if (message.obj === 'close') {
          this.alert('房主关闭了共享')
          this.peers.screen.close()
          if (this.peers.camera) {
            this.peers.camera.close()
          }
          setTimeout(window.location.href = '/index', 5000)
        } else if (message.obj === 'error room') {
          this.alert('房间号错误')
          setTimeout(window.location.href = '/index', 5000)
        }
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
    sendMessage(message) {
      this.websocket.send(JSON.stringify(message))
    },
    initRTCPeer(fromUserId) {
      let t = this
      const localPeerConnection = new RTCPeerConnection(this.configuration)
      localPeerConnection.onicecandidate = function (e) {
        t.sendCandidate(e, fromUserId)
      }
      localPeerConnection.ontrack = function (event) {
        let remoteStream = event.streams[0];
        if (remoteStream) {
          if (!t.isCameraStream) {
            let screenStream = document.getElementById("screen");
            screenStream.srcObject = remoteStream;
            screenStream.play().catch(function (e) {
              alert('进入房间异常')
              setTimeout(window.location.href = '/index', 5000)
            })
            t.isCameraStream = true
          } else {
            let cameraStream = document.getElementById("camera");
            cameraStream.srcObject = remoteStream
            setTimeout(()=>{
              cameraStream.play().then(function (e) {
                cameraStream.style.display = 'block'
              })
            },150)
          }
        }
      }
      return localPeerConnection
    },
    sendAnswer(localPeerConnection, isCamera) {
      let t = this
      localPeerConnection.createAnswer().then(function (sdp) {
        localPeerConnection.setLocalDescription(sdp)
        t.sendMessage({
          sdp: sdp,
          type: MessageType.ANSWER,
          roomId: t.roomId,
          camera: isCamera
        })
      }).catch(function (reason) {
        console.log(reason)
      })
    },
    relayAnswer(message) {
      const localPeerConnection = this.initRTCPeer(message.fromUserId)
      let sdp = JSON.parse(message.sdp)
      localPeerConnection.setRemoteDescription(new RTCSessionDescription(sdp))
      if (message.camera) {
        this.peers.camera = localPeerConnection
      } else {
        this.peers.screen = localPeerConnection
      }
      this.sendAnswer(localPeerConnection, message.camera)
    },
    sendCandidate(event,fromUserId) {
      if (event.candidate) {
        this.sendMessage({
          candidate: event.candidate,
          type: MessageType.CANDIDATE,
          roomId: this.roomId,
          fromUserId: fromUserId
        })
      }
    },
    relayCandidate(data) {
      if (data.candidate) {
        let candidate = JSON.parse(data.candidate)
        let localPeerConnection
        if (data.camera) {
          localPeerConnection = this.peers.camera
        } else {
          localPeerConnection = this.peers.screen
        }
        localPeerConnection.addIceCandidate(new RTCIceCandidate(candidate)).catch((e) => {
          console.log(e)
        })
      }
    },
    getOffer() {
      let message = {
        type: 'CONNECT',
        roomId: this.roomId,
        username: this.username
      }
      this.sendMessage(message)
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
