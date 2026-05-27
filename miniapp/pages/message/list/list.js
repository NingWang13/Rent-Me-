const app = getApp();

Page({
  data: {
    activeTab: 0,
    tabs: [
      { name: '全部', type: null },
      { name: '订单', type: 2 },
      { name: '支付', type: 3 },
      { name: '积分', type: 4 }
    ],
    messages: [],
    unreadCount: 0,
    page: 1,
    size: 20,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadUnreadCount();
    this.loadMessages();
  },

  onShow() {
    this.loadUnreadCount();
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMoreMessages();
    }
  },

  loadUnreadCount() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/unread-count`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            unreadCount: res.data.data.count
          });
          if (res.data.data.count > 0) {
            wx.setTabBarBadge({
              index: 2,
              text: res.data.data.count.toString()
            });
          } else {
            wx.removeTabBarBadge({ index: 2 });
          }
        }
      }
    });
  },

  loadMessages() {
    this.setData({ loading: true, page: 1 });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/list`,
      method: 'GET',
      data: {
        type: this.data.tabs[this.data.activeTab].type,
        page: 1,
        size: this.data.size
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            messages: res.data.data,
            hasMore: res.data.data.length >= this.data.size
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  loadMoreMessages() {
    if (this.data.loading || !this.data.hasMore) return;

    this.setData({ loading: true });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/list`,
      method: 'GET',
      data: {
        type: this.data.tabs[this.data.activeTab].type,
        page: this.data.page + 1,
        size: this.data.size
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const newMessages = res.data.data;
          this.setData({
            messages: [...this.data.messages, ...newMessages],
            page: this.data.page + 1,
            hasMore: newMessages.length >= this.data.size
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      activeTab: index,
      messages: [],
      page: 1,
      hasMore: true
    });
    this.loadMessages();
  },

  onMessageTap(e) {
    const messageId = e.currentTarget.dataset.id;
    const type = e.currentTarget.dataset.type;
    const relatedId = e.currentTarget.dataset.related;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/${messageId}/read`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      }
    });

    if (type === 2) {
      wx.navigateTo({
        url: `/pages/order/detail/detail?id=${relatedId}`
      });
    } else if (type === 3) {
      wx.navigateTo({
        url: `/pages/wallet/detail/detail?id=${relatedId}`
      });
    }
  },

  markAllAsRead() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/read-all`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '已标记全部已读',
            icon: 'success'
          });
          this.loadMessages();
          this.loadUnreadCount();
        }
      }
    });
  }
});
