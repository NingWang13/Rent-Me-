const app = getApp();

Page({
  data: {
    order: null,
    loading: false
  },

  onLoad(options) {
    if (options.id) {
      this.loadOrderDetail(options.id);
    }
  },

  loadOrderDetail(id) {
    this.setData({ loading: true });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/${id}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          const order = res.data.data;
          const statusMap = {
            1: { text: '待支付', icon: '💰' },
            2: { text: '已支付', icon: '✅' },
            3: { text: '服务中', icon: '🚀' },
            4: { text: '已完成', icon: '🎉' },
            5: { text: '已取消', icon: '❌' },
            6: { text: '退款中', icon: '🔄' },
            7: { text: '已退款', icon: '💸' }
          };
          order.statusText = statusMap[order.status]?.text || '未知';
          order.statusIcon = statusMap[order.status]?.icon || '📦';
          this.setData({ order });
        }
      },
      complete: () => this.setData({ loading: false })
    });
  },

  onPay() {
    wx.navigateTo({ url: `/pages/payment/payment?orderId=${this.data.order.id}` });
  },

  onConfirmComplete() {
    wx.showModal({
      title: '确认完成',
      content: '确认订单已完成？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });
          wx.request({
            url: `${app.globalData.apiBaseUrl}/order/${this.data.order.id}/complete`,
            method: 'POST',
            header: { 'Authorization': `Bearer ${app.globalData.token}` },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '操作成功', icon: 'success' });
                this.loadOrderDetail(this.data.order.id);
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

  onCancel() {
    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });
          wx.request({
            url: `${app.globalData.apiBaseUrl}/order/${this.data.order.id}/cancel`,
            method: 'POST',
            header: { 'Authorization': `Bearer ${app.globalData.token}` },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '订单已取消', icon: 'success' });
                this.loadOrderDetail(this.data.order.id);
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
