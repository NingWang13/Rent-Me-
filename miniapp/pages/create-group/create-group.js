const app = getApp();

Page({
  data: {
    groupName: '',
    description: '',
    chatType: 1,
    joinType: 1,
    friends: [],
    selectedMembers: []
  },

  onLoad() {
    this.loadFriends();
  },

  onGroupNameInput(e) {
    this.setData({ groupName: e.detail.value });
  },

  onDescriptionInput(e) {
    this.setData({ description: e.detail.value });
  },

  onSelectChatType(e) {
    this.setData({ chatType: parseInt(e.currentTarget.dataset.type) });
  },

  onSelectJoinType(e) {
    this.setData({ joinType: parseInt(e.currentTarget.dataset.type) });
  },

  loadFriends() {
    wx.showLoading({ title: '加载中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/friend/list`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const friends = res.data.data.list.map(item => ({
            ...item,
            selected: false,
            avatarColor: this.getRandomColor()
          }));
          this.setData({ friends });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  onSelectMember(e) {
    const friendId = e.currentTarget.dataset.id;
    const friends = this.data.friends.map(item => {
      if (item.friendId === friendId) {
        return { ...item, selected: !item.selected };
      }
      return item;
    });

    const selectedMembers = friends.filter(item => item.selected).map(item => item.friendId);

    this.setData({ friends, selectedMembers });
  },

  onCreateGroup() {
    const { groupName, description, chatType, joinType, selectedMembers } = this.data;

    if (!groupName.trim()) {
      wx.showToast({ title: '请输入群聊名称', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '创建中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/group/create`,
      method: 'POST',
      data: { name: groupName, description, chatType, joinType },
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const groupId = res.data.data.id;
          if (selectedMembers.length > 0) {
            this.inviteMembers(groupId, selectedMembers);
          } else {
            wx.showToast({ title: '群聊创建成功', icon: 'success' });
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          }
        } else {
          wx.showToast({ title: res.data.message || '创建失败', icon: 'none' });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  inviteMembers(groupId, memberIds) {
    let successCount = 0;
    let failCount = 0;

    const promises = memberIds.map(memberId => {
      return new Promise((resolve) => {
        wx.request({
          url: `${app.globalData.apiBaseUrl}/group/${groupId}/invite`,
          method: 'POST',
          data: { userId: memberId },
          header: { 'Authorization': `Bearer ${app.globalData.token}` },
          success: (res) => {
            if (res.data.code === 200) {
              successCount++;
            } else {
              failCount++;
            }
            resolve();
          },
          fail: () => {
            failCount++;
            resolve();
          }
        });
      });
    });

    Promise.all(promises).then(() => {
      let message = '群聊创建成功';
      if (successCount > 0) {
        message += `，已邀请${successCount}人`;
      }
      if (failCount > 0) {
        message += `，${failCount}人邀请失败`;
      }
      wx.showToast({ title: message, icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    });
  },

  getRandomColor() {
    const colors = [
      'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)',
      'linear-gradient(135deg, #845EF7 0%, #A78BFA 100%)',
      'linear-gradient(135deg, #4CAF50 0%, #81C784 100%)',
      'linear-gradient(135deg, #2196F3 0%, #64B5F6 100%)',
      'linear-gradient(135deg, #FF9800 0%, #FFB74D 100%)'
    ];
    return colors[Math.floor(Math.random() * colors.length)];
  }
});
