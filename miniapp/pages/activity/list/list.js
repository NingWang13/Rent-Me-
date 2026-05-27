const app = getApp();

Page({
  data: {
    activities: [],
    loading: false
  },

  onLoad() {
    this.loadActivities();
  },

  loadActivities() {
    this.setData({ loading: true });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/activity/list`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ activities: res.data.data || [] });
        }
      },
      complete: () => this.setData({ loading: false })
    });
  },

  onActivityTap(e) {
    const activityId = e.currentTarget.dataset.id;
    wx.showToast({ title: '活动详情页面开发中', icon: 'none' });
  }
});
