const app = getApp();

Page({
  data: {
    wish: null,
    loading: false,
    canClaim: false,
    isOwner: false
  },

  onLoad(options) {
    if (options.id) {
      this.loadWishDetail(options.id);
    }
  },

  loadWishDetail(id) {
    this.setData({ loading: true });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/wish/${id}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const wish = res.data.data;
          const currentUserId = app.globalData.userId;
          const categoryNames = { 1: '学习类', 2: '陪伴类', 3: '协助类', 4: '圆梦类', 5: '技能类' };
          wish.categoryName = categoryNames[wish.category] || '未知';
          
          this.setData({
            wish,
            canClaim: wish.status === 1 && wish.userId !== currentUserId,
            isOwner: wish.userId === currentUserId
          });
        }
      },
      complete: () => this.setData({ loading: false })
    });
  },

  onClaimWish() {
    wx.showModal({
      title: '确认认领',
      content: '确定要认领这个心愿吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '认领中...' });
          wx.request({
            url: `${app.globalData.apiBaseUrl}/wish/${this.data.wish.id}/claim`,
            method: 'POST',
            header: { 'Authorization': `Bearer ${app.globalData.token}` },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '认领成功', icon: 'success' });
                this.loadWishDetail(this.data.wish.id);
              } else {
                wx.showToast({ title: res.data.message || '认领失败', icon: 'none' });
              }
            },
            complete: () => wx.hideLoading()
          });
        }
      }
    });
  },

  onCancelWish() {
    wx.showModal({
      title: '确认取消',
      content: '确定要取消这个心愿吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '取消中...' });
          wx.request({
            url: `${app.globalData.apiBaseUrl}/wish/${this.data.wish.id}/cancel`,
            method: 'POST',
            header: { 'Authorization': `Bearer ${app.globalData.token}` },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已取消', icon: 'success' });
                setTimeout(() => wx.navigateBack(), 1500);
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

  goBack() {
    wx.navigateBack();
  }
});
