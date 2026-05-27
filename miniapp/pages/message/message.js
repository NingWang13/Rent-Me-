const app = getApp();
const WebSocketService = require('../../utils/websocket');
const NotificationManager = require('../../utils/notification');

Page({
  data: {
    isLogin: false,
    activeTab: 0,
    tabs: [
      { name: '全部', type: null, unreadCount: 0 },
      { name: '订单', type: 2, unreadCount: 0 },
      { name: '支付', type: 3, unreadCount: 0 },
      { name: '积分', type: 4, unreadCount: 0 },
      { name: '系统', type: 1, unreadCount: 0 }
    ],
    messages: [],
    page: 1,
    size: 20,
    hasMore: true,
    loading: false,
    loadingMore: false,
    notificationEnabled: false,
    emptyActions: [
      { text: '去发布心愿', url: '/pages/wish/wish', type: 'primary', openType: 'switchTab', event: 'publishWish' },
      { text: '浏览活动中心', url: '/pages/activity/list/list', type: '', openType: 'navigateTo', event: 'viewActivity' }
    ]
  },

  onLoad() {
    this.checkLoginStatus();
    this.setupWebSocket();
    this.checkNotificationPermission();
  },

  onShow() {
    this.checkLoginStatus();
  },

  onUnload() {
    WebSocketService.offMessage(this.handleNewMessage);
  },

  setupWebSocket() {
    WebSocketService.onMessage(this.handleNewMessage.bind(this));
  },

  handleNewMessage(message) {
    if (message.type === 'PING' || message.type === 'PONG') {
      return;
    }

    const currentTab = this.data.tabs[this.data.activeTab];
    if (currentTab.type === null || message.type === currentTab.type) {
      const newMessage = this.formatSingleMessage(message);
      const messages = [newMessage, ...this.data.messages];
      this.setData({ messages });
    }

    this.loadUnreadCounts();
  },

  formatSingleMessage(message) {
    return {
      ...message,
      timeAgo: '刚刚',
      type: this.getMessageTypeNumber(message.type)
    };
  },

  getMessageTypeNumber(typeStr) {
    const typeMap = {
      'SYSTEM': 1,
      'ORDER': 2,
      'PAYMENT': 3,
      'CREDIT': 4,
      'WISH': 5
    };
    return typeMap[typeStr] || 1;
  },

  async checkNotificationPermission() {
    const enabled = NotificationManager.isNotificationEnabled();
    this.setData({ notificationEnabled: enabled });

    if (!enabled) {
      const granted = await NotificationManager.showPermissionGuide();
      this.setData({ notificationEnabled: granted });
    }
  },

  onPullDownRefresh() {
    this.loadMessages(true);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadMore();
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    this.setData({ isLogin: !!token });
    if (this.data.isLogin) {
      this.loadMessages(true);
      this.loadUnreadCounts();
    }
  },

  loadUnreadCounts() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/unread-counts`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const counts = res.data.data || {};
          const tabs = [...this.data.tabs];
          let totalUnread = 0;
          tabs.forEach((tab, index) => {
            if (tab.type === null) {
              tab.unreadCount = counts.total || 0;
              totalUnread = counts.total || 0;
            } else {
              tab.unreadCount = counts[tab.type] || 0;
            }
          });
          this.setData({ tabs });
          if (totalUnread > 0) {
            wx.setTabBarBadge({
              index: 3,
              text: totalUnread > 99 ? '99+' : totalUnread.toString()
            });
          } else {
            wx.removeTabBarBadge({ index: 3 });
          }
        }
      }
    });
  },

  loadMessages(reset = false) {
    if (this.data.loading) return;

    this.setData({
      loading: !reset,
      loadingMore: reset ? false : this.data.loadingMore
    });

    const currentTab = this.data.tabs[this.data.activeTab];
    const page = reset ? 1 : this.data.page;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/list`,
      method: 'GET',
      data: {
        type: currentTab.type,
        page: page,
        size: this.data.size
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const list = res.data.data.list || [];
          const messages = this.formatMessages(reset ? list : [...this.data.messages, ...list]);
          this.setData({
            messages: messages,
            hasMore: list.length >= this.data.size,
            page: page
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      },
      complete: () => {
        this.setData({ loading: false, loadingMore: false });
        wx.stopPullDownRefresh();
      }
    });
  },

  formatMessages(messages) {
    return messages.map(msg => {
      const now = Date.now();
      const createTime = new Date(msg.createTime).getTime();
      const diff = now - createTime;
      let timeAgo = '';

      if (diff < 60000) {
        timeAgo = '刚刚';
      } else if (diff < 3600000) {
        timeAgo = Math.floor(diff / 60000) + '分钟前';
      } else if (diff < 86400000) {
        timeAgo = Math.floor(diff / 3600000) + '小时前';
      } else if (diff < 604800000) {
        timeAgo = Math.floor(diff / 86400000) + '天前';
      } else {
        timeAgo = msg.createTime.split(' ')[0];
      }

      return { ...msg, timeAgo };
    });
  },

  loadMore() {
    this.setData({
      loadingMore: true,
      page: this.data.page + 1
    });
    this.loadMessages(false);
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    if (index === this.data.activeTab) return;

    this.setData({
      activeTab: index,
      messages: [],
      page: 1,
      hasMore: true
    });
    this.loadMessages(true);
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login/login'
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    const type = e.currentTarget.dataset.type;

    this.markAsRead(id);

    if (type === 2) {
      wx.navigateTo({
        url: `/pages/order/detail/detail?id=${id}`
      });
    } else if (type === 4) {
      wx.navigateTo({
        url: '/pages/user/credit-records/credit-records'
      });
    } else if (type === 5) {
      wx.navigateTo({
        url: '/pages/wish/detail/detail?id=${id}'
      });
    }
  },

  markAsRead(id) {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/${id}/read`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const messages = this.data.messages.map(msg => {
            if (msg.id === id) {
              return { ...msg, isRead: 1 };
            }
            return msg;
          });
          this.setData({ messages });
          this.loadUnreadCounts();
        }
      }
    });
  },

  markAllAsRead() {
    wx.showModal({
      title: '提示',
      content: '确定将所有消息标记为已读？',
      success: (res) => {
        if (res.confirm) {
          this.requestMarkAllAsRead();
        }
      }
    });
  },

  requestMarkAllAsRead() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/read-all`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '操作成功',
            icon: 'success'
          });
          const messages = this.data.messages.map(msg => ({ ...msg, isRead: 1 }));
          this.setData({ messages });
          this.loadUnreadCounts();
        }
      }
    });
  },

  clearAllMessages() {
    wx.showModal({
      title: '提示',
      content: '确定要清空所有消息吗？此操作不可恢复',
      success: (res) => {
        if (res.confirm) {
          this.requestClearAllMessages();
        }
      }
    });
  },

  requestClearAllMessages() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/message/clear-all`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '清空成功',
            icon: 'success'
          });
          this.setData({ messages: [] });
          this.loadUnreadCounts();
        }
      }
    });
  },

  goToAddFriend() {
    wx.navigateTo({ url: '/pages/add-friend/add-friend' });
  },

  goToCreateGroup() {
    wx.navigateTo({ url: '/pages/create-group/create-group' });
  },

  goToMyGroups() {
    wx.navigateTo({ url: '/pages/my-groups/my-groups' });
  },

  onEmptyAction(e) {
    const action = e.detail;
    if (action.event === 'publishWish') {
      wx.switchTab({ url: '/pages/wish/wish' });
    } else if (action.event === 'viewActivity') {
      wx.navigateTo({ url: '/pages/activity/list/list' });
    }
  }
});