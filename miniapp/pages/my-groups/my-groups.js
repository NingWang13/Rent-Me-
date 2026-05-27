const app = getApp();

Page({
  data: {
    groups: [],
    loading: false,
    page: 1,
    size: 20,
    hasMore: true,
    showMenuModal: false,
    currentGroupId: null,
    currentGroupName: '',
    currentRole: 3
  },

  onLoad() {
    this.loadGroups();
  },

  onShow() {
    this.loadGroups();
  },

  onPullDownRefresh() {
    this.loadGroups();
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore();
    }
  },

  loadGroups() {
    this.setData({ loading: true });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/group/list`,
      method: 'GET',
      data: { page: 1, size: this.data.size },
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const groups = res.data.data.list.map(item => ({
            ...item,
            avatarColor: this.getRandomColor(),
            joinTime: item.joinTime ? item.joinTime.split(' ')[0] : ''
          }));
          this.setData({
            groups,
            hasMore: res.data.data.hasMore,
            page: 1
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
        wx.stopPullDownRefresh();
      }
    });
  },

  loadMore() {
    this.setData({ loading: true, page: this.data.page + 1 });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/group/list`,
      method: 'GET',
      data: { page: this.data.page, size: this.data.size },
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const newGroups = res.data.data.list.map(item => ({
            ...item,
            avatarColor: this.getRandomColor(),
            joinTime: item.joinTime ? item.joinTime.split(' ')[0] : ''
          }));
          this.setData({
            groups: [...this.data.groups, ...newGroups],
            hasMore: res.data.data.hasMore
          });
        }
      },
      complete: () => this.setData({ loading: false })
    });
  },

  onEnterGroup(e) {
    const groupId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/group-chat/group-chat?groupId=${groupId}` });
  },

  onShowGroupMenu(e) {
    const { id, name, role } = e.currentTarget.dataset;
    this.setData({
      showMenuModal: true,
      currentGroupId: id,
      currentGroupName: name,
      currentRole: role
    });
  },

  onCloseMenuModal() {
    this.setData({ showMenuModal: false });
  },

  onViewMembers() {
    const groupId = this.data.currentGroupId;
    wx.navigateTo({ url: `/pages/group-members/group-members?groupId=${groupId}` });
    this.onCloseMenuModal();
  },

  onEditGroupInfo() {
    wx.showToast({ title: '功能开发中', icon: 'none' });
    this.onCloseMenuModal();
  },

  onInviteMember() {
    wx.showToast({ title: '功能开发中', icon: 'none' });
    this.onCloseMenuModal();
  },

  onQuitGroup() {
    const groupId = this.data.currentGroupId;
    wx.showModal({
      title: '提示',
      content: '确定要退出该群聊吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '退出中...' });
          wx.request({
            url: `${app.globalData.apiBaseUrl}/group/${groupId}/quit`,
            method: 'POST',
            header: { 'Authorization': `Bearer ${app.globalData.token}` },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已退出群聊', icon: 'success' });
                this.loadGroups();
              } else {
                wx.showToast({ title: res.data.message || '操作失败', icon: 'none' });
              }
            },
            complete: () => wx.hideLoading()
          });
        }
      }
    });
    this.onCloseMenuModal();
  },

  onDismissGroup() {
    const groupId = this.data.currentGroupId;
    wx.showModal({
      title: '提示',
      content: '确定要解散该群聊吗？此操作不可恢复',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '解散中...' });
          wx.request({
            url: `${app.globalData.apiBaseUrl}/group/${groupId}/dismiss`,
            method: 'POST',
            header: { 'Authorization': `Bearer ${app.globalData.token}` },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '群聊已解散', icon: 'success' });
                this.loadGroups();
              } else {
                wx.showToast({ title: res.data.message || '操作失败', icon: 'none' });
              }
            },
            complete: () => wx.hideLoading()
          });
        }
      }
    });
    this.onCloseMenuModal();
  },

  goToCreateGroup() {
    wx.navigateTo({ url: '/pages/create-group/create-group' });
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
  },

  stopPropagation() {}
});
