const app = getApp();

Page({
  data: {
    searchKeyword: '',
    searchResults: [],
    pendingRequests: [],
    pendingCount: 0,
    friends: [],
    showAddModal: false,
    targetUserId: null,
    applyMessage: ''
  },

  onLoad() {
    this.loadPendingRequests();
    this.loadFriends();
  },

  onShow() {
    this.loadPendingRequests();
    this.loadFriends();
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  onSearch() {
    const keyword = this.data.searchKeyword.trim();
    if (!keyword) {
      wx.showToast({ title: '请输入搜索内容', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '搜索中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/search`,
      method: 'GET',
      data: { keyword },
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const results = res.data.data.map(item => ({
            ...item,
            avatarColor: this.getRandomColor(),
            isFriend: item.isFriend || false
          }));
          this.setData({ searchResults: results });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  onScanQRCode() {
    wx.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode'],
      success: (res) => {
        const userId = res.result;
        if (userId) {
          this.setData({ targetUserId: parseInt(userId), showAddModal: true });
        }
      }
    });
  },

  onImportContacts() {
    wx.showToast({ title: '功能开发中', icon: 'none' });
  },

  onShareInvite() {
    wx.showShareMenu({ withShareTicket: true });
    wx.showToast({ title: '请点击右上角分享', icon: 'none' });
  },

  onAddFriend(e) {
    const userId = e.currentTarget.dataset.id;
    this.setData({ targetUserId: userId, applyMessage: '', showAddModal: true });
  },

  onApplyMessageInput(e) {
    this.setData({ applyMessage: e.detail.value });
  },

  onCloseModal() {
    this.setData({ showAddModal: false, targetUserId: null, applyMessage: '' });
  },

  onConfirmAdd() {
    const { targetUserId, applyMessage } = this.data;
    if (!targetUserId) return;

    wx.showLoading({ title: '发送中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/friend/add`,
      method: 'POST',
      data: { friendId: targetUserId, applyMessage },
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '好友申请已发送', icon: 'success' });
          this.onCloseModal();
          this.loadFriends();
        } else {
          wx.showToast({ title: res.data.message || '发送失败', icon: 'none' });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  loadPendingRequests() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/friend/pending`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const requests = res.data.data.list.map(item => ({
            ...item,
            avatarColor: this.getRandomColor()
          }));
          this.setData({
            pendingRequests: requests,
            pendingCount: res.data.data.total
          });
        }
      }
    });
  },

  loadFriends() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/friend/list`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const friends = res.data.data.list.map(item => ({
            ...item,
            avatarColor: this.getRandomColor()
          }));
          this.setData({ friends });
        }
      }
    });
  },

  onAcceptRequest(e) {
    const friendId = e.currentTarget.dataset.id;
    wx.showLoading({ title: '处理中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/friend/${friendId}/accept`,
      method: 'POST',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '已同意', icon: 'success' });
          this.loadPendingRequests();
          this.loadFriends();
        } else {
          wx.showToast({ title: res.data.message || '操作失败', icon: 'none' });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  onRejectRequest(e) {
    const friendId = e.currentTarget.dataset.id;
    wx.showLoading({ title: '处理中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/friend/${friendId}/reject`,
      method: 'POST',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '已拒绝', icon: 'success' });
          this.loadPendingRequests();
        } else {
          wx.showToast({ title: res.data.message || '操作失败', icon: 'none' });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  onChat(e) {
    const friendId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/chat/chat?friendId=${friendId}` });
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
