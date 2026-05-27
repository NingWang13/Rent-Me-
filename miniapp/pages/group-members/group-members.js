const app = getApp();

Page({
  data: {
    groupId: '',
    groupName: '群聊',
    currentUserId: '',
    members: [],
    isOwner: false,
    isAdmin: false
  },

  onLoad(options) {
    if (options.groupId) {
      this.setData({ 
        groupId: options.groupId,
        currentUserId: app.globalData.userId
      });
      this.loadMembers();
    }
  },

  loadMembers() {
    wx.showLoading({ title: '加载中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/group/${this.data.groupId}/members`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const members = res.data.data;
          const currentMember = members.find(m => m.userId === this.data.currentUserId);
          this.setData({
            members: members.map(m => ({
              id: m.userId,
              nickname: m.nickname,
              role: m.role,
              avatarColor: `linear-gradient(135deg, ${this.getRandomColor()} 0%, ${this.getRandomColor()} 100%)`
            })),
            isOwner: currentMember?.role === 1,
            isAdmin: currentMember?.role === 2
          });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  onInviteMember() {
    wx.showToast({ title: '邀请功能开发中', icon: 'none' });
  },

  onRemoveMember(e) {
    const memberId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认移除',
      content: '确定要移除该成员吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });
          wx.request({
            url: `${app.globalData.apiBaseUrl}/group/${this.data.groupId}/members/${memberId}`,
            method: 'DELETE',
            header: { 'Authorization': `Bearer ${app.globalData.token}` },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已移除', icon: 'success' });
                this.loadMembers();
              } else {
                wx.showToast({ title: res.data.message || '操作失败', icon: 'none' });
              }
            },
            complete: () => wx.hideLoading()
          });
        }
      }
    });
  },

  getRandomColor() {
    const colors = ['#FF6B6B', '#845EF7', '#38D9A9', '#4DABF7', '#FFA94D', '#F06595', '#A78BFA', '#20C997'];
    return colors[Math.floor(Math.random() * colors.length)];
  }
});
