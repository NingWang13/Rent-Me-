const app = getApp();

Page({
  data: {
    groupId: '',
    groupName: '群聊',
    currentUserId: '',
    messages: [],
    inputValue: '',
    scrollToViewId: ''
  },

  onLoad(options) {
    if (options.groupId) {
      this.setData({ 
        groupId: options.groupId,
        currentUserId: app.globalData.userId
      });
      this.loadGroupInfo();
      this.loadMessages();
    }
  },

  loadGroupInfo() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/group/${this.data.groupId}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ groupName: res.data.data.name || '群聊' });
        }
      }
    });
  },

  loadMessages() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/group/${this.data.groupId}/messages`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const messages = res.data.data.map(msg => ({
            ...msg,
            time: this.formatTime(msg.createTime)
          }));
          this.setData({ messages });
          this.scrollToBottom();
        }
      }
    });
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value });
  },

  onSend() {
    const content = this.data.inputValue.trim();
    if (!content) return;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/group/${this.data.groupId}/send`,
      method: 'POST',
      header: { 
        'Authorization': `Bearer ${app.globalData.token}`,
        'Content-Type': 'application/json'
      },
      data: {
        content: content,
        type: 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          const newMsg = {
            id: Date.now(),
            senderId: this.data.currentUserId,
            senderName: '我',
            content: content,
            time: this.formatTime(new Date())
          };
          const messages = [...this.data.messages, newMsg];
          this.setData({ 
            messages,
            inputValue: ''
          });
          this.scrollToBottom();
        }
      }
    });
  },

  scrollToBottom() {
    const lastMsg = this.data.messages[this.data.messages.length - 1];
    if (lastMsg) {
      this.setData({ scrollToViewId: `msg-${lastMsg.id}` });
    }
  },

  formatTime(date) {
    const d = new Date(date);
    const hours = d.getHours().toString().padStart(2, '0');
    const minutes = d.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }
});
